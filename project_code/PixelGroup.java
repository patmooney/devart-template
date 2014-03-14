import java.util.ArrayList;

public class PixelGroup {
    public ArrayList<PixelPrim> pixels = new ArrayList<PixelPrim>();
    long totalDistance = 0;
    public double red, green, blue, alpha;
    public boolean isBackground = false;

    public double standardDeviation () {
        long variance = totalDistance / this.pixels.size();
        double sD = Math.pow( variance, 0.5 );
        return sD;
    }

    private int threshold = 50;

    public void randomizeColor() {
        this.red = (int)(Math.random() * 255);
        this.green = (int)(Math.random() * 255);
        this.blue = (int)(Math.random() * 255);
        this.alpha = 255;//(int)(Math.random() * 255);
    }

    public int intColour () {
        int colour = (((int)this.alpha & 0xFF) << 24) |
            (((int)this.red & 0xFF) << 16) |
            (((int)this.green & 0xFF) << 8) |
            (((int)this.blue & 0xFF) << 0);
        return colour;
    }
    public PixelGroup ( PixelPrim pixel, int threshold ){
        this.threshold = threshold;
        this.red = pixel.red;
        this.green = pixel.green;
        this.blue = pixel.blue;
        this.alpha = pixel.alpha;
        this.pixels.add( pixel );
    }
    public boolean isSimilar( PixelPrim pixel ){
        double dist = this._dist( pixel );
        if ( dist < this.threshold ){
            this.addPixel( pixel, dist );
            return true;
        }
        return false;
    }
    public void addPixel ( PixelPrim pixel, double dist ){
        this.red = ( ( this.red * this.pixels.size() ) + pixel.red ) / ( this.pixels.size() + 1 );
        this.green = ( ( this.green * this.pixels.size() ) + pixel.green ) / ( this.pixels.size() + 1 );
        this.blue = ( ( this.blue * this.pixels.size() ) + pixel.blue ) / ( this.pixels.size() + 1 );
        this.alpha = ( ( this.alpha * this.pixels.size() ) + pixel.alpha ) / ( this.pixels.size() + 1 );
        this.pixels.add( pixel );
        this.totalDistance += (int)Math.pow(dist,2);
    }
    private int _dist( PixelPrim pixel ) {
        double distance = Math.pow((
            Math.pow(((pixel.red * 1.0) - this.red),2) + 
            Math.pow(((pixel.green * 1.0) - this.green),2) +
            Math.pow(((pixel.blue * 1.0) - this.blue),2) +
            Math.pow(((pixel.alpha * 1.0) - this.alpha),2)),0.5);
        return (int) distance;
    }

    public String toString() {
        int r = (int) this.red, g = (int) this.green, b = (int) this.blue, a = (int) this.alpha;
        return r + "-" + g + "-" + b + "-" + a + ": " + this.pixels.size() + " - " + this.standardDeviation();
    }

}

