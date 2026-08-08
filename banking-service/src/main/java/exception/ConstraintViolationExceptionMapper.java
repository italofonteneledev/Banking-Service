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
                422,
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