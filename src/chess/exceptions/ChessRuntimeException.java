package chess.exceptions;

public class ChessRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ChessRuntimeException(String message) {
        super(message);
    }

    public ChessRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

