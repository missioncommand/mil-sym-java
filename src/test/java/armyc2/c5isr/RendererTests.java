package armyc2.c5isr;

import armyc2.c5isr.RenderMultipoints.clsRenderer;
import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.MultiPointHandler;
import armyc2.c5isr.web.render.WebRenderer;
import org.junit.jupiter.api.Test;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

public class RendererTests {

    // helper for testInvalidCodes()
    private static void testCode(String testID) {
        ErrorLogger.setLevel(Level.SEVERE); // Ignore warnings
        Map<String, String> modifiers = new HashMap<>();
        Map<String, String> attributes = new HashMap<>();
        MilStdIconRenderer mir = MilStdIconRenderer.getInstance();
        ImageInfo ii = mir.RenderIcon(testID, modifiers, attributes);
        MSInfo msi = null;
        try {
            msi = MSLookup.getInstance().getMSLInfo(testID);
        } catch (Exception ignore) {
        }
        // Expected to get null image back if: symbol id is not a number, symbol id length < 20, invalid control measure, or draw rule is DONOTDRAW
        // Otherwise should get some image back
        if (SymbolUtilities.isNumber(testID) && testID.length() >= 20 && SymbolID.getSymbolSet(testID) != 25 && (msi == null || msi.getDrawRule() != DrawRules.DONOTDRAW))
            assertFalse(ii == null || ii.getImage() == null, "Unexpected null image returned: " + testID);

        // test multipoint
        ArrayList<Point2D> points = new ArrayList<>(Arrays.asList(
                new Point2D.Double(49.929272727272725, 20.07),
                new Point2D.Double(49.931090909090905, 20.03622222222222),
                new Point2D.Double(49.94745454545455, 20.024555555555555),
                new Point2D.Double(49.978272727272724, 20.023666666666667),
                new Point2D.Double(49.98672727272727, 20.052333333333333),
                new Point2D.Double(49.96490909090909, 20.076444444444444)));
        final double scale = 50000;
        String controlPtsStr = "";
        for (int i = 0; i < points.size(); i++) {
            controlPtsStr += points.get(i).getX() + "," + points.get(i).getY();
            if (i < points.size() - 1) {
                controlPtsStr += " ";
            }
        }
        PointConversion pConverter = new PointConversion(1100, 900, 20.1, 49.9, 20.0, 50.0);
        String bbox = pConverter.getLeftLon() + "," + pConverter.getLowerLat() + "," + pConverter.getRightLon() + "," + pConverter.getUpperLat();

        MilStdSymbol mss = WebRenderer.RenderMultiPointAsMilStdSymbol("id", "name", "description", testID, controlPtsStr, "", scale, bbox, modifiers, attributes);
        assertFalse((mss == null || mss.getSymbolShapes() == null) && MultiPointHandler.canRenderMultiPoint(testID, modifiers, points.size()).equals("true"), "Unexpected null from RenderMultiPointAsMilStdSymbol: " + testID);

        String result = WebRenderer.RenderSymbol2D("id", "name", "desc", testID, controlPtsStr, pConverter.getPixelWidth(), pConverter.getPixelHeight(), bbox, modifiers, attributes, WebRenderer.OUTPUT_FORMAT_GEOJSON);
        String altMode = "clampToGround";
        String result3D = WebRenderer.RenderSymbol("id", "name", "desc", testID, controlPtsStr, altMode, scale, bbox, modifiers, attributes, WebRenderer.OUTPUT_FORMAT_GEOJSON);
        assertFalse(result == null || result3D == null || result.isEmpty() || result3D.isEmpty(), "Unexpected null/Empty GeoJSON " + testID);

        if (!SymbolUtilities.isNumber(testID))
            return; // no way for MilStdSymbol constructor to enforce
        MilStdSymbol ms = new MilStdSymbol(testID, "id", points, modifiers);
        clsRenderer.renderWithPolylines(ms, pConverter, null);
        assertNotNull(ms.getSymbolShapes(), "Unexpected null symbol shapes returned: " + testID);
    }

