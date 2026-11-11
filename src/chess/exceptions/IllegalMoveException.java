package chess.exceptions;

public class IllegalMoveException extends ChessException {
    private static final long serialVersionUID = 1L;

    public IllegalMoveException(String message) {
        super("ERR_ILLEGAL_MOVE", message);
    }

    public IllegalMoveException(String errorCode, String message) {
        super(errorCode, message);
    }

    public IllegalMoveException(String message, Throwable cause) {
        super("ERR_ILLEGAL_MOVE", message, cause);
    }
}

