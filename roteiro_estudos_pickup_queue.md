# 🚀 Roteiro Prático de Estudos: Sistema de Fila de Saída Escolar (Pickup Queue)

> **Arquitetura Target:** Arquitetura Hexagonal (Ports & Adapters)  
> **Linguagem & Ecossistema:** Java 21, Spring Boot 3.x, Maven  
> **Infraestrutura Local:** Docker & Docker Compose  
> **Estratégia de Validação:** Testes e Execução REST via Bruno Client  

---

## 📋 1. Visão Geral e Contexto do Domínio

### O Problema
Nos horários de saída escolar, o trânsito nos arredores das escolas torna-se caótico devido a filas duplas e tempo de espera excessivo dos alunos no portão. 

### A Solução
O **Pickup Queue System** permite que os pais/responsáveis avisem à escola através de uma aplicação que estão a caminho ("Estou chegando") ou chegaram ao perímetro. O sistema posiciona o aluno na **Fila de Saída da Escola**, permitindo que os inspetores/professores na portaria e na sala de aula preparem e encaminhem o aluno com antecedência, otimizando o fluxo e garantindo a segurança.

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
│   │   │           │   │   ├── School.java
│   │   │           │   │   ├── Classroom.java
│   │   │           │   │   ├── Student.java
│   │   │           │   │   ├── Parent.java
│   │   │           │   │   ├── PickupQueueItem.java
│   │   │           │   │   └── QueueStatus.java        <-- (EN ROUTE, ARRIVED, CALLED, COMPLETED, CANCELLED)
│   │   │           │   ├── exception/
│   │   │           │   │   ├── StudentNotFoundException.java
│   │   │           │   │   └── InvalidQueueStateException.java
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
│           └── com/schoolqueue/domain/                 <-- Testes unitários do Core puro
│               └── PickupQueueItemTest.java
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
 +------------------+           +------------------+
          ^                              ^
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
              +---------------------+
              |   PickupQueueItem   |
              |---------------------|
              | id: UUID            |
              | arrivalTime: Instant|
              | status: Enum        |
              | estimatedEtaMinutes |
              +---------------------+
```

### Estados da Fila (QueueStatus):
- **EN_ROUTE:** Pai notificou que está a caminho.
- **ARRIVED:** Pai chegou no perímetro escolar.
- **CALLED:** Aluno foi chamado na sala de aula.
- **COMPLETED:** Aluno entregue ao responsável.
- **CANCELLED:** Chamada descartada/cancelada.

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
      - ./postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
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

volumes:
  pgdata:
```

