package connect6.client.ui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class StoneImages {
    private static BufferedImage black, white, field;

    public static void load() {
        try { field = ImageIO.read(new File("images/field.gif")); } catch (IOException ignored) {}
        try { black = ImageIO.read(new File("images/black-stone.gif")); } catch (IOException ignored) {}
        try { white = ImageIO.read(new File("images/white-stone.gif")); } catch (IOException ignored) {}
    }

    public static BufferedImage getBlack() { return black; }
    public static BufferedImage getWhite() { return white; }
    public static BufferedImage getField() { return field; }
}
