/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package armyc2.c5isr.renderer.utilities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Object that holds information on how to draw a multipoint symbol after {@link armyc2.c5isr.web.render.WebRenderer#RenderMultiPointAsMilStdSymbol(String, String, String, String, String, String, double, String, Map, Map)}  is called.
 */
public class MilStdSymbol
{

        //private SymbolDef _symbolDefinition = null;
    //private UnitDef _unitDefinition = null;
    /**
     * modifiers
     */
    private Map<String,String> _Properties = null;

    //for tactical graphics
    private ArrayList<Double> _X_Altitude = null;
    private ArrayList<Double> _AM_Distance = null;
    private ArrayList<Double> _AN_Azimuth = null;

    private String _symbolID = "";

    /**
     * unique ID for this symbol, for client use
     */
    private String _UUID = null;

    private ArrayList<ShapeInfo> _SymbolShapes;

    /**
     * collection of shapes for the modifiers
     */
    private ArrayList<ShapeInfo> _ModifierShapes;

    private ArrayList<Point2D> _Coordinates;

    private int _UnitSize = 50;
    private double _scale = 0;
    private Boolean _KeepUnitRatio = true;

    Integer _LineWidth = 3;
    Color _LineColor = null;
    Color _FillColor = null;
    Color _TextColor = null;
    Color _TextBackgroundColor = null;

    double _Rotation = 0.0;//DEGREES

    //outline singlepoint TGs
    boolean _Outline = false;
    //if null, renderer determines outline Color.
    Color _OutLineColor = null;
    int _OutLineWidth = 0;
    TexturePaint _tp = null;
    boolean _fs = true;

    int _patternFillType = 0;
    
    private static String _AltitudeMode = "";

    private static DistanceUnit _AltitudeUnit = null;

    private static DistanceUnit _DistanceUnit = null;

    private static boolean _useDashArray = true;

    private static boolean _hideOptionalLabels = false;

    private static boolean _DrawAffiliationModifierAsLabel = true;

    private static boolean _UseLineInterpolation = false;

    Object _Tag = null;

    /*
     * Used to hold metadata for each segment of the symbol for multi-point
     * symbols. Each segment can contain one object.
     */
		//private Map _segmentData;
		// Constants for dynamic properties
/*
     public static final String SYMBOL_ID = "Symbol ID";
     //public static final String SOURCE = "Source";
     //public static final String EDITOR_CLASS_TYPE = "Editor Class Type";
     public static final String URN = "URN";
     public static final String UIC = "UIC";
     public static final String ANGLE_OF_ROTATION = "Angle of Rotation";
     public static final String LENGTH = "Length";
     public static final String WIDTH = "Width";
     public static final String RADIUS = "Radius";
     public static final String SEGMENT_DATA = "Segment Data";
     */

    /*
     public static final String GEO_POINT = "point";
     public static final String GEO_LINE = "line";
     public static final String GEO_POLYGON = "area";
     public static final String GEO_TEXT = "text";
     public static final String GEO_CIRCLE = "circle";
     public static final String GEO_RECTANGLE = "rectangle";
     public static final String GEO_ARC = "arc";
     public static final String GEO_SQUARE = "square";
     */
    /*
     private static final String _COORDINATES = "Coordinates";
     private static final String _GEOMETRY = "Geometry";
     private static final String _FILL_COLOR = "Fill Color";
     private static final String _FILL_ALPHA = "Fill Alpha";
     private static final String _FILL_STYLE = "Fill Style";
     private static final String _LINE_WIDTH = "Line Width";
     private static final String _LINE_COLOR = "Line Color";
     private static final String _LINE_ALPHA = "Line Alpha";
     private static final String _TEXT_BACKGROUND_COLOR = "Background Color";
     private static final String _TEXT_FOREGROUND_COLOR = "Foreground Color";
     private static final String _USE_FILL = "Use Fill";
     */
    /*
     protected static const _COORDINATES:String = "Coordinates";
     protected static const _GEOMETRY:String = "Geometry";
     protected static const _FILL_COLOR:String = "Fill Color";
     protected static const _FILL_ALPHA:String = "Fill Alpha";
     private int _FILL_STYLE:String = "Fill Style";
     protected static const _LINE_WIDTH:String = 0;
     private Color _LINE_COLOR = Color.BLACK;
     private int _LINE_ALPHA:String = 0;
     private Color _TEXT_BACKGROUND_COLOR = Color.WHITE;
     private Color _TEXT_FOREGROUND_COLOR = Color.BLACK;
     private bool _USE_FILL:String = "Use Fill";*/

    /**
     * Creates a new MilStdSymbol.
     *
     * @param symbolID code, 20-30 digits long that represents the symbol
     * @param uniqueID for the client's use
     * @param modifiers use keys from Modifiers.
     * @param Coordinates
     * modifiers
     */
    public MilStdSymbol(String symbolID, String uniqueID, ArrayList<Point2D> Coordinates, Map<String,String> modifiers)
    {
        this(symbolID, uniqueID, Coordinates, modifiers, true);
    }

    /**
     *
     * @param symbolID code, 20-30 digits long that represents the symbol
     * @param uniqueID for the client's use
     * @param modifiers use keys from Modifiers.
     * @param Coordinates {@link ArrayList} of {@link Point2D} coordinates for the symbol
     * @param keepUnitRatio - default TRUE
     * modifiers
     */
    public MilStdSymbol(String symbolID, String uniqueID, ArrayList<Point2D> Coordinates, Map<String,String> modifiers, Boolean keepUnitRatio)
    {

        if (modifiers == null)
        {
            _Properties = new HashMap<String,String>();
        }
        else
        {
            _Properties = modifiers;
        }

        _UUID = uniqueID;
        setCoordinates(Coordinates);

        // Set the given symbol id
        setSymbolID(symbolID);

        // Set up default line fill and text colors
        setLineColor(SymbolUtilities.getDefaultLineColor(_symbolID));
        setTextColor(SymbolUtilities.getLineColorOfAffiliation(symbolID));
        //if(SymbolUtilities.isWarfighting(_symbolID))
        if (SymbolUtilities.hasDefaultFill(_symbolID))
        {
            setFillColor(SymbolUtilities.getFillColorOfAffiliation(_symbolID));
        }
            //if(SymbolUtilities.isNBC(_symbolID) && !(SymbolUtilities.isDeconPoint(symbolID)))
        //    setFillColor(SymbolUtilities.getFillColorOfAffiliation(_symbolID));

        _DrawAffiliationModifierAsLabel = RendererSettings.getInstance().getDrawAffiliationModifierAsLabel();

        _UseLineInterpolation = RendererSettings.getInstance().getUseLineInterpolation();

        _KeepUnitRatio = keepUnitRatio;
    }

    public TexturePaint getFillStyle()
    {
        return _tp;
    }

    public void setFillStyle(TexturePaint value)
    {
        _tp = value;
    }

    public boolean getUseFillPattern()
    {
        return _fs;
    }

    public void setUseFillPattern(boolean value)
    {
        _fs = value;
    }

    /**
     * @deprecated
     */
    public int getPatternFillType()
    {
        return _patternFillType;
    }

    /**
     * 0=Solid, 2=ForwardHatch, 3=BackwardHatch, 4=verticalHatch, 5=horizonalHatch, 8=CrossHatch
     * Only affects Basic Shapes.  Will not apply to MilStd Symbology so as to not confuse some
     * symbols with others.
     * @param value {@link Integer}
     *
     * @deprecated
     */
    public void setPatternFillType(int value)
    {
        _patternFillType = value;
    }

    public String getAltitudeMode()
    {
        return _AltitudeMode;
    }

    public void setAltitudeMode(String value)
    {
        _AltitudeMode = value;
    }

    public DistanceUnit getAltitudeUnit(){
        return _AltitudeUnit;
    }

    public void setAltitudeUnit(DistanceUnit unit){
        _AltitudeUnit = unit;
    }

    public DistanceUnit getDistanceUnit(){
        return _DistanceUnit;
    }

    public void setDistanceUnit(DistanceUnit unit){
        _DistanceUnit = unit;
    }
    
    public boolean getUseDashArray()
    {
        return _useDashArray;
    }

    public void setUseDashArray(boolean value)
    {
        _useDashArray = value;
    }

    public boolean getHideOptionalLabels()
    {
        return _hideOptionalLabels;
    }

    public void setHideOptionalLabels(boolean value)
    {
        _hideOptionalLabels = value;
    }

    public void setUseLineInterpolation(boolean value)
    {
        _UseLineInterpolation = value;
    }

    public boolean getUseLineInterpolation()
    {
        return _UseLineInterpolation;
    }

    //Set size for area's internal icon (LAA, mine and CBRN areas)
    public void setUnitSize(int pixelSize){_UnitSize = pixelSize;}

    public int getUnitSize(){return _UnitSize;}

    public void setKeepUnitRatio(boolean value) {_KeepUnitRatio = value;}

    public boolean getKeepUnitRatio() {return _KeepUnitRatio;}

    /**
     * Determines how to draw the Affiliation Modifier. True to draw as modifier
     * label in the "E/F" location. False to draw at the top right corner of the
     * symbol
     * @param value {@link Boolean}
     *
     * @deprecated
     */
    public void setDrawAffiliationModifierAsLabel(boolean value)
    {
        _DrawAffiliationModifierAsLabel = value;
    }

    /**
     * True to draw as modifier label in the "E/F" location. False to draw at
     * the top right corner of the symbol
     * @return {@link Boolean}
     *
     * @deprecated
     */
    public boolean getDrawAffiliationModifierAsLabel()
    {
        return _DrawAffiliationModifierAsLabel;
    }

    /**
     * Returns the modifier map for the symbol
     * @return {@link Map}
     */
    public Map<String,String> getModifierMap()
    {
        return _Properties;
    }

    /**
     * sets the modifier map for the symbol
     * @param modifiers {@link Map}
     */
    public void setModifierMap(Map<String,String> modifiers)
    {
        _Properties = modifiers;
    }

    /**
     * Get a modifier value
     * @param modifier {@link Modifiers}
     * @return {@link String}
     */
    public String getModifier(String modifier)
    {
        if (_Properties.containsKey(modifier))
        {
            return _Properties.get(modifier);
        }
        else
        {
            return getModifier(modifier, 0);
        }
    }

    /**
     * Set a modifier value
     * @param modifier {@link Modifiers}
     * @param value {@link String}
     */
    public void setModifier(String modifier, String value)
    {
        if (value.equals("") == false)
        {
            if (!(modifier == Modifiers.AM_DISTANCE)
                    || modifier == (Modifiers.AN_AZIMUTH)
                    || modifier == (Modifiers.X_ALTITUDE_DEPTH))
            {
                _Properties.put(modifier, value);
            }
            else
            {
                setModifier(modifier, value, 0);
            }
        }
    }

    /**
     * Gets modifier value based on modifier constant and index in array
     * @param modifier {@link Modifiers}
     * @param index {@link Integer} array location, only applicable to AM, AN and X
     * @return {@link String}
     */
    public String getModifier(String modifier, int index)
    {
        if (_Properties.containsKey(modifier))
        {
            return _Properties.get(modifier);
        }
        else if (modifier == (Modifiers.AM_DISTANCE)
                || modifier == (Modifiers.AN_AZIMUTH)
                || modifier == (Modifiers.X_ALTITUDE_DEPTH))
        {
            String value = String.valueOf(getModifier_AM_AN_X(modifier, index));
            if (value != null && !value.equalsIgnoreCase("null") && !value.equalsIgnoreCase(""))
            {
                return value;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

    /**
     * Get modifier value for AM, AN or X
     * @param modifier {@link Modifiers}
     * @param index {@link Integer} array location
     * @return {@link Double}
     */
    public Double getModifier_AM_AN_X(String modifier, int index)
    {
        ArrayList<Double> modifiers = null;
        if (modifier == (Modifiers.AM_DISTANCE))
        {
            modifiers = _AM_Distance;
        }
        else if (modifier == (Modifiers.AN_AZIMUTH))
        {
            modifiers = _AN_Azimuth;
        }
        else if (modifier == (Modifiers.X_ALTITUDE_DEPTH))
        {
            modifiers = _X_Altitude;
        }
        else
        {
            return null;
        }

        if (modifiers != null && modifiers.size() > index)
        {
            Double value = null;
            value = modifiers.get(index);
            if (value != null)
            {
                return value;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Modifiers must be added in order. No setting index 2 without first
     * setting index 0 and 1. If setting out of order is attempted, the value
     * will just be added to the end of the list.
     *
     * @param modifier {@link Modifiers}
     * @param value {@link String}
     * @param index {@link Integer}
     */
    public void setModifier(String modifier, String value, int index)
    {
        if (value.equals("") == false)
        {
            if (!(modifier == (Modifiers.AM_DISTANCE)
                    || modifier == (Modifiers.AN_AZIMUTH)
                    || modifier == (Modifiers.X_ALTITUDE_DEPTH)))
            {
                _Properties.put(modifier, value);
            }
            else
            {
                Double dblValue = Double.valueOf(value);
                if (dblValue != null)
                {
                    setModifier_AM_AN_X(modifier, dblValue, index);
                }
            }
        }
    }

    public void setModifier_AM_AN_X(String modifier, Double value, int index)
    {
        if ((modifier == (Modifiers.AM_DISTANCE)
                || modifier == (Modifiers.AN_AZIMUTH)
                || modifier == (Modifiers.X_ALTITUDE_DEPTH)))
        {
            ArrayList<Double> modifiers = null;
            if (modifier == (Modifiers.AM_DISTANCE))
            {
                if (_AM_Distance == null)
                {
                    _AM_Distance = new ArrayList<Double>();
                }
                modifiers = _AM_Distance;
            }
            else if (modifier == (Modifiers.AN_AZIMUTH))
            {
                if (_AN_Azimuth == null)
                {
                    _AN_Azimuth = new ArrayList<Double>();
                }
                modifiers = _AN_Azimuth;
            }
            else if (modifier == (Modifiers.X_ALTITUDE_DEPTH))
            {
                if (_X_Altitude == null)
                {
                    _X_Altitude = new ArrayList<Double>();
                }
                modifiers = _X_Altitude;
            }
            if (index + 1 > modifiers.size())
            {
                modifiers.add(value);
            }
            else
            {
                modifiers.set(index, value);
            }
        }
    }

    public ArrayList<Double> getModifiers_AM_AN_X(String modifier)
    {
        if (modifier == (Modifiers.AM_DISTANCE))
        {
            return _AM_Distance;
        }
        else if (modifier == (Modifiers.AN_AZIMUTH))
        {
            return _AN_Azimuth;
        }
        else if (modifier == (Modifiers.X_ALTITUDE_DEPTH))
        {
            return _X_Altitude;
        }

        return null;
    }

    public void setModifiers_AM_AN_X(String modifier, ArrayList<Double> modifiers)
    {
        if (modifier == (Modifiers.AM_DISTANCE))
        {
            _AM_Distance = modifiers;
        }
        else if (modifier == (Modifiers.AN_AZIMUTH))
        {
            _AN_Azimuth = modifiers;
        }
        else if (modifier == (Modifiers.X_ALTITUDE_DEPTH))
        {
            _X_Altitude = modifiers;
        }
    }

    /**
     *
     * @param value {@link Color}
     */
    public void setFillColor(Color value)
    {
        _FillColor = value;
    }

    /**
     *
     * @return {@link Color}
     */
    public Color getFillColor()
    {
        return _FillColor;
    }
    
    /**
    *
    * @param value {@link Color}
    */
   public void setTextColor(Color value)
   {
       _TextColor = value;
   }

   /**
    *
    * @return {@link Color}
    */
   public Color getTextColor()
   {
       return _TextColor;
   }
   
   /**
   *
   * @param value {@link Color}
   */
  public void setTextBackgroundColor(Color value)
  {
      _TextBackgroundColor = value;
  }

  /**
   *
   * @return {@link Color}
   */
  public Color getTextBackgroundColor()
  {
      return _TextBackgroundColor;
  }

    /**
     *
     * @param value {@link Integer}
     */
    public void setLineWidth(int value)
    {
        _LineWidth = value;
    }

    /**
     *
     * @return {@link Integer}
     */
    public int getLineWidth()
    {
        return _LineWidth;
    }

    /**
     * If value is null or SymbolUtilities.isGreenProtectionGraphic() is true then value is ignored
     *
     * @param value {@link Color}
     */
    public void setLineColor(Color value)
    {
        if (SymbolUtilities.isGreenProtectionGraphic(getSymbolID())) {
            _LineColor = new Color(0,166,81); // Green from SymbolUtilities.getLineColorOfAffiliation()
        } else if (value != null) {
            _LineColor = value;
        }
    }

    /**
     *
     * @return {@link Color}
     */
    public Color getLineColor()
    {
        return _LineColor;
    }

    /**
     * if null, renderer will use white or black for the outline based on the color
     * of the symbol. Otherwise, it will used the passed color value.
     *
     * @param value {@link Color}
     */
    public void setOutlineColor(Color value)
    {
        _OutLineColor = value;
    }

    public Color getOutlineColor()
    {
        return _OutLineColor;
    }

    /**
     * Extra value for client. defaults to null. Not used for rendering by
     * JavaRenderer
     *
     * @param value  {@link Object}
     * @deprecated
     */
    public void setTag(Object value)
    {
        _Tag = value;
    }

    /**
     * Extra value for client. defaults to null. Not used for rendering by
     * JavaRenderer
     *
     * @return {@link Object}
     * @deprecated
     */
    public Object getTag()
    {
        return _Tag;
    }

    /**
     *
     * @param value {@link ArrayList}
     */
    public void setCoordinates(ArrayList<Point2D> value)
    {
        _Coordinates = value;
    }

    /**
     *
     * @return {@link ArrayList}
     */
    public ArrayList<Point2D> getCoordinates()
    {
        return _Coordinates;
    }

    /**
     * Shapes that represent the symbol modifiers
     *
     * @param value ArrayList&lt;Shape&gt;
     */
    public void setModifierShapes(ArrayList<ShapeInfo> value)
    {
        _ModifierShapes = value;
    }

    /**
     * Shapes that represent the symbol modifiers
     *
     * @return {@link ArrayList}
     */
    public ArrayList<ShapeInfo> getModifierShapes()
    {
        return _ModifierShapes;
    }

    /**
     * the java shapes that make up the symbol
     *
     * @param value ArrayList&lt;ShapeInfo&gt;
     */
    public void setSymbolShapes(ArrayList<ShapeInfo> value)
    {
        _SymbolShapes = value;
    }

    /**
     * the java shapes that make up the symbol
     *
     * @return {@link ArrayList}
     */
    public ArrayList<ShapeInfo> getSymbolShapes()
    {
        return _SymbolShapes;
    }

    /**
     * The Symbol Id of the MilStdSymbol.
     *
     * @return {@link String}
     */
    public String getSymbolID()
    {
        return _symbolID;
    }

    /**
     * Unique ID of the Symbol. For client use.
     *
     * @return {@link String}
     */
    public String getUUID()
    {
        return _UUID;
    }

    /**
     * Unique ID of the Symbol. For client use.
     *
     * @param ID {@link String}
     */
    public void setUUID(String ID)
    {
        _UUID = ID;
    }

    /**
     * Sets the Symbol ID for the symbol. Should be a 20-30 digit string from
     * the milstd.
     *
     * @param value {@link String}
     */
    public void setSymbolID(String value)
    {

        String current = _symbolID;

        try
        {
            //set symbolID
            if (value != null && !value.equals("") && !current.equals(value))
            {
                _symbolID = value;
            }


        }// End try
        catch (Exception e)
        {
            // Log Error
            ErrorLogger.LogException("MilStdSymbol", "setSymbolID" + " - Did not fall under TG or FE", e);
        }
    }	// End set SymbolID
    private boolean _wasClipped=false;
    public void set_WasClipped(boolean value)
    {
        _wasClipped=value;
    }
    public boolean get_WasClipped()
    {
        return _wasClipped;
    }

}
