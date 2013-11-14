package org.arper.turtle;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;

import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.controller.TLObjective;
import org.arper.turtle.internal.TLContext;
import org.arper.turtle.internal.TLSingleton;
import org.arper.turtle.internal.swing.TLSwingUtilities;

import com.google.common.base.Preconditions;


public final class TLApplication {

    public static TLApplication runObjectiveAsApplication(final TLObjective objective, Object... args) {
        final TLApplication app = create();

        TLSwingUtilities.runOnAwtThread(new Runnable() {
            @Override
            public void run() {
                app.context.getWindow().setTitle(objective.getClass().getSimpleName());
                app.context.getWindow().setVisible(true);
            }
        }, false);

        app.startObjectiveWithNewTurtles(objective, args);
        return app;
    }

    public static TLApplication createFromConfigFile(String configFileName) {
        /* TODO: TLApplication constructor */
        throw new UnsupportedOperationException("not yet implemented");
    }

    public static TLApplication createFromConfigStream(InputStream configStream) {
        /* TODO: TLApplication constructor */
        throw new UnsupportedOperationException("not yet implemented");
    }

    public static TLApplication createFromConfig(TLApplicationConfig config) {
        TLApplication app = new TLApplication(config);
        TLSingleton.set(app, app.context);

        return app;
    }

    public static TLApplication create() {
        return createFromConfig(TLApplicationConfig.DEFAULT);
    }

    private TLApplication(TLApplicationConfig config) {
        Preconditions.checkNotNull(config, "TLApplication must have non-null TLApplicationConfig.");

        this.config = config;
        this.context = TLContext.create(config);
    }

    private final TLApplicationConfig config;
    private final TLContext context;

    public InputStream in = System.in;
    public PrintStream out = System.out;

    public TLApplicationConfig getConfig() {
        return config;
    }

    public TLContext getContext() {
        return context;
    }

    public void startObjective(final TLObjective objective,
                               final TLTurtle[] objectiveTurtles,
                               final Object[] args) {

        int turtleCount = objective.getObjectiveTurtleCount();

        Preconditions.checkArgument(objectiveTurtles.length == turtleCount,
                "Objective called with incorrect number of turtles! (Only given %s, but need %s!)",
                args.length, turtleCount);

        for (int i = 0; i < turtleCount; ++i) {
            final String threadName = String.format("TLObjective-%s-Thread-%d",
                    objective.getClass().getSimpleName(),
                    i);
            final int turtleIndex = i;

            context.submitTask(new Callable<Void>() {
                @Override
                public Void call() {
                    objective.runTurtle(turtleIndex, TLApplication.this, objectiveTurtles, args);
                    return null;
                }
            }, threadName);
        }
    }

    public void startObjectiveWithNewTurtles(TLObjective objective, Object... args) {
        int turtleCount = objective.getObjectiveTurtleCount();
        Preconditions.checkArgument(turtleCount > 0,
                "Objective doesn't use any turtles! [turtleCount=%s]",
                turtleCount);

        TLTurtle[] turtles = new TLTurtle[turtleCount];
        for (int i = 0; i < turtleCount; ++i) {
            turtles[i] = new TLTurtle();
            context.registerTurtle(turtles[i]);
        }

        startObjective(objective, turtles, args);
    }

}
