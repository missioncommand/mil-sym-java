/*
 * A class to create renderables for the ShapeInfo from the GeneralPath
 * This class is used for the GoogleEarth Renderer
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.RenderMultipoints;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.JavaLineArray.TacticalLines;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;

import armyc2.c5isr.JavaLineArray.Shape2;
import armyc2.c5isr.JavaLineArray.lineutility;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.JavaLineArray.POINT2;
import static armyc2.c5isr.JavaLineArray.lineutility.CalcDistanceDouble;
import armyc2.c5isr.JavaTacticalRenderer.clsMETOC;
import armyc2.c5isr.renderer.utilities.RendererSettings;


/**
 * Utilities require for GoogleEarth functionality
 * 
 */
public final class clsUtilityGE {
    private static final String _className="clsUtilityGE";
    protected static void setSplineLinetype(TGLight tg)
    {
        switch(tg.get_LineType())
        {
            case TacticalLines.BRDGHD:
                tg.set_LineType(TacticalLines.BRDGHD_GE);
                break;
            case TacticalLines.HOLD:
                tg.set_LineType(TacticalLines.HOLD_GE);
                break;
            case TacticalLines.ICE_OPENINGS_FROZEN:
                tg.set_LineType(TacticalLines.ICE_OPENINGS_FROZEN_GE);
                break;
            case TacticalLines.ICE_OPENINGS_LEAD:
                tg.set_LineType(TacticalLines.ICE_OPENINGS_LEAD_GE);
                break;
            case TacticalLines.ICE_EDGE_RADAR:
                tg.set_LineType(TacticalLines.ICE_EDGE_RADAR_GE);
                break;
            case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                tg.set_LineType(TacticalLines.CRACKS_SPECIFIC_LOCATION_GE);
                break;
            case TacticalLines.JET:
                tg.set_LineType(TacticalLines.JET_GE);
                break;
            case TacticalLines.STREAM:
                tg.set_LineType(TacticalLines.STREAM_GE);
                break;
            case TacticalLines.FLOOD_TIDE:
                tg.set_LineType(TacticalLines.FLOOD_TIDE_GE);
                break;
            case TacticalLines.EBB_TIDE:
                tg.set_LineType(TacticalLines.EBB_TIDE_GE);
                break;
            case TacticalLines.SEAWALL:
                tg.set_LineType(TacticalLines.SEAWALL_GE);
                break;
            case TacticalLines.JETTY_BELOW_WATER:
                tg.set_LineType(TacticalLines.JETTY_BELOW_WATER_GE);
                break;
            case TacticalLines.JETTY_ABOVE_WATER:
                tg.set_LineType(TacticalLines.JETTY_ABOVE_WATER_GE);
                break;
            case TacticalLines.RAMP_BELOW_WATER:
                tg.set_LineType(TacticalLines.RAMP_BELOW_WATER_GE);
                break;
            case TacticalLines.RAMP_ABOVE_WATER:
                tg.set_LineType(TacticalLines.RAMP_ABOVE_WATER_GE);
                break;
            case TacticalLines.PIER:
                tg.set_LineType(TacticalLines.PIER_GE);
                break;
            case TacticalLines.COASTLINE:
                tg.set_LineType(TacticalLines.COASTLINE_GE);
                break;
            case TacticalLines.DEPTH_CONTOUR:
                tg.set_LineType(TacticalLines.DEPTH_CONTOUR_GE);
                break;
            case TacticalLines.DEPTH_CURVE:
                tg.set_LineType(TacticalLines.DEPTH_CURVE_GE);
                break;
            case TacticalLines.CRACKS:
                tg.set_LineType(TacticalLines.CRACKS_GE);
                break;
            case TacticalLines.ESTIMATED_ICE_EDGE:
                tg.set_LineType(TacticalLines.ESTIMATED_ICE_EDGE_GE);
                break;
            case TacticalLines.ICE_EDGE:
                tg.set_LineType(TacticalLines.ICE_EDGE_GE);
                break;
            case TacticalLines.ISOTHERM:
                tg.set_LineType(TacticalLines.ISOTHERM_GE);
                break;
            case TacticalLines.UPPER_AIR:
                tg.set_LineType(TacticalLines.UPPER_AIR_GE);
                break;
            case TacticalLines.ISOBAR:
                tg.set_LineType(TacticalLines.ISOBAR_GE);
                break;
            case TacticalLines.ISODROSOTHERM:
                tg.set_LineType(TacticalLines.ISODROSOTHERM_GE);
                break;
            case TacticalLines.ISOTACH:
                tg.set_LineType(TacticalLines.ISOTACH_GE);
                break;
            case TacticalLines.ISOPLETHS:
                tg.set_LineType(TacticalLines.ISOPLETHS_GE);
                break;
            default:
                break;
        }
        return;
    }
    
