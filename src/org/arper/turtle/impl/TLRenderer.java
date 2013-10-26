package org.arper.turtle.impl;

import java.awt.Graphics2D;

public interface TLRenderer {
    void preRender(Graphics2D g);
    void render(Graphics2D g);
    void markDirty();
}
