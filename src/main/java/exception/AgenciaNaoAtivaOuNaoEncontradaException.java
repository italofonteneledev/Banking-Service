package exception;

public class AgenciaNaoAtivaOuNaoEncontradaException extends RuntimeException {
    public AgenciaNaoAtivaOuNaoEncontradaException(String message) {
        super(message);
    }
}
