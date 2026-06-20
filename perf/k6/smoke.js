import http from 'k6/http';
import { check, sleep } from 'k6';

// Tier S smoke — read-only endpoints. This is k6 load-test script, not app code.
// 404 on GET /meal-plans/latest or /shopping-list/latest means "no plan yet" for this user,
// NOT a missing route (MealPlanController / ShoppingListController exist).

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    'http_req_duration{tier:S}': ['p(95)<200'],
    http_req_failed: ['rate<0.05'],
  },
};

export function setup() {
  const email = __ENV.K6_EMAIL || `k6-${Date.now()}@nutriplus.test`;
  const password = __ENV.K6_PASSWORD || 'k6-secret-123';

  if (!__ENV.K6_EMAIL) {
    const registerRes = http.post(
      `${BASE_URL}/auth/register`,
      JSON.stringify({ name: 'k6 User', email, password }),
      { headers: { 'Content-Type': 'application/json' } },
    );
    check(registerRes, { 'register status 201': (r) => r.status === 201 });
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

  sleep(0.3);
}
