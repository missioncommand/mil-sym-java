/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;


import armyc2.c5isr.web.render.GeoPixelConversion;

/**
 *
 * 
 */
public class GeoPixelConversion3D
{
    public static double metersPerPixel(double scale)
    {
        return GeoPixelConversion.metersPerPixel(scale);
    }

    public static double lat2y(double latitude, double scale, double latOrigin, double metPerPix)
    {
	
	double latRem = Math.abs(latitude-latOrigin);
	double pixDis = 0;
	if(latRem > 0)
	{
	pixDis = (latRem*GeoPixelConversion.METERS_PER_DEG)/metPerPix;
		if(latitude > latOrigin)//was < M. Deutch 6-20-11
		{
                    pixDis = -pixDis;
		}
	}
	return pixDis;
    }

    public static double y2lat(double yPosition, double scale, double latOrigin, double metPerPix)
    {
       
	double latitude  = latOrigin;
	if(yPosition != 0)
	{
		latitude  = latOrigin - ((yPosition * metPerPix)/GeoPixelConversion.METERS_PER_DEG) ;//was + M. Deutch 6-18-11
	}
	return latitude;
    }

    public static double long2x(double longitude,double scale,double longOrigin,double latitude, double metPerPix)
    {
	
	double longRem = Math.abs(longitude-longOrigin);
	double metersPerDeg = GetMetersPerDegAtLat(latitude);
	double pixDis = 0;
	if(longRem > 0)
	{
            pixDis = (longRem*metersPerDeg)/metPerPix;
            if(longitude < longOrigin)
            {
                    pixDis = -pixDis;
            }
	}
	return pixDis;
    }

    public static double x2long(double xPosition,double scale,double longOrigin,double latitude, double metPerPix)
    {
	
	double metersPerDeg = GetMetersPerDegAtLat(latitude);
	double longitude  = longOrigin;
	if(xPosition != 0)
	{
            longitude  = longOrigin + ((xPosition * metPerPix)/metersPerDeg) ;
	}
	return longitude;
    }

    
    public static double Deg2Rad(double deg)
    {
		double conv_factor = (2.0 * Math.PI)/360.0;
		return(deg * conv_factor);
    }

    public static double GetMetersPerDegAtLat(double lat)
    {
        // Convert latitude to radians
        lat = Deg2Rad(lat);
        // Set up "Constants"
        double p1 = 111412.84;		// longitude calculation term 1

        double p2 = -93.5;			// longitude calculation term 2

        double p3 = 0.118;			// longitude calculation term 3

        // Calculate the length of a degree of longitude in meters at given latitude
        double longlen = (p1 * Math.cos(lat)) + (p2 * Math.cos(3 * lat)) + (p3 * Math.cos(5 * lat));

        return longlen;
    }

    
}
