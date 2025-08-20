package armyc2.c5isr.renderer.utilities;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RendererUtilities {

    private static final float OUTLINE_SCALING_FACTOR = 2.5f;
    public static String imgToBase64String(BufferedImage img)
    {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        try
        {
            ImageIO.write(img, "png", os);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
        }
        catch (final IOException ioe)
        {
            throw new UncheckedIOException(ioe);
        }
    }
	private static HashMap<Integer,Color> pastIdealOutlineColors = new HashMap<Integer, Color>();
	/**
     * 
     * @param color {String} color like "#FFFFFF"
     * @return {String}
     */
    public static Color getIdealOutlineColor(Color color){
        Color idealColor = Color.white;
        
        if(color != null && pastIdealOutlineColors.containsKey(color.getRGB()))
        {
            return pastIdealOutlineColors.get(color.getRGB());
        }//*/
        
        if(color != null)
        {
        	
        	int threshold = RendererSettings.getInstance().getTextBackgroundAutoColorThreshold();
			
            int r = color.getRed();
            int g = color.getGreen();
            int b = color.getBlue();
        
            float delta = ((r * 0.299f) + (g * 0.587f) + (b * 0.114f));
            
            if((255 - delta < threshold))
            {
                idealColor = Color.black;
            }
            else
            {
                idealColor = Color.white;
            }
        }
        
        if(color != null)
        	pastIdealOutlineColors.put(color.getRGB(),idealColor);
        
        return idealColor;
    }

    /**
     * Create a copy of the {@Color} object with the passed alpha value.
     * @param color {@Color} object used for RGB values
     * @param alpha {@float} value between 0 and 1
     * @return
     */
    public static Color setColorAlpha(Color color, float alpha) {
        if (color != null)
        {
            if(alpha >= 0 && alpha <= 1)
                return new Color(color.getRed(),color.getGreen(),color.getBlue(),(int)(alpha*255f));
            else
                return color;
        }
        else
            return null;
    }

    /**
     *
     * @param color
     * @return 8 character hex code, will have to prepend '#' or '0x' depending on your usage
     */
    private static String ColorToHex(Color color)
    {
        //String hex = String.format("#%02x%02x%02x%02x", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
        String hex = String.format("%02x%02x%02x%02x", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
        return hex;
    }

    /**
     *
     * @param color
     * @param withAlpha
     * @return
     */
    public static String colorToHexString(Color color, Boolean withAlpha)
    {
        String hex = "";
        if (color != null)
        {
            if(withAlpha)
            {
                hex = String.format("#%02X%02X%02X%02X", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            }
            else
            {
                hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
            }
        }
        return hex;
    }

    /**
     *
     * @param hexValue - String representing hex value (formatted "0xRRGGBB"
     * i.e. "0xFFFFFF") OR formatted "0xAARRGGBB" i.e. "0x00FFFFFF" for a color
     * with an alpha value I will also put up with "RRGGBB" and "AARRGGBB"
     * without the starting "0x" or "#"
     * @return
     */
    public static Color getColorFromHexString(String hexValue)
    {

        try
        {
            if(hexValue==null || hexValue.isEmpty())
                return null;
            String hexOriginal = hexValue;

            String hexAlphabet = "0123456789ABCDEF";

            if (hexValue.charAt(0) == '#')
            {
                hexValue = hexValue.substring(1);
            }
            if (hexValue.substring(0, 2).equals("0x") || hexValue.substring(0, 2).equals("0X"))
            {
                hexValue = hexValue.substring(2);
            }

            hexValue = hexValue.toUpperCase();

            int count = hexValue.length();
            int[] value = null;
            int k = 0;
            int int1 = 0;
            int int2 = 0;

            if (count == 8 || count == 6)
            {
                value = new int[(count / 2)];
                for (int i = 0; i < count; i += 2)
                {
                    int1 = hexAlphabet.indexOf(hexValue.charAt(i));
                    int2 = hexAlphabet.indexOf(hexValue.charAt(i + 1));

                    if(int1 == -1 || int2 == -1)
                    {
                        ErrorLogger.LogMessage("SymbolUtilities", "getColorFromHexString", "Bad hex value: " + hexOriginal, Level.WARNING);
                        return null;
                    }

                    value[k] = (int1 * 16) + int2;
                    k++;
                }

                if (count == 8)
                {
                    return new Color(value[1], value[2], value[3], value[0]);
                }
                else
                {
                    return new Color(value[0], value[1], value[2]);
                }
            }
            else
            {
                ErrorLogger.LogMessage("RendererUtilities", "getColorFromHexString", "Bad hex value: " + hexOriginal, Level.WARNING);
            }
            return null;
        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("RendererUtilities", "getColorFromHexString", exc);
            return null;
        }
    }


    public static int getRecommendedTextOutlineWidth()
    {
        return getRecommendedTextOutlineWidth(RendererSettings.getInstance().getDeviceDPI(),RendererSettings.getInstance().getTextBackgroundMethod());
    }

    public static int getRecommendedTextOutlineWidth(int textBackgroundMethod)
    {
        return getRecommendedTextOutlineWidth(RendererSettings.getInstance().getDeviceDPI(),textBackgroundMethod);
    }

    /**
     *
     * @param dpi
     * @param textBackgroundMethod like RendererSettings.TextBackgroundMethod_OUTLINE or -1 for SVG
     * @return
     */
    public static int getRecommendedTextOutlineWidth(int dpi, int textBackgroundMethod)
    {
        int outlineWidth = 0;

        if(textBackgroundMethod == RendererSettings.TextBackgroundMethod_OUTLINE)
            outlineWidth = (int)Math.floor(Math.max((dpi/24.0),4));
        else if(textBackgroundMethod == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
            outlineWidth = (int)Math.floor(Math.max(((dpi/48.0) - 1),1));
        else if(textBackgroundMethod == -1)//SVG
            outlineWidth = (int)Math.floor(Math.max((dpi/48.0),2));

        return outlineWidth;
    }

    /**
     * For Renderer Use Only
     * Assumes a fresh SVG String from the SVGLookup with its default values
     * @param symbolID
     * @param svg
     * @param strokeColor hex value like "#FF0000";
     * @param fillColor hex value like "#FF0000";
     * @return SVG String
     */
    public static String setSVGFrameColors(String symbolID, String svg, Color strokeColor, Color fillColor)
    {
        String returnSVG = null;
        String hexStrokeColor = null;
        String hexFillColor = null;
        float strokeAlpha = 1;
        float fillAlpha = 1;
        String strokeOpacity = "";
        String fillOpacity = "";

        int ss = SymbolID.getSymbolSet(symbolID);
        int ver = SymbolID.getVersion(symbolID);
        int affiliation = SymbolID.getAffiliation(symbolID);
        String defaultFillColor = null;
        returnSVG = svg;

        if(strokeColor != null)
        {
            if(strokeColor.getAlpha() != 255)
            {
                strokeAlpha = strokeColor.getAlpha() / 255.0f;
                strokeOpacity =  " stroke-opacity=\"" + String.valueOf(strokeAlpha) + "\"";
                fillOpacity =  " fill-opacity=\"" + String.valueOf(strokeAlpha) + "\"";
            }

            hexStrokeColor = colorToHexString(strokeColor,false);
            returnSVG = returnSVG.replaceAll("stroke=\"#000000\"", "stroke=\"" + hexStrokeColor + "\"" + strokeOpacity);
            returnSVG = returnSVG.replaceAll("fill=\"#000000\"", "fill=\"" + hexStrokeColor + "\"" + fillOpacity);

            if(ss == SymbolID.SymbolSet_LandInstallation ||
                    ss == SymbolID.SymbolSet_Space ||
                    ss == SymbolID.SymbolSet_CyberSpace ||
                    ss == SymbolID.SymbolSet_Activities)
            {//add group fill so the extra shapes in these frames have the new frame color
                String svgStart =  "<g id=\"" + SVGLookup.getFrameID(symbolID) + "\">";
                String svgStartReplace = svgStart.substring(0,svgStart.length()-1) + " fill=\"" + hexStrokeColor + "\"" + fillOpacity + ">";
                returnSVG = returnSVG.replace(svgStart,svgStartReplace);
            }

            if((SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_LandInstallation && SymbolID.getFrameShape(symbolID)=='0') ||
                    SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_LandInstallation)
            {
                int i1 = returnSVG.indexOf("<rect") + 5;
                if(ver >= SymbolID.Version_2525E && affiliation == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
                    i1 = returnSVG.indexOf("<rect",i1) + 5;
                //make sure installation indicator matches line color
                returnSVG = returnSVG.substring(0,i1) + " fill=\"" + hexStrokeColor + "\"" + returnSVG.substring(i1);
            }
        }
        else if((SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_LandInstallation && SymbolID.getFrameShape(symbolID)=='0') ||
                SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_LandInstallation)
        {
            int i1 = returnSVG.indexOf("<rect") + 5;
            if(ver >= SymbolID.Version_2525E && affiliation == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
                i1 = returnSVG.indexOf("<rect",i1) + 5;
            //No line color change so make sure installation indicator stays black
            returnSVG = returnSVG.substring(0,i1) + " fill=\"#000000\"" + returnSVG.substring(i1);
        }

        if(fillColor != null)
        {
            if(fillColor.getAlpha() != 255)
            {
                fillAlpha = fillColor.getAlpha() / 255.0f;
                fillOpacity =  " fill-opacity=\"" + String.valueOf(fillAlpha) + "\"";
            }

            hexFillColor = colorToHexString(fillColor,false);
            switch(affiliation)
            {
                case SymbolID.StandardIdentity_Affiliation_Friend:
                case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                    defaultFillColor = "fill=\"#80E0FF\"";//friendly frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                    defaultFillColor = "fill=\"#FF8080\"";//hostile frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                    if(SymbolID.getVersion(symbolID) >= SymbolID.Version_2525E)
                        defaultFillColor = "fill=\"#FFE599\"";//suspect frame fill
                    else
                        defaultFillColor = "fill=\"#FF8080\"";//hostile frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Unknown:
                case SymbolID.StandardIdentity_Affiliation_Pending:
                    defaultFillColor = "fill=\"#FFFF80\"";//unknown frame fill
                    break;
                case SymbolID.StandardIdentity_Affiliation_Neutral:
                    defaultFillColor = "fill=\"#AAFFAA\"";//neutral frame fill
                    break;
                default:
                    defaultFillColor = "fill=\"#80E0FF\"";//friendly frame fill
                    break;
            }

            int fillIndex = returnSVG.lastIndexOf(defaultFillColor);
            if(fillIndex != -1)
                returnSVG = returnSVG.substring(0,fillIndex) + "fill=\"" + hexFillColor + "\"" + fillOpacity + returnSVG.substring(fillIndex + defaultFillColor.length());

            //returnSVG = returnSVG.replaceFirst(defaultFillColor, "fill=\"" + hexFillColor + "\"" + fillOpacity);
        }


        if(returnSVG != null)
            return returnSVG;
        else
            return svg;
    }

    /**
     * For Renderer Use Only
     * Changes colors for single point control measures
     * @param symbolID
     * @param svg
     * @param strokeColor hex value like "#FF0000";
     * @param fillColor hex value like "#FF0000";
     * @param isOutline true if this represents a thicker outline to render first beneath the normal symbol (the function must be called twice)
     * @return SVG String
     *
     */
    public static String setSVGSPCMColors(String symbolID, String svg, Color strokeColor, Color fillColor, boolean isOutline)
    {
        String returnSVG = svg;
        String hexStrokeColor = null;
        String hexFillColor = null;
        float strokeAlpha = 1;
        float fillAlpha = 1;
        String strokeOpacity = "";
        String fillOpacity = "";
        String strokeCapSquare = " stroke-linecap=\"square\"";
        String strokeCapButt = " stroke-linecap=\"butt\"";
        String strokeCapRound = " stroke-linecap=\"round\"";

        int affiliation = SymbolID.getAffiliation(symbolID);
        String defaultFillColor = null;
        if(strokeColor != null)
        {
            if(strokeColor.getAlpha() != 255)
            {
                strokeAlpha = strokeColor.getAlpha() / 255.0f;
                strokeOpacity =  " stroke-opacity=\"" + strokeAlpha + "\"";
                fillOpacity =  " fill-opacity=\"" + strokeAlpha + "\"";
            }

            hexStrokeColor = colorToHexString(strokeColor,false);
            String defaultStrokeColor = "#000000";
            if(symbolID.length()==5)
            {
                int mod = Integer.valueOf(symbolID.substring(2,4));
                if(mod >= 13)
                    defaultStrokeColor = "#00A651";

            }
            //key terrain
            if(symbolID.length() >= 20 &&
                    SymbolUtilities.getBasicSymbolID(symbolID).equals("25132100") &&
                    SymbolID.getVersion(symbolID) >= SymbolID.Version_2525E)
            {
                defaultStrokeColor = "#800080";
            }
            returnSVG = returnSVG.replaceAll("stroke=\"" + defaultStrokeColor + "\"", "stroke=\"" + hexStrokeColor + "\"" + strokeOpacity);
            returnSVG = returnSVG.replaceAll("fill=\"" + defaultStrokeColor + "\"", "fill=\"" + hexStrokeColor + "\"" + fillOpacity);
        }
        else
        {
            strokeColor = Color.BLACK;
        }

        if (isOutline) {
            // Capture and scale stroke-widths to create outlines. Note that some stroke-widths are not integral numbers.
            Pattern pattern = Pattern.compile("(stroke-width=\")(\\d+\\.?\\d*)\"");
            Matcher m = pattern.matcher(svg);
            TreeSet<String> strokeWidthStrings = new TreeSet<>();
            while (m.find()) {
                strokeWidthStrings.add(m.group(0));
            }
            // replace stroke width values in SVG from greatest to least to avoid unintended replacements
            // TODO This might not actually sort strings from greatest to least stroke-width values because they're alphabetical
            for (String target : strokeWidthStrings.descendingSet()) {
                Pattern numPattern = Pattern.compile("\\d+\\.?\\d*");
                Matcher numMatcher = numPattern.matcher(target);
                numMatcher.find();
                float f = Float.parseFloat(numMatcher.group(0));
                String replacement = "stroke-width=\"" + (f * OUTLINE_SCALING_FACTOR) + "\"";
                returnSVG = returnSVG.replace(target, replacement);
            }

            // add stroke-width and stroke (color) to all groups
            pattern = Pattern.compile("(<g)");
            m = pattern.matcher(svg);
            TreeSet<String> groupStrings = new TreeSet<>();
            while (m.find()) {
                groupStrings.add(m.group(0));
            }
            for (String target : groupStrings) {
                String replacement = target + strokeCapSquare + " stroke-width=\"" + (2.5f * OUTLINE_SCALING_FACTOR) + "\" stroke=\"#" + ColorToHex(strokeColor).substring(2) + "\" ";
                returnSVG = returnSVG.replace(target, replacement);
            }

        }
        else
        {
            /* //this code just returned the entire svg string back.  Maybe because there's no line breaks.
            Pattern pattern = Pattern.compile("(font-size=\"\\d+\\.?\\d*)\"");
            Matcher m = pattern.matcher(svg);
            TreeSet<String> fontStrings = new TreeSet<>();
            while (m.find()) {
                fontStrings.add(m.group(0));
            }
            for (String target : fontStrings) {
                String replacement = target + " fill=\"#" + ColorToHex(strokeColor).substring(2) + "\" ";
                returnSVG = returnSVG.replace(target, replacement);
            }
            //*/
            String replacement = " fill=\"#" + ColorToHex(strokeColor).substring(2) + "\" ";
            returnSVG = returnSVG.replace("fill=\"#000000\"",replacement);//only replace black fills, leave white fills alone.

            //In case there are lines that don't have stroke defined, apply stroke color to the top level group.
            String topGroupTag = "<g id=\"" + SymbolUtilities.getBasicSymbolID(symbolID) + "\">";//<g id="25212902">
            String newGroupTag = "<g id=\"" + SymbolUtilities.getBasicSymbolID(symbolID) + "\" stroke=\"" + hexStrokeColor + "\"" + strokeOpacity + " " + replacement + ">";
            returnSVG = returnSVG.replace(topGroupTag,newGroupTag);

        }

        if(fillColor != null)
        {
            if(fillColor.getAlpha() != 255)
            {
                fillAlpha = fillColor.getAlpha() / 255.0f;
                fillOpacity =  " fill-opacity=\"" + fillAlpha + "\"";
            }

            hexFillColor = colorToHexString(fillColor,false);
            defaultFillColor = "fill=\"#000000\"";

            returnSVG = returnSVG.replaceAll(defaultFillColor, "fill=\"" + hexFillColor + "\"" + fillOpacity);
        }

        return returnSVG;
    }

    public static float findWidestStrokeWidth(String svg) {
        Pattern pattern = Pattern.compile("(stroke-width=\")(\\d+\\.?\\d*)\"");
        Matcher m = pattern.matcher(svg);
        TreeSet<Float> strokeWidths = new TreeSet<>();
        while (m.find()) {
            // Log.d("found stroke width", m.group(0));
            strokeWidths.add(Float.valueOf(m.group(2)));
        }

        float largest = 4.0f;
        if (!strokeWidths.isEmpty()) {
            largest = strokeWidths.descendingSet().first();
        }
        return largest * OUTLINE_SCALING_FACTOR;
    }

    public static int getDistanceBetweenPoints(Point2D pt1, Point2D pt2)
    {
        int distance = (int)(Math.sqrt(Math.pow((pt2.getX() - pt1.getX()) ,2) + Math.pow((pt2.getY() - pt1.getY()) ,2)));
        return distance;
    }

    // Overloaded method to return non-outline symbols as normal.
    public static String setSVGSPCMColors(String symbolID, String svg, Color strokeColor, Color fillColor) {
        return setSVGSPCMColors(symbolID, svg, strokeColor, fillColor, false);
    }

    public static SVGInfo scaleIcon(String symbolID, SVGInfo icon)
    {
        SVGInfo retVal= icon;
        //safe square inside octagon:  <rect x="220" y="310" width="170" height="170"/>
        double maxSize = 170;
        Rectangle2D bbox = null;
        if(icon != null)
            bbox =  icon.getBbox();
        double length = 0;
        if(bbox != null)
            length = Math.max(bbox.getWidth(),bbox.getHeight());
        if(length < 100 && length > 0 &&
                SymbolID.getCommonModifier1(symbolID)==0 &&
                SymbolID.getCommonModifier2(symbolID)==0 &&
                SymbolID.getModifier1(symbolID)==0 &&
                SymbolID.getModifier2(symbolID)==0)//if largest side smaller than 100 and there are no section mods, make it bigger
        {
            double ratio = maxSize / length;
            double transx = ((bbox.getX() + (bbox.getWidth()/2)) * ratio) - (bbox.getX() + (bbox.getWidth()/2));
            double transy = ((bbox.getY() + (bbox.getHeight()/2)) * ratio) - (bbox.getY() + (bbox.getHeight()/2));
            String transform = " transform=\"translate(-" + transx + ",-" + transy + ") scale(" + ratio + " " + ratio + ")\">";
            String svg = icon.getSVG();
            svg = svg.replaceFirst(">",transform);
            Rectangle2D newBbox = new Rectangle2D.Double(bbox.getX() - transx,bbox.getY() - transy,bbox.getWidth() * ratio, bbox.getHeight() * ratio);
            retVal = new SVGInfo(icon.getID(),newBbox,svg);
        }
        return retVal;
    }

}
