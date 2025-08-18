import http from 'k6/http';
import { check } from 'k6';
import { Rate, Trend } from 'k6/metrics';

/**
 * ENV (필수 최소)
 * BASE_URL=http://localhost:8080
 * TESTID=brand_like_A  (A=무인덱스, B=단일, C=복합 등 자유)
 *
 * 선택
 * SIZE=20
 * BRANDS=1,2,3,4,5        (미지정 시 1..5)
 * PAGES=0,1,2,3           (미지정 시 0..3)
 * ORDER=LIKE_DESC         (혹은 PRICE_DESC 등)
 * RATE=100                steady 부하 iters/s
 * SPIKE=400               spike 부하 iters/s
 * DEBUG=1                 실패 시 본문 로그
 */

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const TESTID = __ENV.TESTID || 'brand_like_local';
const SIZE = Number(__ENV.SIZE || 20);
const ORDER = __ENV.ORDER || 'LIKE_DESC';
const RATE = Number(__ENV.RATE || 100);
const SPIKE = Number(__ENV.SPIKE || 400);

// BRANDS/PAGES를 지정 안 하면 기본 값(1..5, 0..3) 사용
const BRANDS = (__ENV.BRANDS || '1,2,3,4,5').split(',').map(Number);
const PAGES = (__ENV.PAGES || '0,1,2,3').split(',').map(Number);

export const slow_req = new Rate('slow_req');          // >200ms 비율
export const sort_violations = new Rate('sort_bad');   // 정렬 위반율
export const page_mismatch = new Rate('page_bad');     // 페이지 크기 위반율
export const dur = new Trend('brand_like_duration');   // 응답 시간 트렌드

// ──────────────────────────────────────────────────────────────────────────────
// 시나리오: cold → steady → spike
//   - cold  : 캐시 전 상태 측정 (낮은 RPS, 짧게)
//   - steady: 안정 구간 (기본 RATE, 2분)
//   - spike : 급등 부하 (SPIKE, 20초)
// 태그: phase 와 testid 로 구분해 대시보드에서 케이스별 비교 용이
// ──────────────────────────────────────────────────────────────────────────────
export const options = {
    scenarios: {
        cold: {
            executor: 'constant-arrival-rate',
            rate: Math.max(5, Math.floor(RATE / 10)), // 낮은 RPS
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 30,
            maxVUs: 60,
            exec: 'run',
            tags: { phase: 'cold', ep: 'brand_like', testid: TESTID },
            gracefulStop: '10s',
        },
        steady: {
            executor: 'constant-arrival-rate',
            rate: RATE,
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: Math.max(150, RATE * 2),
            maxVUs: Math.max(300, RATE * 3),
            exec: 'run',
            startTime: '30s',
            tags: { phase: 'steady', ep: 'brand_like', testid: TESTID },
            gracefulStop: '30s',
        },
        spike: {
            executor: 'constant-arrival-rate',
            rate: SPIKE,
            timeUnit: '1s',
            duration: '20s',
            preAllocatedVUs: Math.max(200, SPIKE * 2),
            maxVUs: Math.max(400, SPIKE * 3),
            exec: 'run',
            startTime: '2m30s',
            tags: { phase: 'spike', ep: 'brand_like', testid: TESTID },
            gracefulStop: '30s',
        },
    },
    thresholds: {
        // 단계별 p95 확인 (대시보드 분리 가능)
        'http_req_failed{ep:brand_like}': ['rate<0.01'],
        'http_req_duration{ep:brand_like,phase:cold}':   ['p(95)<800'], // cold는 여유
        'http_req_duration{ep:brand_like,phase:steady}': ['p(95)<500'],
        'http_req_duration{ep:brand_like,phase:spike}':  ['p(95)<700'],

        // 정합: 정렬 위반/페이지 사이즈 위반은 0 이어야 함
        sort_bad: ['rate==0'],
        page_bad: ['rate==0'],

        // 참조용 지표 (느린요청 비율): 엄격히 보진 않되 감시
        slow_req: ['rate<0.30'],
    },
    summaryTrendStats: ['avg','p(90)','p(95)','p(99)'],
};

// ──────────────────────────────────────────────────────────────────────────────
// 유틸
// ──────────────────────────────────────────────────────────────────────────────
function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function callBrandLike(brandId, page) {
    const url = `${BASE}/api/v1/products?page=${page}&size=${SIZE}&brandId=${brandId}&orderTypeRequest=${ORDER}`;
    const res = http.get(url, { tags: { ep: 'brand_like', testid: TESTID } });

    const ok = check(res, {
        '2xx': (r) => r.status >= 200 && r.status < 300,
        'JSON': (r) => (r.headers['Content-Type'] || '').includes('application/json'),
    });

    // 정합 체크 (페이지 사이즈, 정렬 검증)
    try {
        const j = res.json();
        const list = j.content || j.data || j.items || [];
        page_mismatch.add(!(Array.isArray(list) && list.length <= SIZE));

        // ORDER = LIKE_DESC 기준 (tie → id DESC)
        if (ORDER === 'LIKE_DESC') {
            let bad = false;
            for (let i = 1; i < list.length; i++) {
                const a = list[i - 1], b = list[i];
                if (a.like_count < b.like_count) { bad = true; break; }
                if (a.like_count === b.like_count && a.id < b.id) { bad = true; break; }
            }
            sort_violations.add(bad);
        }
        // 필요하면 PRICE_DESC 등 추가 검증 분기 가능
    } catch (e) {
        // JSON 실패 시 위 check에서 잡힘
        if (__ENV.DEBUG === '1') console.log(`JSON parse error: ${e}`);
    }

    slow_req.add(res.timings.duration > 200, { ep: 'brand_like' });
    dur.add(res.timings.duration, { ep: 'brand_like', brand: String(brandId) });

    if (__ENV.DEBUG === '1' && !ok) {
        console.log(`DEBUG status=${res.status} url=${url} body=${String(res.body).slice(0, 300)}`);
    }
    return res;
}

// ──────────────────────────────────────────────────────────────────────────────
// VU entry
// ──────────────────────────────────────────────────────────────────────────────
export function run() {
    const bid = pick(BRANDS);
    const page = pick(PAGES);
    callBrandLike(bid, page);
}
