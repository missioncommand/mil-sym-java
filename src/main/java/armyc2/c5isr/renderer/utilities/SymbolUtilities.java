package armyc2.c5isr.renderer.utilities;



import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Has various utility functions for prcessing the symbol code.
 * See {@link SymbolID} for additional functions related to parsing the symbol code.
 */
public class SymbolUtilities {

    private static SimpleDateFormat dateFormatFront = new SimpleDateFormat("ddHHmmss", Locale.US);
    private static SimpleDateFormat dateFormatBack = new SimpleDateFormat("MMMyyyy", Locale.US);
    private static SimpleDateFormat dateFormatFull = new SimpleDateFormat("ddHHmmssZMMMyyyy", Locale.US);
    private static SimpleDateFormat dateFormatZulu = new SimpleDateFormat("Z", Locale.US);

    //this regex is from: https://docs.oracle.com/javase/7/docs/api/java/lang/Double.html
    private static final String Digits     = "(\\p{Digit}+)";
    private static final String HexDigits  = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    private static final String Exp        = "[eE][+-]?"+Digits;
    private static final String fpRegex    =
            ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
                    "[+-]?(" + // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from section 3.10.2 of
                    // The Java™ Language Specification.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.("+Digits+")("+Exp+")?)|"+

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");// Optional trailing "whitespace"

    private static final Pattern pIsNumber = Pattern.compile(fpRegex);

    /**
     * Determines if a String represents a valid number
     *
     * @param text {@link String}
     * @return "1.56" == true, "1ab" == false
     */
    public static boolean isNumber(String text)
    {
        return pIsNumber.matcher(text).matches();
    }


    /*private static String convert(int integer)
    {
        String hexAlphabet = "0123456789ABCDEF";
        String foo = "gfds" + "dhs";
        char char1 =  hexAlphabet.charAt((integer - integer % 16)/16);
        char char2 = hexAlphabet.charAt(integer % 16);
        String returnVal = String.valueOf(char1) + String.valueOf(char2);
        return returnVal;
    }

    public static String colorToHexString(Color color, Boolean withAlpha)
    {
        String hex = "";
        if(withAlpha == false)
        {
            hex = "#" + convert(color.getRed()) +
                    convert(color.getGreen()) +
                    convert(color.getBlue());
        }
        else
        {
            hex = "#" + convert(color.getAlpha()) +
                    convert(color.getRed()) +
                    convert(color.getGreen()) +
                    convert(color.getBlue());
        }
        return hex;
    }//*/

    /*private static String convert(int integer)
    {
        String hexAlphabet = "0123456789ABCDEF";
        String foo = "gfds" + "dhs";
        char char1 =  hexAlphabet.charAt((integer - integer % 16)/16);
        char char2 = hexAlphabet.charAt(integer % 16);
        String returnVal = String.valueOf(char1) + String.valueOf(char2);
        return returnVal;
    }

    public static String colorToHexString(Color color, Boolean withAlpha)
    {
        if(color != null) {
            String hex = "";
            if (withAlpha == false) {
                hex = "#" + convert(color.getRed()) +
                        convert(color.getGreen()) +
                        convert(color.getBlue());
            } else {
                hex = "#" + convert(color.getAlpha()) +
                        convert(color.getRed()) +
                        convert(color.getGreen()) +
                        convert(color.getBlue());
            }
            return hex;
        }
        else
            return null;
    }//*/


    /**
     * Converts a Java Date object into a properly formatted String for W or W1.
     * DDHHMMSSZMONYYYY
     * Field W: D = day, H = hour, M = minute, S = second, Z = Greenwich or local time, MON= month and Y = year.
     * @param time {@link Date}
     * @return {@link String}
     */
    public static String getDateLabel(Date time)
    {

        String modifierString = null;

        String zulu = "";
        zulu = dateFormatZulu.format(time);

        if (zulu != null && zulu.length() == 5)
        {

            if (zulu.startsWith("+"))//Integer.valueOf doesn't like '+'
            {
                zulu = zulu.substring(1, 3);
            }
            else
            {
                zulu = zulu.substring(0, 3);
            }

            int intZulu = Integer.valueOf(zulu);

            zulu = getZuluCharFromTimeZoneOffset(intZulu);
        }
        else
        {
            zulu = getZuluCharFromTimeZoneOffset(time);
        }

        modifierString = dateFormatFront.format(time) + zulu + dateFormatBack.format(time);

        return modifierString.toUpperCase();
    }

    /**
     * Given date, return character String representing which NATO time zone
     * you're in.
     *
     * @param time {@link Date}
     * @return {@link String}
     */
    private static String getZuluCharFromTimeZoneOffset(Date time)
    {
        TimeZone tz = TimeZone.getDefault();
        Date offset = new Date(tz.getOffset(time.getTime()));
        long lOffset = offset.getTime() / 3600000;//3600000 = (1000(ms)*60(s)*60(m))

        int hour = (int) lOffset;

        return getZuluCharFromTimeZoneOffset(hour);
    }

