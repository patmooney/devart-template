import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class PainterNormal implements Painter {
    private int w,h;
    public PainterNormal ( int w, int h ){
        this.w = w;
        this.h = h;
    }
    public void paintTile ( BufferedImage off_Image, ArrayList<PixelGroup> apgr, int xOffset, int yOffset ){
        for ( PixelGroup pgr : apgr ){
            pgr.randomizeColor();

            for ( PixelPrim pp : pgr.pixels ){
                int colour = pgr.intColour();
                off_Image.setRGB( xOffset+pp.x, yOffset+pp.y, colour );
            }
        }
    }
}
