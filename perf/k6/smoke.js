import http from 'k6/http';
import { check, sleep } from 'k6';
import { setupAuth } from './lib/auth.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const P95_MS = parseInt(__ENV.K6_P95_MS || '200', 10);
const IS_REMOTE = !BASE_URL.includes('localhost') && !BASE_URL.includes('127.0.0.1');

export const options = {
  vus: IS_REMOTE ? 2 : 5,
  duration: '30s',
  thresholds: {
    [`http_req_duration{tier:S}`]: [`p(95)<${P95_MS}`],
    http_req_failed: [`rate<${IS_REMOTE ? 0.15 : 0.05}`],
  },
};

export function setup() {
  return setupAuth(BASE_URL);
}

export default function (data) {
  const params = { headers: data.authHeaders, tags: { tier: 'S' } };

  check(http.get(`${BASE_URL}/health`, { tags: { tier: 'S' } }), {
    'health status 200': (r) => r.status === 200,
  });

  check(http.get(`${BASE_URL}/users/me`, params), {
    'users/me status 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  check(http.get(`${BASE_URL}/nutrition-profile`, params), {
    'nutrition-profile status 2xx/404': (r) => r.status === 200 || r.status === 404,
  });

  check(http.get(`${BASE_URL}/meal-plans/latest`, params), {
    'meal-plans/latest status 2xx/404': (r) => r.status === 200 || r.status === 404,
  });

  check(http.get(`${BASE_URL}/shopping-list/latest`, params), {
    'shopping-list/latest status 2xx/404': (r) => r.status === 200 || r.status === 404,
  });

  check(http.get(`${BASE_URL}/checkins/today`, params), {
    'checkins/today status 2xx/404': (r) => r.status === 200 || r.status === 404,
  });

  check(http.get(`${BASE_URL}/checkins/stats`, params), {
    'checkins/stats status 2xx': (r) => r.status >= 200 && r.status < 300,
  });

  check(http.get(`${BASE_URL}/progress/schedule`, params), {
    'progress/schedule status 2xx/404': (r) => r.status === 200 || r.status === 404,
  });

  check(http.get(`${BASE_URL}/training/sports`, { tags: { tier: 'S' } }), {
    'training/sports status 200': (r) => r.status === 200,
  });

  check(http.get(`${BASE_URL}/legal/terms`, { tags: { tier: 'S' } }), {
    'legal/terms status 200': (r) => r.status === 200,
  });

  sleep(0.3);
}
