package armyc2.c5isr.renderer.utilities;

import java.awt.geom.*;

/**
 *
 * 
 */
public class ShapeUtilities {

    public static void grow(Rectangle2D rect, int size)
    {
        rect.setFrame(rect.getX() - size, rect.getY() - size, rect.getWidth() + (size*2), rect.getHeight() + (size*2));
    }

    public static void offset(Rectangle2D rect, int offsetX, int offsetY)
    {
        rect.setRect(rect.getX() + offsetX, rect.getY() + offsetY, rect.getWidth(), rect.getHeight());
    }

    public static void offset(Rectangle2D rect, double offsetX, double offsetY)
    {
        rect.setRect(rect.getX() + offsetX, rect.getY() + offsetY, rect.getWidth(), rect.getHeight());
    }

    public static void offset(Point2D point, int offsetX, int offsetY)
    {
        point.setLocation(point.getX() + offsetX, point.getY() + offsetY);
    }

    public static void offset(Point2D point, double offsetX, double offsetY)
    {
        point.setLocation(point.getX() + offsetX, point.getY() + offsetY);
    }

    public static void offset(Path2D path, int offsetX, int offsetY)
    {
        path.transform(AffineTransform.getTranslateInstance(offsetX, offsetY));
    }

    public static Ellipse2D offset(Ellipse2D ellipse, double offsetX, double offsetY)
    {
        return new Ellipse2D.Double(ellipse.getX() + offsetX, ellipse.getY() + offsetY, ellipse.getWidth(), ellipse.getHeight());
    }

    public static Ellipse2D offset(Ellipse2D.Float ellipse, float offsetX, float offsetY)
    {
        return new Ellipse2D.Float((float)ellipse.getX() + offsetX, (float)ellipse.getY() + offsetY, (float)ellipse.getWidth(), (float)ellipse.getHeight());
    }

    public static Line2D offset(Line2D line, double offsetX, double offsetY)
    {
        return new Line2D.Double(line.getX1() + offsetX, line.getY1() + offsetY, line.getX2() + offsetX, line.getY2() + offsetY);
    }

    public static Line2D offset(Line2D.Float line, float offsetX, float offsetY)
    {
        return new Line2D.Float((float)line.getX1() + offsetX, (float)line.getY1() + offsetY, (float)line.getX2() + offsetX, (float)line.getY2() + offsetY);
    }
}
