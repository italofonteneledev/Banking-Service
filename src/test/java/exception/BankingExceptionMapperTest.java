package exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BankingExceptionMapperTest {

    private final BankingExceptionMapper mapper = new BankingExceptionMapper();

    @Test
    void genericBankingExceptionMapsToFallback500ProblemJson() {
        BankingException ex = new BankingException("boom") {
        };

        Response response = mapper.toResponse(ex);

        assertEquals(500, response.getStatus());
        assertEquals(BankingExceptionMapper.PROBLEM_CONTENT_TYPE, response.getMediaType().toString());

        ProblemDetail body = assertInstanceOf(ProblemDetail.class, response.getEntity());
        assertAll(
                () -> assertEquals(500, body.status()),
                () -> assertEquals("Banking internal server error", body.title()),
                () -> assertEquals("Contate o suporte do banking", body.detail()),
                () -> assertEquals("about:blank", body.type())
        );
    }

    @Test
    void bankingSubclassOverridingProblemDetailMapsToItsOwnStatus() {
        BankingException ex = new AgenciaNaoAtivaOuNaoEncontradaException("Agencia não encontrada");

        Response response = mapper.toResponse(ex);

        assertEquals(404, response.getStatus());
        assertEquals(BankingExceptionMapper.PROBLEM_CONTENT_TYPE, response.getMediaType().toString());

        ProblemDetail body = assertInstanceOf(ProblemDetail.class, response.getEntity());
        assertAll(
                () -> assertEquals(404, body.status()),
                () -> assertEquals("Agência não ativa ou não encontrada", body.title()),
                () -> assertEquals("Agencia não encontrada", body.detail())
        );
    }
}