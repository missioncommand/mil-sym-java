package armyc2.c5isr.JavaTacticalRenderer;

import java.awt.*;
import java.util.ArrayList;

import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

/**
 * A class to encapsulate the tactical graphic object. Many of the properties
 * correspond to a client MilStdSymbol object.
 *
 * 
 */
public class TGLight {

    public ArrayList<POINT2> LatLongs;
    private static final String _className = "TGLight";

    public ArrayList<POINT2> get_LatLongs() {
        return LatLongs;
    }

    public void set_LatLongs(ArrayList<POINT2> value) {
        LatLongs = value;
    }

    public ArrayList<POINT2> Pixels;

    public ArrayList<POINT2> get_Pixels() {
        return Pixels;
    }

    public void set_Pixels(ArrayList<POINT2> value) {
        Pixels = value;
    }

    public ArrayList<Modifier2> modifiers;

    public ArrayList<Modifier2> get_Modifiers() {
        return modifiers;
    }

    public void set_Modifiers(ArrayList<Modifier2> value) {
        modifiers = value;
    }

    TexturePaint tp = null;

    public void set_TexturePaint(TexturePaint value) {
        tp = value;
    }

    public TexturePaint get_TexturePaint() {
        return tp;
    }

    boolean maskOff;

    public TGLight() {
    }

    private Font font;

    public void set_Font(Font value) {
        font = value;
    }

    public Font get_Font() {
        return font;
    }

    private int iconSize = 50;

    /**
     * Set the icon size for areas that have a symbol like LAA or Biological Contaminated Area
     * @param pixelSize
     */
    public void setIconSize(int pixelSize){iconSize = pixelSize;}

    public int getIconSize(){return iconSize;}

    private boolean keepUnitRatio = true;

    public void set_KeepUnitRatio(boolean value) {
        keepUnitRatio = value;
    }

    public boolean get_KeepUnitRation() {
        return keepUnitRatio;
    }

    private int lineType;

    public void set_LineType(int value) {
        lineType = value;
    }

    public int get_LineType() {
        return lineType;
    }

    private int lineStyle;

    public void set_LineStyle(int value) {
        lineStyle = value;
    }

    public int get_LineStyle() {
        return lineStyle;
    }

    private Color lineColor;

    public Color get_LineColor() {
        return lineColor;
    }

    public void set_LineColor(Color value) {
        lineColor = value;
    }

    private int fillStyle;

    public int get_FillStyle() {
        return fillStyle;
    }

    public void set_Fillstyle(int value) {
        fillStyle = value;
    }

    private Color fillColor;

    public Color get_FillColor() {
        return fillColor;
    }

    public void set_FillColor(Color value) {
        fillColor = value;
    }

    private Color fontBackColor = Color.WHITE;

    //private Color fontBackColor=RendererSettings.getInstance().getLabelBackgroundColor();
    public Color get_FontBackColor() {
        return fontBackColor;
    }

    public void set_FontBackColor(Color value) {
        fontBackColor = value;
    }

    private Color textColor;

    public Color get_TextColor() {
        return textColor;
    }

    public void set_TextColor(Color value) {
        textColor = value;
    }

    private int lineThickness;

    public int get_LineThickness() {
        return lineThickness;
    }

    public void set_LineThickness(int value) {
        lineThickness = value;
    }

    private String t = "";

    public String get_Name() {
        if (visibleModifiers) {
            return t;
        } else {
            return "";
        }
    }

    private String client = "";

    public String get_Client() {
        return client;
    }

    public void set_client(String value) {
        client = value;
    }

    public void set_Name(String value) {
        t = value;
    }

    private String t1 = "";

    public String get_T1() {
        if (visibleModifiers) {
            return t1;
        } else {
            return "";
        }
    }

    public void set_T1(String value) {
        t1 = value;
    }

    private String am = "";

    public String get_AM() {
        if (visibleModifiers) {
            return am;
        } else {
            return "";
        }
    }

    public void set_AM(String value) {
        am = value;
    }

    private String am1 = "";

    public String get_AM1() {
        if (visibleModifiers) {
            return am1;
        } else {
            return "";
        }
    }

    public void set_AM1(String value) {
        am1 = value;
    }

    private String an = "";

    public String get_AN() {
        if (visibleModifiers) {
            return an;
        } else {
            return "";
        }
    }

    public void set_AN(String value) {
        an = value;
    }

    private String v = "";

    public String get_V() {
        if (visibleModifiers) {
            return v;
        } else {
            return "";
        }
    }

    public void set_V(String value) {
        v = value;
    }


    private String ap = "";

    public String get_AP() {
        if (visibleModifiers) {
            return ap;
        } else {
            return "";
        }
    }

    public void set_AP(String value) {
        ap = value;
    }

    private String as = "";

    public String get_AS() {
        if (visibleModifiers) {
            return as;
        } else {
            return "";
        }
    }

    public void set_AS(String value) {
        as = value;
    }

    private String x = "";

    public String get_X() {
        return x;
    }

    public void set_X(String value) {
        x = value;
    }

    private String x1 = "";

    public String get_X1() {
        return x1;
    }

    public void set_X1(String value) {
        x1 = value;
    }

    private String h = "";

    public String get_H() {
        if (visibleModifiers || lineType == TacticalLines.RECTANGULAR) {
            return h;
        } else {
            return "";
        }
    }

    public void set_H(String value) {
        h = value;
    }

    public String get_Location() {
        if (visibleModifiers) {
            if (!y.isEmpty()) {
                return y;
            } else {
                return h;
            }
        } else {
            return "";
        }
    }

    public void set_Location(String value) {
        y = value;
    }

