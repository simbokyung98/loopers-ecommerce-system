import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ENV
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const USER_ID = __ENV.USER_ID || '1';       // 실존 tb_user.id
const PROD_ID = __ENV.PROD_ID || '101';     // 좋아요 보낼 상품
const VUS = Number(__ENV.VUS || 8);
const PRE_SECS = Number(__ENV.PRE_SECS || 20);   // 이벤트 전
const POST_SECS = Number(__ENV.POST_SECS || 20); // 이벤트 후
const TOKEN = __ENV.TOKEN || '';

// endpoints
function listUrl() { return `${BASE}/api/v1/products?orderTypeRequest=LIKE_DESC&page=0&size=20`; }
function likeUrl() { return `${BASE}/api/v1/like/products/${PROD_ID}`; }

// headers/tags
function H(ep, withUser) {
    const h = { 'Content-Type': 'application/json' };
    if (TOKEN) h.Authorization = `Bearer ${TOKEN}`;
    if (withUser) h['X-USER-ID'] = String(USER_ID);
    return { headers: h, tags: { ep } };
}

// metrics
const listLat = new Trend('list_latency_ms');
const calls = new Counter('list_calls');

export const options = {
    thresholds: {
        'http_req_failed{ep:list,phase:pre}': ['rate<0.01'],
        'http_req_failed{ep:list,phase:post}': ['rate<0.01'],
    },
    scenarios: {
        // 이벤트 전: 목록 트래픽
        pre:   { executor: 'constant-vus', vus: VUS, duration: `${PRE_SECS}s`, exec: 'hitPre' },
        // 좋아요 단건(동시성 1) — pre 끝난 직후 1초 뒤
        like:  { executor: 'shared-iterations', vus: 1, iterations: 1, startTime: `${PRE_SECS + 1}s`, exec: 'doLike' },
        // 이벤트 후: 목록 트래픽
        post:  { executor: 'constant-vus', vus: VUS, duration: `${POST_SECS}s`, startTime: `${PRE_SECS + 3}s`, exec: 'hitPost' },
    },
};

export function hitPre() {
    const r = http.get(listUrl(), H('list', false));
    check(r, { '200': res => res.status === 200 });
    calls.add(1);
    listLat.add(r.timings.duration, { phase: 'pre' });
    sleep(0.25);
}

export function doLike() {
    const r = http.post(likeUrl(), null, H('like', true)); // 헤더 X-USER-ID 필요!
    check(r, { 'like 200': res => res.status === 200 });
}

export function hitPost() {
    const r = http.get(listUrl(), H('list', false));
    check(r, { '200': res => res.status === 200 });
    calls.add(1);
    listLat.add(r.timings.duration, { phase: 'post' });
    sleep(0.25);
}
