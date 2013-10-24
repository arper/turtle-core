package org.arper.turtle.impl;

import java.awt.geom.Point2D;

public class TLVector {
    private double x, y;

    public static TLVector unitVectorInDirection(double radians) {
        return new TLVector(Math.cos(radians), Math.sin(radians));
    }

    public TLVector(Point2D a) {
        this(a.getX(), a.getY());
    }

    public TLVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    public TLVector scale(double c) {
        return new TLVector(x * c, y * c);
    }

    public TLVector normalize() {
        double len = length();
        if (len != 0) {
            return scale(1.0 / len);
        } else {
            System.err.println("Warning: attempt to normalize zero-length vector.");
            return this;
        }
    }

    public TLVector add(TLVector b) {
        return new TLVector(x + b.x, y + b.y);
    }

    public TLVector subtract(TLVector b) {
        return new TLVector(x - b.x, y - b.y);
    }

    public Point2D asPoint2D() {
        return new Point2D.Double(x, y);
    }

    public double angle() {
        if (lengthSquared() < .00001) {
            return 0.0;
        } else {
            return Math.atan2(y, x);
        }
    }
}