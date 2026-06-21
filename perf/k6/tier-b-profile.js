import http from 'k6/http';
import { check, sleep } from 'k6';

// Tier B — nutrition profile write (target p95 < 2000ms)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  const email = `k6-b-${Date.now()}@nutriplus.test`;
  const password = 'k6-secret-123';
  http.post(
    `${BASE_URL}/auth/register`,
    JSON.stringify({ name: 'k6 Tier B', email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const token = loginRes.json().token;
  return { authHeaders: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } };
}

export const options = {
  vus: 3,
  duration: '30s',
  thresholds: {
    'http_req_duration{tier:B}': ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
  },
};

export default function (data) {
  const profile = {
    age: 28,
    sex: 'FEMALE',
    heightCm: 165,
    currentWeightKg: 68,
    targetWeightKg: 62,
    goal: 'LOSE_WEIGHT',
    activityLevel: 'MODERATE',
    dietaryPreference: 'OMNIVORE',
    restriction: 'NONE',
    agentPersona: 'LUNA',
  };

  check(
    http.post(`${BASE_URL}/nutrition-profile`, JSON.stringify(profile), {
      headers: data.authHeaders,
      tags: { tier: 'B' },
    }),
    { 'nutrition-profile 2xx': (r) => r.status >= 200 && r.status < 300 },
  );

  sleep(0.5);
}
