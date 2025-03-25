/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.RenderMultipoints;

import armyc2.c5isr.JavaLineArray.*;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.JavaTacticalRenderer.mdlGeodesic;
import armyc2.c5isr.JavaTacticalRenderer.clsUtility;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import armyc2.c5isr.JavaLineArray.Shape2;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.IPointConversion;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.JavaTacticalRenderer.clsMETOC;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

import java.util.Map;
import java.util.HashMap;

/**
 * CPOF utility functions taken from JavaLineArrayCPOF
 *
 * 
 */
public final class clsUtilityCPOF {

    private static final String _className = "clsUtilityCPOF";

    /**
     *
     * @param ptLatLong
     * @param converter
     * @return
     */
    private static POINT2 PointLatLongToPixels(POINT2 ptLatLong,
            IPointConversion converter) {
        POINT2 pt = new POINT2();
        try {
            double x = ptLatLong.x;
            double y = ptLatLong.y;
            Point2D ptPixels = converter.GeoToPixels(new Point2D.Double(x, y));
            pt.x = ptPixels.getX();
            pt.y = ptPixels.getY();
            pt.style = ptLatLong.style;

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "PointLatLongToPixels",
                    new RendererException("Failed inside PointLatLongToPixels", exc));
        }
        return pt;
    }

    /**
     * for the change 1 fire support areas
     *
     * @param tg
     * @param lineType
     * @param radius
     * @param width
     * @param length
     * @param attitude
     */
    private static void GetNumericFields(TGLight tg,
            int lineType,
            ref<double[]> radius,
            ref<double[]> width,
            ref<double[]> length,
            ref<double[]> attitude) {
        try {
            if (lineType == TacticalLines.RANGE_FAN_FILL) {
                return;
            }
            double dist = 0;
            ref<double[]> a12 = new ref(), a21 = new ref();
            POINT2 pt0 = new POINT2(0, 0);
            POINT2 pt1 = new POINT2(0, 0);
            radius.value = new double[1];
            width.value = new double[1];
            attitude.value = new double[1];
            length.value = new double[1];
            switch (lineType) {
                case TacticalLines.CIRCULAR:
                case TacticalLines.BDZ:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.NOTACK:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                    if (SymbolUtilities.isNumber(tg.get_AM())) {
                        radius.value[0] = Double.parseDouble(tg.get_AM());
                    }
                    break;
                case TacticalLines.LAUNCH_AREA:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                case TacticalLines.SHIP_AOI_CIRCULAR:
                    //minor radius in meters
                    if (SymbolUtilities.isNumber(tg.get_AM1())) {
                        length.value[0] = Double.parseDouble(tg.get_AM1());
                    }
                    //major radius in meters
                    if (SymbolUtilities.isNumber(tg.get_AM())) {
                        width.value[0] = Double.parseDouble(tg.get_AM());
                    }
                    //rotation angle in degrees
                    if (SymbolUtilities.isNumber(tg.get_AN())) {
                        attitude.value[0] = Double.parseDouble(tg.get_AN());
                    }

                    break;
                case TacticalLines.RECTANGULAR:
                    if (SymbolUtilities.isNumber(tg.get_AM1())) {
                        length.value[0] = Double.parseDouble(tg.get_AM1());
                    }
                    if (SymbolUtilities.isNumber(tg.get_AM())) {
                        width.value[0] = Double.parseDouble(tg.get_AM());
                    }
                    //assume that attitude was passed in mils
                    //so we must multiply by 360/6400 to convert to degrees
                    if (SymbolUtilities.isNumber(tg.get_AN())) {
                        attitude.value[0] = Double.parseDouble(tg.get_AN()) * (360d / 6400d);
                    }
                    break;
                case TacticalLines.CUED_ACQUISITION:
                    if (SymbolUtilities.isNumber(tg.get_AM())) {
                        length.value[0] = Double.parseDouble(tg.get_AM());
                    }
                    if (SymbolUtilities.isNumber(tg.get_AM1())) {
                        width.value[0] = Double.parseDouble(tg.get_AM1());
                    }
                    if (SymbolUtilities.isNumber(tg.get_AN())) {
                        // Make 0 degrees point north instead of East
                        attitude.value[0] = Double.parseDouble(tg.get_AN()) + 270;
                    }
                    break;
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                    if (tg.LatLongs.size() >= 2) {
                        //get the length and the attitude in mils
                        pt0 = tg.LatLongs.get(0);
                        pt1 = tg.LatLongs.get(1);
                        dist = mdlGeodesic.geodesic_distance(pt0, pt1, a12, a21);
                        attitude.value[0] = a12.value[0];
                    }
                    if (SymbolUtilities.isNumber(tg.get_AM())) {
                        width.value[0] = Double.parseDouble(tg.get_AM());
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetNumericFields",
                    new RendererException("Failed inside GetNumericFields", exc));
        }
    }

    /**
     * Do a 360 degree horizontal shift for points on either side of the
     * midpoint of the display, if the MBR for the pixels is greater than 180
     * degrees wide. Builds pixels for two symbols to draw a symbol flipped
     * about the left edge and also a symbol flipped about the right edge. This
     * function is typically used at world view. Caller must instantiate last
     * two parameters.
     *
     * @param tg
     * @param converter
     * @param farLeftPixels - OUT - the resultant pixels for left shift symbol
     * @param farRightPixels - OUT - the result pixels for the right shift
     * symbol
     */
    protected static void GetFarPixels(TGLight tg,
            IPointConversion converter,
            ArrayList farLeftPixels,
            ArrayList farRightPixels) {
        try {
            if (farLeftPixels == null || farRightPixels == null) {
                return;
            }
            //Cannot use tg.LatLon to get width in degrees because it shifts +/-180 at IDL.
            //Get degrees per pixel longitude, will use it for determining width in degrees
            Point2D ptPixels50 = converter.GeoToPixels(new Point2D.Double(50, 30));
            Point2D ptPixels60 = converter.GeoToPixels(new Point2D.Double(60, 30));
            double degLonPerPixel = 10 / Math.abs(ptPixels60.getX() - ptPixels50.getX());
            int j = 0;
            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            int n = tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for (j = 0; j < n; j++) {
                if (tg.Pixels.get(j).x < minX) {
                    minX = tg.Pixels.get(j).x;
                }
                if (tg.Pixels.get(j).x > maxX) {
                    maxX = tg.Pixels.get(j).x;
                }
            }
            double degWidth = (maxX - minX) * degLonPerPixel;
            if (Math.abs(degWidth) < 180) {
                return;
            }

            //if it did not return then we must shift the pixels left and right
            //first get the midpoint X value to use for partitioning the points
            double midX = Math.abs(180 / degLonPerPixel);
            double x = 0, y = 0;
            //do a shift about the left hand side
            //for(j=0;j<tg.Pixels.size();j++)
            for (j = 0; j < n; j++) {
                x = tg.Pixels.get(j).x;
                y = tg.Pixels.get(j).y;
                if (x > midX) {
                    //shift x left by 360 degrees in pixels
                    x -= 2 * midX;
                }
                //else do not shift the point
                //add the shifted (or not) point to the new arraylist
                farLeftPixels.add(new POINT2(x, y));
            }
            //do a shift about the right hand side
            //for(j=0;j<tg.Pixels.size();j++)
            for (j = 0; j < n; j++) {
                x = tg.Pixels.get(j).x;
                y = tg.Pixels.get(j).y;
                if (x < midX) {
                    //shift x right by 360 degrees in pixels
                    x += 2 * midX;
                }
                //else do not shift the point
                //add the shifted (or not) point to the new arraylist
                farRightPixels.add(new POINT2(x, y));
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetFarPixels",
                    new RendererException("Failed inside GetFarPixels", exc));
        }
    }

    /**
     *
     * @param tg
     * @param lineType
     * @param converter
     * @param shapes
     * @return
     */
    protected static boolean Change1TacticalAreas(TGLight tg,
            int lineType, IPointConversion converter, ArrayList<Shape2> shapes) {
        try {
            ref<double[]> width = new ref(), length = new ref(), attitude = new ref(), radius = new ref();
            int j = 0;
            POINT2 pt0 = tg.LatLongs.get(0);
            POINT2 pt1 = null;
            POINT2 ptTemp = new POINT2();
            POINT2 pt00 = new POINT2();
            if (tg.LatLongs.size() > 1) {
                pt1 = tg.LatLongs.get(1);
            } else {
                pt1 = tg.LatLongs.get(0);
            }
            POINT2[] pPoints = null;
            POINT2 ptCenter = PointLatLongToPixels(pt0, converter);

            GetNumericFields(tg, lineType, radius, width, length, attitude);
            switch (lineType) {
                case TacticalLines.LAUNCH_AREA:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                case TacticalLines.SHIP_AOI_CIRCULAR:
                    POINT2[] ellipsePts = mdlGeodesic.getGeoEllipse(pt0, width.value[0], length.value[0], attitude.value[0]);
                    for (j = 0; j < ellipsePts.length; j++) //was 103
                    {
                        pt0 = ellipsePts[j];
                        pt1 = PointLatLongToPixels(pt0, converter);
                        tg.Pixels.add(pt1);
                    }
                    break;
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    //get the upper left corner                    
                    pt00 = mdlGeodesic.geodesic_coordinate(pt0, width.value[0] / 2, attitude.value[0] - 90);
                    pt00 = PointLatLongToPixels(pt00, converter);

                    pt00.style = 0;
                    tg.Pixels.add(pt00);

                    //second corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, width.value[0] / 2, attitude.value[0] + 90);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    //third corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt1, width.value[0] / 2, attitude.value[0] + 90);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    //fourth corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt1, width.value[0] / 2, attitude.value[0] - 90);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    tg.Pixels.add(pt00);
                    break;
                case TacticalLines.RECTANGULAR_TARGET:
                    POINT2[] pts = new POINT2[4]; // 4 Corners

                    // get the upper left corner
                    pts[0] = mdlGeodesic.geodesic_coordinate(pt0, width.value[0] / 2, attitude.value[0] - 90);
                    ptTemp = PointLatLongToPixels(pts[0], converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    // second corner (clockwise from center)
                    pts[1] = mdlGeodesic.geodesic_coordinate(pt0, width.value[0] / 2, attitude.value[0] + 90);
                    ptTemp = PointLatLongToPixels(pts[1], converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    // third corner (clockwise from center)
                    pts[2] = mdlGeodesic.geodesic_coordinate(pt1, width.value[0] / 2, attitude.value[0] + 90);
                    ptTemp = PointLatLongToPixels(pts[2], converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    // fourth corner (clockwise from center)
                    pts[3] = mdlGeodesic.geodesic_coordinate(pt1, width.value[0] / 2, attitude.value[0] - 90);
                    ptTemp = PointLatLongToPixels(pts[3], converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    // Close shape
                    ptTemp = PointLatLongToPixels(pts[0], converter);
                    ptTemp.style = 5;
                    tg.Pixels.add(ptTemp);

                    double heightD = mdlGeodesic.geodesic_distance(pts[0], pts[1], null, null);
                    double widthD = mdlGeodesic.geodesic_distance(pts[1], pts[2], null, null);
                    double crossLength = Math.min(heightD, widthD) * .4; // Length from center

                    POINT2 centerPt = lineutility.CalcCenterPointDouble2(pts, 4);

                    ptTemp = mdlGeodesic.geodesic_coordinate(centerPt, crossLength, 0);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    ptTemp = mdlGeodesic.geodesic_coordinate(centerPt, crossLength, 180);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 5;
                    tg.Pixels.add(ptTemp);

                    ptTemp = mdlGeodesic.geodesic_coordinate(centerPt, crossLength, -90);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);

                    ptTemp = mdlGeodesic.geodesic_coordinate(centerPt, crossLength, 90);
                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    ptTemp.style = 0;
                    tg.Pixels.add(ptTemp);
                    break;
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CUED_ACQUISITION:
                    //AFATDS swap length and width
                    //comment next three lines to render per Mil-Std-2525
                    //double temp=width.value[0];
                    //width.value[0]=length.value[0];
                    //length.value[0]=temp;

                    //get the upper left corner
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, length.value[0] / 2, attitude.value[0] - 90);//was length was -90
                    ptTemp = mdlGeodesic.geodesic_coordinate(ptTemp, width.value[0] / 2, attitude.value[0] + 0);//was width was 0

                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    tg.Pixels.add(ptTemp);
                    //second corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, length.value[0] / 2, attitude.value[0] + 90);  //was length was +90
                    ptTemp = mdlGeodesic.geodesic_coordinate(ptTemp, width.value[0] / 2, attitude.value[0] + 0);   //was width was 0

                    ptTemp = PointLatLongToPixels(ptTemp, converter);

                    tg.Pixels.add(ptTemp);

                    //third corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, length.value[0] / 2, attitude.value[0] + 90);//was length was +90
                    ptTemp = mdlGeodesic.geodesic_coordinate(ptTemp, width.value[0] / 2, attitude.value[0] + 180);//was width was +180

                    ptTemp = PointLatLongToPixels(ptTemp, converter);

                    tg.Pixels.add(ptTemp);

                    //fouth corner (clockwise from center)
                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, length.value[0] / 2, attitude.value[0] - 90);//was length was -90
                    ptTemp = mdlGeodesic.geodesic_coordinate(ptTemp, width.value[0] / 2, attitude.value[0] + 180);//was width was +180

                    ptTemp = PointLatLongToPixels(ptTemp, converter);
                    tg.Pixels.add(ptTemp);
                    tg.Pixels.add(new POINT2(tg.Pixels.get(0).x, tg.Pixels.get(0).y));
                    break;
                case TacticalLines.CIRCULAR:
                case TacticalLines.BDZ:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.NOTACK:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                    //get a horizontal point on the radius
                    pt0 = tg.LatLongs.get(0);

                    ptTemp = mdlGeodesic.geodesic_coordinate(pt0, radius.value[0], 90);

                    pPoints = new POINT2[3];
                    pPoints[0] = new POINT2(pt0);
                    pPoints[1] = new POINT2(ptTemp);
                    pPoints[2] = new POINT2(ptTemp);

                    ArrayList<POINT2> pPoints2 = mdlGeodesic.GetGeodesicArc(pPoints);
                    POINT2 ptTemp2 = null;
                    //fill pixels and latlongs
                    for (j = 0; j < pPoints2.size(); j++) //was 103
                    {
                        pt0 = pPoints2.get(j);
                        ptTemp2 = new POINT2();
                        ptTemp2 = PointLatLongToPixels(pt0, converter);

                        tg.Pixels.add(ptTemp2);
                    }
                    break;
                case TacticalLines.RANGE_FAN:
                    //get the concentric circles
                    GetConcentricCircles(tg, lineType, converter);
                    //Mil-Std-2525 Rev C does not have the orientation arrow
                    //assume we are using Rev C if there is only 1 anchor point
                    if (tg.LatLongs.size() > 1) {
                        RangeFanOrientation(tg, lineType, converter);
                    }
                    break;
                case TacticalLines.RANGE_FAN_SECTOR:
                    GetSectorRangeFan(tg, converter);
                    RangeFanOrientation(tg, lineType, converter);
                    break;
                case TacticalLines.RADAR_SEARCH:
                    GetSectorRangeFan(tg, converter);
                    break;
                case TacticalLines.RANGE_FAN_FILL:  //circular range fan calls Change1TacticalAreas twice
                    GetSectorRangeFan(tg, converter);
                    break;
                default:
                    return false;
            }

            //the shapes
            ArrayList<POINT2> farLeftPixels = new ArrayList();
            ArrayList<POINT2> farRightPixels = new ArrayList();
            clsUtilityCPOF.GetFarPixels(tg, converter, farLeftPixels, farRightPixels);
            ArrayList<Shape2> shapesLeft = new ArrayList();
            ArrayList<Shape2> shapesRight = new ArrayList();
            //ArrayList<Shape2>shapes=null;   //use this to collect all the shapes

            if (farLeftPixels.isEmpty() || farRightPixels.isEmpty()) {
                //diagnostic
                //Change1PixelsToShapes(tg,shapes);
                ArrayList<POINT2> tempPixels = new ArrayList();
                tempPixels.addAll((ArrayList) tg.Pixels);
                clsUtilityCPOF.postSegmentFSA(tg, converter);
                Change1PixelsToShapes(tg, shapes, false);
                //reuse the original pixels for the subsequent call to AddModifier2
                tg.Pixels = tempPixels;
                //end section
            } else //symbol was more than 180 degrees wide, use left and right symbols
            {
                //set tg.Pixels to the left shapes for the call to Change1PixelsToShapes
                tg.Pixels = farLeftPixels;
                Change1PixelsToShapes(tg, shapesLeft, false);
                //set tg.Pixels to the right shapes for the call to Change1PixelsToShapes
                tg.Pixels = farRightPixels;
                Change1PixelsToShapes(tg, shapesRight, false);
                //load left and right shapes into shapes
                shapes.addAll(shapesLeft);
                shapes.addAll(shapesRight);
            }
            return true;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "Change1TacticalAreas",
                    new RendererException("Failed inside Change1TacticalAreas", exc));
        }
        return false;
    }

    /**
     * build shapes arraylist from tg.Pixels for the Change 1 symbols
     *
     * @param tg
     * @param shapes - OUT - caller instantiates the arraylist
     */
    private static void Change1PixelsToShapes(TGLight tg, ArrayList<Shape2> shapes, boolean fill) {
        Shape2 shape = null;
        boolean beginLine = true;
        POINT2 currentPt = null, lastPt = null;
        int k = 0;
        int linetype = tg.get_LineType();
        int n = tg.Pixels.size();
            //a loop for the outline shapes            
        //for (k = 0; k < tg.Pixels.size(); k++)
        for (k = 0; k < n; k++) {
            //use shapes instead of pixels
            if (shape == null) {
                //shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                if(!fill)
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                else if(fill)
                    shape = new Shape2(Shape2.SHAPE_TYPE_FILL);
            }

            currentPt = tg.Pixels.get(k);
            if (k > 0) {
                lastPt = tg.Pixels.get(k - 1);
            }

            if (beginLine) {
                if (k == 0) {
                    shape.set_Style(currentPt.style);
                }

                if (k > 0) //doubled points with linestyle=5
                {
                    if (currentPt.style == 5 && lastPt.style == 5) {
                        shape.lineTo(currentPt);
                    }
                }

                shape.moveTo(currentPt);
                beginLine = false;
            } else {
                shape.lineTo(currentPt);
                if (currentPt.style == 5 || currentPt.style == 10) {
                    beginLine = true;
                    //unless there are doubled points with style=5
                    if (linetype == TacticalLines.RANGE_FAN_FILL && k < tg.Pixels.size() - 1) {
                        shapes.add(shape);
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    }
                }
            }
            if (k == tg.Pixels.size() - 1) //PBS shapes have 2 shapes, other non-LC symbols have 1 shape
            {
                //shapes.add(shape);
                if(shape.getShapeType()==ShapeInfo.SHAPE_TYPE_FILL)
                    shapes.add(0,shape);
                else
                    shapes.add(shape);
            }
        }   //end for

    }

    private static void GetConcentricCircles(TGLight tg, int lineType, IPointConversion converter) {
        try {
            int j = 0, l = 0;
            double radius = 0;

            POINT2 pt = new POINT2();
            ArrayList<POINT2> pts = new ArrayList();
            double[] radii = null; // AM
            String strAM = tg.get_AM();
            if (tg.LatLongs.size() == 1 && strAM != null)
            {
                String[] strs = strAM.split(",");
                radii = new double[strs.length];
                for (j = 0; j < strs.length; j++) {
                    radii[j] = Double.parseDouble(strs[j]);
                }
            }

            int n = radii.length;

            //loop thru the circles
            POINT2[] pPoints = null;
            for (l = 0; l < n; l++) {
                radius = radii[l];
                if (radius == 0) {
                    continue;
                }

                pPoints = new POINT2[3];
                pt = tg.LatLongs.get(0);
                pPoints[0] = new POINT2(pt);
                //radius, 90, ref lon2c, ref lat2c);
                pt = mdlGeodesic.geodesic_coordinate(pt, radius, 90);
                pPoints[1] = new POINT2(pt);
                pPoints[2] = new POINT2(pt);

                pts = mdlGeodesic.GetGeodesicArc(pPoints);

                POINT2 ptTemp2 = null;
                //fill pixels and latlongs
                int t = pts.size();
                //for (j = 0; j < pts.size(); j++)//was 103
                for (j = 0; j < t; j++)//was 103
                {
                    ptTemp2 = new POINT2();
                    ptTemp2 = PointLatLongToPixels(pts.get(j), converter);
                    ptTemp2.style = 0;
                    if (j == pts.size() - 1) {
                        ptTemp2.style = 5;
                    }

                    tg.Pixels.add(ptTemp2);
                }
            }
            int length = tg.Pixels.size();
            tg.Pixels.get(length - 1).style = 5;

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetConcentricCircles",
                    new RendererException("Failed inside GetConcentricCircles", exc));
        }
    }

    /**
     * if tg.H2 is filled then the max range sector is used to determine the
     * orientation
     *
     * @param tg
     * @return left,right,min,max
     */
    private static String GetMaxSector(TGLight tg) {
        String strLeftRightMinMax = null;
        try {
            double max = 0, maxx = -Double.MAX_VALUE;
            //get the number of sectors
            strLeftRightMinMax = tg.get_LRMM();
            String[] leftRightMinMax = strLeftRightMinMax.split(",");
            int numSectors = leftRightMinMax.length / 4;
            int k = 0, maxIndex = -1;
            //there must be at least one sector
            if (numSectors < 1) {
                return null;
            }

            if (numSectors * 4 != leftRightMinMax.length) {
                return null;
            }
            //get the max index
            try {
                for (k = 0; k < numSectors; k++) {
                    //left = Double.parseDouble(leftRightMinMax[4 * k]);
                    //right = Double.parseDouble(leftRightMinMax[4 * k + 1]);
                    //min = Double.parseDouble(leftRightMinMax[4 * k + 2]);
                    max = Double.parseDouble(leftRightMinMax[4 * k + 3]);
                    if (max > maxx) {
                        maxx = max;
                        maxIndex = k;
                    }
                }
            } catch (NumberFormatException e) {
                return null;
            }
            String strLeft = leftRightMinMax[4 * maxIndex];
            String strRight = leftRightMinMax[4 * maxIndex + 1];
            String strMin = leftRightMinMax[4 * maxIndex + 2];
            String strMax = leftRightMinMax[4 * maxIndex + 3];
            strLeftRightMinMax = strLeft + "," + strRight + "," + strMin + "," + strMax;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetMaxSector",
                    new RendererException("Failed inside GetMaxSector", exc));
        }
        return strLeftRightMinMax;
    }

    /**
     * Create a tg with a new line type to used for circular range fan fill
     *
     * @param tg
     * @return
     */
    protected static TGLight GetCircularRangeFanFillTG(TGLight tg) {
        TGLight tg1 = null;
        try {
            //instantiate a dummy tg which will be used to call GetSectorRangeFan
            tg1 = new TGLight();
            tg1.set_VisibleModifiers(true);
            tg1.set_LineThickness(0);
            tg1.set_FillColor(tg.get_FillColor());
            tg1.set_Fillstyle(tg.get_FillStyle());
            tg1.LatLongs = new ArrayList<POINT2>();
            tg1.Pixels = new ArrayList<POINT2>();
            //we only want the 0th point
            tg1.LatLongs.add(tg.LatLongs.get(0));
            tg1.Pixels.add(tg.Pixels.get(0));
            tg1.Pixels.add(tg.Pixels.get(1));
            tg1.set_LineType(TacticalLines.RANGE_FAN_FILL);

            if (tg.get_LineType() == TacticalLines.RANGE_FAN_SECTOR || tg.get_LineType() == TacticalLines.RADAR_SEARCH) {
                tg1.set_LRMM(tg.get_LRMM());
                return tg1;
            } else if (tg.get_LineType() == TacticalLines.RANGE_FAN) {
                String[] radii = tg.get_AM().split(",");
                String strLeftRightMinMax = "";
                for (int j = 0; j < radii.length - 1; j++) {
                    if (j > 0) {
                        strLeftRightMinMax += ",";
                    }

                    strLeftRightMinMax += "0,0," + radii[j] + "," + radii[j + 1];
                }
                tg1.set_LRMM(strLeftRightMinMax);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetCircularRangeFanFillTG",
                    new RendererException("Failed inside GetCircularRangeFanFillTG", exc));
        }
        return tg1;
    }

    /**
     *
     * @param tg
     * @param converter
     * @return
     */
    private static boolean GetSectorRangeFan(TGLight tg, IPointConversion converter) {
        boolean circle = false;
        try {
            POINT2 ptCenter = tg.LatLongs.get(0);
            int k = 0, l = 0;
            int numSectors = 0;
            clsUtility.GetSectorRadiiFromPoints(tg);

            //use pPoints to get each geodesic arc
            ArrayList<POINT2> pPoints = new ArrayList();
            ArrayList<POINT2> pPointsInnerArc = new ArrayList();
            ArrayList<POINT2> pPointsOuterArc = new ArrayList();
            ArrayList<POINT2> sectorPoints = new ArrayList();
            ArrayList<POINT2> allPoints = new ArrayList();

            //use these and the center to define each sector
            POINT2 pt1 = new POINT2(), pt2 = new POINT2();

            //get the number of sectors
            String strLeftRightMinMax = tg.get_LRMM();
            String[] leftRightMinMax = strLeftRightMinMax.split(",");

            //sanity checks
            double left = 0, right = 0, min = 0, max = 0;
            numSectors = leftRightMinMax.length / 4;

            //there must be at least one sector
            if (numSectors < 1) {
                return false;
            }

            if (numSectors * 4 != leftRightMinMax.length) {
                return false;
            }

            //left must be  less than right,
            //min must be less than max, each sector
            try {
                for (k = 0; k < numSectors; k++) {
                    left = Double.parseDouble(leftRightMinMax[4 * k]);
                    right = Double.parseDouble(leftRightMinMax[4 * k + 1]);
                    min = Double.parseDouble(leftRightMinMax[4 * k + 2]);
                    max = Double.parseDouble(leftRightMinMax[4 * k + 3]);
                }
            } catch (NumberFormatException e) {
                return false;
            }

            for (k = 0; k < numSectors; k++) //was k=0
            {
                //empty any points that were there from the last sector
                sectorPoints.clear();
                pPointsOuterArc.clear();
                pPointsInnerArc.clear();

                left = Double.parseDouble(leftRightMinMax[4 * k]);
                right = Double.parseDouble(leftRightMinMax[4 * k + 1]);
                min = Double.parseDouble(leftRightMinMax[4 * k + 2]);
                max = Double.parseDouble(leftRightMinMax[4 * k + 3]);

                //get the first point of the sector inner arc
                pt1 = mdlGeodesic.geodesic_coordinate(ptCenter, min, left);

                //get the last point of the sector inner arc
                pt2 = mdlGeodesic.geodesic_coordinate(ptCenter, min, right);

                pPoints.clear();

                pPoints.add(ptCenter);
                pPoints.add(pt1);
                pPoints.add(pt2);

                circle = mdlGeodesic.GetGeodesicArc2(pPoints, pPointsInnerArc);

                pPoints.clear();
                circle = false;

                pt1 = mdlGeodesic.geodesic_coordinate(ptCenter, max, left);
                pt2 = mdlGeodesic.geodesic_coordinate(ptCenter, max, right);

                pPoints.add(ptCenter);
                pPoints.add(pt1);
                pPoints.add(pt2);

                //get the geodesic min arc from left to right
                circle = mdlGeodesic.GetGeodesicArc2(pPoints, pPointsOuterArc);

                //we now have all the points and can add them to the polygon to return
                //we will have to reverse the order of points in the outer arc
                int n = pPointsInnerArc.size();
                for (l = 0; l < n; l++) {
                    pt1 = new POINT2(pPointsInnerArc.get(l));
                    sectorPoints.add(pt1);
                }
                n = pPointsOuterArc.size();
                //for (l = pPointsOuterArc.size() - 1; l >= 0; l--)
                for (l = n - 1; l >= 0; l--) {
                    pt1 = new POINT2(pPointsOuterArc.get(l));
                    sectorPoints.add(pt1);
                }

                //close the polygon
                pt1 = new POINT2(pPointsInnerArc.get(0));
                pt1.style = 5;
                sectorPoints.add(pt1);
                n = sectorPoints.size();
                //for (l = 0; l < sectorPoints.size(); l++)
                for (l = 0; l < n; l++) {
                    allPoints.add(sectorPoints.get(l));
                }
            }

            //cleanup what we can
            pPointsInnerArc = null;
            pPointsOuterArc = null;
            ptCenter = null;

            POINT2 ptTemp = null;
            int n = allPoints.size();
            //for (l = 0; l < allPoints.size(); l++)
            for (l = 0; l < n; l++) {
                pt1 = new POINT2();
                pt1 = PointLatLongToPixels(allPoints.get(l), converter);
                //do not add duplicates
                if (ptTemp != null && pt1.x == ptTemp.x && pt1.y == ptTemp.y) {
                    continue;
                }
                tg.Pixels.add(new POINT2(pt1));
                ptTemp = new POINT2(pt1);
            }

            return true;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSectorRangeFan",
                    new RendererException("Failed inside GetSectorRangeFan", exc));
        }
        return circle;
    }

    private static void RangeFanOrientation(TGLight tg, int lineType, IPointConversion converter) {
        try {
            POINT2 pt0 = tg.LatLongs.get(0);
            double dist = 0;
            double orientation = 0;
            double radius = 0;
            //double[] radii = clsUtility.GetRadii(tg,lineType);
            int j = 0;
            POINT2 pt1 = new POINT2();
            //if tg.PointCollection has more than one point
            //we use pts[1] to stuff tg.H with the orientation
            ref<double[]> a12 = new ref(), a21 = new ref();
            if (tg.LatLongs.size() > 1) //rev B can use points
            {
                pt1 = tg.LatLongs.get(1);
                dist = mdlGeodesic.geodesic_distance(pt0, pt1, a12, a21);
                orientation = a12.value[0];
            } else //rev C uses H2
            {
                String strLeftRightMinMax = GetMaxSector(tg);
                String[] sector = strLeftRightMinMax.split(",");
                double left = Double.parseDouble(sector[0]);
                double right = Double.parseDouble(sector[1]);
                double min = Double.parseDouble(sector[2]);
                double max = Double.parseDouble(sector[3]);
                //we want the range to be 0 to 360
                while (left > 360) {
                    left -= 360;
                }
                while (right > 360) {
                    right -= 360;
                }
                while (left < 0) {
                    left += 360;
                }
                while (right < 0) {
                    right += 360;
                }

                if (left > right) {
                    orientation = (left - 360 + right) / 2;
                } else {
                    orientation = (left + right) / 2;
                }

                dist = max;
            }
            radius = dist * 1.1;
            POINT2 pt0F = new POINT2();
            POINT2 pt1F = new POINT2();
            POINT2 ptBaseF = new POINT2();
            POINT2 ptLeftF = new POINT2();
            POINT2 ptRightF = new POINT2();
            POINT2 ptTipF = new POINT2();

            pt0 = tg.LatLongs.get(0);

            pt0F = PointLatLongToPixels(pt0, converter);

            pt1 = mdlGeodesic.geodesic_coordinate(pt0, radius, orientation);

            pt1F = PointLatLongToPixels(pt1, converter);
            dist = lineutility.CalcDistanceDouble(pt0F, pt1F);
            double base = 10;
            if (dist < 100) {
                base = dist / 10;
            }
            if (base < 5) {
                base = 5;
            }
            double basex2 = 2 * base;
            ptBaseF = lineutility.ExtendAlongLineDouble(pt0F, pt1F, dist + base);   //was 10
            ptTipF = lineutility.ExtendAlongLineDouble(pt0F, pt1F, dist + basex2);  //was 20

            ptLeftF = lineutility.ExtendDirectedLine(pt0F, ptBaseF, ptBaseF, 0, base);    //was 10
            ptRightF = lineutility.ExtendDirectedLine(pt0F, ptBaseF, ptBaseF, 1, base);   //was 10
            //length1 = tg.Pixels.size();

            tg.Pixels.add(pt0F);
            ptTipF.style = 5;
            tg.Pixels.add(ptTipF);
            tg.Pixels.add(ptLeftF);
            ptTipF.style = 0;
            tg.Pixels.add(ptTipF);
            tg.Pixels.add(ptRightF);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "RangeFanOrientation",
                    new RendererException("Failed inside RangeFanOrientation", exc));
        }
    }

    /**
     * after filtering pixels it needs to reinitialize the style to 0 or it
     * causes CELineArraydotNet to build wrong shapes
     *
     * @param tg
     */
    protected static void ClearPixelsStyle(TGLight tg) {
        try {
            //do not clear pixel style for the air corridors because
            //arraysupport is using linestyle for these to set the segment width         
            switch (tg.get_LineType()) {
                case TacticalLines.SC:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.TC:
                case TacticalLines.LLTR:
                case TacticalLines.AC:
                case TacticalLines.SAAFR:
                    return;
                default:
                    break;

            }
            int n = tg.Pixels.size();
            //for(int j=0;j<tg.Pixels.size();j++)            
            for (int j = 0; j < n; j++) {
                tg.Pixels.get(j).style = 0;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ClearPixelsStyle",
                    new RendererException("Failed inside ClearPixelsStyle", exc));

        }
    }

    /**
     * Filters too close points after segmenting and clipping
     *
     * @param tg
     * @param converter
     */
    protected static void FilterPoints2(TGLight tg, IPointConversion converter) {
        try {
            int lineType = tg.get_LineType();
            double minSpikeDistance = 0;
            boolean segmented = true;
            if (tg.Pixels.size() < 3) {
                return;
            }

            switch (lineType) {
                case TacticalLines.PL:
                case TacticalLines.FEBA:
                case TacticalLines.LOA:
                case TacticalLines.LL:
                case TacticalLines.EWL:
                case TacticalLines.FCL:
                case TacticalLines.LOD:
                case TacticalLines.LDLC:
                case TacticalLines.PLD:
                case TacticalLines.HOLD:
                case TacticalLines.HOLD_GE:
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.BRDGHD:
                case TacticalLines.BRDGHD_GE:
                case TacticalLines.NFL:
                    minSpikeDistance = arraysupport.getScaledSize(5, tg.get_LineThickness());
                    segmented = false;
                    break;
                case TacticalLines.ATDITCH:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.FLOT:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.FORTL:
                case TacticalLines.STRONG:
                    minSpikeDistance = arraysupport.getScaledSize(25, tg.get_LineThickness());
                    break;
                case TacticalLines.LC:
                case TacticalLines.OBSAREA:
                case TacticalLines.OBSFAREA:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ZONE:
                case TacticalLines.LINE:
                case TacticalLines.ATWALL:
                //case TacticalLines.ATWALL3D:
                case TacticalLines.UNSP:
                case TacticalLines.SFENCE:
                case TacticalLines.DFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                    minSpikeDistance = arraysupport.getScaledSize(35, tg.get_LineThickness());
                    break;
                case TacticalLines.ICE_EDGE_RADAR:  //METOCs
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                    minSpikeDistance = arraysupport.getScaledSize(35, tg.get_LineThickness());
                    break;
                default:
                    return;
            }
            double dist = 0;

            ArrayList<POINT2> pts = new ArrayList();

            //stuff pts with tg.Pixels
            //loop through pts to remove any points which are too close
            //then reset tg.Pixels with the new array with boundary points removed,            
            int j = 0;
            POINT2 pt = null, pt0 = null, pt1 = null;
            int n = tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for (j = 0; j < n; j++) {
                pt = tg.Pixels.get(j);
                pt.style = tg.Pixels.get(j).style;
                pts.add(pt);
            }

            boolean removedPt = true;
            //order of priority is: keep anchor points, then boundary points, then segmented points
            outer:
            while (removedPt == true) {
                removedPt = false;
                //n=pts.size();
                for (j = 0; j < pts.size() - 1; j++) {
                    pt0 = pts.get(j);
                    pt1 = pts.get(j + 1);
                    dist = lineutility.CalcDistanceDouble(pts.get(j), pts.get(j + 1));
                    if (dist < minSpikeDistance) {
                        if (segmented == false) {
                            if (j + 1 == pts.size() - 1) {
                                pts.remove(j);
                            } else {
                                pts.remove(j + 1);
                            }

                            removedPt = true;
                            break outer;
                        } else if (pt0.style == 0 && pt1.style == -1)//-1 are clipped boundary points
                        {
                            pts.remove(j + 1);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == 0 && pt1.style == -2)//-2 are segmented points, this should never happen
                        {
                            pts.remove(j + 1);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -1 && pt1.style == 0) {
                            pts.remove(j);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -1 && pt1.style == -1) {
                            pts.remove(j + 1);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -1 && pt1.style == -2) {
                            pts.remove(j + 1);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -2 && pt1.style == 0)//this should never happen
                        {
                            pts.remove(j);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -2 && pt1.style == -1) {
                            pts.remove(j);
                            removedPt = true;
                            break outer;
                        } else if (pt0.style == -2 && pt1.style == -2) {
                            pts.remove(j + 1);
                            removedPt = true;
                            break outer;
                        }
                    }
                    //n=pts.size();
                }
            }
            tg.Pixels = pts;
            tg.LatLongs = armyc2.c5isr.RenderMultipoints.clsUtility.PixelsToLatLong(pts, converter);

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "FilterPoints2",
                    new RendererException("Failed inside FilterPoints2", exc));

        }
    }

    /**
     * returns true if the line type can be clipped before calculating the
     * shapes
     *
     * @param tg tactical graphic
     * @return true if can pre-clip points
     */
    public static Boolean canClipPoints(TGLight tg) {
        try {
            String symbolId = tg.get_SymbolId();
            if (clsMETOC.IsWeather(symbolId) > 0) {
                return true;
            }

            int linetype = tg.get_LineType();
            switch (linetype) {
                case TacticalLines.ABATIS:
//                case TacticalLines.BOUNDARY:
                case TacticalLines.FLOT:
                case TacticalLines.LC:
                case TacticalLines.PL:
                case TacticalLines.FEBA:
                case TacticalLines.LL:
                case TacticalLines.EWL:
                case TacticalLines.GENERAL:
                case TacticalLines.JTAA:
                case TacticalLines.SAA:
                case TacticalLines.SGAA:
                case TacticalLines.ASSY:
                case TacticalLines.EA:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.DZ:
                case TacticalLines.EZ:
                case TacticalLines.LZ:
                case TacticalLines.PZ:
                case TacticalLines.LAA:
                case TacticalLines.ROZ:
                case TacticalLines.AARROZ:
                case TacticalLines.UAROZ:
                case TacticalLines.WEZ:
                case TacticalLines.FEZ:
                case TacticalLines.JEZ:
                case TacticalLines.FAADZ:
                case TacticalLines.HIDACZ:
                case TacticalLines.MEZ:
                case TacticalLines.LOMEZ:
                case TacticalLines.HIMEZ:
                case TacticalLines.WFZ:
                case TacticalLines.AIRFIELD:
                case TacticalLines.BATTLE:
                case TacticalLines.PNO:
                case TacticalLines.DIRATKAIR:
                case TacticalLines.DIRATKGND:
                case TacticalLines.DIRATKSPT:
                case TacticalLines.FCL:
                case TacticalLines.HOLD:
                case TacticalLines.BRDGHD:
                case TacticalLines.HOLD_GE:
                case TacticalLines.BRDGHD_GE:
                case TacticalLines.LOA:
                case TacticalLines.LOD:
                case TacticalLines.LDLC:
                case TacticalLines.PLD:
                case TacticalLines.ASSAULT:
                case TacticalLines.ATKPOS:
                case TacticalLines.OBJ:
                case TacticalLines.PEN:
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.AO:
                case TacticalLines.AIRHEAD:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.NAI:
                case TacticalLines.TAI:
                case TacticalLines.BASE_CAMP_REVD:
                case TacticalLines.BASE_CAMP:
                case TacticalLines.GUERILLA_BASE_REVD:
                case TacticalLines.GUERILLA_BASE:
                case TacticalLines.GENERIC_AREA:
                case TacticalLines.LINE:
                case TacticalLines.ZONE:
                case TacticalLines.OBSAREA:
                case TacticalLines.OBSFAREA:
                case TacticalLines.ATDITCH:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.ATWALL:
                case TacticalLines.DEPICT:
                case TacticalLines.MINED:
                case TacticalLines.FENCED:
                case TacticalLines.UXO:
                case TacticalLines.UNSP:
                case TacticalLines.SFENCE:
                case TacticalLines.DFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                case TacticalLines.FORTL:
                case TacticalLines.STRONG:
                case TacticalLines.RAD:
                case TacticalLines.BIO:
                case TacticalLines.NUC:
                case TacticalLines.CHEM:
                case TacticalLines.DRCL:
                case TacticalLines.LINTGT:
                case TacticalLines.LINTGTS:
                case TacticalLines.FPF:
                case TacticalLines.FSCL:
                case TacticalLines.BCL_REVD:
                case TacticalLines.BCL:
                case TacticalLines.ICL:
                case TacticalLines.IFF_OFF:
                case TacticalLines.IFF_ON:
                case TacticalLines.GENERIC_LINE:
                case TacticalLines.CFL:
                case TacticalLines.OVERHEAD_WIRE:
                case TacticalLines.NFL:
                case TacticalLines.MFP:
                case TacticalLines.RFL:
                case TacticalLines.AT:
                case TacticalLines.SERIES:
                case TacticalLines.STRIKWARN:
                case TacticalLines.SMOKE:
                case TacticalLines.BOMB:
                case TacticalLines.FSA:
                case TacticalLines.ACA:
                case TacticalLines.FFA:
                case TacticalLines.NFA:
                case TacticalLines.RFA:
                case TacticalLines.PAA:
                case TacticalLines.ATI:
                case TacticalLines.CFFZ:
                case TacticalLines.CFZ:
                case TacticalLines.SENSOR:
                case TacticalLines.CENSOR:
                case TacticalLines.DA:
                case TacticalLines.ZOR:
                case TacticalLines.TBA:
                case TacticalLines.TVAR:
                case TacticalLines.KILLBOXBLUE:
                case TacticalLines.KILLBOXPURPLE:
//                case TacticalLines.MSR:
//                case TacticalLines.ASR:
                case TacticalLines.MSR_ONEWAY:
                case TacticalLines.MSR_TWOWAY:
                case TacticalLines.MSR_ALT:
                case TacticalLines.ASR_ONEWAY:
                case TacticalLines.ASR_TWOWAY:
                case TacticalLines.ASR_ALT:
                case TacticalLines.ROUTE_ONEWAY:
                case TacticalLines.ROUTE_ALT:
                case TacticalLines.DHA_REVD:
                case TacticalLines.DHA:
                case TacticalLines.EPW:
                case TacticalLines.FARP:
                case TacticalLines.RHA:
                case TacticalLines.BSA:
                case TacticalLines.DSA:
                case TacticalLines.CSA:
                case TacticalLines.RSA:
                case TacticalLines.TGMF:
                    return true;
                case TacticalLines.MSR: //post clip these so there are identical points regardless whether segment data is set 10-5-16
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                case TacticalLines.BOUNDARY:
                    return false;
                default:
                    return false;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "canClipPoints",
                    new RendererException("Failed inside canClipPoints", exc));
        }
        return false;
    }

    /**
     * These get clipped so the fill must be treated as a separate shape.
     * Normally lines with fill do not have a separate shape for the fill.
     *
     * @param linetype
     * @return
     */
    protected static boolean LinesWithSeparateFill(int linetype, ArrayList<Shape2> shapes) {
        if (shapes == null) {
            return false;
        }

        switch (linetype) {
            case TacticalLines.MSDZ:
                return true;
            //treat these as lines: because of the feint they need an extra shape for the fill
            case TacticalLines.OBSFAREA:
            case TacticalLines.OBSAREA:
            case TacticalLines.STRONG:
            case TacticalLines.ZONE:
            case TacticalLines.FORT_REVD:
            case TacticalLines.FORT:
            case TacticalLines.ENCIRCLE:
            //return true;
            case TacticalLines.FIX:
            case TacticalLines.BOUNDARY:
            case TacticalLines.FLOT:
            case TacticalLines.LC:
            case TacticalLines.PL:
            case TacticalLines.FEBA:
            case TacticalLines.LL:
            case TacticalLines.EWL:
            case TacticalLines.AC:
            case TacticalLines.MRR:
            case TacticalLines.SL:
            case TacticalLines.TC:
            case TacticalLines.SAAFR:
            case TacticalLines.SC:
            case TacticalLines.LLTR:
            case TacticalLines.DIRATKAIR:
            case TacticalLines.DIRATKGND:
            case TacticalLines.DIRATKSPT:
            case TacticalLines.FCL:
            case TacticalLines.HOLD:
            case TacticalLines.BRDGHD:
            case TacticalLines.HOLD_GE:
            case TacticalLines.BRDGHD_GE:
            case TacticalLines.LOA:
            case TacticalLines.LOD:
            case TacticalLines.LDLC:
            case TacticalLines.PLD:
            case TacticalLines.RELEASE:
            case TacticalLines.HOL:
            case TacticalLines.BHL:
            case TacticalLines.LINE:
            case TacticalLines.ABATIS:
            case TacticalLines.ATDITCH:
            case TacticalLines.ATDITCHC:
            case TacticalLines.ATDITCHM:
            case TacticalLines.ATWALL:
            case TacticalLines.MNFLDFIX:
            case TacticalLines.UNSP:
            case TacticalLines.SFENCE:
            case TacticalLines.DFENCE:
            case TacticalLines.DOUBLEA:
            case TacticalLines.LWFENCE:
            case TacticalLines.HWFENCE:
            case TacticalLines.SINGLEC:
            case TacticalLines.DOUBLEC:
            case TacticalLines.TRIPLE:
            case TacticalLines.FORTL:
            case TacticalLines.LINTGT:
            case TacticalLines.LINTGTS:
            case TacticalLines.FSCL:
            case TacticalLines.BCL_REVD:
            case TacticalLines.BCL:
            case TacticalLines.ICL:
            case TacticalLines.IFF_OFF:
            case TacticalLines.IFF_ON:
            case TacticalLines.GENERIC_LINE:
            case TacticalLines.CFL:
            case TacticalLines.NFL:
            case TacticalLines.MFP:
            case TacticalLines.RFL:
            case TacticalLines.MSR:
            case TacticalLines.MSR_ONEWAY:
            case TacticalLines.MSR_TWOWAY:
            case TacticalLines.MSR_ALT:
            case TacticalLines.ASR:
            case TacticalLines.ASR_ONEWAY:
            case TacticalLines.ASR_TWOWAY:
            case TacticalLines.ASR_ALT:
            case TacticalLines.ROUTE:
            case TacticalLines.ROUTE_ONEWAY:
            case TacticalLines.ROUTE_ALT:
                //undo any fill
                Shape2 shape = null;
                if (shapes != null && shapes.size() > 0) {
                    int n = shapes.size();
                    //for(int j=0;j<shapes.size();j++)
                    for (int j = 0; j < n; j++) {
                        shape = shapes.get(j);
                        if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                            shapes.get(j).setFillColor(null);
                        }
                    }
                }
                return true;
            default:
                return false;

        }
    }

    /**
     * uses a hash map to set the POINT2 style when creating tg.Pixels from
     * Point2D ArrayList
     *
     * @param pts2d
     * @param hashMap
     * @return
     */
    protected static ArrayList<POINT2> Point2DtoPOINT2Mapped(ArrayList<Point2D> pts2d, Map<String, Object> hashMap) {
        ArrayList<POINT2> pts = new ArrayList();
        try {
            Point2D pt2d;
            int style = 0;
            int n = pts2d.size();
            //for(int j=0;j<pts2d.size();j++)
            for (int j = 0; j < n; j++) {
                pt2d = pts2d.get(j);
                //the hash map contains the original tg.Pixels before clipping
                if (hashMap.containsValue(pt2d)) {
                    style = 0;
                } else {
                    style = -1;   //style set to -1 identifies it as a clip bounds point
                }
                pts.add(new POINT2(pts2d.get(j).getX(), pts2d.get(j).getY(), style));
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "Point2DToPOINT2Mapped",
                    new RendererException("Failed inside Point2DToPOINT2Mapped", exc));
        }
        return pts;
    }

    protected static ArrayList<POINT2> Point2DtoPOINT2(ArrayList<Point2D> pts2d) {
        ArrayList<POINT2> pts = new ArrayList();
        try {
            int n = pts2d.size();
            //for(int j=0;j<pts2d.size();j++)
            for (int j = 0; j < n; j++) {
                pts.add(new POINT2(pts2d.get(j).getX(), pts2d.get(j).getY()));
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "Point2DToPOINT2",
                    new RendererException("Failed inside Point2DToPOINT2", exc));
        }
        return pts;
    }

    protected static ArrayList<Point2D> POINT2toPoint2D(ArrayList<POINT2> pts) {
        ArrayList<Point2D> pts2d = new ArrayList();
        try {
            int n = pts.size();
            //for(int j=0;j<pts.size();j++)
            for (int j = 0; j < n; j++) {
                pts2d.add(new Point2D.Double(pts.get(j).x, pts.get(j).y));
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "POINT2toPoint2D",
                    new RendererException("Failed inside POINT2toPoint2D", exc));
        }
        return pts2d;
    }

    /**
     * Builds a single shape from a point array. Currently we assume the array
     * represents a moveTo followed by a series of lineTo operations
     *
     * @param pts2d
     * @return
     */
    private static Shape BuildShapeFromPoints(ArrayList<Point2D> pts2d) {
        GeneralPath shape = new GeneralPath();
        try {
            shape.moveTo(pts2d.get(0).getX(), pts2d.get(0).getY());
            int n = pts2d.size();
            //for(int j=1;j<pts2d.size();j++)
            for (int j = 1; j < n; j++) {
                shape.lineTo(pts2d.get(j).getX(), pts2d.get(j).getY());
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "buildShapeFromPoints",
                    new RendererException("Failed inside buildShapeFromPoints", exc));

        }
        return shape;
    }

    /**
     * Clips a ShapeSpec. Assumes we are not post clipping splines, therefore
     * all the operations are moveTo, lineTo. Each ShapeSpec is assumed to be:
     * moveTo, lineTo ... lineTo, followed by another moveTo, lineTo, ...
     * lineTo, followed by ...
     *
     * @param shapeSpec
     * @param pts
     * @param clipArea
     * @return a single clipped shapeSpec
     */
    protected static ArrayList<Shape2> buildShapeSpecFromPoints(TGLight tg0,
            Shape2 shapeSpec, //the original ShapeSpec
            ArrayList<POINT2> pts,
            Object clipArea) {
        ArrayList<Shape2> shapeSpecs2 = null;
        Shape2 shapeSpec2;
        try {
            //create a tg to use for the clip
            shapeSpecs2 = new ArrayList();
            int j = 0, n = 0;
            //return null if it is outside the bounds
            Rectangle rect = shapeSpec.getBounds();
            int h = shapeSpec.getBounds().height;
            int w = shapeSpec.getBounds().width;
            int x = shapeSpec.getBounds().x;
            int y = shapeSpec.getBounds().y;
//            if(h==0 && w==0)
//                return shapeSpecs2;

            if (h == 0) {
                h = 1;
            }
            if (w == 0) {
                w = 1;
            }

            Rectangle2D clipBounds = null;
            ArrayList<Point2D> clipPoints = null;
            if (clipArea != null && clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                clipBounds = (Rectangle2D) clipArea;
            } else if (clipArea != null && clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                //clipBounds=(Rectangle2D)clipArea;
                Rectangle rectx = (Rectangle) clipArea;
                clipBounds = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
            } else if (clipArea != null && clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                clipPoints = (ArrayList<Point2D>) clipArea;
            }

            if (clipBounds != null && clipBounds.contains(shapeSpec.getShape().getBounds2D()) == false
                    && clipBounds.intersects(shapeSpec.getShape().getBounds2D()) == false) {
                //this tests if the shape has height or width 0
                //but may be contained within the clipbounds or intersect it
                //in that case we gave it a default width or thickness of 1
                if (clipBounds.contains(x, y, w, h) == false
                        && clipBounds.intersects(x, y, w, h) == false) {
                    return shapeSpecs2;
                }
            } else if (clipPoints != null) {
                GeneralPath poly = new GeneralPath();
                n = clipPoints.size();
                //for(j=0;j<clipPoints.size();j++)
                for (j = 0; j < n; j++) {
                    if (j == 0) {
                        poly.moveTo(clipPoints.get(j).getX(), clipPoints.get(j).getY());
                    } else {
                        poly.lineTo(clipPoints.get(j).getX(), clipPoints.get(j).getY());
                    }
                }
                poly.closePath();
                if (poly.contains(shapeSpec.getShape().getBounds2D()) == false
                        && poly.intersects(shapeSpec.getShape().getBounds2D()) == false) {
                    if (poly.contains(x, y, w, h) == false
                            && poly.intersects(x, y, w, h) == false) {
                        return shapeSpecs2;
                    }
                }
            }

            if (shapeSpec.getShapeType() == Shape2.SHAPE_TYPE_MODIFIER
                    || shapeSpec.getShapeType() == Shape2.SHAPE_TYPE_MODIFIER_FILL) {
                shapeSpecs2.add(shapeSpec);
                return shapeSpecs2;
            }
            TGLight tg = new TGLight();
            POINT2 pt = null;
            tg.set_LineType(TacticalLines.PL);
            ArrayList<POINT2> pts2 = new ArrayList();
            ArrayList<Point2D> pts2d = null;
            Shape shape = null;
            GeneralPath gp = new GeneralPath();
            //loop through the points
            n = pts.size();
            //for(j=0;j<pts.size();j++)
            for (j = 0; j < n; j++) {
                pt = pts.get(j);
                //new line
                switch (pt.style) {
                    case 0: //moveTo,
                        //they lifted the pencil, so we build the shape from the existing pts and append it
                        if (pts2.size() > 1) {
                            //clip the points
                            tg = new TGLight();
                            tg.set_LineType(TacticalLines.PL);
                            tg.Pixels = pts2;
                            if (clipBounds != null) {
                                pts2d = clsClipPolygon2.ClipPolygon(tg, clipBounds);
                            } else if (clipPoints != null && !clipPoints.isEmpty()) {
                                pts2d = clsClipQuad.ClipPolygon(tg, clipPoints);
                            }

                            //build a GeneralPath from the points we collected, we will append it
                            if (pts2d != null && pts2d.size() > 1) {
                                shape = BuildShapeFromPoints(pts2d);
                                //append the shape because we want to return only one shape
                                gp.append(shape, false);
                            }
                            //clear the points array and begin the next line
                            pts2.clear();
                            pts2.add(pt);
                        } else {
                            pts2.add(pt);
                        }
                        break;
                    case 1: //lineTo
                        pts2.add(pt);
                        break;
                    default:
                        pts2.add(pt);
                        break;
                }
            }//end for
            //append the last shape
            if (pts2.size() > 1) {
                //clip the points
                tg = new TGLight();
                tg.set_LineType(TacticalLines.PL);
                tg.Pixels = pts2;
                if (clipBounds != null) {
                    pts2d = clsClipPolygon2.ClipPolygon(tg, clipBounds);
                } else if (clipPoints != null) {
                    pts2d = clsClipQuad.ClipPolygon(tg, clipPoints);
                }
                //build a GeneralPath from the points we collected, we will append it
                if (pts2d != null && pts2d.size() > 1) {
                    shape = BuildShapeFromPoints(pts2d);
                    gp.append(shape, false);
                }
                tg0.set_WasClipped(tg.get_WasClipped());
            }
            //create the shapespec here
            //initialize the clipped ShapeSpec
            shapeSpec2 = new Shape2(shapeSpec.getShapeType());
            shapeSpec2.setLineColor(shapeSpec.getLineColor());
            shapeSpec2.setFillColor(shapeSpec.getFillColor());
            shapeSpec2.setStroke(shapeSpec.getStroke());
            shapeSpec2.setTexturePaint(shapeSpec.getTexturePaint());
            shapeSpec2.setShape(gp);
            shapeSpecs2.add(shapeSpec2);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "buildShapeSpecFromPoints",
                    new RendererException("Failed inside buildShapeSpecFromPoints", exc));

        }
        return shapeSpecs2;
    }

    /**
     * Currently assumes no MeTOC symbols are post clipped
     *
     * @param tg
     * @param shapeSpecsArray
     * @param clipArea
     * @return
     */
    protected static ArrayList<Shape2> postClipShapes(TGLight tg, ArrayList<Shape2> shapeSpecsArray, Object clipArea) {
        ArrayList<Shape2> shapeSpecs2 = null;
        ArrayList<Shape2> tempShapes = null;
        try {
            if (shapeSpecsArray == null || shapeSpecsArray.size() == 0) {
                return null;
            }

            shapeSpecs2 = new ArrayList();
            int j = 0;
            ArrayList<Shape2> shapeSpecs = new ArrayList();
            int n = shapeSpecsArray.size();
            //for(j=0;j<shapeSpecsArray.size();j++)
            for (j = 0; j < n; j++) {
                shapeSpecs.add(shapeSpecsArray.get(j));
            }

            ArrayList<POINT2> pts = new ArrayList();//use these
            Shape shape = null;
            POINT2 pt;
            double[] coords = new double[6];
            Shape2 shapeSpec = null;
            n = shapeSpecs.size();
            //for(j=0;j<shapeSpecs.size();j++)
            for (j = 0; j < n; j++) {
                shapeSpec = shapeSpecs.get(j);
                shape = shapeSpec.getShape();
                pts.clear();
                for (PathIterator i = shape.getPathIterator(null); !i.isDone(); i.next()) {
                    int type = i.currentSegment(coords);
                    switch (type) {
                        case PathIterator.SEG_MOVETO:
                            pt = new POINT2(coords[0], coords[1]);
                            pt.style = 0;
                            pts.add(pt);
                            break;
                        case PathIterator.SEG_LINETO:
                            pt = new POINT2(coords[0], coords[1]);
                            pt.style = 1;
                            pts.add(pt);
                            break;
                        case PathIterator.SEG_QUADTO:   //not using this
                            pt = new POINT2(coords[0], coords[1]);
                            pt.style = 2;
                            pts.add(pt);
                            pt = new POINT2(coords[2], coords[3]);
                            pt.style = 2;
                            pts.add(pt);
                            break;
                        case PathIterator.SEG_CUBICTO:  //not using this
                            pt = new POINT2(coords[0], coords[1]);
                            pt.style = 3;
                            pts.add(pt);
                            pt = new POINT2(coords[2], coords[3]);
                            pt.style = 3;
                            pts.add(pt);
                            pt = new POINT2(coords[4], coords[5]);
                            pt.style = 3;
                            pts.add(pt);
                            break;
                        case PathIterator.SEG_CLOSE://not using this
                            pt = new POINT2(coords[0], coords[1]);
                            pt.style = 4;
                            pts.add(pt);
                            break;
                        default:
                            pt = null;
                            break;
                    }//end switch
                }   //end for pathiterator i
                tempShapes = buildShapeSpecFromPoints(tg, shapeSpec, pts, clipArea);
                shapeSpecs2.addAll(tempShapes);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "postClipShapes",
                    new RendererException("Failed inside postClipShapes", exc));
        }
        return shapeSpecs2;
    }

    /**
     * For the 3d map we cannot pre-segment the auto-shapes or fire support
     * areas. We do need to pre-segment generic lines regardless of the status
     * if clipping is set. Currently we are not pre-segmenting axis of advance
     * symbols.
     *
     * @param tg
     * @return true if pre-segmenting is to be used
     */
    private static boolean segmentAnticipatedLine(TGLight tg) {
        try {
            int linetype = tg.get_LineType();
            //do not pre-segment the fire support rectangular and circular areas
            if (clsUtility.IsChange1Area(linetype)) {
                return false;
            }
            //do not pre-segment the autoshapes
            if (clsUtility.isAutoshape(tg)) {
                return false;
            }
            //temporarily do not pre-segment the channel types.
            switch (linetype) {
                case TacticalLines.OVERHEAD_WIRE:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.MAIN:
                case TacticalLines.SPT:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                    return false;
                case TacticalLines.MSR_ONEWAY:
                case TacticalLines.MSR_TWOWAY:
                case TacticalLines.MSR_ALT:
                case TacticalLines.ASR_ONEWAY:
                case TacticalLines.ASR_TWOWAY:
                case TacticalLines.ASR_ALT:
                case TacticalLines.ROUTE_ONEWAY:
                case TacticalLines.ROUTE_ALT:
                    //added because of segment data 4-22-13
                    //removed from this case block since we now post-clip these because of segment color data 10-5-16
//                case TacticalLines.MSR:
//                case TacticalLines.ASR:
//                case TacticalLines.BOUNDARY:
                    return false;
                default:
                    break;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "segmentGenericLine",
                    new RendererException("Failed inside segmentGenericLine", exc));
        }
        return true;
    }

    /**
     * cannot pre-segment the fire support areas, must post segment them after
     * the pixels were calculated
     *
     * @param tg
     * @param converter
     */
    protected static void postSegmentFSA(TGLight tg,
            IPointConversion converter) {
        try {
            if (tg.get_Client().equals("2D")) {
                return;
            }

            int linetype = tg.get_LineType();
            switch (linetype) {
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    break;
                default:
                    return;
            }
            ArrayList<POINT2> latLongs = new ArrayList();
            ArrayList<POINT2> resultPts = new ArrayList();
            int j = 0, k = 0, n = 0;
            POINT2 pt0 = null, pt1 = null, pt = null;
            double dist = 0;
            //double interval=1000000;
            double interval = 250000;
            double az = 0;

            double maxDist = 0;
            Point2D pt2d = null;
            int t = tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for (j = 0; j < t; j++) {
                pt0 = tg.Pixels.get(j);
                pt2d = new Point2D.Double(pt0.x, pt0.y);
                pt2d = converter.PixelsToGeo(pt2d);
                pt0 = new POINT2(pt2d.getX(), pt2d.getY());
                latLongs.add(pt0);
            }
            t = latLongs.size();
            //for(j=0;j<latLongs.size()-1;j++)
            for (j = 0; j < t - 1; j++) {
                pt0 = latLongs.get(j);
                pt1 = latLongs.get(j + 1);
                pt1.style = -1;//end point
                az = mdlGeodesic.GetAzimuth(pt0, pt1);
                dist = mdlGeodesic.geodesic_distance(latLongs.get(j), latLongs.get(j + 1), null, null);
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }

            if (interval > maxDist) {
                interval = maxDist;
            }

            //for(j=0;j<latLongs.size()-1;j++)
            for (j = 0; j < t - 1; j++) {
                pt0 = new POINT2(latLongs.get(j));
                pt0.style = 0;//anchor point
                pt1 = new POINT2(latLongs.get(j + 1));
                pt1.style = 0;//anchor point point
                az = mdlGeodesic.GetAzimuth(pt0, pt1);
                dist = mdlGeodesic.geodesic_distance(latLongs.get(j), latLongs.get(j + 1), null, null);

                n = (int) (dist / interval);
                if (j == 0) {
                    resultPts.add(pt0);
                }

                for (k = 1; k <= n; k++) {
                    pt = mdlGeodesic.geodesic_coordinate(pt0, interval * k, az);
                    pt.style = -2;
                    //we do not want the last segment to be too close to the anchor point
                    //only add the segment point if it is a distance at least half the inteval
                    //from the 2nd anchor point
                    dist = mdlGeodesic.geodesic_distance(pt, pt1, null, null);
                    if (dist >= interval / 2) {
                        resultPts.add(pt);
                    }
                }
                //ad the 2nd anchor point
                resultPts.add(pt1);
            }
            latLongs = resultPts;
            tg.Pixels = armyc2.c5isr.RenderMultipoints.clsUtility.LatLongToPixels(latLongs, converter);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "postSegmentFSA",
                    new RendererException("Failed inside postSegmentFSA", exc));
        }
    }

    /**
     * Similar to Vincenty algorithm for more accurate interpolation of geo
     * anchor points
     *
     * @return the interpolated points
     */
    private static ArrayList<POINT2> toGeodesic(TGLight tg, double interval, HashMap<Integer, String> hmap) {
        ArrayList<POINT2> locs = new ArrayList<POINT2>();
        try {
            int i = 0, k = 0, n = 0;
            ArrayList<POINT2> points = tg.LatLongs;
            String H = "";
            String color = "";
            boolean bolIsAC = false;
            int acWidth = 0;
            int linetype = tg.get_LineType();
            switch (linetype) {
                case TacticalLines.AC:
                case TacticalLines.LLTR:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.SAAFR:
                case TacticalLines.TC:
                case TacticalLines.SC:
                    bolIsAC = true;
                    break;
                default:
                    break;
            }
            for (i = 0; i < points.size() - 1; i++) {
                if(bolIsAC)
                    acWidth=points.get(i).style;
                // Convert coordinates from degrees to Radians
                //var lat1 = points[i].latitude * (PI / 180);
                //var lon1 = points[i].longitude * (PI / 180);
                //var lat2 = points[i + 1].latitude * (PI / 180);
                //var lon2 = points[i + 1].longitude * (PI / 180);                
                double lat1 = Math.toRadians(points.get(i).y);
                double lon1 = Math.toRadians(points.get(i).x);
                double lat2 = Math.toRadians(points.get(i + 1).y);
                double lon2 = Math.toRadians(points.get(i + 1).x);
                // Calculate the total extent of the route
                //var d = 2 * asin(sqrt(pow((sin((lat1 - lat2) / 2)), 2) + cos(lat1) * cos(lat2) * pow((sin((lon1 - lon2) / 2)), 2)));
                double d = 2 * Math.asin(Math.sqrt(Math.pow((Math.sin((lat1 - lat2) / 2)), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow((Math.sin((lon1 - lon2) / 2)), 2)));

                double dist = mdlGeodesic.geodesic_distance(points.get(i), points.get(i + 1), null, null);
                //double dist=d;
                float flt = (float) dist / (float) interval;
                n = Math.round(flt);
                if (n < 1) {
                    n = 1;
                }
                if (n > 32) {
                    n = 32;
                }
                // Calculate  positions at fixed intervals along the route
                for (k = 0; k <= n; k++) {
                    //we must preserve the anchor points
                    if (k == 0) {
                        locs.add(new POINT2(points.get(i)));
                        if (hmap != null && hmap.containsKey(i)) {
                            if (!H.isEmpty()) {
                                H += ",";
                            }
                            color = (String) hmap.get(i);
                            H += Integer.toString(locs.size() - 1) + ":" + color;
                        }
                        continue;
                    } else if (k == n) {
                        if (i == points.size() - 2) {
                            locs.add(new POINT2(points.get(i + 1)));
                            if (hmap != null && hmap.containsKey(i + 1)) {
                                if (!H.isEmpty()) {
                                    H += ",";
                                }
                                color = (String) hmap.get(i + 1);
                                H += Integer.toString(locs.size() - 1) + ":" + color;
                            }
                        }
                        break;
                    }
                    //var f = (k / n);
                    //var A = sin((1 - f) * d) / sin(d);
                    //var B = sin(f * d) / sin(d);
                    double f = ((double) k / (double) n);
                    double A = Math.sin((1 - f) * d) / Math.sin(d);
                    double B = Math.sin(f * d) / Math.sin(d);
                    // Obtain 3D Cartesian coordinates of each point
                    //var x = A * cos(lat1) * cos(lon1) + B * cos(lat2) * cos(lon2);
                    //var y = A * cos(lat1) * sin(lon1) + B * cos(lat2) * sin(lon2);
                    //var z = A * sin(lat1) + B * sin(lat2);
                    double x = A * Math.cos(lat1) * Math.cos(lon1) + B * Math.cos(lat2) * Math.cos(lon2);
                    double y = A * Math.cos(lat1) * Math.sin(lon1) + B * Math.cos(lat2) * Math.sin(lon2);
                    double z = A * Math.sin(lat1) + B * Math.sin(lat2);
                    // Convert these to latitude/longitude
                    //var lat = atan2(z, sqrt(pow(x, 2) + pow(y, 2)));
                    //var lon = atan2(y, x);
                    double lat = Math.atan2(z, Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
                    double lon = Math.atan2(y, x);
                    lat *= 180.0 / Math.PI;
                    lon *= 180.0 / Math.PI;
                    POINT2 pt = new POINT2(lon, lat);
                    if(bolIsAC)
                        pt.style=-acWidth;
                    locs.add(pt);
                    if (hmap != null && hmap.containsKey(i)) {
                        if (!H.isEmpty()) {
                            H += ",";
                        }
                        color = (String) hmap.get(i);
                        H += Integer.toString(locs.size() - 1) + ":" + color;
                    }
                }
            }
            if (!H.isEmpty()) {
                tg.set_H(H);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "toGeodesic",
                    new RendererException("Failed inside toGeodesic", exc));
            return null;
        }
        return locs;
    }

    /**
     * Pre-segment the lines based on max or min latitude for the segment
     * interval. This is necessary because GeoPixelconversion does not work well
     * over distance greater than 1M meters, especially at extreme latitudes.
     *
     * @param tg
     * @param converter
     */
    protected static void SegmentGeoPoints(TGLight tg,
            IPointConversion converter,
            double zoomFactor) {
        try {
            if (tg.get_Client().equals("2D")) {
                return;
            }

            ArrayList<POINT2> resultPts = new ArrayList();
            int lineType = tg.get_LineType();
            //double interval=1000000;
            double interval = 250000;
            boolean bolSegmentAC = false, bolIsAC = false;
            bolSegmentAC = true;
            //conservative interval in meters
            //return early for those lines not requiring pre-segmenting geo points
            switch (lineType) {
                case TacticalLines.AC:
                case TacticalLines.LLTR:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.SAAFR:
                case TacticalLines.TC:
                case TacticalLines.SC:
                    if (!bolSegmentAC) {
                        return;
                    }
                    bolIsAC = true;
                    break;
                case TacticalLines.PLD:
                case TacticalLines.CFL:
                case TacticalLines.UNSP:
                case TacticalLines.TRIPLE:
                case TacticalLines.DOUBLEC:
                case TacticalLines.SINGLEC:
                case TacticalLines.ATDITCH:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.ATWALL:
                case TacticalLines.LINE:
                case TacticalLines.DIRATKAIR:
                case TacticalLines.STRONG:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.FLOT:
                case TacticalLines.ZONE:
                case TacticalLines.OBSAREA:
                case TacticalLines.OBSFAREA:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.FORTL:
                    break;
                case TacticalLines.HWFENCE:
                case TacticalLines.LWFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.DFENCE:
                case TacticalLines.SFENCE:
                    interval = 500000;
                    break;
                case TacticalLines.LC:
                    interval = 2000000;
                    break;
                default:
                    //if the line is an anticipated generic line then segment the line
                    if (segmentAnticipatedLine(tg)) {
                        break;
                    }
                    return;
            }

            int j = 0, k = 0, n = 0;
            POINT2 pt0 = null, pt1 = null, pt = null;
            double dist = 0;
            double az = 0;

            double maxDist = 0;
            int t = tg.LatLongs.size();
            //for(j=0;j<tg.LatLongs.size()-1;j++)
            for (j = 0; j < t - 1; j++) {
                pt0 = tg.LatLongs.get(j);
                pt1 = tg.LatLongs.get(j + 1);
                if(!bolIsAC)
                    pt1.style = -1;//end point
                az = mdlGeodesic.GetAzimuth(pt0, pt1);
                dist = mdlGeodesic.geodesic_distance(tg.LatLongs.get(j), tg.LatLongs.get(j + 1), null, null);
                if (dist > maxDist) {
                    maxDist = dist;
                }
            }

            if (interval > maxDist) {
                interval = maxDist;
            }

            if (zoomFactor > 0 && zoomFactor < 0.01) {
                zoomFactor = 0.01;
            }
            if (zoomFactor > 0 && zoomFactor < 1) {
                interval *= zoomFactor;
            }

            boolean useVincenty = false;
            String H = "";
            String color = "";
            HashMap<Integer, String> hmap = clsUtility.getMSRSegmentColorStrings(tg);
            if (hmap != null) {
                tg.set_H("");
            }
            //uncomment one line to use (similar to) Vincenty algorithm
            useVincenty = true;
            if (useVincenty) {
                resultPts = toGeodesic(tg, interval, hmap);
                tg.LatLongs = resultPts;
                tg.Pixels = armyc2.c5isr.RenderMultipoints.clsUtility.LatLongToPixels(tg.LatLongs, converter);
                return;
            }

            for (j = 0; j < tg.LatLongs.size() - 1; j++) {
                pt0 = new POINT2(tg.LatLongs.get(j));
                pt0.style = 0;//anchor point
                pt1 = new POINT2(tg.LatLongs.get(j + 1));
                pt1.style = 0;//anchor point point
                az = mdlGeodesic.GetAzimuth(pt0, pt1);
                dist = mdlGeodesic.geodesic_distance(tg.LatLongs.get(j), tg.LatLongs.get(j + 1), null, null);

                n = (int) (dist / interval);
                if (j == 0) {
                    resultPts.add(pt0);
                    if (hmap != null && hmap.containsKey(j)) {
                        if (!H.isEmpty()) {
                            H += ",";
                        }
                        color = (String) hmap.get(j);
                        //H+=(resultPts.size()-1).toString()+":"+color;
                        H += Integer.toString(resultPts.size() - 1) + ":" + color;
                    }
                }
                for (k = 1; k <= n; k++) {
                    pt = mdlGeodesic.geodesic_coordinate(pt0, interval * k, az);
                    pt.style = -2;
                    //we do not want the last segment to be too close to the anchor point
                    //only add the segment point if it is a distance at least half the inteval
                    //from the 2nd anchor point
                    dist = mdlGeodesic.geodesic_distance(pt, pt1, null, null);
                    if (dist >= interval / 2) {
                        resultPts.add(pt);
                        if (hmap != null && hmap.containsKey(j)) {
                            color = (String) hmap.get(j);
                            if (!H.isEmpty()) {
                                H += ",";
                            }
                            //H+=(resultPts.size()-1).toString()+":"+color;
                            H += Integer.toString(resultPts.size() - 1) + ":" + color;
                        }
                    }
                }
                //ad the 2nd anchor point
                resultPts.add(pt1);
                if (hmap != null && hmap.containsKey(j + 1)) {
                    if (!H.isEmpty()) {
                        H += ",";
                    }
                    color = (String) hmap.get(j + 1);
                    //H+=(resultPts.size()-1).toString()+":"+color;
                    H += Integer.toString(resultPts.size() - 1) + ":" + color;
                }
            }
            if (!H.isEmpty()) {
                tg.set_H(H);
            }
            tg.LatLongs = resultPts;
            tg.Pixels = armyc2.c5isr.RenderMultipoints.clsUtility.LatLongToPixels(tg.LatLongs, converter);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "SegmentGeoPoints",
                    new RendererException("Failed inside SegmentGeoPoints", exc));
        }
    }

}
