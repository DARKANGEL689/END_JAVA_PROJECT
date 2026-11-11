# Java Chess Application - Exception Handling Architecture

A complete, standalone desktop Chess application built in pure Java 17 using the Java Swing GUI framework and standard library (`java.*`). The application requires no external libraries, Maven plugins, or Gradle configurations, and compiles and runs directly with plain `javac` and `java`.

The core architectural focus of this project is enterprise-grade **exception handling**, featuring a 14-class custom exception hierarchy, multi-layer propagation, structured disk logging, and robust user error recovery.

---

## Folder Structure

```
END-JAVA PROJECT/
├── bin/                              # Compiled bytecode class files
├── game_errors.log                   # Persistent error log generated at runtime
├── README.md                         # Architecture and concept mapping documentation
├── TestScenarios.md                  # 15 interactive demo scenarios for live evaluation
└── src/
    └── chess/
        ├── Main.java                 # Entry point with global uncaught exception safety net
        ├── TestVerifier.java         # Automated regression verifier for all 15 scenarios
        ├── engine/
        │   ├── CheckDetector.java    # Square attack and King check evaluator
        │   ├── GameController.java   # State manager, timer coordinator, and input parser
        │   ├── GameRules.java        # Move execution, turn switching, and game status
        │   └── MoveValidator.java    # Complete chess rule and move validator
        ├── exceptions/
        │   ├── ChessException.java             # Base checked exception with error codes
        │   ├── ChessRuntimeException.java      # Unchecked exception for state corruption
        │   ├── CorruptedSaveFileException.java # Save file tampering and I/O error
        │   ├── GameOverException.java          # Move attempt after game completion
        │   ├── IllegalMoveException.java       # Piece geometry rule violation
        │   ├── InvalidCastlingException.java   # Castling rule violation
        │   ├── InvalidMoveFormatException.java # Algebraic input parsing failure
        │   ├── InvalidPromotionException.java  # Invalid pawn promotion request
        │   ├── KingInCheckException.java       # Move leaving friendly King in check
        │   ├── NoPieceSelectedException.java   # Empty square selection
        │   ├── OutOfBoardException.java        # Coordinate outside 8x8 boundaries
        │   ├── OwnPieceCaptureException.java   # Attempt to capture friendly piece
        │   ├── PathBlockedException.java       # Obstruction on piece path
        │   └── WrongTurnException.java         # Moving out of turn
        ├── io/
        │   ├── GameLoader.java       # ObjectInputStream loader with integrity checks
        │   ├── GameLogger.java       # AutoCloseable file logger with stack trace dump
        │   └── GameSaver.java        # ObjectOutputStream serializer with state validation
        ├── model/
        │   ├── Board.java            # 8x8 grid model and piece tracker
        │   ├── Color.java            # Player color enumeration (WHITE, BLACK)
        │   ├── GameState.java        # Serializable snapshot of complete game session
        │   ├── Move.java             # Move record with algebraic notation formatter
        │   ├── Position.java         # (row, col) coordinates with algebraic converter
        │   └── Square.java           # Board square container
        ├── pieces/
        │   ├── Bishop.java           # Diagonal slider
        │   ├── King.java             # One-step and castling geometry
        │   ├── Knight.java           # L-shaped jumper
        │   ├── Pawn.java             # Forward march, double step, and diagonal capture
        │   ├── Piece.java            # Abstract base piece class
        │   ├── PieceType.java        # Piece type enumeration with Unicode symbols
        │   ├── Queen.java            # Straight and diagonal slider
        │   └── Rook.java             # Orthogonal slider
        └── ui/
            ├── BoardPanel.java       # Interactive 8x8 Swing board with visual highlights
            ├── ChessFrame.java       # Main desktop application window and controls
            ├── MoveHistoryPanel.java # Algebraic move list, timers, and captured pieces
            └── StatusBar.java        # Status indicator and red error message bar
```

---

## Compilation and Execution

### Prerequisites
- Java Development Kit (JDK 8, JDK 17, or newer) installed.

### 1. Compile the Application
From the project root directory (`END-JAVA PROJECT`), run:

```bash
javac -d bin src/chess/exceptions/*.java src/chess/model/*.java src/chess/pieces/*.java src/chess/io/*.java src/chess/engine/*.java src/chess/ui/*.java src/chess/Main.java src/chess/TestVerifier.java
```

On Windows PowerShell:
```powershell
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
```

### 2. Run the Desktop GUI Application
```bash
java -cp bin chess.Main
```

### 3. Run the Automated Test Verifier (Console)
```bash
java -cp bin chess.TestVerifier
```

---

## Exception Handling Concepts Demonstration Table

The table below maps all 14 required exception handling concepts to the exact classes and methods where they are implemented.

