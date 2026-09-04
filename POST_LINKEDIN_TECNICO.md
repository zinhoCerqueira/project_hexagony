# 🚀 POST LINKEDIN TÉCNICO — school-pickup-system

> Versão final para publicação. Mantida em arquivo `.md` para ajustes
> rápidos antes de colar no LinkedIn.

---

## 🪝 1. Hook / Introdução Impactante

Construí, do zero, um sistema de **fila de embarque escolar dirigida por
GPS** em Java 21 + Spring Boot 3, usando **Arquitetura Hexagonal
estrita** como coluna vertebral. O grande desafio: o **compartilhamento
de localização do responsável** não é apenas um "feature flag" — ele é o
**próprio motor da fila**. A distância entre o GPS do pai e o da escola
(Haversine puro, em `domain/`) decide se o aluno já pode ser chamado,
qual a ordem de embarque e quando o item passa de `EN_ROUTE` para
`ARRIVED`/`COMPLETED`. Tudo isso sem `double` mágico espalhado: as
coordenadas são `BigDecimal(9,6)` no Postgres e viajam como tal do banco
até o DTO HTTP.

Quando finalizei a esteira inteira — endpoints REST + casos de uso +
adaptadores JPA + mensageria AMQP — olhei para o monorepositório e vi:
**117 classes de produção, 44 de teste, 0 anotações Spring no domínio,
0 anotações JPA no Core, 1 migração Flyway, 2 routing keys no
RabbitMQ**. Esse post é um raio-x do que foi entregue e das decisões
que valem a pena roubar.

---

## 🧠 2. Destaques Arquiteturais & Tecnológicos

### Stack

- **Java 21** (records, sealed interfaces, pattern matching em `switch`).
- **Spring Boot 3.5.3**, **Spring Data JPA**, **Spring AMQP**,
  **Spring Validation**, **Flyway**.
- **PostgreSQL 16** (`uuid-ossp`, `NUMERIC(9,6)` para GPS, FKs
  explícitas).
- **RabbitMQ 3** (Topic Exchange `school.queue.events`, fila durável
  `queue.notifications`, bindings em `queue.arrival.announced` e
  `queue.status.changed`).
- **Testcontainers** (Postgres + RabbitMQ) + **JUnit 5** + **AssertJ** +
  **Mockito**.
- **JaCoCo** (gate de cobertura em `mvn verify`) e **Spotless** com
  `googleJavaFormat` para manter o estilo objetivo e revisar PR com
  diff limpo.
- **Bruno** como cliente HTTP versionado (coleções em `bruno/`),
  servindo também de documentação executável dos contratos.

### Padrões de projeto aplicados

- **Hexagonal / Ports & Adapters** — o `domain/` é Java puro. As
  fronteiras são interfaces em `domain/ports/in/` (use cases) e
  `domain/ports/out/` (repositórios + notificação). Os services em
  `application/usecase/` **não têm `@Service`** — eles são instanciados
  por `@Bean` explícitos em `BeanConfiguration` (decisão CONF00), mantendo
  o Core livre de framework.
- **Sealed interfaces para state machines** —
  `UpdateQueueStatusUseCase.QueueAction permits UpdateRange,
  MarkAsArrived, MarkAsCompleted, Cancel` força o compilador a lembrar de
  tratar cada caso no `switch` do `UpdateQueueStatusService` e do
  `QueueActionMapper`. Acabaram os `if/else if` silenciosos.
- **Records como DTOs e comandos** —
  `AnnounceArrivalCommand`, `RegisterStudentRequest`, `QueueItemResponse`,
  `ArrivalAnnouncedEvent`, `StatusChangedEvent` são todos `record`s
  imutáveis — zero boilerplate, zero `setter`, validação via Bean
  Validation direto no record component (`@NotNull UUID schoolId`).
- **Domain-Driven Design tático** — `School`, `Classroom`, `Parent`,
  `Student`, `PickupQueueItem` como entidades de domínio com
  invariantes no construtor e métodos de transição (`markAsArrived`,
  `markAsCompleted`, `cancel`, `updateRange`). Transições ilegais
  lançam `InvalidQueueStateException` (mapeada para `409 Conflict` pelo
  `GlobalExceptionHandler`).
- **Strategy/Adapter para persistência** — cada aggregate tem um
  `*PersistenceAdapter` (driven) que assina o port `out` e faz a
  tradução Domain ↔ JPA Entity via `*EntityMapper`. Trocar Postgres por
  outro banco é trocar **um adapter**; o Core não sente.
