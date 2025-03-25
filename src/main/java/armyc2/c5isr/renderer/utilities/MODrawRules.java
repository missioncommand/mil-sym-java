package armyc2.c5isr.renderer.utilities;

/**
 * Draw Rules for METEOROLOGICAL AND OCEANOGRAPHIC SYMBOLOGY
 * 
 */
public class MODrawRules {

    /**
     * Usually an entry in the MilStd that is just a category containing other symbols
     * and not something that gets drawn itself.
     */
    public static final int DONOTDRAW = 0;

    /**
     * Anchor Points: This graphic requires at least three anchor points to
     * define the boundary of the area. Add as many points as necessary to
     * accurately reflect the area's size and shape.
     * Size/Shape: Scalable.
     * Orientation: Not applicable.
     */
    public static final int AREA1 = 101;

    /**
     * Anchor Points: This graphic requires at least three anchor points to
     * define the boundary of the area. Add as many points as necessary to
     * accurately reflect the area's size and shape.
     * Size/Shape: Determined by the anchor points.
     * Orientation: Not applicable.
     */
    public static final int AREA2 = 102;


    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is typically centered over the desired location.
     */
    public static final int POINT1 = 201;

    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the geometric center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is centered over the anchor location.
     */
    public static final int POINT2 = 202;

    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is oriented upright on the display and operator-centered over the desired location.
     */
    public static final int POINT3 = 203;


    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is centered over the location of the reported wind.
     */
    public static final int POINT4 = 204;


    /**
     * Anchor Points: This graphic requires a minimum of two anchor points.
     * The first point defines the location of the plot circle.
     * Additional points define the wind shaft and the speed of the wind.
     * Wind speed is depicted on the shaft using a combination of the shaft
     * alone (1-2 knots), half barbs (5 knots), barbs (10 knots)
     * and pennants (50 knots). Wind speeds 5 knots or greater are rounded
     * to the nearest 5 knots. Missing wind speed is depicted by an "X" at
     * the end of the wind shaft. Winds with missing direction are not displayed.
     *
     * Size/Shape: Not applicable.
     *
     * Orientation: The shaft of the graphic is oriented with reference to
     * true north in the direction from which the wind is blowing to the
     * nearest 10 degrees. The barbs and pennants lie back from the shaft at
     * an angle of 120 degrees and are oriented to the left of the shaft in
     * the Northern Hemisphere and to the right in the Southern Hemisphere.
     * The graphic is operator-centered over the desired location.
     */
    public static final int POINT5 = 205;

    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is centered over the location of the reported cloud cover.
     */
    public static final int POINT6 = 206;

    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is centered over the location of the reported conditions.
     */
    public static final int POINT7 = 207;

    /**
     * Anchor Points: This graphic requires one anchor point. The center point
     * defines the center of the graphic.
     * Size/Shape: Scalable.
     * Orientation: The graphic is centered over the position of the tropical system.
     */
    public static final int POINT8 = 208;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined
     * to extend the line.
     * Size/Shape: Scalable/Curve. The curvature of the line is operator defined.
     * Orientation: The first and last anchor points determine the length of
     * the line.
     */
    public static final int LINE1 = 301;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     *
     * Size/Shape: Scalable/Curve. The points are typically connected with a
     * straight line consisting of a short line section and an alternating
     * V shape. The curvature and amplitude of the waves of the line is
     * operator defined.
     *
     * Orientation: The first and last anchor points determine the length of
     * the line. The line should be drawn so the "V" shapes are facing in
     * the direction of movement. The "V" shapes and short line segment will
     * alternate along the line.
     */
    public static final int LINE2 = 302;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     *
     * Size/Shape: Scalable/Curve. The points are typically connected with a
     * straight line consisting of a short line section and alternating two
     * dots. The curvature and amplitude of the waves of the line is
     * operator defined.
     *
     * Orientation: The first and last anchor points determine the length of the line. The two dots and the short line segment will alternate along the line.
     */
    public static final int LINE3 = 303;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     *
     * Size/Shape: Scalable/Curve. The points are typically connected with a
     * curved/wavy line consisting of a short line and one dot. The curvature
     * and amplitude of the waves of the line is operator defined.
     *
     * Orientation: The first and last anchor points determine the length of
     * the line. The dot and the short line segment will alternate along
     * the line.
     */
    public static final int LINE4 = 304;

    /**
     * Anchor Points: This graphic requires at least three anchor points to
     * define the boundary of the area. Add as many points as necessary to
     * accurately reflect the area's size and shape.
     *
     * Size/Shape: Determined by the anchor points.
     *
     * Orientation: The first and last anchor points determine the length of
     * the line. The dual line segments will be parallel to slightly wider at
     * the western end.
     */
    public static final int LINE5 = 305;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     * Size/Shape: Scalable/Curve. The points are typically connected with a
     * solid straight line with alternating slanted lines connected as depicted
     * in the example to indicate convergence.
     * Orientation: The first and last anchor points determine the length of
     * the line. The alternating slanted lines will be evenly spaced along
     * the line. Orientation is determined by the anchor points.
     */
    public static final int LINE6 = 306;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     *
     * Size/Shape: Scalable/Curve. The points are typically connected with a
     * dashed straight or curved line. The curvature of the line is operator
     * defined.
     *
     * Orientation: The first and last anchor points determine the length of
     * the line. The red and green line segments will alternate along the line.
     * Orientation is determined by the anchor points.
     */
    public static final int LINE7 = 307;

    /**
     * Anchor Points: This graphic requires at least two anchor points,
     * points 1 and 2, to define the line. Additional points can be defined to
     * extend the line.
     * Size/Shape: Scalable.
     * Orientation: The graphic is oriented upright on the display as shown in
     * the example and operator-centered over the desired location.
     */
    public static final int LINE8 = 308;


}
