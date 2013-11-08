package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.managers.hotkey.Hotkey;

public class TLSwingHelpButtonPlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        WebButton button = TLSwingToolbarPlugin.createToolbarButton(window, 
                "icons/help.png", "Open Help Documentation", Hotkey.F1);
        
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                helpAction();
            }
        });
        
        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(button, BorderLayout.EAST);
    }
    
    private void helpAction() {
        /* TODO: helpAction */
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }

}
