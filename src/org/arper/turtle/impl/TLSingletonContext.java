package org.arper.turtle.impl;


public class TLSingletonContext {

    private static TLContext instance;

    public static TLContext get() {
        return instance;
    }

    public static void set(TLContext context) {
        TLSingletonContext.instance = context;
    }

}
