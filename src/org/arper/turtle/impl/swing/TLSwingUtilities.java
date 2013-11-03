package org.arper.turtle.impl.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;


public class TLSwingUtilities {
    public static void assertOnAwtThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new AssertionError("incorrectly off awt thread");
        }
    }

    public static void assertOffAwtThread() {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new AssertionError("incorrectly on awt thread");
        }
    }

    public static void runOnAwtThread(Runnable r, boolean block) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            if (block) {
                try {
                    SwingUtilities.invokeAndWait(r);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                SwingUtilities.invokeLater(r);
            }
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
