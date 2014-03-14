import java.util.ArrayList;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.BufferedImage;

public class PainterDot implements Painter {
    private int w,h;
    public PainterDot ( int w, int h ){
        this.w = w;
        this.h = h;
    }
    public void paintTile ( BufferedImage off_Image, ArrayList<PixelGroup> apgr, int xOffset, int yOffset ){

        int radius = 5;
        int spacing = radius;

        BufferedImage solidImage = new BufferedImage(this.w, this.h, BufferedImage.TYPE_INT_ARGB);
        Painter p = new PainterNormal( w, h );
        p.paintTile( solidImage, apgr, 0, 0 );
        
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(solidImage, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            System.err.println("interrupted waiting for pixels!");
            return;
        }
        if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
            System.err.println("image fetch aborted or errored");
            return;
        }
        
//        off_Image.getGraphics().drawImage( solidImage, xOffset, yOffset, null );

        for ( int i = 0; i < w; i+= spacing ) {
            for ( int j = 0; j < h; j+= spacing ) {
                this.drawSpot( off_Image, i, j, pixels[j * w + i], radius, xOffset, yOffset ); 
            }
        }
    }
    private void drawSpot( BufferedImage bi, int sX, int sY, int pixel, int radius, int xOff, int yOff ){

        for ( int x = (int)(sX - ( radius / 2 ) ); x <= (int)(sX + ( radius / 2 ) ); x++ ){
            if ( x < 0 || x > this.w ) continue;
            for ( int y = (int)(sY - ( radius / 2 ) ); y <= (int)(sY + ( radius / 2 ) ); y++ ){
                if ( y < 0 || y > this.h ) continue;
                double dist = this._dist( x, y, sX, sY );
//                double pc = radius / dist; <-- makes a strange oreo cookie type pattern in the circle???
                double pc = 1;
                if ( dist < (( radius * 1.0 ) / 2) ){
                    try { 
                        bi.setRGB( xOff+x, yOff+y, pixel );
//                        bi.setRGB( xOff+x, yOff+y, this.alphaPixel( new PixelPrim( 0, 0, pixel ), (int)(255 * pc)  ));
                    } catch ( Exception e ){}
                }
            }
        }

    }
    private double _dist ( int x1, int y1, int x2, int y2 ){
        return Math.sqrt(( 
                    Math.pow( x1 - x2, 2 ) +
                    Math.pow( y1 - y2, 2 ) 
                ));
    }
    private int alphaPixel ( PixelPrim p, int alpha ) {
        int colour = ((alpha & 0xFF) << 24) |
            ((p.red & 0xFF) << 16) |
            ((p.green & 0xFF) << 8) |
            ((p.blue & 0xFF) << 0);
        return colour;
    }
}
