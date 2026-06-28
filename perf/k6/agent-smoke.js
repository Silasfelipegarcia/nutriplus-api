import http from 'k6/http';
import { check, sleep } from 'k6';

const AGENT_URL = __ENV.AGENT_URL || 'http://localhost:8000';
const P95_MS = parseInt(__ENV.K6_P95_MS || '500', 10);

export const options = {
  vus: 2,
  duration: '20s',
  thresholds: {
    [`http_req_duration{tier:B}`]: [`p(95)<${P95_MS}`],
    http_req_failed: ['rate<0.1'],
  },
};

export default function () {
  check(http.get(`${AGENT_URL}/health`, { tags: { tier: 'B' } }), {
    'agent health 200': (r) => r.status === 200,
  });

  check(http.get(`${AGENT_URL}/metrics`, { tags: { tier: 'B' } }), {
    'agent metrics 200': (r) => r.status === 200,
  });

  sleep(0.2);
}
