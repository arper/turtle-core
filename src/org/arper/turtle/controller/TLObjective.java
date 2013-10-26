package org.arper.turtle.controller;

import org.arper.turtle.TLApplication;
import org.arper.turtle.TLTurtle;

public interface TLObjective {

    void runTurtle(int index, TLApplication app, TLTurtle[] turtles, Object[] args);
    int getObjectiveTurtleCount();

}
