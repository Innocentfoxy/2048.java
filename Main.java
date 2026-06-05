import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    static final int SIZE = 4;
    // Board: board[row][col], row 0 = top, row 3 = bottom
    static int[][] board = new int[SIZE][SIZE];
    static Random rand = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        spawnTile();
        spawnTile();

        while (true) {
            printBoard();

            if (hasWon()) {
                System.out.println("Congrats! You reached 2048!");
                break;
            }
            if (!canMove()) {
                System.out.println("Game over! No moves left.");
                break;
            }

            System.out.print("Enter move (w/a/s/d): ");
            char input = scanner.next().toLowerCase().charAt(0);

            boolean moved = switch (input) {
                case 'w' -> slideUp();
                case 's' -> slideDown();
                case 'a' -> slideLeft();
                case 'd' -> slideRight();
                default  -> { System.out.println("Invalid key. Use w/a/s/d."); yield false; }
            };

            if (moved) {
                spawnTile();
            }
        }

        scanner.close();
    }

    // ─── Core slide logic ────────────────────────────────────────────────────

    /**
     * Slides and merges a single line (array of 4 values) toward index 0.
     * Works for left-slide; other directions rotate/flip the board before calling.
     */
    static int[] slideLine(int[] line) {
        // Step 1: compact — remove zeros, shift tiles to the left
        int[] compacted = new int[SIZE];
        int pos = 0;
        for (int val : line) {
            if (val != 0) compacted[pos++] = val;
        }

        // Step 2: merge adjacent equal tiles (left to right, each tile merges once)
        for (int i = 0; i < SIZE - 1; i++) {
            if (compacted[i] != 0 && compacted[i] == compacted[i + 1]) {
                compacted[i] *= 2;
                compacted[i + 1] = 0;
                i++; // skip merged tile so it isn't merged again
            }
        }

        // Step 3: compact again after merges
        int[] result = new int[SIZE];
        pos = 0;
        for (int val : compacted) {
            if (val != 0) result[pos++] = val;
        }
        return result;
    }

    // ─── Direction handlers ───────────────────────────────────────────────────

    static boolean slideLeft() {
        boolean moved = false;
        for (int row = 0; row < SIZE; row++) {
            int[] original = board[row].clone();
            board[row] = slideLine(board[row]);
            if (!linesEqual(original, board[row])) moved = true;
        }
        return moved;
    }

    static boolean slideRight() {
        boolean moved = false;
        for (int row = 0; row < SIZE; row++) {
            int[] original = board[row].clone();
            board[row] = reverse(slideLine(reverse(board[row])));
            if (!linesEqual(original, board[row])) moved = true;
        }
        return moved;
    }

    static boolean slideUp() {
        boolean moved = false;
        for (int col = 0; col < SIZE; col++) {
            int[] column   = getColumn(col);
            int[] original = column.clone();
            int[] slid     = slideLine(column);
            if (!linesEqual(original, slid)) moved = true;
            setColumn(col, slid);
        }
        return moved;
    }

    static boolean slideDown() {
        boolean moved = false;
        for (int col = 0; col < SIZE; col++) {
            int[] column   = getColumn(col);
            int[] original = column.clone();
            int[] slid     = reverse(slideLine(reverse(column)));
            if (!linesEqual(original, slid)) moved = true;
            setColumn(col, slid);
        }
        return moved;
    }

    // ─── Board utilities ─────────────────────────────────────────────────────

    static int[] getColumn(int col) {
        int[] column = new int[SIZE];
        for (int row = 0; row < SIZE; row++) column[row] = board[row][col];
        return column;
    }

    static void setColumn(int col, int[] values) {
        for (int row = 0; row < SIZE; row++) board[row][col] = values[row];
    }

    static int[] reverse(int[] line) {
        int[] reversed = new int[line.length];
        for (int i = 0; i < line.length; i++) reversed[i] = line[line.length - 1 - i];
        return reversed;
    }

    static boolean linesEqual(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    // ─── Tile spawning ────────────────────────────────────────────────────────

    static void spawnTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == 0) emptyCells.add(new int[]{r, c});

        if (emptyCells.isEmpty()) return;

        int[] cell = emptyCells.get(rand.nextInt(emptyCells.size()));
        // 90% chance of 2, 10% chance of 4 (classic 2048 rule)
        board[cell[0]][cell[1]] = (rand.nextInt(10) == 0) ? 4 : 2;
    }

    // ─── Win / lose checks ───────────────────────────────────────────────────

    static boolean hasWon() {
        for (int[] row : board)
            for (int val : row)
                if (val >= 2048) return true;
        return false;
    }

    static boolean canMove() {
        // Any empty cell → can move
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (board[r][c] == 0) return true;

        // Any adjacent equal pair → can merge
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (r + 1 < SIZE && board[r][c] == board[r + 1][c]) return true;
                if (c + 1 < SIZE && board[r][c] == board[r][c + 1]) return true;
            }
        }
        return false;
    }

    // ─── Display ─────────────────────────────────────────────────────────────

    static void printBoard() {
        System.out.println("┌──────┬──────┬──────┬──────┐");
        for (int r = 0; r < SIZE; r++) {
            System.out.print("│");
            for (int c = 0; c < SIZE; c++) {
                String cell = board[r][c] == 0 ? "  .   " : String.format(" %-4d ", board[r][c]);
                System.out.print(cell + "│");
            }
            System.out.println();
            if (r < SIZE - 1)
                System.out.println("├──────┼──────┼──────┼──────┤");
        }
        System.out.println("└──────┴──────┴──────┴──────┘");
    }
}