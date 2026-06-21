import http from 'k6/http';
import { check, sleep } from 'k6';

// Tier A — auth endpoints (target p95 < 500ms)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    'http_req_duration{tier:A}': ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const email = `k6-a-${__VU}-${Date.now()}@nutriplus.test`;
  const password = 'k6-secret-123';

  check(
    http.post(
      `${BASE_URL}/auth/register`,
      JSON.stringify({ name: 'k6 Tier A', email, password }),
      { headers: { 'Content-Type': 'application/json' }, tags: { tier: 'A' } },
    ),
    { 'register 201': (r) => r.status === 201 },
  );

  check(
    http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ email, password }),
      { headers: { 'Content-Type': 'application/json' }, tags: { tier: 'A' } },
    ),
    { 'login 200': (r) => r.status === 200 },
  );

  sleep(0.2);
}
