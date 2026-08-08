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