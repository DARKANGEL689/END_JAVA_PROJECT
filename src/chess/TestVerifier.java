package chess;

import chess.engine.GameController;
import chess.exceptions.ChessException;
import chess.exceptions.CorruptedSaveFileException;
import chess.exceptions.GameOverException;
import chess.exceptions.IllegalMoveException;
import chess.exceptions.InvalidCastlingException;
import chess.exceptions.InvalidMoveFormatException;
import chess.exceptions.InvalidPromotionException;
import chess.exceptions.KingInCheckException;
import chess.exceptions.NoPieceSelectedException;
import chess.exceptions.OutOfBoardException;
import chess.exceptions.OwnPieceCaptureException;
import chess.exceptions.PathBlockedException;
import chess.exceptions.WrongTurnException;
import chess.model.Position;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TestVerifier {
    public static void main(String[] args) {
        int passed = 0;
        int total = 15;

        GameController controller = new GameController();

        try {
            Position.fromAlgebraic("z9");
        } catch (OutOfBoardException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMove(new Position(4, 4), new Position(3, 4));
        } catch (NoPieceSelectedException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMove(new Position(1, 4), new Position(3, 4));
        } catch (WrongTurnException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMove(new Position(7, 0), new Position(6, 0));
        } catch (OwnPieceCaptureException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMove(new Position(7, 1), new Position(5, 1));
        } catch (IllegalMoveException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMove(new Position(7, 0), new Position(4, 0));
        } catch (PathBlockedException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMoveFromAlgebraic("e1 g1");
        } catch (InvalidCastlingException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.makeMoveFromAlgebraic("invalidNotation");
        } catch (InvalidMoveFormatException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.undoMove();
        } catch (IllegalStateException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            controller.loadGame(new File("non_existent_chess_save.ser"));
        } catch (FileNotFoundException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            File badFile = new File("corrupted_test.ser");
            try (FileOutputStream fos = new FileOutputStream(badFile)) {
                fos.write(new byte[]{1, 2, 3, 4, 5});
            }
            controller.loadGame(badFile);
        } catch (CorruptedSaveFileException exception) {
            passed++;
            new File("corrupted_test.ser").delete();
        } catch (Exception exception) {
        }

        try {
            controller.makeMoveFromAlgebraic("e2 e4 K");
        } catch (InvalidPromotionException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            GameController scholarController = new GameController();
            scholarController.makeMoveFromAlgebraic("e2 e4");
            scholarController.makeMoveFromAlgebraic("e7 e5");
            scholarController.makeMoveFromAlgebraic("f1 c4");
            scholarController.makeMoveFromAlgebraic("b8 c6");
            scholarController.makeMoveFromAlgebraic("d1 h5");
            scholarController.makeMoveFromAlgebraic("g8 f6");
            scholarController.makeMoveFromAlgebraic("h5 f7");
            scholarController.makeMoveFromAlgebraic("a7 a6");
        } catch (GameOverException | IllegalStateException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            GameController checkController = new GameController();
            checkController.makeMoveFromAlgebraic("e2 e4");
            checkController.makeMoveFromAlgebraic("f7 f5");
            checkController.makeMoveFromAlgebraic("d1 h5");
            checkController.makeMoveFromAlgebraic("a7 a6");
        } catch (KingInCheckException exception) {
            passed++;
        } catch (Exception exception) {
        }

        try {
            GameController saveController = new GameController();
            File saveFile = new File("test_save_game.ser");
            saveController.saveGame(saveFile);
            GameController loadController = new GameController();
            loadController.loadGame(saveFile);
            saveFile.delete();
            passed++;
        } catch (Exception exception) {
        }

        System.out.println("VERIFICATION RESULT: " + passed + " / " + total + " PASSED");
    }
}
