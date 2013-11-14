package org.arper.turtle.internal.swing.plugins;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.arper.turtle.internal.TLSingleton;
import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingStyles;
import org.arper.turtle.internal.swing.TLSwingWindow;

public class TLSwingPauseOverlayPlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        pauseOverlayComponent = createPauseOverlay();

        window.addViewportLayer(pauseOverlayComponent);
    }

    private JComponent pauseOverlayComponent;

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        if ("app_start".equals(name) ||
                "pause".equals(name) ||
                "play".equals(name)) {
            refreshPauseOverlay();
        }
    }

    private void refreshPauseOverlay() {
        pauseOverlayComponent.setVisible(TLSingleton.getContext().getSimulator()
                .getSettings().isPaused());
    }

    private JComponent createPauseOverlay() {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause2.png")));
            JLabel iconLabel = TLSwingStyles.transparent(new JLabel(icon, SwingConstants.CENTER));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel pauseLabel = TLSwingStyles.transparent(new JLabel("Paused"));
            pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pauseLabel.setFont(new Font("Helvetica", Font.BOLD, 128));
            pauseLabel.setForeground(new Color(0f, 0f, 0f, .03f));
            JLabel instrLabel = TLSwingStyles.transparent(new JLabel("spacebar or button at left to run"));
            instrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            instrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            instrLabel.setFont(new Font("Helvetica", Font.BOLD, 28));
            instrLabel.setForeground(new Color(0f, 0f, 0f, .03f));

            JComponent pause = Box.createVerticalBox();
            pause.add(Box.createVerticalGlue());
            pause.add(iconLabel);
            pause.add(pauseLabel);
            pause.add(instrLabel);
            pause.add(Box.createVerticalGlue());
            return pause;
        } catch (Exception e) {
            JLabel l = new JLabel("||");
            l.setFont(new Font("Helvetica", Font.BOLD, 256));
            l.setForeground(new Color(0.0f, 0.0f, 0.0f, 0.04f));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setHorizontalTextPosition(SwingConstants.CENTER);
            return l;
        }
    }

}
