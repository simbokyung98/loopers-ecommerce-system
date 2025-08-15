import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// ===== 환경변수(필요시만 바꿔) =====
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const PAGE = Number(__ENV.PAGE || 0);     // refresh-ahead는 page=0에만 적용했다고 했지?
const SIZE = Number(__ENV.SIZE || 20);
const BRAND = __ENV.BRAND_ID || '';       // 브랜드 필터 없으면 비워둠
const VUS = Number(__ENV.VUS || 10);
const DURATION_SECS = Number(__ENV.DURATION_SECS || 240); // 4분(30s TTL 여러 번 넘김)
const TOKEN = __ENV.TOKEN || '';
const PREWARM = (__ENV.PREWARM || 'true').toLowerCase() === 'true';

const latList = new Trend('list_latency_ms');
const calls = new Counter('list_calls');

export const options = {
    thresholds: {
        'http_req_failed{ep:list}': ['rate<0.01'],
        'http_req_duration{ep:list}': ['p(95)<500'],
    },
    scenarios: {
        list: {
            executor: 'constant-vus',
            vus: VUS,
            duration: `${DURATION_SECS}s`,
            exec: 'listExec',
        },
    },
};

function H() {
    return {
        'Content-Type': 'application/json',
        ...(TOKEN ? { Authorization: `Bearer ${TOKEN}` } : {}),
    };
}
function listUrl() {
    const orderParamName = 'orderTypeRequest';     // ← DTO 필드명에 맞춤
    const orderValue = 'LIKE_DESC';                // ← 높은좋아요순은 이 값
    const brand = BRAND ? `&brandId=${encodeURIComponent(BRAND)}` : '';
    return `${BASE}/api/v1/products?${orderParamName}=${orderValue}&page=${PAGE}&size=${SIZE}${brand}`;
}
// 시작 전에 한 번 예열(캐시에 올려두고 TTL 경계를 여러 번 지나면서 안정성 관찰)
export function setup() {
    if (!PREWARM) return;
    http.get(listUrl(), { headers: H(), tags: { ep: 'list', phase: 'prewarm' } });
}

export function listExec() {
    const res = http.get(listUrl(), { headers: H(), tags: { ep: 'list' } });
    check(res, { '200': r => r.status === 200 });
    calls.add(1);
    latList.add(res.timings.duration);
    sleep(0.3 + Math.random() * 0.4); // 살짝 지연(너무 타이트하게 몰지 않게)
}
