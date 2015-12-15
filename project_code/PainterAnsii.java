import java.util.ArrayList;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.awt.Color;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import java.io.File;

public class PainterAnsii implements Painter {
    private int w,h;
    HashMap<Integer,PixelPrim> ansiiMap;
    ArrayList<String> lines;

    public static void main ( String[] args ){

        String fn = "image.png";
        int resolution = 100;

        if ( args.length > 0 ){
            fn = args[0];
        }
        else {
            System.out.println( "\nUsage: java -jar ansii.jar <image.png/jpg> <resolution ( default 100 )>\n" );
            System.exit( 0 );
        }

        if ( args.length > 1 ){
            try {
                resolution = Integer.parseInt( args[1] );
            }
            catch ( Exception e ) {
                e.printStackTrace();
                System.exit( 2 );
            }
        }

        BufferedImage bi = null;
        try {
            bi = ImageIO.read( new File( fn ) );
            bi = resize( bi, 400 );
        }
        catch ( Exception e ){
            e.printStackTrace();
            System.exit(1);
        }

        PainterAnsii pa = new PainterAnsii( bi.getWidth(), bi.getHeight() );
        pa.toAnsii( bi, resolution );
    }

    public PainterAnsii ( int w, int h ){
        this.w = w;
        this.h = h;
        this.lines = new ArrayList<String>();
    }

    public void paintTile ( BufferedImage off_Image, ArrayList<PixelGroup> apgr, int xOffset, int yOffset ){
        BufferedImage solidImage = new BufferedImage(this.w, this.h, BufferedImage.TYPE_INT_ARGB);

        for ( PixelGroup pgr : apgr ){
            for ( PixelPrim pp : pgr.pixels ){
                int colour = pgr.intColour();
                solidImage.setRGB( xOffset+pp.x, yOffset+pp.y, colour );
            }
        }

        this.toAnsii( solidImage, 100 );
    }

