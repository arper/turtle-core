package org.arper.turtle;

public abstract class TLController {
    public abstract void run(TLApplication app,
                             Object[] args);

    public String getName() {
        return "controller";
    }
}
