package armyc2.c5isr.renderer.utilities;
import java.awt.geom.Point2D;
import java.awt.Point;

/**
 * Interface for Point Conversion objects.  Recommend using the functions
 * that take and return Point2D objects.
 * 
 */
public interface IPointConversion {

//        public void UpdateExtents(int pixelWidth, int pixelHeight,
//                            double geoTop, double geoLeft,
//                            double geoBottom, double geoRight);

//    public Point2D.Double PixelsToGeo(Point pixel);

//    public Point GeoToPixels(Point2D.Double coord);

    public Point2D PixelsToGeo(Point2D pixel);

    public Point2D GeoToPixels(Point2D coord);

//    public int getPixelWidth();
//
//    public int getPixelHeight();
//    public double getUpperLat();
//
//    public double getLowerLat();
//
//    public double getLeftLon();
//
//    public double getRightLon();


}