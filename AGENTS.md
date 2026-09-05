# AGENTS.md

Contexto para agentes que trabalham neste repositório. Este arquivo define como o
versionamento e a organização do trabalho devem ser conduzidos a partir de agora.

## Visão geral

- **Projeto:** school-pickup-system — API de fila de embarque escolar dirigida por GPS (somente busca dos alunos pelos responsáveis; não há fluxo de desembarque).
- **Arquitetura:** Hexagonal (Ports & Adapters) **estrita** — `domain/` é Java puro (zero Spring/JPA), services em `application/usecase/` **sem `@Service`** (instanciados por `@Bean` explícito em `BeanConfiguration`).
- **Stack:** Java 21, Spring Boot 3.5.x, Spring Data JPA, Spring AMQP, Spring Validation, Flyway, PostgreSQL 16, RabbitMQ 3, Maven.
- **Regra-chave da fila:** o `ProximityRange` (FAR/MEDIUM/CLOSE) é calculado no Core por Haversine entre o GPS do responsável e o da escola; a transição de estados (`EN_ROUTE → ARRIVED → COMPLETED`, `CANCELLED`) mora na entidade `PickupQueueItem` e é exposta via sealed interface `QueueAction` em `UpdateQueueStatusUseCase`.
- **Mensageria:** 2 routing keys no exchange topic `school.queue.events` (`queue.arrival.announced`, `queue.status.changed`) bindadas na fila durável `queue.notifications`.
- **Infra local:** Docker Compose (PostgreSQL + RabbitMQ + pgAdmin), credenciais em `.env` (raiz, não commitado), volumes nomeados `pgdata` e `pgadmin-data` (Docker prefixa como `project_hexagony_*` — ver LAC18).
- **Roteiro:** `roteiro_desenvolvimento.md` define a especificação. **Convenção de commits:** ver seção dedicada (mais de uma família de prefixo convive hoje).

## Convenção de branches

Projeto pessoal (desenvolvimento solo): apenas duas branches coexistem por vez.

- `main` — branch base e estável. Todo trabalho novo parte daqui e integra de volta aqui.
- Branch de trabalho da task — `feature/<nome>`, `fix/<nome>` ou `chore/<nome>`.
  Ex.: `feature/announce-arrival`, `chore/spotless`.

Fluxo padrão:

1. `git checkout main && git pull`
2. `git checkout -b feature/<nome>`
3. Trabalhe e faça commits atômicos (sempre pergunte antes de commitar — ver "Convenção de commits").
4. Apresente o resumo de encerramento da task (ver seção abaixo).
5. Com permissão explícita do usuário, integre em `main` (merge) e delete a branch de trabalho.
6. **Pós-merge:** delete a branch de trabalho local com
   `git branch -d feature/<nome>`. A remoção da branch remota
   (`git push origin --delete feature/<nome>`) é opcional e fica a
   critério do usuário.
7. **Estratégia de merge:** use `--no-ff` em features com mais de um
   commit (preserva o histórico da task). Para chores de um único
   commit, fast-forward é aceitável.

## Encerramento de task

- Toda task termina com um **resumo do que foi feito**: arquivos criados/alterados,
  decisões relevantes, verificação executada (build/testes) e os commits gerados.
- Commit, merge para `main` e push **nunca são automáticos**: pergunte sempre ao usuário
  se deve executar cada etapa e só prossiga após aprovação explícita.

## Convenção de commits

- Commits **nunca são automáticos**: antes de commitar, pergunte ao usuário se deve
  prosseguir e só execute após aprovação explícita.
- Mensagens em **português**, descritivas e no estilo do histórico existente.
- **Famílias de prefixo aceitas** (ver `git log --oneline` para exemplos):

  | Prefixo | Uso típico |
  |---|---|
  | `[Task NNN]` | Tasks numéricas antigas do roteiro. |
  | `[GPSnn]` | Compartilhamento de GPS / `LocationSharing*`. |
  | `[FILAnn]` | Domínio e use cases da fila de embarque. |
  | `[CADnn]` | CRUD `School` (escola). |
  | `[CLAnn]` | CRUD `Classroom` (turma/sala). |
  | `[PARnn]` | CRUD `Parent` (responsável). |
  | `[STUnn]` | CRUD `Student` (aluno, inclusive N:N com Parent). |
  | `[OPSnn]` | Operações de infra/credenciais/startup. |
  | `[APInn]` | Adaptadores REST (controllers, DTOs, mappers, GlobalExceptionHandler). |
  | `[MSGnn]` | Mensageria (RabbitMQ). |
  | `[CONFnn]` | Configuração Spring / Beans. |
  | `[TESTnn]` | Cobertura de testes (especialmente regressões). |
  | `[BRUNOnn]` | Coleções Bruno (`.bru`). |
  | `[REVIEW]` | Auditorias / reviews técnicos. |
  | `[FIXnn]` | Bugfixes sem task atrelada. |
  | `[CHOREnn]` | Chores avulsos (atualização de AGENTS, deps, etc.). |

  Famílias adicionais podem ser criadas via task do roteiro;
  padronize 2 dígitos (`GPS00`, `API02`) — `[Task]` é a única exceção.

