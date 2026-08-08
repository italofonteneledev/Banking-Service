# error-handling Specification

## Purpose

Prover um tratamento de erros centralizado e padronizado para a API, traduzindo exceções de domínio e de validação em respostas HTTP no formato RFC 7807 Problem Details, com status, título, detalhe e (quando aplicável) lista de parâmetros inválidos consistentes para os clientes da API.

## Requirements

### Requirement: Erros de domínio retornam Problem Details

O sistema SHALL capturar qualquer exceção de domínio (subclasse de `BankingException`) lançada durante o processamento de uma requisição e responder com um corpo no formato RFC 7807 Problem Details (`application/problem+json`), contendo `status`, `title` e `detail`. O `status` SHALL refletir o HTTP status retornado. Cada exceção de domínio MUST determinar seu próprio `status`, `title` e `detail`.

#### Scenario: Exceção de domínio conhecida vira resposta estruturada
- **WHEN** uma requisição faz o serviço lançar uma exceção de domínio que mapeia para 404 Not Found
- **THEN** o sistema responde HTTP 404 com `Content-Type: application/problem+json` e corpo contendo `status: 404`, `title` e `detail` descritivos

#### Scenario: Exceção de domínio herdeira(sem implementação específica) usa fallback 500
- **WHEN** uma subclasse de `BankingException` não sobrescrever o mapeamento do Problem Detail
- **THEN** o sistema responde HTTP 500 com Problem Details contendo `status: 500` e um título/detalhe genérico de erro interno

### Requirement: Erros de validação retornam 422 com parâmetros inválidos

O sistema SHALL capturar falhas de validação de bean (ex.: `@Valid` / `ConstraintViolationException`) e responder HTTP 422 Unprocessable Entity com Problem Details contendo a extensão `invalid-params`, uma lista de objetos `{ name, message }` (um por campo/violation). O `title` SHALL ser "Invalid request parameters".

#### Scenario: Payload com campos inválidos retorna 422
- **WHEN** uma requisição envia um payload que viola restrições de validação
- **THEN** o sistema responde HTTP 422 com `application/problem+json`, `status: 422`, `title: "Invalid request parameters"`, e `invalid-params` listando cada campo inválido com sua mensagem

#### Scenario: Payload válido não dispara resposta de erro
- **WHEN** uma requisição envia um payload que satisfaz todas as restrições de validação
- **THEN** o sistema processa a requisição normalmente e não retorna resposta 422

### Requirement: Exceção existente de Agência mapeia para 404

O sistema SHALL mapear `AgenciaNaoAtivaOuNaoEncontradaException` para HTTP 404 Not Found com Problem Details cujo `title` é "Agência não ativa ou não encontrada" e `detail` carrega o detalhe informado na construção da exceção. Esto substitui o comportamento anterior que respondia 500 genérico.

#### Scenario: Agência não encontrada retorna 404 estructurado
- **WHEN** o serviço não encontra ou não ativa uma agência e lança `AgenciaNaoAtivaOuNaoEncontradaException` com um detalhe descritivo
- **THEN** o sistema responde HTTP 404 com `application/problem+json`, `status: 404`, `title: "Agência não ativa ou não encontrada"` e `detail` igual ao detalhe informado
