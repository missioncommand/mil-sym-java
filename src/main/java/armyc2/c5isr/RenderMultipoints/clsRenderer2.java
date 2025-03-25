/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.RenderMultipoints;
import armyc2.c5isr.JavaLineArray.arraysupport;
import armyc2.c5isr.JavaLineArray.CELineArray;
import armyc2.c5isr.JavaTacticalRenderer.clsChannelUtility;
import armyc2.c5isr.JavaTacticalRenderer.Modifier2;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.JavaTacticalRenderer.clsUtility;
import armyc2.c5isr.JavaTacticalRenderer.P1;
import armyc2.c5isr.JavaLineArray.ref;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaTacticalRenderer.clsMETOC;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import armyc2.c5isr.JavaLineArray.Shape2;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.JavaLineArray.lineutility;
import java.util.HashMap;


/**
 * Rendering helper class
 * 
 */
public final class clsRenderer2 {
    private static final String _className="clsRenderer2";
    /**
     * MSR and ASR use segment data for segment colors
     * Assumes tg.H has been revised for clipping
     * @param tg
     * @param shapes 
     */
    private static void getMSRShapes(TGLight tg,
            ArrayList<Shape2>shapes)
    {
        try
        {
            int linetype=tg.get_LineType();
            if(linetype != TacticalLines.MSR && linetype != TacticalLines.ASR && linetype != TacticalLines.ROUTE)
                return;
            
            HashMap<Integer, Color> hmap= clsUtility.getMSRSegmentColors(tg);
            Shape2 shape=null;
            
            BasicStroke stroke = clsUtility.getLineStroke(tg.get_LineThickness(),tg.get_LineStyle(),tg.get_lineCap(),BasicStroke.JOIN_ROUND);
            
            int j=0,n=tg.Pixels.size();
            Color color=null;
            Shape2 segShape=null;
            shape=new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
            shape.setLineColor(tg.get_LineColor());
            shape.setStroke(stroke);
            
            //if colors are not set then use one shape
            //assumes colors may be set if string is comma delimited
//            String strH=tg.get_H();
//            if(strH != null && !strH.isEmpty())
//            {               
//                String[] strs=strH.split(",");
//                if(strs.length<2)
//                {
//                    shape.moveTo(tg.Pixels.get(0));
//                    //n=tg.Pixels.size();
//                    //for(j=1;j<tg.Pixels.size();j++)
//                    for(j=1;j<n;j++)
//                    {
//                        shape.lineTo(tg.Pixels.get(j));
//                    }
//                    shapes.add(shape);
//                    return;
//                }
//            }
                        
            //if the hashmap contains the segment then use the color corresponding to the segment
            //in the hashtable to create a one segment shape to add to the shape array.
            //else sdd the segment to the original shape
            Color lastColor=null;   //diagnostic
            double dist=0,dist2=0;
            POINT2 pt0=null,pt1=null;
            POINT2 lastPt=null;
            //for(j=0;j<tg.Pixels.size()-1;j++)
            for(j=0;j<n-1;j++)
            {
                pt0=tg.Pixels.get(j);
                pt1=tg.Pixels.get(j+1);
                if(hmap !=null && hmap.containsKey(j))
                {
                    color=(Color)hmap.get(j);
                    if(color != lastColor)
                    {
                        if(segShape != null)
                            shapes.add(segShape);                    
                        
                        segShape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);                    
                        segShape.setLineColor(color);
                        segShape.set_Style(tg.get_LineStyle());
                        segShape.setStroke(stroke);
                    }
                    segShape.moveTo(pt0);
                    segShape.lineTo(pt1);                    
                    //lastColor=new Color(Integer.toHexString(color.getRGB()));
                    lastColor=color;
               }                                    
                else
                {
                    if(hmap !=null && hmap.containsKey(j+1))
                    {
                        shape.moveTo(pt0);
                        shape.lineTo(pt1);
                        lastPt=new POINT2(pt1);
                    }
                    else if(hmap !=null && hmap.containsKey(j-1))
                    {
                        shape.moveTo(pt0);
                        shape.lineTo(pt1);                        
                        lastPt=new POINT2(pt1);
                    }
                    else if(j==tg.Pixels.size()-2)
                    {
                        shape.moveTo(pt0);
                        shape.lineTo(pt1);                                                
                    }
                    else
                    {
                        if(lastPt==null)
                        {
                            lastPt=new POINT2(pt0);
                            shape.moveTo(lastPt);
                            //shape.lineTo(lastPt);
                        }                        
                        dist=lineutility.CalcDistanceDouble(pt0, pt1);
                        if(dist>10)
                        {
                            //shape.moveTo(pt0);
                            shape.lineTo(pt1);                                                
                            lastPt=new POINT2(pt1);                            
                        }
                        else
                        {
                            dist2=lineutility.CalcDistanceDouble(lastPt, pt1);
                            if(dist2>10)
                            {
                                //shape.moveTo(pt0);
                                shape.lineTo(pt1);                                                
                                lastPt=new POINT2(pt1);                            
                            }                            
                        }
                    }
                    //shapes.add(shape);
                }
            }
            if(segShape != null)
                shapes.add(segShape);                    
            
            shapes.add(shape);
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"getMSRShapes",
                new RendererException("Failed inside getMSRShapes", exc));
        }
    }
    /**
     * 
     * @param tg
     * @param converter client converter
     * @param isTextFlipped
     * @return
     */
    public static ArrayList<Shape2> GetLineArray(TGLight tg,
            IPointConversion converter,
            boolean isTextFlipped,
            Object clipBounds)
    {
        ArrayList<Shape2> shapes=new ArrayList();
        try
        {
            if(tg.Pixels==null || tg.Pixels.isEmpty())
                return null;            
            double x=0;
            double y=0;
            double width=0;
            double height=0;
            Rectangle2D clipBounds2=null;

            Rectangle2D clipRect=null;
            ArrayList<Point2D>clipArray=null;
            if(clipBounds != null)
            {
                if(clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class))
                {            
                    //clipRect=(Rectangle2D.Double)clipBounds;
                    clipRect=(Rectangle2D)clipBounds;
                    x=clipRect.getMinX()-50;
                    y=clipRect.getMinY()-50;
                    width=clipRect.getWidth()+100;
                    height=clipRect.getHeight()+100;
                    clipBounds2=new Rectangle2D.Double(x,y,width,height);
                }
                else if(clipBounds.getClass().isAssignableFrom(Rectangle.class))
                {
                    Rectangle rectx=(Rectangle)clipBounds;
                    clipRect=new Rectangle2D.Double(rectx.x,rectx.y,rectx.width,rectx.height);
                    x=clipRect.getMinX()-50;
                    y=clipRect.getMinY()-50;
                    width=clipRect.getWidth()+100;
                    height=clipRect.getHeight()+100;
                    clipBounds2=new Rectangle2D.Double(x,y,width,height);                    
                }
                else if(clipBounds.getClass().isAssignableFrom(ArrayList.class))
                {
                    clipArray=(ArrayList<Point2D>)clipBounds;
                    clipBounds2= armyc2.c5isr.RenderMultipoints.clsUtility.getMBR(clipArray);
                }
            }

            int lineType = tg.get_LineType();
            // In some cases render shapes as another line type but return to input line type before adding modifiers
            final int inputLineType = lineType;

            // Render complex arrows as simple arrow when very small
            double DPIScaleFactor = RendererSettings.getInstance().getDeviceDPI() / 96.0;
            if ((lineType == TacticalLines.FOLLA || lineType == TacticalLines.FOLSP || lineType == TacticalLines.CONVOY)
                    && lineutility.CalcDistanceDouble(tg.Pixels.get(0), tg.Pixels.get(1)) <= 30 * DPIScaleFactor) {
                lineType = TacticalLines.DIRATKSPT;
                tg.set_LineType(lineType);
            }

            int minPoints2;
            MSInfo msInfo = MSLookup.getInstance().getMSLInfo(tg.get_SymbolId());
            if (msInfo != null) {
                minPoints2 = msInfo.getMinPointCount();
            } else {
                minPoints2 = -1;
            }
            boolean bolResult = clsUtility.IsChange1Area(lineType);
            int bolMeTOC= clsMETOC.IsWeather(tg.get_SymbolId());
            
            ArrayList<POINT2>pts=new ArrayList();
            //uncomment one line for usas1314
            Boolean usas1314=true;
            int j=0,n=tg.Pixels.size();
            if (tg.get_LineType() == TacticalLines.SINGLEC) {
                //reverse single concertina
                pts=(ArrayList<POINT2>)tg.Pixels.clone();
                //for(j=0;j<tg.Pixels.size();j++)
                for(j=0;j<n;j++)
                    tg.Pixels.set(j, pts.get(pts.size()-j-1));
            }

            //set CELineArray.shapes properties
            BufferedImage bi=new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d=bi.createGraphics();
            g2d.setFont(tg.get_Font());

            if(tg.Pixels.size()<minPoints2)
            {
                bolResult=false;
            }

            if (bolResult)
            {

                tg.Pixels.clear();
                bolResult = clsUtilityCPOF.Change1TacticalAreas(tg, lineType, converter, shapes);
            }
            else if(bolMeTOC>0)
            {
                if(tg.Pixels.size()<2)
                    return null;

                try
                {
                    clsMETOC.GetMeTOCShape(tg, shapes);
                }
                catch(Exception exc)
                {
                    ErrorLogger.LogException(_className ,"GetLineArray",
                        new RendererException("Failed inside GetLineArray", exc));
                }
            }
            else
            {
                //this will help with click-drag mode
                if(tg.Pixels.size()<2)
                        return null;
                
                if (CELineArray.CIsChannel(lineType) == 0)
                {
                    if(lineType==TacticalLines.ASR || lineType==TacticalLines.MSR || lineType==TacticalLines.ROUTE)
                    {
                        getMSRShapes(tg,shapes);
                    }
                    else
                    {
                        tg.Pixels=arraysupport.GetLineArray2(tg, tg.Pixels,shapes, clipBounds2, converter);
                    }
                }
                else //channel type
                {
                    clsChannelUtility.DrawChannel(tg.Pixels, lineType, tg,shapes, null, clipBounds2, converter);
                }
            }
            //set CELineArray.shapes properties
            if(bolMeTOC<=0)
            {
                if(lineType!=TacticalLines.ASR && lineType!=TacticalLines.MSR && lineType!=TacticalLines.ROUTE)
                    clsUtility.SetShapeProperties(tg,shapes,bi);
            }

            if (lineType != inputLineType) {
                // lineType was switched temporarily while rendering shapes
                tg.set_LineType(inputLineType);
            }

            //at this point tg.Pixels has the points from CELineArray
            //the following line adds modifiers for those sybmols which require
            //the calculated points to use for the modifiers.
            //currentlly only BLOCK and CONTAIN use tg.Pixels for computing
            //the modifiers after the call to GetLineArray
            //Modifier2.AddModifiers2(tg);//flipped only for 3d for change 1 symbols
            Modifier2.AddModifiers2(tg, converter);

            //boundary has shapes for line break
            Modifier2.GetIntegralTextShapes(tg, g2d, shapes);

            bi.flush();
            g2d.dispose();
            bi=null;
            g2d=null;
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"GetLineArray",
                new RendererException("Failed inside GetLineArray", exc));
        }
        return shapes;
    }
    /**
     * Isolate and others require special handling for the fill shapes.
     * @param tg 
     * @param shapes the existing shapes which characterize the graphic
     */
    static protected void getAutoshapeFillShape(TGLight tg, ArrayList<Shape2>shapes)
    {
        try
        {            
            if(shapes==null || shapes.size()==0)
                return;
            if(tg.Pixels==null || tg.Pixels.size()==0)
                return;
            if(tg.get_FillColor()==null)
                return;
            
            int linetype=tg.get_LineType();
            int j=0;
            Shape2 shape=new Shape2(Shape2.SHAPE_TYPE_FILL);
            shape.setFillColor(tg.get_FillColor());
            shape.setLineColor(null);
            int t=shapes.size();
            int n=tg.Pixels.size();
            switch(linetype)
            {
                case TacticalLines.RETAIN:
                    if(shapes!=null && !shapes.isEmpty())
                        //for(j=0;j<shapes.size();j++)
                        for(j=0;j<t;j++)
                            shapes.get(j).setFillColor(null);
                    
                    shape.moveTo(tg.Pixels.get(0));
                    for(j=1;j<26;j++)                    
                        shape.lineTo(tg.Pixels.get(j));
                    
                    shape.lineTo(tg.Pixels.get(0));
                    shapes.add(0,shape);
                    break;
                case TacticalLines.SECURE:
                case TacticalLines.OCCUPY:
                    if(shapes!=null && !shapes.isEmpty())
                        //for(j=0;j<shapes.size();j++)
                        for(j=0;j<t;j++)
                            shapes.get(j).setFillColor(null);
                    
                    shape.moveTo(tg.Pixels.get(0));
                    //for(j=1;j<tg.Pixels.size()-3;j++)                    
                    for(j=1;j<n-3;j++)                    
                        shape.lineTo(tg.Pixels.get(j));
                    
                    shape.lineTo(tg.Pixels.get(0));
                    shapes.add(0,shape);
                    break;
                case TacticalLines.CONVOY:
                case TacticalLines.HCONVOY:
                    if(shapes!=null && !shapes.isEmpty())
                        //for(j=0;j<shapes.size();j++)
                        for(j=0;j<t;j++)
                            shapes.get(j).setFillColor(null);
                    
                    shape.moveTo(tg.Pixels.get(0));
                    //for(j=1;j<tg.Pixels.size();j++)                    
                    for(j=1;j<n;j++)                    
                        shape.lineTo(tg.Pixels.get(j));
                    
                    shape.lineTo(tg.Pixels.get(0));
                    shapes.add(0,shape);
                    break;
                case TacticalLines.CORDONSEARCH:
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.ISOLATE:
                    //set the fillcolor to null for the existing shapes
                    //we are going to create a new fill shape
                    if(shapes!=null && !shapes.isEmpty())
                        //for(j=0;j<shapes.size();j++)
                        for(j=0;j<t;j++)
                            shapes.get(j).setFillColor(null);
                    
                    shape.moveTo(tg.Pixels.get(0));
                    for(j=26;j<47;j++)                    
                        shape.lineTo(tg.Pixels.get(j));
                    
                    shape.lineTo(tg.Pixels.get(23));
                    shape.lineTo(tg.Pixels.get(24));
                    shape.lineTo(tg.Pixels.get(25));
                    shape.lineTo(tg.Pixels.get(0));
                    shapes.add(0,shape);
                    break;
                default:
                    return;
            }
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"getAutoshapeFillShape",
                new RendererException("Failed inside getAutoshapeFillShape", exc));
        }
    }
}
