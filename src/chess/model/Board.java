package chess.model;

import chess.exceptions.ChessRuntimeException;
import chess.exceptions.OutOfBoardException;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import chess.pieces.Queen;
import chess.pieces.Rook;
import java.io.Serializable;

public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Square[][] grid;

    public Board() {
        this.grid = new Square[8][8];
        initializeEmptySquares();
    }

    private void initializeEmptySquares() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                try {
                    Position position = new Position(row, col);
                    grid[row][col] = new Square(position);
                } catch (OutOfBoardException exception) {
                    throw new ChessRuntimeException("Failed to initialize board coordinates at (" + row + ", " + col + ")", exception);
                }
            }
        }
    }

    public Square getSquare(Position position) throws OutOfBoardException {
        if (position == null) {
            throw new NullPointerException("Position reference cannot be null when accessing square.");
        }
        return getSquare(position.getRow(), position.getCol());
    }

    public Square getSquare(int row, int col) throws OutOfBoardException {
        try {
            return grid[row][col];
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new OutOfBoardException(row, col);
        }
    }

    public Piece getPiece(Position position) throws OutOfBoardException {
        return getSquare(position).getPiece();
    }

    public void setPiece(Position position, Piece piece) throws OutOfBoardException {
        getSquare(position).setPiece(piece);
    }

    public void setupInitialPosition() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                grid[row][col].clear();
            }
        }

        try {
            grid[0][0].setPiece(new Rook(Color.BLACK));
            grid[0][1].setPiece(new Knight(Color.BLACK));
            grid[0][2].setPiece(new Bishop(Color.BLACK));
            grid[0][3].setPiece(new Queen(Color.BLACK));
            grid[0][4].setPiece(new King(Color.BLACK));
            grid[0][5].setPiece(new Bishop(Color.BLACK));
            grid[0][6].setPiece(new Knight(Color.BLACK));
            grid[0][7].setPiece(new Rook(Color.BLACK));

            for (int col = 0; col < 8; col++) {
                grid[1][col].setPiece(new Pawn(Color.BLACK));
            }

            for (int col = 0; col < 8; col++) {
                grid[6][col].setPiece(new Pawn(Color.WHITE));
            }

            grid[7][0].setPiece(new Rook(Color.WHITE));
            grid[7][1].setPiece(new Knight(Color.WHITE));
            grid[7][2].setPiece(new Bishop(Color.WHITE));
            grid[7][3].setPiece(new Queen(Color.WHITE));
            grid[7][4].setPiece(new King(Color.WHITE));
            grid[7][5].setPiece(new Bishop(Color.WHITE));
            grid[7][6].setPiece(new Knight(Color.WHITE));
            grid[7][7].setPiece(new Rook(Color.WHITE));
        } catch (Exception exception) {
            throw new ChessRuntimeException("Unexpected failure during initial board arrangement", exception);
        }
    }

    public Position findKing(Color color) throws ChessRuntimeException {
        if (color == null) {
            throw new NullPointerException("Color cannot be null when finding king position.");
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col].getPiece();
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    return grid[row][col].getPosition();
                }
            }
        }

        throw new ChessRuntimeException("Board integrity failure: " + color.getDisplayName() + " king is missing from the board.");
    }

    public Board copy() {
        Board replica = new Board();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col].getPiece();
                if (piece != null) {
                    try {
                        replica.getSquare(row, col).setPiece(piece.copy());
                    } catch (OutOfBoardException exception) {
                        throw new ChessRuntimeException("Failed to replicate piece at (" + row + ", " + col + ")", exception);
                    }
                }
            }
        }
        return replica;
    }
}

