package org.arper.turtle;

public class TLSimulationSettings {

    private volatile boolean paused = true;
    private volatile float animationSpeed = 1.0f;

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void unpause() {
        paused = false;
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(float animationSpeed) {
        if (animationSpeed <= 0) {
            throw new IllegalArgumentException("animation speed must be positive");
        }
        this.animationSpeed = animationSpeed;
    }

}
