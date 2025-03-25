/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.JavaLineArray;

/**
 * A class for channel points used by clsChannelUtility
 * 
 */
public class CChannelPoints2
{
    protected POINT2 m_Line1;
    protected POINT2 m_Line2;
    protected CChannelPoints2()
    {
        m_Line1=new POINT2();
        m_Line2=new POINT2();
    }
    protected CChannelPoints2(CChannelPoints2 pts)
    {
        m_Line1=new POINT2(pts.m_Line1);
        m_Line2=new POINT2(pts.m_Line2);
    }
}
