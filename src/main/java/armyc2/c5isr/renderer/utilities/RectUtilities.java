package armyc2.c5isr.renderer.utilities;


import java.awt.*;
import java.awt.geom.Rectangle2D;

public class RectUtilities {

	public static Rectangle makeRectangleFromRect(int x1, int y1, int x2, int y2) {
		return new Rectangle(x1, y1, x2-x1, y2-y1);
	}

	public static Rectangle2D makeRectangle2DFromRect(double x1, double y1, double x2, double y2) {
		return new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
	}

	public static Rectangle2D makeRectangle2DFromRect(float x1, float y1, float x2, float y2) {
		return new Rectangle2D.Float(x1, y1, x2-x1, y2-y1);
	}


	/**
	 * Copies a Rectangle
	 * @param rect {@link Rectangle2D}
	 * @return {@link Rectangle2D}
	 */
	public static Rectangle2D copyRect(Rectangle2D rect) {
		return new Rectangle2D.Double((int)rect.getX(), (int)rect.getY(), (int)(rect.getWidth()+0.5), (int)(rect.getHeight()+0.5));
	}

	/**
	 * copies and rounds the points.  x,y round down &amp; width,height round up
	 * @param rect {@link Rectangle2D}
	 * @return {@link Rectangle2D}
	 */
	public static Rectangle2D roundRect(Rectangle2D rect) {
		double offsetX = rect.getX() - (int)(rect.getX());
		double offsetY = rect.getY() - (int)(rect.getY());

		return new Rectangle2D.Double((int)rect.getX(), (int)rect.getY(), (int)(Math.round(rect.getWidth()+offsetX+0.5)), (int)Math.round(rect.getHeight()+offsetY+0.5));
	}

	public static void grow(Rectangle2D rect, int size) {
		rect.setRect(rect.getX() - size, rect.getY() - size, rect.getWidth() + (size*2), rect.getHeight() + (size*2));
		//return new Rectangle2D.Double(rect.left - size, rect.top - size, rect.right + size, rect.bottom + size);
	}


	public static void shift(Rectangle2D rect, int x, int y) {
		rect.setRect(rect.getX() + x, rect.getY() + y, rect.getWidth(), rect.getHeight());
	}


	public static void shiftBR(Rectangle2D rect, int x, int y) {
		rect.setRect(rect.getX(), rect.getY(), rect.getWidth() + x, rect.getHeight() + y);
	}

	public static Rectangle toRectangle(Rectangle2D b) {
		if (b == null) {
			return null;
		}/*from w ww . j a  va 2s . c o  m*/
		if (b instanceof Rectangle) {
			return (Rectangle) b;
		} else {
			return new Rectangle((int) b.getX(), (int) b.getY(),
					(int) b.getWidth(), (int) b.getHeight());
		}
	}

	public static Rectangle toRectangle(double x, double y, double w, double h) {
		return new Rectangle((int) x, (int) y,
				(int) w, (int) h);
	}
}