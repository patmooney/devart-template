import java.util.ArrayList;
import java.awt.image.BufferedImage;
public interface Painter {
    public void paintTile ( BufferedImage off_Image, ArrayList<PixelGroup> apgr, int xOffset, int yOffset );
}
