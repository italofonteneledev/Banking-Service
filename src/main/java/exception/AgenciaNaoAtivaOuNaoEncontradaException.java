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