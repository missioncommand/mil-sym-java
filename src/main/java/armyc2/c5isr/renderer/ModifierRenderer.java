package armyc2.c5isr.renderer;


import java.awt.*;
import java.awt.font.FontRenderContext;
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
    private static float _modifierFontDescent = 2f;


    private static BufferedImage _bmp = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        
        

    @Override
    public void SettingsEventChanged(String type)
    {
        if(type.equals(SettingsChangedEvent.EventType_FontChanged))
        {
            _modifierFont = RendererSettings.getInstance().getLabelFont();
            
            Graphics2D _g2d = _bmp.createGraphics();
            FontMetrics fm = _g2d.getFontMetrics(_modifierFont);

            _modifierFontHeight = fm.getHeight();
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

        if(pixelSize <= 100)
            strokeWidth=2.0f;
        else
            strokeWidth=2 + ((pixelSize-100)/100f);

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
        if (intEchelon > 10 && intEchelon < 29 && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.B_ECHELON))
        {
            strEchelon = SymbolUtilities.getEchelonText(intEchelon);
        }
        if (strEchelon != null && SymbolUtilities.isInstallation(symbolID) == false
                && SymbolUtilities.hasModifier(symbolID, Modifiers.B_ECHELON))
        {

            int echelonOffset = 2,
                    outlineOffset = RS.getTextOutlineWidth();
            Font modifierFont = RS.getLabelFont();
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
        if (SymbolUtilities.isTaskForce(symbolID))
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
                && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AB_FEINT_DUMMY_INDICATOR))
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
            tiAO = new TextInfo(strAO, 0, 0, _modifierFont,frc);
            ebTextBounds = tiAO.getTextBounds();
            ebHeight = (int)ebTextBounds.getHeight() + 4;

            if(fdiBounds != null)//set bar above FDI if present
            {
                ebTop = (int)fdiBounds.getY() - ebHeight - 4;
            }
            else if(tfBounds != null)//set bar above TF if present
            {
                ebTop = (int)tfBounds.getY() - ebHeight - 4;
            }
            else if(echelonBounds != null)//set bar above echelon if present
            {
                ebTop = (int)echelonBounds.getY() - ebHeight - 4;
            }
            else if(SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.C_QUANTITY) &&
                    modifiers.containsKey(Modifiers.C_QUANTITY))
            {
                ebTop = (int)symbolBounds.getY() - ebHeight*2 - 4;
            }
            else if(ss == SymbolID.SymbolSet_LandInstallation)
            {
                ebTop = (int)symbolBounds.getY() - ebHeight - 8;
            }
            else//position above symbol
            {
                ebTop = (int)symbolBounds.getY() - ebHeight - 4;
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
            if (ebBounds != null
                    && ((ebBounds.getMinX() + ebBounds.getWidth() > symbolBounds.getMinX() + symbolBounds.getWidth())))
            {
                y = (int)Math.round(symbolBounds.getMinY() - amOffset);
                x = (int)(ebBounds.getMinX() + ebBounds.getWidth() + amOffset + RendererSettings.getInstance().getTextOutlineWidth());
            }
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
        if (modifiers.containsKey(Modifiers.Q_DIRECTION_OF_MOVEMENT) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.Q_DIRECTION_OF_MOVEMENT))
        {
            String strQ = modifiers.get(Modifiers.Q_DIRECTION_OF_MOVEMENT);

            if(strQ != null && SymbolUtilities.isNumber(strQ))
            {
                float q = Float.valueOf(strQ);

                boolean isY = (modifiers.containsKey(Modifiers.Y_LOCATION));

                domPoints = createDOMArrowPoints(symbolID, symbolBounds, centerPoint, q, isY,frc);

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
        int ociOffset = 4;
        if (SymbolUtilities.hasModifier(symbolID, Modifiers.AL_OPERATIONAL_CONDITION)) {
            if (mobilityBounds != null)
            {
                ociOffset = (int)Math.round((mobilityBounds.getY() + mobilityBounds.getHeight()) - (symbolBounds.getY() + symbolBounds.getHeight())) + 4;
            }
            if(RendererSettings.getInstance().getOperationalConditionModifierType() == RendererSettings.OperationalConditionModifierType_BAR) {
                ociShape = processOperationalConditionIndicator(symbolID, symbolBounds, ociOffset);
                if (ociShape != null) {
                    Rectangle2D temp = (Rectangle2D) ociShape.clone();
                    ShapeUtilities.grow(temp, 1);
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
        //
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
                sbSVG.append(Shape2SVG.Convert(temp, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));
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
                sbSVG.append(Shape2SVG.Convert(tfRectangle, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));
            }
            if(ebBounds != null)
            {
                String svgEBFill = RendererUtilities.colorToHexString(ebColor,false);
                //create fill and outline
                sbSVG.append(Shape2SVG.Convert(ebRectangle, svgStroke, svgEBFill, svgStrokeWidth, svgAlpha, svgAlpha, null));
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

                Path2D fdiPath = new Path2D.Double();
                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray));


            }
            if (liBounds != null)
            {
                int liStrokeWidth = 2;
                if(pixelSize < 100)
                    liStrokeWidth=1;
                sbSVG.append(Shape2SVG.Convert(liPath, svgStroke, null, String.valueOf(liStrokeWidth), svgAlpha, svgAlpha, null));
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
                sbSVG.append(Shape2SVG.Convert(ociBounds, null, svgStroke, svgStrokeWidth, svgAlpha, svgAlpha, null));
                sbSVG.append(Shape2SVG.Convert(ociShape, null, svgOCIStatusColor, svgStrokeWidth, svgAlpha, svgAlpha, null));

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

                sbSVG.append(Shape2SVG.Convert(mobilityPath, svgStroke, null, svgMobilitySW, svgAlpha, svgAlpha, null));

                if (mobilityPathFill != null)
                {
                    sbSVG.append(Shape2SVG.Convert(mobilityPathFill, "none", svgStroke, "0", svgAlpha, svgAlpha, null));
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

                sbSVG.append(Shape2SVG.Convert(ociSlashShape, svgStroke, null, String.valueOf(ociStrokeWidth), svgAlpha, svgAlpha, null));
                ociBounds = null;
                ociSlashShape = null;
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
                sbSVG.append(Shape2SVG.Convert(domPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));

                domPath.reset();

                domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
                domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
                domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
                sbSVG.append(Shape2SVG.Convert(domPath, "none", svgStroke, "0", svgAlpha, svgAlpha, null));

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
            g2d.setFont(RS.getLabelFont());

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

                stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
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
     * @param symbolID
     * @return
     * @deprecated no longer a thing in 2525D
     * TODO: remove
     */
    private static double getYPositionForSCC(String symbolID)
    {
        double yPosition = 0.32;
        /*int aff = SymbolID.getAffiliation(symbolID);
        int context = SymbolID.getContext(symbolID);
        char affiliation = symbolID.charAt(1);

        if(temp.equals("WMGC--"))//GROUND (BOTTOM) MILCO
        {
            if(affiliation == 'H' ||
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition = 0.34;
        }
        else if(temp.equals("WMMC--"))//MOORED MILCO
        {
            if(affiliation == 'H' ||
                    affiliation == 'S')//suspect
                yPosition = 0.25;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.25;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.25;
            else
                yPosition = 0.28;
        }
        else if(temp.equals("WMFC--"))//FLOATING MILCO
        {
            if(affiliation == 'H' ||
                    affiliation == 'S')//suspect
                yPosition = 0.29;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.32;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.32;
            else
                yPosition= 0.34;
        }
        else if(temp.equals("WMC---"))//GENERAL MILCO
        {
            if(affiliation == 'H' ||
                    affiliation == 'S')//suspect
                yPosition = 0.33;
            else if(affiliation == 'N' ||
                    affiliation == 'L')//exercise neutral
                yPosition = 0.36;
            else if(affiliation == 'F' ||
                    affiliation == 'A' ||//assumed friend
                    affiliation == 'D' ||//exercise friend
                    affiliation == 'M' ||//exercise assumed friend
                    affiliation == 'K' ||//faker
                    affiliation == 'J')//joker
                yPosition = 0.36;
            else
                yPosition = 0.36;
        }*/

        return yPosition;
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
    private static Point2D[] createDOMArrowPoints(String symbolID, Rectangle2D bounds, Point2D center, float angle, boolean isY, FontRenderContext frc)
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
        else if(bounds.getHeight() >= 100)
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
                SymbolUtilities.isCBRNEvent(symbolID) || SymbolUtilities.isLand(symbolID))
        {
            //drawStaff = true;
            if(SymbolUtilities.isHQ(symbolID)==false)//has HQ staff to start from
            {
                y1 = (int)(bounds.getY() + bounds.getHeight());
                pt1 = new Point.Double(x1, y1);

                if (isY == true && SymbolUtilities.isCBRNEvent(symbolID))//make room for y modifier
                {
                    int yModifierOffset = (int) _modifierFontHeight;

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

        //create arrowhead//////////////////////////////////////////////////////
        float arrowWidth = 10.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
                theta = 0.423f;//higher value == shorter arrow head//*/

        if (length < 50)
        {
            theta = 0.55f;
        }
        /*float arrowWidth = length * .09f,// 16.0f,//8.0f,//6.5f;//7.0f;//6.5f;//10.0f//default
         theta = length * .0025f;//0.423f;//higher value == shorter arrow head
         if(arrowWidth < 8)
         arrowWidth = 8f;//*/

        int[] xPoints = new int[3];//3
        int[] yPoints = new int[3];//3
        int[] vecLine = new int[2];//2
        int[] vecLeft = new int[2];//2
        double fLength;
        double th;
        double ta;
        double baseX, baseY;

        xPoints[0] = x2;
        yPoints[0] = y2;

        //build the line vector
        vecLine[0] = (xPoints[0] - x1);
        vecLine[1] = (yPoints[0] - y1);

        //build the arrow base vector - normal to the line
        vecLeft[0] = -vecLine[1];
        vecLeft[1] = vecLine[0];

        //setup length parameters
        fLength = Math.sqrt(vecLine[0] * vecLine[0] + vecLine[1] * vecLine[1]);
        th = arrowWidth / (2.0 * fLength);
        ta = arrowWidth / (2.0 * (Math.tan(theta) / 2.0) * fLength);

        //find base of the arrow
        baseX = (xPoints[0] - ta * vecLine[0]);
        baseY = (yPoints[0] - ta * vecLine[1]);

        //build the points on the sides of the arrow
        xPoints[1] = (int) Math.round(baseX + th * vecLeft[0]);
        yPoints[1] = (int) Math.round(baseY + th * vecLeft[1]);
        xPoints[2] = (int) Math.round(baseX - th * vecLeft[0]);
        yPoints[2] = (int) Math.round(baseY - th * vecLeft[1]);

        //line.lineTo((int)baseX, (int)baseY);
        pt3 = new Point2D.Double(Math.round(baseX), Math.round(baseY));

        //arrowHead = new Polygon(xPoints, yPoints, 3);
        arrowPoints[0] = pt1;//new Point2D.Double(pt1.getX(), pt1.getY());
        arrowPoints[1] = pt2;//new Point2D.Double(pt2.getX(), pt2.getY());
        arrowPoints[2] = pt3;//new Point2D.Double(pt3.getX(), pt3.getY());
        arrowPoints[3] = new Point2D.Double(xPoints[0], yPoints[0]);
        arrowPoints[4] = new Point2D.Double(xPoints[1], yPoints[1]);
        arrowPoints[5] = new Point2D.Double(xPoints[2], yPoints[2]);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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
                //bounds = armyc2.c5isr.renderer.utilities.RendererUtilities.getTextOutlineBounds(_modifierFont, text, new SO.Point(0,0));
                tiTemp = new TextInfo(text, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
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
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    /**
     * @param sdi
     * @param symbolID
     * @param modifiers
     * @param attributes
     * @return
     */
    public static SymbolDimensionInfo  processLandUnitTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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

        int ss = SymbolID.getSymbolSet(symbolID);

        

        //check if text is too tall:
        boolean byLabelHeight = true;
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) && SymbolUtilities.hasModifier(symbolID, Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //just below center on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                y = (int)(bounds.getY() + (bounds.getHeight() / 2 + labelHeight + (bufferText/2) - descent));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //below T on left
                x = (int)bounds.getX() - labelWidth - bufferXL;
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 2)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = "";

            String jm = null,
                    km = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                pm = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
            }
            if (km != null && km.equals("") == false)
            {
                modifierValue = modifierValue + " " + km;
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //above X/Y on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = null,
                    F = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(Modifiers.F_REINFORCED_REDUCED);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
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

            if (AS != null && AS.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + AS;
                }
                else
                {
                    modifierValue = AS;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ) && SymbolUtilities.hasModifier(symbolID, Modifiers.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int) ((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));

                y = (int)(symbolBounds.getHeight());//checkpoint, get box above the point
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)symbolBounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo processLandUnitTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;
        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = RendererUtilities.getIdealOutlineColor(textColor);

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (_modifierFontDescent + 0.5);

        if (attributes.containsKey(MilStdAttributes.Alpha))
        {
            alpha = Float.parseFloat(attributes.get(MilStdAttributes.Alpha))/255f;
        }

        Rectangle2D labelBounds = null;
        int labelWidth, labelHeight;

        Rectangle bounds = RectUtilities.toRectangle(sdi.getSymbolBounds());
        Rectangle2D symbolBounds = (Rectangle2D)(sdi.getSymbolBounds().clone());
        Point2D centerPoint = sdi.getSymbolCenterPoint();
        //Rectangle2D imageBounds = new Rectangle(0,0, sdi.getImage().getWidth(), sdi.getImage().getHeight());
        Rectangle2D imageBounds = new Rectangle((int)sdi.getImageBounds().getX(),(int)sdi.getImageBounds().getY(), (int)sdi.getImageBounds().getWidth(), (int)sdi.getImageBounds().getHeight());
        Rectangle2D imageBoundsOld = (Rectangle2D)imageBounds.clone();

        String echelonText = SymbolUtilities.getEchelonText(SymbolID.getAmplifierDescriptor(symbolID));
        String amText = SymbolUtilities.getStandardIdentityModifier(symbolID);

        //adjust width of bounds for mobility/echelon/engagement bar which could be wider than the symbol
        bounds = RectUtilities.toRectangle(imageBounds.getX(), bounds.getY(), imageBounds.getWidth(), bounds.getHeight());

        int ss = SymbolID.getSymbolSet(symbolID);

        

        //check if text is too tall:
        boolean byLabelHeight = true;
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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
        //            int y1 = 0;//X/Y          G/AQ
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //just above V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
        {
            modifierValue = null;

            String gm = null,
                    aqm = null;

            if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) && SymbolUtilities.hasModifier(symbolID, Modifiers.G_STAFF_COMMENTS))
            {
                gm = modifiers.get(Modifiers.G_STAFF_COMMENTS);// xm = modifiers.X;
            }
            if (modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
            {
                aqm = modifiers.get(Modifiers.AQ_GUARDED_UNIT);// ym = modifiers.Y;
            }
            if (gm == null && aqm != null)
            {
                modifierValue = aqm;
            }
            else if (gm != null && aqm == null)
            {
                modifierValue = gm;
            }
            else if (gm != null && aqm != null)
            {
                modifierValue = gm + "  " + aqm;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            modifierValue = null;
            String hm = null;
            String afm = null;

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }
            if (hm == null && afm != null)
            {
                modifierValue = afm;
            }
            else if (hm != null && afm == null)
            {
                modifierValue = hm;
            }
            else if (hm != null && afm != null)
            {
                modifierValue = hm + "  " + afm;
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) || modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) || modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
        {
            modifierValue = "";

            String vm = null,
                    adm = null,
                    aem = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
            {
                vm = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
            {
                adm = modifiers.get(Modifiers.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                aem = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
            }
            if (vm != null && vm.equals("") == false)
            {
                modifierValue = modifierValue + vm;
            }
            if (adm != null && adm.equals("") == false)
            {
                modifierValue = modifierValue + " " + adm;
            }
            if (aem != null && aem.equals("") == false)
            {
                modifierValue = modifierValue + " " + aem;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont,frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //just below center on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //just below V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //below T on left
                x = (int)bounds.getX() - labelWidth - bufferXL;
                //below T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP)
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = "";

            String jm = null,
                    km = null,
                    lm = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                pm = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //above X/Y on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //above X/Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = null,
                    F = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(Modifiers.F_REINFORCED_REDUCED);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
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

            if (AS != null && AS.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + AS;
                }
                else
                {
                    modifierValue = AS;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AA_SPECIAL_C2_HQ) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.AA_SPECIAL_C2_HQ))
        {
            modifierValue = modifiers.get(Modifiers.AA_SPECIAL_C2_HQ);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int) ((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));

                y = (int)(symbolBounds.getHeight());//checkpoint, get box above the point
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)symbolBounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processAirSpaceUnitTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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



        //            int y0 = 0;//             T
        //            int y1 = 0;//             P
        //            int y2 =                  V
        //            int y3 = 0;//             Z/X
        //            int y4 = 0;//             G/H
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {

            String gm = "";
            String hm = "";
            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                gm = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

            modifierValue = gm + " " + hm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //on bottom
                y = (int)(bounds.getY() + bounds.getHeight());

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
        {
            modifierValue = "";
            String zm = "";
            String xm = "";
            if(modifiers.containsKey(Modifiers.Z_SPEED))
                zm = modifiers.get(Modifiers.Z_SPEED);

            if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                xm = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

            modifierValue = zm + " " + xm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //on bottom
                y = (int)(bounds.getY() + bounds.getHeight() - labelHeight);

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
        {
            modifierValue = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above Z
                y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 2));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if(SymbolUtilities.isAir(symbolID))
        {
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                modifierValue = modifiers.get(Modifiers.P_IFF_SIF_AIS);

                if(modifierValue != null && modifierValue.equals("") == false)
                {
                    tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                    labelBounds = tiTemp.getTextBounds();
                    labelWidth = (int)labelBounds.getWidth();

                    //right
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    //above Z
                    y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 3));

                    tiTemp.setLocation(x, y);
                    tiArray.add(tiTemp);

                }
            }

            if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
            {
                modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                if(modifierValue != null && modifierValue.equals("") == false)
                {
                    tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                    labelBounds = tiTemp.getTextBounds();
                    labelWidth = (int)labelBounds.getWidth();

                    //right
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    //above Z
                    y = (int)(bounds.getY()+ bounds.getHeight() - (labelHeight * 4));

                    tiTemp.setLocation(x, y);
                    tiArray.add(tiTemp);

                }
            }

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                modifierValue = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);

                if(modifierValue != null && modifierValue.equals("") == false)
                {
                    tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                    labelBounds = tiTemp.getTextBounds();
                    labelWidth = (int)labelBounds.getWidth();

                    //right
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    //above Z
                    y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 5));

                    tiTemp.setLocation(x, y);
                    tiArray.add(tiTemp);

                }
            }
        }
        else //space
        {
            if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
            {
                modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

                if(modifierValue != null && modifierValue.equals("") == false)
                {
                    tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                    labelBounds = tiTemp.getTextBounds();
                    labelWidth = (int)labelBounds.getWidth();

                    //right
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    //above Z
                    y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 3));

                    tiTemp.setLocation(x, y);
                    tiArray.add(tiTemp);

                }
            }

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                modifierValue = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);

                if(modifierValue != null && modifierValue.equals("") == false)
                {
                    tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                    labelBounds = tiTemp.getTextBounds();
                    labelWidth = (int)labelBounds.getWidth();

                    //right
                    x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                    //above Z
                    y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 4));

                    tiTemp.setLocation(x, y);
                    tiArray.add(tiTemp);

                }
            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processAirSpaceUnitTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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



        //            int y0 = 0;//             AS
        //            int y1 = 0;//             T
        //            int y2 =                  V
        //            int y3 = 0;//             X/Z
        //            int y4 = 0;//             G/H
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {

            String gm = "";
            String hm = "";
            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                gm = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

            modifierValue = gm + " " + hm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //on bottom
                y = (int)(bounds.getY() + bounds.getHeight());

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED) || modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
        {
            modifierValue = null;
            String zm = "";
            String xm = "";
            if(modifiers.containsKey(Modifiers.Z_SPEED))
                zm = modifiers.get(Modifiers.Z_SPEED);

            if(modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
                xm = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

            modifierValue = xm + " " + zm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //on bottom
                y = (int)(bounds.getY() + bounds.getHeight() - labelHeight);

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) || modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            modifierValue = null;
            String vm = "";
            String afm = "";

            if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                vm = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER) && SymbolID.getSymbolSet(symbolID)==SymbolID.SymbolSet_Air)
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);

            modifierValue = vm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above Z
                y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 2));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above Z
                y = (int)(bounds.getY() + bounds.getHeight() - (labelHeight * 3));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AS_COUNTRY) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String em = "";
            String asm = "";

            if(modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
                em = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
            if(modifiers.containsKey(Modifiers.AS_COUNTRY))
                asm = modifiers.get(Modifiers.AS_COUNTRY);

            modifierValue = em + " " + asm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above Z
                y = (int)(bounds.getY()+ bounds.getHeight() - (labelHeight * 4));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processLandEquipmentTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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


        //                                 C
        //            int y0 = 0;//W/AR         AS
        //            int y1 = 0;//X/Y          G/AQ
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M
        //            int y4 = 0;//Z            J/N/L/P
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        //if(Modifiers.C_QUANTITY in modifiers
        if (modifiers.containsKey(Modifiers.C_QUANTITY))
        {
            String text = modifiers.get(Modifiers.C_QUANTITY);
            if(text != null)
            {
                //bounds = armyc2.c5isr.renderer.utilities.RendererUtilities.getTextOutlineBounds(_modifierFont, text, new SO.Point(0,0));
                tiTemp = new TextInfo(text, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();
                x = (int)Math.round((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
                y = (int)Math.round(symbolBounds.getY() - bufferY - descent);
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

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just above V/AD/AE
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
        {
            modifierValue = "";
            String mg = "";
            String maq = "";

            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                mg = modifiers.get(Modifiers.G_STAFF_COMMENTS);
            if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                maq = modifiers.get(Modifiers.AQ_GUARDED_UNIT);

            modifierValue = mg + " " + maq;

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1) | modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            modifierValue = "";
            String hm = "",
                    afm = "";

            hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                hm = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just below V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        if (modifiers.containsKey(Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //below T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = (int) Math.round(bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP)//
                || modifiers.containsKey(Modifiers.N_HOSTILE)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    lm = null,
                    nm = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(Modifiers.N_HOSTILE) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.N_HOSTILE))
            {
                nm = modifiers.get(Modifiers.N_HOSTILE);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
            {
                pm = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            modifierValue = "";
            if (jm != null && jm.equals("") == false)
            {
                modifierValue = modifierValue + jm;
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H/AF
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1) ||
                modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
        {
            modifierValue = "";
            String mw = "";
            String mar = "";

            mw = modifiers.getOrDefault(Modifiers.W_DTG_1,"");

            if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                mar = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

            modifierValue = mw + " " + mar;

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //above X/Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String E = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G/AQ
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
        {
            String mv = null,
                    mad = null,
                    mae = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
            {
                mv = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
            {
                mad = modifiers.get(Modifiers.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                mae = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if (mv != null && mv.equals("") == false)
            {
                modifierValue = modifierValue + mv;
            }
            if (mad != null && mad.equals("") == false)
            {
                modifierValue = modifierValue + " " + mad;
            }
            if (mae != null && mae.equals("") == false)
            {
                modifierValue = modifierValue + " " + mae;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }



        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processLandEquipmentTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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


        //                                 C
        //            int y0 = 0;//W/           AS
        //            int y1 = 0;//X/Y          G/AQ
        //            int y2 = 0;//V/AD/AE      H/AF
        //            int y3 = 0;//T            M
        //            int y4 = 0;//Z            J/K/L/N/P
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        //if(Modifiers.C_QUANTITY in modifiers
        if (modifiers.containsKey(Modifiers.C_QUANTITY))
        {
            String text = modifiers.get(Modifiers.C_QUANTITY);
            if(text != null)
            {
                //bounds = armyc2.c5isr.renderer.utilities.RendererUtilities.getTextOutlineBounds(_modifierFont, text, new SO.Point(0,0));
                tiTemp = new TextInfo(text, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();
                x = (int)Math.round((symbolBounds.getX() + (symbolBounds.getWidth() * 0.5f)) - (labelWidth * 0.5f));
                y = (int)Math.round(symbolBounds.getY() - bufferY - descent);
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

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just above V/AD/AE
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
        {
            modifierValue = "";
            String mg = "";
            String maq = "";

            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                mg = modifiers.get(Modifiers.G_STAFF_COMMENTS);
            if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                maq = modifiers.get(Modifiers.AQ_GUARDED_UNIT);

            modifierValue = mg + " " + maq;

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
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
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
            {
                afm = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }

            modifierValue = hm + " " + afm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just below V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //below T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = (int) Math.round(bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    lm = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
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
                modifierValue = modifierValue + km;
            }
            if (lm != null && lm.equals("") == false)
            {
                modifierValue = modifierValue + " " + lm;
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);


            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //above X/Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE) ||
                modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
        {
            String mv = null,
                    mad = null,
                    mae = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
            {
                mv = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AD_PLATFORM_TYPE))
            {
                mad = modifiers.get(Modifiers.AD_PLATFORM_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
            {
                mae = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
            }

            modifierValue = "";
            if (mv != null && mv.equals("") == false)
            {
                modifierValue = modifierValue + mv;
            }
            if (mad != null && mad.equals("") == false)
            {
                modifierValue = modifierValue + " " + mad;
            }
            if (mae != null && mae.equals("") == false)
            {
                modifierValue = modifierValue + " " + mae;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.AS_COUNTRY) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = "";
            String E = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue += E;
            }

            if (AS != null && AS.equals("") == false)
            {
                modifierValue = modifierValue + " " + AS;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processLandInstallationTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

        int bufferXL = 7;
        int bufferXR = 7;
        int bufferY = 2;
        int bufferText = 2;
        int x = 0;
        int y = 0;//best y

        SymbolDimensionInfo newsdi = null;
        float alpha = -1;

        Color textColor = Color.BLACK;
        Color textBackgroundColor = null;

        ArrayList<TextInfo> tiArray = new ArrayList<TextInfo>(modifiers.size());

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //
        //            int y0 = 0;//
        //            int y1 = 0;//W            G
        //            int y2 = 0;//X/Y          H
        //            int y3 = 0;//T            J/K/P
        //            int y4 = 0;//
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = "";


            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }


            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just below V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = "";
            String mw = "";
            String mar = "";
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //above X/Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = null;
            String E = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) ||
                modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            String mx = null,
                    my = null;

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
            {
                mx = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
            }
            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                my = modifiers.get(Modifiers.Y_LOCATION);
            }


            modifierValue = "";
            if (mx != null && mx.equals("") == false)
            {
                modifierValue = modifierValue + mx;
            }
            if (my != null && my.equals("") == false)
            {
                modifierValue = modifierValue + " " + my;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processLandInstallationTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //
        //            int y0 = 0;// W            AS
        //            int y1 = 0;//X/Y           G/AQ
        //            int y2 = 0;//              H
        //            int y3 = 0;//AE            M
        //            int y4 = 0;//T             J/K/P
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) || modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
        {
            modifierValue = "";
            String mg = "";
            String maq = "";

            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                mg = modifiers.get(Modifiers.G_STAFF_COMMENTS);
            if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                maq = modifiers.get(Modifiers.AQ_GUARDED_UNIT);

            modifierValue = mg + " " + maq;

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }


            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME))
        {
            modifierValue = modifiers.get(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just below center
                y = (int)(bounds.getY() + (bounds.getHeight() / 2 + labelHeight + (bufferText/2) - descent));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //below AE
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 2)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = "";
            String mw = "";
            String mar = "";
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.AS_COUNTRY) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = "";
            String E = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue += E;
            }

            if (AS != null && AS.equals("") == false)
            {
                modifierValue = modifierValue + " " + AS;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) ||
                modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            String mx = null,
                    my = null;

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
            {
                mx = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
            }
            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                my = modifiers.get(Modifiers.Y_LOCATION);
            }


            modifierValue = "";
            if (mx != null && mx.equals("") == false)
            {
                modifierValue = modifierValue + mx;
            }
            if (my != null && my.equals("") == false)
            {
                modifierValue = modifierValue + " " + my;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //above vertical center
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }



        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processDismountedIndividualsTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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


        //
        //            int y0 = 0;//W/           AS
        //            int y1 = 0;//X/Y          G
        //            int y2 = 0;//V/AF         H
        //            int y3 = 0;//T            M
        //            int y4 = 0;//Z            J/K/P
        //
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;


        //if(Modifiers.X_ALTITUDE_DEPTH in modifiers || Modifiers.Y_LOCATION in modifiers)
        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) || modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            String xm = null,
                    ym = null;

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just above V/AD/AE
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = null;


            if(modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
                modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);


            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = null;
            String hm = "";

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);


            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getX() - labelBounds.getWidth() - bufferXL);
                //just below V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.Z_SPEED))
        {
            modifierValue = modifiers.get(Modifiers.Z_SPEED);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //below T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = (int) Math.round(bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING)
                || modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))//
        {
            modifierValue = null;

            String jm = null,
                    km = null,
                    pm = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                jm = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }
            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS) && SymbolUtilities.canSymbolHaveModifier(symbolID, Modifiers.P_IFF_SIF_AIS))
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
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = null;

            modifierValue = modifiers.get(Modifiers.W_DTG_1);


            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);
                //above X/Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) ||
                modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
        {
            String mv = null,
                    maf = null;

            if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
            {
                mv = modifiers.get(Modifiers.V_EQUIP_TYPE);
            }
            if (modifiers.containsKey(Modifiers.AF_COMMON_IDENTIFIER))
            {
                maf = modifiers.get(Modifiers.AF_COMMON_IDENTIFIER);
            }


            modifierValue = "";
            if (mv != null && mv.equals("") == false)
            {
                modifierValue = modifierValue + mv;
            }
            if (maf != null && maf.equals("") == false)
            {
                modifierValue = modifierValue + " " + maf;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX() - labelWidth - bufferXL);

                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.AS_COUNTRY) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
        {
            modifierValue = "";
            String E = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue += E;
            }

            if (AS != null && AS.equals("") == false)
            {
                modifierValue = modifierValue + " " + AS;
            }

            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processSeaSurfaceTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //            int y0 = 0;//AQ/AR        E/T
        //            int y1 = 0;//              V
        //            int y2 =                   P
        //            int y3 = 0;//             G/H
        //            int y4 = 0;//             Y/Z
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
        {
            modifierValue = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //just above P
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                modifierValue = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }



        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = "";
            String mg = "",
                    mh = "";

            if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
            {
                mg += modifiers.get(Modifiers.G_STAFF_COMMENTS);
            }

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                mh += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            modifierValue = mg + " " + mh;

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //just below P
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.Y_LOCATION)
                || modifiers.containsKey(Modifiers.Z_SPEED))//
        {
            modifierValue = null;

            String ym = "",
                    zm = "";

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                ym = modifiers.get(Modifiers.Y_LOCATION);
            }
            if (modifiers.containsKey(Modifiers.Z_SPEED))
            {
                zm = modifiers.get(Modifiers.Z_SPEED);
            }

            modifierValue = ym + " " + zm;

            modifierValue = modifierValue.trim();


            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //below G/H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = (int)Math.round(bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) ||
                modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
        {
            modifierValue = null;

            String maq = "",
                    mar = "";
            if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                maq = modifiers.get(Modifiers.AQ_GUARDED_UNIT);

            if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                mar = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

            modifierValue = maq + " " + mar;
            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX()- labelWidth - bufferXL);
                //across from T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;
                if(y <= bounds.getY() + labelHeight) //unless T is higher than top of the symbol
                {
                    y = (int)bounds.getY() + labelHeight;
                }


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = null;
            String E = null,
                    T = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
            {
                T = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
            }


            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (T != null && T.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + T;
                }
                else
                {
                    modifierValue = T;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above V
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }



        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processSeaSurfaceTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //                                      E/AS
        //            int y0 = 0;//AQ/AR        T
        //            int y1 = 0;//              V
        //            int y2 =                   P
        //            int y3 = 0;//             G/H
        //            int y4 = 0;//             Y/Z
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
        {
            modifierValue = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above vertical center
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                modifierValue = modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //just below center
                y = (int)(bounds.getY() + (bounds.getHeight() / 2 + labelHeight + (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }



        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = "";
            String mg = "",
                    mh = "";

            if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
            {
                mg += modifiers.get(Modifiers.G_STAFF_COMMENTS);
            }

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                mh += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            modifierValue = mg + " " + mh;

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //below P
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 2)));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.Y_LOCATION)
                || modifiers.containsKey(Modifiers.Z_SPEED))//
        {
            modifierValue = null;

            String ym = "",
                    zm = "";

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                ym = modifiers.get(Modifiers.Y_LOCATION);
            }
            if (modifiers.containsKey(Modifiers.Z_SPEED))
            {
                zm = modifiers.get(Modifiers.Z_SPEED);
            }

            modifierValue = ym + " " + zm;

            modifierValue = modifierValue.trim();


            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //below G/H
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 3)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT) ||
                modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
        {
            modifierValue = null;

            String maq = "",
                    mar = "";
            if(modifiers.containsKey(Modifiers.AQ_GUARDED_UNIT))
                maq = modifiers.get(Modifiers.AQ_GUARDED_UNIT);

            if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                mar = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

            modifierValue = maq + " " + mar;
            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX()- labelWidth - bufferXL);
                //oppoiste AS unless that's higher than the top of the symbol
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - (labelHeight * 2)));
                if(y <= bounds.getY() + labelHeight)
                {
                    y = (int)bounds.getY() + labelHeight - descent;
                }


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }


            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (AS != null && AS.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + AS;
                }
                else
                {
                    modifierValue = AS;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above V
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - (labelHeight * 2)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);


            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above V
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processSeaSubSurfaceTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {

        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //            int y0 = 0;//AR           T
        //            int y1 = 0;//             V
        //            int y2 =                  X
        //            int y3 = 0;//             G
        //            int y4 = 0;//             H
        //

        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {

            String em = "";
            String tm = "";
            if(modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
                em = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);

            if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                tm = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            modifierValue = em + " " + tm;
            modifierValue = modifierValue.trim();

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //on top
                y = (int)(bounds.getMinY() + labelHeight - descent);

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE) )
        {
            modifierValue = "";

            if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                modifierValue = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below T
                y = (int)(bounds.getMinY() - descent + (labelHeight * 2));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
        {
            modifierValue = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below V
                y = (int)(bounds.getMinY() - descent + (labelHeight * 3));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below X
                y = (int)(bounds.getMinY() - descent + (labelHeight * 4));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below G
                y = (int)(bounds.getMinY() - descent + (labelHeight * 5));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
        {
            modifierValue = "";

            if(modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
                modifierValue = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

            if(modifierValue != null && modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //on top
                y = (int)(bounds.getMinY() + labelHeight - descent);

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processSeaSubSurfaceTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //                                      E/AS
        //            int y0 = 0;//AQ/AR        T
        //            int y1 = 0;//              V
        //            int y2 =                   P
        //            int y3 = 0;//             G/H
        //            int y4 = 0;//             Y/Z
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;

        if (modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
        {
            modifierValue = modifiers.get(Modifiers.V_EQUIP_TYPE);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above vertical center
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH) ||
                modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
        {
            modifierValue = "";
            String mx = "",
                    mp = "";

            if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH))
            {
                modifierValue = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
            }

            if (modifiers.containsKey(Modifiers.P_IFF_SIF_AIS))
            {
                modifierValue += " " + modifiers.get(Modifiers.P_IFF_SIF_AIS);
            }

            modifierValue = modifierValue.trim();


            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //just below center
                y = (int)(bounds.getY() + (bounds.getHeight() / 2 + labelHeight + (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }



        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS) ||
                modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = "";
            String mg = "",
                    mh = "";

            if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
            {
                mg += modifiers.get(Modifiers.G_STAFF_COMMENTS);
            }

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                mh += modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            modifierValue = mg + " " + mh;

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //below P
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 2)));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.Y_LOCATION)
                || modifiers.containsKey(Modifiers.Z_SPEED))//
        {
            modifierValue = null;

            String ym = "",
                    zm = "";

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                ym = modifiers.get(Modifiers.Y_LOCATION);
            }
            if (modifiers.containsKey(Modifiers.Z_SPEED))
            {
                zm = modifiers.get(Modifiers.Z_SPEED);
            }

            modifierValue = ym + " " + zm;

            modifierValue = modifierValue.trim();


            if (modifierValue.length() > 2 && modifierValue.charAt(0) == ' ')
            {
                modifierValue = modifierValue.substring(1);
            }

            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //below G/H
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 3)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.AR_SPECIAL_DESIGNATOR))
        {
            modifierValue = modifiers.get(Modifiers.AR_SPECIAL_DESIGNATOR);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on left
                x = (int)(bounds.getX()- labelWidth - bufferXL);
                //oppoiste AS unless that's higher than the top of the symbol
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - (labelHeight * 2)));
                if(y <= bounds.getY() + labelHeight)
                {
                    y = (int)bounds.getY() + labelHeight - descent;
                }


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }


            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (AS != null && AS.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + AS;
                }
                else
                {
                    modifierValue = AS;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above V
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - (labelHeight * 2)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);


            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getX()+ bounds.getWidth() + bufferXR);
                //above V
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processActivitiesTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //            int y0 = 0;//W            E/AS
        //            int y1 = 0;//Y            G
        //            int y2 =                  H
        //            int y3 = 0;//             J
        //            int y4 = 0;//
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;


        if (modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                modifierValue = modifiers.get(Modifiers.Y_LOCATION);// ym = modifiers.Y;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getMinX() - labelBounds.getWidth() - bufferXL);
                y = (int)(bounds.getMinY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //just above center
                y = (int)(bounds.getMinY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //just below center
                y = (int)(bounds.getMinY() + (bounds.getHeight() / 2 + labelHeight + (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))//
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                modifierValue = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }


            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below H
                y = (int)(bounds.getMinY() + ((bounds.getHeight() / 2) + ((labelHeight - descent + bufferText) * 2)));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //above Y on left
                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = "",
                    AS = "";

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            modifierValue = E + " " + AS;
            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processActivitiesTextModifiersE(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //            int y0 = 0;//W            E/AS
        //            int y1 = 0;//Y            T
        //            int y2 =                  G
        //            int y3 = 0;//             H
        //            int y4 = 0;//             J
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;


        if (modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                modifierValue = modifiers.get(Modifiers.Y_LOCATION);// ym = modifiers.Y;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getMinX() - labelBounds.getWidth() - bufferXL);
                y = (int)(bounds.getMinY() + ((bounds.getHeight() / 2) - (bufferText/2) - descent));

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
        {
            modifierValue = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //T just above G (center)
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //G centered
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //H just below G (center)
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))//
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.J_EVALUATION_RATING))
            {
                modifierValue = modifiers.get(Modifiers.J_EVALUATION_RATING);
            }


            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //J below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = Math.round((int)bounds.getY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //above Y on left
                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - (labelHeight - bufferText) ));//android
                y = (int)(bounds.getY() + ((bounds.getHeight() / 2) - bufferText - descent - labelHeight));


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = "",
                    AS = "";

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            modifierValue = E + " " + AS;
            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //AS above T
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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

    public static SymbolDimensionInfo  processCyberSpaceTextModifiers(SymbolDimensionInfo sdi, String symbolID, Map<String,String> modifiers, Map<String,String> attributes, FontRenderContext frc)
    {
        ImageInfo ii = null;
        SVGSymbolInfo ssi = null;

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

        int descent = (int) (_modifierFontDescent + 0.5);

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
        labelHeight = (int) (_modifierFontHeight + 0.5);/* RendererUtilities.measureTextHeight(RendererSettings.getModifierFontName(),
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

        //            int y0 = 0;//             E/F/AS
        //            int y1 = 0;//W            G
        //            int y2 =     Y            H
        //            int y3 = 0;//T/V          M
        //            int y4 = 0;//             K/L
        // <editor-fold defaultstate="collapsed" desc="Build Modifiers">
        String modifierValue = null;
        TextInfo tiTemp = null;


        if (modifiers.containsKey(Modifiers.Y_LOCATION))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.Y_LOCATION))
            {
                modifierValue = modifiers.get(Modifiers.Y_LOCATION);// ym = modifiers.Y;
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getMinY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.G_STAFF_COMMENTS))
        {
            modifierValue = modifiers.get(Modifiers.G_STAFF_COMMENTS);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //on right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //just above H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getMinY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
        {
            modifierValue = null;

            if (modifiers.containsKey(Modifiers.H_ADDITIONAL_INFO_1))
            {
                modifierValue = modifiers.get(Modifiers.H_ADDITIONAL_INFO_1);
            }

            if(modifierValue != null && modifierValue.equals("") == false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //center
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + ((labelHeight - descent) * 0.5));
                y = (int)bounds.getMinY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) ||
                modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
        {
            modifierValue = "";

            String mt = "",
                    mv = "";

            if(modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1))
                mt = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);

            if(modifiers.containsKey(Modifiers.V_EQUIP_TYPE))
                mv = modifiers.get(Modifiers.V_EQUIP_TYPE);

            modifierValue = mt + " " + mv;
            modifierValue = modifierValue.trim();

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //just below center on left
                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //just below Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getMinY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
        {
            modifierValue = "";

            if (modifiers.containsKey(Modifiers.M_HIGHER_FORMATION))
            {
                modifierValue += modifiers.get(Modifiers.M_HIGHER_FORMATION);
            }

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //just below H
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText - descent));
                y = (int)bounds.getMinY() + y;

                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }

        if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS)//
                || modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))//
        {
            modifierValue = null;

            String km = null,
                    lm = null;

            if (modifiers.containsKey(Modifiers.K_COMBAT_EFFECTIVENESS))
            {
                km = modifiers.get(Modifiers.K_COMBAT_EFFECTIVENESS);
            }
            if (modifiers.containsKey(Modifiers.L_SIGNATURE_EQUIP))
            {
                lm = modifiers.get(Modifiers.L_SIGNATURE_EQUIP);
            }

            modifierValue = km + " " + lm;
            modifierValue = modifierValue.trim();

            if(modifierValue.equals("")==false)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //below M
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y + ((labelHeight + bufferText) * 2) - (descent * 2);
                y = (int)Math.round(bounds.getMinY() + y);


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }

        }

        if (modifiers.containsKey(Modifiers.W_DTG_1))
        {
            modifierValue = modifiers.get(Modifiers.W_DTG_1);

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //above X/Y on left
                x = (int)(bounds.getMinX() - labelWidth - bufferXL);
                //just above Y
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText));
                y = (int)bounds.getMinY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);
            }
        }

        if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED) ||
                modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER) ||
                modifiers.containsKey(Modifiers.AS_COUNTRY))
        {
            modifierValue = null;
            String E = null,
                    F = null,
                    AS = null;

            if (modifiers.containsKey(Modifiers.E_FRAME_SHAPE_MODIFIER))
            {
                E = modifiers.get(Modifiers.E_FRAME_SHAPE_MODIFIER);
                modifiers.remove(Modifiers.E_FRAME_SHAPE_MODIFIER);
            }
            if (modifiers.containsKey(Modifiers.F_REINFORCED_REDUCED))
            {
                F = modifiers.get(Modifiers.F_REINFORCED_REDUCED);
            }
            if (modifiers.containsKey(Modifiers.AS_COUNTRY))
            {
                AS = modifiers.get(Modifiers.AS_COUNTRY);
            }

            if (E != null && E.equals("") == false)
            {
                modifierValue = E;
            }

            if (F != null && F.equals("") == false)
            {
                if (F.toUpperCase(Locale.US) == ("R"))
                {
                    F = "(+)";
                }
                else if (F.toUpperCase(Locale.US) == ("D"))
                {
                    F = "(-)";
                }
                else if (F.toUpperCase(Locale.US) == ("RD"))
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

            if (AS != null && AS.equals("") == false)
            {
                if (modifierValue != null && modifierValue.equals("") == false)
                {
                    modifierValue = modifierValue + " " + AS;
                }
                else
                {
                    modifierValue = AS;
                }
            }

            if(modifierValue != null)
            {
                tiTemp = new TextInfo(modifierValue, 0, 0, _modifierFont, frc);
                labelBounds = tiTemp.getTextBounds();
                labelWidth = (int)labelBounds.getWidth();

                //right
                x = (int)(bounds.getMinX() + bounds.getWidth() + bufferXR);
                //above G
                y = (int)(bounds.getHeight());
                y = (int) ((y * 0.5) + (labelHeight * 0.5));
                y = y - ((labelHeight + bufferText) * 2);
                y = (int)bounds.getMinY() + y;


                tiTemp.setLocation(x, y);
                tiArray.add(tiTemp);

            }
        }


        // </editor-fold>

        //Shift Points and Draw
        newsdi = shiftUnitPointsAndDraw(tiArray,sdi,attributes);

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
        labelHeight = (int) (_modifierFontHeight + 0.5f);

        int maxHeight = (int)(symbolBounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (_modifierFontDescent + 0.5f);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getCenterX() - (int) (bounds.getWidth() * 0.15f));
                        x = x - (labelWidth);
                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.75f));
                        y = y + (int) (labelHeight * 0.5f);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1) &&
                        (ec == 160300 || ec == 132000)) 
                {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
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
                }


                if (strText != null) {
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

                    x = (int)(bounds.getCenterX() + (bounds.getWidth() * 0.15f));
//                  x = x - (labelbounds.getWidth * 0.5);
                    y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.25f));
                    y = y + (int) (labelHeight * 0.5f);

                    ti.setLocation(Math.round(x), Math.round(y));
                    arrMods.add(ti);
                }


            } 
            else if (ec == 132100)  //Key Terrain
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

                        //One modifier symbols and modifier goes right of center
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5 + bufferXR));

                        y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                        y = y + (int) ((labelHeight - descent) * 0.5f);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                        TextInfo ti2 = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                            ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                            TextInfo ti2 = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                        labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                        x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.7));
                        y = (int)bounds.getMinY() + labelHeight;// + (bounds.getHeight * 0.5);
                        //y = y + (labelHeight * 0.5);

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }

                }
            }
            else if (ec == 21060)  //TACAN
            {
                if (modifiers.containsKey(Modifiers.T_UNIQUE_DESIGNATION_1)) {
                    strText = modifiers.get(Modifiers.T_UNIQUE_DESIGNATION_1);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

                        //One modifier symbols and modifier goes top right of symbol
                        x = (int)(bounds.getMinX() - labelWidth - bufferXL);

                        y = (int)(bounds.getMinY() + labelHeight);
                        

                        ti.setLocation(Math.round(x), Math.round(y));
                        arrMods.add(ti);
                    }
                }
            } 
            else if (ec == 21060)  //Air Detonation
            {
                if (modifiers.containsKey(Modifiers.X_ALTITUDE_DEPTH)) {
                    strText = modifiers.get(Modifiers.X_ALTITUDE_DEPTH);
                    if (strText != null) {
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText + " - ", 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                        ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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

                ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                y = y + (int) ((labelHeight - _modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 162200)//tropopause Level
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
                labelWidth = (int)Math.round(ti.getTextBounds().getWidth());
                //One modifier symbols and modifier goes in center
                x = (int)(bounds.getMinX() + (bounds.getWidth() * 0.5f));
                x = x - (int) (labelWidth * 0.5f);
                y = (int)(bounds.getMinY() + (bounds.getHeight() * 0.5f));
                y = y + (int) ((labelHeight - _modifierFontDescent) * 0.5f);

                ti.setLocation(Math.round(x), Math.round(y));
                arrMods.add(ti);
            }
            else if(ec == 110102)//tropopause Low
            {
                strText = "X?";
                if(modX != null)
                    strText = modX;

                ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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

                ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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

                domPoints = createDOMArrowPoints(symbolID, tempBounds, sdi.getSymbolCenterPoint(), q, false, frc);

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

            ti = new TextInfo("TEST", 0, 0, _modifierFont, frc);
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
                centerPoint.setLocation(centerPoint.getX() + shiftX, centerPoint.getX() + shiftY);
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

                    if (symbolBounds.getHeight() < 20) {
                        dashArray[0] = 5f;
                        dashArray[1] = 3f;
                    }

                    BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
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
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor));

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
                    sbSVG.append(Shape2SVG.Convert(domPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, null));

                    domPath.reset();

                    domPath.moveTo(domPoints[3].getX(), domPoints[3].getY());
                    domPath.lineTo(domPoints[4].getX(), domPoints[4].getY());
                    domPath.lineTo(domPoints[5].getX(), domPoints[5].getY());
                    sbSVG.append(Shape2SVG.Convert(domPath, "none", svgStroke, "0", svgAlpha, svgAlpha, null));

                    domBounds = null;
                    domPoints = null;
                }
                // </editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    String svgFDIDashArray = "6 4";
                    float[] dashArray = {6f,4f};

                    if (symbolBounds.getHeight() < 20)
                    {
                        svgFDIDashArray = "5 3";
                    }

                    Path2D fdiPath = new Path2D.Double();
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                    sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray));
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

        labelHeight = Math.round(_modifierFontHeight + 0.5f);
        int maxHeight = (int)(symbolBounds.getHeight());
        if ((labelHeight * 3) > maxHeight)
        {
            byLabelHeight = true;
        }

        int descent = (int) (_modifierFontDescent + 0.5f);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);

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
                    ti = new TextInfo(strText, 0, 0, _modifierFont, frc);
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

                ti = new TextInfo("TEST", 0, 0, _modifierFont, frc);
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

                    if (symbolBounds.getHeight() < 20) {
                        dashArray[0] = 5f;
                        dashArray[1] = 3f;
                    }

                    BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashArray, 0.0f);
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
                sbSVG.append(renderTextElements(arrMods,textColor,textBackgroundColor));

                //<editor-fold defaultstate="collapsed" desc="Draw FDI">
                if (fdiBounds != null)
                {
                    String svgFDIDashArray = "6 4";
                    float[] dashArray = {6f,4f};

                    if (symbolBounds.getHeight() < 20)
                    {
                        svgFDIDashArray = "5 3";
                    }

                    Path2D fdiPath = new Path2D.Double();
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiLeft.getX(), fdiLeft.getY());
                    fdiPath.moveTo(fdiTop.getX(), fdiTop.getY());
                    fdiPath.lineTo(fdiRight.getX(), fdiRight.getY());//*/

                    sbSVG.append(Shape2SVG.Convert(fdiPath, svgStroke, null, svgStrokeWidth, svgAlpha, svgAlpha, svgFDIDashArray));
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

    private static SymbolDimensionInfo shiftUnitPointsAndDraw(ArrayList<TextInfo> tiArray, SymbolDimensionInfo sdi, Map<String,String> attributes)
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
                sb.append(renderTextElements(tiArray,textColor,textBackgroundColor));
                sb.append(ssi.getSVG());
                newsdi = new SVGSymbolInfo(sb.toString(),centerPoint,symbolBounds,imageBounds);
            }

        }
        return newsdi;
    }

    private static String renderTextElement(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
                svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = "2";//String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
        for (TextInfo ti : tiArray) {
            sbSVG.append(Shape2SVG.Convert(ti, svgStroke,svgFill,svgStrokeWidth,null,null,null));
            sbSVG.append("\n");
        }

        return sbSVG.toString();
    }

    private static String renderTextElements(ArrayList<TextInfo> tiArray, Color color, Color backgroundColor)
    {
        String style = null;
        String name = RendererSettings.getInstance().getLabelFont().getFontName() + ", sans-serif";//"SansSerif";
        String size = String.valueOf(RendererSettings.getInstance().getLabelFont().getSize());
        String weight = null;
        String anchor = null;//"start";
        if(RendererSettings.getInstance().getLabelFont().isBold())
            weight = "bold";
        StringBuilder sbSVG = new StringBuilder();

        String svgStroke = RendererUtilities.colorToHexString(RendererUtilities.getIdealOutlineColor(color),false);
        if(backgroundColor != null)
            svgStroke = RendererUtilities.colorToHexString(backgroundColor,false);

        String svgFill = RendererUtilities.colorToHexString(color,false);
        String svgStrokeWidth = "2";//String.valueOf(RendererSettings.getInstance().getTextOutlineWidth());
        sbSVG.append("\n<g");
        sbSVG.append(" font-family=\"" + name + '"');
        sbSVG.append(" font-size=\"" + size + "px\"");
        if(weight != null)
            sbSVG.append(" font-weight=\"" + weight + "\"");
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
         ctx.drawText(textInfo.getText(), textInfo.getLocation().x, textInfo.getLocation().y, _modifierFont);
         }*/

        int size = tiArray.length;

        int tbm = RendererSettings.getInstance().getTextBackgroundMethod();
        int outlineWidth = RendererSettings.getInstance().getTextOutlineWidth();
        
        if(outlineWidth > 2)
            outlineWidth = 2;
        

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

        g2d.setFont(RendererSettings.getInstance().getLabelFont());
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (tbm == RendererSettings.TextBackgroundMethod_OUTLINE_QUICK)
        {
            //draw text outline
            //_modifierFont.setStyle(Style.FILL);
//            _modifierFont.setStrokeWidth(RS.getTextOutlineWidth());
//            _modifierFont.setColor(outlineColor.toInt());
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

                g2d.setTransform(at);
                g2d.setColor(outlineColor);
                //g2d.setStroke(new BasicStroke(4,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.setStroke(new BasicStroke(4,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,3));

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

}
