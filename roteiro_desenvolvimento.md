# 🚀 Roteiro Prático de Estudos: Sistema de Fila de Embarque Escolar (Pickup Queue)

> **Arquitetura Target:** Arquitetura Hexagonal (Ports & Adapters)  
> **Linguagem & Ecossistema:** Java 21, Spring Boot 3.x, Maven  
> **Infraestrutura Local:** Docker & Docker Compose  
> **Estratégia de Validação:** TDD (JUnit 5 + AssertJ + Mockito), testes de integração (Testcontainers), cobertura (JaCoCo) e E2E REST via Bruno Client  

---

## 📋 1. Visão Geral e Contexto do Domínio

### O Problema
No horário de saída das aulas — quando os responsáveis chegam à escola para **buscar (fazer o embarque dos) alunos** —, o trânsito nos arredores torna-se caótico devido a filas duplas e tempo de espera excessivo dos alunos no portão.

### A Solução
O **Pickup Queue System** é um sistema **exclusivamente de embarque**: os pais/responsáveis só usam a aplicação quando vão buscar os alunos na escola. Ao sair de casa, avisam à escola que estão a caminho ("Estou chegando"), compartilhando sua localização; ao chegarem ao perímetro, são identificados pelo range de proximidade. O sistema posiciona o aluno na **Fila de Embarque da Escola**, permitindo que os inspetores/professores na portaria e na sala de aula preparem e encaminhem o aluno com antecedência, otimizando o fluxo e garantindo a segurança. Não há fluxo de desembarque: a entrega da criança na escola não passa pela fila.

---

## 🏛️ 2. Fundamentação da Arquitetura Hexagonal

A **Arquitetura Hexagonal (Ports & Adapters)** propõe a separação total entre o **Domínio/Negócio** e os **Detalhes Técnicos** (Bancos de dados, Frameworks Web, Filas de Mensageria, APIs externas).

```text
                      ┌─────────────────────────────────────────────────────────┐
                      │                        APLICAÇÃO                        │
                      │                                                         │
  ┌───────────────┐   │   ┌─────────────────────────────────────────────────┐   │   ┌───────────────┐
  │  Bruno Client │───┼──►│ [Driving Adapter] Controller REST               │   │   │  PostgreSQL   │
  │   (HTTP)      │   │   └──────────────────────┬──────────────────────────┘   │   │  Database     │
  └───────────────┘   │                          │                              │   └───────▲───────┘
                      │   ┌──────────────────────▼──────────────────────────┐   │           │
                      │   │ [Driving Port] Ingress Queue UseCase            │   │           │
                      │   └──────────────────────┬──────────────────────────┘   │           │
                      │                          │                              │           │
                      │   ┌──────────────────────▼──────────────────────────┐   │           │
                      │   │              DOMÍNIO / CORE DE REGRAS           │   │           │
                      │   │  Entidades: Student, Parent, School, QueueItem  │   │           │
                      │   └──────────────────────┬──────────────────────────┘   │           │
                      │                          │                              │           │
                      │   ┌──────────────────────▼──────────────────────────┐   │           │
                      │   │ [Driven Port] QueueRepositoryPort               │───┼───────────┘
                      │   └──────────────────────┬──────────────────────────┘   │   [Driven Adapter]
                      │                          │                              │   Spring Data JPA
                      │   ┌──────────────────────▼──────────────────────────┐   │   
                      │   │ [Driven Port] NotificationPublisherPort         │───┼───────────┐
                      │   └─────────────────────────────────────────────────┘   │           │
                      │                                                         │   ┌───────▼───────┐
                      └─────────────────────────────────────────────────────────┘   │   RabbitMQ    │
                                                                                    └───────────────┘
```

### Glossário de Termos:
- **Core (Domain):** O coração da aplicação. Contém entidades de negócio e regras puras. Livre de bibliotecas ou anotações Spring/JPA.
- **Driving Ports (Portas de Entrada):** Interfaces que expõem o que o sistema é capaz de fazer (Casos de Uso).
- **Driving Adapters (Adaptadores de Entrada):** Componentes que acionam o sistema (ex: Controllers Spring MVC HTTP).
- **Driven Ports (Portas de Saída):** Interfaces que o Core usa quando precisa interagir com recursos externos (ex: Repositórios, Notificadores).
- **Driven Adapters (Adaptadores de Saída):** Implementações reais dos recursos externos (ex: Repositórios JPA, adaptadores RabbitMQ, Clientes Redis).

### Portas do Projeto: objetivo e status atual

#### Driving Ports (`domain.ports.in`) — o que o sistema é capaz de fazer

| Port | Comando | Objetivo | Implementação (status) |
|---|---|---|---|
| `AnnounceArrivalUseCase` | `AnnounceArrivalCommand(schoolId, studentId, parentId, latitude, longitude)` | Iniciar o ciclo de embarque: valida duplicidade ativa do aluno e GPS obrigatório, busca a escola (`SchoolRepositoryPort`), calcula o range inicial pela distância real Haversine (`ProximityRange.fromCoordinates`), cria o `PickupQueueItem` gravando o GPS do responsável, persiste e notifica. Se o range inicial for CLOSE, o aluno já nasce chamado (`called = true`). | ✅ `AnnounceArrivalService`. Adapter HTTP ainda não existe |
| `UpdateQueueStatusUseCase` | `UpdateQueueStatusCommand(queueItemId, QueueAction)` onde `QueueAction` é sealed interface com `UpdateRange(newRange)`, `MarkAsArrived`, `MarkAsCompleted`, `Cancel` | Aplicar transições de estado na fila (dirigidas por GPS ou pela escola): busca o item por id (`QueueItemNotFoundException` se ausente), delega a transição aos métodos de domínio, persiste e notifica com o status anterior. Exceção de domínio `InvalidQueueStateException` propaga sem tratamento. | ✅ `UpdateQueueStatusService` (Task 26), dispatch exaustivo via record patterns |
| `FetchActiveQueueUseCase` | `execute(schoolId)` | Consulta da fila de embarque ativa da escola: itens em `[EN_ROUTE, ARRIVED]`, ordenados por `createdAt` ascendente (ordem de chegada); a flag `called` vem junto para a portaria saber quem já foi chamado. Lista possivelmente vazia; somente leitura. | ✅ `FetchActiveQueueService` (Task 27) |

#### Driven Ports (`domain.ports.out`) — do que o Core precisa do mundo externo

| Port | Contrato | Objetivo | Implementação (status) |
|---|---|---|---|
| `QueueRepositoryPort` | `save`, `findById`, `findBySchoolIdAndStatusIn`, `findActiveByStudentId` | Persistência da fila. O Core nunca fala com o Postgres diretamente. Peças prontas: entidades JPA (Task 29), `SpringDataQueueRepository` com queries derivadas (Task 30), `QueueEntityMapper` Domain↔Entity (Task 31). Falta o `QueuePersistenceAdapter` que assina este contrato e os ITs com Testcontainers. | 🟡 em construção (Fase 4) |
| `QueueNotificationPort` | `notifyStudentArrivalAnnounced(item)`, `notifyStatusChanged(item, previousStatus)` | Publicar eventos da fila para consumidores externos (RabbitMQ planejado): anúncio de chegada e mudanças de estado/auto-chamada. Os services já invocam; falta o adapter de mensageria e a config. | ❌ pendente (adapter + RabbitMQConfig) |
| `SchoolRepositoryPort` | cadastro/consulta de escolas (com GPS) | Suporte cadastral à escola, referência das coordenadas usadas pelo GPS. | ❌ adapter pendente |
| `StudentRepositoryPort` | cadastro/consulta de alunos | Suporte cadastral ao aluno vinculado à escola/turma. | ❌ adapter pendente |

> **Regra da arquitetura:** os services de `application` dependem apenas destas interfaces. Nenhuma classe do Core/Application importa Spring, JPA ou driver de banco — a troca de tecnologia acontece só nos adapters.

---

## 🗂️ 3. Estrutura Completa do Projeto (Maven Package Layout)

