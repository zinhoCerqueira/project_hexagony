# 🔍 REVIEW FUNCIONAL E TESTES — school-pickup-system

> Auditoria técnica do que **está implementado** no repositório
> (`src/main/java/com/schoolqueue/...`). Cada card abaixo cobre um módulo
> existente e mostra como validá-lo manualmente (HTTP, Bruno, RabbitMQ,
> Postgres, IDE).
>
> Não inclui itens do backlog (LAC01-LAC18) nem tasks ainda não codificadas.

---

## 📋 0. Verificação da Infra

### 🟦 INF-001 — Subir Postgres + RabbitMQ + pgAdmin via Compose #infra #docker

- **Resumo:** `docker/docker-compose.yml` declara `postgres:16-alpine`,
  `rabbitmq:3-management-alpine` e `dpage/pgadmin4:latest`. Usa credenciais
  injetadas de `.env` (não versionado). Volumes nomeados `pgdata` e
  `pgadmin-data` carregam o estado. Postgres com `PGDATA=/var/lib/postgresql/pgdata_app`
  isola o cluster num subdir (reset seletivo).
- **Componentes/Arquivos:**
  - `docker/docker-compose.yml`
  - `docker/pgadmin/servers.json`
  - `.env` (não commitado), `.env.example`
- **Pré-condições:** Docker Desktop rodando; porta `5432`, `5672`, `15672`,
  `5050` livres.
- **Passo a passo:**
  1. Confirmar o `.env` (raiz) com as chaves `POSTGRES_DB`, `POSTGRES_USER`,
     `POSTGRES_PASSWORD`, `RABBITMQ_USER`, `RABBITMQ_PASS`.
  2. Subir: `docker compose --project-directory . -f docker/docker-compose.yml up -d`.
  3. Validar config sem executar: `docker compose --project-directory . -f docker/docker-compose.yml config`.
- **Endpoints/inspeção na IDE:**
  - Logs: `docker compose --project-directory . -f docker/docker-compose.yml logs -f postgres rabbitmq`.
  - RabbitMQ UI: <http://localhost:15672> (guest / guest).
  - pgAdmin UI: <http://localhost:5050> (`admin@schoolqueue.com` / `admin`) —
     server `school-queue-db` já vem importado via `servers.json`.
  - Saúde do Postgres: `docker exec school_queue_db pg_isready -U queue_user -d school_queue_db`.
- **Resultado esperado:** containers `school_queue_db`, `school_queue_rabbitmq`,
  `school_queue_pgadmin` em estado `healthy`; RabbitMQ UI mostra a fila
  `queue.notifications` somente após o boot da app + primeira ação; pgAdmin
  lista o servidor `school-queue-db`.

### 🟦 INF-002 — Conectar a aplicação Spring Boot ao Postgres + RabbitMQ #infra #spring

- **Resumo:** `src/main/resources/application.yml` define
  `spring.datasource.url=jdbc:postgresql://localhost:5432/school_queue_db`,
  Flyway ligado (`spring.flyway.locations=classpath:db/migration`) e
  `spring.rabbitmq.host=localhost:5672` (guest/guest). JPA está em modo
  `validate` (não-auto-cria schema).
- **Componentes/Arquivos:**
  - `src/main/resources/application.yml`
  - `src/main/resources/db/migration/V1__init_schema.sql`
- **Pré-condições:** Containers do card INF-001 de pé.
- **Passo a passo:**
  1. Build: `mvn -DskipTests package`.
  2. Subir: `mvn spring-boot:run` (ou `java -jar target/school-pickup-system-0.0.1-SNAPSHOT.jar`).
  3. Logs: acompanhar console Spring; ver mensagem
     `Started SchoolQueueApplication in x.xx seconds`.
  4. Conectar no pgAdmin e rodar
     `SELECT version FROM flyway_schema_history;` → deve ter a `V1`
     registrada automaticamente.
- **Endpoints/inspeção na IDE:**
  - Health-check implícito: `GET http://localhost:8080/api/v1/schools` →
     `200 OK` com lista (pode ser vazia).
  - Spring banner mostra perfil default e migrations aplicadas.
- **Resultado esperado:** Flyway aplica `V1__init_schema.sql` (cria
  `schools`, `classrooms`, `parents`, `students`, `parent_student`,
  `pickup_queue`, habilita `uuid-ossp`); Tomcat sobe na `8080`; primeira
  request aos controllers retorna payload (não `404`).

---

## 🏛️ 1. Visão Geral do Projeto Implementado

- **Nome:** `school-pickup-system` (Maven `com.schoolqueue:school-pickup-system:0.0.1-SNAPSHOT`).
- **Stack:** Java 21, Spring Boot 3.5.3, Maven, JPA/Hibernate, Flyway,
  RabbitMQ (AMQP), Jakarta Validation, JUnit 5 + AssertJ + Mockito,
  Testcontainers, JaCoCo, Spotless (`googleJavaFormat`).
