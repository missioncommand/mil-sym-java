/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.RenderMultipoints;

import armyc2.c5isr.JavaTacticalRenderer.clsUtility;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.JavaLineArray.Shape2;
import armyc2.c5isr.JavaLineArray.lineutility;


import java.util.HashMap;
import java.util.Map;

/**
 * Class to clip polygons
 * 
 */
public final class clsClipQuad {
    private static final String _className = "clsClipQuad";
    /**
     * Use the new version which takes an array for polygon clip bounds instead of rectangle
     * @param polygon
     * @param clipBounds
     * @return 
     */
    private static int AddBoundaryPointsForLines(ArrayList<Point2D> polygon,
            ArrayList<Point2D> clipBounds) 
    {
        int result=0;
        try
        {
            Point2D pt02d=polygon.get(0);
            Point2D ptLast2d=polygon.get((polygon.size()-1));
            POINT2 pt0=new POINT2(pt02d.getX(),pt02d.getY());
            POINT2 ptLast=new POINT2(ptLast2d.getX(),ptLast2d.getY());
            Point2D nearestPt=new Point2D.Double();
            Polygon clipArray=new Polygon();
            int j=0;
            double minDist=Double.MAX_VALUE;
            double dist=0;
            POINT2 sidePt=new POINT2();
            Boolean addToFront = false, addToEnd = false;     
            //int n=clipBounds.size();
            //for(j=0;j<clipBounds.size();j++)
            for(j=0;j<clipBounds.size();j++)    //was n
            {
                clipArray.addPoint((int)clipBounds.get(j).getX(), (int)clipBounds.get(j).getY());
            }

            double totalX=0,totalY=0;
            int counter=0;
            //for(j=0;j<clipBounds.size()-1;j++)
            for(j=0;j<clipBounds.size()-1;j++)  //was n-1
            {
                totalX+=clipBounds.get(j).getX();
                totalY+=clipBounds.get(j).getY();
                counter++;
            }
            //if clipBounds is not closed add the jth point
            if( clipBounds.get(0).getX()!=clipBounds.get(j).getX() || 
                    clipBounds.get(0).getY()!=clipBounds.get(j).getY() )
            {
                totalX+=clipBounds.get(j).getX();
                totalY+=clipBounds.get(j).getY();                    
                counter++;
            }
            double avgX=totalX/counter;
            double avgY=totalY/counter;
            POINT2 ptCenter=new POINT2(avgX,avgY);
            POINT2 ptNear=null;
            //first point outside the clip bounds
            if(clipArray.contains(pt02d)==false)
            {
                //add nearest segment midpoint to the front
                //for(j=0;j<clipBounds.size();j++)
                for(j=0;j<clipBounds.size();j++)    //was n
                {
                    sidePt.x=clipBounds.get(j).getX();
                    sidePt.y=clipBounds.get(j).getY();
                    dist=lineutility.CalcDistanceDouble(pt0, sidePt);
                    if(dist<minDist)
                    {
                        minDist=dist;
                        //minDistIndex=j;
                        nearestPt.setLocation(sidePt.x,sidePt.y);
                    }
                }
                //move nearestPt in a bit to not get clipped
                ptNear=new POINT2(nearestPt.getX(),nearestPt.getY());
                ptNear=lineutility.ExtendAlongLineDouble(ptNear, ptCenter, 2);
                nearestPt.setLocation(ptNear.x, ptNear.y);
                polygon.add(0, nearestPt);
                addToFront=true;
            }
            //re-initialize variables
            nearestPt=new Point2D.Double();
            minDist=Double.MAX_VALUE;
            //last point outside the clip bounds
            if(clipArray.contains(ptLast2d)==false)
            {
                //add nearest segment midpoint to the front
                //for(j=0;j<clipBounds.size();j++)
                for(j=0;j<clipBounds.size();j++)    //was n
                {
                    sidePt.x=clipBounds.get(j).getX();
                    sidePt.y=clipBounds.get(j).getY();
                    dist=lineutility.CalcDistanceDouble(ptLast, sidePt);
                    if(dist<minDist)
                    {
                        minDist=dist;
                        //minDistIndex=j;
                        nearestPt.setLocation(sidePt.x,sidePt.y);
                    }
                }            
                //move nearestPt in a bit to not get clipped
                ptNear=new POINT2(nearestPt.getX(),nearestPt.getY());
                ptNear=lineutility.ExtendAlongLineDouble(ptNear, ptCenter, 2);
                nearestPt.setLocation(ptNear.x, ptNear.y);
                polygon.add(nearestPt);
                addToEnd=true;
            }
            if (addToFront == false && addToEnd == false) {
                result = 0;
            }
            else if (addToFront == true && addToEnd == false) {
                result = 1;
            }
            else if (addToFront == false && addToEnd == true) {
                result = 2;
            }
            else if (addToFront == true && addToEnd == true) {
                result = 3;
            }
        }
        catch (Exception exc) 
        {
            ErrorLogger.LogException(_className, "AddBoundaryPointsForLines",
                    new RendererException("Failed inside AddBoundaryPointsForLines", exc));
        }        
        return result;
    }
    private static Point2D CalcTrueIntersectDouble(double m1,
                                            double b1,
                                            double m2,
                                            double b2,
                                            int bolVertical1,
                                            int bolVertical2,
                                            double X1,	//x intercept if line1 is vertical
                                            double X2)
    {
        Point2D ptIntersect=new Point2D.Double(X1,X2);
        try
        {
            double x=0,y=0;

            if(bolVertical1==0 && bolVertical2==0)	//both lines vertical
                return ptIntersect;
            //the following 3 if blocks are the only ways to get an intersection
            if(bolVertical1==0 && bolVertical2==1)	//line1 vertical, line2 not
            {
                ptIntersect.setLocation(X1, m2*X1+b2);
                return ptIntersect;
            }
            if(bolVertical1==1 && bolVertical2==0)	//line2 vertical, line1 not
            {
                ptIntersect.setLocation(X2, m1*X2+b1);
                return ptIntersect;
            }
            //if either of the lines is vertical function has already returned
            //so both m1 and m2 should be valid
            //should always be using this ocase because the lines are neither vertical
            //or horizontal and are perpendicular
            if(m1 != m2)
            {
                x=(b2-b1)/(m1-m2);	//cannot blow up
                y=(m1*x+b1);
                ptIntersect.setLocation(x, y);
                return ptIntersect;
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className ,"CalcTrueIntersectDouble",
                    new RendererException("Failed inside CalcTrueIntersectDouble", exc));
        }
        return ptIntersect;
    }
    /**
     * Gets theoretical intersection of an edge with the line connecting previous and current points.
     * @param previous
     * @param current
     * @param currentEdge the current edge of the clip area, assumed to not be vertical
     * @return 
     */
    private static Point2D intersectPoint2(Point2D previous, 
            Point2D current,
            Line2D currentEdge)
    {
        
        Point2D ptIntersect=null;
        try
        {                        
            Point2D ll=currentEdge.getP1();
            Point2D ul=currentEdge.getP2();
            
            //no vertical client segments
            //if(current.getX()==previous.getX())            
            if(Math.abs(current.getX()-previous.getX())<1)            
                current.setLocation(current.getX()+1, current.getY());                
            
            double m1=( ul.getY()-ll.getY() )/( ul.getX()-ll.getX() );
            double m2=( current.getY()-previous.getY() )/( current.getX()-previous.getX() );
            double b1=ul.getY()-m1*ul.getX();
            double b2=current.getY()-m2*current.getX(); 
            ptIntersect=CalcTrueIntersectDouble(m1,b1,m2,b2,1,1,0,0);                    
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "intersectPoint2",
                    new RendererException("Failed inside intersectPoint2", exc));
        }        
        return ptIntersect;
    }

    /**
     * clips array of pts against a side of the clip bounds polygon
     * assumes clipBounds has no vertical or horizontal segments 
     * @param pts array of points to clip against the clip bounds
     * @param index starting index of clipBounds for the side to clip against
     * @param clipBounds a quadrilateral or a polygon array that is the clipping area
     * @return the clipped array of points
     */
    private static ArrayList<Point2D> clipSide(TGLight tg, ArrayList<Point2D> pts,
            int index,
            ArrayList<Point2D> clipBounds) 
    {
        ArrayList<Point2D> ptsResult=null;
        try
        {
            Point2D pt1=new Point2D.Double(clipBounds.get(index).getX(),clipBounds.get(index).getY());//first point of clip side
            Point2D pt2=new Point2D.Double(clipBounds.get(index+1).getX(),clipBounds.get(index+1).getY());//last point of clip side                        
            Point2D clipBoundsPoint=null;//some point in the clipbounds not on the side
            Point2D ptClipBoundsIntersect=null;//some point in the clipbounds not on the side
            double m1=0,m2=0,b1=0,b2=0,b3=0,b4=0;            
            Point2D ptPreviousIntersect=null,ptCurrentIntersect=null;            
            int j = 0,clipBoundsQuadrant=-1,previousQuadrant=-1,currentQuadrant=-1;  //quadrants relative to side
            Point2D current = null, previous = null;
            Point2D intersectPt = null;
            Line2D edge;  
            ptsResult=new ArrayList();            
            //set some point in the array which is not in the side
            //this point will be used to define which side of the clipping side the rest of the clipbounds points are on
            //then it can be used to figure out whether a given point is to be clipped
            //for this scheme to work it needs to be a convex clipping area
            if(index==0)
            {
                clipBoundsPoint=new Point2D.Double(clipBounds.get(index+2).getX(),clipBounds.get(index+2).getY());
            }
            else if(index>1)
            {
                clipBoundsPoint=new Point2D.Double(clipBounds.get(index-2).getX(),clipBounds.get(index-2).getY());
            }
            else if(index==1)
            {
                clipBoundsPoint=new Point2D.Double(clipBounds.get(0).getX(),clipBounds.get(0).getY());
            }
                
            //no vertical segments
            //if(pt2.getX()==pt1.getX())
            if(Math.abs(pt2.getX()-pt1.getX())<1)
                pt2.setLocation(pt2.getX()+1, pt2.getY());
            //if(pt2.getY()==pt1.getY())
            if(Math.abs(pt2.getY()-pt1.getY())<1)
                pt2.setLocation(pt2.getX(), pt2.getY()+1);
            
            for (j = 0; j < pts.size(); j++) 
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

                m1=(pt2.getY()-pt1.getY())/(pt2.getX()-pt1.getX());
                m2=-1d/m1;  //the slope of the line perpendicular to m1,b1
                b1=pt2.getY()-m1*pt2.getX();
                b2=previous.getY()-m2*previous.getX(); 
                b3=current.getY()-m2*current.getX(); 
                b4=clipBoundsPoint.getY()-m2*clipBoundsPoint.getX(); 
                ptPreviousIntersect=CalcTrueIntersectDouble(m1,b1,m2,b2,1,1,0,0);                    
                ptCurrentIntersect=CalcTrueIntersectDouble(m1,b1,m2,b3,1,1,0,0);
                ptClipBoundsIntersect=CalcTrueIntersectDouble(m1,b1,m2,b4,1,1,0,0);
                clipBoundsQuadrant=lineutility.GetQuadrantDouble(clipBoundsPoint.getX(), clipBoundsPoint.getY(), ptClipBoundsIntersect.getX(), ptClipBoundsIntersect.getY());
                previousQuadrant=lineutility.GetQuadrantDouble(previous.getX(), previous.getY(), ptPreviousIntersect.getX(), ptPreviousIntersect.getY());
                currentQuadrant=lineutility.GetQuadrantDouble(current.getX(), current.getY(), ptCurrentIntersect.getX(), ptCurrentIntersect.getY());

                //case: both inside
                if(previousQuadrant==clipBoundsQuadrant && currentQuadrant==clipBoundsQuadrant)
                    ptsResult.add(current);                                        
                else if(previousQuadrant==clipBoundsQuadrant && currentQuadrant!=clipBoundsQuadrant)//previous inside, current outside
                {
                    edge = new Line2D.Double(pt1, pt2);
                    intersectPt = intersectPoint2(previous, current, edge);
                    if (intersectPt != null) 
                    {
                        ptsResult.add(intersectPt);
                    }
                    tg.set_WasClipped(true);
                }
                else if(previousQuadrant!=clipBoundsQuadrant && currentQuadrant==clipBoundsQuadrant)//current inside, previous outside
                {
                    edge = new Line2D.Double(pt1, pt2);
                    intersectPt = intersectPoint2(previous, current, edge);
                    if (intersectPt != null) 
                    {
                        ptsResult.add(intersectPt);
                    }
                    ptsResult.add(current);
                    tg.set_WasClipped(true);
                }
                else if(previousQuadrant!=clipBoundsQuadrant && currentQuadrant!=clipBoundsQuadrant)
                    continue;
            }//end for j=0 to pts.size()-1
        }//end try
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "clipSide",
                    new RendererException("Failed inside clipSide", exc));
        }    
        return ptsResult;
    }
    /**
     * for pre-clipped lines which also require fill but need the processed points
     * to create the fill. This function is called after the clip, so the fill
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
     * @return
     */
    protected static ArrayList<Shape2> LinesWithFill(TGLight tg,
            ArrayList<Point2D> clipBounds)
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
            shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
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
            ArrayList<Point2D> clipBounds)
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
            int n=0;
            n=tg.Pixels.size();
            
            for(j=0;j<n;j++)
                tg2.Pixels.add(tg.Pixels.get(j));

            closeAreaTG(tg2);

            if(clipBounds != null)
                ClipPolygon(tg2,clipBounds);

            if(tg2.Pixels==null || tg2.Pixels.isEmpty())
                return shapes;

            shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
            shape.setFillColor(tg.get_FillColor());

            shape.moveTo(tg2.Pixels.get(0));
            //original pixels do not include feint
            for(j=1;j<tg2.Pixels.size();j++)
                shape.lineTo(tg2.Pixels.get(j));

            shapes.add(shape);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "fillDMA",
                    new RendererException("Failed inside fillDMA", exc));
        }
        return shapes;
    }
