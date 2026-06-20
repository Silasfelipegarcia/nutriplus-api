# Nutri+ — Engajamento, gamificação e lembretes

Regras de produto para check-ins, motivação e notificações locais.

---

## Gamificação (MVP)

| Elemento | Onde | Fonte de dados |
|----------|------|----------------|
| Streak (dias seguidos) | Aba **Hoje** + Evolução | `GET /checkins/stats` → `currentStreak` |
| Aderência semanal | Aba **Hoje** + Evolução | `weekAdherencePercent` |
| Progresso refeições | Aba **Hoje** | `GET /checkins/today` |
| Meta calórica | Aba **Hoje** | check-ins + extras |
| Mensagem motivacional | Aba **Hoje** | Client-side (`MotivationMessages`) — tom Luna/Bruno |

### Regras das mensagens (determinísticas)

1. Sem plano → convite a gerar plano
2. Todas refeições marcadas → celebração
3. Refeição atrasada (horário passou, não marcada) → nudge suave
4. Após 20h sem check-in → streak em risco
5. Sem check-ins no dia → convite à próxima refeição
6. Default → no caminho + aderência semanal

**Tom:** incentivo, não culpa. Sem pontos, ranking ou LLM em tempo real no MVP.

### Boas-vindas

- `WelcomeCoachSheet` — uma vez após primeiro acesso ao shell principal
- Flag: `SharedPreferences` → `welcome_coach_seen`

---

## Central de Ajuda

- Copy canônico: [HELP_CONTENT.md](./HELP_CONTENT.md)
- App: `HelpScreen` (Perfil → Ajuda e FAQ)
- Login/registro: value prop + link landing (sem FAQ completo pré-login)

---

## Lembretes locais (MVP)

| Preferência | Storage | Default |
|-------------|---------|---------|
| `reminder_meal_enabled` | SharedPreferences | `false` |
| `reminder_lead_minutes` | SharedPreferences | `15` |
| `reminder_evolution_enabled` | SharedPreferences | `true` (UI; push evolução = fase 3) |

### Comportamento

- Opt-in explícito para notificações de refeição
- Permissão SO solicitada ao ativar
- Horários lidos de `meal.scheduled_time` do plano ativo
- Repetição diária (`matchDateTimeComponents: time`)
- Re-sync ao carregar Hoje, ao plano ficar pronto, ao mudar antecedência
- Cancela todas ao logout

### Calendário

- Botão "Adicionar refeições ao calendário" → `add_2_calendar`
- Eventos com título `{tipo refeição} — Nutri+`
- Usuário gerencia no app Calendário nativo

### Apple Watch e Wear OS (MVP)

- Notificações locais **espelhadas** no relógio pareado (comportamento nativo iOS/Android)
- Ação **OK — feita** na notificação → marca check-in via `NotificationCheckinHandler`
- Payload: `mealId` do plano; auth via token em `FlutterSecureStorage`
- **Não** é app WatchKit standalone — fase futura se volume exigir

Requisitos: lembretes ativos + relógio pareado + notificações permitidas no relógio.

---

## Fase 3 (planejado)

- Firebase Cloud Messaging
- Tabela `device_tokens` na API
- Lembretes server-side (backup), reengajamento streak, evolução due

Ver [NUTRI_PLUS_PRO.md](./NUTRI_PLUS_PRO.md).
