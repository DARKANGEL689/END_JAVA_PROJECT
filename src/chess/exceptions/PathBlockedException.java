package chess.exceptions;

public class PathBlockedException extends ChessException {
    private static final long serialVersionUID = 1L;

    public PathBlockedException(String message) {
        super("ERR_PATH_BLOCKED", message);
    }
}

