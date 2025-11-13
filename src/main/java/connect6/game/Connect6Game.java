package connect6.game;

public class Connect6Game {
  private char[][] board;
  private PlayerType currentPlayer;
  private boolean gameOver;
  private String winner;
  private int stonesPlacedThisTurn;
  private boolean isFirstTurn;

  public Connect6Game() {
    board = new char[GameConfig.CFG.BOARD_SIZE][GameConfig.CFG.BOARD_SIZE];
    resetGame();
  }

  private void initializeBoard() {
    for (int r = 0; r < GameConfig.CFG.BOARD_SIZE; r++) {
      for (int c = 0; c < GameConfig.CFG.BOARD_SIZE; c++) {
        board[r][c] = GameConfig.CFG.EMPTY_CELL;
      }
    }
  }

  public synchronized PlaceResult placeStone(int x, int y) {
    if (gameOver) return PlaceResult.GAME_OVER;
    if (!isValidPosition(x, y)) return PlaceResult.INVALID_POSITION;
    if (board[y][x] != GameConfig.CFG.EMPTY_CELL) return PlaceResult.CELL_OCCUPIED;

    board[y][x] =
        (currentPlayer == PlayerType.BLACK)
            ? GameConfig.CFG.BLACK_STONE
            : GameConfig.CFG.WHITE_STONE;

    stonesPlacedThisTurn++;

    if (checkWin(x, y)) {
      gameOver = true;
      winner = currentPlayer.name();
    }

    return PlaceResult.OK;
  }

  public synchronized boolean shouldSwitchPlayer() {
    return stonesPlacedThisTurn >= (isFirstTurn ? 1 : 2);
  }

  public synchronized void switchPlayer() {
    currentPlayer = (currentPlayer == PlayerType.BLACK) ? PlayerType.WHITE : PlayerType.BLACK;
    stonesPlacedThisTurn = 0;
    isFirstTurn = false;
  }

  private boolean checkWin(int x, int y) {
    char stone = board[y][x];
    int[][] dirs = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};

    for (int[] d : dirs) {
      if (countInDirection(x, y, d[0], d[1], stone)
              + countInDirection(x, y, -d[0], -d[1], stone)
              - 1
          >= GameConfig.CFG.WIN_COUNT) {
        return true;
      }
    }
    return false;
  }

  private int countInDirection(int x, int y, int dx, int dy, char stone) {
    int count = 0;
    while (isValidPosition(x, y) && board[y][x] == stone) {
      count++;
      x += dx;
      y += dy;
    }
    return count;
  }

  private boolean isValidPosition(int x, int y) {
    return x >= 0 && x < GameConfig.CFG.BOARD_SIZE && y >= 0 && y < GameConfig.CFG.BOARD_SIZE;
  }

  public synchronized char[][] getBoard() {
    int n = GameConfig.CFG.BOARD_SIZE;
    char[][] copy = new char[n][n];
    for (int i = 0; i < n; i++) {
      System.arraycopy(board[i], 0, copy[i], 0, n);
    }
    return copy;
  }

  public synchronized boolean isGameOver() {
    return gameOver;
  }

  public synchronized String getWinner() {
    return winner;
  }

  public synchronized void resetGame() {
    initializeBoard();
    currentPlayer = PlayerType.BLACK;
    gameOver = false;
    winner = null;
    stonesPlacedThisTurn = 0;
    isFirstTurn = true;
  }
}
