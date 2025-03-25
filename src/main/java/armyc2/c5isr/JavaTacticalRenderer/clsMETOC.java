package armyc2.c5isr.JavaTacticalRenderer;

import armyc2.c5isr.JavaLineArray.*;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import armyc2.c5isr.JavaLineArray.Shape2;

import armyc2.c5isr.renderer.PatternFillRenderer;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.ShapeInfo;
import armyc2.c5isr.renderer.utilities.SymbolID;


/**
 * Class to calculate the points for the Weather symbols
 * 
 */
public final class clsMETOC {
    private static final String _className = "clsMETOC";
    public static int getWeatherLinetype(int version, int entityCode)
    {
        switch(entityCode)
        {
            case 110301:
                return TacticalLines.CF;
            case 110302:
                return TacticalLines.UCF;
            case 110303:
                return TacticalLines.CFG;
            case 110304:
                return TacticalLines.CFY;
            case 110305:
                return TacticalLines.WF;
            case 110306:
                return TacticalLines.UWF;
            case 110307:
                return TacticalLines.WFG;
            case 110308:
                return TacticalLines.WFY;
            case 110309:
                return TacticalLines.OCCLUDED;
            case 110310:
                return TacticalLines.UOF;
            case 110311:
                return TacticalLines.OFY;
            case 110312:
                return TacticalLines.SF;
            case 110313:
                return TacticalLines.USF;
            case 110314:
                return TacticalLines.SFG;
            case 110315:
                return TacticalLines.SFY;
            case 110401:
                return TacticalLines.TROUGH;
            case 110402:
                return TacticalLines.UPPER_TROUGH;
            case 110403:
                return TacticalLines.RIDGE;
            case 110404:
                return TacticalLines.SQUALL;
            case 110405:
                return TacticalLines.INSTABILITY;
            case 110406:
                return TacticalLines.SHEAR;
            case 110407:
                return TacticalLines.ITC;
            case 110408:
                return TacticalLines.CONVERGENCE;
            case 110409:
                return TacticalLines.ITD;
            case 140300:
                return TacticalLines.JET;
            case 140400:
                return TacticalLines.STREAM;
            case 162004:            //tropical storm wind
                break;
            case 170100:
                return TacticalLines.IFR;
            case 170200:
                return TacticalLines.MVFR;
            case 170300:
                return TacticalLines.TURBULENCE;
            case 170400:
                return TacticalLines.ICING;
            case 170500:
                return TacticalLines.NON_CONVECTIVE;
            case 170501:
                return TacticalLines.CONVECTIVE;
            case 170600:
                return TacticalLines.FROZEN;
            case 170700:
                return TacticalLines.THUNDERSTORMS;
            case 170800:
                return TacticalLines.FOG;                
            case 170900:
                return TacticalLines.SAND;
            case 171000:
                return TacticalLines.FREEFORM;
            case 180100:
                return TacticalLines.ISOBAR;
            case 180200:
                return TacticalLines.UPPER_AIR;
            case 180300:
                return TacticalLines.ISOTHERM;
            case 180400:
                return TacticalLines.ISOTACH;
            case 180500:
                return TacticalLines.ISODROSOTHERM;
            case 180600:
                return TacticalLines.ISOPLETHS;
            case 180700:
                return TacticalLines.OPERATOR_FREEFORM;
            case 110501:
                return TacticalLines.LVO;
            case 110502:
                return TacticalLines.UNDERCAST;
            case 110503:
                return TacticalLines.LRO;
            case 110504:
                return TacticalLines.ICE_EDGE;
            case 110505:
                return TacticalLines.ESTIMATED_ICE_EDGE;
            case 110506:
                return TacticalLines.ICE_EDGE_RADAR;
            case 110601:
                return TacticalLines.CRACKS;
            case 110602:
                return TacticalLines.CRACKS_SPECIFIC_LOCATION;
            case 110603:
                return TacticalLines.ICE_OPENINGS_LEAD;
            case 110604:
                return TacticalLines.ICE_OPENINGS_FROZEN;
            case 120102:
                return TacticalLines.DEPTH_CURVE;
            case 120103:
                return TacticalLines.DEPTH_CONTOUR;
            case 120104:
                return TacticalLines.DEPTH_AREA;
            case 120201:
                return TacticalLines.COASTLINE;
            case 120202:
                return TacticalLines.ISLAND;
            case 120203:
                return TacticalLines.BEACH;
            case 120204:
                return TacticalLines.WATER;
            case 120205:
                return TacticalLines.FORESHORE_LINE;
            case 120206:
                return TacticalLines.FORESHORE_AREA;
            case 120305:
                return TacticalLines.ANCHORAGE_LINE;
            case 120306:
                return TacticalLines.ANCHORAGE_AREA;
                
            case 120308:
                return TacticalLines.PIER;
            case 120312:
                return TacticalLines.FISH_TRAPS;
            case 120314:
                return TacticalLines.DRYDOCK;
            case 120317:
                return TacticalLines.LOADING_FACILITY_LINE;
            case 120318:
                return TacticalLines.LOADING_FACILITY_AREA;
                
            case 120319:
                return TacticalLines.RAMP_ABOVE_WATER;
            case 120320:
                return TacticalLines.RAMP_BELOW_WATER;
                
            case 120326:
                return TacticalLines.JETTY_ABOVE_WATER;
            case 120327:
                return TacticalLines.JETTY_BELOW_WATER;
            case 120328:
                return TacticalLines.SEAWALL;
            case 120405:
                return TacticalLines.PERCHES;
            case 120407:
                return TacticalLines.LEADING_LINE;
            case 120503:
                return TacticalLines.UNDERWATER_HAZARD;
            case 120505:
                return TacticalLines.FOUL_GROUND;
            case 120507:
                return TacticalLines.KELP;
            case 120511:
                return TacticalLines.BREAKERS;
            case 120512:
                return TacticalLines.REEF;
            case 120514:
                return TacticalLines.DISCOLORED_WATER;
            case 120702:
                return TacticalLines.EBB_TIDE;
            case 120703:
                return TacticalLines.FLOOD_TIDE;
                
            case 130101:
                return TacticalLines.VDR_LEVEL_12;
            case 130102:
                return TacticalLines.VDR_LEVEL_23;
            case 130103:
                return TacticalLines.VDR_LEVEL_34;
            case 130104:
                return TacticalLines.VDR_LEVEL_45;
            case 130105:
                return TacticalLines.VDR_LEVEL_56;
            case 130106:
                return TacticalLines.VDR_LEVEL_67;
            case 130107:
                return TacticalLines.VDR_LEVEL_78;
            case 130108:
                return TacticalLines.VDR_LEVEL_89;
            case 130109:
                return TacticalLines.VDR_LEVEL_910;
            case 130201:
                return TacticalLines.BEACH_SLOPE_FLAT;
            case 130202:
                return TacticalLines.BEACH_SLOPE_GENTLE;
            case 130203:
                return TacticalLines.BEACH_SLOPE_MODERATE;
            case 130204:
                return TacticalLines.BEACH_SLOPE_STEEP;
            case 140101:
                return TacticalLines.SOLID_ROCK;
            case 140102:
                return TacticalLines.CLAY;
            case 140103:
                return TacticalLines.VERY_COARSE_SAND;
            case 140104:
                return TacticalLines.COARSE_SAND;
            case 140105:
                return TacticalLines.MEDIUM_SAND;
            case 140106:
                return TacticalLines.FINE_SAND;
            case 140107:
                return TacticalLines.VERY_FINE_SAND;
            case 140108:
                return TacticalLines.VERY_FINE_SILT;
            case 140109:
                return TacticalLines.FINE_SILT;
            case 140110:
                return TacticalLines.MEDIUM_SILT;
            case 140111:
                return TacticalLines.COARSE_SILT;
            case 140112:
                return TacticalLines.BOULDERS;
            case 140113:
                return TacticalLines.OYSTER_SHELLS;
            case 140114:
                return TacticalLines.PEBBLES;
            case 140115:
                return TacticalLines.SAND_AND_SHELLS;
            case 140116:
                return TacticalLines.BOTTOM_SEDIMENTS_LAND;
            case 140117:
                return TacticalLines.BOTTOM_SEDIMENTS_NO_DATA;
            case 140118:
                return TacticalLines.BOTTOM_ROUGHNESS_SMOOTH;
            case 140119:
                return TacticalLines.BOTTOM_ROUGHNESS_MODERATE;
            case 140120:
                return TacticalLines.BOTTOM_ROUGHNESS_ROUGH;
            case 140121:
                return TacticalLines.CLUTTER_LOW;
            case 140122:
                return TacticalLines.CLUTTER_MEDIUM;
            case 140123:
                return TacticalLines.CLUTTER_HIGH;
            case 140124:
                return TacticalLines.IMPACT_BURIAL_0;
            case 140125:
                return TacticalLines.IMPACT_BURIAL_10;
            case 140126:
                return TacticalLines.IMPACT_BURIAL_20;
            case 140127:
                return TacticalLines.IMPACT_BURIAL_75;
            case 140128:
                return TacticalLines.IMPACT_BURIAL_100;
            case 140129:
                return TacticalLines.BOTTOM_CATEGORY_A;
            case 140130:
                return TacticalLines.BOTTOM_CATEGORY_B;
            case 140131:
                return TacticalLines.BOTTOM_CATEGORY_C;
            case 140132:
                return TacticalLines.BOTTOM_TYPE_A1;
            case 140133:
                return TacticalLines.BOTTOM_TYPE_A2;
            case 140134:
                return TacticalLines.BOTTOM_TYPE_A3;
            case 140135:
                return TacticalLines.BOTTOM_TYPE_B1;
            case 140136:
                return TacticalLines.BOTTOM_TYPE_B2;
            case 140137:
                return TacticalLines.BOTTOM_TYPE_B3;
            case 140138:
                return TacticalLines.BOTTOM_TYPE_C1;
            case 140139:
                return TacticalLines.BOTTOM_TYPE_C2;
            case 140140:
                return TacticalLines.BOTTOM_TYPE_C3;
            
            case 150100:
                return TacticalLines.MARITIME_LIMIT;
            case 150200:
                return TacticalLines.MARITIME_AREA;
            case 150300:
                return TacticalLines.RESTRICTED_AREA;
            case 150400:
                return TacticalLines.SWEPT_AREA;
            case 150500:
                return TacticalLines.TRAINING_AREA;
            case 150600:
                return TacticalLines.OPERATOR_DEFINED;
            case 160100:
                return TacticalLines.CABLE;
            case 160200:
                return TacticalLines.SUBMERGED_CRIB;
            case 160300:
                return TacticalLines.CANAL;
            case 160700:
                return TacticalLines.OIL_RIG_FIELD;
            case 160800:
                return TacticalLines.PIPE;
                
            default:
                return -1;
        }
        return -1;
    }
    /**
     * @param symbolID Mil-Standard 2525 20-30 digit code
     * @return the line type as an integer if it is a weather symbol, else return -1
     */
    public static int IsWeather(String symbolID) {
        //the MeTOCs
        try
        {
            if(symbolID==null)
                return -1;

            if(symbolID.length()>15)
            {
                int symbolSet = SymbolID.getSymbolSet(symbolID);
                int entityCode = SymbolID.getEntityCode(symbolID);
                int version = SymbolID.getVersion(symbolID);

                switch(symbolSet)
                {
                    case 45:
                    case 46:
                        return getWeatherLinetype(version,entityCode);
                }
            }
        }
        catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsMETOC.IsWeather");
               ErrorLogger.LogException(_className ,"isWeather",
                    new RendererException("Failed inside isWeather", exc));
        }
        return -1;
    }
