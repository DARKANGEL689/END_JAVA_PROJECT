package chess.io;

import chess.exceptions.CorruptedSaveFileException;
import chess.model.GameState;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GameSaver {

    public static void saveGame(GameState gameState, File targetFile) throws CorruptedSaveFileException, IOException {
        if (gameState == null) {
            throw new IllegalArgumentException("Cannot save null game state.");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("Target save file destination cannot be null.");
        }

        try {
            gameState.validateIntegrity();
        } catch (CorruptedSaveFileException validationException) {
            throw new CorruptedSaveFileException("Refusing to save corrupted game state: " + validationException.getMessage(), validationException);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            try {
                objectOutputStream.writeObject(gameState);
                objectOutputStream.flush();
            } catch (IOException serializationException) {
                throw new IOException("Disk write error during game state serialization.", serializationException);
            }

        } catch (IOException streamException) {
            throw new IOException("Failed to establish output stream for file: " + targetFile.getAbsolutePath(), streamException);
        }
    }
}

