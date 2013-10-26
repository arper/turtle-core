package org.arper.turtle.impl;


public interface TLAction {
    float perform(TLTurtleState t, float seconds);
    float getCompletionTime(TLTurtleState t);
}
