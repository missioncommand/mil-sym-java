package armyc2.c5isr.renderer;


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Path2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;


import armyc2.c5isr.renderer.utilities.*;

import java.awt.geom.AffineTransform;

/**
 * This class is used for rendering the labels/amplifiers/modifiers around the single point symbol.
 */
public class ModifierRenderer implements SettingsEventListener
{

    private static ModifierRenderer _instance = null;
    private static String _className = "ModifierRenderer";
    private static RendererSettings RS = RendererSettings.getInstance();
    private static Font _modifierFont = RS.getLabelFont();

    private static float _modifierFontHeight = 11f;
    private static float _modifierFontDescent = 3f;


    private static BufferedImage _bmp = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        
        

    @Override
    public void SettingsEventChanged(String type)
    {
        if(type.equals(SettingsChangedEvent.EventType_FontChanged))
        {
            _modifierFont = RendererSettings.getInstance().getLabelFont();
            
            Graphics2D _g2d = _bmp.createGraphics();
            FontMetrics fm = _g2d.getFontMetrics(_modifierFont);

            _modifierFontHeight = fm.getHeight();// - fm.getMaxDescent();
            _modifierFontDescent = fm.getMaxDescent();

            fm = null;
            _g2d.dispose();
            _g2d = null;

        }
    }

    
    private ModifierRenderer()
    {

    }

    /**
     * Instance of the ModifierRenderer class
     * @return the instance
     */
    public static synchronized ModifierRenderer getInstance()
    {
        if(_instance == null)
            _instance = new ModifierRenderer();

        RendererSettings.getInstance().addEventListener(_instance);
        //onSettingsChanged(new SettingsChangedEvent(SettingsChangedEvent.EventType_FontChanged));
        _instance.SettingsEventChanged(SettingsChangedEvent.EventType_FontChanged);

        return _instance;
    }


    public static SymbolDimensionInfo processUnitDisplayModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        Font modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;
        SymbolDimensionInfo newsdi = null;
        Rectangle2D symbolBounds = (Rectangle2D) sdi.getSymbolBounds().clone();
        Rectangle2D imageBounds = sdi.getImageBounds();
        Point centerPoint = sdi.getSymbolCenterPoint();
        Point2D symbolCenter = new Point2D.Double(symbolBounds.getCenterX(), symbolBounds.getCenterY());
        TextInfo tiEchelon = null;
        TextInfo tiAM = null;
        Rectangle2D echelonBounds = null;
        Rectangle2D amBounds = null;
        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;
        float strokeWidth = 3.0f;
        float strokeWidthNL = 3.0f;
        Color lineColor = Color.BLACK;//SymbolUtilities.getLineColorOfAffiliation(symbolID);
        Color fillColor = SymbolUtilities.getFillColorOfAffiliation(symbolID);
        int buffer = 0;
        float alpha = -1;
        //ctx = null;
        int offsetX = 0;
        int offsetY = 0;
        int pixelSize = RendererSettings.getInstance().getDefaultPixelSize();

