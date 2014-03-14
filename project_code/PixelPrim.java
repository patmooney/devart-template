public class PixelPrim {
    public int red,green,blue,alpha,x,y;
    public PixelPrim( int x, int y, int pixel ) {
        this.x = x;
        this.y = y;

        this.alpha = (pixel >> 24) & 0xff;
        this.red   = (pixel >> 16) & 0xff;
        this.green = (pixel >>  8) & 0xff;
        this.blue  = (pixel      ) & 0xff;
    }
    public PixelPrim( int x, int y, int red, int green, int blue, int alpha ) {
        this.x = x;
        this.y = y;

        this.alpha = alpha;
        this.red   = red;
        this.green = green;
        this.blue  = blue;
    }
}
