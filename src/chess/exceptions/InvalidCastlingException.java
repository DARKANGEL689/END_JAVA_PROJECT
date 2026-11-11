package chess.exceptions;

public class InvalidCastlingException extends ChessException {
    private static final long serialVersionUID = 1L;

    public InvalidCastlingException(String message) {
        super("ERR_INVALID_CASTLING", message);
    }
}