    /**
     * GE has no capability for dashed lines. This function sets each polyline in the array as a new
     * polyline broken into points corresponding to the dash pattern
     * @param polylines
     * @param shape
     */
    private static void createDashedPolylines(ArrayList<ArrayList<Point2D>>polylines, ShapeInfo shape) {
        try {
            if (shape.getLineColor() == null) {
                return;
            }

            BasicStroke stroke = shape.getStroke();
            float[] dash = stroke.getDashArray();
            if (dash == null || dash.length < 2) {
                return;
            }

            ArrayList<ArrayList<Point2D>> dashedPolylines = new ArrayList<>();

            for (ArrayList<Point2D> polyline : polylines) {
                int dashIndex = 0; // Current index in dash array
                double remainingInIndex = dash[dashIndex]; // Length remaining in current dash array index
                for (int i = 0; i < polyline.size() - 1; i++) {
                    Point2D segStartPt = polyline.get(i); // segment start, moves as segment is processed
                    final Point2D segEndPt = polyline.get(i + 1); // Segment end

                    double segLength; // distance remaining in segment
                    while ((segLength = lineutility.CalcDistanceDouble(segStartPt, segEndPt)) > 0) {
                        // If the line segment length is shorter than the current dash then move to the end of the segment continuing to draw or move
                        // Otherwise move to the end of the current dash and start the next dash there
                        if (segLength < remainingInIndex) {
                            if (dashIndex % 2 == 0) {
                                // Continue line
                                ArrayList<Point2D> dashedPolyline = new ArrayList<>(Arrays.asList(segStartPt, segEndPt));
                                dashedPolylines.add(dashedPolyline);
                            }
                            remainingInIndex -= segLength;
                            break; // Next segment
                        } else {
                            // Flip to line or space at dashFlipPoint
                            Point2D dashFlipPoint = lineutility.ExtendAlongLineDouble2(segStartPt, segEndPt, remainingInIndex);
                            if (dashIndex % 2 == 0) {
                                // Continue line
                                ArrayList<Point2D> dashedPolyline = new ArrayList<>(Arrays.asList(segStartPt, dashFlipPoint));
                                dashedPolylines.add(dashedPolyline);
                            }
                            // Next dash
                            dashIndex++;
                            if (dashIndex >= dash.length)
                                dashIndex = 0;
                            remainingInIndex = dash[dashIndex];
                            segStartPt = dashFlipPoint;
                        }
                    }
                }
            }
            polylines.clear();
            polylines.addAll(dashedPolylines);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "createDashedPolylines",
                    new RendererException("Failed inside createDashedPolylines", exc));
        }
    }
    private static ShapeInfo createSimpleFillShape(TGLight tg,ShapeInfo shape,ArrayList<ArrayList<Point2D>>polylines)
    {
        try
        {
            BasicStroke s=shape.getStroke();
            float[]dash=s.getDashArray();
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(tg.get_LineType())==false)
                if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(tg.get_LineType())==false)
                    return null;
            if(dash==null || dash.length<2)
                return null;   
            if(shape.getFillColor()==null)
                return null;
            
            //if we reach this point we know it is a dashed line so we need a separate fill shape
            int j=0,k=0;
            ShapeInfo shape2=new ShapeInfo(shape.getShape());
            shape2.setShapeType(ShapeInfo.SHAPE_TYPE_FILL);
            ArrayList<ArrayList<Point2D>>polylines2=new ArrayList();
            ArrayList<Point2D>polyline=null,polyline2=null;
            Point2D pt2d=null;
            s=new BasicStroke(0);
            shape2.setStroke(s);
            shape2.setFillColor(shape.getFillColor());            
            int n=polylines.size();
            //for(j=0;j<polylines.size();j++)
            for(j=0;j<n;j++)
            {
                polyline=polylines.get(j);
                polyline2=new ArrayList();
                int t=polyline.size();
                //for(k=0;k<polyline.size();k++)
                for(k=0;k<t;k++)
                {
                    pt2d=new Point2D.Double(polyline.get(k).getX(),polyline.get(k).getY());
                    polyline2.add(pt2d);
                }
                polylines2.add(polyline2);
            }
            //reset our original dashed shapinfo type to polyline
            shape.setShapeType(ShapeInfo.SHAPE_TYPE_POLYLINE);
            //this line will prevent unecessary work by multipointhandler
            shape.setFillColor(null);
            shape2.setPolylines(polylines2);            
//            shape2.setAffineTransform(new AffineTransform());
            return shape2;
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "createSimpleFillShape",
                    new RendererException("Failed inside createSimpleFillShape", exc));
        }
        return null;
    }
    private static ShapeInfo createSimplePatternFillShape(TGLight tg,ShapeInfo shape,ArrayList<ArrayList<Point2D>>polylines)
    {
        try
        {
            BasicStroke s=shape.getStroke();
            float[]dash=s.getDashArray();
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(tg.get_LineType())==false)
                if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(tg.get_LineType())==false)
                    return null;
            if(dash==null || dash.length<2)
                return null;
            if(shape.getPatternFillImage()==null)
                return null;

            //if we reach this point we know it is a dashed line so we need a separate pattern fill shape
            int j=0,k=0;
            ShapeInfo shape2=new ShapeInfo(shape.getShape());
            shape2.setShapeType(ShapeInfo.SHAPE_TYPE_FILL);
            ArrayList<ArrayList<Point2D>>polylines2=new ArrayList();
            ArrayList<Point2D>polyline=null,polyline2=null;
            Point2D pt2d=null;
            s=new BasicStroke(0);
            shape2.setStroke(s);
            shape2.setPatternFillImage(shape.getPatternFillImage());
            shape2.setTexturePaint(shape.getTexturePaint());
            int n=polylines.size();
            //for(j=0;j<polylines.size();j++)
            for(j=0;j<n;j++)
            {
                polyline=polylines.get(j);
                polyline2=new ArrayList();
                int t=polyline.size();
                //for(k=0;k<polyline.size();k++)
                for(k=0;k<t;k++)
                {
                    pt2d=new Point2D.Double(polyline.get(k).getX(),polyline.get(k).getY());
                    polyline2.add(pt2d);
                }
                polylines2.add(polyline2);
            }
            //reset our original dashed shapinfo type to polyline
            shape.setShapeType(ShapeInfo.SHAPE_TYPE_POLYLINE);
            //this line will prevent unecessary work by multipointhandler
            shape.setPatternFillImage(null);
            shape.setTexturePaint(null);
            shape2.setPolylines(polylines2);
