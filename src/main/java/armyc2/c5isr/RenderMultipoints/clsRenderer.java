/*
 * A class to serve JavaRendererServer
 */
package armyc2.c5isr.RenderMultipoints;

import armyc2.c5isr.JavaLineArray.CELineArray;
import armyc2.c5isr.JavaLineArray.POINT2;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import armyc2.c5isr.JavaLineArray.DISMSupport;

import armyc2.c5isr.JavaLineArray.Shape2;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.JavaLineArray.lineutility;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.JavaTacticalRenderer.Modifier2;
import armyc2.c5isr.JavaTacticalRenderer.mdlGeodesic;

import armyc2.c5isr.renderer.utilities.*;


/**
 * Rendering class
 *
 * 
 */
public final class clsRenderer {

    private static final String _className = "clsRenderer";

    /**
     * Set tg geo points from the client points
     *
     * @param milStd
     * @param tg
     */
    private static void setClientCoords(MilStdSymbol milStd,
                                        TGLight tg) {
        try {
            ArrayList<POINT2> latLongs = new ArrayList();
            int j = 0;
            ArrayList<Point2D> coords = milStd.getCoordinates();
            Point2D pt2d = null;
            POINT2 pt2 = null;
            int n = coords.size();
            //for (j = 0; j < coords.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2d = coords.get(j);
                pt2 = clsUtility.Point2DToPOINT2(pt2d);
                latLongs.add(pt2);
            }
            tg.set_LatLongs(latLongs);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "setClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + milStd.getSymbolID(), exc));
        }
    }

    private static ArrayList<Point2D> getClientCoords(TGLight tg) {
        ArrayList<Point2D> coords = null;
        try {
            int j = 0;
            Point2D pt2d = null;
            POINT2 pt2 = null;
            coords = new ArrayList();
            int n = tg.LatLongs.size();
            //for (j = 0; j < tg.LatLongs.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2 = tg.LatLongs.get(j);
                pt2d = new Point2D.Double(pt2.x, pt2.y);
                coords.add(pt2d);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "getClientCoords",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return coords;
    }

    /**
     * Create MilStdSymbol from tactical graphic
     *
     * @deprecated
     * @param tg tactical graphic
     * @param converter geographic to pixels to converter
     * @return MilstdSymbol object
     */
    public static MilStdSymbol createMilStdSymboFromTGLight(TGLight tg, IPointConversion converter) {
        MilStdSymbol milStd = null;
        try {
            String symbolId = tg.get_SymbolId();
            int lineType = armyc2.c5isr.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            String status = tg.get_Status();
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            boolean isClosedArea = armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);
            if (isClosedArea) {
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            ArrayList<Point2D> coords = getClientCoords(tg);
            tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            Map<String,String> modifiers = new HashMap<>();
            modifiers.put(Modifiers.W_DTG_1, tg.get_DTG());
            modifiers.put(Modifiers.W1_DTG_2, tg.get_DTG1());
            modifiers.put(Modifiers.H_ADDITIONAL_INFO_1, tg.get_H());
            modifiers.put(Modifiers.H1_ADDITIONAL_INFO_2, tg.get_H1());
            modifiers.put(Modifiers.H2_ADDITIONAL_INFO_3, tg.get_H2());
            modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, tg.get_Name());
            modifiers.put(Modifiers.T1_UNIQUE_DESIGNATION_2, tg.get_T1());
            modifiers.put(Modifiers.Y_LOCATION, tg.get_Location());
            modifiers.put(Modifiers.N_HOSTILE, tg.get_N());

            milStd = new MilStdSymbol(symbolId, "1", coords, modifiers);
            milStd.setFillColor(tg.get_FillColor());
            milStd.setLineColor(tg.get_LineColor());
            milStd.setLineWidth(tg.get_LineThickness());
            milStd.setFillStyle(tg.get_TexturePaint());
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createMilStdSymboFromTGLight",
                    new RendererException("Failed to set geo points or pixels for " + tg.get_SymbolId(), exc));
        }
        return milStd;
    }

    /**
     * Build a tactical graphic object from the client MilStdSymbol
     *
     * @param milStd MilstdSymbol object
     * @param converter geographic to pixels converter
     * @return tactical graphic
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter) {
        TGLight tg = new TGLight();
        try {
            String symbolId = milStd.getSymbolID();
            tg.set_SymbolId(symbolId);
            boolean useLineInterpolation = milStd.getUseLineInterpolation();
            tg.set_UseLineInterpolation(useLineInterpolation);
            int lineType = armyc2.c5isr.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            tg.set_LineType(lineType);
            String status = tg.get_Status();
            if (status != null && status.equals("A")) {
                tg.set_LineStyle(1);
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            //tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            tg.set_Font(RendererSettings.getInstance().getMPLabelFont());

            tg.set_FillColor(milStd.getFillColor());
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());

            tg.setIconSize(milStd.getUnitSize());
            tg.set_KeepUnitRatio(milStd.getKeepUnitRatio());

            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(milStd.getTextColor());
            if (milStd.getModifier(Modifiers.W_DTG_1) != null) {
                tg.set_DTG(milStd.getModifier(Modifiers.W_DTG_1));
            }
            if (milStd.getModifier(Modifiers.W1_DTG_2) != null) {
                tg.set_DTG1(milStd.getModifier(Modifiers.W1_DTG_2));
            }
            if (milStd.getModifier(Modifiers.H_ADDITIONAL_INFO_1) != null) {
                tg.set_H(milStd.getModifier(Modifiers.H_ADDITIONAL_INFO_1));
            }
            if (milStd.getModifier(Modifiers.H1_ADDITIONAL_INFO_2) != null) {
                tg.set_H1(milStd.getModifier(Modifiers.H1_ADDITIONAL_INFO_2));
            }
            if (milStd.getModifier(Modifiers.H2_ADDITIONAL_INFO_3) != null) {
                tg.set_H2(milStd.getModifier(Modifiers.H2_ADDITIONAL_INFO_3));
            }
            if (milStd.getModifier(Modifiers.T_UNIQUE_DESIGNATION_1) != null) {
                tg.set_Name(milStd.getModifier(Modifiers.T_UNIQUE_DESIGNATION_1));
            }
            if (milStd.getModifier(Modifiers.T1_UNIQUE_DESIGNATION_2) != null) {
                tg.set_T1(milStd.getModifier(Modifiers.T1_UNIQUE_DESIGNATION_2));
            }
            if (milStd.getModifier(Modifiers.V_EQUIP_TYPE) != null) {
                tg.set_V(milStd.getModifier(Modifiers.V_EQUIP_TYPE));
            }
            if (milStd.getModifier(Modifiers.AS_COUNTRY) != null) {
                tg.set_AS(milStd.getModifier(Modifiers.AS_COUNTRY));
            }
            if (milStd.getModifier(Modifiers.AP_TARGET_NUMBER) != null) {
                tg.set_AP(milStd.getModifier(Modifiers.AP_TARGET_NUMBER));
            }
            if (milStd.getModifier(Modifiers.Y_LOCATION) != null) {
                tg.set_Location(milStd.getModifier(Modifiers.Y_LOCATION));
            }
            if (milStd.getModifier(Modifiers.N_HOSTILE) != null) {
                tg.set_N(milStd.getModifier(Modifiers.N_HOSTILE));
            }
            tg.set_UseDashArray(milStd.getUseDashArray());
            tg.set_UseHatchFill(milStd.getUseFillPattern());
            //tg.set_UsePatternFill(milStd.getUseFillPattern());
            tg.set_HideOptionalLabels(milStd.getHideOptionalLabels());
            boolean isClosedArea = armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if (lineType == TacticalLines.STRIKWARN) {
                ArrayList<POINT2> poly1Pixels = new ArrayList<>(tg.Pixels.subList(0, tg.Pixels.size() / 2));
                ArrayList<POINT2> poly1LatLons = new ArrayList<>(tg.LatLongs.subList(0, tg.LatLongs.size() / 2));
                ArrayList<POINT2> poly2Pixels = new ArrayList<>(tg.Pixels.subList(tg.Pixels.size() / 2, tg.Pixels.size()));
                ArrayList<POINT2> poly2LatLons = new ArrayList<>(tg.LatLongs.subList(tg.LatLongs.size() / 2, tg.LatLongs.size()));

                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(poly1Pixels);
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(poly1LatLons);
                tg.Pixels = poly1Pixels;
                tg.LatLongs = poly1LatLons;

                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(poly2Pixels);
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(poly2LatLons);
                tg.Pixels.addAll(poly2Pixels);
                tg.LatLongs.addAll(poly2LatLons);
            }
            else if (isClosedArea) {
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //implement meters to feet for altitude labels
            String altitudeLabel = milStd.getAltitudeMode();
            if (altitudeLabel == null || altitudeLabel.isEmpty()) {
                altitudeLabel = "AMSL";
            }
            DistanceUnit altitudeUnit = milStd.getAltitudeUnit();
            if(altitudeUnit == null){
                altitudeUnit = DistanceUnit.FEET;
            }
            DistanceUnit distanceUnit = milStd.getDistanceUnit();
            if(distanceUnit == null){
                distanceUnit = DistanceUnit.METERS;
            }

            String strXAlt = "";
            //construct the H1 and H2 modifiers for sector from the mss AM, AN, and X arraylists
            if (lineType == TacticalLines.RANGE_FAN_SECTOR) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH);
                ArrayList<Double> X = milStd.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
                if (AM != null) {
                    String strAM = "";
                    for (int j = 0; j < AM.size(); j++) {
                        strAM += Double.toString(AM.get(j));
                        if (j < AM.size() - 1) {
                            strAM += ",";
                        }
                    }
                    tg.set_AM(strAM);
                }
                if (AN != null) {
                    String strAN = "";
                    for (int j = 0; j < AN.size(); j++) {
                        strAN += AN.get(j);
                        if (j < AN.size() - 1) {
                            strAN += ",";
                        }
                    }
                    tg.set_AN(strAN);
                }
                if (X != null) {
                    String strX = "";
                    for (int j = 0; j < X.size(); j++) {
                        strXAlt = createAltitudeLabel(X.get(j), altitudeUnit, altitudeLabel);
                        strX += strXAlt;

                        if (j < X.size() - 1) {
                            strX += ",";
                        }
                    }
                    tg.set_X(strX);
                }
                if (AM != null && AN != null) {
                    int numSectors = AN.size() / 2;
                    double left, right, min = 0, max = 0;
                    //construct left,right,min,max from the arraylists
                    String strLeftRightMinMax = "";
                    for (int j = 0; j < numSectors; j++) {
                        left = AN.get(2 * j);
                        right = AN.get(2 * j + 1);
                        if (j + 1 == AM.size()) {
                            break;
                        }
                        min = AM.get(j);
                        max = AM.get(j + 1);
                        strLeftRightMinMax += Double.toString(left) + "," + Double.toString(right) + "," + Double.toString(min) + "," + Double.toString(max);
                        if (j < numSectors - 1) {
                            strLeftRightMinMax += ",";
                        }

                    }
                    int len = strLeftRightMinMax.length();
                    String c = strLeftRightMinMax.substring(len - 1, len);
                    if (c.equalsIgnoreCase(",")) {
                        strLeftRightMinMax = strLeftRightMinMax.substring(0, len - 1);
                    }
                    tg.set_LRMM(strLeftRightMinMax);
                }
            } else if (lineType == TacticalLines.RADAR_SEARCH) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH);
                if (AM != null) {
                    String strAM = "";
                    for (int j = 0; j < AM.size() && j < 2; j++) {
                        strAM += Double.toString(AM.get(j));
                        if (j < AM.size() - 1) {
                            strAM += ",";
                        }
                    }
                    tg.set_AM(strAM);
                }
                if (AN != null) {
                    String strAN = "";
                    for (int j = 0; j < AN.size() && j < 2; j++) {
                        strAN += AN.get(j);
                        if (j < AN.size() - 1) {
                            strAN += ",";
                        }
                    }
                    tg.set_AN(strAN);
                }
                if (AM != null && AN != null) {
                    double left, right, min = 0, max = 0;
                    //construct left,right,min,max from the arraylists
                    String strLeftRightMinMax = "";
                    left = AN.get(0);
                    right = AN.get(1);
                    min = AM.get(0);
                    max = AM.get(1);
                    strLeftRightMinMax += Double.toString(left) + "," + Double.toString(right) + "," + Double.toString(min) + "," + Double.toString(max);
                    tg.set_LRMM(strLeftRightMinMax);
                }
            }
            int j = 0;
            if (lineType == TacticalLines.LAUNCH_AREA || lineType == TacticalLines.DEFENDED_AREA_CIRCULAR || lineType == TacticalLines.SHIP_AOI_CIRCULAR) //geo ellipse
            {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH);
                if (AM != null && AM.size() > 1) {
                    String strAM = AM.get(0).toString(); // major axis
                    tg.set_AM(strAM);
                    String strAM1 = AM.get(1).toString(); // minor axis
                    tg.set_AM1(strAM1);
                }
                if (AN != null && AN.size() > 0) {
                    String strAN = AN.get(0).toString(); // rotation
                    tg.set_AN(strAN);
                }
            }
            switch (lineType) {
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
                case TacticalLines.ACA:
                case TacticalLines.ACA_RECTANGULAR:
                case TacticalLines.ACA_CIRCULAR:
                    ArrayList<Double> X = milStd.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
                    if (X != null && X.size() > 0) {
                        strXAlt = createAltitudeLabel(X.get(0), altitudeUnit, altitudeLabel);
                        tg.set_X(strXAlt);
                    }
                    if (X != null && X.size() > 1) {
                        strXAlt = createAltitudeLabel(X.get(1), altitudeUnit, altitudeLabel);
                        tg.set_X1(strXAlt);
                    }
                    break;
                case TacticalLines.SC:
                case TacticalLines.MRR:
                case TacticalLines.SL:
                case TacticalLines.TC:
                case TacticalLines.LLTR:
                case TacticalLines.AC:
                case TacticalLines.SAAFR:
                    POINT2 pt = tg.LatLongs.get(0);
                    Point2D pt2d0 = new Point2D.Double(pt.x, pt.y);
                    Point2D pt2d0Pixels = converter.GeoToPixels(pt2d0);
                    POINT2 pt0Pixels = new POINT2(pt2d0Pixels.getX(), pt2d0Pixels.getY());

                    //get some point 10000 meters away from pt
                    //10000 should work for any scale                    
                    double dist = 10000;
                    POINT2 pt2 = mdlGeodesic.geodesic_coordinate(pt, dist, 0);
                    Point2D pt2d1 = new Point2D.Double(pt2.x, pt2.y);
                    Point2D pt2d1Pixels = converter.GeoToPixels(pt2d1);
                    POINT2 pt1Pixels = new POINT2(pt2d1Pixels.getX(), pt2d1Pixels.getY());
                    //calculate pixels per meter
                    double distPixels = lineutility.CalcDistanceDouble(pt0Pixels, pt1Pixels);
                    double pixelsPerMeter = distPixels / dist;

                    ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                    if (AM != null) {
                        String strAM = "";
                        for (j = 0; j < AM.size(); j++) {
                            strAM += AM.get(j).toString();
                            if (j < AM.size() - 1) {
                                strAM += ",";
                            }
                        }
                        tg.set_AM(strAM);
                    }
                    String[] strRadii = null;
                    //get the widest value
                    //the current requirement is to use the greatest width as the default width
                    double maxWidth = 0,
                    temp = 0;
                    double maxWidthMeters = 0;
                    if (tg.get_AM() != null && tg.get_AM().isEmpty() == false) {
                        strRadii = tg.get_AM().split(",");
                        if (strRadii.length > 0) {
                            for (j = 0; j < strRadii.length; j++) {
                                if (!Double.isNaN(Double.parseDouble(strRadii[j]))) {
                                    temp = Double.parseDouble(strRadii[j]);
                                    if (temp > maxWidth) {
                                        maxWidth = temp;
                                    }
                                }
                            }
                            maxWidthMeters = maxWidth;
                            maxWidth *= pixelsPerMeter / 2;

                            for (j = 0; j < tg.Pixels.size(); j++) {
                                if (strRadii.length > j) {
                                    if (!Double.isNaN(Double.parseDouble(strRadii[j]))) {
                                        double pixels = Double.parseDouble(strRadii[j]) * pixelsPerMeter / 2;
                                        tg.Pixels.get(j).style = (int) pixels;
                                        tg.LatLongs.get(j).style = (int) pixels;
                                    } else {
                                        tg.Pixels.get(j).style = (int) maxWidth;
                                        tg.LatLongs.get(j).style = (int) maxWidth;
                                    }
                                } else {
                                    tg.Pixels.get(j).style = (int) maxWidth;
                                    tg.LatLongs.get(j).style = (int) maxWidth;
                                }
                            }
                        }
                    }

                    maxWidthMeters *= distanceUnit.conversionFactor;
                    maxWidthMeters *= 10.0;
                    maxWidthMeters = Math.round(maxWidthMeters);
                    int tempWidth = (int) maxWidthMeters;
                    maxWidthMeters = tempWidth / 10.0;

                    tg.set_AM(Double.toString(maxWidthMeters) + " " + distanceUnit.label);
                    //use X, X1 to set tg.H, tg.H1
                    X = milStd.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
                    if (X != null && X.size() > 0) {
                        strXAlt = createAltitudeLabel(X.get(0), altitudeUnit, altitudeLabel);
                        tg.set_X(strXAlt);
                    }
                    if (X != null && X.size() > 1) {
                        strXAlt = createAltitudeLabel(X.get(1), altitudeUnit, altitudeLabel);
                        tg.set_X1(strXAlt);
                    }
                    break;
                default:
                    break;
            }
            //circular range fans
            if (lineType == TacticalLines.RANGE_FAN) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                ArrayList<Double> X = milStd.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
                String strAM = "";
                String strX = "";
                if (AM != null) {
                    // Range fan circular has a maximum of 3 circles
                    for (j = 0; j < AM.size() && j < 3; j++) {
                        strAM += Double.toString(AM.get(j));
                        if (j < AM.size() - 1) {
                            strAM += ",";
                        }

                        if (X != null && j < X.size()) {
                            strXAlt = createAltitudeLabel(X.get(j), altitudeUnit, altitudeLabel);
                            strX += strXAlt;
                            if (j < X.size() - 1) {
                                strX += ",";
                            }
                        }
                    }
                }
                tg.set_AM(strAM);
                tg.set_X(strX);
            }
            switch (lineType) {
                case TacticalLines.PAA_RECTANGULAR:
                case TacticalLines.RECTANGULAR_TARGET:
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
                case TacticalLines.KILLBOXBLUE_RECTANGULAR:
                case TacticalLines.KILLBOXPURPLE_RECTANGULAR:
                    ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                    if (AM != null && AM.size() > 0) {
                        String strAM = Double.toString(AM.get(0));
                        //set width for rectangles or radius for circles
                        tg.set_AM(strAM);
                    }
                    break;
                default:
                    break;
            }
            if (lineType == TacticalLines.RECTANGULAR || lineType == TacticalLines.CUED_ACQUISITION) {
                ArrayList<Double> AM = milStd.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                ArrayList<Double> AN = milStd.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH);
                if (AN == null) {
                    AN = new ArrayList<>();
                }
                if (AN.isEmpty()) {
                    AN.add(0d);
                }

                if (AM != null && AM.size() > 1) {
                    String strAM = Double.toString(AM.get(0));    //width
                    String strAM1 = Double.toString(AM.get(1));     //length
                    //set width and length in meters for rectangular target
                    tg.set_AM(strAM);
                    tg.set_AM1(strAM1);
                    //set attitude in degrees
                    String strAN = Double.toString(AN.get(0));
                    tg.set_AN(strAN);
                }
                /*
                if(AM.size()>2)
                {
                    String strH1 = Double.toString(AM.get(2));     //buffer size
                    tg.set_H1(strH1);
                }
                 */
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }

    private static String createAltitudeLabel(double distance, DistanceUnit altitudeUnit, String altitudeLabel){
        double conversionFactor;

        // if using "FL" (Flight Level) for altitudeLabel, override conversion factor to avoid potential user error with altitudeUnit
        if (altitudeLabel.equals("FL")) {
            conversionFactor = DistanceUnit.FLIGHT_LEVEL.conversionFactor;
        } else {
            conversionFactor = altitudeUnit.conversionFactor;
        }

        // Truncate the result
        double result = distance * conversionFactor;
        result *= 10.0;
        result = Math.round(result);
        int tempResult = (int) result;
        int truncatedResult = tempResult / 10;
        // MIL-STD-2525D says altitude/depth must be an integer

        // Simplifies labels of "0 units AGL" to "GL" (Ground Level) and "0 units AMSL/BMSL" to "MSL" (Mean Sea Level)
        // as permitted by MIL-STD-2525D 5.3.7.5.1.
        // Also works for "0 units GL" and "0 units MSL", which are improperly labeled but can be understood to mean the same thing.
        if (truncatedResult == 0) {
            if (altitudeLabel.equals("AGL") || altitudeLabel.equals("GL")) {
                return "GL";
            }
            if (altitudeLabel.equals("AMSL") || altitudeLabel.equals("BMSL") || altitudeLabel.equals("MSL")) {
                return "MSL";
            }
        }

        // Flight level is a special altitude displayed as "FL ###" where ### are 3 digits representing hundreds of feet.
        if (altitudeLabel.equals("FL")) {
            return "FL " + String.format("%03d", truncatedResult);
        }

        return truncatedResult + " " + altitudeUnit.label + " " + altitudeLabel;
    }

    /**
     * @deprecated @param milStd
     * @param converter
     * @param computeChannelPt
     * @return
     */
    public static TGLight createTGLightFromMilStdSymbol(MilStdSymbol milStd,
            IPointConversion converter, Boolean computeChannelPt) {
        TGLight tg = new TGLight();
        try {
            String symbolId = milStd.getSymbolID();
            tg.set_SymbolId(symbolId);
            String status = tg.get_Status();
            if (status != null && status.equals("A")) {
                //lineStyle=GraphicProperties.LINE_TYPE_DASHED;
                tg.set_LineStyle(1);
            }
            tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, converter);
            tg.set_Font(new Font("Arial", Font.PLAIN, 12));
            tg.set_FillColor(milStd.getFillColor());
            tg.set_LineColor(milStd.getLineColor());
            tg.set_LineThickness(milStd.getLineWidth());
            tg.set_TexturePaint(milStd.getFillStyle());
            tg.set_FontBackColor(Color.WHITE);
            tg.set_TextColor(milStd.getTextColor());

//            tg.set_DTG(milStd.getModifier(Modifiers.W_DTG_1));
//            tg.set_DTG1(milStd.getModifier(Modifiers.W1_DTG_2));
//            tg.set_H(milStd.getModifier(Modifiers.H_ADDITIONAL_INFO_1));
//            tg.set_H1(milStd.getModifier(Modifiers.H1_ADDITIONAL_INFO_2));
//            tg.set_H2(milStd.getModifier(Modifiers.H2_ADDITIONAL_INFO_3));
//            tg.set_Name(milStd.getModifier(Modifiers.T_UNIQUE_DESIGNATION_1));
//            tg.set_T1(milStd.getModifier(Modifiers.T1_UNIQUE_DESIGNATION_2));
//            tg.set_Location(milStd.getModifier(Modifiers.Y_LOCATION));
//            tg.set_N(Modifiers.N_HOSTILE);
            if (milStd.getModifier(Modifiers.W_DTG_1) != null) {
                tg.set_DTG(milStd.getModifier(Modifiers.W_DTG_1));
            }
            if (milStd.getModifier(Modifiers.W1_DTG_2) != null) {
                tg.set_DTG1(milStd.getModifier(Modifiers.W1_DTG_2));
            }
            if (milStd.getModifier(Modifiers.H_ADDITIONAL_INFO_1) != null) {
                tg.set_H(milStd.getModifier(Modifiers.H_ADDITIONAL_INFO_1));
            }
            if (milStd.getModifier(Modifiers.H1_ADDITIONAL_INFO_2) != null) {
                tg.set_H1(milStd.getModifier(Modifiers.H1_ADDITIONAL_INFO_2));
            }
            if (milStd.getModifier(Modifiers.H2_ADDITIONAL_INFO_3) != null) {
                tg.set_H2(milStd.getModifier(Modifiers.H2_ADDITIONAL_INFO_3));
            }
            if (milStd.getModifier(Modifiers.T_UNIQUE_DESIGNATION_1) != null) {
                tg.set_Name(milStd.getModifier(Modifiers.T_UNIQUE_DESIGNATION_1));
            }
            if (milStd.getModifier(Modifiers.T1_UNIQUE_DESIGNATION_2) != null) {
                tg.set_T1(milStd.getModifier(Modifiers.T1_UNIQUE_DESIGNATION_2));
            }
            if (milStd.getModifier(Modifiers.V_EQUIP_TYPE) != null) {
                tg.set_V(milStd.getModifier(Modifiers.V_EQUIP_TYPE));
            }
            if (milStd.getModifier(Modifiers.AS_COUNTRY) != null) {
                tg.set_AS(milStd.getModifier(Modifiers.AS_COUNTRY));
            }
            if (milStd.getModifier(Modifiers.AP_TARGET_NUMBER) != null) {
                tg.set_AP(milStd.getModifier(Modifiers.AP_TARGET_NUMBER));
            }
            if (milStd.getModifier(Modifiers.Y_LOCATION) != null) {
                tg.set_Location(milStd.getModifier(Modifiers.Y_LOCATION));
            }
            if (milStd.getModifier(Modifiers.N_HOSTILE) != null) {
                tg.set_N(milStd.getModifier(Modifiers.N_HOSTILE));
            }

            //int lineType=CELineArray.CGetLinetypeFromString(tg.get_SymbolId());
            int lineType = armyc2.c5isr.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(symbolId);
            boolean isClosedArea = armyc2.c5isr.JavaTacticalRenderer.clsUtility.isClosedPolygon(lineType);

            if (isClosedArea) {
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.Pixels);
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.ClosePolygon(tg.LatLongs);
            }

            //these channels need a channel point added
            if (computeChannelPt) {
                switch (lineType) {
                    case TacticalLines.CATK:
                    case TacticalLines.CATKBYFIRE:
                    case TacticalLines.AAAAA:
                    case TacticalLines.AIRAOA:
                    case TacticalLines.MAIN:
                    case TacticalLines.SPT:
                        POINT2 ptPixels = armyc2.c5isr.JavaTacticalRenderer.clsUtility.ComputeLastPoint(tg.Pixels);
                        tg.Pixels.add(ptPixels);
                        //Point pt = clsUtility.POINT2ToPoint(ptPixels);
                        Point2D pt = new Point2D.Double(ptPixels.x, ptPixels.y);
                        //in case it needs the corresponding geo point
                        Point2D ptGeo2d = converter.PixelsToGeo(pt);
                        POINT2 ptGeo = clsUtility.Point2DToPOINT2(ptGeo2d);
                        tg.LatLongs.add(ptGeo);
                        //}
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "createTGLightfromMilStdSymbol",
                    new RendererException("Failed to build multipoint TG for " + milStd.getSymbolID(), exc));
        }
        return tg;
    }

    private static void Shape2ToShapeInfo(ArrayList<ShapeInfo> shapeInfos, ArrayList<Shape2> shapes) {
        try {
            int j = 0;
            Shape2 shape = null;
            if (shapes == null || shapeInfos == null || shapes.size() == 0) {
                return;
            }

            for (j = 0; j < shapes.size(); j++) {
                shape = shapes.get(j);
                shapeInfos.add((ShapeInfo) shape);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "Shape2ToShapeInfo",
                    new RendererException("Failed to build ShapeInfo ArrayList", exc));
        }
    }

    /**
     * Added function to handle when coords or display area spans IDL but not
     * both, it prevents the symbol from rendering if the bounding rectangles
     * don't intersect.
     *
     * @param tg
     * @param converter
     * @param clipArea
     * @return
     */
    public static boolean intersectsClipArea(TGLight tg, IPointConversion converter, Object clipArea)
    {
        boolean result=false;
        try
        {
            if (clipArea==null || tg.LatLongs.size() < 2)
                return true;
            Rectangle2D clipBounds = null;
            ArrayList<Point2D> clipPoints = null;
            
//            if (clipArea != null) {
//                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
//                    clipBounds = (Rectangle2D.Double) clipArea;
//                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
//                    clipBounds = (Rectangle2D) clipArea;
//                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
//                    clipPoints = (ArrayList<Point2D>) clipArea;
//                }
//            }
            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    Rectangle rectx = (Rectangle) clipArea;
                    clipBounds = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;                    
                    //double x0=clipPoints.get(0).getX(),y0=clipPoints.get(0).getY();
                    //double w=clipPoints.get(1).getX()-x0,h=clipPoints.get(3).getY()-y0;
                    //clipBounds = new Rectangle2D.Double(x0, y0, w, h);                    
                    clipBounds=clsUtility.getMBR(clipPoints);
                }
            }
            //assumes we are using clipBounds
            int j = 0;
            double x = clipBounds.getMinX();
            double y = clipBounds.getMinY();
            double width = clipBounds.getWidth();
            double height = clipBounds.getHeight();
            POINT2 tl = new POINT2(x, y);
            POINT2 br = new POINT2(x + width, y + height);
            tl = clsUtility.PointPixelsToLatLong(tl, converter);
            br = clsUtility.PointPixelsToLatLong(br, converter);
            //the latitude range
            //boolean ptInside = false, ptAbove = false, ptBelow = false;
            double coordsLeft = tg.LatLongs.get(0).x;
            double coordsRight = coordsLeft;
            double coordsTop=tg.LatLongs.get(0).y;
            double coordsBottom=coordsTop;
            boolean intersects=false;
            double minx=tg.LatLongs.get(0).x,maxx=minx,maxNegX=0;
            for (j = 0; j < tg.LatLongs.size(); j++)
            {                
                POINT2 pt=tg.LatLongs.get(j);
                if (pt.x < minx)
                    minx = pt.x;
                if (pt.x > maxx)
                    maxx = pt.x;
                if(maxNegX==0 && pt.x<0)
                    maxNegX=pt.x;
                if(maxNegX<0 && pt.x<0 && pt.x>maxNegX)
                    maxNegX=pt.x;
                if (pt.y < coordsBottom)
                    coordsBottom = pt.y;
                if (pt.y > coordsTop)
                    coordsTop = pt.y;                
            }
            boolean coordSpanIDL = false;
            if(maxx==180 || minx==-180)
                coordSpanIDL=true;
            if(maxx-minx>=180)
            {
                coordSpanIDL=true;
                coordsLeft=maxx;
                coordsRight=maxNegX;
            }else
            {
                coordsLeft=minx;
                coordsRight=maxx;
            }
            //if(canClipPoints)
            //{                
                if(br.y<=coordsBottom && coordsBottom <= tl.y)
                    intersects=true;
                else if(coordsBottom<=br.y && br.y <=coordsTop)
                    intersects=true;
                else
                    return false;
            //}
            //if it gets this far then the latitude ranges intersect
            //re-initialize intersects for the longitude ranges
            intersects=false;
            //the longitude range
            //the min and max coords longitude
            boolean boxSpanIDL = false;
            //boolean coordSpanIDL = false;
            if(tl.x==180 || tl.x==-180 || br.x==180 || br.x==-180)
                boxSpanIDL=true;
            else if (Math.abs(br.x - tl.x) > 180)
                boxSpanIDL = true;
            