- **Arquitetura:** Hexagonal (Ports & Adapters) estrita.
  - `domain/` — entidades puras, enums, exceções, `ports/in/` (use cases) e
    `ports/out/` (repositórios + notificação). **Zero** Spring/JPA.
  - `application/usecase/` — services que implementam as `ports/in`,
    orquestrando ports `out`. Sem anotações Spring (CONF00 → todos
    `@Bean` explícitos no `BeanConfiguration`).
  - `infrastructure/adapters/in/web/` — `@RestController` + DTOs + mappers +
    `GlobalExceptionHandler`.
  - `infrastructure/adapters/out/persistence/` — entidades JPA, Spring Data
    repositories, mappers de domínio↔entity, adapters que assinam os ports
    `out`.
  - `infrastructure/adapters/out/messaging/` — `RabbitMQNotificationAdapter`
    + DTOs de evento.
  - `infrastructure/config/` — `BeanConfiguration`, `RabbitMQConfig`.
- **Domínio coberto:** gestão de **escolas**, **turmas**, **responsáveis**,
  **alunos** (com vínculo N:N `parent_student`); **fila de embarque**
  dirigida por GPS (`PickupQueueItem` com `latitude`/`longitude` do
  responsável + `ProximityRange` calculado por Haversine vs. coordenadas da
  escola). Eventos publicados no RabbitMQ em 2 routing keys:
  `queue.arrival.announced` e `queue.status.changed`.
- **Sem frontend:** UI não implementada — toda interação é via HTTP
  (Bruno/Insomnia/cURL) ou direto no Postgres/pgAdmin.

---

## 📦 2. Módulos / Funcionalidades

### 🟩 ESC-001 — Gestão de Escolas (CRUD parcial) #escola #rest

- **Resumo:** Mantém a entidade raiz do domínio. Escola carrega GPS
  (`latitude`/`longitude`, `BigDecimal(9,6)`) — pré-requisito para o
  cálculo de proximidade na fila. Rotas implementadas: `POST`, `GET
  (lista)`, `GET /{id}`, `PUT /{id}`. **`DELETE` retornará `405 Method Not
  Allowed`** propositadamente. Atualização troca nome e coordenadas.
- **Componentes/Arquivos:**
  - Domain: `domain/model/School.java`, `domain/exception/SchoolNotFoundException.java`.
  - Use cases: `application/usecase/RegisterSchoolService.java`,
    `FetchSchoolService.java`, `ListSchoolsService.java`,
    `UpdateSchoolService.java`. Registrados em `infrastructure/config/BeanConfiguration.java`.
  - Driven port: `domain/ports/out/SchoolRepositoryPort.java`.
  - Driving port: `domain/ports/in/{Register,Fetch,List,Update}SchoolUseCase.java`.
  - Adapter: `infrastructure/adapters/out/persistence/SchoolPersistenceAdapter.java` +
    `entity/SchoolEntity.java` + `mapper/SchoolEntityMapper.java` +
    `repository/SpringDataSchoolRepository.java`.
  - Driving adapter: `infrastructure/adapters/in/web/SchoolController.java`
    + DTOs (`RegisterSchoolRequest`, `UpdateSchoolRequest`,
    `SchoolResponse`) + `mapper/SchoolDtoMapper.java`.
  - Migração: `src/main/resources/db/migration/V1__init_schema.sql` (tabela
    `schools`).
  - Tests: `SchoolTest` (domínio), `Register/Fetch/List/UpdateSchoolServiceTest`
    (Mockito), `SchoolPersistenceAdapterIT` (Testcontainers),
    `SchoolEntityMapperTest`, `SchoolControllerWebTest`
    (`@WebMvcTest` + MockMvc).
  - Bruno: `bruno/Schools/{Create,Get,List,Update,Delete} School.bru`,
    `Create School Without Coordinates.bru`.
- **Pré-condições:** Containers `postgres` + `rabbitmq` de pé; aplicação
  Spring Boot rodando na `8080` (INF-002).
- **Passo a passo (via HTTP):**
  1. **Criar escola**:
     ```
     POST http://localhost:8080/api/v1/schools
     Content-Type: application/json

     { "name": "Escola Central", "latitude": -23.550520, "longitude": -46.633308 }
     ```
     ⇒ `201 Created`, header `Location: /api/v1/schools/<uuid>`, body com
     o `id` gerado.
  2. **Listar**:
     ```
     GET http://localhost:8080/api/v1/schools
     ```
     ⇒ `200 OK`, JSON array.
  3. **Buscar por id**:
     ```
     GET http://localhost:8080/api/v1/schools/<uuid>
     ```
     ⇒ `200 OK`; com id inexistente ⇒ `404` (`SchoolNotFoundException`
     → `GlobalExceptionHandler`, `field=schoolId`).
  4. **Atualizar** (nome + coordenadas):
     ```
     PUT http://localhost:8080/api/v1/schools/<uuid>
     { "name": "Escola Central Atualizada", "latitude": -23.550000, "longitude": -46.633000 }
     ```
     ⇒ `200 OK`, retorna o objeto atualizado.
  5. **Deletar (negativo)**:
     ```
     DELETE http://localhost:8080/api/v1/schools/<uuid>
     ```
     ⇒ `405 Method Not Allowed` (não há service; controller responde
     propositalmente).
  6. **Validação (negativo)**:
     ```
     POST http://localhost:8080/api/v1/schools
     { "name": "", "latitude": null, "longitude": null }
     ```
     ⇒ `400 Bad Request` com
     `{"status":400,"errors":[{"field":"name","message":"must not be blank"}, ...]}`.
