package armyc2.c5isr.web.render;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;


import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.utilities.JavaRendererUtilities;
import armyc2.c5isr.web.render.utilities.LineInfo;
import armyc2.c5isr.web.render.utilities.SymbolInfo;
import armyc2.c5isr.web.render.utilities.TextInfo;
import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.RenderMultipoints.clsRenderer;
import armyc2.c5isr.JavaTacticalRenderer.mdlGeodesic;

import java.util.List;
import java.util.logging.Level;


@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public class MultiPointHandler {
    private static final int _maxPixelWidth = 1920;
    private static final int _minPixelWidth = 720;


    /**
     * GE has the unusual distinction of being an application with coordinates
     * outside its own extents. It appears to only be a problem when lines cross
     * the IDL
     *
     * @param pts2d the client points
     */
    public static void NormalizeGECoordsToGEExtents(double leftLongitude,
            double rightLongitude,
            ArrayList<Point2D> pts2d) {
        try {
            int j = 0;
            double x = 0, y = 0;
            Point2D pt2d = null;
            int n = pts2d.size();
            //for (j = 0; j < pts2d.size(); j++) 
            for (j = 0; j < n; j++) {
                pt2d = pts2d.get(j);
                x = pt2d.getX();
                y = pt2d.getY();
                while (x < leftLongitude) {
                    x += 360;
                }
                while (x > rightLongitude) {
                    x -= 360;
                }

                pt2d = new Point2D.Double(x, y);
                pts2d.set(j, pt2d);
            }
        } catch (Exception exc) {
        }
    }

    /**
     * GE recognizes coordinates in the range of -180 to +180
     *
     * @param pt2d
     * @return
     */
    protected static Point2D NormalizeCoordToGECoord(Point2D pt2d) {
        Point2D ptGeo = null;
        try {
            double x = pt2d.getX(), y = pt2d.getY();
            while (x < -180) {
                x += 360;
            }
            while (x > 180) {
                x -= 360;
            }

            ptGeo = new Point2D.Double(x, y);
        } catch (Exception exc) {
        }
        return ptGeo;
    }

    /**
     * We have to ensure the bounding rectangle at least includes the symbol or
     * there are problems rendering, especially when the symbol crosses the IDL
     *
     * @param controlPoints the client symbol anchor points
     * @param bbox the original bounding box
     * @return the modified bounding box
     */
    private static String getBoundingRectangle(String controlPoints,
            String bbox) {
        String bbox2 = "";
        try {
            //first get the minimum bounding rect for the geo coords
            Double left = 0.0;
            Double right = 0.0;
            Double top = 0.0;
            Double bottom = 0.0;

            String[] coordinates = controlPoints.split(" ");
            int len = coordinates.length;
            int i = 0;
            left = Double.MAX_VALUE;
            right = -Double.MAX_VALUE;
            top = -Double.MAX_VALUE;
            bottom = Double.MAX_VALUE;
            for (i = 0; i < len; i++) {
                String[] coordPair = coordinates[i].split(",");
                Double latitude = Double.valueOf(coordPair[1].trim());
                Double longitude = Double.valueOf(coordPair[0].trim());
                if (longitude < left) {
                    left = longitude;
                }
                if (longitude > right) {
                    right = longitude;
                }
                if (latitude > top) {
                    top = latitude;
                }
                if (latitude < bottom) {
                    bottom = latitude;
                }
            }
            bbox2 = left.toString() + "," + bottom.toString() + "," + right.toString() + "," + top.toString();
        } catch (Exception ex) {
            System.out.println("Failed to create bounding rectangle in MultiPointHandler.getBoundingRect");
        }
        return bbox2;
    }

    /**
     * need to use the symbol to get the upper left control point in order to
     * produce a valid PointConverter
     *
     * @param geoCoords
     * @return
     */
    private static Point2D getControlPoint(ArrayList<Point2D> geoCoords) {
        Point2D pt2d = null;
        try {
            double left = Double.MAX_VALUE;
            double right = -Double.MAX_VALUE;
            double top = -Double.MAX_VALUE;
            double bottom = Double.MAX_VALUE;
            Point2D ptTemp = null;
            int n = geoCoords.size();
            //for (int j = 0; j < geoCoords.size(); j++) 
            for (int j = 0; j < n; j++) {
                ptTemp = geoCoords.get(j);
                if (ptTemp.getX() < left) {
                    left = ptTemp.getX();
                }
                if (ptTemp.getX() > right) {
                    right = ptTemp.getX();
                }
                if (ptTemp.getY() > top) {
                    top = ptTemp.getY();
                }
                if (ptTemp.getY() < bottom) {
                    bottom = ptTemp.getY();
                }
            }
            pt2d = new Point2D.Double(left, top);
        } catch (Exception ex) {
            System.out.println("Failed to create control point in MultiPointHandler.getControlPoint");
        }
        return pt2d;
    }

    /**
     * Assumes a reference in which the north pole is on top.
     *
     * @param geoCoords the geographic coordinates
     * @return the upper left corner of the MBR containing the geographic
     * coordinates
     */
    private static Point2D getGeoUL(ArrayList<Point2D> geoCoords) {
        Point2D ptGeo = null;
        try {
            int j = 0;
            Point2D pt = null;
            double left = geoCoords.get(0).getX();
            double top = geoCoords.get(0).getY();
            double right = geoCoords.get(0).getX();
            double bottom = geoCoords.get(0).getY();
            int n = geoCoords.size();
            //for (j = 1; j < geoCoords.size(); j++) 
            for (j = 1; j < n; j++) {
                pt = geoCoords.get(j);
                if (pt.getX() < left) {
                    left = pt.getX();
                }
                if (pt.getX() > right) {
                    right = pt.getX();
                }
                if (pt.getY() > top) {
                    top = pt.getY();
                }
                if (pt.getY() < bottom) {
                    bottom = pt.getY();
                }
            }
            //if geoCoords crosses the IDL
            if (right - left > 180) {
                //There must be at least one x value on either side of +/-180. Also, there is at least
                //one positive value to the left of +/-180 and negative x value to the right of +/-180.
                //We are using the orientation with the north pole on top so we can keep
                //the existing value for top. Then the left value will be the least positive x value
                //left = geoCoords.get(0).getX();
                left = 180;
                //for (j = 1; j < geoCoords.size(); j++) 
                n = geoCoords.size();
                for (j = 0; j < n; j++) {
                    pt = geoCoords.get(j);
                    if (pt.getX() > 0 && pt.getX() < left) {
                        left = pt.getX();
                    }
                }
            }
            ptGeo = new Point2D.Double(left, top);
        } catch (Exception ex) {
            System.out.println("Failed to create control point in MultiPointHandler.getControlPoint");
        }
        return ptGeo;
    }
    private static String getBboxFromCoords(ArrayList<Point2D> geoCoords) {
        //var ptGeo = null;
        String bbox = null;
        try {
            int j = 0;
            Point2D pt = null;
            double left = geoCoords.get(0).getX();
            double top = geoCoords.get(0).getY();
            double right = geoCoords.get(0).getX();
            double bottom = geoCoords.get(0).getY();
            for (j = 1; j < geoCoords.size(); j++) {
                pt = geoCoords.get(j);
                if (pt.getX() < left) {
                    left = pt.getX();
                }
                if (pt.getX() > right) {
                    right = pt.getX();
                }
                if (pt.getY() > top) {
                    top = pt.getY();
                }
                if (pt.getY() < bottom) {
                    bottom = pt.getY();
                }
            }
            //if geoCoords crosses the IDL
            if (right - left > 180) {
                //There must be at least one x value on either side of +/-180. Also, there is at least
                //one positive value to the left of +/-180 and negative x value to the right of +/-180.
                //We are using the orientation with the north pole on top so we can keep
                //the existing value for top. Then the left value will be the least positive x value
                //left = geoCoords[0].x;
                left = 180;
                right = -180;
                for (j = 0; j < geoCoords.size(); j++) {
                    pt = geoCoords.get(j);
                    if (pt.getX() > 0 && pt.getX() < left) {
                        left = pt.getX();
                    }
                    if (pt.getX() < 0 && pt.getX() > right) {
                        right = pt.getX();
                    }
                }
            }
            //ptGeo = new Point2D(left, top);
            bbox = Double.toString(left) + "," + Double.toString(bottom) + "," + Double.toString(right) + "," + Double.toString(top);
        } catch (Exception ex) {
            System.out.println("Failed to create control point in MultiPointHandler.getBboxFromCoords");
        }
        //return ptGeo;            
        return bbox;
    }

    private static boolean crossesIDL(ArrayList<Point2D> geoCoords) {
        boolean result = false;
        Point2D pt2d = getControlPoint(geoCoords);
        double left = pt2d.getX();
        Point2D ptTemp = null;
        int n = geoCoords.size();
        //for (int j = 0; j < geoCoords.size(); j++) 
        for (int j = 0; j < n; j++) {
            ptTemp = geoCoords.get(j);
            if (Math.abs(ptTemp.getX() - left) > 180) {
                return true;
            }
        }
        return result;
    }

    /**
     * Checks if a symbol is one with decorated lines which puts a strain on
     * google earth when rendering like FLOT. These complicated lines should be
     * clipped when possible.
     *
     * @param symbolID
     * @return
     */
    public static Boolean ShouldClipSymbol(String symbolID) {
        //TODO: need to reevaluate this function to make sure we clip the right symbols.
        int status = SymbolID.getStatus(symbolID);

        if (SymbolUtilities.isTacticalGraphic(symbolID) && status == SymbolID.Status_Planned_Anticipated_Suspect) {
            return true;
        }

        if (SymbolUtilities.isWeather(symbolID)) {
            return true;
        }

        int id = Integer.valueOf(SymbolUtilities.getBasicSymbolID(symbolID));
        //TODO: needs to be reworked
        if(id == 25341100 || //Task Fix
                id == 25260200 || //CFL
                id == 25110100 || //Boundary
                id == 25110200 || //Light Line (LL)
                id == 25110300 || //Engineer Work Line (EWL)
                id == 25140100 || //FLOT
                id == 25140200 ||  //Line of contact is now just two flots
                id == 25151000 || //Fortified Area
                id == 25151100 || //Limited Access Area
                id == 25172000 || //Weapons Free Zone
                id == 25151202 || //Battle Position/Prepared but not Occupied
                id == 25151203 || //Strong Point
                id == 25141200 || //Probable Line of Deployment (PLD)
                id == 25270800 || //Mined Area
                id == 25270801 || //Mined Area, Fenced
                id == 25170100 || //Air Corridor
                id == 25170200 || //Low Level Transit Route (LLTR)
                id == 25170300 || //Minimum-Risk Route (MRR)
                id == 25170400 || //Safe Lane (SL)
                id == 25170500 || //Standard Use ARmy Aircraft Flight Route (SAAFR)
                id == 25170600 || //Transit Corridors (TC)
                id == 25170700 || //Special Corridor (SC)

                id == 25270100 || //Obstacle Belt
                id == 25270200 || //Obstacle Zone
                id == 25270300 || //Obstacle Free Zone
                id == 25270400 || //Obstacle Restricted Zone

                id == 25290100 || //Obstacle Line
                id == 25290201 || //Antitank Ditch - Under Construction
                id == 25290202 || //Antitank Ditch - Completed
                id == 25290203 || //Antitank Ditch Reinforced, with Antitank Mines
                id == 25290204 || //Antitank Wall
                id == 25290301 || //Unspecified
                id == 25290302 || //Single Fence
                id == 25290303 || //Double Fence
                id == 25290304 || //Double Apron Fence
                id == 25290305 || //Low Wire Fence
                id == 25290306 || //High Wire Fence
                id == 25290307 || //Single Concertina
                id == 25290308 || //Double Strand Concertina
                id == 25290309 || //Triple Strand Concertina

                id == 25341100 || //Obstacles Effect Fix now Mission Tasks Fix
                id == 25290400 || //Mine Cluster
                id == 25282003 || //Aviation / Overhead Wire
                id == 25270602 || //Bypass Difficult
                id == 25271500 || //Ford Easy
                id == 25271600 || //Ford Difficult

                id == 25290900 || //Fortified Line

                id == 25271700 || //Biological Contaminated Area
                id == 25271800 || //Chemical Contaminated Area
                id == 25271900 || //Nuclear Contaminated Area
                id == 25272000 || //Radiological Contaminated Area

                id == 25240301 || //No Fire Area (NFA) - Irregular
                id == 25240302 || //No Fire Area (NFA) - Rectangular
                id == 25240303 || //No Fire Area (NFA) - Circular


                id == 25240701 || //Linear Target
                id == 25240702 || //Linear Smoke Target
                id == 25240703 || //Final Protective Fire (FPF)
                id == 25151800 || //Encirclement

                id == 25330300 || //MSR
                id == 25330301 || //MSR / One Way Traffic
                id == 25330302 || //MSR / Two Way Traffic
                id == 25330303 || //MSR / Alternating Traffic

                id == 25330400 || //ASR
                id == 25330401 || //ASR / One Way Traffic
                id == 25330402 || //ASR / Two Way Traffic
                id == 25330403 || //AMSR / Alternating Traffic

                id == 25151205 || //Retain
                id == 25341500 || //Isolate

                id == 25340600 || //counterattack.
                id == 25340700 || //counterattack by fire.
                //id == G*G*PA----****X || //AoA for Feint - appears to be gone in 2525D
                id == 25271200 || //Blown Bridges Planned
                id == 25271202 || //Blown Bridges Explosives, State of Readiness 1 (Safe)
                id == 25341200) // Follow and Assume
        {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Assumes bbox is of form left,bottom,right,top and it is currently only
     * using the width to calculate a reasonable scale. If the original scale is
     * within the max and min range it returns the original scale.
     *
     * @param bbox
     * @param origScale
     * @return
     */
    private static double getReasonableScale(String bbox, double origScale) {
        try {
            String[] bounds = bbox.split(",");
            double left = Double.valueOf(bounds[0]);
            double right = Double.valueOf(bounds[2]);
            double top = Double.valueOf(bounds[3]);
            double bottom = Double.valueOf(bounds[1]);

            POINT2 ul = new POINT2(left, top);
            POINT2 ur = new POINT2(right, top);

            double widthInMeters;
            if ((left == -180 && right == 180) || (left == 180 && right == -180))
                widthInMeters = 40075017d / 2d; // Earth's circumference / 2
            else
                widthInMeters = mdlGeodesic.geodesic_distance(ul, ur, null, null);

            double minScale = widthInMeters / ((double) _maxPixelWidth / RendererSettings.getInstance().getDeviceDPI() / GeoPixelConversion.INCHES_PER_METER);
            if (origScale < minScale) {
                return minScale;
            }

            double maxScale = widthInMeters / ((double) _minPixelWidth / RendererSettings.getInstance().getDeviceDPI() / GeoPixelConversion.INCHES_PER_METER);
            if (origScale > maxScale) {
                return maxScale;
            }
        } catch (NumberFormatException ignored) {
        }
        return origScale;
    }

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param symbolModifiers {@link Map}, keyed using constants from
     * Modifiers. Pass in comma delimited String for modifiers with multiple
     * values like AM, AN &amp; X
     * @param symbolAttributes {@link Map}, keyed using constants from
     * MilStdAttributes. pass in double[] for AM, AN and X; Strings for the
     * rest.
     * @param format
     * @return
     */
    public static String RenderSymbol(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            Map<String,String> symbolModifiers,
            Map<String,String> symbolAttributes,
            int format)//,
    {
        //System.out.println("MultiPointHandler.RenderSymbol()");
        boolean normalize = true;
        //Double controlLat = 0.0;
        //Double controlLong = 0.0;
        //Double metPerPix = GeoPixelConversion.metersPerPixel(scale);
        //String bbox2=getBoundingRectangle(controlPoints,bbox);
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;
        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = new ArrayList<ShapeInfo>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;
        //diagnostic create geoCoords here
        Point2D coordsUL=null;

        String symbolIsValid = canRenderMultiPoint(symbolCode, symbolModifiers, len);
        if (!symbolIsValid.equals("true")) {
            String ErrorOutput = "";
            ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + " - ID: " + id + " - ");
            ErrorOutput += symbolIsValid; //reason for error
            ErrorOutput += ("\"}");
            ErrorLogger.LogMessage("MultiPointHandler","RenderSymbol",symbolIsValid,Level.WARNING);
            return ErrorOutput;
        }

        if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() != DrawRules.AREA10) // AREA10 can support infinite points
            len = Math.min(len, MSLookup.getInstance().getMSLInfo(symbolCode).getMaxPointCount());
        for (int i = 0; i < len; i++) 
        {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        ArrayList<POINT2> tgPoints = null;
        IPointConversion ipc = null;

        //Deutch moved section 6-29-11
        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        Point2D temp = null;
        Point2D ptGeoUL = null;
        int width = 0;
        int height = 0;
        int leftX = 0;
        int topY = 0;
        int bottomY = 0;
        int rightX = 0;
        int j = 0;
        ArrayList<Point2D> bboxCoords = null;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = null;
            if (bbox.contains(" "))//trapezoid
            {
                bboxCoords = new ArrayList<Point2D>();
                double x = 0;
                double y = 0;
                String[] coords = bbox.split(" ");
                String[] arrCoord;
                for (String coord : coords) {
                    arrCoord = coord.split(",");
                    x = Double.valueOf(arrCoord[0]);
                    y = Double.valueOf(arrCoord[1]);
                    bboxCoords.add(new Point2D.Double(x, y));
                }
                //use the upper left corner of the MBR containing geoCoords
                //to set the converter
                ptGeoUL = getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                String bbox2=getBboxFromCoords(bboxCoords);
                scale = getReasonableScale(bbox2, scale);
                ipc = new PointConverter(left, top, scale);
                Point2D ptPixels = null;
                Point2D ptGeo = null;
                int n = bboxCoords.size();
                //for (j = 0; j < bboxCoords.size(); j++) 
                for (j = 0; j < n; j++) {
                    ptGeo = bboxCoords.get(j);
                    ptPixels = ipc.GeoToPixels(ptGeo);
                    x = ptPixels.getX();
                    y = ptPixels.getY();
                    if (x < 20) {
                        x = 20;
                    }
                    if (y < 20) {
                        y = 20;
                    }
                    ptPixels.setLocation(x, y);
                    //end section
                    bboxCoords.set(j, (Point2D) ptPixels);
                }
            } else//rectangle
            {
                bounds = bbox.split(",");
                left = Double.valueOf(bounds[0]);
                right = Double.valueOf(bounds[2]);
                top = Double.valueOf(bounds[3]);
                bottom = Double.valueOf(bounds[1]);
                scale = getReasonableScale(bbox, scale);
                ipc = new PointConverter(left, top, scale);
            }

            Point2D pt2d = null;
            if (bboxCoords == null) {
                pt2d = new Point2D.Double(left, top);
                temp = ipc.GeoToPixels(pt2d);

                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                pt2d = new Point2D.Double(right, bottom);
                temp = ipc.GeoToPixels(pt2d);

                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //diagnostic clipping does not work at large scales
//                if(scale>10e6)
//                {
//                    //diagnostic replace above by using a new ipc based on the coordinates MBR
//                    coordsUL=getGeoUL(geoCoords);
//                    temp = ipc.GeoToPixels(coordsUL);
//                    left=coordsUL.getX();
//                    top=coordsUL.getY();
//                    //shift the ipc to coordsUL origin so that conversions will be more accurate for large scales.
//                    ipc = new PointConverter(left, top, scale);
//                    //shift the rect to compenstate for the shifted ipc so that we can maintain the original clipping area.
//                    leftX -= (int)temp.getX();
//                    rightX -= (int)temp.getX();
//                    topY -= (int)temp.getY();
//                    bottomY -= (int)temp.getY();
//                    //end diagnostic
//                }
                //end section

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

//        for (int i = 0; i < len; i++) {
//            String[] coordPair = coordinates[i].split(",");
//            Double latitude = Double.valueOf(coordPair[1].trim());
//            Double longitude = Double.valueOf(coordPair[0].trim());
//            geoCoords.add(new Point2D.Double(longitude, latitude));
//        }
        if (ipc == null) {
            Point2D ptCoordsUL = getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true) 
//        if(Math.abs(right-left)>180)
//        {
//            normalize = true;
//            ((PointConverter)ipc).set_normalize(true);
//        } 
//        else {
//            normalize = false;
//            ((PointConverter)ipc).set_normalize(false);
//        }

        //seems to work ok at world view
//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
//        }

        //M. Deutch 10-3-11
        //must shift the rect pixels to synch with the new ipc
        //the old ipc was in synch with the bbox, so rect x,y was always 0,0
        //the new ipc synchs with the upper left of the geocoords so the boox is shifted
        //and therefore the clipping rectangle must shift by the delta x,y between
        //the upper left corner of the original bbox and the upper left corner of the geocoords
        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
//        }

        //disable clipping
        if (ShouldClipSymbol(symbolCode) == false) 
            if(crossesIDL(geoCoords)==false)
            {
                rect = null;
                bboxCoords = null;
            }   
        
        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);

        try {

            //String fillColor = null;
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG){
                // Use dash array and hatch pattern fill for SVG output
                symbolAttributes.put(MilStdAttributes.UseDashArray, "true");
                symbolAttributes.put(MilStdAttributes.UsePatternFill, "true");
            }

            if (symbolModifiers != null || symbolAttributes != null) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            if (bboxCoords == null) {
                clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            if (format == WebRenderer.OUTPUT_FORMAT_JSON) {
                jsonOutput.append("{\"type\":\"symbol\",");
                jsonContent = JSONize(shapes, modifiers, ipc, true, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            }
            else if (format == WebRenderer.OUTPUT_FORMAT_GEOJSON)
            {
                /*
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);
                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\",\"wasClipped\":\"");
                jsonOutput.append(String.valueOf(mSymbol.get_WasClipped()));
                jsonOutput.append("\"}}");         */

                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);

                //moving meta data properties to the last feature with no coords as feature collection doesn't allow properties
                jsonOutput.replace(jsonOutput.toString().length()-1,jsonOutput.toString().length(),"" );
                jsonOutput.append(",{\"type\": \"Feature\",\"geometry\": { \"type\": \"Polygon\",\"coordinates\": [ ]}");

                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\",\"wasClipped\":\"");
                jsonOutput.append(String.valueOf(mSymbol.get_WasClipped()));
                //jsonOutput.append("\"}}");

                jsonOutput.append("\"}}]}");
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG) {
                String textColor = mSymbol.getTextColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextColor(), false) : "";
                String backgroundColor = mSymbol.getTextBackgroundColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextBackgroundColor(), false) : "";
                //returns an svg with a geoTL and geoBR value to use to place the canvas on the map
                jsonContent = MultiPointHandlerSVG.GeoSVGize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor, backgroundColor, mSymbol.get_WasClipped());
                jsonOutput.append(jsonContent);
            }

        } catch (Exception exc) {
            String st = JavaRendererUtilities.getStackTrace(exc);
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append(st);
            jsonOutput.append("\"}");

            ErrorLogger.LogException("MultiPointHandler", "RenderSymbol", exc);
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("Scale: " + scale);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
            if (jsonOutput != null) {
                System.out.println(jsonOutput.toString());
            }
        }

        ErrorLogger.LogMessage("MultiPointHandler", "RenderSymbol()", "exit RenderSymbol", Level.FINER);
        return jsonOutput.toString();

    }

    /**
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param scale
     * @param bbox
     * @param symbolModifiers
     * @param symbolAttributes
     * @return
     */
    public static MilStdSymbol RenderSymbolAsMilStdSymbol(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            Double scale,
            String bbox,
            Map<String,String> symbolModifiers,
            Map<String,String> symbolAttributes)//,
    //ArrayList<ShapeInfo>shapes)
    {
        MilStdSymbol mSymbol = null;
        //System.out.println("MultiPointHandler.RenderSymbol()");
        boolean normalize = true;
        Double controlLat = 0.0;
        Double controlLong = 0.0;
        //String jsonContent = "";

        Rectangle rect = null;

        //for symbol & line fill
        ArrayList<POINT2> tgPoints = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = null;//new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = null;//new ArrayList<ShapeInfo>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;

        IPointConversion ipc = null;

        //Deutch moved section 6-29-11
        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        Point2D temp = null;
        Point2D ptGeoUL = null;
        int width = 0;
        int height = 0;
        int leftX = 0;
        int topY = 0;
        int bottomY = 0;
        int rightX = 0;
        int j = 0;
        ArrayList<Point2D> bboxCoords = null;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = null;
            if (bbox.contains(" "))//trapezoid
            {
                bboxCoords = new ArrayList<Point2D>();
                double x = 0;
                double y = 0;
                String[] coords = bbox.split(" ");
                String[] arrCoord;
                for (String coord : coords) {
                    arrCoord = coord.split(",");
                    x = Double.valueOf(arrCoord[0]);
                    y = Double.valueOf(arrCoord[1]);
                    bboxCoords.add(new Point2D.Double(x, y));
                }
                //use the upper left corner of the MBR containing geoCoords
                //to set the converter
                ptGeoUL = getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                ipc = new PointConverter(left, top, scale);
                Point2D ptPixels = null;
                Point2D ptGeo = null;
                int n = bboxCoords.size();
                //for (j = 0; j < bboxCoords.size(); j++) 
                for (j = 0; j < n; j++) {
                    ptGeo = bboxCoords.get(j);
                    ptPixels = ipc.GeoToPixels(ptGeo);
                    x = ptPixels.getX();
                    y = ptPixels.getY();
                    if (x < 20) {
                        x = 20;
                    }
                    if (y < 20) {
                        y = 20;
                    }
                    ptPixels.setLocation(x, y);
                    //end section
                    bboxCoords.set(j, (Point2D) ptPixels);
                }
            } else//rectangle
            {
                bounds = bbox.split(",");
                left = Double.valueOf(bounds[0]);
                right = Double.valueOf(bounds[2]);
                top = Double.valueOf(bounds[3]);
                bottom = Double.valueOf(bounds[1]);
                scale = getReasonableScale(bbox, scale);
                ipc = new PointConverter(left, top, scale);
            }

            Point2D pt2d = null;
            if (bboxCoords == null) {
                pt2d = new Point2D.Double(left, top);
                temp = ipc.GeoToPixels(pt2d);

                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                pt2d = new Point2D.Double(right, bottom);
                temp = ipc.GeoToPixels(pt2d);

                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //diagnostic clipping does not work for large scales
//                if (scale > 10e6) {
//                    //get widest point in the AOI
//                    double midLat = 0;
//                    if (bottom < 0 && top > 0) {
//                        midLat = 0;
//                    } else if (bottom < 0 && top < 0) {
//                        midLat = top;
//                    } else if (bottom > 0 && top > 0) {
//                        midLat = bottom;
//                    }
//
//                    temp = ipc.GeoToPixels(new Point2D.Double(right, midLat));
//                    rightX = (int) temp.getX();
//                }
                //end section

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                if(width==0 || height==0)
                    rect=null;
                else
                    rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }
        //end section

        //check for required points & parameters
        String symbolIsValid = canRenderMultiPoint(symbolCode, symbolModifiers, len);
        if (!symbolIsValid.equals("true")) {
            ErrorLogger.LogMessage("MultiPointHandler", "RenderSymbolAsMilStdSymbol", symbolIsValid, Level.WARNING);
            return mSymbol;
        }

        if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() != DrawRules.AREA10) // AREA10 can support infinite points
            len = Math.min(len, MSLookup.getInstance().getMSLInfo(symbolCode).getMaxPointCount());
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim());
            Double longitude = Double.valueOf(coordPair[0].trim());
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }
        if (ipc == null) {
            Point2D ptCoordsUL = getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }
        //if (crossesIDL(geoCoords) == true) 