### Script Inicial SQL (`docker/postgres/init.sql`)

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE schools (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL
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
    status VARCHAR(50) NOT NULL,
    estimated_eta_minutes INT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

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
    private QueueStatus status;
    private Integer estimatedEtaMinutes;
    private final Instant createdAt;
    private Instant updatedAt;

    public PickupQueueItem(UUID id, UUID schoolId, UUID studentId, UUID parentId, Integer estimatedEtaMinutes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.schoolId = schoolId;
        this.studentId = studentId;
        this.parentId = parentId;
        this.status = QueueStatus.EN_ROUTE;
        this.estimatedEtaMinutes = estimatedEtaMinutes;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Regras de Negócio Puras
    public void markAsArrived() {
        if (this.status != QueueStatus.EN_ROUTE) {
            throw new InvalidQueueStateException("Apenas alunos a caminho podem ser marcados como 'Chegou'");
        }
        this.status = QueueStatus.ARRIVED;
        this.updatedAt = Instant.now();
    }

    public void markAsCompleted() {
        if (this.status != QueueStatus.ARRIVED && this.status != QueueStatus.CALLED) {
            throw new InvalidQueueStateException("Aluno não pode ser entregue sem ter chegado");
        }
        this.status = QueueStatus.COMPLETED;
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
        Integer etaMinutes
    ) {}
}
```

### Passo 3: O Caso de Uso (Application Service)

```java
// File: src/main/java/com/schoolqueue/application/usecase/AnnounceArrivalService.java
package com.schoolqueue.application.usecase;

import com.schoolqueue.domain.model.PickupQueueItem;
import com.schoolqueue.domain.ports.in.AnnounceArrivalUseCase;
import com.schoolqueue.domain.ports.out.QueueNotificationPort;
import com.schoolqueue.domain.ports.out.QueueRepositoryPort;

public class AnnounceArrivalService implements AnnounceArrivalUseCase {

    private final QueueRepositoryPort queueRepositoryPort;
    private final QueueNotificationPort notificationPort;

    public AnnounceArrivalService(QueueRepositoryPort queueRepositoryPort, QueueNotificationPort notificationPort) {
        this.queueRepositoryPort = queueRepositoryPort;
        this.notificationPort = notificationPort;
    }

    @Override
    public PickupQueueItem execute(AnnounceArrivalCommand command) {
        // Valida se já existe uma chamada ativa para esse aluno
        queueRepositoryPort.findActiveByStudentId(command.studentId())
            .ifPresent(item -> {
                throw new IllegalStateException("Já existe um aviso de saída ativo para este aluno.");
            });

        PickupQueueItem newItem = new PickupQueueItem(
            null,
            command.schoolId(),
            command.studentId(),
            command.parentId(),
            command.etaMinutes()
        );

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
            request.etaMinutes()
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
    "etaMinutes": 10
  }
}

assert {
  res.status: eq 200
  res.body.status: eq EN_ROUTE
}
```

---

## 📍 8. Feature Complementar: Compartilhamento de GPS por 15 Minutos

> **Importante:** Esta feature deve ser desenvolvida **separadamente**, apenas **após** a conclusão e validação de toda a funcionalidade de "avisar que está indo buscar" (Anunciar Chegada) **sem** a parte de GPS. O objetivo é manter o escopo base da fila de saída funcional e testado antes de evoluir com rastreamento de localização.

### O Problema
O responsável avisa que está indo buscar o aluno, mas a escola não tem como estimar com precisão o momento real da chegada do responsável ao portão, gerando chamadas prematuras ou atrasadas do aluno.

### A Solução
Quando o responsável notificar a escola que está indo buscar o aluno (ação do `AnnounceArrivalUseCase`), o sistema iniciará uma **sessão de compartilhamento de localização (GPS) do responsável com a escola** com duração de **15 minutos**. Durante esse período, a escola poderá acompanhar a posição em tempo real do responsável se aproximando do perímetro escolar, refinando a estimativa de chegada e preparando o aluno na hora certa.

### Novos Elementos de Domínio (Core)

**Estados da Sessão de Compartilhamento (`LocationSharingStatus`):**
- **ACTIVE:** Compartilhamento em andamento (dentro dos 15 minutos).
- **EXPIRED:** Período de 15 minutos encerrado (compartilhamento automaticamente desativado).

**Entidade Pura `LocationSharingSession`:**
```java
public class LocationSharingSession {
    private final UUID id;
    private final UUID queueItemId;   // vínculo com o item da fila
    private final UUID parentId;
    private final UUID schoolId;
    private final Instant startedAt;  // = momento do anúncio de chegada
    private final Instant expiresAt;  // = startedAt + 15 minutes
    private LocationSharingStatus status;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Instant lastUpdatedAt;

    // Regras de Negócio Puras
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        if (this.status != LocationSharingStatus.ACTIVE) {
            throw new InvalidSharingSessionException("Sessão de GPS expirada ou inativa.");
        }
        if (Instant.now().isAfter(this.expiresAt)) {
            this.status = LocationSharingStatus.EXPIRED;
            throw new InvalidSharingSessionException("Tempo de compartilhamento (15 min) encerrado.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdatedAt = Instant.now();
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
    LocationSharingSession execute(UpdateLocationCommand command);
    record UpdateLocationCommand(UUID sharingSessionId, BigDecimal latitude, BigDecimal longitude) {}
}

public interface FetchSharedLocationUseCase {
    Optional<LocationSharingSession> execute(UUID schoolId, UUID sharingSessionId);
}
```

**Driven Port:**
```java
public interface LocationSharingRepositoryPort {
    LocationSharingSession save(LocationSharingSession session);
    Optional<LocationSharingSession> findByQueueItemId(UUID queueItemId);
    List<LocationSharingSession> findActiveBySchoolId(UUID schoolId);
}
```

### Integração com o Fluxo Existente
- O `AnnounceArrivalService` passa a acionar também o `StartLocationSharingUseCase` ao salvar um novo item na fila (opcional e desacoplado — o fluxo principal não depende do GPS para funcionar).
- Adaptadores de entrada adicionais no Controller REST:
  - `POST /api/v1/location-sharing` — iniciar compartilhamento (ou retornado automaticamente pela resposta do `/announce`).
  - `PATCH /api/v1/location-sharing/{id}/location` — receber atualização de GPS do responsável.
  - `GET /api/v1/location-sharing/school/{schoolId}/active` — escola consulta sessões ativas.
- **Nota de realidade técnica:** Em um app real, o GPS seria enviado periodicamente pelo aplicativo do responsável via webhook/websocket; para fins de estudo, os endpoints REST acima simulam esse comportamento.

### Regras de Negócio Resumidas
- Compartilhamento inicia junto com o anúncio de chegada (`EN_ROUTE`).
- Duração fixa de **15 minutos** a partir do início (auto-expiração ao validar qualquer atualização).
- Apenas a escola vinculada pode consultar a localização.
- Após o expirar ou a entrega concluída, a sessão é encerrada (nenhum dado de localização é persistido além do necessário).

---

## 🚀 9. Checklist Prático do Roteiro de Estudos

Siga a ordem sequencial abaixo para construir o projeto do zero:

- [ ] **Fase 1: Infraestrutura**
  - [ ] Subir o container Docker (`docker-compose up -d`).
  - [ ] Validar a criação do banco executando a query `SELECT * FROM schools;` via cliente Postgres.
- [ ] **Fase 2: Core (Domínio Puramente Java)**
  - [ ] Criar as Entidades Java puras (`School`, `Student`, `Parent`, `PickupQueueItem`).
  - [ ] Criar Enums e Exceções de Domínio.
  - [ ] Escrever Testes Unitários com JUnit 5 para os métodos de transição de estado na entidade `PickupQueueItem`.
- [ ] **Fase 3: Contratos & Casos de Uso**
  - [ ] Criar os pacotes `ports.in` e `ports.out`.
  - [ ] Implementar as interfaces dos Use Cases.
  - [ ] Implementar as classes de Serviço que orquestram a lógica no pacote `application`.
- [ ] **Fase 4: Adaptadores de Banco de Dados**
  - [ ] Criar as entidades JPA (`@Entity`) no pacote `infrastructure.adapters.out.persistence.entity`.
  - [ ] Criar os Mappers para converter entre `Domain Model` <-> `JPA Entity`.
  - [ ] Implementar a classe `QueuePersistenceAdapter` que assina o contrato `QueueRepositoryPort`.
- [ ] **Fase 5: Adaptadores REST e Configurações Spring**
  - [ ] Criar as classes de configuração `@Configuration` para publicar os Beans de Domínio.
  - [ ] Criar o Controller REST `@RestController`.
  - [ ] Executar a aplicação Spring Boot.
- [ ] **Fase 6: Testes E2E com Bruno**
  - [ ] Executar as requisições na ordem: Criar Escola -> Cadastrar Aluno -> Anunciar Chegada -> Marcar Chegada -> Finalizar Entregas.
- [ ] **Fase 7 (separada): Compartilhamento de GPS por 15 Minutos** — *somente após as Fases 1–6 concluídas e validadas, sem a parte de GPS*
  - [ ] Criar o enum `LocationSharingStatus` e a entidade pura `LocationSharingSession` no Core com a regra de auto-expiração de 15 minutos.
  - [ ] Escrever testes unitários (JUnit 5) para a regra de expiração dos 15 minutos na entidade.
  - [ ] Criar as portas `StartLocationSharingUseCase`, `UpdateParentLocationUseCase`, `FetchSharedLocationUseCase` e `LocationSharingRepositoryPort`.
  - [ ] Implementar os serviços no pacote `application` e integrar (desacoplado) ao `AnnounceArrivalService`.
  - [ ] Criar a entidade JPA, mapper e adaptador de persistência para `LocationSharingSession`.
  - [ ] Adicionar a migração SQL da tabela `location_sharing`.
  - [ ] Expor os endpoints REST de iniciar compartilhamento, atualizar localização e consultar sessões ativas.
  - [ ] Testar via Bruno: Anunciar Chegada -> Atualizar GPS -> Consultar localização na escola -> Validar expiração após 15 min.
