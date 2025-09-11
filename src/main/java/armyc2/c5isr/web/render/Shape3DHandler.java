package armyc2.c5isr.web.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;

import armyc2.c5isr.JavaLineArray.POINT2;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.RenderMultipoints.clsRenderer;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.utilities.Basic3DShapes;
import armyc2.c5isr.web.render.utilities.JavaRendererUtilities;
import armyc2.c5isr.web.render.utilities.Point3D;
import armyc2.c5isr.web.render.utilities.ShapeInfo3D;

public class Shape3DHandler {
    public static String RenderMilStd3dSymbol(String id,
                                              String name,
                                              String description,
                                              String symbolCode,
                                              String controlPoints,
                                              String altitudeMode,
                                              Double scale,
                                              String bbox,
                                              Map<String, String> symbolModifiers,
                                              Map<String, String> symbolAttributes,
                                              int format) {
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
        ArrayList<ShapeInfo3D> shapes = new ArrayList<ShapeInfo3D>();
        ArrayList<ShapeInfo3D> modifiers = new ArrayList<ShapeInfo3D>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;
        //diagnostic create geoCoords here
        Point2D coordsUL = null;

        // 3D default colors
        if (symbolAttributes.get(MilStdAttributes.LineColor) == null) {
            Color defaultColor = SymbolUtilities.getLineColorOfAffiliation(symbolCode);
            if (defaultColor == null) {
                defaultColor = Color.BLACK;
            }
            symbolAttributes.put(MilStdAttributes.LineColor, RendererUtilities.colorToHexString(defaultColor, true));
        }
        if (symbolAttributes.get(MilStdAttributes.FillColor) == null) {
            Color defaultColor = SymbolUtilities.getFillColorOfAffiliation(symbolCode);
            if (defaultColor == null) {
                defaultColor = Color.white;
            }
            defaultColor = new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), 170);
            symbolAttributes.put(MilStdAttributes.FillColor, RendererUtilities.colorToHexString(defaultColor, true));
        }

        if (altitudeMode == null || altitudeMode.equals("clampToGround"))
            altitudeMode = "absolute";

        if (!JavaRendererUtilities.is3dSymbol(symbolCode)) {
            String basicID = SymbolUtilities.getBasicSymbolID(symbolCode);
            final String errorMsg = "Basic ID: " + basicID + " is not a 3D Symbol";
            String ErrorOutput = "";
            ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the 3D MilStdSymbol " + symbolCode + " - ID: " + id + " - ");
            ErrorOutput += errorMsg; //reason for error
            ErrorOutput += ("\"}");
            ErrorLogger.LogMessage("Shape3DHandler", "RenderMilStd3dSymbol", errorMsg, Level.FINE);
            return ErrorOutput;
        }

        String symbolIsValid = MultiPointHandler.canRenderMultiPoint(symbolCode, symbolModifiers, len);
        if (!symbolIsValid.equals("true")) {
            String ErrorOutput = "";
            ErrorOutput += ("{\"type\":\"error\",\"error\":\"There was an error creating the 3D MilStdSymbol " + symbolCode + " - ID: " + id + " - ");
            ErrorOutput += symbolIsValid; //reason for error
            ErrorOutput += ("\"}");
            ErrorLogger.LogMessage("Shape3DHandler", "RenderMilStd3dSymbol", symbolIsValid, Level.WARNING);
            return ErrorOutput;
        }

        if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() != DrawRules.AREA10) // AREA10 can support infinite points
            len = Math.min(len, MSLookup.getInstance().getMSLInfo(symbolCode).getMaxPointCount());
        for (int i = 0; i < len; i++) {
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
                ptGeoUL = MultiPointHandler.getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                String bbox2 = MultiPointHandler.getBboxFromCoords(bboxCoords);
                scale = MultiPointHandler.getReasonableScale(bbox2, scale);
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
                scale = MultiPointHandler.getReasonableScale(bbox, scale);
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
            Point2D ptCoordsUL = MultiPointHandler.getGeoUL(geoCoords);
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
        if (MultiPointHandler.ShouldClipSymbol(symbolCode) == false)
            if (MultiPointHandler.crossesIDL(geoCoords) == false) {
                rect = null;
                bboxCoords = null;
            }

        tgl.set_SymbolId(symbolCode);// "GFGPSLA---****X" AMBUSH symbol code
        tgl.set_Pixels(null);

        try {

            //String fillColor = null;
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG) {
                // Use dash array and hatch pattern fill for SVG output
                symbolAttributes.put(MilStdAttributes.UseDashArray, "true");
                symbolAttributes.put(MilStdAttributes.UsePatternFill, "true");
            }

            if (symbolModifiers != null || symbolAttributes != null) {
                MultiPointHandler.populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            if (bboxCoords == null) {
                Rectangle clipBounds = MultiPointHandler.getOverscanClipBounds(rect, ipc);
                clsRenderer.renderWithPolylines(mSymbol, ipc, clipBounds);
            } else {
                clsRenderer.renderWithPolylines(mSymbol, ipc, bboxCoords);
            }

            // Convert 2D shape to 3D
            if (MSLookup.getInstance().getMSLInfo(symbolCode).getDrawRule() == DrawRules.CORRIDOR1) {
                // Remove circles from air corridor for 3d
                // Set line color for other shapes
                for (int i = 0; i < mSymbol.getSymbolShapes().size() - 1; i++) {
                    mSymbol.getSymbolShapes().get(i).setLineColor(mSymbol.getSymbolShapes().get(mSymbol.getSymbolShapes().size() - 1).getLineColor());
                }
                mSymbol.getSymbolShapes().remove(mSymbol.getSymbolShapes().size() - 1);
                Collections.reverse(mSymbol.getSymbolShapes());
            }
            // Confirm there are at least two altitudes per shape
            ArrayList<Double> altitudes = mSymbol.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
            if (altitudes.size() == 1) {
                altitudes.add(0, 0.0);
            }
            final Double lastAlt = altitudes.get(altitudes.size() - 1);
            final Double nextToLastAlt = altitudes.get(altitudes.size() - 2);
            while (altitudes.size() < mSymbol.getSymbolShapes().size() * 2) {
                altitudes.add(nextToLastAlt);
                altitudes.add(lastAlt);
            }
            for (int shapeIndex = 0; shapeIndex < mSymbol.getSymbolShapes().size(); shapeIndex++) {
                final Double minAlt = altitudes.get(shapeIndex * 2);
                final Double maxAlt = altitudes.get((shapeIndex * 2) + 1);
                final ShapeInfo oldShape = mSymbol.getSymbolShapes().get(shapeIndex);

                ShapeInfo3D bottomShape = new ShapeInfo3D();
                bottomShape.setShapeType(oldShape.getShapeType());
                bottomShape.setStroke(oldShape.getStroke());
                bottomShape.setLineColor(oldShape.getLineColor());
                bottomShape.setFillColor(oldShape.getFillColor());
                bottomShape.setPatternFillImage(oldShape.getPatternFillImage());
                bottomShape.setPolylines3D(new ArrayList<>());
                ShapeInfo3D topShape = new ShapeInfo3D();
                topShape.setShapeType(oldShape.getShapeType());
                topShape.setStroke(oldShape.getStroke());
                topShape.setLineColor(oldShape.getLineColor());
                topShape.setFillColor(oldShape.getFillColor());
                topShape.setPatternFillImage(oldShape.getPatternFillImage());
                topShape.setPolylines3D(new ArrayList<>());

                for (int polyLineIndex = 0; polyLineIndex < oldShape.getPolylines().size(); polyLineIndex++) {
                    final ArrayList<Point2D> polyline = oldShape.getPolylines().get(polyLineIndex);
                    bottomShape.getPolylines3D().add(new ArrayList<Point3D>());
                    topShape.getPolylines3D().add(new ArrayList<Point3D>());
                    for (int ptIndex = 0; ptIndex < polyline.size(); ptIndex++) {
                        final Point2D pt = polyline.get(ptIndex);
                        final Point2D pt2 = polyline.get((ptIndex + 1) % polyline.size());
                        bottomShape.getPolylines3D().get(polyLineIndex).add(new Point3D(pt, minAlt));
                        topShape.getPolylines3D().get(polyLineIndex).add(new Point3D(pt, maxAlt));

                        ShapeInfo3D sideShape = new ShapeInfo3D();
                        sideShape.setShapeType(oldShape.getShapeType());
                        sideShape.setStroke(oldShape.getStroke());
                        sideShape.setLineColor(oldShape.getLineColor());
                        sideShape.setFillColor(oldShape.getFillColor());
                        sideShape.setPatternFillImage(oldShape.getPatternFillImage());
                        sideShape.setPolylines3D(new ArrayList<ArrayList<Point3D>>());
                        sideShape.getPolylines3D().add(new ArrayList<>());
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, minAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt2, minAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt2, maxAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, maxAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, minAlt));
                        shapes.add(sideShape);
                    }
                }
                shapes.add(bottomShape);
                shapes.add(topShape);
            }

            if (!mSymbol.getSymbolShapes().isEmpty() && !mSymbol.getModifierShapes().isEmpty()) {
                final double modifierAlt = Collections.max(altitudes.subList(0, mSymbol.getSymbolShapes().size() * 2));
                for (ShapeInfo oldShape : mSymbol.getModifierShapes()) {
                    ShapeInfo3D modShape = new ShapeInfo3D();
                    modShape.setModifierString(oldShape.getModifierString());
                    modShape.setModifierPosition(new Point3D(oldShape.getModifierPosition(), modifierAlt));
                    modShape.setModifierAngle(oldShape.getModifierAngle());
                    modShape.setTextJustify(oldShape.getTextJustify());
                    modShape.setModifierImage(oldShape.getModifierImage());
                    modifiers.add(modShape);
                }
            }

            if (format == WebRenderer.OUTPUT_FORMAT_KML) {
                Color textColor = mSymbol.getTextColor();
                if (textColor == null) textColor = mSymbol.getLineColor();

                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor, altitudeMode, mSymbol.get_WasClipped());
                jsonOutput.append(jsonContent);
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOJSON) {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);

                //moving meta data properties to the last feature with no coords as feature collection doesn't allow properties
                jsonOutput.replace(jsonOutput.toString().length() - 1, jsonOutput.toString().length(), "");
                if (jsonContent.length() > 2) jsonOutput.append(",");
                jsonOutput.append("{\"type\": \"Feature\",\"geometry\": { \"type\": \"Polygon\",\"coordinates\": [ ]}");

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
                jsonOutput.append("\"}}]}");
            }
        } catch (Exception exc) {
            String st = JavaRendererUtilities.getStackTrace(exc);
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the 3D MilStdSymbol ").append(symbolCode).append(": ").append("- ");
            jsonOutput.append(exc.getMessage()).append(" - ");
            jsonOutput.append(st);
            jsonOutput.append("\"}");

            ErrorLogger.LogException("Shape3DHandler", "RenderMilStd3dSymbol", exc);
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

        ErrorLogger.LogMessage("Shape3DHandler", "RenderMilStd3dSymbol()", "exit RenderMilStd3dSymbol", Level.FINER);
        return jsonOutput.toString();
    }

    public static String RenderBasic3DShape(String id,
                                            String name,
                                            String description,
                                            int basicShapeType,
                                            String controlPoints,
                                            String altitudeMode,
                                            Double scale,
                                            String bbox,
                                            Map<String, String> symbolModifiers,
                                            Map<String, String> symbolAttributes,
                                            int format) {
        boolean normalize = true;
        //Double controlLat = 0.0;
        //Double controlLong = 0.0;
        //Double metPerPix = GeoPixelConversion.metersPerPixel(scale);
        //String bbox2=getBoundingRectangle(controlPoints,bbox);
        StringBuilder jsonOutput = new StringBuilder();
        String jsonContent = "";

        Rectangle rect = null;
        String[] coordinates = controlPoints.split(" ");
        ArrayList<ShapeInfo3D> shapes = new ArrayList<ShapeInfo3D>();
        ArrayList<ShapeInfo3D> modifiers = new ArrayList<ShapeInfo3D>();
        //ArrayList<Point2D> pixels = new ArrayList<Point2D>();
        ArrayList<Point2D> geoCoords = new ArrayList<Point2D>();
        int len = coordinates.length;
        //diagnostic create geoCoords here
        Point2D coordsUL = null;
        final String symbolCode = "";

        // 3D default colors
        if (symbolAttributes.get(MilStdAttributes.LineColor) == null) {
            symbolAttributes.put(MilStdAttributes.LineColor, RendererUtilities.colorToHexString(Color.BLACK, true));
        }
        if (symbolAttributes.get(MilStdAttributes.FillColor) == null) {
            Color defaultColor = new Color(255, 255, 255, 170);
            symbolAttributes.put(MilStdAttributes.FillColor, RendererUtilities.colorToHexString(defaultColor, true));
        }

        if (altitudeMode == null || altitudeMode.equals("clampToGround"))
            altitudeMode = "absolute";

        for (int i = 0; i < len; i++) {
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
                ptGeoUL = MultiPointHandler.getGeoUL(bboxCoords);
                left = ptGeoUL.getX();
                top = ptGeoUL.getY();
                String bbox2 = MultiPointHandler.getBboxFromCoords(bboxCoords);
                scale = MultiPointHandler.getReasonableScale(bbox2, scale);
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
                scale = MultiPointHandler.getReasonableScale(bbox, scale);
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

                width = (int) Math.abs(rightX - leftX);
                height = (int) Math.abs(bottomY - topY);

                rect = new Rectangle(leftX, topY, width, height);
            }
        } else {
            rect = null;
        }

        if (ipc == null) {
            Point2D ptCoordsUL = MultiPointHandler.getGeoUL(geoCoords);
            ipc = new PointConverter(ptCoordsUL.getX(), ptCoordsUL.getY(), scale);
        }

        ArrayList<Point2D> geoCoords2 = new ArrayList<Point2D>();
        geoCoords2.add(new Point2D.Double(left, top));
        geoCoords2.add(new Point2D.Double(right, bottom));

