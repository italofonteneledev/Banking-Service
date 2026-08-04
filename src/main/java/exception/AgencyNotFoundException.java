package exception;

public class AgencyNotFoundException extends BankingException {

    private final String detail;

    public AgencyNotFoundException(String detail) {
        super(detail);
        this.detail = detail;
    }

    @Override
    public ProblemDetail toProblemDetail() {
        return new ProblemDetail(
                404,
                "Agency not found",
                detail
        );
    }
}
