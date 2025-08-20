package armyc2.c5isr.renderer.utilities;

public class Modifier {
    String _modifierID = null;
    int _yIndex = -999;
    int _xIndex = -999;
    double _x = 0;
    double _y = 0;
    String _text = "";
    boolean _centered = true;

    public Modifier(String id, String text, int indexX, int indexY, boolean centered, double x, double y)
    {
        _modifierID = id;
        if(text != null && !text.isEmpty())
            _text = text;
        _xIndex = indexX;
        _yIndex = indexY;
        _x = x;
        _y = y;
        _centered = centered;
    }

    public Modifier(String id, String text, int indexX, int indexY, boolean centered)
    {
        _modifierID = id;
        if(text != null && !text.isEmpty())
            _text = text;
        _xIndex = indexX;
        _yIndex = indexY;
        _centered = centered;
    }

    public String getID(){return _modifierID;}

    public String getText(){return _text;}

    public int getIndexX(){return _xIndex;}

    public int getIndexY(){return _yIndex;}

    public boolean getCentered(){return _centered;}

    public double getX(){return _x;}
    public void setX(double x){_x = x;}

    public double getY(){return _y;}
    public void setY(double y){_y = y;}
}
