package org.arper.__old__.turtle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.impl.TLAction;
import org.arper.turtle.impl.TLActions;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class TLAnimator {
	public static final double EPSILON = .000001;
	
	private List<Turtle> turtles;
	private Map<Turtle, TLTurtleAnimationHelper> turtleHelpers;
	private AnimationThread animationThread;
	private boolean running;
	private final TLSimulationSettings settings;
	
	public TLAnimator() {
		turtles = Lists.newArrayList();
		turtleHelpers = Maps.newHashMap();
		running = false;
		
		animationThread = new AnimationThread();
		settings = new TLSimulationSettings();
	}
	
	public synchronized void reset() {
	    synchronized(turtles) {
	        turtles.clear();
	        turtleHelpers.clear();
	    }
	    
	    if (animationThread != null) {
	        animationThread.threadLive = false;
	        animationThread.interrupt();
	    }
        animationThread = new AnimationThread();
        
        if (running) {
            animationThread.start();
        }
	}
	
	public TLSimulationSettings getSettings() {
	    return settings;
	}
    
    public boolean isRunning() {
        return running;
    }

	public void registerTurtle(Turtle t) {
		synchronized (turtles) {
		    if (turtles.contains(t)) {
		        return;
		    }
		    
		    turtles.add(t);
		    turtleHelpers.put(t, new TLTurtleAnimationHelper(t));
		}
	}
	
	public void invokeAndWait(TLAction a, Turtle t) {
	    if (!isRunning()) {
	        a.execute(t.getMutableState());
	        return;
	    }
	    
	    TLTurtleAnimationHelper helper;
	    synchronized (turtles) {
	        helper = turtleHelpers.get(t);
	    }
        Preconditions.checkState(helper != null, "Turtle not a valid actor being tracked by this animator!");
        helper.enqueueAndWait(a);
	}
	
	public void awaitAll() {
	    synchronized (turtles) {
	        for (Turtle t : turtles) {
	            t.await();
	        }  
	    }
	}
	
	public void await(Turtle t) {
	    invokeAndWait(TLActions.empty(), t);
	}
	
	public synchronized void start() {
		if (!animationThread.isAlive()) {
			running = true;
			animationThread.start();
		}
	}
	
	public synchronized void stop() {
		running = false;
	}
	
	private class AnimationThread extends Thread {
		private static final int NANOS_PER_MILLI = 1000000;
		private boolean threadLive = true;
		
		public AnimationThread() {
		    super("TLAnimator-AnimationThread-" + System.currentTimeMillis());
		}
		
		@Override
		public void run() {
			long lastTimeMillis = 0L;
			
			while (running && threadLive) {
				long now = System.nanoTime() / NANOS_PER_MILLI;
				long elapsed = now - lastTimeMillis;
				if (elapsed < 15) {
				    try {
				        Thread.sleep(15 - elapsed);
				    } catch (Exception e) {
				        break;
				    }
				    elapsed = 15;
				}
				else if (elapsed > 60) {
				    elapsed = 60;
				}
				
				updateTurtles(elapsed);
				
				lastTimeMillis = now;
			}
		}
	}
	
	private void updateTurtles(long millis) {
	    if (settings.isPaused()) {
	        return;
	    }
	    
        double scaledElapsed = millis / 1000.0f * settings.getAnimationSpeed();
        synchronized(turtles) {
            for(Turtle t : turtles) {
                turtleHelpers.get(t).update(scaledElapsed);
            }
        }
	}
	
	private static class TLTurtleAnimationHelper {
	    
	    private final Turtle turtle;
	    private final BlockingQueue<TLAction> actions;
	    private final Lock actionsMutex;
	    private final Condition emptyActionsCondition;

        private double remainingUpdate;
	    private double elapsedSeconds;
	    
	    /* package-private */ TLTurtleAnimationHelper(Turtle t) {
	        turtle = t;
	        actions = new LinkedBlockingQueue<TLAction>();
	        actionsMutex = new ReentrantLock();
	        emptyActionsCondition = actionsMutex.newCondition();
	    }

	    public void update(double seconds) {
	        elapsedSeconds += seconds;
	        advanceActionQueue(seconds);
	    }
	    
	    private void advanceActionQueue(double seconds) {
	        actionsMutex.lock();
	        try {
	            remainingUpdate = seconds;
	            while (!actions.isEmpty() && remainingUpdate > 0) {
	                TLAction top = actions.peek();
	                remainingUpdate = top.perform(turtle.getMutableState(), remainingUpdate);
	                if (remainingUpdate > 0) { // action was completed with time to spare
	                    actions.remove();
	                    if (actions.isEmpty()) {
	                        emptyActionsCondition.signalAll();
	                    }
	                }
	            }
	        } finally {
	            actionsMutex.unlock();
	        }
//	        turtle.helperGetSprite().markDirty();
	    }

	    public void enqueueAndWait(TLAction a) {
	        try {
	            actions.put(a);
	            if (remainingUpdate > 0) {
	                advanceActionQueue(remainingUpdate);
	            }
	            
	            actionsMutex.lock();
	            try {
	                while (!actions.isEmpty()) {
	                    emptyActionsCondition.await();
	                }
	            } finally {
	                actionsMutex.unlock();
	            }
	        } catch (InterruptedException ie) {
	            throw Throwables.propagate(ie);
	        }
	    }

	    public double getElapsedSeconds() {
	        return elapsedSeconds;
	    }
	}
}
