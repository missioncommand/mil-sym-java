/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.renderer.utilities;

import java.util.ArrayList;
import java.util.Map;

/**
 * Symbol attribute constants to be used as keys in the Map when calling {@link armyc2.c5isr.renderer.MilStdIconRenderer#RenderIcon(String, Map, Map)}
 * or {@link armyc2.c5isr.web.render.WebRenderer#RenderSymbol(String, String, String, String, String, String, double, String, Map, Map, int)}
 */
public class MilStdAttributes {
     
    /**
     * Line color of the symbol. hex value.
     */
    public static final String LineColor = "LINECOLOR";
    
    /**
     * Fill color of the symbol. hex value
     */
    public static final String FillColor = "FILLCOLOR";

    /**
     * Main color of internal icon.  Only relevant to framed symbols. hex value
     */
    public static final String IconColor = "ICONCOLOR";

    
    /**
     * size of the single point image
     */
    public static final String PixelSize = "PIXELSIZE";
    

    /**
     * defaults to true
     */
    public static final String KeepUnitRatio = "KEEPUNITRATIO";
    
    /**
     * transparency value of the symbol with values from 0 - 255.
     */
    public static final String Alpha = "ALPHA";
    
    /**
     * outline the symbol, true/false
     */
    public static final String OutlineSymbol = "OUTLINESYMBOL";
    
    /**
     * specify and outline color rather than letting renderer picking 
     * the best contrast color. hex value
     */
    public static final String OutlineColor = "OUTLINECOLOR";
    
    /*
     * specifies thickness of the symbol outline
     */
    //public static final String OutlineWidth = 9;
    
    /**
     * just draws the core symbol
     */
    public static final String DrawAsIcon = "DRAWASICON";

    /**
     * Specifies the line width of the multipoint symbology
     */
    public static final String LineWidth = "LINEWIDTH";

    /**
     * Specifies the color for text labels
     */
    public static final String TextColor = "TEXTCOLOR";

    /**
     * Specifies the color for the text background (color outline or fill)
     */
    public static final String TextBackgroundColor = "TEXTBACKGROUNDCOLOR";
    
    /**
     * If false, the renderer will create a bunch of little lines to create
     * the "dash" effect (expensive but necessary for KML).  
     * If true, it will be on the user to create the dash effect using the
     * DashArray from the Stroke object from the ShapeInfo object.
     */
    public static final String UseDashArray = "USEDASHARRAY";

    /**
     * The mode that altitude labels will be displayed in, the default value is AMSL.
     *
     * This value acts as a label, appending whatever string that is passed in to the end of the altitude units.
     * Currently only effective for multi-point graphics.
     */
    public static final String AltitudeMode = "ALTITUDEMODE";

    /**
     * At the moment, this refers to the optional range fan labels.
     */
    public static final String HideOptionalLabels = "HIDEOPTIONALLABELS";

    /**
     * For internal use
     */
    public static final String UsePatternFill = "USEPATTERNFILL";

    /**
     * For internal use
     */
    public static final String PatternFillType = "PATTERNFILLTYPE";

    /**
     * The conversion factor and the label that you want all distances to display in. The conversion factor
     * is converting from meters. The default unit is meters.<br><br>
     *
     * Must be in the form [conversionFactor],[label]. So for example converting to feet would be "3.28084,FT".
     * The helper class {@link DistanceUnit} can be used.
     */
    public static final String DistanceUnits = "DISTANCEUNITS";

    /**
     * The conversion factor and the label that you want all distances to display in.
     * Conventionally, the conversion factor is converting from meters by default,
     * but other values could be passed, like "1,KM" to use an unaltered value in kilometers.<br><br>
     *
     * Must be in the form [conversionFactor],[label]. So for example converting meters to feet would be "3.28084,FT".
     * The helper class {@link DistanceUnit} can be used.
     * Currently only effective for multi-point graphics.
     */
    public static final String AltitudeUnits = "ALTITUDEUNITS";

    /**
     * If the engagement/target amplifier bar is to be used to designate targets, non-targets, and
     * pruned or expired targets, a different coloring schema shall be used. Hostile tracks which
     * are deemed targets shall have a red bar (RGB: 255, 0, 0) to indicate target. For hostile
     * tracks deemed to be non-targets, white (RGB: 255, 255, 255) should be used to indicate non
     * target. Finally, for hostile tracks which have been pruned or have expired shall be colored
     * orange (RGB: 255, 120, 0).
     * This attribute expects a hex string for the color
     */
    public static final String EngagementBarColor = "ENGAGEMENTBARCOLOR";

