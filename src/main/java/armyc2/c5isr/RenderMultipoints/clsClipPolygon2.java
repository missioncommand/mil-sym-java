/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.RenderMultipoints;

import armyc2.c5isr.JavaTacticalRenderer.clsUtility;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.JavaLineArray.Shape2;
import java.util.HashMap;
import java.util.Map;


/**
 * A class to clip tactical lines and areas
 * 
 */
public final class clsClipPolygon2 {

    private static final String _className = "clsClipPolygon2";
    /**
     * Calculate the point the line intersects an edge of the clipbounds
     * @param pt0 start point of the line
     * @param pt1 end point of the line
     * @param currentEdge
     * @return
     */
    private static Point2D intersectPoint(Point2D pt0,
                                          Point2D pt1,
                                          Line2D currentEdge) {
        Point2D ptIntersect = null;
        try {
            Point2D edgePt1 = currentEdge.getP1();
            Point2D edgePt2 = currentEdge.getP2();
            double edge_x = 0, edge_y = 0, m = 0;
            double deltaX = 0, deltaY = 0;
            //vertical edge
            if (Math.abs(edgePt1.getX() - edgePt2.getX()) < Math.abs(edgePt1.getY() - edgePt2.getY()))
            {
                ptIntersect=new Point2D.Double();
                edge_x = edgePt1.getX();
                //if (pt1.getX() == pt0.getX())
                if (Math.abs(pt1.getX() - pt0.getX())<1)
                    pt1.setLocation(pt1.getX()+1, pt1.getY());

                m = (pt1.getY() - pt0.getY()) / (pt1.getX() - pt0.getX());
                deltaX = edge_x - pt0.getX();
                ptIntersect.setLocation(edge_x, pt0.getY() + m * deltaX);
            }
            //horizontal edge
            else
            {
                ptIntersect=new Point2D.Double();
                edge_y = edgePt1.getY();
                //if (pt1.getX() == pt0.getX())
                if (Math.abs(pt1.getX() - pt0.getX())<1)
                    pt1.setLocation(pt1.getX()+1, pt1.getY());

                m = (pt1.getY() - pt0.getY()) / (pt1.getX() - pt0.getX());
                deltaY = edge_y - pt0.getY();
                ptIntersect.setLocation(pt0.getX() + deltaY / m, edge_y);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "intersectPoint",
                    new RendererException("Failed inside intersectPoint", exc));
        }
        return ptIntersect;
    }
    /**
     * clip the top
     * on the line is considered inside
     * @param pts
     * @param clipBounds
     * @return
     */
    private static ArrayList<Point2D> clipTop(TGLight tg,ArrayList<Point2D> pts,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> ptsResult = new ArrayList();
        try {
            double ulx = 0, uly = 0, lrx = 0;// lry = 0;
            ulx = clipBounds.getMinX();
            uly = clipBounds.getMinY();
            Point2D ul = new Point2D.Double(ulx, uly);
            Point2D ur = new Point2D.Double(lrx, uly);

            int j = 0;
            Point2D current = null, previous = null;
            Point2D intersectPt = null;
            Line2D edge;
            int n=pts.size();
            //for (j = 0; j < pts.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                current = pts.get(j);
                if (j == 0) 
                {
                    previous = pts.get(pts.size() - 1);
                } 
                else 
                {
                    previous = pts.get(j - 1);
                }

                //both inside
                if (previous.getY() >= ul.getY() && current.getY() >= ul.getY()) {
                    ptsResult.add(current);
                }
                //previous inside, current outside
                if (previous.getY() >= ul.getY() && current.getY() < ul.getY()) {
                    edge = new Line2D.Double(ul, ur);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }
                    tg.set_WasClipped(true);
                }
                //both outside
                if (previous.getY() < ul.getY() && current.getY() < ul.getY()) {
                    continue;
                }

                //previous outside current inside
                if (previous.getY() < ul.getY() && current.getY() >= ul.getY()) {
                    edge = new Line2D.Double(ul, ur);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }
                    ptsResult.add(current);
                    tg.set_WasClipped(true);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "clipTop",
                    new RendererException("Failed inside clipTop", exc));
        }
        return ptsResult;
    }
    /**
     * on the boundary is considered inside
     * clip the bottom
     * @param pts
     * @param clipBounds
     * @return
     */
    private static ArrayList<Point2D> clipBottom(TGLight tg, ArrayList<Point2D> pts,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> ptsResult = new ArrayList();
        try {
            double ulx = 0, uly = 0, lrx = 0, lry = 0;
            ulx = clipBounds.getMinX();
            lrx = clipBounds.getMaxX();
            lry = clipBounds.getMaxY();
            Point2D ll = new Point2D.Double(ulx, lry);
            Point2D lr = new Point2D.Double(lrx, lry);

            int j = 0;
            Point2D current = null, previous = null;
            Point2D intersectPt = null;
            Line2D edge;
            int n=pts.size();
            //for (j = 0; j < pts.size(); j++)
            for (j = 0; j < n; j++)
            {
                current = pts.get(j);
                if (j == 0)
                {
                    previous = pts.get(pts.size() - 1);
                } 
                else
                {
                    previous = pts.get(j - 1);
                }

                //both inside
                if (previous.getY() <= lr.getY() && current.getY() <= lr.getY())
                {
                    ptsResult.add(current);
                }
                //previous inside, current outside
                if (previous.getY() <= lr.getY() && current.getY() > lr.getY())
                {
                    edge = new Line2D.Double(ll, lr);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null)
                    {
                        ptsResult.add(intersectPt);
                    }
                    tg.set_WasClipped(true);
                }
                //both outside
                if (previous.getY() > lr.getY() && current.getY() > lr.getY())
                {
                    continue;
                }

                //previous outside current inside
                if (previous.getY() > lr.getY() && current.getY() <= lr.getY()) {
                    edge = new Line2D.Double(ll, lr);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }

                    ptsResult.add(current);
                    tg.set_WasClipped(true);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "clipBottom",
                    new RendererException("Failed inside clipBottom", exc));
        }
        return ptsResult;
    }
    /**
     * on the bounds is considered inside
     * clip the right side
     * @param pts
     * @param clipBounds
     * @return
     */
    private static ArrayList<Point2D> clipRight(TGLight tg,ArrayList<Point2D> pts,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> ptsResult = new ArrayList();
        try {
            double uly = 0, lrx = 0, lry = 0;
            uly = clipBounds.getMinY();
            lrx = clipBounds.getMaxX();
            lry = clipBounds.getMaxY();
            Point2D ur = new Point2D.Double(lrx, uly);
            Point2D lr = new Point2D.Double(lrx, lry);
            int j = 0;            
            Point2D current = null, previous = null;
            Point2D intersectPt = null;
            Line2D edge;
            int n=pts.size();
            //for (j = 0; j < pts.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                current = pts.get(j);
                if (j == 0) {
                    previous = pts.get(pts.size() - 1);
                } else {
                    previous = pts.get(j - 1);
                }

                //both inside
                if (previous.getX() <= lr.getX() && current.getX() <= lr.getX()) {
                    ptsResult.add(current);
                }
                //previous inside, current outside
                if (previous.getX() <= lr.getX() && current.getX() > lr.getX()) {
                    edge = new Line2D.Double(ur, lr);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {                        
                        ptsResult.add(intersectPt);                        
                    }
                    tg.set_WasClipped(true);
                }
                //both outside
                if (previous.getX() > lr.getX() && current.getX() > lr.getX()) {
                    continue;
                }

                //previous outside current inside
                if (previous.getX() > lr.getX() && current.getX() <= lr.getX()) {
                    edge = new Line2D.Double(ur, lr);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }

                    //if(j!=0 || clsUtility.isClosedPolygon(tg.get_LineType())==true)
                    ptsResult.add(current);
                    tg.set_WasClipped(true);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "clipRight",
                    new RendererException("Failed inside clipRight", exc));
        }
        return ptsResult;
    }
    /**
     * on the line is considered inside
     * clip the left side
     * @param pts
     * @param clipBounds
     * @return
     */
    private static ArrayList<Point2D> clipLeft(TGLight tg, ArrayList<Point2D> pts,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> ptsResult = new ArrayList();
        try {
            double ulx = 0, uly = 0, lry = 0;
            ulx = clipBounds.getMinX();
            uly = clipBounds.getMinY();
            lry = clipBounds.getMaxY();
            Point2D ul = new Point2D.Double(ulx, uly);
            Point2D ll = new Point2D.Double(ulx, lry);

            int j = 0;
            Point2D current = null, previous = null;
            Point2D intersectPt = null;
            Line2D edge;     
            int n=pts.size();
            //for (j = 0; j < pts.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                current = pts.get(j);
                if (j == 0) 
                {
                    previous = pts.get(pts.size() - 1);
                } 
                else 
                {
                    previous = pts.get(j - 1);
                }

                //both inside
                if (previous.getX() >= ll.getX() && current.getX() >= ll.getX()) {
                    ptsResult.add(current);
                }
                //previous inside, current outside
                if (previous.getX() >= ll.getX() && current.getX() < ll.getX()) {
                    edge = new Line2D.Double(ul, ll);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }
                    tg.set_WasClipped(true);
                }
                //both outside
                if (previous.getX() < ll.getX() && current.getX() < ll.getX()) {
                    continue;
                }

                //previous outside current inside
                if (previous.getX() < ll.getX() && current.getX() >= ll.getX()) {
                    edge = new Line2D.Double(ul, ll);
                    intersectPt = intersectPoint(previous, current, edge);
                    if (intersectPt != null) {
                        ptsResult.add(intersectPt);
                    }

                    //if(j!=0 || clsUtility.isClosedPolygon(tg.get_LineType())==true)
                    ptsResult.add(current);
                    tg.set_WasClipped(true);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "clipLeft",
                    new RendererException("Failed inside clipLeft", exc));
        }
        return ptsResult;
    }

    /**
     * for non-areas add points to the ends as necessary to make the algorithm work
     * @param polygon
     * @param clipBounds
     */
    private static int AddBoundaryPointsForLines(ArrayList<Point2D> polygon,
            Rectangle2D clipBounds) {
        int result = 0;
        try {
            double ulx = 0, uly = 0, lrx = 0, lry = 0;
            ulx = clipBounds.getMinX();
            uly = clipBounds.getMinY();
            lrx = clipBounds.getMaxX();
            lry = clipBounds.getMaxY();
            //move these inside by 10 pixels so the algoithm will treat them as inside points
            Point2D ul = new Point2D.Double(ulx + 10, uly + 10);
            Point2D ur = new Point2D.Double(lrx - 10, uly + 10);
            Point2D ll = new Point2D.Double(ulx + 10, lry - 10);
            Point2D lr = new Point2D.Double(lrx - 10, lry - 10);

            Point2D pt0 = polygon.get(0);
            Point2D ptn = polygon.get(polygon.size() - 1);
            //double dist0 = 0, dist1 = 0;
            Boolean addToFront = false, addToEnd = false;
            //add a point to the begining of the array
            if (pt0.getY() < uly) //above the top clip
            {
                polygon.add(0, ul);
                addToFront = true;
            } else if (pt0.getX() < ulx) //outside the left clip
            {
                polygon.add(0, ul);
                addToFront = true;
            } else if (pt0.getX() > lrx) //outside the right clip
            {
                polygon.add(0, lr);
                addToFront = true;
            } else if (pt0.getY() > lry) //below the bottom clip
            {
                polygon.add(0, lr);
                addToFront = true;
            }

            //add a point to the end of the array
            if (ptn.getY() < uly) //above the top clip
            {
                polygon.add(ul);
                addToEnd = true;
            } else if (ptn.getX() < ulx) //outside the left clip
            {
                polygon.add(ul);
                addToEnd = true;
            } else if (ptn.getX() > lrx) //outside the right clip
            {
                polygon.add(lr);
                addToEnd = true;
            } else if (ptn.getY() > lry) //below the bottom clip
            {
                polygon.add(lr);
                addToEnd = true;
            }

            if (addToFront == false && addToEnd == false) {
                result = 0;
            }
            if (addToFront == true && addToEnd == false) {
                result = 1;
            }
            if (addToFront == false && addToEnd == true) {
                result = 2;
            }
            if (addToFront == true && addToEnd == true) {
                result = 3;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddBoundaryPointsForLines",
                    new RendererException("Failed inside AddBoundaryPointsForLines", exc));
        }
        return result;
    }
    /**
     * closes an area
     * @param tg
     */
    private static void closeAreaTG(TGLight tg)
    {
        try
        {
            if(tg.Pixels==null || tg.Pixels.isEmpty())
                return;

            POINT2 pt0=tg.Pixels.get(0);
            POINT2 ptn=tg.Pixels.get(tg.Pixels.size()-1);
            if(pt0.x != ptn.x || pt0.y != ptn.y)
                tg.Pixels.add(pt0);
            
        }
            catch (Exception exc) {
            ErrorLogger.LogException(_className, "closeAreaTG",
                    new RendererException("Failed inside closeAreaTG", exc));
        }
    }
    /**
     * DMA, DMAF fill must be handled separately because of the feint
     * @param tg
     * @param clipBounds
     * @return
     */
    protected static ArrayList<Shape2> fillDMA(TGLight tg,
            Rectangle2D clipBounds)
    {
        ArrayList<Shape2>shapes=new ArrayList();
        try
        {
            switch(tg.get_LineType())
            {
                case TacticalLines.OBSFAREA:
                case TacticalLines.OBSAREA:
                case TacticalLines.STRONG:
                case TacticalLines.ZONE:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                    break;
                default:
                    return shapes;
            }
            Shape2 shape=null;

            //create a generic area tg from the pixels and clip it
            int j=0;
            TGLight tg2=new TGLight();
            tg2.set_LineType(TacticalLines.GENERAL);
            tg2.Pixels=new ArrayList();
            //to get the original pixels size
            //int n=0;
            int n=tg.Pixels.size();
            
            for(j=0;j<n;j++)
                tg2.Pixels.add(tg.Pixels.get(j));

            closeAreaTG(tg2);

            if(clipBounds != null)
                ClipPolygon(tg2,clipBounds);

            if(tg2.Pixels==null || tg2.Pixels.isEmpty())
                return shapes;

            //shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
            shape=new Shape2(Shape2.SHAPE_TYPE_FILL);
            shape.setFillColor(tg.get_FillColor());

            shape.moveTo(tg2.Pixels.get(0));
            //original pixels do not include feint
            n=tg2.Pixels.size();
            //for(j=1;j<tg2.Pixels.size();j++)
            for(j=1;j<n;j++)
                shape.lineTo(tg2.Pixels.get(j));

            shapes.add(shape);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "fillDMA",
                    new RendererException("Failed inside fillDMA", exc));
        }
        return shapes;
    }
    /**
     * for pre-clipped lines which also require fill but need the processed points
     * to create the fill. This functioni is called after the clip, so the fill
     * does not get clipped.
     * @param tg
     * @param shapes
     */
    protected static void addAbatisFill(TGLight tg,
            ArrayList<Shape2>shapes)
    {
        try
        {
            if(tg.Pixels==null ||
                    tg.Pixels.size()<2 ||
                    tg.get_FillColor()==null ||
                    tg.get_FillColor().getAlpha()<2 ||
                    shapes==null)
                return;

            int j=0,n=tg.Pixels.size();
            Shape2 shape=null;
            TGLight tg2=null;
            switch(tg.get_LineType())
            {
                case TacticalLines.MSDZ:
                    double dist0=0,dist1=0,dist2=0;
                    shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setFillColor(tg.get_FillColor());
                    if(tg.Pixels != null & tg.Pixels.size()>=300)
                    {
                        dist0=Math.abs(tg.Pixels.get(0).x-tg.Pixels.get(50).x);
                        dist1=Math.abs(tg.Pixels.get(100).x-tg.Pixels.get(150).x);
                        dist2=Math.abs(tg.Pixels.get(200).x-tg.Pixels.get(250).x);
                        int start=-1,end=-1;
                        if(dist0>=dist1 && dist0>=dist2)
                        {
                            start=0;
                            end=99;
                        }
                        else if(dist1>=dist0 && dist1>=dist2)
                        {
                            start=100;
                            end=199;
                        }
                        else
                        {
                            start=200;
                            end=299;
                        }
                        shape.moveTo(tg.Pixels.get(start));
                        for(j=start;j<=end;j++)
                            shape.lineTo(tg.Pixels.get(j));

                        //shapes.add(0,shape);
                    }
                    break;
                case TacticalLines.ABATIS:
                    shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setFillColor(tg.get_FillColor());
                    tg2=new TGLight();
                    tg2.set_LineType(TacticalLines.GENERAL);
                    tg2.Pixels=new ArrayList();
                    if(tg.Pixels != null && tg.Pixels.size()>2)
                    {
                        tg2.Pixels.add(tg.Pixels.get(n-3));
                        tg2.Pixels.add(tg.Pixels.get(n-2));
                        tg2.Pixels.add(tg.Pixels.get(n-1));
                        tg2.Pixels.add(tg.Pixels.get(n-3));

                        shape.moveTo(tg2.Pixels.get(0));
                        for(j=1;j<tg2.Pixels.size();j++)
                            shape.lineTo(tg2.Pixels.get(j));

                        //shapes.add(shape);
                    }
                    break;
                default:
                    return;
            }//end switch
            if(shapes != null)
                shapes.add(0,shape);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "addAbatisFill",
                    new RendererException("Failed inside addAbatisFill", exc));
        }
    }
    /**
     * for lines with glyphs the fill must be handled (clipped) as a separate shape.
     * this function needs to be called before the clipping is done to the line
     * @param tg
     * @param clipBounds
     */
    protected static ArrayList<Shape2> LinesWithFill(TGLight tg,
            Rectangle2D clipBounds)
    {                        
        ArrayList<Shape2>shapes=null;
        try
        {
            if(tg.get_FillColor()==null || tg.get_FillColor().getAlpha()<=1 ||
                    tg.Pixels==null || tg.Pixels.isEmpty())
                return shapes;

            switch(tg.get_LineType())
            {
                case TacticalLines.ABATIS:
                case TacticalLines.SPT:
                case TacticalLines.MAIN:
                case TacticalLines.AAAAA:
                case TacticalLines.AIRAOA:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.CORDONSEARCH:
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.SECURE:
                case TacticalLines.OCCUPY:
                case TacticalLines.RETAIN:
                case TacticalLines.ISOLATE:
                case TacticalLines.CONVOY:
                case TacticalLines.HCONVOY:
                    return shapes;
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                    return null;
                case TacticalLines.OBSFAREA:
                case TacticalLines.OBSAREA:
                case TacticalLines.STRONG:
                case TacticalLines.ZONE:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                    return fillDMA(tg,clipBounds);
                default:
                    break;
            }
            if(clsUtility.LinesWithFill(tg.get_LineType())==false)
                return shapes;

            shapes=new ArrayList();
            //undo any fillcolor that might have been set for the existing shape
            //because we are divorcing fill from the line
            Shape2 shape=null;

            //create a generic area tg from the pixels and clip it
            TGLight tg2=new TGLight();
            tg2.set_LineType(TacticalLines.GENERAL);
            tg2.Pixels=new ArrayList();
            tg2.Pixels.addAll(tg.Pixels);
            closeAreaTG(tg2);
            //tg2.Pixels.add(tg.Pixels.get(0));
            if(clipBounds != null)
                ClipPolygon(tg2,clipBounds);


            if(tg2.Pixels==null || tg2.Pixels.isEmpty())
                return null;

            int j=0;
            //shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
            shape=new Shape2(Shape2.SHAPE_TYPE_FILL);
            shape.setFillColor(tg.get_FillColor());

            shape.moveTo(tg2.Pixels.get(0));
            for(j=1;j<tg2.Pixels.size();j++)
                shape.lineTo(tg2.Pixels.get(j));
            
            if(tg.get_FillColor() != null || tg.get_FillColor().getAlpha()>1)
            {
                shapes.add(shape);
            }
            else
                return  null;
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "LinesWithFill",
                    new RendererException("Failed inside LinesWithFill", exc));
        }
        return shapes;
    }
    /**
     * @deprecated
     * for polygon completely outside the clip area
     * pass back a small box to be able to continue normal processing
     * @param clipBounds
     * @return
     */
    private static ArrayList<Point2D> buildBox(Rectangle2D clipBounds) {
        ArrayList<Point2D> box = new ArrayList();
        try {
            {
                double ulx = 0, uly = 0, lrx = 0, lry = 0;
                ulx = clipBounds.getMinX() - 200;
                uly = clipBounds.getMinY() - 200;
                lrx = clipBounds.getMaxX() + 200;
                lry = clipBounds.getMaxY() + 200;
                Point2D lr = new Point2D.Double(ulx, uly);
                Point2D ll = new Point2D.Double(ulx - 10, uly);
                Point2D ul = new Point2D.Double(ulx - 10, uly - 10);
                Point2D ur = new Point2D.Double(ulx, uly - 10);
                box.add(lr);
                box.add(ll);
                box.add(ul);
                box.add(ur);
                box.add(lr);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "buildBox",
                    new RendererException("Failed inside buildBox", exc));
        }
        return box;
    }
    /**
     * Works for tactical lines and areas
     * @param tg
     * @param clipBounds
     * @return
     */
    public static ArrayList<Point2D> ClipPolygon(TGLight tg,
            Rectangle2D clipBounds) {
        ArrayList<Point2D> poly = new ArrayList();
        try {
            ArrayList polygon = clsUtilityCPOF.POINT2toPoint2D(tg.Pixels);            
            Boolean isClosed = clsUtility.isClosedPolygon(tg.get_LineType());
            //create a hashtable to hold the original points
            Map<String,Object>hashMap=new HashMap<String,Object>();
            int j=0;
            for(j=0;j<polygon.size();j++)
            {
                hashMap.put(Integer.toString(j), polygon.get(j));
            }
            
            Rectangle2D clipBounds2 = new Rectangle2D.Double(clipBounds.getX() - 50, clipBounds.getY() - 50, clipBounds.getWidth() + 100, clipBounds.getHeight() + 100);

            int addedLinePoints = 0;
            if (isClosed) {
                polygon.remove(polygon.size() - 1);
                isClosed = true;
            } else {
                //for tactical lines it always seems to work if the 0th and last points are inside the area
                //add points on the edge as needed to make that happen
                addedLinePoints = AddBoundaryPointsForLines(polygon, clipBounds2);
            }
            //expand the clip bounds by 10 pixels

            poly = clipRight(tg, polygon, clipBounds2);
            poly = clipTop(tg, poly, clipBounds2);
            poly = clipLeft(tg, poly, clipBounds2);
            poly = clipBottom(tg, poly, clipBounds2);

            if (isClosed)
            {
                if (poly.size() > 0)
                {
                    poly.add(poly.get(0));
                }
            } 
            else
            {
                switch (addedLinePoints)
                {
                    case 0: //no points were added, do nothing
                        break;
                    case 1: //point was added to the front to make algorithm work, remove segment
                        if (poly.size() > 0) {
                            poly.remove(0);
                        }
                        if (poly.size() > 0) {
                            poly.remove(0);
                        }
                        break;
                    case 2: //point was added to the end to make algorithm work, remove segment
                        if (poly.size() > 0) {
                            poly.remove(poly.size() - 1);
                        }
                        if (poly.size() > 0) {
                            poly.remove(poly.size() - 1);
                        }
                        break;
                    case 3: //point was added to the front and end to make algorithm work, remove segments
                        if (poly.size() > 0) {
                            poly.remove(0);
                        }
                        if (poly.size() > 0) {
                            poly.remove(0);
                        }
                        if (poly.size() > 0) {
                            poly.remove(poly.size() - 1);
                        }
                        if (poly.size() > 0) {
                            poly.remove(poly.size() - 1);
                        }
                        break;
                }
            }
            
            if (isClosed == true)
            {
                if (poly.size() > 2)
                {
                    //tg.Pixels = clsUtilityCPOF.Point2DtoPOINT2(poly);
                    tg.Pixels = clsUtilityCPOF.Point2DtoPOINT2Mapped(poly,hashMap);
                } 
                else
                {
                    //poly = buildBox(clipBounds);
                    //tg.Pixels = clsUtilityCPOF.Point2DtoPOINT2(poly);
                    tg.Pixels=new ArrayList();
                }

            } 
            else
            {
                if (poly.size() > 1)
                {
                    tg.Pixels = clsUtilityCPOF.Point2DtoPOINT2Mapped(poly,hashMap);
                } 
                else
                {
                    tg.Pixels=new ArrayList();
                }
            }            

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ClipPolygon",
                    new RendererException("Failed inside ClipPolygon", exc));
        }
        return poly;
    }
}
