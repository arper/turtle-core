package org.arper.turtle;

import java.io.InputStream;

import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.controller.TLController;
import org.arper.turtle.controller.TLObjective;
import org.arper.turtle.impl.TLContext;

import com.google.common.base.Preconditions;


public final class TLApplication {

    public static void runObjectiveAsApplication(TLObjective objective, Object... args) {
        new TLApplication(TLApplicationConfig.DEFAULT)
            .startObjectiveWithNewTurtles(objective, args);
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
        this.context = new TLContext(config);

        context.getWindow().setVisible(true);
    }

    private final TLApplicationConfig config;
    private final TLContext context;

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
