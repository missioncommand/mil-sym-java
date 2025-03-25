/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.JavaLineArray;
import java.util.ArrayList;

import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.renderer.utilities.ErrorLogger;
import armyc2.c5isr.renderer.utilities.RendererException;
import armyc2.c5isr.renderer.utilities.RendererSettings;

/**
 * A class for the interface between the points calculation CELineArray and
 * the tactical renderer.
 *
 * 
 */
public final class CELineArray {
    private static final String _className="CELineArray";
    /**
    * public function to return the line count required for all of the symbols
    *
    * @param plArrayOfLongs the client points as an array of POINT2 in pixels.
    * @param lElements the number of client points.
    * @param ChannelWidth the chanel width in pixels
    *
    * @return the number of points which will be required for the symbol.
    */
    public static int CGetLineCountDouble(TGLight tg,
            double[] plArrayOfLongs,
            int lElements, //number of points
            int ChannelWidth) {
        int lResult = 0;
        try {
            //declarations
            int lPtrcntr = 0;
            int lLowerFlotCount = 0, lUpperFlotCount = 0;
            POINT2[] pLinePoints = new POINT2[lElements];
            POINT2[] pLowerLinePoints = new POINT2[lElements],
                    pUpperLinePoints = new POINT2[lElements],
                    pUpperLowerLinePoints = new POINT2[2 * lElements + 2];
            short i = 0;
            //end declarations

            if (lElements <= 0) {
                return -1;
            }

            lineutility.InitializePOINT2Array(pLinePoints);
            lineutility.InitializePOINT2Array(pUpperLinePoints);
            lineutility.InitializePOINT2Array(pLowerLinePoints);
            for (i = 0; i < lElements; i++) {
                pLinePoints[i].x = plArrayOfLongs[lPtrcntr];
                lPtrcntr++;
                pLinePoints[i].y = plArrayOfLongs[lPtrcntr];
                lPtrcntr++;
            }
            for (i = 0; i < lElements; i++) {
                pLowerLinePoints[i] = new POINT2(pLinePoints[i]);
                pUpperLinePoints[i] = new POINT2(pLinePoints[i]);
            }

            switch (tg.get_LineType()) {
                case TacticalLines.CHANNEL:
                case TacticalLines.CHANNEL_FLARED:
                case TacticalLines.CHANNEL_DASHED:
                    lResult = 2 * lElements;
                    break;
                case TacticalLines.MAIN:
                case TacticalLines.MAIN_STRAIGHT:
                case TacticalLines.AIRAOA:
                case TacticalLines.SPT:
                case TacticalLines.SPT_STRAIGHT:
                    //points for these need not be bounded
                    //they have an extra 8 points for the arrowhead
                    lResult = 2 * lElements + 8;
                    break;
                case TacticalLines.CATK:
                    lResult = 2 * lElements + 8;
                    break;
                case TacticalLines.CATKBYFIRE:
                    lResult = 2 * lElements + 17;
                    break;
                case TacticalLines.AAAAA:
                    lResult = 2 * lElements + 19;
                    break;
                case TacticalLines.LC:
                    pUpperLinePoints = Channels.GetChannelArray2Double(1, pUpperLinePoints, 1, lElements, tg.get_LineType(), ChannelWidth);
                    pLowerLinePoints = Channels.GetChannelArray2Double(1, pLowerLinePoints, 0, lElements, tg.get_LineType(), ChannelWidth);
                    lUpperFlotCount = flot.GetFlotCountDouble(pUpperLinePoints, arraysupport.getScaledSize(20, tg.get_LineThickness()), lElements);
                    lLowerFlotCount = flot.GetFlotCountDouble(pLowerLinePoints, arraysupport.getScaledSize(20, tg.get_LineThickness()), lElements);
                    lResult = lUpperFlotCount + lLowerFlotCount;
                    break;
                default:
                    //call GetCountersDouble for the remaining line types.
                    lResult = countsupport.GetCountersDouble(tg, lElements, pLinePoints, null);
                    break;
            }


            //clean up
            //pvblCounters = null;
            pLinePoints = null;
            pLowerLinePoints = null;
            pUpperLinePoints = null;
            pUpperLowerLinePoints = null;
            //GC.Collect();
        } catch (Exception exc) {
            ErrorLogger.LogException(_className ,"CGetLineCountDouble",
                    new RendererException("Failed inside CGetLineCount " + Integer.toString(tg.get_LineType()), exc));
        }
        return (lResult);
    }
    /**
     * Return true is the line type is a channel type
     * @param lineType line type
     * @return
     */
    public static int CIsChannel(int lineType) {
        int lResult = 0;
        try {
            switch (lineType) {
                case TacticalLines.CATK:
                case TacticalLines.CATKBYFIRE:
                case TacticalLines.LC:
                case TacticalLines.AIRAOA:
                case TacticalLines.AAAAA:
                case TacticalLines.MAIN:
                case TacticalLines.MAIN_STRAIGHT:
                case TacticalLines.SPT:
                case TacticalLines.SPT_STRAIGHT:
                case TacticalLines.UNSP:
                case TacticalLines.SFENCE:
                case TacticalLines.DFENCE:
                case TacticalLines.DOUBLEA:
                case TacticalLines.LWFENCE:
                case TacticalLines.HWFENCE:
                case TacticalLines.SINGLEC:
                case TacticalLines.DOUBLEC:
                case TacticalLines.TRIPLE:
                case TacticalLines.CHANNEL:
                case TacticalLines.CHANNEL_FLARED:
                case TacticalLines.CHANNEL_DASHED:
                    lResult = 1;
                    break;
                default:
                    lResult = 0;
                    break;
            }
        } 
        catch (Exception exc)
        {
            ErrorLogger.LogException(_className ,"CIsChannel",
                    new RendererException("Failed inside CIsChannel " + Integer.toString(lineType), exc));
        }
        return lResult;
    }
    private static String _client="";
    public static void setClient(String value)
    {
        _client=value;
        Channels.setClient(value);
    }
    public static String getClient()
    {
        return _client;
    }
//    public static void setMinLength(double value)
//    {
//        DISMSupport.setMinLength(value);
//        arraysupport.setMinLength(value);
//        countsupport.setMinLength(value);
//        return;
//    }
}