- **Endpoints/inspeção na IDE:**
  - cURL criar:
    ```bash
    curl -i -X POST http://localhost:8080/api/v1/schools \
      -H "Content-Type: application/json" \
      -d '{"name":"Escola Central","latitude":-23.550520,"longitude":-46.633308}'
    ```
  - Bruno: abrir `bruno/Schools/Create School.bru` (variável
    `schoolId` é gravada automaticamente).
  - Postgres: `SELECT id, name, latitude, longitude FROM schools;` via
    pgAdmin ou
    `docker exec school_queue_db psql -U queue_user -d school_queue_db -c "SELECT * FROM schools;"`.
- **Resultado esperado:** escola persistida com UUID gerado pelo
  `uuid-ossp` no Postgres; retorno consistente com a entidade; erros de
  validação retornando 400 com payload `ValidationErrorResponse`; 404 com
  `field=schoolId` quando id inexistente.

### 🟩 ESC-002 — Gestão de Turmas (Classrooms) #turma #rest

- **Resumo:** Turmas pertencem a uma escola (`school_id` FK). Rotas:
  `POST`, `GET /{id}`, `GET /school/{schoolId}`, `PUT /{id}`. `DELETE`
  também retorna `405`.
- **Componentes/Arquivos:**
  - Domain: `domain/model/Classroom.java`, `domain/exception/ClassroomNotFoundException.java`.
  - Use cases: `RegisterClassroomService`, `FetchClassroomService`,
    `ListClassroomsBySchoolService`, `UpdateClassroomService`.
  - Driven port: `ClassroomRepositoryPort`.
  - Adapter: `ClassroomPersistenceAdapter` + `ClassroomEntity` +
    `ClassroomEntityMapper` + `SpringDataClassroomRepository`.
  - Driving adapter: `ClassroomController` + DTOs + `ClassroomDtoMapper`.
  - Migração: tabela `classrooms` na `V1`.
  - Tests: `ClassroomTest`, services `*ServiceTest`, IT
    `SchoolPersistenceAdapterIT` (parcialmente cobre `classrooms`),
    `ClassroomEntityMapperTest`, `ClassroomControllerWebTest`.
  - Bruno: `bruno/Classrooms/{Create,Get,List Classrooms By School,Update,Delete} Classroom.bru`.
- **Pré-condições:** Pelo menos uma escola criada (ESC-001).
- **Passo a passo (via HTTP):**
  1. **Criar**:
     ```
     POST http://localhost:8080/api/v1/classrooms
     { "schoolId": "<uuid da escola>", "name": "Turma A" }
     ```
     ⇒ `201 Created`, body com `id`.
  2. **Buscar por id**:
     ```
     GET http://localhost:8080/api/v1/classrooms/<uuid>
     ```
     ⇒ `200 OK`; id inexistente ⇒ `404` (`field=classroomId`).
  3. **Listar por escola**:
     ```
     GET http://localhost:8080/api/v1/classrooms/school/<uuid da escola>
     ```
     ⇒ `200 OK`, array.
  4. **Atualizar** (reaponta para outra escola e/ou renomeia):
     ```
     PUT http://localhost:8080/api/v1/classrooms/<uuid>
     { "schoolId": "<uuid>", "name": "Turma A Atualizada" }
     ```
     ⇒ `200 OK`.
  5. **Validação (negativo)**:
     ```
     POST /api/v1/classrooms { "schoolId": null, "name": "" }
     ```
     ⇒ `400`.
- **Endpoints/inspeção na IDE:**
  - cURL listar por escola:
    ```bash
    curl http://localhost:8080/api/v1/classrooms/school/<uuid-escola>
    ```
  - Postgres: `SELECT id, school_id, name FROM classrooms;`.
- **Resultado esperado:** turma persistida com FK para `schools`; listagem
  filtra por `school_id`; atualização altera `school_id`/`name` e mantém
  o mesmo `id`.

### 🟩 ESC-003 — Gestão de Responsáveis (Parents) #responsavel #rest

- **Resumo:** Responsáveis guardam `name` + `phone` (string livre). Rotas:
  `POST`, `GET (lista)`, `GET /{id}`, `PUT /{id}`. `DELETE` ⇒ `405`.
- **Componentes/Arquivos:**
  - Domain: `domain/model/Parent.java`, `domain/exception/ParentNotFoundException.java`.
  - Use cases: `RegisterParentService`, `FetchParentService`,
    `ListParentsService`, `UpdateParentService`.
  - Driven port: `ParentRepositoryPort`.
  - Adapter: `ParentPersistenceAdapter` + `ParentEntity` +
    `ParentEntityMapper` + `SpringDataParentRepository`.
  - Driving adapter: `ParentController` + DTOs + `ParentDtoMapper`.
  - Migração: tabela `parents` na `V1`.
  - Tests: `ParentTest`, services `*ServiceTest`,
    `ParentEntityMapperTest`, `ParentControllerWebTest`.
  - Bruno: `bruno/Parents/{Create,Get,List,Update,Delete} Parent.bru`.
