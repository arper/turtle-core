package org.arper.turtle;

import java.io.InputStream;

import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.controller.TLController;
import org.arper.turtle.controller.TLObjective;
import org.arper.turtle.impl.TLContext;
import org.arper.turtle.impl.j2d.TLAwtUtilities;
import org.arper.turtle.ui.TLConsole;

import com.google.common.base.Preconditions;


public final class TLApplication {

    public static TLApplication runObjectiveAsApplication(final TLObjective objective, Object... args) {
        final TLApplication app = new TLApplication(TLApplicationConfig.DEFAULT);
        
        TLAwtUtilities.runOnAwtThread(new Runnable() {
            @Override
            public void run() {
                app.context.getWindow().setTitle(objective.getClass().getSimpleName());
                app.context.getWindow().setVisible(true);
            }        
        }, false);

        app.startObjectiveWithNewTurtles(objective, args);
        return app;
    }
    
    public TLApplication(String configFileName) {
        /* TODO: TLApplication constructor */
        throw new UnsupportedOperationException("not yet implemented");
    }

    public TLApplication(InputStream configFileInput) {
        /* TODO: TLApplication constructor */
        throw new UnsupportedOperationException("not yet implemented");
    }

    public TLApplication(TLApplicationConfig config) {
        Preconditions.checkNotNull(config, "TLApplication must have non-null TLApplicationConfig.");
        this.config = config;
        this.context = TLContext.create(config);
        
        console = context.getWindow().getConsole();
    }

    private final TLApplicationConfig config;
    private final TLContext context;
    
    public final TLConsole console;

    public TLApplicationConfig getConfig() {
        return config;
    }

    public void startController(final TLController controller, final Object... args) {
        context.runInControllerThread(new Runnable() {
            @Override
            public void run() {
                controller.run(TLApplication.this, args);
            }
        }, "TLSimulationThread-" + controller.getClass().getSimpleName());
    }

    public void startObjective(final TLObjective objective, final TLTurtle[] objectiveTurtles, final Object[] args) {
        int turtleCount = objective.getObjectiveTurtleCount();

        Preconditions.checkArgument(objectiveTurtles.length == turtleCount,
                "Objective called with incorrect number of turtles! (Only given %s, but need %s!)",
                args.length, turtleCount);

        for (int i = 0; i < turtleCount; ++i) {
            String threadName = "TLObjective-" + objective.getClass().getSimpleName() + "-Thread-" + i;
            final int turtleIndex = i;

            context.runInControllerThread(new Runnable() {
                @Override
                public void run() {
                    objective.runTurtle(turtleIndex, TLApplication.this, objectiveTurtles, args);
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
            turtles[i] = context.createTurtle();
        }

        startObjective(objective, turtles, args);
    }

}
