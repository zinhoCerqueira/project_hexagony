---

kanban-plugin: board

---

> ✅ REVIEW FUNCIONAL E TESTES v2.0 — Sequência Incremental T01 → T12. Execute na ordem. Cada task depende da anterior. Arraste de Backlog → Em Andamento → Concluído.

## Backlog

- [ ] **[T01] [INF-001] Subir Postgres + RabbitMQ + pgAdmin via Compose**
  🎯 **Objetivo:** Subir a infra local com credenciais de `.env`.
  ✅ **Critérios de Aceite:**
  - `docker compose --project-directory . -f docker/docker-compose.yml config` valida sem erro.
  - `docker compose --project-directory . -f docker/docker-compose.yml up -d` sobe `school_queue_db`, `school_queue_rabbitmq`, `school_queue_pgadmin` saudáveis.
  - `docker exec school_queue_db pg_isready -U queue_user -d school_queue_db` retorna `accepting connections`.
  - RabbitMQ UI em `http://localhost:15672` acessível; pgAdmin em `http://localhost:5050` lista server `school-queue-db`. #infra #docker
  *seq-01.*
- [ ] **[T02] [INF-002] Conectar app Spring Boot ao Postgres + RabbitMQ**
  🎯 **Objetivo:** Validar startup, Flyway V1 e JPA `validate`. Pré-req: T01.
  ✅ **Critérios de Aceite:**
  - `mvn -DskipTests package` compila; `mvn spring-boot:run` loga `Started SchoolQueueApplication`.
  - `SELECT version FROM flyway_schema_history;` tem 1 linha `V1 success`.
  - `GET http://localhost:8080/api/v1/schools` retorna `200`.
  - 6 tabelas existem: `schools`, `classrooms`, `parents`, `students`, `parent_student`, `pickup_queue`. #infra #spring #db
  *seq-02.*
- [ ] **[T03] [ESC-001] Gestão de Escolas (CRUD parcial + GPS obrigatório)**
  🎯 **Objetivo:** Criar a entidade raiz com GPS (pré-req do Haversine). Pré-req: T02.
  ✅ **Critérios de Aceite:**
  - `POST /api/v1/schools {"name":"Escola Central","latitude":-23.550520,"longitude":-46.633308}` → `201` com `id` (salvar `schoolId`).
  - `GET /api/v1/schools` → `200`; `GET /{id}` → `200`, inexistente → `404` `field=schoolId`.
  - `PUT /{id}` → `200`; `DELETE` → `405` proposital.
  - `POST` sem coordenadas → `400`. #escola #rest
  *seq-03.*
- [ ] **[T04] [ESC-002] Gestão de Turmas (Classrooms por escola)**
  🎯 **Objetivo:** Criar turmas com FK para escola. Pré-req: T03.
  ✅ **Critérios de Aceite:**
  - `POST /api/v1/classrooms {"schoolId":"{{schoolId}}","name":"Turma A"}` → `201` (salvar `classroomId`).
  - `GET /{id}` → `200` ou `404`; `GET /school/{schoolId}` → `200` array filtrado.
  - `PUT /{id}` → `200`; `POST` inválido → `400`; `DELETE` → `405`. #turma #rest
  *seq-04.*
- [ ] **[T05] [ESC-003] Gestão de Responsáveis (Parents)**
  🎯 **Objetivo:** Cadastrar responsáveis. Pré-req: T02.
  ✅ **Critérios de Aceite:**
  - `POST /api/v1/parents {"name":"Maria Souza","phone":"11999998888"}` → `201` (salvar `parentId`).
  - `GET /api/v1/parents` → `200`; `GET /{id}` → `200` ou `404`.
  - `PUT /{id}` → `200`; `POST` vazio → `400`; `DELETE` → `405`. #responsavel #rest
  *seq-05.*
- [ ] **[T06] [ESC-004] Gestão de Alunos + vínculo N:N**
  🎯 **Objetivo:** Criar aluno com `schoolId + classroomId + parentIds[]`. Pré-req: T03+T04+T05.
  ✅ **Critérios de Aceite:**
  - `POST /api/v1/students` → `201` com `parentIds` (salvar `studentId`).
  - `GET /{id}`, `GET /school/{schoolId}`, `GET /classroom/{classroomId}` → `200`.
  - `PUT` sobrescreve `parent_student` sem duplicatas; `schoolId` inexistente → `404`; payload vazio → `400`; `DELETE` → `405`. #aluno #rest
  *seq-06.*
- [ ] **[T07] [FILA-001] Anunciar Chegada (`POST /api/v1/queue/announce`)**
  🎯 **Objetivo:** Abrir item `EN_ROUTE` com Haversine + `ArrivalAnnouncedEvent`. Pré-req: T06.
  ✅ **Critérios de Aceite:**
  - GPS da escola → `CLOSE`, `called=true`, `EN_ROUTE` (salvar `queueItemId`).
  - GPS distante → `FAR`, `called=false`.
  - Duplicado mesmo `studentId` → `400`; escola inexistente → `404`; sem GPS → `400`.
  - RabbitMQ `queue.arrival.announced` com msg; log `Published arrival.announced`. #fila #gps #rabbitmq
  *seq-07.*
- [ ] **[T08] [FILA-002] Atualizar Estado (`PATCH /api/v1/queue/{id}/status`)**
  🎯 **Objetivo:** State machine selada + `StatusChangedEvent`. Pré-req: T07.
  ✅ **Critérios de Aceite:**
  - `UPDATE_RANGE CLOSE` → `called=true`; `MARK_AS_ARRIVED` → `ARRIVED` (repetir → `409`); `MARK_AS_COMPLETED` com `called` → `COMPLETED` (sem → `409`); `CANCEL` → `CANCELLED` (em `COMPLETED` → `409`).
  - `action` inválida / sem `newRange` → `400`; log `Published status.changed`. #fila #state-machine
  *seq-08.*
