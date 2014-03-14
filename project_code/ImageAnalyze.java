import javax.imageio.ImageIO;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class ImageAnalyze {
    private BufferedImage bi;
    private ArrayList<PixelGroup> pixelGroups = new ArrayList<PixelGroup>();

    private int threshold = 50;
    private Painter painter;

    private int tilesAcross = 2, tilesDown = 2;

    public static void main( String[] args ) {
        String fn = "image.png";
        int threshold = 50;
        if ( args.length > 0 ){
            fn = args[0];
        }
        if ( args.length > 1 ){
            threshold = Integer.parseInt( args[1] );
        }
        ImageAnalyze ia = new ImageAnalyze( fn, threshold );
        ia.analyze();
    }
    public ImageAnalyze ( String fn, int threshold ){
        this.threshold = threshold;
        try {
            this.bi = ImageIO.read( new File( fn ) );
        }
        catch ( Exception e ){
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void analyze(){
        this.handlepixels( this.bi, 0, 0, this.bi.getWidth(), this.bi.getHeight() );
    }
    public void handlesinglepixel(PixelPrim pixel) {

        for ( PixelGroup pg : this.pixelGroups ){
            if ( pg.isSimilar( pixel ) ){
                return;
            }
        }

        this.pixelGroups.add( new PixelGroup( pixel, this.threshold ) );
    }

    public void handlepixels(Image img, int x, int y, int w, int h) {
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
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
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                handlesinglepixel(new PixelPrim( x+i, y+j, pixels[j * w + i] ));
            }
        }

        BufferedImage off_Image = new BufferedImage(w*tilesAcross, h*tilesDown, BufferedImage.TYPE_INT_ARGB);

        Collections.sort( this.pixelGroups, new CustomComparator() );

        int sublist_topindex = 5;
        if ( this.pixelGroups.size() < 5 ){
            sublist_topindex = this.pixelGroups.size();
        }
        List<PixelGroup> pgBySize = this.pixelGroups.subList( 0, sublist_topindex );

        PixelGroup lowestsD = null;
        for ( PixelGroup pgr : pgBySize ) {
            System.out.println( pgr );
            if ( lowestsD == null || pgr.standardDeviation() < lowestsD.standardDeviation() ){
                lowestsD = pgr;
            }
        }
        System.out.println( "======================================\n" + lowestsD + "\n\n=============================" );
        lowestsD.isBackground = true;

        this.painter = new PainterDot( w, h );

        int xOffset=0,yOffset=0;
        for ( int tid = 0; tid < ( tilesAcross * tilesDown ); tid++ ){
            this.painter.paintTile( off_Image, this.pixelGroups, xOffset * w, yOffset * h );
            xOffset++;
            if ( xOffset >= tilesAcross ){
                xOffset = 0;
                yOffset++;
            }
        }

        try {
            // retrieve image
            File outputfile = new File("/tmp/patimageout.png");
            ImageIO.write(off_Image, "png", outputfile);
        } catch (Exception e) {
        }
        

    }


    public class CustomComparator implements Comparator<PixelGroup> {
        @Override
        public int compare(PixelGroup o1, PixelGroup o2) {
            return o2.pixels.size() - o1.pixels.size();
        }
    }
}
