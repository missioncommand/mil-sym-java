/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.JavaLineArray;
import armyc2.c5isr.renderer.utilities.ShapeInfo;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;


/**
 *
 * 
 */
public class Shape2 extends ShapeInfo
{

    public Shape2(int value)
    {
        setShapeType(value);
        _Shape=new GeneralPath();
        BasicStroke stroke=new BasicStroke();
        this.setStroke(stroke);
    }
    private int style=0;  //e.g. 26 for enemy flots
    public void set_Style(int value)
    {
        style = value;
    }
    private int fillStyle;
    public void set_Fillstyle(int value)
    {
        fillStyle=value;
    }
    public int get_FillStyle()
    {
        return fillStyle;
    }
    public int get_Style()  //used by TacticalRenderer but not client
    {
        return style;
    }
    public void lineTo(POINT2 pt)
    {
        ((GeneralPath)_Shape).lineTo(pt.x, pt.y);
    }
    public void moveTo(POINT2 pt)
    {       
        ((GeneralPath)_Shape).moveTo(pt.x, pt.y);
    }
    @Override
    public Rectangle getBounds()
    {
        if(_Shape instanceof GeneralPath)
        {
            return _Shape.getBounds();
        }
        else
            return this.getBounds();
    }

    public ArrayList<POINT2> getPoints() {
        ArrayList<POINT2> points = new ArrayList<>();
        for (PathIterator i = this.getShape().getPathIterator(null); !i.isDone(); i.next()) {
            double[] coords = new double[6];
            int type = i.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                case PathIterator.SEG_CLOSE:
                    points.add(new POINT2(coords[0], coords[1], type));
                    break;
                case PathIterator.SEG_QUADTO:
                    points.add(new POINT2(coords[0], coords[1], type));
                    points.add(new POINT2(coords[2], coords[3], type));
                    break;
                case PathIterator.SEG_CUBICTO:
                    points.add(new POINT2(coords[0], coords[1], type));
                    points.add(new POINT2(coords[2], coords[3], type));
                    points.add(new POINT2(coords[4], coords[5], type));
                    break;
                default:
                    break;
            }
        }
        return points;
    }
}
