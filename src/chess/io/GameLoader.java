package chess.io;

import chess.exceptions.CorruptedSaveFileException;
import chess.model.GameState;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GameLoader {

    public static GameState loadGame(File sourceFile) throws CorruptedSaveFileException, FileNotFoundException {
        if (sourceFile == null) {
            throw new IllegalArgumentException("Source save file reference cannot be null.");
        }

        if (!sourceFile.exists()) {
            throw new FileNotFoundException("Chess save file not found at path: " + sourceFile.getAbsolutePath());
        }

        GameState loadedState = null;

        try (FileInputStream fileInputStream = new FileInputStream(sourceFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Object deserializedObject;
            try {
                deserializedObject = objectInputStream.readObject();
            } catch (ClassNotFoundException classNotFoundException) {
                throw new CorruptedSaveFileException("Save file contains an unknown class structure or incompatible version.", classNotFoundException);
            }

            if (!(deserializedObject instanceof GameState)) {
                throw new CorruptedSaveFileException("Save file does not contain a valid chess game state instance.");
            }

            loadedState = (GameState) deserializedObject;

            try {
                loadedState.validateIntegrity();
            } catch (CorruptedSaveFileException integrityException) {
                throw new CorruptedSaveFileException("Integrity check failed on loaded save file: " + integrityException.getMessage(), integrityException);
            }

        } catch (FileNotFoundException fileNotFoundException) {
            throw fileNotFoundException;
        } catch (IOException ioException) {
            throw new CorruptedSaveFileException("I/O failure while reading save file content.", ioException);
        }

        return loadedState;
    }
}

