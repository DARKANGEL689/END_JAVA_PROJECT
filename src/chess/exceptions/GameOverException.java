package chess.exceptions;

public class GameOverException extends ChessException {
    private static final long serialVersionUID = 1L;

    public GameOverException(String message) {
        super("ERR_GAME_OVER", message);
    }
}