/**
 * Sets tactical graphic properties based on Mil-Std-2525 Appendix C.
 * @param tg
 */
    private static void SetMeTOCProperties(TGLight tg) {
        try
        {
            //METOC's have no user defined fills
            //any fills per Mil-Std-2525 will be set below
            //tg.set_FillColor(null);
            String symbolId=tg.get_SymbolId();
            switch (tg.get_LineType()) {   //255:150:150                    
                case TacticalLines.SQUALL:
                    tg.set_LineColor(Color.BLACK);
                    tg.set_lineCap(BasicStroke.CAP_BUTT);
                    break;
                case TacticalLines.TROUGH:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(Color.BLACK);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    break;
                case TacticalLines.UPPER_TROUGH:
                    tg.set_LineColor(Color.BLACK);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    break;
                case TacticalLines.BOTTOM_TYPE_A1:
                    tg.set_LineColor(new Color(48, 255, 0));   // green
                    tg.set_FillColor(new Color(48, 255, 0));
                    break;
                case TacticalLines.BOTTOM_TYPE_A2:
                    tg.set_LineColor(new Color(127, 255, 0));   //light green
                    tg.set_FillColor(new Color(127, 255, 0));
                    break;
                case TacticalLines.BOTTOM_TYPE_C2:
                    tg.set_LineColor(new Color(255, 80, 0));   //dark orange
                    tg.set_FillColor(new Color(255, 80, 0));
                    break;
                case TacticalLines.BOTTOM_TYPE_C3:
                    tg.set_LineColor(new Color(255, 48, 0));   //orange red
                    tg.set_FillColor(new Color(255, 48, 0));
                    break;
                case TacticalLines.IMPACT_BURIAL_0:
                    tg.set_LineColor(new Color(0, 0, 255));   //blue
                    tg.set_FillColor(new Color(0, 0, 255));
                    break;
                case TacticalLines.BOTTOM_TYPE_C1:
                case TacticalLines.IMPACT_BURIAL_75:
                    tg.set_LineColor(new Color(255, 127, 0));   //orange
                    tg.set_FillColor(new Color(255, 127, 0));
                    break;
                case TacticalLines.BOTTOM_CATEGORY_C:
                case TacticalLines.IMPACT_BURIAL_100:
                case TacticalLines.CLUTTER_HIGH:
                case TacticalLines.BOTTOM_ROUGHNESS_ROUGH:
                    tg.set_LineColor(new Color(255, 0, 0));   //red
                    tg.set_FillColor(new Color(255, 0, 0));
                    break;
                case TacticalLines.BOTTOM_TYPE_B2:
                case TacticalLines.BOTTOM_CATEGORY_B:
                case TacticalLines.IMPACT_BURIAL_20:
                case TacticalLines.CLUTTER_MEDIUM:
                case TacticalLines.BOTTOM_ROUGHNESS_MODERATE:
                    tg.set_LineColor(new Color(255, 255, 0));   //yellow
                    tg.set_FillColor(new Color(255, 255, 0));
                    break;
                case TacticalLines.BOTTOM_CATEGORY_A:
                case TacticalLines.IMPACT_BURIAL_10:
                case TacticalLines.CLUTTER_LOW:
                case TacticalLines.BOTTOM_ROUGHNESS_SMOOTH:
                    tg.set_LineColor(new Color(0, 255, 0));   //green
                    tg.set_FillColor(new Color(0, 255, 0));
                    break;
                case TacticalLines.BOTTOM_SEDIMENTS_NO_DATA:
                    tg.set_LineColor(new Color(230, 230, 230));   //light gray
                    tg.set_FillColor(new Color(230, 230, 230));
                    break;
                case TacticalLines.BOTTOM_SEDIMENTS_LAND:
                    tg.set_LineColor(new Color(220, 220, 220));   //gray
                    tg.set_FillColor(new Color(220, 220, 220));
                    break;
                case TacticalLines.SAND_AND_SHELLS:
                    tg.set_LineColor(new Color(255, 220, 220));   //light peach
                    tg.set_FillColor(new Color(255, 220, 220));
                    break;
                case TacticalLines.PEBBLES:
                    tg.set_LineColor(new Color(255, 190, 190));   //peach
                    tg.set_FillColor(new Color(255, 190, 190));
                    break;
                case TacticalLines.OYSTER_SHELLS:
                    tg.set_LineColor(new Color(255, 150, 150));   //dark peach
                    tg.set_FillColor(new Color(255, 150, 150));
                    break;
                case TacticalLines.BOULDERS:
                    tg.set_LineColor(new Color(255, 0, 0));
                    tg.set_FillColor(new Color(255, 0, 0));
                    break;
                case TacticalLines.COARSE_SILT:
                    tg.set_LineColor(new Color(200, 255, 105));
                    tg.set_FillColor(new Color(200, 255, 105));
                    break;
                case TacticalLines.MEDIUM_SILT:
                    tg.set_LineColor(new Color(0, 255, 0));     //green
                    tg.set_FillColor(new Color(0, 255, 0));
                    break;
                case TacticalLines.FINE_SILT:
                    tg.set_LineColor(new Color(25, 255, 230));     //turquoise
                    tg.set_FillColor(new Color(25, 255, 230));
                    break;
                case TacticalLines.VERY_FINE_SILT:
                    tg.set_LineColor(new Color(0, 215, 255));     //turquoise
                    tg.set_FillColor(new Color(0, 215, 255));
                    break;
                case TacticalLines.VERY_FINE_SAND:
                    tg.set_LineColor(new Color(255, 255, 220));     //pale yellow
                    tg.set_FillColor(new Color(255, 255, 220));
                    break;
                case TacticalLines.FINE_SAND:
                    tg.set_LineColor(new Color(255, 255, 140));     //light yellow
                    tg.set_FillColor(new Color(255, 255, 140));
                    break;
                case TacticalLines.MEDIUM_SAND:
                    tg.set_LineColor(new Color(255, 235, 0));     //yellow
                    tg.set_FillColor(new Color(255, 235, 0));
                    break;
                case TacticalLines.COARSE_SAND:
                    tg.set_LineColor(new Color(255, 215, 0));     //light gold
                    tg.set_FillColor(new Color(255, 215, 0));
                    break;
                case TacticalLines.BOTTOM_TYPE_B3:
                    tg.set_LineColor(new Color(255, 207, 0));     //gold
                    tg.set_FillColor(new Color(255, 207, 0));
                    break;
                case TacticalLines.VERY_COARSE_SAND:
                    tg.set_LineColor(new Color(255, 180, 0));     //gold
                    tg.set_FillColor(new Color(255, 180, 0));
                    break;
                case TacticalLines.CLAY:
                    tg.set_LineColor(new Color(100, 130, 255));     //periwinkle
                    tg.set_FillColor(new Color(100, 130, 255));
                    break;
                case TacticalLines.SOLID_ROCK:
                    //tg.set_LineColor(new Color(160, 32, 240));     //purple
                    //tg.set_FillColor(new Color(160, 32, 240));
                    tg.set_LineColor(new Color(255, 0, 255));     //magenta
                    tg.set_FillColor(new Color(255, 0, 255));
                    break;
                case TacticalLines.VDR_LEVEL_12:
                    tg.set_LineColor(new Color(26, 153, 77));     //dark green
                    tg.set_FillColor(new Color(26, 153, 77));
                    break;
                case TacticalLines.VDR_LEVEL_23:
                    tg.set_LineColor(new Color(26, 204, 77));     //light green
                    tg.set_FillColor(new Color(26, 204, 77));
                    break;
                case TacticalLines.BOTTOM_TYPE_A3:
                    tg.set_LineColor(new Color(175, 255, 0));    //lime green
                    tg.set_FillColor(new Color(175, 255, 0));
                    break;
                case TacticalLines.VDR_LEVEL_34:
                    tg.set_LineColor(new Color(128, 255, 51));    //lime green
                    tg.set_FillColor(new Color(128, 255, 51));
                    break;
                case TacticalLines.BOTTOM_TYPE_B1:
                    tg.set_LineColor(new Color(207, 255, 0));    //yellow green
                    tg.set_FillColor(new Color(207, 255, 0));
                    break;
                case TacticalLines.VDR_LEVEL_45:
                    tg.set_LineColor(new Color(204, 255, 26));    //yellow green
                    tg.set_FillColor(new Color(204, 255, 26));
                    break;
                case TacticalLines.VDR_LEVEL_56:
                    tg.set_LineColor(new Color(255, 255, 0));     //yellow
                    tg.set_FillColor(new Color(255, 255, 0));
                    break;
                case TacticalLines.VDR_LEVEL_67:
                    tg.set_LineColor(new Color(255, 204, 0));     //gold
                    tg.set_FillColor(new Color(255, 204, 0));
                    break;
                case TacticalLines.VDR_LEVEL_78:
                    tg.set_LineColor(new Color(255, 128, 0));     //light orange
                    tg.set_FillColor(new Color(255, 128, 0));
                    break;
                case TacticalLines.VDR_LEVEL_89:
                    tg.set_LineColor(new Color(255, 77, 0));      //dark orange
                    tg.set_FillColor(new Color(255, 77, 0));
                    break;
                case TacticalLines.VDR_LEVEL_910:
                    tg.set_LineColor(Color.RED);
                    tg.set_FillColor(Color.RED);
                    break;
                case TacticalLines.CANAL:
                    tg.set_LineColor(Color.BLACK);
                    tg.set_LineThickness(2 * tg.get_LineThickness()); // Thick line
                    break;
                case TacticalLines.OPERATOR_DEFINED:
                    tg.set_LineColor(new Color(255, 128, 0));
                    break;
                case TacticalLines.MARITIME_LIMIT:
                case TacticalLines.MARITIME_AREA:
                    tg.set_LineColor(Color.MAGENTA);
                    tg.set_LineStyle(1);
                    break;
                case TacticalLines.PERCHES:
                case TacticalLines.SUBMERGED_CRIB:
                    tg.set_LineColor(Color.BLACK);
                    tg.set_LineStyle(2);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    tg.set_FillColor(Color.BLUE);
                    break;
                case TacticalLines.DISCOLORED_WATER:
                case TacticalLines.UNDERWATER_HAZARD:
                    tg.set_LineColor(Color.BLACK);
                    tg.set_LineStyle(2);
                    tg.set_FillColor(new Color(0, 191, 255)); //deep sky blue
                    break;
                case TacticalLines.LOADING_FACILITY_AREA:
                    tg.set_LineColor(new Color(210, 180, 140));
                    tg.set_FillColor(new Color(210, 180, 140));
                    break;
                case TacticalLines.LOADING_FACILITY_LINE:
                    tg.set_LineColor(Color.GRAY);
                    tg.set_LineThickness(2 * tg.get_LineThickness()); // Thick line
                    break;
                case TacticalLines.DRYDOCK:
                    tg.set_LineColor(Color.BLACK);
                    //tg.set_FillColor(new Color(165, 42, 42)); //brown
                    tg.set_FillColor(new Color(205, 133, 63)); //brown
                    tg.set_LineStyle(1);
                    break;
                case TacticalLines.FORESHORE_AREA:
                    //tg.set_LineColor(new Color(154, 205, 50));
                    //tg.set_FillColor(new Color(154, 205, 50));
                    tg.set_LineColor(new Color(173, 255, 47));
                    tg.set_FillColor(new Color(173, 255, 47));
                    break;
                case TacticalLines.FORESHORE_LINE:
                    //tg.set_LineColor(new Color(154, 205, 50));
                    tg.set_LineColor(new Color(173, 255, 47));
                    break;
                case TacticalLines.RESTRICTED_AREA:
                case TacticalLines.TRAINING_AREA:
                case TacticalLines.ANCHORAGE_LINE:
                case TacticalLines.ANCHORAGE_AREA:
                    tg.set_LineColor(Color.MAGENTA);
                    //tg.set_LineStyle(1);    //dashed
                    break;
                case TacticalLines.PIPE:
                    tg.set_LineColor(Color.GRAY);
                    tg.set_FillColor(Color.GRAY);
                    break;
                case TacticalLines.WATER:
                    tg.set_LineColor(Color.WHITE);
                    tg.set_FillColor(Color.WHITE);
                    break;
                case TacticalLines.FISH_TRAPS:
                    tg.set_LineColor(new Color(192,192,192));
                    tg.set_LineStyle(1);
                    break;
                case TacticalLines.SWEPT_AREA:
                case TacticalLines.OIL_RIG_FIELD:
                case TacticalLines.FOUL_GROUND:
                case TacticalLines.KELP:
                    tg.set_LineColor(null);
                    break;
                case TacticalLines.BEACH:
                    tg.set_LineColor(new Color(206, 158, 140));
                    tg.set_FillColor(new Color(206, 158, 140, (int) (255 * 0.12)));
                    break;
                case TacticalLines.DEPTH_AREA:
                    tg.set_LineColor(Color.BLUE);
                    tg.set_FillColor(Color.WHITE);
                    break;
                case TacticalLines.CONVERGENCE:
                case TacticalLines.ITC:
                    tg.set_LineColor(new Color(255, 128, 0));
                    tg.set_lineCap(BasicStroke.CAP_BUTT);
                    break;
                case TacticalLines.OFY:
                case TacticalLines.OCCLUDED:
                    tg.set_LineColor(new Color(160, 32, 240));
                    tg.set_FillColor(new Color(160, 32, 240));
                    break;
                case TacticalLines.UOF:
                    tg.set_LineColor(new Color(160, 32, 240));
                    break;
                case TacticalLines.WFY:
                case TacticalLines.WFG:
                case TacticalLines.WF:
                    tg.set_FillColor(Color.RED);
                    tg.set_LineColor(Color.RED);
                    break;
                case TacticalLines.UWF:
                case TacticalLines.IFR:
                    tg.set_LineColor(Color.RED);
                    break;
                case TacticalLines.CFG:
                case TacticalLines.CFY:
                case TacticalLines.CF:
                    tg.set_LineColor(Color.BLUE);
                    tg.set_FillColor(Color.BLUE);
                    break;
                case TacticalLines.UCF:
                case TacticalLines.MVFR:
                    tg.set_LineColor(Color.BLUE);
                    break;
                case TacticalLines.TURBULENCE:
                    tg.set_LineColor(Color.BLUE);
                    tg.set_LineStyle(2);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    int minThickness = Math.max(RendererSettings.getInstance().getDeviceDPI() / 96, 1) * 6;
                    if (tg.get_LineThickness() < minThickness)
                        tg.set_LineThickness(minThickness);
                    break;
                case TacticalLines.CABLE:
                    tg.set_LineColor(Color.MAGENTA);
                    break;
                case TacticalLines.ISLAND:
                    //tg.set_LineColor(new Color(165, 42, 42)); //brown
                    //tg.set_FillColor(new Color(165, 42, 42)); //brown
                    tg.set_LineColor(new Color(210, 180, 140)); //tan
                    tg.set_FillColor(new Color(210, 180, 140)); //tan
                    break;
                case TacticalLines.SEAWALL:
                case TacticalLines.SEAWALL_GE:
                case TacticalLines.FLOOD_TIDE:
                case TacticalLines.FLOOD_TIDE_GE:
                case TacticalLines.EBB_TIDE:
                case TacticalLines.EBB_TIDE_GE:
                case TacticalLines.JETTY_ABOVE_WATER:
                case TacticalLines.JETTY_ABOVE_WATER_GE:
                    tg.set_LineColor(Color.GRAY);
                    break;
                case TacticalLines.BEACH_SLOPE_MODERATE:
                case TacticalLines.BEACH_SLOPE_FLAT:
                    tg.set_LineColor(new Color(179, 179, 179));
                    tg.set_FillColor(null);
                    break;
                case TacticalLines.BEACH_SLOPE_GENTLE:
                case TacticalLines.BEACH_SLOPE_STEEP:
                    tg.set_LineColor(new Color(128, 128, 128));
                    tg.set_FillColor(null);
                    break;
                case TacticalLines.BREAKERS:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(Color.GRAY);
                    break;
                case TacticalLines.JETTY_BELOW_WATER:
                case TacticalLines.JETTY_BELOW_WATER_GE:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(Color.GRAY);
                    break;
                case TacticalLines.DEPTH_CURVE:
                case TacticalLines.DEPTH_CURVE_GE:
                case TacticalLines.DEPTH_CONTOUR:
                case TacticalLines.DEPTH_CONTOUR_GE:
                case TacticalLines.COASTLINE:
                case TacticalLines.COASTLINE_GE:
                case TacticalLines.PIER:
                case TacticalLines.PIER_GE:
                    tg.set_LineColor(Color.GRAY);
                    break;
                case TacticalLines.FROZEN:
                case TacticalLines.JET:
                case TacticalLines.JET_GE:
                    tg.set_LineColor(Color.RED);
                    break;
                case TacticalLines.THUNDERSTORMS:
                    tg.set_LineColor(Color.RED);
                    tg.set_LineStyle(3);
                    break;
                case TacticalLines.RAMP_BELOW_WATER:
                case TacticalLines.RAMP_BELOW_WATER_GE:
                case TacticalLines.ESTIMATED_ICE_EDGE:
                case TacticalLines.ESTIMATED_ICE_EDGE_GE:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(Color.BLACK);
                    break;
                case TacticalLines.ISODROSOTHERM:
                case TacticalLines.ISODROSOTHERM_GE:
                    tg.set_LineColor(Color.GREEN);
                    break;
                case TacticalLines.LRO:
                case TacticalLines.UNDERCAST:
                case TacticalLines.LVO:
                case TacticalLines.RIDGE:
                //case TacticalLines.TROUGH:
                case TacticalLines.ICE_OPENINGS_LEAD:
                case TacticalLines.ICE_OPENINGS_LEAD_GE:
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.ICE_OPENINGS_FROZEN_GE:
                case TacticalLines.LEADING_LINE:
                case TacticalLines.STREAM:
                case TacticalLines.STREAM_GE:
                case TacticalLines.CRACKS:
                case TacticalLines.CRACKS_GE:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION_GE:
                case TacticalLines.ISOBAR:
                case TacticalLines.ISOBAR_GE:
                case TacticalLines.UPPER_AIR:
                case TacticalLines.UPPER_AIR_GE:
                case TacticalLines.ICE_EDGE:
                case TacticalLines.ICE_EDGE_GE:
                case TacticalLines.ICE_EDGE_RADAR:
                case TacticalLines.ICE_EDGE_RADAR_GE:
                case TacticalLines.REEF:
                    tg.set_LineColor(Color.BLACK);
                    break;
                case TacticalLines.INSTABILITY:
                    tg.set_LineStyle(4);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    tg.set_LineColor(Color.BLACK);
                    break;
                case TacticalLines.SHEAR:
                    tg.set_LineStyle(3);
                    tg.set_lineCap(BasicStroke.CAP_ROUND);
                    tg.set_LineColor(Color.BLACK);
                    break;
                case TacticalLines.ISOPLETHS:
                case TacticalLines.ISOPLETHS_GE:
                case TacticalLines.ISOTHERM:
                case TacticalLines.ISOTHERM_GE:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(Color.RED);
                    break;
                case TacticalLines.ISOTACH:
                case TacticalLines.ISOTACH_GE:
                    tg.set_LineStyle(1);
                    tg.set_LineColor(new Color(160, 32, 240));
                    break;
                case TacticalLines.SAND:
                    tg.set_LineColor(new Color(165, 121, 82)); //brown
                    break;
                case TacticalLines.ICING:
                    tg.set_LineColor(new Color(189, 154, 56)); //brown
                    break;
                case TacticalLines.NON_CONVECTIVE:
                    tg.set_LineColor(Color.GREEN);
                    break;
                case TacticalLines.CONVECTIVE:
                    tg.set_LineColor(Color.GREEN);
                    tg.set_LineStyle(3);
                    break;
                case TacticalLines.FOG:
                    tg.set_LineColor(Color.YELLOW);
                    break;
                case TacticalLines.RAMP_ABOVE_WATER:
                case TacticalLines.RAMP_ABOVE_WATER_GE:
                    tg.set_LineColor(Color.BLACK);
                    break;
                default:
                    break;
            }
        }
        catch (Exception exc) {
            //clsUtility.WriteFile("Error in clsMETOC.SetMeTOCProperties");
               ErrorLogger.LogException(_className ,"SetMeTOCProperties",
                    new RendererException("Failed inside SetMeTOCProperties", exc));
        }
    }

    /**
     *
     * Rotates axis by theta for point and curve
     * Finds next closest point with same x position on the splinePoints curve as pt
     * walks up the curve and if it does not find a range that straddles x it return null.
     * We ultimately will draw a line from pt to the extrapolated point on the splinePoints spline.
     * used for ICE_OPENINGS_FROZEN_LEAD
     *
     * @param splinePoints - the points on the opposite spline
     * @param pt - the point in the original curve from which the line will start
     * @param theta angle of curve at pt. Perpendicular to new line to be drawn
     *
     * @return The extrapolated point on the opposite spline to which the line will be drawn
     */
    private static POINT2 ExtrapolatePointFromCurve(ArrayList<POINT2> splinePoints,
                                                    POINT2 pt, double theta) {
        try {
            // cos(theta) and sin(theta) only need to be calculated once
            final double cosTheta = Math.cos(theta);
            final double sinTheta = Math.sin(theta);

            // p at the end of variable name represents "prime" and means it's a rotated coordinate
            double xp = pt.x * cosTheta + pt.y * sinTheta;

            //if we find a pair which straddle xp then extrapolate the y value from the curve and
            //return the point
            for (int j = 0; j < splinePoints.size() - 1; j++) {
                double x1p = splinePoints.get(j).x * cosTheta + splinePoints.get(j).y * sinTheta;
                double x2p = splinePoints.get(j + 1).x * cosTheta + splinePoints.get(j + 1).y * sinTheta;
                if ((x1p <= xp && x2p >= xp) || (x1p >= xp && x2p <= xp)) {
                    double y1p = -splinePoints.get(j).x * sinTheta + splinePoints.get(j).y * cosTheta;
                    double y2p = -splinePoints.get(j + 1).x * sinTheta + splinePoints.get(j + 1).y * cosTheta;

                    double mp = (y2p - y1p) / (x2p - x1p); // slope
                    double yp = y1p + (xp - x1p) * mp;

                    // Rotate back to normal coordinates
                    double x = xp * cosTheta - yp * sinTheta;
                    double y = xp * sinTheta + yp * cosTheta;
                    return new POINT2(x, y);
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ExtrapolatePointFromCurve",
                    new RendererException("Failed inside ExtrapolatePointFromCurve", exc));
        }
        return null;
    }
    /**
     * The public interface, main function to return METOC shapes
     * @param tg the tactical graphic
     * @param shapes the ShapeInfo array
     */
    public static void GetMeTOCShape(TGLight tg, 
            ArrayList<Shape2> shapes) {
        try
        {
            if(shapes==null)
                return;
            GeneralPath lineObject = null;
            GeneralPath lineObject2 = null;
            ArrayList<POINT2> splinePoints = new ArrayList();
            ArrayList<POINT2> splinePoints2 = new ArrayList();
            double d = 0;
            int j = 0, k=0, l=0;
            Shape2 shape=null;
            POINT2 ptLast = tg.Pixels.get(tg.Pixels.size() - 1);
            ArrayList<POINT2> twoSplines = null;
            ArrayList<POINT2> upperSpline = null;
            ArrayList<POINT2> lowerSpline = null;
            ArrayList<POINT2> originalPixels = null;
            int t=0,u=0,v=0,w=0,tt=0,uu=0,vv=0,ww=0;
            
            ArrayList<POINT2>pixels=null;
            originalPixels=null;
            ArrayList<P1>partitions=null;
            SetMeTOCProperties(tg);
            switch (tg.get_LineType()) {
                case TacticalLines.SF:
                case TacticalLines.USF:
                case TacticalLines.SFG:
                case TacticalLines.SFY:
                case TacticalLines.WFY:
                case TacticalLines.WFG:
                case TacticalLines.WF:
                case TacticalLines.UWF:
                case TacticalLines.UCF:
                case TacticalLines.CF:
                case TacticalLines.CFG:
                case TacticalLines.CFY:
                case TacticalLines.OCCLUDED:
                case TacticalLines.UOF:
                case TacticalLines.OFY:
                case TacticalLines.TROUGH:
                case TacticalLines.UPPER_TROUGH:
                case TacticalLines.CABLE:
                case TacticalLines.INSTABILITY:
                case TacticalLines.SHEAR:
                case TacticalLines.RIDGE:
                case TacticalLines.SQUALL:
                case TacticalLines.ITC:
                case TacticalLines.CONVERGENCE:
                case TacticalLines.ITD:
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
                case TacticalLines.OPERATOR_FREEFORM:
                case TacticalLines.LVO:
                case TacticalLines.UNDERCAST:
                case TacticalLines.LRO:
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
                case TacticalLines.ANCHORAGE_LINE:
                case TacticalLines.PIPE:
                case TacticalLines.TRAINING_AREA:
                case TacticalLines.RESTRICTED_AREA:
                case TacticalLines.REEF:
                case TacticalLines.FORESHORE_AREA:
                case TacticalLines.FORESHORE_LINE:
                case TacticalLines.DRYDOCK:
                case TacticalLines.LOADING_FACILITY_LINE:
                case TacticalLines.LOADING_FACILITY_AREA:
                case TacticalLines.PERCHES:
                case TacticalLines.UNDERWATER_HAZARD:
                case TacticalLines.BREAKERS:
                case TacticalLines.DISCOLORED_WATER:
                case TacticalLines.BEACH_SLOPE_FLAT:
                case TacticalLines.BEACH_SLOPE_GENTLE:
                case TacticalLines.MARITIME_LIMIT:
                case TacticalLines.MARITIME_AREA:
                case TacticalLines.OPERATOR_DEFINED:
                case TacticalLines.SUBMERGED_CRIB:
                case TacticalLines.CANAL:
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
                    arraysupport.GetLineArray2(tg, tg.Pixels, shapes,null,null);
                    break;
                case TacticalLines.ISOBAR:
                case TacticalLines.ISOBAR_GE:
                case TacticalLines.UPPER_AIR:
                case TacticalLines.UPPER_AIR_GE:
                case TacticalLines.ISOTHERM:
                case TacticalLines.ISOTHERM_GE:
                case TacticalLines.ISOTACH:
                case TacticalLines.ISOTACH_GE:
                case TacticalLines.ISODROSOTHERM:
                case TacticalLines.ISODROSOTHERM_GE:
                case TacticalLines.ISOPLETHS:
                case TacticalLines.ISOPLETHS_GE:
                case TacticalLines.ICE_EDGE:
                case TacticalLines.ICE_EDGE_GE:
                case TacticalLines.ESTIMATED_ICE_EDGE:
                case TacticalLines.ESTIMATED_ICE_EDGE_GE:
                case TacticalLines.CRACKS:
                case TacticalLines.CRACKS_GE:
                case TacticalLines.DEPTH_CURVE:
                case TacticalLines.DEPTH_CURVE_GE:
                case TacticalLines.DEPTH_CONTOUR:
                case TacticalLines.DEPTH_CONTOUR_GE:
                case TacticalLines.COASTLINE:
                case TacticalLines.COASTLINE_GE:
                case TacticalLines.PIER:
                case TacticalLines.PIER_GE:
                case TacticalLines.RAMP_ABOVE_WATER:
                case TacticalLines.RAMP_ABOVE_WATER_GE:
                case TacticalLines.RAMP_BELOW_WATER:
                case TacticalLines.RAMP_BELOW_WATER_GE:
                case TacticalLines.JETTY_ABOVE_WATER:
                case TacticalLines.JETTY_ABOVE_WATER_GE:
                case TacticalLines.JETTY_BELOW_WATER:
                case TacticalLines.JETTY_BELOW_WATER_GE:
                case TacticalLines.SEAWALL:
                case TacticalLines.SEAWALL_GE:
                case TacticalLines.EBB_TIDE:
                case TacticalLines.FLOOD_TIDE:
                case TacticalLines.EBB_TIDE_GE:
                case TacticalLines.FLOOD_TIDE_GE:
                case TacticalLines.JET:
                case TacticalLines.STREAM:
                case TacticalLines.JET_GE:
                case TacticalLines.STREAM_GE:
                    lineObject2 = DrawSplines(tg, splinePoints);
                    lineObject2.lineTo(ptLast.x, ptLast.y);
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setShape(lineObject2);
                    shapes.add(shape);
                    break;
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION_GE:
                case TacticalLines.ICE_EDGE_RADAR:
                case TacticalLines.ICE_EDGE_RADAR_GE:
                    lineObject2 = DrawSplines(tg, splinePoints);
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setShape(lineObject2);
                    shapes.add(shape);
                    break;
                case TacticalLines.ICE_OPENINGS_LEAD:
                    originalPixels=tg.Pixels;
                    partitions=clsChannelUtility.GetPartitions2(tg);
                    v=partitions.size();
                    //for(l=0;l<partitions.size();l++)
                    for(l=0;l<v;l++)
                    {
                        tg.Pixels=originalPixels;
                        pixels=new ArrayList();
                        for(k=partitions.get(l).start;k<=partitions.get(l).end_Renamed+1;k++)
                            pixels.add(tg.Pixels.get(k));
                        
                        if(pixels==null || pixels.isEmpty())
                            continue;

                        twoSplines = new ArrayList();
                        twoSplines = ParallelLines2(pixels, (int) arraysupport.getScaledSize(20, tg.get_LineThickness()));

                        upperSpline = new ArrayList();
                        lowerSpline = new ArrayList();
                        w=twoSplines.size();
                        //for (j = 0; j < twoSplines.size() / 2; j++) 
                        for (j = 0; j < w / 2; j++) 
                        {
                            upperSpline.add(twoSplines.get(j));
                        }

                        //for (j = twoSplines.size() / 2; j < twoSplines.size(); j++) 
                        for (j = w / 2; j < w; j++) 
                        {
                            lowerSpline.add(twoSplines.get(j));
                        }

                        tg.Pixels = lowerSpline;
                        lineObject2 = DrawSplines(tg, splinePoints);
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);

                        tg.Pixels = upperSpline;
                        lineObject2 = DrawSplines(tg, splinePoints);
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);
                    }
                    break;
                case TacticalLines.ICE_OPENINGS_LEAD_GE:
                    originalPixels=tg.Pixels;
                    partitions=clsChannelUtility.GetPartitions2(tg);
                    t=partitions.size();
                    //for(l=0;l<partitions.size();l++)
                    for(l=0;l<t;l++)
                    {
                        tg.Pixels=originalPixels;
                        pixels=new ArrayList();
                        for(k=partitions.get(l).start;k<=partitions.get(l).end_Renamed+1;k++)
                            pixels.add(tg.Pixels.get(k));

                        if(pixels==null || pixels.isEmpty())
                            continue;

                        twoSplines = new ArrayList();
                        twoSplines = ParallelLines2(pixels, (int) arraysupport.getScaledSize(20, tg.get_LineThickness()));

                        upperSpline = new ArrayList();
                        lowerSpline = new ArrayList();
                        u=twoSplines.size();
                        //for (j = 0; j < twoSplines.size() / 2; j++) 
                        for (j = 0; j < u / 2; j++) 
                        {
                            upperSpline.add(twoSplines.get(j));
                        }

                        //for (j = twoSplines.size() / 2; j < twoSplines.size(); j++) 
                        for (j = u / 2; j < u; j++) 
                        {
                            lowerSpline.add(twoSplines.get(j));
                        }

                        tg.Pixels = lowerSpline;
                        lineObject2 = DrawSplines(tg, splinePoints);
                        ptLast=tg.Pixels.get(tg.Pixels.size()-1);
                        lineObject2.lineTo(ptLast.x, ptLast.y);
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);

                        tg.Pixels = upperSpline;
                        splinePoints=new ArrayList();
                        lineObject2 = DrawSplines(tg, splinePoints);
                        ptLast=tg.Pixels.get(tg.Pixels.size()-1);
                        lineObject2.lineTo(ptLast.x, ptLast.y);
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);
                    }
                    break;
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.ICE_OPENINGS_FROZEN_GE:
                    originalPixels = tg.Pixels;
                    partitions = clsChannelUtility.GetPartitions2(tg);
                    t = partitions.size();
                    //for(l=0;l<partitions.size();l++)
                    for (l = 0; l < t; l++) {
                        tg.Pixels = originalPixels;
                        pixels = new ArrayList<>();
                        for (k = partitions.get(l).start; k <= partitions.get(l).end_Renamed + 1; k++)
                            pixels.add(tg.Pixels.get(k));

                        if (pixels.isEmpty())
                            continue;

                        twoSplines = ParallelLines2(pixels, (int) arraysupport.getScaledSize(20, tg.get_LineThickness()));
                        upperSpline = new ArrayList<>();
                        lowerSpline = new ArrayList<>();
                        u = twoSplines.size();
                        //for (j = 0; j < twoSplines.size() / 2; j++)
                        for (j = 0; j < u / 2; j++) {
                            upperSpline.add(twoSplines.get(j));
                        }

                        //for (j = twoSplines.size() / 2; j < twoSplines.size(); j++)
                        for (j = u / 2; j < u; j++) {
                            lowerSpline.add(twoSplines.get(j));
                        }

                        tg.Pixels = lowerSpline;
                        if (tg.get_LineType() == TacticalLines.ICE_OPENINGS_FROZEN) {
                            lineObject2 = DrawSplines(tg, splinePoints);
                        } else {
                            ArrayList<POINT2> splinePoints3 = new ArrayList<>();
                            lineObject2 = DrawSplines(tg, splinePoints3);
                            splinePoints.addAll(splinePoints3);
                            ptLast = tg.Pixels.get(tg.Pixels.size() - 1);
                            lineObject2.lineTo(ptLast.x, ptLast.y);
                        }
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);

                        tg.Pixels = upperSpline;
                        if (tg.get_LineType() == TacticalLines.ICE_OPENINGS_FROZEN) {
                            lineObject2 = DrawSplines(tg, splinePoints2);
                        } else {
                            ArrayList<POINT2> splinePoints4 = new ArrayList<>();
                            lineObject2 = DrawSplines(tg, splinePoints4);
                            splinePoints2.addAll(splinePoints4);
                            ptLast = tg.Pixels.get(tg.Pixels.size() - 1);
                            lineObject2.lineTo(ptLast.x, ptLast.y);
                        }
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shapes.add(shape);

                        //parse upper and lower arrays to find the corresponding splines
                        ArrayList<ArrayList<POINT2>> splinePointsArrays = new ArrayList<>();
                        ArrayList<ArrayList<POINT2>> splinePoints2Arrays = new ArrayList<>();
                        ArrayList<POINT2> ptsArray = new ArrayList<>();
                        for (j = 0; j < splinePoints.size(); j++) {
                            if (splinePoints.get(j).style != 47) {
                                ptsArray.add(splinePoints.get(j));
                            } else {
                                splinePointsArrays.add(ptsArray);
                                ptsArray = new ArrayList<>();
                            }
                        }
                        for (j = 0; j < splinePoints2.size(); j++) {
                            if (splinePoints2.get(j).style != 47) {
                                ptsArray.add(splinePoints2.get(j));
                            } else {
                                splinePoints2Arrays.add(ptsArray);
                                ptsArray = new ArrayList<>();
                            }
                        }

                        lineObject = new GeneralPath();
                        for (j = 0; j < splinePointsArrays.size(); j++) {
                            //the lines to connect the extrapolated points
                            ArrayList<POINT2> array;
                            ArrayList<POINT2> array2;
                            if (splinePoints2Arrays.size() <= j)
                                break;
                            if (splinePointsArrays.size() >= splinePoints2Arrays.size()) {
                                array = splinePointsArrays.get(j);
                                array2 = splinePoints2Arrays.get(j);
                            } else {
                                array = splinePoints2Arrays.get(j);
                                array2 = splinePointsArrays.get(j);
                            }
                            //extrapolate against points in the shortest array
                            for (k = 0; k < array.size(); k++) {
                                double theta;
                                if (array.size() == 1) // Unable to find slope
                                    continue;
                                else if (k == 0)
                                    theta = Math.atan2(array.get(k + 1).y - array.get(k).y, array.get(k + 1).x - array.get(k).x);
                                else if (k == array.size() - 1)
                                    theta = Math.atan2(array.get(k).y - array.get(k - 1).y, array.get(k).x - array.get(k - 1).x);
                                else
                                    theta = Math.atan2(array.get(k + 1).y - array.get(k - 1).y, array.get(k + 1).x - array.get(k - 1).x);

                                POINT2 pt = array.get(k);
                                POINT2 pt2 = ExtrapolatePointFromCurve(array2, pt, theta);
                                //if we got a valid extrapolation point then draw the line
                                if (pt2 != null) {
                                    lineObject.moveTo(pt.x, pt.y);
                                    lineObject.lineTo(pt2.x, pt2.y);
                                }
                            }
                        }
                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject);
                        shapes.add(shape);
                    }
                    break;
                case TacticalLines.LEADING_LINE:
                    //the solid line
                    lineObject = DrawSplines(tg, splinePoints);
                    lineObject2 = new GeneralPath();
                    if(splinePoints.size()>0)
                        lineObject2.moveTo(splinePoints.get(0).x, splinePoints.get(0).y);
                    else
                    {
                        lineObject2.moveTo(tg.Pixels.get(0).x,tg.Pixels.get(0).y);
                        t=tg.Pixels.size();
                        //for(j=0;j<tg.Pixels.size();j++)
                        for(j=0;j<t;j++)
                            lineObject2.lineTo(tg.Pixels.get(j).x,tg.Pixels.get(j).y);

                        shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                        shape.setShape(lineObject2);
                        shape.set_Style(1);
                        shapes.add(shape);
                        return;
                    }

                    int n = splinePoints.size() / 2;
                    for (j = 1; j <= n; j++) {
                        if(splinePoints.size()>=j-1)
                            lineObject2.lineTo(splinePoints.get(j).x, splinePoints.get(j).y);
                    }
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setShape(lineObject2);
                    shapes.add(shape);

                    //the dashed line
                    lineObject2 = new GeneralPath();
                    lineObject2.moveTo(splinePoints.get(n).x, splinePoints.get(n).y);
                    u=splinePoints.size();
                    //for (j = n + 1; j < splinePoints.size(); j++) 
                    for (j = n + 1; j < u; j++) 
                    {
                        if(splinePoints.size()>=j-1)
                            lineObject2.lineTo(splinePoints.get(j).x, splinePoints.get(j).y);
                    }
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setShape(lineObject2);
                    shape.set_Style(1);
                    shapes.add(shape);
                    break;
                default:
                    break;
            }
            //add the last point
            if (tg.get_LineType() != TacticalLines.ICE_OPENINGS_LEAD &&
                    tg.get_LineType() != TacticalLines.ICE_OPENINGS_LEAD_GE &&
                    tg.get_LineType() != TacticalLines.ICE_OPENINGS_FROZEN &&
                    tg.get_LineType() != TacticalLines.ICE_OPENINGS_FROZEN_GE &&
                    tg.get_LineType() != TacticalLines.ICE_EDGE_RADAR)
            {
                if (splinePoints != null && splinePoints.size() > 0)
                {
                    lineObject2 = new GeneralPath();
                    lineObject2.moveTo(splinePoints.get(splinePoints.size() - 1).x, splinePoints.get(splinePoints.size() - 1).y);
                    lineObject2.lineTo(ptLast.x, ptLast.y);
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setShape(lineObject2);
                    shape.set_Style(0);
                    shapes.add(shape);
                }
            }
            SetShapeProperties(tg, shapes);
        } 
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"GetMeTOCShape",
                    new RendererException("Failed inside GetMeTOCShape", exc));
        }
    }
