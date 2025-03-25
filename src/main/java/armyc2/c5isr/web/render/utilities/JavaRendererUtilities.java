package armyc2.c5isr.web.render.utilities;


import armyc2.c5isr.renderer.utilities.MilStdAttributes;
import armyc2.c5isr.renderer.utilities.Modifiers;


import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * 
 */
@SuppressWarnings("unused")
public class JavaRendererUtilities {

    public static final String HOSTILE_FILL_COLOR = "FFFF8080";
    public static final String FRIENDLY_FILL_COLOR = "FF80E0FF";
    public static final String NEUTRAL_FILL_COLOR = "FFAAFFAA";
    public static final String UNKNOWN_FILL_COLOR = "FFFFFF80";

    /**
     * Converts ARGB string format to the Google used ABGR string format. Google
     * reverses the blue and red positioning.
     *
     * @param rgbString A color string of the format AARRGGBB in hex value.
     * @return the reverse of the input string in hex. The format should now be
     * AABBGGRR
     */
    public static String ARGBtoABGR(String rgbString) {
        if(rgbString.length()==6)
        {
            String s="FF";
            rgbString=s.concat(rgbString);
        }

    	String bgrString = rgbString.toUpperCase(Locale.US);
    	
    	if(rgbString.length() == 8)
    	{
    		char[] c = rgbString.toCharArray();
    		char temp1 = c[2];
            char temp2 = c[3];
            c[2] = c[6];
            c[3] = c[7];
            c[6] = temp1;
            c[7] = temp2;
            bgrString = new String(c);
    	}
    	else if(rgbString.length() == 6)
    	{
    		char[] c = rgbString.toCharArray();
    		char temp1 = c[0];
            char temp2 = c[1];
            c[0] = c[4];
            c[1] = c[5];
            c[4] = temp1;
            c[5] = temp2;
            bgrString = "FF" + new String(c);
            //bgrString = "FF" + bgrString;
    	}
    	else
    	{
    		System.err.println("JavaRendererUtilties.ARGBtoABGR(): " + "\"" + String.valueOf(rgbString) + "\" is not a 6 or 8 character String in the format of RRGGBB or AARRGGBB");
    	}

        return bgrString;
    }

    /**
     * Returns a symbolId with just the identifiable symbol Id pieces. All
     * variable information is returned as '*'. For example, a boundary,
     * "GFGPGLB----KUSX" returns "G*G*GLB---****X";
     *
     * @param symbolCode A 15 character symbol ID.
     * @return The normalized SymbolCode.
     * @deprecated
     */
    public static String normalizeSymbolCode(String symbolCode) {

        String newSymbolCode = symbolCode;

        if (symbolCode.startsWith("G") || symbolCode.startsWith("S")) {
            // Remove Affiliation
            newSymbolCode = newSymbolCode.substring(0, 1) + '*' + newSymbolCode.substring(2);
            // Remove planned/present field
            newSymbolCode = newSymbolCode.substring(0, 3) + '*' + newSymbolCode.substring(4);
            // Remove echelon, special code and country codes
            newSymbolCode = newSymbolCode.substring(0, 10) + "****" + newSymbolCode.substring(14);
        }

        // If a unit replace last character with *.
        if (symbolCode.startsWith("S")) {
            newSymbolCode = newSymbolCode.substring(0, 14) + '*';
        }

        return newSymbolCode;
    }
    
    public static void addAltModeToModifiersString(Map<String,String> attributes, String altMode)
    {
    	if(altMode.equals("relativeToGround"))
            attributes.put(MilStdAttributes.AltitudeMode, "AGL");
        else if(altMode.equals("absolute"))
        	attributes.put(MilStdAttributes.AltitudeMode, "AMSL");
    }