//    private static Boolean isClosed(ArrayList<POINT2>pts)
//    {
//        boolean closed=false;
//        POINT2 pt0=pts.get(0);
//        POINT2 ptLast=pts.get(pts.size()-1);
//        if(pt0.x==ptLast.x && pt0.y==ptLast.y)
//            closed=true;
//        return closed;
//    }
    /**
     * 
     * @param tg
     * @param clipBounds polygon representing clipping area
     * @return 
     */
    protected static ArrayList<Point2D> ClipPolygon(TGLight tg,
                                                    ArrayList<Point2D> clipBounds) {
        ArrayList<Point2D> poly = new ArrayList();
        try 
        {            
            //diagnostic
            Boolean isClosed = clsUtility.isClosedPolygon(tg.get_LineType());            
            //Boolean isClosed = isClosed(tg.Pixels);
            //M. Deutch commented one line 12-27-12
            //clipBounds=clsUtilityGE.expandPolygon(clipBounds, 20);
            clipBounds=clsUtilityGE.expandPolygon(clipBounds, 20);
            //int n=clipBounds.size();
            ArrayList polygon = clsUtilityCPOF.POINT2toPoint2D(tg.Pixels);            
            
            int j=0;
            Map<String,Object>hashMap=new HashMap<String,Object>();
            //int hashCode=0;
            for(j=0;j<polygon.size();j++)            
                hashMap.put(Integer.toString(j), polygon.get(j));
            
            //close the clipbounds if necessary
            Point2D clipBoundsPtStart=clipBounds.get(0);
            Point2D clipBoundsPtEnd=clipBounds.get(clipBounds.size()-1);
            if(clipBoundsPtStart.getX() != clipBoundsPtEnd.getX() ||
                    clipBoundsPtStart.getY() != clipBoundsPtEnd.getY())
                clipBounds.add(clipBoundsPtStart);
                        
            int addedLinePoints = 0;
            if (isClosed)             
                polygon.remove(polygon.size() - 1);             
            else 
            {                     
                addedLinePoints = AddBoundaryPointsForLines(polygon, clipBounds);
            }

            //for(j=0;j<clipBounds.size()-1;j++)
            for(j=0;j<clipBounds.size()-1;j++)
            {
                if(j==0)
                    poly=clipSide(tg,polygon,j,clipBounds);
                else
                    poly=clipSide(tg,poly,j,clipBounds);
            }
            
            
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
                    tg.Pixels = clsUtilityCPOF.Point2DtoPOINT2Mapped(poly,hashMap);
                } 
                else
                {
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
