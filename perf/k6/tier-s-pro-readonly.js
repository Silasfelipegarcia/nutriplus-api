import http from 'k6/http';
import { check, sleep } from 'k6';
import { setupAuth } from './lib/auth.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const P95_MS = parseInt(__ENV.K6_P95_MS || '200', 10);

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: {
    [`http_req_duration{tier:S}`]: [`p(95)<${P95_MS}`],
    http_req_failed: ['rate<0.2'],
  },
};

export function setup() {
  return setupAuth(BASE_URL, { name: 'k6 Pro Readonly' });
}

export default function (data) {
  const params = { headers: data.authHeaders, tags: { tier: 'S' } };

  const dashboard = http.get(`${BASE_URL}/pro/dashboard`, params);
  check(dashboard, {
    'pro dashboard ok': (r) => r.status === 200 || r.status === 403,
  });

  const patients = http.get(`${BASE_URL}/pro/patients`, params);
  check(patients, {
    'pro patients ok': (r) => r.status === 200 || r.status === 403,
  });

  sleep(0.5);
}