    /**
     * Given hour offset from Zulu return character String representing which
     * NATO time zone you're in.
     *
     * @param hour {@link Integer}
     * @return {@link String}
     */
    private static String getZuluCharFromTimeZoneOffset(int hour)
    {
        if (hour == 0)
        {
            return "Z";
        }
        else if (hour == -1)
        {
            return "N";
        }
        else if (hour == -2)
        {
            return "O";
        }
        else if (hour == -3)
        {
            return "P";
        }
        else if (hour == -4)
        {
            return "Q";
        }
        else if (hour == -5)
        {
            return "R";
        }
        else if (hour == -6)
        {
            return "S";
        }
        else if (hour == -7)
        {
            return "T";
        }
        else if (hour == -8)
        {
            return "U";
        }
        else if (hour == -9)
        {
            return "V";
        }
        else if (hour == -10)
        {
            return "W";
        }
        else if (hour == -11)
        {
            return "X";
        }
        else if (hour == -12)
        {
            return "Y";
        }
        else if (hour == 1)
        {
            return "A";
        }
        else if (hour == 2)
        {
            return "B";
        }
        else if (hour == 3)
        {
            return "C";
        }
        else if (hour == 4)
        {
            return "D";
        }
        else if (hour == 5)
        {
            return "E";
        }
        else if (hour == 6)
        {
            return "F";
        }
        else if (hour == 7)
        {
            return "G";
        }
        else if (hour == 8)
        {
            return "H";
        }
        else if (hour == 9)
        {
            return "I";
        }
        else if (hour == 10)
        {
            return "K";
        }
        else if (hour == 11)
        {
            return "L";
        }
        else if (hour == 12)
        {
            return "M";
        }
        else
        {
            return "-";
        }
    }

    /**
     * Determines if a symbol, based on it's symbol ID, can have the specified modifier/amplifier.
     * @param symbolID 30 Character {@link String}
     * @param modifier {@link Modifiers}
     * @return {@link Boolean}
     */
    public static Boolean hasModifier(String symbolID, String modifier)
    {
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);

