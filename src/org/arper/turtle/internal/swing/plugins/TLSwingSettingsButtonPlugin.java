package org.arper.turtle.internal.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingStyles;
import org.arper.turtle.internal.swing.TLSwingUtilities;
import org.arper.turtle.internal.swing.TLSwingWindow;

import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.hotkey.Hotkey;

public class TLSwingSettingsButtonPlugin implements TLSwingPlugin {

    private JComponent settingsPanel;
    private JToggleButton settingsButton;

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        settingsButton = new WebToggleButton();
        TLSwingToolbarPlugin.styleToolbarButton(settingsButton, window,
                "icons/config.png", "Settings", Hotkey.F4);

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                settingsAction();
            }
        });

        settingsPanel = new WebPanel(false);
        
        TLSwingStyles.setPainter(settingsPanel, TLSwingStyles.getPanelPainter());
        settingsPanel.setLayout(new BorderLayout());
        settingsPanel.add(new JLabel("Hello settings!"), BorderLayout.NORTH);
        settingsPanel.add(new JSlider(), BorderLayout.SOUTH);
        settingsPanel.setSize(settingsPanel.getPreferredSize());

        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(settingsButton, BorderLayout.EAST);
        window.addViewportLayer(TLSwingStyles.noLayout(settingsPanel));

        TLSwingUtilities.anchorComponent(
                settingsPanel, new Point2D.Double(1, 0),
                settingsButton, new Point2D.Double(1, 0),
                true,
                true);
        settingsPanel.setVisible(false);
    }

    private void settingsAction() {
        settingsPanel.setVisible(settingsButton.isSelected());
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }

}