```plaintext
school-pickup-system/
├── docker/
│   ├── docker-compose.yml
│   └── postgres/
│       └── init.sql
├── bruno/
│   ├── bruno.json
│   ├── Schools/
│   │   └── Create School.bru
│   ├── Students/
│   │   └── Register Student.bru
│   └── Queue/
│       ├── Announce Arrival.bru
│       └── List Active Queue.bru
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── schoolqueue/
│   │   │           ├── SchoolQueueApplication.java
│   │   │           │
│   │   │           ├── domain/                         <-- CORE (Zero libs externas)
│   │   │           │   ├── model/
│   │   │           │   │   ├── School.java            <-- inclui latitude/longitude (GPS)
│   │   │           │   │   ├── Classroom.java
│   │   │           │   │   ├── Student.java
│   │   │           │   │   ├── Parent.java
│   │   │           │   │   ├── PickupQueueItem.java   <-- journeyStatus + called + currentRange
│   │   │           │   │   ├── QueueStatus.java       <-- (EN_ROUTE, ARRIVED, COMPLETED, CANCELLED)
│   │   │           │   │   ├── ProximityRange.java    <-- (FAR, MEDIUM, CLOSE)
│   │   │           │   │   └── LocationSharingStatus.java <-- (ACTIVE, EXPIRED)
│   │   │           │   ├── exception/
│   │   │           │   │   ├── StudentNotFoundException.java
│   │   │           │   │   ├── InvalidQueueStateException.java
│   │   │           │   │   └── InvalidSharingSessionException.java
│   │   │           │   └── ports/                      <-- CONTRATOS
│   │   │               ├── in/                         <-- Driving Ports (Casos de Uso)
│   │   │               │   ├── AnnounceArrivalUseCase.java
│   │   │               │   ├── UpdateQueueStatusUseCase.java
│   │   │               │   └── FetchActiveQueueUseCase.java
│   │   │               └── out/                        <-- Driven Ports (Infra)
│   │   │                   ├── SchoolRepositoryPort.java
│   │   │                   ├── StudentRepositoryPort.java
│   │   │                   ├── QueueRepositoryPort.java
│   │   │                   └── QueueNotificationPort.java
│   │   │           │
│   │   │           ├── application/                    <-- ORQUESTRAÇÃO & SERVIÇOS
│   │   │           │   └── usecase/
│   │   │           │       ├── AnnounceArrivalService.java
│   │   │           │       ├── UpdateQueueStatusService.java
│   │   │           │       └── FetchActiveQueueService.java
│   │   │           │
│   │   │           └── infrastructure/                 <-- TECNOLOGIAS & ADAPTADORES
│   │   │               ├── adapters/
│   │   │               │   ├── in/                     <-- Driving Adapters
│   │   │               │   │   └── web/
│   │   │               │   │       ├── PickupQueueController.java
│   │   │               │   │       ├── dto/
│   │   │               │   │       │   ├── AnnounceArrivalRequest.java
│   │   │               │   │       │   └── QueueItemResponse.java
│   │   │               │   │       └── mapper/
│   │   │               │   │           └── QueueDtoMapper.java
│   │   │               │   │
│   │   │               │   └── out/                    <-- Driven Adapters
│   │   │               │       ├── persistence/
│   │   │               │       │   ├── entity/         <-- Entidades JPA
│   │   │               │       │   │   ├── SchoolEntity.java
│   │   │               │       │   │   ├── StudentEntity.java
│   │   │               │       │   │   └── PickupQueueEntity.java
│   │   │               │       │   ├── repository/     <-- Interfaces Spring Data JPA
│   │   │               │       │   │   └── SpringDataQueueRepository.java
│   │   │               │       │   ├── mapper/
│   │   │               │       │   │   └── QueueEntityMapper.java
│   │   │               │       │   └── QueuePersistenceAdapter.java
│   │   │               │       │
│   │   │               │       └── messaging/
│   │   │               │           └── RabbitMQNotificationAdapter.java
│   │   │               │
│   │   │               └── config/                     <-- Bean Configurations
│   │   │                   ├── BeanConfiguration.java
│   │   │                   └── RabbitMQConfig.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/schoolqueue/
│               ├── domain/                              <-- Testes unitários do Core puro
│               │   ├── PickupQueueItemTest.java
│               │   ├── SchoolTest.java
│               │   ├── ClassroomTest.java
│               │   ├── ParentTest.java
│               │   ├── StudentTest.java
│               │   └── LocationSharingSessionTest.java
│               ├── application/
│               │   └── usecase/                         <-- Testes dos services (Mockito)
│               │       ├── AnnounceArrivalServiceTest.java
│               │       ├── UpdateQueueStatusServiceTest.java
│               │       ├── FetchActiveQueueServiceTest.java
│               │       ├── StartLocationSharingServiceTest.java
│               │       ├── UpdateParentLocationServiceTest.java
│               │       └── FetchSharedLocationServiceTest.java
│               ├── infrastructure/
│               │   ├── adapters/in/web/                 <-- Testes dos controllers (MockMvc)
│               │   │   ├── PickupQueueControllerTest.java
│               │   │   ├── LocationSharingControllerTest.java
│               │   │   └── GlobalExceptionHandlerTest.java
│               │   └── adapters/out/persistence/        <-- Testes de repositórios/adapter
│               │       ├── SpringDataQueueRepositoryTest.java
│               │       ├── QueuePersistenceAdapterTest.java
│               │       ├── SpringDataLocationSharingRepositoryTest.java
│               │       └── LocationSharingPersistenceAdapterTest.java
│               └── integration/                         <-- E2E (Testcontainers, sufixo *IT)
│                   ├── PickupQueueFlowIT.java
│                   └── LocationSharingFlowIT.java
└── pom.xml
```

---

## 🧩 4. Modelo de Dados e Entidades

### Diagrama de Entidades
```plaintext
 +------------------+           +------------------+
 |      School      | 1       * |    Classroom     |
 |------------------|<----------|------------------|
 | id: UUID         |           | id: UUID         |
 | name: String     |           | name: String     |
 | latitude: BigDecimal (GPS) |  +------------------+
 | longitude: BigDecimal (GPS)|
 +------------------+                    ^
          ^                              |
          | 1                            | 1
          |                              |
          | *                            | *
 +------------------+           +------------------+
 |      Parent      | *       * |     Student      |
 |------------------|-----------|------------------|
 | id: UUID         |           | id: UUID         |
 | name: String     |           | name: String     |
 | phone: String    |           +------------------+
 +------------------+                    ^
          |                              | 1
          | 1                            |
          +--------------+---------------+
                         |
                         | *
              +------------------------------+
              |        PickupQueueItem       |
              |------------------------------|
              | id: UUID                     |
               | journeyStatus: QueueStatus   |
               | called: boolean              |
               | currentRange: ProximityRange |
               | latitude/longitude (pai, GPS)|
               | created/updatedAt            |
              +------------------------------+
```

### Padrão das Entidades Puras (Domain Model)

As entidades cadastrais do Core (`School`, `Classroom`, `Parent`, `Student`) seguem este padrão:

- **Identidade imutável:** apenas o campo `id` — o UUID da própria entidade — é `final`. Ele identifica a entidade e nunca muda.
- **Atributos mutáveis:** todos os demais campos são `private` sem `final` e possuem setters (`setName`, `setSchoolId`, ...). Isso vale também para **referências a outras entidades** (ex.: `classroom.schoolId` não é `final`, pois uma turma pode ser realocada para outra escola).
- **`id` opcional no construtor:** quando `null`, o construtor gera `UUID.randomUUID()`.
- **Sem anotações JPA/Lombok:** Core puro, sem dependência do Spring.
- **Acessores estilo record** (`id()`, `name()`) combinados com setters JavaBeans (`setName(...)`).

| Campo | Final? | Motivo |
|---|---|---|
| `id` (da própria entidade) | ✅ `final` | Identidade, imutável |
| `schoolId` / `classroomId` (referências) | ❌ mutável | Vínculo pode ser alterado |
| `name`, `phone`, etc. | ❌ mutável | Dado pode ser atualizado |

```java
public class Student {
  private final UUID id;   // único campo final
  private UUID schoolId;   // referência: mutável
  private UUID classroomId; // referência: mutável
  private String name;     // dado: mutável

  public Student(UUID id, UUID schoolId, UUID classroomId, String name) {
    this.id = id != null ? id : UUID.randomUUID();
    this.schoolId = schoolId;
    this.classroomId = classroomId;
    this.name = name;
  }

  // acessores de leitura + setters
}
```

> Entidades com ciclo de vida próprio (ex.: `PickupQueueItem`, `LocationSharingSession`) fogem deste padrão: além do `id`, mantêm `final` em campos imutáveis de contexto (ex.: `createdAt`) e expõem métodos de negócio que transicionam o estado em vez de setters genéricos.

### Estados da Fila (QueueStatus) e Ranges de Proximidade

O fluxo da fila é **dirigido por GPS**: o responsável anuncia a chegada já compartilhando sua localização em tempo real, e a distância até a escola define o range de proximidade que controla quando o aluno é chamado.

**`QueueStatus` (eixo principal da jornada):**
- **EN_ROUTE:** Pai notificou que está a caminho.
- **ARRIVED:** Pai chegou no perímetro escolar (opcional; não é pré-requisito para chamar ou entregar).
- **COMPLETED:** Aluno entregue ao responsável (terminal).
- **CANCELLED:** Chamada descartada/cancelada (terminal).

> O `CALLED` **não é mais um estado do enum**: vira a flag booleana `called` na entidade, permitindo combinações naturais como "EN_ROUTE + CALLED" ou "ARRIVED + CALLED".

**`ProximityRange` (3 tiers, dirigidos por distância real):**
- **FAR:** distância > 2 km do GPS da escola.
- **MEDIUM:** 0,5 < distância ≤ 2 km.
- **CLOSE:** distância ≤ 0,5 km.

> Calculados no Core via Haversine (`ProximityRange.fromCoordinates(...)`) entre o GPS do responsável e o da escola. Limiares fixos (`fromDistanceKm`), configuráveis via `application.yml` em task futura. Ao entrar no range **CLOSE**, o sistema marca `called = true` automaticamente. O `etaMinutes` foi removido do comando e do schema.

---

## 🛠️ 5. Configuração do Ambiente Docker

Crie o arquivo `docker/docker-compose.yml` para rodar o PostgreSQL e o RabbitMQ.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: school_queue_db
    environment:
      POSTGRES_DB: school_queue_db
      POSTGRES_USER: queue_user
      POSTGRES_PASSWORD: queue_password
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U queue_user -d school_queue_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: school_queue_rabbitmq
    ports:
      - "5672:5672"   # AMQP Protocol
      - "15672:15672" # Management Dashboard
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "-q", "ping"]
      interval: 8s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: school_queue_pgadmin
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@schoolqueue.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"   # Web UI
    volumes:
      - pgadmin-data:/var/lib/pgadmin

volumes:
  pgdata:
  pgadmin-data:
```

### Persistência, volumes e backup do banco

> O estado do projeto mora em **dois volumes Docker nomeados**:
> `pgdata` (cluster Postgres com schema + dados) e `pgadmin-data`
> (configuração do pgAdmin). Perder o `pgdata` significa perder
> **todas** as escolas, alunos, pais e filas cadastradas. Esta subseção
> documenta o que é sensível e como se proteger.

**O que cada volume guarda:**

| Volume | Conteúdo | O que acontece se for removido |
|---|---|---|
| `pgdata` | Cluster Postgres (`/var/lib/postgresql/data`) — schema Flyway + dados de `schools`, `students`, `pickup_queue`, etc. | Postgres sobe vazio, Flyway reaplica `V1__init_schema.sql`, **todos os dados somem**. |
| `pgadmin-data` | `pgadmin4.db` (servers cadastrados, preferências) | pgAdmin reseta; o server `school-queue-db` reaparece via `docker/pgadmin/servers.json` (montado pelo Compose). |

**Comandos que apagam dados sem aviso:**

- `docker compose down -v` — remove **todos** os volumes nomeados do projeto. **Nunca usar** em ambiente de estudo sem backup.
- `docker volume rm <nome>` / `docker volume prune` / `docker system prune --volumes` — apagam volumes órfãos (sem container referenciando).
- Reset de fábrica do Docker Desktop, reinstalação/atualização do Docker, ou falha de disco — podem descartar `/var/lib/docker/volumes` inteiro.
- O `docker-compose.yml` **não** declara `restart: always` em nenhum serviço. Se um container cair e ninguém reiniciar, o volume fica órfão e exposto ao próximo `prune`.

**Backups recomendados antes de qualquer mexida arriscada em infra:**

```bash
# 1. Backup lógico (gera um .sql versionável)
docker exec school_queue_db \
  pg_dump -U queue_user -d school_queue_db > backup-$(date +%Y%m%d-%H%M).sql

# Restaurar:
cat backup-YYYYMMDD-HHMM.sql | docker exec -i school_queue_db \
  psql -U queue_user -d school_queue_db

# 2. Backup binário do volume inteiro (mais pesado; copia fiel do cluster)
docker run --rm -v project_hexagony_pgdata:/from -v $(pwd):/to alpine \
  tar czf /to/pgdata-backup-$(date +%Y%m%d).tar.gz -C /from .
