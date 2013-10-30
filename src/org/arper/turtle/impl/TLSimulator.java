package org.arper.turtle.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.TLTurtle;
import org.arper.turtle.impl.j2d.TLAwtUtilities;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


public class TLSimulator {

    public static final float SIMULATION_EPSILON = .0001f;

	public TLSimulator(int numSimulationCores,
	                   long interpolationStepMicros,
                       long maxInterpolationStutterMicros,
	                   long maxBlockingSimulationPeriodMicros) {
		settings = new TLSimulationSettings();
		actionScheduler = createScheduler(numSimulationCores);

		this.interpolationStepMicros = interpolationStepMicros;
		this.maxInterpolationStutterMicros = maxInterpolationStutterMicros;
		this.maxBlockingSimulationPeriodMicros = maxBlockingSimulationPeriodMicros;
		this.turtleState = CacheBuilder.newBuilder()
		    .concurrencyLevel(numSimulationCores)
		    .weakKeys()
		    .build(new CacheLoader<TLTurtle, TLTurtleState>() {
                @Override
                public TLTurtleState load(TLTurtle key) throws Exception {
                    return new TLTurtleState();
                }
		    });

	}

    private final TLSimulationSettings settings;
    private final long interpolationStepMicros;
    private final long maxInterpolationStutterMicros;
    private final long maxBlockingSimulationPeriodMicros;
    private final ScheduledExecutorService actionScheduler;

    private final LoadingCache<TLTurtle, TLTurtleState> turtleState;

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

	public TLTurtleState getTurtleState(TLTurtle turtle) {
	    try {
            return turtleState.get(turtle);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
	}

	public void invokeAndWait(TLAction a, TLTurtle t) {
	    TLAwtUtilities.assertOffAwtThread();

	    try {
	        invokeAndWaitInterruptibly(a, t);
	    } catch (InterruptedException e) {
	        throw Throwables.propagate(e);
	    }
	}

	private void markDirty(TLTurtle t) {
	    TLSingletonContext.get().getWindow().getCanvas().getRenderer(t).markDirty();
	}

	private float realToSimulationTime(float time) {
	    return settings.isPaused()? 0 : time * settings.getAnimationSpeed();
	}

	private float simulationToRealTime(float time) {
	    return settings.isPaused()? Float.MAX_VALUE : time / settings.getAnimationSpeed();
	}

	private void invokeAndWaitInterruptibly(TLAction a, TLTurtle t) throws InterruptedException {
        float spinEndTime = 0;
        synchronized (t) {
            float estimatedTimeMicros = a.getCompletionTime(getTurtleState(t))
                    * MICROS_IN_SECOND;
            estimatedTimeMicros = simulationToRealTime(estimatedTimeMicros);

            if (estimatedTimeMicros < maxBlockingSimulationPeriodMicros) {
                long startMicros = currentTimeMicros();
                a.perform(getTurtleState(t), Float.MAX_VALUE);
                spinEndTime = startMicros + estimatedTimeMicros;
            }
        }

        /* If we completed the action without the need to schedule interpolation,
         * simply simulate execution and return
         */
        if (spinEndTime > 0) {
            markDirty(t);
            spin(spinEndTime);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
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

    private static final long NANOS_IN_MICRO = 1000L;
    private static final float MICROS_IN_SECOND = 1000000.0f;

	private static long currentTimeMicros() {
	    return System.nanoTime() / NANOS_IN_MICRO;
	}

	private class TLActionInterpolationRunnable implements Runnable {

	    public TLActionInterpolationRunnable(TLAction action, TLTurtle turtle) {
            this.action = action;
            this.turtle = turtle;
            this.latch = new CountDownLatch(1);

            lastExecutionTimeMicros = currentTimeMicros();
        }

        private final TLAction action;
        private final TLTurtle turtle;
	    private final CountDownLatch latch;

        private long lastExecutionTimeMicros;

        private long nextScheduleTimeMicros() {
//            long estimatedTimeMicros = (long) Math.ceil(
//                    simulationToRealTime(action.getCompletionTime(getTurtleState(turtle)))
//                    * MICROS_IN_SECOND);
//            return Math.min(estimatedTimeMicros, interpolationStepMicros);
            return interpolationStepMicros;
        }

        private void schedule() {
            actionScheduler.schedule(this, nextScheduleTimeMicros(), TimeUnit.MICROSECONDS);
        }

        @Override
        public void run() {
            long time = currentTimeMicros();
            float elapsedMicros = (time - lastExecutionTimeMicros);
            if (elapsedMicros > maxInterpolationStutterMicros) {
                elapsedMicros = maxInterpolationStutterMicros;
            }

            float elapsedSeconds = realToSimulationTime(elapsedMicros) / MICROS_IN_SECOND;

            if (action.perform(getTurtleState(turtle), elapsedSeconds) > 0) {
                latch.countDown();
            } else {
                lastExecutionTimeMicros = time;
                schedule();
            }
            
            if (!settings.isPaused()) {
                markDirty(turtle);
            }
        }

	}
}
