# Live Demonstration Test Scenarios

This document provides 15 step-by-step demo scenarios you can perform live during your presentation or evaluation. Each scenario triggers a specific custom exception, demonstrates how the application handles it without crashing, highlights the invalid action visually, and logs the detailed stack trace to `game_errors.log`.

---

### Scenario 1: OutOfBoardException
- **Concept / Exception**: `chess.exceptions.OutOfBoardException` (Error Code: `ERR_OUT_OF_BOARD`)
- **Action**:
  1. In the **Manual Move** text box at the bottom, type `z9` or `e9 e4`.
  2. Press **Enter** or click **Submit Move**.
- **Expected GUI Behavior**:
  - A warning dialog pops up: `"File 'z' is outside chessboard columns (a-h)"` or `"Rank 9 is outside chessboard rows (1-8)"`.
  - The status bar displays in red: `[ERR_OUT_OF_BOARD] ...`
  - The game state remains unaffected and ready for the next action.
- **Log Entry**:
  - `game_errors.log` records an `OutOfBoardException` entry with error code `ERR_OUT_OF_BOARD` and stack trace.

---

### Scenario 2: NoPieceSelectedException
- **Concept / Exception**: `chess.exceptions.NoPieceSelectedException` (Error Code: `ERR_NO_PIECE_SELECTED`)
- **Action**:
  1. On the board, click on any empty square (e.g., click `e4` or `d5` at the start of the game).
- **Expected GUI Behavior**:
  - The clicked empty square flashes red briefly.
  - The status bar displays in red: `[ERR_NO_PIECE_SELECTED] No piece exists at square e4. Select a valid piece to move.`
  - No piece is selected, and the board remains completely intact.
- **Log Entry**:
  - `game_errors.log` records a `NoPieceSelectedException` entry.

---

### Scenario 3: WrongTurnException
- **Concept / Exception**: `chess.exceptions.WrongTurnException` (Error Code: `ERR_WRONG_TURN`)
- **Action**:
  1. While it is White's turn at the start of the game, click on a Black piece (e.g., Black pawn at `e7` or Black Knight at `b8`).
- **Expected GUI Behavior**:
  - The clicked square flashes red.
  - The status bar displays in red: `[ERR_WRONG_TURN] It is White's turn. You clicked a Black piece.`
  - The Black piece is not selected.
- **Log Entry**:
  - `game_errors.log` records a `WrongTurnException` entry with error code `ERR_WRONG_TURN`.

---

### Scenario 4: OwnPieceCaptureException
- **Concept / Exception**: `chess.exceptions.OwnPieceCaptureException` (Error Code: `ERR_OWN_PIECE_CAPTURE`)
- **Action**:
  1. Click on the White Rook at `a1` to select it.
  2. Click on the White Pawn directly ahead at `a2`.
- **Expected GUI Behavior**:
  - Square `a2` flashes red.
  - The status bar displays in red: `[ERR_OWN_PIECE_CAPTURE] Cannot capture friendly piece at a2 (White PAWN).`
  - The selection is safely cleared by the `finally` block.
- **Log Entry**:
  - `game_errors.log` records an `OwnPieceCaptureException` entry.

---

### Scenario 5: IllegalMoveException
- **Concept / Exception**: `chess.exceptions.IllegalMoveException` (Error Code: `ERR_ILLEGAL_MOVE`)
- **Action**:
  1. Click on the White Knight at `b1`.
  2. Click on square `b3` (attempting to move a Knight straight like a Pawn).
- **Expected GUI Behavior**:
  - Square `b3` flashes red.
  - The status bar displays in red: `[ERR_ILLEGAL_MOVE] White KNIGHT cannot move from b1 to b3.`
  - The selection is cleared and the knight stays on `b1`.
- **Log Entry**:
  - `game_errors.log` records an `IllegalMoveException` entry.

---

