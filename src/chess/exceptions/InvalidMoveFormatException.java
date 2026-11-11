package chess.exceptions;

public class InvalidMoveFormatException extends ChessException {
    private static final long serialVersionUID = 1L;

    public InvalidMoveFormatException(String message) {
        super("ERR_INVALID_MOVE_FORMAT", message);
    }

    public InvalidMoveFormatException(String message, Throwable cause) {
        super("ERR_INVALID_MOVE_FORMAT", message, cause);
    }
}

