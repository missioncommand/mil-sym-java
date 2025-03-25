package armyc2.c5isr.renderer.utilities;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface SymbolDimensionInfo {


    /**
     * The x value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getSymbolCenterX();

    /**
     * The y value the image should be centered on or the "anchor point".
     * @return {@link Integer}
     */
    public int getSymbolCenterY();

    /**
     * The point the image should be centered on or the "anchor point".
     * @return {@link Point}
     */
    public Point getSymbolCenterPoint();

    /**
     * minimum bounding rectangle for the core symbol. Does
     * not include modifiers, display or otherwise.
     * @return {@link Rectangle2D}
     */
    public Rectangle2D getSymbolBounds();

    /**
     * Dimension of the entire image.
     * @return {@link Rectangle2D}
     */

    public Rectangle2D getImageBounds();
}
