package connect6.client.ui;

import connect6.client.GameClient;
import java.awt.*;
import javax.swing.*;

public class GameClientUI {
  public final JTextField nameField =
      new JTextField("Player" + (System.currentTimeMillis() % 1000), 15);
  public final JButton connectBtn = new JButton("Connect");
  public final JButton disconnectBtn = new JButton("Disconnect");
  public final JLabel statusLabel = new JLabel("Not connected");
  public final JLabel roleLabel = new JLabel("Role: -");
  public final JLabel turnLabel = new JLabel("Turn: -");
  public final JLabel scoreLabel = new JLabel();
  public final GameBoardPanel boardPanel = new GameBoardPanel(19);

  public JPanel createMainPanel(GameClient client) {
    JPanel root = new JPanel(new BorderLayout(8, 8));

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    top.add(new JLabel("Player Name:"));
    top.add(nameField);
    top.add(connectBtn);
    top.add(disconnectBtn);
    root.add(top, BorderLayout.NORTH);

    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    right.add(statusLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(roleLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(turnLabel);
    right.add(Box.createVerticalStrut(8));
    right.add(scoreLabel);

    root.add(right, BorderLayout.EAST);
    root.add(boardPanel, BorderLayout.CENTER);

    return root;
  }
}
