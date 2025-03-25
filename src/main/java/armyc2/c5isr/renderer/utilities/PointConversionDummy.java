/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Makes no change to the passed points.  Useful for when the points
 * are already in pixels.
 * 
 */
public class PointConversionDummy implements IPointConversion {


    public PointConversionDummy()
    {
    }

    public Point2D.Double PixelsToGeo(Point pixel)
    {
        Point2D.Double coords = new Point2D.Double();

        coords.x = pixel.x;
        coords.y = pixel.y;

        return coords;
    }

    public Point GeoToPixels(Point2D.Double coord)
    {
        Point pixel = new Point();

        pixel.x = (int)coord.x;

        pixel.y = (int)coord.y;

        return pixel;
    }

    public Point2D PixelsToGeo(Point2D pixel)
    {
        Point2D.Double coords = new Point2D.Double();

        coords.x = pixel.getX();
        coords.y = pixel.getY();

        return coords;
    }

    public Point2D GeoToPixels(Point2D coord)
    {
        Point2D.Double pixel = new Point2D.Double();

        pixel.x = coord.getX();
        pixel.y = coord.getY();

        return pixel;
    }

}
