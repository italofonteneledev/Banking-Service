# Global Exception (Quarkus / JAX-RS)

Tratamento centralizado de erros no padrão idiomático do Quarkus (RESTEasy Reactive + JAX-RS), emitindo respostas no formato RFC 7807 Problem Details (`application/problem+json`). No Quarkus **não existe** `@RestControllerAdvice` (Spring); o equivalente são `ExceptionMapper<E>` do JAX-RS, descobertos via CDI (`@Provider`).

## `ProblemDetail` (record próprio — não há `ProblemDetail` no JAX-RS)

```java
package exception;

import java.util.Map;

public record ProblemDetail(
        String type,
        int status,
        String title,
        String detail,
        Map<String, Object> properties
) {

    private static final String DEFAULT_TYPE = "about:blank";

    public ProblemDetail(int status, String title, String detail) {
        this(DEFAULT_TYPE, status, title, detail, null);
    }

    public ProblemDetail(int status, String title, String detail, Map<String, Object> properties) {
        this(DEFAULT_TYPE, status, title, detail, properties);
    }

    public static ProblemDetail forStatus(int status) {
        return new ProblemDetail(status, null, null, null);
    }
}
```

## `InvalidParamDto`

```java
package exception.dto;

public record InvalidParamDto(String name, String message) {
}
```

## `BankingException` (exceção global de domínio)

```java
package exception;

public abstract class BankingException extends RuntimeException {

    public BankingException(String message) {
        super(message);
    }

    public BankingException(Throwable cause) {
        super(cause);
    }

    public ProblemDetail toProblemDetail() {
        return new ProblemDetail(
                500,
                "Banking internal server error",
                "Contate o suporte do banking"
        );
    }
}
```

Cada exceção de domínio é uma subclasse que sobrescreve `toProblemDetail()` definindo seu próprio `status`, `title` e `detail`.

## Mappers JAX-RS (`@Provider`)

No Quarkus, classes anotadas com `@Provider` no classpath são automaticamente registradas pelo indexer do RESTEasy Reactive — não é necessário `beans.xml`.

### `BankingExceptionMapper` — domínio

```java
package exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BankingExceptionMapper implements ExceptionMapper<BankingException> {

    public static final String PROBLEM_CONTENT_TYPE = "application/problem+json";

    @Override
    public Response toResponse(BankingException e) {
        return Response.status(e.toProblemDetail().status())
                .type(PROBLEM_CONTENT_TYPE)
                .entity(e.toProblemDetail())
                .build();
    }
}
```

### `ConstraintViolationExceptionMapper` — validação (Bean Validation) → 422

```java
package exception;

import exception.dto.InvalidParamDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    public static final String PROBLEM_CONTENT_TYPE = "application/problem+json";

    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<InvalidParamDto> invalidParams = e.getConstraintViolations().stream()
                .map(this::toInvalidParam)
                .collect(Collectors.toList());

        ProblemDetail problemDetail = new ProblemDetail(
                422,                                  // 422 Unprocessable Entity (semântica REST para payload bem-formado, porém semanticamente inválido)
                "Invalid request parameters",
                "There are invalid fields in the request",
                Map.of("invalid-params", invalidParams)
        );

        return Response.status(422)
                .type(PROBLEM_CONTENT_TYPE)
                .entity(problemDetail)
                .build();
    }

    private InvalidParamDto toInvalidParam(ConstraintViolation<?> violation) {
        String name = null;
        for (Path.Node node : violation.getPropertyPath()) {
            name = node.getName();
        }
        return new InvalidParamDto(name, violation.getMessage());
    }
}
```

> **Nota sobre o status:** usa-se **422 Unprocessable Entity** (não 420 custom do Spring original, nem 400), refletindo semântica REST para payload bem-formado porém semanticamente inválido.

## Exemplo de exceção de domínio: `AgenciaNaoAtivaOuNaoEncontradaException`

```java
package exception;

public class AgenciaNaoAtivaOuNaoEncontradaException extends BankingException {

    private final String detail;

    public AgenciaNaoAtivaOuNaoEncontradaException(String detail) {
        super(detail);
        this.detail = detail;
    }

    @Override
    public ProblemDetail toProblemDetail() {
        return new ProblemDetail(
                404,
                "Agência não ativa ou não encontrada",
                detail
        );
    }
}
```

Após implementar esses mappers, erros de domínio são criados individualmente seguindo a nomenclatura por domínio: p.ex. ao não achar uma carteira, `WalletNotFoundException` (404); ao não achar um usuário, `UserNotFoundException` (404); etc. Sempre nomear com algo que faça sentido ao erro, herdando de `BankingException` e sobrescrevendo `toProblemDetail()`.