package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class Knight extends Piece {
    private static final long serialVersionUID = 1L;

    public Knight(Color color) {
        super(color, PieceType.KNIGHT);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int rowDifference = Math.abs(end.getRow() - start.getRow());
        int colDifference = Math.abs(end.getCol() - start.getCol());

        return (rowDifference == 1 && colDifference == 2) || (rowDifference == 2 && colDifference == 1);
    }

    @Override
    public Piece copy() {
        Knight clone = new Knight(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

