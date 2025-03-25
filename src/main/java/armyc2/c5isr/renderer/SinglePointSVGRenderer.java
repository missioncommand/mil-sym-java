package armyc2.c5isr.renderer;


import armyc2.c5isr.renderer.utilities.*;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class SinglePointSVGRenderer {

    private final String TAG = "SinglePointSVGRenderer";
    private static SinglePointSVGRenderer _instance = null;
    public static final String RENDERER_ID = "2525D";
    private BufferedImage _buffer = null;
    private FontRenderContext _fontRenderContext = null;


    public SinglePointSVGRenderer()
    {

    }
    
    private void init()
    {
        try
        {
            SVGLookup.getInstance();
            ModifierRenderer.getInstance();

            if(_buffer == null)
            {
                _buffer = new BufferedImage(8,8,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D)_buffer.createGraphics();
                _fontRenderContext = g2d.getFontRenderContext();
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SinglePointRenderer", "init", exc);
        }
    }

    public static synchronized SinglePointSVGRenderer getInstance()
    {
        if (_instance == null)
        {
            _instance = new SinglePointSVGRenderer();
            _instance.init();
        }

        return _instance;
    }


    public  SVGSymbolInfo render(String symbolID, Map<String, String> modifiers, Map<String, String> attributes) {
        SVGSymbolInfo si = null;//new SinglePointInfo(null, x, y);


        if(modifiers == null)
        {
            modifiers = new HashMap<String, String>();
        }

        if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_ControlMeasure ||
                SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_Atmospheric ||
                SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_Oceanographic ||
                SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_MeteorologicalSpace)
        {
            //30022500001310010000
            si = this.RenderSP(symbolID, modifiers, attributes);
        }
        else
        {
            //30020100001107000000
            si = this.RenderUnit(symbolID, modifiers, attributes);
        }

        return si;
    }

    public SVGSymbolInfo RenderUnit(String symbolID, Map<String, String> modifiers, Map<String, String> attributes) {
        ImageInfo ii = null;//new SinglePointInfo(null, x, y);
        SVGSymbolInfo si = null;
        SymbolDimensionInfo newSDI = null;
        try
        {
            String lineColor = null;//SymbolUtilitiesD.getLineColorOfAffiliation(symbolID);
            String fillColor = null;

            if(SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_MineWarfare && RendererSettings.getInstance().getSeaMineRenderMethod()==RendererSettings.SeaMineRenderMethod_MEDAL)
            {
                lineColor = RendererUtilities.colorToHexString(SymbolUtilities.getLineColorOfAffiliation(symbolID), false);
                fillColor = RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID), true);
            }

            String iconColor = null;

            float alpha = -1;

            //SVG values
            String frameID = null;
            String iconID = null;
            String mod1ID = null;
            String mod2ID = null;
            SVGInfo siFrame = null;
            SVGInfo siIcon = null;
            SVGInfo siMod1 = null;
            SVGInfo siMod2 = null;
            int top = 0;
            int left = 0;
            int width = 0;
            int height = 0;
            String svgStart = null;
            String strSVG = null;
            String strSVGFrame = null;


            Rectangle2D symbolBounds = null;
            Rectangle2D fullBounds = null;
            BufferedImage fullBMP = null;

            boolean hasDisplayModifiers = false;
            boolean hasTextModifiers = false;

            int pixelSize = 50;
            boolean keepUnitRatio = true;
            double scale = 1.0;
            boolean icon = false;
            boolean asIcon = false;
            boolean noFrame = false;

            int ver = SymbolID.getVersion(symbolID);


            if(modifiers != null)
            {
                if(attributes.containsKey(MilStdAttributes.PixelSize))
                    pixelSize = Integer.valueOf(attributes.get(MilStdAttributes.PixelSize));
                if(attributes.containsKey(MilStdAttributes.LineColor))
                    lineColor = attributes.get(MilStdAttributes.LineColor);
                if(attributes.containsKey(MilStdAttributes.FillColor))
                    fillColor = attributes.get(MilStdAttributes.FillColor);
                if(attributes.containsKey(MilStdAttributes.IconColor))
                    iconColor = attributes.get(MilStdAttributes.IconColor);
                if(attributes.containsKey(MilStdAttributes.DrawAsIcon))
                    asIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                if(attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                    keepUnitRatio = Boolean.valueOf(attributes.get(MilStdAttributes.KeepUnitRatio));
            }

            // <editor-fold defaultstate="collapsed" desc="Parse Attributes">
            try
            {

                if (attributes.containsKey(MilStdAttributes.PixelSize))
                {
                    pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
                }
                else
                {
                    pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
                }

                if (attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                {
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));
                }

                if (attributes.containsKey(MilStdAttributes.DrawAsIcon))
                {
                    icon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                }

                if (icon)//icon won't show modifiers or display icons
                {
                    //TODO: symbolID modifications as necessary
                    keepUnitRatio = false;
                    hasDisplayModifiers = false;
                    hasTextModifiers = false;
                    //symbolID = symbolID.substring(0, 10) + "-----";
                }
                else
                {
                    hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                    hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
                }

                if (attributes.containsKey(MilStdAttributes.LineColor))
                {
                    lineColor = attributes.get(MilStdAttributes.LineColor);
                }
                if (attributes.containsKey(MilStdAttributes.FillColor))
                {
                    fillColor = attributes.get(MilStdAttributes.FillColor);
                }
            /* if (attributes.indexOfKey(MilStdAttributes.IconColor) >= 0)
            {
                iconColor = new Color(attributes.get(MilStdAttributes.IconColor));
            }//*/
                if (attributes.containsKey(MilStdAttributes.Alpha))
                {
                    alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha));
                }

            }
            catch (Exception excModifiers)
            {
                ErrorLogger.LogException("MilStdIconRenderer", "RenderUnit", excModifiers);
            }
            // </editor-fold>


            String key = makeCacheKey(symbolID, lineColor, fillColor, pixelSize, keepUnitRatio, false);;
            //see if it's in the cache
            /*if(_unitCache != null)
            {
                ii = _unitCache.get(key);
            }//*/

            if(ii == null) {

                int version = SymbolID.getVersion(symbolID);
                //Get SVG pieces of symbol
                frameID = SVGLookup.getFrameID(symbolID);
                iconID = SVGLookup.getMainIconID(symbolID);
                mod1ID = SVGLookup.getMod1ID(symbolID);
                mod2ID = SVGLookup.getMod2ID(symbolID);
                siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                siIcon = SVGLookup.getInstance().getSVGLInfo(iconID, version);

                if(siFrame == null)
                {
                    frameID = SVGLookup.getFrameID(SymbolUtilities.reconcileSymbolID(symbolID));
                    siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                    if(siFrame == null)//still no match, get unknown frame
                    {
                        frameID = SVGLookup.getFrameID(SymbolID.setSymbolSet(symbolID,SymbolID.SymbolSet_Unknown));
                        siFrame = SVGLookup.getInstance().getSVGLInfo(frameID, version);
                    }
                }

                if(siIcon == null)
                {
                    if(iconID.substring(2,8).equals("000000")==false && MSLookup.getInstance().getMSLInfo(symbolID) == null)
                        siIcon = SVGLookup.getInstance().getSVGLInfo("98100000", version);//inverted question mark
                    else if(SymbolID.getSymbolSet(symbolID) == SymbolID.SymbolSet_Unknown)
                        siIcon = SVGLookup.getInstance().getSVGLInfo("00000000", version);//question mark
                }

                siMod1 = SVGLookup.getInstance().getSVGLInfo(mod1ID, version);
                siMod2 = SVGLookup.getInstance().getSVGLInfo(mod2ID, version);
                top = (int)Math.round(siFrame.getBbox().getY());
                left = (int)Math.round(siFrame.getBbox().getX());
                width = (int)Math.round(siFrame.getBbox().getWidth());
                height = (int)Math.round(siFrame.getBbox().getHeight());

                //update line and fill color of frame SVG
                if(lineColor != null || fillColor != null)
                    strSVGFrame = RendererUtilities.setSVGFrameColors(symbolID,siFrame.getSVG(), RendererUtilities.getColorFromHexString(lineColor), RendererUtilities.getColorFromHexString(fillColor));
                else
                    strSVGFrame = siFrame.getSVG();

                if(frameID.equals("octagon"))//for the 1 unit symbol that doesn't have a frame: 30 + 15000
                {
                    noFrame = true;
                    strSVGFrame = strSVGFrame.replaceFirst("<g id=\"octagon\">", "<g id=\"octagon\" display=\"none\">");
                }


                //get SVG dimensions and target dimensions
                symbolBounds = RectUtilities.toRectangle(left,top,width,height);
                Rectangle2D rect = RectUtilities.copyRect(symbolBounds);
                float ratio = -1;

                if (pixelSize > 0 && keepUnitRatio == true)
                {
                    float heightRatio = SymbolUtilities.getUnitRatioHeight(symbolID);
                    float widthRatio = SymbolUtilities.getUnitRatioWidth(symbolID);

                    if(noFrame == true)//using octagon with display="none" as frame for a 1x1 shape
                    {
                        heightRatio = 1.0f;
                        widthRatio = 1.0f;
                    }

                    if (heightRatio > widthRatio)
                    {
                        pixelSize = (int) ((pixelSize / 1.5f) * heightRatio);
                    }
                    else
                    {
                        pixelSize = (int) ((pixelSize / 1.5f) * widthRatio);
                    }
                }
                if (pixelSize > 0)
                {
                    float p = pixelSize;
                    float h = (float)rect.getHeight();
                    float w = (float)rect.getWidth();

                    ratio = Math.min((p / h), (p / w));

                    symbolBounds = RectUtilities.makeRectangle2DFromRect(0f, 0f, w * ratio, h * ratio);
                }

                //StringBuilder sbGroupUnit = new StringBuilder();
                String sbGroupUnit = "";
                if(siFrame != null)
                {
                    sbGroupUnit += ("<g transform=\"translate(" + (siFrame.getBbox().getX() * -ratio) + ',' + (siFrame.getBbox().getY() * -ratio) + ") scale(" + ratio + "," + ratio + ")\"" + ">");
                    if(siFrame != null)
                        sbGroupUnit += (strSVGFrame);//(siFrame.getSVG());

                    String color = "";
                    if(iconColor != null)
                    {
                        //make sure string is properly formatted.
                        iconColor = RendererUtilities.colorToHexString(RendererUtilities.getColorFromHexString(iconColor),false);
                        if(iconColor != null && iconColor != "#000000" && iconColor != "")
                            color = " stroke=\"" + iconColor + "\" fill=\"" + iconColor + "\" ";
                        else
                            iconColor = null;
                    }
                    String unit = "<g" + color + ">";
                    if (siIcon != null)
                        unit += (siIcon.getSVG());
                    if (siMod1 != null)
                        unit += (siMod1.getSVG());
                    if (siMod2 != null)
                        unit += (siMod2.getSVG());
                    if(iconColor != null)
                        unit = unit.replaceAll("#000000",iconColor);
                    unit += "</g>";

                    sbGroupUnit += unit + "</g>";
                }




                //center of octagon is the center of all unit symbols
                Point centerOctagon = new Point(306, 396);
                centerOctagon.translate(-left,-top);//offset for the symbol bounds x,y
                //scale center point by same ratio as the symbol
                centerOctagon = new Point((int)(centerOctagon.x * ratio), (int)(centerOctagon.y * ratio));

                //set centerpoint of the image
                Point centerPoint = centerOctagon;
                Point centerCache = new Point(centerOctagon.x, centerOctagon.y);

                //y offset to get centerpoint so we set back to zero when done.
                //symbolBounds.top = 0;
                RectUtilities.shift(symbolBounds,0,(int)-symbolBounds.getY());

                //Add core symbol to SVGSymbolInfo
                Point2D anchor = new Point2D.Double(symbolBounds.getCenterX(),symbolBounds.getCenterY());
                si =  new SVGSymbolInfo(sbGroupUnit.toString(), anchor,symbolBounds,symbolBounds);

                hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
                hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);
                //process display modifiers
                if (hasDisplayModifiers)
                {
                    newSDI = ModifierRenderer.processUnitDisplayModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                    if(newSDI != null)
                    {
                        si = (SVGSymbolInfo) newSDI;
                        newSDI = null;
                    }
                }
            }

            //process text modifiers
            if (hasTextModifiers)
            {
                int ss = SymbolID.getSymbolSet(symbolID);
                switch(ss)
                {
                    case SymbolID.SymbolSet_LandUnit:
                    case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandUnitTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processLandUnitTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_LandEquipment:
                    case SymbolID.SymbolSet_SignalsIntelligence_Land:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processLandEquipmentTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_LandInstallation:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processLandInstallationTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processLandInstallationTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_DismountedIndividuals:
                        newSDI = ModifierRenderer.processDismountedIndividualsTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_Space:
                    case SymbolID.SymbolSet_SpaceMissile:
                    case SymbolID.SymbolSet_Air:
                    case SymbolID.SymbolSet_AirMissile:
                    case SymbolID.SymbolSet_SignalsIntelligence_Air:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_SignalsIntelligence_Space:
                        if(ver < SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processAirSpaceUnitTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else//SIGINT in 2525E+ uses modifer places based on frame shape
                        {
                            char frameShape = SymbolID.getFrameShape(symbolID);
                            if(frameShape == SymbolID.FrameShape_Space || frameShape == SymbolID.FrameShape_Air)
                                newSDI = ModifierRenderer.processAirSpaceUnitTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                            else if(frameShape == SymbolID.FrameShape_LandEquipment_SeaSurface)//sea surface, but can't tell which so default land equip
                                newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                            else if(frameShape == SymbolID.FrameShape_SeaSubsurface)
                                newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                            else//default land equipment
                                newSDI = ModifierRenderer.processLandEquipmentTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        }
                        break;
                    case SymbolID.SymbolSet_SeaSurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processSeaSurfaceTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processSeaSurfaceTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_SeaSubsurface:
                    case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processSeaSubSurfaceTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_Activities:
                        if(ver >= SymbolID.Version_2525E)
                            newSDI = ModifierRenderer.processActivitiesTextModifiersE(si, symbolID, modifiers, attributes, _fontRenderContext);
                        else
                            newSDI = ModifierRenderer.processActivitiesTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_CyberSpace:
                        newSDI = ModifierRenderer.processCyberSpaceTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                        break;
                    case SymbolID.SymbolSet_MineWarfare:
                        break;//no modifiers
                    case SymbolID.SymbolSet_Unknown:
                    default: //in theory, will never get here
                        newSDI = ModifierRenderer.processUnknownTextModifiers(si, symbolID, modifiers, attributes, _fontRenderContext);
                }

            }

            if (newSDI != null)
            {
                si = (SVGSymbolInfo) newSDI;
            }
            newSDI = null;//*/

            int widthOffset = 0;
            if(hasTextModifiers)
                widthOffset = 2;//add for the text outline

            int svgWidth = (int)(si.getImageBounds().getWidth()+widthOffset);
            int svgHeight = (int)si.getImageBounds().getHeight();
            //add SVG tag with dimensions
            //draw unit from SVG
            String svgAlpha = "";
            if(alpha >=0 && alpha <= 255)
                svgAlpha = " opacity=\"" + alpha/255f + "\"";
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + svgWidth + "\" height=\"" + svgHeight +"\" viewBox=\"" + 0 + " " + 0 + " " + svgWidth + " " + svgHeight + "\"" + svgAlpha + ">\n";
            String svgTranslateGroup = null;

            double transX = si.getImageBounds().getX() * -1;
            double transY = si.getImageBounds().getY() * -1;
            Point2D anchor = si.getSymbolCenterPoint();
            Rectangle2D imageBounds = si.getImageBounds();
            if(transX > 0 || transY > 0)
            {
                ShapeUtilities.offset(anchor,transX,transY);
                ShapeUtilities.offset(symbolBounds,transX,transY);
                ShapeUtilities.offset(imageBounds,transX,transY);
                svgTranslateGroup = "<g transform=\"translate(" + transX + "," + transY + ")" +"\">\n";
            }
            imageBounds.setRect(imageBounds.getX(),imageBounds.getY(),svgWidth,svgHeight);
            si = new SVGSymbolInfo(si.getSVG(),anchor,symbolBounds,imageBounds);
            StringBuilder sbSVG = new StringBuilder();
            sbSVG.append(svgStart);
            sbSVG.append(makeDescTag(si));
            sbSVG.append(makeMetadataTag(symbolID, si));
            if(svgTranslateGroup != null)
                sbSVG.append(svgTranslateGroup);
            sbSVG.append(si.getSVG());
            if(svgTranslateGroup != null)
                sbSVG.append("\n</g>");
            sbSVG.append("\n</svg>");
            si =  new SVGSymbolInfo(sbSVG.toString(),anchor,symbolBounds,imageBounds);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("SinglePointRenderer","renderUnit",exc);
        }

        return si;
    }

    public SVGSymbolInfo RenderSP(String symbolID, Map<String, String> modifiers, Map<String, String> attributes)
    {
        SVGSymbolInfo si = null;

        int pixelSize = 50;
        double scale = 1.0;
        String lineColor = null;//SymbolUtilitiesD.getLineColorOfAffiliation(symbolID);
        String fillColor = null;
        float alpha = -1;

        boolean keepUnitRatio = true;
        boolean asIcon = false;
        boolean hasDisplayModifiers = false;
        boolean hasTextModifiers = false;
        boolean outlineSymbol = false;

        //SVG rendering variables
        MSInfo msi = null;
        String iconID = null;
        SVGInfo siIcon = null;
        String mod1ID = null;
        SVGInfo siMod1 = null;
        int top = 0;
        int left = 0;
        int width = 0;
        int height = 0;
        String svgStart = null;
        String strSVG = null;

        double ratio = 0;

        Rectangle2D symbolBounds = null;
        Rectangle2D fullBounds = null;
        BufferedImage fullBMP = null;


        ImageInfo ii = null;


        try
        {
            msi = MSLookup.getInstance().getMSLInfo(symbolID);

            int ss = SymbolID.getSymbolSet(symbolID);
            int ec = SymbolID.getEntityCode(symbolID);
            int mod1 = 0;
            int drawRule = 0;
            boolean hasAPFill = false;
            if(msi!=null){drawRule = msi.getDrawRule();}
            if(SymbolUtilities.isActionPoint(symbolID) || //action points
                    drawRule==DrawRules.POINT10 || //Sonobuoy
                    ec == 180100 || ec == 180200 || ec == 180400) //ACP, CCP, PUP
            {
                if(SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_ControlMeasure)
                {
                    lineColor = "#000000";
                    hasAPFill = true;
                }
            }
            if(lineColor==null)
                lineColor = RendererUtilities.colorToHexString(SymbolUtilities.getDefaultLineColor(symbolID),false);

            //fillColor = "#FF0000";
            //stroke-opacity
            //fill-opacity="0.4"
            //opacity
            if(attributes != null)
            {
                if(attributes.containsKey(MilStdAttributes.PixelSize))
                    pixelSize = Integer.valueOf(attributes.get(MilStdAttributes.PixelSize));
                if(attributes.containsKey(MilStdAttributes.LineColor))
                    lineColor = attributes.get(MilStdAttributes.LineColor);
                if(attributes.containsKey(MilStdAttributes.FillColor))
                    fillColor = attributes.get(MilStdAttributes.FillColor);
                if (attributes.containsKey(MilStdAttributes.Alpha))
                    alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                if(attributes.containsKey(MilStdAttributes.DrawAsIcon))
                    asIcon = Boolean.parseBoolean(attributes.get(MilStdAttributes.DrawAsIcon));
                if(attributes.containsKey(MilStdAttributes.KeepUnitRatio))
                    keepUnitRatio = Boolean.parseBoolean(attributes.get(MilStdAttributes.KeepUnitRatio));

                if(asIcon==false)//don't outline icons because they're not going on the map
                {
                    if(attributes.containsKey(MilStdAttributes.OutlineSymbol))
                        outlineSymbol = Boolean.parseBoolean(attributes.get(MilStdAttributes.OutlineSymbol));
                    else
                        outlineSymbol = RendererSettings.getInstance().getOutlineSPControlMeasures();
                }

                if(SymbolUtilities.isMultiPoint(symbolID))
                    outlineSymbol=false;//icon previews for multipoints do not need outlines since they shouldn't be on the map

            }

            if(keepUnitRatio)
            {
                pixelSize = (int)Math.ceil((pixelSize/1.5f) * 1.75f);
            }




            if (ss==SymbolID.SymbolSet_ControlMeasure && ec == 270701)//static depiction
            {
                //add mine fill to image
                mod1 = SymbolID.getModifier1(symbolID);
                if (!(mod1 >= 13 && mod1 <= 50))
                    symbolID = SymbolID.setModifier1(symbolID, 13);
            }


            //if not, generate symbol.
            if (si == null)//*/
            {
                int version = SymbolID.getVersion(symbolID);
                //check symbol size////////////////////////////////////////////
                Rectangle2D rect = null;
                iconID = SVGLookup.getMainIconID(symbolID);
                siIcon = SVGLookup.getInstance().getSVGLInfo(iconID, version);
                mod1ID = SVGLookup.getMod1ID(symbolID);
                siMod1 = SVGLookup.getInstance().getSVGLInfo(mod1ID, version);
                float borderPadding = 0;
                if (outlineSymbol) {
                    borderPadding = RendererUtilities.findWidestStrokeWidth(siIcon.getSVG());
                }
                top = (int)Math.round(siIcon.getBbox().getY());
                left = (int)Math.round(siIcon.getBbox().getX());
                width = (int)Math.round(siIcon.getBbox().getWidth());
                height = (int)Math.round(siIcon.getBbox().getHeight());

                String strSVGIcon = null;


                if(drawRule==DrawRules.POINT1) //action points and a few others
                {//TODO: move this stroke width adjustment to the external took that makes 2525D.SVG & 2525E.SVG
                    siIcon = new SVGInfo(siIcon.getID(),siIcon.getBbox(), siIcon.getSVG().replaceAll("stroke-width=\"4\"","stroke-width=\"6\""));
                }
                if(hasAPFill) //Action Point(s), Sonobuoys, ACP, CCP, PUP
                {
                    String apFill;
                    if(fillColor != null)
                        apFill = fillColor;
                    else
                        apFill = RendererUtilities.colorToHexString(SymbolUtilities.getFillColorOfAffiliation(symbolID),false);
                    siIcon = new SVGInfo(siIcon.getID(),siIcon.getBbox(), siIcon.getSVG().replaceAll("fill=\"none\"","fill=\"" + apFill + "\""));
                }

                //update line and fill color of frame SVG
                if(msi.getSymbolSet() == SymbolID.SymbolSet_ControlMeasure && (lineColor != null || fillColor != null)) {
                    if (outlineSymbol) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon = RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), RendererUtilities.getIdealOutlineColor(RendererUtilities.getColorFromHexString(lineColor)), RendererUtilities.getColorFromHexString(fillColor), true);
                    }
                    else
                        strSVGIcon = "";

                    // append normal symbol SVG to be layered on top of outline
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(symbolID, siIcon.getSVG(), RendererUtilities.getColorFromHexString(lineColor), RendererUtilities.getColorFromHexString(fillColor), false);
                }
                else//weather symbol (don't change color of weather graphics)
                    strSVGIcon = siIcon.getSVG();

                //If symbol is Static Depiction, add internal mine graphic based on sector modifier 1
                if(SymbolID.getEntityCode(symbolID) == 270701 && siMod1 != null)
                {
                    if (outlineSymbol) {
                        // create outline with larger stroke-width first (if selected)
                        strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), RendererUtilities.getIdealOutlineColor(RendererUtilities.getColorFromHexString("#00A651")), RendererUtilities.getColorFromHexString("#00A651"), true);
                    }
                    //strSVGIcon += siMod1.getSVG();
                    strSVGIcon += RendererUtilities.setSVGSPCMColors(mod1ID, siMod1.getSVG(), RendererUtilities.getColorFromHexString(lineColor), RendererUtilities.getColorFromHexString(fillColor), false);
                }

                if (pixelSize > 0)
                {
                    symbolBounds = RectUtilities.toRectangle(left,top,width,height);//actual measurement of symbol svg
                    rect = RectUtilities.copyRect(symbolBounds);

                    //adjust size
                    float p = pixelSize;
                    double h = rect.getHeight();
                    double w = rect.getWidth();

                    ratio = Math.min((p / h), (p / w));

                    //measurement of target size/location of symbol after being translated/scaled into the new SVG
                    symbolBounds = RectUtilities.toRectangle(0, 0, w * ratio, h * ratio);//.makeRect(0f, 0f, w * ratio, h * ratio);

                    //make sure border padding isn't excessive.
                    w = symbolBounds.getWidth();
                    h = symbolBounds.getHeight();

                    if(borderPadding > (h * 0.1))
                    {
                        borderPadding = (float)(h * 0.1);
                    }
                    else if(borderPadding > (w * 0.1))
                    {
                        borderPadding = (float)(w * 0.1);
                    }//*/

                }

                Rectangle2D borderPaddingBounds = null;
                int offset = 0;
                if(msi.getSymbolSet()==SymbolID.SymbolSet_ControlMeasure && outlineSymbol && borderPadding != 0)
                {
                    borderPaddingBounds = RectUtilities.toRectangle(0, 0, (rect.getWidth()+(borderPadding)) * ratio, (rect.getHeight()+(borderPadding)) * ratio);//.makeRect(0f, 0f, w * ratio, h * ratio);
                    symbolBounds = borderPaddingBounds;

                    //grow size SVG to accommodate the outline we added
                    offset = (int)borderPadding/2;//4;
                    RectUtilities.grow(rect, offset);

                }

                String strLineJoin = "";

                if(msi.getSymbolSet()==SymbolID.SymbolSet_ControlMeasure && msi.getDrawRule()==DrawRules.POINT1)//smooth out action points
                    strLineJoin = " stroke-linejoin=\"round\" ";

                StringBuilder sbGroupUnit = new StringBuilder();
                if(siIcon != null)
                {
                    sbGroupUnit.append("<g transform=\"translate(" + (rect.getX() * -ratio) + ',' + (rect.getY() * -ratio) + ") scale(" + ratio + "," + ratio + ")\"" + strLineJoin + ">");
                    sbGroupUnit.append(strSVGIcon);//(siIcon.getSVG());
                    sbGroupUnit.append("</g>");
                }

                //Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID, RectUtilities.makeRectangle2DFromRect(offset, offset, symbolBounds.getWidth()-offset, symbolBounds.getHeight()-offset));
                Point centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID, RectUtilities.makeRectangle2DFromRect(0, 0, symbolBounds.getWidth(), symbolBounds.getHeight()));

                if(borderPaddingBounds != null) {
                    RectUtilities.grow(symbolBounds, 4);
                }

                si = new SVGSymbolInfo(sbGroupUnit.toString(), centerPoint,symbolBounds,symbolBounds);

            }

            SVGSymbolInfo siNew = null;

            ////////////////////////////////////////////////////////////////////
            hasDisplayModifiers = ModifierRenderer.hasDisplayModifiers(symbolID, modifiers);
            hasTextModifiers = ModifierRenderer.hasTextModifiers(symbolID, modifiers);


            if(SymbolUtilities.isMultiPoint(symbolID))
            {
                hasTextModifiers = false;
                hasDisplayModifiers = false;
            }
            //process display modifiers
            if (asIcon == false && (hasTextModifiers || hasDisplayModifiers))
            {
                SymbolDimensionInfo sdiTemp = null;
                Color cLineColor = RendererUtilities.getColorFromHexString(lineColor);
                if (SymbolUtilities.isSPWithSpecialModifierLayout(symbolID))//(SymbolUtilitiesD.isTGSPWithSpecialModifierLayout(symbolID))
                {
                    sdiTemp = ModifierRenderer.ProcessTGSPWithSpecialModifierLayout(si, symbolID, modifiers, attributes, cLineColor,_fontRenderContext);
                }
                else
                {
                    sdiTemp = ModifierRenderer.ProcessTGSPModifiers(si, symbolID, modifiers, attributes, cLineColor, _fontRenderContext);
                }
                siNew = (sdiTemp instanceof SVGSymbolInfo ? (SVGSymbolInfo)sdiTemp : null);
            }

            if (siNew != null)
            {
                si = siNew;
            }
            siNew = null;


            //add SVG tag with dimensions
            //draw unit from SVG
            String svgAlpha = "";
            if(alpha >=0 && alpha <= 255)
                svgAlpha = " opacity=\"" + alpha/255f + "\"";
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + (int)si.getImageBounds().getWidth() + "\" height=\"" + (int)si.getImageBounds().getHeight() +"\" viewBox=\"" + 0 + " " + 0 + " " + (int)si.getImageBounds().getWidth() + " " + (int)si.getImageBounds().getHeight() + "\"" + svgAlpha + ">\n";
            String svgTranslateGroup = null;

            double transX = si.getImageBounds().getX() * -1;
            double transY = si.getImageBounds().getY() * -1;
            Point2D anchor = si.getSymbolCenterPoint();
            Rectangle2D imageBounds = si.getImageBounds();
            if(transX > 0 || transY > 0)
            {
                ShapeUtilities.offset(anchor,transX,transY);
                ShapeUtilities.offset(symbolBounds,transX,transY);
                ShapeUtilities.offset(imageBounds,transX,transY);
                svgTranslateGroup = "<g transform=\"translate(" + transX + "," + transY + ")" +"\">\n";
            }
            si = new SVGSymbolInfo(si.getSVG(),anchor,symbolBounds,imageBounds);
            StringBuilder sbSVG = new StringBuilder();
            sbSVG.append(svgStart);
            sbSVG.append(makeDescTag(si));
            sbSVG.append(makeMetadataTag(symbolID, si));
            if(svgTranslateGroup != null)
                sbSVG.append(svgTranslateGroup);
            sbSVG.append(si.getSVG());
            if(svgTranslateGroup != null)
                sbSVG.append("\n</g>");
            sbSVG.append("\n</svg>");
            si =  new SVGSymbolInfo(sbSVG.toString(),anchor,symbolBounds,imageBounds);


        }
        catch (Exception exc)
        {
            ErrorLogger.LogException("MilStdIconRenderer", "RenderSP", exc);
            return null;
        }
        return si;
    }



    private String makeDynamicSVG()
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        String svgStart = null;
        /*if(siIcon.getBbox().bottom > 400)
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
        else
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";*/

        pw.println("<svg width=\"35\" height=\"35\" style=\"fill:none;stroke-width:16\">");