        if(msi != null  && msi.getDrawRule() != DrawRules.DONOTDRAW)
        {
            ArrayList<String> mods = msi.getModifiers();

            if(mods != null && mods.contains(modifier))
                return true;
            else if(msi.getSymbolSet()== SymbolID.SymbolSet_ControlMeasure && modifier == Modifiers.AB_FEINT_DUMMY_INDICATOR)
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * Gets Basic Symbol ID which is the Symbol Set + Entity Code
     * @param id 30 Character {@link String}
     * @return 8 character {@link String} (Symbol Set + Entity Code)
     */
    public static String getBasicSymbolID(String id)
    {
        if(id.length() == 8)
        {
            return id;
        }
        else if(id.startsWith("B"))
            return id;
        else if(id.equals("octagon"))
            return id;
        else if (id.length() >= 20 && id.length() <= 30)
        {
            String key = id.substring(4, 6) + id.substring(10, 16);
            return key;
        }
        else if (id.length()==15)
        {
            return getBasicSymbolID2525C(id);
        }
        return id;
    }

    /**
     * Gets the basic Symbol ID for a 2525C symbol
     * S*F*GPU---*****
     * G*G*GPP---****X
     * @param strSymbolID 15 Character {@link String}
     * @return 15 Character {@link String}
     * @deprecated function will be removed
     */
    public static String getBasicSymbolID2525C(String strSymbolID)
    {
        if(strSymbolID != null && strSymbolID.length() == 15)
        {
            StringBuilder sb = new StringBuilder();
            char scheme = strSymbolID.charAt(0);
            if (scheme == 'G')
            {
                sb.append(strSymbolID.charAt(0));
                sb.append("*");
                sb.append(strSymbolID.charAt(2));
                sb.append("*");
                sb.append(strSymbolID.substring(4, 10));
                sb.append("****X");
            }
            else if (scheme != 'W' && scheme != 'B' && scheme != 'P')
            {
                sb.append(strSymbolID.charAt(0));
                sb.append("*");
                sb.append(strSymbolID.charAt(2));
                sb.append("*");
                sb.append(strSymbolID.substring(4, 10));
                sb.append("*****");
            }
            else
            {
                return strSymbolID;
            }
            return sb.toString();
        }
        return strSymbolID;
    }

    /**
     * Attempts to resolve a bad symbol ID into a value that can be found in {@link MSLookup}.
     * If it fails, it will return the symbol code for a invalid symbol which is displayed as
     * an inverted question mark (110098000010000000000000000000)
     * @param symbolID 30 character {@link String}
     * @return 30 character {@link String} representing the resolved symbol ID.
     */
    public static String reconcileSymbolID(String symbolID)
    {

        String newID = "";
        try {


            int v = SymbolID.getVersion(symbolID);
            if (v < SymbolID.Version_2525E)
                newID = String.valueOf(SymbolID.Version_2525Dch1);
            else
                newID = String.valueOf(SymbolID.Version_2525E);
            int c = SymbolID.getContext(symbolID);
            if (c > 2)
                newID += String.valueOf(SymbolID.StandardIdentity_Context_Reality);
            else
                newID += String.valueOf(c);
            int a = SymbolID.getAffiliation(symbolID);
            if (a > 6)
                newID += String.valueOf(SymbolID.StandardIdentity_Affiliation_Unknown);
            else
                newID += String.valueOf(a);
            int ss = SymbolID.getSymbolSet(symbolID);
            switch (ss) {
                case SymbolID.SymbolSet_Unknown:
                case SymbolID.SymbolSet_Air:
                case SymbolID.SymbolSet_AirMissile:
                case SymbolID.SymbolSet_SignalsIntelligence_Air:
                case SymbolID.SymbolSet_Space:
                case SymbolID.SymbolSet_SpaceMissile:
                case SymbolID.SymbolSet_SignalsIntelligence_Space:
                case SymbolID.SymbolSet_LandUnit:
                case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                case SymbolID.SymbolSet_LandEquipment:
                case SymbolID.SymbolSet_SignalsIntelligence_Land:
                case SymbolID.SymbolSet_LandInstallation:
                case SymbolID.SymbolSet_DismountedIndividuals:
                case SymbolID.SymbolSet_SeaSurface:
                case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
                case SymbolID.SymbolSet_SeaSubsurface:
                case SymbolID.SymbolSet_MineWarfare:
                case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                case SymbolID.SymbolSet_Activities:
                case SymbolID.SymbolSet_ControlMeasure:
                case SymbolID.SymbolSet_Atmospheric:
                case SymbolID.SymbolSet_Oceanographic:
                case SymbolID.SymbolSet_MeteorologicalSpace:
                case SymbolID.SymbolSet_CyberSpace:
                    newID += String.format("%02d",ss);
                    break;
                default:
                    newID += String.format("%02d", SymbolID.SymbolSet_Unknown);//String.valueOf(SymbolID.SymbolSet_Unknown);
            }

            int s = SymbolID.getStatus(symbolID);
            if (s > SymbolID.Status_Present_FullToCapacity)
                newID += String.valueOf(SymbolID.Status_Present);
            else
                newID += String.valueOf(s);

            newID += String.valueOf(SymbolID.getHQTFD(symbolID));//just add, won't get used if value bad
            newID += String.format("%02d",SymbolID.getAmplifierDescriptor(symbolID));//just add, won't get used if value bad

            int ec = SymbolID.getEntityCode(symbolID);

            if (ec == 0)
                newID += "000000";//root symbol for symbol set
            else if (SVGLookup.getInstance().getSVGLInfo(SVGLookup.getMainIconID(newID + ec + "0000"),v) == null) {
                //set to invalid symbol since we couldn't find it in the lookup
                newID = SymbolID.setSymbolSet(newID, 98);
                newID += 100000;
            }
            else
                newID += String.format("%06d",ec);//we found it so add the entity code

            //newID += SymbolID.getMod1ID(symbolID);//just add, won't get used if value bad
            //newID += SymbolID.getMod2ID(symbolID);//just add, won't get used if value bad
            newID += symbolID.substring(16);//just add, won't get used if value bad
        }
        catch(Exception exc)
        {
            newID = "110098000010000000000000000000";//invalid symbol
        }

        return newID;
    }

    /**
     * Gets line color used if no line color has been set. The color is specified based on the affiliation of
     * the symbol and whether it is a unit or not.
     * @param symbolID 30 character {@link String}
     * @return {@link Color}
     */
    public static Color getLineColorOfAffiliation(String symbolID)
    {
        Color retColor = null;

        int symbolSet = SymbolID.getSymbolSet(symbolID);
        int set = SymbolID.getSymbolSet(symbolID);
        int affiliation = SymbolID.getAffiliation(symbolID);
        int symStd = SymbolID.getVersion(symbolID);
        int entityCode = SymbolID.getEntityCode(symbolID);

        try
        {
            // We can't get the line color if there is no symbol id, since that also means there is no affiliation
            if((symbolID == null) || (symbolID.equals("")))
            {
                return retColor;
            }

            if(symbolSet == SymbolID.SymbolSet_ControlMeasure)
            {
                int entity = SymbolID.getEntity(symbolID);
                int entityType = SymbolID.getEntityType(symbolID);
                int entitySubtype = SymbolID.getEntitySubtype(symbolID);


                if(SymbolUtilities.isGreenProtectionGraphic(entity, entityType, entitySubtype))
                {
                    //Obstacles/Protection Graphics, some are green obstacles and we need to
                    //check for those.
                    retColor = new Color(0,166,81);//Color.GREEN;
                }
                //just do color by affiliation if no other color has been set yet.
                if(retColor == null)
                {
                    switch (affiliation) {
                        case SymbolID.StandardIdentity_Affiliation_Friend:
                        case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                            retColor = AffiliationColors.FriendlyGraphicLineColor;//Color.BLACK;//0x000000;	// Black
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                            retColor = AffiliationColors.HostileGraphicLineColor;//Color.RED;//0xff0000;	// Red
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                            if(symStd >= SymbolID.Version_2525E)
                                retColor = AffiliationColors.SuspectGraphicLineColor;//255,188,1
                            else
                                retColor = AffiliationColors.HostileGraphicLineColor;//Color.RED;//0xff0000;	// Red
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Neutral:
                            retColor = AffiliationColors.NeutralGraphicLineColor;//Color.GREEN;//0x00ff00;	// Green
                            break;
                        default:
                            retColor = AffiliationColors.UnknownGraphicLineColor;//Color.YELLOW;//0xffff00;	// Yellow
                            break;
                    }
                }
            }
            else if (set >= 45 && set <= 47)//METOC
            {
                // If not black then color will be set in clsMETOC.SetMeTOCProperties()
                retColor = Color.BLACK;
;           }
            else if (set == SymbolID.SymbolSet_MineWarfare && (RendererSettings.getInstance().getSeaMineRenderMethod() == RendererSettings.SeaMineRenderMethod_MEDAL))
            {
                if(!(entityCode == 110600 || entityCode == 110700))
                {
                    switch(affiliation)
                    {
                        case SymbolID.StandardIdentity_Affiliation_Friend:
                        case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                            retColor = AffiliationColors.FriendlyUnitFillColor;//0x00ffff;	// Cyan
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                            retColor = AffiliationColors.HostileGraphicLineColor;//Color.RED;//0xff0000;	// Red
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                            if(symStd >= SymbolID.Version_2525E)
                                retColor = AffiliationColors.SuspectGraphicLineColor;//255,188,1
                            else
                                retColor = AffiliationColors.HostileGraphicLineColor;//Color.RED;//0xff0000;	// Red
                            break;
                        case SymbolID.StandardIdentity_Affiliation_Neutral:
                            retColor = AffiliationColors.NeutralUnitFillColor;//0x7fff00;	// Light Green
                            break;
                        default://unknown, pending, everything else
                            retColor = AffiliationColors.UnknownUnitFillColor;//new Color(255,250, 205); //0xfffacd;	// LemonChiffon 255 250 205
                            break;
                    }
                }
                else
                {
                    retColor = Color.BLACK;
                }
            }
            else//everything else
            {
                //stopped doing check because all warfighting
                //should have black for line color.
                retColor = Color.BLACK;
            }
        }
        catch(Exception e)
        {
            // Log Error
            ErrorLogger.LogException("SymbolUtilities", "getLineColorOfAffiliation", e);
            //throw e;
        }    // End catch
        return retColor;
    }    // End get LineColorOfAffiliation

    /**
     * For Control Measures, returns the default color for a symbol when it differs from the
     * affiliation line color.  If there is no default color, returns the value from {@link #getLineColorOfAffiliation(String)}
     * @param symbolID 30 Character {@link String}
     * @return {@link Color}
     */
    public static Color getDefaultLineColor(String symbolID) {
        try {
            if (symbolID == null || symbolID.equals("")) {
                return null;
            }

            int symbolSet = SymbolID.getSymbolSet(symbolID);
            int entityCode = SymbolID.getEntityCode(symbolID);
            int version = SymbolID.getVersion(symbolID);

            if (symbolSet == SymbolID.SymbolSet_ControlMeasure) {
                if (entityCode == 200600) {
                    return Color.WHITE;
                } else if (entityCode == 200700) {
                    return new Color(51, 136, 136);
                } else if (entityCode == 200101) {
                    return new Color(255, 155, 0);
                } else if (entityCode == 200201 || entityCode == 200202) {
                    return new Color(85, 119, 136);
                } else if (version >= SymbolID.Version_2525E &&
                        (entityCode == 132100 || //key terrain
                                entityCode == 282001 || //Tower, Low
                                entityCode == 282002 || //Tower, High
                                entityCode == 282003)) { // Overhead wire
                    return new Color(128, 0, 128);//purple
                }
            }
        } catch (Exception e) {
            ErrorLogger.LogException("SymbolUtilities", "getDefaultLineColor", e);
        }
        return getLineColorOfAffiliation(symbolID);
    }

    /**
     * Checks if a symbol should be filled by default
     * 
     * @param strSymbolID The 20 digit representation of the 2525D symbol
     * @return true if there is a default fill
     */
    public static boolean hasDefaultFill(String strSymbolID) {
        int ec = SymbolID.getEntityCode(strSymbolID);
        switch (ec) {
            case 200101:
            case 200201:
            case 200202:
            case 200600:
            case 200700:
                return true;
            default:
                return !SymbolUtilities.isTacticalGraphic(strSymbolID);
        }
    }

    /**
     * Determines if the symbol is a tactical graphic
     *
     * @param strSymbolID 30 Character {@link String}
     * @return true if symbol set is 25 (control measure), or is a weather graphic
     */
    public static boolean isTacticalGraphic(String strSymbolID) {
        try {
            int ss = SymbolID.getSymbolSet(strSymbolID);

            if(ss == SymbolID.SymbolSet_ControlMeasure || isWeather(strSymbolID)) {
                return true;
            }
        }
        catch (Exception e) {
            ErrorLogger.LogException("SymbolUtilities", "getFillColorOfAffiliation", e);
        }
        return false;
    }

    /**
     * Determines if the Symbol can be rendered as a multipoint graphic and not just as an icon
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isMultiPoint(String symbolID)
    {
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
        if (msi == null) {
            return false;
        }
        int drawRule = msi.getDrawRule();
        int ss = msi.getSymbolSet();
        if(ss != SymbolID.SymbolSet_ControlMeasure && ss != SymbolID.SymbolSet_Oceanographic && ss != SymbolID.SymbolSet_Atmospheric && ss != SymbolID.SymbolSet_MeteorologicalSpace)
        {
            return false;
        }
        else if (ss == SymbolID.SymbolSet_ControlMeasure)
        {
            if(msi.getMaxPointCount() > 1)
                return true;
            else if((drawRule < DrawRules.POINT1 || drawRule > DrawRules.POINT16 || drawRule == DrawRules.POINT12) &&
                drawRule != DrawRules.DONOTDRAW && drawRule != DrawRules.AREA22)
            {
                return true;
            }
            else
                return false;
        }
        else if(ss == SymbolID.SymbolSet_Oceanographic || ss == SymbolID.SymbolSet_Atmospheric || ss == SymbolID.SymbolSet_MeteorologicalSpace)
        {
            if(msi.getMaxPointCount() > 1)
                return true;
            else
                return false;
        }
        return false;
    }

    public static boolean isActionPoint(String symbolID)
    {
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
        if(msi.getDrawRule()==DrawRules.POINT1)
        {
            int ec = SymbolID.getEntityCode(symbolID);
            if(ec != 131300 && ec != 131301 && ec != 182600 && ec != 212800)
                return true;
        }
        return false;
    }


    /**
     * Control Measures and Tactical Graphics that have labels but not with the Action Point layout
     * @param strSymbolID 30 Character {@link String}
     * @return {@link Boolean}
     * @deprecated see {@link #isSPWithSpecialModifierLayout(String)}
     */
    public static boolean isTGSPWithSpecialModifierLayout(String strSymbolID)
    {
        try
        {
            int ss = SymbolID.getSymbolSet(strSymbolID);
            int entityCode = SymbolID.getEntityCode(strSymbolID);
            if(ss == SymbolID.SymbolSet_ControlMeasure) //|| isWeather(strSymbolID)) {
            {
                if(SymbolUtilities.isCBRNEvent(strSymbolID))
                    return true;

                if(SymbolUtilities.isSonobuoy(strSymbolID))
                    return true;

                switch (entityCode)
                {
                    case 130500: //contact point
                    case 130700: //decision point
                    case 212800: //harbor
                    case 131300: //point of interest
                    case 131800: //waypoint
                    case 240900: //fire support station
                    case 180100: //Air Control point
                    case 180200: //Communications Check point
                    case 160300: //T (target reference point)
                    case 240601: //ap,ap1,x,h (Point/Single Target)
                    case 240602: //ap (nuclear target)
                    case 270701: //static depiction
                    case 282001: //tower, low
                    case 282002: //tower, high
                        return true;
                    default:
                        return false;
                }
            }
            else if(ss == SymbolID.SymbolSet_Atmospheric)
            {
                switch (entityCode)
                {
                    case 162300: //Freezing Level
                    case 162200: //tropopause Level
                    case 110102: //tropopause Low
                    case 110202: //tropopause High
                        return true;
                    default:
                        return false;
                }
            }
        }
        catch (Exception e) {
            ErrorLogger.LogException("SymbolUtilities", "isTGSPWithSpecialModifierLayout", e);
        }
        return false;
    }

    /**
     * Returns the fill color for the symbol based on its affiliation
     * @param symbolID 30 Character {@link String}
     * @return {@link Color}
     */
    public static Color getFillColorOfAffiliation(String symbolID)
    {
        Color retColor = null;
        int entityCode = SymbolID.getEntityCode(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        int entityType = SymbolID.getEntityType(symbolID);
        int entitySubtype = SymbolID.getEntitySubtype(symbolID);

        int affiliation = SymbolID.getAffiliation(symbolID);

        try
        {
            // We can't get the fill color if there is no symbol id, since that also means there is no affiliation
            if ((symbolID == null) || (symbolID.equals(""))) {
                return retColor;
            }
            if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure) {
                switch (entityCode) {
                    case 200101:
                        retColor = new Color(255, 155, 0, (int) (.25 * 255));
                        break;
                    case 200201:
                    case 200202:
                    case 200600:
                        retColor = new Color(85, 119, 136, (int) (.25 * 255));
                        break;
                    case 200700:
                        retColor = new Color(51, 136, 136, (int) (.25 * 255));
                        break;
                }
            }
            else if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_MineWarfare &&
                    (RendererSettings.getInstance().getSeaMineRenderMethod() == RendererSettings.SeaMineRenderMethod_MEDAL) &&
                    (!(entityCode == 110600 || entityCode == 110700)))
            {
                retColor = new Color(0,0,0,0);//transparent
            }
            //just do color by affiliation if no other color has been set yet
            if (retColor == null) {
                switch(affiliation)
                {
                    case SymbolID.StandardIdentity_Affiliation_Friend:
                    case SymbolID.StandardIdentity_Affiliation_AssumedFriend:
                        retColor = AffiliationColors.FriendlyUnitFillColor;//0x00ffff;	// Cyan
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Hostile_Faker:
                        retColor = AffiliationColors.HostileUnitFillColor;//0xfa8072;	// Salmon
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Suspect_Joker:
                        if(SymbolID.getVersion(symbolID) >= SymbolID.Version_2525E)
                            retColor = AffiliationColors.SuspectGraphicFillColor;//255,229,153
                        else
                            retColor = AffiliationColors.HostileGraphicFillColor;//Color.RED;//0xff0000;	// Red
                        break;
                    case SymbolID.StandardIdentity_Affiliation_Neutral:
                        retColor = AffiliationColors.NeutralUnitFillColor;//0x7fff00;	// Light Green
                        break;
                    default://unknown, pending, everything else
                        retColor = AffiliationColors.UnknownUnitFillColor;//new Color(255,250, 205); //0xfffacd;	// LemonChiffon 255 250 205
                        break;
                }
            }
        } // End try
        catch (Exception e)
        {
            // Log Error
            ErrorLogger.LogException("SymbolUtilities", "getFillColorOfAffiliation", e);
            //throw e;
        }    // End catch

        return retColor;
    }    // End FillColorOfAffiliation