//            shape2.setAffineTransform(new AffineTransform());
            return shape2;
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "createSimplePatternFillShape",
                    new RendererException("Failed inside createSimplePatternFillShape", exc));
        }
        return null;
    }
    private static boolean allowFillForThese(TGLight tg)
    {
        try
        {
            int linetype=tg.get_LineType();
            int bolMETOC=clsMETOC.IsWeather(tg.get_SymbolId());
            if(bolMETOC >= 0)            
                return true;            

            switch(linetype)
            {
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.MAIN:
                case TacticalLines.SPT:
                
                case TacticalLines.SARA:
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                case TacticalLines.RANGE_FAN:
                case TacticalLines.MNFLDFIX:
                case TacticalLines.TURN:
                case TacticalLines.MNFLDDIS:
                //case TacticalLines.OVERHEAD_WIRE:
                case TacticalLines.EASY:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.FERRY:
                case TacticalLines.BYDIF:
                case TacticalLines.BYIMP:
                case TacticalLines.DEPTH_AREA:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "allowFillForThese",
                    new RendererException("Failed inside allowFillForThese", exc));
        }
        return false;
    }
    protected static void SetShapeInfosPolylines(TGLight tg, ArrayList<ShapeInfo> shapeInfos, Object clipBounds)
    {
        try
        {
            int j=0;
            Shape shape=null;
            ShapeInfo shapeInfo=null;
            ArrayList<ArrayList<Point2D>>polylines=null;
            int type=-1;
            ShapeInfo simpleFillShape =null;//diagnostic
            Boolean isClosed= armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(tg.get_LineType());
            int linetype=tg.get_LineType();
            Color fillColor=null;
            int n=shapeInfos.size();
            //for(j=0;j<shapeInfos.size();j++)
            for(j=0;j<n;j++)
            {
                shapeInfo=shapeInfos.get(j);
                type=shapeInfo.getShapeType();
                shape=shapeInfo.getShape();
                if(isClosed==false && type != Shape2.SHAPE_TYPE_FILL)
                    polylines=createRenderablesFromShape(tg,shape,type,clipBounds);
                else
                    polylines=createRenderablesFromShape(tg,shape,type,null);
                //create a simple fill shape here and change the shape type to SHAPE_TYPE_POLYLINE if it has non-null dash
                //add the simple fill shape to shapeInfos after the loop
                if(simpleFillShape==null)
                    simpleFillShape=createSimpleFillShape(tg,shapeInfo,polylines);
                if(simpleFillShape==null)
                    simpleFillShape=createSimplePatternFillShape(tg,shapeInfo,polylines);

                fillColor=shapeInfo.getFillColor();
                //if(simpleFillShape!=null || fillColor != null)//the symbol has a basic fill shape
                if(simpleFillShape!=null)//the symbol has a basic fill shape
                    if(allowFillForThese(tg)==false)
                        shapeInfo.setFillColor(null);

                if (!tg.get_UseDashArray())
                    createDashedPolylines(polylines,shapeInfo);
                
                shapeInfo.setPolylines(polylines);
            }            
            if(simpleFillShape != null)
                shapeInfos.add(0,simpleFillShape);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "SetShapeInfosPolylines",
                    new RendererException("Failed inside SetShapeInfosPolylines", exc));
        }
    }
    /**
     * Separates the Shape into separate polylines, eas as an ArrayList of Point2D
     * @param shape
     * @return
     */
    private static ArrayList<ArrayList<Point2D>>createRenderablesFromShape(TGLight tg, Shape shape, int shapeType, Object clipArea)
    {
        ArrayList<Point2D> ptsPoly=new ArrayList();
        ArrayList<ArrayList<Point2D>>polylines2=new ArrayList<ArrayList<Point2D>>();
        Point2D ptPoly=null;        
        try 
        {
            //this is not going to work for splines
            double[] coords = new double[6];
            for (PathIterator i = shape.getPathIterator(null); !i.isDone(); i.next())
            {
                int type = i.currentSegment(coords);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        //newshape.moveTo(coords[0], coords[1]);
                        //finalize the last Polyline and add it to the array
                        if(ptsPoly.size()>0)
                        {
                            if(shapeType==ShapeInfo.SHAPE_TYPE_FILL)
                            {
                                if(ptsPoly.get(ptsPoly.size()-1).getX() != ptsPoly.get(0).getX() || 
                                        ptsPoly.get(ptsPoly.size()-1).getY() != ptsPoly.get(0).getY() )
                                {
                                    Point2D pt2d=new Point2D.Double(ptsPoly.get(0).getX(), ptsPoly.get(0).getY());
                                    ptsPoly.add(pt2d);
                                }
                            }                           
                            if(ptsPoly.size()>1)
                                polylines2.add(ptsPoly);
                        }
                        //start the ArrayList for next Polyline                       
                        ptsPoly=new ArrayList();
                        ptPoly=new Point2D.Double(coords[0], coords[1]);
                        ptsPoly.add(ptPoly);
                        break;
                    case PathIterator.SEG_LINETO:
                        //newshape.lineTo(coords[0], coords[1]);
                        ptPoly=new Point2D.Double(coords[0],coords[1]);
                        ptsPoly.add(ptPoly);                        
                        break;
                    case PathIterator.SEG_QUADTO: //quadTo was never used
                        //no idea what to do with this
                        //newshape.quadTo(coords[0], coords[1], coords[2], coords[3]);
                        break;
                    case PathIterator.SEG_CUBICTO:  //curveTo was used for some METOC's
                        //no idea what to do with these
                        //newshape.curveTo(coords[0], coords[1], coords[2], coords[3],
                        //        coords[4], coords[5]);
                        break;
                    case PathIterator.SEG_CLOSE:    //closePath was never used
                        //newshape.closePath();
                        break;
                }
            }
            if(ptsPoly.size()>1)
            {
                //add the last line to the ArrayList
                //if it is a fill shape then the Google Earth linear ring requires the last point be added
                if(shapeType==ShapeInfo.SHAPE_TYPE_FILL)
                {
                    if(ptsPoly.get(ptsPoly.size()-1).getX() != ptsPoly.get(0).getX() || 
                            ptsPoly.get(ptsPoly.size()-1).getY() != ptsPoly.get(0).getY() )
                    {
                        Point2D pt2d=new Point2D.Double(ptsPoly.get(0).getX(), ptsPoly.get(0).getY());
                        ptsPoly.add(pt2d);
                    }
                }
                polylines2.add(ptsPoly);                
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "createRenderableFromShape",
                    new RendererException("Failed inside createRenderableFromShape", exc));
        }
        //return newshape;
        return polylines2;
    }   
    /**
     * Assumes a convex polygon for the clipping area.
     * expand the polygon using pixels and a similar algorithm to what flash renderer does for DEPTH AREA
     * @param pts clipping area to expand
     * @param expand pixels expansion
     * @return
     */
    protected static ArrayList<Point2D>expandPolygon(ArrayList<Point2D>pts,
                                                     double expand)
    {
        ArrayList<Point2D>lgPoly=null;
        try
        {
            int j=0;
            Point2D[]destPts=null;
            boolean isClosed=false;
            if(pts.get(pts.size()-1).getX()==pts.get(0).getX() && pts.get(pts.size()-1).getY()==pts.get(0).getY())
            {
                pts.remove(pts.size()-1);
                isClosed=true;
            }
            ArrayList<POINT2>pts2=clsUtility.Points2DToPOINT2(pts);
            POINT2 pt0=null,pt1=null,pt2=null,pt3=null;
            double m=0,m1=0,b=0,b1=0;
            ArrayList<Line2D>lineSegments=new ArrayList();
            //n vertical segments
            int n=pts2.size();
            //for(j=0;j<pts2.size()-1;j++)
            for(j=0;j<n-1;j++)
            {
                pt0=new POINT2(pts2.get(j));
                pt1=new POINT2(pts2.get(j+1));
                //no vertical segments
                if(pt0.x==pt1.x)
                {
                    pt1.x+=1;                                
                    pts2.set(j+1, pt1);
                }
            }
            POINT2 ptn=pts2.get(pts2.size()-1);
            pt0=new POINT2(pts2.get(0));
            //last segment not vertical
            if(ptn.x==pt0.x)
            {
                ptn.x+=1;
                pts2.set(pts2.size()-1, ptn);
            }
            //close pts2
            pts2.add(pt0);;
            
            //POINT2 ptOther=null;
            //int quadrant=-1,otherQuadrant=-1;
            Polygon poly=new Polygon();
            n=pts2.size();
            //for(j=0;j<pts2.size();j++)
            for(j=0;j<n;j++)
                poly.addPoint((int)pts2.get(j).x, (int)pts2.get(j).y);
            
            Line2D lineSegment=null;
            POINT2 midPt=null;
            //pts2 is closed
            n=pts2.size();
            //for(j=0;j<pts2.size()-1;j++)
            for(j=0;j<n-1;j++)
            {                
                pt0=new POINT2(pts2.get(j));
                pt1=new POINT2(pts2.get(j+1));                
                m=(pt0.y-pt1.y)/(pt0.x-pt1.x);
                //m1=-1/m;
                if(Math.abs(m)<1)
                {
                    pt2=lineutility.ExtendDirectedLine(pt0, pt1, pt0, lineutility.extend_above, expand);
                    pt3=lineutility.ExtendDirectedLine(pt0, pt1, pt1, lineutility.extend_above, expand);
                    midPt=lineutility.MidPointDouble(pt2, pt3, 0);
                    //we want the polygon to not contain the extended points
                    if(poly.contains(midPt.x, midPt.y))
                    {
                        pt2=lineutility.ExtendDirectedLine(pt0, pt1, pt0, lineutility.extend_below, expand);
                        pt3=lineutility.ExtendDirectedLine(pt0, pt1, pt1, lineutility.extend_below, expand);                        
                    }                                        
                }
                else
                {
                    pt2=lineutility.ExtendDirectedLine(pt0, pt1, pt0, lineutility.extend_left, expand);
                    pt3=lineutility.ExtendDirectedLine(pt0, pt1, pt1, lineutility.extend_left, expand);
                    midPt=lineutility.MidPointDouble(pt2, pt3, 0);
                    //we want the polygon to not contain the extended points
                    if(poly.contains(midPt.x, midPt.y))
                    {
                        pt2=lineutility.ExtendDirectedLine(pt0, pt1, pt0, lineutility.extend_right, expand);
                        pt3=lineutility.ExtendDirectedLine(pt0, pt1, pt1, lineutility.extend_right, expand);                        
                    }                                                            
                }
                lineSegment=new Line2D.Double(pt2.x, pt2.y, pt3.x, pt3.y);
                lineSegments.add(lineSegment);
            }
            //we will intersect the line segments to form an expanded polygon
            ArrayList<POINT2> expandPts=new ArrayList();
            Line2D thisLine=null,nextLine=null;
            double x1=0,y1=0,x2=0,y2=0,x=0,y=0;
            int t=lineSegments.size();
            //for(j=0;j<lineSegments.size();j++)
            for(j=0;j<t;j++)
            {
                thisLine=lineSegments.get(j);
                x1=thisLine.getX1();
                y1=thisLine.getY1();
                x2=thisLine.getX2();
                y2=thisLine.getY2();
                //thisLine line equation
                m=(y1-y2)/(x1-x2);
                b=y1-m*x1;
                
                if(j==lineSegments.size()-1)
                    nextLine=lineSegments.get(0);
                else
                    nextLine=lineSegments.get(j+1);
                
                x1=nextLine.getX1();
                y1=nextLine.getY1();
                x2=nextLine.getX2();
                y2=nextLine.getY2();
                //nextLine line equation
                m1=(y1-y2)/(x1-x2);
                b1=y1-m1*x1;
                
                //intersect thisLine with nextLine
                if(m != m1)
                {
                    x=(b1-b)/(m-m1);	//cannot blow up
                    y=(m*x+b);
                }
                else    //this should not happen
                {
                    x=thisLine.getX2();
                    y=thisLine.getY2();
                }
                expandPts.add(new POINT2(x,y));
            }           
            lgPoly=new ArrayList();
            t=expandPts.size();
            //for(j=0;j<expandPts.size();j++)            
            for(j=0;j<t;j++)            
                lgPoly.add(new Point2D.Double(expandPts.get(j).x, expandPts.get(j).y));
            
            //close the aray if the original clipping array if applicable
            if(isClosed)
                lgPoly.add( new Point2D.Double( lgPoly.get(0).getX(),lgPoly.get(0).getY() ) );
        }
        catch (Exception exc) 
        {
            ErrorLogger.LogException(_className, "expandPolygon2",
                    new RendererException("Failed inside expandPolygon2", exc));
        }
        return lgPoly;
    }
    /**
     * use cheap algorithm to expand polygons, works best on regular 4+ sided convex polygons
     * used primarily for expanding the original clipping areas. After clipping a tactical line against
     * the expanded clipping area, the original clipping area can be used to drop the clip lines
     * @param pts points to expand, usually a clipping area
     * @param expandX X expansion factor, e.g 10% growth would be 1.1
     * @param expandY Y expansion factor
     * @return points for the expanded polygon
     */
    protected static ArrayList<Point2D>expandPolygon2(ArrayList<Point2D>pts,
            double expandX, 
            double expandY)
    {
        ArrayList<Point2D>lgPoly=null;
        try
        {
//            AffineTransform at=new AffineTransform();
//            at.setToIdentity();        
            //get the center of the pts using an average
            double avgX=0,avgY=0,totalX=0,totalY=0;
            int j=0;
            boolean isClosed=false;
            //open the array, remove the last point if necessary
            if(pts.get(pts.size()-1).getX()==pts.get(0).getX() && pts.get(pts.size()-1).getY()==pts.get(0).getY())
            {
                pts.remove(pts.size()-1);
                isClosed=true;
            }
            //asumes open array
            int n=pts.size();
            //for(j=0;j<pts.size();j++)
            for(j=0;j<n;j++)
            {
                totalX+=pts.get(j).getX();
                totalY+=pts.get(j).getY();
            }
            avgX=totalX/pts.size();
            avgY=totalY/pts.size();
            Point2D.Double[]srcPts=new Point2D.Double[pts.size()];
            //for(j=0;j<pts.size();j++)
            n=pts.size();
            for(j=0;j<n;j++)
            {
                srcPts[j]=new Point2D.Double(pts.get(j).getX(),pts.get(j).getY());
            }
            Point2D[]destPts=new Point2D[pts.size()];
            //translate the points to crcumscribe 0,0
//            at.translate(-avgY, -avgY);//ideally would be close to 0        
//            at.transform(srcPts, 0, destPts, 0, srcPts.length);
//            at.setToIdentity();
            //scale the points by 10%
//            at.scale(expandX, expandY);
//            at.transform(destPts, 0, destPts, 0, destPts.length);
//            at.setToIdentity();
//            at.translate(avgY, avgY);
//            at.transform(destPts, 0, destPts, 0, destPts.length);
            lgPoly=new ArrayList<Point2D>();
            int t=destPts.length;
            //for(j=0;j<destPts.length;j++)
            for(j=0;j<t;j++)
            {
                lgPoly.add(destPts[j]);
            }
            //close the aray if the original clipping array was closed
            if(isClosed)
                lgPoly.add(new Point2D.Double(destPts[0].getX(),destPts[0].getY()));
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "expandPolygon",
                    new RendererException("Failed inside expandPolygon", exc));
        }
        return lgPoly;
    }
    /**
     * @deprecated 
     * For tactical lines break up the arraylists into separate arraylists within the bounds.
     * This was added for the Google Earth 3D map because small scales cut off and we want the clip lines
     * to not be visible.
     * @param ptsPoly
     * @param clipBounds
     * @return 
     */
    private static ArrayList<ArrayList<Point2D>> ptsPolyToPtsPoly(TGLight tg, ArrayList<ArrayList<Point2D>>ptsPoly,
            Rectangle2D clipBounds)
    {
        ArrayList<ArrayList<Point2D>> ptsPoly2=null;
        try
        {
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(tg.get_LineType())==true)
                return ptsPoly;
            
            int j=0,k=0;
            ArrayList<Point2D>pts=null;
            ArrayList<Point2D>addPts=null;
            Point2D pt0=null;
            Point2D pt1=null;
            Line2D line=null;
            ptsPoly2=new ArrayList();
            int n=ptsPoly.size();
            //for(j=0;j<ptsPoly.size();j++)
            for(j=0;j<n;j++)
            {
                addPts=null;
                pts=ptsPoly.get(j);
                //find the first point inside the clipbounds
                int t=pts.size();
                //for(k=0;k<pts.size()-1;k++)
                for(k=0;k<t-1;k++)
                {
                    pt0=pts.get(k);
                    pt1=pts.get(k+1);
                                        
                    line=new Line2D.Double(pt0,pt1);
                    //both points out of bounds, do not add points
                    if(clipBounds.contains(pt0)==false && clipBounds.contains(pt1)==false)
                    {                                                
                        if(clipBounds.intersectsLine(line)==false)
                        {
                            addPts=null;
                            continue;
                        }
                        else
                        {
                            if(addPts==null)
                            {
                                addPts=new ArrayList();
                                addPts.add(pt0);
                            }
                            if(addPts.contains(pt0)==false)
                                addPts.add(pt0);
                            
                            addPts.add(pt1);
                            ptsPoly2.add(addPts);
                            addPts=null;
                        }
                    }
                    else if(clipBounds.contains(pt0)==false && clipBounds.contains(pt1)==true)
                    {
                        if(addPts == null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        
                        addPts.add(pt1);
                    }
                    else if(clipBounds.contains(pt0)==true && clipBounds.contains(pt1)==true)
                    {
                        if(addPts==null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        
                        addPts.add(pt1);                        
                    }
                    else if(clipBounds.contains(pt0)==true && clipBounds.contains(pt1)==false)
                    {
                        if(addPts==null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        //end the current polyline
                        //and add it to the array list
                        addPts.add(pt1);
                        ptsPoly2.add(addPts);                                                
                        addPts=null;                                                
                    }                                            
                }
                //add the final array list
                if(addPts != null && addPts.size()>0)
                    ptsPoly2.add(addPts);
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "ptsPolyToPtsPoly",
                    new RendererException("Failed inside ptsPolyToPtsPoly", exc));
        }
        return ptsPoly2;
    }
    /**
     * @deprecated 
     * function to remove the clip lines from the polygon that was clipped
     * @param ptsPoly the clipped points array
     * @param clipBounds the clipping points
     * @return 
     */
    private static ArrayList<ArrayList<Point2D>> ptsPolyToPtsPoly(TGLight tg, ArrayList<ArrayList<Point2D>>ptsPoly,
            ArrayList<Point2D> clipBounds)//was rectangle2D clipBounds
    {
        ArrayList<ArrayList<Point2D>> ptsPoly2=null;
        try
        {
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(tg.get_LineType())==true)
                return ptsPoly;
            
            int j=0,k=0;
            ArrayList<Point2D>pts=null;
            ArrayList<Point2D>addPts=null;
            Point2D pt0=null;
            Point2D pt1=null;
            Line2D line=null;
            ptsPoly2=new ArrayList();
            Polygon clipPoly=new Polygon();
            
            //ArrayList<Point2D>ptsClipArea=null;
            int n=clipBounds.size();
            //for(j=0;j<clipBounds.size();j++)    
            for(j=0;j<n;j++)    
            {
                clipPoly.addPoint((int)clipBounds.get(j).getX(), (int)clipBounds.get(j).getY());
            }
            n=ptsPoly.size();
            //for(j=0;j<ptsPoly.size();j++)
            for(j=0;j<n;j++)
            {
                addPts=null;
                pts=ptsPoly.get(j);
                //find the first point inside the clipbounds
                int t=pts.size();
                //for(k=0;k<pts.size()-1;k++)
                for(k=0;k<t-1;k++)
                {
                    pt0=pts.get(k);
                    pt1=pts.get(k+1);
                    line=new Line2D.Double(pt0,pt1);
                    //both points out of bounds, do not add points
                    if(clipPoly.contains(pt0)==false && clipPoly.contains(pt1)==false)
                    {                                                
                        if(lineIntersectsClipArea(line,clipBounds)==false)
                        {
                            addPts=null;
                            continue;
                        }
                        else
                        {
                            if(addPts==null)
                            {
                                addPts=new ArrayList();
                                addPts.add(pt0);
                            }
                            if(addPts.contains(pt0)==false)
                                addPts.add(pt0);
                            
                            addPts.add(pt1);
                            ptsPoly2.add(addPts);
                            addPts=null;
                        }
                    }
                    else if(clipPoly.contains(pt0)==false && clipPoly.contains(pt1)==true)
                    {
                        if(addPts == null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        
                        addPts.add(pt1);
                    }
                    else if(clipPoly.contains(pt0)==true && clipPoly.contains(pt1)==true)
                    {
                        if(addPts==null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        
                        addPts.add(pt1);                        
                    }
                    else if(clipPoly.contains(pt0)==true && clipPoly.contains(pt1)==false)
                    {
                        if(addPts==null)
                        {
                            addPts=new ArrayList();
                            addPts.add(pt0);
                        }
                        if(addPts.contains(pt0)==false)
                            addPts.add(pt0);
                        //end the current polyline
                        //and add it to the array list
                        addPts.add(pt1);
                        ptsPoly2.add(addPts);                                                
                        addPts=null;                                                
                    }                                            
                }
                //add the final array list
                if(addPts != null && addPts.size()>0)
                    ptsPoly2.add(addPts);
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "ptsPolyToPtsPoly",
                    new RendererException("Failed inside ptsPolyToPtsPoly", exc));
        }
        return ptsPoly2;
    }    
    /**
     * removes leading or trailing segments after the points were clipped
     * @param tg
     * @param clipArea 
     */
    protected static void removeTrailingPoints(TGLight tg, Object clipArea)
    {
        try
        {
            boolean isClosed= armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(tg.get_LineType());
            if(isClosed)
                return;
            
            Polygon poly=new Polygon();
            Area area=null;
            Rectangle2D clipBounds=null;
            ArrayList<Point2D>clipPoints=null;
            Point2D pt2d=null;
            int j=0;
            if(clipArea==null)
                return;

            if(clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class))
            {
                clipBounds=(Rectangle2D.Double)clipArea;
            }
            else if(clipArea.getClass().isAssignableFrom(Rectangle.class))
            {
                //clipBounds=(Rectangle2D)clipArea;
                Rectangle rectx=(Rectangle)clipArea;
                clipBounds=new Rectangle2D.Double(rectx.x,rectx.y,rectx.width,rectx.height);
            }
            else if(clipArea.getClass().isAssignableFrom(ArrayList.class))
            {
                clipPoints=(ArrayList<Point2D>)clipArea;            
            }
            if(clipBounds != null)
            {
                clipPoints=new ArrayList<Point2D>();
                clipPoints.add(new Point2D.Double(clipBounds.getX(),clipBounds.getY()));                
                clipPoints.add(new Point2D.Double(clipBounds.getX()+clipBounds.getWidth(),clipBounds.getY()));                
                clipPoints.add(new Point2D.Double(clipBounds.getX()+clipBounds.getWidth(),clipBounds.getY()+clipBounds.getHeight()));                
                clipPoints.add(new Point2D.Double(clipBounds.getX(),clipBounds.getY()+clipBounds.getHeight()));                
                clipPoints.add(new Point2D.Double(clipBounds.getX(),clipBounds.getY()));                
            }   

            Point2D ptLast=clipPoints.get(clipPoints.size()-1);
            Point2D pt02d=clipPoints.get(0);
            Point2D pt12d=null;
            //close the area
            if(pt02d.getX() != ptLast.getX() || pt02d.getY() != ptLast.getY())
            {
                clipPoints.add(new Point2D.Double(pt02d.getX(),pt02d.getY()));
                //poly.addPoint((int)pt02d.getX(),(int)pt02d.getY());
            }
            //fill the polygon
            int n=clipPoints.size();
            //for(j=0;j<clipPoints.size();j++)
            for(j=0;j<n;j++)
            {
                pt02d=clipPoints.get(j);            
                poly.addPoint((int)pt02d.getX(), (int)pt02d.getY());
            }
            area=new Area(poly);
            Line2D line=null;
            POINT2 pt0=null,pt1=null;
            boolean intersects=false;
            int frontIndex=0,backIndex=tg.Pixels.size()-1;
            //breaks at the first leading segment that intersects the clip area
            n=tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size()-1;j++)
            for(j=0;j<n-1;j++)
            {
               pt0=tg.Pixels.get(j);
               pt1=tg.Pixels.get(j+1);
               line=new Line2D.Double(pt0.x, pt0.y, pt1.x, pt1.y);
               intersects=lineIntersectsClipArea(line, clipPoints);
               if(intersects==true)
               {
                   frontIndex=j;
                   break;
               }
               else if(area.contains((int)pt0.x,(int)pt0.y) || area.contains((int)pt1.x,(int)pt1.y))
               {
                   frontIndex=j;
                   break;               
               }           
            }
            //breaks at the first trailing segment that intersects the clip area
            n=tg.Pixels.size();
            //for(j=tg.Pixels.size()-1;j>0;j--)
            for(j=n-1;j>0;j--)
            {
               pt0=tg.Pixels.get(j);
               pt1=tg.Pixels.get(j-1);
               line=new Line2D.Double(pt0.x, pt0.y, pt1.x, pt1.y);
               intersects=lineIntersectsClipArea(line, clipPoints);
               if(intersects==true)
               {
                   backIndex=j;
                   break;
               }
               else if(area.contains((int)pt0.x,(int)pt0.y) || area.contains((int)pt1.x,(int)pt1.y))
               {
                   backIndex=j;
                   break;               
               }           
            }
            ArrayList<POINT2>pts=new ArrayList();
            for(j=frontIndex;j<=backIndex;j++)
            {
                pt0=new POINT2(tg.Pixels.get(j));
                pts.add(pt0);
            }
            tg.Pixels=pts;           
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsRenderer" ,"removeTrailingPoints",
                    new RendererException("Failed inside removeTrailingPoints", exc));
        }
    }
