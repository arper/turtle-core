package org.arper.turtle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JOptionPane;

import org.arper.turtle.impl.TLAwtUtilities;
import org.asper.turtle.ui.TLWindow;



public final class TLApplication {

    private static TLWindow mainWindow;
    private static Class<?> mainClass;

    public static TLWindow run(final int width, final int height, final Turtle... turtles) {
        if (turtles.length == 0) {
            System.err.println("Turtle Warning: no turtles specified to run.");
        }
        
        if (mainClass == null) {
            StackTraceElement[] stack = Thread.currentThread ().getStackTrace ();
            String mainClassName = stack[stack.length - 1].getClassName ();
            try {
                mainClass = ClassLoader.getSystemClassLoader().loadClass(mainClassName);
            } catch (Exception e) {
                System.err.println("Error finding main class: " + mainClass);
            }
        }
        
        final BlockingQueue<TLWindow> retVal = new ArrayBlockingQueue<TLWindow>(1);

        TLAwtUtilities.runOnAwtThread(new Runnable() {
            @Override
            public void run() {
                TLWindow window;
                if (mainWindow == null) {
                    window = mainWindow = new TLWindow(width, height);
                } else if (!mainWindow.getCanvas().getAnimator().isRunning()) {
                    window = mainWindow;
                } else {
                    window = new TLWindow(width, height);
                }
                
                StringBuffer windowTitle = new StringBuffer();
                for (Turtle t : turtles) {
                    windowTitle.append(t.getClass().getSimpleName() + ", ");
                }
                if (windowTitle.length() > 0)
                    windowTitle.delete(windowTitle.length() - 2, windowTitle.length());
                windowTitle.append(": Turtle Learning Library");
                window.setTitle(windowTitle.toString());

                List<TurtleThread> threads = new ArrayList<TurtleThread>();

                for (int i = 0; i < turtles.length; i++) {
                    window.getCanvas().registerTurtle(turtles[i]);
                    threads.add(new TurtleThread(turtles[i], i));
                }

                window.setVisible(true);
                window.repaint();
                retVal.add(window);
                TLAnimator a = window.getCanvas().getAnimator();
                a.start();
                
                for (Turtle turtle : turtles)
                    turtle.init();

                for (Thread turtleThread : threads)
                    turtleThread.start();
            }
        });
        
        try {
            return retVal.take();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void restart(TLWindow window) {
        boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean(). getInputArguments().toString().contains("-agentlib:jdwp");
        if (!isDebug) {
            TLAnimatorSettings s = window.getCanvas().getAnimator().getSettings();
            boolean paused = s.isPaused();
            if (!paused)
                s.pause();
            int choice = JOptionPane.showConfirmDialog(window, 
                    "You are not running in debug mode! For a 'restart' to pick up new changes to your Turtle program,\n" +
            		"you must be running in debug mode (the little bug button next to the Run button in Eclipse).\n" +
            		"Would you like to restart anyway?",
                    "Not in Debug Mode", JOptionPane.YES_NO_OPTION);
            if (!paused)
                s.unpause();
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (window == null) {
            throw new IllegalArgumentException("null window");
        } if (mainClass == null) {
            throw new IllegalArgumentException("application is not being run");
        }

        window.getCanvas().getAnimator().stop();
        window.getCanvas().reset();
        try {
            mainClass.getMethod("main", String[].class).invoke(null, new Object[]{null});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static class TurtleThread extends Thread {
        private Turtle turtle;
        private int index;
        private boolean done;

        public TurtleThread(Turtle t, int index) {
            turtle = t;
            this.index = index;
            done = false;
            setPriority(MAX_PRIORITY);
        }

        @Override
        public void run() {
            try {
                turtle.run(index);
                done = true;
            } catch (Exception e) {
                if (e.getCause() instanceof InterruptedException) {
                    done = true;
                } else {
                    e.printStackTrace();
                }
            }
        }

        @SuppressWarnings("unused")
        public boolean isDone() {
            return done;
        }
    }
    
    private TLApplication() {}
}
