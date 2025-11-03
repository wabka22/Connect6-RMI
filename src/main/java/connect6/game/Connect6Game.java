package connect6.game;

public class Connect6Game {
    private char[][] board;
    private char currentPlayer;
    private boolean gameOver;
    private String winner;
    private int moveCount;
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
        for (int i = 0; i < GameConstants.BOARD_SIZE; i++) {
            for (int j = 0; j < GameConstants.BOARD_SIZE; j++) {
                board[i][j] = GameConstants.EMPTY_CELL;
            }
        }
    }

    public boolean makeMove(int x, int y) {
        if (gameOver || !isValidPosition(x, y) || board[x][y] != GameConstants.EMPTY_CELL) {
            return false;
        }

        board[x][y] = currentPlayer;
        stonesPlacedThisTurn++;

        if (checkWin(x, y)) {
            gameOver = true;
            winner = (currentPlayer == GameConstants.BLACK_STONE) ? "BLACK" : "WHITE";
            return true;
        }

        // Правила Connect6
        if (moveCount == 0 && stonesPlacedThisTurn == 1) {
            switchPlayer();
            stonesPlacedThisTurn = 0;
            moveCount++;
        } else if (moveCount > 0 && stonesPlacedThisTurn == 2) {
            switchPlayer();
            stonesPlacedThisTurn = 0;
            moveCount++;
        }

        return true;
    }

    private boolean checkWin(int x, int y) {
        char player = board[x][y];
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;

            for (int i = 1; i < GameConstants.WIN_COUNT; i++) {
                int newX = x + dir[0] * i;
                int newY = y + dir[1] * i;
                if (isValidPosition(newX, newY) && board[newX][newY] == player) {
                    count++;
                } else {
                    break;
                }
            }

            for (int i = 1; i < GameConstants.WIN_COUNT; i++) {
                int newX = x - dir[0] * i;
                int newY = y - dir[1] * i;
                if (isValidPosition(newX, newY) && board[newX][newY] == player) {
                    count++;
                } else {
                    break;
                }
            }

            if (count >= GameConstants.WIN_COUNT) {
                return true;
            }
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
    public char[][] getBoard() { return board; }
    public char getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public int getBoardSize() { return GameConstants.BOARD_SIZE; }
    public int getStonesPlacedThisTurn() { return stonesPlacedThisTurn; }

    public void resetGame() {
        initializeBoard();
        currentPlayer = GameConstants.BLACK_STONE;
        gameOver = false;
        winner = null;
        moveCount = 0;
        stonesPlacedThisTurn = 0;
    }
}