- Cards de backlog (LACxx em `roteiro_desenvolvimento.md`) **não** viram
  prefixo de commit por conta própria — eles entram quando viram task.
- **Commits sem prefixo `[...]`** também são aceitos em dois casos:
  1. **Integrações** para `main` (mensagem no estilo
     `Integra [XYZnn] <descrição> em main`) — refletem o merge da
     branch de trabalho.
  2. **Avulsos curtos** que não cabem em família (ex.: `chore: ...`,
     `Alinha payload do Bruno...`, `Registra LACxx: ...`) — preferir
     sempre que possível alinhar a uma família existente.
- Commits atômicos: um commit por mudança coesa.
- Nunca commite secrets: `.env` e arquivos sensíveis estão no `.gitignore`.
- Revise `git status` e `git diff` antes de commitar; inclua apenas arquivos relacionados.

## Convenções de teste

- O pacote de teste espelha o de produção: `src/test/java/com/schoolqueue/...` corresponde a `src/main/java/com/schoolqueue/...`.
- Stack: JUnit 5 + **AssertJ** (assertions fluent) + **Mockito** (mocks das `Ports`). No Core de domínio, zero dependência do Spring.
- `@DisplayName` descritivo em inglês; nomes de método no padrão Given/When/Then,
  ex.: `shouldTransitionToArrivedWhenEnRoute`.
- Sufixos: `*Test` = teste unitário (Surefire, roda no `mvn test`, sem Docker);
  `*IT` = teste de integração (Failsafe + Testcontainers, roda no `mvn verify`).

### Padrões adicionais em uso

- **Testes de controller (`@WebMvcTest`)** — usar `@MockitoBean` nos
  `ports.in` (use cases), manter o `GlobalExceptionHandler` real no
  contexto e validar 200/400/404/409 conforme contrato.
- **Testes do `GlobalExceptionHandler`** — feito de forma
  **standalone** (instanciar o advice diretamente, sem Spring/MockMvc)
  para regressão isolada dos mapeamentos (`InvalidQueueStateException →
  409`, `IllegalStateException → 400`, `MethodArgumentNotValidException →
  400`, `*NotFoundException → 404`).
- **Testcontainers** — usado tanto para **PostgreSQL** quanto para
  **RabbitMQ** (deps já presentes no `pom.xml`). Roda em `mvn verify`,
  não em `mvn test`.
- **Cobertura (JaCoCo)** — gate configurado em `pom.xml`. Rodar
  `mvn verify` para gerar relatório em `target/site/jacoco/index.html`.
- **Formatação (Spotless + googleJavaFormat)** — `mvn spotless:check`
  falha o build se houver desvio; `mvn spotless:apply` corrige.

## Verificação

- **Unit + Web:** `mvn test` (Surefire, sem Docker).
- **Tudo (com Testcontainers):** `mvn verify` (Surefire + Failsafe +
  JaCoCo report).
- **Formato:** `mvn spotless:check` (CI gate) ou
  `mvn spotless:apply` (corrige local).
- **Compose:** validar antes de subir —
  `docker compose --project-directory . -f docker/docker-compose.yml config`.
- **Sem wrapper `mvnw`** neste projeto: usar o Maven do sistema
  (`mvn -v` para conferir).
- **Convenção geral:** antes de PR, rodar `mvn verify` localmente; o
  CI pode repetir o mesmo gate.

## Persistência e volumes Docker

O `docker/docker-compose.yml` declara dois volumes nomeados que carregam
**todo o estado do projeto**:

| Volume | Conteúdo | Consequência se removido |
|---|---|---|
| `project_hexagony_pgdata` | Diretório `/var/lib/postgresql` (PGDATA aponta para `pgdata_app/`) — schema Flyway + dados de `schools`, `students`, `pickup_queue`, etc. O cluster Postgres mora num subdir dedicado, **não** na raiz do volume. | Postgres reinicia vazio, Flyway reaplica a `V1` e **todos os dados somem**. Reset seguro e rápido: `docker compose down -v pgdata` (afeta só este volume; `pgadmin-data` permanece intacto). |
| `project_hexagony_pgadmin-data` | `pgadmin4.db` (lista de servers, usuários, preferências) | pgAdmin reseta; o server `school-queue-db` precisa ser reimportado via `docker/pgadmin/servers.json` (já acontece se o arquivo estiver versionado). |

