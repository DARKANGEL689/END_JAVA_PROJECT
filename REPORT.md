# ACADEMIC PROJECT REPORT

## PROJECT TITLE: Java Chess - Exception Handling Architecture
**Course / Discipline**: Object-Oriented Software Engineering & Advanced Java  
**Platform / Language**: Java SE 17 (Pure Standard Library, Swing GUI)  
**Build & Execution Harness**: Plain `javac` & `java` (Zero External Dependencies)  
**Source Repository**: [https://github.com/DARKANGEL689/END_JAVA_PROJECT.git](https://github.com/DARKANGEL689/END_JAVA_PROJECT.git)  
**Author / Developer**: DARKANGEL689  

---

## 1. EXECUTIVE SUMMARY

The **Java Chess: Exception Handling Architecture** project is a production-grade, standalone desktop Chess application implemented in standard Java 17 using the Java Swing graphical framework. Built entirely without third-party frameworks, build plugins (such as Maven or Gradle), or external binaries, the application executes natively on any standard Java Virtual Machine (JDK 8, JDK 17, or newer).

While providing a fully functional, standard chess simulation—including turn-based mechanics, piece-specific movement vectors, check/checkmate/stalemate detection, kingside/queenside castling, en passant, pawn promotion, time controls, and state persistence—the central architectural objective of this project is **demonstrating rigorous, multi-layered exception handling**.

Rather than relying on defensive Boolean flags or naive `System.err.println` diagnostics, the system employs an enterprise exception infrastructure:
- A custom 14-class hierarchy extending `Exception` (checked) and `RuntimeException` (unchecked).
- Machine-parseable error codes (`ERR_*`) combined with contextual user-facing error explanations.
- Multi-layer exception propagation across Model, Engine, I/O, and Presentation layers.
- Advanced Java exception mechanics: try-with-resources with custom `AutoCloseable` resources, exception chaining via `getCause()`, multi-catch blocks using the `|` operator, specific-to-general catch ordering, nested try-catch blocks, re-throwing after partial logging, and an uncaught exception global safety net.
- Guaranteed state recovery: `finally` blocks ensure UI and selection pointers are reset, preventing interface deadlock or corrupted board states upon illegal operations.
- Non-destructive execution: handled exceptions are logged with timestamps, thread identification, error codes, and full stack trace arrays into a dedicated disk log (`game_errors.log`), while the user receives clear, visual, non-crashing feedback.

---

## 2. PROBLEM STATEMENT & OBJECTIVES

### 2.1 The Problem
In standard academic and enterprise software development, exception handling is frequently relegated to an afterthought—often characterized by empty catch blocks, catch-all `catch (Exception e)` statements, unhandled runtime crashes, or user interfaces that freeze when unexpected input or rule violations occur. In complex stateful systems such as Chess, illegal operations (e.g., trying to move through obstacles, capturing friendly pieces, or moving into check) are common and must be handled deterministically without system degradation.

### 2.2 Project Objectives
1. **Zero-Crash Resilience**: Guarantee that invalid user mouse inputs, malformed algebraic notation text strings, corrupted save files, or invalid turns never crash the JVM or freeze the GUI.
2. **Layered Separation of Concerns**: Enforce strict separation between the Domain Model (`chess.model`), Piece Logic (`chess.pieces`), Game Engine (`chess.engine`), Persistence/Logging (`chess.io`), and GUI (`chess.ui`).
3. **Exemplary Exception Handling**: Actively implement and demonstrate all 14 standard Java exception handling patterns in real, meaningful application scenarios.
4. **Structured Auditing**: Provide transparent runtime audit trails by replacing standard terminal dumping (`printStackTrace`) with structured file logging (`GameLogger`).
5. **Self-Documenting Code Standards**: Adhere strictly to clean Object-Oriented Design (encapsulation, polymorphism, abstraction, inheritance) with **zero code comments**, proving that clean architecture and expressive naming render comments redundant.

---

## 3. SYSTEM ARCHITECTURE & DESIGN

The project is structured into six modular packages, following a variant of the Model-View-Controller (MVC) architectural pattern:

```
+-------------------------------------------------------------------------+
|                              PRESENTATION                               |
|              (chess.ui: ChessFrame, BoardPanel, StatusBar, ...)         |
+-------------------------------------------------------------------------+
        |                                                 ^
        | Move Requests / UI Events                       | Repaint / Errors
        v                                                 |
+-------------------------------------------------------------------------+
|                               CONTROLLER                                |
|             (chess.engine: GameController, GameRules)                   |
+-------------------------------------------------------------------------+
        |                                                 ^
        | Rule Validation / State Evaluation              | Validated Moves
        v                                                 |
+-------------------------------------------------------------------------+
|                            DOMAIN ENGINE                                |
|        (chess.engine: MoveValidator, CheckDetector; chess.pieces)       |
+-------------------------------------------------------------------------+
        |                                                 ^
        | State Inspection / Piece Query                  | Coordinate Checks
        v                                                 |
+-------------------------------------------------------------------------+
|                                MODEL                                    |
|              (chess.model: Board, Square, Position, GameState)          |
+-------------------------------------------------------------------------+
        |                                                 ^
        | Serialization / Logging                         | Deserialization
        v                                                 |
+-------------------------------------------------------------------------+
|                            PERSISTENCE & I/O                            |
|             (chess.io: GameSaver, GameLoader, GameLogger)               |
+-------------------------------------------------------------------------+
```

### 3.1 Package Organization

| Package | Primary Responsibility | Key Classes |
|---|---|---|
| `chess.exceptions` | Custom exception hierarchy containing checked and unchecked domain errors | `ChessException`, `IllegalMoveException`, `OutOfBoardException`, `WrongTurnException`, `NoPieceSelectedException`, `OwnPieceCaptureException`, `PathBlockedException`, `KingInCheckException`, `InvalidCastlingException`, `InvalidPromotionException`, `GameOverException`, `InvalidMoveFormatException`, `CorruptedSaveFileException`, `ChessRuntimeException` |
| `chess.model` | Core board state representations, coordinate systems, and game state snapshots | `Board`, `Square`, `Position`, `Move`, `Color`, `GameState` |
| `chess.pieces` | Polymorphic piece representations, movement vectors, and Unicode glyphs | `Piece` (abstract), `King`, `Queen`, `Rook`, `Bishop`, `Knight`, `Pawn`, `PieceType` |
| `chess.engine` | Validation pipeline, check/checkmate detection algorithms, and controller logic | `MoveValidator`, `CheckDetector`, `GameRules`, `GameController` |
| `chess.io` | Game persistence via serialization and file-based audit logging | `GameSaver`, `GameLoader`, `GameLogger` |
| `chess.ui` | Graphical Swing interface, custom canvas drawing, and interactive panels | `ChessFrame`, `BoardPanel`, `MoveHistoryPanel`, `StatusBar` |
| `chess` | Application entry point and automated verification harness | `Main`, `TestVerifier` |

---

## 4. CUSTOM EXCEPTION HIERARCHY

The exception system is designed around checked exceptions for recoverable user/domain rule violations, and unchecked exceptions for fatal board state integrity failures.

### 4.1 Exception Inheritance Diagram

```
java.lang.Throwable
   │
   ├── java.lang.Exception
   │     │
   │     └── chess.exceptions.ChessException (checked, base class)
   │           ├── IllegalMoveException (ERR_ILLEGAL_MOVE)
   │           ├── OutOfBoardException (ERR_OUT_OF_BOARD)
   │           ├── WrongTurnException (ERR_WRONG_TURN)
   │           ├── NoPieceSelectedException (ERR_NO_PIECE_SELECTED)
   │           ├── OwnPieceCaptureException (ERR_OWN_PIECE_CAPTURE)
   │           ├── PathBlockedException (ERR_PATH_BLOCKED)
   │           ├── KingInCheckException (ERR_KING_IN_CHECK)
   │           ├── InvalidCastlingException (ERR_INVALID_CASTLING)
   │           ├── InvalidPromotionException (ERR_INVALID_PROMOTION)
   │           ├── GameOverException (ERR_GAME_OVER)
   │           ├── InvalidMoveFormatException (ERR_INVALID_MOVE_FORMAT)
   │           └── CorruptedSaveFileException (ERR_CORRUPTED_SAVE)
   │
   └── java.lang.RuntimeException
         │
         └── chess.exceptions.ChessRuntimeException (unchecked, state corruption)
```

### 4.2 Exception Specifications

1. **`ChessException`**:
   - Extends: `java.lang.Exception`
   - Attributes: `private final String errorCode`
   - Purpose: Base class for all checked game exceptions. Exposes `getErrorCode()` so user interfaces can categorize issues programmatically.
2. **`IllegalMoveException`**:
   - Error Code: `ERR_ILLEGAL_MOVE`
   - Purpose: Thrown when a piece attempts a trajectory that violates its geometric movement rules (e.g., a Knight moving straight, or a Bishop moving horizontally).
3. **`OutOfBoardException`**:
   - Error Code: `ERR_OUT_OF_BOARD`
   - Purpose: Thrown when coordinate calculations or algebraic inputs fall outside the `[0..7]` row/column bounds.
4. **`WrongTurnException`**:
   - Error Code: `ERR_WRONG_TURN`
   - Purpose: Thrown when a player attempts to select or move an opponent's piece.
5. **`NoPieceSelectedException`**:
   - Error Code: `ERR_NO_PIECE_SELECTED`
   - Purpose: Thrown when a move initiation begins on an empty square.
6. **`OwnPieceCaptureException`**:
   - Error Code: `ERR_OWN_PIECE_CAPTURE`
   - Purpose: Thrown when a piece's destination square contains a friendly piece of the same color.
7. **`PathBlockedException`**:
   - Error Code: `ERR_PATH_BLOCKED`
   - Purpose: Thrown when a sliding piece (Rook, Bishop, Queen) or a double-stepping Pawn encounters an intermediate piece.
8. **`KingInCheckException`**:
   - Error Code: `ERR_KING_IN_CHECK`
   - Purpose: Thrown when a proposed move fails King safety validation (i.e., leaves the player's own King under attack).
9. **`InvalidCastlingException`**:
   - Error Code: `ERR_INVALID_CASTLING`
   - Purpose: Thrown when castling violates any official rule (King has moved, Rook has moved, intervening squares occupied, or King passes through/into check).
10. **`InvalidPromotionException`**:
    - Error Code: `ERR_INVALID_PROMOTION`
    - Purpose: Thrown when a player specifies an illegal promotion target (e.g., King or Pawn) or attempts promotion prior to reaching the opposite back rank.
11. **`GameOverException`**:
    - Error Code: `ERR_GAME_OVER`
    - Purpose: Thrown when any move is attempted after the game has terminated via checkmate, stalemate, or clock expiration.
12. **`InvalidMoveFormatException`**:
    - Error Code: `ERR_INVALID_MOVE_FORMAT`
    - Purpose: Thrown when manual text input cannot be parsed into valid start and end algebraic coordinates.
13. **`CorruptedSaveFileException`**:
    - Error Code: `ERR_CORRUPTED_SAVE`
    - Purpose: Thrown when reading a tampered, incomplete, or incompatible binary `.ser` save file.
14. **`ChessRuntimeException`**:
    - Extends: `java.lang.RuntimeException`
    - Purpose: Unchecked exception thrown when internal board integrity is violated (e.g., missing King during check evaluation or array access inconsistency).

---

## 5. EXCEPTION HANDLING CONCEPTS IMPLEMENTATION MATRIX

The following table provides the exhaustive mapping of all 14 required exception handling concepts to their concrete implementation sites across the codebase:

| Concept # | Language Construct | Class / File | Method / Location | Concrete Architectural Usage |
|---|---|---|---|---|
| **1** | `try-catch-finally` | `BoardPanel` (`chess.ui`) | `handleBoardClick` | Encloses move dispatch. The `finally` block unconditionally clears selected positions and repaints the board. |
| **2** | Multiple Catch Blocks (Specific to General) | `ChessFrame` (`chess.ui`) | `handleTextMove` | Catches `InvalidMoveFormatException` -> `OutOfBoardException` -> `PathBlockedException` -> `KingInCheckException` -> `IllegalMoveException` -> `ChessException` -> `Exception`. |
| **2** | Multiple Catch Blocks (Specific to General) | `GameLoader` (`chess.io`) | `loadGame` | Catches specific `FileNotFoundException` before general `IOException`. |
| **2** | Multiple Catch Blocks (Specific to General) | `GameController` (`chess.engine`) | `makeMoveFromAlgebraic` | Catches specific `InputMismatchException` before general `NoSuchElementException`. |
| **3** | Multi-Catch (`\|` operator) | `MoveValidator` (`chess.engine`) | `hasAnyLegalMoves` | Groups unrelated exceptions: `catch (OwnPieceCaptureException \| PathBlockedException)` and `catch (InvalidCastlingException \| InvalidPromotionException)`. |
| **3** | Multi-Catch (`\|` operator) | `BoardPanel` (`chess.ui`) | `handleBoardClick` | Handles initial square validation via `catch (NoPieceSelectedException \| WrongTurnException)`. |
| **4** | `throw` and `throws` keywords | `MoveValidator`, `Position`, `GameRules` | Throughout codebase | Declares checked exceptions on method signatures and triggers custom exceptions using `throw new ...`. |
| **5** | Custom Checked Exceptions | `chess.exceptions.*` | 13 Checked Classes | Models domain rule failures with structured error codes. |
| **5** | Custom Unchecked Exceptions | `ChessRuntimeException` | `Board.findKing`, `Board.initializeEmptySquares` | Catches board state corruption where recovery is impossible without resetting. |
| **6** | Exception Chaining (`getCause()`) | `Position` (`chess.model`) | `fromAlgebraic` | Catches `NumberFormatException` and chains it: `new InvalidMoveFormatException(..., exception)`. |
| **6** | Exception Chaining (`getCause()`) | `GameLoader` (`chess.io`) | `loadGame` | Catches `ClassNotFoundException` and `IOException` and chains them into `CorruptedSaveFileException(msg, cause)`. |
| **6** | Exception Chaining (`getCause()`) | `ChessFrame` (`chess.ui`) | `handleLoadGame` | Inspects `exception.getCause()` and displays root-cause class names and messages in UI error dialogs. |
| **7** | `try-with-resources` (`AutoCloseable`) | `GameLogger` (`chess.io`) | `logStatic`, `logGlobalError` | Custom class implementing `AutoCloseable`. Flushes and closes `PrintWriter` automatically upon exit. |
| **7** | `try-with-resources` (`AutoCloseable`) | `GameSaver`, `GameLoader` | `saveGame`, `loadGame` | Safely manages `FileOutputStream`, `ObjectOutputStream`, `FileInputStream`, and `ObjectInputStream`. |
| **7** | `try-with-resources` (`AutoCloseable`) | `GameController` (`chess.engine`) | `makeMoveFromAlgebraic` | Employs `try (Scanner scanner = new Scanner(sanitized))` for deterministic resource cleanup. |
| **8** | Nested `try-catch` Blocks | `GameLoader` (`chess.io`) | `loadGame` | Outer block manages stream lifecycle; inner nested try blocks isolate `readObject()` and `validateIntegrity()`. |
| **8** | Nested `try-catch` Blocks | `GameSaver` (`chess.io`) | `saveGame` | Outer block handles disk output; inner nested try block isolates byte-level serialization. |
| **9** | Re-throwing After Partial Handling | `GameRules` (`chess.engine`) | `executeMove` | Catches `ChessException`, logs audit metadata to `GameLogger`, then executes `throw chessException;` to notify UI. |
| **10** | `finally` State Reset & Cleanup | `BoardPanel` (`chess.ui`) | `handleBoardClick` | Guarantees that whether a move succeeds or fails, `selectedPosition = null` and `legalMovePositions.clear()` occur. |
| **11** | Built-in: `NumberFormatException` | `Position` (`chess.model`) | `fromAlgebraic` | Catches rank parsing errors when parsing strings like `"eX"`. |
| **11** | Built-in: `ArrayIndexOutOfBoundsException` | `Board` (`chess.model`) | `getSquare(int, int)` | Catches array indexing bounds and re-maps to `OutOfBoardException`. |
| **11** | Built-in: `NullPointerException` | `Board`, `Piece` | `Board.getSquare`, `Piece.<init>` | Guards against null pointers for coordinates and color parameters. |
| **11** | Built-in: `IOException` | `GameSaver`, `GameLoader`, `GameLogger` | Persistence layer | Handled during file creation, byte writes, and stream reads. |
| **11** | Built-in: `FileNotFoundException` | `GameLoader` (`chess.io`) | `loadGame` | Handled when loading nonexistent `.ser` paths. |
| **11** | Built-in: `ClassNotFoundException` | `GameLoader` (`chess.io`) | `loadGame` | Handled when deserializing incompatible class byte streams. |
| **11** | Built-in: `InputMismatchException` | `GameController` (`chess.engine`) | `makeMoveFromAlgebraic` | Handled during scanner token validation. |
| **11** | Built-in: `IllegalArgumentException` | `Piece`, `Square`, `CheckDetector` | Constructors | Enforces preconditions on non-null parameters. |
| **11** | Built-in: `IllegalStateException` | `GameController`, `ChessFrame` | `undoMove` | Thrown when attempting to undo with an empty history stack; caught non-destructively in UI. |
| **12** | Global Uncaught Exception Handler | `Main` (`chess`) | `main` | `Thread.setDefaultUncaughtExceptionHandler` catches any unhandled thread errors, logs to disk, and displays modal dialog. |
| **13** | Multi-Layer Exception Propagation | System-Wide | Move Execution Flow | Model throws -> Engine validates & re-throws -> Controller manages state -> UI catches, flashes red, and notifies user. |
| **14** | `getMessage()` & `getStackTrace()` Logging | `GameLogger`, `StatusBar` | `GameLogger.log`, `StatusBar.showErrorMessage` | Dumps formatted stack trace arrays to `game_errors.log`; displays clean `getMessage()` text to user. |

---

## 6. CORE CHESS ENGINE IMPLEMENTATION

### 6.1 Board & Coordinate System
The chessboard is modeled as an 8x8 matrix (`Square[8][8]`). Coordinates are encapsulated in an immutable `Position` class representing `(row, col)` with values `[0..7]`:
- Row `0` represents Rank 8 (Black back rank: `a8` to `h8`).
- Row `1` represents Rank 7 (Black pawns: `a7` to `h7`).
- Row `6` represents Rank 2 (White pawns: `a2` to `h2`).
- Row `7` represents Rank 1 (White back rank: `a1` to `h1`).
- Columns `0` through `7` correspond to Files `a` through `h`.

`Position.fromAlgebraic(String)` converts notation like `"e4"` into `row = 4, col = 4`, using explicit exception handling to catch bad files or non-integer ranks.

### 6.2 Piece Movement & Polymorphism
Every piece extends the abstract `Piece` class, which declares:
```java
public abstract boolean isValidPieceMovement(Position start, Position end);
public abstract Piece copy();
```
- **King**: Maximum distance of 1 square horizontally, vertically, or diagonally; 2 squares horizontally for castling.
- **Queen**: Arbitrary straight or diagonal distance.
- **Rook**: Arbitrary straight distance horizontally or vertically. Tracks `hasMoved` state for castling.
- **Bishop**: Arbitrary diagonal distance.
- **Knight**: L-shape trajectory (`abs(dx) == 1 && abs(dy) == 2` or `abs(dx) == 2 && abs(dy) == 1`). Can jump over occupied squares.
- **Pawn**: Forward 1 step; forward 2 steps on first move; diagonal capture forward 1 step. Direction is `-1` for White and `+1` for Black.

### 6.3 Check & King Safety Validation
Before committing any move, `MoveValidator.verifyKingSafetyAfterMove(...)` creates a cloned trial board (`Board.copy()`), executes the proposed move on the clone, and runs `CheckDetector.isKingInCheck(...)`.
If the player's own King is attacked on the simulated board, the move is rejected with `KingInCheckException`.

### 6.4 Castling Rules
Castling requires:
1. Neither the King nor the chosen Rook has previously moved.
2. The King is not currently in check.
3. All intermediate squares between King and Rook are empty.
4. The transit square that the King crosses is not under attack.
5. The destination square is not under attack.
Violating any condition throws `InvalidCastlingException`.

### 6.5 En Passant
When a pawn advances 2 squares, the intermediate skipped square is recorded in `GameState.enPassantTarget`. On the immediately subsequent half-move, an opposing pawn moving diagonally to this target square captures the pawn and removes it from the board.

### 6.6 Pawn Promotion
When a pawn reaches row `0` (White) or row `7` (Black), the user is prompted via a modal dialog to select between Queen, Rook, Bishop, or Knight. If an invalid type is submitted via text or the selection is cancelled, `InvalidPromotionException` is thrown.

---

## 7. PERSISTENCE, AUDITING & LOGGING

### 7.1 Binary State Serialization (`GameSaver` & `GameLoader`)
The entire `GameState` object implements `java.io.Serializable`.
- **Saving**: Enclosed in `try-with-resources` managing `FileOutputStream` and `ObjectOutputStream`. Before serialization, `gameState.validateIntegrity()` verifies that both Kings exist and clock values are valid.
- **Loading**: Opens `ObjectInputStream` within a `try-with-resources` block. Detects file missing (`FileNotFoundException`), corrupted bytes (`StreamCorruptedException`), or version mismatches (`ClassNotFoundException`), wrapping them in `CorruptedSaveFileException` with root causes preserved.

### 7.2 Structured Audit Logging (`GameLogger`)
The custom `GameLogger` class implements `java.lang.AutoCloseable`:
- Opens `game_errors.log` in append mode.
- Avoids raw console dumping by formatting structured records containing:
  - Exact timestamp (`yyyy-MM-dd HH:mm:ss.SSS`)
  - Execution thread name
  - Architectural context identifier
  - Exception class name
  - Error code (`ERR_*`)
  - Detail message
  - Chained root cause
  - Formatted stack trace array iterating over `StackTraceElement` instances.

Sample log output:
```
================================================================================
TIMESTAMP: 2026-11-03 21:49:24.221
THREAD:    main
CONTEXT:   MOVE_EXECUTION_RETHROWN
EXCEPTION: chess.exceptions.NoPieceSelectedException
ERROR CODE: ERR_NO_PIECE_SELECTED
MESSAGE:   No piece exists at square e4.
STACK TRACE:
    at chess.engine.MoveValidator.validate(MoveValidator.java:56)
    at chess.engine.GameRules.executeMove(GameRules.java:36)
    at chess.engine.GameController.makeMove(GameController.java:58)
    at chess.ui.BoardPanel.handleBoardClick(BoardPanel.java:134)
================================================================================
```

---

## 8. USER INTERFACE & HUMAN-COMPUTER INTERACTION

The UI is developed in Swing and comprises:
1. **`ChessFrame`**: The top-level `JFrame` hosting menus, control toolbars (New Game, Undo Move, Save Game, Load Game), manual text move inputs, and the master layout.
2. **`BoardPanel`**: Custom `JPanel` rendering the 8x8 checkerboard:
   - Alternating wood colors: Light `#F0D9B5`, Dark `#B58863`.
   - Selected piece: Translucent yellow highlight.
   - Legal move destinations: Soft green circles.
   - King in check: Distinct translucent red square overlay.
   - **Illegal move visual feedback**: Destination square flashes **bright red** for 650ms via a non-blocking `javax.swing.Timer`.
3. **`MoveHistoryPanel`**: Tabular list of played moves in algebraic notation (`1. e4 e5 2. Nf3 Nc6 ...`), captured piece pools with Unicode glyphs, and active player countdown clocks.
4. **`StatusBar`**: Persistent bottom bar displaying active turn and color-coded status messages (green for valid moves, bold red with `[ERR_*]` codes for exceptions).

---

## 9. VERIFICATION & TESTING METHODOLOGY

### 9.1 Automated Regression Test Suite (`TestVerifier.java`)
The application includes an automated test runner (`chess.TestVerifier`) that programmatically verifies the top 15 illegal scenarios without requiring GUI interaction:
```bash
java -cp bin chess.TestVerifier
```
**Test Result**: `VERIFICATION RESULT: 15 / 15 PASSED` (100% pass rate).

### 9.2 Summary of 15 Test Scenarios

| Scenario | Trigger / User Action | Exception Raised | Error Code | UI / System Reaction |
|---|---|---|---|---|
| **1. Out of Bounds** | Type `z9` in manual text box | `OutOfBoardException` | `ERR_OUT_OF_BOARD` | Warning dialog + red status bar |
| **2. Empty Selection** | Click empty square `e4` on turn 1 | `NoPieceSelectedException` | `ERR_NO_PIECE_SELECTED` | Square flashes red + red status bar |
| **3. Wrong Turn** | Click Black pawn `e7` on White's turn | `WrongTurnException` | `ERR_WRONG_TURN` | Square flashes red + red status bar |
| **4. Friendly Capture** | Click White Rook `a1` -> White Pawn `a2` | `OwnPieceCaptureException` | `ERR_OWN_PIECE_CAPTURE` | Target flashes red; selection cleared in `finally` |
| **5. Illegal Trajectory** | Click White Knight `b1` -> Square `b3` | `IllegalMoveException` | `ERR_ILLEGAL_MOVE` | Target flashes red + red status bar |
| **6. Path Blocked** | Click White Queen `d1` -> Square `d4` | `PathBlockedException` | `ERR_PATH_BLOCKED` | Destination flashes red + obstacle identified |
| **7. King in Check** | King under check; player moves non-defending piece | `KingInCheckException` | `ERR_KING_IN_CHECK` | Destination flashes red; King highlighted red |
| **8. Blocked Castling** | Type `e1 g1` on turn 1 (pieces between King & Rook) | `InvalidCastlingException` | `ERR_INVALID_CASTLING` | Alert dialog: square `g1` between King & Rook occupied |
| **9. Castling in Check** | Attempt castling while King passes through attacked square | `InvalidCastlingException` | `ERR_INVALID_CASTLING` | Alert dialog: transit square under attack |
| **10. Bad Promotion** | Type `e7 e8 K` (attempting promotion to King) | `InvalidPromotionException` | `ERR_INVALID_PROMOTION` | Alert dialog: Pawns cannot promote to King |
| **11. Early Promotion** | Type `e2 e4 Q` (promoting on non-back rank) | `InvalidPromotionException` | `ERR_INVALID_PROMOTION` | Alert dialog: Only pawns reaching final rank promote |
| **12. Game Over Move** | Make a move after delivering Scholar's Mate checkmate | `GameOverException` | `ERR_GAME_OVER` | Warning dialog: Game finished |
| **13. Malformed Notation**| Type `move pawn` or `e4` (single coordinate) | `InvalidMoveFormatException` | `ERR_INVALID_MOVE_FORMAT` | Warning dialog + chained scanner exception |
| **14. Corrupted Save** | Load dummy file with random bytes via Load Game | `CorruptedSaveFileException` | `ERR_CORRUPTED_SAVE` | Error dialog displays chained `StreamCorruptedException` |
| **15. Undo on Empty** | Click "Undo Move" at start of new game | `IllegalStateException` | `ERR_ILLEGAL_STATE` | Non-blocking info dialog: no history recorded |

---

## 10. CODE QUALITY & COMPLIANCE

1. **Zero Comments Policy**: All 45 Java source files contain **zero comments** (`no //`, `no /* */`, `no /** */`). Method, parameter, and class names are self-documenting.
2. **SOLID Principles**:
   - *Single Responsibility*: Engine calculates, Model stores, UI renders, IO persists.
   - *Open/Closed*: Adding a new piece requires extending `Piece` without altering `BoardPanel` or `CheckDetector`.
   - *Liskov Substitution*: Any `Piece` subclass can be substituted into `Square` and validated uniformly.
   - *Interface Segregation*: Components implement precise interfaces (`Serializable`, `AutoCloseable`).
   - *Dependency Inversion*: `GameRules` depends on abstractions (`MoveValidator`, `CheckDetector`) rather than UI implementations.
3. **Encapsulation**: All fields are `private` or `private final` with strict accessor and mutator methods enforcing validation.

---

## 11. BUILD & RUN PROCEDURES

### 11.1 Compilation
From the project root directory (`END-JAVA PROJECT`):

**Standard Command Prompt / Bash**:
```bash
javac -d bin src/chess/exceptions/*.java src/chess/model/*.java src/chess/pieces/*.java src/chess/io/*.java src/chess/engine/*.java src/chess/ui/*.java src/chess/Main.java src/chess/TestVerifier.java
```

**Windows PowerShell**:
```powershell
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName })
```

### 11.2 Execution
To run the desktop GUI application:
```bash
java -cp bin chess.Main
```

To run the automated 15-scenario verification test:
```bash
java -cp bin chess.TestVerifier
```

---

## 12. CONCLUSION

The **Java Chess: Exception Handling Architecture** successfully fulfills all academic and functional requirements. By integrating a 14-class custom exception hierarchy into every phase of the chess domain lifecycle—from coordinate parsing and piece geometry to King safety simulations, game state serialization, and GUI event handling—the application proves that thorough exception handling is essential to building reliable, resilient, and enterprise-grade Java software.
