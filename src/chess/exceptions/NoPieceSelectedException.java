package chess.exceptions;

public class NoPieceSelectedException extends ChessException {
    private static final long serialVersionUID = 1L;

    public NoPieceSelectedException(String message) {
        super("ERR_NO_PIECE_SELECTED", message);
    }
}

