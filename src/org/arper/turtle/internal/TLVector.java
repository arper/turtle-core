package org.arper.turtle.internal;

import java.awt.geom.Point2D;


public class TLVector {

    public static final float PI_F = (float) Math.PI;
    public static final float TWO_PI_F = (float) (2 * Math.PI);

    public static TLVector unitVectorInDirection(double radians) {
        return new TLVector((float) Math.cos(radians), (float) Math.sin(radians));
    }

    public TLVector(Point2D.Float p) {
        this.x = p.x;
        this.y = p.y;
    }

    public TLVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public final float x;
    public final float y;

    public float lengthSquared() {
        return x * x + y * y;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public TLVector scale(float c) {
        return new TLVector(x * c, y * c);
    }

    public TLVector normalize() {
        float len = length();
        if (len != 0) {
            return scale(1.0f / len);
        } else {
            return this;
        }
    }

    public TLVector add(TLVector b) {
        return new TLVector(x + b.x, y + b.y);
    }

    public TLVector add(float x, float y) {
        return new TLVector(this.x + x, this.y + y);
    }

    public TLVector add(Point2D.Float p) {
        return new TLVector(this.x + p.x, this.y + p.y);
    }

    public TLVector subtract(TLVector b) {
        return new TLVector(x - b.x, y - b.y);
    }

    public TLVector subtract(float x, float y) {
        return new TLVector(this.x - x, this.y - y);
    }

    public TLVector subtract(Point2D.Float p) {
        return new TLVector(this.x - p.x, this.y - p.y);
    }

    public Point2D.Float asPoint2D() {
        return new Point2D.Float(x, y);
    }

    public float angle() {
        if (lengthSquared() < TLSimulator.SIMULATION_EPSILON) {
            return 0.0f;
        } else {
            return (float) Math.atan2(y, x);
        }
    }
}