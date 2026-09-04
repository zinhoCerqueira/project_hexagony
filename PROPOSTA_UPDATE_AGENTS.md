# 📝 Proposta de Atualização — AGENTS.md

> Documento de **planejamento futuro** (NÃO é um PR). Serve para você
> revisar, priorizar e decidir quais blocos aplicar quando quiser.
> Cada bloco abaixo é independente: pode entrar em commits separados
> (sugestão de prefixo `[CHORE]` ou `[DOCS]`) ou como uma task do
> roteiro.

---

## 🎯 Objetivo

Reduzir o **gap** entre o `AGENTS.md` atual e o estado real do
repositório: novas famílias de prefixo de commit, regras operacionais
que ficaram implícitas (CONF00, API02, TEST02), novas ferramentas
(spotless, jacoco, testcontainers rabbitmq) e referências cruzadas ao
backlog (LACxx) que já foram detectadas.

---

## 🧭 Como aplicar este arquivo

1. Escolher um bloco (ou um conjunto) abaixo.
2. Criar branch: `git checkout main && git checkout -b chore/update-agents-md-<bloco>`.
3. Aplicar **apenas** as alterações daquele bloco.
4. Commit: `[CHORE] AGENTS.md: <bloco>` (ou `[DOCS]` se preferir).
5. Seguir o fluxo padrão: perguntar antes de merge + push (ver
   "Convenção de branches" do AGENTS.md).

Os blocos são **mutuamente exclusivos** — não há ordem obrigatória.

---

## 🧱 Bloco 1 — Visão geral

**Arquivo alvo:** `AGENTS.md` (seção "Visão geral", linhas 7–12).

**Estado atual:**

```markdown
- **Projeto:** school-pickup-system — API de fila de embarque escolar (somente busca dos alunos pelos responsáveis; não há fluxo de desembarque).
- **Arquitetura:** Hexagonal (Ports & Adapters), Java 21, Spring Boot 3.x, Maven.
- **Infra local:** Docker Compose (PostgreSQL + RabbitMQ), credenciais em `.env` (raiz, não commitado).
- **Roteiro:** as tarefas são numeradas em `roteiro_desenvolvimento.md` (ex.: Task 10, Task 11).
  Referencie a task via prefixo no commit, conforme a seção "Convenção de commits".
```

**Proposta de substituição:**

```markdown
- **Projeto:** school-pickup-system — API de fila de embarque escolar dirigida por GPS (somente busca dos alunos pelos responsáveis; não há fluxo de desembarque).
- **Arquitetura:** Hexagonal (Ports & Adapters) **estrita** — `domain/` é Java puro (zero Spring/JPA), services em `application/usecase/` **sem `@Service`** (instanciados por `@Bean` explícito em `BeanConfiguration`).
- **Stack:** Java 21, Spring Boot 3.5.x, Spring Data JPA, Spring AMQP, Spring Validation, Flyway, PostgreSQL 16, RabbitMQ 3, Maven.
- **Regra-chave da fila:** o `ProximityRange` (FAR/MEDIUM/CLOSE) é calculado no Core por Haversine entre o GPS do responsável e o da escola; a transição de estados (`EN_ROUTE → ARRIVED → COMPLETED`, `CANCELLED`) mora na entidade `PickupQueueItem` e é exposta via sealed interface `QueueAction` em `UpdateQueueStatusUseCase`.
- **Mensageria:** 2 routing keys no exchange topic `school.queue.events` (`queue.arrival.announced`, `queue.status.changed`) bindadas na fila durável `queue.notifications`.
- **Infra local:** Docker Compose (PostgreSQL + RabbitMQ + pgAdmin), credenciais em `.env` (raiz, não commitado), volumes nomeados `pgdata` e `pgadmin-data` (Docker prefixa como `project_hexagony_*` — ver LAC18).
- **Roteiro:** `roteiro_desenvolvimento.md` define a especificação. **Convenção de commits:** ver seção dedicada (mais de uma família de prefixo convive hoje).
```

**Por que mudar:** hoje a seção esconde a parte mais importante do
projeto (GPS dirige a fila) e o status real do Hexagonal (estrito +
CONF00).

---

## 🧱 Bloco 2 — Convenção de branches

**Arquivo alvo:** `AGENTS.md` (seção "Convenção de branches", linhas
14–28).