    /**
     * No Longer relevant
     * @return {@link ArrayList}
     * @deprecated see {@link #GetAttributesList(String)}
     */
    public static ArrayList<String> GetModifierList()
    {
        ArrayList<String> list = new ArrayList<>();
        
        list.add(LineColor);
        list.add(FillColor);
        //list.add(IconColor);
        //list.add(FontSize);
        list.add(PixelSize);
        list.add(KeepUnitRatio);
        list.add(Alpha);
        list.add(OutlineSymbol);
        list.add(OutlineColor);
        //list.add(OutlineWidth);
        list.add(DrawAsIcon);
        list.add(HideOptionalLabels);
        list.add(DistanceUnits);
        list.add(AltitudeUnits);
        list.add(EngagementBarColor);
        
        return list;
    }

    public static ArrayList<String> GetAttributesList(String symbolID)
    {
        ArrayList<String> list = new ArrayList<>();

        list.add(LineColor);
        list.add(FillColor);
        //list.add(IconColor);
        list.add(PixelSize);

        if(SymbolUtilities.isMultiPoint(symbolID)==false) {
            list.add(KeepUnitRatio);
            list.add(OutlineSymbol);
            list.add(OutlineColor);
            list.add(DrawAsIcon);
            if(SymbolUtilities.hasModifier(symbolID,Modifiers.AO_ENGAGEMENT_BAR))
                list.add(EngagementBarColor);
        }
        else
        {
            list.add(LineWidth);
            list.add(HideOptionalLabels);
            list.add(DistanceUnits);
            list.add(AltitudeUnits);
        }
        list.add(Alpha);

        return list;
    }

    /**
     * @param attribute constant like MilStdAttributes.LineColor
     * @return attribute name based on attribute constants
     */
    public static String getAttributeName(String attribute) {
        switch (attribute) {
            case LineColor:
                return "Line Color";
            case FillColor:
                return "Fill Color";
            case PixelSize:
                return "Pixel Size";
            case KeepUnitRatio:
                return "Keep Unit Ratio";
            case Alpha:
                return "Alpha";
            case OutlineSymbol:
                return "Outline Symbol";
            case OutlineColor:
                return "Outline Color";
            case DrawAsIcon:
                return "Draw as Icon";
            case LineWidth:
                return "Line Width";
            case TextColor:
                return "Text Color";
            case TextBackgroundColor:
                return "Text Background Color";
            case UseDashArray:
                return "Use Dash Array";
            case AltitudeMode:
                return "Altitude Mode";
            case HideOptionalLabels:
                return "Hide Optional Labels";
            case UsePatternFill:
                return "Use Pattern Fill";
            case PatternFillType:
                return "Pattern Fill Type";
            case DistanceUnits:
                return "Distance Units";
            case AltitudeUnits:
                return "Altitude Units";
            default:
                return "unrecognized attribute";
        }
    }

    /**
     * Takes a string representation of an attribute and returns the appropriate int key value
     * @param attribute "LINECOLOR" will return MilStdAtttributes.LineColor
     * @return {@link Integer} value representing Attribute constant.
     */
    public static String getAttributeKey(String attribute) {
        switch (attribute.toUpperCase()) {
            case "LINECOLOR":
                return LineColor;
            case "FILLCOLOR":
                return FillColor;
            case "PIXELSIZE":
                return PixelSize;
            case "KEEPUNITRATIO":
                return KeepUnitRatio;
            case "ALPHA":
                return Alpha;
            case "OUTLINESYMBOL":
                return OutlineSymbol;
            case "OUTLINECOLOR":
                return OutlineColor;
            case "DRAWASICON":
                return DrawAsIcon;
            case "LINEWIDTH":
                return LineWidth;
            case "TEXTCOLOR":
                return TextColor;
            case "TEXTBACKGROUNDCOLOR":
                return TextBackgroundColor;
            case "USEDASHARRAY":
                return UseDashArray;
            case "ALTITUDEMODE":
                return AltitudeMode;
            case "HIDEOPTIONALLABELS":
                return HideOptionalLabels;
            case "USEPATTERNFILL":
                return UsePatternFill;
            case "PATTERNFILLTYPE":
                return PatternFillType;
            case "DISTANCEUNITS":
                return DistanceUnits;
            case "ALTITUDEUNITS":
                return AltitudeUnits;
            default:
                return null;
        }
    }
}
