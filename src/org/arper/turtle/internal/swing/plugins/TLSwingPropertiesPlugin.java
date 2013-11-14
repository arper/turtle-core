package org.arper.turtle.internal.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.managers.hotkey.Hotkey;

public class TLSwingPropertiesPlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        WebButton button = TLSwingToolbarPlugin.createToolbarButton(window, 
                "icons/properties.png", "Show Turtle Properties", Hotkey.F3);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
//                restartAction();
            }
        });
        
        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(button, BorderLayout.EAST);
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* TODO: properties updates on turtle click */
    }

}