- **Pré-condições:** Containers + app de pé (INF).
- **Passo a passo (via HTTP):**
  1. **Criar**:
     ```
     POST http://localhost:8080/api/v1/parents
     { "name": "Maria Souza", "phone": "11999998888" }
     ```
     ⇒ `201 Created`, `Location: /api/v1/parents/<uuid>`.
  2. **Listar**:
     ```
     GET http://localhost:8080/api/v1/parents
     ```
     ⇒ `200 OK` array.
  3. **Buscar**:
     ```
     GET /api/v1/parents/<uuid>
     ```
     ⇒ `200` ou `404` (`field=parentId`).
  4. **Atualizar**:
     ```
     PUT /api/v1/parents/<uuid>
     { "name": "Maria Souza Atualizada", "phone": "11988887777" }
     ```
     ⇒ `200`.
  5. **Validação (negativo)**:
     ```
     POST /api/v1/parents { "name": "", "phone": "" }
     ```
     ⇒ `400`.
- **Endpoints/inspeção na IDE:**
  - Bruno: `bruno/Parents/Create Parent.bru` salva `parentId`.
  - Postgres: `SELECT id, name, phone FROM parents;`.
- **Resultado esperado:** responsável persistido; vínculos com alunos
  ainda não expostos por endpoint dedicado (veja ESC-004).

### 🟩 ESC-004 — Gestão de Alunos (Students) + Vínculo Responsável #aluno #rest

- **Resumo:** Alunos pertencem a uma escola e a uma turma (`school_id`,
  `classroom_id` FKs). Mantêm lista de responsáveis vinculados via tabela
  N:N `parent_student`. Rotas: `POST`, `GET /{id}`,
  `GET /school/{schoolId}`, `GET /classroom/{classroomId}`, `PUT /{id}`,
  `DELETE` ⇒ `405`. `RegisterStudentService` valida existência de escola,
  turma e cada responsável antes de persistir; depois chama
  `ParentStudentLinkRepositoryPort.replaceParentsOfStudent(...)` (que faz
  `deleteByStudentId` + insert em transação).
- **Componentes/Arquivos:**
  - Domain: `domain/model/Student.java`, `domain/exception/StudentNotFoundException.java`.
  - Use cases: `RegisterStudentService`, `FetchStudentService`,
    `ListStudentsBySchoolService`, `ListStudentsByClassroomService`,
    `UpdateStudentService` (todos `@Bean` em `BeanConfiguration`).
  - Driven ports: `StudentRepositoryPort`, `ParentStudentLinkRepositoryPort`.
  - Adapters: `StudentPersistenceAdapter`, `ParentStudentLinkPersistenceAdapter`
    (+ `ParentStudentEntity`, `SpringDataParentStudentRepository` com queries
    JPQL `findParentIdsByStudentId`, `findStudentIdsByParentId`,
    `deleteByStudentId`).
  - Driving adapter: `StudentController` + DTOs (`RegisterStudentRequest`,
    `UpdateStudentRequest`, `StudentResponse`) + `StudentDtoMapper` (resolve
    `parentIds` via `linkPort.findParentsOfStudent(...)`).
  - Migração: tabelas `students` e `parent_student` (PK composta) na `V1`.
  - Tests: `StudentTest`, services `*ServiceTest`,
    `StudentEntityMapperTest`, `StudentControllerWebTest`.
  - Bruno: `bruno/Students/{Register,Get,List Students By School,List
    Students By Classroom,Update,Delete} Student.bru`.
- **Pré-condições:** 1 escola (ESC-001), 1 turma (ESC-002), ≥1 responsável
  (ESC-003) já criados.
- **Passo a passo (via HTTP):**
  1. **Criar aluno com vínculo**:
     ```
     POST http://localhost:8080/api/v1/students
     {
       "schoolId": "<uuid-escola>",
       "classroomId": "<uuid-turma>",
       "name": "João Aluno Exemplo",
       "parentIds": ["<uuid-responsavel>"]
     }
     ```
     ⇒ `201 Created`, body inclui `parentIds` resolvidos pelo
     `ParentStudentLinkRepositoryPort`.
  2. **Buscar por id**:
     ```
     GET /api/v1/students/<uuid>
     ```
     ⇒ `200 OK` com `parentIds`. Id inexistente ⇒ `404`.
  3. **Listar por escola**:
     ```
     GET /api/v1/students/school/<uuid-escola>
     ```
     ⇒ `200` array.
  4. **Listar por turma**:
     ```
     GET /api/v1/students/classroom/<uuid-turma>
     ```
     ⇒ `200` array.
  5. **Atualizar (troca de turma e/ou responsáveis)**:
     ```
     PUT /api/v1/students/<uuid>
     {
       "schoolId": "<uuid-escola>",
       "classroomId": "<uuid-turma>",
       "name": "João Aluno Atualizado",
       "parentIds": ["<uuid-responsavel>"]
     }
     ```
     ⇒ `200 OK`; o `replaceParentsOfStudent` apaga e recria os vínculos
     em transação.
  6. **Validação (negativo)**:
     ```
     POST /api/v1/students { "schoolId": null, "classroomId": null, "name": "", "parentIds": [] }
     ```
     ⇒ `400` (`@NotEmpty parentIds`, `@NotBlank name`, etc.).
  7. **Erro de domínio (negativo)**:
     ```
     POST /api/v1/students { "schoolId": "<uuid-inexistente>", "classroomId": "<uuid>", "name": "x", "parentIds": ["<uuid>"] }
     ```
     ⇒ `404` (`SchoolNotFoundException`, `field=schoolId`).
