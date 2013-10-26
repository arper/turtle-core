package org.arper.turtle.impl;


public class TLActions {

    public static TLAction moveTo(float x, float y) {
        return new MoveToAction(x, y);
    }

    public static TLAction turn(float turnAmount) {
        return new TurnAction(turnAmount);
    }

    public static TLAction lookAt(float x, float y) {
        return new LookAtAction(x, y);
    }

    public static TLAction head(float heading) {
        return new HeadingAction(heading);
    }

    public static TLAction forward(float amount) {
        return new ForwardAction(amount);
    }

    public static TLAction pause(float pauseAmount, boolean showStatus) {
        return new PauseAction(pauseAmount, showStatus);
    }

    public static TLAction empty() {
        return EMPTY_ACTION;
    }

    private static float doHeading(float heading, TLTurtleState t, float seconds) {
        float diff = turnAmount(t.heading, heading);
        if (Math.abs(diff) <= seconds * t.turningSpeed) {
            t.heading = heading;
            return seconds - Math.abs(diff) / t.turningSpeed;
        } else {
            t.heading += Math.signum(diff) * t.turningSpeed * seconds;
            return 0;
        }
    }

    private static float doLookAt(float x, float y, TLTurtleState t, float seconds) {
        TLVector v = new TLVector(x, y).subtract(t.location);
        float turnAmount = turnAmount(t.heading, v.angle());
        if (Math.abs(turnAmount) <= seconds * t.turningSpeed) {
            t.heading += turnAmount;
            return seconds - Math.abs(turnAmount) / t.turningSpeed;
        } else {
            t.heading += Math.signum(turnAmount) * t.turningSpeed * seconds;
            return 0;
        }
    }

    private static float doForward(float amount, TLTurtleState t, float seconds) {
        if (amount <= t.movementSpeed * seconds) {
            TLVector end = new TLVector(t.location).add(TLVector.unitVectorInDirection(t.heading).scale(amount));
            t.location.setLocation(end.x, end.y);
            return seconds - amount / t.movementSpeed;
        } else {
            TLVector end = new TLVector(t.location).add(TLVector.unitVectorInDirection(t.heading).scale(seconds * t.movementSpeed));
            t.location.setLocation(end.x, end.y);
            return 0;
        }
    }

    private static float turnAmount(float heading, float target) {
        float diff = (target - heading) % (TLVector.TWO_PI_F);
        if (diff < -TLVector.PI_F) {
            diff += TLVector.TWO_PI_F;
        }
        else if (diff > TLVector.PI_F) {
            diff -= TLVector.TWO_PI_F;
        }
        return diff;
    }

    private static class MoveToAction implements TLAction {
        public MoveToAction(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private final float x;
        private final float y;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            seconds = doLookAt(x, y, t, seconds);
            if (seconds > 0) {
                seconds = doForward(new TLVector(x, y).subtract(t.location).length(), t, seconds);
            }
            return seconds;
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            TLVector v = new TLVector(x, y).subtract(t.location);
            return Math.abs(turnAmount(t.heading, v.angle())) / t.turningSpeed
                    + v.length() / t.movementSpeed;
        }
    }

    private static class ForwardAction implements TLAction {

        public ForwardAction(float amount) {
            this.amount = amount;
        }

        private float amount;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            if (amount <= t.movementSpeed * seconds) {
                TLVector end = new TLVector(t.location).add(TLVector.unitVectorInDirection(t.heading).scale(amount));
                t.location.setLocation(end.x, end.y);
                amount = 0;
                return seconds - amount / t.movementSpeed;
            } else {
                TLVector end = new TLVector(t.location).add(TLVector.unitVectorInDirection(t.heading).scale(seconds * t.movementSpeed));
                t.location.setLocation(end.x, end.y);
                amount -= seconds * t.movementSpeed;
                return 0;
            }
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            return amount / t.movementSpeed;
        }

    }

    private static class LookAtAction implements TLAction {

        public LookAtAction(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private final float x;
        private final float y;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            return doLookAt(x, y, t, seconds);
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            TLVector v = new TLVector(x, y).subtract(t.location);
            return Math.abs(turnAmount(t.heading, v.angle())) / t.turningSpeed;
        }
    }

    private static class HeadingAction implements TLAction {

        public HeadingAction(float heading) {
            this.heading = heading;
        }

        private final float heading;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            return doHeading(heading, t, seconds);
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            return Math.abs(turnAmount(t.heading, heading)) / t.turningSpeed;
        }

    }

    private static class TurnAction implements TLAction {
        public TurnAction(float turnAmount) {
            this.turnAmount = turnAmount;
        }

        private float turnAmount;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            if (Math.abs(turnAmount) <= seconds * t.turningSpeed) {
                t.heading += turnAmount;
                seconds -= Math.abs(turnAmount) / t.turningSpeed;
                turnAmount = 0;
                return seconds;
            } else {
                float turn = Math.signum(turnAmount) * seconds * t.turningSpeed;
                turnAmount -= turn;
                t.heading += turn;
                return 0;
            }
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            return Math.abs(turnAmount) / t.turningSpeed;
        }
    }

    private static class PauseAction implements TLAction {

        public PauseAction(float pauseAmount, boolean showStatus) {
            this.pauseAmount = pauseAmount;
            this.showStatus = showStatus;
        }

        private final boolean showStatus;
        private float pauseAmount;
        private String initialStatus;
        private boolean firstRun = true;

        @Override
        public float perform(TLTurtleState t, float seconds) {
            if (firstRun) {
                firstRun = false;
                initialStatus = t.status;
            }

            if (pauseAmount < seconds) {
                float retval = seconds - pauseAmount;
                pauseAmount = 0;
                if (showStatus) {
                    t.status = initialStatus;
                }
                return retval;
            } else {
                pauseAmount -= seconds;
                if (showStatus) {
                    t.status = String.format("%.1f", pauseAmount);
                }
                return 0;
            }
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            return pauseAmount;
        }
    }

    private static final TLAction EMPTY_ACTION = new TLAction() {

        @Override
        public float perform(TLTurtleState t, float seconds) {
            return seconds;
        }

        @Override
        public float getCompletionTime(TLTurtleState t) {
            return 0;
        }

    };
}
