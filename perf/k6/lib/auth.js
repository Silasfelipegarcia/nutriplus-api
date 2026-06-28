import http from 'k6/http';
import { check } from 'k6';

const DEFAULT_LOCAL_EMAIL = 'teste@nutriplus.local';
const DEFAULT_LOCAL_PASSWORD = 'Nutri123!';

export function idempotencyKey(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
}

export function jsonHeaders(extra = {}) {
  return { 'Content-Type': 'application/json', ...extra };
}

export function registerUser(baseUrl, email, password, name = 'k6 User') {
  return http.post(
    `${baseUrl}/auth/register`,
    JSON.stringify({
      name,
      email,
      password,
      cpf: '529.982.247-25',
      birthDate: '1990-06-15',
      contactPhone: '11999999999',
    }),
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

function isLocal(baseUrl) {
  return baseUrl.includes('localhost') || baseUrl.includes('127.0.0.1');
}

export function setupAuth(baseUrl, options = {}) {
  const email =
    options.email ||
    __ENV.PERF_TEST_EMAIL ||
    (isLocal(baseUrl) ? DEFAULT_LOCAL_EMAIL : null);
  const password =
    options.password ||
    __ENV.PERF_TEST_PASSWORD ||
    (isLocal(baseUrl) ? DEFAULT_LOCAL_PASSWORD : null);

  if (!email || !password) {
    throw new Error('Set PERF_TEST_EMAIL/PERF_TEST_PASSWORD for remote k6 runs');
  }

  if (options.register) {
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
