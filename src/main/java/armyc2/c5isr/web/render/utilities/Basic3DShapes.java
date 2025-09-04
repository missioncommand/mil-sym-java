package armyc2.c5isr.web.render.utilities;

import armyc2.c5isr.renderer.utilities.Modifiers;
import armyc2.c5isr.JavaLineArray.TacticalLines;


public class Basic3DShapes {
    /**
     * Anchor Points: This shape requires one anchor point
     * <p>
     * Modifiers: radius ({@link Modifiers#AM_DISTANCE}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int CYLINDER = TacticalLines.PBS_CIRCLE;

    /**
     * Anchor Points: This shape requires two anchor points
     * <p>
     * Modifiers: width ({@link Modifiers#AM_DISTANCE}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int ORBIT = TacticalLines.BS_ORBIT;

    /**
     * Anchor Points: This shape requires at least two anchor points
     * <p>
     * Modifiers: width ({@link Modifiers#AM_DISTANCE}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int ROUTE = TacticalLines.BS_ROUTE;

    /**
     * Anchor Points: This shape requires at least three anchor points
     * <p>
     * Modifiers: min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int POLYGON = TacticalLines.BS_AREA;

    /**
     * Anchor Points: This shape requires one anchor point
     * <p>
     * Modifiers: min radius and max radius ({@link Modifiers#AM_DISTANCE}), left and right azimuth ({@link Modifiers#AN_AZIMUTH}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int RADARC = TacticalLines.BS_RADARC;

    /**
     * Anchor Points: This shape requires at least three anchor points
     * <p>
     * Modifiers: radius ({@link Modifiers#AM_DISTANCE}), left and right azimuth ({@link Modifiers#AN_AZIMUTH}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int POLYARC = TacticalLines.BS_POLYARC;

    /**
     * A collection of radarcs
     * <p>
     * Anchor Points: This shape requires one anchor point
     * <p>
     * Modifiers (for each radarc): min radius and max radius ({@link Modifiers#AM_DISTANCE}), left and right azimuth ({@link Modifiers#AN_AZIMUTH}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int CAKE = TacticalLines.BS_CAKE;

    /**
     * A collection of routes
     * <p>
     * Anchor Points: This shape requires at least two anchor points
     * <p>
     * Modifiers (for each segment): left and right width ({@link Modifiers#AM_DISTANCE}), and min and max altitude ({@link Modifiers#X_ALTITUDE_DEPTH}).
     */
    public static final int TRACK = TacticalLines.BS_TRACK;
}