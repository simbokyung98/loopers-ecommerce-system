import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    vus: 10,          // 동시 사용자 수
    duration: '30s',  // 테스트 지속 시간
};

export default function () {
    http.get('https://test-api.k6.io/public/crocodiles/1/');
    sleep(1); // 1초 대기
}