- **Endpoints/inspeção na IDE:**
  - Bruno `Register Student.bru` salva `studentId`.
  - cURL:
    ```bash
    curl -X POST http://localhost:8080/api/v1/students \
      -H "Content-Type: application/json" \
      -d "{\"schoolId\":\"$SCHOOL_ID\",\"classroomId\":\"$CLASSROOM_ID\",\"name\":\"João\",\"parentIds\":[\"$PARENT_ID\"]}"
    ```
  - Postgres: `SELECT * FROM students;` e
    `SELECT * FROM parent_student WHERE student_id = '<uuid>';`.
- **Resultado esperado:** aluno persistido; `parent_student` com
  `student_id` e `parent_id` preenchidos; updates sobrescrevem o conjunto
  de responsáveis (não acumula duplicatas); erros 404 quando FK não
  existe; 400 quando payload vazio/inválido.

### 🟧 FILA-001 — Anunciar Chegada (`POST /api/v1/queue/announce`) #fila #gps #rabbitmq

- **Resumo:** Use case central. Recebe GPS do responsável
  (`latitude`/`longitude`), busca escola para calcular o
  `ProximityRange` inicial via Haversine (FAR/MEDIUM/CLOSE), abre um item
  na fila em `EN_ROUTE`, marca `called = true` se já nasceu em `CLOSE`,
  persiste e publica `ArrivalAnnouncedEvent` no RabbitMQ.
  Bloqueios: aluno sem aviso ativo duplicado ⇒ 400; escola inexistente ⇒
  404; GPS obrigatório (`@NotNull` no DTO + check no service).
- **Componentes/Arquivos:**
  - Driving port: `domain/ports/in/AnnounceArrivalUseCase.java`
    (record `AnnounceArrivalCommand`).
  - Service: `application/usecase/AnnounceArrivalService.java`.
  - Modelo de domínio: `domain/model/PickupQueueItem.java` (transições +
    GPS), `domain/model/ProximityRange.java` (Haversine + thresholds
    0.5 km CLOSE / 2.0 km MEDIUM).
  - Exceção: `SchoolNotFoundException`, `IllegalStateException`
    ("Já existe um aviso de saída ativo para este aluno").
  - Driving adapter: `PickupQueueController` + `AnnounceArrivalRequest` +
    `QueueItemResponse` + `QueueDtoMapper`.
  - Driven ports: `QueueRepositoryPort`, `QueueNotificationPort`,
    `SchoolRepositoryPort`.
  - Adapters: `QueuePersistenceAdapter`, `RabbitMQNotificationAdapter`,
    `SchoolPersistenceAdapter`.
  - Eventos: `ArrivalAnnouncedEvent` (queueItemId, studentId, schoolId,
    status, called, currentRange, occurredAt).
  - Tests: `AnnounceArrivalServiceTest`, `PickupQueueItemTest`,
    `ProximityRangeTest`, `PickupQueueControllerWebTest`,
    `RabbitMQNotificationAdapterTest`, `QueueEntityMapperTest`.
- **Pré-condições:** Escola com GPS, aluno, responsável e turma criados
  (ESC-001 a ESC-004). Containers + RabbitMQ de pé.
- **Passo a passo:**
  1. **Anunciar (distância curta)** com Bruno
     `bruno/Queue/Announce Arrival.bru` (variáveis `schoolId`,
     `studentId`, `parentId` populadas pelos cards anteriores):
     ```
     POST http://localhost:8080/api/v1/queue/announce
     {
       "schoolId": "{{schoolId}}",
       "studentId": "{{studentId}}",
       "parentId": "{{parentId}}",
       "latitude": -23.550520,
       "longitude": -46.633308
     }
     ```
     ⇒ `200 OK`. Como o `latitude/longitude` é idêntico ao cadastrado da
     escola, `currentRange = CLOSE`, `called = true`, `journeyStatus =
     EN_ROUTE`. Bruno grava `queueItemId`.
  2. **Anunciar (distância longa)** alterando `latitude`/`longitude`
     (ex.: `-23.7,-46.8`) ⇒ `currentRange = FAR`, `called = false`.
  3. **Duplicado (negativo)**: reusar mesmo `studentId` com outro
     `parentId` ⇒ `400 Bad Request`,
     `{"status":400,"errors":[{"field":"state","message":"Já existe um
     aviso de saída ativo para este aluno."}]}`.
  4. **Escola inexistente (negativo)**:
     `POST /queue/announce` com `schoolId` inexistente ⇒ `404`,
     `field=schoolId`.
  5. **Sem GPS (negativo)**:
     `POST /queue/announce` com `latitude=null` ⇒ `400` na camada
     `@NotNull` do DTO.
