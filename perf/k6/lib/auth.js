import http from 'k6/http';
import { check } from 'k6';

export function idempotencyKey(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function jsonHeaders(extra = {}) {
  return { 'Content-Type': 'application/json', ...extra };
}

export function registerUser(baseUrl, email, password, name = 'k6 User') {
  return http.post(
    `${baseUrl}/auth/register`,
    JSON.stringify({ name, email, password }),
    { headers: jsonHeaders({ 'Idempotency-Key': idempotencyKey('k6-reg') }) },
  );
}

export function loginUser(baseUrl, email, password) {
  return http.post(
    `${baseUrl}/auth/login`,
    JSON.stringify({ email, password }),
    { headers: jsonHeaders() },
  );
}

export function setupAuth(baseUrl, options = {}) {
  const email = options.email || `k6-${Date.now()}@nutriplus.test`;
  const password = options.password || 'k6-secret-123';

  if (!options.email) {
    const registerRes = registerUser(baseUrl, email, password, options.name || 'k6 User');
    check(registerRes, {
      'register status 201': (r) => r.status === 201,
    });
  }

  const loginRes = loginUser(baseUrl, email, password);
  check(loginRes, { 'login status 200': (r) => r.status === 200 });

  const body = loginRes.json();
  return {
    email,
    token: body.token,
    authHeaders: { Authorization: `Bearer ${body.token}` },
  };
}
