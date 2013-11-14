package org.arper.turtle.internal.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.managers.hotkey.Hotkey;

public class TLSwingRestartButtonPlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        WebButton button = TLSwingToolbarPlugin.createToolbarButton(window, 
                "icons/restart.png", "Restart Simulation", Hotkey.F5);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                restartAction();
            }
        });
        
        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(button, BorderLayout.WEST);
    }
    
    private void restartAction() {
        /* TODO: restartAction */
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }

}
