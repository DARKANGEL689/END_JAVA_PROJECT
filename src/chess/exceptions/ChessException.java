package chess.exceptions;

public class ChessException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String errorCode;

    public ChessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ChessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