- **Endpoints/inspeção na IDE:**
  - cURL:
    ```bash
    curl -X POST http://localhost:8080/api/v1/queue/announce \
      -H "Content-Type: application/json" \
      -d "{\"schoolId\":\"$SCHOOL_ID\",\"studentId\":\"$STUDENT_ID\",\"parentId\":\"$PARENT_ID\",\"latitude\":-23.550520,\"longitude\":-46.633308}"
    ```
  - Log do Spring: linha
    `Published arrival.announced for queueItemId=<uuid>` (vem do
    `RabbitMQNotificationAdapter`).
  - RabbitMQ UI (<http://localhost:15672>): em **Exchanges →
    school.queue.events**, ver mensagem publicada na routing key
    `queue.arrival.announced`; em **Queues → queue.notifications**, ver
    `Messages ready` crescendo.
  - Postgres:
    `SELECT id, journey_status, called, current_range, latitude, longitude
    FROM pickup_queue ORDER BY created_at DESC LIMIT 5;`.
- **Resultado esperado:** item persistido em `pickup_queue` com
  `journey_status = EN_ROUTE`, `current_range = CLOSE`/`MEDIUM`/`FAR`
  conforme a distância, `called = true` somente em CLOSE; evento
  `ArrivalAnnouncedEvent` chegando em `queue.notifications`; logs no
  console com `Published arrival.announced`.

### 🟧 FILA-002 — Atualizar Estado do Item da Fila
(`PATCH /api/v1/queue/{id}/status`) #fila #state-machine

- **Resumo:** Aplica uma transição no `PickupQueueItem`. Use case recebe um
  `UpdateQueueStatusCommand(queueItemId, action)` onde `action` é uma
  `sealed interface QueueAction` com 4 tipos:
  - `UpdateRange(ProximityRange)` → recalcula range e **auto-seta
    `called = true`** se entrar em `CLOSE` e ainda não havia sido chamado.
  - `MarkAsArrived()` → `ARRIVED` (somente de `EN_ROUTE`).
  - `MarkAsCompleted()` → `COMPLETED` (somente se `called == true`).
  - `Cancel()` → `CANCELLED` (qualquer estado, exceto `COMPLETED`).
  Após salvar, publica `StatusChangedEvent` (routing key
  `queue.status.changed`) carregando `previousStatus` + `newStatus`.
- **Componentes/Arquivos:**
  - Driving port: `UpdateQueueStatusUseCase.java` (sealed types +
    `UpdateQueueStatusCommand`).
  - Service: `UpdateQueueStatusService.java` (switch sobre `action`).
  - Modelo: `PickupQueueItem.updateRange`, `markAsArrived`,
    `markAsCompleted`, `cancel` (lançam `InvalidQueueStateException` em
    transição ilegal).
  - Driving adapter: `PickupQueueController` + `UpdateStatusRequest` +
    `QueueActionMapper` (mapeia string `action` → record selado; valida
    `newRange` quando `UPDATE_RANGE`).
  - Driven: `QueueRepositoryPort.save` + `QueueNotificationPort.notifyStatusChanged`.
  - Adapter RabbitMQ: `RabbitMQNotificationAdapter.notifyStatusChanged` →
    `StatusChangedEvent`.
  - Tests: `UpdateQueueStatusServiceTest`, `PickupQueueItemTest`,
    `PickupQueueControllerWebTest` (inclui cenário 409 via
    `InvalidQueueStateException`), `RabbitMQNotificationAdapterTest`.
  - Bruno: `bruno/Queue/Update Status.bru`.
- **Pré-condições:** Pelo menos um item criado via FILA-001.
- **Passo a passo:**
  1. **Atualizar range**:
     ```
     PATCH http://localhost:8080/api/v1/queue/<queueItemId>/status
     { "action": "UPDATE_RANGE", "newRange": "CLOSE" }
     ```
     ⇒ `200 OK`, `currentRange=CLOSE`, `called=true` (se ainda não era).
  2. **Marcar chegada**:
     ```
     PATCH /api/v1/queue/<queueItemId>/status
     { "action": "MARK_AS_ARRIVED" }
     ```
     ⇒ `200`, `journeyStatus=ARRIVED`. Tentar de novo ⇒ `409 Conflict`
     (`InvalidQueueStateException` → `GlobalExceptionHandler`,
     `field=state`).
  3. **Finalizar entrega** (somente após `called=true`):
     ```
     PATCH /api/v1/queue/<queueItemId>/status
     { "action": "MARK_AS_COMPLETED" }
     ```
     ⇒ `200`, `journeyStatus=COMPLETED`. Se ainda não havia sido
     chamado ⇒ `409` "Aluno não pode ser entregue sem ter sido chamado".
  4. **Cancelar** (não funciona em `COMPLETED`):
     ```
     PATCH /api/v1/queue/<queueItemId>/status
     { "action": "CANCEL" }
     ```
     ⇒ `200`, `journeyStatus=CANCELLED`. Em `COMPLETED` ⇒ `409`.
  5. **Negativos:**
     - `action: ""` ⇒ `400` (`@NotBlank`).
     - `action: "FOO"` ⇒ `400` (`IllegalStateException("Unknown action: FOO")`).
     - `action: "UPDATE_RANGE"` sem `newRange` ⇒ `400` ("newRange is
       required for UPDATE_RANGE").
- **Endpoints/inspeção na IDE:**
  - cURL:
    ```bash
    curl -X PATCH http://localhost:8080/api/v1/queue/$QUEUE_ITEM_ID/status \
      -H "Content-Type: application/json" \
      -d '{"action":"MARK_AS_COMPLETED"}'
    ```
  - Log: `Published status.changed for queueItemId=<uuid> previousStatus=ARRIVED newStatus=COMPLETED`.
  - RabbitMQ UI: mensagem em `queue.notifications` com header
    `__TypeId__ = StatusChangedEvent`.
  - Postgres: `SELECT id, journey_status, called, current_range,
    updated_at FROM pickup_queue WHERE id = '<uuid>';`.
- **Resultado esperado:** cada transição válida persiste o novo estado e
  publica um `StatusChangedEvent`; transições inválidas retornam `409`
  com `field=state`; payloads malformados ⇒ `400`.

### 🟧 FILA-003 — Consultar Fila Ativa
(`GET /api/v1/queue/school/{schoolId}/active`) #fila #read

- **Resumo:** Lista itens em `EN_ROUTE` ou `ARRIVED` da escola,
  ordenados por `createdAt` ascendente (fila de embarque real).
  Implementação: `FetchActiveQueueUseCase` → `QueueRepositoryPort
  .findBySchoolIdAndStatusIn(...)` + `Comparator.comparing(createdAt)`.
- **Componentes/Arquivos:**
  - Driving port: `FetchActiveQueueUseCase`.
  - Service: `FetchActiveQueueService` (constante `ACTIVE_STATUSES =
    [EN_ROUTE, ARRIVED]`).
  - Driving adapter: `PickupQueueController.activeQueue` (path
    `/school/{schoolId}/active`).
  - Driven: `SpringDataQueueRepository.findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc`.
  - Tests: `FetchActiveQueueServiceTest`, `PickupQueueControllerWebTest`,
    `SpringDataQueueRepositoryTest`.
  - Bruno: `bruno/Queue/List Active Queue.bru`.
- **Pré-condições:** Escola criada (ESC-001) e ≥1 item de fila (FILA-001).
- **Passo a passo:**
  1. ```
     GET http://localhost:8080/api/v1/queue/school/<uuid-escola>/active
     ```
     ⇒ `200 OK`, JSON array de `QueueItemResponse` ordenado por
     `createdAt` crescente.
  2. Após `MARK_AS_COMPLETED` ou `CANCEL`, o item desaparece da
     listagem (não está em status ativo).
  3. Após `MARK_AS_ARRIVED`, o item continua na lista (ARRIVED é ativo).
- **Endpoints/inspeção na IDE:**
  - Bruno: a coleção salva `studentId` em `script:post-response` do
    `Register Student`; `List Active Queue` valida via assert
    `res("..[?(@.studentId == '{{studentId}}')]"): isNotEmpty`.
  - cURL:
    ```bash
    curl http://localhost:8080/api/v1/queue/school/$SCHOOL_ID/active | jq
    ```
- **Resultado esperado:** apenas itens com `journeyStatus ∈ {EN_ROUTE,
  ARRIVED}`; ordenados pelo instante de criação; payload inclui todos os
  11 campos do `QueueItemResponse`.

### 🟨 MSG-001 — Publicação de Eventos no RabbitMQ #messaging #rabbitmq

- **Resumo:** Toda mudança relevante no domínio (anúncio de chegada,
  mudança de estado) emite um evento JSON no exchange topic
  `school.queue.events`. A fila `queue.notifications` é durável e está
  bindada em ambas as routing keys. Producer confirmado
  (`publisher-confirm-type` é o default do Spring Boot, mensagens são
  `convertAndSend`).
- **Componentes/Arquivos:**
  - Driven port: `domain/ports/out/QueueNotificationPort.java`.
  - Adapter: `infrastructure/adapters/out/messaging/RabbitMQNotificationAdapter.java`
    (usa `RabbitTemplate` + `Jackson2JsonMessageConverter`).
  - Configuração: `infrastructure/config/RabbitMQConfig.java`
    (`TopicExchange`, `Queue`, 2 `Binding`, `Jackson2JsonMessageConverter`,
    `RabbitTemplateCustomizer`).
  - DTOs: `ArrivalAnnouncedEvent`, `StatusChangedEvent` (records imutáveis).
  - Tests: `RabbitMQNotificationAdapterTest` (Mockito) + `RabbitMQConfigTest`.
- **Pré-condições:** RabbitMQ de pé (INF-001); ter disparado pelo menos
  um `POST /queue/announce` ou `PATCH /queue/{id}/status`.
- **Passo a passo:**
  1. Disparar `POST /queue/announce` (FILA-001) e/ou `PATCH /queue/{id}/status`
     (FILA-002).
  2. Abrir RabbitMQ UI: <http://localhost:15672> (guest/guest) → **Queues →
     queue.notifications**. Ver `Messages ready` > 0.
  3. Clicar em **Get messages** (ack mode `Ack message requeue true`) e
     inspecionar o payload:
     - `ArrivalAnnouncedEvent` em `queue.arrival.announced`.
     - `StatusChangedEvent` em `queue.status.changed`, contendo
       `previousStatus` + `newStatus`.
  4. Spring console deve mostrar logs `Published arrival.announced ...` /
     `Published status.changed ...`.
- **Endpoints/inspeção na IDE:**
  - Logback: `grep "Published" school_queue_app.log`.
  - Endpoint REST opcional: `GET /api/actuator/rabbitmq` não está exposto;
    usar a UI do broker.
- **Resultado esperado:** 2 routing keys ativas; mensagens JSON
  serializadas; headers AMQP com `__TypeId__` apontando para o record
  do evento; nenhum consumer foi implementado ainda — a fila apenas
  acumula (decisão LAC07 — backlog).

### 🟨 DB-001 — Migrações Flyway + JPA Validate #db #flyway

- **Resumo:** `spring.jpa.hibernate.ddl-auto = validate` (não mexe no
  schema). `spring.flyway.enabled = true` aplica migrations em
  `classpath:db/migration`. Existe apenas a `V1__init_schema.sql` que
  cria 6 tabelas e habilita `uuid-ossp`.
- **Componentes/Arquivos:**
  - `src/main/resources/db/migration/V1__init_schema.sql`.
  - `src/main/resources/application.yml`.
- **Pré-condições:** Postgres ativo; primeira subida da app.
- **Passo a passo:**
  1. Subir a aplicação; logs do Flyway:
     `Successfully validated 1 migration ... Schema "public" is up to date.`.
  2. Via pgAdmin ou
     `docker exec school_queue_db psql -U queue_user -d school_queue_db -c "\\dn+"`:
     confirmar schema `public` com as 6 tabelas (`schools`, `classrooms`,
     `parents`, `students`, `parent_student`, `pickup_queue`).
  3. `SELECT * FROM flyway_schema_history;` deve ter 1 linha (`V1`,
     `success`).
  4. `SELECT extname FROM pg_extension WHERE extname = 'uuid-ossp';`
     deve retornar 1 linha.
- **Endpoints/inspeção na IDE:**
  - Logs do app no startup.
  - pgAdmin: árvore **school-queue-db → Databases → school_queue_db →
    Schemas → public → Tables**.
- **Resultado esperado:** todas as 6 tabelas presentes; entidades JPA
  carregam sem erro (`validate` confirma se o mapeamento bate com o
  schema).

---

## 🔬 3. Mapa rápido de cobertura

| Componente | Unit (`mvn test`) | Integration (`mvn verify` + Testcontainers) | Web (`@WebMvcTest`) |
|---|---|---|---|
| `School` (domínio) | `SchoolTest` | — | — |
| `Classroom` | `ClassroomTest` | — | — |
| `Parent` | `ParentTest` | — | — |
| `Student` | `StudentTest` | — | — |
| `PickupQueueItem` | `PickupQueueItemTest` | — | — |
| `ProximityRange` | `ProximityRangeTest` | — | — |
| `LocationSharingStatus` | `LocationSharingStatusTest` | — | — |
| `School*Service` | `Register/Fetch/List/UpdateSchoolServiceTest` | — | — |
| `Classroom*Service` | `Register/Fetch/List/Update…ServiceTest` | — | — |
| `Parent*Service` | `Register/Fetch/List/Update…ServiceTest` | — | — |
| `Student*Service` | `Register/Fetch/List/Update…ServiceTest` | — | — |
| `AnnounceArrivalService` | `AnnounceArrivalServiceTest` | — | — |
| `UpdateQueueStatusService` | `UpdateQueueStatusServiceTest` | — | — |
| `FetchActiveQueueService` | `FetchActiveQueueServiceTest` | — | — |
| `SchoolPersistenceAdapter` | — | `SchoolPersistenceAdapterIT` | — |
| `QueuePersistenceAdapter` | — | `QueuePersistenceAdapterIT` | — |
| `SpringDataQueueRepository` | `SpringDataQueueRepositoryTest` | — | — |
| Mappers JPA | `School/Classroom/Parent/Student/QueueEntityMapperTest` | — | — |
| `SchoolController` | — | — | `SchoolControllerWebTest` |
| `ClassroomController` | — | — | `ClassroomControllerWebTest` |
| `ParentController` | — | — | `ParentControllerWebTest` |
| `StudentController` | — | — | `StudentControllerWebTest` |
| `PickupQueueController` | — | — | `PickupQueueControllerWebTest` |
| `GlobalExceptionHandler` | `GlobalExceptionHandlerTest` (standalone) | — | — |
| `RabbitMQNotificationAdapter` | `RabbitMQNotificationAdapterTest` | — | — |
| `RabbitMQConfig` | `RabbitMQConfigTest` | — | — |
| `BeanConfiguration` | `BeanConfigurationTest` | — | — |

Comandos úteis para reproduzir tudo:

```bash
# Unit + Web (sem Docker)
mvn -DskipITs test

# Tudo (sobe Testcontainers Postgres + RabbitMQ)
mvn verify
```

---

## ✅ 4. Roteiro mínimo de teste E2E manual

1. **INF-001** subir compose; **INF-002** subir app.
2. **ESC-001** criar escola com GPS (lat/lng ≈ da cidade). Bruno salva
   `schoolId`.
3. **ESC-002** criar turma nessa escola. Bruno salva `classroomId`.
4. **ESC-003** criar responsável. Bruno salva `parentId`.
5. **ESC-004** criar aluno vinculando o responsável. Bruno salva
   `studentId`.
6. **FILA-001** anunciar chegada com GPS próximo (CLOSE) — `called=true`
   já nasce; abrir RabbitMQ UI e ver 1 mensagem em `queue.notifications`.
7. **FILA-002** sequenciar `MARK_AS_ARRIVED` → `MARK_AS_COMPLETED`;
   conferir logs `Published status.changed` e atualizar visualização na
   UI do broker.
8. **FILA-003** `GET /queue/school/{schoolId}/active` deve estar vazio
   (item foi para `COMPLETED`).
9. Criar novo anúncio com `studentId` diferente e distância grande
   (FAR) → `called=false`. Repetir `FILA-002` aplicando `UPDATE_RANGE`
   para `CLOSE` e ver `called=true` ser setado automaticamente.
10. **DB-001** abrir pgAdmin e inspecionar `pickup_queue` e
    `parent_student`.