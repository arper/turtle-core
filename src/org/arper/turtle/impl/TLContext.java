package org.arper.turtle.impl;

import java.util.List;

import org.arper.turtle.TLTurtle;
import org.arper.turtle.config.TLAnglePolicy;
import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.impl.j2d.TLAwtUtilities;
import org.arper.turtle.impl.j2d.TLJ2DWindow;
import org.arper.turtle.ui.TLWindow;

import com.google.common.collect.Lists;

public class TLContext {

    public static TLContext create(TLApplicationConfig config) {
        return new TLContext(config);
    }
    
    private TLContext(final TLApplicationConfig config) {
        TLSingletonContext.set(this);

        this.anglePolicy = config.getAnglePolicy();
        this.simulator = new TLSimulator(config.getSimulationCores(),
                config.getSimulationStepMicros(),
                config.getSimulationMaxStutterMicros(),
                config.getSimulationMaxBusyWaitMicros());

        this.turtles = Lists.newArrayList();
        this.runningControllers = Lists.newArrayList();
        
        TLAwtUtilities.runOnAwtThread(new Runnable() {
            @Override
            public void run() {
                window = new TLJ2DWindow(config.getCanvasWidth(), config.getCanvasHeight());
            }
        }, true);
    }
    
    private final TLAnglePolicy anglePolicy;
    private TLWindow window;
    private final TLSimulator simulator;
    private final List<TLTurtle> turtles;
    private final List<Thread> runningControllers;

    public TLAnglePolicy getAnglePolicy() {
        return anglePolicy;
    }

    public void runInControllerThread(Runnable r, String threadName) {
        TLControllerThread thread = new TLControllerThread(r, threadName);
        runningControllers.add(thread);
        thread.start();
    }

    public TLTurtle createTurtle() {
        TLTurtle t = new TLTurtle();
        synchronized(turtles) {
            turtles.add(t);
        }
        return t;
    }

    public TLWindow getWindow() {
        return window;
    }
    
    public TLSimulator getSimulator() {
        return simulator;
    }

    public List<TLTurtle> getTurtles() {
        return turtles;
    }

    private class TLControllerThread extends Thread {

        private TLControllerThread(Runnable r, String name) {
            super(name);
            this.controllerRunnable = r;
        }

        private final Runnable controllerRunnable;

        @Override
        public void run() {
            try {
                controllerRunnable.run();
            } finally {
                runningControllers.remove(this);
            }
        }
    }

}
