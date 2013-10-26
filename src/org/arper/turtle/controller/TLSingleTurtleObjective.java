package org.arper.turtle.controller;

import org.arper.turtle.TLApplication;
import org.arper.turtle.TLTurtle;

public abstract class TLSingleTurtleObjective implements TLObjective {

    protected abstract void run(TLTurtle turtle, TLApplication app, Object[] args);

    @Override
    public int getObjectiveTurtleCount() {
        return 1;
    }

    @Override
    public void runTurtle(int index, TLApplication app, TLTurtle[] turtles, Object[] args) {
        switch (index) {
        case 0:
            run(turtles[0], app, args);
            break;
        default:
            throw new IllegalArgumentException("Turtle index out of range: " + index);
        }
    }
}
