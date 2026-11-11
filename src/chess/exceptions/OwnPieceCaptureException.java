package chess.exceptions;

public class OwnPieceCaptureException extends ChessException {
    private static final long serialVersionUID = 1L;

    public OwnPieceCaptureException(String message) {
        super("ERR_OWN_PIECE_CAPTURE", message);
    }
}