- [ ] **[T09] [FILA-003] Consultar Fila Ativa (`GET /school/{schoolId}/active`)**
  🎯 **Objetivo:** Listar só `EN_ROUTE + ARRIVED` ordenados. Pré-req: T07+T08.
  ✅ **Critérios de Aceite:**
  - `200` array ordenado por `createdAt`; `ARRIVED` continua, `COMPLETED`/`CANCELLED` somem. #fila #read
  *seq-09.*
- [ ] **[T10] [MSG-001] Publicação de Eventos no RabbitMQ**
  🎯 **Objetivo:** Validar exchange/fila/bindings e JSON. Pré-req: T07+T08.
  ✅ **Critérios de Aceite:**
  - Exchange `school.queue.events` + fila `queue.notifications` + 2 bindings; `Get messages` mostra `ArrivalAnnouncedEvent` e `StatusChangedEvent` com `__TypeId__`. #messaging #rabbitmq
  *seq-10.*

## Em Andamento

- [ ] ...

## Concluído

- [x] **[T11] [DB-001] Migrações Flyway + JPA Validate**
  🎯 **Objetivo:** Schema versionado e `validate`. Pré-req: T02.
  ✅ **Critérios de Aceite:**
  - Flyway `V1 success`; `uuid-ossp` presente; 6 tabelas em `public.*`. #db #flyway
  *seq-11.*
  ✔ Validada em 2026-09-25: banco vivo com V1+V2 `success`, `uuid-ossp`, 7 tabelas de domínio (`parent_school` da V2 além das 6 do card); boot com `ddl-auto: validate` sem erro; `mvn verify` verde (197 unit + 10 IT + `spotless:check`).

- [x] **[T12] [E2E] Roteiro mínimo + suíte automatizada**
  🎯 **Objetivo:** Fechar ciclo manual e gates. Pré-req: T01→T11.
  ✅ **Critérios de Aceite:**
  - Manual: escola CLOSE → turma → responsável → aluno → announce CLOSE → `ARRIVED` → `COMPLETED` → active vazio → announce FAR → `UPDATE_RANGE CLOSE`.
  - `mvn -DskipITs test` verde; `mvn verify` verde; `mvn spotless:check` verde; Bruno E2E verde. #tests #e2e #quality
  *seq-12.*
  ✔ Validada em 2026-09-26: roteiro manual 11/11 via HTTP (201×4, announce CLOSE `EN_ROUTE/CLOSE/called`, active n=1, `ARRIVED`, `COMPLETED`, active n=0, announce FAR `EN_ROUTE/FAR/!called`, `UPDATE_RANGE CLOSE` auto-chamada; 5 msgs em `queue.notifications`); `PickupQueueFlowIT` full-stack verde; Bruno `Queue/` com 7 requests seq 1–7; `SpringDataQueueRepositoryTest` renomeado para `*IT` (`mvn test` sem Docker).

%% kanban:settings
```
{"kanban-plugin":"board","list-collapse":[null]}
```
%%

---

## Anexos — comandos copia-e-cola (só markdown, fora do board)

```bash
# T01
docker compose --project-directory . -f docker/docker-compose.yml config
docker compose --project-directory . -f docker/docker-compose.yml up -d
docker exec school_queue_db pg_isready -U queue_user -d school_queue_db
# T02
mvn -DskipTests package
mvn spring-boot:run
curl http://localhost:8080/api/v1/schools
# T03-T06
curl -i -X POST http://localhost:8080/api/v1/schools -H "Content-Type: application/json" -d '{"name":"Escola Central","latitude":-23.550520,"longitude":-46.633308}'
curl -X POST http://localhost:8080/api/v1/classrooms -H "Content-Type: application/json" -d "{\"schoolId\":\"$SCHOOL_ID\",\"name\":\"Turma A\"}"
curl -X POST http://localhost:8080/api/v1/parents -H "Content-Type: application/json" -d '{"name":"Maria Souza","phone":"11999998888"}'
curl -X POST http://localhost:8080/api/v1/students -H "Content-Type: application/json" -d "{\"schoolId\":\"$SCHOOL_ID\",\"classroomId\":\"$CLASSROOM_ID\",\"name\":\"João\",\"parentIds\":[\"$PARENT_ID\"]}"
# T07-T09
curl -X POST http://localhost:8080/api/v1/queue/announce -H "Content-Type: application/json" -d "{\"schoolId\":\"$SCHOOL_ID\",\"studentId\":\"$STUDENT_ID\",\"parentId\":\"$PARENT_ID\",\"latitude\":-23.550520,\"longitude\":-46.633308}"
curl -X PATCH http://localhost:8080/api/v1/queue/$QUEUE_ITEM_ID/status -H "Content-Type: application/json" -d '{"action":"MARK_AS_ARRIVED"}'
curl -X PATCH http://localhost:8080/api/v1/queue/$QUEUE_ITEM_ID/status -H "Content-Type: application/json" -d '{"action":"MARK_AS_COMPLETED"}'
curl http://localhost:8080/api/v1/queue/school/$SCHOOL_ID/active | jq
# T12
mvn -DskipITs test
mvn verify
mvn spotless:check
```

## Mapa v1 → v2

| v1 | v2 |
|---|---|
| INF-001 | T01 |
| INF-002 | T02 |
| ESC-001 | T03 |
| ESC-002 | T04 |
| ESC-003 | T05 |
| ESC-004 | T06 |
| FILA-001 | T07 |
| FILA-002 | T08 |
| FILA-003 | T09 |
| MSG-001 | T10 |
| DB-001 | T11 |
| §3 + §4 E2E | T12 |