        int ss = SymbolID.getSymbolSet(symbolID);

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }
        if (attributes.containsKey(MilStdAttributes.TextColor))
        {
            textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
        }
        if (attributes.containsKey(MilStdAttributes.TextBackgroundColor))
        {
            textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
        }
        else
        {
            textBackgroundColor = RendererUtilities.getIdealOutlineColor(textColor);
        }
        if (attributes.containsKey(MilStdAttributes.LineColor))
        {
            lineColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.LineColor));
        }
        if (attributes.containsKey(MilStdAttributes.FillColor))
        {
            fillColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.FillColor));
        }
        if (attributes.containsKey(MilStdAttributes.PixelSize))
        {
            pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
        }

        int dpi = RendererSettings.getInstance().getDeviceDPI();
        strokeWidth = 1;//dpi/96f;//min DPI
        strokeWidth = Math.max(pixelSize / 25f,strokeWidth);//dpi base on symbol size
        strokeWidth = Math.min(strokeWidth,dpi/32f);//max dpi

        // <editor-fold defaultstate="collapsed" desc="Build Mobility Modifiers">
        Rectangle2D mobilityBounds = null;
        int ad = SymbolID.getAmplifierDescriptor(symbolID);//echelon/mobility
        List<Path2D> shapes = new ArrayList<Path2D>();
        Path2D mobilityPath = null;
        Path2D mobilityPathFill = null;
        if (ad >= SymbolID.Mobility_WheeledLimitedCrossCountry &&
                (SymbolUtilities.hasModifier(symbolID, Modifiers.R_MOBILITY_INDICATOR) ||
                        SymbolUtilities.hasModifier(symbolID, Modifiers.AG_AUX_EQUIP_INDICATOR)))
        {

            //Draw Mobility
            int fifth = (int) ((symbolBounds.getWidth() * 0.2) + 0.5f);
            int x = 0;
            int y = 0;
            int centerX = 0;
            int bottomY = 0;
            int height = 0;
            int width = 0;
            int middleY = 0;
            int wheelOffset = 2;
            int wheelSize = fifth;//10;
            int rrHeight = fifth;//10;
            int rrArcWidth = (int) ((fifth * 1.5) + 0.5f);//16;


            x = (int) symbolBounds.getX() + 1;
            y = (int) symbolBounds.getY();
            height = (int) (symbolBounds.getHeight());
            width = (int) Math.round(symbolBounds.getWidth()) - 3;
            bottomY = y + height + 3;



            if (ad >= SymbolID.Mobility_WheeledLimitedCrossCountry && ad < SymbolID.Mobility_ShortTowedArray &&//31, mobility starts above 30
                    SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.R_MOBILITY_INDICATOR))
            {
                mobilityPath = new Path2D.Double();

                //wheelSize = width / 7;
                //rrHeight = width / 7;
                //rrArcWidth = width / 7;
                if (ad == SymbolID.Mobility_WheeledLimitedCrossCountry)//MO
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_WheeledCrossCountry)//MP
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //center wheel
                    mobilityPath.append(new Ellipse2D.Double(x + (width/2)-(wheelSize/2), bottomY + wheelOffset, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Tracked)//MQ
                {
                    //round rectangle
                    mobilityPath.append(new RoundRectangle2D.Double(x, bottomY, width, rrHeight, rrArcWidth, rrHeight),false);

                }
                else if (ad == SymbolID.Mobility_Wheeled_Tracked)//MR
                {
                    //round rectangle
                    mobilityPath.append(new RoundRectangle2D.Double(x, bottomY, width, rrHeight, rrArcWidth, rrHeight),false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x - wheelSize - wheelSize, bottomY, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Towed)//MS
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x + wheelSize,bottomY + (wheelSize/2),x + width - wheelSize, bottomY + (wheelSize/2)), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY, wheelSize, wheelSize), false);
                }
                else if (ad == SymbolID.Mobility_Rail)//MT
                {
                    //line
                    mobilityPath.append(new Line2D.Double(x,bottomY,x + width, bottomY), false);
                    //left circle
                    mobilityPath.append(new Ellipse2D.Double(x + wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //left circle2
                    mobilityPath.append(new Ellipse2D.Double(x, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);
                    //right circle2
                    mobilityPath.append(new Ellipse2D.Double(x + width - wheelSize - wheelSize, bottomY + wheelOffset, wheelSize, wheelSize), false);

                }
                else if (ad == SymbolID.Mobility_OverSnow)//MU
                {
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.lineTo(x + 5, bottomY + 5);
                    mobilityPath.lineTo(x + width, bottomY + 5);
                }
                else if (ad == SymbolID.Mobility_Sled)//MV
                {
                    mobilityPath.moveTo(x, bottomY);
                    mobilityPath.curveTo(x, bottomY, x - rrHeight, bottomY + rrHeight/2, x, bottomY + rrHeight);
                    mobilityPath.lineTo(x + width, bottomY + rrHeight);
                    mobilityPath.curveTo(x + width, bottomY + rrHeight, x + width + rrHeight, bottomY + rrHeight/2, x + width, bottomY);
                }
                else if (ad == SymbolID.Mobility_PackAnimals)//MW
                {
                    centerX = (int)Math.round(symbolBounds.getCenterX());
                    int angleWidth = rrHeight / 2;
                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX - angleWidth, bottomY);
                    mobilityPath.lineTo(centerX - angleWidth*2, bottomY + rrHeight + 2);

                    mobilityPath.moveTo(centerX, bottomY + rrHeight + 2);
                    mobilityPath.lineTo(centerX + angleWidth, bottomY);
                    mobilityPath.lineTo(centerX + angleWidth*2, bottomY + rrHeight + 2);
                }
                else if (ad == SymbolID.Mobility_Barge)//MX
                {
                    centerX = (int)symbolBounds.getCenterX();
                    double quarterX = (centerX - x)/2;
                    double quarterY = (((bottomY + rrHeight) - bottomY)/2);
                    mobilityPath.moveTo(x+width, bottomY);
                    mobilityPath.lineTo(x, bottomY);
                    mobilityPath.curveTo(x+quarterX, bottomY+rrHeight, centerX + quarterX, bottomY + rrHeight, x + width, bottomY);
                }
                else if (ad == SymbolID.Mobility_Amphibious)//MY
                {
                    double incrementX = width / 7;
                    middleY = (((bottomY + rrHeight) - bottomY)/2);

                    mobilityPath.append(new Arc2D.Double(x, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*2, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*3, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*4, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*5, bottomY + middleY, incrementX, rrHeight, 0, -180, Arc2D.OPEN),false);
                    mobilityPath.append(new Arc2D.Double(x + incrementX*6, bottomY + middleY, incrementX, rrHeight, 0, 180, Arc2D.OPEN),false);
                }

            }
            //Draw Towed Array Sonar
            if ((ad == SymbolID.Mobility_ShortTowedArray || ad == SymbolID.Mobility_LongTowedArray) &&
                    SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AG_AUX_EQUIP_INDICATOR))
            {
                mobilityPath = new Path2D.Double();
                int boxHeight = (int) ((rrHeight * 0.5f) + 0.5f);
                if(boxHeight < 5)
                    strokeWidthNL = 1f;
                bottomY = y + height + (boxHeight / 7);
                mobilityPathFill = new Path2D.Double();
                offsetY = boxHeight / 7;//1;
                centerX = (int)symbolBounds.getCenterX();
                int squareOffset = Math.round(boxHeight * 0.5f);
                middleY = ((boxHeight / 2) + bottomY) + offsetY;//+1 for offset from symbol
                if (ad == SymbolID.Mobility_ShortTowedArray) {
                    //subtract 0.5 becase lines 1 pixel thick get aliased into
                    //a line two pixels wide.
                    //line
                    mobilityPath.append(new Line2D.Double(centerX,bottomY - 1, centerX, bottomY + offsetY + boxHeight + offsetY), false);
                    //PathUtilties.addLine(mobilityPath, centerX - 1, bottomY - 1, centerX - 1, bottomY + boxHeight + offsetY);

                    //line
                    mobilityPath.append(new Line2D.Double(x, middleY, x + width, middleY), false);
                    //PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);

                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(Math.round(centerX - squareOffset), bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square
                    mobilityPathFill.append(new Rectangle2D.Double(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                } else if (ad == SymbolID.Mobility_LongTowedArray) {
                    int leftX = x + (centerX - x) / 2,
                            rightX = centerX + (x + width - centerX) / 2;

                    //line vertical left
                    mobilityPath.append(new Line2D.Double(leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY), false);
                    //PathUtilties.addLine(mobilityPath, leftX, bottomY - 1, leftX, bottomY + offsetY + boxHeight + offsetY);

                    //line vertical right
                    mobilityPath.append(new Line2D.Double(rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY), false);
                    //PathUtilties.addLine(mobilityPath, rightX, bottomY - 1, rightX, bottomY + offsetY + boxHeight + offsetY);

                    //line horizontal
                    mobilityPath.append(new Line2D.Double(x, middleY, x + width, middleY), false);
                    //PathUtilties.addLine(mobilityPath, x, middleY, x + width, middleY);

                    //square left
                    mobilityPathFill.append(new Rectangle2D.Double(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square middle
                    mobilityPathFill.append(new Rectangle2D.Double(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(centerX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square right
                    mobilityPathFill.append(new Rectangle2D.Double(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(x + width - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square middle left
                    mobilityPathFill.append(new Rectangle2D.Double(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(leftX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);

                    //square middle right
                    mobilityPathFill.append(new Rectangle2D.Double(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), false);
                    //mobilityPathFill.addRect(PathUtilties.makeRectF(rightX - squareOffset, bottomY + offsetY, boxHeight, boxHeight), Direction.CW);
                }
            }

            //get mobility bounds
            if (mobilityPath != null)
            {

                //build mobility bounds
                mobilityBounds = mobilityPath.getBounds2D();
                RectUtilities.shiftBR(mobilityBounds,0,1);

                if (mobilityPathFill != null)
                {
                    Rectangle2D mobilityFillBounds = mobilityPathFill.getBounds2D();
                    mobilityBounds = mobilityBounds.createUnion(mobilityFillBounds);
                }

                //grow bounds to handle strokeWidth
                if(ad == SymbolID.Mobility_ShortTowedArray || ad == SymbolID.Mobility_LongTowedArray)
                    ShapeUtilities.grow(mobilityBounds, (int)Math.ceil((strokeWidthNL/2)));
                else
                    ShapeUtilities.grow(mobilityBounds, (int)Math.ceil((strokeWidth/2)));

                imageBounds = imageBounds.createUnion(mobilityBounds);

            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Leadership Indicator Modifier">
        Rectangle2D liBounds = null;
        Path2D liPath = null;
        Point2D liTop = null;
        Point2D liLeft = null;
        Point2D liRight = null;
        if(ad == SymbolID.Leadership_Individual && ss == SymbolID.SymbolSet_DismountedIndividuals &&
                (SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_DismountedIndividuals ||
                        SymbolID.getFrameShape(symbolID)==SymbolID.FrameShape_Unknown))
        {
            liPath = new Path2D.Double();

            int si = SymbolID.getStandardIdentity(symbolID);
            int af = SymbolID.getAffiliation(symbolID);
            int c = SymbolID.getContext(symbolID);
            //int fs = SymbolID.getFrameShape(symbolID);
            double centerOffset = 0;
            double sideOffset = 0;
            double left = symbolBounds.getX();
            double right = symbolBounds.getX() + symbolBounds.getWidth();

            if(af == SymbolID.StandardIdentity_Affiliation_Unknown || af == SymbolID.StandardIdentity_Affiliation_Pending)
            {
                centerOffset = (symbolBounds.getHeight()*0.1012528735632184);
                sideOffset = (right - left)*0.3583513488109785;
                //left = symbolBounds.getCenterX() - ((symbolBounds.getWidth() / 2) * 0.66420458);
                //right = symbolBounds.getCenterX() + ((symbolBounds.getWidth() / 2) * 0.66420458);
            }
            if(af == SymbolID.StandardIdentity_Affiliation_Neutral)
            {
                centerOffset = (symbolBounds.getHeight()*0.25378787878787878);
                sideOffset = (right - left)*0.2051402812352822;
            }
            if(SymbolUtilities.isReality(symbolID) || SymbolUtilities.isSimulation(symbolID))
            {
                if(af==SymbolID.StandardIdentity_Affiliation_Friend || af==SymbolID.StandardIdentity_Affiliation_AssumedFriend)
                {//hexagon friend/assumed friend
                    centerOffset = (symbolBounds.getHeight()*0.08);
                    sideOffset = (right - left)*0.282714524168219;//(symbolBounds.getHeight()*0.29);
                }
                else if(af==SymbolID.StandardIdentity_Affiliation_Hostile_Faker || af==SymbolID.StandardIdentity_Affiliation_Suspect_Joker)
                {//diamond hostile/suspect
                    left = symbolBounds.getCenterX() - ((symbolBounds.getWidth() / 2) * 1.0653694149);//1.07);//1.0653694149);
                    right = symbolBounds.getCenterX() + ((symbolBounds.getWidth() / 2) * 1.0653694149);//1.07);//1.0653694149);

                    centerOffset = (symbolBounds.getHeight()*0.08);//0.0751139601139601
                    sideOffset = (right - left)*0.4923255424955992;
                }
            }
            else//Exercise
            {
                //hexagon
                if(af!=SymbolID.StandardIdentity_Affiliation_Unknown ||
                        af==SymbolID.StandardIdentity_Affiliation_Neutral)
                {
                    centerOffset = (symbolBounds.getHeight()*0.08);
                    sideOffset = (right - left)*0.282714524168219;
                }
            }

            //create leadership indicator /\
            liTop = new Point2D.Double(symbolBounds.getCenterX(), symbolBounds.getY() - centerOffset);
            liLeft = new Point2D.Double(left, liTop.getY() + sideOffset);
            liRight = new Point2D.Double(right, liTop.getY() + sideOffset);




            //liPath.append(new Line2D.Double(liLeft.getX(),liLeft.getY(), liTop.getX(), liTop.getY()), false);
            //liPath.append(new Line2D.Double(liTop.getX(), liTop.getY(), liRight.getX(), liRight.getY()), false);

            liPath.moveTo(liTop.getX(), liTop.getY());
            liPath.lineTo(liLeft.getX(), liLeft.getY());
            liPath.moveTo(liTop.getX(), liTop.getY());
            liPath.lineTo(liRight.getX(), liRight.getY());

            liBounds = liPath.getBounds2D();
            liBounds = new Rectangle2D.Double(liLeft.getX(), liTop.getY(), liRight.getX() - liLeft.getX(), liLeft.getY() - liTop.getY());

            RectUtilities.grow(liBounds,2);

            imageBounds = imageBounds.createUnion(liBounds);
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Echelon">
        //Draw Echelon
        int intEchelon = SymbolID.getAmplifierDescriptor(symbolID);// SymbolUtilitiesD.getEchelon(symbolID);//symbolID.substring(11, 12);
        String strEchelon = null;
        if (intEchelon > 10 && intEchelon < 29 && SymbolUtilities.hasModifier(symbolID, Modifiers.B_ECHELON))
        {
            strEchelon = SymbolUtilities.getEchelonText(intEchelon);
        }
        if (strEchelon != null && SymbolUtilities.isInstallation(symbolID) == false
                && SymbolUtilities.hasModifier(symbolID, Modifiers.B_ECHELON))
        {

            int echelonOffset = 2,
                    outlineOffset = RS.getTextOutlineWidth();
            //Font modifierFont = RS.getLabelFont();
            tiEchelon = new TextInfo(strEchelon, 0, 0, modifierFont,frc);
            echelonBounds = tiEchelon.getTextBounds();

            int y = (int)Math.round(symbolBounds.getY() - echelonOffset);
            int x = (int)(Math.round(symbolBounds.getX()) + (symbolBounds.getWidth() / 2) - (echelonBounds.getWidth() / 2));
            tiEchelon.setLocation(x, y);

            //There will never be lowercase characters in an echelon so trim that fat.
            //Remove the descent from the bounding box.
            //needed?
            //tiEchelon.getTextOutlineBounds();//.shiftBR(0,Math.round(-(echelonbounds.getHeight()*0.3)));

            //make echelon bounds a little more spacious for things like nearby labels and Task Force.
            ShapeUtilities.grow(echelonBounds, outlineOffset);
            //tiEchelon.getTextOutlineBounds();
//                RectUtilities.shift(echelonBounds, x, -outlineOffset);
            //echelonBounds.shift(0,-outlineOffset);// - Math.round(echelonOffset/2));
            tiEchelon.setLocation(x, y - outlineOffset);

            imageBounds = imageBounds.createUnion(echelonBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Task Force">
        Rectangle2D tfBounds = null;
        Rectangle2D tfRectangle = null;
        int hqtfd = SymbolID.getHQTFD(symbolID);
        if (SymbolUtilities.hasModifier(symbolID, Modifiers.D_TASK_FORCE_INDICATOR) && SymbolUtilities.isTaskForce(symbolID))
        {
            int height = (int)Math.round(symbolBounds.getHeight() / 4);
            int width = (int)Math.round(symbolBounds.getWidth() / 3);

            if(!SymbolUtilities.hasRectangleFrame(symbolID))
            {
                height = (int)Math.round(symbolBounds.getHeight() / 6);
            }

            tfRectangle = new Rectangle2D.Double((int) (symbolBounds.getX() + width),
                    (int)(symbolBounds.getY() - height),
                    width,
                    height);

            tfBounds = new Rectangle2D.Double((int)(tfRectangle.getX() - 1),
                    (int)(tfRectangle.getY() - 1),
                    (int)(tfRectangle.getWidth() + 2),
                    (int)(tfRectangle.getHeight() + 2));

            if (echelonBounds != null)
            {
                double tfx = tfRectangle.getX();
                double tfw = tfRectangle.getWidth();
                double tfy = tfRectangle.getY();
                double tfh = tfRectangle.getHeight();

                if(echelonBounds.getWidth() > tfRectangle.getWidth())
                {
                    tfx = symbolBounds.getX() + symbolBounds.getWidth()/2 - (echelonBounds.getWidth()/2) - 1;
                    tfw = echelonBounds.getWidth()+2;
                }
                if(echelonBounds.getHeight() > tfRectangle.getHeight())
                {
                    tfy = echelonBounds.getY()-1;
                    tfh = echelonBounds.getHeight()+2;

                }
                tfRectangle = new Rectangle2D.Double(tfx,
                        tfy,// + outlineOffset,
                        tfw,
                        tfh);


                tfBounds = new Rectangle2D.Double((int)(tfRectangle.getX() - 1),
                        (int)(tfRectangle.getY() - 1),
                        (int)(tfRectangle.getWidth() + 2),
                        (int)(tfRectangle.getHeight() + 2));

            }
            imageBounds = imageBounds.createUnion(tfBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">
        Rectangle2D fdiBounds = null;
        Point2D fdiTop = null;
        Point2D fdiLeft = null;
        Point2D fdiRight = null;

        if (SymbolUtilities.hasFDI(symbolID)
                && SymbolUtilities.hasModifier(symbolID, Modifiers.AB_FEINT_DUMMY_INDICATOR))
        {
            //create feint indicator /\
            fdiLeft = new Point2D.Double(symbolBounds.getX(), symbolBounds.getY());
            fdiRight = new Point2D.Double((symbolBounds.getX() + symbolBounds.getWidth()), symbolBounds.getY());
            fdiTop = new Point2D.Double(Math.round(symbolBounds.getCenterX()), Math.round(symbolBounds.getY() - (symbolBounds.getWidth() * .5f)));


            fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());

            if (echelonBounds != null)
            {
                int shiftY = (int)Math.round(symbolBounds.getY() - echelonBounds.getHeight() - 2);
                fdiLeft.setLocation(fdiLeft.getX(), fdiLeft.getY() + shiftY);
                //fdiLeft.offset(0, shiftY);
                fdiTop.setLocation(fdiTop.getX(), fdiTop.getY() + shiftY);
                //fdiTop.offset(0, shiftY);
                fdiRight.setLocation(fdiRight.getX(), fdiRight.getY() + shiftY);
                //fdiRight.offset(0, shiftY);
                fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());
                //fdiBounds.offset(0, shiftY);
            }

            imageBounds = imageBounds.createUnion(fdiBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Engagement Bar (AO)">
        //A:BBB-CC
        String strAO = null;
        Rectangle2D ebRectangle = null;
        Rectangle2D ebBounds = null;
        Rectangle2D ebTextBounds = null;
        TextInfo tiAO = null;
        int ebTop = 0;
        int ebLeft = 0;
        int ebWidth = 0;
        int ebHeight = 0;
        Color ebColor = null;//SymbolUtilities.getFillColorOfAffiliation(symbolID);

        if(attributes.containsKey(MilStdAttributes.EngagementBarColor))
            ebColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.EngagementBarColor));
        else
            ebColor = fillColor;

        if(SymbolUtilities.hasModifier(symbolID, Modifiers.AO_ENGAGEMENT_BAR) &&
                modifiers.containsKey(Modifiers.AO_ENGAGEMENT_BAR))
            strAO = modifiers.get(Modifiers.AO_ENGAGEMENT_BAR);
        if(strAO != null)
        {
            tiAO = new TextInfo(strAO, 0, 0, modifierFont,frc);
            ebTextBounds = tiAO.getTextBounds();
            ebHeight = (int)ebTextBounds.getHeight() + 4;

            int barOffset = Math.max(RendererSettings.getInstance().getDeviceDPI()/32, 4);

            if(fdiBounds != null)//set bar above FDI if present
            {
                ebTop = (int)fdiBounds.getY() - ebHeight - barOffset;
            }
            else if(tfBounds != null)//set bar above TF if present
            {
                ebTop = (int)tfBounds.getY() - ebHeight - barOffset;
            }
            else if(echelonBounds != null)//set bar above echelon if present
            {
                ebTop = (int)echelonBounds.getY() - ebHeight - barOffset;
            }
            else if((isCOnTop(symbolID) && modifiers.containsKey(Modifiers.C_QUANTITY)) ||
                    SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Exercise ||
                    SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Simulation)
            {
                ebTop = (int)symbolBounds.getY() - (int)(ebHeight*2.4);
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                ebTop = (int)symbolBounds.getY() - ebHeight - barOffset;
            }
            else//position above symbol
            {
                ebTop = (int)symbolBounds.getY() - ebHeight - barOffset;
            }

            //if text wider than symbol, extend the bar.
            if(ebTextBounds.getWidth() + 4 > symbolBounds.getWidth())
            {
                ebWidth = (int)ebTextBounds.getWidth() + 4;
                ebLeft = (int)symbolCenter.getX() - (ebWidth/2);
            }
            else
            {
                ebLeft = (int)symbolBounds.getX()+1;// - 2;//leave room for outline
                ebWidth = (int)symbolBounds.getWidth()-2;// + 4;//leave room for outline
            }

            //set text location within the bar
            tiAO.setLocation((int)(symbolCenter.getX() - (ebTextBounds.getWidth()/2)), (int)(ebTop + ebHeight - 2 - ((ebHeight - ebTextBounds.getHeight()) / 2)));

            ebRectangle = new Rectangle2D.Double(ebLeft,ebTop,ebWidth,ebHeight);
            ebBounds = RectUtilities.copyRect(ebRectangle);
            RectUtilities.grow(ebBounds,1);

            imageBounds = imageBounds.createUnion(ebBounds);
        }


        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Affiliation Modifier">
        //Draw Echelon
        //not needed for 2525D because built into the SVG files.
        String affiliationModifier = null;

        if (RS.getDrawAffiliationModifierAsLabel() == false)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {

            int amOffset = 2;
            int outlineOffset = RS.getTextOutlineWidth();

            tiAM = new TextInfo(affiliationModifier, 0, 0, RS.getLabelFont(), frc);
            amBounds = tiAM.getTextBounds();

            int x, y;

            if (echelonBounds != null
                    && ((echelonBounds.getMinX() + echelonBounds.getWidth() > symbolBounds.getMinX() + symbolBounds.getWidth())))
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(echelonBounds.getMinX() + echelonBounds.getWidth() + amOffset);
            }
            /*if (ebBounds != null
                    && ((ebBounds.getMinX() + ebBounds.getWidth() > symbolBounds.getMinX() + symbolBounds.getWidth())))
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(ebBounds.getMinX() + ebBounds.getWidth() + amOffset + RendererSettings.getInstance().getTextOutlineWidth());
            }//*/
            else
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(Math.round(symbolBounds.getMinX() + symbolBounds.getWidth() + amOffset + RendererSettings.getInstance().getTextOutlineWidth()));
            }
            tiAM.setLocation(x, y);

            //adjust for outline.
            ShapeUtilities.grow(amBounds, outlineOffset);
            ShapeUtilities.offset(amBounds, 0, -outlineOffset);
            tiAM.setLocation(x, y - outlineOffset);

            imageBounds = imageBounds.createUnion(amBounds);
        }//*/
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build HQ Staff">
        Point2D pt1HQ = null;
        Point2D pt2HQ = null;
        Rectangle2D hqBounds = null;
        //Draw HQ Staff
        if (SymbolUtilities.isHQ(symbolID))
        {

            int affiliation = SymbolID.getAffiliation(symbolID);
            int context = SymbolID.getContext(symbolID);
            //get points for the HQ staff
            if (SymbolUtilities.hasRectangleFrame(symbolID))
            {
                pt1HQ = new Point2D.Double(symbolBounds.getX() + 1,
                        (symbolBounds.getY() + symbolBounds.getHeight()));
            }
            else
            {
                pt1HQ = new Point2D.Double((int) symbolBounds.getX() + 1,
                        (int) (symbolBounds.getY() + (symbolBounds.getHeight() / 2)));
            }
            pt2HQ = new Point2D.Double(pt1HQ.getX(), (pt1HQ.getY() + symbolBounds.getHeight()));

            //create bounding rectangle for HQ staff.
            hqBounds = new Rectangle2D.Double(pt1HQ.getX(), pt1HQ.getY(), 2, pt2HQ.getY() - pt1HQ.getY());
            //adjust the image bounds accordingly.
            imageBounds = imageBounds.createUnion(new Rectangle2D.Double(pt1HQ.getX(), pt1HQ.getY(), pt2HQ.getX() - pt1HQ.getX(), pt2HQ.getY() - pt1HQ.getY()));
            //RectUtilities.shiftBR(imageBounds, 0, (int) (pt2HQ.y - imageBounds.bottom));
            //imageBounds.shiftBR(0,pt2HQ.y-imageBounds.bottom);
            //adjust symbol center
            centerPoint.setLocation(pt2HQ.getX(), pt2HQ.getY());
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build DOM Arrow">
        Point2D[] domPoints = null;
        Rectangle2D domBounds = null;
        if (SymbolUtilities.hasModifier(symbolID, Modifiers.Q_DIRECTION_OF_MOVEMENT) &&
                modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT))
        {
            String strQ = modifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT);

            if(strQ != null && SymbolUtilities.isNumber(strQ))
            {
                float q = Float.valueOf(strQ);

                boolean isY = (modifiers.containsKey(Modifiers.Y_LOCATION));

                domPoints = createDOMArrowPoints(symbolID, symbolBounds, centerPoint, q, isY,modifierFontHeight);

                domBounds = new Rectangle2D.Double(domPoints[0].getX(), domPoints[0].getY(), 1, 1);

                Point2D temp = null;
                for (int i = 1; i < 6; i++)
                {
                    temp = domPoints[i];
                    if (temp != null)
                    {
                        domBounds = domBounds.createUnion(new Rectangle2D.Double(temp.getX(), temp.getY(),1,1));
                    }
                }
                imageBounds = imageBounds.createUnion(domBounds);
            }
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Operational Condition Indicator">
        Rectangle2D ociBounds = null;
        Rectangle2D ociShape = null;
        Path2D ociSlashShape = null;
        int ociOffset = Math.max(RendererSettings.getInstance().getDeviceDPI()/32, 4);
        if (SymbolUtilities.hasModifier(symbolID, Modifiers.AL_OPERATIONAL_CONDITION)) {
            if (mobilityBounds != null)
            {
                ociOffset = (int)Math.round((mobilityBounds.getY() + mobilityBounds.getHeight()) - (symbolBounds.getY() + symbolBounds.getHeight())) + 4;
            }
            if(RendererSettings.getInstance().getOperationalConditionModifierType() == RendererSettings.OperationalConditionModifierType_BAR) {
                ociShape = processOperationalConditionIndicator(symbolID, symbolBounds, ociOffset);
                if (ociShape != null) {
                    Rectangle2D temp = (Rectangle2D) ociShape.clone();
                    ShapeUtilities.grow(temp, 2);
                    ociBounds = temp;
                    imageBounds = imageBounds.createUnion(ociBounds);
                }
            }
            else//slash
            {
                ociSlashShape = processOperationalConditionIndicatorSlash(symbolID, symbolBounds);
                if (ociSlashShape != null)
                {
                    //build mobility bounds
                    ociBounds = ociSlashShape.getBounds2D();
                    imageBounds = imageBounds.createUnion(ociBounds);
                }
            }
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Restricted Indicator">
        Rectangle2D rBounds = null;
        Path2D rPath = null;
        Path2D rPath2 = null;
        Ellipse2D rCircle = null;
        float rStrokeWidth = 3;
        if(SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_Restricted_Target_Reality)
        {
            // <path id="primary" d="M380,320l38,-67l40,67h-78m38,-11v-1m0,-10l0,-20" fill="yellow" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="7" />
            float nsTx = 0;
            float nsTy = 0;
            double ratio = 1;
            SVGInfo si = SVGLookup.getInstance().getSVGLInfo(SVGLookup.getFrameID(symbolID),SymbolID.getVersion(symbolID));
            if(symbolBounds.getHeight() > symbolBounds.getWidth())
            {
                double sHeight = si.getBbox().getHeight();
                ratio = symbolBounds.getHeight() / sHeight;
            }
            else
            {
                double sWidth = si.getBbox().getWidth();
                ratio = symbolBounds.getHeight() / sWidth;
            }

            nsTx = (float)(si.getBbox().getX() * ratio) * -1;
            nsTy = (float)(si.getBbox().getY() * ratio) * -1;

            float radius = 36 * (float)ratio;

            //<path d="m373,313l53,-97l57,97l-110,0" fill="yellow" id="triangle" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="6"/>
            //<path d="m373,313L426,216L483,313L373,313" fill="yellow" id="triangle" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="6"/>
            rPath = new Path2D.Float();//triangle
            rPath.moveTo(373 * ratio, 313 * ratio);
            rPath.lineTo(426 * ratio, 216 * ratio);
            rPath.lineTo(483 * ratio, 313 * ratio);
            rPath.lineTo(373 * ratio, 313 * ratio);

            //<path d="M426.5,276L426.5,244" fill="none" id="line" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="10"/>
            rPath2 = new Path2D.Float();//line
            rPath2.moveTo(426.5 * ratio, 276 * ratio);
            rPath2.lineTo(426.5 * ratio, 248 * ratio);

            //<circle cx="426.5" cy="293" r="6" id="dot"/>
            rCircle = new Ellipse2D.Float(423.5f * (float)ratio, 290 * (float)ratio, 6 * (float)ratio, 6 * (float)ratio);

            //need to shift like we do the frame and main icons since it's based in that space
            AffineTransform txfm = AffineTransform.getTranslateInstance(nsTx,nsTy);
            rPath = (Path2D) txfm.createTransformedShape(rPath);
            rPath2 = (Path2D)txfm.createTransformedShape(rPath2);
            rCircle = new Ellipse2D.Float((float)rCircle.getX() + nsTx,(float)rCircle.getY() + nsTy,(float)rCircle.getWidth(),(float)rCircle.getHeight());
            //rCircle = (Ellipse2D) txfm.createTransformedShape(rCircle);


            Rectangle bounds = rPath.getBounds();//triangle bounds
            rBounds = RectUtilities.toRectangle2D(bounds.getX(),bounds.getY(),bounds.getWidth(), bounds.getHeight());
            rStrokeWidth = (2/66.666667f) * ((float)symbolBounds.getHeight() / SymbolUtilities.getUnitRatioHeight(symbolID));
            RectUtilities.grow(rBounds,(int)Math.ceil(rStrokeWidth/2));
            imageBounds = imageBounds.createUnion(rBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build No Strike Indicator">
        Rectangle2D nsBounds = null;
        Ellipse2D nsCircle = null;
        Line2D nsLine = null;
        double nsStrokeWidth = 3;
        if(SymbolID.getContext(symbolID) == SymbolID.StandardIdentity_Context_No_Strike_Entity_Reality)
        {
            //octagon~182.08058166503906~272.0794677734375~245.8407440185547~244.85235595703125
            //restricted~375.44801678047673~248.63298320770264~85.1039714496415~79.36734275822477
            //no-strike~378.0~248.0~80.0~80.0
            //<circle cx="418" cy="288" fill="yellow" r="36" stroke="black" stroke-width="8"/>
            //<line fill="none" stroke="black" stroke-linecap="round" stroke-linejoin="round" stroke-width="8" x1="390" x2="446" y1="265" y2="310"/>
            //nsCircle = new Ellipse(x,y,radius * 2, radius * 2);
            //nsLine = new Line(390 * ratio, 265 * ratio, 446 * ratio, 310 * ratio);
            double nsTx = 0;
            double nsTy = 0;
            double ratio = 1;
            SVGInfo si = SVGLookup.getInstance().getSVGLInfo(SVGLookup.getFrameID(symbolID),SymbolID.getVersion(symbolID));
            if(symbolBounds.getHeight() > symbolBounds.getWidth())
            {
                double sHeight = si.getBbox().getHeight();
                ratio = symbolBounds.getHeight() / sHeight;
            }
            else
            {
                double sWidth = si.getBbox().getWidth();
                ratio = symbolBounds.getWidth() / sWidth;
            }

            nsTx = (si.getBbox().getX() * ratio) * -1;
            nsTy = (si.getBbox().getY() * ratio) * -1;

            double radius = 50 * ratio;
            double x = 426 * ratio - radius;
            double y = 267 * ratio - radius;
            nsCircle = new Ellipse2D.Double(x,y,radius * 2, radius * 2);
            nsLine = new Line2D.Double(390 * ratio, 235 * ratio, 463 * ratio, 298 * ratio);

            //need to shift like we do the frame and main icons since it's based in that space
            //AffineTransform txfm = AffineTransform.getTranslateInstance(nsTx,nsTy);
            //nsCircle = (Ellipse2D) txfm.createTransformedShape(nsCircle);
            nsCircle = new Ellipse2D.Double(nsCircle.getX() + nsTx,nsCircle.getY() + nsTy,nsCircle.getWidth(),nsCircle.getHeight());
            //nsLine = (Line2D) txfm.createTransformedShape(nsLine);
            nsLine = new Line2D.Double(nsLine.getX1() + nsTx,nsLine.getY1() + nsTy,nsLine.getX2() + nsTx,nsLine.getY2() + nsTy);

            Rectangle bounds = nsCircle.getBounds();
            bounds.union(nsLine.getBounds());
            nsBounds = RectUtilities.toRectangle2D(bounds.getX(),bounds.getY(),bounds.getWidth(), bounds.getHeight());
            nsStrokeWidth = (2/66.666667) * (symbolBounds.getHeight() / SymbolUtilities.getUnitRatioHeight(symbolID));
            RectUtilities.grow(nsBounds,(int)Math.ceil(nsStrokeWidth/2));
            imageBounds = imageBounds.createUnion(nsBounds);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Shift Modifiers">
        //adjust points if necessary
        if (sdi instanceof ImageInfo && (imageBounds.getX() < 0 || imageBounds.getY() < 0))
        {
            int shiftX = (int)Math.abs(imageBounds.getX());
            int shiftY = (int)Math.abs(imageBounds.getY());

            if (hqBounds != null)
            {
                ShapeUtilities.offset(pt1HQ, shiftX, shiftY);
                ShapeUtilities.offset(pt2HQ, shiftX, shiftY);
            }
            if (echelonBounds != null)
            {
                tiEchelon.setLocation((int)tiEchelon.getLocation().getX() + shiftX, (int)tiEchelon.getLocation().getY() + shiftY);
            }
            if (amBounds != null)
            {
                tiAM.setLocation((int)tiAM.getLocation().getX() + shiftX, (int)tiAM.getLocation().getY() + shiftY);
            }
            if (tfBounds != null)
            {
                ShapeUtilities.offset(tfRectangle, shiftX, shiftY);
                ShapeUtilities.offset(tfBounds, shiftX, shiftY);
            }
            if(ebBounds != null)
            {
                ShapeUtilities.offset(ebRectangle,shiftX,shiftY);
                ShapeUtilities.offset(ebBounds,shiftX,shiftY);
                //tiEchelon.setLocation((int)tiEchelon.getLocation().getX() + shiftX, (int)tiEchelon.getLocation().getY() + shiftY);
                tiAO.shift(shiftX, shiftY);
                ShapeUtilities.offset(ebTextBounds,shiftX,shiftY);

            }
            if (fdiBounds != null)
            {
                ShapeUtilities.offset(fdiBounds, shiftX, shiftY);
                ShapeUtilities.offset(fdiLeft, shiftX, shiftY);
                ShapeUtilities.offset(fdiTop, shiftX, shiftY);
                ShapeUtilities.offset(fdiRight, shiftX, shiftY);
            }
            if (liBounds != null)
            {
                ShapeUtilities.offset(liBounds, shiftX, shiftY);
                ShapeUtilities.offset(liLeft, shiftX, shiftY);
                ShapeUtilities.offset(liTop, shiftX, shiftY);
                ShapeUtilities.offset(liRight, shiftX, shiftY);
                if(liPath != null)
                {
                    ShapeUtilities.offset(liPath,shiftX,shiftY);
                }
            }
            if (ociBounds != null)
            {
                ShapeUtilities.offset(ociBounds, shiftX, shiftY);
                if(ociShape != null)
                    ShapeUtilities.offset(ociShape, shiftX, shiftY);
                if(ociSlashShape != null)
                    ShapeUtilities.offset(ociSlashShape, shiftX, shiftY);
            }
            if(rBounds != null)
            {
                ShapeUtilities.offset(rBounds, shiftX, shiftY);
                ShapeUtilities.offset(rPath, shiftX, shiftY);//triangle
                ShapeUtilities.offset(rPath2, shiftX, shiftY);//exclamation
                rCircle = ShapeUtilities.offset(rCircle, shiftX, shiftY);//dot
            }
            if(nsBounds != null)
            {
                ShapeUtilities.offset(nsBounds, shiftX, shiftY);
                nsCircle = ShapeUtilities.offset(nsCircle, shiftX, shiftY);//circle
                nsLine = ShapeUtilities.offset(nsLine, shiftX, shiftY);//line
            }
            if (domBounds != null)
            {
                for (int i = 0; i < 6; i++)
                {
                    Point2D temp = domPoints[i];
                    if (temp != null)
                    {
                        ShapeUtilities.offset(temp, shiftX, shiftY);
                    }
                }
                ShapeUtilities.offset(domBounds, shiftX, shiftY);
            }
            if (mobilityBounds != null)
            {
                //shift mobility points
                ShapeUtilities.offset(mobilityPath, shiftX, shiftY);
                if (mobilityPathFill != null)
                {
                    ShapeUtilities.offset(mobilityPathFill, shiftX, shiftY);
                }
                ShapeUtilities.offset(mobilityBounds, shiftX, shiftY);
            }

            ShapeUtilities.offset(centerPoint, shiftX, shiftY);
            ShapeUtilities.offset(symbolBounds, shiftX, shiftY);
            ShapeUtilities.offset(imageBounds, shiftX, shiftY);
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Convert to SVG (SVGSymbolInfo)">
        if(sdi instanceof SVGSymbolInfo)
        {
            StringBuilder sbSVG = new StringBuilder();
            Path2D temp = null;
            String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
            String svgFill = RendererUtilities.colorToHexString(fillColor,false);
            String svgTextColor = RendererUtilities.colorToHexString(textColor,false);
            String svgTextBGColor = RendererUtilities.colorToHexString(textBackgroundColor,false);
            String svgStrokeWidth = String.valueOf(strokeWidth);
            String svgTextOutlineWidth = String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
            String svgAlpha = null;
            if(alpha >= 0 && alpha <= 1)
                svgAlpha = String.valueOf(alpha);
            String svgDashArray = null;

            if(hqBounds != null)
            {
                Line2D hqStaff = new Line2D.Double(pt1HQ,pt2HQ);
                temp = new Path2D.Double();
                temp.append(hqStaff,false);
                sbSVG.append(Shape2SVG.Convert(temp, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
            }
            if (echelonBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tiEchelon, svgTextBGColor, svgTextColor, svgTextOutlineWidth, svgAlpha, svgAlpha, null));
            }
            if (amBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tiAM, svgTextBGColor, svgTextColor, svgTextOutlineWidth, svgAlpha, svgAlpha, null));
            }
            if (tfBounds != null)
            {
                sbSVG.append(Shape2SVG.Convert(tfRectangle, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
            }
            if(ebBounds != null)
            {
                String svgEBFill = RendererUtilities.colorToHexString(ebColor,false);
                //create fill and outline
                sbSVG.append(Shape2SVG.Convert(ebRectangle, svgStroke, svgEBFill, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
                //create internal text
                sbSVG.append(Shape2SVG.Convert(tiAO, null, "#000000", null, svgAlpha, svgAlpha, null));
            }
            if (fdiBounds != null)
            {
                String svgFDIDashArray = "6 4";
                float[] dashArray = {6f,4f};

                if (symbolBounds.getHeight() < 20)
                {
                    svgFDIDashArray = "5 3";
                }

                /// ///////////////////////////////////
                //Divide line in 14 parts. line is 3 parts to 2 parts gap
                float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                //distance = distance / 14f;
                dashArray[1] = (int)((distance / 14f) * 2);
                dashArray[0] = (int)((distance / 14f) * 3);//*/
                svgFDIDashArray = "" + dashArray[0] + " " + dashArray[1];
                /// //////////////////////////////////

                Path2D fdiPath = new Path2D.Double();
                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray,"round"));

            }
            if (liBounds != null)
            {
                int liStrokeWidth = 2;
                if(pixelSize < 100)
                    liStrokeWidth=1;
                sbSVG.append(Shape2SVG.Convert(liPath, svgStroke, null, String.valueOf(liStrokeWidth), svgAlpha, svgAlpha, null,null));
            }
            if (ociBounds != null && ociShape != null)
            {

                int status = SymbolID.getStatus(symbolID);
                Color statusColor = null;

                switch (status) {
                    //Fully Capable
                    case SymbolID.Status_Present_FullyCapable:
                        statusColor = Color.green;
                        break;
                    //Damaged
                    case SymbolID.Status_Present_Damaged:
                        statusColor = Color.yellow;
                        break;
                    //Destroyed
                    case SymbolID.Status_Present_Destroyed:
                        statusColor = Color.red;
                        break;
                    //full to capacity(hospital)
                    case SymbolID.Status_Present_FullToCapacity:
                        statusColor = Color.blue;
                        break;
                    default:
                        break;
                }

                String svgOCIStatusColor = RendererUtilities.colorToHexString(statusColor,false);
                sbSVG.append(Shape2SVG.Convert(ociBounds, null, svgStroke, svgStrokeWidth, svgAlpha, svgAlpha, null,null));
                sbSVG.append(Shape2SVG.Convert(ociShape, null, svgOCIStatusColor, svgStrokeWidth, svgAlpha, svgAlpha, null,null));

                ociBounds = null;
                ociShape = null;

            }
            if (mobilityBounds != null)
            {

                String svgMobilitySW = svgStrokeWidth;
                if (!(ad > 30 && ad < 60))//mobility
                {
                    svgMobilitySW = String.valueOf(strokeWidthNL);
                }

                sbSVG.append(Shape2SVG.Convert(mobilityPath, svgStroke, null, svgMobilitySW, svgAlpha, svgAlpha, null,null));

                if (mobilityPathFill != null)
                {
                    sbSVG.append(Shape2SVG.Convert(mobilityPathFill, "none", svgStroke, "0", svgAlpha, svgAlpha, null,null));
                }

                mobilityBounds = null;
            }

            //add symbol
            ssi = (SVGSymbolInfo)sdi;
            sbSVG.append(ssi.getSVG());

            if (ociBounds != null && ociSlashShape != null)
            {
                double size = symbolBounds.getWidth();
                float ociStrokeWidth = 3f;

                ociStrokeWidth = (float) size / 20f;
                if (ociStrokeWidth < 1f)
                    ociStrokeWidth = 1f;

                sbSVG.append(Shape2SVG.Convert(ociSlashShape, svgStroke, null, String.valueOf(ociStrokeWidth), svgAlpha, svgAlpha, null,null));
                ociBounds = null;
                ociSlashShape = null;
            }

            if(rBounds != null)
            {
                String restrictedGroup = "<g id=\"restricted\" stroke-linecap=\"round\" stroke-linejoin=\"round\">";
                //triangle
                restrictedGroup += Shape2SVG.Convert(rPath, "#000000", "#FFFF00", String.valueOf(rStrokeWidth),svgAlpha,svgAlpha,null,null);
                //exclamation
                restrictedGroup += Shape2SVG.Convert(rPath2, "#000000", null, String.valueOf(rStrokeWidth * 1.66667),svgAlpha,svgAlpha,null,null);
                //dot
                restrictedGroup += Shape2SVG.Convert(rCircle, "#000000", "#000000", String.valueOf(rStrokeWidth),svgAlpha,svgAlpha,null,null);
                restrictedGroup += "</g>";

                sbSVG.append(restrictedGroup);
            }

            if(nsBounds != null)
            {
                String noStrikeGroup = "<g id=\"nostrike\">";
                noStrikeGroup += Shape2SVG.Convert(nsCircle, "#000000", "#FFFF00", String.valueOf(nsStrokeWidth),svgAlpha,svgAlpha,null,null);
                noStrikeGroup += Shape2SVG.Convert(nsLine, "#000000", null, String.valueOf(nsStrokeWidth),svgAlpha,svgAlpha,null,null);
                noStrikeGroup += "</g>";
                sbSVG.append(noStrikeGroup);
            }

            if (domBounds != null)
            {
                Path2D domPath = new Path2D.Double() ;

                domPath.moveTo(domPoints[0].getX(), domPoints[0].getY());
                if (domPoints[1] != null)
                {
                    domPath.lineTo(domPoints[1].getX(), domPoints[1].getY());
                }
                if (domPoints[2] != null)
                {
                    domPath.lineTo(domPoints[2].getX(), domPoints[2].getY());
                }
                sbSVG.append(Shape2SVG.Convert(domPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null,null));

                domPath.reset();

                domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
                domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
                domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
                sbSVG.append(Shape2SVG.Convert(domPath, "none", svgStroke, "0", svgAlpha, svgAlpha, null,null));

                domBounds = null;
                domPoints = null;
            }

            newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
        }

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Draw Modifiers (ImageInfo)">

        if(sdi instanceof ImageInfo)
        {
            BufferedImage bmp = new BufferedImage((int) Math.ceil(imageBounds.getWidth()), (int) Math.ceil(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bmp.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            lineColor = RendererUtilities.setColorAlpha(lineColor,alpha);
            textColor = RendererUtilities.setColorAlpha(textColor,alpha);
            textBackgroundColor = RendererUtilities.setColorAlpha(textBackgroundColor,alpha);
            ebColor = RendererUtilities.setColorAlpha(ebColor,alpha);

            g2d.setColor(lineColor);
            g2d.setFont(modifierFont);

            ArrayList<TextInfo> tiArray = new ArrayList<>();
            if (echelonBounds != null) {
                tiArray.add(tiEchelon);
            }

            if (amBounds != null) {
                tiArray.add(tiAM);
            }

            if (tiArray.isEmpty() == false) {
                renderText(g2d, tiArray, textColor, textBackgroundColor);
            }

            //render////////////////////////////////////////////////////////

            BasicStroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
            g2d.setColor(lineColor);
            g2d.setStroke(stroke);

            if (hqBounds != null) {
                g2d.drawLine((int) pt1HQ.getX(), (int) pt1HQ.getY(), (int) pt2HQ.getX(), (int) pt2HQ.getY());
            }

            if (tfBounds != null) {
                g2d.draw(tfRectangle);
            }

            if (ebBounds != null) {
                stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 4.0f);
                //draw bar fill
                g2d.setColor(ebColor);
                g2d.fillRect((int) ebRectangle.getX(), (int) ebRectangle.getY(), (int) ebRectangle.getWidth(), (int) ebRectangle.getHeight());

                //draw bar outline
                g2d.setColor(lineColor);
                g2d.setStroke(stroke);
                g2d.drawRect((int) ebRectangle.getX(), (int) ebRectangle.getY(), (int) ebRectangle.getWidth(), (int) ebRectangle.getHeight());

                //draw bar text
                TextInfo[] aTiAO =
                        {
                                tiAO
                        };
                renderText(g2d, aTiAO, RendererUtilities.setColorAlpha(Color.BLACK,alpha), new Color(0, 0, 0, 0));//*/No outline because it's on a colored bar
                amBounds = null;
                tiAO = null;


                ebBounds = null;
                tiAO = null;
                ebRectangle = null;

            }

            if (echelonBounds != null) {
                TextInfo[] aTiEchelon =
                        {
                                tiEchelon
                        };
                renderText(g2d, aTiEchelon, textColor, textBackgroundColor);

                echelonBounds = null;
                tiEchelon = null;
            }

            if (amBounds != null) {
                TextInfo[] aTiAM =
                        {
                                tiAM
                        };
                renderText(g2d, aTiAM, textColor, textBackgroundColor);
                amBounds = null;
                tiAM = null;
            }

            if (fdiBounds != null) {
                float[] dashArray = {6f, 4f};

                g2d.setColor(lineColor);

                if (symbolBounds.getHeight() < 20) {
                    dashArray[0] = 5f;
                    dashArray[1] = 3f;
                }

                /// ///////////////////////////////////
                //Divide line in 14 parts. line is 3 parts to 2 parts gap
                float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                //distance = distance / 14f;
                dashArray[1] = (int)((distance / 14f) * 2);
                dashArray[0] = (int)((distance / 14f) * 3);//*/
                /// //////////////////////////////////

                stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
                g2d.setStroke(stroke);
                Path2D fdiPath = new Path2D.Double();

            /*fdiPath.moveTo(fdiLeft.getX(), fdiLeft.getY());
            fdiPath.lineTo(fdiTop.getX(), fdiTop.getY());
            fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());*/

                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                g2d.draw(fdiPath);

                fdiBounds = null;

            }

            if (liBounds != null) {

                g2d.setColor(lineColor);
                int liStrokeWidth = 2;
                if (pixelSize < 100)
                    liStrokeWidth = 1;
                stroke = new BasicStroke(liStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                g2d.setStroke(stroke);

            /*liPath = new Path2D.Double();
            liPath.moveTo(liTop.getX(), liTop.getY());
            liPath.lineTo(liLeft.getX(), liLeft.getY());
            liPath.moveTo(liTop.getX(), liTop.getY());
            liPath.lineTo(liRight.getX(), liRight.getY());//*/

            /*
            liPath.moveTo(liLeft.getX(), liLeft.getY());
            liPath.lineTo(liTop.getX(), liTop.getY());
            liPath.lineTo(liRight.getX(), liRight.getY());//*/

                g2d.draw(liPath);

                liBounds = null;

            }

            if (mobilityBounds != null) {
                stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                g2d.setColor(lineColor);

                //ctx.lineCap = "butt";
                //ctx.lineJoin = "miter";
                if (ad > 30 && ad < 60)//mobility
                {
                    //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else //NS or NL
                {
                    stroke = new BasicStroke(strokeWidthNL, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                    //mobilityPaint.setAntiAlias(true);
                }

                g2d.setStroke(stroke);
                g2d.draw(mobilityPath);

                if (mobilityPathFill != null) {
                    g2d.fill(mobilityPathFill);
                }

                mobilityBounds = null;

            }

            if (ociBounds != null && ociShape != null) {

                Color statusColor = null;
                int status = SymbolID.getStatus(symbolID);

                switch (status)
                {
                    //Fully Capable
                    case SymbolID.Status_Present_FullyCapable:
                        statusColor = Color.green;
                        break;
                    //Damaged
                    case SymbolID.Status_Present_Damaged:
                        statusColor = Color.yellow;
                        break;
                    //Destroyed
                    case SymbolID.Status_Present_Destroyed:
                        statusColor = Color.red;
                        break;
                    //full to capacity(hospital)
                    case SymbolID.Status_Present_FullToCapacity:
                        statusColor = Color.blue;
                        break;
                    default:
                        break;


                }

                statusColor = RendererUtilities.setColorAlpha(statusColor, alpha);

                g2d.setColor(lineColor);
                g2d.fillRect((int) ociBounds.getX(), (int) ociBounds.getY(), (int) ociBounds.getWidth(), (int) ociBounds.getHeight());
                g2d.setColor(statusColor);
                g2d.fillRect((int) ociShape.getX(), (int) ociShape.getY(), (int) ociShape.getWidth(), (int) ociShape.getHeight());

                ociBounds = null;
                ociShape = null;
            }



            //draw original icon.
            //ctx.drawImage(ii.getImage(),symbolBounds.getX(), symbolBounds.getY());

            ii = (ImageInfo) sdi;

            g2d.drawImage(ii.getImage(), (int) symbolBounds.getX(), (int) symbolBounds.getY(), null);// drawBitmap(pi.getImage(), null, symbolBounds, null);

            if(rBounds != null)
            {
                stroke = new BasicStroke(rStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g2d.setStroke(stroke);
                g2d.setColor(RendererUtilities.setColorAlpha(Color.YELLOW,alpha));
                g2d.fill(rPath);//circle fill
                g2d.setColor(RendererUtilities.setColorAlpha(Color.BLACK,alpha));
                stroke = new BasicStroke(rStrokeWidth * 1.66667f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g2d.setStroke(stroke);
                g2d.draw(rPath2);//exclamation stroke
                stroke = new BasicStroke(rStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                g2d.setStroke(stroke);
                g2d.fill(rCircle);//exclamation period
                g2d.draw(rCircle);//exclamation period
                g2d.draw(rPath);//triangle outline
            }
            if(nsBounds != null)
            {
                stroke = new BasicStroke((float)nsStrokeWidth);
                g2d.setStroke(stroke);
                g2d.setColor(RendererUtilities.setColorAlpha(Color.YELLOW,alpha));
                g2d.fill(nsCircle);
                g2d.setColor(RendererUtilities.setColorAlpha(Color.BLACK,alpha));
                g2d.draw(nsCircle);
                g2d.draw(nsLine);
            }

            if (domBounds != null) {
                drawDOMArrow(g2d, domPoints, lineColor, strokeWidth);

                domBounds = null;
                domPoints = null;
            }

            if (ociBounds != null && ociSlashShape != null) {
                g2d.setColor(lineColor);

                double size = symbolBounds.getWidth();
                float ociStrokeWidth = 3f;

                ociStrokeWidth = (float) size / 20f;
                if (ociStrokeWidth < 1f)
                    ociStrokeWidth = 1f;

                stroke = new BasicStroke(ociStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
                g2d.setStroke(stroke);
                g2d.draw(ociSlashShape);

                ociBounds = null;
                ociSlashShape = null;
            }
            if (bmp != null)
                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
        }
        // </editor-fold>


        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        // </editor-fold>

        //return newii;
        if (newsdi != null)
        {
            return newsdi;
        }
        else
        {
            return null;
        }
        //*/
        //return null;

    }

    /**
     *
     * @param {type} symbolID
     * @param {type} bounds symbolBounds SO.Rectangle
     * @param {type} center SO.Point Location where symbol is centered.
     * @param {type} angle in degrees
     * @param {Boolean} isY Boolean. (Y modifier is present)
     * @returns {Array} of SO.Point. First 3 items are the line. Last three are
     * the arrowhead.
     */
    private static Point2D[] createDOMArrowPoints(String symbolID, Rectangle2D bounds, Point2D center, float angle, boolean isY, float modifierFontHeight)
    {
        Point2D[] arrowPoints = new Point2D.Double[6];
        Point2D pt1 = null;
        Point2D pt2 = null;
        Point2D pt3 = null;


        int length = 40;
        if (SymbolUtilities.isCBRNEvent(symbolID))
        {
            length = (int)Math.round(bounds.getHeight() / 2);
        }
        else if((SymbolUtilities.isHQ(symbolID)))
        {
            if(SymbolUtilities.hasRectangleFrame(symbolID))
                length = (int)Math.round(bounds.getHeight());
            else
                length = (int)Math.round(bounds.getHeight() * 0.7);
        }
        else //if(bounds.getHeight() >= 100)
        {
            length = (int)Math.round(bounds.getHeight() * 0.7);
        }

        //get endpoint
        int dx2, dy2,
                x1, y1,
                x2, y2;

        x1 = (int)Math.round(center.getX());
        y1 = (int)Math.round(center.getY());

        pt1 = new Point2D.Double(x1, y1);

        if (SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.Q_DIRECTION_OF_MOVEMENT ) &&
                SymbolUtilities.isCBRNEvent(symbolID) ||
                SymbolUtilities.isLand(symbolID) ||
                SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_DismountedIndividuals)
        {
            //drawStaff = true;
            if(SymbolUtilities.isHQ(symbolID)==false)//has HQ staff to start from
            {
                y1 = (int)(bounds.getY() + bounds.getHeight());
                pt1 = new Point.Double(x1, y1);

                if (isY == true && SymbolUtilities.isCBRNEvent(symbolID))//make room for y modifier
                {
                    int yModifierOffset = (int) modifierFontHeight;

                    yModifierOffset += RS.getTextOutlineWidth();

                    pt1.setLocation(pt1.getX(), pt1.getY() + yModifierOffset);
                }//*/

                y1 = y1 + length;
                pt2 = new Point.Double(x1, y1);
            }
            else
            {
                x1 = (int)bounds.getX()+1;

                if (SymbolUtilities.hasRectangleFrame(symbolID))
                {
                    /*y1 = bounds.top + bounds.height();
                    pt1 = new Point(x1, y1);
                    y1 = y1 + length;
                    pt2 = new Point(x1, y1);//*/

                    y1 = (int)(bounds.getY() + bounds.getHeight());
                    pt1 = new Point2D.Double(x1, y1);
                    y1 = y1 + length;
                    pt2 = new Point2D.Double(x1, y1);//*/

                }
                else
                {
                    y1 = (int)(bounds.getY() + (bounds.getHeight() / 2));
                    pt1 = new Point2D.Double(x1, y1);

                    x2 = x1;
                    y1 = (int)(pt1.getY() + bounds.getHeight());
                    pt2= new Point2D.Double(x2, y1);

                    //I feel like the below code is the same as above but it didn't work out that way
                    //keeping to try and figure out later
                    /*y1 = (int)(bounds.getY() + (bounds.getHeight() / 2));
                    pt1 = new Point2D.Double(x1, y1);

                    x2 = x1;
                    y2 = (int)(pt1.getY() + bounds.getHeight());
                    pt2= new Point2D.Double(x2, y2);*/
                }
            }
        }

        //get endpoint given start point and an angle
        //x2 = x1 + (length * Math.cos(radians)));
        //y2 = y1 + (length * Math.sin(radians)));
        angle = angle - 90;//in java, east is zero, we want north to be zero
        double radians = 0;
        radians = (angle * (Math.PI / 180));//convert degrees to radians

        dx2 = x1 + (int) (length * Math.cos(radians));
        dy2 = y1 + (int) (length * Math.sin(radians));
        x2 = Math.round(dx2);
        y2 = Math.round(dy2);


        //UPDATED ARROWHEAD CODE
        Point2D[] head = null;
        Point2D endPoint = new Point2D.Double(x2, y2);
        if(pt2 != null)
            head = createDOMArrowHead(pt2, endPoint);//pt3);
        else
            head = createDOMArrowHead(pt1, endPoint);//pt3);

        if(head != null)
        {
            arrowPoints[0] = pt1;
            arrowPoints[1] = pt2;
            arrowPoints[2] = pt3;
            arrowPoints[3] = head[0];
            arrowPoints[4] = head[1];
            arrowPoints[5] = head[2];

            //adjusted endpoint
            if(head.length >= 4 && head[3] != null)
            {
                arrowPoints[2] = head[3];
            }
        }

        return arrowPoints;

    }

    private static Point2D[] createDOMArrowHead(Point2D lpt1, Point2D lpt2)
    {
        Point2D[] arrowPoints = new Point2D.Double[6];
        Point2D pt1 = null;
        Point2D pt2 = null;
        Point2D pt3 = null;

        double x1 = lpt1.getX();
        double y1 = lpt1.getY();
        double x2 = lpt2.getX();
        double y2 = lpt2.getY();

        // Compute direction vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dy * dy);

        // Scale triangle size
        double scale = length * 0.15;  // Scaling factor for size
        double offset = scale * 1.5;  // Move triangle further down the line

        // Normalize direction vector
        double unitX = dx / length;
        double unitY = dy / length;

        // Compute perpendicular vector for triangle base
        double nx = -unitY;
        double ny = unitX;

        // Compute adjusted triangle vertices
        double tipX = x2;
        double tipY = y2;
        double baseX1 = (int) (x2 - offset * unitX + scale * nx);
        double baseY1 = (int) (y2 - offset * unitY + scale * ny);
        double baseX2 = (int) (x2 - offset * unitX - scale * nx);
        double baseY2 = (int) (y2 - offset * unitY - scale * ny);


        //arrowHead = new Polygon(xPoints, yPoints, 3);
        arrowPoints[0] = new Point2D.Double(tipX,tipY);
        arrowPoints[1] = new Point2D.Double(baseX1,baseY1);
        arrowPoints[2] = new Point2D.Double(baseX2,baseY2);
        // Adjust line endpoint to be the middle of the base line of the arrowhead
        double adjustedX2 = (baseX1 + baseX2) / 2;
        double adjustedY2 = (baseY1 + baseY2) / 2;
        arrowPoints[3] = new Point2D.Double(adjustedX2,adjustedY2);

        return arrowPoints;

    }

    private static void drawDOMArrow(Graphics2D g2d, Point2D[] domPoints, Color color, float strokeWidth)
    {
        BasicStroke stroke = new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);

        Path2D domPath = new Path2D.Double() ;

        domPath.moveTo(domPoints[0].getX(), domPoints[0].getY());
        if (domPoints[1] != null)
        {
            domPath.lineTo(domPoints[1].getX(), domPoints[1].getY());
        }
        if (domPoints[2] != null)
        {
            domPath.lineTo(domPoints[2].getX(), domPoints[2].getY());
        }
        g2d.setStroke(stroke);
        g2d.setColor(color);
        g2d.draw(domPath);

        domPath.reset();

        domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
        domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
        domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
        g2d.fill(domPath);
    }

    private static Rectangle2D processOperationalConditionIndicator(String symbolID, Rectangle2D symbolBounds, int offsetY)
    {
        //create Operational Condition Indicator
        //set color
        Rectangle2D bar = null;
        int status;
        Color statusColor;
        int barSize = 0;
        int pixelSize = (int)symbolBounds.getHeight();

        status = SymbolID.getStatus(symbolID);
        if (status == SymbolID.Status_Present_FullyCapable ||
                status == SymbolID.Status_Present_Damaged ||
                status == SymbolID.Status_Present_Destroyed ||
                status == SymbolID.Status_Present_FullToCapacity)
        {
            if (pixelSize > 0)
            {
                barSize = Math.round(pixelSize / 5);
            }

            if (barSize < 2)
            {
                barSize = 2;
            }

            offsetY += Math.round(symbolBounds.getY() + symbolBounds.getHeight());

            bar = new Rectangle2D.Double((int)symbolBounds.getX() + 2, offsetY, (int)Math.round(symbolBounds.getWidth()) - 4, barSize);
        }

        return bar;
    }

    private static Path2D processOperationalConditionIndicatorSlash(String symbolID, Rectangle2D symbolBounds)
    {
        //create Operational Condition Indicator
        Path2D path = null;
        int status;
        status = SymbolID.getStatus(symbolID);

        if (status == SymbolID.Status_Present_Damaged  || status == SymbolID.Status_Present_Destroyed)
        {
            float widthRatio = SymbolUtilities.getUnitRatioWidth(symbolID);
            float heightRatio = SymbolUtilities.getUnitRatioHeight(symbolID);

            double slashHeight = (symbolBounds.getHeight() / heightRatio * 1.47f);
            double slashWidth = (symbolBounds.getWidth() / widthRatio * 0.85f);
            double centerX = symbolBounds.getCenterX();
            double centerY = symbolBounds.getCenterY();
            path = new Path2D.Double();
            if(status == SymbolID.Status_Present_Damaged)//Damaged /
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
            }
            else if(status == SymbolID.Status_Present_Destroyed)//Destroyed X
            {
                path.moveTo(centerX - (slashWidth/2),centerY+(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY-(slashHeight/2));
                path.moveTo(centerX - (slashWidth/2),centerY-(slashHeight/2));
                path.lineTo(centerX + (slashWidth/2),centerY+(slashHeight/2));
            }
            return path;

        }

        return path;
    }

    public static SymbolDimensionInfo processSpeedLeader(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        SymbolDimensionInfo rsdi = sdi;

        Rectangle2D imageBounds = sdi.getImageBounds();
        Rectangle2D symbolBounds = sdi.getSymbolBounds();
        Point2D symbolCenter = sdi.getSymbolCenterPoint();
        int ss = SymbolID.getSymbolSet(symbolID);
        int pixelSize = RendererSettings.getInstance().getDefaultPixelSize();
        int dpi = RendererSettings.getInstance().getDeviceDPI();
        if(attributes.containsKey(MilStdAttributes.PixelSize))
            pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
        float strokeWidth = 3f;
        strokeWidth = (float) dpi / 48f;
        if (strokeWidth < 1f)
            strokeWidth = 1f;

        Path2D slPath = null;
        Rectangle2D slBounds = null;
        try {
            if (SymbolUtilities.hasModifier(symbolID, Modifiers.AJ_SPEED_LEADER) &&
                    (modifiers.containsKey(Modifiers.AJ_SPEED_LEADER))) {
                String aj = modifiers.get(Modifiers.AJ_SPEED_LEADER);
                String[] values = aj.split(" ");
                if (values.length >= 3) {
                    int speed = Integer.parseInt(values[0]);
                    String speedUnit = values[1];
                    int angle = 0;
                    if (values[2].length() == 3)
                        angle = Integer.parseInt(values[2]) - 90;
                    else
                        angle = (int) (Integer.parseInt(values[2]) * 0.05625) - 90;

                    slPath = new Path2D.Double();
                    slPath.moveTo(symbolCenter.getX(), symbolCenter.getY());

                    //convert to Knots
                    switch (speedUnit)//KPH, KPS, MPH, NMH, KTS//https://www.aviationhunt.com/speed-converter/
                    {
                        case "KPH":
                            speed = (int) (speed * 0.539957);
                            break;
                        case "KPS"://https://www.metric-conversions.org/speed/kilometers-per-second-to-knots.htm
                            speed = (int) (speed * 1943.84);
                            break;
                        case "MPH":
                            speed = (int) (speed * 0.86897);
                            break;
                    }

                    int distance = 0;
                    char frame = SymbolID.getFrameShape(symbolID);
                    boolean fast = false;
                    if (frame == '0' && ss == SymbolID.SymbolSet_Air ||
                            ss == SymbolID.SymbolSet_AirMissile ||
                            ss == SymbolID.SymbolSet_SignalsIntelligence_Air ||
                            ss == SymbolID.SymbolSet_SpaceMissile ||
                            ss == SymbolID.SymbolSet_Space ||
                            (SymbolID.getVersion(symbolID) <= SymbolID.Version_2525Dch1 && ss == SymbolID.SymbolSet_SignalsIntelligence_Space))
                    {
                        fast = true;
                    }
                    else if(frame == SymbolID.FrameShape_Air || frame == SymbolID.FrameShape_Space)
                    {
                        fast = true;
                    }

                    float distanceScaler = dpi;//spec does scale by inch, but if the symbol is too big, scale by pixel size
                    if(dpi < pixelSize)
                        distanceScaler = pixelSize;

                    if(fast)
                    {
                        if (speed < 300)
                            distance = (int) ((distanceScaler * 0.25)/300f * speed);
                        else if (speed < 600)
                            distance = (int) ((distanceScaler * 0.5)/600f * speed);
                        else
                            distance = (int) (distanceScaler * 0.75);
                    } else//submarine might be 1/4 inch if its speed is less than 15 knots, 1/2 inch if its speed is between 15 and 30 knots and 3/4 inch if its speed is more than 30 knots
                    {
                        if (speed < 15)
                            distance = (int) ((distanceScaler * 0.25)/15f * speed);
                        else if (speed < 30)
                            distance = (int) ((distanceScaler * 0.5)/30f * speed);
                        else
                            distance = (int) (distanceScaler * 0.75);
                    }
                    double radians = (angle * (Math.PI / 180));//convert degrees to radians
                    int x2 = (int) (symbolCenter.getX() + distance * Math.cos(radians));
                    int y2 = (int) (symbolCenter.getY() + distance * Math.sin(radians));

                    slPath.lineTo(x2, y2);
                    slBounds = slPath.getBounds2D();
                    imageBounds = imageBounds.createUnion(slBounds);
                }

                if (sdi instanceof ImageInfo) {
                    BufferedImage bmp = new BufferedImage((int) Math.ceil(imageBounds.getWidth()), (int) Math.ceil(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = bmp.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int alpha = 1;
                    if (attributes.containsKey(MilStdAttributes.Alpha))
                        alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
                    Color lineColor = RendererUtilities.setColorAlpha(Color.BLACK, alpha);
                    g2d.setColor(lineColor);


                    Stroke stroke = new BasicStroke(strokeWidth);

                    double offsetX = 0;
                    double offsetY = 0;
                    if (imageBounds.getX() < 0)
                        offsetX = imageBounds.getX() * -1;
                    if (imageBounds.getY() < 0)
                        offsetY = imageBounds.getY() * -1;

                    ShapeUtilities.offset(slPath, (int)offsetX, (int)offsetY);

                    g2d.drawImage(((ImageInfo) sdi).getImage(), null, (int) offsetX, (int) offsetY);
                    g2d.setStroke(stroke);
                    g2d.setColor(RendererUtilities.setColorAlpha(Color.BLACK, alpha));
                    g2d.draw(slPath);

                    ShapeUtilities.offset(symbolBounds, offsetX, offsetY);
                    ShapeUtilities.offset(imageBounds, offsetX, offsetY);
                    ShapeUtilities.offset(symbolCenter, offsetX, offsetY);

                    rsdi = new ImageInfo(bmp, symbolCenter, symbolBounds);
                }
                else if (sdi instanceof SVGSymbolInfo)
                {//public static String Convert(Shape shape,String stroke, String fill, String strokeWidth, String strokeOpacity, String fillOpacity, String dashArray, String lineCap)
                    String svg = ((SVGSymbolInfo) sdi).getSVG();

                    svg += (Shape2SVG.Convert(slPath, "#000000", "none", String.valueOf(strokeWidth),null,null,null, null));
                    rsdi = new SVGSymbolInfo(svg,symbolCenter,symbolBounds,imageBounds);
                }
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("ModifierRenderer","processSpeedLineIndicator",exc);
        }

        return rsdi;
    }

    /**
     * uses 2525C layout which shows most modifiers
     *
     * @param sdi
     * @param symbolID
     * @param modifiers
     * @param attributes
     * @return
     */
    public static SymbolDimensionInfo  processUnknownTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        Font modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        int bufferHorizontal = (int)modifierFontHeight/2;
        int bufferXL = bufferHorizontal;
        int bufferXR = bufferHorizontal;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo  newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }

        Rectangle2D labelBounds = null;
        int labelWidth, labelHeight;

        Rectangle2D bounds = (Rectangle2D)(sdi.getSymbolBounds().clone());
        Rectangle2D symbolBounds = (Rectangle2D)(sdi.getSymbolBounds().clone());
        Point2D centerPoint = sdi.getSymbolCenterPoint();
        Rectangle2D imageBounds = new Rectangle((int)sdi.getImageBounds().getX(),(int)sdi.getImageBounds().getY(), (int)sdi.getImageBounds().getWidth(), (int)sdi.getImageBounds().getHeight());
        Rectangle2D imageBoundsOld = (Rectangle2D)imageBounds.clone();

        String echelonText = SymbolUtilities.getEchelonText(SymbolID.getAmplifierDescriptor(symbolID));
        String amText = SymbolUtilities.getStandardIdentityModifier(symbolID);

        //adjust width of bounds for mobility/echelon/engagement bar which could be wider than the symbol
        bounds = RectUtilities.toRectangle(imageBounds.getX(), bounds.getY(), imageBounds.getWidth(), bounds.getHeight());

        

        //check if text is too tall:
        boolean byLabelHeight = true;
        labelHeight = (int) (modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
         RendererSettings.getModifierFontSize(),
         RendererSettings.getModifierFontStyle()).fullHeight;*/

        int maxHeight = (int)(bounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(Modifiers.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[Modifiers.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/

        //Check for Valid Country Code
        String cc = GENCLookup.getInstance().get3CharCode(SymbolID.getCountryCode(symbolID));
        if (cc != null && !cc.equals(""))
        {
            modifiers.put(Modifiers.AS_COUNTRY, cc);
            //modifiers[Modifiers.CC_COUNTRY_CODE] = symbolID.substring(12,14);
        }

        //            int y0 = 0;//W            E/F
        //            int y1 = 0;//X/Y          G
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M CC
        //            int y4 = 0;//Z            J/K/L/N/P
        //
        //            y0 = bounds.y - 0;
        //            y1 = bounds.y - labelHeight;
        //            y2 = bounds.y - (labelHeight + (int)bufferText) * 2;
        //            y3 = bounds.y - (labelHeight + (int)bufferText) * 3;
        //            y4 = bounds.y - (labelHeight + (int)bufferText) * 4;
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        //if(Modifiers.C_QUANTITY in modifiers
        if (modifiers.containsKey(Modifiers.C_QUANTITY)
                && SymbolUtilities.hasModifier(symbolID, Modifiers.C_QUANTITY))
        {
            String text = modifiers.get(Modifiers.C_QUANTITY);
            if(text != null)
            {
                //bounds = armyc2.c5isr.renderer.utilities.RendererUtilities.getTextOutlineBounds(modifierFont, text, new SO.Point(0,0));
                tiTemp = new TextInfo(text, 0, 0, modifierFont, frc);
                labelBounds = RectUtilities.toRectangle(tiTemp.getTextBounds());
                labelWidth = (int)labelBounds.getWidth();
                x = (int)Math.round((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
                y = (int)Math.round(symbolBounds.getY() - bufferY - tiTemp.getDescent());
                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        //if(Modifiers.X_ALTITUDE_DEPTH in modifiers || Modifiers.Y_LOCATION in modifiers)
        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            String xm = null,
                    ym = null;

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) && SymbolUtilities.hasModifier(symbolID, Modifiers.X_ALTITUDE_DEPTH))
            {
                xm = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);// xm = modifiers.X;
            }
            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                ym = modifiers.get(Modifiers.Y_LOCATION);// ym = modifiers.Y;
            }
            if (xm == null && ym != null)
            {
                modifierValue = ym;
            }
            else if (xm != null && ym == null)
            {
                modifierValue = xm;
            }
            else if (xm != null && ym != null)
            {
                modifierValue = xm + "  " + ym;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                if (!byLabelHeight)
                {
                    x = (int)Math.round(bounds.getX() - labelBounds.getWidth() - bufferXL);
                    y = (int)Math.round(bounds.getY() + labelHeight - tiTemp.getDescent());
                }
                else
                {
                    x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);

                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y - ((labelHeight + bufferText));
                    y = (int)(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) && SymbolUtilities.hasModifier(symbolID, Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                if (!byLabelHeight)
                {
                    y = (int)(bounds.getY() + labelHeight - tiTemp.getDescent());
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y - ((labelHeight + bufferText));
                    y = (int)(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if ((modifiers.containsKey(Modifiers.V_EQUIP_TYPE)) ||
                (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE)) ||
                (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME)))
        {
            String vm = null,
                    adm = null,
                    aem = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) && SymbolUtilities.hasModifier(symbolID, Modifiers.V_EQUIP_TYPE))
            {
                vm = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) && SymbolUtilities.hasModifier(symbolID, Modifiers.AD_PLATFORM_TYPE))
            {
                adm = modifiers.get(Modifiers.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME) && SymbolUtilities.hasModifier(symbolID, Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                aem = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if(vm != null && vm.equals("") == false)
                modifierValue = vm;
            if(adm != null && adm.equals("") == false)
                modifierValue += " " + adm;
            if(aem != null && aem.equals("") == false)
                modifierValue += " " + aem;

            if(modifierValue != null)
                modifierValue = modifierValue.trim();
            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);

                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5f) + ((labelHeight - tiTemp.getDescent()) * 0.5f));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            modifierValue = "";
            String hm = "",
                    afm = "";

            hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER) && SymbolUtilities.hasModifier(symbolID, Modifiers.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);

                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - tiTemp.getDescent()) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                if (!byLabelHeight)
                {
                    x = (int)bounds.getX() - labelWidth - bufferXL;
                    y = (int)(bounds.getY() + bounds.getHeight());
                }
                else
                {
                    x = (int)(bounds.getX() - labelWidth - bufferXL);

                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText) - tiTemp.getDescent()));
                    y = (int)(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION) || modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION) && SymbolUtilities.hasModifier(symbolID, Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                if (modifierValue.length() > 0)
                {
                    modifierValue += " ";
                }
                modifierValue += modifiers.get(Modifiers.AS_COUNTRY);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                if (!byLabelHeight)
                {
                    y = (int)(bounds.getY() + bounds.getHeight());
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText - tiTemp.getDescent())));
                    y = (int)bounds.getY() + y;
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED) && SymbolUtilities.hasModifier(symbolID, Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelWidth - bufferXL);
                if (!byLabelHeight)
                {
                    y = (int)(Math.round(bounds.getY() + bounds.getHeight()+ labelHeight + bufferText));
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText) * 2) - (tiTemp.getDescent() * 2));
                    y = (int)Math.round(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP)//
                || modifiers.containsKey(Modifiers.N_HOSTILE)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    lm = null,
                    nm = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) && SymbolUtilities.hasModifier(symbolID, Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) && SymbolUtilities.hasModifier(symbolID, Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(Modifiers.N_HOSTILE) && SymbolUtilities.hasModifier(symbolID, Modifiers.N_HOSTILE))
            {
                nm = modifiers.get(Modifiers.N_HOSTILE);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.hasModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
            {
                pm = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            modifierValue = "";
            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
            }
            if (km != null && km.equals("") == false)
            {
                modifierValue = modifierValue + " " + km;
            }
            if (lm != null && lm.equals("") == false)
            {
                modifierValue = modifierValue + " " + lm;
            }
            if (nm != null && nm.equals("") == false)
            {
                modifierValue = modifierValue + " " + nm;
            }
            if (pm != null && pm.equals("") == false)
            {
                modifierValue = modifierValue + " " + pm;
            }

            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                if (!byLabelHeight)
                {
                    y = (int)(Math.round(bounds.getY() + bounds.getHeight()+ labelHeight + bufferText));
                }
                else
                {
                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = (int)(y + ((labelHeight + bufferText) * 2) - (tiTemp.getDescent() * 2));
                    y = (int)Math.round(bounds.getY() + y);
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                if (!byLabelHeight)
                {
                    x = (int)(bounds.getX() - labelWidth - bufferXL);
                    y = (int)(bounds.getY() - bufferY - tiTemp.getDescent());
                }
                else
                {
                    x = (int)(bounds.getX() - labelWidth - bufferXL);

                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y - ((labelHeight + bufferText) * 2);
                    y = (int)bounds.getY() + y;
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String E = null,
                    F = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) && SymbolUtilities.hasModifier(symbolID, Modifiers.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(Modifiers.F_REINFORCED_REDUCED);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US).equals("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US).equals("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US).equals("RD"))
                {
                    F = "(" + (char) (177) + ")";
                }
            }

            if (F != null && F.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + F;
                }
                else
                {
                    modifierValue = F;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                if (!byLabelHeight)
                {
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    y = (int)(bounds.getY() - bufferY - tiTemp.getDescent());
                }
                else
                {
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);

                    y = (int)(bounds.getHeight());
                    y = (int) ((y * 0.5) + (labelHeight * 0.5));

                    y = y - ((labelHeight + bufferText) * 2);
                    y = (int)bounds.getY() + y;
                }

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ) && SymbolUtilities.hasModifier(symbolID, Modifiers.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int) ((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));

                y = (int)(symbolBounds.getHeight());//checkpoint, get box above the point
                y = (int) ((y * 0.5) + ((labelHeight - tiTemp.getDescent()) * 0.5));
                y = (int)symbolBounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        // </editor-fold>

         //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes, modifierFont);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        imageBoundsOld = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newsdi;

    }

    public static SymbolDimensionInfo ProcessSPTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        Font modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo  newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (modifierFontDescent + 0.5);

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }

        Rectangle2D labelBounds = null;
        int labelWidth, labelHeight;

        Rectangle bounds = RectUtilities.toRectangle(sdi.getSymbolBounds());
        Rectangle2D symbolBounds = (Rectangle2D)(sdi.getSymbolBounds().clone());
        Point2D centerPoint = sdi.getSymbolCenterPoint();
        Rectangle2D imageBounds = new Rectangle((int)sdi.getImageBounds().getX(),(int)sdi.getImageBounds().getY(), (int)sdi.getImageBounds().getWidth(), (int)sdi.getImageBounds().getHeight());
        Rectangle2D imageBoundsOld = (Rectangle2D)imageBounds.clone();

        String echelonText = SymbolUtilities.getEchelonText(SymbolID.getAmplifierDescriptor(symbolID));
        String amText = SymbolUtilities.getStandardIdentityModifier(symbolID);

        //adjust width of bounds for mobility/echelon/engagement bar which could be wider than the symbol
        bounds = RectUtilities.toRectangle(imageBounds.getX(), bounds.getY(), imageBounds.getWidth(), bounds.getHeight());



        //check if text is too tall:
        boolean byLabelHeight = true;
        labelHeight = (int) (modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
     RendererSettings.getModifierFontSize(),
     RendererSettings.getModifierFontStyle()).fullHeight;*/

        int maxHeight = (int)(bounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        //Affiliation Modifier being drawn as a display modifier
        String affiliationModifier = null;
        if (RS.getDrawAffiliationModifierAsLabel() == true)
        {
            affiliationModifier = SymbolUtilities.getStandardIdentityModifier(symbolID);
        }
        if (affiliationModifier != null)
        {   //Set affiliation modifier
            modifiers.put(Modifiers.E_FRAME_SHAPE_MODIFIER, affiliationModifier);
            //modifiers[Modifiers.E_FRAME_SHAPE_MODIFIER] = affiliationModifier;
        }//*/

        //Check for Valid Country Code
        int cc = SymbolID.getCountryCode(symbolID);
        String scc = "";
        if(cc > 0)
        {
            scc = GENCLookup.getInstance().get3CharCode(cc);
        }
        if(!scc.isEmpty())
            modifiers.put(Modifiers.AS_COUNTRY, scc);


        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        List<Modifier> mods = getLabelPositionIndexes(symbolID, modifiers, attributes);

        Modifier mod = null;
        if(mods != null) {
            for (int i = 0; i < mods.size(); i++) {
                mod = mods.get(i);

                tiTemp = new TextInfo(mod.getText(), 0, 0, modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int) labelBounds.getWidth();

                //on left
                x = (int) getLabelXPosition(bounds, labelWidth, mod.getIndexX(), modifierFontHeight);
                //above center V
                y = (int) getLabelYPosition(bounds, labelHeight, descent, bufferText, mod.getCentered(), mod.getIndexY());

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes, modifierFont);

        // <editor-fold defaultstate="collapsed" desc="Cleanup">
        tiArray = null;
        tiTemp = null;
        //tempShape = null;
        imageBoundsOld = null;
        //ctx = null;
        //buffer = null;
        // </editor-fold>

        return newsdi;

    }

    public static SymbolDimensionInfo ProcessTGSPWithSpecialModifierLayout(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, Color lineColor, FontRenderContext frc)
    {

        Font modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];
        
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;

        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        float strokeWidth = 2.0f;
        float alpha = -1;
        SymbolDimensionInfo newsdi = null;
        Color textColor = lineColor;
        Color textBackgroundColor = null;
        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        int e = SymbolID.getEntity(symbolID);
        int et = SymbolID.getEntityType(symbolID);
        int est = SymbolID.getEntitySubtype(symbolID);

        //Feint Dummy Indicator variables
        Rectangle2D fdiBounds = null;
        Point2D fdiTop = null;
        Point2D fdiLeft = null;
        Point2D fdiRight = null;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        Rectangle2D bounds = RectUtilities.copyRect(sdi.getSymbolBounds());
        Rectangle2D symbolBounds = RectUtilities.copyRect(sdi.getSymbolBounds());
        Point centerPoint = new Point(sdi.getSymbolCenterPoint());
        Rectangle2D imageBounds = RectUtilities.copyRect(sdi.getImageBounds());

        if(attributes.containsKey(MilStdAttributes.PixelSize))
        {
            int pixelSize = Integer.parseInt(attributes.get(MilStdAttributes.PixelSize));
            if(pixelSize <= 100)
                strokeWidth=2.0f;
            else
                strokeWidth=2 + ((pixelSize-100)/100f);
        }

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }

        centerPoint = new Point(Math.round(sdi.getSymbolCenterPoint().x), Math.round(sdi.getSymbolCenterPoint().y));

        boolean byLabelHeight = false;
        labelHeight = (int) (modifierFontHeight + 0.5f);

        int maxHeight = (int)(symbolBounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (modifierFontDescent + 0.5f);
        int yForY = -1;

        Rectangle2D labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rectangle2D labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;


        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }


        // <editor-fold defaultstate="collapsed" desc="Process Special Modifiers">
        TextInfo ti = null;
        if (SymbolUtilities.isCBRNEvent(symbolID))//chemical
        {
            if ((labelHeight * 3) > bounds.getHeight())
            {
                byLabelHeight = true;
            }
        }

        if(ss == SymbolID.SymbolSet_ControlMeasure) {
            if (ec == 130500 //contact point
                    || ec == 130700) //decision point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //One modifier symbols and modifier goes in center
                        x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5f));
                        x = x - (int) (labelWidth * 0.5f);
                        y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.4f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 212800)//harbor
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //One modifier symbols and modifier goes in center
                        x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5f));
                        x = x - (int) (labelWidth * 0.5f);
                        y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.5f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 131300)//point of interest
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //One modifier symbols, top third & center
                        x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5f));
                        x = x - (int) (labelWidth * 0.5f);
                        y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.25f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 131800//waypoint
                    || ec == 240900)//fire support station
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes right of center
                        if (ec == 131800)
                            x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.75f));
                        else
                            x = (int)(bounds.getMinX() + (bounds.getWidth()));
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 131900)  //Airfield (AEGIS Only)
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes right of center
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));

                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 180100 //Air Control point
                    || ec == 180200) //Communications Check point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)ti.getTextBounds().getWidth();
                        //One modifier symbols and modifier goes just below of center
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                        y = y + (int) (((bounds.getHeight() * 0.5f) - labelHeight) / 2) + labelHeight - descent;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } else if (ec == 160300 || //T (target reference point)
                    ec == 132000 || //T (Target Handover)
                    ec == 240601 || //ap,ap1,x,h (Point/Single Target)
                    ec == 240602) //T (nuclear target)
            { //Targets with special modifier positions
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)
                        && ec == 240601)//H //point single target
                {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        x = (int)(bounds.getCenterX() + (bounds.getWidth() * 0.15f));
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.75f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)
                        && ec == 240601)//X point or single target
                {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getCenterX() - (int) (bounds.getWidth() * 0.15f));
                        x = x - (labelWidth);
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.75f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                strText = null;
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) &&
                        (ec == 160300 || ec == 132000)) 
                {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        x = (int)(bounds.getCenterX() + (bounds.getWidth() * 0.15f));
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.25f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (ec == 240601 || ec == 240602)
                {
                    if (modifiers.containsKey(Modifiers.AP_TARGET_NUMBER)) {
                        strText = modifiers.get(Modifiers.AP_TARGET_NUMBER);
                    }
                    if (ec == 240601 && modifiers.containsKey(Modifiers.AP1_TARGET_NUMBER_EXTENSION)) {
                        if (strText != null)
                            strText = strText + "  " + modifiers.get(Modifiers.AP1_TARGET_NUMBER_EXTENSION);
                        else
                            strText = modifiers.get(Modifiers.AP1_TARGET_NUMBER_EXTENSION);
                    }
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        x = (int)(bounds.getCenterX() + (bounds.getWidth() * 0.15f));
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.25f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 132100)  //Key Terrain
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes right of center
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5 + bufferXR));

                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                        y = y + (int) ((labelHeight - descent) * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if(ec == 182600)//Isolated Personnel Location
            {
                if (modifiers.containsKey(Modifiers.C_QUANTITY)) {
                    strText = modifiers.get(Modifiers.C_QUANTITY);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //subset of NBC, just nuclear
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)bounds.getMinY() - descent;
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                        x = (int)bounds.getMinX() - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)bounds.getMinY() + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5) - ((labelHeight - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W1_DTG_2)) {
                    strText = modifiers.get(Modifiers.W1_DTG_2);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                        x = (int)bounds.getMinX() - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)bounds.getMinY() + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5) - (((labelHeight * 2) - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            }
            else if (SymbolUtilities.isCBRNEvent(symbolID)) //CBRN
            {
                if (modifiers.containsKey(Modifiers.N_HOSTILE)) {
                    strText = modifiers.get(Modifiers.N_HOSTILE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);

                        if (!byLabelHeight) {
                            y = (int)(bounds.getMinY() + bounds.getHeight());
                        } else {
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5f) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                        if (!byLabelHeight) {
                            y = (int)(bounds.getMinY() + labelHeight - descent);
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5f) - ((labelHeight - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                        x = (int)bounds.getMinX() - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)bounds.getMinY() + labelHeight - descent;
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + (labelHeight * 0.5) - (labelHeight + bufferText));
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5) - ((labelHeight - descent) * 0.5) + (-descent - bufferText)));
                        }

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if ((ec == 281500 || ec == 281600) && modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {//nuclear event or nuclear fallout producing event
                    strText = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //subset of nbc, just nuclear
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)bounds.getMinX() - labelWidth - bufferXL;
                        y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5) + ((labelHeight - descent) * 0.5)));//((bounds.getHeight / 2) - (labelHeight/2));

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)bounds.getMinX() - labelWidth - bufferXL;
                        if (!byLabelHeight) {
                            y = (int)(bounds.getMinY() + bounds.getHeight());
                        } else {
                            //y = bounds.y + ((bounds.getHeight * 0.5) + ((labelHeight-descent) * 0.5) + (labelHeight + bufferText));
                            y = (int)(bounds.getMinY() + ((bounds.getHeight() * 0.5) + ((labelHeight - descent) * 0.5) + (labelHeight - descent + bufferText)));
                        }
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.Y_LOCATION)) {
                    strText = modifiers.get(Modifiers.Y_LOCATION);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //just NBC
                        //x = bounds.getX() + (bounds.getWidth() * 0.5);
                        //x = x - (labelWidth * 0.5);
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                        x = x - (int) (labelWidth * 0.5f);

                        if (!byLabelHeight) {
                            y = (int)(bounds.getMinY() + bounds.getHeight() + labelHeight - descent + bufferY);
                        } else {
                            y = (int)(bounds.getMinY() + (int) ((bounds.getHeight() * 0.5) + ((labelHeight - descent) * 0.5) + ((labelHeight + bufferText) * 2) - descent));

                        }
                        yForY = y + descent; //so we know where to start the DOM arrow.
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.C_QUANTITY)) {
                    strText = modifiers.get(Modifiers.C_QUANTITY);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        //subset of NBC, just nuclear
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)bounds.getMinY() - descent;
                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            } 
            else if (ec == 270701)//static depiction
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)bounds.getMinY() - descent;// + (bounds.getHeight * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
                if (modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5));
                        x = x - (int) (labelWidth * 0.5);
                        y = (int)(bounds.getMinY() + (bounds.getHeight()));
                        y = y + (labelHeight);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.N_HOSTILE)) {
                    strText = modifiers.get(Modifiers.N_HOSTILE);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (bounds.getWidth()) + bufferXR);//right
                        //x = x + labelWidth;//- (labelbounds.getWidth * 0.75);

                        duplicate = true;

                        x2 = (int)bounds.getMinX();//left
                        x2 = x2 - labelWidth - bufferXL;// - (labelbounds.getWidth * 0.25);

                        y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.5));//center
                        y = y + (int) ((labelHeight - descent) * 0.5);

                        y2 = y;

                        ti.setLocation(Math.round(x), Math.round(y));
                        ti2.setLocation(Math.round(x2), Math.round(y2));
                        arrMods.add(ti);
                        arrMods.add(ti2);
                    }
                }

            }
            else if(e == 21 && et == 35)//sonobuoys
            {
                //H sitting on center of circle to the right
                //T above H
                centerPoint = SymbolUtilities.getCMSymbolAnchorPoint(symbolID,RectUtilities.copyRect(bounds));
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (bounds.getWidth()) + bufferXR);//right
                        y = centerPoint.y;

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (est == 0 || est == 1 || est == 4 || est == 7 || est == 8 || est == 15) {
                    if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                        strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                        if (strText != null) {
                            ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                            TextInfo ti2 = new TextInfo(strText, 0, 0, modifierFont, frc);
                            labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                            x = (int)(bounds.getMinX() + (bounds.getWidth()) + bufferXR);//right
                            y = centerPoint.y - labelHeight;

                            ti.setLocation(Math.round(x), Math.round(y));
                            arrMods.add(ti);
                        }
                    }
                }
            }
            else if(ec == 282001 || //tower, low
                    ec == 282002)   //tower, high
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.7));
                        y = (int)bounds.getMinY() + labelHeight;// + (bounds.getHeight * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if (ec == 180600)  //TACAN
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));

                        y = (int)(bounds.getMinY() + labelHeight);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 210300)  //Defended Asset
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() - labelWidth - bufferXL);

                        y = (int)(bounds.getMinY() + labelHeight);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 210600)  //Air Detonation
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));

                        y = (int)(bounds.getMinY() + labelHeight);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 210800)  //Impact Point
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes upper right of center
                        x = (int)(bounds.getX() + (bounds.getWidth() * 0.65f));
