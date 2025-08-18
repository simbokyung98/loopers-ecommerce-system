import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ==== 환경변수 ====
// BASE_URL: 베이스 URL (예: http://localhost:8080)
// PRODUCT_IDS: "1,2,3,4,5,6,7,8,9,10"
// VUS: 동시 사용자 수(기본 20)
// COLD: 콜드 구간 길이(기본 2m)  WARM: 웜 구간 길이(기본 2m)
// TOKEN: 필요 시 Authorization 헤더

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const IDS  = (__ENV.PRODUCT_IDS || '1,2,3,4,5,6,7,8,9,10').split(',').map(s => s.trim());
const VUS  = Number(__ENV.VUS || 20);
const COLD_SECS = Number(__ENV.COLD_SECS || 120); // 2m = 120s
const WARM_SECS = Number(__ENV.WARM_SECS || 120);
const TOKEN = __ENV.TOKEN || '';

const latDetail = new Trend('detail_latency_ms');
const hitsDetail = new Counter('detail_calls');

export const options = {
    tags: {                       // (선택) 전역 testid를 env로부터 넣고 싶을 때
        ...( __ENV.TEST_ID ? { testid: __ENV.TEST_ID } : {} ),
    },
    thresholds: {
        'http_req_failed{phase:cold}': ['rate<0.01'],
        'http_req_failed{phase:warm}': ['rate<0.01'],
    },
    scenarios: {
        cold:    { executor: 'constant-vus', vus: VUS, duration: `${COLD_SECS}s`, exec: 'runCold',  tags: { phase: 'cold' } },
        idle:    { executor: 'shared-iterations', vus: 1, iterations: 1, startTime: `${COLD_SECS}s`,      exec: 'idle10s',  tags: { phase: 'idle' } },
        prewarm: { executor: 'shared-iterations', vus: 1, iterations: 1, startTime: `${COLD_SECS+10}s`,   exec: 'prewarm',  tags: { phase: 'prewarm' } },
        warm:    { executor: 'constant-vus', vus: VUS, duration: `${WARM_SECS}s`, startTime: `${COLD_SECS+12}s`, exec: 'runWarm', tags: { phase: 'warm' } },
    },
};
function H() {
    return { 'Content-Type': 'application/json', ...(TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {}) };
}
function detailUrl(id) { return `${BASE}/api/v1/products/${id}`; }
function pickId() { return IDS[Math.floor(Math.random() * IDS.length)]; }

export function runCold() {
    const id = pickId();
    const r = http.get(detailUrl(id), { headers: H(), tags: { phase: 'cold' } });
    check(r, { '200': res => res.status === 200 });
    hitsDetail.add(1);
    latDetail.add(r.timings.duration, { phase: 'cold' });
    sleep(0.25);
}

export function idle10s() { sleep(10); }

export function prewarm() {
    // 각 ID 1회 호출로 캐시 채우기
    IDS.forEach(id => http.get(detailUrl(id), { headers: H(), tags: { phase: 'prewarm' } }));
}

export function runWarm() {
    const id = pickId();
    const r = http.get(detailUrl(id), { headers: H(), tags: { phase: 'warm' } });
    check(r, { '200': res => res.status === 200 });
    hitsDetail.add(1);
    latDetail.add(r.timings.duration, { phase: 'warm' });
    sleep(0.25);
}