    /**
     *
     * @param symbolID 30 Character {@link String}
     * @param modifier {@link Modifiers}
     * @return {@link Boolean}
     * @deprecated see {@link #hasModifier(String, String)}
     */
    public static Boolean canSymbolHaveModifier(String symbolID, String modifier)
    {
        return hasModifier(symbolID, modifier);
    }

    /**
     * Checks if the Symbol Code has FDI set.
     * Does not check if the symbol can have an FDI.
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean hasFDI(String symbolID)
    {
        int hqtfd = SymbolID.getHQTFD(symbolID);
        if(hqtfd == SymbolID.HQTFD_FeintDummy
                || hqtfd == SymbolID.HQTFD_FeintDummy_TaskForce
                || hqtfd == SymbolID.HQTFD_FeintDummy_Headquarters
                || hqtfd == SymbolID.HQTFD_FeintDummy_TaskForce_Headquarters)
        {
            return true;
        }
        else
            return false;
    }

    /***
     * Returns true if graphic is protection graphic (obstacles which render green)
     * Assumes control measure symbol code where SS == 25
     * @param entity {@link Integer}
     * @param entityType {@link Integer}
     * @param entitySubtype {@link Integer}
     * @return {@link Boolean}
     */
    public static boolean isGreenProtectionGraphic(int entity, int entityType, int entitySubtype)
    {
        if(entity >= 27 && entity <= 29)//Protection Areas, Points and Lines
        {
            if(entity == 27)
            {
                if(entityType > 0 && entityType <= 5)
                    return true;
                else if(entityType == 7 || entityType == 8 || entityType == 10 || entityType == 12)
                {
                    return true;
                }
                else
                    return false;
            }
            else if(entity == 28)
            {
                if(entityType > 0 && entityType <= 7)
                    return true;
                if(entityType == 19)
                    return true;
                else
                    return false;
            }
            else if(entity == 29)
            {
                if(entityType >= 01 && entityType <= 05)
                    return true;
                else
                    return false;
            }
        }
        else
        {
            return false;
        }
        return false;
    }

