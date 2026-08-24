package armyc2.c5isr.JavaLineArray;

import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.RectUtilities;
import armyc2.c5isr.renderer.utilities.RendererSettings;
import armyc2.c5isr.renderer.utilities.RendererUtilities;
import armyc2.c5isr.renderer.utilities.SymbolID;
import armyc2.c5isr.renderer.utilities.SymbolUtilities;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Map;

public class LinePattern {

    BufferedImage pattern = null;
    String svg = null;
    double vOffset = 0;

    LinePattern(BufferedImage linePattern, String svgPattern, double verticalOffset)
    {
        pattern = linePattern;
        svg = svgPattern;
        vOffset = verticalOffset;
    }

    public BufferedImage getLinePatternImage()
    {
        return pattern;
    }

    public String getLinePatternSVG()
    {
        return svg;
    }

    public double getLinePatternVerticalOffset(){return vOffset;}

    public static boolean supportsLinePattern(String symbolCode) {
        int ec = SymbolID.getEntityCode(symbolCode);
        if (SymbolID.getSymbolSet(symbolCode) == SymbolID.SymbolSet_ControlMeasure)
        {
            switch (ec)
            {
                // FLOT
                case 140100:
                // Line of Contact
                case 140200:
                // Strong Point
                case 151203:
                // Encirclement
                case 151800:
                // Obstacle Belt || Obstacle Zone || Obstacle Line
                case 270100:
                case 270200:
                case 290100:
                // Obstacle Free Zone OR Obstacle Restricted Zone OR Antitank Wall
                case 270300:
                case 270400:
                case 290204:
                // Ditch - Under Construction || Ditch - Completed
                case 290201:
                case 290202:
                // Ditch Reinforced, with Antitank Mines
                case 290203:
                // Unspecified (Obstacle)
                case 290301:
                // Single Fence
                case 290302:
                // Double Fence
                case 290303:
                // Double Apron Fence
                case 290304:
                // Low Wire Fence
                case 290305:
                // High Wire Fence
                case 290306:
                // Single Concertina
                case 290307:
                // Double Strand Concertina
                case 290308:
                // Triple Strand Concertina
                case 290309:
                // Fortified Line OR Fortified Area
                case 290900:
                case 151000:
                    return true;
                default:
                    return false;
            }
        }
        else
            return false;
    }

    public static LinePattern getLinePattern(String symbolCode, Map<String,String> attributes)
    {
        Color lineColor = SymbolUtilities.getLineColorOfAffiliation(symbolCode);
        Color fillColor = null;
        int lineWidth = 3;
        if(attributes != null)
        {
            if(attributes.containsKey(MilStdAttributes.LineColor))
                lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
            if(attributes.containsKey(MilStdAttributes.FillColor))
                fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
            if(attributes.containsKey(MilStdAttributes.LineWidth))
                lineWidth = Integer.parseInt(attributes.get(MilStdAttributes.LineWidth));
        }
        return getLinePattern(symbolCode, lineColor, fillColor, lineWidth);
    }

