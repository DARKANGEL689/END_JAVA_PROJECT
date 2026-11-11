package chess.io;

import chess.exceptions.ChessException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameLogger implements AutoCloseable {
    private static final String LOG_FILE_PATH = "game_errors.log";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final PrintWriter writer;

    public GameLogger() throws IOException {
        File file = new File(LOG_FILE_PATH);
        this.writer = new PrintWriter(new FileWriter(file, true), true);
    }

    public void log(Throwable throwable) {
        log("APPLICATION_ERROR", throwable);
    }

    public void log(String context, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        String threadName = Thread.currentThread().getName();
        String exceptionClass = throwable.getClass().getName();
        String message = throwable.getMessage() != null ? throwable.getMessage() : "No detail message provided";

        writer.println("================================================================================");
        writer.println("TIMESTAMP: " + timestamp);
        writer.println("THREAD:    " + threadName);
        writer.println("CONTEXT:   " + context);
        writer.println("EXCEPTION: " + exceptionClass);

        if (throwable instanceof ChessException) {
            ChessException chessException = (ChessException) throwable;
            writer.println("ERROR CODE: " + chessException.getErrorCode());
        }

        writer.println("MESSAGE:   " + message);

        Throwable cause = throwable.getCause();
        if (cause != null) {
            writer.println("CAUSED BY: " + cause.getClass().getName() + " - " + cause.getMessage());
        }

        writer.println("STACK TRACE:");
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            writer.println("    at " + element.getClassName() + "." + element.getMethodName()
                    + "(" + element.getFileName() + ":" + element.getLineNumber() + ")");
        }
        writer.println("================================================================================");
        writer.flush();
    }

    public static void logStatic(Throwable throwable) {
        logStatic("GENERAL", throwable);
    }

    public static void logStatic(String context, Throwable throwable) {
        try (GameLogger logger = new GameLogger()) {
            logger.log(context, throwable);
        } catch (IOException ioException) {
            System.err.println("Failed to write to log file: " + ioException.getMessage());
        }
    }

    public static void logGlobalError(Thread thread, Throwable throwable) {
        try (GameLogger logger = new GameLogger()) {
            logger.log("UNCAUGHT_THREAD_EXCEPTION [" + thread.getName() + "]", throwable);
        } catch (IOException ioException) {
            System.err.println("Fatal: Unable to log uncaught thread exception: " + ioException.getMessage());
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }
}

