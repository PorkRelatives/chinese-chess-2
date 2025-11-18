import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The main container class for the Xiangqi Game Engine Logic.
 *
 * This program implements the core data structures and basic movement logic (Chariot and General)
 * for a Chinese Chess (Xiangqi) engine, following OOP principles.
 * The board is 10 rows (0-9) by 9 columns (0-8)
 */
public class Engine {

    // --- 1. Enumerations ---

    public enum Color {
        RED, BLACK;

        public Color opposite() {
            return this == RED ? BLACK : RED;
        }

        @Override
        public String toString() {
            return this == RED ? "R" : "B";
        }
    }

    public enum PieceType {
        GENERAL, ADVISOR, ELEPHANT, CHARIOT, HORSE, CANNON, SOLDIER;
    }

    // --- 2. Position (Immutable Value Object) ---

    /** Represents a single square (row, col) on the 10x9 board. */
    public static class Position {
        public final int row;
        public final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }

        @Override
        public String toString() {
            return "(" + row + ", " + col + ")";
        }
    }

    // --- 3. Move (Immutable Value Object) ---

    /** Represents a transition from a start Position to an end Position. */
    public static class Move {
        public final Position start;
        public final Position end;
        public final Piece piece;

        public Move(Position start, Position end, Piece piece) {
            this.start = start;
            this.end = end;
            this.piece = piece;
        }

        @Override
        public String toString() {
            return piece.getShortCode() + " from " + start + " to " + end;
        }
    }

    // --- 4. Abstract Piece Class (The Contract) ---

    public static abstract class Piece {
        public final Color color;
        public final PieceType type;

        public Piece(Color color, PieceType type) {
            this.color = color;
            this.type = type;
        }

        /**
         * Calculates all potential moves for this piece, ignoring the "check" rule.
         * This is the piece's pattern only.
         */
        public abstract List<Move> getPseudoLegalMoves(Board board, Position start);

        /** Returns a short code for board printing (e.g., 'RC' for Red Chariot) */
        public String getShortCode() {
            String typeCode = type.name().substring(0, 1);
            return color.toString() + typeCode;
        }
    }

    // --- 5. Concrete Piece Implementations ---

    /** Implements the movement logic for the CHRARIOT (Rook). */
    public static class ChariotPiece extends Piece {
        public ChariotPiece(Color color) {
            super(color, PieceType.CHARIOT);
        }

        @Override
        public List<Move> getPseudoLegalMoves(Board board, Position start) {
            List<Move> moves = new ArrayList<>();
            int[] directions = {-1, 1}; // Move in negative or positive directions

            // 1. Horizontal Moves (Columns)
            for (int dir : directions) {
                for (int c = start.col + dir; board.isValid(start.row, c); c += dir) {
                    Position end = new Position(start.row, c);
                    Piece target = board.getPiece(end);

                    if (target == null) {
                        moves.add(new Move(start, end, this)); // Empty square
                    } else {
                        if (target.color != this.color) {
                            moves.add(new Move(start, end, this)); // Capture
                        }
                        break; // Stop at the first piece encountered
                    }
                }
            }

            // 2. Vertical Moves (Rows)
            for (int dir : directions) {
                for (int r = start.row + dir; board.isValid(r, start.col); r += dir) {
                    Position end = new Position(r, start.col);
                    Piece target = board.getPiece(end);

                    if (target == null) {
                        moves.add(new Move(start, end, this)); // Empty square
                    } else {
                        if (target.color != this.color) {
                            moves.add(new Move(start, end, this)); // Capture
                        }
                        break; // Stop at the first piece encountered
                    }
                }
            }
            return moves;
        }
    }

    /** Implements the movement logic and PALACE constraint for the GENERAL (King). */
    public static class GeneralPiece extends Piece {
        public GeneralPiece(Color color) {
            super(color, PieceType.GENERAL);
        }

        /** Checks if a position is within the General's palace (Cols 3-5). */
        private boolean isInPalace(int row, int col) {
            if (col < 3 || col > 5) return false;
            // Red Palace: Rows 7, 8, 9
            if (color == Color.RED && row >= 7 && row <= 9) return true;
            // Black Palace: Rows 0, 1, 2
            if (color == Color.BLACK && row >= 0 && row <= 2) return true;
            return false;
        }

        @Override
        public List<Move> getPseudoLegalMoves(Board board, Position start) {
            List<Move> moves = new ArrayList<>();
            // General moves exactly one step in any cardinal direction
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

            for (int[] dir : directions) {
                int newRow = start.row + dir[0];
                int newCol = start.col + dir[1];

                if (board.isValid(newRow, newCol)) {
                    // Check Palace Constraint
                    if (isInPalace(newRow, newCol)) {
                        Position end = new Position(newRow, newCol);
                        Piece target = board.getPiece(end);

                        if (target == null || target.color != this.color) {
                            moves.add(new Move(start, end, this));
                        }
                    }
                }
            }
            return moves;
        }
    }

    // --- 6. Board (The State Container) ---

    public static class Board {
        public static final int ROWS = 10;
        public static final int COLS = 9;
        private final Piece[][] grid = new Piece[ROWS][COLS];
        private Color currentTurn = Color.RED;

        public Board() {
            initializeBoard();
        }

        /** Initial setup of a Xiangqi game. */
        private void initializeBoard() {
            // Place Generals
            grid[0][4] = new GeneralPiece(Color.BLACK);
            grid[9][4] = new GeneralPiece(Color.RED);

            // Place Chariots (Rooks) - Example pieces for demonstration
            grid[0][0] = new ChariotPiece(Color.BLACK);
            grid[0][8] = new ChariotPiece(Color.BLACK);
            grid[9][0] = new ChariotPiece(Color.RED);
            grid[9][8] = new ChariotPiece(Color.RED);

            // Initialize the rest of the board with nulls (empty) by default.
            // Other pieces (Horse, Cannon, etc.) would be placed here.
        }

        public boolean isValid(int row, int col) {
            return row >= 0 && row < ROWS && col >= 0 && col < COLS;
        }

        public Piece getPiece(Position p) {
            return getPiece(p.row, p.col);
        }

        public Piece getPiece(int row, int col) {
            if (!isValid(row, col)) return null;
            return grid[row][col];
        }

        /** Moves a piece from start to end, assuming the move is legal. */
        public void makeMove(Move move) {
            Piece piece = grid[move.start.row][move.start.col];
            if (piece == null || piece.color != currentTurn) {
                System.out.println("Invalid: Not your piece or square is empty.");
                return;
            }

            // Check if the move is in the list of pseudo-legal moves (simplified check for demo)
            List<Move> possibleMoves = piece.getPseudoLegalMoves(this, move.start);
            if (!possibleMoves.stream().anyMatch(m -> m.end.equals(move.end))) {
                System.out.println("Invalid move pattern for " + piece.type + ".");
                return;
            }

            // Apply move (Capture, then Move)
            grid[move.end.row][move.end.col] = piece;
            grid[move.start.row][move.start.col] = null;

            // Switch turn
            currentTurn = currentTurn.opposite();
            System.out.println(move.toString() + " executed.");
            System.out.println("It is now " + currentTurn + "'s turn.");
        }

        public void printBoard() {
            System.out.println("\n  -----------------------------------");
            System.out.println("  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |");
            System.out.println("  -----------------------------------");
            for (int r = 0; r < ROWS; r++) {
                System.out.printf("%d |", r);
                for (int c = 0; c < COLS; c++) {
                    Piece p = grid[r][c];
                    String cell = (p != null) ? p.getShortCode() : "  ";
                    System.out.print(" " + cell + " |");
                }
                if (r == 4) {
                    System.out.print(" <--- The River");
                }
                System.out.println();
                System.out.println("  -----------------------------------");
            }
            System.out.println("Current Turn: " + currentTurn);
        }
    }

    // --- 7. Main Engine Class ---

    public static void main(String[] args) {
        Board board = new Board();
        board.printBoard();

        System.out.println("\n--- DEMONSTRATION ---");

        // 1. Red General (RG) move attempt (Start: 9, 4)
        // Move 1: Legal move (9,4) to (8,4) - Vertical, within palace
        Move m1 = new Move(new Position(9, 4), new Position(8, 4), board.getPiece(9, 4));
        System.out.println("Attempting move 1: " + m1);
        board.makeMove(m1);
        board.printBoard();

        // 2. Black Chariot (BC) move attempt (Start: 0, 0)
        // Move 2: Legal move (0,0) to (0,4) - Horizontal slide, no block
        Move m2 = new Move(new Position(0, 0), new Position(0, 4), board.getPiece(0, 0));
        System.out.println("Attempting move 2: " + m2);
        board.makeMove(m2);
        board.printBoard();

        // 3. Red Chariot (RH) move attempt (Start: 9, 0)
        // Move 3: Legal move (9,0) to (9,1) - Horizontal slide, no block
        Move m3 = new Move(new Position(9, 0), new Position(9, 1), board.getPiece(9, 0));
        System.out.println("Attempting move 3: " + m3);
        board.makeMove(m3);
        board.printBoard();

        // 4. Black General (BG) move attempt (Start: 0, 4)
        // Move 4: Illegal move (0,4) to (3,4) - Outside palace
        Move m4 = new Move(new Position(0, 4), new Position(3, 4), board.getPiece(0, 4));
        System.out.println("Attempting move 4: " + m4);
        board.makeMove(m4);
    }
}
