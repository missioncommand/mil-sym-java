/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;


//import sec.web.renderer.GeoPixelConversion3D;
import java.awt.geom.Point2D;
import java.awt.Point;
/**
 *
 * 
 */
public class PointConverter3D implements IPointConversion
{
    private double _controlLat=0;
    private double _controlLong=0;
    private double _scale=0;
    private double _metersPerPixel=0;
    public PointConverter3D(double controlLong, double controlLat, double scale)
    {
        try
        {
            this._controlLat=controlLat;
            this._controlLong=controlLong;
            this._scale=scale;
            _metersPerPixel=GeoPixelConversion3D.metersPerPixel(scale);
        }
        catch(Error e)
        {
            throw e;
        }
    }
    public Point2D.Double PixelsToGeo(Point pixel)
    {
        Point2D.Double pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion3D.y2lat(pixel.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.x2long(pixel.getX(), _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
    }

    public Point GeoToPixels(Point2D.Double coord)
    {
        Point ptPixels=null;
        try
        {
            double y=GeoPixelConversion3D.lat2y(coord.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.long2x(coord.getX(), _scale, _controlLong, coord.getY(), _metersPerPixel);
            ptPixels=new Point();
            ptPixels.setLocation(x, y);
        }
        catch(Error e)
        {
            throw e;
        }
        return ptPixels;
    }

    public Point2D PixelsToGeo(Point2D pixel)
    {
        Point2D.Double pt2dGeo=null;
        try
        {
            double y=GeoPixelConversion3D.y2lat(pixel.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.x2long(pixel.getX(), _scale, _controlLong, y, _metersPerPixel);
            pt2dGeo=new Point2D.Double(x,y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2dGeo;
    }

    public Point2D GeoToPixels(Point2D coord)
    {
        Point2D.Double pt2DPixels=null;
        try
        {
            double y=GeoPixelConversion3D.lat2y(coord.getY(), _scale, _controlLat, _metersPerPixel);
            double x=GeoPixelConversion3D.long2x(coord.getX(), _scale, _controlLong, coord.getY(), _metersPerPixel);
            pt2DPixels=new Point2D.Double(x, y);
        }
        catch(Error e)
        {
            throw e;
        }
        return pt2DPixels;
    }
}
