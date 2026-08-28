import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 20,
  iterations: 200,
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const agendaId = __ENV.AGENDA_ID;

export default function () {
  const associateId = `associate-${__VU}-${__ITER}`;
  const response = http.post(
    `${baseUrl}/api/v1/agendas/${agendaId}/votes`,
    JSON.stringify({
      associateId,
      cpf: '12345678909',
      vote: 'YES',
    }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  check(response, {
    'voto registrado': (result) => result.status === 201,
  });
}