//                  x = x - (labelBounds.width * 0.5);
                        y = (int)(bounds.getY() + (bounds.getHeight() * 0.25f));
                        y = y + (int) (labelHeight * 0.5f);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 211000)  //Launched Torpedo
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes upper right of center
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getX() + (bounds.getWidth() * 0.5) - (labelWidth/2));
                        y = (int)(bounds.getY() - bufferY);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if(ec == 214900 || ec == 215600)//General Sea SubSurface Station & General Sea Surface Station
            {
                if (modifiers.containsKey(Modifiers.W_DTG_1)) {
                    strText = modifiers.get(Modifiers.W_DTG_1);
                    if (strText != null) {
                        ti = new TextInfo(strText + " - ", 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));
                        y = (int)(bounds.getMinY() + labelHeight);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.W1_DTG_2)) {
                    strText = modifiers.get(Modifiers.W1_DTG_2);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));
                        y = (int)(bounds.getMinY() + (labelHeight * 2));

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() + (bounds.getWidth() + bufferXR));
                        y = (int)(bounds.getMinY() + (labelHeight * 3));

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if(ec == 217000)//Shore Control Station
            {
                if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)) {
                    strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes upper right of center
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getX() + (bounds.getWidth() * 0.5) - (labelWidth/2));
                        y = (int)(bounds.getY() + bounds.getHeight() + labelHeight + bufferY);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 250600)//Known Point
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                        //One modifier symbols and modifier goes upper right of center
                        x = (int)(bounds.getX() + (bounds.getWidth() + bufferXR));
