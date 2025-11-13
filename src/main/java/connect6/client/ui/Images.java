package connect6.client.ui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Images {
  private static BufferedImage black, white, field;
  private static ImageIcon icon;

  public static void load() {
    black = loadImage("/images/black-stone.gif");
    white = loadImage("/images/white-stone.gif");
    icon = loadIcon("/images/icon.png");
  }

  private static BufferedImage loadImage(String path) {
    try {
      URL url = Images.class.getResource(path);
      if (url != null) {
        return ImageIO.read(url);
      } else {
        System.err.println("Image not found: " + path);
      }
    } catch (IOException e) {
      System.err.println("Error loading image: " + path);
      e.printStackTrace();
    }
    return null;
  }

  private static ImageIcon loadIcon(String path) {
    URL url = Images.class.getResource(path);
    if (url != null) {
      return new ImageIcon(url);
    } else {
      System.err.println("Icon not found: " + path);
    }
    return null;
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