**Estado atual:** fluxo com 5 passos numerados, sem mencionar `--no-ff`,
sem mencionar deleção local pós-merge.

**Proposta de complemento** (após o passo 5):

```markdown
6. **Pós-merge:** delete a branch de trabalho local com
   `git branch -d feature/<nome>`. A remoção da branch remota
   (`git push origin --delete feature/<nome>`) é opcional e fica a
   critério do usuário.
7. **Estratégia de merge:** use `--no-ff` em features com mais de um
   commit (preserva o histórico da task). Para chores de um único
   commit, fast-forward é aceitável.
```

**Por que mudar:** foi exatamente o que fizemos na task REVIEW e não
estava documentado.

---

## 🧱 Bloco 3 — Convenção de commits

**Arquivo alvo:** `AGENTS.md` (seção "Convenção de commits", linhas
37–47).

**Estado atual:** só cita `[Task NNN]`. O histórico real usa pelo menos
7 famílias.

**Proposta de substituição** (mantendo o espírito da seção):

```markdown
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
- Commits que não correspondem a nenhuma task/área não levam prefixo de
  área (ex.: `[CHORE] docs: ajusta AGENTS.md`).
- Commits atômicos: um commit por mudança coesa.
- Nunca commite secrets: `.env` e arquivos sensíveis estão no `.gitignore`.
- Revise `git status` e `git diff` antes de commitar; inclua apenas arquivos relacionados.
```

**Por que mudar:** cobre o que já é prática (LAC01, LAC02 resolvidas
pelo uso, mas ainda pendentes de documentação). Ver
`git log --oneline -30` para evidência.

---

## 🧱 Bloco 4 — Convenções de teste

**Arquivo alvo:** `AGENTS.md` (seção "Convenções de teste", linhas
49–56).

**Estado atual:** cita JUnit 5 + AssertJ + Mockito, sufixos `*Test`
vs `*IT`, padrão de nome. Não cita `@WebMvcTest`, `GlobalExceptionHandler`
test, JaCoCo, Spotless, Testcontainers RabbitMQ.

**Proposta de complemento** (após a linha 56):

```markdown
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
```

**Por que mudar:** hoje o agente novo só sabe que existe sufixo; não
sabe os padrões que de fato rodam (TEST02, BRUNOxx etc.).

---

## 🧱 Bloco 5 — Verificação

**Arquivo alvo:** `AGENTS.md` (seção "Verificação", linhas 58–61).

**Estado atual:** menciona `mvn test` e `docker compose config`.

**Proposta de substituição:**

```markdown
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
```

**Por que mudar:** amplia a lista de comandos efetivos sem mudar o
espírito da seção.

---

## 🧱 Bloco 6 — Persistência e volumes Docker (nota sobre nomes)

