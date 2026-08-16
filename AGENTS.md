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

- `main` — código estável. Recebe apenas integrações revisadas.
- `develop` — branch de integração. Todo trabalho novo parte daqui.
- `feature/<nome>` — novas funcionalidades (parte de `develop`, integra de volta em `develop`).
  Ex.: `feature/announce-arrival`.
- `fix/<nome>` — correções de bugs.
- `chore/<nome>` — tarefas de manutenção/setup (devops, tooling, infra).

Fluxo padrão:

1. `git checkout develop && git pull`
2. `git checkout -b feature/<nome>`
3. Trabalhe e faça commits atômicos.
4. Integre de volta em `develop` (merge/pull request).

## Convenção de commits

- Mensagens em **português**, descritivas e no estilo do histórico existente.
- Comece com um verbo/setup (ex.: "Setup", "Adiciona", "Corrige", "Refatora", "Torna explícito").
- Commits atômicos: um commit por mudança coesa.
- Quando o trabalho atende a uma task do roteiro, mencione o número dela ao final da mensagem.
  Ex.: `Setup .env com credenciais centralizadas (Task 10)`.
- Nunca commite secrets: `.env` e arquivos sensíveis estão no `.gitignore`.
- Revise `git status` e `git diff` antes de commitar; inclua apenas arquivos relacionados.

## Verificação

- Rodar build/testes Maven quando relevante: `./mvnw test`.
- Validar o Compose antes de subir: `docker compose --project-directory . -f docker/docker-compose.yml config`.