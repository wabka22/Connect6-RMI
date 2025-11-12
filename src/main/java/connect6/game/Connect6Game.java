package connect6.game;

public class Connect6Game {
  private char[][] board;
  private PlayerType currentPlayer;
  private boolean gameOver;
  private String winner;
  private int stonesPlacedThisTurn;
  private boolean isFirstTurn;

  public Connect6Game() {
    board = new char[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
    resetGame();
  }

  private void initializeBoard() {
    for (int r = 0; r < GameConstants.BOARD_SIZE; r++) {
      for (int c = 0; c < GameConstants.BOARD_SIZE; c++) {
        board[r][c] = GameConstants.EMPTY_CELL;
      }
    }
  }

  public synchronized PlaceResult placeStone(int x, int y) {
    if (gameOver) return PlaceResult.GAME_OVER;
    if (!isValidPosition(x, y)) return PlaceResult.INVALID_POSITION;
    if (board[y][x] != GameConstants.EMPTY_CELL) return PlaceResult.CELL_OCCUPIED;

    board[y][x] =
        (currentPlayer == PlayerType.BLACK) ? GameConstants.BLACK_STONE : GameConstants.WHITE_STONE;
    stonesPlacedThisTurn++;

    if (checkWin(x, y)) {
      gameOver = true;
      winner = currentPlayer.name();
    }

    return PlaceResult.OK;
  }

  public synchronized boolean shouldSwitchPlayer() {
    int limit = isFirstTurn ? 1 : 2;
    return stonesPlacedThisTurn >= limit;
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
      int count = 1;
      int dx = d[0], dy = d[1];

      for (int i = 1; i < GameConstants.WIN_COUNT; i++) {
        int nx = x + dx * i, ny = y + dy * i;
        if (isValidPosition(nx, ny) && board[ny][nx] == stone) count++;
        else break;
      }
      for (int i = 1; i < GameConstants.WIN_COUNT; i++) {
        int nx = x - dx * i, ny = y - dy * i;
        if (isValidPosition(nx, ny) && board[ny][nx] == stone) count++;
        else break;
      }
      if (count >= GameConstants.WIN_COUNT) return true;
    }
    return false;
  }

  private boolean isValidPosition(int x, int y) {
    return x >= 0 && x < GameConstants.BOARD_SIZE && y >= 0 && y < GameConstants.BOARD_SIZE;
  }

  public synchronized char[][] getBoard() {
    int n = GameConstants.BOARD_SIZE;
    char[][] copy = new char[n][n];
    for (int i = 0; i < n; i++) {
      System.arraycopy(board[i], 0, copy[i], 0, n);
    }
    return copy;
  }

  public synchronized PlayerType getCurrentPlayer() {
    return currentPlayer;
  }

  public synchronized boolean isGameOver() {
    return gameOver;
  }

  public synchronized String getWinner() {
    return winner;
  }

  public synchronized int getStonesPlacedThisTurn() {
    return stonesPlacedThisTurn;
  }

  public synchronized void resetGame() {
    initializeBoard();
    currentPlayer = PlayerType.BLACK;
    gameOver = false;
    winner = null;
    stonesPlacedThisTurn = 0;
    isFirstTurn = true;
  }

  public int getBoardSize() {
    return GameConstants.BOARD_SIZE;
  }
}
