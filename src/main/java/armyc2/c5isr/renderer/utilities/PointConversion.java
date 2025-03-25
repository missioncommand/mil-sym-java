/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 *
 * 
 */
public class PointConversion implements IPointConversion {
    int _pixelWidth = 0;
    int _PixelHeight = 0;
    double _geoTop = 0;
    double _geoLeft = 0;
    double _geoBottom = 0;
    double _geoRight = 0;
    boolean _normalize = true;
    //pixels to geo
    //double _geoMultiplierX = 0;
    //double _geoMultiplierY = 0;
    //geo to pixels
    double _pixelMultiplierX = 0;
    double _pixelMultiplierY = 0;
    public void set_normalize(boolean value)
    {
        _normalize=value;
    }

    public PointConversion(int pixelWidth, int pixelHeight,
                           double geoTop, double geoLeft,
                           double geoBottom, double geoRight)
    {/*
            _pixelWidth = pixelWidth;
            _PixelHeight = pixelHeight;
            _geoTop = geoTop;
            _geoLeft = geoLeft;
            _geoBottom = geoBottom;
            _geoRight = geoRight;*/

        UpdateExtents(pixelWidth, pixelHeight, geoTop, geoLeft, geoBottom, geoRight);
    }

    public void UpdateExtents(int pixelWidth, int pixelHeight,
                              double geoTop, double geoLeft,
                              double geoBottom, double geoRight)
    {
        _pixelWidth = pixelWidth;
        _PixelHeight = pixelHeight;
        _geoTop = geoTop;
        _geoLeft = geoLeft;
        _geoBottom = geoBottom;
        _geoRight = geoRight;

        //_geoMultiplierX = ((double)_pixelWidth) / (_geoRight - _geoLeft) ;
        //_geoMultiplierY = ((double)_PixelHeight) / (_geoTop - _geoBottom) ;

        _pixelMultiplierX = (_geoRight - _geoLeft) / ((double)_pixelWidth) ;
        _pixelMultiplierY = (_geoTop - _geoBottom) / ((double)_PixelHeight) ;

        //diagnostic 12-18-12
        if(_geoRight-_geoLeft < -180)
        {
            _pixelMultiplierX = (_geoRight - _geoLeft + 360) / ((double)_pixelWidth) ;
        }
        if(_geoRight-_geoLeft > 180)
        {
            _pixelMultiplierX = (360 - (_geoRight - _geoLeft) ) / ((double)_pixelWidth) ;
        }
        //end section
        if(_geoTop < _geoBottom)
            _pixelMultiplierY = -Math.abs(_pixelMultiplierY);
        else
            _pixelMultiplierY = Math.abs(_pixelMultiplierY);

//            if(_geoRight < _geoLeft)
//                _pixelMultiplierX = -Math.abs(_pixelMultiplierX);
//            else
//                _pixelMultiplierX = Math.abs(_pixelMultiplierX);
        //end section
    }

    public Point2D.Double PixelsToGeo(Point pixel)
    {
        Point2D.Double coords = new Point2D.Double();

        coords.x = pixel.x * _pixelMultiplierX + _geoLeft; //xMultiplier;
        coords.y = _geoTop - (pixel.y * _pixelMultiplierY);

        //diagnostic 12-18-12
        if(coords.x > 180)
            coords.x -= 360;
        if(coords.x < -180)
            coords.x += 360;
        //end section

        return coords;
    }

    public Point GeoToPixels(Point2D.Double coord)
    {
        Point pixel = new Point();

        //double xMultiplier = _pixelMultiplierX;//(_geoRight - _geoLeft) / ((double)_pixelWidth) ;
        //double yMultiplier = _pixelMultiplierY;//(_geoTop - _geoBottom) / ((double)_PixelHeight) ;
        double temp;

        //temp = ((coord.x  - _geoLeft) / _pixelMultiplierX);//xMultiplier);
        double calcValue=coord.getX()  - _geoLeft;
        if(_normalize)
        {
            if(calcValue<-180)
                calcValue+=360;
            else if(calcValue>180)
                calcValue-=360;
        }
        temp = (calcValue / _pixelMultiplierX);//xMultiplier);

        pixel.x = (int)temp;

        temp = ((_geoTop - coord.y) / _pixelMultiplierY);//yMultiplier);
        pixel.y = (int)temp;

        return pixel;
    }

    public Point2D PixelsToGeo(Point2D pixel)
    {
        Point2D.Double coords = new Point2D.Double();

        coords.x = pixel.getX() * _pixelMultiplierX + _geoLeft; //xMultiplier;
        coords.y = _geoTop - (pixel.getY() * _pixelMultiplierY);

        //diagnostic 12-18-12
        if(coords.x < -180)
            coords.x += 360;
        if(coords.x > 180)
            coords.x -= 360;
        //end section

        return coords;
    }

    public Point2D GeoToPixels(Point2D coord)
    {
        Point2D.Double pixel = new Point2D.Double();

        //double xMultiplier = _pixelMultiplierX;//(_geoRight - _geoLeft) / ((double)_pixelWidth) ;
        //double yMultiplier = _pixelMultiplierY;//(_geoTop - _geoBottom) / ((double)_PixelHeight) ;
        double temp;
        //temp = ((coord.getX()  - _geoLeft) / _pixelMultiplierX);//xMultiplier);
        double calcValue=coord.getX()  - _geoLeft;
        if(_normalize)
        {
            if(calcValue<-180)
                calcValue+=360;
            else if(calcValue>180)
                calcValue-=360;
        }
        temp = (calcValue / _pixelMultiplierX);

        pixel.x = temp;

        temp = ((_geoTop - coord.getY()) / _pixelMultiplierY);//yMultiplier);
        pixel.y = temp;

        return pixel;
    }

    public int getPixelWidth()
    {
        return _pixelWidth;
    }

    public int getPixelHeight()
    {
        return _PixelHeight;
    }

    public double getUpperLat()
    {
        return _geoTop;
    }

    public double getLowerLat()
    {
        return _geoBottom;
    }

    public double getLeftLon()
    {
        return _geoLeft;
    }

    public double getRightLon()
    {
        return _geoRight;
    }

}
