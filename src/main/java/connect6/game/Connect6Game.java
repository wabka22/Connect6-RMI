package connect6.game;

public class Connect6Game {
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private String winner;
    private int moveCount; // кол-во ходов (ход = смена очередности), нужен для первого хода
    private int stonesPlacedThisTurn;

    public Connect6Game() {
        board = new char[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        currentPlayer = GameConstants.BLACK_STONE;
        gameOver = false;
        moveCount = 0;
        stonesPlacedThisTurn = 0;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int r = 0; r < GameConstants.BOARD_SIZE; r++) {
            for (int c = 0; c < GameConstants.BOARD_SIZE; c++) {
                board[r][c] = GameConstants.EMPTY_CELL;
            }
        }
    }

    /**
     * Делает ход в клетку (x, y).
     * x - колонка (0..N-1), y - строка (0..N-1).
     */
    public synchronized boolean makeMove(int x, int y) {
        if (gameOver || !isValidPosition(x, y) || board[y][x] != GameConstants.EMPTY_CELL) {
            return false;
        }

        // Помещаем камень
        board[y][x] = currentPlayer;
        stonesPlacedThisTurn++;

        // Проверка победы после каждой установки
        if (checkWin(x, y)) {
            gameOver = true;
            winner = (currentPlayer == GameConstants.BLACK_STONE) ? "BLACK" : "WHITE";
            return true;
        }

        // Правила Connect6:
        // - Первый ход (moveCount == 0): первый игрок (BLACK) ставит 1 камень, затем ход переходит.
        // - В последующих ходах каждый игрок ставит по 2 камня и затем очередь меняется.
        if (moveCount == 0) {
            if (stonesPlacedThisTurn >= 1) {
                switchPlayer();
                stonesPlacedThisTurn = 0;
                moveCount++; // теперь обычный режим
            }
        } else {
            if (stonesPlacedThisTurn >= 2) {
                switchPlayer();
                stonesPlacedThisTurn = 0;
                moveCount++;
            }
        }

        return true;
    }

    private boolean checkWin(int x, int y) {
        char player = board[y][x];
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] d : directions) {
            int dx = d[0], dy = d[1];
            int count = 1;

            // в положительном направлении
            for (int step = 1; step < GameConstants.WIN_COUNT; step++) {
                int nx = x + dx * step;
                int ny = y + dy * step;
                if (isValidPosition(nx, ny) && board[ny][nx] == player) count++;
                else break;
            }

            // в отрицательном направлении
            for (int step = 1; step < GameConstants.WIN_COUNT; step++) {
                int nx = x - dx * step;
                int ny = y - dy * step;
                if (isValidPosition(nx, ny) && board[ny][nx] == player) count++;
                else break;
            }

            if (count >= GameConstants.WIN_COUNT) return true;
        }

        return false;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < GameConstants.BOARD_SIZE && y >= 0 && y < GameConstants.BOARD_SIZE;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == GameConstants.BLACK_STONE) ? GameConstants.WHITE_STONE : GameConstants.BLACK_STONE;
    }

    // Геттеры
    public synchronized char[][] getBoard() { return board; }
    public synchronized char getCurrentPlayer() { return currentPlayer; }
    public synchronized boolean isGameOver() { return gameOver; }
    public synchronized String getWinner() { return winner; }
    public int getBoardSize() { return GameConstants.BOARD_SIZE; }
    public synchronized int getStonesPlacedThisTurn() { return stonesPlacedThisTurn; }

    public synchronized void resetGame() {
        initializeBoard();
        currentPlayer = GameConstants.BLACK_STONE;
        gameOver = false;
        winner = null;
        moveCount = 0;
        stonesPlacedThisTurn = 0;
    }
}
