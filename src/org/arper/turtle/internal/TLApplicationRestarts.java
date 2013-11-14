package org.arper.turtle.internal;

import java.util.Map;

import org.arper.turtle.TLApplication;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class TLApplicationRestarts {

    public static void register(TLApplication app,
                                Runnable restartRunnable) {
        restartRunnables.put(app, restartRunnable);
    }

    public static void restart(TLApplication app) throws IllegalStateException {
        Preconditions.checkState(restartRunnables.containsKey(app),
                "App was not registered for restart");

    }

    private static Map<TLApplication, Runnable> restartRunnables = Maps.newHashMap();

    private TLApplicationRestarts() {
        /* do not instantiate */
    }

}
