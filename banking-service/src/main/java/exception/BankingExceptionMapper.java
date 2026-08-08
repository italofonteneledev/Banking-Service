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