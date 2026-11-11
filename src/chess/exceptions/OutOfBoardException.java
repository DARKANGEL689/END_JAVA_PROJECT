package chess.exceptions;

public class OutOfBoardException extends ChessException {
    private static final long serialVersionUID = 1L;

    public OutOfBoardException(String message) {
        super("ERR_OUT_OF_BOARD", message);
    }

    public OutOfBoardException(int row, int col) {
        super("ERR_OUT_OF_BOARD", "Coordinates (" + row + ", " + col + ") are outside the 8x8 chessboard boundaries.");
    }
}

