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
    int vOffset = 0;

    LinePattern(BufferedImage linePattern, String svgPattern, int verticalOffset)
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

    private static LinePattern getLinePattern(String symbolCode, Color lineColor, Color fillColor, float lineWidth)
    {
        LinePattern lp = null;
        StringBuilder svgPath = null;
        StringBuilder svgEllipse = null;
        StringBuilder svgText = null;
        Rectangle2D svgBounds = null;


        if(symbolCode != null && symbolCode.length() >= 20)
        {
            int symbolSet = SymbolID.getSymbolSet(symbolCode);
            int ec = SymbolID.getEntityCode(symbolCode);
            int dpi = RendererSettings.getInstance().getDeviceDPI();

            float offset = lineWidth/2f;// + 0.5f;
            float multiplier = dpi > 96 ? dpi/96f : 1;

            if(ec == 151000)//Fortified Area
            {

            }
            else if(ec == 151203)//Strong Point
            {

            }
            else if(ec == 151800)//Encirclement
            {

            }
            else if(ec == 270100)//Obstacle Belt
            {

            }
            else if(ec == 270200)//Obstacle Zone
            {

            }
            else if(ec == 270300)//Obstacle Free Zone
            {

            }
            else if(ec == 270400)//Obstacle Restricted Zone (has fill, might be a problem)
            {

            }
            else if(ec == 290100)//Obstacle Line
            {

            }
            else if(ec == 290201)//Ditch - Under Construction
            {

            }
            else if(ec == 290202)//Ditch - Completed
            {

            }
            else if(ec == 290203)//Ditch Reinforced, with Antitank Mines
            {

            }
            else if(ec == 290204)//Antitank Wall
            {

            }
            else if(ec == 290301)//Unspecified
            {

            }
            else if(ec == 290302)//Single Fence
            {

            }
            else if(ec == 290303)//Double Fence
            {

            }
            else if(ec == 290304)//Double Apron Fence
            {

            }
            else if(ec == 290305)//Low Wire Fence
            {

            }
            else if(ec == 290306)//High Wire Fence
            {

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

                svgBounds = new Rectangle2D.Double(left,top,width,bottom - top);
            }
            else if(ec == 290900)//Fortified Line
            {
                Path2D path = new Path2D.Float();
                path.moveTo(10,50);
                path.lineTo(20,50);
                path.lineTo(20,30);
                path.lineTo(40,30);
                path.lineTo(40,50);
                path.lineTo(50,50);


                //build line pattern
                svgPath = new StringBuilder("<path d=\"");
                svgPath.append("M ").append("10 ").append("200 ");
                svgPath.append("l ").append((10 + offset) * multiplier).append(" 0 ");
                svgPath.append("l ").append(" 0 ").append((-20 - lineWidth)*multiplier).append(" ");
                svgPath.append("l ").append((20 + lineWidth) * multiplier).append(" 0 ");
                svgPath.append("l ").append(" 0 ").append((20 + lineWidth)*multiplier).append(" ");
                svgPath.append("l ").append((10 + offset) * multiplier).append(" 0 ").append("\" ");
                //svgPath.append(sbStyle);
                //svgPath.append(" />");

                //calculate bounds of line pattern
                double left = 10;
                double width = 0;
                double top = 200;
                double bottom = 200;
                top += ( -20 - lineWidth)*multiplier - Math.ceil(lineWidth/2.0f);
                bottom += Math.ceil(lineWidth/2.0f);
                width = (10 + lineWidth + 20+lineWidth + 10 + lineWidth) * multiplier;

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
                if(svgEllipse != null){
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
                    lp = new LinePattern(bmp,sbSVG.toString(), 0);
            }
        }
        return lp;
    }
}


