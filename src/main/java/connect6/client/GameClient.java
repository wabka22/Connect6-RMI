package connect6.client;

import connect6.network.MessageListener;
import connect6.network.NetworkClient;

import javax.swing.*;
import java.awt.*;

public class GameClient extends JFrame implements MessageListener {
    private NetworkClient networkClient;

    private JLabel statusLabel;
    private JLabel turnInfoLabel;
    private GameBoardPanel boardPanel;

    private String playerRole;
    private boolean myTurn = false;
    private boolean gameActive = false;

    public GameClient() {
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        setTitle("Connect6 Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        statusLabel = new JLabel("Connecting to server...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.NORTH);

        turnInfoLabel = new JLabel("Waiting for game to start...");
        turnInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(turnInfoLabel, BorderLayout.SOUTH);

        boardPanel = new GameBoardPanel(19);
        boardPanel.setClickListener(this::handleBoardClick);
        add(new JScrollPane(boardPanel), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void connectToServer() {
        networkClient = new NetworkClient(this);
        networkClient.connect(ClientConstants.SERVER_HOST, ClientConstants.SERVER_PORT);
    }

    private void handleBoardClick(int x, int y) {
        if (gameActive && myTurn) {
            networkClient.sendMessage("MOVE " + x + " " + y);
        }
    }

    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> processServerMessage(message));
    }

    @Override
    public void onConnectionStatusChanged(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                statusLabel.setText("Connected to server. Waiting for players...");
            } else {
                statusLabel.setText("Disconnected from server");
                gameActive = false;
                myTurn = false;
            }
        });
    }

    private void processServerMessage(String message) {
        if (message.startsWith("WELCOME")) {
            // Игрок подключен
        }
        else if (message.startsWith("ROLE")) {
            playerRole = message.split(" ")[1];
            statusLabel.setText("You are: " + playerRole);
        }
        else if (message.startsWith("GAME_START")) {
            gameActive = true;
            turnInfoLabel.setText("Game started!");
        }
        else if (message.startsWith("TURN")) {
            String turnPlayer = message.split(" ")[1];
            myTurn = turnPlayer.equals(playerRole);
            turnInfoLabel.setText(myTurn ? "Your turn! (" + playerRole + ")" : "Opponent's turn (" + turnPlayer + ")");
        }
        else if (message.startsWith("BOARD")) {
            updateBoard(message.substring(6));
        }
        else if (message.startsWith("GAME_OVER")) {
            gameActive = false;
            myTurn = false;
            String result = message.substring(10);
            if (result.equals("DISCONNECT")) {
                JOptionPane.showMessageDialog(this, "Opponent disconnected!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Game Over! Winner: " + result, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
            turnInfoLabel.setText("Game Over - Winner: " + result);
        }
        else if (message.startsWith("ERROR")) {
            JOptionPane.showMessageDialog(this, message.substring(6), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBoard(String boardState) {
        int boardSize = 19;
        char[][] newBoard = new char[boardSize][boardSize];
        int index = 0;

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                newBoard[i][j] = boardState.charAt(index++);
            }
        }

        boardPanel.setBoard(newBoard);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameClient().setVisible(true);
        });
    }
}