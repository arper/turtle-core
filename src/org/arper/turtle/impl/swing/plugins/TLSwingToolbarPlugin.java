package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.CodeSource;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.TLTurtle;
import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingStyles;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyData;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

public class TLSwingToolbarPlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        this.window = window;

        JComponent toolbar = TLSwingStyles.pad(createToolbar(), 4);
        window.addPluginLayer(TLSwingStyles.anchor(toolbar, BorderLayout.NORTH));
    }

    private TLSwingWindow window;
    private WebButton playPauseButton;
    private ImageIcon playIcon, pauseIcon;


    private JComponent createToolbar() {
        WebPanel tools = TLSwingStyles.transparent(new WebPanel(false));

        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));

        playPauseButton = createToolbarButton(null, "\u25B8", "Play/Pause", Hotkey.P);
        playPauseButton.setFont(new Font("Helvetica", Font.BOLD, 37));
        playPauseButton.setMargin(new Insets(2, 2, 2, 2));
        playPauseButton.setForeground(new Color(15, 159, 11));

        try {
            playIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/play.png")));
        } catch (Exception e) {}
        try {
            pauseIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause.png")));
        } catch (Exception e) {}

        playPauseButton.setText("");
        playPauseButton.setIcon(playIcon);

        playPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                playPauseAction();
            }
        });
        tools.add(playPauseButton);

        WebButton restartButton = createToolbarButton("icons/restart.png", "Restart",
                "Restart Application", Hotkey.R);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* TODO: restart */
            }
        });
        tools.add(restartButton);

        WebButton helpButton = createToolbarButton("icons/help2.png", "Help",
                "Help - Go to Online Documentation (http://stanford.edu/~alexryan/cgi-bin/turtledoc/)",
                Hotkey.F1);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpAction();
            }
        });

        WebButton updateButton = createToolbarButton("icons/update.png", "Update", "Download latest libraries", Hotkey.F2);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAction();
            }
        });
        tools.add(Box.createHorizontalGlue());
        tools.add(updateButton);
        tools.add(helpButton);

        return tools;
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
    //
    //    private void speedAction() {
    //        TLSimulationSettings settings = TLSingleton.getContext().getSimulator().getSettings();
    //        double val = speedSlider.getModel().getValue() * 1.0 / speedSlider.getModel().getMaximum();
    //        synchronized (settings) {
    //            settings.setAnimationSpeed((float) (Math.pow(36, val) / 6));
    //        }
    //    }

    private void helpAction() {
        final String DOC_URL = "http://stanford.edu/~alexryan/cgi-bin/turtledoc/";
        try {
            Desktop.getDesktop().browse(new URL(DOC_URL).toURI());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window,
                    "Unable to open documentation:\n" + e.getMessage(),
                    "Help Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAction() {
        try {
            CodeSource codeSource = TLTurtle.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            String libName = jarFile.getName();
            if (libName.matches("turtle-learning-v.+\\.jar")) {
                FileOutputStream fos = null;
                try {
                    URL libFile = new URL("http://stanford.edu/~alexryan/cgi-bin/" + libName);
                    URLConnection connection = libFile.openConnection();
                    if (connection.getLastModified() <= jarFile.lastModified()) {
                        JOptionPane.showMessageDialog(window,
                                "All libraries up-to-date!", "Update Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    ReadableByteChannel rbc = Channels.newChannel(libFile.openStream());
                    fos = new FileOutputStream(jarFile);
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    jarFile.setLastModified(connection.getLastModified());
                    JOptionPane.showMessageDialog(window,
                            "Successfully updated " + libName + " to latest version.\n" +
                                    "\nRe-run the program to load the new library code.",
                                    "Update Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(window,
                            "Could not update libraries: unexpected error.\n" + e.getMessage(),
                            "Error updating libraries",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) { /* ignored */ }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(window,
                        "Could not update libraries: unknown library location.",
                        "Error updating libraries",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private WebButton createToolbarButton(String icon, String backupText, String tooltip, HotkeyData h) {
        WebButton b = new WebButton();
        try {
            b.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(icon))));
        } catch (Exception e) {
            b.setText(backupText);
        }
        TLSwingStyles.applyStyle(b);
        HotkeyManager.registerHotkey (window, b, h, new ButtonHotkeyRunnable ( b, 50 ), TooltipWay.trailing );
        TooltipManager.setTooltip ( b, tooltip, TooltipWay.down, 0 );

        int size = 48;
        b.setPreferredSize(new Dimension(size, size));
        b.setMinimumSize(b.getPreferredSize());
        b.setMaximumSize(b.getPreferredSize());
        return b;
    }


    private void refreshPlayPauseButton() {
        TLSimulationSettings settings = TLSingleton.getContext().getSimulator().getSettings();
        if (!settings.isPaused()) {
            if (pauseIcon != null) {
                playPauseButton.setText("");
                playPauseButton.setIcon(pauseIcon);
            } else {
                playPauseButton.setText("Play");
                playPauseButton.setIcon(null);
            }
        } else {
            if (playIcon != null) {
                playPauseButton.setText("");
                playPauseButton.setIcon(playIcon);
            } else {
                playPauseButton.setText("Pause");
                playPauseButton.setIcon(null);
            }
        }
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        if ("play".equals(name) || "pause".equals(name)) {
            refreshPlayPauseButton();
        }
    }
}
