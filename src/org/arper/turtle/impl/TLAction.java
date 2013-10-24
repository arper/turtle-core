package org.arper.turtle.impl;


public interface TLAction {
    public abstract double perform(TLMutableState m, double seconds);
    public abstract void execute(TLMutableState m);
}
