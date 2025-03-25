package armyc2.c5isr.renderer.utilities;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.Base64;

public class Shape2SVG {

    /**
     *
     * @param shape like {@link Shape}
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @return
     */
    public static String Convert(Shape shape,String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        if(shape instanceof Path2D)
            return convertPath((Path2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        else if(shape instanceof Rectangle2D)
            return convertRect((Rectangle2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
        else
            return null;
    }

    public static String Convert(String text, int x, int y, Font font, FontRenderContext frc, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        //(String text, int x, int y, Font font, FontRenderContext frc)
        TextInfo textInfo = new TextInfo(text, x, y, font, frc);
        return Convert(textInfo, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray);
    }

    public static String Convert(TextInfo textInfo, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        String svg = null;
        StringBuilder sb = new StringBuilder();
        if(textInfo != null)
        {
            String style = null;
            String name = RendererSettings.getInstance().getLabelFont().getFontName() + ", sans-serif";//"SansSerif";
            String size = String.valueOf(RendererSettings.getInstance().getLabelFont().getSize());
            String weight = null;
            String anchor = null;//"start";
            String text = textInfo.getText();

            text = text.replaceAll("&","&amp;");
            text = text.replaceAll("<","&lt;");
            text = text.replaceAll(">","&gt;");

            Point2D location = new Point2D.Double(textInfo.getLocation().getX(),textInfo.getLocation().getY());

            if(textInfo.getLocation().getX() < 0)
            {
                if(textInfo.getLocation().getX() + textInfo.getTextBounds().getWidth() > 0)
                {
                    anchor = "middle";
                    location.setLocation(textInfo.getTextBounds().getCenterX(), location.getY());
                }
                else
                {
                    anchor = "end";
                    location.setLocation(textInfo.getTextBounds().getMaxX(), location.getY());
                }
            }

            if(RendererSettings.getInstance().getLabelFont().isBold())
                weight = "bold";

            sb.append("<text x=\"" + location.getX() + "\" y=\"" + location.getY() + "\"");

            if(anchor != null)
                sb.append(" text-anchor=\"" + anchor + "\"");
            sb.append(" font-family=\"" + name + '"');
            sb.append(" font-size=\"" + size + "px\"");
            if(weight != null)
                sb.append(" font-weight=\"" + weight + "\"");
            sb.append(" alignment-baseline=\"alphabetic\"");//
            sb.append(" stroke-miterlimit=\"3\"");

            //sb.append(" text-anchor=\"" + anchor + "\"");//always start for single points and default SVG behavior

            /*if(this._angle)
            {
                se += ' transform="rotate(' + this._angle + ' ' + this._anchor.getX() + ' ' + this._anchor.getY() + ')"';
            }*/

            String seStroke = "",
                    seFill = "";



            if(stroke != null)
            {
                seStroke = sb.toString();

                seStroke += " stroke=\"" + stroke + "\"";
                /*else
                    seStroke = se + ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

                if(strokeWidth != null)
                    seStroke += " stroke-width=\"" + strokeWidth + "\"";
                seStroke += " fill=\"none\"";
                seStroke += ">";
                seStroke += text;
                seStroke += "</text>";
            }

            if(fill != null)
            {
                seFill = sb.toString();

                seFill += " fill=\"" + fill + "\"";
                seFill += ">";
                seFill += text;
                seFill += "</text>";
            }

            sb = new StringBuilder();
            if(stroke != null && fill != null)
                sb.append(seStroke + "\n" + seFill).append("\n");
            else if(fill != null)
                sb.append(seFill);
            else
                return null;
            return sb.toString();
        }
        return null;
    }

    /**
     * Assumes common font properties will be defined in the group.
     * @param textInfo
     * @param stroke
     * @param fill
     * @param strokeWidth
     * @param strokeOpacity
     * @param fillOpacity
     * @param dashArray
     * @return
     */
    public static String ConvertForGroup(TextInfo textInfo, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        String svg = null;
        StringBuilder sb = new StringBuilder();
        if(textInfo != null)
        {
            String anchor = null;//"start";
            String text = textInfo.getText();

            text = text.replaceAll("&","&amp;");
            text = text.replaceAll("<","&lt;");
            text = text.replaceAll(">","&gt;");

            Point2D location = new Point2D.Double(textInfo.getLocation().getX(),textInfo.getLocation().getY());

            if(textInfo.getLocation().getX() < 0)
            {
                if(textInfo.getLocation().getX() + textInfo.getTextBounds().getWidth() > 0)
                {
                    anchor = "middle";
                    location.setLocation(textInfo.getTextBounds().getCenterX(), location.getY());
                }
                else
                {
                    anchor = "end";
                    location.setLocation(textInfo.getTextBounds().getMaxX(), location.getY());
                }
            }



            sb.append("<text x=\"" + location.getX() + "\" y=\"" + location.getY() + "\"");

            if(anchor != null)
                sb.append(" text-anchor=\"" + anchor + "\"");

            //sb.append(" text-anchor=\"" + anchor + "\"");//always start for single points and default SVG behavior

            /*if(this._angle)
            {
                se += ' transform="rotate(' + this._angle + ' ' + this._anchor.getX() + ' ' + this._anchor.getY() + ')"';
            }*/

            String seStroke = "",
                    seFill = "";



            if(stroke != null)
            {
                seStroke = sb.toString();

                seStroke += " stroke=\"" + stroke + "\"";
                /*else
                    seStroke = se + ' stroke="' + stroke.replace(/#/g,"&#35;") + '"';*/

                if(strokeWidth != null)
                    seStroke += " stroke-width=\"" + strokeWidth + "\"";
                seStroke += " fill=\"none\"";
                seStroke += ">";
                seStroke += text;
                seStroke += "</text>";
            }

            if(fill != null)
            {
                seFill = sb.toString();


                seFill += " fill=\"" + fill + "\"";
                seFill += ">";
                seFill += text;
                seFill += "</text>";
            }

            sb = new StringBuilder();
            if(stroke != null && fill != null)
                sb.append(seStroke + "\n" + seFill).append("\n");
            else if(fill != null)
                sb.append(seFill);
            else
                return null;
            return sb.toString();
        }
        return null;
    }

    public static String makeBase64Safe(String svg)
    {
        if(svg != null)
        {
            //Base64 encoding
            //return new String(Base64.getEncoder().encode(svg.getBytes()));
            //URL-safe Base64 encoding
            return new String(Base64.getUrlEncoder().encodeToString(svg.getBytes()));
        }
        else
            return null;
    }

    private static String convertArc(Arc2D arc)
    {
        return null;
    }

    /**
     *
     * @param path2D like {@link Path2D.Double}
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @return
     */
    private static String convertPath(Path2D path2D, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        double[] coords = new double[6];
        StringBuilder sbPath = new StringBuilder();
        StringBuilder sbLine = new StringBuilder();
        String path = "";
        Point2D moveTo = null;
        int windingRule = PathIterator.WIND_EVEN_ODD;
        int format = 1;

        PathIterator pitr = path2D.getPathIterator(null);
        windingRule = pitr.getWindingRule();
        do
        {
            int type = pitr.currentSegment(coords);
            if(type==PathIterator.SEG_LINETO)
            {
                sbPath.append("L" + coords[0] + " " + coords[1]);
            }
            else if(type==PathIterator.SEG_MOVETO)
            {
                sbPath.append("M" + coords[0] + " " + coords[1]);
            }
            else if(type==PathIterator.SEG_QUADTO)
            {
                sbPath.append("Q" + coords[0] + " " + coords[1] + " " + coords[2] + " " + coords[3]);
            }
            else if(type==PathIterator.SEG_CUBICTO)
            {
                sbPath.append("C" + coords[0] + " " + coords[1] + " " + coords[2] + " " + coords[3] + " " + coords[4] + " " + coords[5]);
            }
            else if(type==PathIterator.SEG_CLOSE)
            {
                sbPath.append("Z");
            }
            pitr.next();
        } while (!pitr.isDone());

        sbLine.append("<path d=\"").append(sbPath).append("\"");

        if(stroke != null)
        {
            if(format == 2)
                sbLine.append(" stroke=\"").append(stroke.replace("#","%23")).append("\"");
            else
                sbLine.append(" stroke=\"").append(stroke).append("\"");

            if(strokeWidth != null)
                sbLine.append(" stroke-width=\"").append(strokeWidth).append("\"");
            else
                sbLine.append(" stroke-width=\"2\"");

            if(strokeOpacity != null && strokeOpacity != "1.0")
            {
                sbLine.append(" stroke-opacity=\"").append(strokeOpacity).append("\"");
            }

            //sbLine.append(" stroke-linecap=\"round\"");

            if(dashArray != null)
                sbLine.append(" stroke-dasharray=\"").append(dashArray).append("\"");

            if(fill != null)
            {
                if(format == 2)
                    sbLine.append(" fill=\"").append(fill.replace("#","%23")).append("\"");
                else
                    sbLine.append(" fill=\"").append(fill).append("\"");

                if(fillOpacity != null && fillOpacity != "1.0")
                {
                    sbLine.append(" fill-opacity=\"").append(fillOpacity).append("\"");
                }
            }
            else
                sbLine.append(" fill=\"none\"");

            sbLine.append(" />");
        }

        return sbLine.toString();
    }

    private static String convertRect(Rectangle2D rect, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray)
    {
        StringBuilder sb = new StringBuilder();
        if(rect != null && rect.isEmpty() != true)
        {
            sb.append("<rect x=\"" + rect.getX() + "\" y=\"" + rect.getY());
            sb.append("\" width=\"" + rect.getWidth() + "\" height=\"" + rect.getHeight() + "\"");

            if(stroke != null)
            {
                sb.append(" stroke=\"" + stroke + "\"");

                if(strokeWidth != null)
                    sb.append(" stroke-width=\"" + strokeWidth + "\"");
                else
                    sb.append(" stroke-width=\"2\"");
            }

            if(fill != null)
                sb.append(" fill=\"" + fill + "\"");
            else
                sb.append(" fill=\"none\"");

            sb.append("/>");

            return sb.toString();
        }
        else
            return null;
    }
}
