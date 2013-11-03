package org.arper.turtle.impl.swing;


public interface TLSwingPlugin {
    void initSwingPlugin(TLSwingWindow window);
    void onSwingPluginEvent(TLSwingWindow window, String name, Object... args);
}
