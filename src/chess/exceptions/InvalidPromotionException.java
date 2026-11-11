package chess.exceptions;

public class InvalidPromotionException extends ChessException {
    private static final long serialVersionUID = 1L;

    public InvalidPromotionException(String message) {
        super("ERR_INVALID_PROMOTION", message);
    }
}

