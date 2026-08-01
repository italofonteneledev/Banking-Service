## Why

A aplicação não tem um tratamento de erros centralizado. A exceção existente (`AgenciaNaoAtivaOuNaoEncontradaException`) herda diretamente de `RuntimeException` sem mapear a resposta HTTP, então erros de domínio chegam ao cliente como um 500 genérico sem um corpo estruturado. Precisamos de um formato de erro consistente (RFC 7807 Problem Details) e de um ponto único que traduza exceções de negócio e de validação em respostas padronizadas, idiomático do Quarkus (RESTEasy Reactive).

## What Changes

- Introduzir `BankingException`, exceção base abstrata de domínio que carrega um `ProblemDetail` (RFC 7807) com status, título e detalhe configuráveis por subclasse.
- Adicionar um `ExceptionMapper` global (JAX-RS `ExceptionMapper`) que captura `BankingException` e retorna seu `ProblemDetail` serializado em JSON.
- Adicionar um mapper para erros de validação (`ConstraintViolationException` / bean validation) que retorna `422 Unprocessable Entity` com a lista de parâmetros inválidos (`invalid-params`), no formato Problem Details.
- Introduzir o record `InvalidParamDto` para representar `{ field, message }`.
- **BREAKING**: Refatorar `AgenciaNaoAtivaOuNaoEncontradaException` para herdar de `BankingException` e implementar `toProblemDetail()` com status 404 e título "Agência não ativa ou não encontrada". Antes tratada como 500 genérico, agora vira 404 estruturado.

## Capabilities

### New Capabilities
- `error-handling`: Tratamento centralizado de erros da API — mapeia exceções de domínio (`BankingException`) e de validação para respostas HTTP no formato RFC 7807 Problem Details, com status, título, detalhe e parâmetros inválidos padronizados.

### Modified Capabilities
<!-- Nenhuma capability existente — não há specs em openspec/specs/. -->

## Impact

- **Código**: novo pacote `exception` já existente; adicionar `BankingException`, `GlobalExceptionMapper` (ou mappers individuais), `InvalidParamDto`. Refatorar `AgenciaNaoAtivaOuNaoEncontradaException`.
- **APIs**: respostas de erro agora seguem RFC 7807 (`application/problem+json`). Controllers que lançam exceções de negócio passam a responder com status estruturado em vez de 500.
- **Dependências**: nenhuma nova — JAX-RS / RESTEasy Reactive e Jackson já estão no projeto via `quarkus-rest-jackson`.
- **Testes**: adicionar testes cobrindo o mapper de `BankingException`, o mapper de validação (422) e a refatoração da exceção existente (404).