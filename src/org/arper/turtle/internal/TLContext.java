package org.arper.turtle.internal;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.arper.turtle.TLTurtle;
import org.arper.turtle.config.TLAnglePolicy;
import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.internal.swing.TLSwingUtilities;
import org.arper.turtle.internal.swing.TLSwingWindow;
import org.arper.turtle.ui.TLWindow;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class TLContext {

    public static TLContext create(TLApplicationConfig config) {
        return new TLContext(config);
    }

    private TLContext(final TLApplicationConfig config) {
        this.anglePolicy = config.getAnglePolicy();
        this.simulator = new TLSimulator(config.getSimulationCores(),
                config.getSimulationStepMicros(),
                config.getSimulationMaxStutterMicros(),
                config.getSimulationMaxBusyWaitMicros());

        this.turtles = Lists.newCopyOnWriteArrayList();
        this.executorService = Executors.newCachedThreadPool();
        this.submittedTasks = ArrayListMultimap.create();

        TLSwingUtilities.runOnAwtThread(new Runnable() {
            @Override
            public void run() {
                window = new TLSwingWindow(config.getCanvasWidth(),
                        config.getCanvasHeight(),
                        TLSwingWindow.DEFAULT_PLUGINS);
            }
        }, true);
    }

    public void registerTurtle(TLTurtle t) {
        synchronized (turtles) {
            turtles.add(t);
        }
    }

    private final TLAnglePolicy anglePolicy;
    private TLWindow window;
    private final TLSimulator simulator;
    private final List<TLTurtle> turtles;
    private final ExecutorService executorService;
    private final Multimap<String, Future<?>> submittedTasks;

    public TLAnglePolicy getAnglePolicy() {
        return anglePolicy;
    }

    public TLWindow getWindow() {
        return window;
    }

    public TLSimulator getSimulator() {
        return simulator;
    }

    public Iterable<TLTurtle> getTurtles() {
        return Iterables.unmodifiableIterable(turtles);
    }

    public <T> Future<T> submitTask(final Callable<T> task,
            final String taskName) {
        Future<T> future = executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                Thread.currentThread().setName(taskName);
                return task.call();
            }
        });

        synchronized(submittedTasks) {
            submittedTasks.put(taskName, future);
        }

        return future;
    }

}
