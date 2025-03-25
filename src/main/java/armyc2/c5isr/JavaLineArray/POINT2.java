/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.JavaLineArray;

/**
 * Class to provide a Point object with a linestyle to facilitate drawing.
 * 
 */
public class POINT2
{
    public double  x;
    public double  y;
    public int style;
    public int segment;
    public POINT2(double x, double y)
    {
        this.x=x;
        this.y=y;
        this.style=0;
    }
    public POINT2(double x, double y, int segment, int style)
    {
        this.x=x;
        this.y=y;
        this.segment=segment;
        this.style=style;
    }
    public POINT2(double x, double y, int style)
    {
        this.x=x;
        this.y=y;
        this.style=style;
    }
    public POINT2(POINT2 pt)
    {
        this.x=pt.x;
        this.y=pt.y;
        this.segment=pt.segment;
        this.style=pt.style;
    }
    public POINT2()
    {
        this.x=0;
        this.y=0;
        this.style=0;
    }
}