//        if(Math.abs(right-left)>180)
//        {
//            normalize = true;
//            ((PointConverter)ipc).set_normalize(true);
//        } 
//        else {
//            normalize = false;
//            ((PointConverter)ipc).set_normalize(false);
//        }

        //seems to work ok at world view
//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords);
//        }

        //M. Deutch 10-3-11
        //must shift the rect pixels to synch with the new ipc
        //the old ipc was in synch with the bbox, so rect x,y was always 0,0
        //the new ipc synchs with the upper left of the geocoords so the boox is shifted
        //and therefore the clipping rectangle must shift by the delta x,y between
        //the upper left corner of the original bbox and the upper left corner of the geocoords
        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
//        }

        //disable clipping
        if (ShouldClipSymbol(symbolCode) == false) 
            if(crossesIDL(geoCoords)==false)
            {
                rect = null;
                bboxCoords=null;
            }
        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);
        
        try {

            String fillColor = null;
            mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

//            mSymbol.setUseDashArray(true);

            if (symbolModifiers != null || symbolAttributes != null) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            if (mSymbol.getFillColor() != null) {
                Color fc = mSymbol.getFillColor();
                fillColor = Integer.toHexString(fc.getRGB());

            }

            if (bboxCoords == null) {
                clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            //convert points////////////////////////////////////////////////////
            ArrayList<ArrayList<Point2D>> polylines = null;
            ArrayList<ArrayList<Point2D>> newPolylines = null;
            ArrayList<Point2D> newLine = null;
            for (ShapeInfo shape : shapes) {
                polylines = shape.getPolylines();
                //System.out.println("pixel polylines: " + String.valueOf(polylines));
                newPolylines = ConvertPolylinePixelsToCoords(polylines, ipc, normalize);
                shape.setPolylines(newPolylines);
            }

            for (ShapeInfo label : modifiers) {
                Point2D pixelCoord = label.getModifierPosition();
                if (pixelCoord == null) {
                    pixelCoord = label.getGlyphPosition();
                }
                Point2D geoCoord = ipc.PixelsToGeo(pixelCoord);

                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
                }

                double latitude = geoCoord.getY();
                double longitude = geoCoord.getX();
                label.setModifierPosition(new Point2D.Double(longitude, latitude));

            }   

            ////////////////////////////////////////////////////////////////////
            mSymbol.setModifierShapes(modifiers);
            mSymbol.setSymbolShapes(shapes);

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            System.out.println("Symbol Code: " + symbolCode);
            exc.printStackTrace();
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("Scale: " + scale);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                //System.out.println("Pixel: " + pixels.toString());
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
        }

        return mSymbol;

    }

    private static ArrayList<ArrayList<Point2D>> ConvertPolylinePixelsToCoords(ArrayList<ArrayList<Point2D>> polylines, IPointConversion ipc, Boolean normalize) {
        ArrayList<ArrayList<Point2D>> newPolylines = new ArrayList<ArrayList<Point2D>>();

        double latitude = 0;
        double longitude = 0;
        ArrayList<Point2D> newLine = null;
        try {
            for (ArrayList<Point2D> line : polylines) {
                newLine = new ArrayList<Point2D>();
                for (Point2D pt : line) {
                    Point2D geoCoord = ipc.PixelsToGeo(pt);

                    if (normalize) {
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
                    }

                    latitude = geoCoord.getY();
                    longitude = geoCoord.getX();
                    newLine.add(new Point2D.Double(longitude, latitude));
                }
                newPolylines.add(newLine);
            }
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
            exc.printStackTrace();
        }
        return newPolylines;
    }

    /**
     * Multipoint Rendering on flat 2D maps
     *
     * @param id A unique ID for the symbol. only used in KML currently
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param pixelWidth pixel dimensions of the viewable map area
     * @param pixelHeight pixel dimensions of the viewable map area
     * @param bbox The viewable area of the map. Passed in the format of a
     * string "lowerLeftX,lowerLeftY,upperRightX,upperRightY." example:
     * "-50.4,23.6,-42.2,24.2"
     * @param symbolModifiers Modifier with multiple values should be comma
     * delimited
     * @param symbolAttributes
     * @param format An enumeration: 0 for KML, 1 for JSON.
     * @return A JSON or KML string representation of the graphic.
     */
    public static String RenderSymbol2D(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            int pixelWidth,
            int pixelHeight,
            String bbox,
            Map<String,String> symbolModifiers,
            Map<String,String> symbolAttributes,
            int format) {
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;

        ArrayList<POINT2> tgPoints = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<ShapeInfo> shapes = new ArrayList<ShapeInfo>();
        ArrayList<ShapeInfo> modifiers = new ArrayList<ShapeInfo>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;
        IPointConversion ipc = null;

        //check for required points & parameters
        String symbolIsValid = canRenderMultiPoint(symbolCode, symbolModifiers, len);
        if (!symbolIsValid.equals("true")) {
            String ErrorOutput = "";
            ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + " - ID: " + id + " - ");
            ErrorOutput += symbolIsValid; //reason for error
            ErrorOutput += ("\"}");
            ErrorLogger.LogMessage("MultiPointHandler", "RenderSymbol2D", symbolIsValid, Level.WARNING);
            return ErrorOutput;
        }

        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = bbox.split(",");

            left = Double.valueOf(bounds[0]).doubleValue();
            right = Double.valueOf(bounds[2]).doubleValue();
            top = Double.valueOf(bounds[3]).doubleValue();
            bottom = Double.valueOf(bounds[1]).doubleValue();

            ipc = new PointConversion(pixelWidth, pixelHeight, top, left, bottom, right);
        } else {
            System.out.println("Bad bbox value: " + bbox);
            System.out.println("bbox is viewable area of the map.  Passed in the format of a string \"lowerLeftX,lowerLeftY,upperRightX,upperRightY.\" example: \"-50.4,23.6,-42.2,24.2\"");
            return "ERROR - Bad bbox value: " + bbox;
        }
        //end section

        //get coordinates
        if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() != DrawRules.AREA10) // AREA10 can support infinite points
            len = Math.min(len, MSLookup.getInstance().getMSLInfo(symbolCode).getMaxPointCount());
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }

        try {
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG){
                // Use dash array and hatch pattern fill for SVG output
                symbolAttributes.put(MilStdAttributes.UseDashArray, "true");
                symbolAttributes.put(MilStdAttributes.UsePatternFill, "true");
            }

            if (symbolModifiers != null && symbolModifiers.equals("") == false) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            //build clipping bounds
            Point2D temp = null;
            int leftX;
            int topY;
            int bottomY;
            int rightX;
            int width;
            int height;
            boolean normalize = false;
