package chess.io;

import chess.exceptions.CorruptedSaveFileException;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Move;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MatchRecorder {
    private static final String AUTOSAVE_FILE_NAME = "autosave_game.ser";
    private static final String PGN_FILE_NAME = "match_history.pgn";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void autoSaveMove(GameState gameState, Move move) throws CorruptedSaveFileException, IOException {
        if (gameState == null || move == null) {
            throw new IllegalArgumentException("GameState and Move must not be null during auto-save.");
        }

        File autoSaveFile = new File(AUTOSAVE_FILE_NAME);
        GameSaver.saveGame(gameState, autoSaveFile);

        appendMoveToPgn(gameState, move);
    }

    private static void appendMoveToPgn(GameState gameState, Move move) throws IOException {
        File pgnFile = new File(PGN_FILE_NAME);
        boolean isNewMatch = !pgnFile.exists() || gameState.getMoveHistory().size() <= 1;

        try (FileWriter fileWriter = new FileWriter(pgnFile, !isNewMatch);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            if (isNewMatch) {
                printWriter.println("[Event \"Desktop Chess Auto-Recorded Match\"]");
                printWriter.println("[Site \"Local Application\"]");
                printWriter.println("[Date \"" + LocalDate.now().format(DATE_FORMATTER) + "\"]");
                printWriter.println("[Time \"" + LocalTime.now().format(TIME_FORMATTER) + "\"]");
                printWriter.println("[White \"White Player\"]");
                printWriter.println("[Black \"Black Player\"]");
                printWriter.println("[Result \"*\"]");
                printWriter.println();
            }

            int moveIndex = gameState.getMoveHistory().size();
            boolean isWhiteMove = (moveIndex % 2 != 0);
            int moveNumber = (moveIndex + 1) / 2;

            if (isWhiteMove) {
                printWriter.print(moveNumber + ". " + move.toAlgebraicNotation() + " ");
            } else {
                printWriter.println(move.toAlgebraicNotation());
            }

            printWriter.flush();
        } catch (IOException ioException) {
            throw new IOException("Failed to append move to PGN match journal: " + ioException.getMessage(), ioException);
        }
    }

    public static void recordGameConclusion(GameState gameState, String gameResult) {
        if (gameState == null) {
            return;
        }

        File pgnFile = new File(PGN_FILE_NAME);
        if (!pgnFile.exists()) {
            return;
        }

        String terminationScore = "*";
        if (gameResult != null) {
            if (gameResult.contains("White wins")) {
                terminationScore = "1-0";
            } else if (gameResult.contains("Black wins")) {
                terminationScore = "0-1";
            } else if (gameResult.contains("drawn") || gameResult.contains("Stalemate")) {
                terminationScore = "1/2-1/2";
            }
        }

        try (FileWriter fileWriter = new FileWriter(pgnFile, true);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
             PrintWriter printWriter = new PrintWriter(bufferedWriter)) {

            printWriter.println();
            printWriter.println("{ Termination: " + (gameResult != null ? gameResult : "Concluded") + " } " + terminationScore);
            printWriter.println();
            printWriter.flush();
        } catch (IOException ioException) {
            GameLogger.logStatic("PGN_CONCLUSION_WRITE_ERROR", ioException);
        }
    }

    public static boolean hasAutoSave() {
        File autoSaveFile = new File(AUTOSAVE_FILE_NAME);
        return autoSaveFile.exists() && autoSaveFile.isFile() && autoSaveFile.length() > 0;
    }

    public static GameState loadAutoSave() throws CorruptedSaveFileException, FileNotFoundException {
        File autoSaveFile = new File(AUTOSAVE_FILE_NAME);
        if (!autoSaveFile.exists()) {
            throw new FileNotFoundException("Auto-save file does not exist: " + autoSaveFile.getAbsolutePath());
        }
        return GameLoader.loadGame(autoSaveFile);
    }

    public static void clearAutoSave() {
        File autoSaveFile = new File(AUTOSAVE_FILE_NAME);
        if (autoSaveFile.exists()) {
            autoSaveFile.delete();
        }
    }

    public static String readMatchLog() throws IOException {
        File pgnFile = new File(PGN_FILE_NAME);
        if (!pgnFile.exists()) {
            throw new FileNotFoundException("No match history PGN log file exists yet.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        try (FileReader fileReader = new FileReader(pgnFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append(System.lineSeparator());
            }
        } catch (FileNotFoundException fileNotFoundException) {
            throw fileNotFoundException;
        } catch (IOException ioException) {
            throw new IOException("Unable to read match journal file: " + pgnFile.getAbsolutePath(), ioException);
        }

        return stringBuilder.toString();
    }
}