/**
     * tests of a Line2D intersects a polygon by using line.intersectsLine on each segment of the polygon
     * assumes clip clipping area was parsed to shift points of vertical segments to make them not vertical
     * @param line a clipping line in the clipping polygon
     * @param clipPts array of clip points assumed to be closed
     * @return true if the line intersects the clip bounds
     */
    private static boolean lineIntersectsClipArea(Line2D line, 
            ArrayList<Point2D> clipPts)
    {
        boolean result=false;
        try
        {
            int j=0;           
            
            //test if polygon contains an end point
            Polygon poly=new Polygon();
            int n=clipPts.size();
            //for(j=0;j<clipPts.size();j++)            
            for(j=0;j<n;j++)            
                poly.addPoint((int)clipPts.get(j).getX(),(int)clipPts.get(j).getY());
            
            if(poly.contains(line.getX1(),line.getY1()))
                return true;
            if(poly.contains(line.getX2(),line.getY2()))
                return true;
            //end section
            
            Line2D currentSegment=null;
            n=clipPts.size();
            //for(j=0;j<clipPts.size()-1;j++)
            for(j=0;j<n-1;j++)
            {
                currentSegment=new Line2D.Double(clipPts.get(j).getX(),clipPts.get(j).getY(),clipPts.get(j+1).getX(),clipPts.get(j+1).getY());
                if(line.intersectsLine(currentSegment)==true)
                    return true;            
            }
            //if the clipPts are not closed then the above loop did not test the closing segment            
            Point2D pt0=clipPts.get(0);
            Point2D ptLast=clipPts.get(clipPts.size()-1);
            //int n=clipPts.size()-1;            
            if(pt0.getX()!=ptLast.getX() || pt0.getY()!=ptLast.getY())
            {
                //currentSegment=new Line2D.Double(clipPts.get(n).getX(),clipPts.get(n).getY(),clipPts.get(0).getX(),clipPts.get(0).getY());
                currentSegment=new Line2D.Double(ptLast.getX(),ptLast.getY(),pt0.getX(),pt0.getY());
                if(line.intersectsLine(currentSegment)==true)
                    return true;                            
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "lineIntersectsClipArea",
                    new RendererException("Failed inside lineIntersectsClipArea", exc));
        }
        return result;
    }
    /**
     * returns true if segment data set for MSR, ASR, Boundary
     * @param tg
     * @return 
     */
    protected static boolean segmentColorsSet(TGLight tg)
    {
        try
        {
            switch(tg.get_LineType())
            {
                case TacticalLines.BOUNDARY:
                case TacticalLines.MSR:
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                    break;
                default:
                    return false;
            }
            String strH=tg.get_H();
            if(strH==null || strH.isEmpty())
                return false;
            String[] strs=strH.split(",");
            if(strs.length>1)
                return true;            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className, "segmentColorsSet",
                    new RendererException("Failed inside segmentColorsSet", exc));
        }
        return false;
    }
    /**
     * Use clipping rectangle or clip points to build a zoom factor if the client zoomed in after the initial render.
     * Multiply the geo segmenting interval by this factor.
     * @param rect
     * @param clipPoints
     * @param pixels
     * @return 
     */
    protected static double getZoomFactor(Rectangle2D rect, ArrayList<Point2D> clipPoints, ArrayList<POINT2>pixels)
    {
        double factor=-1;
        try
        {
            if(pixels==null || pixels.size()<2)
                return factor;
            if(clipPoints==null && rect==null)
                return factor;
            double maxLengthPixels=0, maxLengthClipArea=0,temp=0;
            int j=0;
            Point2D pt2d0=null,pt2d1=null;POINT2 pt0=null, pt1=null;
            for(j=0;j<pixels.size()-1;j++)
            {
               pt0=pixels.get(j);
               pt1=pixels.get(j+1);
               temp=lineutility.CalcDistanceDouble(pt0, pt1);
               if(temp>maxLengthPixels)
                   maxLengthPixels=temp;
            }
            temp=0;
            if(clipPoints != null)
            {
                for(j=0;j<clipPoints.size()-1;j++)
                {
                   pt2d0=clipPoints.get(j);
                   pt2d1=clipPoints.get(j+1);
                   pt0=new POINT2(pt2d0.getX(),pt2d0.getY());
                   pt1=new POINT2(pt2d1.getX(),pt2d1.getY());
                   temp=lineutility.CalcDistanceDouble(pt0, pt1);
                }
            }
            else if(rect != null)
            {
                temp=rect.getMaxX()-rect.getMinX();
                if(temp < rect.getMaxY()-rect.getMinY())
                    temp=rect.getMaxY()-rect.getMinY();
            }
            if(temp>maxLengthClipArea)
                maxLengthClipArea=temp;
            if(maxLengthPixels > 0 && maxLengthClipArea > 0)
                factor=maxLengthClipArea/maxLengthPixels;
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className, "getZoomFactor",
                    new RendererException("Failed inside getZoomFactor", exc));
        }
        return factor;
    }
    
}
