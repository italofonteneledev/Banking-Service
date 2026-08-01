## 1. Estrutura base de Problem Details

- [x] 1.1 Criar `exception/dto/InvalidParamDto.java` — record `InvalidParamDto(String name, String message)`
- [x] 1.2 Criar `exception/ProblemDetail.java` — record com `type` (default `about:blank`), `status` (int), `title`, `detail`, `properties` (`Map<String,Object>`). Incluir construtores / factory que simplifiquem o uso (`ProblemDetail.forStatus(int)`).

## 2. Exceção base de domínio

- [x] 2.1 Criar `exception/BankingException.java` — abstrata, herda `RuntimeException`, com construtores `(String)` e `(Throwable)`, e método `toProblemDetail()` retornando `ProblemDetail` com `status=500`, `title="Banking internal server error"`, `detail="Contate o suporte do banking"`.
- [x] 2.2 Validar compilação do pacote `exception` até aqui.

## 3. Mappers JAX-RS

- [x] 3.1 Criar `exception/BankingExceptionMapper.java` — `@Provider` `implements ExceptionMapper<BankingException>`; `toResponse(e)` retorna `Response.status(e.toProblemDetail().status()).type("application/problem+json").entity(e.toProblemDetail()).build()`.
- [x] 3.2 Criar `exception/ConstraintViolationExceptionMapper.java` — `@Provider` `implements ExceptionMapper<ConstraintViolationException>`; coleta `constraintViolations`, mapeia para `List<InvalidParamDto(name, message)` (name via `propertyPath` final node; message via `getMessage()`), monta `ProblemDetail` `status=422`, `title="Invalid request parameters"`, `detail="There are invalid fields in the request"`, `properties.put("invalid-params", list)`.
- [x] 3.3 Confirmar descoberta CDI dos mappers no Quarkus (classes `@Provider` no classpath são registradas).

## 4. Refatoração da exceção existente

- [x] 4.1 Refatorar `exception/AgenciaNaoAtivaOuNaoEncontradaException.java` para herdar de `BankingException`, guardar `detail` (campo final), sobrescrever `toProblemDetail()` retornando `status=404`, `title="Agência não ativa ou não encontrada"`, `detail=<detail>`. Manter construtor `(String)`.
- [x] 4.2 Verificar que lançamentos existentes (ex.: em `AgenciaService`) continuam compilando sem alteração.

## 5. Testes

- [x] 5.1 Teste do `BankingExceptionMapper`: lançar `BankingException` genérica → espera HTTP 500 `application/problem+json` com `title`/`detail` do fallback; lançar uma subclasse que sobrescreve `toProblemDetail()` com 404 → espera 404.
- [x] 5.2 Teste do `ConstraintViolationExceptionMapper`: construir `ConstraintViolationException` com violações → espera 422, `title="Invalid request parameters"`, `invalid-params` populado corretamente.
- [x] 5.3 Teste de integração do `AgenciaController`/`AgenciaService`: ao não encontrar/ativar agência, resposta é HTTP 404 `application/problem+json` com `title="Agência não ativa ou não encontrada"` e `detail` igual ao informado. _Descartado pelo usuário (não realizar testes de integração neste change). A cobertura do caminho 404 via `AgenciaNaoAtivaOuNaoEncontradaException` é exercida no teste unitário 5.1._

## 6. Verificação

- [x] 6.1 Rodar `./mvnw clean test` e garantir verde.
- [x] 6.2 Validar o change com `openspec validate --change "add-global-exception-handling" --strict`.
- [x] 6.3 Atualizar `external-docs/global-erros-handling.md` com a versão Quarkus (substituir imports Spring, `@RestControllerAdvice` → `ExceptionMapper`, anotar 422).