    @Test
    public void testInvalidCodes() {
        final String defaultID = "100301000011000011110000000000";

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setVersion(defaultID, i));
        }

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setStandardIdentity(defaultID, i));
        }

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setSymbolSet(defaultID, i));
        }

        for (int i = 0; i <= 9; i++) {
            testCode(SymbolID.setStatus(defaultID, i));
        }

        for (int i = 0; i <= 9; i++) {
            testCode(SymbolID.setHQTFD(defaultID, i));
        }

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setAmplifierDescriptor(defaultID, i));
        }

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setModifier1(defaultID, i));
        }

        for (int i = 0; i <= 99; i++) {
            testCode(SymbolID.setModifier2(defaultID, i));
        }
    }

    @Test
    // Takes roughly 8 minutes
    public void testRandomEC() {
        final String defaultID = "100301000011000011110000000000";
        for (int i = 100000; i <= 500000; i++) {
            testCode(SymbolID.setEntityCode(defaultID, i));
        }
    }

    // Verifies valid output without errors for all multipoint symbols using both expected control points and duplicated control points
    @Test
    public void testMPSymbols() {
        HashSet<String> allBasicIDs = new HashSet<>();
        allBasicIDs.addAll(MSLookup.getInstance().getIDList(SymbolID.Version_2525Dch1));
        allBasicIDs.addAll(MSLookup.getInstance().getIDList(SymbolID.Version_2525Ech1));
        HashMap<String, String> modifiers = new HashMap<>();
        modifiers.put(Modifiers.AM_DISTANCE, "1,10");
        modifiers.put(Modifiers.AN_AZIMUTH, "0,90");
        HashMap<String, String> attributes = new HashMap<>();
        final String validControlPtsStr = "50,20 50,21 50,22 49,22 49,21 49,20";
        final String duplicateControlPtsStr = "50,20 50,20 50,20 50,20 50,20 50,20";
        final String bbox = "49,20,50,22";
        for (String basicID : allBasicIDs) {
            for (int version : new int[]{SymbolID.Version_2525Dch1, SymbolID.Version_2525Ech1}) {
                final String symbolCode = "" + version + SymbolID.StandardIdentity_Context_Reality +
                        SymbolID.StandardIdentity_Affiliation_Friend + basicID.substring(0, 2) +
                        SymbolID.Status_Present + SymbolID.HQTFD_Unknown + SymbolID.Echelon_Team_Crew +
                        basicID.substring(2) + "0000000000";

                if (SymbolUtilities.isMultiPoint(symbolCode)) {
                    for (String controlPtsStr : new String[]{validControlPtsStr, duplicateControlPtsStr}) {
                        ByteArrayOutputStream ErrOutputStream = new ByteArrayOutputStream();
                        System.setErr(new PrintStream(ErrOutputStream));
                        MilStdSymbol mss = WebRenderer.RenderMultiPointAsMilStdSymbol("", "", "", symbolCode, controlPtsStr, "", 100, bbox, modifiers, attributes);
                        assertNotNull(mss);
                        assertEquals(ErrOutputStream.toString(), ""); // Assert no errors thrown

                        for (ShapeInfo shape : mss.getSymbolShapes()) {
                            for (ArrayList<Point2D> polyline : shape.getPolylines()) {
                                for (Point2D pt : polyline) {
                                    assertNotNull(pt);
                                    assertFalse(pt.getX() == 0 && pt.getY() == 0);
                                }
                            }
                        }
                        for (ShapeInfo shape : mss.getModifierShapes()) {
                            Point2D pt = shape.getModifierPosition();
                            assertNotNull(pt);
                            assertFalse(pt.getX() == 0 && pt.getY() == 0);
                            pt = shape.getGlyphPosition();
                            assertNotNull(pt);
                            assertFalse(pt.getX() == 0 && pt.getY() == 0);
                        }

                        String kml = WebRenderer.RenderSymbol("", "", "", symbolCode, controlPtsStr, "", 100, bbox, modifiers, attributes, WebRenderer.OUTPUT_FORMAT_KML);
                        assertTrue(kml.startsWith("<Folder id=\"\"><name><![CDATA[]]>"));
                        String geojson = WebRenderer.RenderSymbol("", "", "", symbolCode, controlPtsStr, "", 100, bbox, modifiers, attributes, WebRenderer.OUTPUT_FORMAT_GEOJSON);
                        assertTrue(geojson.startsWith("{\"type\":\"FeatureCollection\",\"features\":"));
                        String svg = WebRenderer.RenderSymbol("", "", "", symbolCode, controlPtsStr, "", 100, bbox, modifiers, attributes, WebRenderer.OUTPUT_FORMAT_GEOSVG);
                        assertTrue(svg.startsWith("<svg width="));
                    }
                }
            }
        }
    }
}
