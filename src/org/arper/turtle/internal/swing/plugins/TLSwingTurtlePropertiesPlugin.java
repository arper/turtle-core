package org.arper.turtle.internal.swing.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;

import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingStyles;
import org.arper.turtle.internal.swing.TLSwingWindow;

import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;

public class TLSwingTurtlePropertiesPlugin implements TLSwingPlugin {

    private JComponent propertiesOverlay;
    private WebToggleButton propertiesControlToggle;
    
    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        propertiesOverlay = new WebPanel();
        TLSwingStyles.applyStyle(propertiesOverlay);
        
        propertiesControlToggle = createOverlayControlButton();
        
        window.addViewportLayer(TLSwingStyles.noLayout(propertiesOverlay));
    }
    
    private WebToggleButton createOverlayControlButton() {
        WebToggleButton toggleButton = new WebToggleButton();
        TLSwingStyles.applyStyle(toggleButton);
        
        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                refreshOverlayVisibility();
            }
        });
        
        return toggleButton;
    }
    
    private void refreshOverlayVisibility() {
        propertiesOverlay.setVisible(propertiesControlToggle.isSelected());
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }

}
