import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

// ===== Metrics
export const list_duration = new Trend('list_duration');
export const detail_duration = new Trend('detail_duration');
export const slow_req = new Rate('slow_req');
export const list_calls = new Counter('list_calls');
export const detail_calls = new Counter('detail_calls');

// ===== Env
const BASE_URL   = __ENV.BASE_URL || 'http://localhost:8080';
const PAGE_SIZE  = __ENV.PAGE_SIZE ? parseInt(__ENV.PAGE_SIZE) : 20;
const BRAND_IDS  = (__ENV.BRAND_IDS || '1,2,3,4,5,6,7,8,9,10').trim();

// 캐시 키 깨기(목록 MISS 유도)
const CACHE_BUST = (__ENV.CACHE_BUST || '1') === '1';

// 상세 비율(각 단계에서 목록:상세 = (100-DETAIL_RATIO):DETAIL_RATIO)
const DETAIL_RATIO = __ENV.DETAIL_RATIO ? parseInt(__ENV.DETAIL_RATIO) : 25; // 기본 25%

// 고정 상세 풀(환경변수 or 하드코딩)
const ENV_HOT  = (__ENV.DETAIL_HOT_IDS  || '').split(',').map(s=>s.trim()).filter(Boolean);
const ENV_COLD = (__ENV.DETAIL_COLD_IDS || '').split(',').map(s=>s.trim()).filter(Boolean);
const FIXED_HOT_IDS  = ENV_HOT.map(v => Number.isFinite(+v) ? +v : v);
const FIXED_COLD_IDS = ENV_COLD.map(v => Number.isFinite(+v) ? +v : v);
// 필요하면 여기에 하드코딩:
// const FIXED_HOT_IDS  = [101,102,103,104,105];
// const FIXED_COLD_IDS = [201,202,203,204,205,206];

// ===== Scenarios (MISS → PREWARM → HIT)
export const options = {
    scenarios: {
        miss: {
            executor: 'constant-arrival-rate',
            rate: __ENV.MISS_RATE ? parseInt(__ENV.MISS_RATE) : 350,
            timeUnit: '1s',
            duration: __ENV.MISS_DUR || '120s',
            preAllocatedVUs: __ENV.MISS_VU ? parseInt(__ENV.MISS_VU) : 400,
            maxVUs: __ENV.MISS_VU_MAX ? parseInt(__ENV.MISS_VU_MAX) : 800,
            tags: { phase: 'miss' },
            gracefulStop: '20s',
        },
        prewarm: {
            executor: 'shared-iterations',
            startTime: __ENV.MISS_DUR || '120s',
            vus: 1, iterations: 60, // 상위 페이지+상세 핫 아이디 예열
            tags: { phase: 'prewarm' },
            gracefulStop: '5s',
        },
        hit: {
            executor: 'constant-arrival-rate',
            startTime: (__ENV.MISS_DUR ? parseInt(__ENV.MISS_DUR) : 120) + 5 + 's',
            rate: __ENV.HIT_RATE ? parseInt(__ENV.HIT_RATE) : 350,
            timeUnit: '1s',
            duration: __ENV.HIT_DUR || '120s',
            preAllocatedVUs: __ENV.HIT_VU ? parseInt(__ENV.HIT_VU) : 400,
            maxVUs: __ENV.HIT_VU_MAX ? parseInt(__ENV.HIT_VU_MAX) : 800,
            tags: { phase: 'hit' },
            gracefulStop: '20s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{phase:miss,ep:list}':   ['p(95)<2500'], // BEFORE에선 깨질 수도 있음(의도)
        'http_req_duration{phase:hit,ep:list}':    ['p(95)<150'],
        'http_req_duration{ep:detail}':            ['p(95)<300'],
        slow_req: ['rate<0.20'],
        list_calls: ['count>0'],
        detail_calls: ['count>0'],
    },
};

// 페이지 풀: MISS는 5~20페이지만(캐시 히트 최소화), HIT은 0~2페이지만(히트 극대화)
const MISS_PAGES = Array.from({ length: 16 }, (_, i) => i + 5); // [5..20]
const HIT_PAGES  = [0, 1, 2];

function pick(arr, iter) { return arr[iter % arr.length]; }

// 목록
function listRequest({ page, order, phase, cb }) {
    const tags = { ep: 'list', order, page: String(page), phase };
    let url = `${BASE_URL}/api/v1/products?page=${page}&size=${PAGE_SIZE}&orderTypeRequest=${order}&brandIds=${encodeURIComponent(BRAND_IDS)}`;
    if (cb) url += `&cb=${cb}`;

    const res = http.get(url, { tags });
    list_calls.add(1);

    const d = res.timings.duration;
    list_duration.add(d);
    slow_req.add(d > 500);

    check(res, { 'list 200': r => r.status === 200 });
    return res;
}

// 상세
function detailRequest({ hot, phase, iter }) {
    const pool = hot ? FIXED_HOT_IDS : FIXED_COLD_IDS;
    if (!pool || pool.length === 0) return;
    const id = pool[iter % pool.length];
    const res = http.get(`${BASE_URL}/api/v1/products/${id}`, { tags: { ep: 'detail', hot: hot ? '1' : '0', phase } });
    detail_calls.add(1);

    const d = res.timings.duration;
    detail_duration.add(d);
    slow_req.add(d > 300);

    check(res, { 'detail 200': r => r.status === 200 });
    return res;
}

// 각 단계에서 목록/상세 비율을 결정론적으로 섞기
function isDetail(iter) {
    // 0~99 사이에서 DETAIL_RATIO%에 해당하면 상세
    return (iter % 100) < DETAIL_RATIO;
}

export function prewarm() {
    // 목록 상위 페이지 예열
    for (let i = 0; i < 10; i++) {
        for (const p of HIT_PAGES) {
            listRequest({ page: p, order: 'LIKE_DESC', phase: 'prewarm', cb: null });
            sleep(0.03);
        }
    }
    // 상세 핫 아이디 예열
    for (let i = 0; i < Math.min(30, FIXED_HOT_IDS.length || 0); i++) {
        detailRequest({ hot: true, phase: 'prewarm', iter: i });
        sleep(0.02);
    }
}

export default function () {
    const iter = __ITER;
    const phase = __ENV.K6_SCENARIO_NAME; // 'miss' | 'hit' | 'prewarm'

    // 목록/상세 믹스
    const doDetail = isDetail(iter);

    if (phase === 'miss') {
        if (doDetail) {
            // 상세는 캐시 키 깨기 힘들어도 "혼합 부하"를 위해 포함
            // (Before/After 동일 대상이므로 비교 공정성 OK)
            detailRequest({ hot: (iter % 10) < 8, phase, iter }); // hot:80%, cold:20%
        } else {
            const page = pick(MISS_PAGES, iter);
            const cb = CACHE_BUST ? String(iter) : null; // 목록 MISS 강제
            listRequest({ page, order: 'LIKE_DESC', phase, cb });
        }
        return;
    }

    if (phase === 'hit') {
        if (doDetail) {
            detailRequest({ hot: (iter % 10) < 8, phase, iter });
        } else {
            const page = pick(HIT_PAGES, iter);
            listRequest({ page, order: 'LIKE_DESC', phase, cb: null });
        }
        sleep(0.02);
        return;
    }

    // prewarm은 export function prewarm()에서 처리
}