/**
 * Sets the shape properties based on the tacttical graphic properties and also based on shape
 * styles which may have been set by JavaLineArray
 * @param tg
 * @param shapes shapes array to set properties
 */
    protected static void SetShapeProperties(TGLight tg, ArrayList<Shape2> shapes) {
        try
        {
            if (shapes == null)
            {
                return;
            }
            switch(tg.get_LineType())
            {
                case TacticalLines.DEPTH_AREA:
                    return;
                default:
                    break;
            }

            int j = 0, n=0;
            Shape2 shape = null;
            BasicStroke stroke = null;
            BufferedImage bi2 = null;
            int lineThickness = tg.get_LineThickness();
            Rectangle2D.Double rect = null;
            TexturePaint tp = tg.get_TexturePaint();
            switch (tg.get_LineType()) {
                case TacticalLines.FISH_TRAPS:
                case TacticalLines.SWEPT_AREA:
                case TacticalLines.OIL_RIG_FIELD:
                case TacticalLines.FOUL_GROUND:
                case TacticalLines.KELP:
                case TacticalLines.BEACH_SLOPE_MODERATE:
                case TacticalLines.BEACH_SLOPE_STEEP:
                    bi2 = PatternFillRenderer.MakeMetocPatternFill(tg);
                    shape = shapes.get(0);
                    shape.setLineColor(tg.get_LineColor());
                    shape.setPatternFillImage(bi2);
                    if (bi2 != null) {
                        rect = new Rectangle2D.Double(0, 0, bi2.getWidth(), bi2.getHeight());
                        tp = new TexturePaint(bi2, rect);
                        shape.setTexturePaint(tp);
                    }
                    break;
                case TacticalLines.SF:
                case TacticalLines.USF:
                case TacticalLines.SFG:
                case TacticalLines.SFY:
                    n=shapes.size();
                    //for (j = 0; j < shapes.size(); j++) 
                    for (j = 0; j < n; j++) 
                    {
                        shape = shapes.get(j);
                        if (shape == null || shape.getShape() == null) {
                            continue;
                        }

                        shape.set_Style(tg.get_LineStyle());
                        stroke = clsUtility.getLineStroke(lineThickness, shape.get_Style(), tg.get_lineCap(), BasicStroke.JOIN_ROUND);
                        shape.setStroke(stroke);
                    }
                    return;
                default:
                    break;
            }

            int shapeType = -1;
            int lineType = tg.get_LineType();
            boolean isChange1Area = clsUtility.IsChange1Area(lineType);
            boolean isClosedPolygon = clsUtility.isClosedPolygon(lineType);
            n=shapes.size();
            //for (j = 0; j < shapes.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                shape = shapes.get(j);
                if (shape == null || shape.getShape() == null) {
                    continue;
                }

                if (shape.getShapeType() == Shape2.SHAPE_TYPE_FILL) {
                    shape.setFillColor(tg.get_FillColor());
                }

                //clsUtility.ResolveModifierShape(tg,shape);

                shapeType = shape.getShapeType();
                switch (tg.get_LineType()) {
                    case TacticalLines.SF:
                    case TacticalLines.USF:
                    case TacticalLines.SFG:
                    case TacticalLines.SFY:
                    case TacticalLines.ITD:
                        break;
                    case TacticalLines.LEADING_LINE:
                    case TacticalLines.TRAINING_AREA:
                        shape.setLineColor(tg.get_LineColor());
                        break;
                    default:
                        shape.setLineColor(tg.get_LineColor());
                        shape.set_Style(tg.get_LineStyle());
                        break;
                }

                if (isClosedPolygon || shapeType == Shape2.SHAPE_TYPE_FILL)
                {
                    switch(tg.get_LineType())//these have fill instead of TexturePaint
                    {
                        case TacticalLines.FORESHORE_AREA:
                        case TacticalLines.WATER:
                        case TacticalLines.BEACH:
                        case TacticalLines.ISLAND:
                        case TacticalLines.DRYDOCK:
                        case TacticalLines.LOADING_FACILITY_AREA:
                        case TacticalLines.PERCHES:
                        case TacticalLines.UNDERWATER_HAZARD:
                        case TacticalLines.DISCOLORED_WATER:
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
                        case TacticalLines.FINE_SAND:
                        case TacticalLines.MEDIUM_SAND:
                        case TacticalLines.COARSE_SAND:
                        case TacticalLines.VERY_COARSE_SAND:
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
                        case TacticalLines.BOTTOM_ROUGHNESS_MODERATE:
                        case TacticalLines.BOTTOM_ROUGHNESS_ROUGH:
                        case TacticalLines.BOTTOM_ROUGHNESS_SMOOTH:
                        case TacticalLines.CLUTTER_HIGH:
                        case TacticalLines.CLUTTER_MEDIUM:
                        case TacticalLines.CLUTTER_LOW:
                        case TacticalLines.IMPACT_BURIAL_0:
                        case TacticalLines.IMPACT_BURIAL_10:
                        case TacticalLines.IMPACT_BURIAL_100:
                        case TacticalLines.IMPACT_BURIAL_20:
                        case TacticalLines.IMPACT_BURIAL_75:
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
                        case TacticalLines.SUBMERGED_CRIB:
                        case TacticalLines.FREEFORM:
                            shape.setFillColor(tg.get_FillColor());
                            break;
                        default:
                            break;
                    }
                }

                if (lineType == TacticalLines.INSTABILITY || lineType == TacticalLines.SHEAR) {
                    // Calculate dash array for instability and shear so that dots are on peak of curves
                    final float dotLength = 1f;
                    float spacing = lineThickness * 2;

                    ArrayList<POINT2> points = shape.getPoints();

                    float arcLength = 0;
                    for (int i = 0; i < 6; i++) { // 6 segments in each arc
                        arcLength += (float) lineutility.CalcDistanceDouble(points.get(i), points.get(i + 1));
                    }

                    // For very large line thicknesses get a reasonable spacing
                    // Helps avoid calculating negative dashLength if spacing is longer than arc
                    spacing = Math.min(spacing, arcLength / 5f);

                    // dashLength is space remaining in arc after adding dots and spacing.
                    // Divide remaining space by two because there's a dash on both sides of the dots
                    float[] dash;
                    if (lineType == TacticalLines.INSTABILITY) {
                        float dotAndSpaceLength = dotLength * 2 + spacing * 3;
                        float dashLength = (arcLength - dotAndSpaceLength) / 2;
                        dash = new float[]{dashLength, spacing, dotLength, spacing, dotLength, spacing, dashLength, 0};
                    } else { // SHEAR
                        float dotAndSpaceLength = dotLength + spacing * 2;
                        float dashLength = (arcLength - dotAndSpaceLength) / 2;
                        dash = new float[]{dashLength, spacing, dotLength, spacing, dashLength, 0};
                    }
                    stroke = new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 4f, dash, 0f);
                } else if (lineType == TacticalLines.TROUGH) {
                    // The dashed lines look odd when longer than the arc length. This will set a max length for dashes relative to the arc length.
                    ArrayList<POINT2> points = shape.getPoints();

                    float arcLength = 0;
                    for (int i = 0; i < 6; i++) { // 6 segments in each arc
                        arcLength += (float) lineutility.CalcDistanceDouble(points.get(i), points.get(i + 1));
                    }

                    float dashLength = 2 * lineThickness; // from clsUtility.getLineStroke

                    dashLength = Math.min(dashLength, arcLength / 4);

                    float[] dash = {dashLength, dashLength};
                    stroke = new BasicStroke(lineThickness, tg.get_lineCap(), BasicStroke.JOIN_ROUND, 4f, dash, 0f);
                } else {
                    stroke = clsUtility.getLineStroke(lineThickness, shape.get_Style(), tg.get_lineCap(), BasicStroke.JOIN_ROUND);
                }
                shape.setStroke(stroke);
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("error in clsMETOC.SetShapeProperties");
               ErrorLogger.LogException(_className ,"SetShapeProperties",
                    new RendererException("Failed inside SetShapeProperties", exc));
        }
    }

    /**
     * Draws an arrow to the GeneralPath object from pt1 to pt2.
     *
     * @param pt1 arrow tip
     * @param pt2 - arrow base
     * @param size - arrow size in pixels
     * @param lineObject - general path to draw the arrow
     *
     * @return arrow sprite
     */
    private static void DrawArrow(POINT2 pt1,
            POINT2 pt2,
            double size,
            GeneralPath lineObject) {
        try
        {
            POINT2 ptBase = new POINT2();
            POINT2 ptTemp = new POINT2();
            ArrayList<POINT2> pts = new ArrayList();
            ptBase = lineutility.ExtendAlongLineDouble(pt2, pt1, size);
            ptTemp = lineutility.ExtendDirectedLine(pt1, ptBase, ptBase, 2, size);

            pts.add(ptTemp);
            pts.add(pt2);
            ptTemp = lineutility.ExtendDirectedLine(pt1, ptBase, ptBase, 3, size);
            pts.add(ptTemp);
            lineObject.moveTo(pts.get(0).x, pts.get(0).y);
            lineObject.lineTo(pts.get(1).x, pts.get(1).y);
            lineObject.lineTo(pts.get(2).x, pts.get(2).y);
            pts.clear();
            pts = null;
        } catch (Exception exc) {
               ErrorLogger.LogException(_className ,"DrawArrow",
                    new RendererException("Failed inside DrawArrow", exc));
        }
    }
    /**
     * Returns a GeneralPath for symbols which require splines. Also returns the calculated
     * spline points for those symbols with additional features based on them.
     * @param tg
     * @param splinePoints2 spline points in pixels
     * @return
     */
    private static GeneralPath DrawSplines(TGLight tg,
            ArrayList<POINT2> splinePoints2) {
        GeneralPath lineObject = new GeneralPath();
        try {
            int i = 0, j = 0;
            int n=0,t=0;
            ArrayList<POINT2> splinePoints;
            ArrayList<POINT2> array = tg.get_Pixels();
            POINT2 pt0 = new POINT2(), pt1 = new POINT2(), pt2 = new POINT2(), pt3 = new POINT2(),
                    pt4 = new POINT2(), pt5 = new POINT2(), pt6 = new POINT2();
            POINT2 pt, pt_before, pt_after, Di, p2, p3, pt_after2;
            double tension = 0.33;
            double control_scale = (tension / 0.5 * 0.175);
            double d;
            ArrayList<POINT2> tmpArray = null;
            n=array.size();
            //for (i = 0; i < array.size() - 1; i++) //was length-1
            for (i = 0; i < n - 1; i++) //was length-1
            {
                pt = array.get(i);
                if (i == 0) {
                    lineObject.moveTo(pt.x, pt.y);
                    pt_before = pt;
                } else {
                    pt_before = array.get(i - 1);
                }

                if (i == array.size() - 1) {
                    pt2 = array.get(i);
                } else {
                    pt2 = array.get(i + 1);
                }

                if (i < array.size() - 2) {
                    pt_after = array.get(i + 1);
                } else {
                    pt_after = array.get(array.size() - 1);
                }

                if (i < array.size() - 2) {
                    pt_after2 = array.get(i + 2);
                } else {
                    pt_after2 = array.get(array.size() - 1);
                }


                Di = new POINT2();
                p2 = new POINT2();

                Di.x = pt_after.x - pt_before.x;
                Di.y = pt_after.y - pt_before.y;
                p2.x = pt.x + control_scale * Di.x;
                p2.y = pt.y + control_scale * Di.y;

                p3 = new POINT2();
                POINT2 DiPlus1 = new POINT2();

                DiPlus1.x = pt_after2.x - pt.x;
                DiPlus1.y = pt_after2.y - pt.y;
                p3.x = pt_after.x - control_scale * DiPlus1.x;
                p3.y = pt_after.y - control_scale * DiPlus1.y;

                tmpArray = drawCubicBezier2(tg, lineObject, pt, p2, p3, pt2);

                //ICE_OPENINGS_FROZEN needs to know which segment corresponds to each spline point
                if (tg.get_LineType() == TacticalLines.ICE_OPENINGS_FROZEN ||
                        tg.get_LineType() == TacticalLines.ICE_OPENINGS_FROZEN_GE)
                {
                    if(tmpArray.size()>0)
                        tmpArray.get(tmpArray.size() - 1).style = 47;   //use this to differentiate the arrays
                }
                splinePoints2.addAll(tmpArray);

                splinePoints = tmpArray;

                switch (tg.get_LineType()) {
                    case TacticalLines.EBB_TIDE:
                        if (i == array.size() - 2)
                        {
                            if(splinePoints.size()>=2)
                                DrawArrow(splinePoints.get(splinePoints.size() - 2), tg.Pixels.get(tg.Pixels.size() - 1), arraysupport.getScaledSize(10, tg.get_LineThickness()), lineObject);
                        }
                        break;
                    case TacticalLines.FLOOD_TIDE:
                        d = arraysupport.getScaledSize(10, tg.get_LineThickness());
                        if (i == 0 && splinePoints.size() > 1) {
                            //finally get the feather points
                            //must allocate for the feather points, requires 4 additional points
                            pt0 = splinePoints.get(0);
                            pt1 = splinePoints.get(1);
                            pt2 = lineutility.ExtendLineDouble(pt0, pt1, d);
                            pt3 = lineutility.ExtendLineDouble(pt0, pt1, d * 2);
                            pt4 = lineutility.ExtendLineDouble(pt0, pt1, d * 3);
                            pt5 = lineutility.ExtendDirectedLine(pt3, pt2, pt2, 3, d);
                            pt6 = lineutility.ExtendDirectedLine(pt4, pt3, pt3, 3, d);

                            //first feather line
                            lineObject.moveTo(pt3.x, pt3.y);
                            lineObject.lineTo(pt5.x, pt5.y);
                            //second feather line
                            lineObject.moveTo(pt4.x, pt4.y);
                            lineObject.lineTo(pt6.x, pt6.y);
                        }
                        if (i == array.size() - 2)
                        {
                            if(splinePoints.size()>=2)
                                DrawArrow(splinePoints.get(splinePoints.size() - 2), tg.Pixels.get(tg.Pixels.size() - 1), d, lineObject);
                        }
                        break;
                    case TacticalLines.STREAM:
                    case TacticalLines.JET:
                        if (splinePoints.size() > i + 1) {
                            DrawArrow(splinePoints.get(i + 1), splinePoints.get(i), arraysupport.getScaledSize(10, tg.get_LineThickness()), lineObject);
                        }
                        break;
                    case TacticalLines.FLOOD_TIDE_GE:
                        d = arraysupport.getScaledSize(10, tg.get_LineThickness());
                        if (i == 0 && splinePoints.size() > 1) {
                            //finally get the feather points
                            //must allocate for the feather points, requires 4 additional points
                            pt0 = splinePoints.get(0);
                            pt1 = splinePoints.get(1);
                            pt2 = lineutility.ExtendLineDouble(pt0, pt1, d);
                            pt3 = lineutility.ExtendLineDouble(pt0, pt1, d * 2);
                            pt4 = lineutility.ExtendLineDouble(pt0, pt1, d * 3);
                            pt5 = lineutility.ExtendDirectedLine(pt3, pt2, pt2, 3, d);
                            pt6 = lineutility.ExtendDirectedLine(pt4, pt3, pt3, 3, d);

                            //first feather line
                            lineObject.moveTo(pt3.x, pt3.y);
                            lineObject.lineTo(pt5.x, pt5.y);
                            //second feather line
                            lineObject.moveTo(pt4.x, pt4.y);
                            lineObject.lineTo(pt6.x, pt6.y);
                        }
                        if(i==array.size()-2)//the last point in the array
                        {
                            lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                            t=splinePoints2.size();
                            //for(j=1;j<splinePoints2.size();j++)
                            for(j=1;j<t;j++)
                                lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);

                            if(splinePoints.size()>=2)
                                DrawArrow(splinePoints.get(splinePoints.size() - 2), tg.Pixels.get(tg.Pixels.size() - 1), d, lineObject);
                        }
                        break;
                    case TacticalLines.EBB_TIDE_GE:
                        if(i==array.size()-2)//the last point in the array
                        {
                            lineObject=new GeneralPath();
                            lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                            t=splinePoints2.size();
                            //for(j=1;j<splinePoints2.size();j++)
                            for(j=1;j<t;j++)
                                lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);

                            if(splinePoints.size()>=2)
                                DrawArrow(splinePoints.get(splinePoints.size() - 2), tg.Pixels.get(tg.Pixels.size() - 1), arraysupport.getScaledSize(10, tg.get_LineThickness()), lineObject);
                        }
                        break;
                    case TacticalLines.JET_GE:
                    case TacticalLines.STREAM_GE:
                        if (splinePoints.size() > i + 1) {
                            DrawArrow(splinePoints.get(i + 1), splinePoints.get(i), arraysupport.getScaledSize(10, tg.get_LineThickness()), lineObject);
                        }
                        if(i==array.size()-2)//the last point in the array
                        {
                            lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                            t=splinePoints2.size();
                            //for(j=1;j<splinePoints2.size();j++)
                            for(j=1;j<t;j++)
                                lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);
                        }
                        break;
                    case TacticalLines.ICE_OPENINGS_FROZEN_GE:
                    case TacticalLines.ICE_OPENINGS_LEAD_GE:
                    case TacticalLines.SEAWALL_GE:
                    case TacticalLines.JETTY_BELOW_WATER_GE:
                    case TacticalLines.JETTY_ABOVE_WATER_GE:
                    case TacticalLines.RAMP_ABOVE_WATER_GE:
                    case TacticalLines.RAMP_BELOW_WATER_GE:
                    case TacticalLines.PIER_GE:
                    case TacticalLines.COASTLINE_GE:
                    case TacticalLines.DEPTH_CONTOUR_GE:
                    case TacticalLines.DEPTH_CURVE_GE:
                    case TacticalLines.CRACKS_GE:
                    case TacticalLines.ESTIMATED_ICE_EDGE_GE:
                    case TacticalLines.ICE_EDGE_GE:
                    case TacticalLines.ISOPLETHS_GE:
                    case TacticalLines.ISODROSOTHERM_GE:
                    case TacticalLines.ISOTACH_GE:
                    case TacticalLines.ISOTHERM_GE:
                    case TacticalLines.UPPER_AIR_GE:
                    case TacticalLines.ISOBAR_GE:
                        if(splinePoints2!=null && !splinePoints2.isEmpty())
                        {
                            lineObject=new GeneralPath();
                            if(i==array.size()-2)//the last point in the array
                            {
                                lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                                t=splinePoints2.size();
                                //for(j=1;j<splinePoints2.size();j++)
                                for(j=1;j<t;j++)
                                    lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);
                            }
                        }
                        break;
                    case TacticalLines.ICE_EDGE_RADAR:
                        t=splinePoints.size();
                        d = arraysupport.getScaledSize(5, tg.get_LineThickness());
                        //for (j = 0; j < splinePoints.size() - 1; j++)
                        for (j = 0; j < t - 1; j++) 
                        {
                            pt0 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt2 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 45, d);
                            pt1 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt3 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt1, -45, d);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt2.x, pt2.y);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt3.x, pt3.y);

                            pt0 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt2 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 135, d);
                            pt1 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt3 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt1, -135, d);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt2.x, pt2.y);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt3.x, pt3.y);
                        }
                        break;
                    case TacticalLines.ICE_EDGE_RADAR_GE:
                        t=splinePoints.size();
                        d = arraysupport.getScaledSize(5, tg.get_LineThickness());
                        //for (j = 0; j < splinePoints.size() - 1; j++)
                        for (j = 0; j < t - 1; j++)
                        {
                            pt0 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt2 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 45, d);
                            pt1 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt3 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt1, -45, d);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt2.x, pt2.y);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt3.x, pt3.y);

                            pt0 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt2 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 135, d);
                            pt1 = new POINT2(splinePoints.get(j).x, splinePoints.get(j).y);
                            pt3 = lineutility.ExtendAngledLine(splinePoints.get(j), splinePoints.get(j + 1), pt1, -135, d);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt2.x, pt2.y);
                            lineObject.moveTo(splinePoints.get(j).x, splinePoints.get(j).y);
                            lineObject.lineTo(pt3.x, pt3.y);
                        }
                        if(i==array.size()-2)//the last point in the array
                        {
                            lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                            t=splinePoints2.size();
                            //for(j=1;j<splinePoints2.size();j++)
                            for(j=1;j<t;j++)
                                lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);
                        }
                        break;
                    case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                        t=splinePoints.size();
                        d = arraysupport.getScaledSize(5, tg.get_LineThickness());
                        //for (j = 0; j < splinePoints.size() - 1; j++)
                        for (j = 0; j < t - 1; j++)
                        {
                            //get perpendicular points (point pair)
                            pt0 = splinePoints.get(j + 1);
                            pt1 = lineutility.ExtendDirectedLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 2, d);
                            lineObject.moveTo(pt1.x, pt1.y);
                            pt1 = lineutility.ExtendDirectedLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 3, d);
                            lineObject.lineTo(pt1.x, pt1.y);
                        }
                        break;
                    case TacticalLines.CRACKS_SPECIFIC_LOCATION_GE:
                        t=splinePoints.size();
                        d = arraysupport.getScaledSize(5, tg.get_LineThickness());
                        //for (j = 0; j < splinePoints.size() - 1; j++)
                        for (j = 0; j < t - 1; j++)
                        {
                            //get perpendicular points (point pair)
                            pt0 = splinePoints.get(j + 1);
                            pt1 = lineutility.ExtendDirectedLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 2, d);
                            lineObject.moveTo(pt1.x, pt1.y);
                            pt1 = lineutility.ExtendDirectedLine(splinePoints.get(j), splinePoints.get(j + 1), pt0, 3, d);
                            lineObject.lineTo(pt1.x, pt1.y);
                        }
                        if(i==array.size()-2)//the last point in the array
                        {
                            lineObject.moveTo((int)splinePoints2.get(0).x,(int)splinePoints2.get(0).y);
                            t=splinePoints2.size();
                            //for(j=1;j<splinePoints2.size();j++)
                            for(j=1;j<t;j++)
                                lineObject.lineTo((int)splinePoints2.get(j).x,(int)splinePoints2.get(j).y);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"DrawSplines",
                    new RendererException("Failed inside DrawSplines", exc));
        }
        return lineObject;
    }

    /**
     * Calculates a point on a segment using a ratio of the segment length.
     * This function is used for calculating control points on Bezier curves.
     *
     * @param P0 the 1st point on the segment.
     * @param P1 the last point on the segment
     * @param ratio the fraction of the segment length
     *
     * @return calculated point on the P0-P1 segment.
     */
    private static POINT2 getPointOnSegment(POINT2 P0, POINT2 P1, double ratio) {
        //return {x: (P0.x + ((P1.x - P0.x) * ratio)), y: (P0.y + ((P1.y - P0.y) * ratio))};
        //var pt:Point=new Point();
        POINT2 pt = new POINT2();
        try
        {
            pt.x = P0.x + (P1.x - P0.x) * ratio;
            pt.y = P0.y + (P1.y - P0.y) * ratio;
        }
        catch (Exception exc)
        {
               ErrorLogger.LogException(_className ,"getPointOnSegment",
                    new RendererException("Failed inside getPointOnSegment", exc));
        }
        return pt;
    }

    /**
     * This function will trace a cubic approximation of the cubic Bezier
     * It will calculate a series of (control point/Destination point] which
     * will be used to draw quadratic Bezier starting from P0
     *
     * @param lineObject - the sprite to use for drawing
     * @param P0 - 1st client point
     * @param P1 - 1st control point for a cubic Bezier
     * @param P2 - 2nd control point
     * @param P3 - 2nd client point
     *
     * @return an array of points along the spline at linetype specific intervals
     */
    private static ArrayList drawCubicBezier2(
            TGLight tg,
            GeneralPath lineObject,
            POINT2 P0,
            POINT2 P1,
            POINT2 P2,
            POINT2 P3)
    {
        ArrayList<POINT2> array = new ArrayList();
        try {
            // this stuff may be unnecessary
            // calculates the useful base points
            POINT2 PA = getPointOnSegment(P0, P1, 0.75);
            POINT2 PB = getPointOnSegment(P3, P2, 0.75);

            // get 1/16 of the [P3, P0] segment
            double dx = (P3.x - P0.x) / 16d;
            double dy = (P3.y - P0.y) / 16d;

            // calculates control point 1
            POINT2 Pc_1 = getPointOnSegment(P0, P1, 0.375);

            // calculates control point 2
            POINT2 Pc_2 = getPointOnSegment(PA, PB, 0.375);
            Pc_2.x -= dx;
            Pc_2.y -= dy;

            // calculates control point 3
            POINT2 Pc_3 = getPointOnSegment(PB, PA, 0.375);
            Pc_3.x += dx;
            Pc_3.y += dy;

            // calculates control point 4
            POINT2 Pc_4 = getPointOnSegment(P3, P2, 0.375);

            // calculates the 3 anchor points
            POINT2 Pa_1 = lineutility.MidPointDouble(Pc_1, Pc_2, 0);
            POINT2 Pa_2 = lineutility.MidPointDouble(PA, PB, 0);
            POINT2 Pa_3 = lineutility.MidPointDouble(Pc_3, Pc_4, 0);
            switch (tg.get_LineType()) {   //draw the solid curve for these
                case TacticalLines.ISOBAR:
                case TacticalLines.UPPER_AIR:
                case TacticalLines.ISODROSOTHERM:
                case TacticalLines.ICE_EDGE:
                case TacticalLines.CRACKS:
                case TacticalLines.DEPTH_CURVE:
                case TacticalLines.DEPTH_CONTOUR:
                case TacticalLines.COASTLINE:
                case TacticalLines.PIER:
                case TacticalLines.RAMP_ABOVE_WATER:
                case TacticalLines.JETTY_ABOVE_WATER:
                case TacticalLines.SEAWALL:
                case TacticalLines.ICE_OPENINGS_LEAD:
                case TacticalLines.ISOTACH:
                case TacticalLines.ISOTHERM:
                case TacticalLines.ISOPLETHS:
                case TacticalLines.ESTIMATED_ICE_EDGE:
                case TacticalLines.RAMP_BELOW_WATER:
                case TacticalLines.JETTY_BELOW_WATER:
                    lineObject.moveTo(P0.x, P0.y);
                    lineObject.curveTo(P1.x, P1.y, P2.x, P2.y, P3.x, P3.y);
                    return array;
                case TacticalLines.ICE_OPENINGS_LEAD_GE:
                case TacticalLines.SEAWALL_GE:
                case TacticalLines.JETTY_BELOW_WATER_GE:
                case TacticalLines.JETTY_ABOVE_WATER_GE:
                case TacticalLines.RAMP_ABOVE_WATER_GE:
                case TacticalLines.RAMP_BELOW_WATER_GE:
                case TacticalLines.PIER_GE:
                case TacticalLines.COASTLINE_GE:
                case TacticalLines.DEPTH_CONTOUR_GE:
                case TacticalLines.DEPTH_CURVE_GE:
                case TacticalLines.CRACKS_GE:
                case TacticalLines.ESTIMATED_ICE_EDGE_GE:
                case TacticalLines.ICE_EDGE_GE:
                case TacticalLines.ISOPLETHS_GE:
                case TacticalLines.ISOTACH_GE:
                case TacticalLines.ISOTHERM_GE:
                case TacticalLines.ISOBAR_GE:
                case TacticalLines.UPPER_AIR_GE:
                case TacticalLines.ISODROSOTHERM_GE:
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.ICE_OPENINGS_FROZEN_GE:
                case TacticalLines.ICE_EDGE_RADAR:
                case TacticalLines.ICE_EDGE_RADAR_GE:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION_GE:
                case TacticalLines.EBB_TIDE:
                case TacticalLines.FLOOD_TIDE:
                case TacticalLines.EBB_TIDE_GE:
                case TacticalLines.FLOOD_TIDE_GE:
                case TacticalLines.JET:
                case TacticalLines.STREAM:
                case TacticalLines.JET_GE:
                case TacticalLines.STREAM_GE:
                    lineObject.moveTo(P0.x, P0.y);
                    lineObject.curveTo(P1.x, P1.y, P2.x, P2.y, P3.x, P3.y);
                    //do not return, we still need the spline points
                    //to claculate other features
                    break;
                default:
                    //the rest of them must use the calculated curve points
                    break;
            }
            //var sprite:Sprite;
            int j = 0;
            double distance;
            int n = 0;
            double x = 0, y = 0, increment = 0;
            POINT2 pt0, pt1, pt2;
            double t;
            POINT2 pt;
            array.clear();
            //distance=clsUtility.Distance2(P0,Pa_1);
            //add the curve points to tg.Pixels
            switch (tg.get_LineType()) {
                case TacticalLines.ICE_EDGE_RADAR:
                case TacticalLines.ICE_EDGE_RADAR_GE:
                    increment = arraysupport.getScaledSize(20, tg.get_LineThickness());
                    break;
                case TacticalLines.ICE_OPENINGS_FROZEN:
                case TacticalLines.ICE_OPENINGS_FROZEN_GE:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION:
                case TacticalLines.CRACKS_SPECIFIC_LOCATION_GE:
                    //increment = 12.0;
                    increment = arraysupport.getScaledSize(7, tg.get_LineThickness());
                    break;
                default:
                    increment = arraysupport.getScaledSize(10, tg.get_LineThickness());
                    break;
            }

            distance = lineutility.CalcDistanceDouble(P0, Pa_1);
            if(distance<increment)
                distance=increment;
            n = (int) (distance / increment);
            
            pt0 = P0;
            pt1 = Pc_1;
            pt2 = Pa_1;
            for (j = 0; j < n; j++) {
                t = (double) j * (increment / distance);
                x = (1d - t) * (1d - t) * pt0.x + 2 * (1d - t) * t * pt1.x + t * t * pt2.x;
                y = (1d - t) * (1d - t) * pt0.y + 2 * (1d - t) * t * pt1.y + t * t * pt2.y;
                pt = new POINT2(x, y);
                //array.push(pt);
                array.add(pt);
            }
            //distance=clsUtility.Distance2(Pa_1,Pa_2);
            distance = lineutility.CalcDistanceDouble(Pa_1, Pa_2);

            //add the curve points to tg.Pixels
            n = (int) (distance / increment);
            pt0 = Pa_1;
            pt1 = Pc_2;
            pt2 = Pa_2;
            for (j = 0; j < n; j++) {
                t = (double) j * (increment / distance);
                x = (1d - t) * (1d - t) * pt0.x + 2 * (1d - t) * t * pt1.x + t * t * pt2.x;
                y = (1d - t) * (1d - t) * pt0.y + 2 * (1d - t) * t * pt1.y + t * t * pt2.y;
                pt = new POINT2(x, y);
                array.add(pt);
            }

            //distance=clsUtility.Distance2(Pa_2,Pa_3);
            distance = lineutility.CalcDistanceDouble(Pa_2, Pa_3);
            //add the curve points to tg.Pixels
            n = (int) (distance / increment);
            pt0 = Pa_2;
            pt1 = Pc_3;
            pt2 = Pa_3;
            for (j = 0; j < n; j++) {
                t = (double) j * (increment / distance);
                x = (1d - t) * (1d - t) * pt0.x + 2 * (1d - t) * t * pt1.x + t * t * pt2.x;
                y = (1d - t) * (1d - t) * pt0.y + 2 * (1d - t) * t * pt1.y + t * t * pt2.y;
                pt = new POINT2(x, y);
                array.add(pt);
            }
            //distance=clsUtility.Distance2(Pa_3,P3);
            distance = lineutility.CalcDistanceDouble(Pa_3, P3);
            //add the curve points to tg.Pixels
            n = (int) (distance / increment);
            pt0 = Pa_3;
            pt1 = Pc_4;
            pt2 = P3;
            for (j = 0; j < n; j++) {
                t = (double) j * (increment / distance);
                x = (1d - t) * (1d - t) * pt0.x + 2 * (1d - t) * t * pt1.x + t * t * pt2.x;
                y = (1d - t) * (1d - t) * pt0.y + 2 * (1d - t) * t * pt1.y + t * t * pt2.y;
                pt = new POINT2(x, y);
                array.add(pt);
            }
        }
        catch (Exception exc) {
               ErrorLogger.LogException(_className ,"drawCubicBezier2",
                    new RendererException("Failed inside drawCubicBezier2", exc));
        }
        return array;
    }

    /*
     *
     * Called by Splines2TG to get straight channel lines for splines.
     *
     * @param tg - TGlight
     *
     * @return An ArrayList to use for building the parallel splines
     */
