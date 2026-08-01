# Global Exception

## Substituir IBankException para o que faz sentido ao projeto, pergunte ao usuario qual devera ser o nome da excecao global

package io.github.ital023.IBank.exception;

import io.github.ital023.IBank.exception.dto.InvalidParamDto;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IBankException.class)
    public ProblemDetail handleIBankException(IBankException e) {
        return e.toProblemDetail();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        var invalidParams = e.getFieldErrors()
                .stream()
                .map(fe -> new InvalidParamDto(fe.getField(), fe.getDefaultMessage()))
                .toList();

        var pd = ProblemDetail.forStatus(420);

        pd.setTitle("Invalid request parameters");
        pd.setDetail("There's invalid fields on the request");
        pd.setProperty("invalid-params", invalidParams);

        return pd;
    }


}

# Invalid Param Dto

package io.github.ital023.IBank.exception.dto;

public record InvalidParamDto(String field, String defaultMessage) {
}

# IBank Exception (Excecao que deve ter o nome substituido pelo o que o usuario pedir) 

package io.github.ital023.IBank.exception;

import org.springframework.http.ProblemDetail;

public abstract class IBankException extends RuntimeException {
    public IBankException(String message) {
        super(message);
    }

    public IBankException(Throwable cause) {
        super(cause);
    }

    public ProblemDetail toProblemDetail() {
        var pd = ProblemDetail.forStatus(500);

        pd.setTitle("Ibank Internal Server Error");
        pd.setDetail("Contact Ibank support");

        return pd;
    }
}

# Depois da implementação desses 3, os erros devem ser criados individualmentes, por exemplo se tivermos uma aplicação que busca uma carteira e essa carteira não é achada no banco de dados deve retornar uma WalletNotFoundException, sempre levar essa nomenclatura com algo que faça sentido ao erro, como se não achar um usuario, UserNotFoundException e etc...

package io.github.ital023.IBank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class WalletNotFoundException extends IBankException {

    private final String detail;

    public WalletNotFoundException(String detail) {
        super(detail);
        this.detail = detail;
    }

    @Override
    public ProblemDetail toProblemDetail() {

        var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        pd.setTitle("Wallet not found");
        pd.setDetail(detail);

        return pd;
    }
}