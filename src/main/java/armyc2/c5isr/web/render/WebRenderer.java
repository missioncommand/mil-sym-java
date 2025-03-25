package armyc2.c5isr.web.render;
// This import is if we need to call a javascript function
// It requires that you import the plugins.jar from the jdk folder into the project libraries
//import netscape.javascript.JSObject;


import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.utilities.JavaRendererUtilities;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main class for rendering multi-point graphics such as Control Measures, Atmospheric, and Oceanographic.
 *
 */
//@SuppressWarnings("unused")
public final class WebRenderer /* extends Applet */ {

    @Deprecated
    public static final int OUTPUT_FORMAT_JSON = 1;
    public static final int OUTPUT_FORMAT_GEOJSON = 2;
    public static final int OUTPUT_FORMAT_GEOSVG = 3;



    
    private static boolean _initSuccess = false;
    

    private static synchronized void init() {
        try
        {
        	if(_initSuccess == false)
        	{
	            //use WebRenderer.setLoggingLevel()
	            
	            //sets default value for single point symbology to have an outline.
	            //outline color will be automatically determined based on line color
	            //unless a color value is manually set.
	            
	            //Set Renderer Settings/////////////////////////////////////////////
	            RendererSettings.getInstance().setTextBackgroundMethod(
	                            RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
	            //RendererSettings.getInstance().setLabelForegroundColor(Color.BLACK);
	            //RendererSettings.getInstance().setLabelBackgroundColor(new Color(255, 255, 255, 200));
	            RendererSettings.getInstance().setLabelFont("arial", Font.PLAIN, 12);
	            ErrorLogger.setLevel(Level.FINE);

	            _initSuccess = true;
        	}
            
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("WebRenderer", "init", exc, Level.WARNING);
        }


    }



    /**
     * Renders all multi-point symbols, creating KML that can be used to draw
     * it on a Google map.  Multipoint symbols cannot be draw the same 
     * at different scales. For instance, graphics with arrow heads will need to 
     * redraw arrowheads when you zoom in on it.  Similarly, graphics like a 
     * Forward Line of Troops drawn with half circles can improve performance if 
     * clipped when the parts of the graphic that aren't on the screen.  To help 
     * readjust graphics and increase performance, this function requires the 
     * scale and bounding box to help calculate the new locations.
     * @param id A unique identifier used to identify the symbol by Google map. 
     * The id will be the folder name that contains the graphic.
     * @param name a string used to display to the user as the name of the 
     * graphic being created.
     * @param description a brief description about the graphic being made and 
     * what it represents.
     * @param symbolCode A 20-30 digit symbolID corresponding to one of the
     * graphics in the MIL-STD-2525D
     * @param controlPoints The vertices of the graphics that make up the
     * graphic.  Passed in the format of a string, using decimal degrees 
     * separating lat and lon by a comma, separating coordinates by a space.  
     * The following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
     * @param altitudeMode Indicates whether the symbol should interpret 
     * altitudes as above sea level or above ground level. Options are 
     * "clampToGround", "relativeToGround" (from surface of earth), "absolute" 
     * (sea level), "relativeToSeaFloor" (from the bottom of major bodies of 
     * water).
     * @param scale A number corresponding to how many meters one meter of our 
     * map represents. A value "50000" would mean 1:50K which means for every 
     * meter of our map it represents 50000 meters of real world distance.
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
     * but can speed up rendering in some cases.
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers {@link Map}, keyed using constants from Modifiers.
     * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
     * @param attributes {@link Map}, keyed using constants from MilStdAttributes.
     * @param format An enumeration: 2 for GeoJSON.
     * @return A JSON string representation of the graphic.
     */
    public static String RenderSymbol(String id, String name, String description,
                                      String symbolCode, String controlPoints, String altitudeMode,
                                      double scale, String bbox, Map<String,String> modifiers, Map<String,String> attributes, int format) {
        String output = "";
        try {         
        	
        	JavaRendererUtilities.addAltModeToModifiersString(attributes,altitudeMode);


            output = MultiPointHandler.RenderSymbol(id, name, description, symbolCode, controlPoints,
                    scale, bbox, modifiers, attributes, format);

            //DEBUGGING
            if(ErrorLogger.getLevel().intValue() <= Level.FINER.intValue())
            {
                System.out.println("");
                StringBuilder sb = new StringBuilder();
                sb.append("\nID: " + id + "\n");
                sb.append("Name: " + name + "\n");
                sb.append("Description: " + description + "\n");
                sb.append("SymbolID: " + symbolCode + "\n");
                sb.append("Scale: " + String.valueOf(scale) + "\n");
                sb.append("BBox: " + bbox + "\n");
                sb.append("Coords: " + controlPoints + "\n");
                sb.append("Modifiers: " + modifiers + "\n");
                ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", sb.toString(),Level.FINER);
            }
            if(ErrorLogger.getLevel().intValue() <= Level.FINEST.intValue())
            {
                String briefOutput = output.replaceAll("</Placemark>", "</Placemark>\n");
                briefOutput = output.replaceAll("(?s)<description[^>]*>.*?</description>", "<description></description>");
                ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", "Output:\n" + briefOutput,Level.FINEST);
            }

            
            
        } catch (Exception ea) {
            
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol - " + ea.toString() + "'}";
            ErrorLogger.LogException("WebRenderer", "RenderSymbol", ea, Level.WARNING);
        }
        
        return output;
    }
    

         


    /**
     * Renders all multi-point symbols, creating KML or JSON for the user to
     * parse and render as they like.
     * This function requires the bounding box to help calculate the new
     * locations.
     * @param id A unique identifier used to identify the symbol by Google map.
     * The id will be the folder name that contains the graphic.
     * @param name a string used to display to the user as the name of the 
     * graphic being created.
     * @param description a brief description about the graphic being made and 
     * what it represents.
     * @param symbolCode A 20-30 digit symbolID corresponding to one of the
     * graphics in the MIL-STD-2525D
     * @param controlPoints The vertices of the graphics that make up the
     * graphic.  Passed in the format of a string, using decimal degrees
     * separating lat and lon by a comma, separating coordinates by a space.
     * The following format shall be used "x1,y1 [xn,yn]..."
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY."
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers {@link Map}, keyed using constants from Modifiers.
     * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
     * @param attributes {@link Map}, keyed using constants from MilStdAttributes.
     * @param format An enumeration: 2 for GeoJSON.
     * @return A JSON (1) or KML (0) string representation of the graphic.
     */
    public static String RenderSymbol2D(String id, String name, String description, String symbolCode, String controlPoints,
            int pixelWidth, int pixelHeight, String bbox, Map<String,String> modifiers,
            Map<String,String> attributes, int format)
    {
        String output = "";
        try
        {
            output = MultiPointHandler.RenderSymbol2D(id, name, description, 
                    symbolCode, controlPoints, pixelWidth, pixelHeight, bbox, 
                    modifiers, attributes, format);
        }
        catch(Exception exc)
        {
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol: " + symbolCode + " - " + exc.toString() + "'}";
        }
        return output;
    }


    /**
	 * Renders all MilStd 2525 multi-point symbols, creating MilStdSymbol that contains the
	 * information needed to draw the symbol on the map.
     * DOES NOT support RADARC, CAKE, TRACK etc...
	 * ArrayList&lt;Point2D&gt; milStdSymbol.getSymbolShapes.get(index).getPolylines()
	 * and 
	 * ShapeInfo = milStdSymbol.getModifierShapes.get(index). 
	 * 
	 * 
	 * @param id
	 *            A unique identifier used to identify the symbol by Google map.
	 *            The id will be the folder name that contains the graphic.
	 * @param name
	 *            a string used to display to the user as the name of the
	 *            graphic being created.
	 * @param description
	 *            a brief description about the graphic being made and what it
	 *            represents.
	 * @param symbolCode
	 *            A 20-30 digit symbolID corresponding to one of the graphics
	 *            in the MIL-STD-2525D
	 * @param controlPoints
	 *            The vertices of the graphics that make up the graphic. Passed
	 *            in the format of a string, using decimal degrees separating
	 *            lat and lon by a comma, separating coordinates by a space. The
	 *            following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
	 * @param altitudeMode
	 *            Indicates whether the symbol should interpret altitudes as
	 *            above sea level or above ground level. Options are
	 *            "clampToGround", "relativeToGround" (from surface of earth),
	 *            "absolute" (sea level), "relativeToSeaFloor" (from the bottom
	 *            of major bodies of water).
	 * @param scale
	 *            A number corresponding to how many meters one meter of our map
	 *            represents. A value "50000" would mean 1:50K which means for
	 *            every meter of our map it represents 50000 meters of real
	 *            world distance.
	 * @param bbox
	 *            The viewable area of the map. Passed in the format of a string
	 *            "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
	 *            but can speed up rendering in some cases. example:
	 *            "-50.4,23.6,-42.2,24.2"
	 * @param modifiers
	 *            Used like:
	 *            modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, "T");
	 *            Or
	 *            modifiers.put(Modifiers.AM_DISTANCE, "1000,2000,3000");
	 * @param attributes
	 * 			  Used like:
	 *            attributes.put(MilStdAttributes.LineWidth, "3");
	 *            Or
	 *            attributes.put(MilStdAttributes.LineColor, "#00FF00");
     * @return MilStdSymbol
     */
    public static MilStdSymbol RenderMultiPointAsMilStdSymbol(String id, String name, String description, String symbolCode,
			String controlPoints, String altitudeMode, double scale, String bbox, Map<String,String> modifiers, Map<String,String> attributes)
    {
		MilStdSymbol mSymbol = null;
		try 
		{
			mSymbol = MultiPointHandler.RenderSymbolAsMilStdSymbol(id, name, description, symbolCode,
                    controlPoints, scale, bbox, modifiers, attributes);

            //Uncomment to show sector1 modifiers as fill pattern
//            int symbolSet = SymbolID.getEntityCode(symbolCode);
//            if(symbolSet == 270707 || symbolSet == 270800 || symbolSet == 270801 || symbolSet == 151100) //Mined Areas
//            {
//                int size = RendererSettings.getInstance().getDefaultPixelSize();
//
//                ArrayList<ShapeInfo> shapes = mSymbol.getSymbolShapes();
//                if(shapes.size() > 0){
//                    ShapeInfo shape = shapes.get(0);
//                    shape.setPatternFillImage(PatternFillRendererD.MakeSymbolPatternFill(symbolCode,size));
//                    if(shape.getPatternFillImage() != null)
//                        shape.setShader(new BitmapShader(shape.getPatternFillImage(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
//                }
//            }
		}
		catch (Exception ea) 
		{
			mSymbol=null;
			ErrorLogger.LogException("WebRenderer", "RenderMultiPointAsMilStdSymbol" + " - " + symbolCode, ea, Level.WARNING);
		}
		
		//System.out.println("RenderMultiPointAsMilStdSymbol exit");
		return mSymbol;
    }

}
