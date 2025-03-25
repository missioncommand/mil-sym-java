/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.JavaTacticalRenderer;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.ref;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;

/**
 * Class to calculate the geodesic based shapes for the Fire Support Areas
 * 
 */
public final class mdlGeodesic {
    private static final String _className = "mdlGeodesic";
    private static final double sm_a	= 6378137;

    private static double DegToRad(double deg) {
        return deg / 180.0 * Math.PI;
    }

    private static double RadToDeg(double rad) {
        return rad / Math.PI * 180.0;
    }
/**
 * Returns the azimuth from true north between two points
 * @param c1
 * @param c2
 * @return the azimuth from c1 to c2
 */
    public static double GetAzimuth(POINT2 c1, 
            POINT2 c2) {//was private
        double theta = 0;
        try {
            double lat1 = DegToRad(c1.y);
            double lon1 = DegToRad(c1.x);
            double lat2 = DegToRad(c2.y);
            double lon2 = DegToRad(c2.x);
            //formula
            //θ = atan2( sin(Δlong).cos(lat2),
            //cos(lat1).sin(lat2) − sin(lat1).cos(lat2).cos(Δlong) )
            //var theta:Number = Math.atan2( Math.sin(lon2-lon1)*Math.cos(lat2),
            //Math.cos(lat1)*Math.sin(lat2) − Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1) );
            double y = Math.sin(lon2 - lon1);
            y *= Math.cos(lat2);
            double x = Math.cos(lat1);
            x *= Math.sin(lat2);
            double z = Math.sin(lat1);
            z *= Math.cos(lat2);
            z *= Math.cos(lon2 - lon1);
            x = x - z;
            theta = Math.atan2(y, x);
            theta = RadToDeg(theta);
        }
        catch (Exception exc) {
            //System.out.println(e.getMessage());
            //clsUtility.WriteFile("Error in mdlGeodesic.GetAzimuth");
               ErrorLogger.LogException(_className ,"GetAzimuth",
                    new RendererException("Failed inside GetAzimuth", exc));
        }
        return theta;//RadToDeg(k);
    }
    /**
     * Calculates the distance in meters between two geodesic points.
     * Also calculates the azimuth from c1 to c2 and from c2 to c1.
     *
     * @param c1 the first point
     * @param c2 the last point
     * @param a12 OUT - an object with a member to hold the calculated azimuth in degrees from c1 to c2
     * @param a21 OUT - an object with a member to hold the calculated azimuth in degrees from c2 to c1
     * @return the distance in meters between c1 and c2
     */
    public static double geodesic_distance(POINT2 c1,
            POINT2 c2,
            ref<double[]> a12,
            ref<double[]> a21) {
        double h = 0;
        try {
            //formula
            //R = earth’s radius (mean radius = 6,371km)
            //Δlat = lat2− lat1
            //Δlong = long2− long1
            //a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
            //c = 2.atan2(√a, √(1−a))
            //d = R.c
            if(a12 != null && a21 !=null)
            {
                a12.value = new double[1];
                a21.value = new double[1];
                //set the azimuth
                a12.value[0] = GetAzimuth(c1, c2);
                a21.value[0] = GetAzimuth(c2, c1);
            }
            //c1.x+=360;
            double dLat = DegToRad(c2.y - c1.y);
            double dLon = DegToRad(c2.x - c1.x);

            double b = 0, lat1 = 0, lat2 = 0, e = 0, f = 0, g = 0, k = 0;
            b = Math.sin(dLat / 2);
            lat1 = DegToRad(c1.y);
            lat2 = DegToRad(c2.y);
            e = Math.sin(dLon / 2);
            f = Math.cos(lat1);
            g = Math.cos(lat2);
            //uncomment this to test calculation
            //var a:Number = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(DegToRad(c1.y)) * Math.cos(DegToRad(c2.y)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double a = b * b + f * g * e * e;
            h = Math.sqrt(a);
            k = Math.sqrt(1 - a);
            h = 2 * Math.atan2(h, k);
        }
        catch (Exception exc) {
            //System.out.println(e.getMessage());
            //clsUtility.WriteFile("Error in mdlGeodesic.geodesic_distance");
               ErrorLogger.LogException(_className ,"geodesic_distance",
                    new RendererException("Failed inside geodesic_distance", exc));
        }
        return sm_a * h;
    }
    /**
     * Calculates a geodesic point and given distance and azimuth from the srating geodesic point
     *
     * @param start the starting point
     * @param distance the distance in meters
     * @param azimuth the azimuth or bearing in degrees
     *
     * @return the calculated point
     */
    public static POINT2 geodesic_coordinate(POINT2 start,
            double distance,
            double azimuth) {
        POINT2 pt = null;
        try
        {
        //formula
        //lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
        //lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))

        double a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0,
                j = 0, k = 0, l = 0, m = 0, n = 0, p = 0, q = 0;

        a = DegToRad(start.y);
        b = Math.cos(a);
        c = DegToRad(azimuth);
        d = Math.sin(a);
        e = Math.cos(distance / sm_a);
        f = Math.sin(distance / sm_a);
        g = Math.cos(c);
        //uncomment to test calculation
        //var lat2:Number = RadToDeg(Math.asin(Math.sin(DegToRad(start.y)) * Math.cos(DegToRad(distance / sm_a)) + Math.cos(DegToRad(start.y)) * Math.sin(DegToRad(distance / sm_a)) * Math.cos(DegToRad(azimuth))));
        //lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
        //var lat2:Number = RadToDeg(Math.asin(Math.sin(DegToRad(start.y)) * Math.cos(distance / sm_a) + Math.cos(DegToRad(start.y)) * Math.sin(distance / sm_a) * Math.cos(DegToRad(azimuth))));
        //double lat2 = RadToDeg(Math.asin(Math.sin(DegToRad(start.y)) * Math.cos(distance / sm_a) + Math.cos(DegToRad(start.y)) * Math.sin(distance / sm_a) * Math.cos(DegToRad(azimuth))));
        double lat = RadToDeg(Math.asin(d * e + b * f * g));
        h = Math.sin(c);
        k = Math.sin(h);
        l = Math.cos(a);
        m = DegToRad(lat);
        n = Math.sin(m);
        p = Math.atan2(h * f * b, e - d * n);
        //uncomment to test calculation
        //var lon2:Number = start.x + DegToRad(Math.atan2(Math.sin(DegToRad(azimuth)) * Math.sin(DegToRad(distance / sm_a)) * Math.cos(DegToRad(start.y)), Math.cos(DegToRad(distance / sm_a)) - Math.sin(DegToRad(start.y)) * Math.sin(DegToRad(lat))));
        //lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))
        //var lon2:Number = start.x + RadToDeg(Math.atan2(Math.sin(DegToRad(azimuth)) * Math.sin(distance / sm_a) * Math.cos(DegToRad(start.y)), Math.cos(distance / sm_a) - Math.sin(DegToRad(start.y)) * Math.sin(DegToRad(lat2))));
        double lon = start.x + RadToDeg(p);
        pt = new POINT2(lon, lat);
        }
        catch (Exception exc) {
            //clsUtility.WriteFile("Error in mdlGeodesic.geodesic_distance");
               ErrorLogger.LogException(_className ,"geodesic_coordinate",
                    new RendererException("Failed inside geodesic_coordinate", exc));
        }
        return pt;
    }
    /**
     * Calculates an arc from geodesic point and uses them for the change 1 circular symbols
     *
     * @param pPoints array of 3 points, currently the last 2 points are the same. The first point
     * is the center and the next point defines the radius.
     *
     * @return points for the geodesic circle
     */
    public static ArrayList<POINT2> GetGeodesicArc(POINT2[] pPoints) {
        ArrayList<POINT2> pPoints2 = new ArrayList();
        try {
            if (pPoints == null) {
                return null;
            }
            if (pPoints.length < 3) {
                return null;
            }

            POINT2 ptCenter = new POINT2(pPoints[0]);
            POINT2 pt1 = new POINT2(pPoints[1]);
            POINT2 pt2 = new POINT2(pPoints[2]);
            POINT2 ptTemp = null;
            ref<double[]> a12b = new ref();
            double dist2 = 0.0;
            double dist1 = 0.0;
            ref<double[]> a12 = new ref();
            ref<double[]> a21 = new ref();
            //distance and azimuth from the center to the 1st point
            dist1 = geodesic_distance(ptCenter, pt1, a12, a21);
            double saveAzimuth = a21.value[0];
            //distance and azimuth from the center to the 2nd point
            dist2 = geodesic_distance(ptCenter, pt2, a12b, a21);
            //if the points are nearly the same we want 360 degree range fan
            if (Math.abs(a21.value[0] - saveAzimuth) <= 1) {
                if (a12.value[0] < 360) {
                    a12.value[0] += 360;
                }

                a12b.value[0] = a12.value[0] + 360;
            }

            ref<double[]> a12c = new ref();
            int j = 0;
            if (a12b.value[0] < 0) {
                a12b.value[0] = 360 + a12b.value[0];
            }
            if (a12.value[0] < 0) {
                a12.value[0] = 360 + a12.value[0];
            }
            if (a12b.value[0] < a12.value[0]) {
                a12b.value[0] = a12b.value[0] + 360;
            }
            a12c.value=new double[1];
            for (j = 0; j <= 100; j++) {

                a12c.value[0] = a12.value[0] + ((double) j / 100.0) * (a12b.value[0] - a12.value[0]);
                ptTemp = geodesic_coordinate(ptCenter, dist1, a12c.value[0]);
                pPoints2.add(ptTemp);
            }

            //if the points are nearly the same we want 360 degree range fan
            //with no line from the center
            if (Math.abs(a21.value[0] - saveAzimuth) > 1) {
                pPoints2.add(ptCenter);
            }

            if (a12.value[0] < a12b.value[0]) {
                pPoints2.add(pt1);
            } else {
                pPoints2.add(pt2);
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in mdlGeodesic.GetGeodesicArc");
               ErrorLogger.LogException(_className ,"GetGeodesicArc",
                    new RendererException("Failed inside GetGeodesicArc", exc));
        }
        return pPoints2;
    }
    /**
     * Calculates the sector points for a sector range fan.
     *
     * @param pPoints array of 3 points. The first point
     * is the center and the next two points define either side of the sector
     * @param pPoints2 OUT - the calculated geodesic sector points
     *
     * @return true if the sector is a circle
     */
    public static boolean GetGeodesicArc2(ArrayList<POINT2> pPoints,
            ArrayList<POINT2> pPoints2) {
        boolean circle = false;
        try {
            POINT2 ptCenter = new POINT2(pPoints.get(0)), pt1 = new POINT2(pPoints.get(1)), pt2 = new POINT2(pPoints.get(2));

            ref<double[]> a12b = new ref();
            //double dist2 = 0d;
            double dist1 = 0d;
            ref<double[]> a12 = new ref();
            ref<double[]> a21 = new ref();
            //double lat2c = 0.0;
            //distance and azimuth from the center to the 1st point
            //geodesic_distance(lonCenter, latCenter, lon1, lat1, ref dist1, ref a12, ref a21);
            dist1 = geodesic_distance(ptCenter, pt1, a12, a21);
            double saveAzimuth = a21.value[0];
            //distance and azimuth from the center to the 2nd point
            //geodesic_distance(lonCenter, latCenter, lon2, lat2, ref dist2, ref a12b, ref a21);
            double dist2 = geodesic_distance(ptCenter, pt2, a12b, a21);
            //if the points are nearly the same we want 360 degree range fan
            if (Math.abs(a21.value[0] - saveAzimuth) <= 1) {
                if (a12.value[0] < 360) {
                    a12.value[0] += 360;
                }
                a12b.value[0] = a12.value[0] + 360;
                circle = true;
            }

            //assume caller has set pPoints2 as new Array

            ref<double[]> a12c = new ref();
            a12c.value = new double[1];
            int j = 0;
            POINT2 pPoint = new POINT2();
            if (a12b.value[0] < 0) {
                a12b.value[0] = 360 + a12b.value[0];
            }
            if (a12.value[0] < 0) {
                a12.value[0] = 360 + a12.value[0];
            }
            if (a12b.value[0] < a12.value[0]) {
                a12b.value[0] = a12b.value[0] + 360;
            }
            for (j = 0; j <= 100; j++) {

                a12c.value[0] = a12.value[0] + ((double) j / 100) * (a12b.value[0] - a12.value[0]);
                pPoint = geodesic_coordinate(ptCenter, dist1, a12c.value[0]);
                pPoints2.add(pPoint);
            }
        }
        catch (Exception exc) {
            //System.out.println(e.getMessage());
            //clsUtility.WriteFile("Error in mdlGeodesic.GetGeodesicArc2");
               ErrorLogger.LogException(_className ,"GetGeodesicArc2",
                    new RendererException("Failed inside GetGeodesicArc2", exc));
        }
        return circle;
    }
    /**
     * returns intersection of two lines, each defined by a point and a bearing
     * <a href="http://creativecommons.org/licenses/by/3.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by/3.0/88x31.png"></a><br>This work is licensed under a <a href="http://creativecommons.org/licenses/by/3.0/">Creative Commons Attribution 3.0 Unported License</a>.
     * @param p1 1st point
     * @param brng1 first line bearing in degrees from true north
     * @param p2 2nd point
     * @param brng2 2nd point bearing in degrees from true north
     * @return
     * @deprecated
     */
    public static POINT2 IntersectLines(POINT2 p1,
            double brng1, 
            POINT2 p2,
            double brng2) {
        POINT2 ptResult = null;
        try {
            double lat1 = DegToRad(p1.y);//p1._lat.toRad();
            double lon1 = DegToRad(p1.x);//p1._lon.toRad();
            double lat2 = DegToRad(p2.y);//p2._lat.toRad();
            double lon2 = DegToRad(p2.x);//p2._lon.toRad();
            double brng13 = DegToRad(brng1);//brng1.toRad();
            double brng23 = DegToRad(brng2);//brng2.toRad();
            double dLat = lat2 - lat1;
            double dLon = lon2 - lon1;


            double dist12 = 2 * Math.asin(Math.sqrt(Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)));

            if (dist12 == 0) {
                return null;
            }

            double brngA = Math.acos((Math.sin(lat2) - Math.sin(lat1) * Math.cos(dist12)) /
                    (Math.sin(dist12) * Math.cos(lat1)));

            if (Double.isNaN(brngA)) {
                brngA = 0;  // protect against rounding
            }
            double brngB = Math.acos((Math.sin(lat1) - Math.sin(lat2) * Math.cos(dist12)) /
                    (Math.sin(dist12) * Math.cos(lat2)));

            double brng12 = 0, brng21 = 0;
            if (Math.sin(lon2 - lon1) > 0) {
                brng12 = brngA;
                brng21 = 2 * Math.PI - brngB;
            } else {
                brng12 = 2 * Math.PI - brngA;
                brng21 = brngB;
            }

            double alpha1 = (brng13 - brng12 + Math.PI) % (2 * Math.PI) - Math.PI;  // angle 2-1-3
            double alpha2 = (brng21 - brng23 + Math.PI) % (2 * Math.PI) - Math.PI;  // angle 1-2-3

            if (Math.sin(alpha1) == 0 && Math.sin(alpha2) == 0) {
                return null;  // infinite intersections
            }
            if (Math.sin(alpha1) * Math.sin(alpha2) < 0) {
                return null;       // ambiguous intersection
            }
            //alpha1 = Math.abs(alpha1);
            //alpha2 = Math.abs(alpha2);  // ... Ed Williams takes abs of alpha1/alpha2, but seems to break calculation?
            double alpha3 = Math.acos(-Math.cos(alpha1) * Math.cos(alpha2) +
                    Math.sin(alpha1) * Math.sin(alpha2) * Math.cos(dist12));

            double dist13 = Math.atan2(Math.sin(dist12) * Math.sin(alpha1) * Math.sin(alpha2),
                    Math.cos(alpha2) + Math.cos(alpha1) * Math.cos(alpha3));

            double lat3 = Math.asin(Math.sin(lat1) * Math.cos(dist13) +
                    Math.cos(lat1) * Math.sin(dist13) * Math.cos(brng13));
            double dLon13 = Math.atan2(Math.sin(brng13) * Math.sin(dist13) * Math.cos(lat1),
                    Math.cos(dist13) - Math.sin(lat1) * Math.sin(lat3));
            double lon3 = lon1 + dLon13;
            lon3 = (lon3 + Math.PI) % (2 * Math.PI) - Math.PI;  // normalise to -180..180º

            //return new POINT2(lat3.toDeg(), lon3.toDeg());
            ptResult = new POINT2(RadToDeg(lon3), RadToDeg(lat3));

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "IntersectLines",
                    new RendererException("Failed inside IntersectLines", exc));
        }
        return ptResult;
    }
    /**
     * Normalizes geo points for arrays which span the IDL
     *
     * @param geoPoints
     * @return
     */
    public static ArrayList<POINT2> normalize_points(ArrayList<POINT2> geoPoints) {
        ArrayList<POINT2> normalizedPts = null;
        try {
            if (geoPoints == null || geoPoints.isEmpty()) {
                return normalizedPts;
            }

            int j = 0;
            double minx = geoPoints.get(0).x;
            double maxx = minx;
            boolean spansIDL = false;
            POINT2 pt = null;
            int n=geoPoints.size();
            //for (j = 1; j < geoPoints.size(); j++) 
            for (j = 1; j < n; j++) 
            {
                pt = geoPoints.get(j);
                if (pt.x < minx) {
                    minx = pt.x;
                }
                if (pt.x > maxx) {
                    maxx = pt.x;
                }
            }
            if (maxx - minx > 180) {
                spansIDL = true;
            }

            if (!spansIDL) {
                return geoPoints;
            }

            normalizedPts = new ArrayList();
            n=geoPoints.size();
            //for (j = 0; j < geoPoints.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                pt = geoPoints.get(j);
                if (pt.x < 0) {
                    pt.x += 360;
                }
                normalizedPts.add(pt);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "normalize_pts",
                    new RendererException("Failed inside normalize_pts", exc));
        }
        return normalizedPts;
    }

    /**
     * calculates the geodesic MBR, intended for regular shaped areas
     *
     * @param geoPoints
     * @return
     */
    public static Rectangle2D.Double geodesic_mbr(ArrayList<POINT2> geoPoints) {
        Rectangle2D.Double rect2d = null;
        try {
            if (geoPoints == null || geoPoints.isEmpty()) {
                return rect2d;
            }
            
            ArrayList<POINT2>normalizedPts=normalize_points(geoPoints);
            double ulx=normalizedPts.get(0).x;
            double lrx=ulx;
            double uly=normalizedPts.get(0).y;
            double lry=uly;
            int j=0;
            POINT2 pt=null;
            int n=normalizedPts.size();
            //for(j=1;j<normalizedPts.size();j++)
            for(j=1;j<n;j++)
            {
                pt=normalizedPts.get(j);
                if(pt.x<ulx)
                    ulx=pt.x;
                if(pt.x>lrx)
                    lrx=pt.x;
            
                if(pt.y>uly)
                    uly=pt.y;
                if(pt.y<lry)
                    lry=pt.y;
            }
            POINT2 ul=new POINT2(ulx,uly);
            POINT2 ur=new POINT2(lrx,uly);
            POINT2 lr=new POINT2(lrx,lry);
            double width=geodesic_distance(ul,ur,null,null);
            double height=geodesic_distance(ur,lr,null,null);
            rect2d=new Rectangle2D.Double(ulx,uly,width,height);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "geodesic_mbr",
                    new RendererException("Failed inside geodesic_mbr", exc));
        }
        return rect2d;
    }

    /**
     * Currently used by AddModifiers for greater accuracy on center labels
     *
     * @param geoPoints
     * @return
     */
    public static POINT2 geodesic_center(ArrayList<POINT2> geoPoints) {
        POINT2 pt = null;
        try {
            if(geoPoints==null || geoPoints.isEmpty())
                return pt;
            
            Rectangle2D.Double rect2d=geodesic_mbr(geoPoints);
            double deltax=rect2d.getWidth()/2;
            double deltay=rect2d.getHeight()/2;
            POINT2 ul=new POINT2(rect2d.x,rect2d.y);
            //first walk east by deltax
            POINT2 ptEast=geodesic_coordinate(ul,deltax,90);
            //next walk south by deltay;
            pt=geodesic_coordinate(ptEast,deltay,180);
            
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "geodesic_center",
                    new RendererException("Failed inside geodesic_center", exc));
        }
        return pt;
    }
    /**
     * rotates a point from a center point in degrees
     * @param ptCenter center point to rotate about
     * @param ptRotate point to rotate
     * @param rotation rotation angle in degrees
     * @return 
     */
    private static POINT2 geoRotatePoint(POINT2 ptCenter, POINT2 ptRotate, double rotation)
    {
        try
        {
            double bearing=GetAzimuth(ptCenter, ptRotate);
            double dist=geodesic_distance(ptCenter,ptRotate,null,null);
            return geodesic_coordinate(ptCenter,dist,bearing+rotation);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "geoRotatePoint",
                    new RendererException("Failed inside geoRotatePoint", exc));
        }
        return null;
    }
    /**
     * Calculates points for a geodesic ellipse and rotates the points by rotation
     * @param ptCenter
     * @param majorRadius
     * @param minorRadius
     * @param rotation  rotation angle in degrees
     * @return 
     */
    public static POINT2[] getGeoEllipse(POINT2 ptCenter, double majorRadius, double minorRadius, double rotation)
    {        
        POINT2[]pEllipsePoints=null;
        try
        {
            pEllipsePoints=new POINT2[37];
            //int l=0;
            POINT2 pt=null;            
            double dFactor, azimuth=0,a=0,b=0,dist=0,bearing=0;
            POINT2 ptLongitude=null,ptLatitude=null;
            for (int l = 1; l < 37; l++)
            {
                dFactor = (10.0 * l) * Math.PI / 180.0;                
                a=majorRadius * Math.cos(dFactor);
                b=minorRadius * Math.sin(dFactor);
                //dist=Math.sqrt(a*a+b*b);
                //azimuth = (10.0 * l);// * Math.PI / 180.0;  
                //azimuth=90-azimuth;
                //pt = geodesic_coordinate(ptCenter,dist,azimuth);                
                //pt = geodesic_coordinate(ptCenter,dist,azimuth);                
                ptLongitude=geodesic_coordinate(ptCenter,a,90);
                ptLatitude=geodesic_coordinate(ptCenter,b,0);
                //pt=new POINT2(ptLatitude.x,ptLongitude.y);
                pt=new POINT2(ptLongitude.x,ptLatitude.y);
                //pEllipsePoints[l-1]=pt;
                pEllipsePoints[l-1]=geoRotatePoint(ptCenter,pt,-rotation);
            }            
            pEllipsePoints[36]=new POINT2(pEllipsePoints[0]);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className, "GetGeoEllipse",
                    new RendererException("GetGeoEllipse", exc));
        }
        return pEllipsePoints;
    }
}
