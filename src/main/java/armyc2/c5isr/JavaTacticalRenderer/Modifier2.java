package armyc2.c5isr.JavaTacticalRenderer;


import armyc2.c5isr.JavaLineArray.*;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import armyc2.c5isr.renderer.SinglePointRenderer;
import armyc2.c5isr.renderer.utilities.*;

import java.util.HashMap;
import java.util.Map;

import static armyc2.c5isr.JavaTacticalRenderer.clsUtility.GetLinetypeFromString;

/**
 * This class handles everything having to do with text for a
 * tactical graphic. Note: labels are handled the same as text modifiers.
 * 
 *
 */
public class Modifier2 {
    private POINT2[] textPath;
    private String textID;
    private String featureID;
    private String text;

    private BufferedImage image;
    private int iteration;
    private int justify;
    private int type;
    private double lineFactor;
    private static final String _className = "Modifier2";
    @Deprecated
    private boolean isIntegral = false;
    private boolean fitsMBR = true;

    Modifier2() {
        textPath = new POINT2[2];
    }

    private static final int toEnd = 1; // Put next to pt0 on opposite side of line
    private static final int aboveMiddle = 2;    //use both points
    private static final int area = 3;   //use one point
    private static final int screen = 4;   //use one point, screen, cover, guard points
    private static final int aboveEnd = 5; // Put next to pt0 on line
    private static final int aboveMiddlePerpendicular = 6; //use both points
    private static final int aboveStartInside = 7; //place at the start inside the shape
    private static final int aboveEndInside = 8;  //place at the end inside the shape
    private static final int areaImage = 9;   //use one point
    private static double fillAlphaCanObscureText = 50d;

