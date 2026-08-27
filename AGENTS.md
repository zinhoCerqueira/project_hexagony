# AGENTS.md

Contexto para agentes que trabalham neste repositório. Este arquivo define como o
versionamento e a organização do trabalho devem ser conduzidos a partir de agora.

## Visão geral

- **Projeto:** school-pickup-system — API de fila de embarque escolar (somente busca dos alunos pelos responsáveis; não há fluxo de desembarque).
- **Arquitetura:** Hexagonal (Ports & Adapters), Java 21, Spring Boot 3.x, Maven.
- **Infra local:** Docker Compose (PostgreSQL + RabbitMQ), credenciais em `.env` (raiz, não commitado).
- **Roteiro:** as tarefas são numeradas em `roteiro_desenvolvimento.md` (ex.: Task 10, Task 11).
  Referencie a task via prefixo no commit, conforme a seção "Convenção de commits".

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

## Encerramento de task

- Toda task termina com um **resumo do que foi feito**: arquivos criados/alterados,
  decisões relevantes, verificação executada (build/testes) e os commits gerados.
- Commit, merge para `main` e push **nunca são automáticos**: pergunte sempre ao usuário
  se deve executar cada etapa e só prossiga após aprovação explícita.

## Convenção de commits

- Commits **nunca são automáticos**: antes de commitar, pergunte ao usuário se deve
  prosseguir e só execute após aprovação explícita.
- Mensagens em **português**, descritivas e no estilo do histórico existente.
- Quando o trabalho atende a uma task do roteiro, inicie a mensagem com o prefixo
  `[Task NNN]`, seguido do verbo/descrição. Ex.: `[Task 10] Setup .env com credenciais centralizadas`.
- Commits que não correspondem a nenhuma task não levam o prefixo (ex.: ajustes de docs, chores avulsos).
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

## Verificação

- Rodar build/testes Maven quando relevante: `mvn test` (sem wrapper `mvnw`; usar o Maven do sistema).
- Validar o Compose antes de subir: `docker compose --project-directory . -f docker/docker-compose.yml config`.

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