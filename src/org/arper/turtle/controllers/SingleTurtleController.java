package org.arper.turtle.controllers;

import java.util.Arrays;

import org.arper.turtle.TLApplication;
import org.arper.turtle.TLController;
import org.arper.turtle.Turtle;

import com.google.common.base.Preconditions;

public abstract class SingleTurtleController extends TLController {

    @Override
    public void run(TLApplication app, Object[] args) {
        Preconditions.checkArgument(args.length > 0 && args[0] instanceof Turtle, "SingleTurtleController called without a turtle arg!");
        runWithTurtle((Turtle) args[0], app, Arrays.copyOfRange(args, 1, args.length));
    }

    public abstract void runWithTurtle(Turtle t, TLApplication app, Object[] args);
}