> **Nota (LAC18):** os volumes são declarados em `docker/docker-compose.yml`
> como `pgdata` e `pgadmin-data`. O Docker prefixa automaticamente com o
> nome do diretório do projeto (`project_hexagony_*`). Os dois nomes se
> referem ao mesmo volume — não é divergência.

**Comandos que APAGAM dados sem aviso:**

- `docker compose down -v` — remove **todos** os volumes nomeados do projeto. **Nunca usar** em ambiente de estudo sem backup.
- `docker volume rm <nome>` / `docker volume prune` / `docker system prune --volumes` — apagam volumes órfãos. Volumes viram "órfãos" quando nenhum container os referencia (ex.: depois de um `down` simples).
- Reset de fábrica do Docker Desktop, atualização/reinstalação do Docker, ou falha de disco — podem descartar `/var/lib/docker/volumes` inteiro.

**Boas práticas (sempre que for mexer em infra):**

1. **Backup lógico antes de qualquer coisa arriscada** (gera um `.sql` que pode ser commitado ou versionado fora do repo):
   ```bash
   docker exec school_queue_db \
     pg_dump -U queue_user -d school_queue_db > backup-$(date +%Y%m%d-%H%M).sql
   ```
   Restaurar:
   ```bash
   cat backup-YYYYMMDD-HHMM.sql | docker exec -i school_queue_db \
     psql -U queue_user -d school_queue_db
   ```
2. **Backup binário do volume inteiro** (mais pesado; copia fiel do cluster):
   ```bash
   docker run --rm -v project_hexagony_pgdata:/from -v $(pwd):/to alpine \
     tar czf /to/pgdata-backup-$(date +%Y%m%d).tar.gz -C /from .
   ```
3. **Sempre conferir antes de `prune`**: `docker volume prune --dry-run` lista o que sairia.
4. **Não confiar em `restart: always`** — o `docker-compose.yml` não o declara. Container que cai e ninguém reinicia deixa o volume órfão e exposto ao `prune`.
5. **Versão do `docker/pgadmin/servers.json`** garante que o server `school-queue-db` volta na próxima subida mesmo se o `pgadmin4.db` for perdido. **Não** confie nele para o `pgdata` — esse é só `pg_dump`.

**Onde encontrar o que está montado no Postgres:**

- `docker exec school_queue_db psql -U queue_user -d school_queue_db -c "\dn+"`
  lista schemas; `\dt public.*` lista tabelas.
- `SELECT datname, numbackends, xact_commit, tup_inserted FROM pg_stat_database;`
  mostra atividade acumulada por banco.
- `SELECT * FROM pg_stat_activity WHERE datname='school_queue_db';`
  lista conexões ativas (HikariCP do Spring, pgAdmin Dashboard, psql, etc.).

## Backlog de lacunas encontradas

- Ao longo do desenvolvimento, sempre que o agente identificar uma lacuna relevante
  (inconsistência entre código/roteiro, falta de teste, desvio de padrão, etc.),
  registrar um **card** no final de `roteiro_desenvolvimento.md`, na seção
  `## 🐞 10. Backlog de Lacunas Encontradas`.
- Cada card segue o estilo dos cards de roteiro: **título curto**, **hashtags**
  no final (`#backend`, `#rest`, `#test`, `#db`, `#docs`, `#arch`, …) e o
  **problema/lacuna** descrita em 1–3 linhas. Quando fizer sentido, incluir
  referência a arquivos com `file:line` e o que a solução envolveria.
- Os cards **acumulam**: o agente não os resolve sozinho, apenas registra. Não há
  remoção sem o usuário pedir. Quando o card virar uma task do roteiro, ele pode
  permanecer como referência ou ser marcado como resolvido (a critério do
  usuário).
- Periodicamente, o agente **avisa o usuário** sobre os cards pendentes no
  backlog (ex.: ao final de uma task, ao retomar o trabalho, ou quando ficarem
  muitos). O usuário decide o que priorizar.
- **Gatilhos para avisar sobre o backlog:**
  1. Ao final de cada task (antes de merge).
  2. Ao retomar o trabalho (início de sessão).
  3. Quando 3+ cards novos forem acumulados desde o último aviso.
- O aviso deve listar os cards pendentes (id + título + hashtags) e
  perguntar ao usuário o que priorizar. Não resolve nada sozinho.