package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.managers.hotkey.HotkeyData;

public class TLSwingPlayPauseButtonPlugin implements TLSwingPlugin {

    private WebButton playPauseButton;
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private TLSwingWindow window;

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        this.window = window;

        initComponents();
        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(playPauseButton, BorderLayout.WEST);
    }

    private void initComponents() {
        playPauseButton = TLSwingToolbarPlugin.createToolbarButton(window, 
                null, "Play/Pause", new HotkeyData(false, false, true, KeyEvent.VK_SPACE));

        try {
            playIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/play.png")));
        } catch (Exception e) {}
        try {
            pauseIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause.png")));
        } catch (Exception e) {}

        playPauseButton.setIcon(playIcon);
        playPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                playPauseAction();
            }
        });
    }

    private void playPauseAction() {
        TLSimulationSettings settings = TLSingleton.getContext().getSimulator().getSettings();
        synchronized (settings) {
            if (settings.isPaused()) {
                settings.unpause();
                window.fireSwingPluginEvent("play");
            } else {
                settings.pause();
                window.fireSwingPluginEvent("pause");
            }
        }
    }

    private void refreshPlayPauseButton() {
        TLSimulationSettings settings = TLSingleton.getContext().getSimulator().getSettings();
        if (!settings.isPaused()) {
            playPauseButton.setIcon(pauseIcon);
        } else {
            playPauseButton.setIcon(playIcon);
        }
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        if ("play".equals(name) || "pause".equals(name)) {
            refreshPlayPauseButton();
        }
    }

}