//        pw.println("    <g id=\"txfm\" >");
//        pw.println("        <g id=\"scale\" >");
//        pw.println("            <g id=\"symbol\" fill=\"#000000\"> ");
//        pw.println("            </g>");
//        pw.println("        </g>");
//        pw.println("    </g>");
        pw.println("</svg>");

        pw.close();
        return sw.toString();
    }

    private String makeDescTag(SVGSymbolInfo si)
    {
        StringBuilder sbDesc = new StringBuilder();

        if(si != null)
        {
            Rectangle2D bounds = si.getSymbolBounds();
            Rectangle2D iBounds = si.getImageBounds();
            sbDesc.append("<desc>").append(si.getSymbolCenterX()).append(" ").append(si.getSymbolCenterY()).append(" ");
            sbDesc.append(bounds.getX()).append(" ").append(bounds.getY()).append(" ").append(bounds.getWidth()).append(" ").append(bounds.getHeight()).append(" ");
            sbDesc.append(iBounds.getX()).append(" ").append(iBounds.getY()).append(" ").append(iBounds.getWidth()).append(" ").append(iBounds.getHeight());
            sbDesc.append("</desc>\n");
        }
        return sbDesc.toString();
    }

    private String makeMetadataTag(String symbolID, SVGSymbolInfo si)
    {
        StringBuilder sbDesc = new StringBuilder();

        if(si != null)
        {
            Rectangle2D bounds = si.getSymbolBounds();
            Rectangle2D iBounds = si.getImageBounds();
            sbDesc.append("<metadata>\n");
            sbDesc.append("<symbolID>").append(symbolID).append("</symbolID>\n");
            sbDesc.append("<anchor>").append(si.getSymbolCenterX()).append(" ").append(si.getSymbolCenterY()).append("</anchor>\n");
            sbDesc.append("<symbolBounds>").append(bounds.getX()).append(" ").append(bounds.getY()).append(" ").append(bounds.getWidth()).append(" ").append(bounds.getHeight()).append("</symbolBounds>\n");
            sbDesc.append("<imageBounds>").append(iBounds.getX()).append(" ").append(iBounds.getY()).append(" ").append(iBounds.getWidth()).append(" ").append(iBounds.getHeight()).append("</imageBounds>\n");;
            sbDesc.append("</metadata>\n");
        }
        return sbDesc.toString();
    }

    private String getSVGString(String symbolID, boolean isOutline)
    {
        int version = SymbolID.getVersion(symbolID);
        SVGInfo svgi = SVGLookup.getInstance().getSVGLInfo(SymbolUtilities.getBasicSymbolID(symbolID), version);

        String strSVG = svgi.getSVG();
        if(isOutline)
            strSVG = strSVG.replaceFirst("<g id=\"" + SymbolUtilities.getBasicSymbolID(symbolID) + "\">","<g id=\"" + SymbolUtilities.getBasicSymbolID(symbolID) + "_outline\">");

        String svgStart = null;
        if(svgi.getBbox().getMaxY() > 400)
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 612 792\">";
        else
            svgStart = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 400 400\">";

        strSVG = svgStart + strSVG + "</svg>";

        return strSVG;

    }


    private static String makeCacheKey(String symbolID, String lineColor, String fillColor, int size, boolean keepUnitRatio, boolean drawOutline)
    {
        //String key = symbolID.substring(0, 20) + String.valueOf(lineColor) + String.valueOf(fillColor) + String.valueOf(size) + String.valueOf(keepUnitRatio);
        String key = symbolID.substring(0, 7) + symbolID.substring(10, 20) + SymbolID.getFrameShape(symbolID) + lineColor + fillColor + size + keepUnitRatio + drawOutline;
        return key;
    }

}