    /**
     *
     * @param SymbolInfo something like
     * "SymbolID?LineColor=0x000000&amp;FillColor=0xFFFFFF&amp;size=35"
     */
    public static Map<String, String> createParameterMapFromURL(String SymbolInfo) {
        Map<String, String> modifiers = new HashMap<String, String>();
        String symbolID = null;
        String parameters = null;
        String key = null;
        String value = null;
        String arrParameters[] = null;
        String arrKeyValue[] = null;
        String temp = null;
        int questionIndex = SymbolInfo.lastIndexOf('?');

        try {
            if (questionIndex == -1) {
                symbolID = java.net.URLDecoder.decode(SymbolInfo, "UTF-8");
            } else {
                symbolID = java.net.URLDecoder.decode(SymbolInfo.substring(0, questionIndex), "UTF-8");
            }

        } catch (Exception exc) {
            System.err.println("Error parsing SymbolID");
            System.err.println(exc.getMessage());
        }

        try {   //build a map for the other createMilstdSymbol function to use
            //to build a milstd symbol.
            if (questionIndex > 0 && (questionIndex + 1 < SymbolInfo.length())) {
                parameters = SymbolInfo.substring(questionIndex + 1, SymbolInfo.length());
                arrParameters = parameters.split("&");
                int n = arrParameters.length;
                //for(int i = 0; i < arrParameters.length; i++)
                for (int i = 0; i < n; i++) {
                    arrKeyValue = arrParameters[i].split("=");
                    if (arrKeyValue.length == 2 && arrKeyValue[1] != null && arrKeyValue[1].equals("") == false) {

                        key = arrKeyValue[0];
                        value = arrKeyValue[1];

                        temp = java.net.URLDecoder.decode(value, "UTF-8");
                        modifiers.put(key.toUpperCase(), temp);

                        //System.out.println("key: " + key + " value: " + temp);
                    }
                }
            }
        } catch (Exception exc) {
            System.err.println("Error parsing \"" + key.toUpperCase() + "\" parameter from URL");
            System.err.println(exc.getMessage());
        }
        return modifiers;
    }

    /*
     * Try to turn a bad code into something renderable.
     *
     * @param symbolID
     * @return
     * @deprecated use SymbolUtilties.reconcileSymbolID() 9/5/2013
     */
    /*public static String ReconcileSymbolID(String symbolID) {
        StringBuilder sb = new StringBuilder("");
        char codingScheme = symbolID.charAt(0);

        if (symbolID.length() < 15) {
            while (symbolID.length() < 15) {
                symbolID += "-";
            }
        }
        if (symbolID.length() > 15) {
            symbolID = symbolID.substring(0, 14);
        }

        if (symbolID != null && symbolID.length() == 15) {
            if (codingScheme == 'S' || //warfighting
                    codingScheme == 'I' ||//sigint
                    codingScheme == 'O' ||//stability operation
                    codingScheme == 'E')//emergency management
            {
                sb.append(codingScheme);

                if (SymbolUtilities.hasValidAffiliation(symbolID) == false) {
                    sb.append('U');
                } else {
                    sb.append(symbolID.charAt(1));
                }

                if (SymbolUtilities.hasValidBattleDimension(symbolID) == false) {
                    sb.append('Z');
                    sb.replace(0, 1, "S");
                } else {
                    sb.append(symbolID.charAt(2));
                }

                if (SymbolUtilities.hasValidStatus(symbolID) == false) {
                    sb.append('P');
                } else {
                    sb.append(symbolID.charAt(3));
                }

                sb.append("------");
                sb.append(symbolID.substring(10, 15));

            } else if (codingScheme == 'G')//tactical
            {
                sb.append(codingScheme);

                if (SymbolUtilities.hasValidAffiliation(symbolID) == false) {
                    sb.append('U');
                } else {
                    sb.append(symbolID.charAt(1));
                }

                sb.append('G');

                if (SymbolUtilities.hasValidStatus(symbolID) == false) {
                    sb.append('P');
                } else {
                    sb.append(symbolID.charAt(3));
                }

                sb.append("GPP---");//return an action point
                sb.append(symbolID.substring(10, 15));

            } else if (codingScheme == 'W')//weather
            {//no default weather graphic
                return "SUZP-----------";//unknown
            } else//bad codingScheme
            {
                sb.append('S');
                if (SymbolUtilities.hasValidAffiliation(symbolID) == false) {
                    sb.append('U');
                } else {
                    sb.append(symbolID.charAt(1));
                }

                if (SymbolUtilities.hasValidBattleDimension(symbolID) == false) {
                    sb.append('Z');
                } else {
                    sb.append(symbolID.charAt(2));
                }

                if (SymbolUtilities.hasValidStatus(symbolID) == false) {
                    sb.append('P');
                } else {
                    sb.append(symbolID.charAt(3));
                }

                sb.append("------");
                sb.append(symbolID.substring(10, 15));
            }
        } else {
            return "SUZP-----------";//unknown
        }

        return sb.toString();

    }//*/

