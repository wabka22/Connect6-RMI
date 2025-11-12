package connect6.client.ui;

import java.awt.*;

public class HoverHighlighter {
  public static void draw(Graphics2D g, int x, int y, int cellSize) {
    g.setColor(new Color(255, 255, 0, 64)); // прозрачный желтый
    g.fillRect(x, y, cellSize, cellSize);
    g.setColor(new Color(0, 0, 0, 64));
    g.drawRect(x, y, cellSize, cellSize);
  }
}
