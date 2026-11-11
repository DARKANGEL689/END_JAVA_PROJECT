package chess.model;

import chess.exceptions.InvalidMoveFormatException;
import chess.exceptions.OutOfBoardException;
import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int col;

    public Position(int row, int col) throws OutOfBoardException {
        if (row < 0 || row > 7 || col < 0 || col > 7) {
            throw new OutOfBoardException(row, col);
        }
        this.row = row;
        this.col = col;
    }

    public static Position of(int row, int col) throws OutOfBoardException {
        return new Position(row, col);
    }

    public static Position fromAlgebraic(String notation) throws OutOfBoardException, InvalidMoveFormatException {
        if (notation == null || notation.trim().length() != 2) {
            throw new InvalidMoveFormatException("Coordinate must be a 2-character algebraic square (e.g., 'e4'), received: " + notation);
        }

        String cleaned = notation.trim().toLowerCase();
        char fileChar = cleaned.charAt(0);
        char rankChar = cleaned.charAt(1);

        if (fileChar < 'a' || fileChar > 'h') {
            throw new OutOfBoardException("File '" + fileChar + "' is outside chessboard columns (a-h).");
        }

        int rankNumber;
        try {
            rankNumber = Integer.parseInt(String.valueOf(rankChar));
        } catch (NumberFormatException exception) {
            throw new InvalidMoveFormatException("Rank '" + rankChar + "' could not be parsed as a valid integer.", exception);
        }

        if (rankNumber < 1 || rankNumber > 8) {
            throw new OutOfBoardException("Rank " + rankNumber + " is outside chessboard rows (1-8).");
        }

        int targetCol = fileChar - 'a';
        int targetRow = 8 - rankNumber;

        return new Position(targetRow, targetCol);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String toAlgebraic() {
        char file = (char) ('a' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Position other = (Position) obj;
        return row == other.row && col == other.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}

