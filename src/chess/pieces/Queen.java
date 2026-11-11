package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class Queen extends Piece {
    private static final long serialVersionUID = 1L;

    public Queen(Color color) {
        super(color, PieceType.QUEEN);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int rowDifference = Math.abs(end.getRow() - start.getRow());
        int colDifference = Math.abs(end.getCol() - start.getCol());

        boolean isStraight = (rowDifference == 0 || colDifference == 0);
        boolean isDiagonal = (rowDifference == colDifference);

        return isStraight || isDiagonal;
    }

    @Override
    public Piece copy() {
        Queen clone = new Queen(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