    @Deprecated
    private String h1 = "";

    /**
     * @deprecated
     */
    public String get_H1() {
        if (visibleModifiers) {
            return h1;
        } else {
            return "";
        }
    }

    /**
     * @deprecated
     */
    public void set_H1(String value) {
        h1 = value;
    }

    //location
    private String y = "";

    private String n = "ENY";

    public String get_N() {
        return n;
    }

    public void set_N(String value) {
        n = value;
    }

    @Deprecated
    private String h2 = "";

    /**
     * @deprecated
     */
    public String get_H2() {
        if (visibleModifiers || lineType == TacticalLines.RECTANGULAR) {
            return h2;
        } else {
            return "";
        }
    }

    /**
     * @deprecated
     */
    public void set_H2(String value) {
        h2 = value;
    }

    /**
     * Only used for range fan
     * left azimuth,right azimuth,min radius,max radius
     */
    private String leftRightMinMax = "";

    public String get_LRMM() {
        return leftRightMinMax;
    }

    public void set_LRMM(String value) {
        leftRightMinMax = value;
    }

    private String w = "";

    public String get_DTG() {
        if (visibleModifiers) {
            return w;
        } else {
            return "";
        }
    }

    public void set_DTG(String value) {
        w = value;
    }

    private String w1 = "";

    public String get_DTG1() {
        if (visibleModifiers) {
            return w1;
        } else {
            return "";
        }
    }

    public void set_DTG1(String value) {
        w1 = value;
    }


    private String standardIdentity = "00";

    public String get_StandardIdentity(){
        return standardIdentity;
    }

    /**
     * @return true if standard identity is suspect/joker or hostile/faker
     */
    public boolean isHostile() {
        if (standardIdentity != null) {
            return standardIdentity.charAt(1) == '5' || standardIdentity.charAt(1) == '6';
        } else {
            return false;
        }
    }

    private String echelonSymbol = "";

    protected String get_EchelonSymbol() {
        return echelonSymbol;
    }

    public void set_EchelonSymbol(String value) {
        echelonSymbol = value;
    }

    private String symbolId = "00000000";

    public String get_SymbolId() {
        return symbolId;
    }

    // "P" for present or "A" for anticipated
    private String status = "P";

    public String get_Status() {
        return status;
    }

    public void set_Status(String value) {
        status = value;
    }

    /**
     * Sets tactical graphic properties based on the 20-30 digit Mil-Std-2525 symbol code
     *
     * @param value
     */
    public void set_SymbolId(String value) {
        try {
            symbolId = value;
            int symbolSet = SymbolID.getSymbolSet(symbolId);
            if (symbolSet == 25) {
                standardIdentity = SymbolID.getStandardIdentity(symbolId) + "";
                if(standardIdentity.length()==1)
                    standardIdentity = "0" + standardIdentity;

                status = "P"; // default to present
                if (SymbolID.getStatus(symbolId) == 1) {
                    // Planned/Anticipated/Suspect
                    status = "A";
                    lineStyle = 1; // dashed
                }

                int amplifier = SymbolID.getAmplifierDescriptor(symbolId);
                echelonSymbol = SymbolUtilities.getEchelonText(amplifier);
                if (echelonSymbol == null) {
                    echelonSymbol = "";
                }
            }
        } catch (Exception exc) {
            //clsUtility.WriteFile("Error in TGLight.set_SymbolId");
            ErrorLogger.LogException(_className, "set_SymbolId",
                    new RendererException("Failed inside set_SymbolId", exc));
        }
    }

    @Deprecated // value is always true
    private boolean visibleModifiers = true;

    /**
     * @deprecated
     */
    public void set_VisibleModifiers(boolean value) {
        visibleModifiers = value;
    }

    /**
     * @deprecated
     */
    protected boolean get_VisibleModifiers() {
        return visibleModifiers;
    }

    @Deprecated // value is never used
    private boolean visibleLabels;

    /**
     * @deprecated
     */
    public void set_VisibleLabels(boolean value) {
        visibleLabels = value;
    }

    /**
     * @deprecated
     */
    protected boolean get_VisibleLabels() {
        return visibleLabels;
    }

    boolean _useLineInterpolation = false;

    public boolean get_UseLineInterpolation() {
        return _useLineInterpolation;
    }

    public void set_UseLineInterpolation(boolean value) {
        _useLineInterpolation = value;
    }

    boolean _useDashArray = false;

    public boolean get_UseDashArray() {
        return _useDashArray;
    }

    public void set_UseDashArray(boolean value) {
        _useDashArray = value;
    }

    boolean _useHatchFill = false;

    public boolean get_UseHatchFill() {
        return _useHatchFill;
    }

    public void set_UseHatchFill(boolean value) {
        _useHatchFill = value;
    }

//    boolean _usePatternFill = false;    
//    public boolean get_UsePatternFill() {
//        return _usePatternFill;
//    }
//
//    public void set_UsePatternFill(boolean value) {
//        _usePatternFill = value;
//    }

    private boolean _wasClipped = false;

    public void set_WasClipped(boolean value) {
        _wasClipped = value;
    }

    public boolean get_WasClipped() {
        return _wasClipped;
    }

    //boolean determines whether to add the range and azimuth modifiers for range fans
    private boolean _HideOptionalLabels = false;

    public boolean get_HideOptionalLabels() {
        return _HideOptionalLabels;
    }

    public void set_HideOptionalLabels(boolean value) {
        _HideOptionalLabels = value;
    }

    private int lineCap = BasicStroke.CAP_SQUARE;

    public void set_lineCap(int cap) {
        lineCap = cap;
    }

    public int get_lineCap() {
        return lineCap;
    }
}
