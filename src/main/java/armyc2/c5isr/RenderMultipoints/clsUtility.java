/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.RenderMultipoints;
import armyc2.c5isr.renderer.PatternFillRenderer;
import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.JavaLineArray.lineutility;
import armyc2.c5isr.JavaLineArray.POINT2;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import armyc2.c5isr.JavaLineArray.Shape2;

import static armyc2.c5isr.JavaTacticalRenderer.clsUtility.isAutoshape;

/**
 * Server general utility class
 * 
 */
public final class clsUtility {
    private static final String _className="clsUtility";
    public static final int Hatch_ForwardDiagonal=2;
    public static final int Hatch_BackwardDiagonal=3;
    public static final int Hatch_Vertical=4;
    public static final int Hatch_Horizontal=5;
    public static final int Hatch_Cross=8;

    /**
     * Adds hatch fill to shapes via PatternFillRendererD.MakeHatchPatternFill() or buildHatchFill()
     * @param tg
     * @param shapes
     */
    protected static void addHatchFills(TGLight tg, ArrayList<ShapeInfo>shapes)
    {
        try
        {
            if(shapes==null || shapes.size()==0)
                return;

            int lineType=tg.get_LineType();
            int hatchStyle=tg.get_FillStyle();
            int j=0,hatch2=0;
            Shape2 shape2=null;
            int index=0;
            float hatchLineThickness = (float) (tg.get_LineThickness() / 2.0);
            Color hatchColor = tg.get_LineColor();
            int hatchSpacing = (int) (hatchLineThickness * 10);

//            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType)==false)
//                if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(lineType, null)==false)
//                    return;
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType)==false)
            {
                if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(lineType)==false)
                {
                    return;
                }
            }

            switch(lineType)
            {
                case TacticalLines.NFA:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.LAA:
                    hatchStyle = Hatch_BackwardDiagonal;
                    break;
                case TacticalLines.BIO:
                case TacticalLines.NUC:
                case TacticalLines.CHEM:
                case TacticalLines.RAD:
                    hatchStyle=Hatch_BackwardDiagonal;
                    hatchColor = Color.yellow;
                    hatchLineThickness = tg.get_LineThickness();
                    break;
                case TacticalLines.WFZ:
                    hatchStyle=Hatch_BackwardDiagonal;
                    if (tg.get_LineColor() == Color.BLACK)
                        hatchColor = Color.GRAY;
                    hatchSpacing /= 2;
                    break;
                case TacticalLines.OBSAREA:
                    //CPOF client required adding a simple shape for
                    //setting texturepaint which WebRenderer does not use
                    for(j=0;j<shapes.size();j++)
                    {
                        ShapeInfo shape=shapes.get(j);
                        Color color=shape.getLineColor();
                        if(color==null)
                            continue;
                        //if(shape.getLineColor().getRGB()==0)
                        if(shape.getLineColor().getRGB()==0)
                            shapes.remove(j);
                    }
                    hatchStyle = Hatch_BackwardDiagonal;
                    hatchSpacing *= 1.25;
                    break;
                default:
                    if(hatchStyle<=0)
                        return;
                    break;
            }
            //get the index of the shape with the same fillstyle
            int n=shapes.size();
            //for(j=0;j<shapes.size();j++)
            for(j=0;j<n;j++)
            {
                shape2=(Shape2)shapes.get(j);
                hatch2=shape2.get_FillStyle();
                if(hatch2==hatchStyle)
                {
                    index=j;
                    break;
                }
            }
            n=shapes.size();
            //for(int k=0;k<shapes.size();k++)
            for(int k=0;k<n;k++)
            {
                //the outline should always be the 0th shape for areas
                ShapeInfo shape=null;
                if(lineType==TacticalLines.RANGE_FAN || lineType==TacticalLines.RANGE_FAN_SECTOR || lineType==TacticalLines.RADAR_SEARCH)
                {
                    shape=shapes.get(k);
                    shape2=(Shape2)shapes.get(k);
                    hatchStyle=shape2.get_FillStyle();
                }
                else
                    shape=shapes.get(index);

                if(hatchStyle<Hatch_ForwardDiagonal)//Hatch_ForwardDiagonal is the 0th hatch element
                    continue;

                if (tg.get_UseHatchFill())
                {
                    BufferedImage hatchImg = PatternFillRenderer.MakeHatchPatternFill(hatchStyle, hatchSpacing, (int) hatchLineThickness, hatchColor);
                    shape.setPatternFillImage(hatchImg);
                    Rectangle2D rect = new Rectangle2D.Double(0, 0, hatchImg.getWidth(), hatchImg.getHeight());
                    TexturePaint tp = new TexturePaint(shape.getPatternFillImage(), rect);
                    shape.setTexturePaint(tp);
                }
                else if(hatchStyle != Hatch_Cross)
                {
                    Shape2 shape3= buildHatchArea(tg, shape,hatchStyle, hatchSpacing);
                    //shape.setStroke(new BasicStroke(1));
                    shape3.setStroke(new BasicStroke(hatchLineThickness));
                    shape3.setLineColor(hatchColor);
                    shapes.add(shape3);
                }
                else    //cross hatch
                {
                    Shape2 shapeBk= buildHatchArea(tg, shape,Hatch_BackwardDiagonal,hatchSpacing);
                    Shape2 shapeFwd= buildHatchArea(tg, shape,Hatch_ForwardDiagonal,hatchSpacing);
                    //shapeBk.setStroke(new BasicStroke(1));
                    shapeBk.setStroke(new BasicStroke(hatchLineThickness));
                    shapeBk.setLineColor(hatchColor);
                    shapes.add(shapeBk);
                    //shapeFwd.setStroke(new BasicStroke(1));
                    shapeFwd.setStroke(new BasicStroke(hatchLineThickness));
                    shapeFwd.setLineColor(hatchColor);
                    shapes.add(shapeFwd);
                }
                if(lineType != TacticalLines.RANGE_FAN && lineType != TacticalLines.RANGE_FAN_SECTOR && lineType != TacticalLines.RADAR_SEARCH)
                    break;
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "addHatchFills",
                    new RendererException("Failed inside addHatchFills", exc));
        }
    }

    /**
     * Build Hatch fill. Does not use texture paint or shader.
     * @param tg
     * @param shape
     * @param hatchStyle
     * @return
     */
    protected static Shape2 buildHatchArea(TGLight tg, ShapeInfo shape, int hatchStyle, double spacing)
    {
        Shape2 hatchLineShape=null;
        try
        {
            hatchLineShape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
            Area hatchLineArea=null;
            Rectangle rect=shape.getBounds();
            double x0=rect.getX();
            double y0=rect.getY();
            double width=rect.getWidth();
            double height=rect.getHeight();
            //we need a square
            if(width>height)
                height=width;
            else
                width=height;

            //diagnostic
            if(tg.get_UseHatchFill())
            {
//                hatchLineShape.moveTo(new POINT2(x0,y0));
//                hatchLineShape.lineTo(new POINT2(x0+width,y0));
//                hatchLineShape.lineTo(new POINT2(x0+width,y0+width));
//                hatchLineShape.lineTo(new POINT2(x0,y0+width));
                hatchLineShape.set_Fillstyle(hatchStyle);
//                hatchLineShape.lineTo(new POINT2(x0,y0));
//                Area shapeArea=new Area(shape.getShape());
//                hatchLineArea=new Area(hatchLineShape.getShape());
//                //intersect the hatch lines with the original shape area to get the fill
//                hatchLineArea.intersect(shapeArea);
//                hatchLineShape.setShape(hatchLineArea);
                hatchLineShape.setShape(lineutility.createStrokedShape(shape.getShape()));
                return hatchLineShape;
            }
            //end section

            width *= 2;
            height *= 2;
            //the next two values should be equal
            int horizLimit=0;
            int vertLimit=0;
            int j=0;
            ArrayList<POINT2>vertPts=new ArrayList();
            ArrayList<POINT2>horizPts=new ArrayList();
            POINT2 vertPt=null,horizPt=null;
            if(hatchStyle==Hatch_BackwardDiagonal)
            {
                horizLimit=(int)(width/spacing);
                vertLimit=(int)(height/spacing);
                for(j=0;j<vertLimit;j++)
                {
                    vertPt=new POINT2(x0,y0+spacing*j);
                    vertPts.add(vertPt);
                }
                for(j=0;j<horizLimit;j++)
                {
                    horizPt=new POINT2(x0+spacing*j,y0);
                    horizPts.add(horizPt);
                }

                hatchLineShape.moveTo(new POINT2(x0-spacing/2,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0,y0));
                for(j=0;j<vertLimit;j++)
                {
                    if(j%2==0)
                    {
                        hatchLineShape.lineTo(vertPts.get(j));
                        hatchLineShape.lineTo(horizPts.get(j));
                    }
                    else
                    {
                        hatchLineShape.lineTo(horizPts.get(j));
                        hatchLineShape.lineTo(vertPts.get(j));
                    }
                }
                //go outside the bottom right corner to complete a valid area
                hatchLineShape.lineTo(new POINT2(x0+width+spacing/2,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing/2,y0-spacing/2));
            }
            if(hatchStyle==Hatch_ForwardDiagonal)
            {
                horizLimit=(int)(width/spacing);
                vertLimit=(int)(height/spacing);
                width /= 2;
                for(j=0;j<vertLimit;j++)
                {
                    vertPt=new POINT2(x0+width,y0+spacing*j);
                    vertPts.add(vertPt);
                }
                for(j=0;j<horizLimit;j++)
                {
                    horizPt=new POINT2(x0+width-spacing*j,y0);
                    horizPts.add(horizPt);
                }

                hatchLineShape.moveTo(new POINT2(x0+width+spacing/2,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0,y0));
                for(j=0;j<vertLimit;j++)
                {
                    if(j%2==0)
                    {
                        hatchLineShape.lineTo(vertPts.get(j));
                        hatchLineShape.lineTo(horizPts.get(j));
                    }
                    else
                    {
                        hatchLineShape.lineTo(horizPts.get(j));
                        hatchLineShape.lineTo(vertPts.get(j));
                    }
                }
                //go outside the bottom left corner to complete a valid area
                hatchLineShape.lineTo(new POINT2(x0-spacing/2,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing/2,y0-spacing/2));
            }
            if(hatchStyle==Hatch_Vertical)
            {
                horizLimit=(int)(width/(spacing/2));
                vertLimit=(int)(height/(spacing/2));
                for(j=0;j<horizLimit;j++)
                {
                    if(j%2==0)
                    {
                        vertPt=new POINT2(x0+spacing/2*j,y0);
                        vertPts.add(vertPt);
                        vertPt=new POINT2(x0+spacing/2*j,y0+height);
                        vertPts.add(vertPt);
                    }
                    else
                    {
                        vertPt=new POINT2(x0+spacing/2*j,y0+height);
                        vertPts.add(vertPt);
                        vertPt=new POINT2(x0+spacing/2*j,y0);
                        vertPts.add(vertPt);
                    }
                }
                hatchLineShape.moveTo(new POINT2(x0-spacing/2,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0,y0));
                for(j=0;j<vertLimit-1;j++)
                {
                    hatchLineShape.lineTo(vertPts.get(j));
                }
                //go outside the bottom right corner to complete a valid area
                hatchLineShape.lineTo(new POINT2(x0+width+spacing/2,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing/2,y0-spacing/2));
            }
            if(hatchStyle==Hatch_Horizontal)
            {
                horizLimit=(int)(width/(spacing/2));
                vertLimit=(int)(height/(spacing/2));
                for(j=0;j<vertLimit;j++)
                {
                    if(j%2==0)
                    {
                        horizPt=new POINT2(x0,y0+spacing/2*j);
                        horizPts.add(horizPt);
                        horizPt=new POINT2(x0+width,y0+spacing/2*j);
                        horizPts.add(horizPt);
                    }
                    else
                    {
                        horizPt=new POINT2(x0+width,y0+spacing/2*j);
                        horizPts.add(horizPt);
                        horizPt=new POINT2(x0,y0+spacing/2*j);
                        horizPts.add(horizPt);
                    }
                }
                hatchLineShape.moveTo(new POINT2(x0-spacing/2,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0,y0));
                for(j=0;j<vertLimit-1;j++)
                {
                    hatchLineShape.lineTo(horizPts.get(j));
                }
                //go outside the bottom left corner to complete a valid area
                hatchLineShape.lineTo(new POINT2(x0-spacing/2,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing,y0+height+spacing/2));
                hatchLineShape.lineTo(new POINT2(x0-spacing,y0-spacing/2));
                hatchLineShape.lineTo(new POINT2(x0+width+spacing/2,y0-spacing/2));
            }

            Area shapeArea=new Area(shape.getShape());
            hatchLineArea=new Area(hatchLineShape.getShape());
            //intersect the hatch lines with the original shape area to get the fill
            hatchLineArea.intersect(shapeArea);
            hatchLineShape.setShape(hatchLineArea);
            //return null;
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException(_className, "buildHatchArea",
                    new RendererException("Failed inside buildHatchArea", exc));
        }
        return hatchLineShape;
    }

    protected static Point POINT2ToPoint(POINT2 pt2)
    {
        Point pt=new Point();
        pt.x=(int)pt2.x;
        pt.y=(int)pt2.y;
        return pt;
    }
    protected static POINT2 PointToPOINT2(Point pt)
    {
        POINT2 pt2=new POINT2(pt.x,pt.y);
        return pt2;
    }
    protected static Point2D.Double POINT2ToPoint2D(POINT2 pt2)
    {
        Point2D.Double pt2d=new Point2D.Double(pt2.x,pt2.y);
        return pt2d;
    }
    protected static ArrayList<POINT2> Points2DToPOINT2(ArrayList<Point2D>pts2d)
    {
        ArrayList<POINT2>pts=new ArrayList();
        POINT2 pt=null;
        int n=pts2d.size();
        //for(int j=0;j<pts2d.size();j++)        
        for(int j=0;j<n;j++)        
        {
            pt=new POINT2(pts2d.get(j).getX(),pts2d.get(j).getY());
            pts.add(pt);
        }        
        return pts;
    }
    protected static POINT2 Point2DToPOINT2(Point2D pt2d)
    {
        POINT2 pt2=new POINT2(pt2d.getX(),pt2d.getY());
        return pt2;
    }
    /**
     * @deprecated   
     * @param tg
     * @return 
     */
    protected static boolean addModifiersBeforeClipping(TGLight tg)
    {
        boolean result=false;
        int linetype=tg.get_LineType();
        switch(linetype)
        {
            case TacticalLines.TORPEDO:
            case TacticalLines.OPTICAL:
            case TacticalLines.ELECTRO:
            case TacticalLines.BEARING_EW:
            case TacticalLines.ACOUSTIC:
            case TacticalLines.ACOUSTIC_AMB:
            case TacticalLines.BEARING:
            case TacticalLines.BEARING_J:
            case TacticalLines.BEARING_RDF:
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
            case TacticalLines.HCONVOY:
            case TacticalLines.CONVOY:
            case TacticalLines.MFP:
            case TacticalLines.RFL:
            case TacticalLines.NFL:
            case TacticalLines.CFL:
            case TacticalLines.FSCL:
            case TacticalLines.BCL_REVD:
            case TacticalLines.BCL:
            case TacticalLines.ICL:
            case TacticalLines.IFF_OFF:
            case TacticalLines.IFF_ON:
            case TacticalLines.GENERIC_LINE:
            case TacticalLines.FPF:
            case TacticalLines.LINTGT:
            case TacticalLines.LINTGTS:
            case TacticalLines.MSDZ:
            case TacticalLines.GAP:
            case TacticalLines.IL:
            case TacticalLines.DIRATKAIR:
            case TacticalLines.PDF:
            case TacticalLines.AC:
            case TacticalLines.SAAFR:
            case TacticalLines.LLTR:
            case TacticalLines.SC:
            case TacticalLines.MRR:
            case TacticalLines.SL:
            case TacticalLines.TC:
            case TacticalLines.BOUNDARY:
            case TacticalLines.WDRAWUP:
            case TacticalLines.WITHDRAW:
            case TacticalLines.RETIRE:
            case TacticalLines.FPOL:
            case TacticalLines.RPOL:
            case TacticalLines.RIP:
            case TacticalLines.DELAY:
            case TacticalLines.CATK:
            case TacticalLines.CATKBYFIRE:
            case TacticalLines.SCREEN:
            case TacticalLines.COVER:
            case TacticalLines.GUARD:
            case TacticalLines.FLOT:
            case TacticalLines.LC:
            case TacticalLines.PL:
            case TacticalLines.FEBA:
            case TacticalLines.LL:
            case TacticalLines.EWL:
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
                result = true;
                break;
            default:
                break;
        }
        if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(linetype)==true)
            result=true;
        return result;
    }

    /**
     * @deprecated
     */
    protected static void FilterPoints(TGLight tg)
    {
        try
        {
            int lineType = tg.get_LineType();
            double minSpikeDistance = 0;
            switch (lineType)
            {
                //case TacticalLines.LC:
                case TacticalLines.ATDITCH:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.FLOT:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.FORTL:
                case TacticalLines.STRONG:
                    minSpikeDistance=25;
                    break;
                case TacticalLines.LC:
                case TacticalLines.OBSAREA:
                case TacticalLines.OBSFAREA:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ZONE:
                case TacticalLines.LINE:
                case TacticalLines.ATWALL:
                case TacticalLines.UNSP:
                case TacticalLines.SFENCE:
                case TacticalLines.DFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                    minSpikeDistance=35;
                    break;
                case TacticalLines.UCF:
                case TacticalLines.CF:
                case TacticalLines.CFG:
                case TacticalLines.CFY:
                    minSpikeDistance=60;
                    break;
                case TacticalLines.SF:
                case TacticalLines.USF:
                case TacticalLines.OCCLUDED:
                case TacticalLines.UOF:
                    minSpikeDistance=60;//was 120
                    break;
                case TacticalLines.SFG:
                case TacticalLines.SFY:
                    minSpikeDistance=60;//was 180
                    break;
                case TacticalLines.WFY:
                case TacticalLines.WFG:
                case TacticalLines.OFY:
                    minSpikeDistance=60;//was 120
                    break;
                case TacticalLines.WF:
                case TacticalLines.UWF:
                    minSpikeDistance=40;
                    break;

                case TacticalLines.RIDGE:
                case TacticalLines.ICE_EDGE_RADAR:  //METOCs
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                    minSpikeDistance=35;
                    break;
                default:
                    return;
            }
            int j=0;
            double dist=0;
            ArrayList<POINT2>pts=new ArrayList();
            ArrayList<POINT2>ptsGeo=new ArrayList();
            pts.add(tg.Pixels.get(0));
            ptsGeo.add(tg.LatLongs.get(0));
            POINT2 lastGoodPt=tg.Pixels.get(0);
            POINT2 currentPt=null;
            POINT2 currentPtGeo=null;
            boolean foundGoodPt=false;
            int n=tg.Pixels.size();
            //for(j=1;j<tg.Pixels.size();j++)
            for(j=1;j<n;j++)
            {
                //we can not filter out the original end points
                currentPt=tg.Pixels.get(j);
                currentPtGeo=tg.LatLongs.get(j);
                if(currentPt.style==-1)
                {
                    lastGoodPt=currentPt;
                    pts.add(currentPt);
                    ptsGeo.add(currentPtGeo);
                    foundGoodPt=true;
                    currentPt.style=0;
                    continue;
                }
                dist=lineutility.CalcDistanceDouble(lastGoodPt, currentPt);
                switch(lineType)
                {
                    case TacticalLines.LC:
                        if(dist>minSpikeDistance)
                        {
                            lastGoodPt=currentPt;
                            pts.add(currentPt);
                            ptsGeo.add(currentPtGeo);
                            foundGoodPt=true;
                        }
                        else
                        {   //the last point is no good
                            //replace the last good point with the last point
                            if(j==tg.Pixels.size()-1)
                            {
                                pts.set(pts.size()-1, currentPt);
                                ptsGeo.set(ptsGeo.size()-1, currentPtGeo);
                            }
                        }
                        break;
                    default:
                        if(dist>minSpikeDistance || j==tg.Pixels.size()-1)
                        {
                            lastGoodPt=currentPt;
                            pts.add(currentPt);
                            ptsGeo.add(currentPtGeo);
                            foundGoodPt=true;
                        }
                        break;
                }
            }
            if(foundGoodPt==true)
            {
                tg.Pixels=pts;
                tg.LatLongs=ptsGeo;
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsUtility", "FilterPoints",
                    new RendererException("Failed inside FilterPoints", exc));

        }
    }

    public static ArrayList<POINT2> PixelsToLatLong(ArrayList<POINT2> pts, IPointConversion converter)
    {
        int j=0;
        POINT2 pt=null;
        POINT2 ptGeo=null;
        ArrayList<POINT2> ptsGeo=new ArrayList();
        int n=pts.size();
        //for(j=0;j<pts.size();j++)
        for(j=0;j<n;j++)
        {
            pt=pts.get(j);
            ptGeo=PointPixelsToLatLong(pt,converter);
            ptsGeo.add(ptGeo);
        }
        return ptsGeo;
    }

    protected static ArrayList<POINT2> LatLongToPixels(ArrayList<POINT2> pts, IPointConversion converter)
    {
        int j=0;
        POINT2 pt=null;
        POINT2 ptPixels=null;
        ArrayList<POINT2> ptsPixels=new ArrayList();
        int n=pts.size();
        //for(j=0;j<pts.size();j++)
        for(j=0;j<n;j++)
        {
            pt=pts.get(j);
            ptPixels=PointLatLongToPixels(pt,converter);
            ptsPixels.add(ptPixels);
        }
        return ptsPixels;
    }

    private static POINT2 PointLatLongToPixels(POINT2 ptLatLong,IPointConversion converter)
    {
        POINT2 pt2 = new POINT2();
        try
        {
            Point2D pt2d=POINT2ToPoint2D(ptLatLong);            
            pt2d=converter.GeoToPixels(pt2d);
            pt2=Point2DToPOINT2(pt2d);
            pt2.style=ptLatLong.style;
        } catch (Exception e) {
            armyc2.c5isr.JavaTacticalRenderer.clsUtility.WriteFile("Error in clsUtility.PointLatLongToPixels");
        }
        return pt2;
    }

    protected static void FilterAXADPoints(TGLight tg, IPointConversion converter) {
        try {
            int lineType = tg.get_LineType();
            switch (lineType) {
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.SPT:
                case TacticalLines.MAIN:
                    break;
                default:
                    return;
            }
            int j=0;
            ArrayList<POINT2> pts = new ArrayList();
            ArrayList<POINT2> ptsGeo = new ArrayList();
            POINT2 pt0 = tg.Pixels.get(0);
            POINT2 pt1 = tg.Pixels.get(1);

            Point2D pt=new Point2D.Double(pt1.x,pt1.y);
            Point2D pt1Geo2d=converter.PixelsToGeo(pt);

            POINT2 pt1geo=new POINT2(pt1Geo2d.getX(),pt1Geo2d.getY());
            POINT2 ptj=null,ptjGeo=null;
            POINT2 controlPt=tg.Pixels.get(tg.Pixels.size()-1); //the control point
            POINT2 pt0Relative=lineutility.PointRelativeToLine(pt0, pt1, pt0, controlPt);
            double relativeDist=lineutility.CalcDistanceDouble(pt0Relative, controlPt);
            relativeDist += 5;
            double pt0pt1dist=lineutility.CalcDistanceDouble(pt0, pt1);
            boolean foundGoodPoint=false;
            if(relativeDist>pt0pt1dist)
            {
                //first point is too close, begin rebuilding the arrays
                pts.add(pt0);
                pt=new Point2D.Double(pt0.x,pt0.y);
                pt1Geo2d=converter.PixelsToGeo(pt);

                pt1geo=new POINT2(pt1Geo2d.getX(),pt1Geo2d.getY());
                ptsGeo.add(pt1geo);
                //create a good first point and add it to the array
                pt1=lineutility.ExtendAlongLineDouble(pt0, pt1, relativeDist);
                pts.add(pt1);

                pt=new Point2D.Double(pt1.x,pt1.y);
                pt1Geo2d=converter.PixelsToGeo(pt);
                pt1geo=new POINT2(pt1Geo2d.getX(),pt1Geo2d.getY());
                ptsGeo.add(pt1geo);
            }
            else
            {
                //the first point is good, there is no need to do anything
                foundGoodPoint=true;
                pts=tg.Pixels;
                ptsGeo=tg.LatLongs;
            }

            //do not add mores points to the array until we find at least one good point
            int n=tg.Pixels.size();
            if(foundGoodPoint==false)
            {
                //for(j=2;j<tg.Pixels.size()-1;j++)
                for(j=2;j<n-1;j++)
                {
                    ptj=tg.Pixels.get(j);
                    ptjGeo=tg.LatLongs.get(j);
                    if(foundGoodPoint)
                    {
                       //then stuff the remainder of the arrays with the original points
                        pts.add(ptj);
                        ptsGeo.add(ptjGeo);
                    }
                    else    //no good points yet
                    {
                        //calculate the distance and continue if it is no good
                        pt0pt1dist=lineutility.CalcDistanceDouble(pt0, ptj);
                        if(relativeDist>pt0pt1dist)
                            continue;
                        else
                        {
                           //found a good point
                           pts.add(ptj);
                           ptsGeo.add(ptjGeo);
                           //set the boolean so that it will stuff the array with the rest of the points
                           foundGoodPoint=true;
                        }
                    }
                }
                //finally add the control point to the arrays and set the arrays
                pts.add(controlPt);
                //pt1Geo2d=converter.convertPixelsToLonLat(controlPt.x, controlPt.y);
                pt=new Point2D.Double(controlPt.x, controlPt.y);
                pt1Geo2d=converter.PixelsToGeo(pt);

                pt1geo=new POINT2(pt1Geo2d.getX(),pt1Geo2d.getY());
                ptsGeo.add(pt1geo);
            }   //end if foundGoodPoint is false

            //add all the successive points which are far enough apart
            POINT2 lastGoodPt=pts.get(1);
            POINT2 currentPt=null;
            POINT2 currentPtGeo=null;
            double dist=0;
            tg.Pixels=new ArrayList();
            tg.LatLongs=new ArrayList();
            for(j=0;j<2;j++)
            {
                tg.Pixels.add(pts.get(j));
                tg.LatLongs.add(ptsGeo.get(j));
            }
            n=pts.size();
            //for(j=2;j<pts.size()-1;j++)
            for(j=2;j<n-1;j++)
            {
                currentPt=pts.get(j);
                currentPtGeo=ptsGeo.get(j);
                dist=lineutility.CalcDistanceDouble(currentPt, lastGoodPt);
                if(dist>5)
                {
                    lastGoodPt=currentPt;
                    tg.Pixels.add(currentPt);
                    tg.LatLongs.add(currentPtGeo);
                }
            }
            //add the control point
            tg.Pixels.add(pts.get(pts.size()-1));
            tg.LatLongs.add(ptsGeo.get(ptsGeo.size()-1));
        }
        catch (Exception exc) {
            ErrorLogger.LogException("clsUtility", "FilterAXADPoints",
                    new RendererException("Failed inside FilterAXADPoints", exc));

        }
    }
    /**
     *
     * @param tg
     */
    protected static void RemoveDuplicatePoints(TGLight tg)
    {
        try
        {
            //do not remove autoshape duplicate points
//            if(isAutoshape(tg))
//                return;
            switch (tg.get_LineType()) {
                case TacticalLines.SC:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.TC:
                case TacticalLines.LLTR:
                case TacticalLines.AC:
                case TacticalLines.SAAFR:
                    break;
                default:
                    if(isAutoshape(tg))
                        return;
            }

            //we assume tg.H to have colors if it is comma delimited.
            //only exit if colors are not set
            switch(tg.get_LineType())   //preserve segment data
            {
                case TacticalLines.CATK:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.SPT:
                case TacticalLines.MAIN:
                case TacticalLines.CATKBYFIRE:	//80
                    return;
                case TacticalLines.BOUNDARY:
                case TacticalLines.MSR:
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                    String strH=tg.get_H();
                    if(strH != null && !strH.isEmpty())
                    {
                        String[] strs=strH.split(",");
                        if(strs.length>1)
                            return;
                    }
                    break;
                default:
                    break;
            }
            int linetype=tg.get_LineType();
            if(armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(linetype))
                return;

            POINT2 ptCurrent=null;
            POINT2 ptLast=null;
            Boolean isClosedPolygon= armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(tg.get_LineType());
            int minSize=2;
            if(isClosedPolygon)
                minSize=3;
            for(int j=1;j<tg.Pixels.size();j++)
            {
                ptLast=new POINT2(tg.Pixels.get(j-1));
                ptCurrent=new POINT2(tg.Pixels.get(j));
                //if(ptCurrent.x==ptLast.x && ptCurrent.y==ptLast.y)
                if (Math.abs(ptCurrent.x - ptLast.x)<0.5 && Math.abs(ptCurrent.y - ptLast.y)<0.5)
                {
                    if(tg.Pixels.size()>minSize)
                    {
                        tg.Pixels.remove(j);
                        tg.LatLongs.remove(j);
                        j=1;
                    }
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsUtility", "RemoveDuplicatePoints",
                    new RendererException("Failed inside RemoveDuplicatePoints", exc));

        }
    }
    protected static POINT2 PointPixelsToLatLong(POINT2 ptPixels,IPointConversion converter)
    {
        POINT2 pt2 = new POINT2();
        try
        {
            //Point pt=POINT2ToPoint(ptPixels);
            Point2D pt=new Point2D.Double(ptPixels.x,ptPixels.y);
            Point2D pt2d=converter.PixelsToGeo(pt);
            pt2=Point2DToPOINT2(pt2d);
            pt2.style=ptPixels.style;

        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsUtility" ,"PointPixelsToLatLong",
                    new RendererException("Could not convert point to geo", exc));
        }
        return pt2;
    }

    protected static Rectangle2D getMBR(ArrayList<Point2D> clipBounds)
    {
        Rectangle2D rect=null;
        try
        {
            int j=0;
            Point2D pt=null;
            double xmax=clipBounds.get(0).getX(),xmin=xmax,ymax=clipBounds.get(0).getY(),ymin=ymax;
            int n=clipBounds.size();
            //for(j=0;j<clipBounds.size();j++)
            for(j=0;j<n;j++)
            {
                pt=clipBounds.get(j);
                if(pt.getX()<xmin)
                    xmin=pt.getX();
                if(pt.getX()>xmax)
                    xmax=pt.getX();
                if(pt.getY()<=ymin)
                    ymin=pt.getY();
                if(pt.getY()>ymax)
                    ymax=pt.getY();
            }
            rect=new Rectangle2D.Double(xmin, ymin, xmax-xmin, ymax-ymin);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddBoundaryPointsForLines",
                    new RendererException("Failed inside AddBoundaryPointsForLines", exc));
        }
        return rect;
    }

    static void GetMBR(ArrayList<Shape2> shapes,
                       POINT2 ptUl,
                       POINT2 ptUr,
                       POINT2 ptLr,
                       POINT2 ptLl) {
        try {
            POINT2 firstPoint = shapes.get(0).getPoints().get(0);
            ptUl.x = firstPoint.x;
            ptUl.y = firstPoint.y;
            ptUr.x = firstPoint.x;
            ptUr.y = firstPoint.y;
            ptLl.x = firstPoint.x;
            ptLl.y = firstPoint.y;
            ptLr.x = firstPoint.x;
            ptLr.y = firstPoint.y;
            for (Shape2 shape: shapes) {
                ArrayList<POINT2> points = shape.getPoints();
                for (int j = 0; j < points.size(); j++) {
                    double x = points.get(j).x;
                    double y = points.get(j).y;
                    if (x < ptLl.x) {
                        ptLl.x = x;
                        ptUl.x = x;
                    }
                    if (x > ptLr.x) {
                        ptLr.x = x;
                        ptUr.x = x;
                    }
                    if (y > ptLl.y) {
                        ptLl.y = y;
                        ptLr.y = y;
                    }
                    if (y < ptUl.y) {
                        ptUl.y = y;
                        ptUr.y = y;
                    }
                }
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetMBR",
                    new RendererException("Failed inside GetMBR", exc));
        }
    }
}
