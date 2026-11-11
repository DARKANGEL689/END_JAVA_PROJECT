package chess.exceptions;

public class WrongTurnException extends ChessException {
    private static final long serialVersionUID = 1L;

    public WrongTurnException(String message) {
        super("ERR_WRONG_TURN", message);
    }
}