//            if (coordsRight - coordsLeft > 180)
//            {
//                double temp = coordsLeft;
//                coordsLeft = coordsRight;
//                coordsRight = temp;
//                coordSpanIDL=true;
//            }
            //boolean intersects=false;
            if(coordSpanIDL && boxSpanIDL)
                intersects=true;
            else if(!coordSpanIDL && !boxSpanIDL)   //was && canclipPoints
            {
                if(coordsLeft<=tl.x && tl.x<=coordsRight)
                    intersects=true;
                if(coordsLeft<=br.x && br.x<=coordsRight)
                    intersects=true;
                if(tl.x<=coordsLeft && coordsLeft<=br.x)
                    intersects=true;
                if(tl.x<=coordsRight && coordsRight<=br.x)
                    intersects=true;
            }
            else if(!coordSpanIDL && boxSpanIDL)    //box spans IDL and coords do not
            {   
                if(tl.x<coordsRight && coordsRight<180)
                    intersects=true;
                if(-180<coordsLeft && coordsLeft<br.x)
                    intersects=true;
            }
            else if(coordSpanIDL && !boxSpanIDL)    //coords span IDL and box does not
            {   
                if(coordsLeft<br.x && br.x<180)
                    intersects=true;
                if(-180<tl.x && tl.x<coordsRight)
                    intersects=true;
            }
            return intersects;
            
        }
        catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "intersectsClipArea",
                    new RendererException("Failed inside intersectsClipArea", exc));
        }    
        return result;
    }

    /**
     * Adds Feint, decoy, or dummy indicator to shapes. Does not check if tactical graphic should have indicator
     */
    private static void addFDI(TGLight tg, ArrayList<Shape2> shapes) {
        try {
            MSInfo msi = MSLookup.getInstance().getMSLInfo(tg.get_SymbolId());
            final int drawRule = msi != null ? msi.getDrawRule() : -1;
            final int lineType = tg.get_LineType();

            if (lineType == TacticalLines.MAIN) {
                // Only Axis of Advance with arrowhead in a different location
                ArrayList<POINT2> points = shapes.get(1).getPoints();
                POINT2 ptA = new POINT2(points.get(points.size() - 3));
                POINT2 ptB = new POINT2(points.get(points.size() - 8));
                POINT2 ptC = new POINT2(points.get(points.size() - 7));
                shapes.add(DISMSupport.getFDIShape(tg, ptA, ptB, ptC));
            } else if (drawRule == DrawRules.AXIS1 || drawRule == DrawRules.AXIS2) {
                // Axis of Advance symbols
                ArrayList<POINT2> points = shapes.get(0).getPoints();
                POINT2 ptA = new POINT2(points.get(points.size() / 2 - 1));
                POINT2 ptB = new POINT2(points.get(points.size() / 2));
                POINT2 ptC = new POINT2(points.get(points.size() / 2 + 1));
                shapes.add(DISMSupport.getFDIShape(tg, ptA, ptB, ptC));
            }
            // Direction of attack symbols
            else if (lineType == TacticalLines.DIRATKAIR) {
                ArrayList<POINT2> points = shapes.get(2).getPoints();
                POINT2 ptA = new POINT2(points.get(0));
                POINT2 ptB = new POINT2(points.get(1));
                POINT2 ptC = new POINT2(points.get(2));
                shapes.add(DISMSupport.getFDIShape(tg, ptA, ptB, ptC));
            } else if (lineType == TacticalLines.DIRATKGND) {
                ArrayList<POINT2> points = shapes.get(1).getPoints();
                POINT2 ptA = new POINT2(points.get(7));
                POINT2 ptB = new POINT2(points.get(4));
                POINT2 ptC = new POINT2(points.get(9));
                shapes.add(DISMSupport.getFDIShape(tg, ptA, ptB, ptC));
            } else if (lineType == TacticalLines.DIRATKSPT) {
                ArrayList<POINT2> points = shapes.get(1).getPoints();
                POINT2 ptA = new POINT2(points.get(0));
                POINT2 ptB = new POINT2(points.get(1));
                POINT2 ptC = new POINT2(points.get(2));
                shapes.add(DISMSupport.getFDIShape(tg, ptA, ptB, ptC));
            } else {
                // Shape has no arrow. Put on top of shape
                POINT2 firstPoint = shapes.get(0).getPoints().get(0);
                POINT2 ptUl = new POINT2(firstPoint);
                POINT2 ptUr = new POINT2(firstPoint);
                POINT2 ptLr = new POINT2(firstPoint);
                POINT2 ptLl = new POINT2(firstPoint);
                clsUtility.GetMBR(shapes, ptUl, ptUr, ptLr, ptLl);
                shapes.add(DISMSupport.getFDIShape(tg, ptUl, ptUr));
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "addFDI", new RendererException("failed inside addFDI", exc));
        }
    }

    /**
     * @param mss
     * @param converter
     * @param clipArea
     * @param g2d
     * @deprecated Graphics2D not used
     */
    public static void renderWithPolylines(MilStdSymbol mss,
            IPointConversion converter,
            Object clipArea,
            Graphics2D g2d) {
        try {
            TGLight tg = clsRenderer.createTGLightFromMilStdSymbol(mss, converter);
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            if (intersectsClipArea(tg, converter, clipArea)) {
                render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea, g2d);
            }
            mss.setSymbolShapes(shapeInfos);
            mss.setModifierShapes(modifierShapeInfos);
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }

    /**
     * GoogleEarth renderer uses polylines for rendering
     *
     * @param mss MilStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipArea the clip bounds
     */
    public static void renderWithPolylines(MilStdSymbol mss,
            IPointConversion converter,
            Object clipArea) {
        try {
            TGLight tg = clsRenderer.createTGLightFromMilStdSymbol(mss, converter);
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            if (intersectsClipArea(tg, converter, clipArea)) {
                render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea);
            }
            mss.setSymbolShapes(shapeInfos);
            mss.setModifierShapes(modifierShapeInfos);
            mss.set_WasClipped(tg.get_WasClipped());
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "renderWithPolylines",
                    new RendererException("Failed inside renderWithPolylines", exc));
        }
    }

    /**
     * See render_GE below for comments
     *
     * @param tg
     * @param shapeInfos
     * @param modifierShapeInfos
     * @param converter
     * @param clipArea
     * @param g2d test android-gradle
     * @deprecated Graphics2D not used
     */
    public static void render_GE(TGLight tg,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            IPointConversion converter,
            Object clipArea,
            Graphics2D g2d) //was Rectangle2D
    {
        render_GE(tg, shapeInfos, modifierShapeInfos, converter, clipArea);
    }

    /**
     * Google Earth renderer: Called by mapfragment-demo This is the public
     * interface for Google Earth renderer assumes tg.Pixels is filled assumes
     * the caller instantiated the ShapeInfo arrays
     *
     * @param tg tactical graphic
     * @param shapeInfos symbol ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param converter geographic to pixels coordinate converter
     * @param clipArea clipping bounds in pixels
     */
    public static void render_GE(TGLight tg,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            IPointConversion converter,
            Object clipArea) {
        try {
            reversePointsRevD(tg);

            Rectangle2D clipBounds = null;
            CELineArray.setClient("ge");
//            ArrayList<POINT2> origPixels = null;
//            ArrayList<POINT2> origLatLongs = null;
//            if (clsUtilityGE.segmentColorsSet(tg)) {
//                origPixels=lineutility.getDeepCopy(tg.Pixels);
//                origLatLongs=lineutility.getDeepCopy(tg.LatLongs);
//            }
            ArrayList<POINT2> origFillPixels = lineutility.getDeepCopy(tg.Pixels);

            if (tg.get_LineType() == TacticalLines.LC || tg.get_LineType() == TacticalLines.LC_HOSTILE)
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.SegmentLCPoints(tg, converter);

//            boolean shiftLines = Channels.getShiftLines();
//            if (shiftLines) {
//                String affiliation = tg.get_Affiliation();
//                Channels.setAffiliation(affiliation);
//            }
            //CELineArray.setMinLength(2.5);    //2-27-2013
            ArrayList<Point2D> clipPoints = null;
            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    Rectangle rectx = (Rectangle) clipArea;
                    clipBounds = new Rectangle2D.Double(rectx.x, rectx.y, rectx.width, rectx.height);
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;
                }
            }
            double zoomFactor = clsUtilityGE.getZoomFactor(clipBounds, clipPoints, tg.Pixels);
            //add sub-section to test clipArea if client passes the rectangle
            boolean useClipPoints = false;    //currently not used
            if (useClipPoints == true && clipBounds != null) {
                double x = clipBounds.getMinX();
                double y = clipBounds.getMinY();
                double width = clipBounds.getWidth();
                double height = clipBounds.getHeight();
                clipPoints = new ArrayList();
                clipPoints.add(new Point2D.Double(x, y));
                clipPoints.add(new Point2D.Double(x + width, y));
                clipPoints.add(new Point2D.Double(x + width, y + height));
                clipPoints.add(new Point2D.Double(x, y + height));
                clipPoints.add(new Point2D.Double(x, y));
                clipBounds = null;
            }
            //end section

            if (tg.get_Client() == null || tg.get_Client().isEmpty()) {
                tg.set_client("ge");
            }

            clsUtility.RemoveDuplicatePoints(tg);

            int linetype = tg.get_LineType();
            if (linetype < 0) {
                linetype = armyc2.c5isr.JavaTacticalRenderer.clsUtility.GetLinetypeFromString(tg.get_SymbolId());
                //clsUtilityCPOF.SegmentGeoPoints(tg, converter);
                tg.set_LineType(linetype);
            }

            Boolean isTextFlipped = false;
            ArrayList<Shape2> shapes = null;   //use this to collect all the shapes
            clsUtilityGE.setSplineLinetype(tg);
            setHostileLC(tg);

            clsUtilityCPOF.SegmentGeoPoints(tg, converter, zoomFactor);
            if (clipBounds != null || clipPoints != null) {
                if (clsUtilityCPOF.canClipPoints(tg)) {
                    //check assignment
                    if (clipBounds != null) {
                        clsClipPolygon2.ClipPolygon(tg, clipBounds);
                    } else if (clipPoints != null) {
                        clsClipQuad.ClipPolygon(tg, clipPoints);
                    }

                    clsUtilityGE.removeTrailingPoints(tg, clipArea);
                    tg.LatLongs = clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }
            }

            //if MSR segment data set use original pixels unless tg.Pixels is empty from clipping
