package org.arper.turtle.impl;

import org.arper.turtle.TLContext;

public class TLSingletonContext {

    private static TLContext instance;

    public static TLContext get() {
        return instance;
    }

    public static void set(TLContext context) {
        TLSingletonContext.instance = context;
    }

}