//    private static ArrayList ParallelLines(TGLight tg,int rev) {
//        ArrayList<POINT2> channelPoints2 = new ArrayList();
//        try {
//            double[] pLinePoints = new double[tg.Pixels.size() * 2];
//            double[] channelPoints = new double[6 * tg.Pixels.size()];
//            int j = 0;
//            int n=tg.Pixels.size();
//            //for (j = 0; j < tg.Pixels.size(); j++) 
//            for (j = 0; j < n; j++) 
//            {
//                pLinePoints[2 * j] = tg.Pixels.get(j).x;
//                pLinePoints[2 * j + 1] = tg.Pixels.get(j).y;
//            }
//            int numPoints = tg.Pixels.size();
//            int channelWidth = 20;
//            int usePtr = 0;
//            ArrayList<Shape2> shapes = null;
//
//            try {
//                CELineArray.CGetChannel2Double(pLinePoints, pLinePoints, channelPoints, numPoints, numPoints, (int) TacticalLines.CHANNEL, channelWidth, usePtr, shapes,rev);
//            } catch (Exception e) {
//                ErrorLogger.LogException(_className, "ParallelLines",
//                    new RendererException("Failed inside ParallelLines", e));
//            }
//
//            POINT2 pt2 = null;
//            int style = 0;
//            n=channelPoints.length;
//            //for (j = 0; j < channelPoints.length / 3; j++) 
//            for (j = 0; j < n / 3; j++) 
//            {
//                pt2 = new POINT2(channelPoints[3 * j], channelPoints[3 * j + 1], style);
//                channelPoints2.add(pt2);
//            }
//        } catch (Exception exc) {
//            ErrorLogger.LogException(_className, "ParallelLines",
//                    new RendererException("Failed inside ParallelLines", exc));
//        }
//        return channelPoints2;
//    }
    /**
     * Call this function with segment
     * @param Pixels a segment of tg.Pixels
     * @return
     */
    private static ArrayList<POINT2> ParallelLines2(ArrayList<POINT2> Pixels, int channelWidth) {
        ArrayList<POINT2> channelPoints2 = new ArrayList();
        try {
            double[] pLinePoints = new double[Pixels.size() * 2];
            double[] channelPoints = new double[6 * Pixels.size()];
            int j = 0;
            int n=Pixels.size();
            //for (j = 0; j < Pixels.size(); j++) 
            for (j = 0; j < n; j++) 
            {
                pLinePoints[2 * j] = Pixels.get(j).x;
                pLinePoints[2 * j + 1] = Pixels.get(j).y;
            }
            int numPoints = Pixels.size();
            int usePtr = 0;
            ArrayList<Shape2> shapes = null;

            try {
                TGLight tg = new TGLight();
                tg.set_LineType(TacticalLines.CHANNEL);
                Channels.GetChannel1Double(tg, pLinePoints, pLinePoints, channelPoints, numPoints, numPoints, channelWidth, usePtr, shapes);
            } catch (Exception e) {
                ErrorLogger.LogException(_className, "ParallelLines2",
                    new RendererException("Failed inside ParallelLines2", e));
            }

            POINT2 pt2 = null;
            int style = 0;
            n=channelPoints.length;
            //for (j = 0; j < channelPoints.length / 3; j++) 
            for (j = 0; j < n / 3; j++) 
            {
                pt2 = new POINT2(channelPoints[3 * j], channelPoints[3 * j + 1], style);
                channelPoints2.add(pt2);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "ParallelLines2",
                    new RendererException("Failed inside ParallelLines2", exc));
        }
        return channelPoints2;
    }
}
