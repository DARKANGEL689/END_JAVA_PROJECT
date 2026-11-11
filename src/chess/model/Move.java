package chess.model;

import chess.pieces.Piece;
import chess.pieces.PieceType;
import java.io.Serializable;
import java.util.Objects;

public class Move implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position start;
    private final Position end;
    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final Piece promotionPiece;
    private final boolean isCastling;
    private final boolean isEnPassant;
    private final Position enPassantCapturePosition;

    public Move(Position start, Position end, Piece movedPiece, Piece capturedPiece) {
        this(start, end, movedPiece, capturedPiece, null, false, false, null);
    }

    public Move(Position start, Position end, Piece movedPiece, Piece capturedPiece, Piece promotionPiece,
                boolean isCastling, boolean isEnPassant, Position enPassantCapturePosition) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end positions must not be null.");
        }
        if (movedPiece == null) {
            throw new IllegalArgumentException("Moved piece must not be null.");
        }
        this.start = start;
        this.end = end;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.promotionPiece = promotionPiece;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.enPassantCapturePosition = enPassantCapturePosition;
    }

    public Position getStart() {
        return start;
    }

    public Position getEnd() {
        return end;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getPromotionPiece() {
        return promotionPiece;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public boolean isEnPassant() {
        return isEnPassant;
    }

    public Position getEnPassantCapturePosition() {
        return enPassantCapturePosition;
    }

    public String toAlgebraicNotation() {
        if (isCastling) {
            if (end.getCol() > start.getCol()) {
                return "O-O";
            } else {
                return "O-O-O";
            }
        }

        StringBuilder builder = new StringBuilder();
        if (movedPiece.getType() != PieceType.PAWN) {
            builder.append(movedPiece.getType().getNotationSymbol());
        } else if (capturedPiece != null || isEnPassant) {
            builder.append((char) ('a' + start.getCol()));
        }

        if (capturedPiece != null || isEnPassant) {
            builder.append("x");
        }

        builder.append(end.toAlgebraic());

        if (promotionPiece != null) {
            builder.append("=").append(promotionPiece.getType().getNotationSymbol());
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Move other = (Move) obj;
        return isCastling == other.isCastling &&
               isEnPassant == other.isEnPassant &&
               Objects.equals(start, other.start) &&
               Objects.equals(end, other.end) &&
               Objects.equals(movedPiece, other.movedPiece) &&
               Objects.equals(capturedPiece, other.capturedPiece) &&
               Objects.equals(promotionPiece, other.promotionPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, movedPiece, capturedPiece, promotionPiece, isCastling, isEnPassant);
    }

    @Override
    public String toString() {
        return toAlgebraicNotation();
    }
}