### Scenario 6: PathBlockedException
- **Concept / Exception**: `chess.exceptions.PathBlockedException` (Error Code: `ERR_PATH_BLOCKED`)
- **Action**:
  1. Click on the White Queen at `d1`.
  2. Click on square `d4` (attempting to slide the Queen straight through the White Pawn on `d2`).
- **Expected GUI Behavior**:
  - Square `d4` flashes red.
  - The status bar displays in red: `[ERR_PATH_BLOCKED] Path to d4 is obstructed by another piece at square d2.`
  - The Queen remains on `d1`.
- **Log Entry**:
  - `game_errors.log` records a `PathBlockedException` entry.

---

### Scenario 7: KingInCheckException
- **Concept / Exception**: `chess.exceptions.KingInCheckException` (Error Code: `ERR_KING_IN_CHECK`)
- **Action**:
  1. In the manual move box, enter `e2 e4` (White opens).
  2. Enter `f7 f5` (Black weakens king).
  3. Enter `d1 h5` (White Queen puts Black King in check).
  4. Now on Black's turn, attempt to move an unrelated piece, such as typing `a7 a6` or clicking the Black Rook at `a8` to `b8`.
- **Expected GUI Behavior**:
  - The attempted destination flashes red.
  - An alert dialog or red status bar appears: `[ERR_KING_IN_CHECK] Move leaves or places Black King in check.`
  - Black King square (`e8`) is highlighted in red.
  - Black is prevented from completing any move that does not resolve the check.
- **Log Entry**:
  - `game_errors.log` records a `KingInCheckException` entry.

---

### Scenario 8: InvalidCastlingException (Pieces Between King and Rook)
- **Concept / Exception**: `chess.exceptions.InvalidCastlingException` (Error Code: `ERR_INVALID_CASTLING`)
- **Action**:
  1. Start a New Game.
  2. In the manual move box, type `e1 g1` (attempting kingside castling while Bishop at `f1` and Knight at `g1` are still on the board).
  3. Click **Submit Move**.
- **Expected GUI Behavior**:
  - A warning dialog pops up: `"Cannot castle: square g1 between King and Rook is occupied."`
  - The status bar displays in red: `[ERR_INVALID_CASTLING] ...`
  - Castling is rejected.
- **Log Entry**:
  - `game_errors.log` records an `InvalidCastlingException` entry.

---

### Scenario 9: InvalidCastlingException (Transit Square Under Attack)
- **Concept / Exception**: `chess.exceptions.InvalidCastlingException` (Error Code: `ERR_INVALID_CASTLING`)
- **Action**:
  1. Clear the path between White King and Kingside Rook by moving `e2 e4`, `g1 f3`, and `f1 c4`.
  2. Have Black position a Bishop or Queen attacking `f1` (e.g. Black bishop at `a6` or pawn attacking `f1`).
  3. Attempt `e1 g1`.
- **Expected GUI Behavior**:
  - An alert dialog pops up: `"Cannot castle: King would cross through attacked square f1."`
  - The status bar displays in red: `[ERR_INVALID_CASTLING] ...`
  - The King remains on `e1`.
- **Log Entry**:
  - `game_errors.log` records an `InvalidCastlingException` entry.

---

### Scenario 10: InvalidPromotionException (Illegal Promotion Piece)
- **Concept / Exception**: `chess.exceptions.InvalidPromotionException` (Error Code: `ERR_INVALID_PROMOTION`)
- **Action**:
  1. In the manual move box, enter `e2 e4 K` or type `e7 e8 K` (attempting to promote a pawn to a King).
  2. Click **Submit Move**.
- **Expected GUI Behavior**:
  - An alert dialog pops up: `"Pawns cannot be promoted to a King."`
  - The status bar displays in red: `[ERR_INVALID_PROMOTION] Pawns cannot be promoted to a King.`
- **Log Entry**:
  - `game_errors.log` records an `InvalidPromotionException` entry.

---

