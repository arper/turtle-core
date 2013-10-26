package org.arper.turtle;

public class TLSimulationSettings {

    private volatile boolean paused = true;
    private volatile double animationSpeed = 1.0;

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public double getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(double animationSpeed) {
        if (animationSpeed <= 0) {
            throw new IllegalArgumentException("animation speed must be positive");
        }
        this.animationSpeed = animationSpeed;
    }

}
