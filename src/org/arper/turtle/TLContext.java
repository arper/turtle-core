package org.arper.turtle;

import java.util.List;

import org.arper.turtle.impl.TLLockingContext;
import org.arper.turtle.impl.TLSingletonContext;
import org.arper.turtle.ui.TLWindow;

import com.google.common.collect.Lists;

public class TLContext {

    public TLContext(int width, int height) {
        TLSingletonContext.set(this);


        this.lockingContext = new TLLockingContext();
        this.simulator = new TLSimulator(TLApplicationConfig.TL_NUM_SIMULATION_CORES,
                lockingContext, TLApplicationConfig.TL_INTERPOLATION_STEP_MICROS);

        this.turtles = Lists.newArrayList();
        this.window = new TLWindow(width, height);
    }

    private final TLWindow window;
    private final TLLockingContext lockingContext;
    private final TLSimulator simulator;
    private final List<Turtle> turtles;

    public Turtle createTurtle() {
        Turtle t = new Turtle();
        synchronized(turtles) {
            turtles.add(t);
        }
        return t;
    }

    public TLWindow getWindow() {
        return window;
    }

    public TLLockingContext getLockingContext() {
        return lockingContext;
    }

    public TLSimulator getSimulator() {
        return simulator;
    }

    public List<Turtle> getTurtles() {
        return turtles;
    }

}
