package org.arper.turtle.impl;

import java.awt.Graphics2D;
import java.awt.Shape;

public interface TLRenderer {
    void preRender(Graphics2D g, Shape canvasClipRect);
    void render(Graphics2D g, Shape canvasClipRect);
    void markDirty();
}
