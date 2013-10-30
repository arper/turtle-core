package org.arper.turtle.ui;

import java.awt.event.KeyListener;

public interface TLWindow {
    void setVisible(boolean visible);
    void setTitle(String title);
    void addKeyListener(KeyListener listener);
    TLCanvas getCanvas();
    TLConsole getConsole();
}
