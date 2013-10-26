package org.arper.turtle.controller;

import org.arper.turtle.TLApplication;
import org.arper.turtle.Turtle;

public interface TLObjective {

    void runTurtle(int index, TLApplication app, Turtle[] turtles, Object[] args);
    int getObjectiveTurtleCount();

}
