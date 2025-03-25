/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.JavaLineArray;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;

import java.io.*;

import armyc2.c5isr.renderer.utilities.IPointConversion;
import armyc2.c5isr.JavaTacticalRenderer.mdlGeodesic;

/**
 * A class to provide the utility functions required for calculating the line
 * points.
 *
 * 
 */
public final class lineutility {

    private static final String _className = "lineutility";
    public static final int extend_left = 0;
    public static final int extend_right = 1;
    public static final int extend_above = 2;
    public static final int extend_below = 3;

    /**
     * Resizes the array to the length speicifed, called by the Channels class.
     *
     * @param pLinePoints the array to resize
     * @param length the length to which to resize the array.
     * @return the resized array
     */
    protected static POINT2[] ResizeArray(POINT2[] pLinePoints, int length) {
        POINT2[] array = new POINT2[length];
        try {
            if (pLinePoints.length <= length) {
                return pLinePoints;
            }

            int j = 0;
            for (j = 0; j < length; j++) {
                array[j] = new POINT2(pLinePoints[j]);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ResizeArray",
                    new RendererException("Failed inside ResizeArray", exc));
        }
        return array;
    }

    /**
     * post-segments a line segment into 50 pixel intervals
     *
     * @param pt0
     * @param pt1
     * @param shape
     */
    protected static void SegmentLineShape(POINT2 pt0, POINT2 pt1, Shape2 shape) {
        try {
            if (pt0 == null || pt1 == null) {
                return;
            }

            int j = 0, n = 0;
            double dist = CalcDistanceDouble(pt0, pt1);
            n = (int) (dist / 25d);
            POINT2 pt = null;
            shape.lineTo(pt0);
            for (j = 1; j <= n; j++) {
                pt = lineutility.ExtendAlongLineDouble(pt0, pt1, 25);
                shape.lineTo(pt);
            }
            shape.lineTo(pt1);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "SegmentLineShape",
                    new RendererException("Failed inside SegmentLineShape", exc));
        }
    }

    /**
     * Calculates the middle segment for the Direction of Attack Aviation symbol
     *
     * @param pLinePoints the point array
     * @param vblSaveCounter the size of the point array
     * @return the middle segment
     */
    public static int GetDirAtkAirMiddleSegment(POINT2[] pLinePoints,
            int vblSaveCounter) {
        int middleSegment = -1;
        try {
            double d = 0;
            int k = 0;
            for (k = vblSaveCounter - 1; k > 0; k--) {
                d += lineutility.CalcDistanceDouble(pLinePoints[k], pLinePoints[k - 1]);
                if (d > 60) {
                    break;
                }
            }
            if (d > 60) {
                middleSegment = k;
            } else {
                if (vblSaveCounter <= 3) {
                    middleSegment = 1;
                } else {
                    middleSegment = 2;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetDirAtkAirMiddleSegment",
                    new RendererException("Failed inside GetDirAtkAirMiddleSegment", exc));
        }
        return middleSegment;
    }

    /**
     * Computes the angle in radians between two points
     *
     * @param pt0 the first point
     * @param pt1 the last point
     *
     * @return the angle in radians
     */
    protected static double CalcSegmentAngleDouble(POINT2 pt0,
            POINT2 pt1) {
        double dAngle = 0;
        try {
            //declarations
            int nTemp = 0;
            ref<double[]> m = new ref();
            //end declarations

            nTemp = CalcTrueSlopeDouble(pt0, pt1, m);
            if (nTemp == 0) {
                dAngle = Math.PI / 2;
            } else {
                dAngle = Math.atan(m.value[0]);
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcSegmentAngleDouble",
                    new RendererException("Failed inside CalcSegmentAngleDouble", exc));
        }
        return dAngle;
    }

    /**
     * POINT2 in previous applications has been a struct that did not require
     * initialization.
     *
     * @param pts array of points to instantiate.
     */
    protected static void InitializePOINT2Array(POINT2[] pts) {
        //int j=0;
        if (pts == null || pts.length == 0) {
            return;
        }
        int n=pts.length;
        //for (int j = 0; j < pts.length; j++) 
        for (int j = 0; j < n; j++) 
        {
            pts[j] = new POINT2();
        }
    }

    /**
     * Calculates the center point of an area using the first vblCounter points
     * in the array.
     *
     * @param pLinePoints the client points
     * @param vblCounter the number of points in the array to use
     *
     * @return the center point
     */
    protected static POINT2 CalcCenterPointDouble(POINT2[] pLinePoints,
            int vblCounter) {
        POINT2 CenterLinePoint = new POINT2(pLinePoints[0]);
        try {
            //declarations
            int j = 0;
            double dMinX = pLinePoints[0].x,
                    dMinY = pLinePoints[0].y,
                    dMaxX = pLinePoints[0].x,
                    dMaxY = pLinePoints[0].y;

            //end declarations
            dMinX = pLinePoints[0].x;
            dMinY = pLinePoints[0].y;
            dMaxX = pLinePoints[0].x;
            dMaxY = pLinePoints[0].y;

            for (j = 0; j < vblCounter; j++) {
                if (pLinePoints[j].x < dMinX) {
                    dMinX = pLinePoints[j].x;
                }

                if (pLinePoints[j].y < dMinY) {
                    dMinY = pLinePoints[j].y;
                }

                if (pLinePoints[j].x > dMaxX) {
                    dMaxX = pLinePoints[j].x;
                }

                if (pLinePoints[j].y > dMaxY) {
                    dMaxY = pLinePoints[j].y;
                }

            }	//end for

            CenterLinePoint.x = (dMinX + dMaxX) / 2;
            CenterLinePoint.y = (dMinY + dMaxY) / 2;
        } catch (Error exc) {
            ErrorLogger.LogException(_className, "CalcCenterPointDouble",
                    new RendererException("Failed inside CalcCenterPointDouble", exc));
        }
        return CenterLinePoint;
    }

    /**
     * Called by renderer Modifier2 class after ArrayList.ToArray was called,
     * which produces an array of objects.
     *
     * @param pLinePoints
     * @param vblCounter
     * @return
     */
    public static POINT2 CalcCenterPointDouble2(Object[] pLinePoints,
            int vblCounter) {
        POINT2 pt0 = (POINT2) pLinePoints[0];
        POINT2 CenterLinePoint = new POINT2();
        try {
            //declarations
            int j = 0;
            double dMinX = pt0.x,
                    dMinY = pt0.y,
                    dMaxX = pt0.x,
                    dMaxY = pt0.y;

            //end declarations
            dMinX = pt0.x;
            dMinY = pt0.y;
            dMaxX = pt0.x;
            dMaxY = pt0.y;

            POINT2 pt;

            for (j = 0; j < vblCounter; j++) {
                pt = (POINT2) pLinePoints[j];
                if (pt.x < dMinX) {
                    dMinX = pt.x;
                }

                if (pt.y < dMinY) {
                    dMinY = pt.y;
                }

                if (pt.x > dMaxX) {
                    dMaxX = pt.x;
                }

                if (pt.y > dMaxY) {
                    dMaxY = pt.y;
                }

            }	//end for

            CenterLinePoint.x = (dMinX + dMaxX) / 2;
            CenterLinePoint.y = (dMinY + dMaxY) / 2;
        } catch (Error exc) {
            ErrorLogger.LogException(_className, "CalcCenterPointDouble2",
                    new RendererException("Failed inside CalcCenterPointDouble2", exc));
        }
        return CenterLinePoint;
    }

    /**
     * Calculates the distance in pixels between two points
     *
     * @param p1 the first point
     * @param p2 the last point
     *
     * @return the distance between p1 and p2 in pixels
     */
    public static double CalcDistanceDouble(POINT2 p1,
            POINT2 p2) {
        double returnValue = 0;
        try {
            returnValue = Math.sqrt((p1.x - p2.x)
                    * (p1.x - p2.x)
                    + (p1.y - p2.y)
                    * (p1.y - p2.y));

            //sanity check
            //return x or y distance if returnValue is 0 or infinity
            double xdist = Math.abs(p1.x - p2.x);
            double ydist = Math.abs(p1.y - p2.y);
            double max = xdist;
            if (ydist > xdist) {
                max = ydist;
            }

            if (returnValue == 0 || Double.isInfinite(returnValue)) {
                if (max > 0) {
                    returnValue = max;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcDistanceDouble",
                    new RendererException("Failed inside CalcDistanceDouble", exc));
        }
        return returnValue;
    }

    /**
     * Calculates the distance in pixels between two points
     *
     * @param p1 the first point
     * @param p2 the last point
     *
     * @return the distance between p1 and p2 in pixels
     */
    public static double CalcDistanceDouble(Point2D p1,
                                            Point2D p2) {
        double returnValue = 0;
        try {
            returnValue = Math.sqrt((p1.getX() - p2.getX())
                    * (p1.getX() - p2.getX())
                    + (p1.getY() - p2.getY())
                    * (p1.getY() - p2.getY()));

            //sanity check
            //return x or y distance if returnValue is 0 or infinity
            double xdist = Math.abs(p1.getX() - p2.getX());
            double ydist = Math.abs(p1.getY() - p2.getY());
            double max = xdist;
            if (ydist > xdist) {
                max = ydist;
            }

            if (returnValue == 0 || Double.isInfinite(returnValue)) {
                if (max > 0) {
                    returnValue = max;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcDistanceDouble",
                    new RendererException("Failed inside CalcDistanceDouble", exc));
        }
        return returnValue;
    }

    /**
     * Computes the slope of a line
     *
     * @param firstLinePoint the first line point
     * @param lastLinePoint the last line point
     * @param slope OUT - object with member to hold the slope of the line
     *
     * @return 1 if successful, else return 0
     */
    protected static int CalcTrueSlopeDouble(POINT2 firstLinePoint,
            POINT2 lastLinePoint,
            ref<double[]> slope)//ref is a double
    {
        int result = 1;
        try {
            if (slope.value == null) {
                slope.value = new double[1];
            }

            double deltaX = 0, deltaY = 0;
            deltaX = firstLinePoint.x - lastLinePoint.x;
            //if (deltaX == 0) 
            if (Math.abs(deltaX) < 1) 
            {
                //deltaX = 1;
                if(deltaX>=0)
                    deltaX=1;
                else
                    deltaX=-1;
                result = 1;
            }
            deltaY = firstLinePoint.y - lastLinePoint.y;

            slope.value[0] = deltaY / deltaX;	//cannot blow up
        } catch (Error exc) {
            ErrorLogger.LogException(_className, "CalcTrueSlopeDouble",
                    new RendererException("Failed inside CalcTrueSlopeDouble", exc));
        }
        return result;
    }

    public static void WriteFile(String str) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("Test.txt"));
            bufferedWriter.write(str);
            bufferedWriter.close();
            bufferedWriter = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "WriteFile",
                    new RendererException("Failed inside WriteFile", exc));
        }
    }

    /**
     * reverses the first vblCounter points
     *
     * @param pLowerLinePoints OUT - points to reverse
     * @param vblCounter
     */
    protected static void ReversePointsDouble2(POINT2[] pLowerLinePoints,
            int vblCounter) {
        try {
            POINT2[] pResultPoints = new POINT2[vblCounter];
            int k = 0;
            for (k = 0; k < vblCounter; k++) {
                pResultPoints[k] = new POINT2(pLowerLinePoints[vblCounter - k - 1]);
            }
            for (k = 0; k < vblCounter; k++) {
                pLowerLinePoints[k] = new POINT2(pResultPoints[k]);
            }
            pResultPoints = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ReversePointsDouble2",
                    new RendererException("Failed inside ReversePointsDouble2", exc));
        }
    }

    public static boolean CalcTrueSlopeDoubleForRoutes(POINT2 firstLinePoint,
            POINT2 lastLinePoint,
            ref<double[]> slope) {
        try {
            double deltaX = 0, deltaY = 0;
            deltaX = (double) (firstLinePoint.x) - (double) (lastLinePoint.x);
            if (Math.abs(deltaX) < 2) //was 2,infinite slope
            {
                return (false);
            }

            deltaY = (double) (firstLinePoint.y) - (double) (lastLinePoint.y);
            if (slope.value == null) {
                slope.value = new double[1];
            }

            slope.value[0] = deltaY / deltaX;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcTrueSlopeDoubleForRoutes",
                    new RendererException("Failed inside CalcTrueSlopeDoubleForRoutes", exc));
        }
        return true;
    }

    /**
     * Computes the slope of a line
     *
     * @param firstLinePoint the first line point
     * @param lastLinePoint the last line point
     * @param slope OUT - object with member to hold the slope of the line
     *
     * @return true if successful
     */
    public static boolean CalcTrueSlopeDouble2(POINT2 firstLinePoint,
            POINT2 lastLinePoint,
            ref<double[]> slope) {
        Boolean result = true;
        try {
            double deltaX = 0, deltaY = 0;
            deltaX = (double) (firstLinePoint.x) - (double) (lastLinePoint.x);
            //if (deltaX == 0)
            if (Math.abs(deltaX) < 1) 
            {
                //deltaX = 1;
                if(deltaX>=0)
                    deltaX=1;
                else
                    deltaX=-1;
                result = false;
            }

            deltaY = (double) (firstLinePoint.y) - (double) (lastLinePoint.y);
            if (slope.value == null) {
                slope.value = new double[1];
            }

            slope.value[0] = deltaY / deltaX;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcTrueSlopeDouble2",
                    new RendererException("Failed inside CalcTrueSlopeDouble2", exc));
        }
        return result;
    }

    /**
     * Calculates the slopes and y intercepts in pixels for the line from pt1 to
     * pt2 and a parallel line a vertical distance from the line
     *
     * @param nDistance the distance in pixels
     * @param linePoint1 first point on the line
     * @param linePoint2 last point on the line
     * @param pdResult OUT - array to hold m, b for both lines
     *
     * @return 1 if the lines are not vertical, else return 0
     */
    protected static int CalcTrueLinesDouble(long nDistance,
            POINT2 linePoint1,
            POINT2 linePoint2,
            ref<double[]> pdResult) //for vertical line e.g. if line equation is x=7
    {
        try {
            //declarations
            int nTemp = 0;
            double b = 0;
            double delta = 0;
            ref<double[]> m = new ref();
            //end declarations
            nTemp = CalcTrueSlopeDouble(linePoint1, linePoint2, m);
            pdResult.value = new double[6];
            //Fill the result array with the line parameters
            if (nTemp == 0) //vertical lines
            {
                pdResult.value[3] = linePoint1.x + (double) nDistance;	//the lower line eqn, e.g. x=7
                pdResult.value[5] = linePoint1.x - (double) nDistance;	//the upper line eqn,
                return 0;
            } else {
                b = linePoint2.y - m.value[0] * linePoint2.x;
                delta = Math.sqrt(m.value[0] * m.value[0] * ((double) (nDistance) * (double) (nDistance))
                        + ((double) (nDistance) * (double) (nDistance)));
                pdResult.value[0] = m.value[0];    //original line eq'n: y = mx + b
                pdResult.value[1] = b;
                pdResult.value[2] = m.value[0];    //lower line eq'n: y = mx + (b+dDistance)
                pdResult.value[3] = b + delta;
                pdResult.value[4] = m.value[0];    //upper line eq'n: y = mx + (b-dDistance)
                pdResult.value[5] = b - delta;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcTrueLinesDouble",
                    new RendererException("Failed inside CalcTrueLinesDouble", exc));
        }
        return 1;
    }

    /**
     * Calculates the intersection of two lines.
     *
     * @param m1 slope of first line
     * @param b1 Y intercept of first line
     * @param m2 slope of second line
     * @param b2 Y intercept of second line
     * @param bolVertical1 0 if first line is vertical, else 1
     * @param bolVertical2 0 if second line is vertical, else 1
     * @param X1 X intercept if first line is vertical
     * @param X2 X intercept if 2nd line is vertical.
     *
     * @return intersection point
     */
    public static POINT2 CalcTrueIntersectDouble2(double m1,
            double b1,
            double m2,
            double b2,
            int bolVertical1,
            int bolVertical2,
            double X1, //x intercept if line1 is vertical
            double X2) {
        POINT2 ptIntersect = new POINT2();
        try {
            //declarations
            double x = 0, y = 0;
            //end declarations

            //initialize ptIntersect
            ptIntersect.x = X1;
            ptIntersect.y = X2;
            if (bolVertical1 == 0 && bolVertical2 == 0) //both lines vertical
            {
                return ptIntersect;
            }
            //the following 3 if blocks are the only ways to get an intersection
            if (bolVertical1 == 0 && bolVertical2 == 1) //line1 vertical, line2 not
            {
                ptIntersect.x = X1;
                ptIntersect.y = m2 * X1 + b2;
                return ptIntersect;
            }
            if (bolVertical1 == 1 && bolVertical2 == 0) //line2 vertical, line1 not
            {
                ptIntersect.x = X2;
                ptIntersect.y = m1 * X2 + b1;
                return ptIntersect;
            }
            //if either of the lines is vertical function has already returned
            //so both m1 and m2 should be valid
            if (m1 != m2) {
                x = (b2 - b1) / (m1 - m2);	//cannot blow up
                y = (m1 * x + b1);
                ptIntersect.x = x;
                ptIntersect.y = y;
                return ptIntersect;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcTrueIntersectDouble2",
                    new RendererException("Failed inside CalcTrueIntersectDouble2", exc));
        }
        return ptIntersect;
    }

    /**
     * Calculates an offset point for channel types which require arrows.
     *
     * @param startLinePoint the first point
     * @param endLinePoint the last point
     * @param nOffset the offset in pixels
     *
     * @return the offset point
     */
    protected static POINT2 GetOffsetPointDouble(POINT2 startLinePoint,
            POINT2 endLinePoint,
            long nOffset) {
        POINT2 tempLinePoint = new POINT2(startLinePoint);
        try {
            //declarations
            double dx = endLinePoint.x - startLinePoint.x,
                    dy = endLinePoint.y - startLinePoint.y,
                    dOffset = (double) nOffset,
                    dHypotenuse = 0,
                    dAngle = 0;

            //end declarations
            if (dx == 0) {
                if (dy > 0) {
                    tempLinePoint.x = endLinePoint.x;
                    tempLinePoint.y = endLinePoint.y + dOffset;
                } else {
                    tempLinePoint.x = endLinePoint.x;
                    tempLinePoint.y = endLinePoint.y - dOffset;
                }
                return tempLinePoint;
            }
            if (dy == 0) {
                if (dx > 0) {
                    tempLinePoint.x = endLinePoint.x + dOffset;
                    tempLinePoint.y = endLinePoint.y;
                } else {
                    tempLinePoint.x = endLinePoint.x - dOffset;
                    tempLinePoint.y = endLinePoint.y;
                }
                return tempLinePoint;
            }

            if (dy == 0) {
                dAngle = 0;
            } else {
                dAngle = Math.atan(dx / dy) + Math.PI / 2;//1.570795;
            }
            dHypotenuse = (double) nOffset;
            if (endLinePoint.x > startLinePoint.x) {
                tempLinePoint.x = endLinePoint.x + dHypotenuse * Math.abs(Math.cos(dAngle));
            } else {
                tempLinePoint.x = endLinePoint.x - dHypotenuse * Math.abs(Math.cos(dAngle));
            }
            if (endLinePoint.y > startLinePoint.y) {
                tempLinePoint.y = endLinePoint.y + dHypotenuse * Math.abs(Math.sin(dAngle));
            } else {
                tempLinePoint.y = endLinePoint.y - dHypotenuse * Math.abs(Math.sin(dAngle));
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetOffsetPointDouble",
                    new RendererException("Failed inside GetOffsetPointDouble", exc));
        }
        return (tempLinePoint);
    }

    /**
     * Used for DMAF
     *
     * @param pLinePoints the client points
     * @return ArrayList of X points
     */
    protected static ArrayList LineOfXPoints(TGLight tg, POINT2[] pLinePoints) {
        ArrayList xPoints = new ArrayList();
        try {
            int j = 0, k = 0;
            double dist = 0;
            int iterations = 0;
            POINT2 frontPt = null, backPt = null;
            POINT2 extendFrontAbove = null, extendFrontBelow = null;
            POINT2 extendBackAbove = null, extendBackBelow = null;
            POINT2 xPoint1 = null, xPoint2 = null;
            int n=pLinePoints.length;
            final double xSize = arraysupport.getScaledSize(5, tg.get_LineThickness());
            final double dIncrement = xSize * 4;
            //for (j = 0; j < pLinePoints.length - 1; j++) 
            for (j = 0; j < n - 1; j++) 
            {
                dist = CalcDistanceDouble(pLinePoints[j], pLinePoints[j + 1]);
                iterations = (int) ((dist - xSize) / dIncrement);
                if (dist - iterations * dIncrement > dIncrement / 2) {
                    iterations += 1;
                }

                for (k = 0; k < iterations; k++) {
                    frontPt = ExtendAlongLineDouble(pLinePoints[j], pLinePoints[j + 1], k * dIncrement - xSize);
                    backPt = ExtendAlongLineDouble(pLinePoints[j], pLinePoints[j + 1], k * dIncrement + xSize);
                    extendFrontAbove = ExtendDirectedLine(pLinePoints[j], pLinePoints[j + 1], frontPt, 2, xSize);
                    extendFrontBelow = ExtendDirectedLine(pLinePoints[j], pLinePoints[j + 1], frontPt, 3, xSize);
                    extendBackAbove = ExtendDirectedLine(pLinePoints[j], pLinePoints[j + 1], backPt, 2, xSize);
                    extendBackBelow = ExtendDirectedLine(pLinePoints[j], pLinePoints[j + 1], backPt, 3, xSize);
                    xPoints.add(extendFrontAbove);
                    extendBackBelow.style = 5;
                    xPoints.add(extendBackBelow);
                    xPoints.add(extendBackAbove);
                    extendFrontBelow.style = 5;
                    xPoints.add(extendFrontBelow);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "LineOfXPoints",
                    new RendererException("Failed inside LineOfXPoints", exc));
        }
        return xPoints;
    }

    /**
     * Computes the distance in pixels of pt3 to the line from pt1 to pt2.
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt3 point distance to compute
     * @return distance to pt3
     */
    public static double CalcDistanceToLineDouble(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt3) {
        double dResult = 0;
        try {
            //declarations
            double m1 = 1, b = 0, b1 = 0;
            POINT2 ptIntersect = new POINT2(pt1);
            int bolVertical = 0;
            ref<double[]> m = new ref();
            //end declarations

            bolVertical = CalcTrueSlopeDouble(pt1, pt2, m);

            //get line y intercepts
            if (bolVertical != 0 && m.value[0] != 0) {
                m1 = -1 / m.value[0];
                b = pt1.y - m.value[0] * pt1.x;
                b1 = pt3.y - m1 * pt3.x;
                ptIntersect = CalcTrueIntersectDouble2(m.value[0], b, m1, b1, 1, 1, ptIntersect.x, ptIntersect.y);
            }
            if (bolVertical != 0 && m.value[0] == 0) //horizontal line
            {
                ptIntersect.y = pt1.y;
                ptIntersect.x = pt3.x;
            }
            if (bolVertical == 0) //vertical line
            {
                ptIntersect.y = pt3.y;
                ptIntersect.x = pt1.x;
            }

            dResult = CalcDistanceDouble(pt3, ptIntersect);
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "CaclDistanceToLineDouble",
                    new RendererException("Failed inside CalcDistanceToLineDouble", exc));
        }
        return dResult;
    }

    /**
     * Calculates a point along a line. Returns the past point if the distance
     * is 0.
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param dist extension distance in pixels from the beginning of the line
     *
     * @return the extension point
     */
    public static POINT2 ExtendLineDouble(POINT2 pt1,
            POINT2 pt2,
            double dist) {
        POINT2 pt3 = new POINT2();
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);
            if (dOriginalDistance == 0 || dist == 0) {
                return pt2;
            }

            pt3.x = (dOriginalDistance + dist) / dOriginalDistance * (pt2.x - pt1.x) + pt1.x;
            pt3.y = (dOriginalDistance + dist) / dOriginalDistance * (pt2.y - pt1.y) + pt1.y;
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendLineDouble",
                    new RendererException("Failed inside ExtendLineDouble", exc));
        }
        return pt3;
    }

    /**
     * Extends a point along a line. If dist is 0 returns last point.
     *
     * @param pt1 first point on the line
     * @param pt2 last point on the line
     * @param dist the distance in pixels from pt1
     *
     * @return the extended point
     */
    public static POINT2 ExtendAlongLineDouble(POINT2 pt1, POINT2 pt2, double dist) {
        POINT2 pt3 = new POINT2();
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);
            if (dOriginalDistance == 0 || dist == 0) {
                return pt2;
            }

            pt3.x = ((dist / dOriginalDistance) * (pt2.x - pt1.x) + pt1.x);
            pt3.y = ((dist / dOriginalDistance) * (pt2.y - pt1.y) + pt1.y);
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendAlongLineDouble",
                    new RendererException("Failed inside ExtendAlongLineDouble", exc));
        }
        return pt3;
    }

    public static POINT2 ExtendAlongLineDouble2(POINT2 pt1, POINT2 pt2, double dist) {
        POINT2 pt3 = new POINT2();
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);
            if (dOriginalDistance == 0 || dist == 0) {
                return pt1;
            }

            pt3.x = (dist / dOriginalDistance * (pt2.x - pt1.x) + pt1.x);
            pt3.y = (dist / dOriginalDistance * (pt2.y - pt1.y) + pt1.y);
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendAlongLineDouble2",
                    new RendererException("Failed inside ExtendAlongLineDouble2", exc));
        }
        return pt3;
    }

    public static Point2D ExtendAlongLineDouble2(Point2D pt1, Point2D pt2, double dist) {
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);
            if (dOriginalDistance == 0 || dist == 0) {
                return new Point2D.Double(pt1.getX(), pt1.getY());
            }

            double x = (dist / dOriginalDistance * (pt2.getX() - pt1.getX()) + pt1.getX());
            double y = (dist / dOriginalDistance * (pt2.getY() - pt1.getY()) + pt1.getY());
            return new Point2D.Double(x, y);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtendAlongLineDouble2",
                    new RendererException("Failed inside ExtendAlongLineDouble2", exc));
        }
        return new Point2D.Double(0, 0);
    }

    public static POINT2 ExtendAlongLineDouble(POINT2 pt1, POINT2 pt2, double dist, int styl) {
        POINT2 pt3 = new POINT2();
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);
            if (dOriginalDistance == 0 || dist == 0) {
                return pt2;
            }

            pt3.x = (dist / dOriginalDistance * (pt2.x - pt1.x) + pt1.x);
            pt3.y = (dist / dOriginalDistance * (pt2.y - pt1.y) + pt1.y);
            pt3.style = styl;
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendAlongLineDouble",
                    new RendererException("Failed inside ExtendAlongLineDouble", exc));
        }
        return pt3;
    }

    /**
     * Extends a point above a line
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt3 point at which to extend
     * @param d distance in pixels to extend above the line
     * @param X OUT - extended point x value
     * @param Y OUT - extended point y value
     * @param direction direction to extend the line
     *
     * @return 1 if successful, else return 0
     */
    protected static int ExtendLineAbove(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt3,
            double d,
            ref<double[]> X,
            ref<double[]> Y,
            int direction) {
        try {
            ref<double[]> m = new ref();
            double dx = 0, dy = 0;
            int bolVertical = 0;

            X.value = new double[1];
            Y.value = new double[1];

            bolVertical = CalcTrueSlopeDouble(pt1, pt2, m);
            if (bolVertical == 0) {
                return 0;	//cannot extend above a vertical line
            }
            if (m.value[0] == 0) {
                X.value[0] = pt3.x;
                if (direction == 0) //extend above the line
                {
                    Y.value[0] = pt3.y - Math.abs(d);
                } else //extend below the line
                {
                    Y.value[0] = pt3.y + Math.abs(d);
                }
                return 1;
            }
            //the line is neither vertical nor horizontal
            //else function would already have returned
            if (direction == 0) //extend above the line
            {
                dy = -Math.abs(d / (m.value[0] * Math.sqrt(1 + 1 / (m.value[0] * m.value[0]))));
            } else //extend below the line
            {
                dy = Math.abs(d / (m.value[0] * Math.sqrt(1 + 1 / (m.value[0] * m.value[0]))));
            }

            dx = -m.value[0] * dy;
            X.value[0] = pt3.x + dx;
            Y.value[0] = pt3.y + dy;
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendLineAbove",
                    new RendererException("Failed inside ExtendLineAbove", exc));
        }
        return 1;
    }

    /**
     * Extends a point to the left of a line
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt3 point at which to extend
     * @param d distance in pixels to extend above the line
     * @param X OUT - extended point x value
     * @param Y OUT - extended point y value
     * @param direction direction to extend the line
     *
     * @return 1 if successful, else return 0
     */
    protected static int ExtendLineLeft(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt3,
            double d,
            ref<double[]> X,
            ref<double[]> Y,
            int direction) {
        try {
            ref<double[]> m = new ref();
            double dx = 0, dy = 0;
            int bolVertical = 0;

            X.value = new double[1];
            Y.value = new double[1];

            bolVertical = CalcTrueSlopeDouble(pt1, pt2, m);
            if (bolVertical != 0 && m.value[0] == 0) {
                return 0;	//cannot left of horiz line
            }
            if (bolVertical == 0) //vertical line
            {
                Y.value[0] = pt3.y;
                if (direction == 0) //extend left of the line
                {
                    X.value[0] = pt3.x - Math.abs(d);
                } else //extend right of the line
                {
                    X.value[0] = pt3.x + Math.abs(d);
                }

                return 1;
            }
            //the line is neither vertical nor horizontal
            //else function would already have returned
            if (direction == 0) //extend left of the line
            {
                dx = -Math.abs(d / Math.sqrt(1 + 1 / (m.value[0] * m.value[0])));
            } else //extend right of the line
            {
                dx = Math.abs(d / Math.sqrt(1 + 1 / (m.value[0] * m.value[0])));
            }

            dy = -(1 / m.value[0]) * dx;

            X.value[0] = pt3.x + dx;
            Y.value[0] = pt3.y + dy;
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendLineLeft",
                    new RendererException("Failed inside ExtendLineLeft", exc));
        }
        return 1;
    }

    /**
     * Calculates the direction of a point relative to a line
     *
     * @param pt0 first point fo the line
     * @param pt1 last point of the line
     * @param pt2 relative point
     * @deprecated
     * @return 0 if left, 1 if right, 2 if above, 3 if below
     */
    protected static int CalcDirectionFromLine(POINT2 pt0,
            POINT2 pt1,
            POINT2 pt2) {
        int result = -1;
        try {
            double m2 = 0, b1 = 0, b2 = 0;
            ref<double[]> m1 = new ref();
            POINT2 ptIntersect = new POINT2();
            //int direction=-1;
            //handle vertical line
            if (pt0.x == pt1.x) {
                if (pt2.x < pt0.x) {
                    return 0;
                } else {
                    return 1;
                }
            }
            //handle horizontal line so that we do not have slope = 0.
            if (pt0.y == pt1.y) {
                if (pt2.y < pt0.y) {
                    return 2;
                } else {
                    return 3;
                }
            }
            CalcTrueSlopeDouble(pt0, pt1, m1);
            m2 = -1 / m1.value[0];	//slope for the perpendicular line from the line to pt2
            //b=mx-y line equation for line
            b1 = pt0.y - m1.value[0] * pt0.x;
            //b=mx-y line equation for perpendicular line which contains pt2
            b2 = pt2.y - m2 * pt2.x;
            ptIntersect = CalcTrueIntersectDouble2(m1.value[0], b1, m2, b2, 1, 1, 0, 0);
            //compare the intersection point with pt2 to get the direction,
            //i.e. the direction from the line is the same as the direction
            //from the interseciton point.
            if (m1.value[0] > 1) //line is steep, use left/right
            {
                if (pt2.x < ptIntersect.x) {
                    return 0;
                } else {
                    return 1;
                }
            } else //line is not steep, use above/below
            {
                if (pt2.y < ptIntersect.y) {
                    return 2;
                } else {
                    return 3;
                }
            }
            //should not reach this point
            //return direction;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     * Returns a point extended perpendicularly from a line at a given direction
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt0 on line from which to extend
     * @param direction the direction to extend: above, below, left, right
     * @param d the length to extend in pixels
     *
     */
    public static POINT2 ExtendDirectedLine(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt0,
            int direction,
            double d) {
        POINT2 ptResult = new POINT2();
        try {
            ref<double[]> X = new ref(), Y = new ref();
            ptResult = new POINT2(pt0);
            switch (direction) {
                case 0:	//extend left
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 1:	//extend right
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 1);
                    break;
                case 2:	//extend above
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 3:	//extend below
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 1);
                    break;
                default:
                    break;
            }
            ptResult.x = X.value[0];
            ptResult.y = Y.value[0];
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendDirectedLine",
                    new RendererException("Failed inside ExtendDirectedLine", exc));
        }
        return ptResult;
    }

    /**
     * @deprecated Returns a point extended perpendicularly from a line at a
     * given direction same as original function except it accounts for vertical
     * lines and negative d values
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt0 on line from which to extend
     * @param direction the direction to extend: above, below, left, right
     * @param d the length to extend in pixels
     *
     */
    public static POINT2 ExtendDirectedLineText(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt0,
            int direction,
            double d) {
        POINT2 ptResult = new POINT2();
        try {
            ref<double[]> X = new ref(), Y = new ref();
            ptResult = new POINT2(pt0);
            if (d < 0) {
                switch (direction) {
                    case 0:
                        direction = extend_right;
                        break;
                    case 1:
                        direction = extend_left;
                        break;
                    case 2:
                        direction = extend_below;
                        break;
                    case 3:
                        direction = extend_above;
                        break;
                    default:
                        break;
                }
                d = Math.abs(d);
            }
            if (pt1.y == pt2.y)//horizontal segment
            {
                switch (direction) {
                    case 0://left means above
                        direction = extend_above;
                        break;
                    case 1://right means below
                        direction = extend_below;
                        break;
                    default:
                        break;
                }
            }
            if (pt1.x == pt2.x)//vertical segment
            {
                switch (direction) {
                    case 2://above means left
                        direction = extend_left;
                        break;
                    case 3://below means right
                        direction = extend_right;
                        break;
                    default:
                        break;
                }
            }
            switch (direction) {
                case 0:	//extend left
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 1:	//extend right
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 1);
                    break;
                case 2:	//extend above
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 3:	//extend below
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 1);
                    break;
                default:
                    break;
            }
            ptResult.x = X.value[0];
            ptResult.y = Y.value[0];
        } catch (Exception exc) {
            //System.out.println(e.getMessage());
            ErrorLogger.LogException(_className, "ExtendDirectedLine",
                    new RendererException("Failed inside ExtendDirectedLine", exc));
        }
        return ptResult;
    }

    /**
     * Returns a point extended perpendicularly from a line at a given direction
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param pt0 on line from which to extend
     * @param direction the direction to extend: above, below, left, right
     * @param d the length to extend in pixels
     * @param style the style to assign the return point
     *
     */
    public static POINT2 ExtendDirectedLine(POINT2 pt1,
            POINT2 pt2,
            POINT2 pt0,
            int direction,
            double d,
            int style) {
        POINT2 ptResult = new POINT2(pt0);
        try {
            ref<double[]> X = new ref(), Y = new ref();
            //int bolResult=0;
            //handle parallel, perpendicular cases
            if (pt1.x == pt2.x) {
                if (direction == 2) {
                    direction = 0;
                }
                if (direction == 3) {
                    direction = 1;
                }
            }
            if (pt1.y == pt2.y) {
                if (direction == 0) {
                    direction = 2;
                }
                if (direction == 1) {
                    direction = 3;
                }
            }
            switch (direction) {
                case 0:	//extend left
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 1:	//extend right
                    ExtendLineLeft(pt1, pt2, pt0, d, X, Y, 1);
                    break;
                case 2:	//extend above
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 0);
                    break;
                case 3:	//extend below
                    ExtendLineAbove(pt1, pt2, pt0, d, X, Y, 1);
                    break;
            }
            ptResult.x = X.value[0];
            ptResult.y = Y.value[0];
            ptResult.style = style;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtendDirectedLine",
                    new RendererException("Failed inside ExtendDirectedLine", exc));
        }
        return ptResult;
    }

    /**
     * Calculates a point along a line
     *
     * @param pt1 first line point
     * @param pt2 last line point
     * @param dist extension distance in pixels from the beginning of the line
     * @param styl the line style to assign the point
     *
     * @return the extension point
     */
    protected static POINT2 ExtendLine2Double(POINT2 pt1,
            POINT2 pt2,
            double dist,
            int styl) {
        POINT2 pt3 = new POINT2();
        try {
            double dOriginalDistance = CalcDistanceDouble(pt1, pt2);

            pt3.x = pt2.x;
            pt3.y = pt2.y;
            if (dOriginalDistance > 0) {
                pt3.x = ((dOriginalDistance + dist) / dOriginalDistance * (pt2.x - pt1.x) + pt1.x);
                pt3.y = ((dOriginalDistance + dist) / dOriginalDistance * (pt2.y - pt1.y) + pt1.y);
                pt3.style = styl;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtendLine2Double",
                    new RendererException("Failed inside ExtendLine2Double", exc));
        }
        return pt3;
    }

    /**
     * Extends a point at an angle from a line.
     *
     * @param pt0 the first line point
     * @param pt1 the second line point
     * @param pt2 point on line from which to extend
     * @param alpha angle of extension in degrees
     * @param d the distance in pixels to extend
     *
     * @return the extension point
     */
    public static POINT2 ExtendAngledLine(POINT2 pt0,
            POINT2 pt1,
            POINT2 pt2,
            double alpha,
            double d) {
        POINT2 pt = new POINT2();
        try {
            //first get the angle psi between pt0 and pt1
            double psi = Math.atan((pt1.y - pt0.y) / (pt1.x - pt0.x));
            //convert alpha to radians
            double alpha1 = Math.PI * alpha / 180;

            //theta is the angle of extension from the x axis
            double theta = psi + alpha1;
            //dx is the x extension from pt2
            double dx = d * Math.cos(theta);
            //dy is the y extension form pt2
            double dy = d * Math.sin(theta);
            pt.x = pt2.x + dx;
            pt.y = pt2.y + dy;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtendAngledLine",
                    new RendererException("Failed inside ExtendAngledLine", exc));
        }
        return pt;
    }

    /**
     * Returns an integer indicating the quadrant for the direction of the line
     * from pt1 to pt2
     *
     * @param pt1 first line point
     * @param pt2 second line point
     *
     * @return the quadrant
     */
    public static int GetQuadrantDouble(POINT2 pt1,
            POINT2 pt2) {
        int nQuadrant = 1;
        try {
            if (pt2.x >= pt1.x && pt2.y <= pt1.y) {
                nQuadrant = 1;
            }
            if (pt2.x >= pt1.x && pt2.y >= pt1.y) {
                nQuadrant = 2;
            }
            if (pt2.x <= pt1.x && pt2.y >= pt1.y) {
                nQuadrant = 3;
            }
            if (pt2.x <= pt1.x && pt2.y <= pt1.y) {
                nQuadrant = 4;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetQuadrantDouble",
                    new RendererException("Failed inside GetQuadrantDouble", exc));
        }
        return nQuadrant;
    }

    public static int GetQuadrantDouble(double x1, double y1,
            double x2, double y2) {
        int nQuadrant = 1;
        try {
//            if(pt2.x>=pt1.x && pt2.y<=pt1.y)
//                    nQuadrant=1;
//            if(pt2.x>=pt1.x && pt2.y>=pt1.y)
//                    nQuadrant=2;
//            if(pt2.x<=pt1.x && pt2.y>=pt1.y)
//                    nQuadrant=3;
//            if(pt2.x<=pt1.x && pt2.y<=pt1.y)
//                    nQuadrant=4;

            if (x2 >= x1 && y2 <= y1) {
                nQuadrant = 1;
            }
            if (x2 >= x1 && y2 >= y1) {
                nQuadrant = 2;
            }
            if (x2 <= x1 && y2 >= y1) {
                nQuadrant = 3;
            }
            if (x2 <= x1 && y2 <= y1) {
                nQuadrant = 4;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetQuadrantDouble",
                    new RendererException("Failed inside GetQuadrantDouble", exc));
        }
        return nQuadrant;
    }

    /**
     * Returns the smallest x and y pixel values from an array of points
     *
     * @param ptsSeize array of points from which to find minimum vaules
     * @param vblCounter the number of points to test in the array
     * @param x OUT - an object with a member to hold the xminimum
     * @param y OUT - an object with a member to hold the y minimum value
     *
     */
    public static void GetPixelsMin(POINT2[] ptsSeize,
            int vblCounter,
            ref<double[]> x,
            ref<double[]> y) {
        try {
            double xmin = Double.POSITIVE_INFINITY;
            double ymin = Double.POSITIVE_INFINITY;
            int j = 0;

            for (j = 0; j < vblCounter; j++) {
                if (ptsSeize[j].x < xmin) {
                    xmin = ptsSeize[j].x;
                }
                if (ptsSeize[j].y < ymin) {
                    ymin = ptsSeize[j].y;
                }
            }
            x.value = new double[1];
            y.value = new double[1];
            x.value[0] = xmin;
            y.value[0] = ymin;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetPixelsMin",
                    new RendererException("Failed inside GetPixelsMin", exc));
        }
    }

    /**
     * Returns the largest x and y pixel values from an array of points
     *
     * @param ptsSeize array of points from which to find maximum values
     * @param vblCounter the number of points to test in the array
     * @param x OUT - an object with a member to hold the x maximum value
     * @param y OUT - an object with a member to hold the y maximum value
     *
     */
    public static void GetPixelsMax(POINT2[] ptsSeize,
                                    int vblCounter,
                                    ref<double[]> x,
                                    ref<double[]> y) {
        try {
            double xmax = Double.NEGATIVE_INFINITY;
            double ymax = Double.NEGATIVE_INFINITY;
            int j = 0;

            for (j = 0; j < vblCounter; j++) {
                if (ptsSeize[j].x > xmax) {
                    xmax = ptsSeize[j].x;
                }
                if (ptsSeize[j].y > ymax) {
                    ymax = ptsSeize[j].y;
                }
            }
            x.value = new double[1];
            y.value = new double[1];
            x.value[0] = xmax;
            y.value[0] = ymax;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetPixelsMax",
                    new RendererException("Failed inside GetPixelsMax", exc));
        }
    }

    /**
     * Returns center point for a clockwise arc to connect pts 1 and 2. Also
     * returns an extended point on the line between pt1 and the new center
     * Caller passes a POINT1 array of size 2 for ptsSeize, passes pt1 and pt2
     * in ptsSeize Returns the radius of the 90 degree arc between C (arc
     * center) and pt1
     *
     * @param ptsSeize OUT - two point array also used for the returned two
     * points
     *
     * @return the radius
     */
    protected static double CalcClockwiseCenterDouble(POINT2[] ptsSeize) {
        double dRadius = 0;
        try {
            //declarations
            POINT2 pt1 = new POINT2(ptsSeize[0]);
            POINT2 pt2 = new POINT2(ptsSeize[1]);
            POINT2 C = new POINT2(pt1), midPt = new POINT2(pt1);	//the center to calculate
            POINT2 E = new POINT2(pt1);	//the extended point to calculate
            POINT2 ptYIntercept = new POINT2(pt1);
            int nQuadrant = 1;
            double b = 0, b1 = 0, b2 = 0, dLength = 0;
            ref<double[]> m = new ref();
            int bolVertical = 0;
            ref<double[]> offsetX = new ref(), offsetY = new ref();
            POINT2[] ptsTemp = new POINT2[2];
            //end declarations

            //must offset the points if necessary because there will be calculations
            //extending from the Y Intercept
            ptsTemp[0] = new POINT2(pt1);
            ptsTemp[1] = new POINT2(pt2);
            GetPixelsMin(ptsTemp, 2, offsetX, offsetY);
            if (offsetX.value[0] < 0) {
                offsetX.value[0] = offsetX.value[0] - 100;
            } else {
                offsetX.value[0] = 0;
            }
            //end section

            midPt.x = (pt1.x + pt2.x) / 2;
            midPt.y = (pt1.y + pt2.y) / 2;
            dLength = CalcDistanceDouble(pt1, pt2);
            dRadius = dLength / Math.sqrt(2);
            nQuadrant = GetQuadrantDouble(pt1, pt2);

            bolVertical = CalcTrueSlopeDouble(pt1, pt2, m);
            if (bolVertical != 0 && m.value[0] != 0) //line not vertical or horizontal
            {
                b = pt1.y - m.value[0] * pt1.x;
                //y intercept of line perpendicular to midPt of pt,p2
                b1 = midPt.y + (1 / m.value[0]) * midPt.x;
                //we want to shift the Y axis to the left by offsetX
                //so we get the new Y intercept at x=offsetX
                b2 = (-1 / m.value[0]) * offsetX.value[0] + b1;
                ptYIntercept.x = offsetX.value[0];
                ptYIntercept.y = b2;
                switch (nQuadrant) {
                    case 1:
                    case 4:
                        C = ExtendLineDouble(ptYIntercept, midPt, dLength / 2);
                        break;
                    case 2:
                    case 3:
                        C = ExtendLineDouble(ptYIntercept, midPt, -dLength / 2);
                        break;
                    default:
                        break;
                }
            }
            if (bolVertical != 0 && m.value[0] == 0) //horizontal line
            {
                C.x = midPt.x;
                if (pt1.x < pt2.x) {
                    C.y = midPt.y + dLength / 2;
                } else {
                    C.y = midPt.y - dLength / 2;
                }
            }
            if (bolVertical == 0) //vertical line
            {
                ptYIntercept.x = offsetX.value[0];
                ptYIntercept.y = midPt.y;
                switch (nQuadrant) {
                    case 1:
                    case 4:
                        C = ExtendLineDouble(ptYIntercept, midPt, dLength / 2);
                        break;
                    case 2:
                    case 3:
                        C = ExtendLineDouble(ptYIntercept, midPt, -dLength / 2);
                        break;
                    default:
                        break;
                }
            }

            E = ExtendLineDouble(C, pt1, 50);
            ptsSeize[0] = new POINT2(C);
            ptsSeize[1] = new POINT2(E);

            ptsTemp = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcClockwiseCenterDouble",
                    new RendererException("Failed inside CalcClockwiseCenterDouble", exc));
        }
        return dRadius;
    }

    /**
     * Computes the points for an arrowhead based on a line segment
     *
     * @param startLinePoint segment start point
     * @param endLinePoint segment end point
     * @param nBiSector bisecotr in pixels
     * @param nBase base size in pixels
     * @param pResultLinePoints OUT - the arrowhead points
     * @param styl the line style to assign the last aroowhead point
     */
    protected static void GetArrowHead4Double(POINT2 startLinePoint,
            POINT2 endLinePoint,
            int nBiSector,
            int nBase,
            POINT2[] pResultLinePoints,
            int styl) {
        try {
            //declarations
            int j = 0;
            double dy = (double) (endLinePoint.y - startLinePoint.y),
                    dx = (double) (endLinePoint.x - startLinePoint.x),
                    dSign = 1.0,
                    AHBY = 0,
                    AHBX = 0,
                    AHBLY = 0,
                    AHBLX = 0,
                    AHBRY = 0,
                    AHBRX = 0,
                    dAngle = 0,
                    dHypotenuse = 0;

            POINT2 tempLinePoint = new POINT2(startLinePoint);
            //end declarations

            if (dy == 0) {
                if (dx > 0) {
                    dAngle = Math.PI;
                } else {
                    dAngle = 0;
                }
            } else {
                dAngle = Math.atan(dx / dy) + Math.PI / 2;
            }

            tempLinePoint.style = 0;//PS_SOLID;

            if (dx <= 0.0 && dy <= 0.0) {
                dSign = -1.0;
            }
            if (dx >= 0.0 && dy <= 0.0) {
                dSign = -1.0;
            }
            if (dx <= 0.0 && dy >= 0.0) {
                dSign = 1.0;
            }
            if (dx >= 0.0 && dy >= 0.0) {
                dSign = 1.0;
            }

            dHypotenuse = dSign * (double) nBiSector;

            //Find x, y for Arrow Head nBase startLinePoint POINT1
            AHBX = (double) endLinePoint.x + dHypotenuse * Math.cos(dAngle);
            AHBY = (double) endLinePoint.y - dHypotenuse * Math.sin(dAngle);

            //Half of the arrow head's length will be 10 units
            dHypotenuse = dSign * (double) (nBase / 2.0);

            //Find x, y of Arrow Head nBase Left side end POINT1
            AHBLX = AHBX - dHypotenuse * Math.sin(dAngle);
            AHBLY = AHBY - dHypotenuse * Math.cos(dAngle);

            //Find x, y of Arrow Head nBase Right side end POINT1
            AHBRX = AHBX + dHypotenuse * Math.sin(dAngle);
            AHBRY = AHBY + dHypotenuse * Math.cos(dAngle);

            //replacement, just trying to return the POINT1s
            tempLinePoint.x = (int) AHBLX;
            tempLinePoint.y = (int) AHBLY;
            pResultLinePoints[0] = new POINT2(tempLinePoint);
            pResultLinePoints[1] = new POINT2(endLinePoint);
            tempLinePoint.x = (int) AHBRX;
            tempLinePoint.y = (int) AHBRY;
            pResultLinePoints[2] = new POINT2(tempLinePoint);
            switch (styl) {
                case 0:
                    for (j = 0; j < 2; j++) {
                        pResultLinePoints[j].style = 0;
                    }
                    pResultLinePoints[2].style = 5;
                    break;
                case 9:
                    for (j = 0; j < 2; j++) {
                        pResultLinePoints[j].style = 9;
                    }
                    pResultLinePoints[2].style = 10;
                    break;
                case 18:
                    for (j = 0; j < 2; j++) {
                        pResultLinePoints[j].style = 18;
                    }
                    pResultLinePoints[2].style = 5;
                    break;
                default:
                    for (j = 0; j < 2; j++) {
                        pResultLinePoints[j].style = styl;
                    }
                    pResultLinePoints[2].style = 5;
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetArrowhead4Double",
                    new RendererException("Failed inside GetArrowhead4Double", exc));
        }
    }

    /**
     * Returns the midpoint between two points.
     *
     * @param pt0 the first point
     * @param pt1 the second point
     * @param styl the style to assign the mid point
     *
     * @return the mid point
     */
    public static POINT2 MidPointDouble(POINT2 pt0,
            POINT2 pt1,
            int styl) {
        POINT2 ptResult = new POINT2(pt0);
        try {
            ptResult.x = (pt0.x + pt1.x) / 2;
            ptResult.y = (pt0.y + pt1.y) / 2;
            ptResult.style = styl;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "MidPointDouble",
                    new RendererException("Failed inside MidPointDouble", exc));
        }
        return ptResult;
    }

    /**
     * Rotates an the first vblCounter points in the array about its first point
     *
     * @param pLinePoints OUT - the points to rotate
     * @param vblCounter the number of points to rotate
     * @param lAngle the angle in degrees to rotate
     *
     * @return pLinePoints
     */
    protected static POINT2[] RotateGeometryDoubleOrigin(POINT2[] pLinePoints,
            int vblCounter,
            int lAngle) {
        try {
            //declarations
            int j = 0;
            double dRotate = 0,
                    dTheta = 0,
                    dGamma = 0,
                    x = 0,
                    y = 0;
            //end declarations

            if (lAngle != 0) {
                POINT2 pdCenter = new POINT2();
                dRotate = (double) lAngle * Math.PI / 180d;
                //pdCenter = CalcCenterPointDouble(pLinePoints,vblCounter);
                pdCenter = new POINT2(pLinePoints[0]);

                for (j = 0; j < vblCounter; j++) {
                    dGamma = Math.PI + Math.atan((pLinePoints[j].y - pdCenter.y)
                            / (pLinePoints[j].x - pdCenter.x));

                    if (pLinePoints[j].x >= pdCenter.x) {
                        dGamma = dGamma + Math.PI;
                    }

                    dTheta = dRotate + dGamma;
                    y = CalcDistanceDouble(pLinePoints[j], pdCenter) * Math.sin(dTheta);
                    x = CalcDistanceDouble(pLinePoints[j], pdCenter) * Math.cos(dTheta);
                    pLinePoints[j].y = pdCenter.y + y;
                    pLinePoints[j].x = pdCenter.x + x;
                }	//end for

                return pLinePoints;
            }	//end if
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "RotateGeometryDoubleOrigin",
                    new RendererException("Failed inside RotateGeometryDoubleOrigin", exc));
        }
        return pLinePoints;
    }  // end function

    /**
     * Returns a point a distance d pixels perpendicular to the pt0-pt1 line and
     * going toward pt2
     *
     * @param pt0 the first line point
     * @param pt1 the second line point
     * @param pt2 the relative line point
     * @param d the distance in pixels
     * @param styl the linestyle to assign the computed point
     *
     * @return the extended point
     */
    public static POINT2 ExtendTrueLinePerpDouble(POINT2 pt0,
            POINT2 pt1,
            POINT2 pt2,
            double d,
            int styl) {
        POINT2 ptResult = new POINT2(pt0);
        try {
            POINT2 ptYIntercept = new POINT2(pt0);
            ref<double[]> m = new ref();
            double b = 0, b1 = 0;	//b is the normal Y intercept (at 0)
            int nTemp = 0;			//b1 is the y intercept at offsetX

            //must obtain x minimum to get the y-intercept to the left of
            //the left-most point
            ref<double[]> offsetX = new ref(), offsetY = new ref();
            POINT2[] pts = new POINT2[3];
            pts[0] = new POINT2(pt0);
            pts[1] = new POINT2(pt1);
            pts[2] = new POINT2(pt2);
            GetPixelsMin(pts, 3, offsetX, offsetY);

            if (offsetX.value[0] <= 0) //was < 0
            {
                offsetX.value[0] = offsetX.value[0] - 100;
            } else {
                offsetX.value[0] = 0;
            }
            //end section

            nTemp = CalcTrueSlopeDouble(pt0, pt1, m);
            switch (nTemp) {
                case 0:	//vertical line
                    if (pt0.y < pt1.y) {
                        ptResult.x = pt2.x - d;
                        ptResult.y = pt2.y;
                    } else {
                        ptResult.x = pt2.x + d;
                        ptResult.y = pt2.y;
                    }
                    break;
                default:	//non-vertical line
                    if (m.value[0] == 0) {
                        ptResult.x = pt2.x;
                        ptResult.y = pt2.y + d;
                    } else {
                        b = (double) pt2.y + (1 / m.value[0]) * (double) pt2.x;
                        //we need the y-intercept at the -offset
                        b1 = (-1 / m.value[0]) * offsetX.value[0] + b;
                        ptYIntercept.x = offsetX.value[0];
                        ptYIntercept.y = b1;
                        ptResult = ExtendLineDouble(ptYIntercept, pt2, d);
                    }
                    break;
            }
            ptResult.style = styl;
            pts = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtendTrueLinePerpDouble",
                    new RendererException("Failed inside ExtendTrueLinePerpDouble", exc));
        }
        return ptResult;
    }

    /**
     * Calculates the intersection of 2 lines pelative to a point. if one of the
     * lines is vertical use a distance dWidth above or below the line. pass
     * bolVertical1 = 1, or bolVertical2 = 1 if either line segment is vertical,
     * else pass 0. return the unique intersection in X,Y pointers. p2 is the
     * point that connects the 2 line segments to which the intersecting lines
     * are related, i.e. the intersecting lines are a distance dWidth pixels
     * above or below p2. uses dWidth and lOrient for cases in which at least
     * one of the lines is vertical. for normal lines this function assumes the
     * caller has passed the m, b for the appropriate upper or lower lines to
     * get the desired intgercept. this function is used for calculating the
     * upper and lower channel lines for channel types. For lOrient: see
     * comments in Channels.ConnectTrueDouble2
     *
     * @param m1 slope of the first line
     * @param b1 intercept of the first line
     * @param m2 slope of the second line
     * @param b2 y intercept of the second line
     * @param p2 point that connects the 2 line segments to which the
     * intersecting lines are related
     * @param bolVerticalSlope1 1 if first segment is vertical, else 0
     * @param bolVerticalSlope2 1 if second line segment is vertical, else 0
     * @param dWidth the distance of the intersecting lines from p2 in pixels
     * @param lOrient the orientation of the intersecting lines relative to the
     * segments connecting p2
     * @param X OUT - object holds the x value of the intersection point
     * @param Y OUT - object holds the y value of the intersection point
     */
    protected static int CalcTrueIntersectDouble(double m1,
            double b1,
            double m2,
            double b2,
            POINT2 p2, //can use for vertical lines
            int bolVerticalSlope1,
            int bolVerticalSlope2,
            double dWidth, //use for vertical lines, use + for upper line, - for lower line
            int lOrient,
            ref<double[]> X, //intersection x value
            ref<double[]> Y) //intersection y value
    {

        try {
            //case both lines are vertical
            double dWidth2 = Math.abs(dWidth);
            double b = 0;
            double dx = 0, dy = 0, m = 0;
            X.value = new double[1];
            Y.value = new double[1];

            //cannot get out of having to do this
            //the problem is caused by inexact slopes which are created by
            //clsLineUtility.DisplayIntersectPixels. This occurs when setting
            //pt2 or pt3 with X or Y on the boundary +/-maxPixels
            //if you try to walk out until you get exactly the same slope
            //it can be thousands of pixels, so you have to accept an arbitrary
            //and, unfortuantely, inexact slope
            if (m1 != m2 && Math.abs(m1 - m2) <= Double.MIN_VALUE) {
                m1 = m2;
            }
            if (b1 != b2 && Math.abs(b1 - b2) <= Double.MIN_VALUE) {
                b1 = b2;
            }

            //M. Deutch 10-24-11
            if (b1 == b2 && m1 + b1 == m2 + b2) {
                m1 = m2;
            }

            if (bolVerticalSlope1 == 0 && bolVerticalSlope2 == 0) //both lines vertical
            {
                switch (lOrient) {
                    case 0:
                        X.value[0] = p2.x - dWidth2;
                        Y.value[0] = p2.y;
                        break;
                    case 3:
                        X.value[0] = p2.x + dWidth2;
                        Y.value[0] = p2.y;
                        break;
                    default:	//can never occur
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y;
                        break;
                }
                return 1;
            }
            if (bolVerticalSlope1 == 0 && bolVerticalSlope2 != 0) //line1 vertical, line2 is not
            {	//there is a unique intersection
                switch (lOrient) {
                    case 0:	//Line1 above segment1
                    case 1:
                        X.value[0] = p2.x - dWidth2;
                        Y.value[0] = m2 * X.value[0] + b2;
                        break;
                    case 2:	//Line1 below segment1
                    case 3:
                        X.value[0] = p2.x + dWidth2;
                        Y.value[0] = m2 * X.value[0] + b2;
                        break;
                    default:	//can not occur
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y;
                        break;
                }
                return 1;
            }
            if (bolVerticalSlope2 == 0 && bolVerticalSlope1 != 0) //line2 vertical, line1 is not
            {	//there is a unique intersection
                switch (lOrient) {
                    case 0:	//Line1 above segment2
                    case 2:
                        X.value[0] = p2.x - dWidth2;
                        Y.value[0] = m1 * (X.value[0]) + b1;
                        break;
                    case 1:	//Line1 below segment2
                    case 3:
                        X.value[0] = p2.x + dWidth2;
                        Y.value[0] = m1 * (X.value[0]) + b1;
                        break;
                    default:	//can not occur
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y;
                        break;
                }
                return 1;
            }//end if

            //must deal with this case separately because normal lines use m1-m2 as a denominator
            //but we've handled all the vertical cases above so can assume it's not vertical
            //if the b's are different then one is an upper line, the other is a lower, no intersection
            //m and b will be used to build the perpendicular line thru p2 which we will use to
            //build the intersection, so must assume slopes are not 0, handle separately
            if (m1 == m2 && m1 != 0) {
                if (b1 == b2) //then the intercept is the point joining the 2 segments
                {
                    //build the perpendicular line
                    m = -1 / m1;
                    b = p2.y - m * p2.x;
                    X.value[0] = (b2 - b) / (m - m2);	//intersect the lines (cannot blow up, m = m2 not possible)
                    Y.value[0] = (m1 * (X.value[0]) + b1);
                    return 1;
                } else //can not occur
                {
                    X.value[0] = p2.x;
                    Y.value[0] = p2.y;
                    return 1;
                }
            }
            //slope is zero
            if (m1 == m2 && m1 == 0) {
                switch (lOrient) {
                    case 0:	//Line1 above the line
                    case 1:	//should never happen
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y - dWidth2;
                        break;
                    case 3:	//Line1 below the line
                    case 2:	//should never happen
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y + dWidth2;
                        break;
                    default:	//can not occur
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y;
                        break;
                }
                return 1;
            }

            if (m1 == m2 && b1 == b2 && bolVerticalSlope1 != 0 && bolVerticalSlope2 != 0) {
                switch (lOrient) {
                    case 0:	//Line1 is above the line
                        if (m1 < 0) {
                            dy = m1 * dWidth / Math.sqrt(1 + m1 * m1);	//dy is negative
                            dx = dy / m1;	//dx is negative
                            X.value[0] = p2.x + dx;
                            Y.value[0] = p2.y + dy;
                        }
                        if (m1 > 0) //slope is positive
                        {
                            dy = -m1 * dWidth / Math.sqrt(1 + m1 * m1);	//dy is negative
                            dx = -dy / m1;	//dx is positive
                            X.value[0] = p2.x + dx;
                            Y.value[0] = p2.y + dy;
                        }
                        break;
                    case 3:	//Line1 is below the line
                        if (m1 <= 0) {
                            dy = -m1 * dWidth / Math.sqrt(1 + m1 * m1);	//dy is positive
                            dx = dy / m1;	//dx is positive
                            X.value[0] = p2.x + dx;
                            Y.value[0] = p2.y + dy;
                        } else {
                            dy = m1 * dWidth / Math.sqrt(1 + m1 * m1);	//dy is positive
                            dx = -dy / m1;	//dx is negative
                            X.value[0] = p2.x + dx;
                            Y.value[0] = p2.y + dy;
                        }
                        break;
                    default:
                        X.value[0] = p2.x;
                        Y.value[0] = p2.y;
                        break;
                }
                return 1;
            }//end if

            //a normal line. no vertical or identical slopes
            //if m1=m2 function will not reach this point
            X.value[0] = (b2 - b1) / (m1 - m2);	//intersect the lines
            Y.value[0] = (m1 * (X.value[0]) + b1);
            return 1;
        }//end try
        catch (Exception exc) {
            X.value[0] = p2.x;
            Y.value[0] = p2.y;
            ErrorLogger.LogException(_className, "CalcTrueIntersectDouble",
                    new RendererException("Failed inside ExtendTrueIntersectDouble", exc));
        }
        return 1;
    }

    /**
     * Returns the distance in pixels from x1,y1 to x2,y2
     *
     * @param x1 first point x location in pixels
     * @param y1 first point y location in pixels
     * @param x2 second point x location in pixels
     * @param y2 second point y location in pixels
     *
     * @return the distance
     */
    protected static double CalcDistance2(long x1,
            long y1,
            long x2,
            long y2) {
        double dResult = 0;
        try {
            dResult = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

            //sanity check
            //return x or y distance if return value is 0 or infinity
            double xdist = Math.abs(x1 - x2);
            double ydist = Math.abs(y1 - y2);
            double max = xdist;
            if (ydist > xdist) {
                max = ydist;
            }
            if (dResult == 0 || Double.isInfinite(dResult)) {
                if (max > 0) {
                    dResult = max;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcDistance2",
                    new RendererException("Failed inside CalcDistance2", exc));
        }
        return dResult;
    }
    /**
     * gets the middle line for Rev B air corridors AC, LLTR, MRR, UAV
     * Middle line is handled separately now because the line may have been segmented
     * @param pLinePoints
     * @return 
     */
    protected static POINT2[] GetSAAFRMiddleLine(POINT2[] pLinePoints) {
        POINT2[] pts = null;
        try {
            int j = 0, count = 0;
            for (j = 0; j < pLinePoints.length-1; j++) {
                if (pLinePoints[j].style > 0) {
                    count++;
                }
            }
            pts = new POINT2[count*2];
            count=0;
            double dMRR=0;
            POINT2 firstSegPt=null,lastSegPt=null,pt0=null,pt1=null;
            for (j = 0; j < pLinePoints.length; j++) {
                if(pLinePoints[j].style>=0 || j==pLinePoints.length-1)
                {
                    if(lastSegPt != null)
                    {
                        firstSegPt=new POINT2(lastSegPt);
                        lastSegPt=new POINT2(pLinePoints[j]);
                        dMRR=firstSegPt.style;
                        pt0 = ExtendLine2Double(lastSegPt, firstSegPt, -dMRR, 0);
                        pt1 = ExtendLine2Double(firstSegPt, lastSegPt, -dMRR, 5);                        
                        pts[count++]=pt0;
                        pts[count++]=pt1;
                    }
                    else
                    {
                        lastSegPt=new POINT2(pLinePoints[j]);
                    }
                }
            }            
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSAAFRMiddleLine",
                    new RendererException("Failed inside GetSAAFRMiddleLine", exc));
        }
        return pts;
    }
    /**
     * Computes the points for a SAAFR segment
     *
     * @param pLinePoints OUT - the client points also used for the returned
     * points
     * @param lineType the line type
     * @param dMRR the symbol width
     */
    protected static void GetSAAFRSegment(POINT2[] pLinePoints,
            int lineType,
            double dMRR) {
        try {
            POINT2 pt0 = new POINT2();
            POINT2 pt1 = new POINT2();
            POINT2 pt2 = new POINT2();
            POINT2 pt3 = new POINT2();
            POINT2 pt4 = new POINT2();
            POINT2 pt5 = new POINT2();
            ref<double[]> m = new ref();
            int bolVertical = CalcTrueSlopeDouble(pLinePoints[0], pLinePoints[1], m);
            //shortened line
            //pt1=ExtendLine2Double(pLinePoints[0],pLinePoints[1],-dMRR/2,5);
            //pt0=ExtendLine2Double(pLinePoints[1],pLinePoints[0],-dMRR/2,0);
            pt1 = ExtendLine2Double(pLinePoints[0], pLinePoints[1], -dMRR, 5);
            pt0 = ExtendLine2Double(pLinePoints[1], pLinePoints[0], -dMRR, 0);
            if (bolVertical != 0 && m.value[0] < 1) {
                //upper line
                pt2 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 2, dMRR);
                pt2.style = 0;
                pt3 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 2, dMRR);
                pt3.style = 5;
                //lower line
                pt4 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 3, dMRR);
                pt4.style = 0;
                pt5 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 3, dMRR);
                pt5.style = 5;
            } //if( (bolVertical!=0 && m>1) || bolVertical==0)
            else {
                //left line
                pt2 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 0, dMRR);
                pt2.style = 0;
                pt3 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 0, dMRR);
                pt3.style = 5;
                //right line
                pt4 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 1, dMRR);
                pt4.style = 0;
                pt5 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 1, dMRR);
                pt5.style = 5;
            }
            //load the line points
            pLinePoints[0] = new POINT2(pt0);
            pLinePoints[1] = new POINT2(pt1);
            pLinePoints[2] = new POINT2(pt2);
            pLinePoints[3] = new POINT2(pt3);
            pLinePoints[4] = new POINT2(pt4);
            pLinePoints[5] = new POINT2(pt5);
            pLinePoints[5].style = 5;
            pLinePoints[0].style = 5;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSAAFRSegment",
                    new RendererException("Failed inside GetSAAFRSegment", exc));
        }
    }
    /**
     * Called by arraysupport for SAAFR and AC fill shapes
     * @param pLinePoints
     * @param dMRR
     */
    protected static void GetSAAFRFillSegment(POINT2[] pLinePoints,
            double dMRR) {
        try {
            POINT2 pt2 = new POINT2();
            POINT2 pt3 = new POINT2();
            POINT2 pt4 = new POINT2();
            POINT2 pt5 = new POINT2();
            ref<double[]> m = new ref();
            int bolVertical = CalcTrueSlopeDouble(pLinePoints[0], pLinePoints[1], m);
            if (bolVertical != 0 && m.value[0] < 1) {
                //upper line
                pt2 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 2, dMRR);
                pt3 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 2, dMRR);
                //lower line
                pt4 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 3, dMRR);
                pt5 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 3, dMRR);
            } //if( (bolVertical!=0 && m>1) || bolVertical==0)
            else {
                //left line
                pt2 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 0, dMRR);
                pt3 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 0, dMRR);
                //right line
                pt4 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[0], 1, dMRR);
                pt5 = ExtendDirectedLine(pLinePoints[0], pLinePoints[1], pLinePoints[1], 1, dMRR);
            }
            //load the line points
            pLinePoints[0] = new POINT2(pt2);
            pLinePoints[1] = new POINT2(pt3);
            pLinePoints[2] = new POINT2(pt5);
            pLinePoints[3] = new POINT2(pt4);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSAAFRFillSegment",
                    new RendererException("Failed inside GetSAAFRFillSegment", exc));
        }
        //return;
    }
    /**
     * Computes an arc.
     *
     * @param pResultLinePoints OUT - contains center and start point and holds
     * the result arc points
     * @param vblCounter the number of client points
     * @param dRadius the arc radius in pixels
     * @param linetype the linetype determines start andgle and end angle for
     * the arc
     *
     */
    protected static POINT2[] ArcArrayDouble(POINT2[] pResultLinePoints,
            int vblCounter,
            double dRadius,
            int linetype,
            IPointConversion converter) {
        try {
            //declarations
            double startangle = 0, //start of pArcLinePoints
                    endangle = 0, //end of the pArcLinePoints
                    increment = 0,
                    //m = 0,
                    length = 0, //length of a to e
                    M = 0;

            int j, numarcpts = 0, bolVertical = 0;
            ref<double[]> m = new ref();
            //C is the center of the pArcLinePoints derived from a and e
            POINT2 C = new POINT2(pResultLinePoints[0]),
                    a = new POINT2(pResultLinePoints[1]),
                    e = new POINT2(pResultLinePoints[0]);

            POINT2[] pArcLinePoints = null;
            //end declarations

            bolVertical = CalcTrueSlopeDouble(a, e, m);
            if (bolVertical != 0) {
                M = Math.atan(m.value[0]);
            } else {
                if (a.y < e.y) {
                    M = -Math.PI / 2;
                } else {
                    M = Math.PI / 2;
                }
            }
            if(converter != null)
            {
                Point2D pt02d=new Point2D.Double(pResultLinePoints[0].x,pResultLinePoints[0].y);
                Point2D pt12d=new Point2D.Double(pResultLinePoints[1].x,pResultLinePoints[1].y);
                //boolean reverseM=false;
                pt02d=converter.PixelsToGeo(pt02d);
                pt12d=converter.PixelsToGeo(pt12d);
                //M=mdlGeodesic.GetAzimuth(pt02d,pt12d);
                M= mdlGeodesic.GetAzimuth(new POINT2(pt02d.getX(),pt02d.getY()),new POINT2(pt12d.getX(),pt12d.getY()  )  );
                M*=(Math.PI/180);
                if(M<0)
                    M+=Math.PI;
            }
            length = CalcDistanceDouble(a, e);
            if(converter != null)
            {
                Point2D pt02d=new Point2D.Double(pResultLinePoints[0].x,pResultLinePoints[0].y);
                Point2D pt12d=new Point2D.Double(pResultLinePoints[1].x,pResultLinePoints[1].y);
                pt02d=converter.PixelsToGeo(pt02d);
                pt12d=converter.PixelsToGeo(pt12d);
                //length=mdlGeodesic.geodesic_distance(pt02d,pt12d,null,null);
                length=mdlGeodesic.geodesic_distance(new POINT2(pt02d.getX(),pt02d.getY()),new POINT2(pt12d.getX(),pt12d.getY()),null,null);
            }
            switch (linetype) {
                case TacticalLines.CLUSTER:
                    startangle = M - 90 * Math.PI / 180.0;
                    endangle = startangle + 2 * 90 * Math.PI / 180.0;
                    break;
                case TacticalLines.TRIP:
                    startangle = M - 45 * Math.PI / 180.0;
                    endangle = startangle + 2 * 45 * Math.PI / 180.0;
                    break;
                case TacticalLines.ISOLATE:
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.CORDONSEARCH:
                    startangle = M;
                    endangle = startangle + 330 * Math.PI / 180;
                    break;
                case TacticalLines.TURN:
                    startangle = M;
                    endangle = startangle + 90 * Math.PI / 180;
                    break;
                case TacticalLines.OCCUPY:
                case TacticalLines.RETAIN:
                case TacticalLines.SECURE:
                    startangle = M;
                    //if(CELineArrayGlobals.Change1==false)
                    endangle = startangle + 338 * Math.PI / 180;
                    //else
                    //	endangle=startangle+330*pi/180;
                    break;
                default:
                    startangle = 0;
                    endangle = 2 * Math.PI;
                    break;
            }

            if (a.x < e.x) {
                switch (linetype) {
                    case TacticalLines.ISOLATE:
                    case TacticalLines.CORDONKNOCK:
                    case TacticalLines.CORDONSEARCH:
                        startangle = M - Math.PI;
                        endangle = startangle + 330 * Math.PI / 180;
                        break;
                    case TacticalLines.OCCUPY:
                    case TacticalLines.RETAIN:
                    case TacticalLines.SECURE:
                        startangle = M - Math.PI;
                        //if(CELineArrayGlobals.Change1==false)
                        endangle = startangle + 338 * Math.PI / 180;
                        //else
                        //	endangle=startangle+330*pi/180;
                        break;
                    case TacticalLines.TURN:
                        startangle = M - Math.PI;
                        endangle = startangle + 90 * Math.PI / 180;
                        break;
                    case TacticalLines.CLUSTER:
                        startangle = M - Math.PI + 90 * Math.PI / 180.0;
                        endangle = startangle - 2 * 90 * Math.PI / 180.0;
                        break;
                    case TacticalLines.TRIP:
                        startangle = M - Math.PI + 45 * Math.PI / 180.0;
                        endangle = startangle - 2 * 45 * Math.PI / 180.0;
                        break;
                    default:
                        break;
                }
            }

            numarcpts = 26;
            pArcLinePoints = new POINT2[numarcpts];
            InitializePOINT2Array(pArcLinePoints);
            increment = (endangle - startangle) / (numarcpts - 1);
            if(dRadius != 0 && length != 0)
            {
                C.x = (int) ((double) e.x - (dRadius / length)
                        * ((double) a.x - (double) e.x));
                C.y = (int) ((double) e.y - (dRadius / length)
                        * ((double) a.y - (double) e.y));
            }
            else
            {
                C.x=e.x;
                C.y=e.y;
            }
            if (converter != null)
            {
                Point2D C2d=new Point2D.Double(pResultLinePoints[0].x,pResultLinePoints[0].y);
                C2d=converter.PixelsToGeo(C2d);    
                double az=0;
                Point2D ptGeo2d=null;
                POINT2 ptGeo=null;
                POINT2 ptPixels=null;
                for (j = 0; j < numarcpts; j++) {
                    az=startangle*180/Math.PI+j*increment*180/Math.PI;
                    //ptGeo=mdlGeodesic.geodesic_coordinate(C2d,length,az);
                    ptGeo=mdlGeodesic.geodesic_coordinate(new POINT2(C2d.getX(),C2d.getY()),length,az);
                    ptGeo2d=new Point2D.Double(ptGeo.x,ptGeo.y);
                    ptGeo2d=converter.GeoToPixels(ptGeo2d);
                    ptPixels=new POINT2(ptGeo2d.getX(),ptGeo2d.getY());
                    pArcLinePoints[j].x = ptPixels.x;
                    pArcLinePoints[j].y = ptPixels.y;                            
                }
            }
            else
            {
                for (j = 0; j < numarcpts; j++) {
                    //pArcLinePoints[j]=pResultLinePoints[0];	//initialize
                    pArcLinePoints[j].x = (int) (dRadius * Math.cos(startangle + j * increment));
                    pArcLinePoints[j].y = (int) (dRadius * Math.sin(startangle + j * increment));
                }

                for (j = 0; j < numarcpts; j++) {
                    pArcLinePoints[j].x += C.x;
                    pArcLinePoints[j].y += C.y;
                }
            }
            for (j = 0; j < numarcpts; j++) {
                pResultLinePoints[j] = new POINT2(pArcLinePoints[j]);
            }
            pArcLinePoints = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ArcArrayDouble",
                    new RendererException("Failed inside ArcArrayDouble", exc));
        }
        return pResultLinePoints;
    }
    /**
     * Gets geodesic circle using the converter
     * @param Center in pixels
     * @param pt1 a point on the radius in pixels
     * @param numpts number of points to return
     * @param CirclePoints the result points
     * @param converter 
     */
    protected static void CalcCircleDouble2(POINT2 Center,
            POINT2 pt1,
            int numpts,
            POINT2[] CirclePoints,
            IPointConversion converter) {
        try {
            int j = 0;
            double increment = (Math.PI * 2) / (numpts - 1);
            Point2D ptCenter2d=new Point2D.Double(Center.x,Center.y);
            ptCenter2d=converter.PixelsToGeo(ptCenter2d);
            Point2D pt12d=new Point2D.Double(pt1.x,pt1.y);
            pt12d=converter.PixelsToGeo(pt12d);
            Center=new POINT2(ptCenter2d.getX(),ptCenter2d.getY());
            pt1=new POINT2(pt12d.getX(),pt12d.getY());
            double dist=mdlGeodesic.geodesic_distance(Center, pt1, null, null);
            
            //double dSegmentAngle = 2 * Math.PI / numpts;
            double az=0;
            double startangle=0,endAngle=Math.PI*2;
            POINT2 ptGeo=null,ptPixels=null;
            Point2D ptGeo2d=null;           
            for (j = 0; j < numpts - 1; j++) {
                az=startangle*180/Math.PI+j*increment*180/Math.PI;
                //ptGeo=mdlGeodesic.geodesic_coordinate(C2d,length,az);
                ptGeo=mdlGeodesic.geodesic_coordinate(Center,dist,az);
                ptGeo2d=new Point2D.Double(ptGeo.x,ptGeo.y);
                ptGeo2d=converter.GeoToPixels(ptGeo2d);
                ptPixels=new POINT2(ptGeo2d.getX(),ptGeo2d.getY());
                CirclePoints[j].x = ptPixels.x;
                CirclePoints[j].y = ptPixels.y;                            
            }
            CirclePoints[numpts - 1] = new POINT2(CirclePoints[0]);

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcCircleDouble2",
                    new RendererException("Failed inside CalcCircleDouble2", exc));
        }
        return;
    }
    /**
     * Computes the points for a circle. Assumes CirclePoints has been allocated
     * with size numpts.
     *
     * @param Center the cicle center
     * @param radius the circle radius in pixels
     * @param numpts the number of circle points
     * @param CirclePoints - OUT - array of circle points
     * @param styl the style to set the last circle point
     */
    protected static void CalcCircleDouble(POINT2 Center,
            double radius,
            int numpts,
            POINT2[] CirclePoints,
            int styl) {
        try {
            int j = 0;
            double dSegmentAngle = 2 * Math.PI / (numpts - 1);
            double x = 0, y = 0;
            for (j = 0; j < numpts - 1; j++) {
                x = Center.x + (radius * Math.cos((double) j * dSegmentAngle));
                y = Center.y + (radius * Math.sin((double) j * dSegmentAngle));
                CirclePoints[j] = new POINT2(x, y);
                CirclePoints[j].style = styl;
            }
            CirclePoints[numpts - 1] = new POINT2(CirclePoints[0]);

            switch (styl) {
                case 0:
                    CirclePoints[numpts - 1].style = 0;
                    break;
                case 9:
                    CirclePoints[numpts - 1].style = 10;
                    break;
                case 11:
                    CirclePoints[numpts - 1].style = 12;
                    break;
                default:
                    CirclePoints[numpts - 1].style = 5;
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcCircleDouble",
                    new RendererException("Failed inside CalcCircleDouble", exc));
        }
    }

    protected static Shape2 CalcCircleShape(POINT2 Center,
            double radius,
            int numpts,
            POINT2[] CirclePoints,
            int styl) {
        Shape2 shape;
        if (styl == 9) {
            shape = new Shape2(Shape2.SHAPE_TYPE_FILL);
        } else {
            shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
        }

        shape.set_Style(styl);
        try {
            int j = 0;
            CalcCircleDouble(Center, radius, numpts, CirclePoints, styl);
            shape.moveTo(CirclePoints[0]);
            for (j = 1; j < numpts; j++) {
                shape.lineTo(CirclePoints[j]);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcCircleShape",
                    new RendererException("Failed inside CalcCircleShape", exc));
        }
        return shape;
    }

    private static void GetSquallCurve(POINT2 StartPt,
            POINT2 EndPt,
            POINT2[] pSquallPts,
            int sign,
            double amplitude,
            int quantity) {
        try {
            double dist = CalcDistanceDouble(StartPt, EndPt);
            POINT2 ptTemp = new POINT2();
            int j = 0;
                //end declarations

            //get points along the horizontal segment between StartPt and EndPt2;
            for (j = 0; j < quantity; j++) {
                ptTemp = ExtendLineDouble(EndPt, StartPt, -dist * (double) j / (double) quantity);
                pSquallPts[j].x = ptTemp.x;
                //calculate the sin value along the x axis
                pSquallPts[j].y = ptTemp.y + amplitude * sign * Math.sin((double) j * 180 / (double) quantity * Math.PI / 180);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSquallShape",
                    new RendererException("Failed inside GeSquallShape", exc));
        }
    }
    //caller needs to instantiate sign.value
    /**
     * Gets the squall curves for a line segment Assumes pSquallPts has been
     * allocated the proper number of points.
     *
     * @param StartPt segment start point
     * @param EndPt segment end point
     * @param pSquallPts OUT - the squall points
     * @param sign OUT - an object with a member to hold the starting curve sign
     * for the segment.
     * @param amplitude the sin curve amplitutde
     * @param quantity the number of points for each sin curve
     * @param length the desired length of the curve along the segment for each
     * sin curve
     *
     * @return segment squall points count
     */
    protected static int GetSquallSegment(POINT2 StartPt,
            POINT2 EndPt,
            POINT2[] pSquallPts,
            ref<int[]> sign,
            double amplitude,
            int quantity,
            double length) {
        int counter = 0;
        try {
            POINT2 StartCurvePt, EndCurvePt;	//use these for the curve points
            POINT2[] pSquallPts2 = new POINT2[quantity];
            double dist = CalcDistanceDouble(StartPt, EndPt);
            int numCurves = (int) (dist / (double) length);
            int j = 0, k = 0;
            POINT2 EndPt2 = new POINT2();
            double angle = Math.atan((StartPt.y - EndPt.y) / (StartPt.x - EndPt.x));
            int lAngle = (int) ((180 / Math.PI) * angle);
            InitializePOINT2Array(pSquallPts2);
            //define EndPt2 to be the point dist from StartPt along the x axis
            if (StartPt.x < EndPt.x) {
                EndPt2.x = StartPt.x + dist;
            } else {
                EndPt2.x = StartPt.x - dist;
            }

            EndPt2.y = StartPt.y;

            EndCurvePt = StartPt;
            for (j = 0; j < numCurves; j++) {
                StartCurvePt = ExtendLineDouble(EndPt2, StartPt, -(double) (j * length));
                EndCurvePt = ExtendLineDouble(EndPt2, StartPt, -(double) ((j + 1) * length));

                //get the curve points
                GetSquallCurve(StartCurvePt, EndCurvePt, pSquallPts2, sign.value[0], amplitude, quantity);

                //fill the segment points with the curve points
                for (k = 0; k < quantity; k++) {
                    //pSquallPts[counter].x=pSquallPts2[k].x;
                    //pSquallPts[counter].y=pSquallPts2[k].y;
                    pSquallPts[counter] = new POINT2(pSquallPts2[k]);
                    counter++;
                }
                //reverse the sign

                sign.value[0] = -sign.value[0];
            }
            if (numCurves == 0) {
                pSquallPts[counter] = new POINT2(StartPt);
                counter++;
                pSquallPts[counter] = new POINT2(EndPt);
                counter++;
            }
            //the points are along the x axis. Rotate them about the first point as the origin
            RotateGeometryDoubleOrigin(pSquallPts, counter, lAngle);
            pSquallPts2 = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetSquallSegment",
                    new RendererException("Failed inside GetSquallSegment", exc));
        }
        return counter;
    }

    //temporarily using 2000 pixels
    private static int PointInBounds(POINT2 pt) {
        try {
            //double maxPixels=CELineArrayGlobals.MaxPixels2;
            double maxPixels = 100000;//was 2000
            if (Math.abs(pt.x) <= maxPixels && Math.abs(pt.y) <= maxPixels) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "PointInBounds",
                    new RendererException("Failed inside PointInBounds", exc));
        }
        return 1;
    }

    /**
     * @param pt
     * @param ul
     * @param lr
     * @return
     */
    private static int PointInBounds2(POINT2 pt, POINT2 ul, POINT2 lr) {
        try {
            double maxX = lr.x, minX = ul.x, maxY = lr.y, minY = ul.y;
            if (pt.x <= maxX && pt.x >= minX && pt.y <= maxY && pt.y >= minY) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "PointInBounds2",
                    new RendererException("Failed inside PointInBounds2", exc));
        }
        return 1;
    }

    /**
     * Analyzes if line from pt0 to pt 1 intersects a side and returns the
     * intersection or null assumes pt0 to pt1 is not vertical. the caller will
     * replace pt0 with the intersection point if it is not null
     *
     * @param pt0
     * @param pt1
     * @param sidePt0 vertical or horizontal side first point
     * @param sidePt1
     * @return null if it does not intersect the side
     */
    private static POINT2 intersectSegment(POINT2 pt0, POINT2 pt1, POINT2 sidePt0, POINT2 sidePt1) {
        POINT2 pt = null;
        try {
            if (pt0.x == pt1.x) {
                return null;
            }
            double m = (pt1.y - pt0.y) / (pt1.x - pt0.x);
            double dx = 0, dy = 0, x = 0, y = 0;
            POINT2 upper = null, lower = null, left = null, right = null;
            Boolean bolVertical = false;
            //the side is either vertical or horizontal
            if (sidePt0.x == sidePt1.x) //vertical side
            {
                bolVertical = true;
                if (sidePt0.y < sidePt1.y) {
                    upper = sidePt0;
                    lower = sidePt1;
                } else {
                    upper = sidePt1;
                    lower = sidePt0;
                }
            } else //horizontal side
            {
                if (sidePt0.x < sidePt1.x) {
                    left = sidePt0;
                    right = sidePt1;
                } else {
                    left = sidePt1;
                    right = sidePt0;
                }
            }
            //travel in the direction from pt0 to pt1 to find the pt0 intersect
            if (bolVertical) {  //the side to intersect is vertical
                dx = upper.x - pt0.x;
                dy = m * dx;
                x = upper.x;
                y = pt0.y + dy;
                //the potential intersection point
                pt = new POINT2(x, y);

                if (pt0.x <= pt.x && pt.x <= pt1.x) //left to right
                {
                    if (upper.y <= pt.y && pt.y <= lower.y) {
                        return pt;
                    }
                } else if (pt0.x >= pt.x && pt.x >= pt1.x) //right to left
                {
                    if (upper.y <= pt.y && pt.y <= lower.y) {
                        return pt;
                    }
                }
            } else //horizontal side
            {
                dy = left.y - pt0.y;
                dx = dy / m;
                x = pt0.x + dx;
                y = left.y;
                //the potential intersection point
                pt = new POINT2(x, y);

                if (pt0.y <= pt.y && pt.y <= pt1.y) {
                    if (left.x <= pt.x && pt.x <= right.x) {
                        return pt;
                    }
                } else if (pt0.y >= pt.y && pt.y >= pt1.y) {
                    if (left.x <= pt.x && pt.x <= right.x) {
                        return pt;
                    }
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "intersectSegment",
                    new RendererException("Failed inside intersectSegment", exc));
        }
        return null;
    }

    /**
     * side 1 ----- | | side 0 | | side 2 | | ------ side 3 bounds one segment
     * for autoshapes that need it: bydif, fordif, fix, mnfldfix if null is
     * returned the client should conect the original line points (i.e. no
     * jaggies)
     *
     * @param pt0
     * @param pt1
     * @param ul
     * @param lr
     * @return bounded segment or null
     */
    public static POINT2[] BoundOneSegment(POINT2 pt0, POINT2 pt1, POINT2 ul, POINT2 lr) {
        POINT2[] line = new POINT2[2];
        try {
            if (pt0.y < ul.y && pt1.y < ul.y) {
                return null;
            }
            if (pt0.y > lr.y && pt1.y > lr.y) {
                return null;
            }
            if (pt0.x < ul.x && pt1.x < ul.x) {
                return null;
            }
            if (pt0.x > lr.x && pt1.x > lr.x) {
                return null;
            }

            Boolean bolVertical = false;
            InitializePOINT2Array(line);
            if (pt0.x == pt1.x) {
                bolVertical = true;
            }

            if (bolVertical) {
                line[0] = new POINT2(pt0);
                if (line[0].y < ul.y) {
                    line[0].y = ul.y;
                }
                if (line[0].y > lr.y) {
                    line[0].y = lr.y;
                }

                line[1] = new POINT2(pt1);
                if (line[1].y < ul.y) {
                    line[1].y = ul.y;
                }
                if (line[1].y > lr.y) {
                    line[1].y = lr.y;
                }

                return line;
            }

            double dx = 0, dy = 0, x = 0, y = 0;
            double m = (pt1.y - pt0.y) / (pt1.x - pt0.x);
            Boolean side0Intersect = false,
                    side1Intersect = false,
                    side2Intersect = false,
                    side3Intersect = false;
            //travel in the direction from pt0 to pt1 to find pt0 intersect
            POINT2 ur = new POINT2(lr.x, ul.y);
            POINT2 ll = new POINT2(ul.x, lr.y);

            POINT2 pt0Intersect = null;
            if (PointInBounds2(pt0, ul, lr) == 1) {
                pt0Intersect = pt0;
            }
            if (pt0Intersect == null) {
                pt0Intersect = intersectSegment(pt0, pt1, ll, ul);  //interesect side 0
                side0Intersect = true;
            }
            if (pt0Intersect == null) {
                pt0Intersect = intersectSegment(pt0, pt1, ul, ur);  //interesect side 1
                side1Intersect = true;
            }
            if (pt0Intersect == null) {
                pt0Intersect = intersectSegment(pt0, pt1, ur, lr);  //interesect side 2
                side2Intersect = true;
            }
            if (pt0Intersect == null) {
                pt0Intersect = intersectSegment(pt0, pt1, ll, lr);  //interesect side 3
                side3Intersect = true;
            }

            //travel in the direction from pt1 to pt0 to find pt1 intersect
            POINT2 pt1Intersect = null;
            if (PointInBounds2(pt1, ul, lr) == 1) {
                pt1Intersect = pt1;
            }
            if (pt1Intersect == null && side0Intersect == false) {
                pt1Intersect = intersectSegment(pt1, pt0, ll, ul);  //interesect side 0
            }
            if (pt1Intersect == null && side1Intersect == false) {
                pt1Intersect = intersectSegment(pt1, pt0, ul, ur);  //interesect side 1
            }
            if (pt1Intersect == null && side2Intersect == false) {
                pt1Intersect = intersectSegment(pt1, pt0, ur, lr);  //interesect side 2
            }
            if (pt1Intersect == null && side3Intersect == false) {
                pt1Intersect = intersectSegment(pt1, pt0, ll, lr);  //interesect side 3
            }

            if (pt0Intersect != null && pt1Intersect != null) {
                line[0] = pt0Intersect;
                line[1] = pt1Intersect;
                //return line;
            } else {
                line = null;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "BoundOneSegment",
                    new RendererException("Failed inside BoundOneSegment", exc));
        }
        return line;
    }

    private static int DisplayIntersectPixels(POINT2 pt0,
            POINT2 pt1,
            ref<double[]> pt2x,
            ref<double[]> pt2y,
            ref<double[]> pt3x,
            ref<double[]> pt3y) //POINT2 ul,
    //POINT2 lr)
    {
        int nResult = -1;
        try {
            //declarations
            double X = 0, Y = 0;
            ref<double[]> m = new ref();
            //double maxPixels=CELineArrayGlobals.MaxPixels2;
            double maxPixels = 2000;
            //double maxX=lr.x,minX=ul.x,maxY=lr.y,minY=ul.y;

            int bol0Inside = 0, bol1Inside = 0;
            int bolVertical = CalcTrueSlopeDouble(pt0, pt1, m);
            double b = pt0.y - m.value[0] * pt0.x;	//the y intercept for the segment line
            POINT2 pt2, pt3;
            //end declarations

            pt2x.value = new double[1];
            pt2y.value = new double[1];
            pt3x.value = new double[1];
            pt3y.value = new double[1];
            pt2 = new POINT2(pt0);
            pt3 = new POINT2(pt1);

            //diagnostic
            if (pt0.x <= maxPixels && pt0.x >= -maxPixels
                    && pt0.y <= maxPixels && pt0.y >= -maxPixels) {
                bol0Inside = 1;
            }
            if (pt1.x <= maxPixels && pt1.x >= -maxPixels
                    && pt1.y <= maxPixels && pt1.y >= -maxPixels) {
                bol1Inside = 1;
            }
            //if both points are inside the area then use the whole segment
            if (bol0Inside == 1 && bol1Inside == 1) {
                return 0;
            }
            //if at leat one of the points is inside the area then use some of the segment
            if (bol0Inside == 1 || bol1Inside == 1) {
                nResult = 1;
            }

            //segment is not vertical
            if (bolVertical != 0) {
                //analysis for side 0, get the intersection for either point if it exists
                //diagnostic
                X = -maxPixels;
                //X=minX;

                Y = m.value[0] * X + b;
                if (pt0.x < -maxPixels && -maxPixels < pt1.x) //pt0 is outside the area
                {
                    if (-maxPixels <= Y && Y <= maxPixels) //intersection is on side 0
                    //if(minY<=Y && Y<=maxY)	//intersection is on side 0
                    {
                        pt2.x = X;
                        pt2.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }
                if (pt1.x < -maxPixels && -maxPixels < pt0.x) //pt1 is outside the area
                //if(pt1.x<minX && minX<pt0.x)	//pt1 is outside the area
                {
                    if (-maxPixels <= Y && Y <= maxPixels) //intersection is on side 0
                    {
                        pt3.x = X;
                        pt3.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }

                //analysis for side 1, get the intersection for either point if it exists
                Y = -maxPixels;
                if (m.value[0] != 0) {
                    X = (Y - b) / m.value[0];
                    if (pt0.y < -maxPixels && -maxPixels < pt1.y) //pt0 is outside the area
                    {
                        if (-maxPixels <= X && X <= maxPixels) //intersection is on side 1
                        {
                            pt2.x = X;
                            pt2.y = Y;
                            nResult = 1;	//use at least some of the pixels
                        }
                    }
                    if (pt1.y <= -maxPixels && -maxPixels <= pt0.y) //pt1 is outside the area
                    {
                        if (-maxPixels < X && X < maxPixels) //intersection is on the boundary
                        {
                            pt3.x = X;
                            pt3.y = Y;
                            nResult = 1;	//use at least some of the pixels
                        }
                    }
                }
                //analysis for side 2, get the intersection for either point if it exists
                X = maxPixels;
                Y = m.value[0] * X + b;
                if (pt0.x < maxPixels && maxPixels < pt1.x) //pt1 is outside the area
                {
                    if (-maxPixels <= Y && Y <= maxPixels) //intersection is on the boundary
                    {
                        pt3.x = X;
                        pt3.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }
                if (pt1.x < maxPixels && maxPixels < pt0.x) //pt0 is outside the area
                {
                    if (-maxPixels <= Y && Y <= maxPixels) //intersection is on the boundary
                    {
                        pt2.x = X;
                        pt2.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }

                //analysis for side 3, get the intersection for either point if it exists
                Y = maxPixels;
                if (m.value[0] != 0) {
                    X = (Y - b) / m.value[0];
                    if (pt0.y < maxPixels && maxPixels < pt1.y) //pt1 is outside the area
                    {
                        if (-maxPixels <= X && X <= maxPixels) //intersection is on the boundary
                        {
                            pt3.x = X;
                            pt3.y = Y;
                            nResult = 1;	//use at least some of the pixels
                        }
                    }
                    if (pt1.y < maxPixels && maxPixels < pt0.y) //pt0 is outside the area
                    {
                        if (-maxPixels <= X && X <= maxPixels) //intersection is on the boundary
                        {
                            pt2.x = X;
                            pt2.y = Y;
                            nResult = 1;	//use at least some of the pixels
                        }
                    }
                }
            }

            //segment is vertical
            if (bolVertical == 0) {
                //analysis for side 1
                X = pt0.x;
                Y = -maxPixels;
                if (-maxPixels < pt0.x && pt0.x < maxPixels) {
                    if (pt0.y <= -maxPixels && -maxPixels <= pt1.y) //pt0 outside the area
                    {
                        pt2.x = X;
                        pt2.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                    if (pt1.y <= -maxPixels && -maxPixels <= pt0.y) //pt1 outside the area
                    {
                        pt3.x = X;
                        pt3.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }

                //analysis for side 3
                X = pt0.x;
                Y = maxPixels;
                if (-maxPixels < pt0.x && pt0.x < maxPixels) {
                    if (pt0.y <= maxPixels && maxPixels <= pt1.y) //pt1 outside the area
                    {
                        pt3.x = X;
                        pt3.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                    if (pt1.y <= maxPixels && maxPixels <= pt0.y) //pt0 outside the area
                    {
                        pt2.x = X;
                        pt2.y = Y;
                        nResult = 1;	//use at least some of the pixels
                    }
                }
            }

            pt2x.value[0] = pt2.x;
            pt2y.value[0] = pt2.y;
            pt3x.value[0] = pt3.x;
            pt3y.value[0] = pt3.y;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "DisplayIntersectPixels",
                    new RendererException("Failed inside DisplayIntersectPixels", exc));
        }
        return nResult;
    }
    /**
     * Computes Ditch spikes for the ATDITCH line types. This function uses
     * linestyles provided by the caller to skip segments.
     *
     * @param pLinePoints OUT - the client points also used for the return
     * points
     * @param nOldCounter the number of client points
     * @param bWayIs the parallel line to use (0) for inner or outer spikes
     *
     * @return the symbol point count
     */
    protected static int GetDitchSpikeDouble(TGLight tg, POINT2[] pLinePoints,
            int nOldCounter,
            int bWayIs) {
        int nSpikeCounter = 0;
        try {
            //declarations
            int linetype = tg.get_LineType();
            int nNumberOfSegments = 0,
                    lCircleCounter = 0,
                    bolVertical = 0,
                    nTemp = 0,
                    i,
                    j;
            double dPrinter = 1.0;
            double dIntLocation1x = 0,
                    dIntLocation2x = 0,
                    dIntLocation1y = 0,
                    dIntLocation2y = 0,
                    r = 0,
                    s = 0,
                    use = 0,
                    length = 0,
                    k = 0,
                    bint = 0;
            ref<double[]> pdAnswer = new ref();//new double[6];
            ref<double[]> m = new ref();

            POINT2 UpperLinePoint = new POINT2(pLinePoints[0]),
                    Lower1LinePoint = new POINT2(pLinePoints[0]),
                    Lower2LinePoint = new POINT2(pLinePoints[0]),
                    a = new POINT2(pLinePoints[0]),
                    b = new POINT2(pLinePoints[0]);
            POINT2[] pCirclePoints = new POINT2[pLinePoints.length];
            POINT2 averagePoint = new POINT2();
            POINT2 lastAveragePoint = new POINT2();
            POINT2[] pTempLinePoints = null;
            //end declarations

            pTempLinePoints = new POINT2[nOldCounter];
            for (j = 0; j < nOldCounter; j++) {
                pTempLinePoints[j] = new POINT2(pLinePoints[j]);
            }

            ArrayList<POINT2> basePoints = new ArrayList();

            InitializePOINT2Array(pCirclePoints);
            nSpikeCounter = nOldCounter;
            double spikeLength = arraysupport.getScaledSize(12, tg.get_LineThickness());
            double spikeHeight = spikeLength * 1.25;
            double minLength = 2 * spikeLength;
            for (i = 0; i < nOldCounter - 1; i++) {
                if (linetype == TacticalLines.ATDITCHM && i == 0) {
                    double radius = arraysupport.getScaledSize(4, tg.get_LineThickness());
                    minLength = spikeLength * 2.5 + radius * 2;
                }

                nTemp = CalcTrueLinesDouble((long) (spikeHeight * dPrinter), pLinePoints[i], pLinePoints[i + 1], pdAnswer);
                r = pdAnswer.value[3];
                s = pdAnswer.value[5];
                length = CalcDistanceDouble(pLinePoints[i], pLinePoints[i + 1]);
                bolVertical = CalcTrueSlopeDouble(pLinePoints[i], pLinePoints[i + 1], m);
                nNumberOfSegments = (int) ((length - 1) / (spikeLength * dPrinter));

                if (length > minLength * dPrinter) {    //minLength was 24
                    if (bWayIs != 0) {
                        if (pLinePoints[i].x <= pLinePoints[i + 1].x) {
                            use = r;
                        }
                        if (pLinePoints[i].x >= pLinePoints[i + 1].x) {
                            use = s;
                        }
                    } //end if
                    else {
                        if (pLinePoints[i].x <= pLinePoints[i + 1].x) {
                            use = s;
                        }
                        if (pLinePoints[i].x >= pLinePoints[i + 1].x) {
                            use = r;
                        }
                    }	//end else

                    for (j = 1; j <= nNumberOfSegments; j++) {
                        k = (double) j;
                        a = new POINT2(pLinePoints[i]);
                        b = new POINT2(pLinePoints[i + 1]);

                        if (j > 1) {
                            dIntLocation1x = dIntLocation2x;
                        } else {
                            dIntLocation1x
                                    = (double) pLinePoints[i].x + ((k * spikeLength - spikeLength) * dPrinter / length)
                                    * (double) (pLinePoints[i + 1].x - pLinePoints[i].x);
                        }

                        if (j > 1) //added M. Deutch 2-23-99
                        {
                            dIntLocation1y = dIntLocation2y;
                        } else {
                            dIntLocation1y
                                    = (double) pLinePoints[i].y + ((k * spikeLength - spikeLength / 2) * dPrinter / length)
                                    * (double) (pLinePoints[i + 1].y - pLinePoints[i].y);
                        }

                        dIntLocation2x = (double) pLinePoints[i].x
                                + ((k * spikeLength + spikeLength / 2) * dPrinter / length)
                                * (double) (pLinePoints[i + 1].x
                                - pLinePoints[i].x);

                        dIntLocation2y = (double) pLinePoints[i].y
                                + ((k * spikeLength + spikeLength / 2) * dPrinter / length)
                                * (double) (pLinePoints[i + 1].y
                                - pLinePoints[i].y);

                        if (m.value[0] != 0 && bolVertical != 0) {
                            bint = (dIntLocation1y + dIntLocation2y) / 2.0
                                    + (1 / m.value[0]) * (dIntLocation1x + dIntLocation2x) / 2.0;
                            //independent of direction
                            UpperLinePoint = CalcTrueIntersectDouble2(m.value[0], use, -1 / m.value[0], bint, 1, 1, pLinePoints[0].x, pLinePoints[0].y);
                        }

                        if (bolVertical == 0) //vertical segment
                        {
                            if (dIntLocation1y < dIntLocation2y) {
                                UpperLinePoint.y = (int) dIntLocation1y + (int) (length / nNumberOfSegments / 2);
                            } else {
                                UpperLinePoint.y = (int) dIntLocation1y - (int) (length / nNumberOfSegments / 2);
                            }
                            if (pLinePoints[i].y < pLinePoints[i + 1].y) {
                                UpperLinePoint.x = (int) dIntLocation1x + (int) (length / nNumberOfSegments);
                            } else {
                                UpperLinePoint.x = (int) dIntLocation1x - (int) (length / nNumberOfSegments);
                            }
                        }
                        if (m.value[0] == 0 && bolVertical != 0) {
                            if (dIntLocation1x < dIntLocation2x) {
                                UpperLinePoint.x = (int) dIntLocation1x + (int) (length / nNumberOfSegments / 2);
                            } else {
                                UpperLinePoint.x = (int) dIntLocation1x - (int) (length / nNumberOfSegments / 2);
                            }
                            if (pLinePoints[i + 1].x < pLinePoints[i].x) {
                                UpperLinePoint.y = (int) dIntLocation1y + (int) (length / nNumberOfSegments);
                            } else {
                                UpperLinePoint.y = (int) dIntLocation1y - (int) (length / nNumberOfSegments);
                            }
                        }
                        //end section

                        Lower1LinePoint.x = dIntLocation1x;
                        Lower1LinePoint.y = dIntLocation1y;
                        Lower2LinePoint.x = dIntLocation2x;
                        Lower2LinePoint.y = dIntLocation2y;

                        pLinePoints[nSpikeCounter] = new POINT2(Lower1LinePoint);
                        if (linetype == TacticalLines.ATDITCHC || linetype == TacticalLines.ATDITCHM) {
                            pLinePoints[nSpikeCounter].style = 9;
                        }
                        if (j % 2 == 1 && linetype == TacticalLines.ATDITCHM)//diagnostic 1-8-13
                        {
                            pLinePoints[nSpikeCounter].style = 5;
                        }

                        nSpikeCounter++;

                        pLinePoints[nSpikeCounter] = new POINT2(UpperLinePoint);
                        if (linetype == (long) TacticalLines.ATDITCHC || linetype == (long) TacticalLines.ATDITCHM) {
                            pLinePoints[nSpikeCounter].style = 9;
                        }
                        if (j % 2 == 1 && linetype == TacticalLines.ATDITCHM)//diagnostic 1-8-13
                        {
                            pLinePoints[nSpikeCounter].style = 5;
                        }

                        nSpikeCounter++;

                        pLinePoints[nSpikeCounter] = new POINT2(Lower2LinePoint);
                        if (linetype == (long) TacticalLines.ATDITCHC || linetype == (long) TacticalLines.ATDITCHM) {
                            pLinePoints[nSpikeCounter].style = 10;
                        }
                        if (j % 2 == 1 && linetype == TacticalLines.ATDITCHM)//diagnostic 1-8-13
                        {
                            pLinePoints[nSpikeCounter].style = 5;
                        }

                        nSpikeCounter++;

                        if (linetype == TacticalLines.ATDITCHM) {
                            if (j % 2 == 0) {
                                averagePoint = lineutility.MidPointDouble(Lower1LinePoint, Lower2LinePoint, 0);
                                averagePoint = lineutility.MidPointDouble(averagePoint, UpperLinePoint, 0);
                            } else if (j == 1) {
                                averagePoint = lineutility.ExtendLineDouble(Lower2LinePoint, Lower1LinePoint, 5);
                                averagePoint = lineutility.MidPointDouble(averagePoint, UpperLinePoint, 0);
                            }
                        }
                        //end section
                        if (j > 1 && j < nNumberOfSegments) {
                            basePoints.add(new POINT2(Lower1LinePoint));
                            //if(j==nNumberOfSegments-1)
                            //  basePoints.get(basePoints.size()-1).style=5;
                        } else if (j == 1) {
                            basePoints.add(new POINT2(pLinePoints[i]));
                        } else if (j == nNumberOfSegments) {
                            basePoints.add(new POINT2(pLinePoints[i + 1]));
                            basePoints.get(basePoints.size() - 1).style = 5;
                        }
                        if (linetype == TacticalLines.ATDITCHM && j > 1) {
                            if (j % 2 == 0) {
                                pCirclePoints[lCircleCounter] = lineutility.MidPointDouble(averagePoint, lastAveragePoint, 20);
                                lCircleCounter++;
                            }
                            //end section
                        }
                        if (j < nNumberOfSegments && linetype == TacticalLines.ATDITCHM) {
                            if (j == 1 || j % 2 == 0) {
                                //LastUpperLinePoint = new POINT2(UpperLinePoint);
                                lastAveragePoint = new POINT2(averagePoint);
                            }
                            //end section
                        }
                    }//end for j<numberOfsegments
                } //end if length big enough
                else {
                    //diagnostic
                    pLinePoints[nSpikeCounter].x = pLinePoints[i].x;
                    pLinePoints[nSpikeCounter].y = pLinePoints[i].y;
                    pLinePoints[nSpikeCounter].style = 0;
                    nSpikeCounter++;
                    pLinePoints[nSpikeCounter].x = pLinePoints[i + 1].x;
                    pLinePoints[nSpikeCounter].y = pLinePoints[i + 1].y;
                    pLinePoints[nSpikeCounter].style = 5;
                    nSpikeCounter++;
                }
            }

            for (j = 0; j < nOldCounter; j++) //reverse the first nOldCounter points for
            {
                pLinePoints[j] = new POINT2(pTempLinePoints[nOldCounter - j - 1]); //purpose of drawing
                pLinePoints[j].style = 5;
            }

            if (pLinePoints[nSpikeCounter - 1].style == 0) {
                pLinePoints[nSpikeCounter - 1].style = 5;
            }
            int t=basePoints.size();
            //for (j = nSpikeCounter; j < nSpikeCounter + basePoints.size(); j++) 
            for (j = nSpikeCounter; j < nSpikeCounter + t; j++) 
            {
                pLinePoints[j] = new POINT2(basePoints.get(j - nSpikeCounter));
                //if(linetype == TacticalLines.ATDITCHM && pLinePoints[j].style != 5)
                if (pLinePoints[j].style != 5) {
                    pLinePoints[j].style = 0;
                }
            }
            nSpikeCounter += basePoints.size();

            if (linetype == (int) TacticalLines.ATDITCHM) {
                pLinePoints[nSpikeCounter - 1].style = 5;//was 10
                for (j = nSpikeCounter; j < nSpikeCounter + lCircleCounter; j++) {
                    pLinePoints[j] = new POINT2(pCirclePoints[j - nSpikeCounter]);
                    pLinePoints[j].style = 20;
                }
                nSpikeCounter += lCircleCounter;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetDitchSpikeDouble",
                    new RendererException("Failed inside GetDitchSpikeDouble", exc));
        }
        return nSpikeCounter;
    }

    /**
     * Moves pixels if points are identical, used for the channel types
     *
     * @param pLinePoints OUT - client points also for returned points
     */
    protected static void MoveChannelPixels(POINT2[] pLinePoints) {
        try {
            if (pLinePoints == null || pLinePoints.length <= 0) {
                return;
            }

            double[] pixels = new double[pLinePoints.length * 2];
            boolean bolNoRepeats;
            int j, k = 0;
            double x1;
            double y1;
            double x2;
            double y2;
            int count = pLinePoints.length;
            //stuff pixels
            for (j = 0; j < count; j++) {
                pixels[k++] = pLinePoints[j].x;
                pixels[k++] = pLinePoints[j].y;
            }

            bolNoRepeats = false;
            do {
                bolNoRepeats = true;
                for (j = 0; j < count - 1; j++) {
                    x1 = pixels[2 * j];
                    y1 = pixels[2 * j + 1];
                    x2 = pixels[2 * j + 2];
                    y2 = pixels[2 * j + 3];
                    if (x1 == x2 && y1 == y2) //it's the same point
                    {
                        bolNoRepeats = false;
                        pixels[2 * j + 2] = (long) x2 + 1; //move the point
                        break;
                    }
                }
            } while (bolNoRepeats == false);
            //stuff pLinePoints
            k = 0;
            for (j = 0; j < count; j++) {
                pLinePoints[j].x = pixels[k++];
                pLinePoints[j].y = pixels[k++];
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "MoveChannelPixels",
                    new RendererException("Failed inside MoveChannelPixels", exc));
        }
    }

    /**
     * Single Concertina cannot have horizontal first segment
     *
     * @param linetype
     * @param pLinePoints
     */
    protected static void moveSingleCPixels(int linetype, POINT2[] pLinePoints) {
        try {
            switch (linetype) {
                case TacticalLines.SINGLEC:
                    break;
                default:
                    return;
            }
            if (pLinePoints.length > 1) {
                if (pLinePoints[1].y == pLinePoints[0].y) {
                    pLinePoints[1].y++;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "MoveSingleCPixels",
                    new RendererException("Failed inside MoveSingleCPixels", exc));
        }
    }

    /**
     * Rotates an the first vblCounter points in the array about its first point
     *
     * @param pLinePoints OUT - the points to rotate
     * @param vblCounter the number of points to rotate
     * @param lAngle the angle in degrees to rotate
     */
    protected static void RotateGeometryDouble(POINT2[] pLinePoints,
            int vblCounter,
            double lAngle) {
        try {
            int j = 0;
            double dRotate = 0,
                    dTheta = 0,
                    dGamma = 0,
                    x = 0,
                    y = 0;

            if (lAngle != 0) //if the angle is 0 no rotation occurs
            {
                POINT2 pdCenter;
                dRotate = lAngle * Math.PI / 180d;
                pdCenter = CalcCenterPointDouble(pLinePoints, vblCounter);

                for (j = 0; j < vblCounter; j++) {
                    //added if/else to get rid of divide by zero error 5/12/04 M. Deutch
                    if (pLinePoints[j].x == pdCenter.x) {
                        if ((pLinePoints[j].y > pdCenter.y)) {
                            dGamma = Math.PI + Math.PI / 2;
                        } else {
                            dGamma = Math.PI / 2;
                        }
                    } else {
                        dGamma = Math.PI + Math.atan((pLinePoints[j].y - pdCenter.y)
                                / (pLinePoints[j].x - pdCenter.x));
                    }

                    if ((double) pLinePoints[j].x >= pdCenter.x) {
                        dGamma = dGamma + Math.PI;
                    }

                    dTheta = dRotate + dGamma;
                    y = CalcDistanceDouble(pLinePoints[j], pdCenter) * Math.sin(dTheta);
                    x = CalcDistanceDouble(pLinePoints[j], pdCenter) * Math.cos(dTheta);
                    pLinePoints[j].y = pdCenter.y + y;
                    pLinePoints[j].x = pdCenter.x + x;
                }	//end for

                return;
            }	//end if
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "RotateGeometryDouble",
                    new RendererException("Failed inside RotateGeometryDouble", exc));
        }
    }  // end

    /**
     * Returns the point perpendicular to the line (pt0 to pt1) at the midpoint
     * the same distance from (and on the same side of) the the line as
     * ptRelative.
     *
     * @param pt0 the first point
     * @param pt1 the second point
     * @param ptRelative the point to use for computing the return point
     *
     * @return the point perpendicular to the line at the midpoint
     */
    protected static POINT2 PointRelativeToLine(POINT2 pt0,
            POINT2 pt1,
            POINT2 ptRelative) {
        POINT2 ptResult = new POINT2(pt0);
        try {
            int bolVertical = 0;
            ref<double[]> m = new ref();
            POINT2 midPt = MidPointDouble(pt0, pt1, 0);
            double b1 = 0, b2 = 0;
            //end declarations

            bolVertical = CalcTrueSlopeDouble(pt0, pt1, m);
            if (bolVertical == 0) //line is vertical
            {
                ptResult.x = ptRelative.x;
                ptResult.y = midPt.y;
            }
            if (bolVertical != 0 && m.value[0] == 0) {
                ptResult.x = midPt.x;
                ptResult.y = ptRelative.y;
            }
            if (bolVertical != 0 && m.value[0] != 0) {
                b1 = midPt.y + (1 / m.value[0]) * midPt.x;	//the line perp to midPt
                b2 = ptRelative.y - m.value[0] * ptRelative.x;	//the line  ptRelative with the slope of pt1-pt2
                ptResult = CalcTrueIntersectDouble2(-1 / m.value[0], b1, m.value[0], b2, 1, 1, 0, 0);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "PointRelativeToLine",
                    new RendererException("Failed inside PointRelativeToLine", exc));
        }
        return ptResult;
    }

    /**
     * shift the control point to match the shift that occurs in
     * Channels.GetAXADDouble for CATKBYFIRE. This is because the rotary feature
     * arrow tip must align with the anchor point
     *
     * @param linetype
     * @param pLinePoints the anchor points including the control point
     * @param dist the minimum required distance from the front of the rotary
     * arrow
     */
    public static void adjustCATKBYFIREControlPoint(int linetype,
            ArrayList<POINT2> pLinePoints,
            double dist) {
        try {
            if (linetype != TacticalLines.CATKBYFIRE) {
                return;
            }

            double dist2 = lineutility.CalcDistanceDouble(pLinePoints.get(0), pLinePoints.get(1));
            if (dist2 <= dist) {
                return;
            }

            POINT2 pt = null;
            int count = pLinePoints.size();
            POINT2 pt0 = new POINT2(pLinePoints.get(0));
            POINT2 pt1 = new POINT2(pLinePoints.get(1));
            POINT2 controlPt = new POINT2(pLinePoints.get(count - 1));
            POINT2 pt4 = PointRelativeToLine(pt0, pt1, pt1, controlPt);
            pt = lineutility.ExtendLineDouble(pt4, controlPt, dist);
            pLinePoints.set(count - 1, pt);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "adjustCATKBYFIREControlPoint",
                    new RendererException("Failed inside adjustCATKBYFIREControlPoint", exc));
        }
    }

    /**
     * Returns the point perpendicular to the line (pt0 to pt1) at atPoint the
     * same distance from (and on the same side of) the the line as ptRelative.
     *
     * @param pt0 the first point
     * @param pt1 the second point
     * @param atPoint the point on the line at which to compute the extended
     * point
     * @param ptRelative the point to use for computing the return point
     *
     * @return the point perpendicular to the line at ptRelative
     */
    public static POINT2 PointRelativeToLine(POINT2 pt0,
            POINT2 pt1,
            POINT2 atPoint,
            POINT2 ptRelative) {
        POINT2 ptResult = new POINT2(pt0);
        try {
            int bolVertical = 0;
            ref<double[]> m = new ref();
            double b1 = 0, b2 = 0;

            bolVertical = CalcTrueSlopeDouble(pt0, pt1, m);
            if (bolVertical == 0) //line is vertical
            {
                ptResult.x = ptRelative.x;
                ptResult.y = atPoint.y;
            }
            if (bolVertical != 0 && m.value[0] == 0) {
                ptResult.x = atPoint.x;
                ptResult.y = ptRelative.y;
            }
            if (bolVertical != 0 && m.value[0] != 0) {
                b1 = atPoint.y + (1 / m.value[0]) * atPoint.x;	//the line perp to midPt
                b2 = ptRelative.y - m.value[0] * ptRelative.x;	//the line  ptRelative with the slope of pt1-pt2
                ptResult = CalcTrueIntersectDouble2(-1 / m.value[0], b1, m.value[0], b2, 1, 1, 0, 0);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "PointRelativeToLine",
                    new RendererException("Failed inside PointRelativeToLine", exc));
        }
        return ptResult;
    }

    /**
     * Returns in pt2 and pt3 the line segment parallel to segment pt0-pt1 which
     * would contain ptRelative. pt2 corresponds to pt0 and pt3 corresponds to
     * pt1.
     *
     * @param pt0 first line point
     * @param pt1 second line point
     * @param ptRelative relative line point
     * @param pt2 OUT - first computed relative line point
     * @param pt3 OUT - second computed relative line point
     */
    public static void LineRelativeToLine(POINT2 pt0,
            POINT2 pt1,
            POINT2 ptRelative,
            POINT2 pt2,
            POINT2 pt3) {
        try {
            int bolVertical = 0;
            ref<double[]> m = new ref();
            double b1 = 0, b2 = 0;
            POINT2 pt2Temp = null;
            POINT2 pt3Temp = null;

            bolVertical = CalcTrueSlopeDouble(pt0, pt1, m);
            if (bolVertical == 0) //line is vertical
            {
                pt2.x = ptRelative.x;
                pt2.y = pt0.y;
                pt3.x = ptRelative.x;
                pt3.y = pt1.y;
            }
            if (bolVertical != 0 && m.value[0] == 0) //line is horizontal
            {
                pt2.x = pt0.x;
                pt2.y = ptRelative.y;
                pt3.x = pt1.x;
                pt3.y = ptRelative.y;
            }
            if (bolVertical != 0 && m.value[0] != 0) {
                b1 = pt0.y + (1 / m.value[0]) * pt0.x;	//the line perp to pt0
                b2 = ptRelative.y - m.value[0] * ptRelative.x;	//the line the ptRelative with the slope of pt0-pt1
                pt2Temp = CalcTrueIntersectDouble2(-1 / m.value[0], b1, m.value[0], b2, 1, 1, 0, 0);

                b1 = pt1.y + (1 / m.value[0]) * pt1.x;	//the line perp to pt1
                //b2=ptRelative.y-m*ptRelative.x;	//the line the ptRelative with the slope of pt0-pt1
                pt3Temp = CalcTrueIntersectDouble2(-1 / m.value[0], b1, m.value[0], b2, 1, 1, 0, 0);

                pt2.x = pt2Temp.x;
                pt2.y = pt2Temp.y;
                pt3.x = pt3Temp.x;
                pt3.y = pt3Temp.y;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "LineRelativeToLine",
                    new RendererException("Failed inside LineRelativeToLine", exc));
        }
    }

    private static void CalcMBR(POINT2[] pLinePoints,
            int numpts,
            ref<double[]> ulx,
            ref<double[]> uly,
            ref<double[]> lrx,
            ref<double[]> lry) {
        try {
            int j = 0;
            //initialize the MBR
            ulx.value = new double[1];
            uly.value = new double[1];
            lrx.value = new double[1];
            lry.value = new double[1];
            ulx.value[0] = Double.MAX_VALUE;//was 99999
            uly.value[0] = Double.MAX_VALUE;//was 99999
            lrx.value[0] = -Double.MAX_VALUE;//was -99999
            lry.value[0] = -Double.MAX_VALUE;//was -99999
            for (j = 0; j < numpts; j++) {
                if (pLinePoints[j].x > lrx.value[0]) {
                    lrx.value[0] = pLinePoints[j].x;
                }
                if (pLinePoints[j].y > lry.value[0]) {
                    lry.value[0] = pLinePoints[j].y;
                }
                if (pLinePoints[j].x < ulx.value[0]) {
                    ulx.value[0] = pLinePoints[j].x;
                }
                if (pLinePoints[j].y < uly.value[0]) {
                    uly.value[0] = pLinePoints[j].y;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcMBR",
                    new RendererException("Failed inside CalcMBR", exc));
        }
        return;
    }

    public static void CalcMBRPoints(POINT2[] pLinePoints,
            int numpts,
            POINT2 ul,
            POINT2 lr) {
        try {
            int j = 0;
            ul.x = Double.MAX_VALUE;
            ul.y = Double.MAX_VALUE;
            lr.x = -Double.MAX_VALUE;
            lr.y = -Double.MAX_VALUE;
            for (j = 0; j < numpts; j++) {
                if (pLinePoints[j].x > lr.x) {
                    lr.x = pLinePoints[j].x;
                }
                if (pLinePoints[j].y > lr.y) {
                    lr.y = pLinePoints[j].y;
                }
                if (pLinePoints[j].x < ul.x) {
                    ul.x = pLinePoints[j].x;
                }
                if (pLinePoints[j].y < ul.y) {
                    ul.y = pLinePoints[j].y;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "CalcMBRPoints",
                    new RendererException("Failed inside CalcMBRPoints", exc));
        }
    }

    /**
     * Computes the distance in pixels from upper left to lower right of the
     * minimum bounding rectangle for the first numpts of pLinePoints
     *
     * @param pLinePoints the inpupt point array
     * @param numpts the number of points to use
     *
     * @return the distance in pixels
     */
    protected static double MBRDistance(POINT2[] pLinePoints,
            int numpts) {
        double result = 0;
        try {
            ref<double[]> ulx = new ref(), uly = new ref(), lrx = new ref(), lry = new ref();
            CalcMBR(pLinePoints, numpts, ulx, uly, lrx, lry);
            result = Math.sqrt((lrx.value[0] - ulx.value[0]) * (lrx.value[0] - ulx.value[0]) + (lry.value[0] - uly.value[0]) * (lry.value[0] - uly.value[0]));
            //sanity check

            //return x or y distance if returnValue is 0 or infinity
            double xdist = Math.abs(lrx.value[0] - ulx.value[0]);
            double ydist = Math.abs(lry.value[0] - uly.value[0]);
            double max = xdist;
            if (ydist > xdist) {
                max = ydist;
            }

            if (result == 0 || Double.isInfinite(result)) {
                if (max > 0) {
                    result = max;
                }
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "MBRDistance",
                    new RendererException("Failed inside MBRDistance", exc));
        }
        return result;
    }

    /**
     * Swaps two points.
     *
     * @param pt1 OUT - first point
     * @param pt2 OUT - second point
     *
     */
    protected static void Reverse2Points(POINT2 pt1, POINT2 pt2) {
        try {
            POINT2 tempPt = new POINT2();
            //store pt1
            tempPt.x = pt1.x;
            tempPt.y = pt1.y;
            pt1.x = pt2.x;
            pt1.y = pt2.y;
            pt2.x = tempPt.x;
            pt2.y = tempPt.y;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "Reverse2Points",
                    new RendererException("Failed inside Reverse2Points", exc));
        }
    }
    /**
     * Creates a GeneralPath from a Path2D
     *
     * @param shape
     * @return
     */
    public static Shape createStrokedShape(Shape shape) {
        GeneralPath newshape = new GeneralPath(); // Start with an empty shape
        try {
            // Iterate through the specified shape, perturb its coordinates, and
            // use them to build up the new shape.
            double[] coords = new double[6];
            for (PathIterator i = shape.getPathIterator(null); !i.isDone(); i.next()) {
                int type = i.currentSegment(coords);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        //perturb(coords, 2);
                        newshape.moveTo(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_LINETO:
                        //perturb(coords, 2);
                        newshape.lineTo(coords[0], coords[1]);
                        break;
                    case PathIterator.SEG_QUADTO:
                        //perturb(coords, 4);
                        newshape.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        break;
                    case PathIterator.SEG_CUBICTO:
                        //perturb(coords, 6);
                        newshape.curveTo(coords[0], coords[1], coords[2], coords[3],
                                coords[4], coords[5]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        newshape.closePath();
                        break;
                }

            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "createStrokedShape",
                    new RendererException("Failed inside createStrokedShape", exc));
        }
        return newshape;
    }
    //These functions were added to create a minimum bounding polygon
    /**
     * @deprecated Returns the determinant of the point matrix This determinant
     * tells how far p3 is from vector p1p2 and on which side it is
     * @param p1
     * @param p2
     * @param p3
     * @return
     */
    static private int distance(Point p1, Point p2, Point p3) {
        try {
            int x1 = p1.x;
            int x2 = p2.x;
            int x3 = p3.x;
            int y1 = p1.y;
            int y2 = p2.y;
            int y3 = p3.y;
            return x1 * y2 + x3 * y1 + x2 * y3 - x3 * y2 - x2 * y1 - x1 * y3;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "distance",
                    new RendererException("Failed inside distance", exc));
        }
        return 0;
    }

    /**
     * @deprecated Returns the determinant of the point matrix This determinant
     * tells how far p3 is from vector p1p2 and on which side it is
     * @param p1
     * @param p2
     * @param p3
     * @return
     */
    static private double distance2(POINT2 p1, POINT2 p2, POINT2 p3) {
        try {
            double x1 = p1.x;
            double x2 = p2.x;
            double x3 = p3.x;
            double y1 = p1.y;
            double y2 = p2.y;
            double y3 = p3.y;
            return x1 * y2 + x3 * y1 + x2 * y3 - x3 * y2 - x2 * y1 - x1 * y3;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "distance2",
                    new RendererException("Failed inside distance2", exc));
        }
        return 0;
    }

    /**
     * @deprecated @param points
     * @param l
     * @param r
     * @param path
     */
    static private void cHull(ArrayList<Point> points, Point l, Point r, ArrayList<Point> path) {

        if (points.size() < 3) {
            return;
        }

        int maxDist = 0;
        int tmp;
        Point p = null;

        for (Point pt : points) {
            if (pt != l && pt != r) {
                tmp = distance(l, r, pt);

                if (tmp > maxDist) {
                    maxDist = tmp;
                    p = pt;
                }
            }
        }

        ArrayList<Point> left = new ArrayList<Point>();
        ArrayList<Point> right = new ArrayList<Point>();
        left.add(l);
        right.add(p);

        for (Point pt : points) {
            if (distance(l, p, pt) > 0) {
                left.add(pt);
            } else if (distance(p, r, pt) > 0) {
                right.add(pt);
            }
        }

        left.add(p);
        right.add(r);
        cHull(left, l, p, path);
        path.add(p);
        cHull(right, p, r, path);
    }

    /**
     * @deprecated @param points
     * @param l
     * @param r
     * @param path
     */
    static private void cHull2(ArrayList<POINT2> points, POINT2 l, POINT2 r, ArrayList<POINT2> path) {

        if (points.size() < 3) {
            return;
        }

        double maxDist = 0;
        double tmp;
        POINT2 p = null;

        for (POINT2 pt : points) {
            if (pt != l && pt != r) {
                tmp = distance2(l, r, pt);

                if (tmp > maxDist) {
                    maxDist = tmp;
                    p = pt;
                }
            }
        }

        ArrayList<POINT2> left = new ArrayList<POINT2>();
        ArrayList<POINT2> right = new ArrayList<POINT2>();
        left.add(l);
        right.add(p);

        for (POINT2 pt : points) {
            if (distance2(l, p, pt) > 0) {
                left.add(pt);
            } else if (distance2(p, r, pt) > 0) {
                right.add(pt);
            }
        }

        left.add(p);
        right.add(r);
        cHull2(left, l, p, path);
        path.add(p);
        cHull2(right, p, r, path);
    }
    //Returns the points of convex hull in the correct order
    /**
     * @deprecated @param array
     * @return
     */
    static public ArrayList<Point> cHull(ArrayList<Point> array) {
        int size = array.size();
        if (size < 2) {
            return null;
        }

        Point l = array.get(0);
        Point r = array.get(size - 1);
        ArrayList<Point> path = new ArrayList<Point>();
        path.add(l);
        cHull(array, l, r, path);
        path.add(r);
        cHull(array, r, l, path);
        return path;
    }

    /**
     * @deprecated @param array
     * @return
     */
    static public ArrayList<POINT2> cHull2(ArrayList<POINT2> array) {
        try {
            int size = array.size();
            if (size < 2) {
                return null;
            }

            POINT2 l = array.get(0);
            POINT2 r = array.get(size - 1);
            ArrayList<POINT2> path = new ArrayList<POINT2>();
            path.add(l);
            cHull2(array, l, r, path);
            path.add(r);
            cHull2(array, r, l, path);
            return path;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "cHull2",
                    new RendererException("Failed inside cHull2", exc));
        }
        return null;
    }

    public static void getExteriorPoints(POINT2[] pLinePoints,
            int vblCounter,
            int lineType,
            boolean interior
    ) {
        int j;
        int index;
        POINT2 pt0, pt1, pt2;
        ref<double[]> m01 = new ref(), m12 = new ref();
        int direction;
        POINT2 intersectPt;
        //ref<double[]> m1 = new ref(), m2 = new ref();
        ArrayList<POINT2> intersectPoints = new ArrayList();
        double b01, b12;	//the y intercepts for the lines corresponding to m1,m2 
        double dist = pLinePoints[0].style;
        for (j = 0; j < vblCounter; j++) {
            if (j == 0 || j == vblCounter - 1) {
                pt0 = new POINT2(pLinePoints[vblCounter - 2]);
                pt1 = new POINT2(pLinePoints[0]);
                pt2 = new POINT2(pLinePoints[1]);
            } else {
                pt0 = new POINT2(pLinePoints[j - 1]);
                pt1 = new POINT2(pLinePoints[j]);
                pt2 = new POINT2(pLinePoints[j + 1]);
            }
            if (pt1.style > 0) {
                dist = pt1.style;
            }
            //the exterior/interior points
            POINT2 pt00, pt01, pt10, pt11;

            index = j - 1;
            if (index < 0) {
                index = vblCounter - 1;
            }
            POINT2[] pts = new POINT2[pLinePoints.length];
            int n=pLinePoints.length;
            //for (int k = 0; k < pLinePoints.length; k++) 
            for (int k = 0; k < n; k++) 
            {
                pts[k] = pLinePoints[k];
            }

            direction = arraysupport.GetInsideOutsideDouble2(pt0, pt1, pts, vblCounter, index, lineType);
            //reverse the direction if these are interior points
            if (interior == true) {
                switch (direction) {
                    case 0:
                        direction = 1;
                        break;
                    case 1:
                        direction = 0;
                        break;
                    case 2:
                        direction = 3;
                        break;
                    case 3:
                        direction = 2;
                        break;
                    default:
                        break;
                }
            }
            //pt00-pt01 will be the interior line inside line pt0-pt1
            //pt00 is inside pt0, pt01 is inside pt1
            pt00 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, direction, dist);
            pt01 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, direction, dist);

            //pt10-pt11 will be the interior line inside line pt1-pt2
            //pt10 is inside pt1, pt11 is inside pt2
            index = j;
            if (j == vblCounter - 1) {
                index = 0;
            }
            direction = arraysupport.GetInsideOutsideDouble2(pt1, pt2, (POINT2[]) pts, vblCounter, index, lineType);
            //reverse the direction if these are interior points
            if (interior == true) {
                switch (direction) {
                    case 0:
                        direction = 1;
                        break;
                    case 1:
                        direction = 0;
                        break;
                    case 2:
                        direction = 3;
                        break;
                    case 3:
                        direction = 2;
                        break;
                    default:
                        break;
                }
            }
            pt10 = lineutility.ExtendDirectedLine(pt1, pt2, pt1, direction, dist);
            pt11 = lineutility.ExtendDirectedLine(pt1, pt2, pt2, direction, dist);
            //intersectPt=new POINT2(null);
            //get the intersection of pt01-p00 and pt10-pt11
            //so it it is the interior intersection of pt0-pt1 and pt1-pt2

            //first handle the case of vertical lines.
            if (pt0.x == pt1.x && pt1.x == pt2.x) {
                intersectPt = new POINT2(pt01);
                intersectPoints.add(intersectPt);
                continue;
            }
            //it's the same situation if the slopes are identical,
            //simply use pt01 or pt10 since they already uniquely define the intesection
            lineutility.CalcTrueSlopeDouble2(pt00, pt01, m01);
            lineutility.CalcTrueSlopeDouble2(pt10, pt11, m12);
            //if(m01.dbl==m12.dbl)					
            if (m01.value[0] == m12.value[0]) {
                intersectPt = new POINT2(pt01);
                intersectPoints.add(intersectPt);
                continue;
            }
            //now we are assuming a non-trivial intersection
            //calculate the y-intercepts using y=mx+b (use b=y-mx)
            b01 = pt01.y - m01.value[0] * pt01.x;
            b12 = pt11.y - m12.value[0] * pt11.x;
            intersectPt = lineutility.CalcTrueIntersectDouble2(m01.value[0], b01, m12.value[0], b12, 1, 1, 0, 0);
            intersectPoints.add(intersectPt);
        }//end for
        int n=intersectPoints.size();
        //for (j = 0; j < intersectPoints.size(); j++) 
        for (j = 0; j < n; j++) 
        {
            pLinePoints[j] = intersectPoints.get(j);
        }
    }
    public static ArrayList<POINT2> getDeepCopy(ArrayList<POINT2>pts)
    {
        ArrayList<POINT2>deepCopy=null;
        try
        {
            if(pts == null || pts.isEmpty())
                return pts;
            deepCopy=new ArrayList();
            int j=0;
            POINT2 pt=null;
            for(j=0;j<pts.size();j++)
            {                
                pt=new POINT2(pts.get(j).x,pts.get(j).y,pts.get(j).style);
                deepCopy.add(pt);
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "getDeepCopy",
                    new RendererException("Failed inside getDeepCopy", exc));
        }
        return deepCopy;
    }

}//end lineutility
