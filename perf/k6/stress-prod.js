/**
 * Stress test Tier S em prod/homolog.
 * Uso: BASE_URL=... k6 run perf/k6/stress-prod.js
 * Credenciais: PERF_TEST_EMAIL/PERF_TEST_PASSWORD ou PERF_REGISTER=1 (usuário efêmero).
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';
import { setupAuth } from './lib/auth.js';

const BASE_URL = __ENV.BASE_URL || 'https://nutriplus-api-production.up.railway.app';
const IS_REMOTE = !BASE_URL.includes('localhost') && !BASE_URL.includes('127.0.0.1');

const MAX_VUS = parseInt(__ENV.STRESS_MAX_VUS || (IS_REMOTE ? '10' : '20'), 10);
const P95_MS = parseInt(__ENV.K6_P95_MS || (IS_REMOTE ? '800' : '300'), 10);

const bootstrapDuration = new Trend('endpoint_bootstrap_ms', true);
const usersMeDuration = new Trend('endpoint_users_me_ms', true);
const checkinsDuration = new Trend('endpoint_checkins_today_ms', true);
const genStatusDuration = new Trend('endpoint_generation_status_ms', true);
const healthDuration = new Trend('endpoint_health_ms', true);

export const options = {
  scenarios: {
    stress_ramp: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: Math.max(2, Math.floor(MAX_VUS / 2)) },
        { duration: '1m', target: Math.max(2, Math.floor(MAX_VUS / 2)) },
        { duration: '30s', target: MAX_VUS },
        { duration: '1m', target: MAX_VUS },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '15s',
    },
  },
  thresholds: {
    http_req_failed: [`rate<${IS_REMOTE ? 0.1 : 0.05}`],
    [`http_req_duration{tier:S}`]: [`p(95)<${P95_MS}`],
    endpoint_bootstrap_ms: [`p(95)<${P95_MS}`],
  },
};

export function setup() {
  const register =
    __ENV.PERF_REGISTER === '1' &&
    !IS_REMOTE &&
    !(__ENV.PERF_TEST_EMAIL && __ENV.PERF_TEST_PASSWORD);

  const auth = setupAuth(BASE_URL, {
    register,
    email: __ENV.PERF_TEST_EMAIL,
    password: __ENV.PERF_TEST_PASSWORD,
    name: 'k6 Stress',
  });

  if (!auth.token) {
    throw new Error('Login falhou — defina PERF_TEST_EMAIL/PERF_TEST_PASSWORD (prod não aceita register)');
  }

  return {
    email: auth.email,
    authHeaders: { ...auth.authHeaders, 'Content-Type': 'application/json' },
  };
}

function timedGet(url, params, trend) {
  const start = Date.now();
  const res = http.get(url, params);
  trend.add(Date.now() - start);
  return res;
}

export default function (data) {
  const publicTags = { tags: { tier: 'S' } };
  const authTags = { headers: data.authHeaders, tags: { tier: 'S' } };

  const health = timedGet(`${BASE_URL}/health`, publicTags, healthDuration);
  check(health, { 'health 200': (r) => r.status === 200 });

  const bootstrap = timedGet(`${BASE_URL}/app/bootstrap`, authTags, bootstrapDuration);
  check(bootstrap, { 'bootstrap 200': (r) => r.status === 200 });

  const me = timedGet(`${BASE_URL}/users/me`, authTags, usersMeDuration);
  check(me, { 'users/me 200': (r) => r.status === 200 });

  const checkins = timedGet(`${BASE_URL}/checkins/today`, authTags, checkinsDuration);
  check(checkins, { 'checkins/today ok': (r) => r.status === 200 || r.status === 404 });

  const gen = timedGet(`${BASE_URL}/meal-plans/generation-status`, authTags, genStatusDuration);
  check(gen, { 'generation-status 200': (r) => r.status === 200 });

  check(http.get(`${BASE_URL}/feature-flags`, publicTags), {
    'feature-flags 200': (r) => r.status === 200,
  });

  sleep(IS_REMOTE ? 0.8 : 0.3);
}

export function handleSummary(data) {
  const out = __ENV.STRESS_SUMMARY_OUT;
  if (out) {
    return { [out]: JSON.stringify(data, null, 2) };
  }
  return {};
}
