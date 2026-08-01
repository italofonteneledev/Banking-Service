## Context

A aplicação é Quarkus 3.33 (Java 21, RESTEasy Reactive via `quarkus-rest-jackson`), sem qualquer tratamento de erro central. A exceção existente (`AgenciaNaoAtivaOuNaoEncontradaException`) é `RuntimeException` solta e hoje vira 500 sem corpo. A adaptação migra o padrão Spring (`@RestControllerAdvice` + `ProblemDetail`) para o equivalente idiomático do Quarkus/JAX-RS. Ver proposal.md - Why para a motivação.

## Goals / Non-Goals

**Goals:**
- Reestabelecer o padrão de tratamento de erro do autor em Quarkus, sem acoplar a Spring.
- Respostas de erro 100% RFC 7807 (`application/problem+json`), status correto por exceção.
- Ponto único de mapeamento para exceções de domínio e de validação.

**Non-Goals:**
- Não introduzir bibliotecas externas de Problem Details (ex.: `quarkus-problem-something`); usar apenas servlet/JAX-RS + Jackson já presentes no projeto.
- Não refatorar todos os controllers agora — apenas garantir o caminho das exceções existentes.
- Não padronizar logging/observability de erros (futuro).

## Decisions

### Decisão 1: JAX-RS `ExceptionMapper` em vez de `@ControllerAdvice`
No Quarkus não existe `@RestControllerAdvice`. O equivalente idiomático são `ExceptionMapper<E extends Throwable>` do JAX-RS, descobertos via CDI (`@Provider`). Usaremos:
- `BankingExceptionMapper implements ExceptionMapper<BankingException>` → delega para `bankingException.toProblemDetail()` e retorna `Response` com content-type `application/problem+json` (`jakarta.ws.rs.core.Response`).
- `ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException>` (trata tanto validação de parâmetros de método quanto `@Valid` no body quando propagada) → monta Problem Details 422 com `invalid-params`.

Justificativa: nativo do JAX-RS, sem dependências extras, integra-se ao ciclo RESTEasy Reactive. Alternativa considerada: `@ServerExceptionMapper` (RESTEasy Reactive específico) — mais conciso, menos portável; descartado para manter código comum a qualquer runtime JAX-RS do Quarkus. Reavaliar se surgir vantagem.

### Decisão 2: `BankingException` abstrata com `toProblemDetail()`
Mirror do `IBankException` original: classe base abstrata que herda `RuntimeException`, com método `toProblemDetail()` retornando um `ProblemDetailDto` (record próprio, ver Decisão 3). Subclasses sobrescrevem para definir status/título/detalhe. O mapper apenas delega — lógica do erro fica co-locada com a exceção.

Justificativa: mantém o design que o autor já usa (cohesion: cada exceção conhece seu ProblemDetail), apenas trocando o tipo de retorno do Spring por um DTO próprio serializável via Jackson. Alternativa: campos (status, title, detail) na base lidos pelo mapper — mais genérico mas perde o polimorfismo simples; descartado para preservar padrão familiar.

### Decisão 3: Record próprio `ProblemDetailDto` (sem `ProblemDetail` do Spring/JAX-RS)
O `ProblemDetail` usado no Spring (`org.springframework.http.ProblemDetail`) **não** existe no JAX-RS/Jakarta RESTful — não há equivalente padrão. Logo, criamos um record próprio `ProblemDetailDto`:
- Campos: `uri` (opcional, default `about:blank`), `status` (int), `title` (String), `detail` (String), `type` (default `about:blank`), e `Map<String, Object> properties` para extensões RFC 7807 como `invalid-params`. Serializado direto pelo Jackson (já no projeto via `quarkus-rest-jackson`).
- `BankingException.toProblemDetail()` retorna `ProblemDetailDto`; os mappers empacotam em `Response` com content-type `application/problem+json`.

`InvalidParamDto(String name, String message)` (renomeei `defaultMessage`→`message` para clareza; no JSON o campo do parâmetro vem como `name` e a descrição como `message`), adicionado à propriedade `invalid-params` do ProblemDetailDto.

### Decisão 4: Status 422 para validação (decisão do usuário)
Confirmado pelo usuário: usar 422 Unprocessable Entity (não 420 custom do Spring original, não 400). A escolha reflete semântica REST para payload bem-formado semanticamente inválido.

### Decisão 5: Content-Type `application/problem+json`
Mappers retornam `Response.status(...).type("application/problem+json").entity(problemDetail).build()`. Garante que o client identifique como Problem Details mesmo que o Jackson default seria `application/json`.

### Decisão 6: Refatorar `AgenciaNaoAtivaOuNaoEncontradaException`
Tornar subclasse de `BankingException`, guardar `detail`, sobrescrever `toProblemDetail()` com `status=404`, `title="Agência não ativa ou não encontrada"`, `detail=<detail>`. Manter construtor `(String)`. Callers que já a lançam não precisam mudar — retrofit compatível.