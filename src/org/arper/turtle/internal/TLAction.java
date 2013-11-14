package org.arper.turtle.internal;


public interface TLAction {
    float perform(TLTurtleState t, float seconds);
    float getCompletionTime(TLTurtleState t);
}
