import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';

export const list_duration = new Trend('list_duration');
export const detail_duration = new Trend('detail_duration');
export const slow_req = new Rate('slow_req');
export const list_calls = new Counter('list_calls');
export const detail_calls = new Counter('detail_calls');

const BASE_URL  = __ENV.BASE_URL || 'http://localhost:8080';
const PAGE_SIZE = __ENV.PAGE_SIZE ? parseInt(__ENV.PAGE_SIZE) : 20;
const BRAND_IDS = (__ENV.BRAND_IDS || '1,2,3,4,5,6,7,8,9,10').trim();
const CACHE_BUST = (__ENV.CACHE_BUST || '1') === '1';
const DETAIL_RATIO = __ENV.DETAIL_RATIO ? parseInt(__ENV.DETAIL_RATIO) : 25;

const ENV_HOT  = (__ENV.DETAIL_HOT_IDS  || '').split(',').map(s=>s.trim()).filter(Boolean);
const ENV_COLD = (__ENV.DETAIL_COLD_IDS || '').split(',').map(s=>s.trim()).filter(Boolean);
const FIXED_HOT_IDS  = ENV_HOT.map(v => Number.isFinite(+v) ? +v : v);
const FIXED_COLD_IDS = ENV_COLD.map(v => Number.isFinite(+v) ? +v : v);

export const options = {
    scenarios: {
        miss: {
            exec: 'run_miss',
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
            exec: 'run_prewarm',
            executor: 'shared-iterations',
            startTime: '120s',         // MISS 120s 뒤에 시작 (필요시 env로 바꿔도 됨)
            vus: 1, iterations: 60,
            tags: { phase: 'prewarm' },
            gracefulStop: '5s',
        },
        hit: {
            exec: 'run_hit',
            executor: 'constant-arrival-rate',
            startTime: '125s',         // prewarm 직후 시작
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
        'http_req_duration{phase:miss,ep:list}': ['p(95)<2500'],
        'http_req_duration{phase:hit,ep:list}':  ['p(95)<150'],
        'http_req_duration{ep:detail}':          ['p(95)<300'],
        slow_req: ['rate<0.20'],
        list_calls: ['count>0'],
        detail_calls: ['count>0'],
    },
};

const MISS_PAGES = Array.from({ length: 16 }, (_, i) => i + 5); // 5..20
const HIT_PAGES  = [0, 1, 2];

function pick(arr, iter) { return arr[iter % arr.length]; }
function isDetail(iter) { return (iter % 100) < DETAIL_RATIO; }

function listRequest({ page, phase, cb }) {
    const tags = { ep: 'list', order: 'LIKE_DESC', page: String(page), phase };
    let url = `${BASE_URL}/api/v1/products?page=${page}&size=${PAGE_SIZE}&orderTypeRequest=LIKE_DESC&brandIds=${encodeURIComponent(BRAND_IDS)}`;
    if (cb) url += `&cb=${cb}`;
    const res = http.get(url, { tags });

    list_calls.add(1);
    const d = res.timings.duration;
    list_duration.add(d);
    slow_req.add(d > 500);
    check(res, { 'list 200': r => r.status === 200 });
}

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
}

// === 각 시나리오 실행 함수 ===
export function run_miss() {
    const iter = __ITER;
    if (isDetail(iter)) {
        detailRequest({ hot: (iter % 10) < 8, phase: 'miss', iter });
    } else {
        const page = pick(MISS_PAGES, iter);
        const cb = CACHE_BUST ? String(iter) : null; // 캐시 미스 강제
        listRequest({ page, phase: 'miss', cb });
    }
}

export function run_prewarm() {
    // 목록 상위 페이지 예열
    for (let i = 0; i < 10; i++) {
        for (const p of HIT_PAGES) {
            listRequest({ page: p, phase: 'prewarm', cb: null });
            sleep(0.02);
        }
    }
    // 상세 핫 아이디 예열
    for (let i = 0; i < Math.min(30, FIXED_HOT_IDS.length || 0); i++) {
        detailRequest({ hot: true, phase: 'prewarm', iter: i });
        sleep(0.01);
    }
}

export function run_hit() {
    const iter = __ITER;
    if (isDetail(iter)) {
        detailRequest({ hot: (iter % 10) < 8, phase: 'hit', iter });
    } else {
        const page = pick(HIT_PAGES, iter);
        listRequest({ page, phase: 'hit', cb: null });
        sleep(0.02);
    }
}
