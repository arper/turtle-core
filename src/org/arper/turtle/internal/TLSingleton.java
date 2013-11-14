package org.arper.turtle.internal;

import org.arper.turtle.TLApplication;

import com.google.common.base.Preconditions;


public class TLSingleton {

    private static TLContext context;
    private static TLApplication app;

    public static void set(TLApplication app, TLContext context) {
        Preconditions.checkState(TLSingleton.context == null && TLSingleton.app == null,
                "singletons initialized twice");
        TLSingleton.app = app;
        TLSingleton.context = context;
    }

    public static TLContext getContext() {
        return context;
    }

    public static TLApplication getApplication() {
        return app;
    }

}
