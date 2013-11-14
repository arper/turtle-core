package org.arper.turtle.internal.swing;


public interface TLSwingPlugin {
    void initSwingPlugin(TLSwingWindow window);
    void onSwingPluginEvent(TLSwingWindow window, String name, Object... args);
}
