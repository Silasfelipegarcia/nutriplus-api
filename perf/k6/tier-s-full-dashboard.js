import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { setupAuth } from './lib/auth.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const P95_MS = parseInt(__ENV.K6_P95_MS || '800', 10);
const IS_REMOTE = !BASE_URL.includes('localhost') && !BASE_URL.includes('127.0.0.1');

const dashboardFlow = new Trend('dashboard_flow_duration', true);

export const options = {
  vus: IS_REMOTE ? 2 : 3,
  duration: '30s',
  thresholds: {
    dashboard_flow_duration: [`p(95)<${P95_MS}`],
    [`http_req_duration{tier:S}`]: [`p(95)<${parseInt(__ENV.K6_TIER_S_P95_MS || '200', 10)}`],
    http_req_failed: [`rate<${IS_REMOTE ? 0.15 : 0.05}`],
  },
};

export function setup() {
  return setupAuth(BASE_URL, { name: 'k6 Dashboard' });
}

export default function (data) {
  const params = { headers: data.authHeaders, tags: { tier: 'S' } };
  const start = Date.now();

  const urls = [
    `${BASE_URL}/users/me`,
    `${BASE_URL}/nutrition-profile`,
    `${BASE_URL}/meal-plans/latest`,
    `${BASE_URL}/shopping-list/latest`,
    `${BASE_URL}/checkins/today`,
    `${BASE_URL}/checkins/stats`,
    `${BASE_URL}/progress/schedule`,
  ];

  for (const url of urls) {
    const res = http.get(url, params);
    check(res, {
      [`${url} ok`]: (r) => r.status === 200 || r.status === 404,
    });
  }

  dashboardFlow.add(Date.now() - start);
  sleep(0.5);
}
