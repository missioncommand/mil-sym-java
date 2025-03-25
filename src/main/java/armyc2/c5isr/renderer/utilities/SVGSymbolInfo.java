package armyc2.c5isr.renderer.utilities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Base64;

public class SVGSymbolInfo implements SymbolDimensionInfo{

    private String _svg = null;
    private String _svgDataURI = null;

    private int _anchorX = 0;
    private int _anchorY = 0;
    private Rectangle2D _symbolBounds = null;
    private Rectangle2D _bounds = null;

    public SVGSymbolInfo(String svg, Point2D anchorPoint, Rectangle2D symbolBounds, Rectangle2D svgBounds)
    {
        _svg = svg;
        _anchorX = (int)anchorPoint.getX();
        _anchorY = (int)anchorPoint.getY();
        _symbolBounds = symbolBounds;
        _bounds = svgBounds;
    }

    public String getSVGDataURI()
    {
        if(_svgDataURI==null)
        {
            _svgDataURI = new String(Base64.getEncoder().encode(_svg.getBytes()));
        }
        return _svgDataURI;
    }

    public String getSVG(){return _svg;}

    /**
     * The x value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getSymbolCenterX()
    {
        return _anchorX;
    }

    /**
     * The y value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getSymbolCenterY()
    {
        return _anchorY;
    }

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link Point}
     */
    public Point getSymbolCenterPoint()
    {
        return new Point(_anchorX, _anchorY);
    }

    /**
     * minimum bounding rectangle for the core symbol. Does
     * not include modifiers, display or otherwise.
     * @return {@link Rectangle2D}
     */
    public Rectangle2D getSymbolBounds()
    {
        return new Rectangle2D.Double(_symbolBounds.getX(),_symbolBounds.getY(),_symbolBounds.getWidth(),_symbolBounds.getHeight());
    }

    /**
     * Dimension of the entire image.
     * @return {@link Rectangle2D}
     */

    public Rectangle2D getImageBounds()
    {
        return new Rectangle2D.Double(_bounds.getX(),_bounds.getY(),_bounds.getWidth(),_bounds.getHeight());
    }



}
