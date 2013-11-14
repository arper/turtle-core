package org.arper.turtle.ui;

import java.awt.Color;
import java.awt.Font;

import org.arper.turtle.TLTurtle;
import org.arper.turtle.internal.TLRenderer;

public interface TLCanvas {
    TLRenderer getRenderer(TLTurtle turtle);
    void clear();
    void setBackground(Color color);

    public void drawString(String text, double x, double y);
    public void drawString(String text, double x, double y, 
            Color c, int alignment, Font font);
}
