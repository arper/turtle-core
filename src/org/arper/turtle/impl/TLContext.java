package org.arper.turtle.impl;

import java.util.List;

import org.arper.turtle.TLTurtle;
import org.arper.turtle.config.TLAnglePolicy;
import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.ui.TLWindow;

import com.google.common.collect.Lists;

public class TLContext {

    public TLContext(TLApplicationConfig config) {
        TLSingletonContext.set(this);

        this.anglePolicy = config.getAnglePolicy();
        this.simulator = new TLSimulator(config.getSimulationCores(),
                config.getSimulationStepMicros(),
                config.getSimulationMaxStutterMicros(),
                config.getSimulationMaxBusyWaitMicros());

        this.turtles = Lists.newArrayList();
        this.runningControllers = Lists.newArrayList();
        this.window = new TLWindow(config.getCanvasWidth(), config.getCanvasHeight());
    }

    private final TLAnglePolicy anglePolicy;
    private final TLWindow window;
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
