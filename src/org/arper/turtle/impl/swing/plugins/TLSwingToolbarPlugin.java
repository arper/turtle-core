package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
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
        initButtons();

        JComponent tools = createToolbar();
        TLSwingStyles.setPainter(tools, TLSwingStyles.getPanelPainter());

        window.add(tools, BorderLayout.NORTH);
    }

    private TLSwingWindow window;
    private WebButton playPauseButton;
    private WebButton restartButton;
    private WebButton updateButton;
    private WebButton helpButton;
    private ImageIcon playIcon, pauseIcon;

    private JComponent createToolbar() {
        JComponent tools = new WebPanel(false) {
            @Override
            public Component add(Component comp) {
                super.add(comp);
                super.add(Box.createRigidArea(new Dimension(2,2)));
                return comp;
            }
        };

        tools.setBackground(null);
        tools.setOpaque(false);
        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));

        tools.add(playPauseButton);
        tools.add(restartButton);
//        tools.add(Box.createHorizontalGlue());
        tools.add(updateButton);
        tools.add(helpButton);

        tools.add(Box.createHorizontalGlue());

        tools.add(createToolbarButton("icons/play.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/pause.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/restart.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/help.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/console.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/config.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/screenshot.png", "", "", Hotkey.A));
        tools.add(createToolbarButton("icons/properties.png", "", "", Hotkey.A));


        return TLSwingStyles.pad(tools, 2);
    }


    private void initButtons() {
        playPauseButton = createToolbarButton(null, "", "Play/Pause", Hotkey.P);
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

        restartButton = createToolbarButton("icons/restart.png", "Restart",
                "Restart Application", Hotkey.R);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* TODO: restart */
            }
        });

        helpButton = createToolbarButton("icons/help.png", "Help",
                "Help - Go to Online Documentation (http://stanford.edu/~alexryan/cgi-bin/turtledoc/)",
                Hotkey.F1);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpAction();
            }
        });

        updateButton = createToolbarButton("icons/screenshot.png", "Update", "Download latest libraries", Hotkey.F2);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAction();
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

        Dimension size = new Dimension(44, 38);
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);
        return b;
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