- **Event-Driven** — `QueueNotificationPort` é a saída para RabbitMQ.
  Toda mudança relevante na fila publica um JSON numa routing key;
  consumers (a serem adicionados) ganham的通知 assíncrono sem acoplar o
  use case à mensageria.

### Soluções para gargalos & complexidade

- **Haversine onde precisa, fórmula pura onde dá** — o cálculo de
  `ProximityRange` (CLOSE ≤ 0.5 km, MEDIUM ≤ 2 km, FAR) mora em
  `domain/model/ProximityRange.java`. Reaproveitável em testes
  determinísticos, sem mock. Testes unitários cobrem o cálculo
  inteiro; o `Application` confia nele e nada mais.
- **Auto-chamada ao entrar em CLOSE** — `PickupQueueItem.updateRange`
  seta `called = true` automaticamente quando o range vira `CLOSE` e
  ainda não havia sido chamado. Isso evita coordenar manualmente
  "chamei o aluno?" no use case e impede inconsistência entre o range
  e o flag.
- **Lookup indexado por status ativo** —
  `SpringDataQueueRepository.findBySchoolIdAndJourneyStatusInOrderByCreatedAtAsc`
  + `findFirstByStudentIdAndJourneyStatusInOrderByCreatedAtDesc`
  sustentam tanto a fila ordenada quanto o bloqueio de "aluno já na
  fila" sem varrer tabela.
- **`replaceParentsOfStudent` em transação** — o vínculo N:N
  aluno↔responsável é sempre reescrito em uma transação
  (`@Transactional` no adapter), evitando `parent_student` órfão e
  duplicatas acumuladas.
- **Validação no boundary, invariante no Core** — `@NotNull`/`@NotBlank`
  no DTO de entrada + checagens de domínio no service (`latitude`/
  `longitude` não nulos, escola existente). A regra de negócio mais
  sensível ("não pode finalizar sem ter sido chamado") vive no domínio
  e é testada sem Spring.
- **Migração imutável + `validate` no Hibernate** —
  `spring.jpa.hibernate.ddl-auto = validate` impede drift entre o
  schema Flyway e o mapeamento JPA; qualquer divergência estoura na
  subida.
- **Mapper dedicado para mensagens** — `ArrivalAnnouncedEvent` e
  `StatusChangedEvent` carregam `previousStatus` para que consumidores
  possam reagir a transições, não apenas a snapshots.

---

## 🧩 3. Principais Módulos/Funcionalidades Entregues

- **CRUD parcial de Escolas** com GPS obrigatório (BigDecimal lat/lng);
  validação 400 e 404 com payload estruturado
  (`ValidationErrorResponse`).
- **CRUD parcial de Turmas** com FK à escola; listagem por escola.
- **CRUD parcial de Responsáveis** (nome + telefone).
- **CRUD parcial de Alunos** com vínculo N:N a responsáveis, validação
  cruzada de `schoolId`/`classroomId`/`parentIds`, atualização que
  reescreve o conjunto de responsáveis em transação.
- **Anúncio de chegada dirigido por GPS**
  (`POST /api/v1/queue/announce`) — calcula range inicial via
  Haversine, abre item em `EN_ROUTE`, marca `called=true` se já nasceu
  em `CLOSE`, publica `ArrivalAnnouncedEvent`.
- **Atualização de estado do item da fila**
  (`PATCH /api/v1/queue/{id}/status`) com 4 ações seladas:
  `UPDATE_RANGE` (recalcula range e auto-chama), `MARK_AS_ARRIVED`,
  `MARK_AS_COMPLETED`, `CANCEL` — todas validadas pela máquina de
  estados do domínio (`409` em transição inválida).
- **Consulta da fila ativa**
  (`GET /api/v1/queue/school/{schoolId}/active`) — itens em `EN_ROUTE`
  ou `ARRIVED`, ordenados por `createdAt` ascendente.
- **Mensageria AMQP** ligada a dois eventos do domínio
  (`queue.arrival.announced`, `queue.status.changed`) com serialização
  JSON via `Jackson2JsonMessageConverter`.
- **Tratamento global de erros** com payload uniforme
  (`FieldError`/`ValidationErrorResponse`), separando 400 (validação +
  `IllegalStateException`), 404 (`*NotFoundException`) e 409
  (`InvalidQueueStateException`).
