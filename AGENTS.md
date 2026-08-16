# AGENTS.md

Contexto para agentes que trabalham neste repositório. Este arquivo define como o
versionamento e a organização do trabalho devem ser conduzidos a partir de agora.

## Visão geral

- **Projeto:** school-pickup-system — API de fila de embarque/desembarque escolar.
- **Arquitetura:** Hexagonal (Ports & Adapters), Java 21, Spring Boot 3.x, Maven.
- **Infra local:** Docker Compose (PostgreSQL + RabbitMQ), credenciais em `.env` (raiz, não commitado).
- **Roteiro:** as tarefas são numeradas em `roteiro_desenvolvimento.md` (ex.: Task 10, Task 11).
  Referencie o número da task nos commits quando o trabalho corresponder a uma delas.

## Convenção de branches

Projeto pessoal (desenvolvimento solo): apenas duas branches coexistem por vez.

- `main` — branch base e estável. Todo trabalho novo parte daqui e integra de volta aqui.
- Branch de trabalho da task — `feature/<nome>`, `fix/<nome>` ou `chore/<nome>`.
  Ex.: `feature/announce-arrival`, `chore/spotless`.

Fluxo padrão:

1. `git checkout main && git pull`
2. `git checkout -b feature/<nome>`
3. Trabalhe e faça commits atômicos.
4. Integre de volta em `main` (merge/pull request) e delete a branch de trabalho.

## Convenção de commits

- Mensagens em **português**, descritivas e no estilo do histórico existente.
- Comece com um verbo/setup (ex.: "Setup", "Adiciona", "Corrige", "Refatora", "Torna explícito").
- Commits atômicos: um commit por mudança coesa.
- Quando o trabalho atende a uma task do roteiro, mencione o número dela ao final da mensagem.
  Ex.: `Setup .env com credenciais centralizadas (Task 10)`.
- Nunca commite secrets: `.env` e arquivos sensíveis estão no `.gitignore`.
- Revise `git status` e `git diff` antes de commitar; inclua apenas arquivos relacionados.

## Verificação

- Rodar build/testes Maven quando relevante: `mvn test` (sem wrapper `mvnw`; usar o Maven do sistema).
- Validar o Compose antes de subir: `docker compose --project-directory . -f docker/docker-compose.yml config`.