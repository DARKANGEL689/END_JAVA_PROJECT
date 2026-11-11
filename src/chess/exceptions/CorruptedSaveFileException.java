package chess.exceptions;

public class CorruptedSaveFileException extends ChessException {
    private static final long serialVersionUID = 1L;

    public CorruptedSaveFileException(String message) {
        super("ERR_CORRUPTED_SAVE", message);
    }

    public CorruptedSaveFileException(String message, Throwable cause) {
        super("ERR_CORRUPTED_SAVE", message, cause);
    }
}

