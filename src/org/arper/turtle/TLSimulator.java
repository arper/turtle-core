package org.arper.turtle;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.arper.turtle.impl.TLAction;
import org.arper.turtle.impl.TLAwtUtilities;
import org.arper.turtle.impl.TLLockingContext;
import org.arper.turtle.impl.TurtleState;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class TLSimulator {

    private static final long NANOS_IN_MICRO = 1000L;
    private static final float MICROS_IN_SECOND = 1000000.0f;
    public static final float SIMULATION_EPSILON = .0001f;

	private final TLSimulationSettings settings;
	private final TLLockingContext lockingContext;
	private final long interpolationStepMicros;
    private final ScheduledExecutorService actionScheduler;

    private final LoadingCache<Turtle, TurtleState> turtleState;

	public TLSimulator(int numSimulationCores,
                       TLLockingContext lockingContext,
	                   long interpolationStepMicros) {
		settings = new TLSimulationSettings();
		actionScheduler = createScheduler(numSimulationCores);

		this.lockingContext = lockingContext;
		this.interpolationStepMicros = interpolationStepMicros;
		this.turtleState = CacheBuilder.newBuilder()
		    .concurrencyLevel(TLApplicationConfig.TL_NUM_SIMULATION_CORES)
		    .weakKeys()
		    .build(new CacheLoader<Turtle, TurtleState>() {
                @Override
                public TurtleState load(Turtle key) throws Exception {
                    return new TurtleState();
                }
		    });

	}

	private static ScheduledExecutorService createScheduler(int poolSize) {
	    return Executors.newScheduledThreadPool(poolSize, new ThreadFactory() {
	        int count = 0;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TLSimulatorThread-" + (count++));
            }
	    });
	}

	public void shutdown() {
	    actionScheduler.shutdownNow();
	}

	public TLSimulationSettings getSettings() {
	    return settings;
	}

	public TurtleState getTurtleState(Turtle turtle) {
	    try {
            return turtleState.get(turtle);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
	}

	public void invokeAndWait(TLAction a, Turtle t) {
	    TLAwtUtilities.assertOffAwtThread();
	    try {
	        invokeAndWaitInterruptibly(a, getTurtleState(t));
	    } catch (InterruptedException e) {
	        throw Throwables.propagate(e);
	    }
	}

	private void invokeAndWaitInterruptibly(TLAction a, TurtleState t) throws InterruptedException {
        Lock l = lockingContext.getLock(t);
        l.lockInterruptibly();
        float spinEndTime = 0;
        try {
            float estimatedTimeMicros = a.getCompletionTime(t)
                    * MICROS_IN_SECOND;
            if (estimatedTimeMicros < interpolationStepMicros) {
                long startMicros = currentTimeMicros();
                a.perform(t, Float.MAX_VALUE);
                spinEndTime = startMicros + estimatedTimeMicros;
            }
        } finally {
            l.unlock();
        }

        /* If we completed the action without the need to schedule interpolation,
         * simply simulate execution and return
         */
        if (spinEndTime > 0) {
            spin(spinEndTime);
            return;
        }

        TLActionInterpolationRunnable r = new TLActionInterpolationRunnable(a, t);
        r.schedule();
        r.latch.await();
	}

	private static void spin(float endTimeMicros) {
	    while (currentTimeMicros() < endTimeMicros) {
	        /* spinning */
	    }
	}

	private static long currentTimeMicros() {
	    return System.nanoTime() / NANOS_IN_MICRO;
	}

	private class TLActionInterpolationRunnable implements Runnable {

	    public TLActionInterpolationRunnable(TLAction action, TurtleState state) {
            this.action = action;
            this.state = state;
            this.latch = new CountDownLatch(1);

            lastExecutionTimeMicros = currentTimeMicros();
        }

        private final TLAction action;
        private final TurtleState state;
	    private final CountDownLatch latch;

        private long lastExecutionTimeMicros;

        private long nextScheduledTimeMicros() {
            long estimatedTimeMicros = (long) Math.ceil(action.getCompletionTime(state)
                    * MICROS_IN_SECOND);
            return Math.min(estimatedTimeMicros, interpolationStepMicros);
        }

        private void schedule() {
            actionScheduler.schedule(this, nextScheduledTimeMicros(), TimeUnit.MICROSECONDS);
        }

        @Override
        public void run() {
            long time = currentTimeMicros();
            float elapsedSeconds = (time - lastExecutionTimeMicros) / MICROS_IN_SECOND;
            if (action.perform(state, elapsedSeconds) > 0) {
                latch.countDown();
            } else {
                lastExecutionTimeMicros = time;
                schedule();
            }
        }

	}
}