```

**Antes de qualquer `prune`, conferir a lista:**

```bash
docker volume prune --dry-run
```

**Inspeção rápida do banco (sem entrar no pgAdmin):**

```bash
# schemas e tabelas
docker exec school_queue_db psql -U queue_user -d school_queue_db -c "\dn+"
docker exec school_queue_db psql -U queue_user -d school_queue_db -c "\dt public.*"

# atividade acumulada por banco
docker exec school_queue_db psql -U queue_user -d school_queue_db -c \
  "SELECT datname, numbackends, xact_commit, tup_inserted FROM pg_stat_database;"

# conexões ativas (HikariCP, pgAdmin, psql, etc.)
docker exec school_queue_db psql -U queue_user -d school_queue_db -c \
  "SELECT pid, application_name, state, query_start FROM pg_stat_activity WHERE datname='school_queue_db';"
```

> O `docker/pgadmin/servers.json` versionado garante que o server
> `school-queue-db` reaparece no pgAdmin mesmo se o `pgadmin-data` for
> perdido. Isso **não** vale para o `pgdata` — esse é só `pg_dump`.

### Schema Versionado via Flyway (`src/main/resources/db/migration/V1__init_schema.sql`)

O schema não é mais criado por script de inicialização do container: quem cria as tabelas é o **Flyway**, executado no startup da aplicação (`spring.flyway.locations: classpath:db/migration`), com `ddl-auto=validate` garantindo que os mapeamentos JPA batem com o DDL. O Compose sobe apenas o Postgres vazio + volume.

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE schools (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    latitude NUMERIC(9,6),
    longitude NUMERIC(9,6)
);

CREATE TABLE classrooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES schools(id),
    name VARCHAR(100) NOT NULL
);

CREATE TABLE parents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL
);

CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES schools(id),
    classroom_id UUID NOT NULL REFERENCES classrooms(id),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE parent_student (
    parent_id UUID REFERENCES parents(id),
    student_id UUID REFERENCES students(id),
    PRIMARY KEY (parent_id, student_id)
);

CREATE TABLE pickup_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    school_id UUID NOT NULL REFERENCES schools(id),
    student_id UUID NOT NULL REFERENCES students(id),
    parent_id UUID NOT NULL REFERENCES parents(id),
    journey_status VARCHAR(50) NOT NULL,
    called BOOLEAN NOT NULL DEFAULT FALSE,
    current_range VARCHAR(50) NOT NULL,
    estimated_eta_minutes INT,
    latitude NUMERIC(9,6),
    longitude NUMERIC(9,6),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

> `pickup_queue` já nasce com o modelo dirigido por GPS/ETA: `journey_status`, flag `called` (auto-chamada), `current_range` e o GPS do responsável (`latitude`/`longitude`, nulos — GPS opcional).

---

## 💻 6. Implementação Guiada Passo a Passo

### Passo 1: O Núcleo do Domínio (Entidade Pura)
Sem anotações JPA (`@Entity`) ou Lombok no Core para manter desacoplamento total.

```java
// File: src/main/java/com/schoolqueue/domain/model/PickupQueueItem.java
package com.schoolqueue.domain.model;

import com.schoolqueue.domain.exception.InvalidQueueStateException;
import java.time.Instant;
import java.util.UUID;

public class PickupQueueItem {
    private final UUID id;
    private final UUID schoolId;
    private final UUID studentId;
    private final UUID parentId;
    private QueueStatus journeyStatus;
    private boolean called;
    private ProximityRange currentRange;
    private BigDecimal latitude;   // GPS do pai
    private BigDecimal longitude;  // GPS do pai
    private final Instant createdAt;
    private Instant updatedAt;

    public PickupQueueItem(UUID id, UUID schoolId, UUID studentId, UUID parentId,
                           ProximityRange initialRange) {
        this.id = id != null ? id : UUID.randomUUID();
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.parentId = parentId;
        this.journeyStatus = QueueStatus.EN_ROUTE;
        this.called = initialRange == ProximityRange.CLOSE;
        this.currentRange = initialRange;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Regras de Negócio Puras (dirigidas por GPS)
    public void updateRange(ProximityRange newRange) {
        if (this.journeyStatus == QueueStatus.COMPLETED || this.journeyStatus == QueueStatus.CANCELLED) {
            throw new InvalidQueueStateException("Fila já finalizada ou cancelada");
        }
        this.currentRange = newRange;
        if (newRange == ProximityRange.CLOSE && !this.called) {
            this.called = true; // auto-chamada ao entrar no range mais próximo
        }
        this.updatedAt = Instant.now();
    }

    public void markAsArrived() {
        if (this.journeyStatus != QueueStatus.EN_ROUTE) {
            throw new InvalidQueueStateException("Apenas responsáveis a caminho podem ser marcados como 'Chegou'");
        }
        this.journeyStatus = QueueStatus.ARRIVED;
        this.updatedAt = Instant.now();
    }

    public void markAsCompleted() {
        if (!this.called) {
            throw new InvalidQueueStateException("Aluno não pode ser entregue sem ter sido chamado");
        }
        this.journeyStatus = QueueStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (this.journeyStatus == QueueStatus.COMPLETED) {
            throw new InvalidQueueStateException("Entrega concluída não pode ser cancelada");
        }
        this.journeyStatus = QueueStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    // Getters omitidos para brevidade...
}
```

### Passo 2: As Portas (Ports)

**Driven Port (Porta de Saída)**
```java
// File: src/main/java/com/schoolqueue/domain/ports/out/QueueRepositoryPort.java
package com.schoolqueue.domain.ports.out;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.QueueStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepositoryPort {
    PickupQueueItem save(PickupQueueItem item);
    Optional<PickupQueueItem> findById(UUID id);
    List<PickupQueueItem> findBySchoolIdAndStatusIn(UUID schoolId, List<QueueStatus> statuses);
    Optional<PickupQueueItem> findActiveByStudentId(UUID studentId);
}
```

**Driving Port (Porta de Entrada)**
```java
// File: src/main/java/com/schoolqueue/domain/ports/in/AnnounceArrivalUseCase.java
package com.schoolqueue.domain.ports.in;

import com.schoolqueue.domain.model.PickupQueueItem;
import java.util.UUID;

public interface AnnounceArrivalUseCase {
    PickupQueueItem execute(AnnounceArrivalCommand command);

    record AnnounceArrivalCommand(
        UUID schoolId,
        UUID studentId,
        UUID parentId,
        BigDecimal latitude,   // GPS do pai (obrigatório)
        BigDecimal longitude   // GPS do pai (obrigatório)
    ) {}
}
```

### Passo 3: O Caso de Uso (Application Service)

```java
// File: src/main/java/com/schoolqueue/application/usecase/AnnounceArrivalService.java
package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.exception.SchoolNotFoundException;
import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.model.ProximityRange;
import com.schoolqueue.domain.model.School;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import com.schoolqueue.domain.ports.out.SchoolRepositoryPort;

public class AnnounceArrivalService implements AnnounceArrivalUseCase {

    private final QueueRepositoryPort queueRepositoryPort;
    private final QueueNotificationPort notificationPort;
    private final SchoolRepositoryPort schoolRepositoryPort;

    public AnnounceArrivalService(QueueRepositoryPort queueRepositoryPort, QueueNotificationPort notificationPort, SchoolRepositoryPort schoolRepositoryPort) {
        this.queueRepositoryPort = queueRepositoryPort;
        this.notificationPort = notificationPort;
        this.schoolRepositoryPort = schoolRepositoryPort;
    }

    @Override
    public PickupQueueItem execute(AnnounceArrivalCommand command) {
        // Valida se já existe uma chamada ativa para esse aluno
        queueRepositoryPort.findActiveByStudentId(command.studentId())
            .ifPresent(item -> {
                throw new IllegalStateException("Já existe um aviso de saída ativo para este aluno.");
            });

        if (command.latitude() == null || command.longitude() == null) {
            throw new IllegalArgumentException("Latitude and longitude must not be null");
        }

        // Busca a escola e calcula o range inicial pela distância real (Haversine)
        School school = schoolRepositoryPort.findById(command.schoolId())
            .orElseThrow(() -> new SchoolNotFoundException("Escola não encontrada"));
        ProximityRange initialRange = ProximityRange.fromCoordinates(
            command.latitude(), command.longitude(), school.latitude(), school.longitude());

        PickupQueueItem newItem = new PickupQueueItem(
            null,
            command.schoolId(),
            command.studentId(),
            command.parentId(),
            initialRange
        );
        newItem.updateLocation(command.latitude(), command.longitude());

        PickupQueueItem savedItem = queueRepositoryPort.save(newItem);
        notificationPort.notifyStudentArrivalAnnounced(savedItem);

        return savedItem;
    }
}
```

### Passo 4: Adaptadores de Infraestrutura (Driven & Driving)

**Injeção de Dependência via Spring Configuration**
Como as classes do Core não possuem anotações `@Service` do Spring, declaramos os Beans explicitamente:

```java
// File: src/main/java/com/schoolqueue/infrastructure/config/BeanConfiguration.java
package com.schoolqueue.infrastructure.config;

import com.schoolqueue.application.usecase.AnnounceArrivalService;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public AnnounceArrivalUseCase announceArrivalUseCase(
            QueueRepositoryPort queueRepositoryPort,
            QueueNotificationPort notificationPort) {
        return new AnnounceArrivalService(queueRepositoryPort, notificationPort);
    }
}
```

**Driving Adapter (REST Controller)**
```java
// File: src/main/java/com/schoolqueue/infrastructure/adapters/in/web/PickupQueueController.java
package com.schoolqueue.infrastructure.adapters.in.web;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.infrastructure.adapters.in.web.dto.AnnounceArrivalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/queue")
public class PickupQueueController {

    private final AnnounceArrivalUseCase announceArrivalUseCase;

    public PickupQueueController(AnnounceArrivalUseCase announceArrivalUseCase) {
        this.announceArrivalUseCase = announceArrivalUseCase;
    }