//            if(Math.abs(right-left)>180)
//            {
//                ((PointConversion)ipc).set_normalize(true);                
//                normalize=true;
//            }
//            else      
//            {
//                ((PointConversion)ipc).set_normalize(false);
//            }
            if (ShouldClipSymbol(symbolCode)  || crossesIDL(geoCoords)) 
            {
                Point2D lt=new Point2D.Double(left,top);
                //temp = ipc.GeoToPixels(new Point2D.Double(left, top));
                temp = ipc.GeoToPixels(lt);
                leftX = (int) temp.getX();
                topY = (int) temp.getY();

                Point2D rb=new Point2D.Double(right,bottom);
                //temp = ipc.GeoToPixels(new Point2D.Double(right, bottom));
                temp = ipc.GeoToPixels(rb);
                bottomY = (int) temp.getY();
                rightX = (int) temp.getX();
                //////////////////

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }

            //new interface
            //IMultiPointRenderer mpr = MultiPointRenderer.getInstance();
            clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            //boolean normalize = false;

            if (format == WebRenderer.OUTPUT_FORMAT_JSON) {
                jsonOutput.append("{\"type\":\"symbol\",");
                //jsonContent = JSONize(shapes, modifiers, ipc, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            }  else if (format == WebRenderer.OUTPUT_FORMAT_GEOJSON) {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);

                //moving meta data properties to the last feature with no coords as feature collection doesn't allow properties
                jsonOutput.replace(jsonOutput.toString().length()-1,jsonOutput.toString().length(),"" );
                jsonOutput.append(",{\"type\": \"Feature\",\"geometry\": { \"type\": \"Polygon\",\"coordinates\": [ ]}");

                jsonOutput.append(",\"properties\":{\"id\":\"");
                jsonOutput.append(id);
                jsonOutput.append("\",\"name\":\"");
                jsonOutput.append(name);
                jsonOutput.append("\",\"description\":\"");
                jsonOutput.append(description);
                jsonOutput.append("\",\"symbolID\":\"");
                jsonOutput.append(symbolCode);
                jsonOutput.append("\",\"wasClipped\":\"");
                jsonOutput.append(String.valueOf(mSymbol.get_WasClipped()));
                //jsonOutput.append("\"}}");

                jsonOutput.append("\"}}]}");
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG) {
                String textColor = mSymbol.getTextColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextColor(), false) : "";
                String backgroundColor = mSymbol.getTextBackgroundColor() != null ? RendererUtilities.colorToHexString(mSymbol.getTextBackgroundColor(), false) : "";
                //returns an svg with a geoTL and geoBR value to use to place the canvas on the map
                jsonContent = MultiPointHandlerSVG.GeoSVGize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor, backgroundColor, mSymbol.get_WasClipped());
                jsonOutput.append(jsonContent);
            }

        } catch (Exception exc) {
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append(ErrorLogger.getStackTrace(exc));
            jsonOutput.append("\"}");
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                //System.out.println("Pixel: " + pixels.toString());
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
            if (jsonOutput != null) {
                System.out.println(jsonOutput.toString());
            }
        }

        return jsonOutput.toString();

    }

    /**
     * For Mike Deutch testing
     *
     * @param id
     * @param name
     * @param description
     * @param symbolCode
     * @param controlPoints
     * @param pixelWidth
     * @param pixelHeight
     * @param bbox
     * @param symbolModifiers
     * @param shapes
     * @param modifiers
     * @param format
     * @return
     * @deprecated
     */
    public static String RenderSymbol2DX(String id,
            String name,
            String description,
            String symbolCode,
            String controlPoints,
            int pixelWidth,
            int pixelHeight,
            String bbox,
            HashMap<String,String> symbolModifiers,
            HashMap<String,String> symbolAttributes,
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers,
            int format)//,
    //ArrayList<ShapeInfo>shapes)
    {

        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;

        String[] coordinates = controlPoints.split(" ");
        TGLight tgl = new TGLight();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        IPointConversion ipc = null;

        Double left = 0.0;
        Double right = 0.0;
        Double top = 0.0;
        Double bottom = 0.0;
        if (bbox != null && bbox.equals("") == false) {
            String[] bounds = bbox.split(",");

            left = Double.valueOf(bounds[0]).doubleValue();
            right = Double.valueOf(bounds[2]).doubleValue();
            top = Double.valueOf(bounds[3]).doubleValue();
            bottom = Double.valueOf(bounds[1]).doubleValue();

            ipc = new PointConversion(pixelWidth, pixelHeight, top, left, bottom, right);
        } else {
            System.out.println("Bad bbox value: " + bbox);
            System.out.println("bbox is viewable area of the map.  Passed in the format of a string \"lowerLeftX,lowerLeftY,upperRightX,upperRightY.\" example: \"-50.4,23.6,-42.2,24.2\"");
            return "ERROR - Bad bbox value: " + bbox;
        }
        //end section

        //get coordinates
        int len = coordinates.length;
        for (int i = 0; i < len; i++) {
            String[] coordPair = coordinates[i].split(",");
            Double latitude = Double.valueOf(coordPair[1].trim()).doubleValue();
            Double longitude = Double.valueOf(coordPair[0].trim()).doubleValue();
            geoCoords.add(new Point2D.Double(longitude, latitude));
        }

        try {
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (symbolModifiers != null && symbolModifiers.equals("") == false) {
                populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            clsRenderer.renderWithPolylines(mSymbol, ipc, rect);
            shapes = mSymbol.getSymbolShapes();
            modifiers = mSymbol.getModifierShapes();

            boolean normalize = false;

            if (format == WebRenderer.OUTPUT_FORMAT_JSON) {
                jsonOutput.append("{\"type\":\"symbol\",");
                jsonContent = JSONize(shapes, modifiers, ipc, false, normalize);
                jsonOutput.append(jsonContent);
                jsonOutput.append("}");
            }

        } catch (Exception exc) {
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage() + " - ");
            jsonOutput.append("\"}");
        }

        boolean debug = true;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
            }
            if (tgl != null && tgl.get_Pixels() != null)//pixels != null
            {
                //System.out.println("Pixel: " + pixels.toString());
                System.out.println("Pixel: " + tgl.get_Pixels().toString());
            }
            if (bbox != null) {
                System.out.println("geo bounds: " + bbox);
            }
            if (rect != null) {
                System.out.println("pixel bounds: " + rect.toString());
            }
            if (jsonOutput != null) {
                System.out.println(jsonOutput.toString());
            }
        }
        return jsonOutput.toString();

    }

    private static SymbolInfo MilStdSymbolToSymbolInfo(MilStdSymbol symbol) {
        SymbolInfo si = null;

        ArrayList<TextInfo> tiList = new ArrayList<TextInfo>();
        ArrayList<LineInfo> liList = new ArrayList<LineInfo>();

        TextInfo tiTemp = null;
        LineInfo liTemp = null;
        ShapeInfo siTemp = null;

        ArrayList<ShapeInfo> lines = symbol.getSymbolShapes();
        ArrayList<ShapeInfo> modifiers = symbol.getModifierShapes();

        int lineCount = lines.size();
        int modifierCount = modifiers.size();
        for (int i = 0; i < lineCount; i++) {
            siTemp = lines.get(i);
            if (siTemp.getPolylines() != null) {
                liTemp = new LineInfo();
                liTemp.setFillColor(siTemp.getFillColor());
                liTemp.setLineColor(siTemp.getLineColor());
                liTemp.setPolylines(siTemp.getPolylines());
                liTemp.setStroke(siTemp.getStroke());
                liList.add(liTemp);
            }
        }

        for (int j = 0; j < modifierCount; j++) {
            tiTemp = new TextInfo();
            siTemp = modifiers.get(j);
            if (siTemp.getModifierString() != null) {
                tiTemp.setModifierString(siTemp.getModifierString());
                tiTemp.setModifierStringPosition(siTemp.getModifierPosition());
                tiTemp.setModifierStringAngle(siTemp.getModifierAngle());
                tiList.add(tiTemp);
            }
        }
        si = new SymbolInfo(tiList, liList);
        return si;
    }

    /**
     * Populates a symbol with the modifiers from a JSON string. This function
     * will overwrite any previously populated modifier data.
     *
     *
     *
     * @param symbol An existing MilStdSymbol
     * @return
     */
    private static boolean populateModifiers(Map<String,String> saModifiers, Map<String,String> saAttributes, MilStdSymbol symbol) {
        Map<String,String> modifiers = new HashMap<>();
        Map<String,String> attributes = saAttributes;

        // Stores array graphic modifiers for MilStdSymbol;
        ArrayList<Double> altitudes = null;
        ArrayList<Double> azimuths = null;
        ArrayList<Double> distances = null;

        // Stores colors for symbol.
        String fillColor = null;
        String lineColor = null;
        String textColor = null;
        String textBackgroundColor = null;

        int lineWidth = 0;
        String altMode = null;
        boolean useDashArray = symbol.getUseDashArray();
        boolean usePatternFill = symbol.getUseFillPattern();
        int patternFillType = 0;
        boolean hideOptionalLabels = false;
        DistanceUnit distanceUnit = null;
        DistanceUnit altitudeUnit = null;
        int pixelSize = 50;
        boolean keepUnitRatio = true;

        try {

            // The following attirubtes are labels.  All of them
            // are strings and can be added on the creation of the
            // MilStdSymbol by adding to a Map and passing in the
            // modifiers parameter.
            if (saModifiers != null) {
                if (saModifiers.containsKey(Modifiers.C_QUANTITY)) {
                    modifiers.put(Modifiers.C_QUANTITY, String.valueOf(saModifiers.get(Modifiers.C_QUANTITY)));
                }

                if (saModifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    modifiers.put(Modifiers.H_ADDITIONAL_INFO_1, String.valueOf(saModifiers.get(Modifiers.H_ADDITIONAL_INFO_1)));
                }

                if (saModifiers.containsKey(Modifiers.H1_ADDITIONAL_INFO_2)) {
                    modifiers.put(Modifiers.H1_ADDITIONAL_INFO_2, String.valueOf(saModifiers.get(Modifiers.H1_ADDITIONAL_INFO_2)));
                }

                if (saModifiers.containsKey(Modifiers.H2_ADDITIONAL_INFO_3)) {
                    modifiers.put(Modifiers.H2_ADDITIONAL_INFO_3, String.valueOf(saModifiers.get(Modifiers.H2_ADDITIONAL_INFO_3)));
                }

                if (saModifiers.containsKey(Modifiers.N_HOSTILE)) {
                    if (saModifiers.get(Modifiers.N_HOSTILE) == null) {
                        modifiers.put(Modifiers.N_HOSTILE, "");
                    } else {
                        modifiers.put(Modifiers.N_HOSTILE, String.valueOf(saModifiers.get(Modifiers.N_HOSTILE)));
                    }
                }

                if (saModifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT)) {
                    modifiers.put(Modifiers.Q_DIRECTION_OF_MOVEMENT, String.valueOf(saModifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT)));
                }

                if (saModifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1, String.valueOf(saModifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1)));
                }

                if (saModifiers.containsKey(Modifiers.T1_UNIQUE_DESIGNATION_2)) {
                    modifiers.put(Modifiers.T1_UNIQUE_DESIGNATION_2, String.valueOf(saModifiers.get(Modifiers.T1_UNIQUE_DESIGNATION_2)));
                }

                if (saModifiers.containsKey(Modifiers.V_EQUIP_TYPE)) {
                    modifiers.put(Modifiers.V_EQUIP_TYPE, String.valueOf(saModifiers.get(Modifiers.V_EQUIP_TYPE)));
                }

                if (saModifiers.containsKey(Modifiers.AS_COUNTRY)) {
                    modifiers.put(Modifiers.AS_COUNTRY, String.valueOf(saModifiers.get(Modifiers.AS_COUNTRY)));
                } else if (SymbolID.getCountryCode(symbol.getSymbolID()) > 0 && !GENCLookup.getInstance().get3CharCode(SymbolID.getCountryCode(symbol.getSymbolID())).equals("")) {
                    modifiers.put(Modifiers.AS_COUNTRY, GENCLookup.getInstance().get3CharCode(SymbolID.getCountryCode(symbol.getSymbolID())));
                }

                if (saModifiers.containsKey(Modifiers.AP_TARGET_NUMBER)) {
                    modifiers.put(Modifiers.AP_TARGET_NUMBER, String.valueOf(saModifiers.get(Modifiers.AP_TARGET_NUMBER)));
                }

                if (saModifiers.containsKey(Modifiers.W_DTG_1)) {
                    modifiers.put(Modifiers.W_DTG_1, String.valueOf(saModifiers.get(Modifiers.W_DTG_1)));
                }

                if (saModifiers.containsKey(Modifiers.W1_DTG_2)) {
                    modifiers.put(Modifiers.W1_DTG_2, String.valueOf(saModifiers.get(Modifiers.W1_DTG_2)));
                }

                if (saModifiers.containsKey(Modifiers.Y_LOCATION)) {
                    modifiers.put(Modifiers.Y_LOCATION, String.valueOf(saModifiers.get(Modifiers.Y_LOCATION)));
                }

                //Required multipoint modifier arrays
                if (saModifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    altitudes = new ArrayList<Double>();
                    String[] arrAltitudes = String.valueOf(saModifiers.get(Modifiers.X_ALTITUDE_DEPTH)).split(",");
                    for (String x : arrAltitudes) {
                        if (x.equals("") != true) {
                            altitudes.add(Double.parseDouble(x));
                        }
                    }
                }

                if (saModifiers.containsKey(Modifiers.AM_DISTANCE)) {
                    distances = new ArrayList<Double>();
                    String[] arrDistances = String.valueOf(saModifiers.get(Modifiers.AM_DISTANCE)).split(",");
                    for (String am : arrDistances) {
                        if (am.equals("") != true) {
                            distances.add(Double.parseDouble(am));
                        }
                    }
                }

                if (saModifiers.containsKey(Modifiers.AN_AZIMUTH)) {
                    azimuths = new ArrayList<Double>();
                    String[] arrAzimuths = String.valueOf(saModifiers.get(Modifiers.AN_AZIMUTH)).split(",");;
                    for (String an : arrAzimuths) {
                        if (an.equals("") != true) {
                            azimuths.add(Double.parseDouble(an));
                        }
                    }
                }
            }
            if (saAttributes != null) {
                // These properties are ints, not labels, they are colors.//////////////////
                if (saAttributes.containsKey(MilStdAttributes.FillColor)) {
                    fillColor = (String) saAttributes.get(MilStdAttributes.FillColor);
                }

                if (saAttributes.containsKey(MilStdAttributes.LineColor)) {
                    lineColor = (String) saAttributes.get(MilStdAttributes.LineColor);
                }

                if (saAttributes.containsKey(MilStdAttributes.LineWidth)) {
                    lineWidth = Integer.parseInt(saAttributes.get(MilStdAttributes.LineWidth));
                }
                
                if (saAttributes.containsKey(MilStdAttributes.TextColor)) {
                    textColor = (String) saAttributes.get(MilStdAttributes.TextColor);
                }
                
                if (saAttributes.containsKey(MilStdAttributes.TextBackgroundColor)) {
                    textBackgroundColor = (String) saAttributes.get(MilStdAttributes.TextBackgroundColor);
                }

                if (saAttributes.containsKey(MilStdAttributes.AltitudeMode)) {
                    altMode = saAttributes.get(MilStdAttributes.AltitudeMode);
                }

                if (saAttributes.containsKey(MilStdAttributes.UseDashArray)) {
                    useDashArray = Boolean.parseBoolean(saAttributes.get(MilStdAttributes.UseDashArray));
                }

                if (saAttributes.containsKey(MilStdAttributes.UsePatternFill)) {
                    usePatternFill = Boolean.parseBoolean(saAttributes.get(MilStdAttributes.UsePatternFill));
                }

                if (saAttributes.containsKey(MilStdAttributes.PatternFillType)) {
                    patternFillType = Integer.parseInt((saAttributes.get(MilStdAttributes.PatternFillType)));
                }

                if (saAttributes.containsKey(MilStdAttributes.HideOptionalLabels)) {
                    hideOptionalLabels = Boolean.parseBoolean(saAttributes.get(MilStdAttributes.HideOptionalLabels));
                }

                if(saAttributes.containsKey(MilStdAttributes.AltitudeUnits)) {
                    altitudeUnit = DistanceUnit.parse(saAttributes.get(MilStdAttributes.AltitudeUnits));
                }

                if(saAttributes.containsKey(MilStdAttributes.DistanceUnits)) {
                    distanceUnit = DistanceUnit.parse(saAttributes.get(MilStdAttributes.DistanceUnits));
                }

                if(saAttributes.containsKey(MilStdAttributes.PixelSize)) {
                    pixelSize = Integer.parseInt(saAttributes.get(MilStdAttributes.PixelSize));
                    symbol.setUnitSize(pixelSize);
                }

                if (saAttributes.containsKey(MilStdAttributes.KeepUnitRatio)) {
                    keepUnitRatio = Boolean.parseBoolean(saAttributes.get(MilStdAttributes.KeepUnitRatio));
                    symbol.setKeepUnitRatio(keepUnitRatio);
                }
            }

            symbol.setModifierMap(modifiers);

            if (fillColor != null && fillColor.equals("") == false) {
                symbol.setFillColor(RendererUtilities.getColorFromHexString(fillColor));
            } 

            if (lineColor != null && lineColor.equals("") == false) {
                symbol.setLineColor(RendererUtilities.getColorFromHexString(lineColor));
                symbol.setTextColor(RendererUtilities.getColorFromHexString(lineColor));
            }
            else if(symbol.getLineColor()==null)
                symbol.setLineColor(Color.black);

            if (lineWidth > 0) {
                symbol.setLineWidth(lineWidth);
            }
            
            if (textColor != null && textColor.equals("") == false) {
                symbol.setTextColor(RendererUtilities.getColorFromHexString(textColor));
            } else if(symbol.getTextColor()==null)
                symbol.setTextColor(Color.black);
                
            if (textBackgroundColor != null && textBackgroundColor.equals("") == false) {
                symbol.setTextBackgroundColor(RendererUtilities.getColorFromHexString(textBackgroundColor));
            }

            if (altMode != null) {
                symbol.setAltitudeMode(altMode);
            }

            symbol.setUseDashArray(useDashArray);
            symbol.setUseFillPattern(usePatternFill);
            symbol.setHideOptionalLabels(hideOptionalLabels);
            symbol.setAltitudeUnit(altitudeUnit);
            symbol.setDistanceUnit(distanceUnit);

            // Check grpahic modifiers variables.  If we set earlier, populate
            // the fields, otherwise, ignore.
            if (altitudes != null) {
                symbol.setModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH, altitudes);
            }
            if (distances != null) {
                symbol.setModifiers_AM_AN_X(Modifiers.AM_DISTANCE, distances);
            }

            if (azimuths != null) {
                symbol.setModifiers_AM_AN_X(Modifiers.AN_AZIMUTH, azimuths);
            }

            //Check if sector range fan has required min range
            if (SymbolUtilities.getBasicSymbolID(symbol.getSymbolID()).equals("25242200")) {
                if (symbol.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH) != null
                        && symbol.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE) != null) {
                    int anCount = symbol.getModifiers_AM_AN_X(Modifiers.AN_AZIMUTH).size();
                    int amCount = symbol.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE).size();
                    ArrayList<Double> am = null;
                    if (amCount < ((anCount / 2) + 1)) {
                        am = symbol.getModifiers_AM_AN_X(Modifiers.AM_DISTANCE);
                        if (am.get(0) != 0.0) {
                            am.add(0, 0.0);
                        }
                    }
                }
            }
        } catch (Exception exc2) {
            ErrorLogger.LogException("MPH.populateModifiers", "PopulateModifiers", exc2);
        }
        return true;

    }

    private static String KMLize(String id, String name,
            String description,
            String symbolCode,
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers,
            IPointConversion ipc,
            boolean normalize, Color textColor) {

        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        ShapeInfo tempModifier = null;

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        int len = shapes.size();
        kml.append("<Folder id=\"" + id + "\">");
        kml.append("<name>" + cdataStart + name + cdataEnd + "</name>");
        kml.append("<visibility>1</visibility>");
        for (int i = 0; i < len; i++) {

            String shapesToAdd = ShapeToKMLString(name, description, symbolCode, shapes.get(i), ipc, normalize);
            kml.append(shapesToAdd);
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {

            tempModifier = modifiers.get(j);

            //if(geMap)//if using google earth
            //assume kml text is going to be centered
            //AdjustModifierPointToCenter(tempModifier);

            String labelsToAdd = LabelToKMLString(tempModifier, ipc, normalize, textColor);
            kml.append(labelsToAdd);
        }

        kml.append("</Folder>");
        return kml.toString();
    }

    /**
     * 
     * @param shapes
     * @param modifiers
     * @param ipc
     * @param geMap
     * @param normalize
     * @return 
     * @deprecated Use GeoJSONize()
     */
    private static String JSONize(ArrayList<ShapeInfo> shapes, ArrayList<ShapeInfo> modifiers, IPointConversion ipc, Boolean geMap, boolean normalize) {
        String polygons = "";
        String lines = "";
        String labels = "";
        String jstr = "";
        ShapeInfo tempModifier = null;

        int len = shapes.size();
        for (int i = 0; i < len; i++) {
            if (jstr.length() > 0) {
                jstr += ",";
            }
            String shapesToAdd = ShapeToJSONString(shapes.get(i), ipc, geMap, normalize);
            if (shapesToAdd.length() > 0) {
                if (shapesToAdd.startsWith("line", 2)) {
                    if (lines.length() > 0) {
                        lines += ",";
                    }

                    lines += shapesToAdd;
                } else if (shapesToAdd.startsWith("polygon", 2)) {
                    if (polygons.length() > 0) {
                        polygons += ",";
                    }

                    polygons += shapesToAdd;
                }
            }
        }

        jstr += "\"polygons\": [" + polygons + "],"
                + "\"lines\": [" + lines + "],";
        int len2 = modifiers.size();
        labels = "";
        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);
            if (geMap) {
                AdjustModifierPointToCenter(tempModifier);
            }
            String labelsToAdd = LabelToJSONString(tempModifier, ipc, normalize);
            if (labelsToAdd.length() > 0) {
                if (labels.length() > 0) {
                    labels += ",";
                }

                labels += labelsToAdd;

            }
        }
        jstr += "\"labels\": [" + labels + "]";
        return jstr;
    }

    private static Color getIdealTextBackgroundColor(Color fgColor) {
        //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "in function", Level.SEVERE);
        try {
            //an array of three elements containing the
            //hue, saturation, and brightness (in that order),
            //of the color with the indicated red, green, and blue components/
            float hsbvals[] = new float[3];

            if (fgColor != null) {/*
                 Color.RGBtoHSB(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue(), hsbvals);

                 if(hsbvals != null)
                 {
                 //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "length: " + String.valueOf(hsbvals.length));
                 //ErrorLogger.LogMessage("SymbolDraw","getIdealtextBGColor", "H: " + String.valueOf(hsbvals[0]) + " S: " + String.valueOf(hsbvals[1]) + " B: " + String.valueOf(hsbvals[2]),Level.SEVERE);
                 if(hsbvals[2] > 0.6)
                 return Color.BLACK;
                 else
                 return Color.WHITE;
                 }*/

                int nThreshold = RendererSettings.getInstance().getTextBackgroundAutoColorThreshold();//160;
                int bgDelta = (int) ((fgColor.getRed() * 0.299) + (fgColor.getGreen() * 0.587) + (fgColor.getBlue() * 0.114));
                //ErrorLogger.LogMessage("bgDelta: " + String.valueOf(255-bgDelta));
                //if less than threshold, black, otherwise white.
                //return (255 - bgDelta < nThreshold) ? Color.BLACK : Color.WHITE;//new Color(0, 0, 0, fgColor.getAlpha())
                return (255 - bgDelta < nThreshold) ? new Color(0, 0, 0, fgColor.getAlpha()) : new Color(255, 255, 255, fgColor.getAlpha());
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("SymbolDraw", "getIdealtextBGColor", exc);
        }
        return Color.WHITE;
    }

    private static String LabelToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {

        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();

        Color outlineColor = SymbolDraw.getIdealTextBackgroundColor(textColor);
        if (textBackgroundColor != null) {
            outlineColor = textBackgroundColor;
        }

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        String text = shapeInfo.getModifierString();

        int justify = shapeInfo.getTextJustify();
        String strJustify = "left";
        if (justify == 0) {
            strJustify = "left";
        } else if (justify == 1) {
            strJustify = "center";
        } else if (justify == 2) {
            strJustify = "right";
        }

        RendererSettings RS = RendererSettings.getInstance();

        if (text != null && text.equals("") == false) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"label\":\"");
            JSONed.append(text);
            JSONed.append("\",\"pointRadius\":0,\"fontColor\":\"");
            JSONed.append(RendererUtilities.colorToHexString(textColor, false));
            JSONed.append("\",\"fontSize\":\"");
            JSONed.append(String.valueOf(RS.getMPLabelFont().getSize()) + "pt\"");
            JSONed.append(",\"fontFamily\":\"");
            JSONed.append(RS.getMPLabelFont().getName());
            JSONed.append(", sans-serif");

            if (RS.getMPLabelFont().getStyle() == Font.BOLD) {
                JSONed.append("\",\"fontWeight\":\"bold\"");
            } else {
                JSONed.append("\",\"fontWeight\":\"normal\"");
            }

            //JSONed.append(",\"labelAlign\":\"lm\"");
            JSONed.append(",\"labelAlign\":\"");
            JSONed.append(strJustify);
            JSONed.append("\",\"labelBaseline\":\"alphabetic");
            JSONed.append("\",\"labelXOffset\":0");
            JSONed.append(",\"labelYOffset\":0");
            JSONed.append(",\"labelOutlineColor\":\"");
            JSONed.append(RendererUtilities.colorToHexString(outlineColor, false));
            JSONed.append("\",\"labelOutlineWidth\":");
            JSONed.append("4");
            JSONed.append(",\"rotation\":");
            JSONed.append(angle);
            JSONed.append(",\"angle\":");
            JSONed.append(angle);
            JSONed.append("},");

            JSONed.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

    private static String ShapeToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();
        String geometryType = null;
        String sda = null;
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        Color lineColor = shapeInfo.getLineColor();
        Color fillColor = shapeInfo.getFillColor();

        if (shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_FILL || fillColor != null || shapeInfo.getPatternFillImage() != null) {
            geometryType = "\"Polygon\"";
        } else //if(shapeInfo.getShapeType() == ShapeInfo.SHAPE_TYPE_POLYLINE)
        {
            geometryType = "\"MultiLineString\"";
        }

        BasicStroke stroke = null;
        stroke = shapeInfo.getStroke();
        int lineWidth = 4;

        if (stroke != null) {
            lineWidth = (int) stroke.getLineWidth();
            //lineWidth++;
            //System.out.println("lineWidth: " + String.valueOf(lineWidth));
        }

        //generate JSON properties for feature
        properties.append("\"properties\":{");
        properties.append("\"label\":\"\",");
        if (lineColor != null) {
            properties.append("\"strokeColor\":\"" + RendererUtilities.colorToHexString(lineColor, false) + "\",");
            properties.append("\"lineOpacity\":" + String.valueOf(lineColor.getAlpha() / 255f) + ",");
        }
        if (fillColor != null) {
            properties.append("\"fillColor\":\"" + RendererUtilities.colorToHexString(fillColor, false) + "\",");
            properties.append("\"fillOpacity\":" + String.valueOf(fillColor.getAlpha() / 255f) + ",");
        }
        if (shapeInfo.getPatternFillImage() != null) {
            properties.append("\"fillPattern\":\"" + bitmapToString(shapeInfo.getPatternFillImage()) + "\",");
        }
        if(stroke.getDashArray() != null)
        {
            sda = "\"strokeDasharray\":" + Arrays.toString(stroke.getDashArray()) + ",";
            properties.append(sda);
        }


        int lineCap = stroke.getEndCap();
        properties.append("\"lineCap\":" + lineCap + ",");

        String strokeWidth = String.valueOf(lineWidth);
        properties.append("\"strokeWidth\":" + strokeWidth + ",");
        properties.append("\"strokeWeight\":" + strokeWidth + "");
        properties.append("},");


        properties.append("\"style\":{");
        if (lineColor != null) {
            properties.append("\"stroke\":\"" + RendererUtilities.colorToHexString(lineColor, false) + "\",");
            properties.append("\"line-opacity\":" + String.valueOf(lineColor.getAlpha() / 255f) + ",");
        }
        if (fillColor != null) {
            properties.append("\"fill\":\"" + RendererUtilities.colorToHexString(fillColor, false) + "\",");
            properties.append("\"fill-opacity\":" + String.valueOf(fillColor.getAlpha() / 255f) + ",");
        }
        if(stroke.getDashArray() != null)
        {
            float[] da = stroke.getDashArray();
            sda = String.valueOf(da[0]);
            if(da.length > 1)
            {
                for(int i = 1; i < da.length; i++)
                {
                    sda = sda + " " + String.valueOf(da[i]);
                }
            }
            sda = "\"stroke-dasharray\":\"" + sda + "\",";
            properties.append(sda);
            sda = null;
        }

        if(lineCap == BasicStroke.CAP_SQUARE)
            properties.append("\"stroke-linecap\":\"square\",");
        else if(lineCap == BasicStroke.CAP_ROUND)
            properties.append("\"stroke-linecap\":\"round\",");
        else if(lineCap == BasicStroke.CAP_BUTT)
            properties.append("\"stroke-linecap\":\"butt\",");

        strokeWidth = String.valueOf(lineWidth);
        properties.append("\"stroke-width\":" + strokeWidth);
        properties.append("}");


        //generate JSON geometry for feature
        geometry.append("\"geometry\":{\"type\":");
        geometry.append(geometryType);
        geometry.append(",\"coordinates\":[");

        ArrayList shapesArray = shapeInfo.getPolylines();

        for (int i = 0; i < shapesArray.size(); i++) {
            ArrayList pointList = (ArrayList) shapesArray.get(i);

            normalize = normalizePoints(pointList, ipc);

            geometry.append("[");

            //System.out.println("Pixel Coords:");
            for (int j = 0; j < pointList.size(); j++) {
                Point2D coord = (Point2D) pointList.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
                }
                double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                //fix for fill crossing DTL
                if (normalize && fillColor != null) {
                    if (longitude > 0) {
                        longitude -= 360;
                    }
                }

                //diagnostic M. Deutch 10-18-11
                //set the point as geo so that the 
                //coord.setLocation(longitude, latitude);
                coord = new Point2D.Double(longitude, latitude);
                pointList.set(j, coord);
                //end section

                geometry.append("[");
                geometry.append(longitude);
                geometry.append(",");
                geometry.append(latitude);
                geometry.append("]");

                if (j < (pointList.size() - 1)) {
                    geometry.append(",");
                }
            }

            geometry.append("]");

            if (i < (shapesArray.size() - 1)) {
                geometry.append(",");
            }
        }
        geometry.append("]}");

        JSONed.append("{\"type\":\"Feature\",");
        JSONed.append(properties.toString());
        JSONed.append(",");
        JSONed.append(geometry.toString());
        JSONed.append("}");

        return JSONed.toString();
    }

    private static String ImageToGeoJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {

        StringBuilder JSONed = new StringBuilder();
        StringBuilder properties = new StringBuilder();
        StringBuilder geometry = new StringBuilder();

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        BufferedImage image = shapeInfo.getModifierImage();

        RendererSettings RS = RendererSettings.getInstance();

        if (image != null) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"image\":\"");
            JSONed.append(bitmapToString(image));
            JSONed.append("\",\"rotation\":");
            JSONed.append(angle);
            JSONed.append(",\"angle\":");
            JSONed.append(angle);
            JSONed.append("},");
            JSONed.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

    private static String bitmapToString(BufferedImage bitmap) {
        /*final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        byte[] b = byteArrayBitmapStream.toByteArray();
        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;*/

        return RendererUtilities.imgToBase64String(bitmap);
    }

    private static String GeoJSONize(ArrayList<ShapeInfo> shapes, ArrayList<ShapeInfo> modifiers, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {

        String jstr = "";
        ShapeInfo tempModifier = null;
        StringBuilder fc = new StringBuilder();//JSON feature collection

        fc.append("[");

        int len = shapes.size();
        for (int i = 0; i < len; i++) {

            String shapesToAdd = ShapeToGeoJSONString(shapes.get(i), ipc, normalize);
            if (shapesToAdd.length() > 0) {
                fc.append(shapesToAdd);
            }
            if (i < len - 1) {
                fc.append(",");
            }
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);

            String modifiersToAdd = null;
            if(modifiers.get(j).getModifierImage() != null) {
                modifiersToAdd = ImageToGeoJSONString(tempModifier, ipc, normalize);
            } else {
                modifiersToAdd = LabelToGeoJSONString(tempModifier, ipc, normalize, textColor, textBackgroundColor);
            }
            if (modifiersToAdd.length() > 0) {
                fc.append(",");
                fc.append(modifiersToAdd);
            }
        }
        fc.append("]");
        String GeoJSON = fc.toString();
        return GeoJSON;
    }

    /**
     * 
     * @param shapes
     * @param modifiers
     * @param ipc
     * @param normalize
     * @deprecated
     */
    private static void MakeWWReady(
            ArrayList<ShapeInfo> shapes,
            ArrayList<ShapeInfo> modifiers,
            IPointConversion ipc,
            boolean normalize) {
        ShapeInfo temp = null;
        int len = shapes.size();
        for (int i = 0; i < len; i++) {

            temp = ShapeToWWReady(shapes.get(i), ipc, normalize);
            shapes.set(i, temp);

        }

        int len2 = modifiers.size();
        ShapeInfo tempModifier = null;
        for (int j = 0; j < len2; j++) {

            tempModifier = modifiers.get(j);

            //Do we need this for World Wind?
            tempModifier = LabelToWWReady(tempModifier, ipc, normalize);
            modifiers.set(j, tempModifier);

        }

    }

    private static Boolean normalizePoints(ArrayList<Point2D.Double> shape, IPointConversion ipc) {
        ArrayList geoCoords = new ArrayList();
        int n = shape.size();
        //for (int j = 0; j < shape.size(); j++) 
        for (int j = 0; j < n; j++) {
            Point2D coord = shape.get(j);
            Point2D geoCoord = ipc.PixelsToGeo(coord);
            geoCoord = NormalizeCoordToGECoord(geoCoord);
            double latitude = geoCoord.getY();
            double longitude = geoCoord.getX();
            Point2D pt2d = new Point2D.Double(longitude, latitude);
            geoCoords.add(pt2d);
        }
        Boolean normalize = crossesIDL(geoCoords);
        return normalize;
    }

    private static Boolean IsOnePointSymbolCode(String symbolCode) {
        String basicCode = SymbolUtilities.getBasicSymbolID(symbolCode);
        //TODO: Revisit for basic shapes
        //some airspaces affected
        if (symbolCode.equals("CAKE-----------")) {
            return true;
        } else if (symbolCode.equals("CYLINDER-------")) {
            return true;
        } else if (symbolCode.equals("RADARC---------")) {
            return true;
        }

        return false;
    }

    private static String ShapeToKMLString(String name,
            String description,
            String symbolCode,
            ShapeInfo shapeInfo,
            IPointConversion ipc,
            boolean normalize) {

        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        Color lineColor = null;
        Color fillColor = null;
        String googleLineColor = null;
        String googleFillColor = null;

        //String lineStyleId = "lineColor";

        BasicStroke stroke = null;
        int lineWidth = 4;

        symbolCode = JavaRendererUtilities.normalizeSymbolCode(symbolCode);

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        kml.append("<Placemark>");//("<Placemark id=\"" + id + "_mg" + "\">");
        kml.append("<description>" + cdataStart + "<b>" + name + "</b><br/>" + "\n" + description + cdataEnd + "</description>");
        //kml.append("<Style id=\"" + lineStyleId + "\">");
        kml.append("<Style>");

        lineColor = shapeInfo.getLineColor();
        if (lineColor != null) {
            googleLineColor = Integer.toHexString(shapeInfo.getLineColor().getRGB());

            stroke = shapeInfo.getStroke();

            if (stroke != null) {
                lineWidth = (int) stroke.getLineWidth();
            }

            googleLineColor = JavaRendererUtilities.ARGBtoABGR(googleLineColor);

            kml.append("<LineStyle>");
            kml.append("<color>" + googleLineColor + "</color>");
            kml.append("<colorMode>normal</colorMode>");
            kml.append("<width>" + String.valueOf(lineWidth) + "</width>");
            kml.append("</LineStyle>");
        }

        fillColor = shapeInfo.getFillColor();
        BufferedImage fillPattern = shapeInfo.getPatternFillImage();
        if (fillColor != null || fillPattern != null) {
            kml.append("<PolyStyle>");

            if (fillColor != null) {
                googleFillColor = Integer.toHexString(shapeInfo.getFillColor().getRGB());
                googleFillColor = JavaRendererUtilities.ARGBtoABGR(googleFillColor);
                kml.append("<color>" + googleFillColor + "</color>");
                kml.append("<colorMode>normal</colorMode>");
            }
            if (fillPattern != null){
                kml.append("<shader>" + bitmapToString(fillPattern) + "</shader>");
            }

            kml.append("<fill>1</fill>");
            if (lineColor != null) {
                kml.append("<outline>1</outline>");
            } else {
                kml.append("<outline>0</outline>");
            }
            kml.append("</PolyStyle>");
        }

        kml.append("</Style>");

        ArrayList shapesArray = shapeInfo.getPolylines();
        int len = shapesArray.size();
        kml.append("<MultiGeometry>");

        for (int i = 0; i < len; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);
            normalize = normalizePoints(shape, ipc);
            if (lineColor != null && fillColor == null) {
                kml.append("<LineString>");
                kml.append("<tessellate>1</tessellate>");
                kml.append("<altitudeMode>clampToGround</altitudeMode>");
                kml.append("<coordinates>");
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    if (normalize) {
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
                    }

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    if(j<shape.size()-1)
                        kml.append(" ");
                }

                kml.append("</coordinates>");
                kml.append("</LineString>");
            }

            if (fillColor != null) {

                if (i == 0) {
                    kml.append("<Polygon>");
                }
                //kml.append("<outerBoundaryIs>");
                if (i == 1 && len > 1) {
                    kml.append("<innerBoundaryIs>");
                } else {
                    kml.append("<outerBoundaryIs>");
                }
                kml.append("<LinearRing>");
                kml.append("<altitudeMode>clampToGround</altitudeMode>");
                kml.append("<tessellate>1</tessellate>");
                kml.append("<coordinates>");

                //this section is a workaround for a google earth bug. Issue 417 was closed
                //for linestrings but they did not fix the smae issue for fills. If Google fixes the issue
                //for fills then this section will need to be commented or it will induce an error.
                double lastLongitude = Double.MIN_VALUE;
                if (normalize == false && IsOnePointSymbolCode(symbolCode)) {
                    int n = shape.size();
                    //for (int j = 0; j < shape.size(); j++) 
                    for (int j = 0; j < n; j++) {
                        Point2D coord = (Point2D) shape.get(j);
                        Point2D geoCoord = ipc.PixelsToGeo(coord);
                        double longitude = geoCoord.getX();
                        if (lastLongitude != Double.MIN_VALUE) {
                            if (Math.abs(longitude - lastLongitude) > 180d) {
                                normalize = true;
                                break;
                            }
                        }
                        lastLongitude = longitude;
                    }
                }
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;

                    //fix for fill crossing DTL
                    if (normalize) {
                        if (longitude > 0) {
                            longitude -= 360;
                        }
                    }

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    if(j<shape.size()-1)
                        kml.append(" ");
                }

                kml.append("</coordinates>");
                kml.append("</LinearRing>");
                if (i == 1 && len > 1) {
                    kml.append("</innerBoundaryIs>");
                } else {
                    kml.append("</outerBoundaryIs>");
                }
                if (i == len - 1) {
                    kml.append("</Polygon>");
                }
            }
        }

        kml.append("</MultiGeometry>");
        kml.append("</Placemark>");

        return kml.toString();
    }

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param normalize
     * @return
     * @deprecated
     */
    private static ShapeInfo ShapeToWWReady(
            ShapeInfo shapeInfo,
            IPointConversion ipc,
            boolean normalize) {

        ArrayList shapesArray = shapeInfo.getPolylines();
        int len = shapesArray.size();

        for (int i = 0; i < len; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);

            if (shapeInfo.getLineColor() != null) {
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    //M. Deutch 9-26-11
                    if (normalize) {
                        geoCoord = NormalizeCoordToGECoord(geoCoord);
                    }

                    shape.set(j, geoCoord);

                }

            }

            if (shapeInfo.getFillColor() != null) {
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++) 
                for (int j = 0; j < n; j++) {
                    Point2D coord = (Point2D) shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    //M. Deutch 9-26-11
                    //commenting these two lines seems to help with fill not go around the pole
                    //if(normalize)
                    //geoCoord=NormalizeCoordToGECoord(geoCoord);

                    shape.set(j, geoCoord);
                }
            }
        }

        return shapeInfo;
    }

    private static ShapeInfo LabelToWWReady(ShapeInfo shapeInfo,
            IPointConversion ipc,
            boolean normalize) {

        try {
            Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
            Point2D geoCoord = ipc.PixelsToGeo(coord);
            //M. Deutch 9-26-11
            if (normalize) {
                geoCoord = NormalizeCoordToGECoord(geoCoord);
            }
            double latitude = geoCoord.getY();
            double longitude = geoCoord.getX();
            long angle = Math.round(shapeInfo.getModifierAngle());

            String text = shapeInfo.getModifierString();

            if (text != null && text.equals("") == false) {
                shapeInfo.setModifierPosition(geoCoord);
            } else {
                return null;
            }
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }

        return shapeInfo;
    }

    /**
     * Google earth centers text on point rather than drawing from that point.
     * So we need to adjust the point to where the center of the text would be.
     *
     * @param modifier
     */
    private static void AdjustModifierPointToCenter(ShapeInfo modifier) {
        AffineTransform at = null;
        try {
            Rectangle2D bounds2 = modifier.getTextLayout().getBounds();
            Rectangle2D bounds = new Rectangle2D.Double(bounds2.getX(), bounds2.getY(), bounds2.getWidth(), bounds2.getHeight());
        } catch (Exception exc) {
            System.err.println(exc.getMessage());
            exc.printStackTrace();
        }
    }

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param geMap
     * @param normalize
     * @return
     * @deprecated
     */
    private static String ShapeToJSONString(ShapeInfo shapeInfo, IPointConversion ipc, Boolean geMap, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        String fillColor = null;
        String lineColor = null;

        if (shapeInfo.getLineColor() != null) {
            lineColor = Integer.toHexString(shapeInfo.getLineColor().getRGB());
            if (geMap) {
                lineColor = JavaRendererUtilities.ARGBtoABGR(lineColor);
            }

        }
        if (shapeInfo.getFillColor() != null) {
            fillColor = Integer.toHexString(shapeInfo.getFillColor().getRGB());
            if (geMap) {
                fillColor = JavaRendererUtilities.ARGBtoABGR(fillColor);
            }
        }

        BasicStroke stroke = null;
        stroke = shapeInfo.getStroke();
        int lineWidth = 4;

        if (stroke != null) {
            lineWidth = (int) stroke.getLineWidth();
        }

        ArrayList shapesArray = shapeInfo.getPolylines();
        int n = shapesArray.size();
        //for (int i = 0; i < shapesArray.size(); i++) 
        for (int i = 0; i < n; i++) {
            ArrayList shape = (ArrayList) shapesArray.get(i);

            if (fillColor != null) {
                JSONed.append("{\"polygon\":[");
            } else {
                JSONed.append("{\"line\":[");
            }

            int t = shape.size();
            //for (int j = 0; j < shape.size(); j++) 
            for (int j = 0; j < t; j++) {
                Point2D coord = (Point2D) shape.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = NormalizeCoordToGECoord(geoCoord);
                }
                double latitude = geoCoord.getY();
                double longitude = geoCoord.getX();

                //diagnostic M. Deutch 10-18-11
                //set the point as geo so that the 
                coord = new Point2D.Double(longitude, latitude);
                shape.set(j, coord);

                JSONed.append("[");
                JSONed.append(longitude);
                JSONed.append(",");
                JSONed.append(latitude);
                JSONed.append("]");

                if (j < (shape.size() - 1)) {
                    JSONed.append(",");
                }
            }

            JSONed.append("]");
            if (lineColor != null) {
                JSONed.append(",\"lineColor\":\"");
                JSONed.append(lineColor);

                JSONed.append("\"");
            }
            if (fillColor != null) {
                JSONed.append(",\"fillColor\":\"");
                JSONed.append(fillColor);
                JSONed.append("\"");
            }

            JSONed.append(",\"lineWidth\":\"");
            JSONed.append(String.valueOf(lineWidth));
            JSONed.append("\"");

            JSONed.append("}");

            if (i < (shapesArray.size() - 1)) {
                JSONed.append(",");
            }
        }

        return JSONed.toString();
    }

    private static String LabelToKMLString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize, Color textColor) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-26-11
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        long angle = Math.round(shapeInfo.getModifierAngle());

        String text = shapeInfo.getModifierString();

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        String color = Integer.toHexString(textColor.getRGB());
        color = JavaRendererUtilities.ARGBtoABGR(color);
        float kmlScale = RendererSettings.getInstance().getKMLLabelScale();

        if (kmlScale > 0 && text != null && text.equals("") == false) {
            kml.append("<Placemark>");//("<Placemark id=\"" + id + "_lp" + i + "\">");
            kml.append("<name>" + cdataStart + text + cdataEnd + "</name>");
            kml.append("<Style>");
            kml.append("<IconStyle>");
            kml.append("<scale>.7</scale>");
            kml.append("<heading>" + angle + "</heading>");
            kml.append("<Icon>");
            kml.append("<href></href>");
            kml.append("</Icon>");
            kml.append("</IconStyle>");
            kml.append("<LabelStyle>");
            kml.append("<color>" + color + "</color>");
            kml.append("<scale>" + String.valueOf(kmlScale) +"</scale>");
            kml.append("</LabelStyle>");
            kml.append("</Style>");
            kml.append("<Point>");
            kml.append("<extrude>1</extrude>");
            kml.append("<altitudeMode>relativeToGround</altitudeMode>");
            kml.append("<coordinates>");
            kml.append(longitude);
            kml.append(",");
            kml.append(latitude);
            kml.append("</coordinates>");
            kml.append("</Point>");
            kml.append("</Placemark>");
        } else {
            return "";
        }

        return kml.toString();
    }

    /**
     * 
     * @param shapeInfo
     * @param ipc
     * @param normalize
     * @return
     * @deprecated
     */
    private static String LabelToJSONString(ShapeInfo shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();
        /*
         NOTE: Google Earth / KML colors are backwards.
         They are ordered Alpha,Blue,Green,Red, not Red,Green,Blue,Aplha like the rest of the world
         * */
        JSONed.append("{\"label\":");

        Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        if (normalize) {
            geoCoord = NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = geoCoord.getY();
        double longitude = geoCoord.getX();
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        shapeInfo.setGlyphPosition(coord);

        String text = shapeInfo.getModifierString();

        if (text != null && text.equals("") == false) {
            JSONed.append("[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append("]");

            JSONed.append(",\"text\":\"");
            JSONed.append(text);
            JSONed.append("\"");

            JSONed.append(",\"angle\":\"");
            JSONed.append(angle);
            JSONed.append("\"}");
        } else {
            return "";
        }

        return JSONed.toString();
    }

    public static String canRenderMultiPoint(String symbolID, Map<String,String> modifiers, int numPoints) {
        try {
            String basicID = SymbolUtilities.getBasicSymbolID(symbolID);
            MSInfo info = MSLookup.getInstance().getMSLInfo(symbolID);

            if (info == null) {
                if (SymbolID.getVersion(symbolID) == SymbolID.Version_2525E) {
                    return "Basic ID: " + basicID + " not recognized in version E (13)";
                } else {
                    return "Basic ID: " + basicID + " not recognized in version D (11)";
                }
            }

            int drawRule = info.getDrawRule();

            if (drawRule == DrawRules.DONOTDRAW) {
                return "Basic ID: " + basicID + " has no draw rule";
            } else if (!SymbolUtilities.isMultiPoint(symbolID)) {
                return "Basic ID: " + basicID + " is not a multipoint symbol";
            } else if (numPoints < info.getMinPointCount()) {
                return "Basic ID: " + basicID + " requires a minimum of " + String.valueOf(info.getMinPointCount()) + " points. " + String.valueOf(numPoints) + " are present.";
            }

            //now check for required modifiers
            ArrayList<Double> AM = new ArrayList();
            ArrayList<Double> AN = new ArrayList();
            if (modifiers.containsKey(Modifiers.AM_DISTANCE)) {
                String[] amArray = modifiers.get(Modifiers.AM_DISTANCE).split(",");
                for (String str : amArray) {
                    if (!str.equals("")) {
                        AM.add(Double.parseDouble(str));
                    }
                }
            }
            if (modifiers.containsKey(Modifiers.AN_AZIMUTH)) {
                String[] anArray = modifiers.get(Modifiers.AN_AZIMUTH).split(",");
                for (String str : anArray) {
                    if (!str.equals("")) {
                        AN.add(Double.parseDouble(str));
                    }
                }
            }

            return hasRequiredModifiers(symbolID, drawRule, AM, AN);
        } catch (Exception exc) {
            ErrorLogger.LogException("MultiPointHandler", "canRenderMultiPoint", exc);
            return "false: " + exc.getMessage();
        }
    }

    static private String hasRequiredModifiers(String symbolID, int drawRule, ArrayList<Double> AM, ArrayList<Double> AN) {

        String message = symbolID;
        try {
            if (drawRule > 700) {
                if (drawRule == DrawRules.CIRCULAR1)
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += " requires a modifiers object that has 1 distance/AM value.";
                        return message;
                    }
                } else if (drawRule == DrawRules.RECTANGULAR2)
                {
                    if (AM != null && AM.size() >= 2
                            && AN != null && AN.size() >= 1) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 2 distance/AM values and 1 azimuth/AN value.");
                        return message;
                    }
                } else if (drawRule == DrawRules.ARC1)
                {
                    if (AM != null && AM.size() >= 1
                            && AN != null && AN.size() >= 2) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 2 distance/AM values and 2 azimuth/AN values per sector.  The first sector can have just one AM value although it is recommended to always use 2 values for each sector.");
                        return message;
                    }
                } else if (drawRule == DrawRules.CIRCULAR2)
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has at least 1 distance/AM value");
                        return message;
                    }
                } else if (drawRule == DrawRules.RECTANGULAR1)
                {
                    if (AM != null && AM.size() > 0) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 1 distance/AM value.");
                        return message;
                    }
                } else if (drawRule == DrawRules.ELLIPSE1)
                {
                    if (AM != null && AM.size() >= 2
                            && AN != null && AN.size() >= 1) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 2 distance/AM values and 1 azimuth/AN value.");
                        return message;
                    }
                }
                else if (drawRule == DrawRules.RECTANGULAR3)
                {
                    if (AM != null && AM.size() >= 1) {
                        return "true";
                    } else {
                        message += (" requires a modifiers object that has 1 distance/AM value.");
                        return message;
                    }
                } else {
                    //should never get here
                    return "true";
                }
            } else if (drawRule == DrawRules.POINT17) {
                if (AM != null && AM.size() >= 2
                        && AN != null && AN.size() >= 1) {
                    return "true";
                } else {
                    message += (" requires a modifiers object that has 2 distance/AM values and 1 azimuth/AN value.");
                    return message;
                }
            } else if (drawRule == DrawRules.POINT18) {
                if (AM != null && AM.size() >= 2
                        && AN != null && AN.size() >= 2) {
                    return "true";
                } else {
                    message += (" requires a modifiers object that has 2 distance/AM values and 2 azimuth/AN values.");
                    return message;
                }
            } else if (drawRule == DrawRules.CORRIDOR1) {
                if (AM != null && AM.size() > 0) {
                    return "true";
                } else {
                    message += (" requires a modifiers object that has 1 distance/AM value.");
                    return message;
                }
            } else {
                //no required parameters
                return "true";
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("MultiPointHandler", "hasRequiredModifiers", exc);
            return "true";
        }
    }
}
