package armyc2.c5isr.web.render;

import armyc2.c5isr.renderer.utilities.RendererSettings;

public class GeoPixelConversion {

    public static final double INCHES_PER_METER = 39.3700787;
    public static final double METERS_PER_DEG = 40075017d / 360d; // Earth's circumference in meters / 360 degrees

    public static double metersPerPixel(double scale) {
        double step1 = scale / RendererSettings.getInstance().getDeviceDPI();
        return step1 / INCHES_PER_METER;
    }

    public static double lat2y(double latitude, double scale, double latOrigin, double metPerPix) {

        double latRem = -(latitude - latOrigin);
        double pixDis = (latRem * METERS_PER_DEG) / metPerPix;
        return pixDis;
    }

    public static double y2lat(double yPosition, double scale, double latOrigin, double metPerPix) {

        double latitude = latOrigin - ((yPosition * metPerPix) / METERS_PER_DEG);        
        return latitude;
    }

    public static double long2x(double longitude, double scale, double longOrigin, double latitude, double metPerPix, boolean normalize) {
        
        double longRem = longitude-longOrigin;
        if(normalize)
        {
            if (longRem > 180) {
                longRem -= 360;
            }
            if (longRem < -180) {
                longRem += 360;
            }        
        }
        double metersPerDeg = GetMetersPerDegAtLat(latitude);
        double pixDis = (longRem * metersPerDeg) / metPerPix;
        return pixDis;
    }

    public static double x2long(double xPosition, double scale, double longOrigin, double latitude, double metPerPix) {
        
        double metersPerDeg = GetMetersPerDegAtLat(latitude);
        double longitude = longOrigin + ((xPosition * metPerPix) / metersPerDeg);

        if (longitude < -180) {
            longitude += 360;
        } else if (longitude > 180) {
            longitude -= 360;
        }

        return longitude;
    }

    public static double Deg2Rad(double deg) {
        double conv_factor = (2.0 * Math.PI) / 360.0;
        return (deg * conv_factor);
    }

    public static double GetMetersPerDegAtLat(double lat) {
        // Convert latitude to radians
        lat = Deg2Rad(lat);
        // Set up "Constants"
        double p1 = 111412.84; // longitude calculation term 1

        double p2 = -93.5; // longitude calculation term 2

        double p3 = 0.118; // longitude calculation term 3

        // Calculate the length of a degree of longitude in meters at given
        // latitude
        double longlen = (p1 * Math.cos(lat)) + (p2 * Math.cos(3 * lat)) + (p3 * Math.cos(5 * lat));

        return longlen;
    }
}
