/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.JavaTacticalRenderer;

import armyc2.c5isr.JavaLineArray.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import armyc2.c5isr.JavaLineArray.Shape2;
import java.io.*;

import armyc2.c5isr.RenderMultipoints.clsRenderer;
import armyc2.c5isr.renderer.utilities.*;

/**
 * A general utility class for the tactical renderer
 * 
 */
public final class clsUtility {
    private static final String _className = "clsUtility";
    protected static Point2D POINT2ToPoint2D(POINT2 pt2) {
        if (pt2 == null) {
            return null;
        }

        double x = pt2.x;
        double y = pt2.y;
        Point2D pt = new Point2D.Double(x, y);
        return pt;
    }
    /**
     * returns true if the line segments are all outside the bounds
     * @param tg the tactical graphic
     * @param clipBounds the pixels based clip bounds
     * @return 
     */
    public static boolean linesOutsideClipBounds(TGLight tg,
            Rectangle2D clipBounds)
    {
        try
        {
            boolean isAutoshape=isAutoshape(tg);
            if(isAutoshape)
                return false;

            double xmin=clipBounds.getMinX();
            double xmax=clipBounds.getMaxX();
            double ymin=clipBounds.getMinY();
            double ymax=clipBounds.getMaxY();
            int j=0;
            POINT2 pt0=null,pt1=null;
            Line2D boundsEdge=null,ptsLine=null;
            int n=tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size()-1;j++)
            for(j=0;j<n-1;j++)
            {
                pt0=tg.Pixels.get(j);
                pt1=tg.Pixels.get(j+1);
                
                //if either point is inside the bounds return false
                if(clipBounds.contains(pt0.x, pt0.y))
                    return false;
                if(clipBounds.contains(pt1.x, pt1.y))
                    return false;
                
                ptsLine=new Line2D.Double(pt0.x,pt0.y,pt1.x,pt1.y);
                
                //if the pt0-pt1 line intersects any clip bounds edge then return false
                boundsEdge=new Line2D.Double(xmin,ymin,xmax,ymin);
                if(ptsLine.intersectsLine(boundsEdge))
                    return false;                
                
                boundsEdge=new Line2D.Double(xmax,ymin,xmax,ymax);
                if(ptsLine.intersectsLine(boundsEdge))
                    return false;                
                
                boundsEdge=new Line2D.Double(xmax,ymax,xmin,ymax);
                if(ptsLine.intersectsLine(boundsEdge))
                    return false;                
                
                boundsEdge=new Line2D.Double(xmin,ymax,xmin,ymin);
                if(ptsLine.intersectsLine(boundsEdge))
                    return false;                
            }
        }
        catch (Exception exc) 
        {
            ErrorLogger.LogException(_className ,"linesOutsideClipBounds",
                    new RendererException("Failed inside linesOutsideClipBounds", exc));
        }    
        return true;
    }
    /**
     * Returns the minimum client points needed for the symbol
     * @param lineType line type
     * @return minimum number of clients required to render the line
     * @deprecated use MSInfo.getMinPointCount()
     */
    public static int GetMinPoints(int lineType) {
        int result = -1;
        switch (lineType) {
            case TacticalLines.RECTANGULAR:
            case TacticalLines.CUED_ACQUISITION:
            case TacticalLines.CIRCULAR:
            case TacticalLines.BDZ:
            case TacticalLines.FSA_CIRCULAR:
            case TacticalLines.NOTACK:
            case TacticalLines.FFA_CIRCULAR:
            case TacticalLines.NFA_CIRCULAR:
            case TacticalLines.RFA_CIRCULAR:
            case TacticalLines.ACA_CIRCULAR:
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
            case TacticalLines.LAUNCH_AREA:
            case TacticalLines.DEFENDED_AREA_CIRCULAR:
            case TacticalLines.SHIP_AOI_CIRCULAR:
            case TacticalLines.RANGE_FAN:
            case TacticalLines.RANGE_FAN_SECTOR:
            case TacticalLines.RADAR_SEARCH:
                result = 1;
                break;
            case TacticalLines.PAA_RECTANGULAR:
            case TacticalLines.FSA_RECTANGULAR:
            case TacticalLines.SHIP_AOI_RECTANGULAR:
            case TacticalLines.DEFENDED_AREA_RECTANGULAR:
            case TacticalLines.FFA_RECTANGULAR:
            case TacticalLines.RFA_RECTANGULAR:
            case TacticalLines.NFA_RECTANGULAR:
            case TacticalLines.ACA_RECTANGULAR:
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
                result = 2; //was 3
                break;
            case TacticalLines.SPTBYFIRE:
            case TacticalLines.RIP:
            case TacticalLines.GAP:
            case TacticalLines.ASLTXING:
            case TacticalLines.MSDZ:
                result = 4;
                break;
            case TacticalLines.BYPASS:
            case TacticalLines.BLOCK:
            case TacticalLines.BREACH:
            case TacticalLines.CANALIZE:
            case TacticalLines.CLEAR:
            case TacticalLines.CONTAIN:
            case TacticalLines.DELAY:
            case TacticalLines.DISRUPT:
            case TacticalLines.PENETRATE:
            case TacticalLines.RETIRE:
            case TacticalLines.FPOL:
            case TacticalLines.RPOL:
            case TacticalLines.SCREEN:
            case TacticalLines.COVER:
            case TacticalLines.GUARD:
            case TacticalLines.SEIZE:
            case TacticalLines.WITHDRAW:
            case TacticalLines.WDRAWUP:
            //non task autoshapes
            case TacticalLines.SARA:
            case TacticalLines.PDF:
            case TacticalLines.IL:
            case TacticalLines.ATKBYFIRE:
            case TacticalLines.AMBUSH:
            case TacticalLines.RELEASE:
            case TacticalLines.HOL:
            case TacticalLines.BHL:
            case TacticalLines.MNFLDBLK:
            case TacticalLines.MNFLDDIS:
            case TacticalLines.TURN:
            case TacticalLines.PLANNED:
            case TacticalLines.ESR1:
            case TacticalLines.ESR2:
            case TacticalLines.ROADBLK:
            case TacticalLines.TRIP:
            case TacticalLines.EASY:
            case TacticalLines.BYDIF:
            case TacticalLines.BYIMP:
            case TacticalLines.FORDSITE:
            case TacticalLines.FORDIF:
            //METOCs
            case TacticalLines.IFR:
            case TacticalLines.MVFR:
            case TacticalLines.TURBULENCE:
            case TacticalLines.ICING:
            case TacticalLines.NON_CONVECTIVE:
            case TacticalLines.CONVECTIVE:
            case TacticalLines.FROZEN:
            case TacticalLines.THUNDERSTORMS:
            case TacticalLines.FOG:
            case TacticalLines.SAND:
            case TacticalLines.FREEFORM:
            case TacticalLines.DEPTH_AREA:
            case TacticalLines.ISLAND:
            case TacticalLines.BEACH:
            case TacticalLines.WATER:
            case TacticalLines.FISH_TRAPS:
            case TacticalLines.SWEPT_AREA:
            case TacticalLines.OIL_RIG_FIELD:
            case TacticalLines.FOUL_GROUND:
            case TacticalLines.KELP:
            case TacticalLines.BEACH_SLOPE_MODERATE:
            case TacticalLines.BEACH_SLOPE_STEEP:
            case TacticalLines.ANCHORAGE_AREA:
            case TacticalLines.TRAINING_AREA:
            case TacticalLines.FORESHORE_AREA:
            case TacticalLines.DRYDOCK:
            case TacticalLines.LOADING_FACILITY_AREA:
            case TacticalLines.PERCHES:
            case TacticalLines.UNDERWATER_HAZARD:
            case TacticalLines.DISCOLORED_WATER:
            case TacticalLines.BEACH_SLOPE_FLAT:
            case TacticalLines.BEACH_SLOPE_GENTLE:
            case TacticalLines.MARITIME_AREA:
            case TacticalLines.OPERATOR_DEFINED:
            case TacticalLines.SUBMERGED_CRIB:
            case TacticalLines.VDR_LEVEL_12:
            case TacticalLines.VDR_LEVEL_23:
            case TacticalLines.VDR_LEVEL_34:
            case TacticalLines.VDR_LEVEL_45:
            case TacticalLines.VDR_LEVEL_56:
            case TacticalLines.VDR_LEVEL_67:
            case TacticalLines.VDR_LEVEL_78:
            case TacticalLines.VDR_LEVEL_89:
            case TacticalLines.VDR_LEVEL_910:
            case TacticalLines.SOLID_ROCK:
            case TacticalLines.CLAY:
            case TacticalLines.VERY_COARSE_SAND:
            case TacticalLines.COARSE_SAND:
            case TacticalLines.MEDIUM_SAND:
            case TacticalLines.FINE_SAND:
            case TacticalLines.VERY_FINE_SAND:
            case TacticalLines.VERY_FINE_SILT:
            case TacticalLines.FINE_SILT:
            case TacticalLines.MEDIUM_SILT:
            case TacticalLines.COARSE_SILT:
            case TacticalLines.BOULDERS:
            case TacticalLines.OYSTER_SHELLS:
            case TacticalLines.PEBBLES:
            case TacticalLines.SAND_AND_SHELLS:
            case TacticalLines.BOTTOM_SEDIMENTS_LAND:
            case TacticalLines.BOTTOM_SEDIMENTS_NO_DATA:
            case TacticalLines.BOTTOM_ROUGHNESS_SMOOTH:
            case TacticalLines.BOTTOM_ROUGHNESS_MODERATE:
            case TacticalLines.BOTTOM_ROUGHNESS_ROUGH:
            case TacticalLines.CLUTTER_LOW:
            case TacticalLines.CLUTTER_MEDIUM:
            case TacticalLines.CLUTTER_HIGH:
            case TacticalLines.IMPACT_BURIAL_0:
            case TacticalLines.IMPACT_BURIAL_10:
            case TacticalLines.IMPACT_BURIAL_20:
            case TacticalLines.IMPACT_BURIAL_75:
            case TacticalLines.IMPACT_BURIAL_100:
            case TacticalLines.BOTTOM_CATEGORY_A:
            case TacticalLines.BOTTOM_CATEGORY_B:
            case TacticalLines.BOTTOM_CATEGORY_C:
            case TacticalLines.BOTTOM_TYPE_A1:
            case TacticalLines.BOTTOM_TYPE_A2:
            case TacticalLines.BOTTOM_TYPE_A3:
            case TacticalLines.BOTTOM_TYPE_B1:
            case TacticalLines.BOTTOM_TYPE_B2:
            case TacticalLines.BOTTOM_TYPE_B3:
            case TacticalLines.BOTTOM_TYPE_C1:
            case TacticalLines.BOTTOM_TYPE_C2:
            case TacticalLines.BOTTOM_TYPE_C3:
                result = 3;
                break;
            case TacticalLines.MRR:
            case TacticalLines.SL:
            case TacticalLines.TC:
            case TacticalLines.SC:
            case TacticalLines.LLTR:
            case TacticalLines.DIRATKAIR:
            case TacticalLines.ABATIS:
            case TacticalLines.CLUSTER:
            case TacticalLines.MNFLDFIX:
            case TacticalLines.FERRY:
            case TacticalLines.MFLANE:
            case TacticalLines.RAFT:
            case TacticalLines.FOXHOLE:
            case TacticalLines.LINTGT:
            case TacticalLines.LINTGTS:
            case TacticalLines.FPF:
            case TacticalLines.CONVOY:
            case TacticalLines.HCONVOY:
                result = 2;
                break;
            default:
                result = 2;
                break;
        }
        if (isClosedPolygon(lineType)) {
            result = 3;
        }
        //add code for change 1 areas
        return result;
    }
    /**
     * @param linetype line type
     * @return true if the line is a closed area
     */
    public static boolean isClosedPolygon(int linetype) {
        boolean result = false;
        switch (linetype) {
            case TacticalLines.AT:
            case TacticalLines.DEPICT:
            case TacticalLines.DZ:
            case TacticalLines.MINED:
            case TacticalLines.FENCED:
            case TacticalLines.UXO:
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
            case TacticalLines.PNO:
            case TacticalLines.BATTLE:
            case TacticalLines.EA:
            case TacticalLines.EZ:
            case TacticalLines.LZ:
            case TacticalLines.PZ:
            case TacticalLines.GENERAL:
            case TacticalLines.JTAA:
            case TacticalLines.SAA:
            case TacticalLines.SGAA:
            case TacticalLines.ASSAULT:
            case TacticalLines.ATKPOS:
            case TacticalLines.OBJ:
            case TacticalLines.AO:
            case TacticalLines.AIRHEAD:
            case TacticalLines.NAI:
            case TacticalLines.TAI:
            case TacticalLines.BASE_CAMP_REVD:
            case TacticalLines.BASE_CAMP:
            case TacticalLines.GUERILLA_BASE_REVD:
            case TacticalLines.GUERILLA_BASE:
            case TacticalLines.GENERIC_AREA:
            case TacticalLines.OBSFAREA:
            case TacticalLines.OBSAREA:
            case TacticalLines.ZONE:
            case TacticalLines.STRONG:
            case TacticalLines.DRCL:
            case TacticalLines.FSA:
            case TacticalLines.ACA:
            case TacticalLines.ASSY:
            case TacticalLines.BSA:
            case TacticalLines.NFA:
            case TacticalLines.RFA:
            case TacticalLines.FARP:
            case TacticalLines.AIRFIELD:
            case TacticalLines.LAA:
            case TacticalLines.BOMB:
            case TacticalLines.FFA:
            case TacticalLines.SMOKE:
            case TacticalLines.PAA:
            case TacticalLines.ENCIRCLE:
            case TacticalLines.DHA_REVD:
            case TacticalLines.DHA:
            case TacticalLines.EPW:
            case TacticalLines.RHA:
            case TacticalLines.DSA:
            case TacticalLines.CSA:
            case TacticalLines.RSA:
            case TacticalLines.FORT_REVD:
            case TacticalLines.FORT:
            case TacticalLines.PEN:
            case TacticalLines.BIO:
            case TacticalLines.NUC:
            case TacticalLines.RAD:
            case TacticalLines.CHEM:
            case TacticalLines.SERIES:
            case TacticalLines.ATI:
            case TacticalLines.TBA:
            case TacticalLines.TVAR:
            case TacticalLines.CFFZ:
            case TacticalLines.CENSOR:
            case TacticalLines.SENSOR:
            case TacticalLines.ZOR:
            case TacticalLines.DA:
            case TacticalLines.CFZ:
            case TacticalLines.KILLBOXBLUE:
            case TacticalLines.KILLBOXPURPLE:
            //METOCs
            case TacticalLines.IFR:
            case TacticalLines.MVFR:
            case TacticalLines.TURBULENCE:
            case TacticalLines.ICING:
            case TacticalLines.NON_CONVECTIVE:
            case TacticalLines.CONVECTIVE:
            case TacticalLines.FROZEN:
            case TacticalLines.THUNDERSTORMS:
            case TacticalLines.FOG:
            case TacticalLines.SAND:
            case TacticalLines.FREEFORM:
            case TacticalLines.DEPTH_AREA:
            case TacticalLines.ISLAND:
            case TacticalLines.BEACH:
            case TacticalLines.WATER:
            case TacticalLines.FISH_TRAPS:
            case TacticalLines.SWEPT_AREA:
            case TacticalLines.OIL_RIG_FIELD:
            case TacticalLines.FOUL_GROUND:
            case TacticalLines.KELP:
            case TacticalLines.BEACH_SLOPE_MODERATE:
            case TacticalLines.BEACH_SLOPE_STEEP:
            case TacticalLines.ANCHORAGE_AREA:
            case TacticalLines.TRAINING_AREA:
            case TacticalLines.FORESHORE_AREA:
            case TacticalLines.DRYDOCK:
            case TacticalLines.LOADING_FACILITY_AREA:
            case TacticalLines.PERCHES:
            case TacticalLines.UNDERWATER_HAZARD:
            case TacticalLines.DISCOLORED_WATER:
            case TacticalLines.BEACH_SLOPE_FLAT:
            case TacticalLines.BEACH_SLOPE_GENTLE:
            case TacticalLines.MARITIME_AREA:
            case TacticalLines.OPERATOR_DEFINED:
            case TacticalLines.SUBMERGED_CRIB:
            case TacticalLines.VDR_LEVEL_12:
            case TacticalLines.VDR_LEVEL_23:
            case TacticalLines.VDR_LEVEL_34:
            case TacticalLines.VDR_LEVEL_45:
            case TacticalLines.VDR_LEVEL_56:
            case TacticalLines.VDR_LEVEL_67:
            case TacticalLines.VDR_LEVEL_78:
            case TacticalLines.VDR_LEVEL_89:
            case TacticalLines.VDR_LEVEL_910:
            case TacticalLines.SOLID_ROCK:
            case TacticalLines.CLAY:
            case TacticalLines.VERY_COARSE_SAND:
            case TacticalLines.COARSE_SAND:
            case TacticalLines.MEDIUM_SAND:
            case TacticalLines.FINE_SAND:
            case TacticalLines.VERY_FINE_SAND:
            case TacticalLines.VERY_FINE_SILT:
            case TacticalLines.FINE_SILT:
            case TacticalLines.MEDIUM_SILT:
            case TacticalLines.COARSE_SILT:
            case TacticalLines.BOULDERS:
            case TacticalLines.OYSTER_SHELLS:
            case TacticalLines.PEBBLES:
            case TacticalLines.SAND_AND_SHELLS:
            case TacticalLines.BOTTOM_SEDIMENTS_LAND:
            case TacticalLines.BOTTOM_SEDIMENTS_NO_DATA:
            case TacticalLines.BOTTOM_ROUGHNESS_SMOOTH:
            case TacticalLines.BOTTOM_ROUGHNESS_MODERATE:
            case TacticalLines.BOTTOM_ROUGHNESS_ROUGH:
            case TacticalLines.CLUTTER_LOW:
            case TacticalLines.CLUTTER_MEDIUM:
            case TacticalLines.CLUTTER_HIGH:
            case TacticalLines.IMPACT_BURIAL_0:
            case TacticalLines.IMPACT_BURIAL_10:
            case TacticalLines.IMPACT_BURIAL_20:
            case TacticalLines.IMPACT_BURIAL_75:
            case TacticalLines.IMPACT_BURIAL_100:
            case TacticalLines.BOTTOM_CATEGORY_A:
            case TacticalLines.BOTTOM_CATEGORY_B:
            case TacticalLines.BOTTOM_CATEGORY_C:
            case TacticalLines.BOTTOM_TYPE_A1:
            case TacticalLines.BOTTOM_TYPE_A2:
            case TacticalLines.BOTTOM_TYPE_A3:
            case TacticalLines.BOTTOM_TYPE_B1:
            case TacticalLines.BOTTOM_TYPE_B2:
            case TacticalLines.BOTTOM_TYPE_B3:
            case TacticalLines.BOTTOM_TYPE_C1:
            case TacticalLines.BOTTOM_TYPE_C2:
            case TacticalLines.BOTTOM_TYPE_C3:
            case TacticalLines.TGMF:
                result = true;
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * Closes the polygon for areas
     * @param Pixels the client points
     */
    public static void ClosePolygon(ArrayList<POINT2> Pixels) {
        try {
            POINT2 pt0 = Pixels.get(0);
            POINT2 pt1 = Pixels.get(Pixels.size() - 1);
            if (pt0.x != pt1.x || pt0.y != pt1.y) {
                Pixels.add(new POINT2(pt0.x, pt0.y));
            }
        } catch (Exception exc) {
               ErrorLogger.LogException(_className ,"ClosePolygon",
                    new RendererException("Failed inside ClosePolygon", exc));
        }
    }
    /**
     * for change 1 symbol the W/w1 modifiers run too close to the symbol outline
     * so it shifts the line along the line away from the edge
     * @param p1
     * @param p2
     * @param shift
     */
    protected static void shiftModifiersLeft(POINT2 p1, POINT2 p2, double shift)
    {
        try
        {
            POINT2 pt1=new POINT2(p1);
            POINT2 pt2=new POINT2(p2);
            double dist=lineutility.CalcDistanceDouble(pt1, pt2);
            if(pt1.x<pt2.x || (pt1.x==pt2.x && pt1.y<pt2.y))
            {
                pt1=lineutility.ExtendAlongLineDouble(pt2, pt1, dist+shift);
                pt2=lineutility.ExtendAlongLineDouble(pt1, pt2, dist-shift);
            }
            else
            {
                pt1=lineutility.ExtendAlongLineDouble(pt2, pt1, dist-shift);
                pt2=lineutility.ExtendAlongLineDouble(pt1, pt2, dist+shift);
            }
            p1.x=pt1.x;
            p1.y=pt1.y;
            p2.x=pt2.x;
            p2.y=pt2.y;
        }
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"shiftModifiersLeft",
                    new RendererException("Failed inside shiftModifiersLeft", exc));
        }
    }
    /**
     * Overrides shape properties for symbols based on Mil-Std-2525
     * @param tg
     * @param shape
     */
    protected static void ResolveModifierShape(TGLight tg, Shape2 shape) {
        try {
            //shape style was set by CELineArray and takes precedence
            //whenever it is set
            int shapeStyle = shape.get_Style();
            int lineStyle = tg.get_LineStyle();
            int lineType = tg.get_LineType();
            boolean hasFill=LinesWithFill(lineType);
            int bolMETOC=clsMETOC.IsWeather(tg.get_SymbolId());
            if(bolMETOC>0)
                return;
            int fillStyle=0;
            //for some of these the style must be dashed
            switch (tg.get_LineType()) {
                case TacticalLines.NFA:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.BIO:
                case TacticalLines.NUC:
                case TacticalLines.CHEM:
                case TacticalLines.RAD:
                case TacticalLines.WFZ:
                //case TacticalLines.OBSAREA:
                    fillStyle=3;
                    if(tg.get_UseHatchFill())
                        fillStyle=0;
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(tg.get_LineStyle());
                        shape.setLineColor(tg.get_LineColor());
                        shape.set_Fillstyle(fillStyle /*GraphicProperties.FILL_TYPE_RIGHT_SLANTS*/);//was 3
                        shape.setFillColor(tg.get_FillColor());
                    }
                    break;
                case TacticalLines.OBSAREA:
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(tg.get_LineStyle());
                        shape.setLineColor(tg.get_LineColor());
                        shape.set_Fillstyle(0 /*GraphicProperties.FILL_TYPE_RIGHT_SLANTS*/);
                        shape.setFillColor(tg.get_FillColor());
                    }
                    break;
                case TacticalLines.LAA:
                    fillStyle=2;
                    if(tg.get_UseHatchFill())
                        fillStyle=0;
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(tg.get_LineStyle());
                        shape.setLineColor(tg.get_LineColor());
                        shape.set_Fillstyle(fillStyle /*GraphicProperties.FILL_TYPE_LEFT_SLANTS*/);//was 2
                        shape.setFillColor(tg.get_FillColor());
                    }
                    break;
                case TacticalLines.DIRATKAIR:
                case TacticalLines.ATDITCHC:
                case TacticalLines.ATDITCHM:
                case TacticalLines.SARA:
                case TacticalLines.FOLSP:
                case TacticalLines.FERRY:
                case TacticalLines.MNFLDFIX:
                case TacticalLines.TURN:
                case TacticalLines.MNFLDDIS:
                case TacticalLines.EASY:
                case TacticalLines.BYDIF:
                case TacticalLines.BYIMP:
                    tg.set_lineCap(BasicStroke.CAP_BUTT);
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_FILL) {
                        shape.set_Fillstyle(1 /*GraphicProperties.FILL_TYPE_SOLID*/);
                        shape.setFillColor(tg.get_LineColor());
                    }
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(tg.get_LineStyle());
                        shape.setLineColor(tg.get_LineColor());
                    }
                    break;
                case TacticalLines.CLUSTER:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.PLD:
                case TacticalLines.PLANNED:
                case TacticalLines.CFL:
                case TacticalLines.FORDSITE:
                case TacticalLines.ACOUSTIC_AMB:
                    //any shape for these symbols is dashed
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(1 /*GraphicProperties.LINE_TYPE_DASHED*/);
                        shape.setLineColor(tg.get_LineColor());
                    }
                    break;
                case TacticalLines.PNO: //always dashed
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.set_Style(1 /*GraphicProperties.LINE_TYPE_DASHED*/);
                        shape.setLineColor(tg.get_LineColor());
                        shape.setFillColor(tg.get_FillColor());
                        shape.set_Fillstyle(tg.get_FillStyle());
                    }
                    break;
                case TacticalLines.FOLLA:
                case TacticalLines.ESR1:
                case TacticalLines.FORDIF:
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        shape.setLineColor(tg.get_LineColor());
                        if (shapeStyle != lineStyle) {
                            if (shapeStyle != 1 /*GraphicProperties.LINE_TYPE_DASHED*/) {
                                shape.set_Style(lineStyle);
                            }
                        }
                    }
                    break;
                default:
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_FILL) {
                        shape.set_Fillstyle(tg.get_FillStyle());
                        shape.setFillColor(tg.get_FillColor());
                    }
                    if (shape.getShapeType() == Shape2.SHAPE_TYPE_POLYLINE) {
                        if (lineType != TacticalLines.LC) {
                            shape.setLineColor(tg.get_LineColor());
                        } else {
                            SetLCColor(tg, shape);
                        }
                        shape.set_Style(lineStyle);
                        if (hasFill || clsUtility.isClosedPolygon(lineType) || clsUtility.IsChange1Area(lineType))
                        {
                            switch(lineType)
                            {
                                case TacticalLines.RANGE_FAN:
                                case TacticalLines.RANGE_FAN_SECTOR:
                                case TacticalLines.RADAR_SEARCH:
                                    shape.setFillColor(null);
                                    break;
                                default:
                                    shape.set_Fillstyle(tg.get_FillStyle());
                                    shape.setFillColor(tg.get_FillColor());
                                    break;
                            }
                        }
                    }
                    break;
            }

        } catch (Exception exc) {
               ErrorLogger.LogException(_className ,"ResolveModifierShape",
                    new RendererException("Failed inside ResolveModifierShape", exc));
        }
    }
    public static Color GetOpaqueColor(Color color)
    {
        int r=color.getRed();
        int g=color.getGreen();
        int b=color.getBlue();
        return new Color(r,g,b);
    }
    /**
     * These lines allow fill
     * @param linetype
     * @return
     */
    public static boolean LinesWithFill(int linetype)
    {
        boolean result=false;
        try
        {
            switch(linetype)
            {
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                case TacticalLines.CFL:
                case TacticalLines.DIRATKAIR:
                case TacticalLines.BOUNDARY:
                case TacticalLines.ISOLATE:
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.CORDONSEARCH:
                case TacticalLines.OCCUPY:
                case TacticalLines.RETAIN:
                case TacticalLines.SECURE:
                case TacticalLines.FLOT:
                case TacticalLines.LC:
                case TacticalLines.PL:
                case TacticalLines.FEBA:
                case TacticalLines.LL:
                case TacticalLines.EWL:
//                case TacticalLines.AC:
//                case TacticalLines.SAAFR:
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
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.LINE:
                case TacticalLines.ABATIS:
                case TacticalLines.ATDITCH:
                case TacticalLines.ATWALL:
                case TacticalLines.SFENCE:
                case TacticalLines.DFENCE:
                case TacticalLines.UNSP:
                case TacticalLines.PLD:
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
                case TacticalLines.NFL:
                case TacticalLines.MFP:
                case TacticalLines.RFL:
                case TacticalLines.CONVOY:
                case TacticalLines.HCONVOY:
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
                    result = true;
                    break;
                default:
                    result = false;
                    break;
            }
        }
        catch(Exception exc)
        {
               ErrorLogger.LogException(_className ,"LinesWithFill",
                    new RendererException("Failed inside LinesWithFill", exc));
        }
        return result;
    }
    /**
     * @deprecated
     * if the line color and fill color are the same or very close then we want to
     * tweak the fill color a bit to make the line appear distinct from the fill.
     * @param tg
     */
    public static void tweakFillColor(TGLight tg)
    {
        try
        {
            if(isSameColor(tg.get_LineColor(),tg.get_FillColor())==false)
                return;

            Color fillColor=tg.get_FillColor();
            int r=fillColor.getRed(),g=fillColor.getGreen(),b=fillColor.getBlue();
            int alpha=fillColor.getAlpha();

            r*=0.9;
            g*=0.9;
            b*=0.9;
            alpha*=0.8;

            fillColor=new Color(r,g,b,alpha);
            tg.set_FillColor(fillColor);
        }
        catch(Exception exc)
        {
               ErrorLogger.LogException(_className ,"tweakFillColor",
                    new RendererException("Failed inside tweakFillColor", exc));
        }
    }
    /**
     * @deprecated
     * Test to see if two colors are similar
     * @param c1
     * @param c2
     * @return true is same (or similar) color
     */
    public static Boolean isSameColor(Color c1, Color c2)
    {
        try
        {
            if(c1==null || c2==null)
                return true;

            int r1=c1.getRed(),r2=c2.getRed(),g1=c1.getGreen(),g2=c2.getGreen(),
                    b1=c1.getBlue(),b2=c2.getBlue();

            if(Math.abs(r1-r2)<5)
                if(Math.abs(g1-g2)<5)
                    if(Math.abs(b1-b2)<5)
                        return true;
        }
        catch(Exception exc)
        {
               ErrorLogger.LogException(_className ,"isSameColor",
                    new RendererException("Failed inside isSameColor", exc));
        }
        return false;
    }
    /**
     * Customer requested routine for setting the stroke dash pattern
     * Scales dash length with line width and DPI
     * @param width
     * @param style
     * @param cap
     * @param join
     * @return
     */
    public static BasicStroke getLineStroke(int width, int style, int cap, int join) {
        // Some segments are of length 0.1 because the Java2D renderer adds line caps of
        // width/2 size to both ends of the segment when "round" is one of BasicStroke.CAP_ROUND
        // or BasicStroke.CAP_SQUARE. This value is small enough not to affect the
        // stipple bit pattern calculation for the 3d map and still look good on the
        // 2d map.

        // NOTE: The dash arrays below do not supportBasisStroke.CAP_BUTT line capping,
        // although it would be relatively simple to change them such that they would.
        BasicStroke stroke=null;
        try {
            final float dashLength = 2 * width;
            final float dotLength = 1f;
            final float dotSpace = 2 * width;
            switch (style) {
                case 0://GraphicProperties.LINE_TYPE_SOLID:
                    stroke = new BasicStroke(width, cap, join);
                    break;
                case 1://GraphicProperties.LINE_TYPE_DASHED:
                    float[] dash = {dashLength, dashLength};
                    stroke = new BasicStroke(width, cap, join, 4f, dash, 0f);
                    break;
                case 2://GraphicProperties.LINE_TYPE_DOTTED:
                    float[] dot = {dotLength, dotSpace};
                    stroke = new BasicStroke(width, cap, join, 4f, dot, 0f);
                    break;
                case 3://GraphicProperties.LINE_TYPE_DASHDOT:
                    float[] dashdot = {2 * dashLength, dotSpace, dotLength, dotSpace};
                    stroke = new BasicStroke(width, cap, join, 4f, dashdot,0f );
                    break;
                case 4://GraphicProperties.LINE_TYPE_DASHDOTDOT:
                    float[] dashdotdot = {dashLength, dotSpace, dotLength, dotSpace, dotLength, dotSpace};
                    stroke = new BasicStroke(width, cap, join, 4f,dashdotdot, 0f );
                    break;
                default:
                    stroke = new BasicStroke(width, cap, join);
                    break;
            }
        } catch(Exception exc) {
            ErrorLogger.LogException(_className ,"getLineStroke",
                    new RendererException("Failed inside getLineStroke", exc));
        }
        return stroke;
    }
    /**
     * Sets shape properties based on other properties which were set by JavaLineArray
     * @param tg tactical graphic
     * @param shapes the ShapeInfo array
     * @param bi BufferedImage to use for setting shape TexturePaint
     */
    public static void SetShapeProperties(TGLight tg, ArrayList<Shape2> shapes,
            BufferedImage bi) {
        try
        {
            if (shapes == null)
            {
                return;
            }
            
            int j = 0;
            Shape2 shape = null;
            BasicStroke stroke = null;
            float[] dash = null;
            int lineThickness = tg.get_LineThickness();
            int shapeType = -1;
            int lineType = tg.get_LineType();
            boolean hasFill=LinesWithFill(lineType);
            boolean isChange1Area = clsUtility.IsChange1Area(lineType);
            boolean isClosedPolygon = clsUtility.isClosedPolygon(lineType);
            //int n=shapes.size();
            //remove air corridors fill shapes if fill is null
            if(tg.get_FillColor()==null)
            {
                switch(tg.get_LineType())
                {
                    case TacticalLines.AC:
                    case TacticalLines.SAAFR:
                    case TacticalLines.MRR:
                    case TacticalLines.SL:
                    case TacticalLines.TC:
                    case TacticalLines.SC:
                    case TacticalLines.LLTR:
                        shape=shapes.get(shapes.size()-1);
                        shapes.clear();
                        shapes.add(shape);
                        break;
                    case TacticalLines.CATK:
                    case TacticalLines.AIRAOA:
                    case TacticalLines.AAAAA:
                    case TacticalLines.SPT:
                    case TacticalLines.MAIN:
                    case TacticalLines.CATKBYFIRE:	//80
                        ArrayList<Shape2> tempShapes=new ArrayList();
                        for(j=0;j<shapes.size();j++)
                        {
                            shape=shapes.get(j);
                            if(shape.getShapeType() != Shape2.SHAPE_TYPE_FILL)
                                tempShapes.add(shape);
                        }
                        shapes=tempShapes;
                        break;
                    default:
                        break;
                }
            }
            for (j = 0; j < shapes.size(); j++) 
            {
                shape = shapes.get(j);
                if (shape == null || shape.getShape() == null) {
                    continue;
                }

                if (shape.getShapeType() == Shape2.SHAPE_TYPE_FILL) 
                {
                    switch(tg.get_LineType())
                    {
                        case TacticalLines.DEPTH_AREA:
                            break;
                        default:
                            shape.setFillColor(tg.get_FillColor());
                            break;
                    }
                }

                //if(lineType != TacticalLines.LEADING_LINE)
                ResolveModifierShape(tg, shape);
                if(lineType==TacticalLines.AIRFIELD)
                    if(j==1)
                        shape.setFillColor(null);

                shapeType = shape.getShapeType();

                Rectangle2D.Double rect = null;
                Graphics2D grid = null;
                TexturePaint tp = tg.get_TexturePaint();
                
                if(lineThickness==0)
                    lineThickness=1;
                //set the shape with the default properties
                //the switch statement below will override specific properties as needed
                stroke = getLineStroke(lineThickness,shape.get_Style(),tg.get_lineCap(),BasicStroke.JOIN_ROUND);
                if(shape.getShapeType()==Shape2.SHAPE_TYPE_FILL)
                {
                    stroke = new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
                    //shape.setStroke(new BasicStroke(0));
                }
                shape.setStroke(stroke);
            } // end loop over shapes
            if (tg.get_LineType() == TacticalLines.DIRATKAIR) {
                // Make arrowhead and bowtie shapes solid even if tg.get_LineStyle() isn't
                for (int i = 2; i < shapes.size(); i++) {
                    Shape2 arrowHeadShape = shapes.get(i);
                    arrowHeadShape.set_Style(0);
                    stroke = getLineStroke(lineThickness, 0, tg.get_lineCap(), BasicStroke.JOIN_ROUND);
                    arrowHeadShape.setStroke(stroke);
                }
            } else if (tg.get_LineType() == TacticalLines.DIRATKGND || tg.get_LineType() == TacticalLines.DIRATKSPT) {
                // Make arrowhead shape solid even if tg.get_LineStyle() isn't
                Shape2 arrowHeadShape = shapes.get(1);
                arrowHeadShape.set_Style(0);
                stroke = getLineStroke(lineThickness,0,tg.get_lineCap(),BasicStroke.JOIN_ROUND);
                arrowHeadShape.setStroke(stroke);
            } else if (tg.get_LineType() == TacticalLines.PDF) {
                Shape2 rectShape = shapes.get(1);
                rectShape.set_Style(0);
                stroke = getLineStroke(lineThickness, 0, tg.get_lineCap(), BasicStroke.JOIN_ROUND);
                rectShape.setStroke(stroke);
                rectShape.setFillColor(rectShape.getLineColor());
            }
        }
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"SetShapeProperties",
                    new RendererException("Failed inside SetShapeProperties", exc));
        }
    }
    /**
     * Returns a boolean indicating whether the line type is a change 1 area
     * @param lineType the line type
     * @return true if change 1 area
     */
    public static boolean IsChange1Area(int lineType) {
        try {
            switch (lineType) {
                case TacticalLines.LAUNCH_AREA:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                case TacticalLines.SHIP_AOI_CIRCULAR:
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CUED_ACQUISITION:
                case TacticalLines.CIRCULAR:
                case TacticalLines.BDZ:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.NOTACK:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.ACA_CIRCULAR:
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
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_FILL:
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.ACA_RECTANGULAR:
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
                    return true;
                default:
                    return false;
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.IsChange1Area");
               ErrorLogger.LogException(_className ,"IsChange1Area",
                    new RendererException("Failed inside IsChange1Area", exc));
        }
        return false;
    }

    public static void WriteFile(String str) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("Test.txt"));
            bufferedWriter.write(str);
            //bufferedWriter.newLine();
            //bufferedWriter.write(pointType);
            bufferedWriter.close();
            bufferedWriter = null;
        } 
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"WriteFile",
                    new RendererException("Failed inside WriteFile", exc));
        }
    }

    /**
     * Calculates point where two lines intersect.
     * First line defined by pt1, m1.
     * Second line defined by pt2, m2.
     * result will be written to ptIntersect.
     * @param pt1 first line point
     * @param m1 slope of first line
     * @param pt2 second line point
     * @param m2 slope of second line
     * @param ptIntersect OUT - intersection point
     */
    protected static void CalcIntersectPt(POINT2 pt1,
            double m1,
            POINT2 pt2,
            double m2,
            POINT2 ptIntersect) {
        try {
            if (m1 == m2) {
                return;
            }

            double x1 = pt1.x;
            double y1 = pt1.y;
            double x2 = pt2.x;
            double y2 = pt2.y;
            //formula for the intersection of two lines
            double dx2 = (double) ((y1 - y2 + m1 * x2 - m1 * x1) / (m2 - m1));
            double x3 = x2 + dx2;
            double y3 = (double) (y2 + m2 * dx2);

            ptIntersect.x = x3;
            ptIntersect.y = y3;
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.CalcIntersectPt");
            ErrorLogger.LogException(_className, "CalcIntersectPt",
                    new RendererException("Failed inside CalcIntersectPt", exc));
        }
    }

    /**
     * Calculates the channel width in pixels for channel types
     * @param pixels the client points as 2-tuples x,y in pixels
     * @param distanceToChannelPOINT2 OUT - the calculated distance in pixels from the tip of the
     * arrowhead to the back of the arrowhead.
     * @return the channel width in pixels
     */
    protected static int ChannelWidth(double[] pixels,
            ref<double[]> distanceToChannelPOINT2) {
        int width = 0;
        try {
            int numPOINT2s = pixels.length / 2;
            if (numPOINT2s < 3) {
                return 0;
            }

            POINT2 channelWidthPOINT2 = new POINT2(0, 0);
            POINT2 lastSegmentPt1 = new POINT2(0, 0);
            POINT2 lastSegmentPt2 = new POINT2(0, 0);

            lastSegmentPt1.x = (double) pixels[2 * numPOINT2s - 6];
            lastSegmentPt1.y = (double) pixels[2 * numPOINT2s - 5];
            lastSegmentPt2.x = (double) pixels[2 * numPOINT2s - 4];
            lastSegmentPt2.y = (double) pixels[2 * numPOINT2s - 3];
            channelWidthPOINT2.x = (double) pixels[2 * numPOINT2s - 2];
            channelWidthPOINT2.y = (double) pixels[2 * numPOINT2s - 1];

            ref<double[]> m = new ref();
            double m1 = 0;
            //m1.value=new double[1];
            double distance = 0;
            POINT2 ptIntersect = new POINT2(0, 0);
            //boolean bolVertical = TrueSlope(lastSegmentPt1, lastSegmentPt2, ref m);
            boolean bolVertical = lineutility.CalcTrueSlopeDouble2(lastSegmentPt1, lastSegmentPt2, m);
            if (bolVertical == true && m.value[0] != 0) {
                m1 = -1 / m.value[0];
                CalcIntersectPt(channelWidthPOINT2, m1, lastSegmentPt2, m.value[0], ptIntersect);
                distance = lineutility.CalcDistanceDouble(channelWidthPOINT2, ptIntersect);
            }
            if (bolVertical == true && m.value[0] == 0) //horizontal segment
            {
                distance = Math.abs(channelWidthPOINT2.y - lastSegmentPt1.y);
            }
            if (bolVertical == false) //vertical segment
            {
                distance = Math.abs(channelWidthPOINT2.x - lastSegmentPt1.x);
                distanceToChannelPOINT2.value = new double[1];
                distanceToChannelPOINT2.value[0] = distance;
                return (int) distance * 4;
            }

            width = (int) distance * 8;
            if (width < 2) {
                width = 2;
            }

            double hypotenuse = lineutility.CalcDistanceDouble(lastSegmentPt2, channelWidthPOINT2);
            distanceToChannelPOINT2.value = new double[1];
            distanceToChannelPOINT2.value[0] = Math.sqrt(hypotenuse * hypotenuse - distance * distance);

        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.ChannelWidth");
            ErrorLogger.LogException(_className, "ChannelWidth",
                    new RendererException("Failed inside ChannelWidth", exc));
        }
        return width;
    }

    private static boolean InYOrder(POINT2 pt0,
            POINT2 pt1,
            POINT2 pt2) {
        try {
            if (pt0.y <= pt1.y && pt1.y <= pt2.y) {
                return true;
            }

            if (pt2.y <= pt1.y && pt1.y <= pt0.y) {
                return true;
            }

        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.InYOrder");
            ErrorLogger.LogException(_className, "InYOrder",
                    new RendererException("Failed inside InYOrder", exc));
        }
        return false;
    }
    /// <summary>
    /// tests if POINT2s have successively increasing or decreasing x values.
    /// </summary>
    /// <param name="pt0"></param>
    /// <param name="pt1"></param>
    /// <param name="pt2"></param>
    /// <returns>true if POINT2s are in X order</returns>

    private static boolean InXOrder(POINT2 pt0,
            POINT2 pt1,
            POINT2 pt2) {
        try {
            if (pt0.x <= pt1.x && pt1.x <= pt2.x) {
                return true;
            }

            if (pt2.x <= pt1.x && pt1.x <= pt0.x) {
                return true;
            }

        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.InXOrder");
            ErrorLogger.LogException(_className, "InXOrder",
                    new RendererException("Failed inside InXOrder", exc));
        }
        return false;
    }

    /**
     * For each sector calculates left azimuth, right azimuth, min radius, max radius
     * and stuff H2 with the string delimited result. The function is public, called by JavaRendererServer
     * @param tg tactical graphic
     */
    public static void GetSectorRadiiFromPoints(TGLight tg) {
        try {
            if(tg.get_LineType()==TacticalLines.RANGE_FAN_FILL)
                return;
            POINT2 ptCenter = tg.LatLongs.get(0);
            POINT2 ptLeftMin = new POINT2(), ptRightMax = new POINT2();
            int k = 0;
            String strLeft = "", strRight = "", strMin = "", strMax = "", temp = "";
            double nLeft = 0, nRight = 0, nMin = 0, nMax = 0;
            //if tg.PointCollection has more than one point
            //we use the points to calculate left,right,min,max
            //and then stuff tg.H2 with the comma delimited string
            double dist = 0;
            ref<double[]> a12 = new ref(), a21 = new ref();
            int numSectors = 0;
            if (tg.LatLongs.size() > 2) {
                numSectors = (tg.LatLongs.size() - 2) / 2;
                for (k = 0; k < numSectors; k++) {
                    //get the sector points
                    ptLeftMin = tg.LatLongs.get(2 * k + 2);
                    ptRightMax = tg.LatLongs.get(2 * k + 3);

                    dist = mdlGeodesic.geodesic_distance(ptCenter, ptLeftMin, a12, a21);
                    nLeft = a12.value[0];
                    strLeft = Double.toString(nLeft);

                    nMin = dist;
                    strMin = Double.toString(nMin);

                    dist = mdlGeodesic.geodesic_distance(ptCenter, ptRightMax, a12, a21);
                    nRight = a12.value[0];
                    strRight = Double.toString(nRight);

                    nMax = dist;
                    strMax = Double.toString(nMax);

                    if (k == 0) {
                        temp = strLeft + "," + strRight + "," + strMin + "," + strMax;
                    } else {
                        temp += "," + strLeft + "," + strRight + "," + strMin + "," + strMax;
                    }
                }
                if (!temp.equals("")) {
                    tg.set_LRMM(temp);
                }
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.GetSectorRadiiFromPoints");
            ErrorLogger.LogException(_className, "GetSectorRadiiFromPoints",
                    new RendererException("Failed inside GetSectorRadiiFromPoints", exc));
        }
    }

    /**
     * Reverses the pixels except for the last point. This is used for
     * the axis of advance type routes. The pixels are 2-tuples x,y
     *
     * @param pixels OUT - Array of client points
     */
    protected static void ReorderPixels(double[] pixels) {
        try {
            double[] tempPixels;
            //reverse the pixels
            int j;
            double x;
            double y;
            int counter;
            int numPoints;
            counter = 0;
            numPoints = pixels.length / 2;
            tempPixels = new double[pixels.length];
            for (j = 0; j < numPoints - 1; j++) {
                x = pixels[pixels.length - 2 * j - 4];
                y = pixels[pixels.length - 2 * j - 3];
                tempPixels[counter] = x;
                tempPixels[counter + 1] = y;
                counter += 2;
            }
            //put the last pixel point into the last temppixels point
            int intPixelSize = pixels.length;
            tempPixels[counter] = pixels[intPixelSize - 2];
            tempPixels[counter + 1] = pixels[intPixelSize - 1];
            //stuff the pixels
            int n=pixels.length;
            //for (j = 0; j < pixels.length; j++) 
            for (j = 0; j < n; j++) 
            {
                pixels[j] = tempPixels[j];
            }
            //tempPixels = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ReorderPixels",
                    new RendererException("Failed inside ReorderPixels", exc));
        }
    }
    /**
     * do not allow vertical segments for these, move the point x value by 1 pixel
     * @param tg tactical graphic
     */
    public static void FilterVerticalSegments(TGLight tg)
    {
        try
        {
            switch(tg.get_LineType())
            {
                case TacticalLines.MAIN:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.SPT:
                case TacticalLines.LC:
                case TacticalLines.UNSP:
                case TacticalLines.DFENCE:
                case TacticalLines.SFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                case TacticalLines.MSR_ONEWAY:
                case TacticalLines.MSR_TWOWAY:
                case TacticalLines.MSR_ALT:
                case TacticalLines.ASR_ONEWAY:
                case TacticalLines.ASR_TWOWAY:
                case TacticalLines.ASR_ALT:
                case TacticalLines.ROUTE_ONEWAY:
                case TacticalLines.ROUTE_ALT:
                case TacticalLines.ATWALL:
                    break;
                default:
                    return;
            }
            POINT2 ptCurrent=null;
            POINT2 ptLast=null;
            int n=tg.Pixels.size();
            //for(int j=1;j<tg.Pixels.size();j++)
            for(int j=1;j<n;j++)
            {
                ptLast=new POINT2(tg.Pixels.get(j-1));
                ptCurrent=new POINT2(tg.Pixels.get(j));
                //if(Math.round(ptCurrent.x)==Math.round(ptLast.x))
                if(Math.abs(ptCurrent.x-ptLast.x)<1)
                {
                    if (ptCurrent.x>=ptLast.x)
                        ptCurrent.x += 1;
                    else
                        ptCurrent.x -= 1;
                    tg.Pixels.set(j, ptCurrent);
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("clsUtility", "FilterVerticalSegments",
                    new RendererException("Failed inside FilterVerticalSegments", exc));

        }
    }
    /**
     * Client utility to calculate the channel points for channel types.
     * This code was ported from CJMTK.
     * @param arrLocation the client points
     * @return the channel point
     */
    public static POINT2 ComputeLastPoint(ArrayList<POINT2> arrLocation) {
        POINT2 locD = new POINT2(0, 0);
        try {
            POINT2 locA = arrLocation.get(1);
            //Get the first point (b) in pixels.
            //var locB:Point=new Point(arrLocation[0].x,arrLocation[0].y);
            POINT2 locB = arrLocation.get(0);

            //Compute the distance in pixels from (a) to (b).
            double dblDx = locB.x - locA.x;
            double dblDy = locB.y - locA.y;

            //Compute the dblAngle in radians from (a) to (b).
            double dblTheta = Math.atan2(-dblDy, dblDx);

            //Compute a reasonable intermediate point along the line from (a) to (b).
            POINT2 locC = new POINT2(0, 0);
            locC.x = (int) (locA.x + 0.85 * dblDx);
            locC.y = (int) (locA.y + 0.85 * dblDy);
            //Put the last point on the left side of the line from (a) to (b).
            double dblAngle = dblTheta + Math.PI / 2.0;
            if (dblAngle > Math.PI) {
                dblAngle = dblAngle - 2.0 * Math.PI;
            }
            if (dblAngle < -Math.PI) {
                dblAngle = dblAngle + 2.0 * Math.PI;
            }

            //Set the magnitude of the dblWidth in pixels.  Make sure it is at least 15 pixels.
            double dblWidth = 30;//was 15

            //Compute the last point in pixels.
            locD.x = (locC.x + dblWidth * Math.cos(dblAngle));
            locD.y = (locC.y - dblWidth * Math.sin(dblAngle));
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsUtility.ComputeLatPoint");
            ErrorLogger.LogException(_className, "ComputeLastPoint",
                    new RendererException("Failed inside ComputeLastPoint", exc));
        }
        return locD;
    }

    /**
     * Called by clsChannelUtility. The segments are used for managing double-backed segments
     * for channel types. If the new point is double-backed then the segment at that index will be false.
     *
     * @param pixels the client points as 2-tuples x,y in pixels
     * @param segments OUT - the segments
     * @param factor a steepness factor for calculating whether the segment is double-backed
     */
    protected static void GetSegments(double[] pixels,
            boolean[] segments,
            double factor) {
        try
        {
            int j = 0;
            ref<double[]> m1 = new ref();
            ref<double[]> m2 = new ref();
            long numPoints = 0;
            boolean bolVertical1 = false;
            boolean bolVertical2 = false;

            POINT2 pt0F = new POINT2(0, 0);
            POINT2 pt1F = new POINT2(0, 0);
            POINT2 pt2F = new POINT2(0, 0);

            segments[0] = true;
            
            numPoints = pixels.length / 2;
            for (j = 0; j < numPoints - 2; j++)
            {
                pt0F.x = (double) pixels[2 * j];
                pt0F.y = (double) pixels[2 * j + 1];

                pt1F.x = (double) pixels[2 * j + 2];
                pt1F.y = (double) pixels[2 * j + 3];

                pt2F.x = (double) pixels[2 * j + 4];
                pt2F.y = (double) pixels[2 * j + 5];

                bolVertical1 = lineutility.CalcTrueSlopeDoubleForRoutes(pt0F, pt1F, m1);
                bolVertical2 = lineutility.CalcTrueSlopeDoubleForRoutes(pt1F, pt2F, m2);

                segments[j + 1] = true;
                if (bolVertical1 == true && bolVertical2 == true)
                {
                    if (Math.abs(Math.atan(m1.value[0]) - Math.atan(m2.value[0])) < 1 / factor && InXOrder(pt0F, pt1F, pt2F) == false) //was 0.1
                    {
                        segments[j + 1] = false;
                    }
                }

                if ((bolVertical1 == false || Math.abs(m1.value[0]) > factor) && (bolVertical2 == false || Math.abs(m2.value[0]) > factor) && InYOrder(pt0F, pt1F, pt2F) == false) //was 10
                {
                    segments[j + 1] = false;
                }
            }	//end for
            //int n=segments.length;
        }
        catch (Exception exc)
        {
            //System.out.println(e.getMessage());
            //clsUtility.WriteFile("Error in clsUtility.GetSegments");
            ErrorLogger.LogException(_className, "GetSegments",
                    new RendererException("Failed inside GetSegments", exc));
        }
    }

    protected static void GetLCPartitions(double[] pixels,
                                          double LCChannelWith,
                                          ArrayList<P1> partitions,
                                          ArrayList<P1> singleLinePartitions) {
        try
        {
            int numPoints = pixels.length / 2;
            POINT2 pt0F = new POINT2(0, 0);
            POINT2 pt1F = new POINT2(0, 0);
            POINT2 pt2F = new POINT2(0, 0);

            P1 nextP = new P1();
            nextP.start = 0;

            //used for debugging
            double[] angles = new double[numPoints - 1];

            for (int i = 0; i < numPoints - 2; i++) {
                pt0F.x = (double) pixels[2 * i];
                pt0F.y = (double) pixels[2 * i + 1];

                pt1F.x = (double) pixels[2 * i + 2];
                pt1F.y = (double) pixels[2 * i + 3];

                pt2F.x = (double) pixels[2 * i + 4];
                pt2F.y = (double) pixels[2 * i + 5];

                double angle1 = Math.atan2(pt1F.y - pt0F.y, pt1F.x - pt0F.x);
                double angle2 = Math.atan2(pt1F.y - pt2F.y, pt1F.x - pt2F.x);
                double angle = angle1-angle2;// * 180/Math.PI;
                double degrees = angle * 180/Math.PI;
                if (angle < 0) {
                    degrees = 360 + degrees;
                }

                if (degrees > 270) {
                    boolean angleTooSmall = false;

                    if (lineutility.CalcDistanceDouble(pt0F, pt1F) < lineutility.CalcDistanceDouble(pt1F, pt2F)) {
                        POINT2 newPt = lineutility.ExtendAlongLineDouble2(pt1F, pt2F, lineutility.CalcDistanceDouble(pt1F, pt0F));
                        if (lineutility.CalcDistanceDouble(pt0F, newPt) < LCChannelWith)
                            angleTooSmall = true;
                    } else {
                        POINT2 newPt = lineutility.ExtendAlongLineDouble2(pt1F, pt0F, lineutility.CalcDistanceDouble(pt1F, pt2F));
                        if (lineutility.CalcDistanceDouble(pt2F, newPt) < LCChannelWith)
                            angleTooSmall = true;
                    }
                    if (angleTooSmall) {
                        // Angle is too small to fit channel, make it a single line partition
                        nextP.end_Renamed = i - 1;
                        partitions.add(nextP);
                        nextP = new P1();
                        nextP.start = i;
                        nextP.end_Renamed=i + 2;
                        singleLinePartitions.add(nextP);
                        i++;
                        nextP = new P1();
                        nextP.start = i + 1;
                    }
                } else if(degrees < 90) {
                    // new Partition
                    nextP.end_Renamed = i;
                    partitions.add(nextP);
                    nextP = new P1();
                    nextP.start = i + 1;
                }
                angles[i] = degrees;
            } //end for
            nextP.end_Renamed = numPoints - 2;
            partitions.add(nextP);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetLCPartitions",
                    new RendererException("Failed inside GetLCPartitions", exc));
        }
    }

    /**
     * Sets the color for the current shape depending on the affiliation
     * @param tg
     * @param shape
     */
    protected static void SetLCColor(TGLight tg, Shape2 shape) {
        try {
            if (tg.isHostile()) {
                if (shape.getLineColor() == Color.RED) {
                    shape.setLineColor(tg.get_LineColor());
                } else {
                    shape.setLineColor(Color.RED);
                }
            } else {
                if (shape.getLineColor() != Color.RED) {
                    shape.setLineColor(tg.get_LineColor());
                } else {
                    shape.setLineColor(Color.RED);
                }
            }

        } catch (Exception exc) {
            //WriteFile("Error in clsUtility.SetLCColor");
            ErrorLogger.LogException(_className, "SetLCColor",
                    new RendererException("Failed inside SetLCColor", exc));
        }
    }
    /**
     * USAS requires a left-right orientation for ENY, which negates the upper-lower
     * orientation we used for Mil-Std-2525 ENY compliance. Therefore we must reverse
     * the client points for two of the quadrants
     * @param tg tactical graphic
     */
    public static void ReverseUSASLCPointsByQuadrant(TGLight tg)
    {
        try
        {
            if(tg.Pixels.size()<2)
                return;
            int quadrant=lineutility.GetQuadrantDouble(tg.Pixels.get(0), tg.Pixels.get(1));
            switch(tg.get_LineType())
            {
                case TacticalLines.LC:
                    if(tg.isHostile())
                    {
                        switch(quadrant)
                        {
                            case 2:
                            case 3:
                                break;
                            case 1://reverse the points for these two quadrants
                            case 4:
                                int n=tg.Pixels.size();
                                ArrayList<POINT2> pts2=(ArrayList<POINT2>)tg.Pixels.clone();
                                        //for(int j=0;j<tg.Pixels.size();j++)
                                        for(int j=0;j<n;j++)
                                            tg.Pixels.set(j, pts2.get(n-j-1));
                                break;
                        }//end switch quadrant
                    }//end if
                    else
                    {
                        switch(quadrant)
                        {
                            case 1:
                            case 4:
                                break;
                            case 2://reverse the points for these two quadrants
                            case 3:
                                int n=tg.Pixels.size();
                                ArrayList<POINT2> pts2=(ArrayList<POINT2>)tg.Pixels.clone();
                                        //for(int j=0;j<tg.Pixels.size();j++)
                                        for(int j=0;j<n;j++)
                                            tg.Pixels.set(j, pts2.get(n-j-1));
                                break;
                        }//end switch quadrant
                    }//end else
                    break;
                default:
                    break;
            }//end switch linetype
        }
        catch (Exception exc) {
            //WriteFile("Error in clsUtility.SetLCColor");
            ErrorLogger.LogException(_className, "ReverseUSASLCPointsByQuadrant",
                    new RendererException("Failed inside ReverseUSASLCPointsByQuadrant", exc));
        }
    }//end ReverseUSASLCPointsByQuadrant
    /**
     * use str if tg is null
     * @param symbolId Mil=Standard-2525 symbol id
     * @return line type
     */
    public static int GetLinetypeFromString(String symbolId)
    {
        try
        {
            if (symbolId.length() < 16){
                return -1;
            }
            int symbolSet = SymbolID.getSymbolSet(symbolId);
            int entityCode = SymbolID.getEntityCode(symbolId);
            int version = SymbolID.getVersion(symbolId);
            if (symbolSet == 25) {
                return clsRenderer.getCMLineType(version, entityCode);
            } else if (symbolSet == 45 || symbolSet == 46) {
                return clsMETOC.getWeatherLinetype(version, entityCode);
            }
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className ,"GetLinetypeFromString",
                    new RendererException("Failed inside GetLinetypeFromString", exc));
        }
        return -1;
    }

    /**
     * An auto-shape is a symbol with a fixed number of anchor points
     *
     * @param tg tactical graphic
     * @return true if auto-shape
     */
    public static boolean isAutoshape(TGLight tg) {
        try {
            MSInfo msInfo = MSLookup.getInstance().getMSLInfo(tg.get_SymbolId());
            if (msInfo == null || IsChange1Area(tg.get_LineType())) {
                return false;
            }
            switch (tg.get_LineType()) {
                case TacticalLines.DIRATKAIR:
                case TacticalLines.DIRATKGND:
                case TacticalLines.DIRATKSPT:
                    // Direction of attack symbols only have two points but can handle more
                    return false;
                default:
                    break;
            }
            switch (msInfo.getDrawRule()) {
                case DrawRules.LINE26: // Two ways to draw but fixed points
                case DrawRules.LINE27: // Two ways to draw but fixed points
                case DrawRules.AREA26: // Need same number of points in first half and second half to make two shapes
                case DrawRules.CORRIDOR1: // Each point represents an Air Control Point or Communications Checkpoint
                    return true;
                default:
                    return msInfo.getMaxPointCount() == msInfo.getMinPointCount();
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "isAutoshape",
                    new RendererException("Failed inside isAutoshape", exc));
        }
        return false;
    }
    /**
     * Client will send the segment colors within a modifier.
     * Format is 0:FFBBBB,4:FFAAAA,...
     * For the time being will assume the modifier being used is the H modifier
     * @param tg
     * @return 
     */
    public static HashMap<Integer,Color> getMSRSegmentColors(TGLight tg)
    {
        HashMap<Integer,Color> hMap=null;
        try
        {
            int linetype=tg.get_LineType();
            switch(linetype)
            {
                case TacticalLines.MSR:
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                case TacticalLines.BOUNDARY:
                    if(tg.get_H()==null || tg.get_H().isEmpty())
                        return null;
                    hMap=new HashMap<Integer,Color>();
                    break;
                default:
                    return null;
            }
            String[]colorStrs=tg.get_H().split(",");
            int j=0,numSegs=colorStrs.length;
            String segPlusColor="";
            String[]seg=null;     
            Color color=null;
            int index=-1;
            for(j=0;j<numSegs;j++)
            {
                segPlusColor=colorStrs[j];
                if(!segPlusColor.contains(":"))
                    continue;
                seg=segPlusColor.split(":");
                color= RendererUtilities.getColorFromHexString(seg[1]);
                index=Integer.parseInt(seg[0]);
                hMap.put(index, color);
            }
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"getMSRSegmentColors",
                    new RendererException("Failed inside getMSRSegmentColors", exc));
        }
        return hMap;
    }
    public static HashMap<Integer, String> getMSRSegmentColorStrings(TGLight tg)
    {
        HashMap<Integer, String> hMap=null;
        try
        {
            int linetype = tg.get_LineType();
            switch (linetype) {
                case TacticalLines.MSR:
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                case TacticalLines.BOUNDARY:
                    if (tg.get_H() == null || tg.get_H().isEmpty())
                        return null;
                    hMap = new HashMap<>();
                    break;
                default:
                    return null;
            }
            String[] colorStrs = tg.get_H().split(",");
            int j = 0;
            int numSegs = colorStrs.length;
            String segPlusColor = "";
            String[] seg = null;
            //Color color = null;
            int index = -1;
            for (j = 0; j < numSegs; j++) {
                segPlusColor = colorStrs[j];
                if (!segPlusColor.contains(":"))
                    continue;
                seg = segPlusColor.split(":");
                //color = armyc2.c5isr.renderer.utilities.SymbolUtilitiesD.getColorFromHexString(seg[1]);
                index = Integer.parseInt(seg[0]);
                //hMap.put(new Integer(index), color);
                hMap.put(new Integer(index), seg[1]);
            }            
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"getMSRSegmentColorStrings",
                    new RendererException("Failed inside getMSRSegmentColorStrings", exc));
        }
        return hMap;
    }
    /**
     * tg.H must be revised for clipped MSR, ASR and Boundary
     * This function is called after the pixels were clipped
     * @param originalPixels the tactical graphic pixels before clipping
     * @param tg 
     */
    public static void reviseHModifier(ArrayList<POINT2>originalPixels, 
            TGLight tg)
    {
        try
        {
            //only revise tg.H if it is not null or empty
            //and the linetype is bounday, MSR, or ASR
            if(tg.get_H()==null || tg.get_H().isEmpty())
                return;
            int linetype=tg.get_LineType();
            switch(linetype)
            {
                case TacticalLines.ASR:
                case TacticalLines.MSR:
                case TacticalLines.ROUTE:
                case TacticalLines.BOUNDARY:
                    break;
                default:
                    return;
            }
            int j=0,k=0;
            //Line2D line=new Line2D.Double();
            
            //get the first common point between the original points and tg.Pixels
            //if it is n then n segments will have been dropped at the front end of
            //the clipped array (from the original pixels) so then we would want to
            //set the start index to n for the loop through the original points
            int n=-1; 
            boolean foundPt=false;
            int t=originalPixels.size();
            int u=tg.Pixels.size();
            //for(j=0;j<originalPixels.size();j++)
            for(j=0;j<t;j++)
            {
                //for(k=0;k<tg.Pixels.size();k++)
                for(k=0;k<u;k++)
                {
                    if(originalPixels.get(j).x==tg.Pixels.get(k).x && originalPixels.get(j).y==tg.Pixels.get(k).y)
                    {
                        n=j;
                        foundPt=true;
                        break;
                    }
                }
                if(foundPt)
                    break;
            }
            HashMap<Integer,Color> hmap=getMSRSegmentColors(tg);
            //use a 2nd hashmap to store the revised segment numbers, and exisitng Colors
            HashMap<Integer,Color> hmap2=new HashMap<Integer,Color>();
            POINT2 segPt0=null,segPt1=null; //the original segments
            POINT2 pt0=null,pt1=null;   //the clipped segments
            Color color=null;
            if(n<1)
                n=1;
            for(Integer key : hmap.keySet()) //keys can begin at 0
            {
                if(key<n-1)
                    continue;
                if(key+1>originalPixels.size()-1)
                    break;
                color=hmap.get(key);
                segPt0=originalPixels.get(key);
                segPt1=originalPixels.get(key+1);
                u=tg.Pixels.size();
                //for(j=0;j<tg.Pixels.size()-1;j++)
                for(j=0;j<u-1;j++)
                {
                    pt0=tg.Pixels.get(j);//clipped pixels
                    pt1=tg.Pixels.get(j+1);
                    if(segPt0.x==pt0.x && segPt0.y==pt0.y)
                    {
                        hmap2.put(j, color);
                        break;
                    }
                    else if(segPt1.x==pt1.x && segPt1.y==pt1.y)
                    {
                        hmap2.put(j, color);
                        break;
                    }
                    else
                    {
                        if(pt0.x==segPt1.x && pt0.y==segPt1.y)
                            continue;
                        if(pt1.x==segPt0.x && pt1.y==segPt0.y)
                            continue;
                        else    
                        {       
                            //if the original segment straddles or clips the clipping area
                            //then the original segment will contain the clipped segment
                            double dist0=lineutility.CalcDistanceToLineDouble(segPt0, segPt1, pt0);
                            double dist1=lineutility.CalcDistanceToLineDouble(segPt0, segPt1, pt1);
                            Line2D lineOrigPts=new Line2D.Double(segPt0.x,segPt0.y, segPt1.x,segPt1.y);
                            Rectangle2D rectOrigPts=lineOrigPts.getBounds2D();
                            Line2D lineClipPts=new Line2D.Double(pt0.x,pt0.y, pt1.x, pt1.y);
                            Rectangle2D rectClipPts=lineClipPts.getBounds2D();
                            //test if the lines coincide and the clipped segment is within the original segment
                            if(dist0<1 && dist1<1 && rectOrigPts.contains(rectClipPts))
                            {
                                hmap2.put(j, color);                                
                            }
                        }
                    }
                }
            }        
            if(hmap2.isEmpty())
            {
                tg.set_H("");
                return;
            }
           
            String h="",temp="";
            for(Integer key : hmap2.keySet()) 
            {
                color=hmap2.get(key);
                temp=Integer.toHexString(color.getRGB());
                h+=key.toString()+":"+temp+",";
            }
            h=h.substring(0, h.length()-1);
            tg.set_H(h);
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "reviseHModifer",
                    new RendererException("Failed inside reviseHModifier", exc));
        }
    }

    /**
     * Adds extra points to LC if there are angles too small to fit the channel
     * @param tg
     * @param converter
     */
    public static void SegmentLCPoints(TGLight tg, IPointConversion converter) {
        try {
            if (tg.get_LineType() != TacticalLines.LC && tg.get_LineType() != TacticalLines.LC_HOSTILE)
                return;

            ArrayList<POINT2> points = tg.get_Pixels();

            double LCChannelWith = arraysupport.getScaledSize(40, tg.get_LineThickness());

            for (int i = 0; i < points.size() - 2; i++) {
                POINT2 ptA = new POINT2(points.get(i).x, points.get(i).y);
                POINT2 ptB = new POINT2(points.get(i+1).x, points.get(i+1).y);
                POINT2 ptC = new POINT2(points.get(i+2).x, points.get(i+2).y);

                double angle1 = Math.atan2(ptB.y - ptA.y, ptB.x - ptA.x);
                double angle2 = Math.atan2(ptB.y - ptC.y, ptB.x - ptC.x);
                double angle = angle1 - angle2;
                double degrees = angle * 180/Math.PI;

                if(angle < 0) {
                    degrees = 360 + degrees;
                }

                if (degrees > 270) {
                    // For acute angles where red is the outer line
                    // Determine shorter segment (BA or BC)
                    // On longer segment calculate potential new point (newPt) that is length of smaller segment from B
                    // If distance between smaller segment end point (A or C) and newPt is smaller than the channel width add newPt to points
                    // In GetLCPartitions() the black line won't be included between the smaller line and newPt since there isn't enough space to fit the channel
                    if (lineutility.CalcDistanceDouble(ptB, ptA) < lineutility.CalcDistanceDouble(ptB, ptC)) {
                        // BA is smaller segment
                        POINT2 newPt = lineutility.ExtendAlongLineDouble2(ptB, ptC, lineutility.CalcDistanceDouble(ptB, ptA));
                        if (lineutility.CalcDistanceDouble(ptA, newPt) < LCChannelWith) {
                            points.add(i + 2, new POINT2(newPt.x, newPt.y));
                            i++;
                        }
                    } else {
                        // BC is smaller segment
                        POINT2 newPt = lineutility.ExtendAlongLineDouble2(ptB, ptA, lineutility.CalcDistanceDouble(ptB, ptC));
                        if (lineutility.CalcDistanceDouble(ptC, newPt) < LCChannelWith) {
                            points.add(i + 1, new POINT2(newPt.x, newPt.y));
                            i++;
                        }
                    }
                }
            }
            tg.Pixels = points;
            tg.LatLongs = armyc2.c5isr.RenderMultipoints.clsUtility.PixelsToLatLong(points, converter);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "segmentLCPoints",
                    new RendererException("Failed inside segmentLCPoints", exc));
        }
    }

    /**
     * Interpolate pixels for lines with points too close together.
     * Drops successive points until the next point is at least 10 pixels from the preceding point
     * @param tg 
     */
    public static void InterpolatePixels(TGLight tg)
    {
        try
        {
            if(tg.get_UseLineInterpolation()==false)
                return;
            
            int linetype=tg.get_LineType();
            double glyphSize=10;
            switch(linetype)
            {
                case TacticalLines.ATDITCH:
                case TacticalLines.ATDITCHC:
                    glyphSize=25;
                    break;
                case TacticalLines.ATDITCHM:
                    glyphSize=50;
                    break;
                case TacticalLines.FLOT:
                case TacticalLines.LC:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.FORTL:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ZONE:
                case TacticalLines.OBSFAREA:
                case TacticalLines.OBSAREA:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                case TacticalLines.STRONG:
                    glyphSize= arraysupport.getScaledSize(30, tg.get_LineThickness());
                    break;
                case TacticalLines.UNSP:
                case TacticalLines.LINE:
                case TacticalLines.ATWALL:
                case TacticalLines.SFENCE:
                    glyphSize=arraysupport.getScaledSize(40, tg.get_LineThickness());
                    break;
                case TacticalLines.DFENCE:
                    glyphSize=arraysupport.getScaledSize(50, tg.get_LineThickness());
                    break;
                default:
                    return;
            }
            HashMap<Integer,POINT2> hmapPixels=new HashMap<Integer,POINT2>();
            HashMap<Integer,POINT2> hmapGeo=new HashMap<Integer,POINT2>();
            int j=0,currentIndex=0;
            double dist=0,dist2=0;
            double direction1=0,direction2=0,delta=0;
            POINT2 pt0=null,pt1=null,pt2=null;
            int n=tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for(j=0;j<n;j++)
            {
                if(j==0)
                {
                    hmapPixels.put(j, tg.Pixels.get(j));
                    hmapGeo.put(j, tg.LatLongs.get(j));
                    currentIndex=0;
                }
                else if(j==tg.Pixels.size()-1)
                {
                    hmapPixels.put(j, tg.Pixels.get(j));
                    hmapGeo.put(j, tg.LatLongs.get(j));                    
                }
                else
                {
                    dist=lineutility.CalcDistanceDouble(tg.Pixels.get(currentIndex), tg.Pixels.get(j));
                    dist2=lineutility.CalcDistanceDouble(tg.Pixels.get(j), tg.Pixels.get(j+1));
                    
                    //change of direction test 2-28-13
                    pt0=tg.Pixels.get(currentIndex);
                    pt1=tg.Pixels.get(j);
                    pt2=tg.Pixels.get(j+1);
                    direction1=(180/Math.PI)*Math.atan((pt0.y-pt1.y)/(pt0.x-pt1.x));
                    direction2=(180/Math.PI)*Math.atan((pt1.y-pt2.y)/(pt1.x-pt2.x));
                    delta=Math.abs(direction1-direction2);
                    if(dist>glyphSize || dist2>glyphSize || delta>20)
                    {
                        hmapPixels.put(j, tg.Pixels.get(j));
                        hmapGeo.put(j, tg.LatLongs.get(j));
                        currentIndex=j;
                    }
                }
            }
            ArrayList<POINT2>pixels=new ArrayList();
            ArrayList<POINT2>geo=new ArrayList();
            n=tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for(j=0;j<n;j++)
            {
                if(hmapPixels.containsKey(j))
                    pixels.add((POINT2)hmapPixels.get(j));
                if(hmapGeo.containsKey(j))
                    geo.add((POINT2)hmapGeo.get(j));
            }
            switch(linetype)
            {
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ZONE:
                case TacticalLines.OBSFAREA:
                case TacticalLines.OBSAREA:
                case TacticalLines.STRONG:
                    if(pixels.size()==2)
                    {
                        n=tg.Pixels.size();
                        //for(j=0;j<tg.Pixels.size();j++)
                        for(j=0;j<n;j++)
                        {
                            if(hmapPixels.containsKey(j)==false && hmapGeo.containsKey(j)==false)
                            {
                                pixels.add(j,tg.Pixels.get(j));
                                geo.add(j,tg.LatLongs.get(j));
                                break;
                            }
                        }                        
                    }
                    break;
                default:
                    break;
            }            
            tg.Pixels=pixels;
            tg.LatLongs=geo;
        }
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "InterpolatePixels",
                    new RendererException("Failed inside InterpolatePixels", exc));
        }
    }
    /**
     * construct a line segment outside the polygon corresponding to some index
     * @param tg
     * @param index
     * @param dist
     * @return 
     */
    protected static Line2D getExtendedLine(TGLight tg,
            int index,
            double dist)
    {
        Line2D line=null;
        try
        {
            Polygon polygon=new Polygon();
            int j=0;
            int n=tg.Pixels.size();
            //for(j=0;j<tg.Pixels.size();j++)
            for(j=0;j<n;j++)
            {
                polygon.addPoint((int)tg.Pixels.get(j).x, (int)tg.Pixels.get(j).y);
            }
            POINT2 pt0=null; 
            POINT2 pt1=null; 
            if(tg.Pixels.size()>3)
            {
                pt0=tg.Pixels.get(index);
                pt1=tg.Pixels.get(index+1);
            }
            else
            {
                pt0=tg.Pixels.get(1);
                pt1=tg.Pixels.get(2);                
            }
            
            POINT2 ptExtend=null;
            int extend=-1;
            POINT2 midPt=lineutility.MidPointDouble(pt0, pt1,0);
            double slope=Math.abs(pt1.y-pt0.y)/(pt1.x-pt0.x);
            if(slope<=1)
            {
                ptExtend=lineutility.ExtendDirectedLine(pt0, pt1, midPt, lineutility.extend_above, 2);
                if(polygon.contains(ptExtend.x,ptExtend.y))
                    extend=lineutility.extend_below;
                else
                    extend=lineutility.extend_above;
            }
            else
            {
                ptExtend=lineutility.ExtendDirectedLine(pt0, pt1, midPt, lineutility.extend_left, 2);
                if(polygon.contains(ptExtend.x,ptExtend.y))
                    extend=lineutility.extend_right;
                else
                    extend=lineutility.extend_left;
                
            }
            POINT2 pt3=null;
            POINT2 pt4=null;
            pt3=lineutility.ExtendDirectedLine(pt0, pt1, pt0, extend, dist);
            pt4=lineutility.ExtendDirectedLine(pt0, pt1, pt1, extend, dist);
            line=new Line2D.Double(pt3.x, pt3.y, pt4.x, pt4.y);
        }
        catch (Exception exc) {            
            ErrorLogger.LogException(_className, "getExtendedLine",
                    new RendererException("Failed inside getExtendedLine", exc));
        }
        return line;
    }

}//end clsUtility
