import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const P95_MS = parseInt(__ENV.K6_P95_MS || '200', 10);

export const options = {
  vus: 3,
  duration: '30s',
  thresholds: {
    [`http_req_duration{tier:S}`]: [`p(95)<${P95_MS}`],
    http_req_failed: ['rate<0.05'],
  },
};

export default function () {
  const listRes = http.get(`${BASE_URL}/nutritionists`, { tags: { tier: 'S' } });
  check(listRes, {
    'nutritionists list 200': (r) => r.status === 200,
  });

  let nutritionistId = null;
  try {
    const body = listRes.json();
    if (Array.isArray(body) && body.length > 0) {
      nutritionistId = body[0].id;
    }
  } catch (_) {
    // ignore parse errors
  }

  if (nutritionistId != null) {
    const detailRes = http.get(`${BASE_URL}/nutritionists/${nutritionistId}`, { tags: { tier: 'S' } });
    check(detailRes, {
      'nutritionist detail 200': (r) => r.status === 200,
    });
  }

  check(http.get(`${BASE_URL}/pricing/guidelines`, { tags: { tier: 'S' } }), {
    'pricing guidelines 200': (r) => r.status === 200,
  });

  sleep(0.3);
}
