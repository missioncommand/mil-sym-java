package armyc2.c5isr.web.render.utilities;

import java.util.ArrayList;

import java.awt.geom.Point2D;

import armyc2.c5isr.renderer.utilities.ShapeInfo;

public class ShapeInfo3D extends ShapeInfo {
    private Point3D _ModifierPosition3D = null;
    private ArrayList<ArrayList<Point3D>> _Polylines3D = null;

    public void setModifierPosition(Point3D value) {
        this._ModifierPosition3D = value;
    }

    @Override
    public Point3D getModifierPosition() {
        return this._ModifierPosition3D;
    }

    /**
     * @deprecated use {@link ShapeInfo3D#getPolylines3D()}
     */
    @Override
    public ArrayList<ArrayList<Point2D>> getPolylines() {
        return null;
    }

    /**
     * @deprecated use {@link ShapeInfo3D#setPolylines3D(ArrayList<ArrayList<Point3D>>)}
     */
    @Override
    public void setPolylines(ArrayList<ArrayList<Point2D>> value) {
    }

    public ArrayList<ArrayList<Point3D>> getPolylines3D() {
        return this._Polylines3D;
    }

    public void setPolylines3D(ArrayList<ArrayList<Point3D>> value) {
        this._Polylines3D = value;
    }
}
