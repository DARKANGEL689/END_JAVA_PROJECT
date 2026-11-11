package chess.exceptions;

public class KingInCheckException extends ChessException {
    private static final long serialVersionUID = 1L;

    public KingInCheckException(String message) {
        super("ERR_KING_IN_CHECK", message);
    }
}

