package armyc2.c5isr.web.render.utilities;

import java.awt.geom.Point2D;

public class Point3D extends Point2D.Double {
    public double z;

    public Point3D() {
        super();
    }

    public Point3D(Point2D pt, double z) {
        super(pt.getX(), pt.getY());
        this.z = z;
    }

    public Point3D(double x, double y, double z) {
        super(x, y);
        this.z = z;

    }

    public double getZ() {
        return this.z;
    }
}