- **Infra local containerizada**: Postgres + RabbitMQ + pgAdmin via
  Compose, com volumes nomeados, `PGDATA` isolado em subdir para reset
  seletivo e `servers.json` versionado para o pgAdmin recriar o server
  automaticamente.
- **Cobertura de testes em pirâmide**: unit de domínio (AssertJ puro),
  unit de services (Mockito), testes `@WebMvcTest` por controller,
  `GlobalExceptionHandler` exercitado de forma isolada, mappers JPA
  com往返 Domain ↔ Entity, ITs Testcontainers para os adapters de
  persistência e adapter AMQP.

---

## 🛠️ 4. Lições Aprendidas & Engenharia de Software

1. **Hexagonal vale o investimento quando o domínio muda.** Hoje o
   modelo é "GPS dirige a fila"; amanhã pode ser "RFID + faixa de
   horário". Como o cálculo de proximidade e a state machine moram no
   `domain/` (Java puro), refatorar o critério de chamada não exige
   tocar em JPA, REST ou RabbitMQ — só no Core e nos testes
   determinísticos.
2. **Records + sealed types = state machines à prova de regressão.**
   Adicionar uma nova ação (`MarkAsNoShow`, por exemplo) obriga o
   compilador a quebrar o `switch` do `UpdateQueueStatusService` e do
   `QueueActionMapper` até todos os call sites serem atualizados.
3. **`@Bean` explícito > component scan quando o Core deve ficar
   puro.** Sacrificar a "mágica" do scan em troca de clareza sobre
   quem instancia o quê vale o preço quando o time precisa confiar que
   `domain/` não importa Spring.
4. **Validação no boundary é guarda, não é regra.** Bean Validation
   filtra payload inválido; a regra de negócio ("não pode finalizar
   sem ter sido chamado") precisa estar no domínio para que qualquer
   adaptador — HTTP hoje, CLI amanhã, batch depois — respeite.
5. **Mensageria é detalhe de infraestrutura.** O `domain/` conhece
   apenas `QueueNotificationPort`; o `RabbitMQNotificationAdapter` é
   swap-able. Isso liberou a MSG02 para implementar
   `notifyStatusChanged` sem tocar no `UpdateQueueStatusService` —
   bastou plugar o método no port.
6. **Testcontainers > H2 para IT de JPA.** Dialeto, tipos `UUID` e
   constraints de Postgres viram primeiro teste de verdade; custo
   aceitável dentro do `mvn verify`.
7. **Versionar `docker/pgadmin/servers.json` salva horas.** Quando o
   volume `pgadmin-data` é perdido (reset de Docker, prune acidental),
   o server `school-queue-db` reaparece na próxima subida sem
   intervenção manual.
8. **Mappers JPA merecem teste próprio.** Erros sutis em
   `reconstitute(...)` (ordem de campos, enums como STRING, BigDecimal vs
   double) só aparecem em runtime se não houver um teste focado de
   ida-e-volta.
9. **`DELETE` deliberadamente `405`** sinaliza para o cliente HTTP
   que aquela operação ainda não foi modelada — mais barato do que
   esquecer e devolver `404` confuso.
10. **Bruno collections viram contrato vivo.** Os arquivos `.bru`
    carregam asserts (`res.status: eq 201`, `res.body.currentRange: eq
    CLOSE`), o que transforma cada collection num teste de fumaça
    executável e revisável em PR.

---

## 📣 5. Call to Action & Hashtags

Se você está construindo sistemas com **Core rico**, **state
machines**, ou precisa **integrar mensageria sem acoplar o domínio**,
esse repositório é um estudo de caso curto e direto: 117 classes de
produção com boundary arquitetural respeitada à risca.

💬 Curiosidades, perguntas ou discordâncias? Comenta aqui — quero
discutir Hexagonal vs. Clean Architecture, sealed types em modelagem de
domínio, e o trade-off de **Haversine no Core** versus no adapter.

👇 Repo público em breve. Se quiser mergulhar antes, peça nos
comentários que eu mando o link.

#Java #SpringBoot #HexagonalArchitecture #PortsAndAdapters
#DomainDrivenDesign #CleanArchitecture #JPA #Flyway
#PostgreSQL #RabbitMQ #EventDriven #StateMachine #SealedTypes
#RecordsPattern #SoftwareEngineering #Backend #Microsserviços
#TestContainers #BrunoAPI #JVM #BackendDevelopment