    @PostMapping("/announce")
    public ResponseEntity<PickupQueueItem> announceArrival(@RequestBody AnnounceArrivalRequest request) {
        var command = new AnnounceArrivalUseCase.AnnounceArrivalCommand(
            request.schoolId(),
            request.studentId(),
            request.parentId(),
            request.latitude(),
            request.longitude()
        );
        
        PickupQueueItem result = announceArrivalUseCase.execute(command);
        return ResponseEntity.ok(result);
    }
}
```

---

## 🧪 7. Testes e Validação com Bruno Client

O Bruno é um cliente API open-source que armazena as coleções em arquivos texto `.bru` dentro do repositório Git.

### Estrutura dos Arquivos Bruno

**1. Arquivo de Coleção (`bruno/bruno.json`)**
```json
{
  "version": "1",
  "name": "School Pickup Queue API",
  "type": "collection"
}
```

**2. Teste de Anúncio de Chegada (`bruno/Queue/Announce Arrival.bru`)**
```plaintext
meta {
  name: Announce Arrival (Estou Chegando)
  type: http
  seq: 1
}

post {
  url: http://localhost:8080/api/v1/queue/announce
  body: json
  auth: none
}

body:json {
  {
    "schoolId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
    "studentId": "c9bf9e57-1685-4c89-bafb-ff5af830be8a",
    "parentId": "d3b07384-d113-424a-4f0b-2232938b2bb4",
    "latitude": -23.5505,
    "longitude": -46.6333
  }
}

assert {
  res.status: eq 200
  res.body.status: eq EN_ROUTE
}
```

---

## 🧪 7.1. Estratégia de Testes: TDD e Pipeline de Qualidade

Além da validação E2E via Bruno, o projeto adota **Test-Driven Development (TDD)** em todas as camadas. A regra é: **primeiro o teste que falha (RED), depois a implementação mínima (GREEN) e por fim o refactor (REFACTOR)**. No dashboard, cada card de desenvolvimento que envolve código possui um card de teste correspondente logo antes, na ordem TDD.

### Pirâmide de Testes

```text
        ╱╲        E2E REST (Bruno Client) — fluxo ponta a ponta manual
       ╱══╲       Integração (@SpringBootTest + Testcontainers) — sufixo *IT
      ╱════╲      Slice: @WebMvcTest (controllers) + @DataJpaTest (repositórios)
     ╱══════╲     Unit Services (JUnit 5 + Mockito nas ports)
    ╱════════╲    Unit Domain Core (JUnit 5 + AssertJ, zero Spring)
```

### Convenções de Teste

- Pacote de teste espelha o de produção (`src/test/java/com/schoolqueue/...`).
- JUnit 5 + **AssertJ** (assertions fluent) + **Mockito** (mocks de ports).
- `@DisplayName` descritivo em inglês e nomes de método no padrão Given/When/Then (ex.: `shouldTransitionToArrivedWhenEnRoute`).
- Sufixo `*Test` = teste unitário (Surefire, **sem Docker**); sufixo `*IT` = teste de integração (Failsafe + Testcontainers).

### Infraestrutura de Testes

- **Testcontainers Postgres**: repositórios e testes de integração rodam contra um PostgreSQL real, fiel ao schema (`timestamptz`, `UUID`, Flyway). Exige Docker durante o `mvn verify`.
- **JaCoCo**: relatório de cobertura no ciclo `verify` com gate de **linha >= 80%** e **ramo >= 70%** nos pacotes `domain` e `application`.
- **Failsafe**: executa os testes de integração `*IT` no `verify`.

### Comandos do Pipeline

| Comando | Escopo |
|---|---|
| `mvn test` | Somente testes unitários (rápido, sem Docker) |
| `mvn verify` | Unitários + integração (`*IT`) + JaCoCo + Spotless |
| `mvn spring-boot:run` | Sobe a aplicação para validar o E2E no Bruno |

### Exemplo: Teste de Domínio (TDD)

```java
// src/test/java/com/schoolqueue/domain/PickupQueueItemTest.java
class PickupQueueItemTest {

    @Test
    @DisplayName("markAsArrived transitions EN_ROUTE to ARRIVED")
    void shouldTransitionToArrivedWhenEnRoute() {
        var item = new PickupQueueItem(null, schoolId, studentId, parentId, 10);

        item.markAsArrived();

        assertThat(item.getStatus()).isEqualTo(QueueStatus.ARRIVED);
    }
}
```

### Exemplo: Teste de Service com Mockito (TDD)

```java
// src/test/java/com/schoolqueue/application/usecase/AnnounceArrivalServiceTest.java
class AnnounceArrivalServiceTest {

    @Mock QueueRepositoryPort repositoryPort;
    @Mock QueueNotificationPort notificationPort;

    @Test
    @DisplayName("execute saves and notifies a new EN_ROUTE item")
    void shouldCreateAndNotify() {
        var command = new AnnounceArrivalUseCase.AnnounceArrivalCommand(
            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), 10);
        var service = new AnnounceArrivalService(repositoryPort, notificationPort);

        var result = service.execute(command);

        assertThat(result.getStatus()).isEqualTo(QueueStatus.EN_ROUTE);
        verify(repositoryPort).save(any(PickupQueueItem.class));
        verify(notificationPort).notifyStudentArrivalAnnounced(any(PickupQueueItem.class));
    }
}
```

---

## 📍 8. Feature Core: Compartilhamento de GPS que Dirige o Fluxo da Fila

> **Importante:** o GPS deixa de ser uma feature complementar separada e passa a **dirigir o fluxo da fila**. A escola cadastra suas coordenadas (GPS da escola) e o responsável compartilha a localização em tempo real; a distância entre eles define o range de proximidade que controla a chamada do aluno.

### Onde o GPS entra no sistema (estado atual, implementado)

1. **Ponto de referência — GPS da escola (obrigatório):** `School` exige `latitude`/`longitude` no construtor (`IllegalArgumentException` se ausentes) e as colunas em `schools` são `NOT NULL`. É a âncora do cálculo de distância.
2. **Entrada do GPS do responsável — no anúncio de chegada:** o `AnnounceArrivalCommand` carrega apenas `latitude` e `longitude` (o `etaMinutes` foi removido). O GPS é obrigatório: coordenadas nulas bloqueiam o anúncio com `IllegalArgumentException`. As coordenadas são gravadas no item via `PickupQueueItem.updateLocation(...)` e persistidas em `pickup_queue.latitude/longitude`.
3. **Classificação de proximidade — distância real (hoje):** o `AnnounceArrivalService` busca a escola via `SchoolRepositoryPort` e calcula `ProximityRange.fromCoordinates(...)`: `≤ 0,5 km → CLOSE`, `0,5–2 km → MEDIUM`, `> 2 km → FAR`.
4. **Auto-chamada:** ao nascer com range CLOSE ou entrar nele depois (`UpdateRange(CLOSE)`), o domínio marca `called = true` automaticamente — é o gatilho que libera a entrega do aluno.
5. **O que ainda não existe:** sessões contínuas de compartilhamento com recálculo periódico do range a cada atualização de posição — é a Fase 7 abaixo, que dirigirá a auto-chamada em tempo real (no anúncio, a distância já é real).

### O Problema
O responsável avisa que está indo buscar o aluno, mas a escola não tem como estimar com precisão o momento real da chegada ao portão, gerando chamadas prematuras ou atrasadas do aluno. Sem o GPS, a transição de estados é rígida (`EN_ROUTE → ARRIVED → CALLED`) e depende de ações manuais.

### A Solução
Quando o responsável notifica que está indo buscar (ação do `AnnounceArrivalUseCase`), o sistema inicia uma **sessão de compartilhamento de localização (GPS) do responsável com a escola** com duração do **ciclo completo da fila** (até `COMPLETED`/`CANCELLED`). A cada atualização de GPS, o sistema recalcula a distância até a **GPS da escola** e classifica o responsável em um dos **3 ranges de proximidade**; ao entrar no range mais próximo, o aluno é **automaticamente chamado** (`called = true`).

### GPS da Escola
- A entidade `School` passa a ter `latitude` e `longitude` (coordenadas do portão/perímetro escolar).
- É o ponto de referência para o cálculo de distância/ETA do responsável.
- Migração `V1`/`V2` ganha as colunas `latitude`/`longitude` na tabela `schools`; endpoint de cadastro de escola recebe as coordenadas.

### Novos Elementos de Domínio (Core)

**`ProximityRange` (3 tiers de proximidade):**
- **FAR:** ETA > 15 minutos.
- **MEDIUM:** 5 < ETA ≤ 15 minutos.
- **CLOSE:** ETA ≤ 5 minutos (dispara `called = true` automaticamente).

> Método utilitário `ProximityRange.fromCoordinates(...)` (Haversine) converte coordenadas pai↔escola em range, com limiares fixos em `fromDistanceKm`. Configuráveis via `application.yml`.

**Estados da Sessão de Compartilhamento (`LocationSharingStatus`):**
- **ACTIVE:** Compartilhamento em andamento (item da fila ativo).
- **EXPIRED:** Item da fila finalizado (`COMPLETED`) ou cancelado (`CANCELLED`) — a sessão é encerrada automaticamente.

**Entidade Pura `LocationSharingSession`:**
```java
public class LocationSharingSession {
    private final UUID id;
    private final UUID queueItemId;   // vínculo com o item da fila
    private final UUID parentId;
    private final UUID schoolId;
    private final Instant startedAt;  // = momento do anúncio de chegada
    private LocationSharingStatus status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant lastUpdatedAt;

