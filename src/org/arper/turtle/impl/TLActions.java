package org.arper.turtle.impl;


public class TLActions {
    
    public static TLAction moveTo(double x, double y) {
        return new MoveToAction(x, y);
    }
    
    public static TLAction turn(double turnAmount) { 
        return new TurnAction(turnAmount);
    }
    
    public static TLAction pause(double pauseAmount, boolean showStatus) {
        return new PauseAction(pauseAmount, showStatus);
    }
    
    public static TLAction empty() {
        return EMPTY_ACTION;
        
    }
        
    private static class MoveToAction implements TLAction {
        private double x, y;
        public MoveToAction(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public double perform(TLMutableState t, double seconds) {
            TLVector v = new TLVector(x - t.getX(), y - t.getY());
            double len = v.length();
            double requiredTime = len / t.getSpeed();

            if (requiredTime < seconds) {
                /* We can get there in less time than we have available */
                t.setLocation(x, y);
                return seconds - requiredTime;
            } else {
                /* We can only get part-way there */
                v = v.normalize();
                TLVector nextPos = new TLVector(t.getX(), t.getY()).add(v.scale(t.getSpeed() * seconds));
                t.setLocation(nextPos.getX(), nextPos.getY());
                return 0;
            }
        }

        @Override
        public void execute(TLMutableState t) {
            t.setLocation(x, y);
        }
    }

    private static class TurnAction implements TLAction {
        private double turnAmount;
        public TurnAction(double turnAmount) {
            this.turnAmount = turnAmount;
        }

        @Override
        public double perform(TLMutableState t, double seconds) {
            double required = Math.abs(turnAmount) / t.getTurnRate();

            if (required < seconds) {
                t.setHeading(t.getHeading() + turnAmount);
                turnAmount = 0;
                return seconds - required;
            } else {
                double update = seconds * t.getTurnRate() * (turnAmount > 0? 1 : -1);
                t.setHeading(t.getHeading() + update);
                turnAmount -= update;
                return 0;
            }
        }

        @Override
        public void execute(TLMutableState t) {
            t.setHeading(t.getHeading() + turnAmount);
        }
    }

    private static class PauseAction implements TLAction {
        private double pauseAmount;
        private boolean showStatus;
        private String initialStatus;
        private boolean firstRun = true;
        
        public PauseAction(double pauseAmount, boolean showStatus) {
            this.pauseAmount = pauseAmount;
            this.showStatus = showStatus;
        }

        @Override
        public double perform(TLMutableState t, double seconds) {
            if (firstRun) {
                firstRun = false;
                initialStatus = t.getStatus();
            }
            
            if (pauseAmount < seconds) {
                double retval = seconds - pauseAmount;
                pauseAmount = 0;
                if (showStatus) {
                    t.setStatus(initialStatus);
                }
                return retval;
            } else {
                pauseAmount -= seconds;
                if (showStatus) {
                    t.setStatus(String.format("%.1f", pauseAmount));
                }
                return 0;
            }
        }

        @Override
        public void execute(TLMutableState t) {
            pauseAmount = 0;
        }
    }

    private static final TLAction EMPTY_ACTION = new TLAction() {

        @Override
        public double perform(TLMutableState t, double seconds) {
            /* do nothing, pass back full seconds amount */
            return seconds;
        }

        @Override
        public void execute(TLMutableState t) {
            /* do nothing */
        }
        
    };
}