**Arquivo alvo:** `AGENTS.md` (tabela na seção "Persistência e volumes
Docker", linhas 67–70).

**Estado atual:** a tabela usa os nomes **já prefixados pelo Docker**
(`project_hexagony_pgdata`, `project_hexagony_pgadmin-data`). O
`docker-compose.yml` declara os volumes como `pgdata` e `pgadmin-data`;
o Docker prefixa com o nome do diretório do projeto.

**Proposta:** adicionar uma **linha de nota** ao final da tabela (sem
remover a tabela existente):

```markdown
> **Nota (LAC18):** os volumes são declarados em `docker/docker-compose.yml`
> como `pgdata` e `pgadmin-data`. O Docker prefixa automaticamente com o
> nome do diretório do projeto (`project_hexagony_*`). Os dois nomes se
> referem ao mesmo volume — não é divergência.
```

**Por que mudar:** evita que o agente novo caia na pegadinha da LAC18
ao copiar comandos de backup/restore.

---

## 🧱 Bloco 7 — Backlog: gatilhos de aviso

**Arquivo alvo:** `AGENTS.md` (seção "Backlog de lacunas encontradas",
linha 122).

**Estado atual:** *"Periodicamente, o agente **avisa o usuário** sobre
os cards pendentes no backlog"* — sem definir periodicidade.

**Proposta de complemento:**

```markdown
- **Gatilhos para avisar sobre o backlog:**
  1. Ao final de cada task (antes de merge).
  2. Ao retomar o trabalho (início de sessão).
  3. Quando 3+ cards novos forem acumulados desde o último aviso.
- O aviso deve listar os cards pendentes (id + título + hashtags) e
  perguntar ao usuário o que priorizar. Não resolve nada sozinho.
```

**Por que mudar:** define um contrato para o comportamento já esperado,
evitando tanto silêncio quanto ruído.

---

## 🧱 Bloco 8 — Nova seção: "Arquitetura de fato"

**Arquivo alvo:** `AGENTS.md` (nova seção, sugestão: após "Visão geral").

**Estado atual:** não existe.

**Proposta de adição:**

````markdown
## Arquitetura de fato (regras operacionais)

Regras que **valem** hoje no repositório e devem ser respeitadas em
qualquer task nova. Foram firmadas em CONF00, API02, TEST02, MSG02 etc.

- **`domain/` é Java puro.** Nenhuma anotação Spring, JPA, Jackson ou
  Lombok nas classes deste pacote. Portas (`ports/in/`, `ports/out/`)
  são interfaces; implementações concretas vivem em `application/` e
  `infrastructure/`.
- **Services em `application/usecase/` não usam `@Service`.** Eles
  são instanciados por `@Bean` explícitos em
  `infrastructure/config/BeanConfiguration.java`. Adicionar `@Service`
  aqui é regressão.
- **State machines novas devem usar `sealed interface`** com
  `permits` para os comandos, espelhando o padrão de
  `UpdateQueueStatusUseCase.QueueAction`. O compilador força a
  exaustividade no `switch`.
- **Records para DTOs e Commands HTTP/domínio** (DTOs em
  `infrastructure/adapters/in/web/dto/`, Commands nos ports `in`).
  Validação via Jakarta Validation direto nos componentes do record.
- **Mappers de persistência** vivem em
  `infrastructure/adapters/out/persistence/mapper/` e devem ter teste
  unitário de ida-e-volta (`*EntityMapperTest`).
- **Migrations Flyway** ficam em
  `src/main/resources/db/migration` (padrão `V<NNN>__<descrição>.sql`).
  A política é `spring.jpa.hibernate.ddl-auto = validate` — não usar
  `update` nem criar schema via Hibernate.
- **Eventos AMQP** são DTOs `record` em
  `infrastructure/adapters/out/messaging/dto/`. Publicação sempre via
  port `out` (`QueueNotificationPort`), nunca via `RabbitTemplate`
  direto no use case.
- **Não criar `REST DELETE`** sem task explícita: o padrão atual é
  responder `405 Method Not Allowed` para sinalizar que a operação
  ainda não foi modelada.
````

**Por que mudar:** essas regras eram conhecidas, mas estavam
espalhadas em commits, backlog e decisões pontuais — colocá-las no
AGENTS.md dá um contrato único para qualquer agente novo.

---

## 🧱 Bloco 9 — Nova seção: "Documentos canônicos do projeto"

**Arquivo alvo:** `AGENTS.md` (nova seção, sugestão: após "Verificação").

**Estado atual:** não existe.

**Proposta de adição:**

```markdown
## Documentos canônicos

Mapa dos arquivos de documentação do repositório e seu papel:

| Arquivo | Papel |
|---|---|
| `roteiro_desenvolvimento.md` | Especificação + backlog (seção 10). É a fonte da verdade do que *deveria* estar pronto. |
| `AGENTS.md` | Convenções para agentes e fluxo de contribuição. Este arquivo. |
| `REVIEW_FUNCIONAL_E_TESTES.md` | Guia E2E manual do que *está* implementado (cards por módulo + Bruno/cURL + Postgres + RabbitMQ). Sincronizar sempre que um módulo novo entrar. |
| `POST_LINKEDIN_TECNICO.md` | Post técnico (template). Atualizar quando houver marcos relevantes. |
| `bruno/` | Coleções HTTP versionadas. Fazem parte do contrato vivo da API (asserts validam respostas). |
| `docker/docker-compose.yml` + `docker/pgadmin/servers.json` | Infra local — ver "Persistência e volumes Docker". |

Quando criar/atualizar documentação:

- **Novos endpoints/recursos** ⇒ atualizar `REVIEW_FUNCIONAL_E_TESTES.md`
  e, se relevante, a coleção Bruno correspondente.
- **Novas regras de contribuição** ⇒ atualizar `AGENTS.md` neste
  arquivo (não em rascunhos soltos).
- **Novo marco técnico publicável** ⇒ considerar atualizar
  `POST_LINKEDIN_TECNICO.md` (a versão final é editável antes de colar
  no LinkedIn).
```

**Por que mudar:** hoje o agente novo pode não saber que existe
`REVIEW_FUNCIONAL_E_TESTES.md` e/ou tratá-lo como rascunho descartável.
---

## 🧱 Bloco 10 — Cross-reference explícita ao backlog

**Arquivo alvo:** `AGENTS.md` (seção "Backlog de lacunas encontradas",
linhas 109–124).

**Estado atual:** descreve o ciclo do backlog, mas não cita que o
backlog mora **dentro** do `roteiro_desenvolvimento.md` (seção 10),
nem que hoje há LAC01..LAC18 catalogados.

**Proposta de complemento** (no início da seção):

```markdown
> O backlog atual está em `roteiro_desenvolvimento.md`, seção
> **"🐞 10. Backlog de Lacunas Encontradas"** (LAC01..LAC18 no último
> snapshot). Esta seção do AGENTS.md descreve **apenas a política** de
> manutenção do backlog — o conteúdo em si fica no roteiro.
```

**Por que mudar:** reduz ambiguidade (qual é o arquivo "de verdade" do
backlog?).

---

## 📊 Mapa "bloco → impacto"

| Bloco | Linhas afetadas | Risco | Prefixo sugerido | Task do roteiro equivalente? |
|---|---|---|---|---|
| 1 — Visão geral | 7–12 | Baixo (só reescrita) | `[CHORE]` | — |
| 2 — Branches | 14–28 | Baixo | `[CHORE]` | — |
| 3 — Commits | 37–47 | Médio (cobre histórico) | `[CHORE]` | — (LAC01/LAC02 resolvidas pelo uso) |
| 4 — Testes | 49–56 | Baixo | `[CHORE]` | — |
| 5 — Verificação | 58–61 | Baixo | `[CHORE]` | — |
| 6 — Volumes | 67–70 | Baixo | `[CHORE]` | LAC18 |
| 7 — Backlog | 109–124 | Baixo | `[CHORE]` | — |
| 8 — Arquitetura de fato | **nova seção** | Médio (fixa contrato) | `[CHORE]` ou `[DOCS]` | — |
| 9 — Documentos canônicos | **nova seção** | Baixo | `[CHORE]` ou `[DOCS]` | — |
| 10 — Cross-ref backlog | 109–124 | Baixo | `[CHORE]` | — |

---

## 🗓️ Sugestão de ordem de aplicação

1. **Bloco 1** (Visão geral) — impacto mais alto, risco mais baixo.
2. **Bloco 3** (Commits) — fixa dívida documental conhecida.
3. **Bloco 8** (Arquitetura de fato) — vira contrato único para agentes novos.
4. **Bloco 9** (Documentos canônicos) — fecha o ciclo de docs.
5. Blocos 2, 4, 5, 6, 7, 10 — em qualquer ordem, podem ser 1 commit cada
   ou agrupados em um `[CHORE] docs: harmoniza AGENTS.md`.

Cada bloco é independente e pode virar 1 commit atômico (padrão da
"Convenção de commits").

---

## ✅ Checklist antes de aplicar (para cada bloco)

- [ ] Ler o bloco inteiro aqui e validar o texto final no `AGENTS.md`.
- [ ] Conferir se algum item foi superado por mudanças mais recentes
      (`git log`, `git status`).
- [ ] Criar branch: `git checkout main && git checkout -b chore/update-agents-md-<bloco>`.
- [ ] Aplicar **só** aquele bloco.
- [ ] Reler o arquivo inteiro para garantir coerência.
- [ ] Perguntar antes de commit, merge e push (ver "Convenção de commits"
      do AGENTS.md atual).
- [ ] Atualizar esta proposta se algo mudou durante a aplicação
      (ex.: linha mudou, novo bloco apareceu).

---

## 📎 Anexos / referências

- `roteiro_desenvolvimento.md` linhas 1029+ → backlog LAC01..LAC18.
- `git log --oneline -30` → evidência das famílias de prefixo.
- `pom.xml` → deps de Spotless, JaCoCo, Testcontainers RabbitMQ
  referenciadas nos blocos 4 e 5.
- `REVIEW_FUNCIONAL_E_TESTES.md` → citado no bloco 9.
- `POST_LINKEDIN_TECNICO.md` → citado no bloco 9.