    /**
     * Returns true if graphic is protection graphic (obstacles which render green)
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isGreenProtectionGraphic(String symbolID) {
        if (SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure) {
            return SymbolUtilities.isGreenProtectionGraphic(SymbolID.getEntity(symbolID), SymbolID.getEntityType(symbolID), SymbolID.getEntitySubtype(symbolID));
        } else {
            return false;
        }
    }

    /**
     * Returns true if Symbol ID represents a chemical, biological, radiological or nuclear incident.
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isCBRNEvent(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);

        if(ss == SymbolID.SymbolSet_ControlMeasure) {
            switch (ec)
            {
                case 281300:
                case 281400:
                case 281500:
                case 281600:
                case 281700:
                    return true;
                default:
            }
        }
        return false;
    }

    /**
     * Returns true if Symbol ID represents a Sonobuoy.
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isSonobuoy(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int e = SymbolID.getEntity(symbolID);
        int et = SymbolID.getEntityType(symbolID);
        if(ss == 25 && e == 21 && et == 35)
            return true;
        else
            return false;
    }

    /**
     * Obstacles are generally required to have a green line color
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     * @deprecated see {@link #isGreenProtectionGraphic(String)}
     */
    public static boolean isObstacle(String symbolID)
    {

        if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure &&
                SymbolID.getEntity(symbolID) == 27)
        {
            return true;
        }
        else
            return false;
    }

    /**
     * Return true if symbol is from the Atmospheric, Oceanographic or Meteorological Space Symbol Sets.
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isWeather(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        if(ss >= SymbolID.SymbolSet_Atmospheric && ss <= SymbolID.SymbolSet_MeteorologicalSpace)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol has the HQ staff indicated by the symbol ID
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isHQ(String symbolID)
    {
        int hq = SymbolID.getHQTFD(symbolID);
        if(SymbolUtilities.hasModifier(symbolID, Modifiers.S_HQ_STAFF_INDICATOR) &&
            (hq == SymbolID.HQTFD_FeintDummy_Headquarters ||
                hq == SymbolID.HQTFD_Headquarters  ||
                hq == SymbolID.HQTFD_FeintDummy_TaskForce_Headquarters ||
                hq == SymbolID.HQTFD_TaskForce_Headquarters))
            return true;
        else
            return false;
    }

    /**
     * Checks if this is a single point control measure or meteorological graphic with a unique layout.
     * Basically anything that's not an action point style graphic with modifiers
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean isSPWithSpecialModifierLayout(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);

        if(ss == SymbolID.SymbolSet_ControlMeasure)
        {
            switch(ec)
            {
                case 130500: //Control Point
                case 130700: //Decision Point
                case 131300: //Point of Interest
                case 131800: //Waypoint
                case 131900: //Airfield (AEGIS Only)
                case 132000: //Target Handover
                case 132100: //Key Terrain
                case 160300: //Target Point Reference
                case 180100: //Air Control Point
                case 180200: //Communications Check Point
                case 180600: //TACAN
                case 210300: //Defended Asset
                case 210600: //Air Detonation
                case 210800: //Impact Point
                case 211000: //Launched Torpedo
                case 212800: //Harbor
                case 213500: //Sonobuoy
                case 213501: //Ambient Noise Sonobuoy
                case 213502: //Air Transportable Communication (ATAC) (Sonobuoy)
                case 213503: //Barra (Sonobuoy)
                case 213504:
                case 213505:
                case 213506:
                case 213507:
                case 213508:
                case 213509:
                case 213510:
                case 213511:
                case 213512:
                case 213513:
                case 213514:
                case 213515:
                case 214900: //General Sea Subsurface Station
                case 215600: //General Sea Station
                case 217000: //Shore Control Station
                case 240601: //Point or Single Target
                case 240602: //Nuclear Target
                case 240900: //Fire Support Station
                case 250600: //Known Point
                case 270701: //Static Depiction
                case 282001: //Tower, Low
                case 282002: //Tower, High
                case 281300: //Chemical Event
                case 281400: //Biological Event
                case 281500: //Nuclear Event
                case 281600: //Nuclear Fallout Producing Event
                case 281700: //Radiological Event
                    return true;
                default:
                    return false;
            }
        }
        else if(ss == SymbolID.SymbolSet_Atmospheric)
        {
            switch(ec)
            {
                case 162300: //Freezing Level
                case 162200: //tropopause Level
                case 110102: //tropopause low
                case 110202: //tropopause high
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Gets the anchor point for single point Control Measure as the anchor point isn't always they center of the symbol.
     * @param symbolID 30 Character {@link String}
     * @param bounds {@link Rectangle2D} representing the bound of the core symbol in the image.
     * @return {@link Point} representing the point in the image that is the anchor point of the symbol.
     */
    public static Point getCMSymbolAnchorPoint(String symbolID, Rectangle2D bounds)
    {

        double centerX = bounds.getWidth()/2;
        double centerY = bounds.getHeight()/2;

        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        MSInfo msi = null;
        int drawRule = 0;

        //center/anchor point is always half width and half height except for control measures
        //and meteorological
        if(ss == SymbolID.SymbolSet_ControlMeasure)
        {
            drawRule = MSLookup.getInstance().getMSLInfo(symbolID).getDrawRule();
            switch(drawRule)//here we check the 'Y' value for the anchor point
            {
                case DrawRules.POINT1://action points //bottom center
                case DrawRules.POINT5://entry point
                case DrawRules.POINT6://ground zero
                case DrawRules.POINT7://missile detection point
                    centerY = bounds.getHeight();
                    break;
                case DrawRules.POINT4://drop point  //almost bottom and center
                    centerY = (bounds.getHeight() * 0.80);
                    break;
                case DrawRules.POINT10://Sonobuoy  //center of circle which isn't center of symbol
                    centerY = (bounds.getHeight() * 0.75);
                    break;
                case DrawRules.POINT13://booby trap  //almost bottom and center
                    centerY = (bounds.getHeight() * 0.74);
                    break;
                case DrawRules.POINT15://Marine Life  //center left
                    centerX = 0;
                    break;
                case DrawRules.POINT16://Tower  //circle at base of tower
                    centerY = (bounds.getHeight() * 0.87);
                    break;
                case DrawRules.POINT2://Several different symbols
                    if(ec == 280500)//Wide Area Antitank Mine
                        centerY = (bounds.getHeight() * 0.35);
                    else if(ec == 280400)//Antitank Mine w/ Anti-handling Device
                        centerY = (bounds.getHeight() * 0.33);
                    else if(ec == 280200)//Antipersonnel Mine
                        centerY = (bounds.getHeight() * 0.7);
                    else if(ec == 280201)//Antipersonnel Mine with Directional Effects
                        centerY = (bounds.getHeight() * 0.65);
                    else if (ec == 219000)//Sea Anomaly
                        centerY = (bounds.getHeight() * 0.7);
                    else if (ec == 212500)//Electromagnetic - Magnetic Anomaly Detections (MAD)
                        centerY = (bounds.getHeight() * 0.4);
                    break;
                default:
            }

            switch (ec)
            //have to adjust center X as some graphics have integrated text outside the symbol
            {
                case 180400: //Pickup Point (PUP)
                    centerX = bounds.getWidth() * 0.3341;
                    break;
                case 240900: //Fire Support Station
                    centerX = bounds.getWidth() * 0.38;
                    break;
                case 280201: //Antipersonnel Mine with Directional Effects
                    centerX = bounds.getWidth() * 0.43;
                    break;
            }
        }

        return new Point(Math.round((float)centerX),Math.round((float)centerY));
    }


    /**
     * Returns true if the symbol is an installation
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */

    public static Boolean isInstallation(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        if(ss == SymbolID.SymbolSet_LandInstallation && entity == 11)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol is from an air based symbol set
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isAir(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        if(ss == SymbolID.SymbolSet_Air ||
                ss == SymbolID.SymbolSet_AirMissile ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_Air)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol is from a space based symbol set
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isSpace(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        if(ss == SymbolID.SymbolSet_Space ||
                ss == SymbolID.SymbolSet_SpaceMissile ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_Space)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol is from a land based symbol set
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isLand(String symbolID)
    {
        int ss = SymbolID.getSymbolSet(symbolID);
        int entity = SymbolID.getEntity(symbolID);
        if(ss == SymbolID.SymbolSet_LandUnit ||
                ss == SymbolID.SymbolSet_LandCivilianUnit_Organization ||
                ss == SymbolID.SymbolSet_LandEquipment ||
                ss == SymbolID.SymbolSet_LandInstallation ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_Land)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol ID has the task for indicator
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isTaskForce(String symbolID)
    {
        int hqtfd = SymbolID.getHQTFD(symbolID);
        if((hqtfd == SymbolID.HQTFD_TaskForce ||
                hqtfd == SymbolID.HQTFD_TaskForce_Headquarters ||
                hqtfd == SymbolID.HQTFD_FeintDummy_TaskForce ||
                hqtfd == SymbolID.HQTFD_FeintDummy_TaskForce_Headquarters) &&
                SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.B_ECHELON))
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol ID indicates the context is Reality
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isReality(String symbolID)
    {
        int c = SymbolID.getContext(symbolID);
        if(c == SymbolID.StandardIdentity_Context_Reality ||
                c == 3 || c == 4)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol ID indicates the context is Exercise
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isExercise(String symbolID)
    {
        int c = SymbolID.getContext(symbolID);
        if(c == SymbolID.StandardIdentity_Context_Exercise ||
                c == 5 || c == 6)
            return true;
        else
            return false;
    }

    /**
     * Returns true if the symbol ID indicates the context is Simulation
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static Boolean isSimulation(String symbolID)
    {
        int c = SymbolID.getContext(symbolID);
        if(c == SymbolID.StandardIdentity_Context_Simulation ||
                c == 7 || c == 8)
            return true;
        else
            return false;
    }



    /**
     * Reads the Symbol ID string and returns the text that represents the echelon
     * code.
     * @param echelon {@link Integer} from positions 9-10 in the symbol ID
     * See {@link SymbolID#getAmplifierDescriptor(String)}
     * @return {@link String} (23 (Army) would be "XXXX")
     */
    public static String getEchelonText(int echelon)
    {
        char[] dots = new char[3];
        dots[0] = (char)8226;
        dots[1] = (char)8226;
        dots[2] = (char)8226;
        String dot = new String(dots);
        String text = null;
        if(echelon == SymbolID.Echelon_Team_Crew)
        {
            text = (char) 216 + "";
        }
        else if(echelon == SymbolID.Echelon_Squad)
        {
            text = dot.substring(0, 1);
        }
        else if(echelon == SymbolID.Echelon_Section)
        {
            text = dot.substring(0, 2);
        }
        else if(echelon == SymbolID.Echelon_Platoon_Detachment)
        {
            text = dot;
        }
        else if(echelon == SymbolID.Echelon_Company_Battery_Troop)
        {
            text = "I";
        }
        else if(echelon == SymbolID.Echelon_Battalion_Squadron)
        {
            text = "II";
        }
        else if(echelon == SymbolID.Echelon_Regiment_Group)
        {
            text = "III";
        }
        else if(echelon == SymbolID.Echelon_Brigade)
        {
            text = "X";
        }
        else if(echelon == SymbolID.Echelon_Division)
        {
            text = "XX";
        }
        else if(echelon == SymbolID.Echelon_Corps_MEF)
        {
            text = "XXX";
        }
        else if(echelon == SymbolID.Echelon_Army)
        {
            text = "XXXX";
        }
        else if(echelon == SymbolID.Echelon_ArmyGroup_Front)
        {
            text = "XXXXX";
        }
        else if(echelon == SymbolID.Echelon_Region_Theater)
        {
            text = "XXXXXX";
        }
        else if(echelon == SymbolID.Echelon_Region_Command)
        {
            text = "++";
        }
        return text;
    }

    /**
     * Returns the Standard Identity Modifier based on the Symbol ID
     * @param symbolID 30 Character {@link String}
     * @return {@link String}
     */
    public static String getStandardIdentityModifier(String symbolID)
    {
        String textChar = null;
        int si = SymbolID.getStandardIdentity(symbolID);
        int context = SymbolID.getContext(symbolID);
        int affiliation = SymbolID.getAffiliation(symbolID);

        if(context == SymbolID.StandardIdentity_Context_Simulation)//Simulation
            textChar = "S";
        else if(context == SymbolID.StandardIdentity_Context_Exercise)
        {
            if(affiliation == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)//exercise Joker
                textChar = "J";
            else if(affiliation == SymbolID.StandardIdentity_Affiliation_Hostile_Faker)//exercise faker
                textChar = "K";
            else if(context == SymbolID.StandardIdentity_Context_Exercise)//exercise
                textChar = "X";
        }

        return textChar;
    }

    /**
     * Returens true if the unit has a rectangle frame
     * @param symbolID 30 Character {@link String}
     * @return {@link Boolean}
     */
    public static boolean hasRectangleFrame(String symbolID)
    {
        int affiliation = SymbolID.getAffiliation(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        if(ss != SymbolID.SymbolSet_ControlMeasure)
        {
            if (affiliation == SymbolID.StandardIdentity_Affiliation_Friend
                    || affiliation == SymbolID.StandardIdentity_Affiliation_AssumedFriend
                    || (SymbolID.getContext(symbolID)==SymbolID.StandardIdentity_Context_Exercise &&
                    (affiliation == SymbolID.StandardIdentity_Affiliation_Hostile_Faker
                            || affiliation == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)))
            {
                return true;
            }
            else
                return false;
        }
        else
            return false;
    }

    /**
     * Returns the height ratio for the unit specified by the symbol ID
     * Based on Figure 4 in 2525E.
     * @param symbolID 30 Character {@link String}
     * @return {@link Float}
     */
    public static float getUnitRatioHeight(String symbolID)
    {
        int ver = SymbolID.getVersion(symbolID);
        int aff = SymbolID.getAffiliation(symbolID);

        float rh = 0;

        if(ver < SymbolID.Version_2525E)
        {
            int ss = SymbolID.getSymbolSet(symbolID);

            if(aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                    aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rh = 1.44f;
                        break;
                    default:
                        rh=1.3f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Friend ||
                    aff == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rh = 1f;
                        break;
                    default:
                        rh=1.2f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rh = 1.1f;
                        break;
                    default:
                        rh=1.2f;
                }
            }
            else //UNKNOWN
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rh = 1.44f;
                        break;
                    default:
                        rh=1.3f;
                }
            }
        }
        else //2525E and up
        {
            String frameID = SVGLookup.getFrameID(symbolID);
            if(frameID.length()==6)
                aff = Integer.parseInt(frameID.substring(2,3));
            else //"octagon"
                return 1f;
            char fs = (frameID.charAt(3));

            if(aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                    aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rh = 1.44f;
                        break;
                    default:
                        rh=1.3f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Friend ||
                    aff == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rh = 1f;
                        break;
                    default:
                        rh=1.2f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rh = 1.1f;
                        break;
                    default:
                        rh=1.2f;
                }
            }
            else //UNKNOWN
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rh = 1.44f;
                        break;
                    default:
                        rh=1.3f;
                }
            }


        }

        return rh;
    }

    /**
     * Returns the width ratio for the unit specified by the symbol ID
     * Based on Figure 4 in 2525E.
     * @param symbolID 30 Character {@link String}
     * @return {@link Float}
     */
    public static float getUnitRatioWidth(String symbolID)
    {
        int ver = SymbolID.getVersion(symbolID);
        int aff = SymbolID.getAffiliation(symbolID);

        float rw = 0;

        if(ver < SymbolID.Version_2525E)
        {
            int ss = SymbolID.getSymbolSet(symbolID);

            if(aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                    aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rw = 1.44f;
                        break;
                    default:
                        rw=1.1f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Friend ||
                    aff == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rw = 1.5f;
                        break;
                    case SymbolID.SymbolSet_LandEquipment:
                        rw = 1.2f;
                        break;
                    default:
                        rw=1.1f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                rw = 1.1f;
            }
            else //UNKNOWN
            {
                switch (ss){
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandInstallation:
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    case SymbolID.SymbolSet_Activities:
                    case SymbolID.SymbolSet_CyberSpace:
                        rw = 1.44f;
                        break;
                    default:
                        rw=1.5f;
                }
            }
        }
        else //2525E and above
        {
            String frameID = SVGLookup.getFrameID(symbolID);
            if(frameID.length()==6)
                aff = Integer.parseInt(frameID.substring(2,3));
            else //"octagon"
                return 1f;
            char fs = (frameID.charAt(3));

            if(aff == SymbolID.StandardIdentity_Affiliation_Hostile_Faker ||
                    aff == SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rw = 1.44f;
                        break;
                    default:
                        rw=1.1f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Friend ||
                    aff == SymbolID.StandardIdentity_Affiliation_AssumedFriend)
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rw = 1.5f;
                        break;
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                        rw = 1.2f;
                        break;
                    default:
                        rw=1.1f;
                }
            }
            else if(aff == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                rw = 1.1f;
            }
            else //UNKNOWN
            {
                switch (fs){
                    case SymbolID.FrameShape_LandUnit:
                    case SymbolID.FrameShape_LandInstallation:
                    case SymbolID.FrameShape_LandEquipment_SeaSurface:
                    case SymbolID.FrameShape_Activity_Event:
                    case SymbolID.FrameShape_Cyberspace:
                        rw = 1.44f;
                        break;
                    default:
                        rw=1.5f;
                }
            }
        }

        return rw;
    }

}