    private void toAnsii( BufferedImage solidImage, int resolution ){

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

        try {
            int spacing = (int)(w / resolution);
            String previousColour = "";
            String line = "";
            for ( int j = 0; j < h; j+= (spacing) ) {
                for ( int i = 0; i < w; i+= (spacing) ) {
                    String newColour = this.ansiiColour( pixels[j * w + i] );
                    if ( newColour.compareTo( previousColour ) != 0 ){
                        line += newColour;
                        previousColour = newColour;
                    }
                    line += " ";
                }
                lines.add( line + "\033[0m" );
                previousColour = "";
                line = "";
            }

/*
            int minMargin = 0;
            for ( String l : lines ) {
                if ( l.length() < 4 ) { continue; }
                if ( l.substring(0,4).compareTo("\033[0m") != 0 ){
                    minMargin = 0; break;
                }
                int count = getMargin( l );
                if ( minMargin == 0 || count < minMargin ) {
                    minMargin = count;
                }
            }
*/
            PrintWriter writer = new PrintWriter( new OutputStreamWriter(System.out) );
            for ( String l : lines ) {
//              String noMargin = l.substring( minMargin );
                writer.println( l );
            }
            writer.close();
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
    }

    private int getMargin ( String s ){
        int counter = 0;
        for( int i=0; i<s.length(); i++ ) {
            if( s.charAt(i) == ' ' ) {
                counter++;
            }
            else {
                break;
            }
        }
        return counter;
    }

    private int alphaPixel ( PixelPrim p, int alpha ) {
        int colour = ((alpha & 0xFF) << 24) |
            ((p.red & 0xFF) << 16) |
            ((p.green & 0xFF) << 8) |
            ((p.blue & 0xFF) << 0);
        return colour;
    }
    private int[] toRGB( int rgb ) {
        int blue = rgb & 255;
        int green = ( rgb >> 8 ) & 255;
        int red = ( rgb >> 16 ) & 255;

        return new int[]{ red, green, blue };
    }

    public String ansiiColour ( int pixel ) {

        if ( pixel == 0 ){
            return "\033[0m";
        }

        if ( this.ansiiMap == null ) {
            this.ansiiMap = new HashMap<Integer,PixelPrim>() {{
                put(0, new PixelPrim( 0, 0, 0, 0, 0, 1 )); put(1, new PixelPrim( 0, 0, 128, 0, 0, 1 )); put(2, new PixelPrim( 0, 0, 0, 128, 0, 1 ));
                put(3, new PixelPrim( 0, 0, 128, 128, 0, 1 )); put(4, new PixelPrim( 0, 0, 0, 0, 128, 1 )); put(5, new PixelPrim( 0, 0, 128, 0, 128, 1 ));
                put(6, new PixelPrim( 0, 0, 0, 128, 128, 1 )); put(7, new PixelPrim( 0, 0, 192, 192, 192, 1 )); put(8, new PixelPrim( 0, 0, 128, 128, 128, 1 ));
                put(9, new PixelPrim( 0, 0, 255, 0, 0, 1 )); put(10, new PixelPrim( 0, 0, 0, 255, 0, 1 )); put(11, new PixelPrim( 0, 0, 255, 255, 0, 1 ));
                put(12, new PixelPrim( 0, 0, 0, 0, 255, 1 )); put(13, new PixelPrim( 0, 0, 255, 0, 255, 1 )); put(14, new PixelPrim( 0, 0, 0, 255, 255, 1 ));
                put(15, new PixelPrim( 0, 0, 255, 255, 255, 1 )); put(16, new PixelPrim( 0, 0, 0, 0, 0, 1 )); put(17, new PixelPrim( 0, 0, 0, 0, 95, 1 ));
                put(18, new PixelPrim( 0, 0, 0, 0, 135, 1 )); put(19, new PixelPrim( 0, 0, 0, 0, 175, 1 )); put(20, new PixelPrim( 0, 0, 0, 0, 215, 1 ));
                put(21, new PixelPrim( 0, 0, 0, 0, 255, 1 )); put(22, new PixelPrim( 0, 0, 0, 95, 0, 1 )); put(23, new PixelPrim( 0, 0, 0, 95, 95, 1 ));
                put(24, new PixelPrim( 0, 0, 0, 95, 135, 1 )); put(25, new PixelPrim( 0, 0, 0, 95, 175, 1 )); put(26, new PixelPrim( 0, 0, 0, 95, 215, 1 ));
                put(27, new PixelPrim( 0, 0, 0, 95, 255, 1 )); put(28, new PixelPrim( 0, 0, 0, 135, 0, 1 )); put(29, new PixelPrim( 0, 0, 0, 135, 95, 1 ));
                put(30, new PixelPrim( 0, 0, 0, 135, 135, 1 )); put(31, new PixelPrim( 0, 0, 0, 135, 175, 1 )); put(32, new PixelPrim( 0, 0, 0, 135, 215, 1 ));
                put(33, new PixelPrim( 0, 0, 0, 135, 255, 1 )); put(34, new PixelPrim( 0, 0, 0, 175, 0, 1 )); put(35, new PixelPrim( 0, 0, 0, 175, 95, 1 ));
                put(36, new PixelPrim( 0, 0, 0, 175, 135, 1 )); put(37, new PixelPrim( 0, 0, 0, 175, 175, 1 )); put(38, new PixelPrim( 0, 0, 0, 175, 215, 1 ));
                put(39, new PixelPrim( 0, 0, 0, 175, 255, 1 )); put(40, new PixelPrim( 0, 0, 0, 215, 0, 1 )); put(41, new PixelPrim( 0, 0, 0, 215, 95, 1 ));
                put(42, new PixelPrim( 0, 0, 0, 215, 135, 1 )); put(43, new PixelPrim( 0, 0, 0, 215, 175, 1 )); put(44, new PixelPrim( 0, 0, 0, 215, 215, 1 ));
                put(45, new PixelPrim( 0, 0, 0, 215, 255, 1 )); put(46, new PixelPrim( 0, 0, 0, 255, 0, 1 )); put(47, new PixelPrim( 0, 0, 0, 255, 95, 1 ));
                put(48, new PixelPrim( 0, 0, 0, 255, 135, 1 )); put(49, new PixelPrim( 0, 0, 0, 255, 175, 1 )); put(50, new PixelPrim( 0, 0, 0, 255, 215, 1 ));
                put(51, new PixelPrim( 0, 0, 0, 255, 255, 1 )); put(52, new PixelPrim( 0, 0, 95, 0, 0, 1 )); put(53, new PixelPrim( 0, 0, 95, 0, 95, 1 ));
                put(54, new PixelPrim( 0, 0, 95, 0, 135, 1 )); put(55, new PixelPrim( 0, 0, 95, 0, 175, 1 )); put(56, new PixelPrim( 0, 0, 95, 0, 215, 1 ));
                put(57, new PixelPrim( 0, 0, 95, 0, 255, 1 )); put(58, new PixelPrim( 0, 0, 95, 95, 0, 1 )); put(59, new PixelPrim( 0, 0, 95, 95, 95, 1 ));
                put(60, new PixelPrim( 0, 0, 95, 95, 135, 1 )); put(61, new PixelPrim( 0, 0, 95, 95, 175, 1 )); put(62, new PixelPrim( 0, 0, 95, 95, 215, 1 ));
                put(63, new PixelPrim( 0, 0, 95, 95, 255, 1 )); put(64, new PixelPrim( 0, 0, 95, 135, 0, 1 )); put(65, new PixelPrim( 0, 0, 95, 135, 95, 1 ));
                put(66, new PixelPrim( 0, 0, 95, 135, 135, 1 )); put(67, new PixelPrim( 0, 0, 95, 135, 175, 1 )); put(68, new PixelPrim( 0, 0, 95, 135, 215, 1 ));
                put(69, new PixelPrim( 0, 0, 95, 135, 255, 1 )); put(70, new PixelPrim( 0, 0, 95, 175, 0, 1 )); put(71, new PixelPrim( 0, 0, 95, 175, 95, 1 ));
                put(72, new PixelPrim( 0, 0, 95, 175, 135, 1 )); put(73, new PixelPrim( 0, 0, 95, 175, 175, 1 )); put(74, new PixelPrim( 0, 0, 95, 175, 215, 1 ));
                put(75, new PixelPrim( 0, 0, 95, 175, 255, 1 )); put(76, new PixelPrim( 0, 0, 95, 215, 0, 1 )); put(77, new PixelPrim( 0, 0, 95, 215, 95, 1 ));
                put(78, new PixelPrim( 0, 0, 95, 215, 135, 1 )); put(79, new PixelPrim( 0, 0, 95, 215, 175, 1 )); put(80, new PixelPrim( 0, 0, 95, 215, 215, 1 ));
                put(81, new PixelPrim( 0, 0, 95, 215, 255, 1 )); put(82, new PixelPrim( 0, 0, 95, 255, 0, 1 )); put(83, new PixelPrim( 0, 0, 95, 255, 95, 1 ));
                put(84, new PixelPrim( 0, 0, 95, 255, 135, 1 )); put(85, new PixelPrim( 0, 0, 95, 255, 175, 1 )); put(86, new PixelPrim( 0, 0, 95, 255, 215, 1 ));
                put(87, new PixelPrim( 0, 0, 95, 255, 255, 1 )); put(88, new PixelPrim( 0, 0, 135, 0, 0, 1 )); put(89, new PixelPrim( 0, 0, 135, 0, 95, 1 ));
                put(90, new PixelPrim( 0, 0, 135, 0, 135, 1 )); put(91, new PixelPrim( 0, 0, 135, 0, 175, 1 )); put(92, new PixelPrim( 0, 0, 135, 0, 215, 1 ));
                put(93, new PixelPrim( 0, 0, 135, 0, 255, 1 )); put(94, new PixelPrim( 0, 0, 135, 95, 0, 1 )); put(95, new PixelPrim( 0, 0, 135, 95, 95, 1 ));
                put(96, new PixelPrim( 0, 0, 135, 95, 135, 1 )); put(97, new PixelPrim( 0, 0, 135, 95, 175, 1 )); put(98, new PixelPrim( 0, 0, 135, 95, 215, 1 ));
                put(99, new PixelPrim( 0, 0, 135, 95, 255, 1 )); put(100, new PixelPrim( 0, 0, 135, 135, 0, 1 )); put(101, new PixelPrim( 0, 0, 135, 135, 95, 1 ));
                put(102, new PixelPrim( 0, 0, 135, 135, 135, 1 )); put(103, new PixelPrim( 0, 0, 135, 135, 175, 1 )); put(104, new PixelPrim( 0, 0, 135, 135, 215, 1 ));
                put(105, new PixelPrim( 0, 0, 135, 135, 255, 1 )); put(106, new PixelPrim( 0, 0, 135, 175, 0, 1 )); put(107, new PixelPrim( 0, 0, 135, 175, 95, 1 ));
                put(108, new PixelPrim( 0, 0, 135, 175, 135, 1 )); put(109, new PixelPrim( 0, 0, 135, 175, 175, 1 )); put(110, new PixelPrim( 0, 0, 135, 175, 215, 1 ));
                put(111, new PixelPrim( 0, 0, 135, 175, 255, 1 )); put(112, new PixelPrim( 0, 0, 135, 215, 0, 1 )); put(113, new PixelPrim( 0, 0, 135, 215, 95, 1 ));
                put(114, new PixelPrim( 0, 0, 135, 215, 135, 1 )); put(115, new PixelPrim( 0, 0, 135, 215, 175, 1 )); put(116, new PixelPrim( 0, 0, 135, 215, 215, 1 ));
                put(117, new PixelPrim( 0, 0, 135, 215, 255, 1 )); put(118, new PixelPrim( 0, 0, 135, 255, 0, 1 )); put(119, new PixelPrim( 0, 0, 135, 255, 95, 1 ));
                put(120, new PixelPrim( 0, 0, 135, 255, 135, 1 )); put(121, new PixelPrim( 0, 0, 135, 255, 175, 1 )); put(122, new PixelPrim( 0, 0, 135, 255, 215, 1 ));
                put(123, new PixelPrim( 0, 0, 135, 255, 255, 1 )); put(124, new PixelPrim( 0, 0, 175, 0, 0, 1 )); put(125, new PixelPrim( 0, 0, 175, 0, 95, 1 ));
                put(126, new PixelPrim( 0, 0, 175, 0, 135, 1 )); put(127, new PixelPrim( 0, 0, 175, 0, 175, 1 )); put(128, new PixelPrim( 0, 0, 175, 0, 215, 1 ));
                put(129, new PixelPrim( 0, 0, 175, 0, 255, 1 )); put(130, new PixelPrim( 0, 0, 175, 95, 0, 1 )); put(131, new PixelPrim( 0, 0, 175, 95, 95, 1 ));
                put(132, new PixelPrim( 0, 0, 175, 95, 135, 1 )); put(133, new PixelPrim( 0, 0, 175, 95, 175, 1 )); put(134, new PixelPrim( 0, 0, 175, 95, 215, 1 ));
                put(135, new PixelPrim( 0, 0, 175, 95, 255, 1 )); put(136, new PixelPrim( 0, 0, 175, 135, 0, 1 )); put(137, new PixelPrim( 0, 0, 175, 135, 95, 1 ));
                put(138, new PixelPrim( 0, 0, 175, 135, 135, 1 )); put(139, new PixelPrim( 0, 0, 175, 135, 175, 1 )); put(140, new PixelPrim( 0, 0, 175, 135, 215, 1 ));
                put(141, new PixelPrim( 0, 0, 175, 135, 255, 1 )); put(142, new PixelPrim( 0, 0, 175, 175, 0, 1 )); put(143, new PixelPrim( 0, 0, 175, 175, 95, 1 ));
                put(144, new PixelPrim( 0, 0, 175, 175, 135, 1 )); put(145, new PixelPrim( 0, 0, 175, 175, 175, 1 )); put(146, new PixelPrim( 0, 0, 175, 175, 215, 1 ));
                put(147, new PixelPrim( 0, 0, 175, 175, 255, 1 )); put(148, new PixelPrim( 0, 0, 175, 215, 0, 1 )); put(149, new PixelPrim( 0, 0, 175, 215, 95, 1 ));
                put(150, new PixelPrim( 0, 0, 175, 215, 135, 1 )); put(151, new PixelPrim( 0, 0, 175, 215, 175, 1 )); put(152, new PixelPrim( 0, 0, 175, 215, 215, 1 ));
                put(153, new PixelPrim( 0, 0, 175, 215, 255, 1 )); put(154, new PixelPrim( 0, 0, 175, 255, 0, 1 )); put(155, new PixelPrim( 0, 0, 175, 255, 95, 1 ));
                put(156, new PixelPrim( 0, 0, 175, 255, 135, 1 )); put(157, new PixelPrim( 0, 0, 175, 255, 175, 1 )); put(158, new PixelPrim( 0, 0, 175, 255, 215, 1 ));
                put(159, new PixelPrim( 0, 0, 175, 255, 255, 1 )); put(160, new PixelPrim( 0, 0, 215, 0, 0, 1 )); put(161, new PixelPrim( 0, 0, 215, 0, 95, 1 ));
                put(162, new PixelPrim( 0, 0, 215, 0, 135, 1 )); put(163, new PixelPrim( 0, 0, 215, 0, 175, 1 )); put(164, new PixelPrim( 0, 0, 215, 0, 215, 1 ));
                put(165, new PixelPrim( 0, 0, 215, 0, 255, 1 )); put(166, new PixelPrim( 0, 0, 215, 95, 0, 1 )); put(167, new PixelPrim( 0, 0, 215, 95, 95, 1 ));
                put(168, new PixelPrim( 0, 0, 215, 95, 135, 1 )); put(169, new PixelPrim( 0, 0, 215, 95, 175, 1 )); put(170, new PixelPrim( 0, 0, 215, 95, 215, 1 ));
                put(171, new PixelPrim( 0, 0, 215, 95, 255, 1 )); put(172, new PixelPrim( 0, 0, 215, 135, 0, 1 )); put(173, new PixelPrim( 0, 0, 215, 135, 95, 1 ));
                put(174, new PixelPrim( 0, 0, 215, 135, 135, 1 )); put(175, new PixelPrim( 0, 0, 215, 135, 175, 1 )); put(176, new PixelPrim( 0, 0, 215, 135, 215, 1 ));
                put(177, new PixelPrim( 0, 0, 215, 135, 255, 1 )); put(178, new PixelPrim( 0, 0, 215, 175, 0, 1 )); put(179, new PixelPrim( 0, 0, 215, 175, 95, 1 ));
                put(180, new PixelPrim( 0, 0, 215, 175, 135, 1 )); put(181, new PixelPrim( 0, 0, 215, 175, 175, 1 )); put(182, new PixelPrim( 0, 0, 215, 175, 215, 1 ));
                put(183, new PixelPrim( 0, 0, 215, 175, 255, 1 )); put(184, new PixelPrim( 0, 0, 215, 215, 0, 1 )); put(185, new PixelPrim( 0, 0, 215, 215, 95, 1 ));
                put(186, new PixelPrim( 0, 0, 215, 215, 135, 1 )); put(187, new PixelPrim( 0, 0, 215, 215, 175, 1 )); put(188, new PixelPrim( 0, 0, 215, 215, 215, 1 ));
                put(189, new PixelPrim( 0, 0, 215, 215, 255, 1 )); put(190, new PixelPrim( 0, 0, 215, 255, 0, 1 )); put(191, new PixelPrim( 0, 0, 215, 255, 95, 1 ));
                put(192, new PixelPrim( 0, 0, 215, 255, 135, 1 )); put(193, new PixelPrim( 0, 0, 215, 255, 175, 1 )); put(194, new PixelPrim( 0, 0, 215, 255, 215, 1 ));
                put(195, new PixelPrim( 0, 0, 215, 255, 255, 1 )); put(196, new PixelPrim( 0, 0, 255, 0, 0, 1 )); put(197, new PixelPrim( 0, 0, 255, 0, 95, 1 ));
                put(198, new PixelPrim( 0, 0, 255, 0, 135, 1 )); put(199, new PixelPrim( 0, 0, 255, 0, 175, 1 )); put(200, new PixelPrim( 0, 0, 255, 0, 215, 1 ));
                put(201, new PixelPrim( 0, 0, 255, 0, 255, 1 )); put(202, new PixelPrim( 0, 0, 255, 95, 0, 1 )); put(203, new PixelPrim( 0, 0, 255, 95, 95, 1 ));
                put(204, new PixelPrim( 0, 0, 255, 95, 135, 1 )); put(205, new PixelPrim( 0, 0, 255, 95, 175, 1 )); put(206, new PixelPrim( 0, 0, 255, 95, 215, 1 ));
                put(207, new PixelPrim( 0, 0, 255, 95, 255, 1 )); put(208, new PixelPrim( 0, 0, 255, 135, 0, 1 )); put(209, new PixelPrim( 0, 0, 255, 135, 95, 1 ));
                put(210, new PixelPrim( 0, 0, 255, 135, 135, 1 )); put(211, new PixelPrim( 0, 0, 255, 135, 175, 1 )); put(212, new PixelPrim( 0, 0, 255, 135, 215, 1 ));
                put(213, new PixelPrim( 0, 0, 255, 135, 255, 1 )); put(214, new PixelPrim( 0, 0, 255, 175, 0, 1 )); put(215, new PixelPrim( 0, 0, 255, 175, 95, 1 ));
                put(216, new PixelPrim( 0, 0, 255, 175, 135, 1 )); put(217, new PixelPrim( 0, 0, 255, 175, 175, 1 )); put(218, new PixelPrim( 0, 0, 255, 175, 215, 1 ));
                put(219, new PixelPrim( 0, 0, 255, 175, 255, 1 )); put(220, new PixelPrim( 0, 0, 255, 215, 0, 1 )); put(221, new PixelPrim( 0, 0, 255, 215, 95, 1 ));
                put(222, new PixelPrim( 0, 0, 255, 215, 135, 1 )); put(223, new PixelPrim( 0, 0, 255, 215, 175, 1 )); put(224, new PixelPrim( 0, 0, 255, 215, 215, 1 ));
                put(225, new PixelPrim( 0, 0, 255, 215, 255, 1 )); put(226, new PixelPrim( 0, 0, 255, 255, 0, 1 )); put(227, new PixelPrim( 0, 0, 255, 255, 95, 1 ));
                put(228, new PixelPrim( 0, 0, 255, 255, 135, 1 )); put(229, new PixelPrim( 0, 0, 255, 255, 175, 1 )); put(230, new PixelPrim( 0, 0, 255, 255, 215, 1 ));
                put(231, new PixelPrim( 0, 0, 255, 255, 255, 1 )); put(232, new PixelPrim( 0, 0, 8, 8, 8, 1 )); put(233, new PixelPrim( 0, 0, 18, 18, 18, 1 ));
                put(234, new PixelPrim( 0, 0, 28, 28, 28, 1 )); put(235, new PixelPrim( 0, 0, 38, 38, 38, 1 )); put(236, new PixelPrim( 0, 0, 48, 48, 48, 1 ));
                put(237, new PixelPrim( 0, 0, 58, 58, 58, 1 )); put(238, new PixelPrim( 0, 0, 68, 68, 68, 1 )); put(239, new PixelPrim( 0, 0, 78, 78, 78, 1 ));
                put(240, new PixelPrim( 0, 0, 88, 88, 88, 1 )); put(241, new PixelPrim( 0, 0, 98, 98, 98, 1 )); put(242, new PixelPrim( 0, 0, 108, 108, 108, 1 ));
                put(243, new PixelPrim( 0, 0, 118, 118, 118, 1 )); put(244, new PixelPrim( 0, 0, 128, 128, 128, 1 )); put(245, new PixelPrim( 0, 0, 138, 138, 138, 1 ));
                put(246, new PixelPrim( 0, 0, 148, 148, 148, 1 )); put(247, new PixelPrim( 0, 0, 158, 158, 158, 1 )); put(248, new PixelPrim( 0, 0, 168, 168, 168, 1 ));
                put(249, new PixelPrim( 0, 0, 178, 178, 178, 1 )); put(250, new PixelPrim( 0, 0, 188, 188, 188, 1 )); put(251, new PixelPrim( 0, 0, 198, 198, 198, 1 ));
                put(252, new PixelPrim( 0, 0, 208, 208, 208, 1 )); put(253, new PixelPrim( 0, 0, 218, 218, 218, 1 )); put(254, new PixelPrim( 0, 0, 228, 228, 228, 1 ));
                put(255, new PixelPrim( 0, 0, 238, 238, 238, 1 ));
            }};
        }

        Integer maxDist = null;
        int closestColour = 0;
        Iterator it = this.ansiiMap.entrySet().iterator();
        while (it.hasNext()) {
            @SuppressWarnings("unchecked")
            Map.Entry<Integer,PixelPrim> entry = (Map.Entry)it.next();
            int newDist = this.dist( pixel, entry.getValue() );
            if ( maxDist == null || newDist < maxDist ){
                maxDist = newDist;
                closestColour = entry.getKey();
            }
        }

        return String.format("\033[48;5;%dm\033[38;5;%dm", closestColour, closestColour);
    }

    private int dist( int pixel, PixelPrim pixelTwo ) {
        Color c = new Color(pixel);
        double distance = Math.pow((
            Math.pow(((pixelTwo.red * 1.0) - c.getRed()),2) +
            Math.pow(((pixelTwo.green * 1.0) - c.getGreen()),2) +
            Math.pow(((pixelTwo.blue * 1.0) - c.getBlue()),2) +
            Math.pow(((pixelTwo.alpha * 1.0) - 1),2)),0.5);
        return (int) distance;
    }

    public static BufferedImage resize(BufferedImage img, int newW) {
        double scale = (double)newW / (double)img.getWidth();
        int newH = (int) ((double) img.getHeight() * scale );

        newH *= 0.5;

        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

}