    // Regras de Negócio Puras
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        if (this.status != LocationSharingStatus.ACTIVE) {
            throw new InvalidSharingSessionException("Sessão de GPS encerrada ou inativa.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdatedAt = Instant.now();
    }

    public void end() {  // chamado ao COMPLETED/CANCELLED
        this.status = LocationSharingStatus.EXPIRED;
    }
    // Getters omitidos para brevidade...
}
```

### Novas Portas (Ports)

**Driving Ports (Novos Casos de Uso):**
```java
public interface StartLocationSharingUseCase {
    LocationSharingSession execute(StartLocationSharingCommand command);
    record StartLocationSharingCommand(UUID queueItemId, UUID parentId, UUID schoolId) {}
}

public interface UpdateParentLocationUseCase {
    PickupQueueItem execute(UpdateLocationCommand command);
    record UpdateLocationCommand(UUID sharingSessionId, BigDecimal latitude, BigDecimal longitude) {}
}

public interface FetchSharedLocationUseCase {
    Optional<LocationSharingSession> execute(UUID schoolId, UUID sharingSessionId);
}
```

> O `UpdateParentLocationUseCase` recalcula o range (comparando com a GPS da escola) e aplica `updateRange(...)` no item da fila — é ele que dirige a auto-chamada.

**Driven Port:**
```java
public interface LocationSharingRepositoryPort {
    LocationSharingSession save(LocationSharingSession session);
    Optional<LocationSharingSession> findByQueueItemId(UUID queueItemId);
    List<LocationSharingSession> findActiveBySchoolId(UUID schoolId);
}
```

### Integração com o Fluxo Principal
- O `AnnounceArrivalService` inicia a sessão de GPS ao salvar um novo item na fila (desacoplado e resiliente — falha do GPS não impede o anúncio).
- Adaptadores de entrada adicionais no Controller REST:
  - `POST /api/v1/schools` — cadastra escola **com latitude/longitude** (GPS da escola).
  - `POST /api/v1/location-sharing` — iniciar compartilhamento (ou retornado automaticamente pela resposta do `/announce`).
  - `PATCH /api/v1/location-sharing/{id}/location` — receber atualização de GPS do responsável (recalcula range e pode auto-chamar).
  - `GET /api/v1/location-sharing/school/{schoolId}/active` — escola consulta sessões ativas.
- **Nota de realidade técnica:** Em um app real, o GPS seria enviado periodicamente pelo aplicativo do responsável via webhook/websocket; para fins de estudo, os endpoints REST acima simulam esse comportamento.

### Regras de Negócio Resumidas
- Compartilhamento inicia junto com o anúncio de chegada (`EN_ROUTE`).
- A sessão dura **todo o ciclo da fila** (encerrada ao `COMPLETED`/`CANCELLED`), não mais 15 minutos fixos.
- A distância pai ↔ **GPS da escola** define o `ProximityRange` (FAR/MEDIUM/CLOSE).
- Ao entrar em `CLOSE`, o aluno é **automaticamente chamado** (`called = true`) — podendo ficar "EN_ROUTE + CALLED" ou "ARRIVED + CALLED".
- `ARRIVED` é opcional (marcado quando o pai chega fisicamente) e não é pré-requisito para chamar/entregar.
- Apenas a escola vinculada pode consultar a localização.

---

## 🚀 9. Checklist Prático do Roteiro de Estudos

Siga a ordem sequencial abaixo para construir o projeto do zero:

- [ ] **Fase 1: Infraestrutura**
  - [ ] Subir o container Docker (`docker-compose up -d`).
  - [ ] Validar a criação do banco executando a query `SELECT * FROM schools;` via cliente Postgres.
- [ ] **Fase 2: Core (Domínio Puramente Java)**
  - [ ] Criar as Entidades Java puras (`School` com latitude/longitude, `Student`, `Parent`, `PickupQueueItem`).
  - [ ] Criar Enums (`QueueStatus`, `ProximityRange`, `LocationSharingStatus`) e Exceções de Domínio.
  - [ ] Escrever Testes Unitários com JUnit 5 para as transições de estado na entidade `PickupQueueItem` (dirigidas por GPS: `updateRange`, `markAsArrived`, `markAsCompleted`, `cancel`).
  - [ ] Criar Testes Unitários (JUnit 5 + AssertJ) para as entidades `School`, `Classroom`, `Parent` e `Student` — teste (RED) antes da implementação (TDD).
- [ ] **Fase 3: Contratos & Casos de Uso**
  - [ ] Criar os pacotes `ports.in` e `ports.out`.
  - [ ] Implementar as interfaces dos Use Cases (`AnnounceArrivalUseCase`, `UpdateQueueStatusUseCase`, `FetchActiveQueueUseCase`, casos de GPS).
  - [ ] Implementar as classes de Serviço que orquestram a lógica no pacote `application`.
  - [ ] Escrever Testes Unitários dos services (JUnit 5 + Mockito) antes de implementá-los (TDD).
- [ ] **Fase 4: Adaptadores de Banco de Dados**
  - [ ] Criar as entidades JPA (`@Entity`) no pacote `infrastructure.adapters.out.persistence.entity`.
  - [ ] Criar os Mappers para converter entre `Domain Model` <-> `JPA Entity`.
  - [ ] Implementar a classe `QueuePersistenceAdapter` que assina o contrato `QueueRepositoryPort`.
  - [ ] Escrever testes de persistência (`@DataJpaTest` + Testcontainers Postgres) para repositórios e adapters — teste (RED) antes da implementação (TDD).
- [ ] **Fase 5: Adaptadores REST e Configurações Spring**
  - [ ] Criar as classes de configuração `@Configuration` para publicar os Beans de Domínio.
  - [ ] Criar o Controller REST `@RestController` (escola com GPS, anúncio, atualização de GPS/range, fila ativa).
  - [ ] Escrever testes dos controllers (`@WebMvcTest`) e do `ExceptionHandler` (409/400) antes de implementá-los (TDD).
  - [ ] Executar a aplicação Spring Boot.
- [ ] **Fase 6: Testes E2E com Bruno**
  - [ ] Executar as requisições na ordem: Criar Escola (com GPS) -> Cadastrar Aluno -> Anunciar Chegada -> Atualizar GPS (entrar no range CLOSE e auto-chamar) -> Marcar Chegada (opcional) -> Finalizar Entrega.
- [ ] **Fase 6.5: Testes de Integração e Qualidade**
  - [ ] Criar `PickupQueueFlowIT` (`@SpringBootTest` + Testcontainers postgres/rabbitmq) validando o fluxo E2E automatizado e a publicação no RabbitMQ.
  - [ ] Rodar `mvn verify` com o gate JaCoCo (linha >= 80%, ramo >= 70%) e validar o relatório de cobertura.
- [ ] **Fase 7: Compartilhamento de GPS (agora dirige o fluxo da fila)**
  - [ ] Adicionar latitude/longitude à entidade `School` e à migração da tabela `schools`.
  - [ ] Criar o enum `ProximityRange` (FAR/MEDIUM/CLOSE) com `fromCoordinates(...)`/Haversine e a entidade pura `LocationSharingSession` (sessão dura o ciclo completo da fila).
  - [ ] Escrever testes unitários (JUnit 5) para o cálculo de range e o encerramento da sessão.
  - [ ] Criar as portas `StartLocationSharingUseCase`, `UpdateParentLocationUseCase`, `FetchSharedLocationUseCase` e `LocationSharingRepositoryPort`.
  - [ ] Escrever testes TDD dos services de GPS (Mockito).
  - [ ] Implementar os serviços no pacote `application` e integrar (desacoplado) ao `AnnounceArrivalService`.
  - [ ] Escrever testes de persistência do GPS (`@DataJpaTest` + Testcontainers) e validar a migração `V2__location_sharing.sql`.
  - [ ] Criar a entidade JPA, mapper e adaptador de persistência para `LocationSharingSession`.
  - [ ] Adicionar a migração SQL da tabela `location_sharing` e das colunas de GPS em `schools`/`pickup_queue`.
  - [ ] Escrever testes dos endpoints REST de GPS (`@WebMvcTest`) e mapeamento 400/409.
  - [ ] Expor os endpoints REST de iniciar compartilhamento, atualizar localização (recalcula range) e consultar sessões ativas.
  - [ ] Criar `LocationSharingFlowIT` (`@SpringBootTest` + Testcontainers) para o fluxo E2E do GPS.
  - [ ] Testar via Bruno: Criar Escola com GPS -> Anunciar Chegada -> Atualizar GPS -> Validar auto-chamada ao entrar no range CLOSE -> Finalizar.

---

## 🐞 10. Backlog de Lacunas Encontradas

> Cards acumulados pelo agente ao longo do desenvolvimento. Não são resolvidos
> sem aprovação explícita; podem virar tasks do roteiro ou permanecer como
> referência. O agente avisa periodicamente sobre os pendentes.

### LAC01 — Convenções de prefixo de commit divergem entre AGENTS.md, roteiro e histórico

`AGENTS.md` documenta apenas o prefixo `[Task NNN]`, mas o histórico já usa
`[GPS00]`, `[GPS01]`, `[FILA01]`, `[API03]`, etc., e a nova seção do AGENTS.md
passa a tratar `APIxx` e `FILAxx` como prefixos de primeira classe. #docs #arch

### LAC02 — `AGENTS.md` ainda não referencia prefixos `[APIxx]`, `[FILAxx]`, `[GPSxx]`

A seção "Convenção de commits" do AGENTS.md segue com apenas o exemplo
`[Task NNN]`, então um agente novo lendo só esse arquivo não saberia prefixar
commits das fases que já rodam (Fase 4/5/7). Solução: alinhar a seção com o
histórico real e com a nova regra do backlog. #docs

### LAC03 — `School` valida GPS só no construtor, `SchoolEntity` aceita colunas nulas

A entidade de domínio `School` lança `IllegalArgumentException` sem GPS, mas a
`SchoolEntity` JPA (`SchoolEntity.java:18-23`) tem apenas `@Column(precision,
scale)`, sem `nullable = false`. Um `setLatitude(null)` no domínio já é
bloqueado, mas a coluna no banco permitiria `null` se algo bypassasse o
domínio (ex.: seed SQL, outro adapter). Alinhar com `nullable = false` (ou
comentar a decisão de manter domínio como única guarda). #db #arch

### LAC04 — `RegisterSchoolService` não detecta escola duplicada por nome

`SchoolRepositoryPort` expõe só `findById` e `save`. Dois POSTs com o mesmo
`name` criam duas escolas distintas. O domínio não trata isso explicitamente;
depende de política de produto. Se for regra de negócio, adicionar
`findByName` ao port, validar no service e retornar 409 via
`GlobalExceptionHandler` (que hoje só trata `MethodArgumentNotValidException`).
#backend #rest #arch

### LAC05 — `RegisterSchoolServiceTest` cobre só o caso feliz

Há 1 teste no service contra 5 cenários no controller. Falta cobrir:
service recebendo latitude ou longitude nula (deve propagar
`IllegalArgumentException` vinda de `School`). #test

### LAC06 — Sem verificação de cadastros repetidos em nenhuma unidade

Reproduzido: `schools` tem 11 linhas com apenas 2 nomes distintos e 1 par
lat/lng (confirmado em `pg_stat_user_tables` via `psql`). Nenhuma das
unidades (`schools`, `students`, `parents`, `classrooms`) tem unicidade
garantida — nem em banco (`UNIQUE` constraint), nem em service
(checagem no use case + 409 no `GlobalExceptionHandler`). O LAC04 já
cobre `schools` por `name`; este card é mais amplo: revisar todas as
unidades e decidir, para cada uma, qual é a chave natural única (nome,
documento, par lat/lng, etc.) e onde aplicá-la (constraint no schema
Flyway + validação no domínio). Inclui o `DataIntegrityViolationException`
no handler para 409 quando a constraint pegar. #backend #db #arch

### LAC07 — Revisar durabilidade/TTL/DLQ de `queue.notifications` em produção

Decisão adotada na `MSG00` (`src/main/java/com/schoolqueue/infrastructure/config/RabbitMQConfig.java`):
exchange `school.queue.events` (topic, durable=true) e fila `queue.notifications`
(durable=true, **sem** `x-message-ttl`, **sem** DLQ, **sem** quorum queue).
Bindings: `queue.arrival.announced` e `queue.status.changed`. Adequado para
dev/local, mas em produção vale revisar: TTL para evitar backlog infinito de
eventos não consumidos; DLQ + política de retry para mensagens
poison/descartadas; quorum queue (vs. classic) para HA; `max-length`/`overflow`
para capar a fila; consumer-side `prefetch`, `ack` manual e idempotência.
Definir quem é o consumer (escola, portaria, app do responsável?) e qual a
janela aceitável de perda zero vs. at-least-once. #messaging #arch #backend

### LAC08 — ✅ Resolvido na MSG02 — Implementar `notifyStatusChanged` no `RabbitMQNotificationAdapter`

A `MSG01` (`src/main/java/com/schoolqueue/infrastructure/adapters/out/messaging/RabbitMQNotificationAdapter.java`)
cobria apenas `notifyStudentArrivalAnnounced`. O segundo método do
`QueueNotificationPort` — invocado por `UpdateQueueStatusService:42` em todo
update de estado da fila — lançava `UnsupportedOperationException("... see LAC08")`.

**Resolução (MSG02):**
- DTO `StatusChangedEvent(queueItemId, studentId, schoolId, previousStatus, newStatus, called, currentRange, occurredAt)` em
  `src/main/java/com/schoolqueue/infrastructure/adapters/out/messaging/dto/StatusChangedEvent.java`.
- Adapter reusa a routing key `queue.status.changed` já declarada na `RabbitMQConfig` (MSG00).
- `RabbitMQNotificationAdapterTest` cobre publicação correta e propagação de `AmqpException` para `notifyStatusChanged`.
- LAC08 permanece no backlog como referência/histórico (decisão do usuário).

### LAC09 — Implementar `PickupQueueController` (Fase 5 do roteiro)

O `SchoolController` (`src/main/java/com/schoolqueue/infrastructure/adapters/in/web/SchoolController.java`)
é o único controller da aplicação. As três coleções Bruno já alinhadas com o
domínio — `Queue/Announce Arrival.bru` (anunciar chegada, payload agora
alinhado ao `AnnounceArrivalCommand`), `Queue/List Active Queue.bru`
(`FetchActiveQueueUseCase.execute(schoolId)`) e `Queue/Update Status` (a
criar) — retornam 404 no estado atual porque o `PickupQueueController` ainda
não existe. Pendente:

- `POST /api/v1/queue/announce` → `AnnounceArrivalUseCase`
- `GET /api/v1/queue/active?schoolId=...` → `FetchActiveQueueUseCase`
- `PATCH /api/v1/queue/{queueItemId}/status` (ou similar) → `UpdateQueueStatusUseCase`
  com `QueueAction` (sealed: `UpdateRange`/`MarkAsArrived`/`MarkAsCompleted`/`Cancel`)

Decidir também: DTOs de request/response (`AnnounceArrivalRequest`,
`QueueItemResponse`, `UpdateStatusRequest`), `QueueDtoMapper`, e
`@WebMvcTest` cobrindo os caminhos feliz + 400 (payload inválido) + 404
(item não encontrado) + 409 (transição inválida → `InvalidQueueStateException`).
Os `*ServiceTest` já cobrem a lógica; o controller precisa ser exercitado
via MockMvc com `GlobalExceptionHandler` real. #rest #arch #backend

### LAC10 — Card [35] cancelado: DTOs sem `etaMinutes` (substituído por `currentRange`)

O card antigo [35] (provavelmente do roteiro "Pai informa ETA em minutos" do
modelo pré-GPS) **foi cancelado**. O modelo da fila é dirigido por GPS
(`ProximityRange` calculado via Haversine entre o GPS do responsável e o da
escola), então o `etaMinutes` saiu:
- do `AnnounceArrivalCommand` (`domain/ports/in/AnnounceArrivalUseCase.java:11-12` carrega só `latitude`/`longitude`).
- da tabela `pickup_queue` (sem coluna `eta`/`eta_minutes` na V1 do Flyway).
- do contrato HTTP de entrada (`AnnounceArrivalRequest`) e de saída (`QueueItemResponse`), conforme `API00`.

A `API00` implementa o substituto: os DTOs da fila usam `currentRange`
(FAR/MEDIUM/CLOSE) e `latitude`/`longitude` do responsável. O card [35] em
si não existe mais nem no roteiro nem no histórico de commits, então a
substituição fica registrada via este card no backlog + mensagem do commit
da `API00`. #arch #backend

### LAC11 — API01 já entregue como parte da API00 (`QueueDtoMapper.toResponse`)

O card [36] do roteiro antigo (criar `QueueDtoMapper.toResponse`) **já foi
implementado** dentro da `API00` — o mapper entrou completo no commit
`8159dc2` e foi integrado a `main` em `699174f`. Não há trabalho de código
pendente: o critério é satisfeito pelo que já está no repositório.

**Implementação atual** (`src/main/java/com/schoolqueue/infrastructure/adapters/in/web/mapper/QueueDtoMapper.java:21-34`):
- `public static QueueItemResponse toResponse(PickupQueueItem item)`
  preenchendo os 11 campos do `QueueItemResponse`:
  `id`, `schoolId`, `studentId`, `parentId`, `journeyStatus`, `called`,
  `currentRange`, `latitude`, `longitude`, `createdAt`, `updatedAt`.
- `called` (linha 28) e `currentRange` (linha 29) são os dois campos exigidos
  pelo critério e estão presentes.
- Classe `final` sem estado (`public final class QueueDtoMapper` + construtor
  `private`) com métodos `public static` — mesmo padrão do `SchoolDtoMapper`.

**Cobertura:** o mapper será exercitado automaticamente quando o
`PickupQueueController` (LAC09) entrar e for coberto por `@WebMvcTest`. Não
foi escrito teste unitário isolado do mapper nesta task por decisão do
usuário; pode ser acrescido em task futura se a cobertura ficar baixa.

O card [36] em si não existe mais nem no roteiro nem no histórico de
commits, então o status "já entregue" fica registrado via este card no
backlog + mensagem do commit `Registra API01 como já entregue (LAC11)`.
#rest #arch

### LAC12 — Handlers 404 no `GlobalExceptionHandler` (`QueueItemNotFoundException`, `SchoolNotFoundException`, `StudentNotFoundException`)

A `API02` cobre os 2 handlers exigidos pelo critério
(`InvalidQueueStateException` → 409 e `IllegalStateException` → 400) em
`src/main/java/com/schoolqueue/infrastructure/adapters/in/web/GlobalExceptionHandler.java`.
Ficou fora do escopo deliberado: mapear `QueueItemNotFoundException`,
`SchoolNotFoundException` e `StudentNotFoundException` (todas em
`src/main/java/com/schoolqueue/domain/exception/`) para **404 Not Found**.
Hoje elas propagam e o Spring retorna **500 Internal Server Error**,
mascarando a causa real e quebrando a expectativa do consumidor da API.

Solução: adicionar 3 handlers no `GlobalExceptionHandler` (um por exceção)
retornando `ResponseEntity.status(HttpStatus.NOT_FOUND).body(...)` com o
mesmo `ValidationErrorResponse`/`FieldError` que já existem. Cobertura no
`PickupQueueControllerWebTest` (PATCH com `queueItemId` inexistente deve
retornar 404) e no `SchoolControllerWebTest` (POST com GPS em escola que
não existe). #rest #backend

### LAC13 — `BeanConfiguration` mistura `@Bean` explícito e component scan

`src/main/java/com/schoolqueue/infrastructure/config/BeanConfiguration.java`
registra `RegisterSchoolUseCase` como `@Bean`, mas os 3 use cases da fila
(`AnnounceArrivalService`, `UpdateQueueStatusService`,
`FetchActiveQueueService`) estavam sem `@Service`/`@Component` até a
`API02` — a injeção só funcionava em runtime porque o app não os usava
(não havia controller). Com a `API02` o `@Service` foi adicionado nos 3
para o `@WebMvcTest` montar o contexto, mas a inconsistência permanece:
`RegisterSchoolService` continua manual, os outros são auto-discovered.

Solução: decidir uma convenção (recomendado: todos via component scan com
`@Service`; remover o `@Bean` do `BeanConfiguration` e apagar a classe
se ficar vazia) e aplicar. Ganha: menos código, padrão único, mais
consistência. #arch #backend

### LAC14 — Endpoints para criar `Student` / `Classroom` / `Parent`

Reproduzido em runtime (E2E manual pós-`API02`): o `POST /api/v1/queue/announce`
precisa que `students`, `classrooms` e `parents` existam no banco, mas **só
existe controller para `School`** (`src/main/java/com/schoolqueue/infrastructure/adapters/in/web/SchoolController.java`).
Quem for testar a fila tem que inserir as FKs via `psql` no Postgres, o que
quebra a paridade com o fluxo do `School` (que vai 100% via HTTP).

**Pendência (mesmo padrão do que foi feito para `School`):**
- `POST /api/v1/students` → `RegisterStudentUseCase` (a criar)
  - body: `{ schoolId, classroomId, name }`
  - 201 + `Location` + `StudentResponse`
  - validação: `@NotNull schoolId`, `@NotNull classroomId`, `@NotBlank name`
  - 404 quando `schoolId` ou `classroomId` não existem (cobre o LAC12)
- `POST /api/v1/classrooms` → `RegisterClassroomUseCase` (a criar)
  - body: `{ schoolId, name }`
  - 201 + `Location` + `ClassroomResponse`
  - 404 quando `schoolId` não existe
- `POST /api/v1/parents` → `RegisterParentUseCase` (a criar)
  - body: `{ name, phone }`
  - 201 + `Location` + `ParentResponse`

**Pré-requisitos no domínio/infra (hoje ausentes):**
- `StudentRepositoryPort`/`ClassroomRepositoryPort`/`ParentRepositoryPort` (driven
  ports em `domain/ports/out/`) — só existem as entidades e o `QueueRepositoryPort`/`SchoolRepositoryPort`.
- `StudentEntity`/`ClassroomEntity`/`ParentEntity` JPA + mapper + adapter de
  persistência (mesma estrutura do `SchoolPersistenceAdapter`).
- Driving ports `RegisterStudentUseCase` / `RegisterClassroomUseCase` /
  `RegisterParentUseCase` (interfaces em `domain/ports/in/`).
- Services em `application/usecase/...` anotados com `@Service`.
- DTOs de request (`RegisterStudentRequest` etc.) e response (`StudentResponse` etc.)
  em `infrastructure/adapters/in/web/dto/`.
- `@WebMvcTest` por controller (mesmo padrão do `SchoolControllerWebTest`).
- Bruno collections (`bruno/Students/Register Student.bru` já existe mas
  retorna 404; falta `Classrooms/Create Classroom.bru` e `Parents/Create Parent.bru`).

**Esboço de ordem (1 task por entidade, escopo pequeno):**
1. `STU00` (ou `CAD00`) — `StudentPersistenceAdapter` + IT Testcontainers +
   `RegisterStudentUseCase`/`Service` + DTOs + `StudentController` + `@WebMvcTest`
   + Bruno. Replica o que a `FILA00` + `API01`/`API02` fizeram para a fila.
2. `CLA00` — idem para `Classroom`.
3. `PAR00` — idem para `Parent` (mais simples, sem FK externa).

Cobertura: depois disso, o fluxo E2E da fila (`School` → `Classroom` → `Student`
→ `Parent` → `Announce` → `Update` → `FetchActiveQueue`) roda 100% via HTTP +
Bruno, sem `psql` manual. #rest #backend #arch

### LAC15 — `StudentRepositoryPort` / `ClassroomRepositoryPort` / `ParentRepositoryPort` (sem adapter)

Espelho da LAC14 mas focado só na infra: enquanto as driven ports não
existirem, qualquer task que tente persistir `Student`/`Classroom`/`Parent`
vai cair no `JPA` direto. Vale criar primeiro as 3 interfaces em
`domain/ports/out/`, com o mínimo (`save` + `findById`), para destravar as
implementações JPA da LAC14 sem violar a regra "Core nunca fala com JPA".
#arch #backend

### LAC16 — LAC13 resolvida no sentido inverso via [CONF00]

A task `[CONF00] BeanConfiguration` padronizou a publicação dos 4 use
cases (`RegisterSchoolUseCase`, `AnnounceArrivalUseCase`,
`UpdateQueueStatusUseCase`, `FetchActiveQueueUseCase`) via `@Bean`
explícito no `src/main/java/com/schoolqueue/infrastructure/config/BeanConfiguration.java`
e removeu o `@Service` das classes de `application/`. Garante o critério
de aceite "Sem anotações Spring nas classes de aplicação" e endurece a
regra "Core nunca fala com Spring".

Decisão oposta à sugestão original da LAC13, que apontava component scan
com `@Service` em todos e consequente remoção da `BeanConfiguration`.
Optou-se pelo caminho inverso porque o critério da CONF00 é explícito
sobre manter o core livre de anotações Spring. Os `@WebMvcTest` da `API02`
seguem funcionando porque usam `@MockitoBean` diretamente nas interfaces
de `ports.in`, sem depender do scan para instanciar os services.
`SchoolControllerWebTest` continua passando também — o `RegisterSchoolUseCase`
já era `@Bean` antes. Cobertura nova: `BeanConfigurationTest` valida que
os 4 beans são registrados. #arch #backend #test

### LAC17 — TEST02 fechou as duas pontas remanescentes (handler dedicado + 409 no PATCH)

A `TEST02` ("Testes TDD do controller REST") tinha praticamente tudo
coberto pela `API02` — o `PickupQueueControllerWebTest` (15 testes via
`@WebMvcTest` + MockMvc) já garantia POST 200/400, PATCH 200/400 e GET.
Ficavam duas lacunas explícitas no card:

1. **`GlobalExceptionHandlerTest` dedicado** — o mapeamento
   `InvalidQueueStateException` → 409 e `IllegalStateException` → 400 era
   exercitado só como efeito colateral dos testes do controller, sem
   regressão isolada do advice.
2. **Cenário 409 no PATCH** — o critério do card diz *"PATCH .../status:
   200 na transição; 409 em estado inválido"*, mas o teste original
   `shouldReturnConflictWhenServiceThrowsInvalidQueueState` exercitava
   apenas o `POST /announce`.

**Resolução (TEST02):**
- Novo `src/test/java/com/schoolqueue/infrastructure/adapters/in/web/GlobalExceptionHandlerTest.java`
  (estilo **standalone puro** — instancia `new GlobalExceptionHandler()`,
  sem Spring/MockMvc, AssertJ) cobrindo 3 cenários:
  - `InvalidQueueStateException` → 409 + `field="state"` + mensagem
    original preservada.
  - `IllegalStateException` → 400 + `field="state"` + mensagem original.
  - `MethodArgumentNotValidException` → 400 com 1 `FieldError` por campo
    inválido (regressão do formato do `ValidationErrorResponse`,
    não estava coberto de forma isolada antes).
- Adicionado `shouldReturnConflictOnPatchWhenServiceThrowsInvalidQueueState`
  no `PickupQueueControllerWebTest` (PATCH com `MARK_AS_COMPLETED` →
  `InvalidQueueStateException("Aluno não pode ser entregue sem ter sido
  chamado")` → 409 com `field="state"` e mensagem preservada).
- `mvn -DskipITs test` verde: 126 testes (122 prévios + 4 novos), 0 falhas.

Nada de produção foi tocado na TEST02 — apenas arquivos de teste. O card
TEST02 pode ser marcado como concluído. #rest #test

### LAC18 — Notação de volume (`pgdata` vs. `project_hexagony_pgdata`) não é divergência

A observação registrada no plano da `OPS00` apontava aparente divergência
entre `docker/docker-compose.yml` (usa `pgdata:` simples, linha 14 e
declaração na linha 52) e `AGENTS.md` (usa o nome já prefixado
`project_hexagony_pgdata` e `project_hexagony_pgadmin-data` na tabela de
volumes da linha 70 e nos comandos de backup das linhas 93–98).

**Verificação (`OPS00`):**
- `docker compose --project-directory . -f docker/docker-compose.yml
  config --volumes` retorna `pgdata` e `pgadmin-data` (nomes curtos
  declarados no Compose).
- `docker volume inspect project_hexagony_pgdata` mostra os labels:
  - `com.docker.compose.project = project_hexagony`
  - `com.docker.compose.volume = pgdata`
  - `com.docker.compose.config-hash = 31cd162e78c7f5765284b8545eef38c98c4bb059c4a9c0957bc032912c62cc02`
- `Name` do volume = `project_hexagony_pgdata`.

**Conclusão:** os dois documentos estão corretos, só misturam notações.
O `docker-compose.yml` declara o nome lógico (`pgdata:`) e o Docker
Compose prefixa automaticamente com o nome do diretório do projeto
(`project_hexagony_`), produzindo o nome final `project_hexagony_pgdata`
que aparece no `AGENTS.md`. Nenhuma correção é necessária: a sintaxe
do `docker-compose.yml` é a recomendada, e os comandos de
backup/restore do `AGENTS.md` (`-v project_hexagony_pgdata:/from`)
operam sobre o nome final resolvido pelo Compose.

A divergência é apenas de notação, não semântica. Manter o
`docker-compose.yml` como está. #devops #docs #arch

### LAC19 — `application.yml` tem credenciais do Postgres hardcoded (duplicação frágil vs `.env`)

Observação levantada no plano da `OPS01`. O
`src/main/resources/application.yml` traz as credenciais do Postgres
**hardcoded** (`username: queue_user`, `password: queue_password`,
`url: jdbc:postgresql://localhost:5432/school_queue_db`),
enquanto o `docker/docker-compose.yml` as lê via `${POSTGRES_*}` do
`.env` da raiz. Os valores coincidem hoje (vide `.env`), mas é uma
duplicação frágil: qualquer divergência silenciosa entre os dois
arquivos quebra a aplicação ou o container sem aviso.

**Por que existe:**
- O `docker-compose.yml` foi parametrizado pela `Task 28` (commit
  `3f381f0`) para usar o `.env` centralizado.
- O `application.yml` continua com valores literais desde o setup
  original (`7a1b9c8`) e nunca foi migrado para placeholders.
- Spring Boot resolve `${POSTGRES_USER}` em `application.yml` se houver
  uma propriedade de ambiente/system com esse nome, mas a
  parametrização atual do `.env` é só para o Compose (não é exportada
  para o processo do app).

**Por que não foi resolvido na OPS01 (fora de escopo):**
- A task é estritamente "subir a aplicação e validar startup". Mexer no
  `application.yml` traz consigo decisões de runtime (profile, env var
  vs. placeholders, segredos) que merecem uma task dedicada.
- Decidir o formato: `${POSTGRES_USER:queue_user}` (placeholder com
  default) vs. env var obrigatória vs. profile `--spring.profiles.active`
  carregando `application-prod.yml`. Cada um tem trade-off.
- Validar a substituição em todos os pontos que importam: datasource,
  Flyway (não — Flyway usa a datasource), e qualquer outro recurso que
  use essas credenciais.

**Solução proposta (task futura):**
- Substituir os 3 valores literais no `application.yml` por
  `${POSTGRES_URL:...}`, `${POSTGRES_USER:queue_user}`,
  `${POSTGRES_PASSWORD:queue_password}`.
- Documentar no `AGENTS.md` que o `.env` é a fonte canônica das
  credenciais e que o app lê via env var.
- Opcional: adicionar `spring.config.import: optional:file:.env[.properties]`
  no `application.yml` para que o app também carregue o `.env` em dev.
- Cobrir com `@SpringBootTest` que injete as props via `@TestPropertySource`.

Nada foi alterado no `application.yml` durante a OPS01 — apenas o
registro deste card. #devops #arch #docs

### LAC20 — DELETE bloqueado em School / Classroom / Parent / Student (LAC14 devolve 405)

A [CAD00], [CLA00], [PAR00] e [STU00] (tasks da LAC14) implementaram CRUD
completo com a convenção de **bloquear deleção** (decisão consciente do
usuário, registrada em chat). Hoje os 4 controllers expõem `@DeleteMapping` que
retornam `405 Method Not Allowed` via `ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build()`.
O método existe na API e nas Bruno collections (`Delete School.bru`,
`Delete Classroom.bru`, `Delete Parent.bru`, `Delete Student.bru`) para
deixar claro que a operação está reservada, não esquecida.

**Lacuna:** quando reativar DELETE, decidir:

- **Cascade** — `DELETE /api/v1/schools/{id}` derruba classrooms, students,
  queue items em cascata. Exige schema com `ON DELETE CASCADE` e
  reescrita do `*PersistenceAdapter` para limpar dependências.
- **Block** — `409 Conflict` quando há dependências. Exige checagem
  prévia (`countClassroomsBySchool`, `hasActiveQueueItem` etc.) e mapeamento
  no `GlobalExceptionHandler` para 409 com mensagem específica.
- **Soft-delete** — coluna `deleted_at` em cada entidade, query de
  listagem filtra `WHERE deleted_at IS NULL`. Mantém histórico e permite
  restore. Exige migration V2 em cada tabela.

Cobertura: `SchoolControllerWebTest`, `ClassroomControllerWebTest`,
`ParentControllerWebTest`, `StudentControllerWebTest` já têm teste
`shouldReturnMethodNotAllowedOnDelete` cada um — esses testes vão precisar
ser invertidos quando a decisão for tomada. #rest #arch #db

### LAC25 — Melhorar mapeamento objeto-relacional (UUID cru → `@ManyToOne`/`@OneToMany`)

Hoje o mapeamento JPA trata FKs como `UUID` cru em todas as entities de
cadastro (`StudentEntity.schoolId`, `StudentEntity.classroomId`,
`ClassroomEntity.schoolId`, `PickupQueueEntity.studentId`,
`PickupQueueEntity.parentId`, `PickupQueueEntity.schoolId`,
`ParentStudentEntity.id.parentId`, `ParentStudentEntity.id.studentId`).
Funciona, mas:

- **Sem joins** — não dá pra navegar `StudentEntity.getSchool().getName()`
  sem ir ao banco duas vezes.
- **Sem cascata** — deletar `School` exige deletar manualmente `Classroom`,
  `Student`, `ParentStudent` (motivo da LAC20 acima).
- **`StudentEntityMapper` é burro** — apenas copia `UUID` cru, sem
  carregar a entidade relacionada. Hoje é proposital (mantém o Core
  puro), mas se a complexidade subir, vale reavaliar.
- **`ParentStudentEntity` usa `@EmbeddedId` manual** — funciona, mas dá
  pra simplificar com `@ManyToOne` + `@JoinColumn` + `parent_student`
  como entidade com FKs explícitas e chave substituta (ou mantendo
  PK composta mas com relacionamentos).

**Solução proposta (task futura):**

- Trocar `UUID schoolId` por `@ManyToOne(fetch = FetchType.LAZY) SchoolEntity school` em
  `StudentEntity`, `ClassroomEntity`, `PickupQueueEntity`.
- Trocar `UUID parentId/studentId` em `PickupQueueEntity` por
  `@ManyToOne` para `ParentEntity`/`StudentEntity`.
- Trocar `@EmbeddedId` em `ParentStudentEntity` por dois `@ManyToOne`
  com `@JoinColumn` (mantém a PK composta no banco via
  `@IdClass` ou coluna `id` surrogate).
- Atualizar `StudentEntityMapper`, `ClassroomEntityMapper`,
  `QueueEntityMapper`, `ParentStudentEntityMapper` para carregar
  as entidades relacionadas (ou decidir ficar com UUID no Core e
  popular o relacionamento só na infraestrutura).
- Atualizar `GlobalExceptionHandler` para mapear `EntityNotFoundException`
  do Hibernate (caso FK aponte para registro inexistente — não deve
  acontecer com a validação no service, mas é cinto + suspensórios).

Cobre também parte da dívida da LAC20 (cascade) e da LAC07 (DLQ do
Rabbit, não relacionada). #db #arch #backend

### LAC26 — Decidir como `AnnounceArrival` resolve o `parentId` quando Student tem múltiplos pais

A STU00 implementou Student com `parentIds: List<UUID>` (N:N via
`parent_student`) — 1 aluno pode ter N responsáveis. Mas o
`AnnounceArrivalCommand` (vide `AnnounceArrivalUseCase.java:11-12` e
`AnnounceArrivalService.java:51`) ainda exige `parentId` único no body:

```json
POST /api/v1/queue/announce
{
  "schoolId": "...",
  "studentId": "...",
  "parentId": "...",       // <— único, hoje
  "latitude": ...,
  "longitude": ...
}
```

Hoje o usuário passa o `parentId` que ele quer registrar (dentre os
`parentIds` do Student), e o `AnnounceArrivalService` confia que esse
parent existe. A `ParentNotFoundException` (handler 404) cobre o caso
de parent inválido, mas **não cobre** o caso de parent válido porém
**não vinculado** ao Student. Ex.: João cadastrado para Maria, mas
`Announce` é chamado com `parentId=outroPai` → o request passa e
o evento Rabbit é publicado com `parentId` que não está no
`parent_student` desse Student.

**Decisões a tomar (em task futura):**

- (a) **Validar que `parentId` ∈ `parentIds` do Student** no
  `AnnounceArrivalService` (lança 400/409 se não estiver). Simples,
  preserva o contrato atual.
- (b) **Tornar `parentId` opcional** no `AnnounceArrivalCommand` e
  inferir o "pai da vez" a partir de quem está mais perto (GPS) —
  alinhado com a feature "pai informa localização". Mais complexo,
  exige mudar o modelo da fila.
- (c) **Aceitar `parentIds: [list]`** no body da fila. Vira N
  responsáveis na fila, o que provavelmente não faz sentido (a fila
  é "o aluno X vai sair, buscado por Y"). Descartado.

Recomendação: (a) — validação explícita, sem mudança de contrato. #arch #backend

### LAC27 — Critério da BRUNO03 cita `200` mas projeto padronizou `201 Created` em POSTs cadastrais

A task `[BRUNO03]` (`bruno/Students/Register Student.bru`, ex-task `[43]` do
roteiro) declara no critério de aceite `Assert: res.status eq 200`, mas o
endpoint `POST /api/v1/students` foi implementado pelo `[STU00]`
(`src/main/java/com/schoolqueue/infrastructure/adapters/in/web/StudentController.java:59`)
retornando `ResponseEntity.created(location).body(...)` — ou seja, `201 Created`
com `Location` header. A coleção `Students` inteira está consistente em `201`
(mesma convenção de `Create School.bru`, `Create Classroom.bru` e
`Create Parent.bru`), e o teste `StudentControllerWebTest.shouldCreateStudent`
(`src/test/java/com/schoolqueue/infrastructure/adapters/in/web/StudentControllerWebTest.java:71`)
valida `isCreated()`. Reverter o endpoint para `200` quebraria a consistência
REST do projeto e o teste atual.

**Decisão (BRUNO03):** manter `201` no `Register Student.bru` e registrar
esta lacuna. O `.bru` já cumpre o restante do critério (payload com
`schoolId`, `classroomId`, `name`; `script:post-response` capturando
`studentId` em `bru.setVar`). Sem mudança funcional. #tests #bruno #rest #docs

### LAC28 — Correções aplicadas ao `bruno/Queue/Announce Arrival.bru` (BRUNO00)

A task `[BRUNO00]` (ex-task `[85]`; substitui o card `[44]` cancelado —
vide LAC10) pediu `POST /api/v1/queue/announce` com `latitude`/`longitude`
e asserts de `EN_ROUTE` + range derivado da distância. O
`bruno/Queue/Announce Arrival.bru` foi atualizado em quatro pontos
(`src/bruno/Queue/Announce Arrival.bru:13-31`):

1. **Assert errado** — usava `res.body.status eq EN_ROUTE` mas o campo
   correto no DTO é `journeyStatus` (vide
   `src/main/java/com/schoolqueue/infrastructure/adapters/in/web/dto/QueueItemResponse.java:14`).
   Corrigido para `res.body.journeyStatus: eq EN_ROUTE`.
2. **Asserts ausentes** — `currentRange eq CLOSE` e `called eq true`
   não estavam declarados. A coordenada do pai igual à da escola
   (`-23.550520, -46.633308`) garante distância Haversine `= 0`, que cai
   no limite `≤ 0.5 km` de `ProximityRange.fromDistanceKm`
   (`src/main/java/com/schoolqueue/domain/model/ProximityRange.java:14-22`),
   e a regra de domínio em `PickupQueueItem.updateRange` força
   `called = true` ao entrar em `CLOSE`.
3. **UUIDs hardcoded** — `schoolId`/`studentId`/`parentId` estavam
   congelados em literais (`a0eebc99-…`, `c9bf9e57-…`, `d3b07384-…`)
   que não correspondiam a nenhuma escola/aluno/responsável real,
   fazendo o request retornar 404 (`SchoolNotFoundException` /
   `StudentNotFoundException` / `ParentNotFoundException`) ao ser
   executado. Substituídos por `{{schoolId}}`/`{{studentId}}`/`{{parentId}}`,
   capturados pelos `.bru` anteriores (`Create School.bru`,
   `Register Student.bru`, `Create Parent.bru`).
4. **`queueItemId` não era capturado** — sem ele, o `Update Status.bru`
   (`bruno/Queue/Update Status.bru:9`) e o `List Active Queue.bru` (path
   via `{{schoolId}}`) não encadeiam com o anúncio. Adicionado
   `script:post-response { bru.setVar("queueItemId", res.body.id); }`,
   mesmo padrão do BRUNO02 (`bruno/Schools/Create School.bru:25-27`).

**`docs` block intencionalmente mantido desatualizado** — a nota "este
endpoint retorna 404 no estado atual do código" é falsa desde
`a55b51e ([API02])` / `d2ab7ac`, mas a decisão do usuário na BRUNO00 foi
deixar como está e apenas registrar a obsolescência aqui para revisão
futura.

Sem mudança em código Java, DTOs, testes ou schema. #tests #bruno #rest #docs

### LAC29 — Assert de conteúdo no `bruno/Queue/List Active Queue.bru` (BRUNO04)

A task `[BRUNO04]` (ex-task `[45]`) pediu `GET /api/v1/queue/school/{schoolId}/active`
com asserts `res.status eq 200` e "lista contém o aluno anunciado". O
`bruno/Queue/List Active Queue.bru` foi atualizado em um único ponto
(`bruno/Queue/List Active Queue.bru:15`):

- **Assert de conteúdo ausente** — o `.bru` original (commit `a55b51e`,
  `[API02]`) validava apenas `res.status: eq 200`. Adicionado
  `res("..[?(@.studentId == '{{studentId}}')]"): isNotEmpty` — usa a
  `res()` query do Bruno (vide
  https://docs.usebruno.com/testing/script/response/response-query) com
  filtro JSONPath para validar que existe pelo menos um item da lista
  cujo `studentId` é o do aluno anunciado, capturado em
  `bruno/Students/Register Student.bru` (`script:post-response` da
  BRUNO03). Forma declarativa, sem bloco JS, alinhada ao padrão
  assert-only das outras `.bru` da coleção.
- **Pré-requisito de encadeamento E2E** — para que o assert passe ao
  rodar a coleção na ordem, a sequência precisa ser
  `Create School.bru → Create Classroom.bru → Register Student.bru →
  Create Parent.bru → Announce Arrival.bru → List Active Queue.bru`. Os
  dois primeiros requests populam o `{{schoolId}}`/`{{classroomId}}`, o
  terceiro popula `{{studentId}}` (consumido pelo assert), o quarto
  popula `{{parentId}}` (consumido pelo Announce), o quinto popula
  `{{queueItemId}}` (consumido pelo `Update Status.bru`, que faz parte
  do fluxo da Fase 6 do roteiro mas não desta task).
- **`docs` block mantido** — já era fiel à realidade (cita
  `PickupQueueController`, `FetchActiveQueueUseCase`, lista
  `EN_ROUTE/ARRIVED`, 11 campos com `called` e `currentRange`), então
  não precisou de ajuste (decisão do usuário na BRUNO04).

Sem mudança em código Java, DTOs, testes ou schema. #tests #bruno #rest #docs
