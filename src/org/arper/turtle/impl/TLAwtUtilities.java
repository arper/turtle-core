package org.arper.turtle.impl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;


public class TLAwtUtilities {
    public static void assertOnAwtThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new AssertionError("incorrectly on awt thread");
        }
    }
    
    public static void assertOffAwtThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new AssertionError("incorrectly off awt thread");
        }
    }
    
    public static void runOnAwtThread(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    private static final Executor OFF_AWT_EXECUTOR = Executors.newCachedThreadPool();
    
    public static void runOffAwtThread(Runnable r) {
        if (!SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            OFF_AWT_EXECUTOR.execute(r);
        }
    }
}
