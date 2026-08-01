## 1. Estrutura base de Problem Details

- [ ] 1.1 Criar `exception/dto/InvalidParamDto.java` — record `InvalidParamDto(String name, String message)`
- [ ] 1.2 Criar `exception/dto/ProblemDetailDto.java` — record com `type` (default `about:blank`), `status` (int), `title`, `detail`, `properties` (`Map<String,Object>`). Incluir construtores / factory que simplifiquem o uso (`ProblemDetailDto.forStatus(int)`).

## 2. Exceção base de domínio

- [ ] 2.1 Criar `exception/BankingException.java` — abstrata, herda `RuntimeException`, com construtores `(String)` e `(Throwable)`, e método `toProblemDetail()` retornando `ProblemDetailDto` com `status=500`, `title="Banking internal server error"`, `detail="Contate o suporte do banking"`.
- [ ] 2.2 Validar compilação do pacote `exception` até aqui.

## 3. Mappers JAX-RS

- [ ] 3.1 Criar `exception/BankingExceptionMapper.java` — `@Provider` `implements ExceptionMapper<BankingException>`; `toResponse(e)` retorna `Response.status(e.toProblemDetail().status()).type("application/problem+json").entity(e.toProblemDetail()).build()`.
- [ ] 3.2 Criar `exception/ConstraintViolationExceptionMapper.java` — `@Provider` `implements ExceptionMapper<ConstraintViolationException>`; coleta `constraintViolations`, mapeia para `List<InvalidParamDto(name, message)` (name via `propertyPath` final node; message via `getMessage()`), monta `ProblemDetailDto` `status=422`, `title="Invalid request parameters"`, `detail="There are invalid fields in the request"`, `properties.put("invalid-params", list)`.
- [ ] 3.3 Confirmar descoberta CDI dos mappers no Quarkus (classes `@Provider` no classpath são registradas).

## 4. Refatoração da exceção existente

- [ ] 4.1 Refatorar `exception/AgenciaNaoAtivaOuNaoEncontradaException.java` para herdar de `BankingException`, guardar `detail` (campo final), sobrescrever `toProblemDetail()` retornando `status=404`, `title="Agência não ativa ou não encontrada"`, `detail=<detail>`. Manter construtor `(String)`.
- [ ] 4.2 Verificar que lançamentos existentes (ex.: em `AgenciaService`) continuam compilando sem alteração.

## 5. Testes

- [ ] 5.1 Teste do `BankingExceptionMapper`: lançar `BankingException` genérica → espera HTTP 500 `application/problem+json` com `title`/`detail` do fallback; lançar uma subclasse que sobrescreve `toProblemDetail()` com 404 → espera 404.
- [ ] 5.2 Teste do `ConstraintViolationExceptionMapper`: construir `ConstraintViolationException` com violações → espera 422, `title="Invalid request parameters"`, `invalid-params` populado corretamente.
- [ ] 5.3 Teste de integração do `AgenciaController`/`AgenciaService`: ao não encontrar/ativar agência, resposta é HTTP 404 `application/problem+json` com `title="Agência não ativa ou não encontrada"` e `detail` igual ao informado.

## 6. Verificação

- [ ] 6.1 Rodar `./mvnw clean test` e garantir verde.
- [ ] 6.2 Validar o change com `openspec validate --change "add-global-exception-handling" --strict`.
- [ ] 6.3 Atualizar `external-docs/global-erros-handling.md` com a versão Quarkus (substituir imports Spring, `@RestControllerAdvice` → `ExceptionMapper`, anotar 422).