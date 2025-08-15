import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 느린 요청 비율(임계 200ms)
export const slow_req = new Rate('slow_req');

export const options = {
    scenarios: {
        // 짧게 상태 점검
        smoke: {
            executor: 'constant-arrival-rate',
            rate: 20, timeUnit: '1s',
            duration: '1m',
            preAllocatedVUs: 50, maxVUs: 100,
        },
        // 한계치 근처까지 천천히 올리기
        ramp_to_break: {
            executor: 'ramping-arrival-rate',
            startRate: 50, timeUnit: '1s',
            stages: [
                { target: 100, duration: '2m' },
                { target: 150, duration: '2m' },
                { target: 200, duration: '2m' },
            ],
            preAllocatedVUs: 200, maxVUs: 800,
            startTime: '1m10s', // smoke 끝나고 시작
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],   // 실패율 < 1%
        http_req_duration: ['p(95)<500'], // p95 < 500ms
        // slow_req: ['rate<0.20'],       // 느린 요청 비율 한도 설정하고 싶으면 주석 해제
    },
};

// 3xx도 실패로 보려면 주석 해제
// http.setResponseCallback(http.expectedStatuses({ min: 200, max: 299 }));

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const API_PATH = __ENV.API_PATH || '/api/v1/products'; // PATH 말고 API_PATH!
const PAGE     = Number(__ENV.PAGE || 0);
const SIZE     = Number(__ENV.SIZE || 20);

// 모든 요청을 동일한 orderTypeRequest로 통일
const ORDER_TYPE_REQUEST = (__ENV.ORDER_TYPE_REQUEST || __ENV.ORDER_TYPE || 'LATEST').trim();

// brandId는 값이 있을 때만 전달 (선택)
const BRAND_ID = __ENV.BRAND_ID ? Number(__ENV.BRAND_ID) : null;

function parse(b){ try { return JSON.parse(b); } catch { return null; } }

export default function () {
    // 페이지 랜덤 접근으로 캐시 편향 줄이기 (원하면 고정 PAGE 쓰기)
    const page = typeof __ENV.RANDOM_PAGE === 'string'
        ? Math.floor(Math.random() * Math.max(1, Number(__ENV.RANDOM_PAGE) || 5))
        : PAGE;

    let qs = `page=${page}&size=${SIZE}&orderTypeRequest=${encodeURIComponent(ORDER_TYPE_REQUEST)}`;
    if (BRAND_ID !== null && !Number.isNaN(BRAND_ID)) qs += `&brandId=${BRAND_ID}`;

    const url = `${BASE_URL}${API_PATH}?${qs}`;

    const headers = { Accept: 'application/json' };
    if (__ENV.X_USER_ID) headers['X-USER-ID'] = __ENV.X_USER_ID;

    const tags = { endpoint: 'products', orderTypeRequest: ORDER_TYPE_REQUEST };
    const res  = http.get(url, { headers, tags });
    const json = parse(res.body);

    // ApiResponse 래핑/비래핑 모두 대응
    const payload = json?.data && (json.data.items || json.data.meta) ? json.data : json;
    const items = payload?.items;
    const meta  = payload?.meta;

    // 처음 몇 회 반복에서 실패 디버그
    if (res.status !== 200 && (__ITER < 3)) {
        console.log(`DEBUG status=${res.status} url=${url}`);
        console.log(`DEBUG body=${String(res.body).slice(0, 400)}`);
    }

    check(res, {
        'status 2xx': (r) => r.status >= 200 && r.status < 300,
        'JSON 파싱됨': () => !!json,
        'meta 존재(있다면 OK)': () => (meta === undefined || meta === null) ? true : !!meta,
        'items 배열(있다면 OK)': () => (items === undefined) ? true : Array.isArray(items),
        'meta.page 일치(있다면 OK)': () => (typeof meta?.page === 'number' ? meta.page === page : true),
        'meta.size 일치(있다면 OK)': () => (typeof meta?.size === 'number' ? meta.size === SIZE : true),
    }, tags);

    // 느린 요청(200ms 기준)
    slow_req.add(res.timings.duration >= 200, tags);

    // 너무 타이트하면 네트워크 과압 → 소량 think time
    sleep(0.1);
}