//            if (origPixels != null) {
//                if (tg.Pixels.isEmpty()) {
//                    return;
//                } else {
//                    tg.Pixels = origPixels;
//                    tg.LatLongs = origLatLongs;
//                    clipArea = null;
//                }
//            }
            armyc2.c5isr.JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);

            tg.modifiers = new ArrayList();
            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setFont(tg.get_Font());
            Modifier2.AddModifiersGeo(tg, g2d, clipArea, converter);

            clsUtilityCPOF.FilterPoints2(tg, converter);
            armyc2.c5isr.JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            clsUtility.FilterAXADPoints(tg, converter);
            clsUtilityCPOF.ClearPixelsStyle(tg);

            ArrayList<Shape2> linesWithFillShapes = null;

            ArrayList<POINT2> savePixels = tg.Pixels;
            tg.Pixels = origFillPixels;

            //check assignment
            if (clipBounds != null) {
                linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, clipBounds);
            } else if (clipPoints != null) {
                linesWithFillShapes = clsClipQuad.LinesWithFill(tg, clipPoints);
            } else if (clipArea == null) {
                linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, null);
            }

            tg.Pixels = savePixels;

            ArrayList<Shape2> rangeFanFillShapes = null;
            //do not fill the original shapes for circular range fans
            int savefillStyle = tg.get_FillStyle();
            if (linetype == TacticalLines.RANGE_FAN) {
                tg.set_Fillstyle(0);
            }

            //check assignment (pass which clip object is not null)
            if (clipBounds != null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds); //takes clip object           
            } else if (clipPoints != null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipPoints);
            } else if (clipArea == null) {
                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, null);
            }

            // Add Feint, decoy, or dummy indicator
            if (shapes != null
                    && SymbolID.getSymbolSet(tg.get_SymbolId()) == SymbolID.SymbolSet_ControlMeasure
                    && SymbolUtilities.hasFDI(tg.get_SymbolId())) {
                addFDI(tg, shapes);
            }

            switch (linetype) {
                case TacticalLines.RANGE_FAN:
                case TacticalLines.RANGE_FAN_SECTOR:
                case TacticalLines.RADAR_SEARCH:
                    if (tg.get_FillColor() == null || tg.get_FillColor().getAlpha() < 2) {
                        break;
                    }
                    TGLight tg1 = clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                    tg1.set_Fillstyle(savefillStyle);
                    tg1.set_SymbolId(tg.get_SymbolId());
                    //check assignment (pass which clip object is not null)
                    if (clipBounds != null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipBounds);
                    } else if (clipPoints != null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipPoints);
                    } else if (clipArea == null) {
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, null);
                    }

                    if (rangeFanFillShapes != null) {
                        if (shapes == null) {
                            System.out.println("shapes is null");
                            break;
                        } else {
                            shapes.addAll(0, rangeFanFillShapes);
                        }

                    }
                    break;
                default:
                    clsRenderer2.getAutoshapeFillShape(tg, shapes);
                    break;
            }
            //end section

            //undo any fillcolor for lines with fill
            clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
            clsClipPolygon2.addAbatisFill(tg, shapes);

            //if this line is commented then the extra line in testbed goes away
            if (shapes != null && linesWithFillShapes != null && linesWithFillShapes.size() > 0) {
                shapes.addAll(0, linesWithFillShapes);
            }

            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            } else if (clsUtilityCPOF.canClipPoints(tg) == false && clipPoints != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipPoints);
            }
            //returns early if textSpecs are null
            //currently the client is ignoring these
            if (modifierShapeInfos != null) {
                ArrayList<Shape2> textSpecs = new ArrayList();
                Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped, converter);
                Shape2ToShapeInfo(modifierShapeInfos, textSpecs);
            }
            Shape2ToShapeInfo(shapeInfos, shapes);
            clsUtility.addHatchFills(tg, shapeInfos);

            //check assignment (pass which clip object is not null)
            if (clipBounds != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);//takes a clip object            
            } else if (clipPoints != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipPoints);
            } else if (clipArea == null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, null);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render_GE",
                    new RendererException("Failed inside render_GE", exc));

        }
    }
    /**
     * creates a shape for known symbols. The intent is to use client points for
     * the shape and is intended for use with ellipse. If hatch &gt; 1 it creates 2 shapes
     * one for the hatch pattern, the second one is for the outline.
     *
     * @param milStd
     * @param ipc
     * @param clipArea
     * @param shapeType
     * @param lineColor
     * @param fillColor
     * @param hatch
     */
    public static void render_Shape(MilStdSymbol milStd,
            IPointConversion ipc,
            Object clipArea,
            int shapeType,
            Color lineColor,
            Color fillColor,
            int hatch) {
        try {
            Rectangle2D clipBounds = null;
            //CELineArray.setClient("ge");
            ArrayList<Point2D> clipPoints = null;

            if (clipArea != null) {
                if (clipArea.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                    clipBounds = (Rectangle2D.Double) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(Rectangle.class)) {
                    clipBounds = (Rectangle2D) clipArea;
                } else if (clipArea.getClass().isAssignableFrom(ArrayList.class)) {
                    clipPoints = (ArrayList<Point2D>) clipArea;
                }
            }
            
            //can't use following line because it resets the pixels
            //TGLight tg = createTGLightFromMilStdSymbol(milStd, ipc);
            TGLight tg = new TGLight();
            tg.set_SymbolId(milStd.getSymbolID());
            //tg.set_VisibleModifiers(true);
            //set tg latlongs and pixels
            setClientCoords(milStd, tg);
            //build tg.Pixels
            tg.Pixels = clsUtility.LatLongToPixels(tg.LatLongs, ipc);            
            
            //int fillStyle = milStd.getPatternFillType();
            Shape2 shape = new Shape2(shapeType);
            shape.setFillColor(fillColor);
            if (lineColor != null) {
                shape.setLineColor(lineColor);
                shape.setStroke(new BasicStroke(milStd.getLineWidth()));
            }
            //the client has already set the coordinates for the shape
            POINT2 pt;
            for (int j = 0; j < tg.Pixels.size(); j++) {
                pt = tg.Pixels.get(j);
                if (j == 0) {
                    shape.moveTo(pt);
                } else {
                    shape.lineTo(pt);
                }
            }

            //post clip the shape and set the polylines
            ArrayList<Shape2> shapes = new ArrayList();
            shapes.add(shape);
            //post-clip the shape
            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            } else if (clsUtilityCPOF.canClipPoints(tg) == false && clipPoints != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipPoints);
            }
            shape=shapes.get(0);
            if (hatch > 1) 
            {
                shape = clsUtility.buildHatchArea(tg, shape, hatch, 20);
                shape.setLineColor(lineColor);
                shape.setStroke(new BasicStroke(1));
                //shapes.clear();
                shapes.add(shape);
            }
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            Shape2ToShapeInfo(shapeInfos, shapes);
            //set the shapeInfo polylines
            if (clipBounds != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipBounds);
            } else if (clipPoints != null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, clipPoints);
            } else if (clipArea == null) {
                clsUtilityGE.SetShapeInfosPolylines(tg, shapeInfos, null);
            }
            //set milStd symbol shapes
            if (milStd.getSymbolShapes() == null) {
                milStd.setSymbolShapes(shapeInfos);
            } else {
                milStd.getSymbolShapes().addAll(shapeInfos);
            }
            return;
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render_Shape",
                    new RendererException("Failed inside render_Shape", exc));

        }
    }
    /**
     * to follow right hand rule for LC when affiliation is hostile. also fixes
     * MSDZ point order and maybe various other wayward symbols
     *
     * @param tg
     */
    private static void setHostileLC(TGLight tg) {
        try {
            Boolean usas1314 = true;
            ArrayList<POINT2> pts = new ArrayList();
            int j = 0;
            switch (tg.get_LineType()) {
                case TacticalLines.LC:
                    if (usas1314 == false) {
                        break;
                    }
                    if (!tg.isHostile()) {
                        break;
                    }
                    pts = (ArrayList<POINT2>) tg.Pixels.clone();
                    for (j = 0; j < tg.Pixels.size(); j++) {
                        tg.Pixels.set(j, pts.get(pts.size() - j - 1));
                    }
                    //reverse the latlongs also
                    pts = (ArrayList<POINT2>) tg.LatLongs.clone();
                    for (j = 0; j < tg.LatLongs.size(); j++) {
                        tg.LatLongs.set(j, pts.get(pts.size() - j - 1));
                    }
                    break;
                case TacticalLines.LINE:    //CPOF client requests reverse orientation
                    pts = (ArrayList<POINT2>) tg.Pixels.clone();
                    for (j = 0; j < tg.Pixels.size(); j++) {
                        tg.Pixels.set(j, pts.get(pts.size() - j - 1));
                    }
                    //reverse the latlongs also
                    pts = (ArrayList<POINT2>) tg.LatLongs.clone();
                    for (j = 0; j < tg.LatLongs.size(); j++) {
                        tg.LatLongs.set(j, pts.get(pts.size() - j - 1));
                    }
                    break;
                default:
                    return;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "setHostileLC",
                    new RendererException("Failed inside setHostileLC", exc));

        }
    }

    /**
     * set the clip rectangle as an arraylist or a Rectangle2D depending on the
     * object
     *
     * @param clipBounds
     * @param clipRect
     * @param clipArray
     * @return
     */
    private static boolean setClip(Object clipBounds, Rectangle2D clipRect, ArrayList<Point2D> clipArray) {
        try {
            if (clipBounds == null) {
                return false;
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle2D.Double.class)) {
                clipRect.setRect((Rectangle2D) clipBounds);
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle2D.class)) {
                clipRect.setRect((Rectangle2D) clipBounds);
            } else if (clipBounds.getClass().isAssignableFrom(Rectangle.class)) {
                //clipRect.setRect((Rectangle2D)clipBounds);
                Rectangle rectx = (Rectangle) clipBounds;
                //clipBounds=new Rectangle2D.Double(rectx.x,rectx.y,rectx.width,rectx.height);
                clipRect.setRect(rectx.x, rectx.y, rectx.width, rectx.height);
            } else if (clipBounds.getClass().isAssignableFrom(ArrayList.class)) {
                clipArray.addAll((ArrayList) clipBounds);
            }
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "setClip",
                    new RendererException("Failed inside setClip", exc));

        }
        return true;
    }

    /**
     * public render function transferred from JavaLineArrayCPOF project. Use
     * this function to replicate CPOF renderer functionality.
     *
     * @param mss the milStdSymbol object
     * @param converter the geographic to pixels coordinate converter
     * @param clipBounds the pixels based clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            Object clipBounds) {
        try {
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            render(mss, converter, shapeInfos, modifierShapeInfos, clipBounds);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("render", exc));

        }
    }

    /**
     * Generic tester button says Tiger or use JavaRendererSample. Generic
     * renderer testers: called by JavaRendererSample and TestJavaLineArray
     * public render function transferred from JavaLineArrayCPOF project. Use
     * this function to replicate CPOF renderer functionality.
     *
     * @param mss MilStdSymbol
     * @param converter geographic to pixels converter
     * @param shapeInfos ShapeInfo array
     * @param modifierShapeInfos modifier ShapeInfo array
     * @param clipBounds clip bounds
     */
    public static void render(MilStdSymbol mss,
            IPointConversion converter,
            ArrayList<ShapeInfo> shapeInfos,
            ArrayList<ShapeInfo> modifierShapeInfos,
            Object clipBounds) {
        try {
            //boolean shiftLines = Channels.getShiftLines();
            //end section

            Rectangle2D clipRect = new Rectangle2D.Double();
            ArrayList<Point2D> clipArray = new ArrayList();
            setClip(clipBounds, clipRect, clipArray);

            TGLight tg = createTGLightFromMilStdSymbol(mss, converter);
            reversePointsRevD(tg);
            CELineArray.setClient("generic");
//            if (shiftLines) {
//                String affiliation = tg.get_Affiliation();
//                Channels.setAffiliation(affiliation);
//            }
            //CELineArray.setMinLength(2.5);    //2-27-2013

            int linetype = tg.get_LineType();
            //replace calls to MovePixels
            clsUtility.RemoveDuplicatePoints(tg);

            setHostileLC(tg);

            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setFont(tg.get_Font());

            clsUtilityCPOF.SegmentGeoPoints(tg, converter, 1);
            clsUtility.FilterAXADPoints(tg, converter);

            //prevent vertical segments for oneway, twoway, alt
            armyc2.c5isr.JavaTacticalRenderer.clsUtility.FilterVerticalSegments(tg);
            boolean isChange1Area = armyc2.c5isr.JavaTacticalRenderer.clsUtility.IsChange1Area(linetype);
            boolean isTextFlipped = false;
            //for 3d change 1 symbols we do not transform the points

            //if it is world view then we want to flip the far points about
            //the left and right sides to get two symbols
            ArrayList<POINT2> farLeftPixels = new ArrayList();
            ArrayList<POINT2> farRightPixels = new ArrayList();
            if (isChange1Area == false) {
                clsUtilityCPOF.GetFarPixels(tg, converter, farLeftPixels, farRightPixels);
            }

            ArrayList<Shape2> shapesLeft = new ArrayList();
            ArrayList<Shape2> shapesRight = new ArrayList();
            ArrayList<Shape2> shapes = null;   //use this to collect all the shapes

            //CPOF 6.0 diagnostic
            ArrayList<Shape2> textSpecsLeft = null;
            ArrayList<Shape2> textSpecsRight = null;
            //Note: DisplayModifiers3 returns early if textSpecs are null
            textSpecsLeft = new ArrayList();
            textSpecsRight = new ArrayList();

            if (farLeftPixels.size() > 0) {
                tg.Pixels = farLeftPixels;
                shapesLeft = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                Modifier2.DisplayModifiers2(tg, g2d, textSpecsLeft, isTextFlipped, null);
            }
            if (farRightPixels.size() > 0) {
                tg.Pixels = farRightPixels;
                shapesRight = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);
                //CPOF 6.0
                //returns early if textSpecs are null
                Modifier2.DisplayModifiers2(tg, g2d, textSpecsRight, isTextFlipped, null);
            }

            //CPOF 6.0 diagnostic
            ArrayList<Shape2> textSpecs = new ArrayList();

            if (shapesLeft.isEmpty() || shapesRight.isEmpty()) {
                ArrayList<Shape2> linesWithFillShapes = null;
                if (clipArray != null && !clipArray.isEmpty()) {
                    linesWithFillShapes = clsClipQuad.LinesWithFill(tg, clipArray);
                } else if (clipRect != null && clipRect.getWidth() != 0) {
                    linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, clipRect);
                } else {
                    linesWithFillShapes = clsClipPolygon2.LinesWithFill(tg, null);
                }

                //diagnostic: comment two lines if using the WW tester
                if (clsUtilityCPOF.canClipPoints(tg) && clipBounds != null) {
                    if (clipArray != null && !clipArray.isEmpty()) {
                        clsClipQuad.ClipPolygon(tg, clipArray);
                    } else if (clipRect != null && clipRect.getWidth() != 0) {
                        clsClipPolygon2.ClipPolygon(tg, clipRect);
                    }

                    tg.LatLongs = clsUtility.PixelsToLatLong(tg.Pixels, converter);
                }

                //diagnostic 1-28-13
                armyc2.c5isr.JavaTacticalRenderer.clsUtility.InterpolatePixels(tg);

                tg.modifiers = new ArrayList();
                Modifier2.AddModifiersGeo(tg, g2d, clipBounds, converter);

                clsUtilityCPOF.FilterPoints2(tg, converter);
                clsUtilityCPOF.ClearPixelsStyle(tg);
                //add section to replace preceding line M. Deutch 11-4-2011
                ArrayList rangeFanFillShapes = null;
                //do not fill the original shapes for circular range fans
                int savefillStyle = tg.get_FillStyle();
                if (linetype == TacticalLines.RANGE_FAN) {
                    tg.set_Fillstyle(0);
                }

                shapes = clsRenderer2.GetLineArray(tg, converter, isTextFlipped, clipBounds);

                // Add Feint, decoy, or dummy indicator
                if (shapes != null
                        && SymbolID.getSymbolSet(tg.get_SymbolId()) == SymbolID.SymbolSet_ControlMeasure
                        && SymbolUtilities.hasFDI(tg.get_SymbolId())) {
                    addFDI(tg, shapes);
                }

                switch (linetype) {
                    case TacticalLines.RANGE_FAN:
                    case TacticalLines.RANGE_FAN_SECTOR:
                    case TacticalLines.RADAR_SEARCH:
                        if (tg.get_FillColor() == null || tg.get_FillColor().getAlpha() < 2) {
                            break;
                        }
                        TGLight tg1 = clsUtilityCPOF.GetCircularRangeFanFillTG(tg);
                        tg1.set_Fillstyle(savefillStyle);
                        tg1.set_SymbolId(tg.get_SymbolId());
                        rangeFanFillShapes = clsRenderer2.GetLineArray(tg1, converter, isTextFlipped, clipBounds);

                        if (rangeFanFillShapes != null) {
                            shapes.addAll(0, rangeFanFillShapes);
                        }
                        break;
                    default:
                        break;
                }

                //undo any fillcolor for lines with fill
                clsUtilityCPOF.LinesWithSeparateFill(tg.get_LineType(), shapes);
                clsClipPolygon2.addAbatisFill(tg, shapes);

                //if this line is commented then the extra line in testbed goes away
                if (shapes != null && linesWithFillShapes != null && linesWithFillShapes.size() > 0) {
                    shapes.addAll(0, linesWithFillShapes);
                }

                if (shapes != null && shapes.size() > 0) {
                    Modifier2.DisplayModifiers2(tg, g2d, textSpecs, isTextFlipped, null);
                    Shape2ToShapeInfo(modifierShapeInfos, textSpecs);
                    mss.setModifierShapes(modifierShapeInfos);
                }
            } else //symbol was more than 180 degrees wide, use left and right symbols
            {
                shapes = shapesLeft;
                shapes.addAll(shapesRight);

                if (textSpecs != null) {
                    textSpecs.addAll(textSpecsLeft);
                    textSpecs.addAll(textSpecsRight);
                }
            }
            //post-clip the points if the tg could not be pre-clipped
            if (clsUtilityCPOF.canClipPoints(tg) == false && clipBounds != null) {
                shapes = clsUtilityCPOF.postClipShapes(tg, shapes, clipBounds);
            }

            Shape2ToShapeInfo(shapeInfos, shapes);
            clsUtility.addHatchFills(tg, shapeInfos);
            mss.setSymbolShapes(shapeInfos);
        } catch (Exception exc) {
            ErrorLogger.LogException(_className, "render",
                    new RendererException("Failed inside render", exc));

        }
    }

    public static int getCMLineType(int version, int entityCode) {
        // Check if line type is specific to a version
        if (version == SymbolID.Version_2525E){
            switch (entityCode) {
                // Added in 2525E
                case 110400:
                    return TacticalLines.GENERIC_LINE;
                case 120700:
                    return TacticalLines.GENERIC_AREA;
                case 141800:
                    return TacticalLines.HOL;
                case 141900:
                    return TacticalLines.BHL;
                case 310800:
                    return TacticalLines.CSA;
                case 330500:
                    return TacticalLines.ROUTE;
                case 330501:
                    return TacticalLines.ROUTE_ONEWAY;
                case 330502:
                    return TacticalLines.ROUTE_ALT;
                case 344100:
                    return TacticalLines.FPOL;
                case 344200:
                    return TacticalLines.RPOL;
                // Updated in 2525E
                case 120500:
                    return TacticalLines.BASE_CAMP;
                case 120600:
                    return TacticalLines.GUERILLA_BASE;
                case 151000:
                    return TacticalLines.FORT;
                case 260400:
                    return TacticalLines.BCL;
                case 310100:
                    return TacticalLines.DHA;
            }
        } else { // 2525Dchange 1 and older
            switch (entityCode) {
                // Updated in 2525E
                case 120500:
                    return TacticalLines.BASE_CAMP_REVD;
                case 120600:
                    return TacticalLines.GUERILLA_BASE_REVD;
                case 151000:
                    return TacticalLines.FORT_REVD;
                case 260400:
                    return TacticalLines.BCL_REVD;
                case 310100:
                    return TacticalLines.DHA_REVD;
                // Removed in 2525E
                case 150300:
                    return TacticalLines.ASSY;
                case 241601:
                    return TacticalLines.SENSOR;
                case 241602:
                    return TacticalLines.SENSOR_RECTANGULAR;
                case 241603:
                    return TacticalLines.SENSOR_CIRCULAR;
            }
        }
        // Line type isn't specific to a version or doesn't exist
        switch (entityCode) {
            case 200101:
                return TacticalLines.LAUNCH_AREA;
            case 200201:
                return TacticalLines.DEFENDED_AREA_CIRCULAR;
            case 200202:
                return TacticalLines.DEFENDED_AREA_RECTANGULAR;
            case 120100:
                return TacticalLines.AO;
            case 120200:
                return TacticalLines.NAI;
            case 120300:
                return TacticalLines.TAI;
            case 120400:
                return TacticalLines.AIRFIELD;
            case 151401:
                return TacticalLines.AIRAOA;
            case 151402:
                return TacticalLines.AAAAA;
            case 151403:
                return TacticalLines.MAIN;
            case 151404:
                return TacticalLines.SPT;
            case 110100:
                return TacticalLines.BOUNDARY;
            case 110200:
                return TacticalLines.LL;
            case 110300:
                return TacticalLines.EWL;
            case 140100:
                return TacticalLines.FLOT;
            case 140200:
                return TacticalLines.LC;
            case 140300:
                return TacticalLines.PL;
            case 140400:
                return TacticalLines.FEBA;
            case 140500:
                return TacticalLines.PDF;
            case 140601:
                return TacticalLines.DIRATKAIR;
            case 140602:
                return TacticalLines.DIRATKGND;
            case 140603:
                return TacticalLines.DIRATKSPT;
            case 140700:
                return TacticalLines.FCL;
            case 140800:
                return TacticalLines.IL;
            case 140900:
                return TacticalLines.LOA;
            case 141000:
                return TacticalLines.LOD;
            case 141100:
                return TacticalLines.LDLC;
            case 141200:
                return TacticalLines.PLD;
            case 150200:
                return TacticalLines.ASSY;
            case 150100:
                return TacticalLines.GENERAL;
            case 150501:
                return TacticalLines.JTAA;
            case 150502:
                return TacticalLines.SAA;
            case 150503:
                return TacticalLines.SGAA;
            case 150600:    //dz no eny
                return TacticalLines.DZ;
            case 150700:    //ez no eny
                return TacticalLines.EZ;
            case 150800:    //lz no eny
                return TacticalLines.LZ;
            case 150900:    //pz no eny
                return TacticalLines.PZ;
            case 151100:
                return TacticalLines.LAA;
            case 151200:
                return TacticalLines.BATTLE;
            case 151202:
                return TacticalLines.PNO;
            case 151204:
                return TacticalLines.CONTAIN;
            case 151205:
                return TacticalLines.RETAIN;
            case 151300:
                return TacticalLines.EA;
            case 151203:
                return TacticalLines.STRONG;
            case 151500:
                return TacticalLines.ASSAULT;
            case 151600:
                return TacticalLines.ATKPOS;
            case 151700:
                return TacticalLines.OBJ;
            case 151800:
                return TacticalLines.ENCIRCLE;
            case 151900:
                return TacticalLines.PEN;
            case 152000:
                return TacticalLines.ATKBYFIRE;
            case 152100:
                return TacticalLines.SPTBYFIRE;
            case 152200:
                return TacticalLines.SARA;
            case 141300:
                return TacticalLines.AIRHEAD;
            case 141400:
                return TacticalLines.BRDGHD;
            case 141500:
                return TacticalLines.HOLD;
            case 141600:
                return TacticalLines.RELEASE;
            case 141700:
                return TacticalLines.AMBUSH;
            case 170100:
                return TacticalLines.AC;
            case 170200:
                return TacticalLines.LLTR;
            case 170300:
                return TacticalLines.MRR;
            case 170400:
                return TacticalLines.SL;
            case 170500:
                return TacticalLines.SAAFR;
            case 170600:
                return TacticalLines.TC;
            case 170700:
                return TacticalLines.SC;
            case 170800:
                return TacticalLines.BDZ;
            case 170900:
                return TacticalLines.HIDACZ;
            case 171000:
                return TacticalLines.ROZ;
            case 171100:
                return TacticalLines.AARROZ;
            case 171200:
                return TacticalLines.UAROZ;
            case 171300:
                return TacticalLines.WEZ;
            case 171400:
                return TacticalLines.FEZ;
            case 171500:
                return TacticalLines.JEZ;
            case 171600:
                return TacticalLines.MEZ;
            case 171700:
                return TacticalLines.LOMEZ;
            case 171800:
                return TacticalLines.HIMEZ;
            case 171900:
                return TacticalLines.FAADZ;
            case 172000:
                return TacticalLines.WFZ;
            case 200401:
                return TacticalLines.SHIP_AOI_CIRCULAR;
            case 240804:
                return TacticalLines.RECTANGULAR_TARGET;
            case 220100:
                return TacticalLines.BEARING;
            case 220101:
                return TacticalLines.ELECTRO;
            case 220102:    //EW                //new label
                return TacticalLines.BEARING_EW;
            case 220103:
                return TacticalLines.ACOUSTIC;
            case 220104:
                return TacticalLines.ACOUSTIC_AMB;
            case 220105:
                return TacticalLines.TORPEDO;
            case 220106:
                return TacticalLines.OPTICAL;
            case 218400:
                return TacticalLines.NAVIGATION;
            case 220107:    //Jammer                //new label
                return TacticalLines.BEARING_J;
            case 220108:    //RDF                   //new label
                return TacticalLines.BEARING_RDF;
            case 240101:
                return TacticalLines.ACA;
            case 240102:
                return TacticalLines.ACA_RECTANGULAR;
            case 240103:
                return TacticalLines.ACA_CIRCULAR;

            case 240201:
                return TacticalLines.FFA;
            case 240202:
                return TacticalLines.FFA_RECTANGULAR;
            case 240203:
                return TacticalLines.FFA_CIRCULAR;

            case 240301:
                return TacticalLines.NFA;
            case 240302:
                return TacticalLines.NFA_RECTANGULAR;
            case 240303:
                return TacticalLines.NFA_CIRCULAR;

            case 240401:
                return TacticalLines.RFA;
            case 240402:
                return TacticalLines.RFA_RECTANGULAR;
            case 240403:
                return TacticalLines.RFA_CIRCULAR;
            case 240503:
                return TacticalLines.PAA;
            case 240501:
                return TacticalLines.PAA_RECTANGULAR;
            case 240502:
                return TacticalLines.PAA_CIRCULAR;
            case 260100:
                return TacticalLines.FSCL;
            case 300100:
                return TacticalLines.ICL;
            case 190100:
                return TacticalLines.IFF_OFF;
            case 190200:
                return TacticalLines.IFF_ON;
            case 260200:
                return TacticalLines.CFL;
            case 260300:
                return TacticalLines.NFL;
            case 260500:
                return TacticalLines.RFL;
            case 260600:
                return TacticalLines.MFP;
            case 240701:
                return TacticalLines.LINTGT;
            case 240702:
                return TacticalLines.LINTGTS;
            case 240703:
                return TacticalLines.FPF;
            case 240801:
                return TacticalLines.AT;
            case 240802:
                return TacticalLines.RECTANGULAR;
            case 240803:
                return TacticalLines.CIRCULAR;
            case 240805:
                return TacticalLines.SERIES;
            case 240806:
                return TacticalLines.SMOKE;
            case 240808:
                return TacticalLines.BOMB;
            case 241001:
                return TacticalLines.FSA;
            case 241002:
                return TacticalLines.FSA_RECTANGULAR;
            case 200402:
                return TacticalLines.SHIP_AOI_RECTANGULAR;
            case 200600:
                return TacticalLines.CUED_ACQUISITION;
            case 200700:
                return TacticalLines.RADAR_SEARCH;
            case 241003:
                return TacticalLines.FSA_CIRCULAR;
            case 200300:
                return TacticalLines.NOTACK;
            case 241101:
                return TacticalLines.ATI;
            case 241102:
                return TacticalLines.ATI_RECTANGULAR;
            case 241103:
                return TacticalLines.ATI_CIRCULAR;
            case 241201:
                return TacticalLines.CFFZ;
            case 241202:
                return TacticalLines.CFFZ_RECTANGULAR;
            case 241203:
                return TacticalLines.CFFZ_CIRCULAR;
            case 241301:
                return TacticalLines.CENSOR;
            case 241302:
                return TacticalLines.CENSOR_RECTANGULAR;
            case 241303:
                return TacticalLines.CENSOR_CIRCULAR;
            case 241401:
                return TacticalLines.CFZ;
            case 241402:
                return TacticalLines.CFZ_RECTANGULAR;
            case 241403:
                return TacticalLines.CFZ_CIRCULAR;
            case 241501:
                return TacticalLines.DA;
            case 241502:
                return TacticalLines.DA_RECTANGULAR;
            case 241503:
                return TacticalLines.DA_CIRCULAR;
            case 241701:
                return TacticalLines.TBA;
            case 241702:
                return TacticalLines.TBA_RECTANGULAR;
            case 241703:
                return TacticalLines.TBA_CIRCULAR;
            case 241801:
                return TacticalLines.TVAR;
            case 241802:
                return TacticalLines.TVAR_RECTANGULAR;
            case 241803:
                return TacticalLines.TVAR_CIRCULAR;
            case 241901:
                return TacticalLines.ZOR;
            case 241902:
                return TacticalLines.ZOR_RECTANGULAR;
            case 241903:
                return TacticalLines.ZOR_CIRCULAR;
            case 242000:
                return TacticalLines.TGMF;
            case 242100:
                return TacticalLines.RANGE_FAN;
            case 242200:
                return TacticalLines.RANGE_FAN_SECTOR;
            case 242301:
                return TacticalLines.KILLBOXBLUE;
            case 242302:
                return TacticalLines.KILLBOXBLUE_RECTANGULAR;
            case 242303:
                return TacticalLines.KILLBOXBLUE_CIRCULAR;
            case 242304:
                return TacticalLines.KILLBOXPURPLE;
            case 242305:
                return TacticalLines.KILLBOXPURPLE_RECTANGULAR;
            case 242306:
                return TacticalLines.KILLBOXPURPLE_CIRCULAR;
            case 270100:
            case 270200:
                return TacticalLines.ZONE;
            case 270300:
                return TacticalLines.OBSFAREA;
            case 270400:
                return TacticalLines.OBSAREA;
            case 270501:
                return TacticalLines.MNFLDBLK;
            case 270502:
                return TacticalLines.MNFLDDIS;
            case 270503:
                return TacticalLines.MNFLDFIX;
            case 270504:
                return TacticalLines.TURN;
            case 270601:
                return TacticalLines.EASY;
            case 270602:
                return TacticalLines.BYDIF;
            case 270603:
                return TacticalLines.BYIMP;
            case 271100:
                return TacticalLines.GAP;
            case 271201:
                return TacticalLines.PLANNED;
            case 271202:
                return TacticalLines.ESR1;
            case 271203:
                return TacticalLines.ESR2;
            case 271204:
                return TacticalLines.ROADBLK;
            case 280100:
                return TacticalLines.ABATIS;
            case 290100:
                return TacticalLines.LINE;
            case 290201:
                return TacticalLines.ATDITCH;
            case 290202:
                return TacticalLines.ATDITCHC;
            case 290203:
                return TacticalLines.ATDITCHM;
            case 290204:
                return TacticalLines.ATWALL;
            case 290301:
                return TacticalLines.UNSP;
            case 290302:
                return TacticalLines.SFENCE;
            case 290303:
                return TacticalLines.DFENCE;
            case 290304:
                return TacticalLines.DOUBLEA;
            case 290305:
                return TacticalLines.LWFENCE;
            case 290306:
                return TacticalLines.HWFENCE;
            case 290307:
                return TacticalLines.SINGLEC;
            case 290308:
                return TacticalLines.DOUBLEC;
            case 290309:
                return TacticalLines.TRIPLE;
            case 290600:
                return TacticalLines.MFLANE;
            case 270707:
                return TacticalLines.DEPICT;
            case 270800:
                return TacticalLines.MINED;
            case 270801:
                return TacticalLines.FENCED;
            case 290101:
                return TacticalLines.MINE_LINE;
            case 271000:
                return TacticalLines.UXO;
            case 271700:
                return TacticalLines.BIO;
            case 271800:
                return TacticalLines.CHEM;
            case 271900:
                return TacticalLines.NUC;
            case 272000:
                return TacticalLines.RAD;
            case 290400:
                return TacticalLines.CLUSTER;
            case 290500:
                return TacticalLines.TRIP;
            case 282003:
                return TacticalLines.OVERHEAD_WIRE;
            case 271300:
                return TacticalLines.ASLTXING;
            case 271500:
                return TacticalLines.FORDSITE;
            case 271600:
                return TacticalLines.FORDIF;
            case 290700:
                return TacticalLines.FERRY;
            case 290800:
                return TacticalLines.RAFT;
            case 290900:
                return TacticalLines.FORTL;
            case 291000:
                return TacticalLines.FOXHOLE;
            case 272100:
                return TacticalLines.MSDZ;
            case 272200:
                return TacticalLines.DRCL;

            case 310200:
                return TacticalLines.EPW;
            case 310300:
                return TacticalLines.FARP;
            case 310400:
                return TacticalLines.RHA;
            case 310500:
                return TacticalLines.RSA;
            case 310600:
                return TacticalLines.BSA;
            case 310700:
                return TacticalLines.DSA;
            case 330100:
                return TacticalLines.CONVOY;
            case 330200:
                return TacticalLines.HCONVOY;
            case 330300:
                return TacticalLines.MSR;
            case 330301:
                return TacticalLines.MSR_ONEWAY;
            case 330401:
                return TacticalLines.ASR_ONEWAY;
            case 330302:
                return TacticalLines.MSR_TWOWAY;
            case 330402:
                return TacticalLines.ASR_TWOWAY;
            case 330303:
                return TacticalLines.MSR_ALT;
            case 330403:
                return TacticalLines.ASR_ALT;

            case 330400:
                return TacticalLines.ASR;

            case 340100:
                return TacticalLines.BLOCK;
            case 340200:
                return TacticalLines.BREACH;
            case 340300:
                return TacticalLines.BYPASS;
            case 340400:
                return TacticalLines.CANALIZE;
            case 340500:
                return TacticalLines.CLEAR;
            case 340600:
                return TacticalLines.CATK;
            case 340700:
                return TacticalLines.CATKBYFIRE;

            case 340800:
                return TacticalLines.DELAY;
            case 341000:
                return TacticalLines.DISRUPT;
            case 341100:
                return TacticalLines.FIX;
            case 341200:
                return TacticalLines.FOLLA;
            case 341300:
                return TacticalLines.FOLSP;
            case 341500:
                return TacticalLines.ISOLATE;
            case 341700:
                return TacticalLines.OCCUPY;
            case 341800:
                return TacticalLines.PENETRATE;
            case 341900:
                return TacticalLines.RIP;
            case 342000:
                return TacticalLines.RETIRE;
            case 342100:
                return TacticalLines.SECURE;
            case 342201:
                return TacticalLines.COVER;
            case 342202:
                return TacticalLines.GUARD;
            case 342203:
                return TacticalLines.SCREEN;
            case 342300:
                return TacticalLines.SEIZE;
            case 342400:
                return TacticalLines.WITHDRAW;
            case 342500:
                return TacticalLines.WDRAWUP;
            case 342600:
                return TacticalLines.CORDONKNOCK;
            case 342700:
                return TacticalLines.CORDONSEARCH;
            case 272101:
                return TacticalLines.STRIKWARN;
            default:
                break;
        }
        return -1;
    }

    /**
     * Some symbol's points are reversed when moving from 2525C to 2525D. This method should be called at the start of each render.
     *
     * It's a simpler fix to reverse the points order at start than to reverse order when rendering.
     *
     * Note: Make sure to only call once to not reverse reversed points
     * @param tg
     */
    private static void reversePointsRevD(TGLight tg) {
        try {
            if (tg.get_SymbolId().length() < 20 || SymbolID.getSymbolSet(tg.get_SymbolId()) != 25) {
                return;
            }
            switch (tg.get_LineType()) {
                case TacticalLines.LC:
                case TacticalLines.UNSP:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                    if (tg.Pixels != null) {
                        Collections.reverse(tg.Pixels);
                    }
                    if (tg.LatLongs != null) {
                        Collections.reverse(tg.LatLongs);
                    }
                    break;
                case TacticalLines.CLUSTER:
                    if (SymbolID.getVersion(tg.get_SymbolId()) < SymbolID.Version_2525E) {
                        if (tg.Pixels != null) {
                            Collections.reverse(tg.Pixels);
                        }
                        if (tg.LatLongs != null) {
                            Collections.reverse(tg.LatLongs);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("clsRenderer", "reversePointsRevD",
                    new RendererException("Failed inside reversePointsRevD", exc));
        }
    }
}