    public static LinePattern getLinePattern(String symbolCode, Color lineColor, Color fillColor, float lineWidth)
    {
        LinePattern lp = null;
        StringBuilder svgPath = null;
        StringBuilder svgEllipse = null;
        StringBuilder svgText = null;
        Rectangle2D svgBounds = null;
        double vOffset = 0;


        if(symbolCode != null && symbolCode.length() >= 20)
        {
            int symbolSet = SymbolID.getSymbolSet(symbolCode);
            int ec = SymbolID.getEntityCode(symbolCode);
            int dpi = RendererSettings.getInstance().getDeviceDPI();

            float offset = lineWidth/2f;// + 0.5f;
            float multiplier = dpi > 96 ? dpi/96f : 1;


            if(ec == 140100)//FLOT
            {
                fillColor=null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = 0;
                double eRadH = ((10 + lineWidth) * multiplier);
                double eRadW = eRadH;// * 0.5f;
                double patternHeight = eRadH + lineWidth;
                double patternWidth = lwOffset + (eRadW * 2) + lineWidth + lwOffset;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'uuuu'
                y += eRadH + lineWidth;
                svgPath.append("M ").append(lwOffset).append(" ").append(y).append(" ");
                svgPath.append("a ").append(eRadW+lwOffset).append(",").append(eRadW+lwOffset).append(" 0 0,1 ").append((eRadW*2) + lineWidth).append(",0\" ");


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            if(ec == 140200)//Line of Contact
            {
                fillColor=null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = 0;
                double eRadH = ((10 + lineWidth) * multiplier);
                double eRadW = eRadH;// * 0.5f;
                double patternHeight = (eRadH + lineWidth)*2;
                double patternWidth = lwOffset + (eRadW * 2) + lineWidth + lwOffset;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'uuuu'
                y += patternHeight;
                //red squiggle
                svgPath.append("M ").append(lwOffset).append(" ").append(y).append(" ");
                svgPath.append("a ").append(eRadW+lwOffset).append(",").append(eRadW+lwOffset).append(" 0 0,1 ").append((eRadW*2) + lineWidth).append(",0\" ");
                svgPath.append("style=\"fill:none;stroke:red;");
                if(lineColor.getAlpha() < 255)
                    svgPath.append("stroke-opacity:").append(lineColor.getAlpha()/255).append(";");
                svgPath.append("stroke-width:").append(lineWidth).append("\" />");
                //affiliation color squiggle
                svgPath.append("<path d=\"");
                svgPath.append("M ").append(lwOffset).append(" ").append(0).append(" ");
                svgPath.append("a ").append(eRadW+lwOffset).append(",").append(eRadW+lwOffset).append(" 0 0,0 ").append((eRadW*2) + lineWidth).append(",0\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 151203)//Strong Point
            {
                fillColor=null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = 0;
                double eRadH = ((10 + lineWidth) * multiplier);
                double eRadW = eRadH * 0.5f;
                double patternHeight = eRadH + lineWidth;
                double patternWidth = (eRadW * 2) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build '_|_' which will repeat
                y += eRadH + lwOffset;
                svgPath.append("M ").append(0).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth/2).append(" ").append(0).append(" ");
                svgPath.append("l ").append(0).append(" ").append(-eRadH - lineWidth).append(" ");
                svgPath.append("m ").append(0).append(" ").append(eRadH + lineWidth).append(" ");
                svgPath.append("l ").append(patternWidth/2).append(" ").append(0).append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 151800)//Encirclement
            {
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadW * 3) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build '/\'
                y += lineWidth + (eRadH) + lwOffset;
                svgPath.append("M ").append(0).append(" ").append(y).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(-eRadH - lineWidth).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(eRadH + lineWidth).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" Z\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 270100 || ec == 270200 || ec == 290100)//Obstacle Belt || Obstacle Zone || Obstacle Line
            {
                // _/\_/\_/\_

                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadW * 3) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build '_/\_'
                y += lwOffset + (eRadH) + lwOffset;
                svgPath.append("M ").append(0).append(" ").append(y).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(-eRadH).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(eRadH).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 270300 || ec == 270400 || ec == 290204)
                {
                //Obstacle Free Zone OR
                //Obstacle Restricted Zone (has fill, might be a problem) OR
                // Antitank Wall
                // _  _  _
                //  \/ \/
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadW * 3) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build
                // _  _  _
                //  \/ \/
                y += lwOffset + (eRadH) + lwOffset;
                svgPath.append("M ").append(0).append(" ").append(lwOffset).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(eRadH).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(-eRadH).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290201 || ec == 290202)//Ditch - Under Construction || Ditch - Completed
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadW * 2) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build '/\'
                y += lwOffset + (eRadH) + lwOffset;
                svgPath.append("M ").append(lwOffset).append(" ").append(y).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(-eRadH).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(eRadH).append(" Z").append("\" ");

                if(ec == 290202)
                    fillColor = SymbolUtilities.getLineColorOfAffiliation(symbolCode);


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290203)//Ditch Reinforced, with Antitank Mines
            {

                fillColor = SymbolUtilities.getLineColorOfAffiliation(symbolCode);
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadW * 3) + lineWidth*2 + ((eRadW*0.6));

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");
                svgEllipse = new StringBuilder("<circle ");

