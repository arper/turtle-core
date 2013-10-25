package org.arper.__old__.turtle.impl;

import java.awt.geom.Point2D;

import org.arper.turtle.TLSimulator;

public class TLVector {

    public static final float PI_F = (float) Math.PI;

    public static TLVector unitVectorInDirection(double radians) {
        return new TLVector((float) Math.cos(radians), (float) Math.sin(radians));
    }

    public TLVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public TLVector(Point2D.Float loc) {
        this.x = loc.x;
        this.y = loc.y;
    }

    private final float x;
    private final float y;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

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

    public TLVector subtract(TLVector b) {
        return new TLVector(x - b.x, y - b.y);
    }

    public TLVector subtract(float x, float y) {
        return new TLVector(this.x - x, this.y - y);
    }

    public Point2D asPoint2D() {
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