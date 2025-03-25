package armyc2.c5isr.renderer.utilities;

import java.awt.geom.Rectangle2D;

public class SVGInfo {

    private String _ID = null;
    private Rectangle2D _Bbox = null;
    private String _SVG = null;
    public SVGInfo(String id, Rectangle2D measurements, String svg)
    {
        _ID = id;
        _Bbox = measurements;
        _SVG = svg;
    }

    public String getID()
    {
        return _ID;
    }

    public Rectangle2D getBbox()
    {
        return _Bbox;
    }

    public String getSVG()
    {
        return _SVG;
    }

    public String toString()
    {
        return _ID + "\n" + _Bbox.toString() + "\n" + _SVG;
    }
}
