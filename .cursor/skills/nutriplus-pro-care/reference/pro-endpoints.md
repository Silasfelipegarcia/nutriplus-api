# Pro — Endpoints e Integrações

## Nutricionista

- `POST /auth/register/nutritionist` — CRN, bio
- `POST /pro/stripe/connect` — Stripe Connect
- `GET /pro/dashboard`, `GET /pro/patients`
- `GET /pro/patients/{id}/dossier`
- `POST /pro/invites` — link `/invite/{code}`

## Paciente

- `GET /nutritionists` — marketplace público Tier S
- `POST /care/request/{nutritionistId}`
- `POST /care/accept-invite/{code}`
- `GET /care/my`
- `POST /consultations/pay`

## Mensagens

- `GET /conversations`, `GET /conversations/{id}`
- `POST /conversations/{id}/messages` — Tier B

## Performance

Endpoints Pro readonly: Tier **S** p95 < 200ms  
`GET /pro/patients/{id}/meal-plans` — batch load (sem N+1)

## Web

Portal Pro em `nutriplus-web/src/presentation/pro/`  
Doc local: `nutriplus-web/docs/ARCHITECTURE.md`
