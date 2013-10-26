package org.arper.turtle.impl;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import org.arper.turtle.TLPathType;

import com.google.common.collect.Lists;

public class TLTurtleState {

    public TLTurtleState() {
        location = new Point2D.Float();
        fillShape = Lists.newArrayList();

        reset();
    }

    public final Point2D.Float location;     // pixels
    public volatile float movementSpeed;  // pixels per second

    public volatile float heading;         // radians
    public volatile float turningSpeed;   // radians per second

    public volatile Color color;           // note: non-null
    public volatile TLPathType pathType;
    public volatile float thickness;       // pixels
    public volatile boolean isPenDown;

    public final List<Point2D.Float> fillShape;// for use to produce fillShapes
    public boolean isFilling;

    public volatile String status;
    public volatile Object data;

    public void reset() {
        location.setLocation(0, 0);
        movementSpeed = 200;

        heading = 0;
        turningSpeed = TLVector.PI_F * 1.5f;

        color = Color.BLACK;
        pathType = TLPathType.Rounded;
        thickness = 8;
        isPenDown = true;

        fillShape.clear();
        isFilling = false;

        status = null;
        data = null;
    }

    public void set(TLTurtleState other) {
        this.location.setLocation(other.location);
        this.movementSpeed = other.movementSpeed;
        this.heading = other.heading;
        this.turningSpeed = other.turningSpeed;
        this.color = other.color;
        this.pathType = other.pathType;
        this.thickness = other.thickness;
        this.isPenDown = other.isPenDown;

        this.fillShape.clear();
        this.fillShape.addAll(other.fillShape);
        this.isFilling = other.isFilling;

        this.status = other.status;
        this.data = other.data;
    }
}