//                  x = x - (labelBounds.width * 0.5);
                        y = (int)(bounds.getY() + (bounds.getHeight() * 0.25f));
                        y = y + (int) (labelHeight * 0.5f);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
        }
        else if(ss == SymbolID.SymbolSet_Atmospheric)
        {
            String modX = null;
            if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                modX = (modifiers.get(Modifiers.X_ALTITUDE_DEPTH));

            if(ec == 162300)//Freezing Level
            {
                strText = "0" + (char)(176) + ":";
                if(modX != null)
                    strText += modX;
                else
                    strText += "?";

                ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                y = y + (int) ((labelHeight - modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 162200)//tropopause Level
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                y = y + (int) ((labelHeight - modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 110102)//tropopause Low
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.5f));
                y = y - descent;

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 110202)//tropopause High
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (int) (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (int) (bounds.getHeight() * 0.5f));
                //y = y + (int) ((labelHeight * 0.5f) + (labelHeight/2));
                y = y + (int) (((labelHeight * 0.5f) - (labelHeight/2)) + labelHeight - descent);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="DOM Arrow">
        Point2D[] domPoints = null;
        Rectangle2D domBounds = null;

        if (modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT) &&
                SymbolUtilities.isCBRNEvent(symbolID))//CBRN events
        {
            strText = modifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT);
            if(strText != null && SymbolUtilities.isNumber(strText))
            {
                float q = Float.parseFloat(strText);
                Rectangle2D tempBounds = RectUtilities.copyRect(bounds);

                tempBounds = tempBounds.createUnion(new Rectangle2D.Double(bounds.getCenterX(), yForY,0,0));

                //boolean isY = modifiers.containsKey(Modifiers.Y_LOCATION);

                domPoints = createDOMArrowPoints(symbolID, tempBounds, sdi.getSymbolCenterPoint(), q, false, modifierFontHeight);

                domBounds = new Rectangle2D.Double(domPoints[0].getX(), domPoints[0].getY(), 1, 1);

                Point2D temp = null;
                for (int i = 1; i < 6; i++)
                {
                    temp = domPoints[i];
                    if (temp != null)
                    {
                        domBounds = domBounds.createUnion(new Rectangle2D.Double(temp.getX(), temp.getY(),0,0));
                    }
                }
                imageBounds = imageBounds.createUnion(domBounds);
            }
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">

        if (SymbolUtilities.hasFDI(symbolID))
        {
            //create feint indicator /\
            fdiLeft = new Point2D.Double(bounds.getX(), bounds.getY());
            fdiRight = new Point2D.Double((bounds.getX() + bounds.getWidth()), bounds.getY());
            fdiTop = new Point2D.Double(Math.round(bounds.getCenterX()), Math.round(bounds.getY() - (bounds.getWidth() * .5f)));


            fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());

            ti = new TextInfo("TEST", 0, 0, modifierFont, frc);
            if (ti != null && SymbolUtilities.isCBRNEvent(symbolID))
            {
                int shiftY = (int)Math.round(bounds.getY() - ti.getTextBounds().getHeight() - 2);
                fdiLeft.setLocation(fdiLeft.getX(), fdiLeft.getY() + shiftY);
                //fdiLeft.offset(0, shiftY);
                fdiTop.setLocation(fdiTop.getX(), fdiTop.getY() + shiftY);
                //fdiTop.offset(0, shiftY);
                fdiRight.setLocation(fdiRight.getX(), fdiRight.getY() + shiftY);
                //fdiRight.offset(0, shiftY);
                fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());
                //fdiBounds.offset(0, shiftY);
            }

            imageBounds = imageBounds.createUnion(fdiBounds);

        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rectangle2D modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds = modifierBounds.createUnion(tempShape.getTextOutlineBounds());
            }

        }

        if (modifierBounds != null || domBounds != null || fdiBounds != null)
        {

            if (modifierBounds != null)
            {
                imageBounds = imageBounds.createUnion(modifierBounds);
            }
            if (domBounds != null)
            {
                imageBounds = imageBounds.createUnion(domBounds);
            }
            if (fdiBounds != null)
            {
                imageBounds = imageBounds.createUnion(fdiBounds);
            }

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.getMinX() < 0 || imageBounds.getMinY() < 0))
            {
                int shiftX = Math.abs((int)imageBounds.getMinX());
                int shiftY = Math.abs((int)imageBounds.getMinY());

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                if(modifierBounds != null)
                    RectUtilities.shift(modifierBounds,shiftX, shiftY);

                if (domBounds != null)
                {
                    for (int i = 0; i < 6; i++)
                    {
                        Point2D temp = domPoints[i];
                        if (temp != null)
                        {
                            temp.setLocation(temp.getX() + shiftX, temp.getY() + shiftY);
                        }
                    }
                    RectUtilities.shift(domBounds,shiftX, shiftY);
                }

                //If there's an FDI
                if (fdiBounds != null)
                {
                    ShapeUtilities.offset(fdiBounds, shiftX, shiftY);
                    ShapeUtilities.offset(fdiLeft, shiftX, shiftY);
                    ShapeUtilities.offset(fdiTop, shiftX, shiftY);
                    ShapeUtilities.offset(fdiRight, shiftX, shiftY);
                }

                //shift image points
                centerPoint.setLocation(centerPoint.getX() + shiftX, centerPoint.getY() + shiftY);
                RectUtilities.shift(symbolBounds, shiftX, shiftY);
                RectUtilities.shift(imageBounds, shiftX, shiftY);
            }

            if (attributes.containsKey(MilStdAttributes.TextColor)) {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
            }
            if (attributes.containsKey(MilStdAttributes.TextBackgroundColor)) {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
            }
            textColor = RendererUtilities.setColorAlpha(textColor,alpha);
            textBackgroundColor = RendererUtilities.setColorAlpha(textBackgroundColor,alpha);

            if(sdi instanceof ImageInfo) {
                ii = (ImageInfo) sdi;
                //Render modifiers//////////////////////////////////////////////////
                BufferedImage bmp = new BufferedImage((int) imageBounds.getWidth(), (int) Math.round(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D) bmp.getGraphics();

                //render////////////////////////////////////////////////////////
                //draw original icon with potential modifiers.
                g2d.drawImage((Image) ii.getImage(), (int) symbolBounds.getX(), (int) symbolBounds.getY(), null);
                //ctx.drawBitmap(ii.getImage(), symbolBounds.getX(), symbolBounds.getY(), null);

                renderText(g2d, arrMods, textColor, textBackgroundColor);

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);

                lineColor = RendererUtilities.setColorAlpha(lineColor,alpha);
                //draw DOM arrow
                if (domBounds != null)
                {
                    drawDOMArrow(g2d, domPoints, lineColor, strokeWidth);
                }

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    float[] dashArray = {6f, 4f};

                    g2d.setColor(lineColor);

                    /*if (symbolBounds.getHeight() < 20) {
                        dashArray[0] = 5f;
                        dashArray[1] = 3f;
                    }//*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    dashArray[1] = (int)((distance / 14f) * 2);
                    dashArray[0] = (int)((distance / 14f) * 3);//*/
                    /// //////////////////////////////////

                    BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
                    g2d.setStroke(stroke);
                    Path2D fdiPath = new Path2D.Double();

                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());

                    g2d.draw(fdiPath);

                    fdiBounds = null;

                }
                //</editor-fold>

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
                g2d = null;
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
                String svgStrokeWidth = String.valueOf(strokeWidth);//"3";
                String svgAlpha = null;
                if(alpha > -1)
                    svgAlpha = String.valueOf(alpha);
                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sbSVG = new StringBuilder();
                sbSVG.append(ssi.getSVG());
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor, modifierFont));

                // <editor-fold defaultstate="collapsed" desc="DOM arrow">
                if (domBounds != null)
                {
                    Path2D domPath = new Path2D.Double() ;

                    domPath.moveTo(domPoints[0].getX(), domPoints[0].getY());
                    if (domPoints[1] != null)
                    {
                        domPath.lineTo(domPoints[1].getX(), domPoints[1].getY());
                    }
                    if (domPoints[2] != null)
                    {
                        domPath.lineTo(domPoints[2].getX(), domPoints[2].getY());
                    }
                    sbSVG.append(Shape2SVG.Convert(domPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null,null));

                    domPath.reset();

                    domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
                    domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
                    domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
                    sbSVG.append(Shape2SVG.Convert(domPath, "none", svgStroke, "0", svgAlpha, svgAlpha, null,null));

                    domBounds = null;
                    domPoints = null;
                }
                // </editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    String svgFDIDashArray = "6 4";
                    float[] dashArray = {6f,4f};

                    /*if (symbolBounds.getHeight() < 20)
                    {
                        svgFDIDashArray = "5 3";
                    }//*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    dashArray[1] = (int)((distance / 14f) * 2);
                    dashArray[0] = (int)((distance / 14f) * 3);
                    svgFDIDashArray = "" + dashArray[0] + " " + dashArray[1];
                    /// //////////////////////////////////

                    Path2D fdiPath = new Path2D.Double();
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                    sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray,"round"));
                }
                //</editor-fold>

                newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
            }

            // <editor-fold defaultstate="collapsed" desc="Cleanup">

            // </editor-fold>

            return newsdi;

        }
        else
        {
            return null;
        }
        // </editor-fold>

    }

    /**
     * Process modifiers for action points
     */
    public static SymbolDimensionInfo ProcessTGSPModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, Color lineColor, FontRenderContext frc)
    {

        // <editor-fold defaultstate="collapsed" desc="Variables">
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        Font modifierFont = getFont(attributes);
        float[] hd = getFontHeightandDescent(modifierFont);
        float modifierFontHeight = hd[0];
        float modifierFontDescent = hd[1];

        int bufferXL = 6;
        int bufferXR = 4;
        int bufferY = 2;
        int bufferText = 2;
        int centerOffset = 1; //getCenterX/Y function seems to go over by a pixel
        int x = 0;
        int y = 0;
        int x2 = 0;
        int y2 = 0;

        //Feint Dummy Indicator variables
        Rectangle2D fdiBounds = null;
        Point2D fdiTop = null;
        Point2D fdiLeft = null;
        Point2D fdiRight = null;

        int outlineOffset = RS.getTextOutlineWidth();
        int labelHeight = 0;
        int labelWidth = 0;
        float alpha = -1;
        SymbolDimensionInfo newsdi = null;

        Color textColor = lineColor;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> arrMods = new ArrayList<TextInfo>();
        boolean duplicate = false;

        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);


        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }

        Rectangle2D bounds = RectUtilities.copyRect(sdi.getSymbolBounds());
        Rectangle2D symbolBounds = RectUtilities.copyRect((sdi.getSymbolBounds()));
        Point centerPoint = new Point(sdi.getSymbolCenterPoint());
        Rectangle2D imageBounds = RectUtilities.copyRect((sdi.getImageBounds()));

        centerPoint = new Point(Math.round(sdi.getSymbolCenterPoint().x), Math.round(sdi.getSymbolCenterPoint().y));

        boolean byLabelHeight = false;

        labelHeight = Math.round(modifierFontHeight + 0.5f);
        int maxHeight = (int)(symbolBounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (modifierFontDescent + 0.5f);
        int yForY = -1;

        Rectangle2D labelBounds1 = null;//text.getPixelBounds(null, 0, 0);
        Rectangle2D labelBounds2 = null;
        String strText = "";
        String strText1 = "";
        String strText2 = "";
        TextInfo text1 = null;
        TextInfo text2 = null;

        String basicID = SymbolUtilities.getBasicSymbolID(symbolID);

        if (outlineOffset > 2)
        {
            outlineOffset = ((outlineOffset - 1) / 2);
        }
        else
        {
            outlineOffset = 0;
        }

        /*bufferXL += outlineOffset;
         bufferXR += outlineOffset;
         bufferY += outlineOffset;
         bufferText += outlineOffset;*/
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Process Modifiers">
        TextInfo ti = null;

        {
            if (msi.getModifiers().contains(Modifiers.N_HOSTILE) && modifiers.containsKey(Modifiers.N_HOSTILE))
            {
                strText = modifiers.get(Modifiers.N_HOSTILE);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                    x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);

                    if (!byLabelHeight)
                    {
                        y = (int)((bounds.getHeight() / 3) * 2);//checkpoint, get box above the point
                        y = (int)bounds.getMinY() + y;
                    }
                    else
                    {
                        //y = ((labelHeight + bufferText) * 3);
                        //y = bounds.y + y - descent;
                        y = (int)(bounds.getMinY() + bounds.getHeight());
                    }

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }

            }
            if (msi.getModifiers().contains(Modifiers.H_ADDITIONAL_INFO_1) && modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                strText = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                    x = x - (int) (labelWidth * 0.5f);
                    y = (int)bounds.getMinY() - descent;

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.H1_ADDITIONAL_INFO_2) && modifiers.containsKey(Modifiers.H1_ADDITIONAL_INFO_2))
            {
                strText = modifiers.get(Modifiers.H1_ADDITIONAL_INFO_2);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                    x = x - (int) (labelWidth * 0.5);
                    y = (int)(bounds.getMinY() + labelHeight - descent + (bounds.getHeight() * 0.07));

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.A_SYMBOL_ICON))
            {
                if(modifiers.containsKey(Modifiers.A_SYMBOL_ICON))
                    strText = modifiers.get(Modifiers.A_SYMBOL_ICON);
                else if (SymbolID.getEntityCode(symbolID)==321706)//NATO Multiple Supply Class Point
                    strText = "ALL?";//make it clear the required 'A' value wasn't set for this symbol.

                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                    x = x - (int) (labelWidth * 0.5);
                    y = (int)(bounds.getMinY() + labelHeight - descent + (bounds.getHeight() * 0.07));

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.W_DTG_1) && modifiers.containsKey(Modifiers.W_DTG_1))
            {
                strText = modifiers.get(Modifiers.W_DTG_1);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                    y = (int)(bounds.getMinY() + labelHeight - descent);

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.W1_DTG_2) && modifiers.containsKey(Modifiers.W1_DTG_2))
            {
                strText = modifiers.get(Modifiers.W1_DTG_2);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    x = (int)bounds.getMinX() - labelWidth - bufferXL;

                    y = ((labelHeight - descent + bufferText) * 2);
                    y = (int)bounds.getMinY() + y;

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.T_UNIQUE_DESIGNATION_1) && modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
            {
                strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);

                    x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                    y = (int)bounds.getMinY() + labelHeight - descent;

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }
            if (msi.getModifiers().contains(Modifiers.T1_UNIQUE_DESIGNATION_2) && modifiers.containsKey(Modifiers.T1_UNIQUE_DESIGNATION_2))
            {
                strText = modifiers.get(Modifiers.T1_UNIQUE_DESIGNATION_2);
                if(strText != null)
                {
                    ti = new TextInfo(strText, 0, 0, modifierFont, frc);
                    labelWidth = (int)Math.round(ti.getTextBounds().getWidth());

                    //points
                    x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5));
                    x = x - (int) (labelWidth * 0.5);
                    //y = bounds.y + (bounds.getHeight * 0.5);

                    y = (int) ((bounds.getHeight() * 0.55));//633333333
                    y = (int)bounds.getMinY() + y;

                    ti.setLocation(x, y);
                    arrMods.add(ti);
                }
            }

            // <editor-fold defaultstate="collapsed" desc="Build Feint Dummy Indicator">

            if (SymbolUtilities.hasFDI(symbolID))
            {
                //create feint indicator /\
                fdiLeft = new Point2D.Double(bounds.getX(), bounds.getY());
                fdiRight = new Point2D.Double((bounds.getX() + bounds.getWidth()), bounds.getY());
                fdiTop = new Point2D.Double(Math.round(bounds.getCenterX()), Math.round(bounds.getY() - (bounds.getWidth() * .5f)));


                fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());

                ti = new TextInfo("TEST", 0, 0, modifierFont, frc);
                if (ti != null)
                {
                    int shiftY = (int)Math.round(bounds.getY() - ti.getTextBounds().getHeight() - 2);
                    fdiLeft.setLocation(fdiLeft.getX(), fdiLeft.getY() + shiftY);
                    //fdiLeft.offset(0, shiftY);
                    fdiTop.setLocation(fdiTop.getX(), fdiTop.getY() + shiftY);
                    //fdiTop.offset(0, shiftY);
                    fdiRight.setLocation(fdiRight.getX(), fdiRight.getY() + shiftY);
                    //fdiRight.offset(0, shiftY);
                    fdiBounds = new Rectangle2D.Double(fdiLeft.getX(), fdiTop.getY(), fdiRight.getX() - fdiLeft.getX(), fdiLeft.getY() - fdiTop.getY());
                    //fdiBounds.offset(0, shiftY);
                }

                imageBounds = imageBounds.createUnion(fdiBounds);

            }
            // </editor-fold>

        }

        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Shift Points and Draw">
        Rectangle2D modifierBounds = null;
        if (arrMods != null && arrMods.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = arrMods.get(0).getTextOutlineBounds();
            int size = arrMods.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = arrMods.get(i);
                modifierBounds = modifierBounds.createUnion(tempShape.getTextOutlineBounds());
            }

        }

        if(fdiBounds != null)
        {
            if(modifierBounds != null)
                modifierBounds = modifierBounds.createUnion(fdiBounds);
            else
                modifierBounds = fdiBounds;
        }


        if (modifierBounds != null)
        {

            imageBounds = imageBounds.createUnion(modifierBounds);

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.getMinX() < 0 || imageBounds.getMinY() < 0))
            {
                int shiftX = (int)Math.abs(imageBounds.getMinX());
                int shiftY = (int)Math.abs(imageBounds.getMinY());

                //shift mobility points
                int size = arrMods.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = arrMods.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                RectUtilities.shift(modifierBounds,shiftX, shiftY);

                //shift image points
                centerPoint.setLocation(centerPoint.getX() + shiftX, centerPoint.getY() + shiftY);
                RectUtilities.shift(symbolBounds,shiftX, shiftY);
                RectUtilities.shift(imageBounds,shiftX, shiftY);

                //If there's an FDI
                if (fdiBounds != null)
                {
                    ShapeUtilities.offset(fdiBounds, shiftX, shiftY);
                    ShapeUtilities.offset(fdiLeft, shiftX, shiftY);
                    ShapeUtilities.offset(fdiTop, shiftX, shiftY);
                    ShapeUtilities.offset(fdiRight, shiftX, shiftY);
                }
            }

            if (attributes.containsKey(MilStdAttributes.TextColor)) {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
            }
            if (attributes.containsKey(MilStdAttributes.TextBackgroundColor)) {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
            }
            textColor = RendererUtilities.setColorAlpha(textColor,alpha);
            textBackgroundColor = RendererUtilities.setColorAlpha(textBackgroundColor,alpha);

            if(sdi instanceof ImageInfo) {
                ii = (ImageInfo) sdi;
                //Render modifiers//////////////////////////////////////////////////
                BufferedImage bmp = new BufferedImage((int) imageBounds.getWidth(), (int) Math.round(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D) bmp.getGraphics();

                //draw original icon with potential modifiers.
                g2d.drawImage((Image) ii.getImage(), (int) symbolBounds.getX(), (int) symbolBounds.getY(), null);
                //ctx.drawBitmap(ii.getImage(), symbolBounds.getX(), symbolBounds.getY(), null);
                //ctx.drawImage(ii.getImage(),imageBoundsOld.left,imageBoundsOld.top);

                renderText(g2d, arrMods, textColor, textBackgroundColor);

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null) {
                    float[] dashArray = {6f, 4f};

                    g2d.setColor(lineColor);

                    /*if (symbolBounds.getHeight() < 20) {
                        dashArray[0] = 5f;
                        dashArray[1] = 3f;
                    }//*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    dashArray[1] = (int)((distance / 14f) * 2);
                    dashArray[0] = (int)((distance / 14f) * 3);//*/
                    /// //////////////////////////////////

                    BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
                    g2d.setStroke(stroke);
                    Path2D fdiPath = new Path2D.Double();

                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());

                    g2d.draw(fdiPath);

                    fdiBounds = null;

                }
                //</editor-fold>

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
                g2d = null;
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                String svgStroke = RendererUtilities.colorToHexString(lineColor,false);
                String svgStrokeWidth = "3";
                String svgAlpha = null;
                if(alpha > -1)
                    svgAlpha = String.valueOf(alpha);
                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sbSVG = new StringBuilder();
                sbSVG.append(ssi.getSVG());
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor, modifierFont));

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    String svgFDIDashArray = "6 4";
                    float[] dashArray = {6f,4f};

                    /*if (symbolBounds.getHeight() < 20)
                    {
                        svgFDIDashArray = "5 3";
                    }*/

                    /// ///////////////////////////////////
                    //Divide line in 14 parts. line is 3 parts to 2 parts gap
                    float distance = RendererUtilities.getDistanceBetweenPoints(fdiTop,fdiLeft);
                    //distance = distance / 14f;
                    dashArray[1] = (int)((distance / 14f) * 2);
                    dashArray[0] = (int)((distance / 14f) * 3);
                    svgFDIDashArray = "" + dashArray[0] + " " + dashArray[1];//*/
                    /// //////////////////////////////////

                    Path2D fdiPath = new Path2D.Double();
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                    sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray,"round"));
                }
                //</editor-fold>

                newsdi = new SVGSymbolInfo(sbSVG.toString(),centerPoint,symbolBounds,imageBounds);
            }

            // <editor-fold defaultstate="collapsed" desc="Cleanup">


            // </editor-fold>
        }
        // </editor-fold>
        return newsdi;

    }

    private static SymbolDimensionInfo shiftUnitPointsAndDraw(ArrayList<TextInfo> tiArray, SymbolDimensionInfo sdi, Map<String,String> attributes, Font modifierFont)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;
        SymbolDimensionInfo newsdi = null;

        int alpha = -1;

        if (attributes != null && attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Integer.parseInt(attributes.get(MilStdAttributes.Alpha));
        }

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        Rectangle2D symbolBounds = sdi.getSymbolBounds();
        Point centerPoint = sdi.getSymbolCenterPoint();
        Rectangle2D imageBounds = sdi.getImageBounds();
        Rectangle2D imageBoundsOld = sdi.getImageBounds();

        Rectangle2D modifierBounds = null;
        if (tiArray != null && tiArray.size() > 0)
        {

            //build modifier bounds/////////////////////////////////////////
            modifierBounds = tiArray.get(0).getTextOutlineBounds();
            int size = tiArray.size();
            TextInfo tempShape = null;
            for (int i = 1; i < size; i++)
            {
                tempShape = tiArray.get(i);
                Rectangle2D.union(modifierBounds, tempShape.getTextOutlineBounds(), modifierBounds);
            }

        }

        if (modifierBounds != null)
        {

            Rectangle2D.union(imageBounds, modifierBounds, imageBounds);

            //shift points if needed////////////////////////////////////////
            if (sdi instanceof ImageInfo && (imageBounds.getX() < 0 || imageBounds.getY() < 0))
            {
                int shiftX = (int)Math.round(Math.abs(imageBounds.getX())),
                        shiftY = (int)Math.round(Math.abs(imageBounds.getY()));

                //shift mobility points
                int size = tiArray.size();
                TextInfo tempShape = null;
                for (int i = 0; i < size; i++)
                {
                    tempShape = tiArray.get(i);
                    tempShape.shift(shiftX, shiftY);
                }
                RectUtilities.shift(modifierBounds, shiftX, shiftY);
                //modifierBounds.shift(shiftX,shiftY);

                //shift image points
                centerPoint.setLocation(centerPoint.getX() + shiftX, centerPoint.getY() + shiftY);
                RectUtilities.shift(symbolBounds, shiftX, shiftY);
                RectUtilities.shift(imageBounds, shiftX, shiftY);
                RectUtilities.shift(imageBoundsOld, shiftX, shiftY);
                /*centerPoint.shift(shiftX, shiftY);
                 symbolBounds.shift(shiftX, shiftY);
                 imageBounds.shift(shiftX, shiftY);
                 imageBoundsOld.shift(shiftX, shiftY);//*/
            }

            if (attributes.containsKey(MilStdAttributes.TextColor)) {
                textColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextColor));
            }
            if (attributes.containsKey(MilStdAttributes.TextBackgroundColor)) {
                textBackgroundColor = RendererUtilities.getColorFromHexString(attributes.get(MilStdAttributes.TextBackgroundColor));
            }
            textColor = RendererUtilities.setColorAlpha(textColor,alpha);
            textBackgroundColor = RendererUtilities.setColorAlpha(textBackgroundColor,alpha);

            if(sdi instanceof ImageInfo) {
                ii = (ImageInfo) sdi;
                BufferedImage bmp = new BufferedImage((int) imageBounds.getWidth(), (int) Math.round(imageBounds.getHeight()), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D) bmp.getGraphics();
                g2d.setFont(modifierFont);
                //old
                //Bitmap bmp = Bitmap.createBitmap(imageBounds.getWidth(), imageBounds.getheight(), Config.ARGB_8888);
                //Canvas ctx = new Canvas(bmp);
                //old

                //render////////////////////////////////////////////////////////
                //draw original icon with potential modifiers.
                g2d.drawImage(ii.getImage(), (int) imageBoundsOld.getX(), (int) imageBoundsOld.getY(), null);
                //ctx.drawBitmap(ii.getImage(), imageBoundsOld.getX(), imageBoundsOld.getY(), null);
                //ctx.drawImage(ii.getImage(),imageBoundsOld.getX(),imageBoundsOld.getY());

                renderText(g2d, tiArray, textColor, textBackgroundColor);

                newsdi = new ImageInfo(bmp, centerPoint, symbolBounds);
            }
            else if(sdi instanceof SVGSymbolInfo)
            {
                ssi = (SVGSymbolInfo)sdi;
                StringBuilder sb = new StringBuilder();
                sb.append(ssi.getSVG());
                sb.append(renderTextElements(tiArray,textColor,textBackgroundColor, modifierFont));
                newsdi = new SVGSymbolInfo(sb.toString(),centerPoint,symbolBounds,imageBounds);
            }

        }
        return newsdi;
    }

    private static String renderTextElement(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor, Font modifierFont)
    {
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
                svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = String.valueOf(RendererSettings.getInstance().getSVGTextOutlineWidth());
        for (TextInfo ti : tiArray) {
            sbSVG.append(Shape2SVG.Convert(ti, svgStroke,svgFill,svgStrokeWidth,null,null,null));
            sbSVG.append("\n");
        }

        return sbSVG.toString();
    }

    private static String renderTextElements(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor, Font modifierFont)
    {
        String style = null;
        String name = modifierFont.getName();//RendererSettings.getInstance().getLabelFont().getFamily() + ", sans-serif";//"SansSerif";
        if(!name.endsWith("serif"))
            name += ", sans-serif";
        String size = String.valueOf(modifierFont.getSize());//String.valueOf(RendererSettings.getInstance().getLabelFont().getSize());
        String weight = null;
        String anchor = null;//"start";
        if(modifierFont.isBold())
            weight = "bold";
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
            svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = String.valueOf(RendererSettings.getInstance().getSVGTextOutlineWidth());
        sbSVG.append("\n<g");
        sbSVG.append(" font-family=\"").append(name).append('"');
        sbSVG.append(" font-size=\"").append(size).append("px\"");
        if(weight != null)
            sbSVG.append(" font-weight=\"").append(weight).append("\"");
        sbSVG.append(" alignment-baseline=\"alphabetic\"");//
        sbSVG.append(">");

        for (TextInfo ti : tiArray) {
            sbSVG.append(Shape2SVG.ConvertForGroup(ti, svgStroke,svgFill,svgStrokeWidth,null,null,null));
            sbSVG.append("\n");
        }
        sbSVG.append("</g>\n");

        return sbSVG.toString();
    }
    private static void renderText(Graphics2D g2d, ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        renderText(g2d, (TextInfo[]) tiArray.toArray(new TextInfo[0]), color, backgroundColor);
    }

    /**
     *
     * @param g2d
     * @param tiArray
     * @param color
     * @param backgroundColor
     */
    public static void renderText(Graphics2D g2d, TextInfo[] tiArray, Color color, Color backgroundColor)
    {
        /*for (TextInfo textInfo : tiArray)
         {
         ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, modifierFont);
         }*/

        int size = tiArray.length;

        int tbm = RendererSettings.getInstance().getTextBackgroundMethod();
        int outlineWidth = RendererSettings.getInstance().getTextOutlineWidth();

        //outlineWidth = RendererUtilities.getRecommendedTextOutlineWidth();


        if (color == null)
        {
            color = Color.BLACK;
        }

        Color outlineColor = null;

        if(backgroundColor != null)
            outlineColor = backgroundColor;
        else
            outlineColor = RendererUtilities.getIdealOutlineColor(color);

        if(color.getAlpha() != 255 && outlineColor.getAlpha()==255)
            outlineColor = RendererUtilities.setColorAlpha(outlineColor,color.getAlpha()/255f);

        //g2d.setFont(RendererSettings.getInstance().getLabelFont());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
        {
            //draw text outline
            //modifierFont.setStyle(Style.FILL);
//            modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
//            modifierFont.setColor(outlineColor.toInt());
            if (outlineWidth > 0)
            {
                g2d.setColor(outlineColor);
                for (int i = 0; i < size; i++)
                {
                    TextInfo textInfo = tiArray[i];
                    if (outlineWidth > 0)
                    {
                        for (int j = 1; j <= outlineWidth; j++)
                        {
                            /*if (j % 2 != 0)
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() - j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() - j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() + j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() + j);
                                
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY());
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY());
                            }
                            else
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY());
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY());
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() + j);
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() - j);
                            }//*/
                            if (j == 1)
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() - j);//TL
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() - j);//TR
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() + j);//LL
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() + j);//LR
                                
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY());//L
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY());//
                            }
                            else
                            {
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() - j);//TL
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() - j);//TR
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY() + j);//LL
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY() + j);//LR
                                
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() - j, (float)textInfo.getLocation().getY());//L
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX() + j, (float)textInfo.getLocation().getY());//

                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() + j);//T
                                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY() - j);//B
                            }
                        }
                    }
                }
            }
            //draw text
            g2d.setColor(color);

            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE)
        {
             //TODO: compare performance against TextBackgroundMethod_OUTLINE_QUICK
            FontRenderContext frc = g2d.getFontRenderContext();
            Font font = RendererSettings.getInstance().getLabelFont();
            AffineTransform at;

            //Glyph Vector Method
            /*for (int k = 0; k < size; k++)
            {                
                TextInfo textInfo = tiArray[k];
                GlyphVector gv = font.createGlyphVector(frc, textInfo.getText());
                at = new AffineTransform();
                at.translate(textInfo.getLocation().getX(),textInfo.getLocation().getY());
                g2d.setTransform(at);
                g2d.setColor(outlineColor);
                //g2d.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setStroke(new BasicStroke(4,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,3));
                g2d.draw(gv.getOutline());
            }
            g2d.setTransform(new AffineTransform());//*/

            //TextLayout Method
            for (int k = 0; k < size; k++)
            {
                TextInfo textInfo = tiArray[k];
                TextLayout tl = new TextLayout(textInfo.getText(), RendererSettings.getInstance().getLabelFont(), frc);
                at = new AffineTransform();
                at.translate(textInfo.getLocation().getX(),textInfo.getLocation().getY());
                Shape shape = tl.getOutline(null);

                at.translate(-0.5,0);//minor offset because outline seems to lean down and right
                g2d.setTransform(at);
                g2d.setColor(outlineColor);
                //g2d.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setStroke(new BasicStroke(outlineWidth,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,3));

                g2d.draw(shape);
                g2d.setTransform(new AffineTransform());
            }//*/

            g2d.setColor(color);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_COLORFILL)
        {
            g2d.setColor(outlineColor);

            //draw rectangle
            for (int k = 0; k < size; k++)
            {
                TextInfo textInfo = tiArray[k];

                g2d.fill(textInfo.getTextOutlineBounds());
            }
            //draw text
            g2d.setColor(color);

            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
        else if (tbm == RendererSettings.TextBackgroundMethod_NONE)
        {
            g2d.setColor(color);
            for (int j = 0; j < size; j++)
            {
                TextInfo textInfo = tiArray[j];
                g2d.drawString(textInfo.getText(), (float)textInfo.getLocation().getX(), (float)textInfo.getLocation().getY());
            }
        }
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return int[] where {xposition (-1 left, 0 centered, 1 right), yposition (0 centered, 1+ goes up, 1- goes down),
     * centered (0-no, 1-yes)} -999 means passed modifier is not supported by this symbol
     */
    private static List<Modifier> getLabelPositionIndexes(String symbolID, Map<String,String> modifiers, Map<String,String> attributes)
    {
        List<Modifier> mods = null;
        if(modifiers != null && !modifiers.isEmpty())
            mods = new ArrayList<>();
        else
            return null;

        int ver = SymbolID.getVersion(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        int x = 0;
        int y = 0;
        boolean centered = true;
        int p = RendererSettings.getInstance().getSPModifierPlacement();
        boolean strict = (RendererSettings.getInstance().getSPModifierPlacement() == RendererSettings.ModifierPlacement_STRICT);
        if(attributes != null && attributes.containsKey(MilStdAttributes.ModifierPlacement))
        {
            String mp = attributes.get(MilStdAttributes.ModifierPlacement);
            if(mp.equals("0") || mp.equals("1") || mp.equals("2"))
            {
                p = Integer.parseInt(mp);
                if(p == 0)
                    strict = true;
                else
                    strict = false;
            }
        }
        String temp = null;
        String sep = " ";
        if(ss == SymbolID.SymbolSet_DismountedIndividuals) {
            ver = SymbolID.Version_2525E;
        }

        if(ver < SymbolID.Version_2525E)
        {
            if(ss == SymbolID.SymbolSet_LandUnit ||
                    ss == SymbolID.SymbolSet_LandCivilianUnit_Organization)
            {

                //Only Command & Control has AA; ec.equals("110000").  Always in the middle of the unit.
                if(modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ))
                {
                    temp = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AA", temp, 0, 0, true));
                }

                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier(Modifiers.J_EVALUATION_RATING, temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandEquipment ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Land)
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;

                    temp = modifiers.get(Modifiers.AS_COUNTRY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.N_HOSTILE) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.N_HOSTILE))
                        temp += modifiers.get(Modifiers.N_HOSTILE) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J L N P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1) || modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;

                    temp = modifiers.get(Modifiers.AS_COUNTRY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X Y", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 1;//above center

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W AR", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Air ||
                    ss == SymbolID.SymbolSet_AirMissile ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Air)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                                modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //No left side labels

            }
            else if(ss == SymbolID.SymbolSet_Space ||
                    ss == SymbolID.SymbolSet_SpaceMissile ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Space)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;
                    if(!strict &&
                            !(modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //No left side labels
            }
            else if(ss == SymbolID.SymbolSet_SeaSurface ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSurface)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = true;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp += modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;
                    if(!strict &&
                            !(modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) || modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 3;//above center
                    if(!strict)
                        y--;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp = modifiers.get(Modifiers.AQ_GUARDED_UNIT) + sep;
                    if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        temp += modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AQ AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSubsurface ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = -1;//below center

                    temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)))
                        y++;
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -3;//below center
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                            y++;
                        if(!modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                            y++;
                    }

                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 3;//above center
                    if(!strict)
                    {
                        y--;
                    }

                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_Activities)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;

                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        y++;
                    temp = temp = modifiers.get(Modifiers.J_EVALUATION_RATING);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !modifiers.containsKey(Modifiers.Y_LOCATION))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_CyberSpace)
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("K L", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = true;

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 0;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }
                else if (!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.W_DTG_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1) + sep;
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp += modifiers.get(Modifiers.V_EQUIP_TYPE);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T V", temp, x, y, centered));
                }

            }
            /*else if(ver == SymbolID.SymbolSet_MineWarfare)
            {
                //no modifiers
            }//*/
            //else//SymbolSet Unknown
                //processUnknownTextModifiers
        }
        else// if(ver >= SymbolID.Version_2525E)
        {
            int fs = SymbolID.getFrameShape(symbolID);
            if(ss == SymbolID.SymbolSet_LandUnit ||
                    ss == SymbolID.SymbolSet_LandCivilianUnit_Organization ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_LandUnit))
            {

                //Only Command & Control has AA; ec.equals("110000").  Always in the middle of the unit.
                if(modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ))
                {
                    temp = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AA", temp, 0, 0, true));
                }

                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K L P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.C_QUANTITY))
                        temp = modifiers.get(Modifiers.C_QUANTITY) + sep;
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp += modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandEquipment ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_LandEquipment))
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if( modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = -1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H AF", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                            modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER)))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K L P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                        modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                        temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AD AE", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                //No top center label

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    //if no "H', bring G and M closer to the center
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp += modifiers.get(Modifiers.AQ_GUARDED_UNIT);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G AQ", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left
                centered = false;

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.C_QUANTITY))
                        temp = modifiers.get(Modifiers.C_QUANTITY) + sep;
                    if(modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
                        temp += modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);

                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C AE", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.C_QUANTITY) || modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME)))
                        y++;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Space ||
                    ss == SymbolID.SymbolSet_SpaceMissile ||
                    ss == SymbolID.SymbolSet_Air ||
                    ss == SymbolID.SymbolSet_AirMissile ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence &&
                            (fs == SymbolID.FrameShape_Space || fs == SymbolID.FrameShape_Air)))
            {
                //No top center label
                x = 0;//centered
                y = 9;//on top of symbol

                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    temp = modifiers.get(Modifiers.C_QUANTITY);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }
                else if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }


                //Do right side labels
                x = 1;//on right
                centered = true;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//
                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AF", temp, x, y, centered));
                }
                else
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;//above center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                        temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS)  ||
                        modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)  ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -1;//below center
                    temp = "";
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp = modifiers.get(Modifiers.P_IFF_SIF_AIS) + sep;
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp = modifiers.get(Modifiers.Z_SPEED);

                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P X Z", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) ||
                        modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;//below center
                    if(!(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS)  ||
                            modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)  ||
                            modifiers.containsKey(Modifiers.Z_SPEED)))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1) + sep;
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp += modifiers.get(Modifiers.J_EVALUATION_RATING);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H J", temp, x, y, centered));
                }

                //No left side labels
                x = -1;//on right
                centered = true;

                if(modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
                {
                    y = 0;//
                    temp = temp += modifiers.get(Modifiers.AD_PLATFORM_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AD", temp, x, y, centered));
                }
                else
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        y--;
                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSurface ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_SeaSurface))
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION) ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp = modifiers.get(Modifiers.Y_LOCATION) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp += modifiers.get(Modifiers.Z_SPEED);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y Z", temp, x, y, centered));
                }

                //No left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) ||
                        modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;
                    if(!strict)
                    {
                        y--;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                        temp = modifiers.get(Modifiers.AQ_GUARDED_UNIT) + sep;
                    if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                        temp += modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AQ AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_SeaSubsurface ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_SeaSubsurface))
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                centered = false;

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        y--;
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 3;
                    if(!strict)
                    {
                        if(!modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                            y--;
                        if(!modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                            y--;
                    }

                    temp = modifiers.get(Modifiers.AS_COUNTRY );

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("P", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                        modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -2;//below center
                    if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        y++;

                    temp = "";
                    if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                        temp = modifiers.get(Modifiers.G_STAFF_COMMENTS) + sep;
                    if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        temp += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    temp = temp.trim();

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION) ||
                        modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -3;
                    if(!strict)
                    {
                        if(!(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)  ||
                                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1)))
                            y++;
                        if(!modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                            y++;
                    }
                    temp = "";
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp = modifiers.get(Modifiers.Y_LOCATION) + sep;
                    if(modifiers.containsKey(Modifiers.Z_SPEED))
                        temp += modifiers.get(Modifiers.Z_SPEED);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y Z", temp, x, y, centered));
                }

                //No left side labels
                x = -1;
                centered = false;
                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("X", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                {
                    y = 2;
                    if(!strict && !modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                    {
                        y--;
                    }
                    temp = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AR", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_DismountedIndividuals)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING) ||
                        modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) ||
                        modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                        temp = modifiers.get(Modifiers.J_EVALUATION_RATING) + sep;
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp += modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
                        temp += modifiers.get(Modifiers.P_IFF_SIF_AIS);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J K P", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                        modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = "";
                    if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                        temp = modifiers.get(Modifiers.V_EQUIP_TYPE) + sep;
                    if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
                        temp += modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V AF", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                        temp = modifiers.get(Modifiers.X_ALTITUDE_DEPTH) + sep;
                    if(modifiers.containsKey(Modifiers.Y_LOCATION))
                        temp += modifiers.get(Modifiers.Y_LOCATION);

                    temp = temp.trim();
                    mods.add(new Modifier("X Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.Z_SPEED))
                {
                    y = -2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y++;
                    temp = modifiers.get(Modifiers.Z_SPEED);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Z", temp, x, y, centered));
                }
            }
            else if(ss == SymbolID.SymbolSet_Activities)
            {
                //No top center label


                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)))
                        y--;
                    temp = modifiers.get(Modifiers.AS_COUNTRY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                        y++;
                    temp = modifiers.get(Modifiers.J_EVALUATION_RATING);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("J", temp, x, y, centered));
                }

                //Do left side labels
                x = -1;//on left

                if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.C_QUANTITY))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.C_QUANTITY);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("C", temp, x, y, centered));
                }

            }
            else if(ss == SymbolID.SymbolSet_CyberSpace ||
                    (ss == SymbolID.SymbolSet_SignalsIntelligence && fs == SymbolID.FrameShape_Cyberspace))
            {
                //Do top center label
                x = 0;//centered
                y = 9;//on top of symbol
                if(modifiers.containsKey(Modifiers.B_ECHELON))
                {
                    temp = modifiers.get(Modifiers.B_ECHELON);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("B", temp, x, y, centered));
                }

                //Do right side labels
                x = 1;//on right
                if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side
                    temp = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("H", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                {
                    y = 1;//above center
                    temp = modifiers.get(Modifiers.G_STAFF_COMMENTS);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("G", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) || modifiers.containsKey(Modifiers.AS_COUNTRY))
                {
                    y = 2;
                    if(!strict && !(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS)))
                        y--;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
                        temp = modifiers.get(Modifiers.F_REINFORCED_REDUCED) + sep;
                    if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                        temp += modifiers.get(Modifiers.AS_COUNTRY);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("F AS", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.M_HIGHER_FORMATION);
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("M", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                {
                    y = -2;
                    if(!strict && !modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
                        y++;
                    temp = "";
                    if(modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
                        temp = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS) + sep;
                    if(modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
                        temp += modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
                    temp = temp.trim();
                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("K L", temp, x, y, centered));
                }

                //Do left side labels
                x=-1;
                if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                {
                    y = 0;//center
                    centered = true;//vertically centered, only matters for labels on left and right side

                    temp = modifiers.get(Modifiers.V_EQUIP_TYPE);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("V", temp, x, y, centered));
                }
                else if(!strict)
                {
                    centered = false;
                }

                if(modifiers.containsKey(Modifiers.Y_LOCATION))
                {
                    y = 1;
                    temp = modifiers.get(Modifiers.Y_LOCATION);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("Y", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.W_DTG_1))
                {
                    y = 2;//above center
                    if(!strict && !(modifiers.containsKey(Modifiers.Y_LOCATION)))
                        y--;

                    temp = modifiers.get(Modifiers.W_DTG_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("W", temp, x, y, centered));
                }

                if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                {
                    y = -1;//below center
                    temp = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                    if(temp != null && !temp.isEmpty())
                        mods.add(new Modifier("T", temp, x, y, centered));
                }
            }
            /*else if(ver == SymbolID.SymbolSet_MineWarfare)
            {
                //no modifiers
            }//*/
            //else//SymbolSet Unknown
            //processUnknownTextModifiers
        }

        return mods;
    }


    /**
     * @param bounds             bounds of the core icon
     * @param labelWidth         height of the label to be placed
     * @param location           -1 left, 0 center, 1 right
     * @param modifierFontHeight
     * @returns
     */
    private static double getLabelXPosition(Rectangle2D bounds, int labelWidth, int location, float modifierFontHeight)
    {
        double x = 0;
        int buffer = (int)modifierFontHeight/2;

        if(location == 1)//on right
        {
            x = bounds.getX() + bounds.getWidth() + buffer;
        }
        else if(location == -1)//left
        {
            x = bounds.getX() - labelWidth - buffer;
        }
        else if(location == 0)
        {
            x = (int)Math.round((bounds.getX() + (bounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
        }

        return x;
    }


    /**
     *
     * @param bounds bounds of the core icon
     * @param labelHeight height of the label to be placed
     * @param descent descent of the label to be placed
     * @param bufferText spacing buffer if desired
     * @param centered if true, there will be a center label location identified by 0
     * @param location positive 1, 2, 3 to be above symbol mid-point or negative values to be below
     * @returns y position
     */
    private static double getLabelYPosition(Rectangle bounds, int labelHeight, int descent, int bufferText, boolean centered, int location) {
        double y = 0;
        if (bounds != null && !bounds.isEmpty())
        {
            if(centered)
            {
                switch (location)
                {
                    case 3://3 above center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText) * 3);
                        y = bounds.getY() + y;
                        break;
                    case 2://2 above center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText) * 2);
                        y = bounds.getY() + y;
                        break;
                    case 1://1 above center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y - ((labelHeight + bufferText));
                        y = bounds.getY() + y;
                        break;
                    case 0: //centered
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + ((labelHeight - descent) * 0.5));
                        y = bounds.getY() + y;
                        break;
                    case -1://1 below center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText - descent));
                        y = bounds.getY() + y;
                        break;
                    case -2://2 below center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText) * 2) - (descent);
                        y = bounds.getY() + y;
                        break;
                    case -3://3 below center
                        y = (bounds.getHeight());
                        y = ((y * 0.5) + (labelHeight * 0.5));
                        y = y + ((labelHeight + bufferText) * 3) - (descent);
                        y = bounds.getY() + y;
                        break;
                }
            }
            else//split between top and bottom
            {
                switch (location)
                {
                    case 3:
                        y = (bounds.getY() + ((bounds.getHeight() / 2) - descent - labelHeight*2 - bufferText));
                        break;
                    case 2:
                        y = (bounds.getY() + ((bounds.getHeight() / 2) - descent - labelHeight - bufferText));
                        break;
                    case 1:
                        y = (bounds.getY() + ((bounds.getHeight() / 2) - descent));
                        break;
                    case -1:
                        y = (bounds.getY() + (bounds.getHeight() / 2) + (labelHeight - descent + bufferText));
                        break;
                    case -2:
                        y = (bounds.getY() + (bounds.getHeight() / 2) + ((labelHeight*2 - descent + bufferText)));
                        break;
                    case -3:
                        y = (bounds.getY() + (bounds.getHeight() / 2) + ((labelHeight*3 - descent + bufferText)));
                        break;
                }
            }
            if(location == 9)//on top of symbol
            {
                y = (int)Math.round(bounds.getY() - bufferText - descent);
            }
        }
        return y;
    }

    /**
     * Currently, modifiers are based on Symbol Set.
     * The exception is SIGINT which required a frame shape value in 2525E+
     * The SSMC couldn't come to an agreement on if frame shape should dictate modifiers.
     * Currently, I'm keeping it tied to Symbol Set.
     * @param symbolID
     * @return
     */
    private static boolean isCOnTop(String symbolID)
    {
        boolean onTop = false;

        int version = SymbolID.getVersion(symbolID);
        int ss = SymbolID.getSymbolSet(symbolID);
        char frame = SymbolID.getFrameShape(symbolID);

        if(SymbolUtilities.hasModifier(symbolID,Modifiers.C_QUANTITY)) {

            if(version >= SymbolID.Version_2525E)
            {

                if (ss == SymbolID.SymbolSet_Air ||
                        ss == SymbolID.SymbolSet_AirMissile ||
                        ss == SymbolID.SymbolSet_Space ||
                        ss == SymbolID.SymbolSet_SpaceMissile ||
                        ss == SymbolID.SymbolSet_LandEquipment)
                {
                    onTop = true;
                }
                else if(ss == SymbolID.SymbolSet_SignalsIntelligence &&
                        (frame == SymbolID.FrameShape_Air ||
                        frame == SymbolID.FrameShape_Space ||
                        frame == SymbolID.FrameShape_LandEquipment || frame == SymbolID.FrameShape_LandUnit || frame == '0'))
                {
                    onTop = true;
                }

            }// else if <= SymbolID.Version_2525Dch1
            else if (ss == SymbolID.SymbolSet_LandEquipment ||
                    ss == SymbolID.SymbolSet_SignalsIntelligence_Land)
            {
                onTop = true;
            }
        }
        return onTop;
    }

    /**
     *
     * @param symbolID
     * @param modifiers
     * @return
     */
    public static boolean hasDisplayModifiers(String symbolID, Map<String,String> modifiers)
    {
        boolean hasModifiers = false;
        int ss = SymbolID.getSymbolSet(symbolID);
        int status = SymbolID.getStatus(symbolID);
        int context = SymbolID.getContext(symbolID);

        if(ss == SymbolID.SymbolSet_ControlMeasure)//check control measure
        {
            if (SymbolUtilities.isCBRNEvent(symbolID) == true && modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT))
            {
                hasModifiers = true;
            }
            else if(SymbolUtilities.hasFDI(symbolID))
                hasModifiers = true;
        }
        else if(ss != SymbolID.SymbolSet_Atmospheric &&
                ss != SymbolID.SymbolSet_Oceanographic &&
                ss != SymbolID.SymbolSet_MeteorologicalSpace)
        {//checking units

            if(context > 0) //Exercise or Simulation
                hasModifiers = true;

            //echelon or mobility,
            if (SymbolID.getAmplifierDescriptor(symbolID) > 0 || modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT))
                hasModifiers = true;

            if(modifiers.containsKey(Modifiers.AJ_SPEED_LEADER))
                hasModifiers = true;

            if(modifiers.containsKey(Modifiers.AO_ENGAGEMENT_BAR))
                hasModifiers = true;

            //HQ/Taskforce
            if(SymbolID.getHQTFD(symbolID) > 0)
                hasModifiers = true;

            if(status > 1)//Fully capable, damaged, destroyed
                hasModifiers = true;
        }//no display modifiers for single point weather



        return hasModifiers;
    }

    public static boolean hasTextModifiers(String symbolID, Map<String,String> modifiers)
    {

        int ss = SymbolID.getSymbolSet(symbolID);
        int ec = SymbolID.getEntityCode(symbolID);
        if(ss == SymbolID.SymbolSet_Atmospheric)
        {
            switch(ec)
            {
                case 110102: //tropopause low
                case 110202: //tropopause high
                case 162200: //tropopause level ?
                case 162300: //freezing level ?
                    return true;
                default:
                    return false;
            }
        }
        else if(ss == SymbolID.SymbolSet_Oceanographic || ss == SymbolID.SymbolSet_MeteorologicalSpace)
        {
            return false;
        }
        else if (ss == SymbolID.SymbolSet_ControlMeasure)
        {
            MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);

            if( msi.getModifiers().size() > 0 && modifiers != null && modifiers.size() > 0)
                return true;
            else
                return false;
        }
        else
        {

            if (SymbolUtilities.getStandardIdentityModifier(symbolID) != null)
            {
                return true;
            }

            int cc = SymbolID.getCountryCode(symbolID);
            if (cc > 0 && !GENCLookup.getInstance().get3CharCode(cc).equals(""))
            {
                return true;
            }//*/

            else if (modifiers.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Rerturns either the default font from RendererSettings or font based on properties
     * set in MilStdAttributes.
     * @param attributes
     * @return
     */
    private static Font getFont(Map<String,String> attributes)
    {
        Font f = null;

        String ff = RendererSettings.getInstance().getLabelFontName();
        int fstyle = RendererSettings.getInstance().getLabelFontType();
        int fsize = RendererSettings.getInstance().getLabelFontSize();
        String temp = null;


        if(attributes.containsKey(MilStdAttributes.FontFamily) ||
                attributes.containsKey(MilStdAttributes.FontStyle) ||
                attributes.containsKey(MilStdAttributes.FontSize))
        {
            if(attributes.containsKey(MilStdAttributes.FontStyle))
            {
                temp = attributes.get(MilStdAttributes.FontStyle);
                if (temp != null && !temp.isEmpty()) {
                    fstyle = Integer.parseInt(temp);
                }
            }
            if(attributes.containsKey(MilStdAttributes.FontSize))
            {
                temp = attributes.get(MilStdAttributes.FontSize);
                if (temp != null && !temp.isEmpty()) {
                    fsize = Integer.parseInt(temp);
                }
            }
            if(attributes.containsKey(MilStdAttributes.FontFamily))
            {
                temp = attributes.get(MilStdAttributes.FontFamily);
                if (temp != null && !temp.isEmpty()) {
                    ff =  temp;//Typeface.create(temp,fstyle);
                }
            }
        }
        else
            return _modifierFont;

        try
        {
            f = new Font(ff, fstyle, fsize);
        }
        catch(Exception exc)
        {
            String message = "font creation error, returning \"" + ff + "\" font, " + fsize + "pt. Check font name and type.";
            ErrorLogger.LogMessage("ModifierRenderer", "getFont", message);
            ErrorLogger.LogMessage("ModifierRenderer", "getFont", exc.getMessage());
            return new Font("arial", Font.BOLD, 12);
        }

        return f;

    }

    private static float[] getFontHeightandDescent(Font font)
    {
        float[] hd = {0f,0f};

        if(font != null)
        {
            FontRenderContext frc = new FontRenderContext(null, true, false);
            LineMetrics lm = font.getLineMetrics("Tj",frc);
            hd[0] = (float)Math.ceil(lm.getHeight());
            hd[1] = (float)Math.ceil(lm.getDescent());
        }

        return hd;
    }

    private static float[] getFontHeightandDescent(Font font, FontMetrics fm)
    {
        float[] hd = {0f,0f};

        if(fm == null)
        {

            BufferedImage bmp = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bmp.createGraphics();
            fm = g2d.getFontMetrics(font);
            //hd = getFontHeightandDescent(font, fm);
            hd[0] = fm.getHeight();
            hd[1] = fm.getMaxDescent();
            g2d.dispose();
            bmp = null;
            g2d = null;

        }

        /*if(font != null && fm != null)
        {
            hd[0] = fm.getHeight();
            hd[1] = fm.getMaxDescent();
        }//*/

        return hd;
    }

}
