package armyc2.c5isr.web.render;
// This import is if we need to call a javascript function
// It requires that you import the plugins.jar from the jdk folder into the project libraries
//import netscape.javascript.JSObject;


import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.utilities.Basic3DShapes;
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

	public static final int OUTPUT_FORMAT_KML = 0;
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
     * Renders all multi-point symbols, creating KML, GeoJSON or SVG that can be used to draw
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
     * <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
     * @param bbox The viewable area of the map.  Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
     * but can speed up rendering in some cases.
     * example: "-50.4,23.6,-42.2,24.2"
     * @param modifiers {@link Map}, keyed using constants from Modifiers.
     * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
     * @param attributes {@link Map}, keyed using constants from MilStdAttributes.
     * @param format {@link WebRenderer#OUTPUT_FORMAT_KML}, {@link WebRenderer#OUTPUT_FORMAT_GEOJSON} or {@link WebRenderer#OUTPUT_FORMAT_GEOSVG}
     * @return A KML, GeoJSON or SVG string representation of the graphic.
     */
    public static String RenderSymbol(String id, String name, String description,
                                      String symbolCode, String controlPoints, String altitudeMode,
                                      double scale, String bbox, Map<String,String> modifiers, Map<String,String> attributes, int format) {
        String output = "";
        try {         
        	
        	JavaRendererUtilities.addAltModeToModifiersString(attributes,altitudeMode);

			if (!altitudeMode.equals("clampToGround")
					&& (format == WebRenderer.OUTPUT_FORMAT_KML || format == WebRenderer.OUTPUT_FORMAT_GEOJSON)
					&& JavaRendererUtilities.is3dSymbol(symbolCode)
					&& modifiers.get(Modifiers.X_ALTITUDE_DEPTH) != null
					&& !modifiers.get(Modifiers.X_ALTITUDE_DEPTH).isEmpty()) {
				output = RenderMilStd3dSymbol(id, name, description, symbolCode, controlPoints, altitudeMode, scale, bbox, modifiers, attributes, format);
			}

			if (output.isEmpty()) {
				output = MultiPointHandler.RenderSymbol(id, name, description, symbolCode, controlPoints,
						scale, bbox, modifiers, attributes, format);

				//DEBUGGING
				if (ErrorLogger.getLevel().intValue() <= Level.FINER.intValue()) {
					System.out.println("");
					StringBuilder sb = new StringBuilder();
					sb.append("\nID: ").append(id).append("\n");
					sb.append("Name: ").append(name).append("\n");
					sb.append("Description: ").append(description).append("\n");
					sb.append("SymbolID: ").append(symbolCode).append("\n");
					sb.append("Scale: ").append(String.valueOf(scale)).append("\n");
					sb.append("BBox: ").append(bbox).append("\n");
					sb.append("Coords: ").append(controlPoints).append("\n");
					sb.append("Modifiers: ").append(modifiers).append("\n");
					ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", sb.toString(), Level.FINER);
				}
				if (ErrorLogger.getLevel().intValue() <= Level.FINEST.intValue()) {
					String briefOutput = output.replaceAll("</Placemark>", "</Placemark>\n");
					briefOutput = output.replaceAll("(?s)<description[^>]*>.*?</description>", "<description></description>");
					ErrorLogger.LogMessage("WebRenderer", "RenderSymbol", "Output:\n" + briefOutput, Level.FINEST);
				}
			}
        } catch (Exception ea) {
            output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol - " + ea.toString() + "'}";
            ErrorLogger.LogException("WebRenderer", "RenderSymbol", ea, Level.WARNING);
        }
        
        return output;
    }
    

         


    /**
     * Renders all multi-point symbols, creating KML, GeoJSON or SVG for the user to
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
	 * @param format {@link WebRenderer#OUTPUT_FORMAT_KML}, {@link WebRenderer#OUTPUT_FORMAT_GEOJSON} or {@link WebRenderer#OUTPUT_FORMAT_GEOSVG}
     * @return A KML, GeoJSON or SVG string representation of the graphic.
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
	 * Renders all 3d multi-point symbols, creating KML or GeoJSON that can be
	 * used to draw it on a Google map.
	 * 3D version of RenderSymbol()
	 *
	 * @param id            A unique identifier used to identify the symbol by Google map.
	 *                      The id will be the folder name that contains the graphic.
	 * @param name          a string used to display to the user as the name of the
	 *                      graphic being created.
	 * @param description   a brief description about the graphic being made and
	 *                      what it represents.
	 * @param symbolCode    A 20-30 digit symbolID corresponding to one of the
	 *                      graphics in the MIL-STD-2525D
	 * @param controlPoints The vertices of the graphics that make up the
	 *                      graphic.  Passed in the format of a string, using decimal degrees
	 *                      separating lat and lon by a comma, separating coordinates by a space.
	 *                      The following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
	 * @param altitudeMode  Indicates whether the symbol should interpret
	 *                      altitudes as above sea level or above ground level. Options are
	 *                      "clampToGround", "relativeToGround" (from surface of earth), "absolute"
	 *                      (sea level), "relativeToSeaFloor" (from the bottom of major bodies of
	 *                      water).
	 * @param scale         A number corresponding to how many meters one meter of our
	 *                      map represents. A value "50000" would mean 1:50K which means for every
	 *                      meter of our map it represents 50000 meters of real world distance.
	 *                      <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
	 * @param bbox          The viewable area of the map.  Passed in the format of a
	 *                      string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
	 *                      but can speed up rendering in some cases.
	 *                      example: "-50.4,23.6,-42.2,24.2"
	 * @param modifiers     keyed using constants from Modifiers.
	 *                      Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
	 * @param attributes    keyed using constants from MilStdAttributes.
	 * @param format        {@link WebRenderer#OUTPUT_FORMAT_KML}, {@link WebRenderer#OUTPUT_FORMAT_GEOJSON} or {@link WebRenderer#OUTPUT_FORMAT_GEOSVG}
	 * @return A KML, GeoJSON or SVG string representation of the graphic.
	 */
	public static String RenderMilStd3dSymbol(String id, String name, String description,
											  String symbolCode, String controlPoints, String altitudeMode,
											  double scale, String bbox, Map<String, String> modifiers, Map<String, String> attributes, int format) {
		String output = "";
		try {

			output = Shape3DHandler.RenderMilStd3dSymbol(id, name, description, symbolCode, controlPoints, altitudeMode,
					scale, bbox, modifiers, attributes, format);

			//DEBUGGING
			if (ErrorLogger.getLevel().intValue() <= Level.FINER.intValue()) {
				System.out.println("");
				StringBuilder sb = new StringBuilder();
				sb.append("\nID: ").append(id).append("\n");
				sb.append("Name: ").append(name).append("\n");
				sb.append("Description: ").append(description).append("\n");
				sb.append("SymbolID: ").append(symbolCode).append("\n");
				sb.append("Scale: ").append(String.valueOf(scale)).append("\n");
				sb.append("BBox: ").append(bbox).append("\n");
				sb.append("Coords: ").append(controlPoints).append("\n");
				sb.append("Modifiers: ").append(modifiers).append("\n");
				ErrorLogger.LogMessage("WebRenderer", "RenderMilStd3dSymbol", sb.toString(), Level.FINER);
			}
			if (ErrorLogger.getLevel().intValue() <= Level.FINEST.intValue()) {
				String briefOutput = output.replaceAll("</Placemark>", "</Placemark>\n");
				briefOutput = output.replaceAll("(?s)<description[^>]*>.*?</description>", "<description></description>");
				ErrorLogger.LogMessage("WebRenderer", "RenderMilStd3dSymbol", "Output:\n" + briefOutput, Level.FINEST);
			}
		} catch (Exception ea) {
			output = "{\"type\":'error',error:'There was an error creating the 3D MilStdSymbol - " + ea.toString() + "'}";
			ErrorLogger.LogException("WebRenderer", "RenderMilStd3dSymbol", ea, Level.WARNING);
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
	 *            ignored
	 * @param scale
	 *            A number corresponding to how many meters one meter of our map
	 *            represents. A value "50000" would mean 1:50K which means for
	 *            every meter of our map it represents 50000 meters of real
	 *            world distance.
	 *            <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
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

	/**
	 * Renders basic shapes as symbols, creating MilStdSymbol that contains the
	 * information needed to draw the shape on the map.
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
	 * @param basicShapeType
	 *            {@link armyc2.c5isr.JavaLineArray.BasicShapes}
	 * @param controlPoints
	 *            The vertices of the graphics that make up the graphic. Passed
	 *            in the format of a string, using decimal degrees separating
	 *            lat and lon by a comma, separating coordinates by a space. The
	 *            following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
	 * @param altitudeMode
	 *            ignored
	 * @param scale
	 *            A number corresponding to how many meters one meter of our map
	 *            represents. A value "50000" would mean 1:50K which means for
	 *            every meter of our map it represents 50000 meters of real
	 *            world distance.
	 *            <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
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
	public static MilStdSymbol RenderBasicShapeAsMilStdSymbol(String id, String name, String description, int basicShapeType,
															  String controlPoints, String altitudeMode, double scale, String bbox, Map<String, String> modifiers, Map<String, String> attributes) {
		MilStdSymbol mSymbol = null;
		try {
			if (SymbolUtilities.isBasicShape(basicShapeType))
				mSymbol = MultiPointHandler.RenderBasicShapeAsMilStdSymbol(id, name, description, basicShapeType,
						controlPoints, scale, bbox, modifiers, attributes);
		} catch (Exception ea) {
			mSymbol = null;
			ErrorLogger.LogException("WebRenderer", "RenderBasicShapeAsMilStdSymbol" + " - " + basicShapeType, ea, Level.WARNING);
		}

		return mSymbol;
	}

	/**
	 * Renders multipoint basic shapes, creating KML, GeoJSON or SVG that can be used to draw
	 * it on a Google map.
	 * @param id A unique identifier used to identify the symbol by Google map.
	 * The id will be the folder name that contains the graphic.
	 * @param name a string used to display to the user as the name of the
	 * graphic being created.
	 * @param description a brief description about the graphic being made and
	 * what it represents.
	 * @param basicShapeType {@link armyc2.c5isr.JavaLineArray.BasicShapes}
	 * @param controlPoints The vertices of the graphics that make up the
	 * graphic.  Passed in the format of a string, using decimal degrees
	 * separating lat and lon by a comma, separating coordinates by a space.
	 * The following format shall be used "x1,y1[,z1] [xn,yn[,zn]]..."
	 * @param altitudeMode ignored
	 * @param scale A number corresponding to how many meters one meter of our
	 * map represents. A value "50000" would mean 1:50K which means for every
	 * meter of our map it represents 50000 meters of real world distance.
	 * <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
	 * @param bbox The viewable area of the map.  Passed in the format of a
	 * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
	 * but can speed up rendering in some cases.
	 * example: "-50.4,23.6,-42.2,24.2"
	 * @param modifiers keyed using constants from Modifiers.
	 * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
	 * @param attributes keyed using constants from MilStdAttributes.
	 * @param format {@link WebRenderer#OUTPUT_FORMAT_KML}, {@link WebRenderer#OUTPUT_FORMAT_GEOJSON} or {@link WebRenderer#OUTPUT_FORMAT_GEOSVG}
	 * @return A KML, GeoJSON or SVG string representation of the graphic.
	 */
	public static String RenderBasicShape(String id, String name, String description, int basicShapeType,
										  String controlPoints, String altitudeMode,
										  double scale, String bbox, Map<String, String> modifiers, Map<String, String> attributes, int format) {
		String output = "";
		try {
			if (SymbolUtilities.isBasicShape(basicShapeType))
				output = MultiPointHandler.RenderBasicShape(id, name, description, basicShapeType, controlPoints,
						scale, bbox, modifiers, attributes, format);
		} catch (Exception ea) {
			output = "{\"type\":'error',error:'There was an error creating the MilStdSymbol - " + ea.toString() + "'}";
			ErrorLogger.LogException("WebRenderer", "RenderBasicShape", ea, Level.WARNING);
		}
		return output;
	}

	/**
	 * Renders basic 3D shapes, creating KML or GeoJSON that can be used to draw
	 * it on a Google map.
	 * @param id A unique identifier used to identify the symbol by Google map.
	 * The id will be the folder name that contains the graphic.
	 * @param name a string used to display to the user as the name of the
	 * graphic being created.
	 * @param description a brief description about the graphic being made and
	 * what it represents.
	 * @param basicShapeType {@link Basic3DShapes}
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
	 * <a href="https://github.com/missioncommand/mil-sym-java/wiki/2525D--Renderer-Overview#3316-scale-for-3d">Scale Calculation Example</a>
	 * @param bbox The viewable area of the map.  Passed in the format of a
	 * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." Not required
	 * but can speed up rendering in some cases.
	 * example: "-50.4,23.6,-42.2,24.2"
	 * @param modifiers {@link Map}, keyed using constants from Modifiers.
	 * Pass in comma delimited String for modifiers with multiple values like AM, AN &amp; X
	 * @param attributes {@link Map}, keyed using constants from MilStdAttributes.
	 * @param format {@link WebRenderer#OUTPUT_FORMAT_KML}, {@link #OUTPUT_FORMAT_GEOJSON}
	 * @return A KML or GeoJSON string representation of the graphic.
	 */
	public static String RenderBasic3DShape(String id, String name, String description, int basicShapeType,
											String controlPoints, String altitudeMode,
											double scale, String bbox, Map<String, String> modifiers, Map<String, String> attributes, int format) {
		String output = "";
		try {
			JavaRendererUtilities.addAltModeToModifiersString(attributes, altitudeMode);
			if (SymbolUtilities.isBasicShape(basicShapeType))
				output = Shape3DHandler.RenderBasic3DShape(id, name, description, basicShapeType, controlPoints, altitudeMode,
						scale, bbox, modifiers, attributes, format);
		} catch (Exception ea) {
			output = "{\"type\":'error',error:'There was an error creating the 3D MilStdSymbol - " + ea.toString() + "'}";
			ErrorLogger.LogException("WebRenderer", "RenderBasic3DShape", ea, Level.WARNING);
		}
		return output;
	}
}
