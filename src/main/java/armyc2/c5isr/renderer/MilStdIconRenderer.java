package armyc2.c5isr.renderer;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import armyc2.c5isr.renderer.utilities.*;

/**
 * This class is used for rendering icons that represent the single point graphics in the MilStd 2525.
 * It can also be used for rendering icon previews for multipoint graphics.
 */
public class MilStdIconRenderer
/* implements IIconRenderer */ {

    private String TAG = "MilStdIconRenderer";

    private static MilStdIconRenderer _instance = null;
    private AtomicBoolean _initSuccess = new AtomicBoolean(false);
    private SinglePointRenderer _SPR = null;
    private SinglePointSVGRenderer _SPSVGR = null;

    private MilStdIconRenderer() {
        init();
    }

    public static synchronized MilStdIconRenderer getInstance() {
        if (_instance == null) {
            _instance = new MilStdIconRenderer();
        }
        return _instance;
    }

    /**
     *
     *
     */
    private synchronized void init()// List<Typeface> fonts, List<String> xml
    {
        try {
            if (!_initSuccess.get()) {

                //Make sure Lookups are loaded in init so they're not loaded during the first render call
                SVGLookup.getInstance();
                MSLookup.getInstance();

                //test SVGLookup////////////////////////////////////////////////////////////////////
                /*SVGInfo oct = SVGLookup.getInstance().getSVGLInfo("octagon");
                System.out.println(oct.toString());*/

                //test MSLookup/////////////////////////////////////////////////////////////////////
                /*MSInfo msi = MSLookup.getInstance().getMSLInfo("50110100",0);//
                msi = MSLookup.getInstance().getMSLInfo("36190100",0);//"Non-Mine Mine–Like Object, Bottom"
                System.out.println(msi.getPath());
                System.out.println(msi.getName());
                msi = MSLookup.getInstance().getMSLInfo("01110300",0);//"Unmanned Aircraft (UA) / Unmanned Aerial Vehicle (UAV) / Unmanned Aircraft System (UAS) / Remotely Piloted Vehicle (RPV)"
                System.out.println(msi.getPath());
                System.out.println(msi.getName());//*/

                // setup single point renderer
                _SPR = SinglePointRenderer.getInstance();
                _SPSVGR = SinglePointSVGRenderer.getInstance();

                //Load country codes
                GENCLookup.getInstance();

                _initSuccess.set(true);
            }

        } catch (Exception exc) {
            ErrorLogger.LogException(TAG, "init",exc);
        }
    }

    public synchronized boolean isReady()
    {
        return _initSuccess.get();
    }

    // @Override

    /**
     * Checks symbol codes and returns whether they can be rendered.
     * For multi-point graphics, modifiers are ignored because we don't need that
     * information to show preview icons in the SymbolPicker.
     *
     * @param symbolID 20-30 digit 2525D Symbol ID Code
     * @param attributes (currently unused)
     * @return true if the basic form of the graphic can be rendered
     */
    public Boolean CanRender(String symbolID, HashMap<String,String> attributes)
    {
        String message = "";
        try {
            // Extract 8-digit ID to use with SVGLookup.
            // MSLookup can handle long codes, but SVGLookup can't because it also takes other strings.
            String lookupID = SymbolUtilities.getBasicSymbolID(symbolID);
            String lookupSVGID = SVGLookup.getMainIconID(symbolID);

            // Renderer only supports 2525D at the moment. 2525E will be in the future.
            /*
            int symStd = -1;
            int version = SymbolID.getVersion(symbolID);
            //SymbolID.Version_2525Dch1
            //SymbolID.Version_2525E
            */

            MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
            if (msi == null) {
                message = String.format("Cannot find %s in MSLookup", lookupID);
            } else if (msi.getDrawRule() == DrawRules.DONOTDRAW) {
                message = String.format("%s (%s) is DoNotDraw", lookupID, msi.getName());
            } else {
                int version = SymbolID.getVersion(symbolID);
                SVGInfo si = SVGLookup.getInstance().getSVGLInfo(lookupSVGID, version);
                if (si != null)// || (SymbolID.getEntityCode(symbolID)==000000 && SVGLookup.getInstance().getSVGLInfo(SVGLookup.getFrameID(symbolID)) != null))
                {
                    return true;
                }
                else
                {
                    message = String.format("Cannot find %s (%s) in SVGLookup", lookupID, msi.getName());
                }
            }
        } catch (Exception exc) {
            ErrorLogger.LogException("MilStdIconRenderer", "CanRender", exc);
        }
        // ErrorLogger.LogMessage(this.getClass().getName(), "CanRender()", message, Level.FINE);
        ErrorLogger.LogMessage("MilStdIconRenderer", "CanRender", message);
        return false;
    }



    // @Override
    public ImageInfo RenderIcon(String symbolID, Map<String,String> modifiers,
            Map<String,String> attributes)
    {

        int ss = SymbolID.getSymbolSet(symbolID);

        ImageInfo temp = null;
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
        if (msi == null)
        {
            //TODO: if null, try to fix the code so that something renders
                /*symbolID = SymbolUtilities.reconcileSymbolID(symbolID);
                basicSymbolID = SymbolUtilities.getBasicSymbolIDStrict(symbolID);
                sd = SymbolDefTable.getInstance().getSymbolDef(basicSymbolID, symStd);//*/
        }
        if (msi != null && msi.getDrawRule() == DrawRules.DONOTDRAW) {
            return null;
        }

        if (ss==SymbolID.SymbolSet_ControlMeasure)
        {
            if (msi != null) {
                //Point12 is actually a multipoint and 17 & 18 are rectangular target and sector range fan
                if (SymbolUtilities.isMultiPoint(symbolID)==false) {
                    temp = _SPR.RenderSP2(symbolID, modifiers, attributes);
                } else {
                    temp = _SPR.RenderSP2(symbolID, null, attributes);
                }
            }
        }
        else if(ss==SymbolID.SymbolSet_Atmospheric ||
                ss==SymbolID.SymbolSet_Oceanographic ||
                ss==SymbolID.SymbolSet_MeteorologicalSpace)
        {
            temp = _SPR.RenderSP2(symbolID, modifiers, attributes);
        }
        else
        {
            temp = _SPR.RenderUnit2(symbolID, modifiers, attributes);
        }

        return temp;
    }

    public SVGSymbolInfo RenderSVG(String symbolID, Map<String,String> modifiers,
                                Map<String,String> attributes)
    {

        //Update to use _SPSVGR.RenderUnit
        int ss = SymbolID.getSymbolSet(symbolID);

        ImageInfo temp = null;
        SVGSymbolInfo svgTemp = null;
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
        if (msi == null)
        {
            //TODO: if null, try to fix the code so that something renders
                /*symbolID = SymbolUtilities.reconcileSymbolID(symbolID);
                basicSymbolID = SymbolUtilities.getBasicSymbolIDStrict(symbolID);
                sd = SymbolDefTable.getInstance().getSymbolDef(basicSymbolID, symStd);//*/
        }
        if (msi != null && msi.getDrawRule() == DrawRules.DONOTDRAW) {
            return null;
        }

        if (ss==SymbolID.SymbolSet_ControlMeasure)
        {
            if (msi != null) {
                //Point12 is actually a multipoint and 17 & 18 are rectangular target and sector range fan
                if (SymbolUtilities.isMultiPoint(symbolID)==false) {
                    svgTemp = _SPSVGR.RenderSP(symbolID, modifiers, attributes);
                } else {
                    svgTemp = _SPSVGR.RenderSP(symbolID, null, attributes);
                }
            }
        }
        else if(ss==SymbolID.SymbolSet_Atmospheric ||
                ss==SymbolID.SymbolSet_Oceanographic ||
                ss==SymbolID.SymbolSet_MeteorologicalSpace)
        {
            svgTemp = _SPSVGR.RenderSP(symbolID, modifiers, attributes);
        }
        else
        {
            svgTemp = _SPSVGR.RenderUnit(symbolID, modifiers, attributes);
        }

        return svgTemp;
    }

    // @Override
    public String getRendererID()
    {

        return "milstd2525";
    }

    private Map<String,String> getDefaultAttributes(String symbolID)
    {
        Map<String,String> map = new HashMap<String,String>();
        try {
            if (symbolID == null || symbolID.length() != 15) {
                if (symbolID == null) {
                    symbolID = "null";
                }
                ErrorLogger.LogMessage("MilStdIconRenderer", "getDefaultAttributes",
                        "getDefaultAttributes passed bad symbolID: " + symbolID);
                return null;
            }

            map.put(MilStdAttributes.Alpha, "255");
            if (SymbolUtilities.hasDefaultFill(symbolID)) {
                map.put(MilStdAttributes.FillColor,
                        RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID),false));
            }

            map.put(MilStdAttributes.LineColor,
                    RendererUtilities.colorToHexString(SymbolUtilities.getLineColorOfAffiliation(symbolID),false));

            map.put(MilStdAttributes.OutlineSymbol, "false");
            // attribute[MilStdAttributes.SymbolOutlineColor] = null;
            // map.put(MilStdAttributes.OutlineWidth,"1");

            map.put(MilStdAttributes.DrawAsIcon, "false");

            RendererSettings rs = RendererSettings.getInstance();

            map.put(MilStdAttributes.KeepUnitRatio, "true");
            return map;
        } catch (Exception exc) {
            ErrorLogger.LogException("MilStdIconRenderer", "getDefaultAttributes", exc);
        }
        return map;
    }

    /**
     * Add a custom framed symbol to the renderer's collection
     * @param msInfo
     * @param svgInfo
     * @return
     */
    public boolean AddCustomSymbol(MSInfo msInfo, SVGInfo svgInfo)
    {
        boolean success = false;
        if(msInfo.getBasicSymbolID().equals(svgInfo.getID()))//Make sure IDs match
        {
            //Make sure entry isn't already there
            if(MSLookup.getInstance().getMSLInfo(msInfo.getBasicSymbolID(),msInfo.getVersion())==null &&
                    SVGLookup.getInstance().getSVGLInfo(svgInfo.getID(),msInfo.getVersion())==null)
            {
                if(MSLookup.getInstance().addCustomSymbol(msInfo))
                    success = SVGLookup.getInstance().addCustomSymbol(svgInfo,msInfo.getVersion());
            }
        }
        else
        {
            ErrorLogger.LogMessage("Symbol Set and Entity Codes do not match", Level.INFO,false);
        }
        return success;
    }
}
