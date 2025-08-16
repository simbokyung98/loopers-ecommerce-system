// scripts/test-api/product_latest_oldest.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        latest: {
            executor: 'constant-arrival-rate',
            rate: 100, timeUnit: '1s', duration: '2m',
            preAllocatedVUs: 150, maxVUs: 300,
            exec: 'latest',            // ✅ 이 시나리오는 latest() 실행
            gracefulStop: '30s',
        },
        oldest: {
            executor: 'constant-arrival-rate',
            rate: 100, timeUnit: '1s', duration: '2m',
            preAllocatedVUs: 150, maxVUs: 300,
            startTime: '2m',
            exec: 'oldest',            // ✅ 이 시나리오는 oldest() 실행
            gracefulStop: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        'http_req_duration{scenario:latest}': ['p(95)<500'],
        'http_req_duration{scenario:oldest}': ['p(95)<500'],
        // 매핑 실수 방지용(원하면 제거)
        'http_reqs{scenario:latest}': ['count>0'],
        'http_reqs{scenario:oldest}': ['count>0'],
    },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const SIZE = __ENV.SIZE || 20;

export default function () { /* noop */ }

export function latest() {
    const res = http.get(`${BASE}/api/v1/products?page=0&size=${SIZE}&orderTypeRequest=LATEST`);
    check(res, { ok: r => r.status === 200 });
}

export function oldest() {
    const res = http.get(`${BASE}/api/v1/products?page=0&size=${SIZE}&orderTypeRequest=OLDEST`);
    check(res, { ok: r => r.status === 200 });
}

