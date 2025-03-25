package armyc2.c5isr.renderer.utilities;

import armyc2.c5isr.renderer.utilities.RendererSettings;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * 
 */
public class TextInfo {
	String _text = "";
	Point2D _location = null;
	Rectangle2D _bounds = null;
	double _descent = 0;
	double _aboveBaseHeight = 0;
	public TextInfo(String text, int x, int y, Font font, FontRenderContext frc)
	{
		if(text != null)
		{
			_text = text;
		}

		_location = new Point2D.Double(x,y);
		_bounds = font.getStringBounds(text, frc);
		_descent = _bounds.getHeight() + _bounds.getY();
		_aboveBaseHeight = _bounds.getY() * -1;

	}

	public void setLocation(int x, int y)
	{

		_bounds.setRect(x, y - _aboveBaseHeight, _bounds.getWidth(),_bounds.getHeight());
		_location.setLocation(x, y);
	}

	public Point2D getLocation()
	{
		return _location;
	}

	public void shift(int x, int y)
	{
		ShapeUtilities.offset(_bounds, x, y);
		ShapeUtilities.offset(_location, x, y);
	}

	public String getText()
	{
		return _text;
	}

	public Rectangle2D getTextBounds()
	{
		return _bounds;
	}


	public Rectangle2D getTextOutlineBounds()
	{
		RendererSettings RS = RendererSettings.getInstance();
		int outlineOffset = RS.getTextOutlineWidth();
		Rectangle2D bounds = (Rectangle2D)_bounds.clone();

		if(outlineOffset > 0)
		{
			if(RS.getTextBackgroundMethod() == RendererSettings.TextBackgroundMethod_OUTLINE)
				ShapeUtilities.grow(bounds, outlineOffset / 2);
			else
				ShapeUtilities.grow(bounds, outlineOffset);
		}

		return bounds;
	}

	public double getDescent()
	{
		return _descent;
	}
}
