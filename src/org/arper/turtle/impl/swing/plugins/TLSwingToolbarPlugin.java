package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingStyles;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.HotkeyData;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.google.common.base.Throwables;

public abstract class TLSwingToolbarPlugin implements TLSwingPlugin {
    
    public static class Top extends TLSwingToolbarPlugin {
        @Override
        protected String getDirection() {
            return BorderLayout.NORTH;
        }
    }
    
    public static class Bottom extends TLSwingToolbarPlugin {
        @Override
        protected String getDirection() {
            return BorderLayout.SOUTH;
        }
    }
    
    private JPanel westPanel;
    private JPanel eastPanel;
    
    protected abstract String getDirection();
    
    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        JComponent tools = createToolbar();
        TLSwingStyles.setPainter(tools, TLSwingStyles.getPanelPainter());
        
        window.getContentPane().add(tools, getDirection());
    }
    
    public void add(JComponent comp, String direction) {
        if (BorderLayout.WEST.equals(direction)) {
            westPanel.add(comp);
        } else if (BorderLayout.EAST.equals(direction)) {
            eastPanel.add(comp);
        } else {
            throw new IllegalArgumentException("unsupported direction " + direction);
        }
    }

    private JComponent createToolbar() {
        JComponent tools = TLSwingStyles.transparent(new WebPanel(false));
        tools.setLayout(new BorderLayout());
        
        westPanel = TLSwingStyles.transparent(new WebPanel(false));
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.X_AXIS));
        
        eastPanel = TLSwingStyles.transparent(new WebPanel(false));
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.X_AXIS));
        
        tools.add(westPanel, BorderLayout.WEST);
        tools.add(eastPanel, BorderLayout.EAST);

        return TLSwingStyles.pad(tools, 2);
    }

    public static WebButton createToolbarButton(JFrame window, String icon, 
            String tooltip, HotkeyData h) {
        WebButton b = new WebButton();
        styleToolbarButton(b, window, icon, tooltip, h);
        return b;
    }
    
    public static void styleToolbarButton(AbstractButton b, JFrame window, String icon, 
            String tooltip, HotkeyData h) {

        if (icon != null) {
            try {
                b.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(icon))));
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }
        
        TLSwingStyles.applyStyle(b);
        HotkeyManager.registerHotkey (window, b, h, 
                new ButtonHotkeyRunnable ( b, 50 ), TooltipWay.trailing );
        TooltipManager.setTooltip ( b, tooltip, TooltipWay.down, 0 );

        Dimension size = new Dimension(44, 38);
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }

}