    private static boolean DoublesBack(POINT2 pt0, POINT2 pt1, POINT2 pt2) {
        boolean result = true;
        try {
            double theta1 = Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x);
            double theta0 = Math.atan2(pt0.y - pt1.y, pt0.x - pt1.x);
            double beta = Math.abs(theta0 - theta1);
            if (beta > 0.1) {
                result = false;
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "DoublesBack",
                    new RendererException("Failed inside DoublesBack", exc));
        }
        return result;
    }

    /**
     * Returns a generic label for the symbol per Mil-Std-2525
     *
     * @param tg
     * @return
     */
    private static String GetCenterLabel(TGLight tg) {
        String label = "";
        try {
            switch (tg.get_LineType()) {
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.SHIP_AOI_CIRCULAR:
                    label = "AOI";
                    break;
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                    label = "DA";
                    break;
                case TacticalLines.NOTACK:
                    label = "N";
                    break;
                case TacticalLines.LAUNCH_AREA:
                    label = "LA";
                    break;
                case TacticalLines.SL:
                    label = "SL";
                    break;
                case TacticalLines.TC:
                    label = "TC";
                    break;
                case TacticalLines.AARROZ:
                    label = "AARROZ";
                    break;
                case TacticalLines.UAROZ:
                    label = "UAROZ";
                    break;
                case TacticalLines.WEZ:
                    label = "WEZ";
                    break;
                case TacticalLines.FEZ:
                    label = "FEZ";
                    break;
                case TacticalLines.JEZ:
                    label = "JEZ";
                    break;
                case TacticalLines.IFF_OFF:
                    label = "IFF OFF";
                    break;
                case TacticalLines.IFF_ON:
                    label = "IFF ON";
                    break;
                case TacticalLines.BCL_REVD:
                case TacticalLines.BCL:
                    label = "BCL";
                    break;
                case TacticalLines.ICL:
                    label = "ICL";
                    break;
                case TacticalLines.FEBA:
                    label = "FEBA";
                    break;
                case TacticalLines.BDZ:
                    label = "BDZ";
                    break;
                case TacticalLines.JTAA:
                    label = "JTAA";
                    break;
                case TacticalLines.SAA:
                    label = "SAA";
                    break;
                case TacticalLines.SGAA:
                    label = "SGAA";
                    break;
                case TacticalLines.ASSAULT:
                    label = "ASLT";
                    break;
                case TacticalLines.SAAFR:
                    label = "SAAFR";
                    break;
                case TacticalLines.AC:
                    label = "AC";
                    break;
                case TacticalLines.SECURE:
                case TacticalLines.SEIZE:
                    label = "S";
                    break;
                case TacticalLines.RETAIN:
                    label = "R";
                    break;
                case TacticalLines.PENETRATE:
                    label = "P";
                    break;
                case TacticalLines.OCCUPY:
                    label = "O";
                    break;
                case TacticalLines.ISOLATE:
                    label = "I";
                    break;
                case TacticalLines.FIX:
                    label = "F";
                    break;
                case TacticalLines.DISRUPT:
                    label = "D";
                    break;
                case TacticalLines.CANALIZE:
                case TacticalLines.CLEAR:
                    label = "C";
                    break;
                case TacticalLines.BREACH:
                case TacticalLines.BYPASS:
                    label = "B";
                    break;
                case TacticalLines.CORDONKNOCK:
                    label = "C/K";
                    break;
                case TacticalLines.CORDONSEARCH:
                    label = "C/S";
                    break;
                case TacticalLines.UXO:
                    label = "UXO";
                    break;
                case TacticalLines.RETIRE:
                    label = "R";
                    break;
                case TacticalLines.FPOL:
                    label = "P(F)";
                    break;
                case TacticalLines.RPOL:
                    label = "P(R)";
                    break;
                case TacticalLines.BRDGHD:
                case TacticalLines.BRDGHD_GE:
                    if (SymbolID.getVersion(tg.get_SymbolId()) >= SymbolID.Version_2525E)
                        label = "BL";
                    else
                        label = "B";
                    break;
                case TacticalLines.HOLD:
                case TacticalLines.HOLD_GE:
                    //label="HOLDING LINE";
                    label = "HL";
                    break;
                case TacticalLines.PL:
                    label = "PL";
                    break;
                case TacticalLines.LL:
                    label = "LL";
                    break;
                case TacticalLines.EWL:
                    label = "EWL";
                    break;
                case TacticalLines.SCREEN:
                    label = "S";
                    break;
                case TacticalLines.COVER:
                    label = "C";
                    break;
                case TacticalLines.GUARD:
                    label = "G";
                    break;
                case TacticalLines.RIP:
                    label = "RIP";
                    break;
                case TacticalLines.WITHDRAW:
                    label = "W";
                    break;
                case TacticalLines.WDRAWUP:
                    label = "WP";
                    break;
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                    label = "CATK";
                    break;
                case TacticalLines.FLOT:
                    label = "FLOT";
                    break;
                case TacticalLines.LC:
                    label = "LC";
                    break;
                case TacticalLines.ASSY:
                    label = "AA";
                    break;
                case TacticalLines.EA:
                    label = "EA";
                    break;
                case TacticalLines.DZ:
                    label = "DZ";
                    break;
                case TacticalLines.EZ:
                    label = "EZ";
                    break;
                case TacticalLines.LZ:
                    label = "LZ";
                    break;
                case TacticalLines.LAA:
                    label = "LAA";
                    break;
                case TacticalLines.PZ:
                    label = "PZ";
                    break;
                case TacticalLines.MRR:
                    label = "MRR";
                    break;
                case TacticalLines.SC:
                    label = "SC";
                    break;
                case TacticalLines.LLTR:
                    label = "LLTR";
                    break;
                case TacticalLines.ROZ:
                    label = "ROZ";
                    break;
                case TacticalLines.FAADZ:
                    label = "SHORADEZ";
                    break;
                case TacticalLines.HIDACZ:
                    label = "HIDACZ";
                    break;
                case TacticalLines.MEZ:
                    label = "MEZ";
                    break;
                case TacticalLines.LOMEZ:
                    label = "LOMEZ";
                    break;
                case TacticalLines.HIMEZ:
                    label = "HIMEZ";
                    break;
                case TacticalLines.WFZ:
                    label = "WFZ";
                    break;
                case TacticalLines.MINED:
                case TacticalLines.FENCED:
                    label = "M";
                    break;
                case TacticalLines.PNO:
                    label = "(P)";
                    break;
                case TacticalLines.OBJ:
                    label = "OBJ";
                    break;
                case TacticalLines.NAI:
                    label = "NAI";
                    break;
                case TacticalLines.TAI:
                    label = "TAI";
                    break;
                case TacticalLines.BASE_CAMP_REVD:
                case TacticalLines.BASE_CAMP:
                    label = "BC";
                    break;
                case TacticalLines.GUERILLA_BASE_REVD:
                case TacticalLines.GUERILLA_BASE:
                    label = "GB";
                    break;
                case TacticalLines.LINTGTS:
                    label = "SMOKE";
                    break;
                case TacticalLines.FPF:
                    label = "FPF";
                    break;
                case TacticalLines.ATKPOS:
                    label = "ATK";
                    break;
                case TacticalLines.FCL:
                    label = "FCL";
                    break;
                case TacticalLines.LOA:
                    label = "LOA";
                    break;
                case TacticalLines.LOD:
                    label = "LD";
                    break;
                case TacticalLines.PLD:
                    label = "PLD";
                    break;
                case TacticalLines.DELAY:
                    label = "D";
                    break;
                case TacticalLines.RELEASE:
                    label = "RL";
                    break;
                case TacticalLines.HOL:
                    label = "HOL";
                    break;
                case TacticalLines.BHL:
                    label = "BHL";
                    break;
                case TacticalLines.SMOKE:
                    label = "SMOKE";
                    break;
                case TacticalLines.NFL:
                    label = "NFL";
                    break;
                case TacticalLines.MFP:
                    label = "MFP";
                    break;
                case TacticalLines.FSCL:
                    label = "FSCL";
                    break;
                case TacticalLines.CFL:
                    label = "CFL";
                    break;
                case TacticalLines.RFL:
                    label = "RFL";
                    break;
                case TacticalLines.AO:
                    label = "AO";
                    break;
                case TacticalLines.BOMB:
                    label = "BOMB";
                    break;
                case TacticalLines.TGMF:
                    label = "TGMF";
                    break;
                case TacticalLines.FSA:
                    label = "FSA";
                    break;
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.FSA_RECTANGULAR:
                    label = "FSA";
                    break;
                case TacticalLines.ACA:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.ACA_RECTANGULAR:
                    label = "ACA";
                    break;
                case TacticalLines.FFA:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.FFA_RECTANGULAR:
                    label = "FFA";
                    break;
                case TacticalLines.NFA:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.NFA_RECTANGULAR:
                    label = "NFA";
                    break;
                case TacticalLines.RFA:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.RFA_RECTANGULAR:
                    label = "RFA";
                    break;
                case TacticalLines.ATI:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.ATI_RECTANGULAR:
                    label = "ATI ZONE";
                    break;
                case TacticalLines.PAA:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.PAA_RECTANGULAR:
                    label = "PAA";
                    break;
                case TacticalLines.CFFZ:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                    label = "CFF ZONE";
                    break;
                case TacticalLines.CFZ:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                    label = "CF ZONE";
                    break;
                case TacticalLines.SENSOR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                    label = "SENSOR ZONE";
                    break;
                case TacticalLines.CENSOR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                    label = "CENSOR ZONE";
                    break;
                case TacticalLines.DA:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.DA_RECTANGULAR:
                    label = "DA";
                    break;
                case TacticalLines.ZOR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                    label = "ZOR";
                    break;
                case TacticalLines.TBA:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TBA_RECTANGULAR:
                    label = "TBA";
                    break;
                case TacticalLines.TVAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                    label = "TVAR";
                    break;
                case TacticalLines.KILLBOXBLUE:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                    label = "BKB";
                    break;
                case TacticalLines.KILLBOXPURPLE:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    label = "PKB";
                    break;
                case TacticalLines.MSR:
                case TacticalLines.MSR_ONEWAY:
                case TacticalLines.MSR_TWOWAY:
                case TacticalLines.MSR_ALT:
                    label = "MSR";
                    break;
                case TacticalLines.ASR:
                case TacticalLines.ASR_ONEWAY:
                case TacticalLines.ASR_TWOWAY:
                case TacticalLines.ASR_ALT:
                    label = "ASR";
                    break;
                case TacticalLines.ROUTE:
                case TacticalLines.ROUTE_ONEWAY:
                case TacticalLines.ROUTE_ALT:
                    label = "ROUTE";
                    break;
                case TacticalLines.LDLC:
                    label = "LD/LC";
                    break;
                case TacticalLines.AIRHEAD:
                    label = "AIRHEAD LINE";
                    break;
                case TacticalLines.BLOCK:
                case TacticalLines.BEARING:
                    label = "B";
                    break;
                case TacticalLines.BEARING_J:
                    label = "J";
                    break;
                case TacticalLines.BEARING_RDF:
                    label = "RDF";
                    break;
                case TacticalLines.ELECTRO:
                    label = "E";
                    break;
                case TacticalLines.BEARING_EW:
                    label = "EW";
                    break;
                case TacticalLines.ACOUSTIC:
                case TacticalLines.ACOUSTIC_AMB:
                    label = "A";
                    break;
                case TacticalLines.TORPEDO:
                    label = "T";
                    break;
                case TacticalLines.OPTICAL:
                    label = "O";
                    break;
                case TacticalLines.DHA:
                    label = "DHA";
                    break;
                case TacticalLines.FARP:
                    label = "FARP";
                    break;
                case TacticalLines.BSA:
                    label = "BSA";
                    break;
                case TacticalLines.DSA:
                    label = "DSA";
                    break;
                case TacticalLines.CSA:
                    label = "CSA";
                    break;
                case TacticalLines.RSA:
                    label = "RSA";
                    break;
                case TacticalLines.CONTAIN:
                    label = "C";
                    break;
                case TacticalLines.OBSFAREA:
                    label = "FREE";
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in Modifier2.GetCenterLabel");
            ErrorLogger.LogException(_className, "GetCenterLabel",
                    new RendererException("Failed inside GetCenterLabel", exc));
        }
        return label;
    }
    //non CPOF clients using best fit need these accessors

    public POINT2[] get_TextPath() {
        return textPath;
    }

    protected void set_TextPath(POINT2[] value) {
        textPath = value;
    }

    @Deprecated
    protected void set_IsIntegral(boolean value) {
        isIntegral = value;
    }

    @Deprecated
    protected boolean get_IsIntegral() {
        return isIntegral;
    }

    private static void AddOffsetModifier(TGLight tg,
                                          String text,
                                          int type,
                                          double lineFactor,
                                          int startIndex,
                                          int endIndex,
                                          double spaces,
                                          String rightOrLeft) {
        if (rightOrLeft == null || tg.Pixels == null || tg.Pixels.size() < 2 || endIndex >= tg.Pixels.size()) {
            return;
        }

        POINT2 pt0 = tg.Pixels.get(startIndex);
        POINT2 pt1 = tg.Pixels.get(endIndex);
        if (rightOrLeft.equals("left")) {
            pt0.x -= spaces;
            pt1.x -= spaces;
        } else {
            pt0.x += spaces;
            pt1.x += spaces;
        }
        AddModifier2(tg, text, type, lineFactor, pt0, pt1, false);
    }

    /**
     *
     * @param tg
     * @param text
     * @param type
     * @param lineFactor
     * @param ptStart
     * @param ptEnd
     */
    private static void AddModifier(TGLight tg,
                                    String text,
                                    int type,
                                    double lineFactor,
                                    POINT2 ptStart,
                                    POINT2 ptEnd) {
        if (tg.Pixels == null || tg.Pixels.size() < 2) {
            return;
        }
        AddModifier2(tg, text, type, lineFactor, ptStart, ptEnd, false);
    }

    private static void AddModifier2(TGLight tg,
                                     String text,
                                     int type,
                                     double lineFactor,
                                     POINT2 pt0,
                                     POINT2 pt1,
                                     boolean isIntegral) {
        AddModifier2(tg, text, type, lineFactor, pt0, pt1, isIntegral, null);
    }

    private static void AddModifier2(TGLight tg,
                                     String text,
                                     int type,
                                     double lineFactor,
                                     POINT2 pt0,
                                     POINT2 pt1,
                                     boolean isIntegral,
                                     String modifierType) {
        try {
            if (text == null || text.equals("")) {
                return;
            }

            Modifier2 modifier = new Modifier2();
            modifier.set_IsIntegral(isIntegral);
            modifier.text = text;
            modifier.type = type;
            modifier.lineFactor = lineFactor;
            modifier.textPath[0] = pt0;
            modifier.textPath[1] = pt1;
            modifier.textID = modifierType;
            tg.modifiers.add(modifier);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddModifier",
                    new RendererException("Failed inside AddModifier", exc));
        }
    }

    private static void AddIntegralModifier(TGLight tg,
                                            String text,
                                            int type,
                                            double lineFactor,
                                            int startIndex,
                                            int endIndex) {
        AddIntegralModifier(tg, text, type, lineFactor, startIndex, endIndex, true);
    }

    private static void AddIntegralModifier(TGLight tg,
                                            String text,
                                            int type,
                                            double lineFactor,
                                            int startIndex,
                                            int endIndex,
                                            Boolean isIntegral) {
        AddIntegralModifier(tg, text, type, lineFactor, startIndex, endIndex, isIntegral, null);
    }

    private static void AddIntegralModifier(TGLight tg,
                                            String text,
                                            int type,
                                            double lineFactor,
                                            int startIndex,
                                            int endIndex,
                                            Boolean isIntegral,
                                            String modifierType) {
        if (tg.Pixels == null || tg.Pixels.isEmpty() || endIndex >= tg.Pixels.size()) {
            return;
        }
        AddIntegralAreaModifier(tg, text, type, lineFactor, tg.Pixels.get(startIndex), tg.Pixels.get(endIndex), isIntegral, modifierType);
    }

    /**
     * Creates and adds center modifiers for generic areas
     *
     * @param tg
     * @param text
     * @param type
     * @param lineFactor
     * @param pt0
     * @param pt1
     */
    private static void AddAreaModifier(TGLight tg,
                                        String text,
                                        int type,
                                        double lineFactor,
                                        POINT2 pt0,
                                        POINT2 pt1) {
        AddIntegralAreaModifier(tg, text, type, lineFactor, pt0, pt1, true);
    }

    /**
     * sets modifier.textId to the modifier type, e.g. label, T, T1, etc.
     *
     * @param tg
     * @param text
     * @param type
     * @param lineFactor
     * @param pt0
     * @param pt1
     * @param modifierType
     */
    private static void AddAreaModifier(TGLight tg,
                                        String text,
                                        int type,
                                        double lineFactor,
                                        POINT2 pt0,
                                        POINT2 pt1,
                                        String modifierType) {
        AddIntegralAreaModifier(tg, text, type, lineFactor, pt0, pt1, true, modifierType);
    }

    private static void AddIntegralAreaModifier(TGLight tg,
                                                String text,
                                                int type,
                                                double lineFactor,
                                                POINT2 pt0,
                                                POINT2 pt1,
                                                Boolean isIntegral) {
        AddIntegralAreaModifier(tg,text,type,lineFactor,pt0, pt1, isIntegral, null);
    }

    private static void AddIntegralAreaModifier(TGLight tg,
                                                BufferedImage image,
                                                int type,
                                                double lineFactor,
                                                POINT2 pt0,
                                                POINT2 pt1,
                                                Boolean isIntegral) {
        try {
            if (image == null || image.equals("")) {
                return;
            }

            Modifier2 modifier = new Modifier2();
            modifier.set_IsIntegral(isIntegral);
            modifier.image = image;
            if (image == null || image.equals("")) {
                return;
            }

            if (pt0 == null || pt1 == null) {
                return;
            }

            modifier.type = type;
            modifier.lineFactor = lineFactor;
            modifier.textPath[0] = pt0;
            modifier.textPath[1] = pt1;
            tg.modifiers.add(modifier);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddAreaModifier",
                    new RendererException("Failed inside AddAreaModifier", exc));
        }
    }

    private static void AddIntegralAreaModifier(TGLight tg,
                                                String text,
                                                int type,
                                                double lineFactor,
                                                POINT2 pt0,
                                                POINT2 pt1,
                                                Boolean isIntegral,
                                                String modifierType) {
        if (pt0 == null || pt1 == null) {
            return;
        }
        AddModifier2(tg, text, type, lineFactor, pt0, pt1, isIntegral, modifierType);
    }

    /**
     * Returns symbol MBR. Assumes points have been initialized with value of
     * 0th point
     *
     * @param tg the tactical graphic object
     * @param ptUl OUT - MBR upper left
     * @param ptUr OUT - MBR upper right
     * @param ptLr OUT - MBR lower right
     * @param ptLl OUT - MBR lower left
     */
    public static void GetMBR(TGLight tg,
                              POINT2 ptUl,
                              POINT2 ptUr,
                              POINT2 ptLr,
                              POINT2 ptLl) {
        try {
            int j = 0;
            double x = 0;
            double y = 0;
            ptUl.x = tg.Pixels.get(0).x;
            ptUl.y = tg.Pixels.get(0).y;
            ptUr.x = tg.Pixels.get(0).x;
            ptUr.y = tg.Pixels.get(0).y;
            ptLl.x = tg.Pixels.get(0).x;
            ptLl.y = tg.Pixels.get(0).y;
            ptLr.x = tg.Pixels.get(0).x;
            ptLr.y = tg.Pixels.get(0).y;
            int n = tg.Pixels.size();
            //for (j = 1; j < tg.Pixels.size(); j++)
            for (j = 1; j < n; j++) {
                x = tg.Pixels.get(j).x;
                y = tg.Pixels.get(j).y;
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
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetMBR",
                    new RendererException("Failed inside GetMBR", exc));
        }
    }

    /**
     * Tests segment of a Boundary
     *
     * @param tg
     * @param g2d
     * @param middleSegment
     * @return
     */
    private static boolean GetBoundarySegmentTooShort(TGLight tg,
                                                      Graphics2D g2d,
                                                      int middleSegment) {
        boolean lineTooShort = false;
        try {
            //int middleSegment = tg.Pixels.size() / 2 - 1;
            g2d.setFont(tg.get_Font());
            FontMetrics metrics = g2d.getFontMetrics();
            String echelonSymbol = null;
            int stringWidthEchelonSymbol = 0;

            POINT2 pt0 = tg.Pixels.get(middleSegment);
            POINT2 pt1 = tg.Pixels.get(middleSegment + 1);
            double dist = lineutility.CalcDistanceDouble(pt0, pt1);

            echelonSymbol = tg.get_EchelonSymbol();

            if (echelonSymbol != null) {
                stringWidthEchelonSymbol = metrics.stringWidth(echelonSymbol);
            }

            int tWidth = 0, t1Width = 0;
            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                tWidth = metrics.stringWidth(tg.get_Name());
            }
            if (tg.get_T1() != null && !tg.get_T1().isEmpty()) {
                t1Width = metrics.stringWidth(tg.get_T1());
            }

            int totalWidth = stringWidthEchelonSymbol;
            if (totalWidth < tWidth) {
                totalWidth = tWidth;
            }
            if (totalWidth < t1Width) {
                totalWidth = t1Width;
            }

            switch (tg.get_LineType()) {
                case TacticalLines.BOUNDARY:
                    if (dist < 1.25 * (totalWidth)) {
                        lineTooShort = true;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetBoundaryLineTooShort",
                    new RendererException("Failed inside GetBoundaryLineTooShort", exc));
        }
        return lineTooShort;
    }

    /**
     * Handles the line breaks for Boundary and Engineer Work Line
     *
     * @param tg
     * @param g2d
     */
    private static void AddBoundaryModifiers(TGLight tg,
                                             Graphics2D g2d,
                                             Object clipBounds) {
        try {
            int j = 0;
            double csFactor = 1d;
            Boolean foundSegment = false;
            POINT2 pt0 = null, pt1 = null, ptLast = null;
            double TLineFactor = 0, T1LineFactor = 0;
            Boolean lineTooShort = false;
            String countryCode = "";
            if(!tg.get_AS().equals("")) {
                countryCode = " (" + tg.get_AS() + ")";
            }
            if (tg.get_Client().equals("cpof3d")) {
                csFactor = 0.85d;
            }

            int middleSegment = getVisibleMiddleSegment(tg, clipBounds);
            //for (j = 0; j < tg.Pixels.size() - 1; j++) {
            for (j = middleSegment; j == middleSegment; j++) {
                /* if (tg.get_Client().equalsIgnoreCase("ge")) {
                    if (j != middleSegment) {
                        continue;
                    }
                }*/

                pt0 = tg.Pixels.get(j);
                pt1 = tg.Pixels.get(j + 1);
                if (pt0.x < pt1.x) {
                    TLineFactor = -1.3;
                    T1LineFactor = 1;
                } else if (pt0.x == pt1.x) {
                    if (pt1.y < pt0.y) {
                        TLineFactor = -1;
                        T1LineFactor = 1;
                    } else {
                        TLineFactor = 1;
                        T1LineFactor = -1;
                    }
                } else {
                    TLineFactor = 1;
                    T1LineFactor = -1.3;
                }
                //is the segment too short?
                lineTooShort = GetBoundarySegmentTooShort(tg, g2d, j);

                if (lineTooShort == false) {
                    foundSegment = true;
                    AddIntegralModifier(tg, tg.get_Name() + countryCode, aboveMiddle, TLineFactor * csFactor, j, j + 1, true);
                    //the echelon symbol
                    if (tg.get_EchelonSymbol() != null && !tg.get_EchelonSymbol().equals("")) {
                        AddIntegralModifier(tg, tg.get_EchelonSymbol(), aboveMiddle, -0.20 * csFactor, j, j + 1, true);
                    }
                    //the T1 modifier
                    AddIntegralModifier(tg, tg.get_T1(), aboveMiddle, T1LineFactor * csFactor, j, j + 1, true);
                }
            }//end for loop
            if (foundSegment == false) {
                pt0 = new POINT2();
                pt1 = new POINT2();
                // Get boundary middle segment
                final String echelonSymbol = tg.get_EchelonSymbol();
                final FontMetrics metrics = g2d.getFontMetrics();
                double modDist = 0;

                if (echelonSymbol != null) {
                    modDist = 1.5 * metrics.stringWidth(echelonSymbol);
                }

                final double segDist = lineutility.CalcDistanceDouble(tg.Pixels.get(middleSegment), tg.Pixels.get(middleSegment + 1));

                g2d.setFont(tg.get_Font());
                POINT2 midpt = lineutility.MidPointDouble(tg.Pixels.get(middleSegment), tg.Pixels.get(middleSegment + 1), 0);
                POINT2 ptTemp = null;
                if (segDist < modDist) {
                    ptTemp = lineutility.ExtendAlongLineDouble(midpt, tg.Pixels.get(middleSegment), modDist / 2);
                    pt0.x = ptTemp.x;
                    pt0.y = ptTemp.y;
                    ptTemp = lineutility.ExtendAlongLineDouble(midpt, tg.Pixels.get(middleSegment + 1), modDist / 2);
                } else {
                    ptTemp = tg.Pixels.get(middleSegment);
                    pt0.x = ptTemp.x;
                    pt0.y = ptTemp.y;
                    ptTemp = tg.Pixels.get(middleSegment + 1);
                }
                pt1.x = ptTemp.x;
                pt1.y = ptTemp.y;

                AddIntegralModifier(tg, tg.get_Name() + countryCode, aboveMiddle, TLineFactor * csFactor, middleSegment, middleSegment + 1, true);
                //the echelon symbol
                if (echelonSymbol != null && !echelonSymbol.equals("")) {
                    AddIntegralModifier(tg, echelonSymbol, aboveMiddle, -0.2020 * csFactor, middleSegment, middleSegment + 1, true);
                }
                //the T1 modifier
                AddIntegralModifier(tg, tg.get_T1(), aboveMiddle, T1LineFactor * csFactor, middleSegment, middleSegment + 1, true);
            }//end if foundSegment==false
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddBoundaryModifiers",
                    new RendererException("Failed inside AddBoundaryModifiers", exc));
        }
    }

    /**
     * added for USAS
     *
     * @param tg
     * @param metrics
     * @deprecated
     */
    private static void AddNameAboveDTG(TGLight tg, FontMetrics metrics) {
        try {
            double csFactor = 1;
            if (tg.get_Client().equals("cpof3d")) {
                csFactor = 0.667;
            }
            String label = GetCenterLabel(tg);
            POINT2 pt0 = new POINT2(tg.Pixels.get(0));
            POINT2 pt1 = new POINT2(tg.Pixels.get(1));
            int lastIndex = tg.Pixels.size() - 1;
            int nextToLastIndex = tg.Pixels.size() - 2;
            POINT2 ptLast = new POINT2(tg.Pixels.get(lastIndex));
            POINT2 ptNextToLast = new POINT2(tg.Pixels.get(nextToLastIndex));
            shiftModifierPath(tg, pt0, pt1, ptLast, ptNextToLast);
            double stringWidth = metrics.stringWidth(label + " " + tg.get_Name());
            AddIntegralAreaModifier(tg, label + " " + tg.get_Name(), toEnd, 0, pt0, pt1, false);
            pt1 = lineutility.ExtendAlongLineDouble(tg.Pixels.get(0), tg.Pixels.get(1), -1.5 * stringWidth);
            AddModifier2(tg, tg.get_DTG(), aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
            AddIntegralAreaModifier(tg, label + " " + tg.get_Name(), toEnd, 0, ptLast, ptNextToLast, false);
            pt0 = tg.Pixels.get(lastIndex);
            pt1 = lineutility.ExtendAlongLineDouble(tg.Pixels.get(lastIndex), tg.Pixels.get(nextToLastIndex), -1.5 * stringWidth);
            AddModifier2(tg, tg.get_DTG(), aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddNameAboveDTG",
                    new RendererException("Failed inside AddNameAboveDTG", exc));
        }
    }

    /**
     * shifts the path for modifiers that use toEnd to prevent vertical paths
     *
     * @param tg
     * @param pt0
     * @param pt1
     * @param ptLast
     * @param ptNextToLast
     */
    private static void shiftModifierPath(TGLight tg,
                                          POINT2 pt0,
                                          POINT2 pt1,
                                          POINT2 ptLast,
                                          POINT2 ptNextToLast) {
        try {
            POINT2 p0 = null, p1 = null;
            double last = -1.0;
            switch (tg.get_LineType()) {
                case TacticalLines.BOUNDARY:
                    for (int j = 0; j < tg.Pixels.size() - 1; j++) {
                        p0 = tg.Pixels.get(j);
                        p1 = tg.Pixels.get(j + 1);
                        //if(p0.x==p1.x)
                        if (Math.abs(p0.x - p1.x) < 1) {
                            p1.x += last;
                            last = -last;
                        }
                    }
                    break;
                case TacticalLines.PDF:
                case TacticalLines.PL:
                case TacticalLines.FEBA:
                case TacticalLines.LOA:
                case TacticalLines.LOD:
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.LDLC:
                case TacticalLines.LL:
                case TacticalLines.EWL:
                case TacticalLines.FCL:
                case TacticalLines.PLD:
                case TacticalLines.NFL:
                case TacticalLines.FLOT:
                case TacticalLines.LC:
                case TacticalLines.HOLD:
                case TacticalLines.BRDGHD:
                case TacticalLines.HOLD_GE:
                case TacticalLines.BRDGHD_GE:
                    //if (pt0 != null && pt1 != null && pt0.x == pt1.x)
                    if (pt0 != null && pt1 != null && Math.abs(pt0.x - pt1.x) < 1) {
                        pt1.x += 1;
                    }
                    //if (ptLast != null && ptNextToLast != null && ptNextToLast.x == ptLast.x)
                    if (ptLast != null && ptNextToLast != null && Math.abs(ptNextToLast.x - ptLast.x) < 1) {
                        ptNextToLast.x += 1;
                    }
                    break;
                default:
                    return;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "shiftModifierPath",
                    new RendererException("Failed inside shiftModifierPath", exc));
        }
    }

    /**
     * Adds label on line
     *
     * Replaces areasWithENY()
     *
     * @param label
     * @param tg
     * @param g2d
     * @param twoLabelOnly - true if only add two instances of label to line (used with N modifier)
     *                     Ignored if RendererSettings.TwoLabelOnly is true
     */
    private static void addModifierOnLine(String label, TGLight tg, Graphics2D g2d, boolean twoLabelOnly) {
        if (label == null || label.isEmpty()) {
            return;
        }
        try {
            if (!RendererSettings.getInstance().getTwoLabelOnly() && !twoLabelOnly) {
                FontMetrics metrics = g2d.getFontMetrics();
                final int stringWidth = metrics.stringWidth(label);
                boolean foundLongSegment = false;

                for (int j = 0; j < tg.Pixels.size() - 1; j++) {
                    POINT2 pt0 = tg.Pixels.get(j);
                    POINT2 pt1 = tg.Pixels.get(j + 1);
                    double dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    if (dist > 1.5 * stringWidth) {
                        foundLongSegment = true;
                        AddIntegralAreaModifier(tg, label, aboveMiddle, 0, pt0, pt1, false);
                    }
                }
                if (!foundLongSegment) {
                    // did not find a long enough segment
                    int middleSegment = tg.Pixels.size() / 2 - 1;
                    int middleSegment2 = tg.Pixels.size() - 2;
                    if (tg.Pixels.size() > 3) {
                        middleSegment = tg.Pixels.size() / 4;
                        middleSegment2 = 3 * tg.Pixels.size() / 4;
                    }
                    if (middleSegment != 0) {
                        AddIntegralModifier(tg, label, aboveMiddle, 0, middleSegment, middleSegment + 1, false);
                    }
                    AddIntegralModifier(tg, label, aboveMiddle, 0, middleSegment2, middleSegment2 + 1, false);
                }
            } else if (tg.Pixels.size() > 0) {
                // 2 labels one to the left and the other to the right of graphic.
                POINT2 leftPt = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                POINT2 rightPt = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);

                for (int j = 1; j < tg.Pixels.size() - 1; j++) {
                    POINT2 midPt = lineutility.MidPointDouble(tg.Pixels.get(j), tg.Pixels.get(j + 1), 0);
                    if (midPt.x <= leftPt.x) {
                        leftPt = midPt;
                    }
                    if (midPt.x >= rightPt.x) {
                        rightPt = midPt;
                    }
                }

                if (leftPt != rightPt)
                    AddAreaModifier(tg, label, aboveMiddle, 0, leftPt, leftPt);
                AddAreaModifier(tg, label, aboveMiddle, 0, rightPt, rightPt);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "addModifierOnLine",
                    new RendererException("Failed inside addModifierOnLine", exc));
        }
    }

    private static void addModifierOnLine(String label, TGLight tg, Graphics2D g2d) {
        addModifierOnLine(label, tg, g2d, false);
    }

    /**
     * Adds N modifier on line
     */
    private static void addNModifier(TGLight tg, Graphics2D g2d) {
        if (tg.isHostile()) {
            addModifierOnLine(tg.get_N(), tg, g2d, true);
        }
    }

    private static void addModifierBottomSegment(TGLight tg, String text) {
        int index = 0;
        double y = tg.Pixels.get(index).y + tg.Pixels.get(index + 1).y;
        for (int i = 1; i < tg.Pixels.size() - 1; i++) {
            if (tg.Pixels.get(i).y + tg.Pixels.get(i + 1).y > y) {
                index = i;
                y = tg.Pixels.get(index).y + tg.Pixels.get(index + 1).y;
            }
        }
        AddIntegralModifier(tg, text, aboveMiddle, 0, index, index + 1, false);
    }

    private static void addModifierTopSegment(TGLight tg, String text) {
        int index = 0;
        double y = tg.Pixels.get(index).y + tg.Pixels.get(index + 1).y;
        for (int i = 1; i < tg.Pixels.size() - 1; i++) {
            if (tg.Pixels.get(i).y + tg.Pixels.get(i + 1).y < y) {
                index = i;
                y = tg.Pixels.get(index).y + tg.Pixels.get(index + 1).y;
            }
        }
        AddIntegralModifier(tg, text, aboveMiddle, 0, index, index + 1, false);
    }

    private static void addDTG(TGLight tg, int type, double lineFactor1, double lineFactor2, POINT2 pt0, POINT2 pt1, FontMetrics metrics) {
        if (pt0 == null || pt1 == null)
            return;

        double maxDTGWidth;
        if (pt0.x == pt1.x && pt0.y == pt1.y) {
            POINT2 ptUl = new POINT2(), ptUr = new POINT2(), ptLr = new POINT2(), ptLl = new POINT2();
            GetMBR(tg, ptUl, ptUr, ptLr, ptLl);
            maxDTGWidth = lineutility.CalcDistanceDouble(ptUl, ptUr);
        } else {
            maxDTGWidth = lineutility.CalcDistanceDouble(pt0, pt1);
        }

        String dash = "";
        if (tg.get_DTG() != null && tg.get_DTG1() != null && !tg.get_DTG().isEmpty() && !tg.get_DTG1().isEmpty()) {
            dash = " - ";
        }

        String combinedDTG = tg.get_DTG() + dash + tg.get_DTG1();

        double stringWidth = metrics.stringWidth(combinedDTG);

        if (stringWidth < maxDTGWidth) {
            // Add on one line
            AddModifier(tg, combinedDTG, type, lineFactor1, pt0, pt1);
        } else {
            // add on two lines
            // Use min and max on lineFactors. Always want W1 on top. This fixes when lineFactor < 0 W1 should use lineFactor1
            AddModifier(tg, tg.get_DTG() + dash, type, Math.min(lineFactor1, lineFactor2), pt0, pt1);
            AddModifier(tg, tg.get_DTG1(), type, Math.max(lineFactor1, lineFactor2), pt0, pt1);
        }
    }

    private static int getVisibleMiddleSegment(TGLight tg, Object clipBounds) {
        int middleSegment = -1;
        try {
            Polygon clipBoundsPoly = null;
            Rectangle2D clipRect = null;
            boolean useClipRect; // true if clipBounds is Rectangle2D otherwise use clipBoundsPoly
            POINT2 pt0 = null, pt1 = null;
            double dist = 0;
            POINT2 lastPt = null;
            long lineType = tg.get_LineType();
            //we want the middle segment to be visible
            middleSegment = (tg.Pixels.size() + 1) / 2 - 1;

            boolean foundVisibleSegment = false;
            if (clipBounds == null) {
                return middleSegment;
            }

            if (ArrayList.class.isAssignableFrom(clipBounds.getClass())) {
                useClipRect = false;
                clipBoundsPoly = new Polygon();
                ArrayList<Point2D> clipArray = (ArrayList<Point2D>) clipBounds;
                for (int j = 0; j < clipArray.size(); j++) {
                    int x = (int) (clipArray.get(j)).getX();
                    int y = (int) (clipArray.get(j)).getY();
                    clipBoundsPoly.addPoint(x, y);
                }
            } else if (Rectangle2D.class.isAssignableFrom(clipBounds.getClass())) {
                useClipRect = true;
                clipRect = (Rectangle2D) clipBounds;
            } else {
                return middleSegment;
            }

            //walk through the segments to find the first visible segment from the middle
            for (int j = middleSegment; j < tg.Pixels.size() - 1; j++) {
                pt0 = tg.Pixels.get(j);
                pt1 = tg.Pixels.get(j + 1);
                dist = lineutility.CalcDistanceDouble(pt0, pt1);
                if (dist < 5) {
                    continue;
                }
                //diagnostic
                if (j > 0 && lineType == TacticalLines.BOUNDARY) {
                    if (lastPt == null) {
                        lastPt = tg.Pixels.get(j - 1);
                    }
                    if (DoublesBack(lastPt, pt0, pt1)) {
                        continue;
                    }

                    lastPt = null;
                }
                //if either of the points is within the bound then most of the segment is visible
                if (!useClipRect) {
                    if (clipBoundsPoly.contains(pt0.x, pt0.y) || clipBoundsPoly.contains(pt1.x, pt1.y)) {
                        middleSegment = j;
                        foundVisibleSegment = true;
                        break;
                    }
                } else {
                    if (clipRect.contains(pt0.x, pt0.y) || clipRect.contains(pt1.x, pt1.y)) {
                        middleSegment = j;
                        foundVisibleSegment = true;
                        break;
                    }
                }
            }

            if (!foundVisibleSegment) {
                for (int j = middleSegment; j > 0; j--) {
                    pt0 = tg.Pixels.get(j);
                    pt1 = tg.Pixels.get(j - 1);
                    dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    if (dist < 5) {
                        continue;
                    }
                    //diagnostic
                    if (lineType == TacticalLines.BOUNDARY) {
                        if (lastPt == null) {
                            lastPt = tg.Pixels.get(j - 1);
                        }

                        if ( DoublesBack(lastPt, pt0, pt1)) {
                            continue;
                        }

                        lastPt = null;
                    }
                    //if either of the points is within the bound then most of the segment is visible
                    if (!useClipRect) {
                        if (clipBoundsPoly.contains(pt0.x, pt0.y) || clipBoundsPoly.contains(pt1.x, pt1.y)) {
                            middleSegment = j - 1;
                            foundVisibleSegment = true;
                            break;
                        }
                    } else {
                        if (clipRect.contains(pt0.x, pt0.y) || clipRect.contains(pt1.x, pt1.y)) {
                            middleSegment = j - 1;
                            foundVisibleSegment = true;
                            break;
                        }
                    }
                }
            }

            if (!foundVisibleSegment) {
                middleSegment = tg.Pixels.size() / 2 - 1;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getMiddleSegment",
                    new RendererException("Failed inside getMiddleSegment", exc));
        }
        return middleSegment;
    }

    /**
     * called repeatedly by RemoveModifiers to remove modifiers which fall
     * outside the symbol MBR
     *
     * @param tg
     * @param modifierType
     */
    private static void removeModifier(TGLight tg,
                                       String modifierType) {
        try {
            int j = 0;
            Modifier2 modifier = null;
            int n = tg.Pixels.size();
            //for (j = 0; j < tg.modifiers.size(); j++)
            for (j = 0; j < n; j++) {
                modifier = tg.modifiers.get(j);

                if (modifier.textID == null) {
                    continue;
                }

                if (modifier.textID.equalsIgnoreCase(modifierType)) {
                    tg.modifiers.remove(modifier);
                    break;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "removeModifier",
                    new RendererException("Failed inside removeModifier", exc));
        }
    }

    /**
     * removes text modifiers for CPOF tactical areas which do not fit inside
     * the symbol MBR
     *
     * @param tg
     * @param g2d
     * @param isTextFlipped true if text is flipped from the last segment
     * orientation
     * @param iteration the instance count for this modifier
     */
    public static void RemoveModifiers(TGLight tg,
                                       Graphics2D g2d,
                                       boolean isTextFlipped,
                                       int iteration) {
        try {
            //CPOF clients only
            if (!tg.get_Client().equalsIgnoreCase("cpof2d") && !tg.get_Client().equalsIgnoreCase("cpof3d")) {
                return;
            }

            int j = 0;
            Polygon mbrPoly = null;
            //if it's a change 1 rectangular area then use the pixels instead of the mbr
            //because those use aboveMiddle to build angular text
            switch (tg.get_LineType()) {
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CUED_ACQUISITION:
                case TacticalLines.ACA_RECTANGULAR: //aboveMiddle modifiers: slanted text
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.CIRCULAR:
                case TacticalLines.BDZ:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.NOTACK:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                    if (tg.modifiers == null || tg.modifiers.isEmpty() || iteration != 1) {
                        return;
                    }

                    mbrPoly = new Polygon();
                    int n = tg.Pixels.size();
                    //for (j = 0; j < tg.Pixels.size(); j++)
                    for (j = 0; j < n; j++) {
                        mbrPoly.addPoint((int) tg.Pixels.get(j).x, (int) tg.Pixels.get(j).y);
                    }

                    break;
                default:    //area modifiers: horizontal text
                    if (clsUtility.isClosedPolygon(tg.get_LineType()) == false || iteration != 0) {
                        return;
                    }
                    if (tg.modifiers == null || tg.modifiers.isEmpty()) {
                        return;
                    }

                    mbrPoly = new Polygon();
                    int t = tg.Pixels.size();
                    //for (j = 0; j < tg.Pixels.size(); j++)
                    for (j = 0; j < t; j++) {
                        mbrPoly.addPoint((int) tg.Pixels.get(j).x, (int) tg.Pixels.get(j).y);
                    }
            }

            Font font = null;
            font = tg.get_Font();    //might have to change this
            if (font == null) {
                font = g2d.getFont();
            }
            g2d.setFont(font);
            FontMetrics metrics = g2d.getFontMetrics();

            double stringWidth = 0, stringHeight = 0;
            boolean wfits = true, w1fits = true, ww1fits = true, hfits = true, h1fits = true, h2fits = true;
            Modifier2 modifier = null;
            String modifierType = "";
            String s = "";
            POINT2 pt0 = null, pt1 = null, pt2 = null, pt3 = null, pt4 = null;
            double lineFactor = 0;
            double x = 0, y = 0;
            double x1 = 0, y1 = 0, x2 = 0, y2 = 0;            //logic as follows:
            //we have to loop through to determine if each modifiers fits and set its fitsMBR member
            //then run a 2nd loop to remove groups of modifiers based on whether any of the others do not fit
            //e.g. if W does not fit then remove W and W1 modifiers
            int n = tg.modifiers.size();
            //for (j = 0; j < tg.modifiers.size(); j++)
            for (j = 0; j < n; j++) {
                modifier = tg.modifiers.get(j);
                if (modifier.textID == null || modifier.textID.isEmpty()) {
                    continue;
                }

                modifierType = modifier.textID;
                lineFactor = modifier.lineFactor;

                if (isTextFlipped) {
                    lineFactor = -lineFactor;
                }

                s = modifier.text;
                if (s == null || s.equals("")) {
                    continue;
                }
                stringWidth = (double) metrics.stringWidth(s) + 1;
                stringHeight = (double) font.getSize();

                if (modifier.type == area) {
                    pt0 = modifier.textPath[0];
                    x1 = pt0.x;
                    y1 = pt0.y;
                    x = (int) x1 - (int) stringWidth / 2;
                    y = (int) y1 + (int) (stringHeight / 2) + (int) (1.25 * lineFactor * stringHeight);
                    //pt1 = modifier.textPath[1];
                    x2 = (int) x1 + (int) stringWidth / 2;
                    y2 = (int) y1 + (int) (stringHeight / 2) + (int) (1.25 * lineFactor * stringHeight);
                    if (mbrPoly.contains(x, y) && mbrPoly.contains(x2, y2)) {
                        modifier.fitsMBR = true;
                    } else {
                        modifier.fitsMBR = false;
                    }
                } else if (modifier.type == aboveMiddle) {
                    pt0 = modifier.textPath[0];
                    pt1 = modifier.textPath[1];
                    //double dist=lineutility.CalcDistanceDouble(pt0, pt1);
                    POINT2 ptCenter = lineutility.MidPointDouble(pt0, pt1, 0);
                    pt0 = lineutility.ExtendAlongLineDouble(ptCenter, pt0, stringWidth / 2);
                    pt1 = lineutility.ExtendAlongLineDouble(ptCenter, pt1, stringWidth / 2);

                    if (lineFactor >= 0) {
                        pt2 = lineutility.ExtendDirectedLine(ptCenter, pt0, pt0, 3, Math.abs((lineFactor) * stringHeight));
                    } else {
                        pt2 = lineutility.ExtendDirectedLine(ptCenter, pt0, pt0, 2, Math.abs((lineFactor) * stringHeight));
                    }

                    if (lineFactor >= 0) {
                        pt3 = lineutility.ExtendDirectedLine(ptCenter, pt1, pt1, 3, Math.abs((lineFactor) * stringHeight));
                    } else {
                        pt3 = lineutility.ExtendDirectedLine(ptCenter, pt1, pt1, 2, Math.abs((lineFactor) * stringHeight));
                    }

                    x1 = pt2.x;
                    y1 = pt2.y;
                    x2 = pt3.x;
                    y2 = pt3.y;
                    if (mbrPoly.contains(x1, y1) && mbrPoly.contains(x2, y2)) {
                        modifier.fitsMBR = true;
                    } else {
                        modifier.fitsMBR = false;
                    }
                } else {
                    modifier.fitsMBR = true;
                }
            }
            n = tg.modifiers.size();
            //for (j = 0; j < tg.modifiers.size(); j++)
            for (j = 0; j < n; j++) {
                modifier = tg.modifiers.get(j);
                if (modifier.textID == null || modifier.textID.isEmpty()) {
                    continue;
                }

                if (modifier.fitsMBR == false) {
                    if (modifier.textID.equalsIgnoreCase("W")) {
                        wfits = false;
                    } else if (modifier.textID.equalsIgnoreCase("W1")) {
                        w1fits = false;
                    } else if (modifier.textID.equalsIgnoreCase("W+W1")) {
                        ww1fits = false;
                    } else if (modifier.textID.equalsIgnoreCase("H")) {
                        hfits = false;
                    } else if (modifier.textID.equalsIgnoreCase("H1")) {
                        h1fits = false;
                    } else if (modifier.textID.equalsIgnoreCase("H2")) {
                        h2fits = false;
                    }
                }
            }
            if (wfits == false || w1fits == false) {
                removeModifier(tg, "W");
                removeModifier(tg, "W1");
            }
            if (ww1fits == false) {
                removeModifier(tg, "W+W1");
            }
            if (hfits == false || h1fits == false || h2fits == false) {
                removeModifier(tg, "H");
                removeModifier(tg, "H1");
                removeModifier(tg, "H2");
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "RemoveModifeirs",
                    new RendererException("Failed inside RemoveModifiers", exc));
        }
    }

    /**
     * Calculates a segment in the pixels middle by length to hold a string.
     *
     * @param tg
     * @param stringWidth
     * @param segPt0
     * @param segPt1
     */
    private static void getPixelsMiddleSegment(TGLight tg,
                                               double stringWidth,
                                               POINT2 segPt0,
                                               POINT2 segPt1) {
        try {
            switch (tg.get_LineType()) {
                case TacticalLines.CFL:
                    break;
                default:
                    return;
            }
            int totalLength = 0;
            int j = 0;
            double dist = 0;
            double mid = 0;
            double remainder = 0;
            POINT2 pt0 = null, pt1 = null, pt2 = null, pt3 = null;
            POINT2 midPt = null;
            //first get the total length of all the segments
            int n = tg.Pixels.size();
            //for (j = 0; j < tg.Pixels.size() - 1; j++)
            for (j = 0; j < n - 1; j++) {
                dist = lineutility.CalcDistanceDouble(tg.Pixels.get(j), tg.Pixels.get(j + 1));
                totalLength += dist;
            }
            mid = totalLength / 2;
            totalLength = 0;
            //walk thru the segments to find the middle
            //for (j = 0; j < tg.Pixels.size() - 1; j++)
            for (j = 0; j < n - 1; j++) {
                dist = lineutility.CalcDistanceDouble(tg.Pixels.get(j), tg.Pixels.get(j + 1));
                totalLength += dist;
                if (totalLength >= mid)//current segment contains the middle
                {
                    remainder = totalLength - mid;
                    pt0 = tg.Pixels.get(j);
                    pt1 = tg.Pixels.get(j + 1);
                    //calculate the pixels mid point
                    midPt = lineutility.ExtendAlongLineDouble2(pt1, pt0, remainder);
                    pt2 = lineutility.ExtendAlongLineDouble2(midPt, pt0, stringWidth / 2);
                    pt3 = lineutility.ExtendAlongLineDouble2(midPt, pt1, stringWidth / 2);
                    segPt0.x = pt2.x;
                    segPt0.y = pt2.y;
                    segPt1.x = pt3.x;
                    segPt1.y = pt3.y;
                    break;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getPixelsMidpoint",
                    new RendererException("Failed inside getPixelsMidpoint", exc));
        }
    }

    private static double getChange1Height(TGLight tg) {
        double height = 0;
        try {
            switch (tg.get_LineType()) {
                //case TacticalLines.PAA_RECTANGULAR:
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
                    return 0;
            }
            double x1 = tg.Pixels.get(0).x;
            double y1 = tg.Pixels.get(0).y;
            double x2 = tg.Pixels.get(1).x;
            double y2 = tg.Pixels.get(1).y;
            double deltax = x2 - x1;
            double deltay = y2 - y1;
            height = Math.sqrt(deltax * deltax + deltay * deltay);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getChange1Height",
                    new RendererException("Failed inside getChange1Height", exc));
        }
        return height;
    }

    /**
     * scale the line factor for closed areas
     *
     * @param tg
     */
    private static void scaleModifiers(TGLight tg) {
        try {
            if (RendererSettings.getInstance().getAutoCollapseModifiers() == false) {
                return;
            }
            if (!tg.get_Client().equalsIgnoreCase("ge")) {
                return;
            }
            //exit if there are no modifiers or it's not a closed area
            if (tg.modifiers == null || tg.modifiers.isEmpty()) {
                return;
            }
            int linetype = tg.get_LineType();
            boolean isClosedPolygon = clsUtility.isClosedPolygon(linetype);
            boolean isChange1Area = clsUtility.IsChange1Area(linetype);
            if (!isClosedPolygon && !isChange1Area) {
                return;
            }
            switch(linetype)
            {
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                    return;
                default:
                    break;
            }
            POINT2 ptUl = new POINT2(), ptUr = new POINT2(), ptLr = new POINT2(), ptLl = new POINT2();
            GetMBR(tg, ptUl, ptUr, ptLr, ptLl);
            int sz = tg.get_Font().getSize();
            //heightMBR is half the MBR height
            //double heightMBR=Math.abs(ptLr.y-ptUr.y)/2;
            double heightMBR = 0;
            double change1Height = getChange1Height(tg);
            if (change1Height <= 0) {
                heightMBR = Math.abs(ptLr.y - ptUr.y) / 2;
            } else {
                heightMBR = change1Height;
            }

            double heightModifiers = 0;
            ArrayList<Modifier2> modifiers = tg.modifiers;
            Modifier2 modifier = null;
            double minLF = Integer.MAX_VALUE;
            int j = 0;
            boolean isValid = false;
            for (j = 0; j < modifiers.size(); j++) {
                modifier = modifiers.get(j);
                //if(modifier.type == area)
                //type3Area=true;
                if (modifier.type == toEnd) {
                    continue;
                }
                if (modifier.type == aboveMiddle && isChange1Area == false) {
                    continue;
                }
                if (modifier.lineFactor < minLF) {
                    minLF = modifier.lineFactor;
                }
                isValid = true;
            }
            //if there are no 'area' modifiers then exit early
            if (!isValid) {
                return;
            }

            heightModifiers = Math.abs(minLF) * sz;
            boolean expandModifiers = false, shrinkModifiers = false;
            if (heightModifiers > heightMBR) {
                shrinkModifiers = true;
            } else if (heightModifiers < 0.5 * heightMBR) {
                expandModifiers = true;
            }

            boolean addEllipsis = false;
            //modifierE is ellipses modifier
            Modifier2 modifierE = new Modifier2();
            if (expandModifiers) {
                double factor = heightMBR / heightModifiers;
                factor = 1 + (factor - 1) / 4;
                if (factor > 2) {
                    factor = 2;
                }
                for (j = 0; j < modifiers.size(); j++) {
                    modifier = modifiers.get(j);
                    if(modifier.type==aboveMiddle)
                    {
                        if(isChange1Area==false)
                            continue;
                    }
                    else if(modifier.type!=area)
                        continue;

                    modifier.lineFactor *= factor;
                }
            } else if (shrinkModifiers) {
                double deltaLF = (heightModifiers - heightMBR) / sz;
                double newLF = 0;
                //use maxLF for the ellipsis modifier
                double maxLF = 0;
                for (j = 0; j < modifiers.size(); j++) {
                    modifier = modifiers.get(j);
                    if(modifier.type==aboveMiddle)
                    {
                        if(isChange1Area==false)
                            continue;
                    }
                    else if(modifier.type!=area)
                        continue;
                    newLF = modifier.lineFactor + deltaLF;
                    if (Math.abs(newLF * sz) >= heightMBR) {
                        //flag the modifier to remove
                        if (modifier.lineFactor > minLF) {
                            modifierE.type = modifier.type;
                            modifier.type = 7;
                            if (!modifier.text.isEmpty()) {
                                addEllipsis = true;
                            }
                        }
                        modifier.lineFactor = newLF;
                        //modifierE.type=area;
                        //modifierE.type=modifier.type;
                        modifierE.textPath = modifier.textPath;
                        continue;
                    }
                    modifier.lineFactor = newLF;
                }
                ArrayList<Modifier2> modifiers2 = new ArrayList();
                for (j = 0; j < modifiers.size(); j++) {
                    modifier = modifiers.get(j);
                    if (modifier.type != 7) {
                        if (modifier.lineFactor > maxLF) {
                            maxLF = modifier.lineFactor;
                        }
                        modifiers2.add(modifier);
                    }
                }
                if (addEllipsis) {
                    Character letter = (char) 9679;
                    String s = Character.toString(letter);
                    String echelonSymbol = s + s + s;
                    modifierE.text = echelonSymbol;
                    modifierE.lineFactor = maxLF + 1;
                    modifiers2.add(modifierE);
                }
                tg.modifiers = modifiers2;
            }   //end shrink modifiers
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "scaleModifiers",
                    new RendererException("Failed inside scaleModifiers", exc));
        }
    }

    /**
     * Calculate modifiers identical to addModifiers except use geodesic
     * calculations for the center point.
     *
     * @param tg
     * @param g2d
     * @param clipBounds
     * @param converter
     */
    public static void AddModifiersGeo(TGLight tg,
                                       Graphics2D g2d,
                                       Object clipBounds,
                                       IPointConversion converter) {
        try {
            //exit early for those not affected
            if (tg.Pixels == null || tg.Pixels.isEmpty()) {
                return;
            }
            ArrayList<POINT2> origPoints = null;
            Font font = tg.get_Font();
            if (font == null) {
                font = g2d.getFont();
            }
            g2d.setFont(font);

            boolean shiftLines = Channels.getShiftLines();
            boolean usas = false, foundSegment = false;
            double csFactor = 1d, dist = 0, dist2 = 0;//this will be used for text spacing the 3d map (CommandCight)
            POINT2 midPt = null;
            int northestPtIndex = 0;
            int southestPtIndex = 0;
            POINT2 northestPt = null;
            POINT2 southestPt = null;

            Rectangle2D clipRect = null;
            ArrayList<Point2D> clipArray = null;
            if (clipBounds != null && ArrayList.class.isAssignableFrom(clipBounds.getClass())) {
                clipArray = (ArrayList<Point2D>) clipBounds;
            }
            if (clipBounds != null && Rectangle2D.Double.class.isAssignableFrom(clipBounds.getClass())) {
                clipRect = (Rectangle2D.Double) clipBounds;
            }

            FontMetrics metrics = g2d.getFontMetrics();
            int stringWidth = 0, stringWidth2 = 0;
            String WDash = ""; // Dash between W and W1 if they're not empty
            String TSpace = "", TDash = ""; // Space or dash between label and T modifier if T isn't empty
            if (tg.get_DTG() != null && tg.get_DTG1() != null && !tg.get_DTG().isEmpty() && !tg.get_DTG1().isEmpty()) {
                WDash = " - ";
            }
            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                TSpace = " ";
                TDash = " - ";
            }

            if (tg.get_Client().equals("cpof3d")) {
                csFactor = 0.9d;
            }

            switch (tg.get_LineType()) {
                case TacticalLines.SERIES:
                case TacticalLines.STRIKWARN:
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
                case TacticalLines.DHA_REVD:
                case TacticalLines.DHA:
                case TacticalLines.EPW:
                case TacticalLines.UXO:
                case TacticalLines.FARP:
                case TacticalLines.BSA:
                case TacticalLines.DSA:
                case TacticalLines.CSA:
                case TacticalLines.RSA:
                case TacticalLines.THUNDERSTORMS:
                case TacticalLines.ICING:
                case TacticalLines.FREEFORM:
                case TacticalLines.RHA:
                case TacticalLines.LINTGT:
                case TacticalLines.LINTGTS:
                case TacticalLines.FPF:
                case TacticalLines.GAP:
                case TacticalLines.DEPICT:
                case TacticalLines.AIRHEAD:
                case TacticalLines.FSA:
                case TacticalLines.DIRATKAIR:
                case TacticalLines.OBJ:
                case TacticalLines.AO:
                case TacticalLines.ACA:
                case TacticalLines.FFA:
                case TacticalLines.PAA:
                case TacticalLines.NFA:
                case TacticalLines.RFA:
                case TacticalLines.ATI:
                case TacticalLines.CFFZ:
                case TacticalLines.CFZ:
                case TacticalLines.TBA:
                case TacticalLines.TVAR:
                case TacticalLines.KILLBOXBLUE:
                case TacticalLines.KILLBOXPURPLE:
                case TacticalLines.ZOR:
                case TacticalLines.DA:
                case TacticalLines.SENSOR:
                case TacticalLines.CENSOR:
                case TacticalLines.SMOKE:
                case TacticalLines.BATTLE:
                case TacticalLines.PNO:
                case TacticalLines.PDF:
                case TacticalLines.NAI:
                case TacticalLines.TAI:
                case TacticalLines.BASE_CAMP_REVD:
                case TacticalLines.BASE_CAMP:
                case TacticalLines.GUERILLA_BASE_REVD:
                case TacticalLines.GUERILLA_BASE:
                case TacticalLines.GENERIC_AREA:
                case TacticalLines.ATKPOS:
                case TacticalLines.ASSAULT:
                case TacticalLines.WFZ:
                case TacticalLines.OBSFAREA:
                case TacticalLines.OBSAREA:
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
                case TacticalLines.SAAFR:
                case TacticalLines.AC:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.TC:
                case TacticalLines.SC:
                case TacticalLines.LLTR:
                case TacticalLines.AIRFIELD:
                case TacticalLines.GENERAL:
                case TacticalLines.JTAA:
                case TacticalLines.SAA:
                case TacticalLines.SGAA:
                case TacticalLines.FORT_REVD:
                case TacticalLines.FORT:
                case TacticalLines.ENCIRCLE:
                case TacticalLines.ASSY:
                case TacticalLines.EA:
                case TacticalLines.DZ:
                case TacticalLines.EZ:
                case TacticalLines.LZ:
                case TacticalLines.PZ:
                case TacticalLines.LAA:
                case TacticalLines.BOUNDARY:
                case TacticalLines.MINED:
                case TacticalLines.FENCED:
                case TacticalLines.PL:
                case TacticalLines.FEBA:
                case TacticalLines.FCL:
                case TacticalLines.HOLD:
                case TacticalLines.BRDGHD:
                case TacticalLines.HOLD_GE:
                case TacticalLines.BRDGHD_GE:
                case TacticalLines.LOA:
                case TacticalLines.LOD:
                case TacticalLines.LL:
                case TacticalLines.EWL:
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.LDLC:
                case TacticalLines.PLD:
                case TacticalLines.NFL:
                case TacticalLines.MFP:
                case TacticalLines.FSCL:
                case TacticalLines.BCL_REVD:
                case TacticalLines.BCL:
                case TacticalLines.ICL:
                case TacticalLines.IFF_OFF:
                case TacticalLines.IFF_ON:
                case TacticalLines.GENERIC_LINE:
                case TacticalLines.CFL:
                case TacticalLines.RFL:
                case TacticalLines.FLOT:
                case TacticalLines.LC:
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.IL:
                case TacticalLines.DRCL:
                case TacticalLines.RETIRE:
                case TacticalLines.FPOL:
                case TacticalLines.RPOL:
                case TacticalLines.WITHDRAW:
                case TacticalLines.WDRAWUP:
                case TacticalLines.BEARING:
                case TacticalLines.BEARING_J:
                case TacticalLines.BEARING_RDF:
                case TacticalLines.ELECTRO:
                case TacticalLines.BEARING_EW:
                case TacticalLines.ACOUSTIC:
                case TacticalLines.ACOUSTIC_AMB:
                case TacticalLines.TORPEDO:
                case TacticalLines.OPTICAL:
                case TacticalLines.RIP:
                case TacticalLines.BOMB:
                case TacticalLines.ZONE:
                case TacticalLines.AT:
                case TacticalLines.STRONG:
                case TacticalLines.MSDZ:
                case TacticalLines.SCREEN:
                case TacticalLines.COVER:
                case TacticalLines.GUARD:
                case TacticalLines.DELAY:
                case TacticalLines.TGMF:
                case TacticalLines.BIO:
                case TacticalLines.CHEM:
                case TacticalLines.NUC:
                case TacticalLines.RAD:
                case TacticalLines.MINE_LINE:
                case TacticalLines.ANCHORAGE_LINE:
                case TacticalLines.ANCHORAGE_AREA:
                case TacticalLines.SPT:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.MAIN:
                case TacticalLines.DIRATKSPT:
                case TacticalLines.DIRATKGND:
                case TacticalLines.LAUNCH_AREA:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CIRCULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                case TacticalLines.LINE:
                case TacticalLines.ASLTXING:
                    origPoints = lineutility.getDeepCopy(tg.Pixels);
                    break;
                default:    //exit early for those not applicable
                    return;
            }

            int linetype = tg.get_LineType();
            int j = 0, k = 0;
            double x = 0, y = 0;

            int lastIndex = tg.Pixels.size() - 1;
            int nextToLastIndex = tg.Pixels.size() - 2;
            POINT2 pt0 = new POINT2(tg.Pixels.get(0));
            POINT2 pt1 = null;
            POINT2 pt2 = null, pt3 = null;
            POINT2 ptLast = new POINT2(tg.Pixels.get(lastIndex));
            POINT2 ptNextToLast = null;
            double DPIScaleFactor = RendererSettings.getInstance().getDeviceDPI() / 96.0;

            if (lastIndex > 0) {
                ptNextToLast = new POINT2(tg.Pixels.get(nextToLastIndex));
            }

            if (tg.Pixels.size() > 1) {
                pt1 = new POINT2(tg.Pixels.get(1));
            }

            //prevent vertical paths for modifiers that use toEnd
            shiftModifierPath(tg, pt0, pt1, ptLast, ptNextToLast);

            String label = GetCenterLabel(tg);
            String v=tg.get_V();
            String ap=tg.get_AP();
            Object[] pts = tg.Pixels.toArray();
            //need this for areas and some lines
            POINT2 ptCenter = null;
            if (converter != null) //cpof uses latlonconverter so cpof passes null for this
            {
                ptCenter = mdlGeodesic.geodesic_center(tg.LatLongs);
                if (ptCenter != null) {
                    Point2D pt22 = converter.GeoToPixels(new Point2D.Double(ptCenter.x, ptCenter.y));
                    ptCenter.x = pt22.getX();
                    ptCenter.y = pt22.getY();
                } else {
                    ptCenter = lineutility.CalcCenterPointDouble2(pts, pts.length);
                }
            } else {
                ptCenter = lineutility.CalcCenterPointDouble2(pts, pts.length);
            }

            int middleSegment = (tg.Pixels.size() + 1) / 2 - 1;
            int middleSegment2 = 0;

            if (clipRect != null) {
                middleSegment = getVisibleMiddleSegment(tg, clipRect);
            } else if (clipArray != null) {
                middleSegment = getVisibleMiddleSegment(tg, clipArray);
            }
            if (tg.Pixels.size() > 2) {
                pt2 = tg.Pixels.get(2);
            }
            if (tg.Pixels.size() > 3) {
                pt3 = tg.Pixels.get(3);
            }
            double TLineFactor = 0, T1LineFactor = 0;
            POINT2 lr = new POINT2(tg.Pixels.get(0));
            POINT2 ll = new POINT2(tg.Pixels.get(0));
            POINT2 ul = new POINT2(tg.Pixels.get(0));
            POINT2 ur = new POINT2(tg.Pixels.get(0));
            int index = 0;
            int nextIndex = 0;
            int size = tg.Pixels.size();
            Line2D line = null;

            double dAngle0, dAngle1;
            int stringHeight;

            switch (linetype) {
                case TacticalLines.PL:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), toEnd, T1LineFactor, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), toEnd, T1LineFactor, ptLast, ptNextToLast, false);
                    break;
                case TacticalLines.FEBA:
                    AddIntegralAreaModifier(tg, label, toEnd, 0, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label, toEnd, 0, ptLast, ptNextToLast, false);
                    break;
                // T before label
                case TacticalLines.FSCL:
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    pt2 = tg.Pixels.get(tg.Pixels.size() - 1);
                    pt3 = tg.Pixels.get(tg.Pixels.size() - 2);
                    dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    dist2 = lineutility.CalcDistanceDouble(pt2, pt3);
                    stringWidth = (int) ((double) metrics.stringWidth(tg.get_Name() + " " + label));
                    stringWidth2 = (int) ((double) metrics.stringWidth(tg.get_DTG()));
                    if (stringWidth2 > stringWidth) {
                        stringWidth = stringWidth2;
                    }

                    if (tg.Pixels.size() == 2) //one segment
                    {
                        pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                        AddModifier2(tg, tg.get_Name() + " " + label , aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        if (dist > 3.5 * stringWidth)//was 28stringwidth+5
                        {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_Name() + " " + label, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    } else //more than one semgent
                    {
                        double dist3 = lineutility.CalcDistanceDouble(pt0, pt2);
                        if (dist > stringWidth + 5 || dist >= dist2 || dist3 > stringWidth + 5) {
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_Name() + " " + label, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                        if (dist2 > stringWidth + 5 || dist2 > dist || dist3 > stringWidth + 5) {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_Name() + " " + label, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    }
                    break;
                // T after label
                case TacticalLines.ICL:
                case TacticalLines.NFL:
                case TacticalLines.BCL_REVD:
                case TacticalLines.RFL:
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    pt2 = tg.Pixels.get(tg.Pixels.size() - 1);
                    pt3 = tg.Pixels.get(tg.Pixels.size() - 2);
                    dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    dist2 = lineutility.CalcDistanceDouble(pt2, pt3);
                    stringWidth = (int) ((double) metrics.stringWidth(tg.get_Name() + " " + label));
                    stringWidth2 = (int) ((double) metrics.stringWidth(tg.get_DTG()));
                    if (stringWidth2 > stringWidth) {
                        stringWidth = stringWidth2;
                    }

                    if (tg.Pixels.size() == 2) //one segment
                    {
                        pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                        AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        if (dist > 3.5 * stringWidth)//was 28stringwidth+5
                        {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    } else //more than one semgent
                    {
                        double dist3 = lineutility.CalcDistanceDouble(pt0, pt2);
                        if (dist > stringWidth + 5 || dist >= dist2 || dist3 > stringWidth + 5) {
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                        if (dist2 > stringWidth + 5 || dist2 > dist || dist3 > stringWidth + 5) {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    }
                    break;
                case TacticalLines.BCL:
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    pt2 = tg.Pixels.get(tg.Pixels.size() - 1);
                    pt3 = tg.Pixels.get(tg.Pixels.size() - 2);
                    dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    dist2 = lineutility.CalcDistanceDouble(pt2, pt3);
                    String TMod = ""; // Don't add parenthesis if T modifier is empty
                    if (tg.get_Name() != null && !tg.get_Name().isEmpty())
                        TMod = " (" + tg.get_Name() + ")";
                    stringWidth = (int) ((double) metrics.stringWidth(label + TMod));
                    stringWidth2 = (int) ((double) metrics.stringWidth(tg.get_DTG()));
                    if (stringWidth2 > stringWidth) {
                        stringWidth = stringWidth2;
                    }

                    if (tg.Pixels.size() == 2) //one segment
                    {
                        pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                        AddModifier2(tg, label + TMod, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        if (dist > 3.5 * stringWidth)//was 28stringwidth+5
                        {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TMod, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    } else //more than one semgent
                    {
                        double dist3 = lineutility.CalcDistanceDouble(pt0, pt2);
                        if (dist > stringWidth + 5 || dist >= dist2 || dist3 > stringWidth + 5) {
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TMod, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                        if (dist2 > stringWidth + 5 || dist2 > dist || dist3 > stringWidth + 5) {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, label + TMod, aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    }
                    break;
                case TacticalLines.DIRATKSPT:
                case TacticalLines.DIRATKAIR:
                case TacticalLines.DIRATKGND:
                    midPt = lineutility.MidPointDouble(pt0, pt1, 0);
                    //midPt=lineutility.MidPointDouble(pt0, midPt, 0);
                    AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 0, pt0, midPt, false);
                    addDTG(tg, aboveMiddle, csFactor, 2 * csFactor, pt0, pt1, metrics);
                    break;
                case TacticalLines.SPT:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.MAIN:
                    if (tg.Pixels.size() == 3) //one segment
                    {
                        midPt = lineutility.MidPointDouble(pt0, pt1, 0);
                        AddIntegralAreaModifier(tg, tg.get_DTG() + WDash, aboveMiddle, 0, midPt, midPt, false);
                        AddIntegralAreaModifier(tg, tg.get_DTG1(), aboveMiddle, csFactor, midPt, midPt, false);
                        AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 2 * csFactor, midPt, midPt, false);

                    } else if (tg.Pixels.size() == 4) //2 segments
                    {
                        midPt = lineutility.MidPointDouble(pt1, pt2, 0);
                        AddIntegralAreaModifier(tg,tg.get_DTG() + WDash, aboveMiddle, 0, midPt, midPt, false);
                        AddIntegralAreaModifier(tg, tg.get_DTG1(), aboveMiddle, csFactor, midPt, midPt, false);
                        AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 2 * csFactor, midPt, midPt, false);
                    } else // 3 or more segments
                    {
                        midPt = lineutility.MidPointDouble(pt1, pt2, 0);
                        AddIntegralAreaModifier(tg, tg.get_DTG() + WDash, aboveMiddle, -csFactor / 2, midPt, midPt, false);
                        AddIntegralAreaModifier(tg, tg.get_DTG1(), aboveMiddle, csFactor / 2, midPt, midPt, false);
                        midPt = lineutility.MidPointDouble(pt2, pt3, 0);
                        AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, -csFactor / 2, midPt, midPt, false);
                    }
                    break;
                case TacticalLines.LL:
                case TacticalLines.LOD:
                case TacticalLines.LDLC:
                case TacticalLines.PLD:
                case TacticalLines.RELEASE:
                case TacticalLines.HOL:
                case TacticalLines.BHL:
                case TacticalLines.FCL:
                case TacticalLines.HOLD:
                case TacticalLines.BRDGHD:
                case TacticalLines.HOLD_GE:
                case TacticalLines.BRDGHD_GE:
                case TacticalLines.LOA:
                case TacticalLines.IFF_OFF:
                case TacticalLines.IFF_ON:
                    AddIntegralAreaModifier(tg, label, aboveEnd, -csFactor, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label, aboveEnd, -csFactor, ptLast, ptNextToLast, false);
                    break;
                case TacticalLines.EWL:
                    AddIntegralAreaModifier(tg, label, aboveEnd, -csFactor, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label, aboveEnd, -csFactor, ptLast, ptNextToLast, false);
                    tg.set_EchelonSymbol("");
                    if (clipRect != null) {
                        AddBoundaryModifiers(tg, g2d, clipRect);
                    } else {
                        AddBoundaryModifiers(tg, g2d, clipArray);
                    }
                    break;
                case TacticalLines.AIRFIELD:
                    ur = new POINT2();
                    ul = new POINT2();
                    ll = new POINT2();
                    lr = new POINT2();
                    Modifier2.GetMBR(tg, ul, ur, lr, ll);
                    stringWidth = metrics.stringWidth(tg.get_H());
                    pt0.x = ur.x + stringWidth / 2 + 1;
                    //pt0.x=ptUr.x+1;
                    //pt0.y=(ptUr.y+ptLr.y)/2-metrics.getFont().getSize()
                    pt0.y = (ur.y + lr.y) / 2 - font.getSize();
                    AddIntegralAreaModifier(tg, tg.get_H(), area, csFactor, pt0, pt0, false);
                    break;
                case TacticalLines.LAUNCH_AREA:
                case TacticalLines.DEFENDED_AREA_CIRCULAR:
                    AddIntegralAreaModifier(tg, label + TDash + tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.JTAA:
                case TacticalLines.SAA:
                case TacticalLines.SGAA:
                    addNModifier(tg, g2d);
                    AddIntegralAreaModifier(tg, label + TDash + tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    addDTG(tg, area, csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.FORT:
                case TacticalLines.ZONE:
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.BDZ:
                    AddIntegralAreaModifier(tg, label, area, 0, pt0, pt0, false);
                    break;
                case TacticalLines.ASSAULT:
                case TacticalLines.ATKPOS:
                case TacticalLines.OBJ:
                case TacticalLines.NAI:
                case TacticalLines.TAI:
                case TacticalLines.BASE_CAMP_REVD:
                case TacticalLines.GUERILLA_BASE_REVD:
                case TacticalLines.ASSY:
                case TacticalLines.EA:
                case TacticalLines.DZ:
                case TacticalLines.EZ:
                case TacticalLines.LZ:
                case TacticalLines.PZ:
                case TacticalLines.AO:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.BASE_CAMP:
                case TacticalLines.GUERILLA_BASE:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, -1 * csFactor, ptCenter, ptCenter, false);
                    AddModifier(tg, tg.get_H(), area, 0, ptCenter, ptCenter);
                    addDTG(tg, area, 1 * csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                    addNModifier(tg, g2d);
                    addModifierBottomSegment(tg, tg.get_EchelonSymbol());
                    break;
                case TacticalLines.GENERIC_AREA:
                    AddIntegralAreaModifier(tg, tg.get_H() + " " + tg.get_Name(), area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    addDTG(tg, area, 0.5 * csFactor, 1.5 * csFactor, ptCenter, ptCenter, metrics);
                    addNModifier(tg, g2d);
                    break;
                case TacticalLines.AIRHEAD:
                    GetMBR(tg, ul, ur, lr, ll);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, csFactor, ll, lr, false);
                    break;
                case TacticalLines.AC:
                case TacticalLines.LLTR:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.TC:
                case TacticalLines.SAAFR:
                case TacticalLines.SC:
                    AddIntegralModifier(tg, "Name: " + tg.get_Name(), aboveMiddle, -7 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, "Width: " + tg.get_AM(), aboveMiddle, -6 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, "Min Alt: " + tg.get_X(), aboveMiddle, -5 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, "Max Alt: " + tg.get_X1(), aboveMiddle, -4 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, "DTG Start: " + tg.get_DTG(), aboveMiddle, -3 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, "DTG End: " + tg.get_DTG1(), aboveMiddle, -2 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, 0, middleSegment, middleSegment + 1, false);
                    break;
                case TacticalLines.BEARING_J:
                case TacticalLines.BEARING_RDF:
                case TacticalLines.BEARING:
                case TacticalLines.ELECTRO:
                case TacticalLines.BEARING_EW:
                case TacticalLines.ACOUSTIC:
                case TacticalLines.ACOUSTIC_AMB:
                case TacticalLines.TORPEDO:
                case TacticalLines.OPTICAL:
                    midPt = lineutility.MidPointDouble(pt0, pt1, 0);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, 0, midPt, midPt, true);
                    pt3 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 3, font.getSize() / 2.0);
                    AddIntegralAreaModifier(tg, tg.get_H(), aboveMiddle, 1, pt3, pt3, true);
                    break;
                case TacticalLines.ACA:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, -3 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_T1(), area, -2 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "MIN ALT: " + tg.get_X(), area, -1 * csFactor, ptCenter, ptCenter, false, "H");
                    AddIntegralAreaModifier(tg, "MAX ALT: " + tg.get_X1(), area, 0, ptCenter, ptCenter, false, "H1");
                    AddIntegralAreaModifier(tg, tg.get_Location(), area, 1 * csFactor, ptCenter, ptCenter, false, "H2");
                    addDTG(tg, area, 2 * csFactor, 3 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.MFP:
                    pt0 = tg.Pixels.get(middleSegment);
                    pt1 = tg.Pixels.get(middleSegment + 1);
                    AddIntegralModifier(tg, label, aboveMiddle, 0, middleSegment, middleSegment + 1, true);
                    AddIntegralModifier(tg, tg.get_DTG() + WDash, aboveEnd, 1 * csFactor, 0, 1, false);
                    AddIntegralModifier(tg, tg.get_DTG1(), aboveEnd, 2 * csFactor, 0, 1, false);
                    break;
                case TacticalLines.LINTGT:
                    AddIntegralModifier(tg, ap, aboveMiddle, -0.7 * csFactor, middleSegment, middleSegment + 1, false);
                    break;
                case TacticalLines.LINTGTS:
                    AddIntegralModifier(tg, ap, aboveMiddle, -0.7 * csFactor, middleSegment, middleSegment + 1, false);
                    AddIntegralModifier(tg, label, aboveMiddle, 0.7 * csFactor, middleSegment, middleSegment + 1, false);
                    break;
                case TacticalLines.FPF:
                    AddIntegralModifier(tg, ap, aboveMiddle, -0.7 * csFactor, 0, 1, false);
                    AddIntegralModifier(tg, label, aboveMiddle, .7 * csFactor, 0, 1, false);
                    AddIntegralModifier(tg, tg.get_T1(), aboveMiddle, 1.7 * csFactor, 0, 1, false);
                    AddIntegralModifier(tg, v, aboveMiddle, 2.7 * csFactor, 0, 1, false);
                    break;
                case TacticalLines.AT:
                    AddIntegralAreaModifier(tg, ap, area, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CIRCULAR:
                    AddIntegralAreaModifier(tg, ap, area, 0, pt0, pt0, false);
                    break;
                case TacticalLines.RECTANGULAR_TARGET:
                    stringWidth = metrics.stringWidth(tg.get_Name());
                    POINT2 offsetCenterPoint = new POINT2(ptCenter.x + ((double) stringWidth) / 2.0, ptCenter.y);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -1 * csFactor, offsetCenterPoint, offsetCenterPoint, false);
                    break;
                case TacticalLines.SMOKE:
                    AddIntegralAreaModifier(tg, ap, area, -csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, label, area, 0, ptCenter, ptCenter, false);
                    addDTG(tg, area, 1 * csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.LINE:
                    AddIntegralModifier(tg, tg.get_Name(), aboveMiddle, csFactor, middleSegment, middleSegment + 1, false);
                    break;
                case TacticalLines.MINED:
                    if (tg.isHostile()) {
                        pt1 = lineutility.MidPointDouble(pt0, pt1, 0);
                        AddIntegralAreaModifier(tg, tg.get_N(), aboveMiddle, 0, pt0, pt1, true);
                        if (middleSegment != 0) {
                            pt0 = tg.Pixels.get(middleSegment);
                            pt1 = tg.Pixels.get(middleSegment + 1);
                            pt1 = lineutility.MidPointDouble(pt0, pt1, 0);
                            AddIntegralAreaModifier(tg, tg.get_N(), aboveMiddle, 0, pt0, pt1, true);
                        }
                    }
                    GetMBR(tg, ul, ur, lr, ll);
                    AddIntegralAreaModifier(tg, tg.get_H(), aboveMiddle, -1.5 * csFactor, ul, ur, false);
                    AddIntegralAreaModifier(tg, tg.get_DTG(), aboveMiddle, 1.5 * csFactor, ll, lr, false);
                    addModifierOnLine("M", tg, g2d);
                    AddIntegralAreaModifier(tg, getImageModifier(tg), areaImage, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.FENCED:
                    if (tg.isHostile()) {
                        pt1 = lineutility.MidPointDouble(pt0, pt1, 0);
                        AddIntegralAreaModifier(tg, tg.get_N(), aboveMiddle, 0, pt0, pt1, true);
                        if (middleSegment != 0) {
                            pt0 = tg.Pixels.get(middleSegment);
                            pt1 = tg.Pixels.get(middleSegment + 1);
                            pt1 = lineutility.MidPointDouble(pt0, pt1, 0);
                            AddIntegralAreaModifier(tg, tg.get_N(), aboveMiddle, 0, pt0, pt1, true);
                        }
                    }
                    addModifierOnLine("M", tg, g2d);
                    AddIntegralAreaModifier(tg, getImageModifier(tg), areaImage, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.ASLTXING:
                    if (tg.Pixels.get(1).y > tg.Pixels.get(0).y) {
                        pt0 = tg.Pixels.get(1);
                        pt1 = tg.Pixels.get(3);
                        pt2 = tg.Pixels.get(0);
                        pt3 = tg.Pixels.get(2);
                    } else {
                        pt0 = tg.Pixels.get(0);
                        pt1 = tg.Pixels.get(2);
                        pt2 = tg.Pixels.get(1);
                        pt3 = tg.Pixels.get(3);
                    }
                    pt2 = lineutility.ExtendAlongLineDouble2(pt0, pt2, -20);
                    pt3 = lineutility.ExtendAlongLineDouble2(pt1, pt3, -20);
                    addDTG(tg, aboveMiddle, 0, csFactor, pt2, pt3, metrics);
                    break;
                case TacticalLines.SERIES:
                case TacticalLines.DRCL:
                    addModifierTopSegment(tg, tg.get_Name());
                    break;
                case TacticalLines.STRIKWARN:
                    AddIntegralModifier(tg, "1", aboveMiddle, 0, index, index + 1, true);
                    AddIntegralModifier(tg, "2", aboveMiddle, 0, size/2, size/2 + 1, true);
                    break;
                case TacticalLines.SCREEN:
                case TacticalLines.COVER:
                case TacticalLines.GUARD:
                    if (tg.Pixels.size() == 4) {
                        pt1 = new POINT2(tg.Pixels.get(1));
                        pt2 = new POINT2(tg.Pixels.get(2));
                        AddIntegralAreaModifier(tg, label, area, 0, pt1, pt1, true);
                        AddIntegralAreaModifier(tg, label, area, 0, pt2, pt2, true);
                    } else {
                        stringHeight = (int) (0.5 * (double) font.getSize());
                        dAngle0 = Math.atan2(tg.Pixels.get(0).y - tg.Pixels.get(1).y, tg.Pixels.get(0).x - tg.Pixels.get(1).x);
                        dAngle1 = Math.atan2(tg.Pixels.get(0).y - tg.Pixels.get(2).y, tg.Pixels.get(0).x - tg.Pixels.get(2).x);
                        pt0 = new POINT2(tg.Pixels.get(0));
                        pt0.x -= 30 * Math.cos(dAngle0);
                        pt0.y -= 30 * Math.sin(dAngle0) + stringHeight;
                        pt1 = new POINT2(tg.Pixels.get(0));
                        pt1.x -= 30 * Math.cos(dAngle1);
                        pt1.y -= 30 * Math.sin(dAngle1) + stringHeight;
                        AddIntegralAreaModifier(tg, label, area, 0, pt0, pt0, true);
                        AddIntegralAreaModifier(tg, label, area, 0, pt1, pt1, true);
                    }
                    break;
                case TacticalLines.MSR_ONEWAY:
                case TacticalLines.ASR_ONEWAY:
                case TacticalLines.ROUTE_ONEWAY:
                case TacticalLines.MSR_TWOWAY:
                case TacticalLines.ASR_TWOWAY:
                case TacticalLines.MSR_ALT:
                case TacticalLines.ASR_ALT:
                case TacticalLines.ROUTE_ALT:
                    stringWidth = (int) (1.5 * (double) metrics.stringWidth(label + TSpace + tg.get_Name()));
                    double arrowOffset = 10 * DPIScaleFactor;
                    if (linetype == TacticalLines.MSR_TWOWAY || linetype == TacticalLines.ASR_TWOWAY)
                        arrowOffset = 25 * DPIScaleFactor;
                    boolean isAlt = linetype == TacticalLines.MSR_ALT || linetype == TacticalLines.ASR_ALT || linetype == TacticalLines.ROUTE_ALT;
                    if (isAlt) {
                        stringWidth2 = (int) (1.5 * (double) metrics.stringWidth("ALT"));
                        if (stringWidth2 > stringWidth) {
                            stringWidth = stringWidth2;
                        }
                    }

                    foundSegment = false;
                    //acevedo - 11/30/2017 - adding option to render only 2 labels.
                    if (RendererSettings.getInstance().getTwoLabelOnly() == false) {
                        for (j = 0; j < tg.Pixels.size() - 1; j++) {
                            pt0 = tg.Pixels.get(j);
                            pt1 = tg.Pixels.get(j + 1);
                            dist = lineutility.CalcDistanceDouble(pt0, pt1);
                            int arrowSide = arraysupport.SupplyRouteArrowSide(pt0, pt1);
                            if (dist < stringWidth) {
                                continue;
                            } else {
                                if (arrowSide == 1 || arrowSide == 2) {
                                    // Shift points to account for arrow shift with DPI
                                    pt0 = lineutility.ExtendDirectedLine(pt1, pt0, pt0, arrowSide, arrowOffset);
                                    pt1 = lineutility.ExtendDirectedLine(pt1, pt0, pt1, arrowSide, arrowOffset);
                                    AddModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1.7 * csFactor, pt0, pt1);
                                    if (isAlt)
                                        AddModifier(tg, "ALT", aboveMiddle, 0, pt0, pt1);
                                } else {
                                    AddModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1);
                                    if (isAlt) {
                                        pt0 = lineutility.ExtendDirectedLine(pt1, pt0, pt0, arrowSide, arrowOffset);
                                        pt1 = lineutility.ExtendDirectedLine(pt1, pt0, pt1, arrowSide, arrowOffset);
                                        AddModifier(tg, "ALT", aboveMiddle, 0, pt0, pt1);
                                    }
                                }
                                foundSegment = true;
                            }
                        }
                        if (foundSegment == false) {
                            pt0 = tg.Pixels.get(middleSegment);
                            pt1 = tg.Pixels.get(middleSegment + 1);
                            int arrowSide = arraysupport.SupplyRouteArrowSide(pt0, pt1);
                            if (arrowSide == 1 || arrowSide == 2) {
                                // Shift points to account for arrow shift with DPI
                                pt0 = lineutility.ExtendDirectedLine(pt1, pt0, pt0, arrowSide, arrowOffset);
                                pt1 = lineutility.ExtendDirectedLine(pt1, pt0, pt1, arrowSide, arrowOffset);
                                AddModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1.7 * csFactor, pt0, pt1);
                                if (isAlt)
                                    AddModifier(tg, "ALT", aboveMiddle, 0, pt0, pt1);
                            } else {
                                AddModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1);
                                if (isAlt) {
                                    pt0 = lineutility.ExtendDirectedLine(pt1, pt0, pt0, arrowSide, arrowOffset);
                                    pt1 = lineutility.ExtendDirectedLine(pt1, pt0, pt1, arrowSide, arrowOffset);
                                    AddModifier(tg, "ALT", aboveMiddle, 0, pt0, pt1);
                                }
                            }
                        }
                    }
                    else {
                        // 2 labels one to the north and the other to the south of graphic.
                        northestPtIndex = 0;
                        northestPt = tg.Pixels.get(northestPtIndex);
                        southestPtIndex = 0;
                        southestPt = tg.Pixels.get(southestPtIndex);

                        for (j = 0; j < tg.Pixels.size() - 1; j++) {
                            pt0 = tg.Pixels.get(j);
                            if (pt0.y >= northestPt.y) {
                                northestPt = pt0;
                                northestPtIndex = j;
                            }
                            if (pt0.y <= southestPt.y) {
                                southestPt = pt0;
                                southestPtIndex = j;
                            }
                        }

                        AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1.7 * csFactor, northestPtIndex, northestPtIndex + 1, false);
                        if (isAlt)
                            AddIntegralModifier(tg, "ALT", aboveMiddle, -0.7 * csFactor, northestPtIndex, northestPtIndex + 1, false);

                        if (northestPtIndex != southestPtIndex) {
                            AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1.7 * csFactor, southestPtIndex, southestPtIndex + 1, false);
                            if (isAlt)
                                AddIntegralModifier(tg, "ALT", aboveMiddle, -0.7 * csFactor, southestPtIndex, southestPtIndex + 1, false);
                        }
                    }//else
                    break;
                case TacticalLines.DHA_REVD:
                    AddIntegralAreaModifier(tg, "DETAINEE", area, -1.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "HOLDING", area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "AREA", area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 1.5 * csFactor, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.EPW:
                    AddIntegralAreaModifier(tg, "EPW", area, -1.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "HOLDING", area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "AREA", area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 1.5 * csFactor, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.UXO:
                    addModifierOnLine("UXO", tg, g2d);
                    break;
                case TacticalLines.GENERAL:
                    addNModifier(tg, g2d);
                    break;
                case TacticalLines.DHA:
                case TacticalLines.FARP:
                    AddIntegralAreaModifier(tg, label, area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.BSA:
                case TacticalLines.DSA:
                case TacticalLines.CSA:
                case TacticalLines.RSA:
                    AddIntegralAreaModifier(tg, label, area, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.RHA:
                    AddIntegralAreaModifier(tg, "REFUGEE", area, -1.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "HOLDING", area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, "AREA", area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 1.5 * csFactor, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.MSR:
                case TacticalLines.ASR:
                case TacticalLines.ROUTE:
                    //AddIntegralModifier(tg, label + tg.get_Name(), aboveMiddle, -1*csFactor, middleSegment, middleSegment + 1,false);
                    foundSegment = false;
                    //acevedo - 11/30/2017 - adding option to render only 2 labels.
                    if (RendererSettings.getInstance().getTwoLabelOnly() == false) {
                        for (j = 0; j < tg.Pixels.size() - 1; j++) {
                            pt0 = tg.Pixels.get(j);
                            pt1 = tg.Pixels.get(j + 1);
                            stringWidth = (int) (1.5 * (double) metrics.stringWidth(label + TSpace + tg.get_Name()));
                            dist = lineutility.CalcDistanceDouble(pt0, pt1);
                            if (dist < stringWidth) {
                                continue;
                            } else {
                                AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1 * csFactor, j, j + 1, false);
                                foundSegment = true;
                            }
                        }
                        if (foundSegment == false) {
                            AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, -1 * csFactor, middleSegment, middleSegment + 1, false);
                        }
                    }
                    else {
                        // 2 labels one to the north and the other to the south of graphic.
                        for (j = 0; j < tg.Pixels.size()  ; j++) {
                            pt0 = tg.Pixels.get(j);

                            if (northestPt == null)
                            {
                                northestPt = pt0;
                                northestPtIndex = j;
                            }
                            if (southestPt == null)
                            {
                                southestPt = pt0;
                                southestPtIndex = j;
                            }
                            if (pt0.y >= northestPt.y)
                            {
                                northestPt = pt0;
                                northestPtIndex = j;
                            }

                            if (pt0.y <= southestPt.y)
                            {
                                southestPt = pt0;
                                southestPtIndex = j;
                            }
                        }//for
                        middleSegment = northestPtIndex;
                        middleSegment2 = southestPtIndex;

                        if (middleSegment  == tg.Pixels.size() -1) {
                            middleSegment-=1;
                        }
                        if (middleSegment2  == tg.Pixels.size() -1) {
                            middleSegment2-=1;
                        }
                        if (middleSegment == middleSegment2) {
                            middleSegment2-=1;
                        }

                        // if (middleSegment != middleSegment2) {
                        AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, 0, middleSegment, middleSegment + 1, false);
                        //}
                        AddIntegralModifier(tg, label + TSpace + tg.get_Name(), aboveMiddle, 0, middleSegment2, middleSegment2 + 1, false);

                    }//else
                    break;
                case TacticalLines.GAP:
                    if (tg.Pixels.get(1).y > tg.Pixels.get(0).y) {
                        pt0 = tg.Pixels.get(1);
                        pt1 = tg.Pixels.get(3);
                        pt2 = tg.Pixels.get(0);
                        pt3 = tg.Pixels.get(2);
                    } else {
                        pt0 = tg.Pixels.get(0);
                        pt1 = tg.Pixels.get(2);
                        pt2 = tg.Pixels.get(1);
                        pt3 = tg.Pixels.get(3);
                    }
                    pt2 = lineutility.ExtendAlongLineDouble2(pt0, pt2, -20);
                    pt3 = lineutility.ExtendAlongLineDouble2(pt1, pt3, -20);
                    AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 0, pt0, pt1, false);
                    addDTG(tg, aboveMiddle, 0, csFactor, pt2, pt3, metrics);
                    break;
                case TacticalLines.BIO:
                case TacticalLines.CHEM:
                case TacticalLines.NUC:
                case TacticalLines.RAD:
                    AddIntegralAreaModifier(tg, getImageModifier(tg), areaImage, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.ANCHORAGE_LINE:
                    AddIntegralAreaModifier(tg, getImageModifier(tg), aboveMiddle, -0.15 * csFactor, tg.Pixels.get(middleSegment), tg.Pixels.get(middleSegment + 1), false);
                    break;
                case TacticalLines.ANCHORAGE_AREA:
                    // Add anchor on segment with lowest midpoint
                    y = pt0.y + pt1.y;
                    index = 0;
                    for (j = 1; j < size - 1; j++) {
                        if (y < tg.Pixels.get(j).y + tg.Pixels.get(j + 1).y) {
                            index = j;
                            y = tg.Pixels.get(index).y + tg.Pixels.get(index + 1).y;
                        }
                    }
                    AddIntegralAreaModifier(tg, getImageModifier(tg), aboveMiddle, -0.25 * csFactor, tg.Pixels.get(index), tg.Pixels.get(index + 1), false);
                    break;
                case TacticalLines.MINE_LINE:
                    AddIntegralAreaModifier(tg, getImageModifier(tg), aboveMiddle, -0.2 * csFactor, tg.Pixels.get(middleSegment), tg.Pixels.get(middleSegment + 1), false);
                    if (tg.isHostile()) {
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, 0.0, pt0, pt1, false);
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, 0.0, ptLast, ptNextToLast, false);
                    }
                    break;
                case TacticalLines.DEPICT:
                    GetMBR(tg, ul, ur, lr, ll);
                    addNModifier(tg, g2d);
                    AddIntegralAreaModifier(tg, getImageModifier(tg), areaImage, 0, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.FFA:
                case TacticalLines.RFA:
                case TacticalLines.NFA:
                    AddIntegralAreaModifier(tg, label, area, -1 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    addDTG(tg, area, 1 * csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.PAA:
                    addModifierOnLine("PAA", tg, g2d);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    addDTG(tg, area, 0.5 * csFactor, 1.5 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.FSA:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    addDTG(tg, area, 0.5 * csFactor, 1.5 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.ATI:
                case TacticalLines.CFFZ:
                case TacticalLines.CFZ:
                case TacticalLines.TBA:
                case TacticalLines.TVAR:
                case TacticalLines.ZOR:
                case TacticalLines.DA:
                case TacticalLines.SENSOR:
                case TacticalLines.CENSOR:
                case TacticalLines.KILLBOXBLUE:
                case TacticalLines.KILLBOXPURPLE:
                    AddIntegralAreaModifier(tg, label, area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    GetMBR(tg, ul, ur, lr, ll);
                    POINT2 ptLeft = ul;
                    POINT2 ptRight = ur;
                    if (tg.get_Client().equalsIgnoreCase("ge")) {
                        ptLeft.x -= font.getSize() / 2;
                        ptRight.x -= font.getSize() / 2;
                    }
                    AddIntegralAreaModifier(tg, tg.get_DTG() + WDash, toEnd, 0.5 * csFactor, ptLeft, ptRight, false, "W");
                    AddIntegralAreaModifier(tg, tg.get_DTG1(), toEnd, 1.5 * csFactor, ptLeft, ptRight, false, "W1");
                    break;
                case TacticalLines.BATTLE:
                case TacticalLines.STRONG:
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    addModifierBottomSegment(tg, tg.get_EchelonSymbol());
                    break;
                case TacticalLines.PNO:
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                    addModifierBottomSegment(tg, tg.get_EchelonSymbol());
                    addNModifier(tg, g2d);
                    break;
                case TacticalLines.WFZ:
                    AddIntegralAreaModifier(tg, label, area, -1.5 * csFactor, ptCenter, ptCenter, true);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -0.5 * csFactor, ptCenter, ptCenter, true);
                    AddIntegralAreaModifier(tg, "TIME FROM: " + tg.get_DTG(), area, 0.5 * csFactor, ptCenter, ptCenter, true, "W");
                    AddIntegralAreaModifier(tg, "TIME TO: " + tg.get_DTG1(), area, 1.5 * csFactor, ptCenter, ptCenter, true, "W1");
                    break;
                case TacticalLines.OBSFAREA:
                    AddIntegralAreaModifier(tg, label, area, -1.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_DTG() + WDash, area, 0.5 * csFactor, ptCenter, ptCenter, false, "W");
                    AddIntegralAreaModifier(tg, tg.get_DTG1(), area, 1.5 * csFactor, ptCenter, ptCenter, false, "W1");
                    break;
                case TacticalLines.OBSAREA:
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -1 * csFactor, ptCenter, ptCenter, true);
                    AddIntegralAreaModifier(tg, tg.get_DTG() + WDash, area, 0, ptCenter, ptCenter, true, "W");
                    AddIntegralAreaModifier(tg, tg.get_DTG1(), area, 1 * csFactor, ptCenter, ptCenter, true, "W1");
                    break;
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
                    AddIntegralAreaModifier(tg, label, area, -2.5, ptCenter, ptCenter, false, "");
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, -1.5, ptCenter, ptCenter, false, "T");
                    AddIntegralAreaModifier(tg, "MIN ALT: " + tg.get_X(), area, -0.5, ptCenter, ptCenter, false, "H");
                    AddIntegralAreaModifier(tg, "MAX ALT: " + tg.get_X1(), area, 0.5, ptCenter, ptCenter, false, "H1");
                    AddIntegralAreaModifier(tg, "TIME FROM: " + tg.get_DTG(), area, 1.5, ptCenter, ptCenter, false, "W");
                    AddIntegralAreaModifier(tg, "TIME TO: " + tg.get_DTG1(), area, 2.5, ptCenter, ptCenter, false, "W1");
                    break;
                case TacticalLines.ENCIRCLE:
                    if (tg.isHostile()) {
                        AddIntegralModifier(tg, tg.get_N(), aboveMiddle, 0, 0, 1, true);
                        AddIntegralModifier(tg, tg.get_N(), aboveMiddle, 0, middleSegment, middleSegment + 1, true);
                    }
                    break;
                case TacticalLines.LAA:
                    AddIntegralAreaModifier(tg, getImageModifier(tg), areaImage, 0, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, label, area, -1 * csFactor, ptCenter, ptCenter, false);
                    break;
                case TacticalLines.BOUNDARY:
                    if (clipRect != null) {
                        AddBoundaryModifiers(tg, g2d, clipRect);
                    } else {
                        AddBoundaryModifiers(tg, g2d, clipArray);
                    }
                    break;
                case TacticalLines.CFL:
                    stringWidth = (int) ((double) metrics.stringWidth(label + TSpace + tg.get_Name()));
                    stringWidth2 = (int) ((double) metrics.stringWidth(tg.get_DTG() + WDash + tg.get_DTG1()));
                    if (stringWidth2 > stringWidth) {
                        stringWidth = stringWidth2;
                    }
                    pt0 = new POINT2(tg.Pixels.get(middleSegment));
                    pt1 = new POINT2(tg.Pixels.get(middleSegment + 1));
                    getPixelsMiddleSegment(tg, stringWidth, pt0, pt1);
                    AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                    addDTG(tg, aboveMiddle, 0.7 * csFactor, 1.7 * csFactor, pt0, pt1, metrics);
                    break;
                case TacticalLines.FLOT:
                    if (tg.get_H().equals("1")) {
                        label = "LC";
                    } else if (tg.get_H().equals("2")) {
                        label = "";
                    }
                    AddIntegralAreaModifier(tg, label, toEnd, 0, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label, toEnd, 0, ptLast, ptNextToLast, false);

                    if (tg.isHostile()) {
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, -1 * csFactor, pt0, pt1, false);
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, -1 * csFactor, ptLast, ptNextToLast, false);
                    }
                    break;
                case TacticalLines.LC:
                    double shiftFactor = 1d;
                    if (shiftLines) {
                        shiftFactor = 0.5d;
                    }
                    if (tg.isHostile()) {
                        if (pt0.x < pt1.x) {
                            TLineFactor = -shiftFactor;//was -1
                        } else {
                            TLineFactor = shiftFactor;//was 1
                        }
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, TLineFactor, pt0, pt1, false);
                        if (ptNextToLast.x < ptLast.x) {
                            TLineFactor = -shiftFactor;//was -1
                        } else {
                            TLineFactor = shiftFactor;//was 1
                        }
                        AddIntegralAreaModifier(tg, tg.get_N(), toEnd, TLineFactor, ptLast, ptNextToLast, false);
                    }
                    AddIntegralAreaModifier(tg, label, toEnd, 0, pt0, pt1, false);
                    AddIntegralAreaModifier(tg, label, toEnd, 0, ptLast, ptNextToLast, false);
                    break;
                case TacticalLines.CATK:
                    AddIntegralModifier(tg, label, aboveMiddle, 0, 1, 0, false);
                    break;
                case TacticalLines.CATKBYFIRE:
                    stringWidth = (int) (1.5 * (double) metrics.stringWidth(label));
                    pt2 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                    AddModifier2(tg, label, aboveMiddle, 0, pt1, pt2, false);
                    break;
                case TacticalLines.IL:
                    AddIntegralModifier(tg, tg.get_Name(), aboveMiddle, 0, 1, 0, false);
                    break;
                case TacticalLines.RETIRE:
                case TacticalLines.FPOL:
                case TacticalLines.RPOL:
                case TacticalLines.WITHDRAW:
                case TacticalLines.WDRAWUP:
                    AddIntegralModifier(tg, label, aboveMiddle, 0, 0, 1, true);
                    break;
                case TacticalLines.RIP:
                case TacticalLines.BOMB:
                case TacticalLines.TGMF:
                    AddIntegralAreaModifier(tg, label, area, 0, ptCenter, ptCenter, true);
                    break;
                case TacticalLines.MSDZ:
                    AddIntegralAreaModifier(tg, "1", area, 0, pt1, pt1, true);
                    AddIntegralAreaModifier(tg, "2", area, 0, pt2, pt2, true);
                    AddIntegralAreaModifier(tg, "3", area, 0, pt3, pt3, true);
                    break;
                case TacticalLines.DELAY:
                    AddIntegralModifier(tg, tg.get_DTG(), aboveMiddle, -1 * csFactor, 0, 1, false);
                    AddIntegralModifier(tg, label, aboveMiddle, 0, 0, 1, true);
                    break;
                case TacticalLines.GENERIC_LINE:
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    pt2 = tg.Pixels.get(tg.Pixels.size() - 1);
                    pt3 = tg.Pixels.get(tg.Pixels.size() - 2);
                    dist = lineutility.CalcDistanceDouble(pt0, pt1);
                    dist2 = lineutility.CalcDistanceDouble(pt2, pt3);
                    stringWidth = (int) ((double) metrics.stringWidth(tg.get_H() + " " + tg.get_Name()));
                    stringWidth2 = (int) ((double) metrics.stringWidth(tg.get_DTG()));
                    if (stringWidth2 > stringWidth) {
                        stringWidth = stringWidth2;
                    }

                    if (tg.Pixels.size() == 2) //one segment
                    {
                        pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                        AddModifier2(tg, tg.get_H() + " " + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                        AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        if (dist > 3.5 * stringWidth)//was 28stringwidth+5
                        {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_H() + " " + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    } else //more than one semgent
                    {
                        double dist3 = lineutility.CalcDistanceDouble(pt0, pt2);
                        if (dist > stringWidth + 5 || dist >= dist2 || dist3 > stringWidth + 5) {
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_H() + " " + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                        if (dist2 > stringWidth + 5 || dist2 > dist || dist3 > stringWidth + 5) {
                            pt0 = tg.Pixels.get(tg.Pixels.size() - 1);
                            pt1 = tg.Pixels.get(tg.Pixels.size() - 2);
                            pt1 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);
                            AddModifier2(tg, tg.get_H() + " " + tg.get_Name(), aboveMiddle, -0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG() + WDash, aboveMiddle, 0.7 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 1.7 * csFactor, pt0, pt1, false);
                        }
                    }
                    break;
                default:
                    break;
            }
            scaleModifiers(tg);
            tg.Pixels = origPoints;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddModifiersGeo",
                    new RendererException("Failed inside AddModifiersGeo", exc));
        }

    }

    /**
     * RFA, NFA, FFA need these for line spacing
     *
     * @param tg
     * @return
     */
    private static int getRFALines(TGLight tg) {
        int lines = 1;
        try {
            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                lines++;
            }
            if (tg.get_DTG() != null && !tg.get_DTG().isEmpty()) {
                lines++;
            } else if (tg.get_DTG1() != null && !tg.get_DTG1().isEmpty()) {
                lines++;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddModifiers",
                    new RendererException("Failed inside AddModifiers", exc));
        }
        return lines;
    }

    /**
     * Added sector range fan modifiers based using the calculated orientation
     * indicator points
     *
     * @param tg
     * @param converter
     * @return
     */
    private static void addSectorModifiers(TGLight tg, IPointConversion converter) {
        try {
            if (tg.get_LineType() == TacticalLines.RANGE_FAN_SECTOR) {
                ArrayList<Double> AM = new ArrayList<>();
                ArrayList<Double> AN = new ArrayList<>();
                //get the number of sectors
                String X = tg.get_X();
                String[] altitudes = null;
                String[] am = tg.get_AM().split(",");
                String[] an = tg.get_AN().split(",");
                int numSectors = an.length / 2;
                //there must be at least one sector
                if (numSectors < 1) {
                    return;
                }
                if (!X.isEmpty()) {
                    altitudes = X.split(",");
                }
                try {
                    for (String s : am) {
                        AM.add(Double.parseDouble(s));
                    }
                    for (String s : an) {
                        AN.add(Double.parseDouble(s));
                    }
                } catch (NumberFormatException e) {
                    return;
                }
                if (numSectors + 1 > AM.size()) {
                    if (Double.parseDouble(am[0]) != 0d) {
                        AM.add(0, 0d);
                    }
                }

                int n = tg.Pixels.size();
                //pt0 and pt1 are points for the location indicator
                POINT2 pt0 = tg.Pixels.get(n - 5);
                POINT2 pt1 = tg.Pixels.get(n - 4);
                Point2D pt02d = new Point2D.Double(pt0.x, pt0.y);
                Point2D pt12d = new Point2D.Double(pt1.x, pt1.y);
                pt02d = converter.PixelsToGeo(pt02d);
                pt12d = converter.PixelsToGeo(pt12d);
                pt0.x = pt02d.getX();
                pt0.y = pt02d.getY();
                pt1.x = pt12d.getX();
                pt1.y = pt12d.getY();
                //azimuth of the orientation indicator
                double az12 = mdlGeodesic.GetAzimuth(pt0, pt1);

                POINT2 pt2 = null;
                ArrayList<POINT2> locModifier = new ArrayList();
                //diagnostic
                POINT2 ptLeft = null, ptRight = null;
                ArrayList<POINT2> locAZModifier = new ArrayList();
                //end section
                Point2D pt22d = null;
                double radius = 0;
                for (int k = 0; k < numSectors; k++) {
                    if (AM.size() < k + 2) {
                        break;
                    }
                    radius = (AM.get(k) + AM.get(k + 1)) / 2;
                    pt2 = mdlGeodesic.geodesic_coordinate(pt0, radius, az12);
                    //need locModifier in geo pixels
                    pt22d = new Point2D.Double(pt2.x, pt2.y);
                    pt22d = converter.GeoToPixels(pt22d);
                    pt2.x = pt22d.getX();
                    pt2.y = pt22d.getY();
                    locModifier.add(pt2);
                    //diagnostic
                    if (tg.get_HideOptionalLabels())
                        continue;
                    ptLeft = mdlGeodesic.geodesic_coordinate(pt0, radius, AN.get(2 * k));
                    //need ptLeft in geo pixels
                    pt22d = new Point2D.Double(ptLeft.x, ptLeft.y);
                    pt22d = converter.GeoToPixels(pt22d);
                    ptLeft.x = pt22d.getX();
                    ptLeft.y = pt22d.getY();
                    ptRight = mdlGeodesic.geodesic_coordinate(pt0, radius, AN.get(2 * k + 1));
                    //need ptRight in geo pixels
                    pt22d = new Point2D.Double(ptRight.x, ptRight.y);
                    pt22d = converter.GeoToPixels(pt22d);
                    ptRight.x = pt22d.getX();
                    ptRight.y = pt22d.getY();
                    locAZModifier.add(ptLeft);
                    locAZModifier.add(ptRight);
                    //end section
                }
                if (altitudes != null) {
                    for (int k = 0; k < altitudes.length; k++) {
                        if (k >= locModifier.size()) {
                            break;
                        }
                        pt0 = locModifier.get(k);
                        AddAreaModifier(tg, "ALT " + altitudes[k], area, 0, pt0, pt0);
                    }
                }

                if (!tg.get_HideOptionalLabels()) {
                    for (int k = 0; k < numSectors; k++) {
                        pt0 = locModifier.get(k);
                        AddAreaModifier(tg, "RG " + AM.get(k + 1), area, -1, pt0, pt0);
                        ptLeft = locAZModifier.get(2 * k);
                        ptRight = locAZModifier.get(2 * k + 1);
                        AddAreaModifier(tg, an[2 * k], area, 0, ptLeft, ptLeft);
                        AddAreaModifier(tg, an[2 * k + 1], area, 0, ptRight, ptRight);
                    }
                }
            } else if (tg.get_LineType() == TacticalLines.RADAR_SEARCH) {
                // Copies functionality from RANGE_FAN_SECTOR with one sector and different modifiers
                String strLeftRightMinMax = tg.get_LRMM();
                String[] sector = strLeftRightMinMax.split(",");
                double left = Double.parseDouble(sector[0]);
                double right = Double.parseDouble(sector[1]);

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

                double orientation = 0;
                if (left > right) {
                    orientation = (left - 360 + right) / 2;
                } else {
                    orientation = (left + right) / 2;
                }

                double dist = Double.parseDouble(sector[3]);
                double radius = dist * 1.1;

                POINT2 pt0 = tg.LatLongs.get(0);
                Point2D ptPixels = converter.GeoToPixels(new Point2D.Double(pt0.x,  pt0.y));
                POINT2 pt0F = new POINT2();
                pt0F.x = ptPixels.getX();
                pt0F.y = ptPixels.getY();
                pt0F.style = pt0.style;

                POINT2 pt1 = mdlGeodesic.geodesic_coordinate(pt0, radius, orientation);
                ptPixels = converter.GeoToPixels(new Point2D.Double(pt1.x, pt1.y));
                POINT2 pt1F = new POINT2();
                pt1F.x = ptPixels.getX();
                pt1F.y = ptPixels.getY();
                pt1F.style = pt1.style;

                dist = lineutility.CalcDistanceDouble(pt0F, pt1F);
                double base = 10;
                if (dist < 100) {
                    base = dist / 10;
                }
                if (base < 5) {
                    base = 5;
                }
                double basex2 = 2 * base;
                POINT2 ptTipF = lineutility.ExtendAlongLineDouble(pt0F, pt1F, dist + basex2);  //was 20

                pt0 = pt0F;
                pt1 = ptTipF;

                ArrayList<Double> AM = new ArrayList<>();
                String[] am = tg.get_AM().split(",");

                for (String s : am) {
                    AM.add(Double.parseDouble(s));
                }

                if (AM.size() < 2) {
                    if (Double.parseDouble(am[0]) != 0d) {
                        AM.add(0, 0d);
                    } else {
                        return;
                    }
                }

                Point2D pt02d = new Point2D.Double(pt0.x, pt0.y);
                Point2D pt12d = new Point2D.Double(pt1.x, pt1.y);
                pt02d = converter.PixelsToGeo(pt02d);
                pt12d = converter.PixelsToGeo(pt12d);
                pt0.x = pt02d.getX();
                pt0.y = pt02d.getY();
                pt1.x = pt12d.getX();
                pt1.y = pt12d.getY();
                double az12 = mdlGeodesic.GetAzimuth(pt0, pt1);

                Point2D pt22d = null;

                radius = (AM.get(0) + AM.get(1)) / 2;
                POINT2 pt2 = mdlGeodesic.geodesic_coordinate(pt0, radius, az12);
                pt22d = new Point2D.Double(pt2.x, pt2.y);
                pt22d = converter.GeoToPixels(pt22d);
                pt2.x = pt22d.getX();
                pt2.y = pt22d.getY();
                AddAreaModifier(tg, tg.get_Name(), area, -1, pt2, pt2);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "addSectorModifiers",
                    new RendererException("Failed inside addSectorModifiers", exc));
        }
    }

    /**
     * Called by the renderer after tg.Pixels has been filled with the
     * calculated points. The modifier path depends on points calculated by
     * CELineArray.
     *
     * @param tg
     */
    public static void AddModifiers2(TGLight tg, IPointConversion converter) {
        try {
            if (tg.Pixels == null || tg.Pixels.isEmpty()) {
                return;
            }
            switch (tg.get_LineType()) {
                case TacticalLines.CONVOY:
                case TacticalLines.HCONVOY:
                case TacticalLines.BREACH:
                case TacticalLines.BYPASS:
                case TacticalLines.CANALIZE:
                case TacticalLines.PENETRATE:
                case TacticalLines.CLEAR:
                case TacticalLines.DISRUPT:
                case TacticalLines.FIX:
                case TacticalLines.ISOLATE:
                case TacticalLines.OCCUPY:
                case TacticalLines.RETAIN:
                case TacticalLines.SECURE:
                case TacticalLines.CONTAIN:
                case TacticalLines.SEIZE:
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.CORDONSEARCH:
                case TacticalLines.FOLLA:
                case TacticalLines.FOLSP:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.ACA_CIRCULAR:
                case TacticalLines.RECTANGULAR:
                case TacticalLines.CUED_ACQUISITION:
                case TacticalLines.CIRCULAR:
                case TacticalLines.BDZ:
                case TacticalLines.FSA_CIRCULAR:
                case TacticalLines.NOTACK:
                case TacticalLines.ATI_CIRCULAR:
                case TacticalLines.CFFZ_CIRCULAR:
                case TacticalLines.SENSOR_CIRCULAR:
                case TacticalLines.CENSOR_CIRCULAR:
                case TacticalLines.DA_CIRCULAR:
                case TacticalLines.CFZ_CIRCULAR:
                case TacticalLines.ZOR_CIRCULAR:
                case TacticalLines.TBA_CIRCULAR:
                case TacticalLines.TVAR_CIRCULAR:
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                case TacticalLines.KILLBOXBLUE_CIRCULAR:
                case TacticalLines.KILLBOXPURPLE_CIRCULAR:
                case TacticalLines.BLOCK:
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.SHIP_AOI_RECTANGULAR:
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                case TacticalLines.PAA:
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
                case TacticalLines.PAA_CIRCULAR:
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                case TacticalLines.SHIP_AOI_CIRCULAR:
                case TacticalLines.MFLANE:
                    break;
                default:
                    return;
            }
            //end section
            ArrayList<POINT2> origPoints = lineutility.getDeepCopy(tg.Pixels);
            int n = tg.Pixels.size();
            if (tg.modifiers == null) {
                tg.modifiers = new ArrayList();
            }
            Font font = tg.get_Font();
            POINT2 ptCenter = null;
            double csFactor = 1d;//this will be used for text spacing the 3d map (CommandCight)
            //String affiliation=tg.get_Affiliation();
            int linetype = tg.get_LineType();
            POINT2 pt0 = null, pt1 = null, pt2 = null, pt3 = null;
            int j = 0, k = 0;
            double dist = 0;
            String label = GetCenterLabel(tg);
            String[] X = null;
            int lastIndex = tg.Pixels.size() - 1;
            int nextToLastIndex = 0;
            if (tg.Pixels.size() > 1) {
                nextToLastIndex = tg.Pixels.size() - 2;
            }
            POINT2 ptLast = new POINT2(tg.Pixels.get(lastIndex));
            POINT2 ptNextToLast = null;
            if (tg.Pixels.size() > 1) {
                ptNextToLast = new POINT2(tg.Pixels.get(nextToLastIndex));
            }
            String WDash = ""; // Dash between W and W1 if they're not empty
            String TSpace = "", TDash = ""; // Space or dash between label and T modifier if T isn't empty
            if (tg.get_DTG() != null && tg.get_DTG1() != null && !tg.get_DTG().isEmpty() && !tg.get_DTG1().isEmpty()) {
                WDash = " - ";
            }
            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                TSpace = " ";
                TDash = " - ";
            }

            POINT2 ptLeft = null, ptRight = null;
            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setFont(tg.get_Font());
            FontMetrics metrics = g2d.getFontMetrics();
            int stringWidth = 0, rfaLines = 0;
            pt0 = new POINT2(tg.Pixels.get(0));
            if (tg.Pixels.size() > 1) {
                pt1 = new POINT2(tg.Pixels.get(1));
            }

            POINT2[] pts = null;
            // if the client is the 3d map (CS) then we want to shrink the spacing bnetween
            // the lines of text
            if (tg.get_Client().equals("cpof3d")) {
                csFactor = 0.9d;
            }

            shiftModifierPath(tg, pt0, pt1, ptLast, ptNextToLast);
            switch (linetype) {
                case TacticalLines.CONVOY:
                case TacticalLines.HCONVOY:
                    pt2 = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(3), 0);
                    pt3 = lineutility.MidPointDouble(tg.Pixels.get(1), tg.Pixels.get(2), 0);
                    AddIntegralAreaModifier(tg, tg.get_V(), aboveEndInside, 0, pt2, pt3, false);
                    AddIntegralAreaModifier(tg, tg.get_H(), aboveStartInside, 0, pt2, pt3, false);
                    addDTG(tg, aboveMiddle, 1.2 * csFactor, 2.2 * csFactor, pt2, pt3, metrics);
                    break;
                case TacticalLines.BREACH:
                case TacticalLines.BYPASS:
                case TacticalLines.CANALIZE:
                    pt0 = tg.Pixels.get(1);
                    pt1 = tg.Pixels.get(2);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddlePerpendicular, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.PENETRATE:
                case TacticalLines.CLEAR:
                    pt0 = tg.Pixels.get(2);
                    pt1 = tg.Pixels.get(3);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.DISRUPT:
                    pt0 = tg.Pixels.get(4);
                    pt1 = tg.Pixels.get(5);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.FIX:
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.ISOLATE:
                case TacticalLines.OCCUPY:
                case TacticalLines.RETAIN:
                case TacticalLines.SECURE:
                    pt0 = tg.Pixels.get(13);
                    pt1 = tg.Pixels.get(14);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.CONTAIN:
                    pt0 = tg.Pixels.get(13);
                    pt1 = tg.Pixels.get(14);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);

                    // Contain always has "ENY" even if friendly (not N modifier)
                    for (j = 0; j < n; j++) {
                        if (tg.Pixels.get(j).style == 14) {
                            pt0 = tg.Pixels.get(j);
                            pt1 = tg.Pixels.get(j + 1);
                            AddIntegralAreaModifier(tg, "ENY", aboveMiddle, 0, pt0, pt1, true);
                            break;
                        }
                    }
                    break;
                case TacticalLines.SEIZE:
                    pt0 = tg.Pixels.get(26);
                    pt1 = tg.Pixels.get(27);
                    //pt1=lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, -0.125 * csFactor, pt0, pt1, true);
                    break;
                case TacticalLines.DEFENDED_AREA_RECTANGULAR:
                    ptLeft = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                    ptRight = lineutility.MidPointDouble(tg.Pixels.get(2), tg.Pixels.get(3), 0);
                    AddIntegralAreaModifier(tg, label + TDash + tg.get_Name(), aboveMiddle, 0, ptLeft, ptRight, false);
                    break;
                case  TacticalLines.SHIP_AOI_RECTANGULAR:
                    if (tg.Pixels.get(0).x > tg.Pixels.get(3).x) {
                        AddIntegralAreaModifier(tg, label, aboveMiddle, csFactor, tg.Pixels.get(0), tg.Pixels.get(3), false);
                    } else {
                        AddIntegralAreaModifier(tg, label, aboveMiddle, csFactor, tg.Pixels.get(1), tg.Pixels.get(2), false);
                    }
                    break;
                case TacticalLines.NOTACK:
                    ptCenter = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(tg.Pixels.size() / 2), 0);
                    AddIntegralAreaModifier(tg, label, area, -1, ptCenter, ptCenter, false);
                    addDTG(tg, area, csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                    break;
                case TacticalLines.SHIP_AOI_CIRCULAR:
                    // Moved from AddModifiersGeo()
                    // AddModifiersGeo() called before getGeoEllipse(). Unable to use getMBR with single anchor point

                    // Get variables from AddModifiersGeo
                    POINT2 lr = new POINT2(tg.Pixels.get(0));
                    POINT2 ll = new POINT2(tg.Pixels.get(0));
                    POINT2 ul = new POINT2(tg.Pixels.get(0));
                    POINT2 ur = new POINT2(tg.Pixels.get(0));
                    GetMBR(tg, ul, ur, lr, ll);

                    AddIntegralAreaModifier(tg, label, aboveMiddle, csFactor, ll, lr, false);
                    break;
                case TacticalLines.MFLANE:
                    //pt0=tg.Pixels.get(7);
                    //pt1=tg.Pixels.get(5);
                    pt0 = tg.Pixels.get(4);
                    pt1 = tg.Pixels.get(2);
                    if (tg.Pixels.get(0).y < tg.Pixels.get(1).y) {
                        addDTG(tg, aboveMiddle, 0.5 * csFactor, 1.5 * csFactor, pt0, pt1, metrics);
                    } else {
                        addDTG(tg, aboveMiddle, -0.5 * csFactor, -1.5 * csFactor, pt0, pt1, metrics);
                    }
                    break;
                case TacticalLines.CORDONKNOCK:
                case TacticalLines.CORDONSEARCH:
                    pt0 = tg.Pixels.get(13);
                    pt1 = tg.Pixels.get(0);
                    stringWidth = metrics.stringWidth(label);
                    if (pt0.x < pt1.x) {
                        stringWidth = -stringWidth;
                    }
                    pt1 = lineutility.ExtendAlongLineDouble2(pt0, pt1, 0.75 * stringWidth);
                    AddIntegralAreaModifier(tg, label, aboveMiddle, 0, pt0, pt1, true);
                    break;
                case TacticalLines.FOLLA:
                    pt0 = tg.Pixels.get(0);
                    pt1 = lineutility.MidPointDouble(tg.Pixels.get(5), tg.Pixels.get(6), 0);
                    pt1 = lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 0, pt0, pt1, true);
                    break;
                case TacticalLines.FOLSP:
                    pt0 = tg.Pixels.get(3);
                    pt1 = tg.Pixels.get(6);
                    pt1 = lineutility.ExtendAlongLineDouble(pt1, pt0, -10);
                    AddIntegralAreaModifier(tg, tg.get_Name(), aboveMiddle, 0, pt0, pt1, true);
                    break;
                case TacticalLines.ACA_RECTANGULAR:
                    ptLeft = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                    ptRight = lineutility.MidPointDouble(tg.Pixels.get(2), tg.Pixels.get(3), 0);
                    AddModifier2(tg, label + TSpace + tg.get_Name(), aboveMiddle, -3 * csFactor, ptLeft, ptRight, false);
                    AddModifier2(tg, tg.get_T1(), aboveMiddle, -2 * csFactor, ptLeft, ptRight, false, "T1");
                    AddModifier2(tg, "MIN ALT: " + tg.get_X(), aboveMiddle, -1 * csFactor, ptLeft, ptRight, false, "H");
                    AddModifier2(tg, "MAX ALT: " + tg.get_X1(), aboveMiddle, 0, ptLeft, ptRight, false, "H1");
                    AddModifier2(tg, "Grids: " + tg.get_H(), aboveMiddle, 1 * csFactor, ptLeft, ptRight, false, "H2");
                    AddModifier2(tg, "EFF: " + tg.get_DTG() + WDash, aboveMiddle, 2 * csFactor, ptLeft, ptRight, false, "W");
                    AddModifier2(tg, tg.get_DTG1(), aboveMiddle, 3 * csFactor, ptLeft, ptRight, false, "W1");
                    break;
                case TacticalLines.ACA_CIRCULAR:
                    ptCenter = lineutility.CalcCenterPointDouble2(tg.Pixels.toArray(), tg.Pixels.size());
                    AddIntegralAreaModifier(tg, label + TSpace + tg.get_Name(), area, -3 * csFactor, ptCenter, ptCenter, false);
                    AddModifier2(tg, tg.get_T1(), area, -2 * csFactor, ptCenter, ptCenter, false, "T1");
                    AddIntegralAreaModifier(tg, "MIN ALT: " + tg.get_X(), area, -1 * csFactor, ptCenter, ptCenter, false, "H");
                    AddIntegralAreaModifier(tg, "MAX ALT: " + tg.get_X1(), area, 0, ptCenter, ptCenter, false, "H1");
                    AddIntegralAreaModifier(tg, "Grids: " + tg.get_H(), area, 1 * csFactor, ptCenter, ptCenter, false, "H2");
                    AddIntegralAreaModifier(tg, "EFF: " + tg.get_DTG() + WDash, area, 2 * csFactor, ptCenter, ptCenter, false, "W");
                    AddIntegralAreaModifier(tg, tg.get_DTG1(), area, 3 * csFactor, ptCenter, ptCenter, false, "W1");
                    break;
                case TacticalLines.FSA_CIRCULAR:
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
                    ptCenter = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(tg.Pixels.size() / 2), 0);
                    AddIntegralAreaModifier(tg, label, area, -0.5 * csFactor, ptCenter, ptCenter, false);
                    AddIntegralAreaModifier(tg, tg.get_Name(), area, 0.5 * csFactor, ptCenter, ptCenter, false);
                    AddOffsetModifier(tg, tg.get_DTG() + WDash, toEnd, -1 * csFactor, tg.Pixels.size() / 2, 0, 4, "left");
                    AddOffsetModifier(tg, tg.get_DTG1(), toEnd, 0, tg.Pixels.size() / 2, 0, 4, "left");
                    break;
                case TacticalLines.FFA_CIRCULAR:
                case TacticalLines.NFA_CIRCULAR:
                case TacticalLines.RFA_CIRCULAR:
                    rfaLines = getRFALines(tg);
                    ptCenter = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(51), 0);
                    switch (rfaLines) {
                        case 3: //2 valid modifiers and a label
                            AddIntegralAreaModifier(tg, label, area, -1 * csFactor, ptCenter, ptCenter, true);
                            AddIntegralAreaModifier(tg, tg.get_Name(), area, 0, ptCenter, ptCenter, true);
                            addDTG(tg, area, 1 * csFactor, 2 * csFactor, ptCenter, ptCenter, metrics);
                            break;
                        case 2: //one valid modifier and a label
                            AddIntegralAreaModifier(tg, label, area, -0.5 * csFactor, ptCenter, ptCenter, true);
                            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                                AddIntegralAreaModifier(tg, tg.get_Name(), area, 0.5 * csFactor, ptCenter, ptCenter, true);
                            } else {
                                addDTG(tg, area, 0.5 * csFactor, 1.5 * csFactor, ptCenter, ptCenter, metrics);
                            }
                            break;
                        default:    //one label only
                            AddIntegralAreaModifier(tg, label, area, 0, ptCenter, ptCenter, true);
                            break;
                    }
                    break;
                case TacticalLines.BLOCK:
                    //for (j = 0; j < tg.Pixels.size(); j++)
                    for (j = 0; j < n; j++) {
                        if (tg.Pixels.get(j).style == 14) {
                            AddIntegralModifier(tg, label, aboveMiddle, 0, j, j + 1);
                            break;
                        }
                    }
                    break;
                case TacticalLines.FFA_RECTANGULAR:
                case TacticalLines.NFA_RECTANGULAR:
                case TacticalLines.RFA_RECTANGULAR:
                    rfaLines = getRFALines(tg);
                    pt0 = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                    pt1 = lineutility.MidPointDouble(tg.Pixels.get(2), tg.Pixels.get(3), 0);
                    switch (rfaLines) {
                        case 3: //two valid modifiers and one label
                            AddModifier2(tg, label, aboveMiddle, -1 * csFactor, pt0, pt1, false);
                            AddModifier2(tg, tg.get_Name(), aboveMiddle, 0, pt0, pt1, false);
                            addDTG(tg, aboveMiddle, 1 * csFactor, 2 * csFactor, pt0, pt1, metrics);
                            break;
                        case 2: //one valid modifier and one label
                            AddModifier2(tg, label, aboveMiddle, -0.5 * csFactor, pt0, pt1, false);
                            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                                AddModifier2(tg, tg.get_Name(), aboveMiddle, 0.5 * csFactor, pt0, pt1, false);
                            } else {
                                addDTG(tg, aboveMiddle, 0.5 * csFactor, 1.5 * csFactor, pt0, pt1, metrics);
                            }
                            break;
                        default:    //one label only
                            AddModifier2(tg, label, aboveMiddle, 0, pt0, pt1, false);
                            break;
                    }
                    break;
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                case TacticalLines.FSA_RECTANGULAR:
                case TacticalLines.ATI_RECTANGULAR:
                case TacticalLines.CFFZ_RECTANGULAR:
                case TacticalLines.SENSOR_RECTANGULAR:
                case TacticalLines.CENSOR_RECTANGULAR:
                case TacticalLines.DA_RECTANGULAR:
                case TacticalLines.CFZ_RECTANGULAR:
                case TacticalLines.ZOR_RECTANGULAR:
                case TacticalLines.TBA_RECTANGULAR:
                case TacticalLines.TVAR_RECTANGULAR:
                    ptLeft = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                    ptRight = lineutility.MidPointDouble(tg.Pixels.get(2), tg.Pixels.get(3), 0);
                    AddModifier2(tg, label, aboveMiddle, -0.5 * csFactor, ptLeft, ptRight, false);
                    AddModifier2(tg, tg.get_Name(), aboveMiddle, 0.5 * csFactor, ptLeft, ptRight, false);
                    pt0 = tg.Pixels.get(0);
                    pt1 = tg.Pixels.get(1);
                    pt2 = tg.Pixels.get(2);
                    pt3 = tg.Pixels.get(3);
                    if (tg.get_Client().equalsIgnoreCase("ge")) {
                        pt0.x -= font.getSize() / 2;
                        pt2.x -= font.getSize() / 2;
                    }
                    if (!tg.get_Client().equalsIgnoreCase("ge"))//added 2-27-12
                    {
                        clsUtility.shiftModifiersLeft(pt0, pt3, 12.5);
                        clsUtility.shiftModifiersLeft(pt1, pt2, 12.5);
                    }
                    if (ptLeft.x == ptRight.x) {
                        ptRight.x += 1;
                    }
                    if (ptLeft.x < ptRight.x) {
                        AddModifier(tg, tg.get_DTG() + WDash, toEnd, 0, pt0, pt3);//was 1,2 switched for CPOF
                        AddModifier(tg, tg.get_DTG1(), toEnd, 1 * csFactor, pt0, pt3);//was 1,2
                    } else {
                        AddModifier(tg, tg.get_DTG() + WDash, toEnd, 0, pt2, pt1);//was 3,0 //switched for CPOF
                        AddModifier(tg, tg.get_DTG1(), toEnd, 1 * csFactor, pt2, pt1);//was 3,0
                    }

                    break;
                case TacticalLines.PAA_RECTANGULAR:
                    AddIntegralModifier(tg, label, aboveMiddlePerpendicular, 0, 0, 1, true);
                    AddIntegralModifier(tg, label, aboveMiddle, 0, 1, 2, true);
                    AddIntegralModifier(tg, label, aboveMiddlePerpendicular, 0, 2, 3, true);
                    AddIntegralModifier(tg, label, aboveMiddle, 0, 3, 0, true);
                    rfaLines = getRFALines(tg);
                    pt0 = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get(1), 0);
                    pt1 = lineutility.MidPointDouble(tg.Pixels.get(2), tg.Pixels.get(3), 0);
                    switch (rfaLines) {
                        case 3: // two valid modifiers
                            AddModifier2(tg, tg.get_Name(), aboveMiddle, -0.5, pt0, pt1, false);
                            addDTG(tg, aboveMiddle, 0.5 * csFactor, 1.5 * csFactor, pt0, pt1, metrics);
                            break;
                        case 2: // one valid modifier
                            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                                AddModifier2(tg, tg.get_Name(), aboveMiddle, 0, pt0, pt1, false);
                            } else {
                                addDTG(tg, aboveMiddle, 0, csFactor, pt0, pt1, metrics);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case TacticalLines.PAA_CIRCULAR:
                    for (int i = 0; i < 4; i++) {
                        AddIntegralModifier(tg, label, area, -0.5 * csFactor, n / 4 * i, n / 4 * i, false);
                    }

                    rfaLines = getRFALines(tg);
                    ptCenter = lineutility.MidPointDouble(tg.Pixels.get(0), tg.Pixels.get((int) (n / 2.0 + 0.5)), 0);
                    switch (rfaLines) {
                        case 3: // two valid modifiers
                            AddIntegralAreaModifier(tg, tg.get_Name(), area, -0.5, ptCenter, ptCenter, false);
                            addDTG(tg, area, 0.5 * csFactor, 1.5 * csFactor, ptCenter, ptCenter, metrics);
                            break;
                        case 2: // one valid modifier
                            if (tg.get_Name() != null && !tg.get_Name().isEmpty()) {
                                AddIntegralAreaModifier(tg, tg.get_Name(), area, 0, ptCenter, ptCenter, false);
                            } else {
                                addDTG(tg, area, 0, csFactor, ptCenter, ptCenter, metrics);
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case TacticalLines.RANGE_FAN:
                    if (tg.get_X() != null) {
                        X = tg.get_X().split(",");
                        for (j = 0; j < X.length; j++) {
                            if (tg.Pixels.size() > j * 102 + 25) {
                                pt0 = tg.Pixels.get(j * 102 + 25);
                                AddAreaModifier(tg, "ALT " + X[j], area, 0, pt0, pt0);
                            }
                        }
                    }
                    if(!tg.get_HideOptionalLabels())
                    {
                        String[] am = tg.get_AM().split(",");
                        for(j=0;j<am.length;j++)
                        {
                            if (tg.Pixels.size() > j * 102 + 25) {
                                pt0 = tg.Pixels.get(j * 102 + 25);
                                //AddAreaModifier(tg, "RG " + am[j], area, -1, pt0, pt0);
                                if(j==0)
                                    AddAreaModifier(tg, "MIN RG " + am[j], 3, -1, pt0, pt0);
                                else
                                    AddAreaModifier(tg, "MAX RG " + "(" + Integer.toString(j) + ") " + am[j], 3, -1, pt0, pt0);
                            }
                        }
                    }// end if set range fan text
                    break;
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                    addSectorModifiers(tg, converter);
                    break;
                default:
                    break;
            }//end switch
            scaleModifiers(tg);
            tg.Pixels = origPoints;
            g2d.dispose();
            g2d = null;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "AddModifiers2",
                    new RendererException("Failed inside AddModifiers2", exc));
        }
    }

    /**
     * Displays the tg modifiers using a client Graphics2D, this is an option
     * provided to clients for displaying modifiers without using shapes
     *
     * @param tg the tactical graphic
     * @param g2d the graphics object for drawing
     * @deprecated
     */
    public static void DisplayModifiers(TGLight tg,
                                        Graphics2D g2d) {
        try {
            Font font = g2d.getFont();
            int j = 0;
            Modifier2 modifier = null;
            g2d.setBackground(Color.white);
            POINT2 pt = null;
            double theta = 0;
            int stringWidth = 0, stringHeight = 0;
            FontMetrics metrics = g2d.getFontMetrics();
            String s = "";
            int x = 0, y = 0;
            POINT2 pt1 = null, pt2 = null;
            int quadrant = -1;
            int n = tg.Pixels.size();
            //for (j = 0; j < tg.modifiers.size(); j++)
            for (j = 0; j < n; j++) {
                modifier = (Modifier2) tg.modifiers.get(j);
                double lineFactor = modifier.lineFactor;
                s = modifier.text;
                double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                pt = modifier.textPath[0];
                x1 = pt.x;
                y1 = pt.y;
                pt = modifier.textPath[1];
                x2 = pt.x;
                y2 = pt.y;
                theta = Math.atan2(y2 - y1, x2 - x1);
                POINT2 midPt;
                if (x1 > x2) {
                    theta -= Math.PI;
                }
                switch (modifier.type) {
                    case toEnd: //corresponds to LabelAndTextBeforeLineTG
                        g2d.rotate(theta, x1, y1);
                        stringWidth = metrics.stringWidth(s);
                        stringHeight = font.getSize();
                        if (x1 < x2 || (x1 == x2 && y1 > y2)) {
                            x = (int) x1 - stringWidth;
                            y = (int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_FontBackColor());
                            g2d.clearRect(x, y, stringWidth, stringHeight);
                            y = (int) y1 + (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_TextColor());
                            g2d.drawString(s, x, y);
                        } else {
                            x = (int) x1;
                            y = (int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_FontBackColor());
                            g2d.clearRect(x, y, stringWidth, stringHeight);
                            y = (int) y1 + (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_TextColor());
                            g2d.drawString(s, x, y);
                        }
                        break;
                    case aboveMiddle:
                        midPt = new POINT2((x1 + x2) / 2, (y1 + y2) / 2);
                        g2d.rotate(theta, midPt.x, midPt.y);
                        stringWidth = metrics.stringWidth(s);
                        stringHeight = font.getSize();
                        x = (int) midPt.x - stringWidth / 2;
                        y = (int) midPt.y - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                        g2d.setColor(tg.get_FontBackColor());
                        g2d.clearRect(x, y, stringWidth, stringHeight);
                        y = (int) midPt.y + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                        g2d.setColor(tg.get_TextColor());
                        g2d.drawString(s, x, y);
                        break;
                    case area:
                        g2d.rotate(0, x1, y1);
                        stringWidth = metrics.stringWidth(s);
                        stringHeight = font.getSize();

                        x = (int) x1 - stringWidth / 2;
                        y = (int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                        g2d.setColor(tg.get_FontBackColor());
                        g2d.clearRect(x, y, stringWidth, stringHeight);
                        y = (int) y1 + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                        g2d.setColor(tg.get_TextColor());
                        g2d.drawString(s, x, y);
                        break;
                    case screen:    //for SCREEN, GUARD, COVER
                        if (tg.Pixels.size() >= 14) {
                            pt1 = tg.Pixels.get(3);
                            pt2 = tg.Pixels.get(10);
                            quadrant = lineutility.GetQuadrantDouble(pt1, pt2);
                            theta = Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x);
                            switch (quadrant) {
                                case 1:
                                    theta += Math.PI / 2;
                                    break;
                                case 2:
                                    theta -= Math.PI / 2;
                                    break;
                                case 3:
                                    theta -= Math.PI / 2;
                                    break;
                                case 4:
                                    theta += Math.PI / 2;
                                    break;
                                default:
                                    break;
                            }

                            g2d.rotate(theta, x1, y1);
                            stringWidth = metrics.stringWidth(s);
                            stringHeight = font.getSize();

                            x = (int) x1 - stringWidth / 2;
                            y = (int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_FontBackColor());
                            g2d.clearRect(x, y, stringWidth, stringHeight);
                            y = (int) y1 + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_TextColor());
                            g2d.drawString(s, x, y);
                        } else {
                            stringWidth = metrics.stringWidth(s);
                            stringHeight = font.getSize();
                            x = (int) tg.Pixels.get(0).x;//(int) x1 - stringWidth / 2;
                            y = (int) tg.Pixels.get(0).y;//(int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_FontBackColor());
                            g2d.clearRect(x, y, stringWidth, stringHeight);
                            y = (int) y + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                            g2d.setColor(tg.get_TextColor());
                            g2d.drawString(s, x, y);
                        }
                        break;
                    default:
                        break;
                }   //end switch
            }   //end for
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "DisplayModifiers",
                    new RendererException("Failed inside DisplayModifiers", exc));
        }
    }//end function

    /**
     * Returns a Shape object for the text background for labels and modifiers
     *
     * @param tg the tactical graphic object
     * @param pt0 1st point of segment
     * @param pt1 last point of segment
     * @param stringWidth string width
     * @param stringHeight string height
     * @param lineFactor number of text lines above or below the segment
     * @param isTextFlipped true if text is flipped
     * @return the modifier shape
     */
    public static Shape2 BuildModifierShape(
            TGLight tg,
            POINT2 pt0,
            POINT2 pt1,
            int stringWidth,
            int stringHeight,
            double lineFactor,
            boolean isTextFlipped) {
        Shape2 modifierFill = null;
        try {

            POINT2 ptTemp0 = new POINT2(pt0), ptTemp1 = new POINT2(pt1);

            if (isTextFlipped) {
                lineFactor += 1;
            }

            if (lineFactor < 0) //extend pt0,pt1 above the line
            {
                ptTemp0 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 2, -lineFactor * stringHeight);
                ptTemp1 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 2, -lineFactor * stringHeight);
            }
            if (lineFactor > 0) //extend pt0,pt1 below the line
            {
                ptTemp0 = lineutility.ExtendDirectedLine(pt0, pt1, pt0, 3, lineFactor * stringHeight);
                ptTemp1 = lineutility.ExtendDirectedLine(pt0, pt1, pt1, 3, lineFactor * stringHeight);
            }
            if (ptTemp0.y == ptTemp1.y) {
                ptTemp0.y += 1;
            }

            POINT2 pt3 = null, pt4 = null, pt5 = null, pt6 = null, pt7 = null;
            pt3 = lineutility.ExtendAlongLineDouble(ptTemp0, ptTemp1, -stringWidth);
            pt4 = lineutility.ExtendDirectedLine(ptTemp1, ptTemp0, pt3, 0, stringHeight / 2);
            pt5 = lineutility.ExtendDirectedLine(ptTemp1, ptTemp0, pt3, 1, stringHeight / 2);
            pt6 = lineutility.ExtendDirectedLine(ptTemp1, ptTemp0, ptTemp0, 1, stringHeight / 2);
            pt7 = lineutility.ExtendDirectedLine(ptTemp1, ptTemp0, ptTemp0, 0, stringHeight / 2);
            modifierFill = new Shape2(Shape2.SHAPE_TYPE_MODIFIER_FILL);

            modifierFill.moveTo(pt4);
            modifierFill.lineTo(pt5);
            modifierFill.lineTo(pt6);
            modifierFill.lineTo(pt7);
            modifierFill.lineTo(pt4);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "BuildModifierShape",
                    new RendererException("Failed inside BuildModifierShape", exc));
        }
        return modifierFill;
    }

    /**
     * For BOUNDARY and other line types which require breaks for the integral
     * text. Currently only boundary uses this
     *
     * @param tg
     * @param g2d the graphics object for drawing
     * @param shapes the shape array
     */
    public static void GetIntegralTextShapes(TGLight tg,
                                             Graphics2D g2d,
                                             ArrayList<Shape2> shapes) {
        try {
            if (tg.Pixels == null || shapes == null) {
                return;
            }

            HashMap<Integer, Color> hmap = clsUtility.getMSRSegmentColors(tg);
            Color color = null;

            Shape2 shape = null;
            Shape2 segShape = null;//diangostic 1-22-13
            g2d.setFont(tg.get_Font());
            int j = 0;
            String affiliation = null;
            FontMetrics metrics = g2d.getFontMetrics();
            String echelonSymbol = null;
            int stringWidthEchelonSymbol = 0;
            //boolean lineTooShort = false;
            POINT2 ptEchelonStart = null, ptEchelonEnd = null, midpt,
                    ptENY0Start = null, ptENY0End = null, ptENY1Start, ptENY1End, pt0 = null, pt1 = null;
            double dist = 0;
            BasicStroke stroke = null;
            switch (tg.get_LineType()) {
                case TacticalLines.BOUNDARY:
                    echelonSymbol = tg.get_EchelonSymbol();
                    //shapes = new ArrayList();
                    shape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                    shape.setLineColor(tg.get_LineColor());
                    shape.set_Style(tg.get_LineStyle());
                    stroke = clsUtility.getLineStroke(tg.get_LineThickness(), shape.get_Style(), tg.get_lineCap(), BasicStroke.JOIN_ROUND);
                    shape.setStroke(stroke);
                    if (echelonSymbol != null && !echelonSymbol.isEmpty()) {
                        stringWidthEchelonSymbol = metrics.stringWidth(echelonSymbol);
                    }
                    //diagnostic
                    if (hmap == null || hmap.isEmpty()) {
                        shape.moveTo(tg.Pixels.get(0));
                        for (j = 1; j < tg.Pixels.size(); j++) {
                            shape.lineTo(tg.Pixels.get(j));
                        }
                        shapes.add(shape);
                        break;
                    }
                    //end section
                    int n = tg.Pixels.size();
                    //for (j = 0; j < tg.Pixels.size() - 1; j++)
                    for (j = 0; j < n - 1; j++) {
                        segShape = null;
                        if (hmap != null) {
                            if (hmap.containsKey(j)) {
                                color = (Color) hmap.get(j);
                                segShape = new Shape2(Shape2.SHAPE_TYPE_POLYLINE);
                                segShape.setLineColor(color);
                                segShape.set_Style(tg.get_LineStyle());
                                segShape.setStroke(stroke);
                            }
                        }

                        pt0 = tg.Pixels.get(j);
                        pt1 = tg.Pixels.get(j + 1);
                        //lineTooShort = GetBoundarySegmentTooShort(tg, g2d, j);
                        if (segShape != null) {
                            segShape.moveTo(pt0);
                        } else {
                            shape.moveTo(pt0);
                        }

                        //uncoment comment to remove line breaks for GE
                        //if (lineTooShort || tg.get_Client().equals("ge"))
                        if (tg.get_Client().equals("ge") || GetBoundarySegmentTooShort(tg, g2d, j) == true) {
                            if (segShape != null) {
                                segShape.lineTo(pt1);
                                shapes.add(segShape);
                                continue;
                            } else {
                                shape.lineTo(pt1);
                                continue;
                            }
                        }

                        midpt = lineutility.MidPointDouble(pt0, pt1, 0);
                        if (segShape != null) {
                            segShape.moveTo(pt0);
                        } else {
                            shape.moveTo(pt0);
                        }

                        if (stringWidthEchelonSymbol > 0) {
                            midpt = lineutility.MidPointDouble(pt0, pt1, 0);
                            dist = lineutility.CalcDistanceDouble(pt0, midpt) - stringWidthEchelonSymbol / 1.5;
                            ptEchelonStart = lineutility.ExtendAlongLineDouble(pt0, pt1, dist);
                            dist = lineutility.CalcDistanceDouble(pt0, midpt) + stringWidthEchelonSymbol / 1.5;
                            ptEchelonEnd = lineutility.ExtendAlongLineDouble(pt0, pt1, dist);
                            if (segShape != null) {
                                segShape.lineTo(ptEchelonStart);
                                segShape.moveTo(ptEchelonEnd);
                            } else {
                                shape.lineTo(ptEchelonStart);
                                shape.moveTo(ptEchelonEnd);
                            }
                        }
                        if (segShape != null) {
                            segShape.lineTo(pt1);
                        } else {
                            shape.lineTo(pt1);
                        }
                        if (segShape != null) {
                            shapes.add(segShape);
                        }
                    }//end for
                    shapes.add(shape);
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "GetIntegralTextShapes",
                    new RendererException("Failed inside GetIntegralTextShapes", exc));
        }
    }

    private static int switchDirection(int direction) {
        int result = -1;
        switch (direction) {
            case 0:
                return 1;
            case 1:
                return 0;
            case 2:
                return 3;
            case 3:
                return 2;
        }
        return result;
    }

    /**
     * Displays the modifiers to a Graphics2D from a BufferedImage
     *
     * @param tg the tactical graphic
     * @param g2d the Graphic for drawing
     * @param shapes the shape array
     * @param isTextFlipped true if text is flipped
     * @param converter to convert between geographic and pixel coordinates
     */
    public static void DisplayModifiers2(TGLight tg,
                                         Graphics2D g2d,
                                         ArrayList<Shape2> shapes,
                                         boolean isTextFlipped,
                                         IPointConversion converter) {
        try {
            if (shapes == null) {
                return;
            }

            if (tg.modifiers == null || tg.modifiers.isEmpty()) {
                return;
            }
            Font font = null;
            int j = 0;
            Modifier2 modifier = null;
            Color fontBackColor = tg.get_FontBackColor();
            double theta = 0;
            double stringWidth = 0, stringHeight = 0;
            String s = "";
            BufferedImage image = null;
            int x = 0, y = 0;
            POINT2 pt0 = null, pt1 = null, pt2 = null, pt3 = null;
            int quadrant = -1;
            Shape2 shape2 = null;
            long lineType = tg.get_LineType();
            font = tg.get_Font();    //might have to change this
            if (font == null) {
                font = g2d.getFont();
            }
            if (font.getSize() == 0) {
                return;
            }
            g2d.setFont(font);
            FontMetrics metrics = g2d.getFontMetrics();
            //we need a background color
            if (fontBackColor != null) {
                g2d.setBackground(fontBackColor);
            } else {
                g2d.setBackground(Color.white);
            }

            int direction = -1;
            Point glyphPosition = null;
            for (j = 0; j < tg.modifiers.size(); j++) {
                modifier = (Modifier2) tg.modifiers.get(j);

                double lineFactor = modifier.lineFactor;

                if (isTextFlipped) {
                    lineFactor = -lineFactor;
                }

                s = modifier.text;
                if (s == null || s.equals("")) {

                    image = modifier.image;
                    if (image == null) {
                        continue;
                    }
                }
                stringWidth = s != null ? (double) metrics.stringWidth(s) + 1 : image.getWidth() + 1;
                stringHeight = s != null ? (double) font.getSize() : image.getHeight();

                double x1 = 0, y1 = 0, x2 = 0, y2 = 0, dist = 0;
                pt0 = modifier.textPath[0];
                x1 = Math.round(pt0.x);
                y1 = Math.round(pt0.y);
                pt1 = modifier.textPath[1];
                x2 = Math.round(pt1.x);
                y2 = Math.round(pt1.y);
                theta = Math.atan2(y2 - y1, x2 - x1);
                POINT2 midPt;
                if (x1 > x2) {
                    theta -= Math.PI;
                }
                pt0 = new POINT2(x1, y1);
                pt1 = new POINT2(x2, y2);
                midPt = new POINT2((x1 + x2) / 2, (y1 + y2) / 2);
                Point2D modifierPosition = null;  //use this if using justify
                int justify = ShapeInfo.justify_left;
                switch (modifier.type) {
                    case aboveEnd: // On line
                    case toEnd: // Next to line
                        if (x1 == x2) {
                            x2 += 1;
                        }

                        if (lineFactor >= 0) {
                            direction = 2;
                        } else {
                            direction = 3;
                        }

                        if (lineType == TacticalLines.LC || tg.get_Client().equalsIgnoreCase("ge")) {
                            direction = switchDirection(direction);
                        }

                        if ((modifier.type == toEnd && x1 < x2) || (modifier.type == aboveEnd && x2 < x1)) {
                            justify = ShapeInfo.justify_right;
                        } else {
                            justify = ShapeInfo.justify_left;
                        }

                        pt3 = lineutility.ExtendDirectedLine(pt1, pt0, pt0, direction, lineFactor * stringHeight);

                        glyphPosition = new Point((int) pt3.x, (int) pt3.y);
                        modifierPosition = new Point2D.Double(pt3.x, pt3.y);
                        break;
                    case aboveStartInside:
                        pt3 = lineutility.ExtendAlongLineDouble(pt0, pt1, stringWidth);

                        glyphPosition = new Point((int) pt3.x, (int) pt3.y);
                        modifierPosition = new Point2D.Double((int) pt3.x, pt3.y);
                        break;
                    case aboveEndInside:
                        pt3 = lineutility.ExtendAlongLineDouble(pt1, pt0, stringWidth);

                        glyphPosition = new Point((int) pt3.x, (int) pt3.y);
                        modifierPosition = new Point2D.Double((int) pt3.x, pt3.y);
                        break;
                    case aboveMiddle:
                    case aboveMiddlePerpendicular:
                        pt2 = midPt;
                        if (tg.get_Client().equals("2D")) {
                            lineFactor += 0.5;
                        }

                        if (lineFactor >= 0) {
                            pt3 = lineutility.ExtendDirectedLine(pt0, pt2, pt2, 3, Math.abs((lineFactor) * stringHeight));
                            midPt = lineutility.ExtendDirectedLine(pt0, midPt, midPt, 3, Math.abs((lineFactor) * stringHeight));
                        } else {
                            pt3 = lineutility.ExtendDirectedLine(pt0, pt2, pt2, 2, Math.abs((lineFactor) * stringHeight));
                            midPt = lineutility.ExtendDirectedLine(pt0, midPt, midPt, 2, Math.abs((lineFactor) * stringHeight));
                        }
                        //pt3=lineutility.ExtendDirectedLine(pt0, pt2, pt2, 2, lineFactor*stringHeight);
                        if (x1 == x2 && y1 > y2) {
                            pt3 = lineutility.ExtendDirectedLine(pt0, pt2, pt2, 1, Math.abs((lineFactor) * stringHeight));
                            midPt = lineutility.ExtendDirectedLine(pt0, midPt, midPt, 1, Math.abs((lineFactor) * stringHeight));
                        }
                        if (x1 == x2 && y1 < y2) {
                            pt3 = lineutility.ExtendDirectedLine(pt0, pt2, pt2, 0, Math.abs((lineFactor) * stringHeight));
                            midPt = lineutility.ExtendDirectedLine(pt0, midPt, midPt, 0, Math.abs((lineFactor) * stringHeight));
                        }

                        glyphPosition = new Point((int) pt3.x, (int) pt3.y);
                        justify = ShapeInfo.justify_center;
                        modifierPosition = new Point2D.Double(midPt.x, midPt.y);

                        if(modifier.type == aboveMiddlePerpendicular) {
                            // Need to negate the original rotation
                            if (x1 > x2) {
                                theta += Math.PI;
                            }
                            // Adjust the label rotation based on the y values
                            if (y1 > y2) {
                                theta += Math.PI;
                            }
                            // Rotate by 90 degrees. This is how we rotate the label perpendicular to the line
                            theta -= Math.PI / 2;
                        }
                        break;
                    case area:
                        theta = 0;

                        //y = (int) y1 + (int) (stringHeight / 2) + (int) (1.25 * lineFactor * stringHeight);
                        y = (int) y1 + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                        x = image != null ? (int) (x1 - stringWidth / 3) : (int) x1;

                        glyphPosition = new Point(x, y);
                        justify = ShapeInfo.justify_center;
                        modifierPosition = new Point2D.Double(x, y);
                        break;
                    case areaImage:
                        glyphPosition = new Point((int)x1, (int)y1);
                        justify = ShapeInfo.justify_center;
                        modifierPosition = new Point2D.Double((int)x1, (int)y1);
                        break;
                    case screen:    //for SCREEN, GUARD, COVER, not currently used
                        if (tg.Pixels.size() >= 14) {
                            pt1 = tg.Pixels.get(3);
                            pt2 = tg.Pixels.get(10);
                            quadrant = lineutility.GetQuadrantDouble(pt1, pt2);
                            theta = Math.atan2(pt2.y - pt1.y, pt2.x - pt1.x);
                            if (Math.abs(theta) < Math.PI / 8) {
                                if (theta < 0) {
                                    theta -= Math.PI / 2;
                                } else {
                                    theta += Math.PI / 2;
                                }
                            }
                            switch (quadrant) {
                                case 1:
                                    theta += Math.PI / 2;
                                    break;
                                case 2:
                                    theta -= Math.PI / 2;
                                    break;
                                case 3:
                                    theta -= Math.PI / 2;
                                    break;
                                case 4:
                                    theta += Math.PI / 2;
                                    break;
                                default:
                                    break;
                            }

                            x = (int) x1 - (int) stringWidth / 2;
                            y = (int) y1 - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            y = (int) y1 + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                        } else {
                            theta = 0;
                            x = (int) tg.Pixels.get(0).x;
                            y = (int) tg.Pixels.get(0).y;
                            x = (int) x - (int) stringWidth / 2;
                            y = (int) y - (int) stringHeight / 2 + (int) (lineFactor * stringHeight);
                            y = (int) y + (int) (stringHeight / 2) + (int) (lineFactor * stringHeight);
                        }

                        glyphPosition = new Point(x, y);
                        //glyphPosition=new Point2D.Double(x,y);
                        break;
                    default:
                        break;
                }   //end switch

                shape2 = new Shape2(Shape2.SHAPE_TYPE_MODIFIER_FILL);

                shape2.setStroke(new BasicStroke(0, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 3));

                if (tg.get_TextColor() != null) {
                    shape2.setFillColor(tg.get_TextColor());
                } else if (tg.get_LineColor() != null) {
                    shape2.setFillColor(tg.get_LineColor());
                }
                if (tg.get_LineColor() != null) {
                    shape2.setLineColor(tg.get_LineColor());
                }
                //only GE uses the converter, generic uses the affine transform and draws at 0,0
                if (converter != null) {
                    shape2.setGlyphPosition(glyphPosition);
                } else {
                    shape2.setGlyphPosition(new Point2D.Double(0, 0));
                }
                //shape2.setGlyphPosition(new Point(0,0));
                //added two settings for use by GE
                if(s != null && !s.equals("")) {
                    shape2.setModifierString(s);
                    TextLayout tl = new TextLayout(s, font, g2d.getFontMetrics().getFontRenderContext());
                    shape2.setTextLayout(tl);
                    shape2.setTextJustify(justify);
                } else if (image != null) {
                    shape2.setModifierImage(image);
                }
                //shape2.setModifierStringPosition(glyphPosition);//M. Deutch 7-6-11
                shape2.setModifierAngle(theta * 180 / Math.PI);
                shape2.setModifierPosition(modifierPosition);

                if (shape2 != null) {
                    shapes.add(shape2);
                }

            }   //end for
        } //end try
        catch (Exception exc) {
            ErrorLogger.LogException(_className, "DisplayModifiers2",
                    exc);
        }
    }//end function

    /**
     * Builds a shape object to wrap text
     *
     * @param g2d the Graphic object for drawing
     * @param str text to wrap
     * @param font the draw font
     * @param tx the drawing transform, text rotation and translation
     * @return
     */
    public static Shape getTextShape(Graphics2D g2d,
                                     String str,
                                     Font font,
                                     AffineTransform tx) {
        TextLayout tl = null;
        FontRenderContext frc = null;
        try {
            frc = g2d.getFontRenderContext();
            tl = new TextLayout(str, font, frc);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getTextShape",
                    new RendererException("Failed inside getTextShape", exc));
        }
        return tl.getOutline(tx);
    }

    /**
     * Creates text outline as a shape
     *
     * @param originalText the original text
     * @return text shape
     */
    public static Shape2 createTextOutline(Shape2 originalText) {
        Shape2 siOutline = null;
        try {
            Shape outline = originalText.getShape();

            siOutline = new Shape2(Shape2.SHAPE_TYPE_MODIFIER_FILL);
            siOutline.setShape(outline);

            if (originalText.getFillColor().getRed() == 255
                    && originalText.getFillColor().getGreen() == 255
                    && originalText.getFillColor().getBlue() == 255) {
                siOutline.setLineColor(Color.BLACK);
            } else {
                siOutline.setLineColor(Color.WHITE);
            }

            int width = RendererSettings.getInstance().getTextOutlineWidth();

            siOutline.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 3));

        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "createTextOutline",
                    new RendererException("Failed inside createTextOutline", exc));
        }
        return siOutline;
    }

    /**
     * Channels don't return points in tg.Pixels. For Channels modifiers we only
     * need to collect the points, don't need internal arrays, and can calculate
     * on which segments the modifiers lie.
     *
     * @param shape
     * @return
     */
    private static ArrayList<POINT2> getShapePoints(Shape shape) {
        try {
            ArrayList<Point2D> ptsPoly = new ArrayList();
            Point2D ptPoly = null;
            double[] coords = new double[6];
            int zeros = 0;
            for (PathIterator i = shape.getPathIterator(null); !i.isDone(); i.next()) {
                int type = i.currentSegment(coords);
                if (type == 0 && zeros == 2) {
                    break;
                }
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        ptPoly = new Point2D.Double(coords[0], coords[1]);
                        ptsPoly.add(ptPoly);
                        zeros++;
                        break;
                    case PathIterator.SEG_LINETO:
                        ptPoly = new Point2D.Double(coords[0], coords[1]);
                        ptsPoly.add(ptPoly);
                        break;
                    case PathIterator.SEG_QUADTO: //quadTo was never used
                        break;
                    case PathIterator.SEG_CUBICTO:  //curveTo was used for some METOC's
                        break;
                    case PathIterator.SEG_CLOSE:    //closePath was never used
                        break;
                }
            }
            if (ptsPoly.size() > 0) {
                ArrayList<POINT2> pts = null;
                pts = new ArrayList();
                for (int j = 0; j < ptsPoly.size(); j++) {
                    Point2D pt2d = ptsPoly.get(j);
                    POINT2 pt = new POINT2(pt2d.getX(), pt2d.getY());
                    pts.add(pt);
                }
                return pts;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "getshapePoints",
                    new RendererException("Failed inside getShapePoints", exc));
        }
        return null;
    }

    private static BufferedImage getImageModifier(TGLight tg) {
        String symbolID = tg.get_SymbolId();
        ImageInfo symbol = null;
        Map<String, String> mods = new HashMap<>();
        Map<String, String> sa = new HashMap<>();
        sa.put(MilStdAttributes.PixelSize, String.valueOf(tg.getIconSize()));
        int contaminationCode = EntityCode.getSymbolForContaminationArea(SymbolID.getEntityCode(symbolID));
        int modifier1Code = SymbolID.getModifier1(symbolID);
        int lineType = GetLinetypeFromString(symbolID);
        if (contaminationCode > 0) {
            sa.put(MilStdAttributes.OutlineSymbol, "true");
            sa.put(MilStdAttributes.FillColor, RendererUtilities.colorToHexString(tg.get_FillColor(), true));
            sa.put(MilStdAttributes.LineColor, RendererUtilities.colorToHexString(tg.get_LineColor(), true));
            String contaminationSP = SymbolID.setEntityCode(symbolID, contaminationCode);
            contaminationSP = SymbolID.setHQTFD(contaminationSP, 0); // Remove fdi if necessary
            symbol = SinglePointRenderer.getInstance().RenderSP2(contaminationSP, mods, sa);
        } else if (lineType == TacticalLines.DEPICT || lineType == TacticalLines.MINED || lineType == TacticalLines.FENCED || lineType == TacticalLines.MINE_LINE) {
            if (modifier1Code < 13 || modifier1Code > 50) {
                // Invalid mine type
                modifier1Code = 13;//unspecified mine (default value if not specified as per MilStd 2525)
                symbolID = SymbolID.setModifier1(symbolID, modifier1Code);
            }
            if (tg.get_KeepUnitRation()) {
                sa.put(MilStdAttributes.PixelSize, String.valueOf((int) (tg.getIconSize() * 1.5)));
            }
            sa.put(MilStdAttributes.OutlineSymbol, "true");
            symbol = SinglePointRenderer.getInstance().RenderModifier2(symbolID, sa);
        } else if (lineType == TacticalLines.LAA && modifier1Code > 0) {
            sa.put(MilStdAttributes.OutlineSymbol, "true");
            sa.put(MilStdAttributes.FillColor, RendererUtilities.colorToHexString(tg.get_FillColor(), true));
            sa.put(MilStdAttributes.LineColor, RendererUtilities.colorToHexString(tg.get_LineColor(), true));
            if (tg.get_KeepUnitRation()) {
                sa.put(MilStdAttributes.PixelSize, String.valueOf((int) (tg.getIconSize() * 1.5)));
            }
            symbol = SinglePointRenderer.getInstance().RenderModifier2(symbolID, sa);
        } else if (lineType == TacticalLines.ANCHORAGE_LINE || lineType == TacticalLines.ANCHORAGE_AREA) {
            sa.put(MilStdAttributes.OutlineSymbol, "false");
            String anchorPoint = SymbolID.setEntityCode(symbolID, EntityCode.EntityCode_AnchoragePoint);
            symbol = SinglePointRenderer.getInstance().RenderSP2(anchorPoint, mods, sa);
        }

        if (symbol != null)
            return symbol.getImage();
        else
            return null;
    }
}
