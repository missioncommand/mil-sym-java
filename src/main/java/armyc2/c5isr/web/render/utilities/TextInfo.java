/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.web.render.utilities;

//import java.awt.geom.Point2D;

import java.awt.geom.Point2D;

/**
 *
 * 
 */
public class TextInfo {
        private String _ModifierString = null;
        private Point2D _ModifierStringPosition = null;
        private double _ModifierStringAngle = 0;
        
    public TextInfo()
    {
        
    }
        //set this when returning text string.
    public void setModifierString(String value)
    {
        _ModifierString = value;
    }

    public String getModifierString()
    {
        return _ModifierString;
    }

    //location to draw ModifierString.
    public void setModifierStringPosition(Point2D value)
    {
        _ModifierStringPosition = value;
    }

    public Point2D getModifierStringPosition()
    {
        return _ModifierStringPosition;
    }

    //angle to draw ModifierString.
    public void setModifierStringAngle(double value)
    {
        _ModifierStringAngle = value;
    }

    public double getModifierStringAngle()
    {
        return _ModifierStringAngle;
    }
}
