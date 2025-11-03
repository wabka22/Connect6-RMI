package connect6.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameBoardPanel extends JPanel {
    private char[][] board;
    private int boardSize;
    private BoardClickListener clickListener;

    public interface BoardClickListener {
        void onBoardClick(int x, int y);
    }

    public GameBoardPanel(int boardSize) {
        this.boardSize = boardSize;
        this.board = new char[boardSize][boardSize];
        initializeBoard();

        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(220, 179, 92));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void initializeBoard() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = '.';
            }
        }
    }

    public void setBoard(char[][] newBoard) {
        this.board = newBoard;
        repaint();
    }

    public void setClickListener(BoardClickListener listener) {
        this.clickListener = listener;
    }

    private void handleClick(int x, int y) {
        int cellSize = Math.min(getWidth(), getHeight()) / boardSize;
        int boardX = x / cellSize;
        int boardY = y / cellSize;

        if (boardX < boardSize && boardY < boardSize && clickListener != null) {
            clickListener.onBoardClick(boardX, boardY);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawStones(g);
    }

    private void drawBoard(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / boardSize;

        g.setColor(Color.BLACK);

        for (int i = 0; i < boardSize; i++) {
            g.drawLine(cellSize / 2, i * cellSize + cellSize / 2,
                    (boardSize - 1) * cellSize + cellSize / 2, i * cellSize + cellSize / 2);
            g.drawLine(i * cellSize + cellSize / 2, cellSize / 2,
                    i * cellSize + cellSize / 2, (boardSize - 1) * cellSize + cellSize / 2);
        }

        if (boardSize == 19) {
            int[] points = {3, 9, 15};
            for (int x : points) {
                for (int y : points) {
                    g.fillOval(x * cellSize + cellSize / 2 - 3,
                            y * cellSize + cellSize / 2 - 3, 6, 6);
                }
            }
        }
    }

    private void drawStones(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / boardSize;

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] != '.') {
                    int stoneX = i * cellSize + cellSize / 2;
                    int stoneY = j * cellSize + cellSize / 2;
                    int stoneSize = cellSize - 6;

                    if (board[i][j] == 'B') {
                        g.setColor(Color.BLACK);
                        g.fillOval(stoneX - stoneSize / 2, stoneY - stoneSize / 2, stoneSize, stoneSize);
                    } else if (board[i][j] == 'W') {
                        g.setColor(Color.WHITE);
                        g.fillOval(stoneX - stoneSize / 2, stoneY - stoneSize / 2, stoneSize, stoneSize);
                        g.setColor(Color.GRAY);
                        g.drawOval(stoneX - stoneSize / 2, stoneY - stoneSize / 2, stoneSize, stoneSize);
                    }
                }
            }
        }
    }
}