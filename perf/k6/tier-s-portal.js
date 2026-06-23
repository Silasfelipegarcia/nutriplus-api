import http from 'k6/http';
import { check, sleep } from 'k6';
import { setupAuth } from './lib/auth.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const P95_MS = parseInt(__ENV.K6_P95_MS || '200', 10);

export const options = {
  vus: 3,
  duration: '30s',
  thresholds: {
    [`http_req_duration{tier:S}`]: [`p(95)<${P95_MS}`],
    http_req_failed: ['rate<0.1'],
  },
};

export function setup() {
  const auth = setupAuth(BASE_URL, { name: 'k6 Portal' });
  return {
    authHeaders: {
      ...auth.authHeaders,
      'Content-Type': 'application/json',
    },
  };
}

export default function (data) {
  const params = { headers: data.authHeaders, tags: { tier: 'S' } };

  const portalGets = [
    `${BASE_URL}/users/me`,
    `${BASE_URL}/meal-plans/generation-status`,
    `${BASE_URL}/nutrition-profile`,
    `${BASE_URL}/checkins/today`,
    `${BASE_URL}/checkins/stats`,
  ];

  for (const url of portalGets) {
    const res = http.get(url, params);
    check(res, {
      [`${url} ok`]: (r) => r.status === 200 || r.status === 404,
    });
  }

  sleep(0.5);
}
