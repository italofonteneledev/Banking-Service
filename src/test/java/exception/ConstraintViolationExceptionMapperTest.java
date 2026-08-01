package exception;

import exception.dto.InvalidParamDto;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ConstraintViolationExceptionMapperTest {

    record SamplePayload(
            @NotBlank String nome,
            @NotNull Integer numero
    ) {
    }

    private final ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();

    @Test
    void violationsMapTo422WithInvalidParams() {
        Validator validator;
        try (ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        Set<jakarta.validation.ConstraintViolation<?>> violations =
                (Set) validator.validate(new SamplePayload("", null));
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        Response response = mapper.toResponse(exception);

        assertAll(
                () -> assertEquals(422, response.getStatus()),
                () -> assertEquals(ConstraintViolationExceptionMapper.PROBLEM_CONTENT_TYPE,
                        response.getMediaType().toString())
        );

        ProblemDetail body = assertInstanceOf(ProblemDetail.class, response.getEntity());
        assertAll(
                () -> assertEquals(422, body.status()),
                () -> assertEquals("Invalid request parameters", body.title()),
                () -> assertEquals("There are invalid fields in the request", body.detail()),
                () -> assertEquals("about:blank", body.type())
        );

        Object invalidParams = body.properties().get("invalid-params");
        List<?> list = assertInstanceOf(List.class, invalidParams);
        assertEquals(2, list.size());
        assertInstanceOf(InvalidParamDto.class, list.get(0));
        Set<String> names = list.stream()
                .map(item -> ((InvalidParamDto) item).name())
                .collect(java.util.stream.Collectors.toSet());
        Set<String> messages = list.stream()
                .map(item -> ((InvalidParamDto) item).message())
                .collect(java.util.stream.Collectors.toSet());
        org.junit.jupiter.api.Assertions.assertTrue(names.contains("nome"));
        org.junit.jupiter.api.Assertions.assertTrue(names.contains("numero"));
        org.junit.jupiter.api.Assertions.assertTrue(messages.stream().anyMatch(m -> m != null && !m.isBlank()));
    }
}