package org.arper.turtle.impl;


public interface TLAction {
    float perform(TurtleState t, float seconds);
    float getCompletionTime(TurtleState t);
}