                //build
                // ______
                //  \/ 0
                y += lwOffset + (eRadH) + lwOffset;
                svgPath.append("M ").append(lwOffset).append(" ").append(lwOffset).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(eRadH).append(" ");
                svgPath.append("l ").append(eRadW).append(" ").append(-eRadH).append(" Z ");
                svgPath.append("M ").append(0).append(" ").append(lwOffset).append(" ");
                svgPath.append("l ").append(patternWidth).append(" ").append(0).append("\" ");

                svgEllipse.append("r=\"").append((eRadW*0.6)-lwOffset).append("\" cx=\"").append(lineWidth + (eRadW*3) + lineWidth).append("\" cy=\"").append((y + lwOffset)*0.6).append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290301)//Unspecified
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadH + lineWidth) * 2.5f;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ").append("\" ");

                //calculate bounds of line pattern
                y += lineWidth + (eRadH * 2) + lineWidth;
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290302)//Single Fence
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 3.5f;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");

                //center line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(eCenterY).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290303)//Double Fence
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = (eRadH + lineWidth) * 5.5f;//(eRadH * 5.5f) + lineWidth;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = (patternWidth/2) - eRadW - lwOffset;
                double eCenterY = lineWidth + lwOffset + eRadH;
                //X1
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");
                //X2
                eCenterX = (patternWidth/2) + eRadW + lwOffset;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");

                //center line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(eCenterY).append(" ");
                svgPath.append("l ").append(patternWidth).append(" 0").append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290304)//Double Apron Fence
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");

                //center line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(eCenterY).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290305)//Low Wire Fence
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");

                //bottom line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290306)//High Wire Fence
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //build 'X'
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY - eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(eRadH*2 + lineWidth).append(" ");
                svgPath.append("M ").append(eCenterX-eRadW).append(" ").append(eCenterY + eRadH + lwOffset).append(" ");
                svgPath.append("l ").append(eRadW*2).append(" ").append(-(eRadH*2) - lineWidth).append(" ");


                //top line
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0 ");

                //bottom line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290307)//Single Concertina
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;


                //build ellipse
                svgEllipse = new StringBuilder("<ellipse ");
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgEllipse.append("cx=\"").append(eCenterX).append("\" cy=\"").append(eCenterY).append("\" ");
                svgEllipse.append("rx=\"").append(eRadW).append("\" ry=\"").append(eRadH).append("\" ");

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //bottom line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }else if(ec == 290308)//Double Strand Concertina
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;


                //build ellipse
                svgEllipse = new StringBuilder("<ellipse ");
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgEllipse.append("cx=\"").append(eCenterX).append("\" cy=\"").append(eCenterY).append("\" ");
                svgEllipse.append("rx=\"").append(eRadW).append("\" ry=\"").append(eRadH).append("\" ");

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //middle line
                svgPath.append("M ").append(x).append(" ").append(eCenterY).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0 ");

                //bottom line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290309)//Triple Strand Concertina
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = lwOffset;
                double eRadH = (10 + (lineWidth*2)) * multiplier;
                double eRadW = eRadH * 0.6f;
                double patternWidth = eRadH * 2.5f;


                //build ellipse
                svgEllipse = new StringBuilder("<ellipse ");
                double eCenterX = lwOffset + (patternWidth/2);
                double eCenterY = lineWidth + lwOffset + eRadH;
                svgEllipse.append("cx=\"").append(eCenterX).append("\" cy=\"").append(eCenterY).append("\" ");
                svgEllipse.append("rx=\"").append(eRadW).append("\" ry=\"").append(eRadH).append("\" ");

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");

                //top line
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0 ");

                //bottom line
                y += lineWidth + (eRadH * 2) + lineWidth;
                svgPath.append("M ").append(x).append(" ").append(y).append(" ");
                svgPath.append("l ").append(patternWidth + lineWidth).append(" 0").append("\" ");


                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth + lineWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = y;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290900 || ec == 151000)//Fortified Line OR Fortified Area
            {
                fillColor = null;
                double lwOffset = lineWidth/2f;
                double x = 0;
                double y = 0;
                double eRadH = (10 + lineWidth) * multiplier;
                double eRadW = eRadH;
                double patternWidth = (eRadW * 2) + (lineWidth*2);

                //build line pattern
                svgPath = new StringBuilder("<path d=\"");
                //         _
                //build '_| |_'
                y += lwOffset + (eRadH) + lwOffset;
                svgPath.append("M ").append(0).append(" ").append(y).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append(" ");
                svgPath.append("l ").append(0).append(" ").append(-eRadH - lwOffset).append(" ");
                svgPath.append("l ").append(eRadW + lineWidth).append(" ").append(0).append(" ");
                svgPath.append("l ").append(0).append(" ").append(eRadH + lwOffset).append(" ");
                svgPath.append("l ").append((eRadW/2) + lwOffset).append(" ").append(0).append("\" ");

                //calculate bounds of line pattern
                double left = 0;
                double width = patternWidth;
                double top = 0;
                double bottom = y + lwOffset;

                vOffset = lwOffset + eRadH;
                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }

            if(svgPath != null)
            {
                //build style string
                StringBuilder sbStyle = new StringBuilder("style=\"");
                if(fillColor != null) {
                    sbStyle.append("fill:").append(RendererUtilities.colorToHexString(fillColor, false)).append(";");
                    if(fillColor.getAlpha() < 255)
                        sbStyle.append("fill-opacity:").append(fillColor.getAlpha()/255).append(";");
                }
                else
                    sbStyle.append("fill:none;");
                sbStyle.append("stroke:").append(RendererUtilities.colorToHexString(lineColor, false)).append(";");
                if(lineColor.getAlpha() < 255)
                    sbStyle.append("stroke-opacity:").append(lineColor.getAlpha()/255).append(";");
                sbStyle.append("stroke-width:").append(lineWidth).append("\"");

                //add style to SVGPath
                svgPath.append(sbStyle);
                svgPath.append(" />");//close path tag

                //add style to Ellipse if present
                if(svgEllipse != null ){
                    svgEllipse.append(sbStyle);
                    svgEllipse.append(" />");//close path tag
                }

                //build svg tag and insert path
                StringBuilder sbSVG = new StringBuilder("<svg xmlns=\"http://www.w3.org/2000/svg\" ");
                sbSVG.append("width=\"").append(svgBounds.getWidth()).append("\" height=\"").append(svgBounds.getHeight()).append("\" ");
                sbSVG.append("viewBox=\"").append(svgBounds.getX()).append(" ").append(svgBounds.getY()).append(" ").append(svgBounds.getWidth()).append(" ").append(svgBounds.getHeight()).append("\" ");
                sbSVG.append("fill=\"none\">");

                sbSVG.append(svgPath);
                if(svgEllipse != null)
                    sbSVG.append(svgEllipse);
                sbSVG.append("</svg>");

                //convert to image
                Rectangle2D biBounds = RectUtilities.roundRect(svgBounds);
                BufferedImage bmp = RendererUtilities.renderSVG(biBounds, sbSVG.toString());

                if(bmp != null)
                    lp = new LinePattern(bmp,sbSVG.toString(), vOffset);
            }
        }
        return lp;
    }
}