| # | Exception Handling Concept | Class Name | File Path | Method / Scope | Description of Real Application |
|---|----------------------------|------------|-----------|----------------|---------------------------------|
| 1 | `try-catch-finally` blocks | `BoardPanel` | `src/chess/ui/BoardPanel.java` | `handleBoardClick` | Surrounds move execution with try-catch-finally to ensure visual selection states and highlights are unconditionally cleared in `finally`. |
| 2 | Multiple catch blocks ordered from specific to general | `ChessFrame` | `src/chess/ui/ChessFrame.java` | `handleTextMove` | Catches `InvalidMoveFormatException` -> `OutOfBoardException` -> `PathBlockedException` -> `KingInCheckException` -> `IllegalMoveException` -> `ChessException` -> `Exception`. |
| 2 | Multiple catch blocks ordered from specific to general | `GameLoader` | `src/chess/io/GameLoader.java` | `loadGame` | Catches specific `FileNotFoundException` before general `IOException`. |
| 2 | Multiple catch blocks ordered from specific to general | `GameController` | `src/chess/engine/GameController.java` | `makeMoveFromAlgebraic` | Catches specific `InputMismatchException` before general `NoSuchElementException`. |
| 3 | Multi-catch using the `\|` operator | `MoveValidator` | `src/chess/engine/MoveValidator.java` | `hasAnyLegalMoves` | Catches unrelated exceptions using `(OwnPieceCaptureException \| PathBlockedException)` and `(InvalidCastlingException \| InvalidPromotionException)`. |
| 3 | Multi-catch using the `\|` operator | `BoardPanel` | `src/chess/ui/BoardPanel.java` | `handleBoardClick` | Catches `(NoPieceSelectedException \| WrongTurnException)` during initial piece selection. |
| 4 | `throw` and `throws` keywords | `MoveValidator`, `Position`, `GameController` | `src/chess/engine/MoveValidator.java`, `src/chess/model/Position.java` | Multiple methods | Explicit method `throws` declarations and runtime `throw new ...` invocations when chess rules are violated. |
| 5 | Custom checked exceptions | `ChessException` and 12 subclasses | `src/chess/exceptions/` | Entire package | Checked exceptions for business domain violations: `IllegalMoveException`, `WrongTurnException`, `PathBlockedException`, etc. |
| 5 | Custom unchecked exceptions | `ChessRuntimeException` | `src/chess/exceptions/ChessRuntimeException.java` | Board integrity checks | Thrown when unexpected board corruption occurs (e.g. missing King or corrupted matrix indexing). |
| 6 | Exception chaining using cause constructor and `getCause()` | `Position` | `src/chess/model/Position.java` | `fromAlgebraic` | Catches `NumberFormatException` and wraps it into `InvalidMoveFormatException` passing the original exception as the cause. |
| 6 | Exception chaining using cause constructor and `getCause()` | `GameLoader` | `src/chess/io/GameLoader.java` | `loadGame` | Catches `ClassNotFoundException` and `IOException`, chaining them into `CorruptedSaveFileException(msg, cause)`. |
| 6 | Exception chaining using cause constructor and `getCause()` | `ChessFrame` | `src/chess/ui/ChessFrame.java` | `handleLoadGame` | Inspects `exception.getCause()` to display root-cause diagnostics to the user. |
| 7 | `try-with-resources` with `AutoCloseable` | `GameLogger` | `src/chess/io/GameLogger.java` | `logStatic`, `logGlobalError` | Implements `AutoCloseable`. Used with `try (GameLogger logger = new GameLogger())` to auto-flush and auto-close writer streams. |
| 7 | `try-with-resources` with `AutoCloseable` | `GameSaver`, `GameLoader` | `src/chess/io/GameSaver.java`, `src/chess/io/GameLoader.java` | `saveGame`, `loadGame` | Automates safe stream closing for `FileOutputStream`, `ObjectOutputStream`, `FileInputStream`, and `ObjectInputStream`. |
| 7 | `try-with-resources` with `AutoCloseable` | `GameController` | `src/chess/engine/GameController.java` | `makeMoveFromAlgebraic` | Uses `try (Scanner scanner = new Scanner(sanitized))` for safe resource release. |
| 8 | Nested `try-catch` blocks | `GameLoader` | `src/chess/io/GameLoader.java` | `loadGame` | Outer try-with-resources manages file streams; inner nested try-catch blocks independently validate `readObject()` and `validateIntegrity()`. |
| 8 | Nested `try-catch` blocks | `GameSaver` | `src/chess/io/GameSaver.java` | `saveGame` | State integrity pre-check runs inside an inner try block before the stream write process. |
| 9 | Re-throwing exceptions after partial handling | `GameRules` | `src/chess/engine/GameRules.java` | `executeMove` | Catches `ChessException`, records audit log via `GameLogger.logStatic`, and immediately re-throws `throw chessException;`. |
| 10 | `finally` block for cleanup and state reset | `BoardPanel` | `src/chess/ui/BoardPanel.java` | `handleBoardClick` | Resets `selectedPosition = null`, clears `legalMovePositions`, and calls `repaint()` in `finally` so the UI never locks up on invalid moves. |
| 11 | Built-in: `NumberFormatException` | `Position` | `src/chess/model/Position.java` | `fromAlgebraic` | Caught and handled when rank coordinate cannot be parsed as an integer. |
| 11 | Built-in: `ArrayIndexOutOfBoundsException` | `Board` | `src/chess/model/Board.java` | `getSquare(int, int)` | Caught when accessing internal grid array and wrapped into `OutOfBoardException`. |
| 11 | Built-in: `NullPointerException` | `Board`, `Piece` | `src/chess/model/Board.java`, `src/chess/pieces/Piece.java` | Multiple | Explicit guards against null positions, null colors, and null pieces. |
| 11 | Built-in: `IOException` | `GameSaver`, `GameLoader`, `GameLogger` | `src/chess/io/` | File operations | Caught and wrapped during disk read, write, and append operations. |
| 11 | Built-in: `FileNotFoundException` | `GameLoader`, `ChessFrame` | `src/chess/io/GameLoader.java`, `src/chess/ui/ChessFrame.java` | `loadGame` | Handled when user attempts to load a non-existent save file. |
| 11 | Built-in: `ClassNotFoundException` | `GameLoader` | `src/chess/io/GameLoader.java` | `loadGame` | Handled during deserialization when encountering unknown classes. |
| 11 | Built-in: `InputMismatchException` | `GameController` | `src/chess/engine/GameController.java` | `makeMoveFromAlgebraic` | Handled during `Scanner` token parsing for manual algebraic move commands. |
| 11 | Built-in: `IllegalArgumentException` | `Piece`, `Square`, `CheckDetector` | Multiple | Parameter validation | Validates non-null constructor arguments and valid game colors. |
| 11 | Built-in: `IllegalStateException` | `GameController`, `ChessFrame` | `src/chess/engine/GameController.java`, `src/chess/ui/ChessFrame.java` | `undoMove` | Thrown when attempting to undo move on an empty history stack; caught and displayed non-destructively in UI. |
| 12 | Global safety net (`Thread.setDefaultUncaughtExceptionHandler`) | `Main` | `src/chess/Main.java` | `main` | Catches any unhandled thread exception globally, logs full stack trace to `game_errors.log`, and alerts the user via a modal dialog. |
| 13 | Exception propagation across layers | `Position` -> `MoveValidator` -> `GameRules` -> `ChessFrame` | Cross-package | Move lifecycle | Model throws boundary exceptions; engine validates rules and re-throws; UI catches, updates status bar, flashes red square, and logs to disk. |
| 14 | `getMessage()`, `getStackTrace()`, and logging over `printStackTrace()` | `GameLogger`, `StatusBar`, `ChessFrame` | `src/chess/io/GameLogger.java`, `src/chess/ui/StatusBar.java` | `log`, `showErrorMessage` | Iterates over `throwable.getStackTrace()` to write formatted trace to `game_errors.log`; displays user-friendly `getMessage()` in GUI. |

---

## Game Features Overview

- **Interactive 8x8 Board**: Click a piece to select, click destination to move. Selected square turns yellow, valid destinations highlighted with green markers, illegal moves flash the target square in red.
- **Full Chess Rules**:
  - Legal moves for King, Queen, Rook, Bishop, Knight, and Pawn.
  - Check detection with red highlighting on the checked King.
  - Checkmate and Stalemate detection.
  - Kingside and Queenside castling with all safety checks (cannot castle out of, through, or into check; pieces between must be clear; King and Rook must not have moved).
  - En passant capture.
  - Pawn promotion dialog (Queen, Rook, Bishop, Knight).
- **Turn-based Play**: Turn indicator for White and Black.
- **Move History**: Complete algebraic notation record with automatic scrolling.
- **Captured Pieces**: Real-time display of captured pieces with Unicode symbols.
- **Clocks / Timers**: Configurable countdown clock per player with automatic flag-fall detection.
- **Manual Move Input**: Text box for typing moves (e.g. `e2 e4` or `e7 e8 Q`) to test algebraic parsing exceptions.
- **Game Persistence**: Save and Load games to `.ser` files via Java Object Serialization.
- **Undo Move**: Reverts board, captured pieces, turn, and clock to previous turn state.