    /**
     * Checks symbolID and if the relevant modifiers are present
     *
     * @param symbolCode
     * @param modifiers
     * @return
     * @deprecated
     */
    public static boolean is3dSymbol(String symbolCode, Map<String,String> modifiers) {
        boolean returnValue = false;

        try {
            String symbolId = symbolCode.substring(4, 10);

            if (symbolId.equals("ACAI--") || // Airspace Coordination Area Irregular
                    symbolId.equals("ACAR--") || // Airspace Coordination Area Rectangular
                    symbolId.equals("ACAC--") || // Airspace Coordination Area Circular
                    symbolId.equals("AKPC--") || // Kill box circular
                    symbolId.equals("AKPR--") || // Kill box rectangular
                    symbolId.equals("AKPI--") || // Kill box irregular
                    symbolId.equals("ALC---") || // Air corridor
                    symbolId.equals("ALM---") || // 
                    symbolId.equals("ALS---") || // SAAFR
                    symbolId.equals("ALU---") || // UAV
                    symbolId.equals("ALL---") || // Low level transit route
                    symbolId.equals("AAR---")
                    || symbolId.equals("AAF---")
                    || symbolId.equals("AAH---")
                    || symbolId.equals("AAM---") || // MEZ
                    symbolId.equals("AAML--") || // LOMEZ
                    symbolId.equals("AAMH--")) {

                try {
                    if (modifiers != null) {

                        // These guys store array values.  Put in appropriate data strucutre
                        // for MilStdSymbol.
                        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                            String[] altitudes = modifiers.get(Modifiers.X_ALTITUDE_DEPTH).split(",");
                            if (altitudes.length < 2) {
                                returnValue = false;
                            } else {
                                returnValue = true;
                            }
                        }

                    }
                } catch (Exception exc) {
                    System.err.println(exc.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return returnValue;
    }

    /**
     * Determines if a String represents a valid number
     *
     * @param text
     * @return "1.56" == true, "1ab" == false
     */
    public static boolean isNumber(String text) {
        if (text != null && text.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Takes a throwable and puts it's stacktrace into a string.
     *
     * @param thrown
     * @return
     */
    public static String getStackTrace(Throwable thrown) {
        try {
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            thrown.printStackTrace(printWriter);
            return writer.toString();
        } catch (Exception exc) {
            //System.out.println("JavaRendererUtilties.getStackTrace()");
            //return "Error - couldn't retrieve stack trace";
            return "";
        }
    }

    public static Point2D getEndPointWithAngle(Point2D ptStart,
                                               //Point2D pt1,
                                               //Point2D pt2,
                                               double angle,
                                               double distance) {
        double newX = 0;
        double newY = 0;
        Point2D pt = new Point2D.Double();
        try {
            //first get the angle psi between pt0 and pt1
            double psi = 0;//Math.atan((pt1.y - pt0.y) / (pt1.x - pt0.x));
            //double psi = Math.atan((ptStart.getY() - ptStart.getY()) / (ptStart.getX() - (ptStart.getX()+100)));
            //convert alpha to radians
            double alpha1 = Math.PI * angle / 180;

            //theta is the angle of extension from the x axis
            double theta = psi + alpha1;
            //dx is the x extension from pt2
            double dx = distance * Math.cos(theta);
            //dy is the y extension form pt2
            double dy = distance * Math.sin(theta);
            newX = ptStart.getX() + dx;
            newY = ptStart.getY() + dy;

            pt.setLocation(newX, newY);
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        return pt;
    }

    /**
     *
     * @param latitude1
     * @param longitude1
     * @param latitude2
     * @param longitude2
     * @param unitOfMeasure meters, kilometers, miles, feet, yards, nautical,
     * nautical miles.
     * @return
     */
    public static double measureDistance(double latitude1, double longitude1, double latitude2, double longitude2, String unitOfMeasure) {
        // latitude1,latitude2 = latitude, longitude1,longitude2 = longitude
        //Radius is 6378.1 (km), 3963.1 (mi), 3443.9 (nm

        double distance = -1,
                rad;

        String uom = unitOfMeasure.toLowerCase();

        if (uom.equals("meters")) {
            rad = 6378137;
        } else if (uom.equals("kilometers")) {
            rad = 6378.137;
        } else if (uom.equals("miles")) {
            rad = 3963.1;
        } else if (uom.equals("feet")) {
            rad = 20925524.9;
        } else if (uom.equals("yards")) {
            rad = 6975174.98;
        } else if (uom.equals("nautical")) {
            rad = 3443.9;
        } else if (uom.equals("nautical miles")) {
            rad = 3443.9;
        } else {
            return -1.0;
        }

        latitude1 = latitude1 * (Math.PI / 180);
        latitude2 = latitude2 * (Math.PI / 180);
        longitude1 = longitude1 * (Math.PI / 180);
        longitude2 = longitude2 * (Math.PI / 180);
        distance = (Math.acos(Math.cos(latitude1) * Math.cos(longitude1) * Math.cos(latitude2) * Math.cos(longitude2) + Math.cos(latitude1) * Math.sin(longitude1) * Math.cos(latitude2) * Math.sin(longitude2) + Math.sin(latitude1) * Math.sin(latitude2)) * rad);

        return distance;
    }
}