### Scenario 11: InvalidPromotionException (Premature Promotion)
- **Concept / Exception**: `chess.exceptions.InvalidPromotionException` (Error Code: `ERR_INVALID_PROMOTION`)
- **Action**:
  1. In the manual move box, enter `e2 e4 Q` (specifying a promotion piece on a move that does not reach the opposing 8th rank).
  2. Click **Submit Move**.
- **Expected GUI Behavior**:
  - An alert dialog pops up: `"Only pawns reaching the final rank can be promoted."`
  - The status bar displays in red: `[ERR_INVALID_PROMOTION] ...`
- **Log Entry**:
  - `game_errors.log` records an `InvalidPromotionException` entry.

---

### Scenario 12: GameOverException
- **Concept / Exception**: `chess.exceptions.GameOverException` (Error Code: `ERR_GAME_OVER`)
- **Action**:
  1. Execute Scholar's Mate via manual move input:
     - `e2 e4`
     - `e7 e5`
     - `f1 c4`
     - `b8 c6`
     - `d1 h5`
     - `g8 f6`
     - `h5 f7` (Checkmate delivered by White Queen!)
  2. Notice the status bar changes to: `Game Over - Checkmate! White wins.`
  3. Now try to click or type another move (e.g. `a7 a6`).
- **Expected GUI Behavior**:
  - Warning dialog: `"Cannot make move: game is already finished."`
  - Status bar displays: `[ERR_GAME_OVER] Game is over (Checkmate! White wins). No further moves are permitted.`
  - The game prevents any moves after completion.
- **Log Entry**:
  - `game_errors.log` records a `GameOverException` entry.

---

### Scenario 13: InvalidMoveFormatException
- **Concept / Exception**: `chess.exceptions.InvalidMoveFormatException` (Error Code: `ERR_INVALID_MOVE_FORMAT`)
- **Action**:
  1. In the manual move text box, type malformed text like `pawn move` or `e4` (only one square) or `xyz`.
  2. Press **Enter** or click **Submit Move**.
- **Expected GUI Behavior**:
  - A warning dialog pops up: `"Input must contain at least start and end squares (e.g., 'e2 e4')."`
  - Status bar shows in red: `[ERR_INVALID_MOVE_FORMAT] ...`
  - The text box retains focus, and the user can easily re-type.
- **Log Entry**:
  - `game_errors.log` records an `InvalidMoveFormatException` entry chained with the underlying `NoSuchElementException`.

---

### Scenario 14: CorruptedSaveFileException
- **Concept / Exception**: `chess.exceptions.CorruptedSaveFileException` (Error Code: `ERR_CORRUPTED_SAVE`)
- **Action**:
  1. Create a dummy text file named `corrupt.ser` containing random text (e.g. "hello world").
  2. In the application, click **Load Game**.
  3. Select `corrupt.ser` and click **Open**.
- **Expected GUI Behavior**:
  - An error dialog pops up titled **Corrupted Save File**:
    `"Corrupted save file:\nI/O failure while reading save file content.\nRoot Cause: StreamCorruptedException - invalid stream header: 68656C6C"`
  - The status bar displays in red: `[ERR_CORRUPTED_SAVE] ...`
  - The application remains completely stable and retains the current game in memory.
- **Log Entry**:
  - `game_errors.log` records a `CorruptedSaveFileException` entry with chained root cause `StreamCorruptedException`.

---

### Scenario 15: IllegalStateException (Undo on Empty History)
- **Concept / Exception**: `java.lang.IllegalStateException` (Error Code: `ERR_ILLEGAL_STATE`)
- **Action**:
  1. Click **New Game** (or start the application fresh).
  2. Before making any moves, immediately click **Undo Move**.
- **Expected GUI Behavior**:
  - An information dialog pops up: `"Cannot undo move: no previous moves recorded in history."`
  - The status bar displays in red: `[ERR_ILLEGAL_STATE] Cannot undo move: no previous moves recorded in history.`
  - No crash occurs; the game continues in its initial ready state.
- **Log Entry**:
  - `game_errors.log` records an `IllegalStateException` entry under context `UNDO_OPERATION_FAILED`.

