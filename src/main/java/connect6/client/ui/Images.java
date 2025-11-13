package connect6.client.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Images {
  private static BufferedImage black, white;
  private static ImageIcon icon;

  public static void load() {
    black = loadBufferedImage("/images/black-stone.gif");
    white = loadBufferedImage("/images/white-stone.gif");
    icon = loadImageIcon("/images/icon.png");
  }

  private static BufferedImage loadBufferedImage(String path) {
    URL url = Images.class.getResource(path);
    if (url == null) {
      System.err.println("Image not found: " + path);
      return null;
    }
    try {
      return ImageIO.read(url);
    } catch (IOException e) {
      System.err.println("Error loading image: " + path);
      return null;
    }
  }

  private static ImageIcon loadImageIcon(String path) {
    URL url = Images.class.getResource(path);
    if (url == null) {
      System.err.println("Icon not found: " + path);
      return null;
    }
    return new ImageIcon(url);
  }

  public static BufferedImage getBlack() {
    return black;
  }

  public static BufferedImage getWhite() {
    return white;
  }

  public static ImageIcon getIcon() {
    return icon;
  }
}
