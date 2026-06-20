import http from 'k6/http';
import { check, sleep } from 'k6';

// Tier C — full meal-plan generation flow (async job + LLM via agente).
// Requires API (:8080) AND nutriplus-agentes (:8000). Run manually; not in CI by default.
//
// Example:
//   BASE_URL=http://localhost:8080 \
//   K6_EMAIL=teste@nutriplus.local K6_PASSWORD=Nutri123! \
//   k6 run perf/k6/generate-flow.js

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const POLL_INTERVAL_SEC = Number(__ENV.K6_POLL_INTERVAL_SEC || '3');
const MAX_POLLS = Number(__ENV.K6_MAX_POLLS || '40');

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate<0.1'],
  },
};

export function setup() {
  const email = __ENV.K6_EMAIL || `k6-gen-${Date.now()}@nutriplus.test`;
  const password = __ENV.K6_PASSWORD || 'k6-secret-123';

  if (!__ENV.K6_EMAIL) {
    const registerRes = http.post(
      `${BASE_URL}/auth/register`,
      JSON.stringify({ name: 'k6 Generate User', email, password }),
      { headers: { 'Content-Type': 'application/json' } },
    );
    check(registerRes, { 'register status 201': (r) => r.status === 201 });

    const profileBody = {
      age: 30,
      sex: 'MALE',
      heightCm: 175,
      currentWeightKg: 80,
      targetWeightKg: 75,
      goal: 'LOSE_WEIGHT',
      activityLevel: 'MODERATE',
      dietaryPreference: 'OMNIVORE',
      restriction: 'NONE',
      agentPersona: 'LUNA',
      foodLikes: 'frango, arroz',
      foodDislikes: 'fígado',
    };

    const loginRes = http.post(
      `${BASE_URL}/auth/login`,
      JSON.stringify({ email, password }),
      { headers: { 'Content-Type': 'application/json' } },
    );
    check(loginRes, { 'login after register 200': (r) => r.status === 200 });
    const token = loginRes.json().token;
    const authHeaders = {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    };

    const profileRes = http.post(`${BASE_URL}/nutrition-profile`, JSON.stringify(profileBody), {
      headers: authHeaders,
    });
    check(profileRes, {
      'nutrition-profile status 2xx': (r) => r.status >= 200 && r.status < 300,
    });

    return { token, authHeaders: { Authorization: `Bearer ${token}` } };
  }

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(loginRes, { 'login status 200': (r) => r.status === 200 });

  const body = loginRes.json();
  return { token: body.token, authHeaders: { Authorization: `Bearer ${body.token}` } };
}

export default function (data) {
  const params = { headers: data.authHeaders, tags: { tier: 'C' } };

  const generateRes = http.post(`${BASE_URL}/meal-plans/generate`, null, params);
  check(generateRes, {
    'generate status 202': (r) => r.status === 202,
  });

  let status = generateRes.json().status;
  let polls = 0;

  while (status !== 'COMPLETED' && status !== 'FAILED' && polls < MAX_POLLS) {
    sleep(POLL_INTERVAL_SEC);
    polls += 1;
    const statusRes = http.get(`${BASE_URL}/meal-plans/generation-status`, params);
    check(statusRes, {
      'generation-status status 200': (r) => r.status === 200,
    });
    status = statusRes.json().status;
  }

  check(null, {
    'job completed (not failed)': () => status === 'COMPLETED',
  });

  if (status === 'COMPLETED') {
    check(http.get(`${BASE_URL}/meal-plans/latest`, params), {
      'meal-plans/latest status 200 after generate': (r) => r.status === 200,
    });
    check(http.get(`${BASE_URL}/shopping-list/latest`, params), {
      'shopping-list/latest status 200 after generate': (r) => r.status === 200,
    });
  }
}