//        if (normalize) {
//            NormalizeGECoordsToGEExtents(0, 360, geoCoords2);
//        }

        try {

            //String fillColor = null;
            MilStdSymbol mSymbol = new MilStdSymbol(symbolCode, null, geoCoords, null);

            if (format == WebRenderer.OUTPUT_FORMAT_GEOSVG) {
                // Use dash array and hatch pattern fill for SVG output
                symbolAttributes.put(MilStdAttributes.UseDashArray, "true");
                symbolAttributes.put(MilStdAttributes.UsePatternFill, "true");
            }

            if (symbolModifiers != null || symbolAttributes != null) {
                MultiPointHandler.populateModifiers(symbolModifiers, symbolAttributes, mSymbol);
            } else {
                mSymbol.setFillColor(null);
            }

            TGLight tg = clsRenderer.createTGLightFromMilStdSymbolBasicShape(mSymbol, ipc, basicShapeType);
            ArrayList<ShapeInfo> shapeInfos = new ArrayList();
            ArrayList<ShapeInfo> modifierShapeInfos = new ArrayList();
            Object clipArea;
            if (bboxCoords == null) {
                clipArea = rect;
            } else {
                clipArea = bboxCoords;
            }
            if (clsRenderer.intersectsClipArea(tg, ipc, clipArea)) {
                clsRenderer.render_GE(tg, shapeInfos, modifierShapeInfos, ipc, clipArea);
            }
            mSymbol.setSymbolShapes(shapeInfos);
            mSymbol.setModifierShapes(modifierShapeInfos);
            mSymbol.set_WasClipped(tg.get_WasClipped());

            // Convert 2D shape to 3D
            // Confirm there are at least two altitudes per shape
            ArrayList<Double> altitudes = mSymbol.getModifiers_AM_AN_X(Modifiers.X_ALTITUDE_DEPTH);
            if (altitudes.size() == 1) {
                altitudes.add(0, 0.0);
            }
            if (basicShapeType == Basic3DShapes.ROUTE){
                altitudes = new ArrayList<>(altitudes.subList(0, 2));
            }
            final Double lastAlt = altitudes.get(altitudes.size() - 1);
            final Double nextToLastAlt = altitudes.get(altitudes.size() - 2);
            while (altitudes.size() < mSymbol.getSymbolShapes().size() * 2) {
                altitudes.add(nextToLastAlt);
                altitudes.add(lastAlt);
            }
            for (int shapeIndex = 0; shapeIndex < mSymbol.getSymbolShapes().size(); shapeIndex++) {
                final Double minAlt = altitudes.get(shapeIndex * 2);
                final Double maxAlt = altitudes.get((shapeIndex * 2) + 1);
                final ShapeInfo oldShape = mSymbol.getSymbolShapes().get(shapeIndex);

                ShapeInfo3D bottomShape = new ShapeInfo3D();
                bottomShape.setShapeType(oldShape.getShapeType());
                bottomShape.setStroke(oldShape.getStroke());
                bottomShape.setLineColor(oldShape.getLineColor());
                bottomShape.setFillColor(oldShape.getFillColor());
                bottomShape.setPatternFillImage(oldShape.getPatternFillImage());
                bottomShape.setPolylines3D(new ArrayList<>());
                ShapeInfo3D topShape = new ShapeInfo3D();
                topShape.setShapeType(oldShape.getShapeType());
                topShape.setStroke(oldShape.getStroke());
                topShape.setLineColor(oldShape.getLineColor());
                topShape.setFillColor(oldShape.getFillColor());
                topShape.setPatternFillImage(oldShape.getPatternFillImage());
                topShape.setPolylines3D(new ArrayList<>());

                for (int polyLineIndex = 0; polyLineIndex < oldShape.getPolylines().size(); polyLineIndex++) {
                    final ArrayList<Point2D> polyline = oldShape.getPolylines().get(polyLineIndex);
                    bottomShape.getPolylines3D().add(new ArrayList<Point3D>());
                    topShape.getPolylines3D().add(new ArrayList<Point3D>());
                    for (int ptIndex = 0; ptIndex < polyline.size(); ptIndex++) {
                        final Point2D pt = polyline.get(ptIndex);
                        final Point2D pt2 = polyline.get((ptIndex + 1) % polyline.size());
                        bottomShape.getPolylines3D().get(polyLineIndex).add(new Point3D(pt, minAlt));
                        topShape.getPolylines3D().get(polyLineIndex).add(new Point3D(pt, maxAlt));

                        ShapeInfo3D sideShape = new ShapeInfo3D();
                        sideShape.setShapeType(oldShape.getShapeType());
                        sideShape.setStroke(oldShape.getStroke());
                        sideShape.setLineColor(oldShape.getLineColor());
                        sideShape.setFillColor(oldShape.getFillColor());
                        sideShape.setPatternFillImage(oldShape.getPatternFillImage());
                        sideShape.setPolylines3D(new ArrayList<ArrayList<Point3D>>());
                        sideShape.getPolylines3D().add(new ArrayList<>());
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, minAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt2, minAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt2, maxAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, maxAlt));
                        sideShape.getPolylines3D().get(0).add(new Point3D(pt, minAlt));
                        shapes.add(sideShape);
                    }
                }
                shapes.add(bottomShape);
                shapes.add(topShape);
            }

            if (!mSymbol.getSymbolShapes().isEmpty() && !mSymbol.getModifierShapes().isEmpty()) {
                final double modifierAlt = Collections.max(altitudes.subList(0, mSymbol.getSymbolShapes().size() * 2));
                for (ShapeInfo oldShape : mSymbol.getModifierShapes()) {
                    ShapeInfo3D modShape = new ShapeInfo3D();
                    modShape.setModifierString(oldShape.getModifierString());
                    modShape.setModifierPosition(new Point3D(oldShape.getModifierPosition(), modifierAlt));
                    modShape.setModifierAngle(oldShape.getModifierAngle());
                    modShape.setTextJustify(oldShape.getTextJustify());
                    modShape.setModifierImage(oldShape.getModifierImage());
                    modifiers.add(modShape);
                }
            }

            if (format == WebRenderer.OUTPUT_FORMAT_KML) {
                Color textColor = mSymbol.getTextColor();
                if (textColor == null)
                    textColor = mSymbol.getLineColor();

                jsonContent = KMLize(id, name, description, symbolCode, shapes, modifiers, ipc, normalize, textColor, altitudeMode, mSymbol.get_WasClipped());
                jsonOutput.append(jsonContent);
            } else if (format == WebRenderer.OUTPUT_FORMAT_GEOJSON) {
                jsonOutput.append("{\"type\":\"FeatureCollection\",\"features\":");
                jsonContent = GeoJSONize(shapes, modifiers, ipc, normalize, mSymbol.getTextColor(), mSymbol.getTextBackgroundColor());
                jsonOutput.append(jsonContent);

                //moving meta data properties to the last feature with no coords as feature collection doesn't allow properties
                jsonOutput.replace(jsonOutput.toString().length() - 1, jsonOutput.toString().length(), "");
                if (jsonContent.length() > 2)
                    jsonOutput.append(",");
                jsonOutput.append("{\"type\": \"Feature\",\"geometry\": { \"type\": \"Polygon\",\"coordinates\": [ ]}");

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
            }
        } catch (Exception exc) {
            String st = JavaRendererUtilities.getStackTrace(exc);
            jsonOutput = new StringBuilder();
            jsonOutput.append("{\"type\":\"error\",\"error\":\"There was an error creating the 3D MilStdSymbol " + symbolCode + ": " + "- ");
            jsonOutput.append(exc.getMessage()).append(" - ");
            jsonOutput.append(st);
            jsonOutput.append("\"}");

            ErrorLogger.LogException("Shape3DHandler", "RenderBasic3DShape", exc);
        }

        boolean debug = false;
        if (debug == true) {
            System.out.println("Symbol Code: " + symbolCode);
            System.out.println("Scale: " + scale);
            System.out.println("BBOX: " + bbox);
            if (controlPoints != null) {
                System.out.println("Geo Points: " + controlPoints);
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

        ErrorLogger.LogMessage("Shape3DHandler", "RenderBasic3DShape()", "exit RenderBasic3DShape", Level.FINER);
        return jsonOutput.toString();

    }

    private static String KMLize(String id,
                                 String name,
                                 String description,
                                 String symbolCode,
                                 ArrayList<ShapeInfo3D> shapes,
                                 ArrayList<ShapeInfo3D> modifiers,
                                 IPointConversion ipc,
                                 boolean normalize,
                                 Color textColor,
                                 String altitudeMode,
                                 boolean wasClipped) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();
        ShapeInfo3D tempModifier = null;
        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";
        int len = shapes.size();
        kml.append("<Folder id=\"").append(id).append("\">");
        kml.append("<name>").append(cdataStart).append(name).append(cdataEnd).append("</name>");
        kml.append("<visibility>1</visibility>");
        kml.append("<description>").append(cdataStart).append(description).append(cdataEnd).append("</description>");
        kml.append("<ExtendedData>");
        kml.append("<Data name=\"symbolID\"><value>").append(cdataStart).append(symbolCode).append(cdataEnd).append("</value></Data>");
        kml.append("<Data name=\"wasClipped\"><value>").append(cdataStart).append(wasClipped).append(cdataEnd).append("</value></Data>");
        kml.append("</ExtendedData>");
        for (int i = 0; i < len; i++) {
            String shapesToAdd = ShapeToKMLString(shapes.get(i), ipc, normalize, altitudeMode);
            kml.append(shapesToAdd);
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {

            tempModifier = modifiers.get(j);

            //if(geMap)//if using google earth
            //assume kml text is going to be centered
            //AdjustModifierPointToCenter(tempModifier);

            String labelsToAdd = LabelToKMLString(tempModifier, ipc, normalize, textColor, altitudeMode);
            kml.append(labelsToAdd);
        }

        kml.append("</Folder>");
        return kml.toString();
    }

    private static String ShapeToKMLString(ShapeInfo3D shapeInfo,
                                           IPointConversion ipc,
                                           boolean normalize,
                                           String altitudeMode) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();
        Color lineColor = null;
        Color fillColor = null;
        String googleLineColor = null;
        String googleFillColor = null;
        BasicStroke stroke = null;
        int lineWidth = 4;

        kml.append("<Placemark>");
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
            kml.append("<color>").append(googleLineColor).append("</color>");
            kml.append("<colorMode>normal</colorMode>");
            kml.append("<width>").append(String.valueOf(lineWidth)).append("</width>");
            kml.append("</LineStyle>");
        }

        fillColor = shapeInfo.getFillColor();
        BufferedImage fillPattern = shapeInfo.getPatternFillImage();
        if (fillColor != null || fillPattern != null) {
            kml.append("<PolyStyle>");

            if (fillColor != null) {
                googleFillColor = Integer.toHexString(shapeInfo.getFillColor().getRGB());
                googleFillColor = JavaRendererUtilities.ARGBtoABGR(googleFillColor);
                kml.append("<color>").append(googleFillColor).append("</color>");
                kml.append("<colorMode>normal</colorMode>");
            }
            if (fillPattern != null) {
                kml.append("<shader>").append(MultiPointHandler.bitmapToString(fillPattern)).append("</shader>");
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

        ArrayList<ArrayList<Point3D>> shapesArray = shapeInfo.getPolylines3D();
        int len = shapesArray.size();
        kml.append("<MultiGeometry>");

        for (int i = 0; i < len; i++) {
            ArrayList<Point3D> shape = shapesArray.get(i);
            normalize = normalizePoints(shape, ipc);
            if (lineColor != null && fillColor == null) {
                kml.append("<Polygon>");
                kml.append("<tessellate>1</tessellate>");
                kml.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
                kml.append("<outerBoundaryIs><LinearRing><coordinates>");
                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++)
                for (int j = 0; j < n; j++) {
                    Point3D coord = shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);
                    if (normalize) {
                        geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
                    }

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
                    double altitude = coord.getZ();

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    kml.append(",");
                    kml.append(altitude);
                    if (j < shape.size() - 1) kml.append(" ");
                }

                kml.append("</coordinates></LinearRing></outerBoundaryIs>");
                kml.append("</Polygon>");
            }

            if (fillColor != null) {

                if (i == 0) {
                    kml.append("<Polygon>");
                    kml.append("<tessellate>1</tessellate>");
                    kml.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
                }
                //kml.append("<outerBoundaryIs>");
                if (i == 1 && len > 1) {
                    kml.append("<innerBoundaryIs>");
                } else {
                    kml.append("<outerBoundaryIs>");
                }
                kml.append("<LinearRing>");
                kml.append("<coordinates>");

                int n = shape.size();
                //for (int j = 0; j < shape.size(); j++)
                for (int j = 0; j < n; j++) {
                    Point3D coord = shape.get(j);
                    Point2D geoCoord = ipc.PixelsToGeo(coord);

                    double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                    double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
                    double altitude = coord.getZ();

                    //fix for fill crossing DTL
                    if (normalize) {
                        if (longitude > 0) {
                            longitude -= 360;
                        }
                    }

                    kml.append(longitude);
                    kml.append(",");
                    kml.append(latitude);
                    kml.append(",");
                    kml.append(altitude);
                    if (j < shape.size() - 1) kml.append(" ");
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

    private static String LabelToKMLString(ShapeInfo3D shapeInfo,
                                           IPointConversion ipc,
                                           boolean normalize,
                                           Color textColor,
                                           String altitudeMode) {
        java.lang.StringBuilder kml = new java.lang.StringBuilder();

        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point3D coord = new Point3D(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY(), shapeInfo.getModifierPosition().getZ());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-26-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double altitude = coord.getZ();
        long angle = Math.round(shapeInfo.getModifierAngle());

        String text = shapeInfo.getModifierString();

        String cdataStart = "<![CDATA[";
        String cdataEnd = "]]>";

        String color = Integer.toHexString(textColor.getRGB());
        color = JavaRendererUtilities.ARGBtoABGR(color);
        float kmlScale = RendererSettings.getInstance().getKMLLabelScale();

        if (kmlScale > 0 && text != null && text.equals("") == false) {
            kml.append("<Placemark>");//("<Placemark id=\"" + id + "_lp" + i + "\">");
            kml.append("<name>").append(cdataStart).append(text).append(cdataEnd).append("</name>");
            kml.append("<Style>");
            kml.append("<IconStyle>");
            kml.append("<scale>").append(kmlScale).append("</scale>");
            kml.append("<heading>").append(angle).append("</heading>");
            kml.append("<Icon>");
            kml.append("<href></href>");
            kml.append("</Icon>");
            kml.append("</IconStyle>");
            kml.append("<LabelStyle>");
            kml.append("<color>").append(color).append("</color>");
            kml.append("<scale>").append(String.valueOf(kmlScale)).append("</scale>");
            kml.append("</LabelStyle>");
            kml.append("</Style>");
            kml.append("<Point>");
            kml.append("<extrude>0</extrude>");
            kml.append("<altitudeMode>").append(altitudeMode).append("</altitudeMode>");
            kml.append("<coordinates>");
            kml.append(longitude);
            kml.append(",");
            kml.append(latitude);
            kml.append(",");
            kml.append(altitude);
            kml.append("</coordinates>");
            kml.append("</Point>");
            kml.append("</Placemark>");
        } else {
            return "";
        }

        return kml.toString();
    }


    private static String GeoJSONize(ArrayList<ShapeInfo3D> shapes,
                                     ArrayList<ShapeInfo3D> modifiers,
                                     IPointConversion ipc,
                                     boolean normalize,
                                     Color textColor,
                                     Color textBackgroundColor) {

        ShapeInfo3D tempModifier = null;
        StringBuilder fc = new StringBuilder();//JSON feature collection

        fc.append("[");

        int len = shapes.size();
        for (int i = 0; i < len; i++) {

            String shapesToAdd = ShapeToGeoJSONString(shapes.get(i), ipc, normalize);
            if (shapesToAdd.length() > 0) {
                fc.append(shapesToAdd);
                if (i < len - 1) {
                    fc.append(",");
                }
            }
        }

        int len2 = modifiers.size();

        for (int j = 0; j < len2; j++) {
            tempModifier = modifiers.get(j);

            String modifiersToAdd = null;
            if (modifiers.get(j).getModifierImage() != null) {
                modifiersToAdd = ImageToGeoJSONString(tempModifier, ipc, normalize);
            } else {
                modifiersToAdd = LabelToGeoJSONString(tempModifier, ipc, normalize, textColor, textBackgroundColor);
            }
            if (modifiersToAdd.length() > 0) {
                if (fc.length() > 1) fc.append(",");
                fc.append(modifiersToAdd);
            }
        }
        fc.append("]");
        String GeoJSON = fc.toString();
        return GeoJSON;
    }

    private static String ShapeToGeoJSONString(ShapeInfo3D shapeInfo, IPointConversion ipc, boolean normalize) {
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
        }

        //generate JSON properties for feature
        properties.append("\"properties\":{");
        properties.append("\"label\":\"\",");
        if (lineColor != null) {
            properties.append("\"strokeColor\":\"").append(RendererUtilities.colorToHexString(lineColor, false)).append("\",");
            properties.append("\"lineOpacity\":").append(String.valueOf(lineColor.getAlpha() / 255f)).append(",");
        }
        if (fillColor != null) {
            properties.append("\"fillColor\":\"").append(RendererUtilities.colorToHexString(fillColor, false)).append("\",");
            properties.append("\"fillOpacity\":").append(String.valueOf(fillColor.getAlpha() / 255f)).append(",");
        }
        if (shapeInfo.getPatternFillImage() != null) {
            properties.append("\"fillPattern\":\"").append(MultiPointHandler.bitmapToString(shapeInfo.getPatternFillImage())).append("\",");
        }
        if (stroke.getDashArray() != null) {
            sda = "\"strokeDasharray\":" + Arrays.toString(stroke.getDashArray()) + ",";
            properties.append(sda);
        }

        int lineCap = stroke.getEndCap();
        properties.append("\"lineCap\":").append(lineCap).append(",");

        String strokeWidth = String.valueOf(lineWidth);
        properties.append("\"strokeWidth\":").append(strokeWidth).append(",");
        properties.append("\"strokeWeight\":").append(strokeWidth);
        properties.append("},");


        properties.append("\"style\":{");
        if (lineColor != null) {
            properties.append("\"stroke\":\"").append(RendererUtilities.colorToHexString(lineColor, false)).append("\",");
            properties.append("\"line-opacity\":").append(String.valueOf(lineColor.getAlpha() / 255f)).append(",");
        }
        if (fillColor != null) {
            properties.append("\"fill\":\"").append(RendererUtilities.colorToHexString(fillColor, false)).append("\",");
            properties.append("\"fill-opacity\":").append(String.valueOf(fillColor.getAlpha() / 255f)).append(",");
        }
        if (stroke.getDashArray() != null) {
            float[] da = stroke.getDashArray();
            sda = String.valueOf(da[0]);
            if (da.length > 1) {
                for (int i = 1; i < da.length; i++) {
                    sda = sda + " " + String.valueOf(da[i]);
                }
            }
            sda = "\"stroke-dasharray\":\"" + sda + "\",";
            properties.append(sda);
            sda = null;
        }

        if (lineCap == BasicStroke.CAP_SQUARE) properties.append("\"stroke-linecap\":\"square\",");
        else if (lineCap == BasicStroke.CAP_ROUND)
            properties.append("\"stroke-linecap\":\"round\",");
        else if (lineCap == BasicStroke.CAP_BUTT) properties.append("\"stroke-linecap\":\"butt\",");

        strokeWidth = String.valueOf(lineWidth);
        properties.append("\"stroke-width\":").append(strokeWidth);
        properties.append("}");


        //generate JSON geometry for feature
        geometry.append("\"geometry\":{\"type\":");
        geometry.append(geometryType);
        geometry.append(",\"coordinates\":[");

        ArrayList<ArrayList<Point3D>> shapesArray = shapeInfo.getPolylines3D();

        for (int i = 0; i < shapesArray.size(); i++) {
            ArrayList<Point3D> pointList = shapesArray.get(i);

            normalize = normalizePoints(pointList, ipc);

            geometry.append("[");

            //System.out.println("Pixel Coords:");
            for (int j = 0; j < pointList.size(); j++) {
                Point3D coord = pointList.get(j);
                Point2D geoCoord = ipc.PixelsToGeo(coord);
                //M. Deutch 9-27-11
                if (normalize) {
                    geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
                }
                double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
                double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
                double altitude = coord.getZ();

                //fix for fill crossing DTL
                if (normalize && fillColor != null) {
                    if (longitude > 0) {
                        longitude -= 360;
                    }
                }

                //diagnostic M. Deutch 10-18-11
                //set the point as geo so that the
                //coord.setLocation(longitude, latitude);
                coord = new Point3D(longitude, latitude, altitude);
                pointList.set(j, coord);
                //end section

                geometry.append("[");
                geometry.append(longitude);
                geometry.append(",");
                geometry.append(latitude);
                geometry.append(",");
                geometry.append(altitude);
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

    private static String ImageToGeoJSONString(ShapeInfo3D shapeInfo, IPointConversion ipc, boolean normalize) {
        StringBuilder JSONed = new StringBuilder();

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point3D coord = new Point3D(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY(), shapeInfo.getModifierPosition().getZ());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double altitude = coord.getZ();
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        BufferedImage image = shapeInfo.getModifierImage();

        RendererSettings RS = RendererSettings.getInstance();

        if (image != null) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"image\":\"");
            JSONed.append(MultiPointHandler.bitmapToString(image));
            JSONed.append("\",\"rotation\":");
            JSONed.append(angle);
            JSONed.append(",\"angle\":");
            JSONed.append(angle);
            JSONed.append("},");
            JSONed.append("\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
            JSONed.append(longitude);
            JSONed.append(",");
            JSONed.append(latitude);
            JSONed.append(",");
            JSONed.append(altitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

    private static String LabelToGeoJSONString(ShapeInfo3D shapeInfo, IPointConversion ipc, boolean normalize, Color textColor, Color textBackgroundColor) {
        StringBuilder JSONed = new StringBuilder();

        Color outlineColor = SymbolDraw.getIdealTextBackgroundColor(textColor);
        if (textBackgroundColor != null) {
            outlineColor = textBackgroundColor;
        }

        //AffineTransform at = shapeInfo.getAffineTransform();
        //Point2D coord = (Point2D)new Point2D.Double(at.getTranslateX(), at.getTranslateY());
        //Point2D coord = (Point2D) new Point2D.Double(shapeInfo.getGlyphPosition().getX(), shapeInfo.getGlyphPosition().getY());
        Point3D coord = new Point3D(shapeInfo.getModifierPosition().getX(), shapeInfo.getModifierPosition().getY(), shapeInfo.getModifierPosition().getZ());
        Point2D geoCoord = ipc.PixelsToGeo(coord);
        //M. Deutch 9-27-11
        if (normalize) {
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
        }
        double latitude = Math.round(geoCoord.getY() * 100000000.0) / 100000000.0;
        double longitude = Math.round(geoCoord.getX() * 100000000.0) / 100000000.0;
        double altitude = coord.getZ();
        double angle = shapeInfo.getModifierAngle();
        coord.setLocation(longitude, latitude);

        //diagnostic M. Deutch 10-18-11
        shapeInfo.setGlyphPosition(coord);

        String text = shapeInfo.getModifierString();

        int justify = shapeInfo.getTextJustify();
        String strJustify = "left";
        if (justify == 0) strJustify = "left";
        else if (justify == 1) strJustify = "center";
        else if (justify == 2) strJustify = "right";


        RendererSettings RS = RendererSettings.getInstance();

        if (text != null && text.equals("") == false) {

            JSONed.append("{\"type\":\"Feature\",\"properties\":{\"label\":\"");
            JSONed.append(text);
            JSONed.append("\",\"pointRadius\":0,\"fontColor\":\"");
            JSONed.append(RendererUtilities.colorToHexString(textColor, false));
            JSONed.append("\",\"fontSize\":\"");
            JSONed.append(String.valueOf(RS.getMPLabelFont().getSize())).append("pt\"");
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
            JSONed.append(",");
            JSONed.append(altitude);
            JSONed.append("]");
            JSONed.append("}}");

        } else {
            return "";
        }

        return JSONed.toString();
    }

    /**
     * copy of {@link MultiPointHandler#normalizePoints(ArrayList, IPointConversion)} with Point3D
     */
    static Boolean normalizePoints(ArrayList<Point3D> shape, IPointConversion ipc) {
        ArrayList geoCoords = new ArrayList();
        int n = shape.size();
        //for (int j = 0; j < shape.size(); j++)
        for (int j = 0; j < n; j++) {
            Point2D coord = shape.get(j);
            Point2D geoCoord = ipc.PixelsToGeo(coord);
            geoCoord = MultiPointHandler.NormalizeCoordToGECoord(geoCoord);
            double latitude = geoCoord.getY();
            double longitude = geoCoord.getX();
            Point2D pt2d = new Point2D.Double(longitude, latitude);
            geoCoords.add(pt2d);
        }
        Boolean normalize = MultiPointHandler.crossesIDL(geoCoords);
        return normalize;
    }
}
