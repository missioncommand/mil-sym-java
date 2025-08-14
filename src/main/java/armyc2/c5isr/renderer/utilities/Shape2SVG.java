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
     * @param lineCap "butt", "round", or "square"
     * @return
     */
    public static String Convert(Shape shape,String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
    {
        if(shape instanceof Path2D)
            return convertPath((Path2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray, lineCap);
        else if(shape instanceof Rectangle2D)
            return convertRect((Rectangle2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray, lineCap);
        else if(shape instanceof Ellipse2D)
            return convertEllipse((Ellipse2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray, lineCap);
        else if(shape instanceof Line2D)
            return convertLine((Line2D)shape, stroke, fill, strokeWidth, strokeOpacity, fillOpacity, dashArray, lineCap);
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
            String name = textInfo.getFontName() + ", sans-serif";//"SansSerif";
            String size = String.valueOf(textInfo.getFontSize());
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

            if(textInfo.getFontStyle() == Font.BOLD)
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
     * @param lineCap "butt", "round", or "square"
     * @return
     */
    private static String convertPath(Path2D path2D, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
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

            if(lineCap != null &&
                    (lineCap.equalsIgnoreCase("butt") ||
                        lineCap.equalsIgnoreCase("round") ||
                        lineCap.equalsIgnoreCase("square")))
            {
                sbLine.append(" stroke-linecap=\"").append(lineCap).append("\"");
            }
            else
                sbLine.append(" stroke-linecap=\"round\"");


            if(dashArray != null)
                sbLine.append(" stroke-dasharray=\"").append(dashArray).append("\"");
        }

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

        return sbLine.toString();
    }

    /**
     *
     * @param rect
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @param lineCap "butt", "round", or "square"
     * @return
     */
    private static String convertRect(Rectangle2D rect, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
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

                if(lineCap != null &&
                        (lineCap.equalsIgnoreCase("butt") ||
                                lineCap.equalsIgnoreCase("round") ||
                                lineCap.equalsIgnoreCase("square")))
                {
                    sb.append(" stroke-linecap=\"").append(lineCap).append("\"");
                }
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

    /**
     *
     * @param line
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @param lineCap "butt", "round", or "square"
     * @return
     */
    private static String convertLine(Line2D line, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
    {
        //<line x1="34.89607146049936" y1="3.2455781704774154" x2="44.36527642021904" y2="11.417631765851933" stroke-width="0.999999995" stroke="black" fill="none"></line>
        StringBuilder sb = new StringBuilder();
        if(line != null)
        {
            sb.append("<line x1=\"" + line.getX1() + "\" y1=\"" + line.getY1());
            sb.append("\" x2=\"" + line.getX2() + "\" y2=\"" + line.getY2() + "\"");

            if(stroke != null)
            {
                sb.append(" stroke=\"" + stroke + "\"");

                if(strokeWidth != null)
                    sb.append(" stroke-width=\"" + strokeWidth + "\"");
                else
                    sb.append(" stroke-width=\"2\"");

                if(lineCap != null &&
                        (lineCap.equalsIgnoreCase("butt") ||
                                lineCap.equalsIgnoreCase("round") ||
                                lineCap.equalsIgnoreCase("square")))
                {
                    sb.append(" stroke-linecap=\"").append(lineCap).append("\"");
                }
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

    /**
     *
     * @param ellipse
     * @param stroke like "#000000
     * @param fill like "#0000FF" or "none"
     * @param strokeWidth "#"
     * @param strokeOpacity "1.0"
     * @param fillOpacity "1.0"
     * @param dashArray "4 1 2 3"
     * @param lineCap "butt", "round", or "square"
     * @return
     */
    private static String convertEllipse(Ellipse2D ellipse, String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
    {
        StringBuilder sb = new StringBuilder();
        if(ellipse != null && !ellipse.isEmpty())
        {
            //<ellipse cx="39.56581637214194" cy="7.396462536381936" rx="6.485756821725808" ry="6.485756821725808" stroke-width="0.999999995" stroke="black" fill="yellow"></ellipse>
            sb.append("<ellipse cx=\"" + (ellipse.getX() + ellipse.getWidth()/2f) + "\" cy=\"" + (ellipse.getY() + ellipse.getHeight()/2f) + "\"");
            sb.append(" rx=\"" + (ellipse.getWidth()/2f) + "\" ry=\"" + (ellipse.getHeight()/2f) + "\"");

            if(stroke != null)
            {
                sb.append(" stroke=\"" + stroke + "\"");

                if(strokeWidth != null)
                    sb.append(" stroke-width=\"" + strokeWidth + "\"");
                else
                    sb.append(" stroke-width=\"2\"");

                if(lineCap != null &&
                        (lineCap.equalsIgnoreCase("butt") ||
                                lineCap.equalsIgnoreCase("round") ||
                                lineCap.equalsIgnoreCase("square")))
                {
                    sb.append(" stroke-linecap=\"").append(lineCap).append("\"");
                }
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
