package org.arper.turtle.impl.j2d;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
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
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ButtonUI;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.TLTurtle;
import org.arper.turtle.impl.TLSingletonContext;
import org.arper.turtle.ui.TLCanvas;
import org.arper.turtle.ui.TLConsole;
import org.arper.turtle.ui.TLWindow;

import com.alee.extended.panel.WebCollapsiblePane;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.button.WebButtonUI;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.laf.text.WebTextPane;
import com.alee.managers.hotkey.ButtonHotkeyRunnable;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyData;
import com.alee.managers.hotkey.HotkeyManager;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;


//import com.alee.laf.WebLookAndFeel;
//import com.alee.laf.button.WebButtonUI;

@SuppressWarnings("serial")
public class TLJ2DWindow extends WebFrame implements TLWindow {
	private TLJ2DCanvas canvas;
	private JComponent pauseOverlay;
	private JButton playButton;
	private ImageIcon playIcon, pauseIcon;
	private JButton restartButton;
    private JButton helpButton;
    private JButton updateButton;
	private JSlider speedSlider;
	private TLConsole console;

	static {
	    try {
	        WebLookAndFeel.install();
	        WebLookAndFeel.setDecorateAllWindows(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public TLJ2DWindow(int canvasWidth, int canvasHeight) {
	    canvas = new TLJ2DCanvas(canvasWidth, canvasHeight, 5);
	    pauseOverlay = createPauseOverlay();
	    layoutComponents(createToolbar(), pauseOverlay,
	            canvas, createConsoleComponent());
	    
        setWindowIcon("icons/turtle.png");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();
        refreshPlayPauseDisplay();
	}

	public TLJ2DWindow() {
		this(Toolkit.getDefaultToolkit().getScreenSize().width / 4,
				Toolkit.getDefaultToolkit().getScreenSize().height / 4);
	}

	private void layoutComponents(JComponent tools, JComponent pauseOverlay,
	        JComponent canvas, JComponent consoleComponent) {
	    /* Overlay */
        JPanel overlay = transparent(new WebPanel(new BorderLayout()));
        overlay.add(pad(tools, 4), BorderLayout.NORTH);
        overlay.add(pauseOverlay, BorderLayout.CENTER);

        /* Console */
        consoleComponent.setPreferredSize(new Dimension(400, 300));

        WebCollapsiblePane consoleFlyout = new WebCollapsiblePane();
        consoleFlyout.setContent(consoleComponent);
        consoleFlyout.setContentMargin(0);
        consoleFlyout.setTitlePanePostion (SwingConstants.TOP);
        consoleFlyout.setStateIconPostion(SwingConstants.LEFT);
        swapIcons(consoleFlyout);
        consoleFlyout.getExpandButton().setPainter(TLJ2DStyles.getButtonPainter());
        consoleFlyout.getExpandButton().setText("  Console");
        consoleFlyout.getExpandButton().setFontSize(13);
        consoleFlyout.getExpandButton().setFontName("Lucida");
        consoleFlyout.getExpandButton().setForeground(Color.white);

        consoleFlyout.getHeaderPanel().setUndecorated(true);
        consoleFlyout.setExpanded(false, false);
        TLJ2DStyles.setUndecorated(consoleFlyout);

        /* Canvas */
	    JScrollPane canvasPane = transparent(new WebScrollPane(canvas, false, false));

	    /* Layering */
	    JPanel layeredContainer = transparent(new WebPanel()); // TLJ2DStyles.styled(new WebPanel());
	    layeredContainer.setLayout(new OverlayLayout(layeredContainer));
	    layeredContainer.add(anchored(pad(consoleFlyout, 4), BorderLayout.SOUTH, BorderLayout.WEST));
	    layeredContainer.add(overlay);
	    layeredContainer.add(canvasPane);

	    getContentPane().add(layeredContainer);
	}

	private JComponent anchored(JComponent comp, String... direction) {
	    JComponent parent = comp;
	    for (String dir : direction) {
	        JPanel nextParent = new WebPanel(new BorderLayout());
	        nextParent.setOpaque(false);
	        nextParent.add(parent, dir);
	        parent = nextParent;
	    }
	    return parent;
	}

	private JComponent createConsoleComponent() {
	    WebTextPane textPane = transparent(new WebTextPane() {
	        @Override
	        public void paintComponent(Graphics g) {
	            g.setColor(getBackground());
	            g.fillRect(getX(), getY(), getWidth(), getHeight());
	            super.paintComponent(g);
	        }
	    });
	    textPane.setBackground(new Color(0, 0, 0, 192));
	    WebTextField textField = transparent(new WebTextField());
        textField.setBackground(new Color(0, 0, 0, 128));
        textField.setForeground(Color.white);

	    console = TLJ2DConsoleImpl.create(textField, textPane);

	    WebPanel comp = new WebPanel(new BorderLayout()) {
	        @Override
	        public void paintComponent(Graphics g) {
	            g.setColor(getBackground());
	            ((Graphics2D)g).fill(getBounds());
	        }
	    };
	    comp.add(textField, BorderLayout.SOUTH);
	    comp.add(transparent(new WebScrollPane(textPane, true, false)), BorderLayout.CENTER);
	    comp.setBackground(new Color(255, 255, 255, 32));
	    comp.setOpaque(false);
	    return comp;
	}

	private <T extends JComponent> T transparent(T comp) {
	    comp.setBackground(new Color(0, 0, 0, 0));
	    comp.setOpaque(false);

	    if (comp instanceof JScrollPane) {
	        ((JScrollPane)comp).setViewportBorder(BorderFactory.createEmptyBorder());
	        ((JScrollPane)comp).getViewport().setOpaque(false);
	    }
	    return comp;
	}

	private void swapIcons(WebCollapsiblePane pane) {
	    ImageIcon collapsed = pane.getCollapseIcon();
	    pane.setCollapseIcon(pane.getExpandIcon());
	    pane.setExpandIcon(collapsed);
	}

    private JComponent createPauseOverlay() {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause2.png")));
            JLabel iconLabel = transparent(new JLabel(icon, SwingConstants.CENTER));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel pauseLabel = transparent(new JLabel("Paused"));
            pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pauseLabel.setFont(new Font("Helvetica", Font.BOLD, 128));
            pauseLabel.setForeground(new Color(0f, 0f, 0f, .03f));
            JLabel instrLabel = transparent(new JLabel("spacebar or button at left to run"));
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

    private JComponent pad(JComponent comp, int padding) {
        JComponent parent = new JPanel(new BorderLayout());
        parent.setOpaque(comp.isOpaque());
        parent.add(comp, BorderLayout.CENTER);
        parent.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        return parent;
    }

    private JComponent createToolbar() {
        WebPanel tools = transparent(new WebPanel(false));

        tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));

        playButton = createToolbarButton(null, "\u25B8", "Play/Pause", Hotkey.P);
        playButton.setFont(new Font("Helvetica", Font.BOLD, 37));
        playButton.setMargin(new Insets(2, 2, 2, 2));
        playButton.setForeground(new Color(15, 159, 11));

        try {
            playIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/play.png")));
        } catch (Exception e) {}
        try {
            pauseIcon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause.png")));
        } catch (Exception e) {}

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                playAction();
            }
        });
        tools.add(playButton);
//        controlPanel.addSeparator();

        speedSlider = new JSlider(JSlider.VERTICAL, 0, 12000, 6000);
        speedSlider.setMajorTickSpacing(3000);
        speedSlider.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        speedSlider.setFocusable(false);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                speedAction();
            }
        });
        Hashtable<Integer, JComponent> labels = new Hashtable<Integer, JComponent>();
        labels.put(0, new JLabel("1/6"));
        labels.put(6000, new JLabel("Speed"));
        labels.put(12000, new JLabel("6"));
        speedSlider.setLabelTable(labels);
        speedSlider.setPaintTicks(true);
//        controlPanel.add(makeLabel("Fast"));
//        controlPanel.add(speedSlider);
//        controlPanel.add(makeLabel("Slow"));
//        controlPanel.addSeparator();

        restartButton = createToolbarButton("icons/restart.png", "Restart",
                "Restart Application", Hotkey.R);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* TODO: restart */
//                TLApplication.restart(TLWindow.this);
            }
        });
        tools.add(restartButton);

        helpButton = createToolbarButton("icons/help2.png", "Help",
                "Help - Go to Online Documentation (http://stanford.edu/~alexryan/cgi-bin/turtledoc/)",
                Hotkey.F1);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpAction();
            }
        });

        updateButton = createToolbarButton("icons/update.png", "Update", "Download latest libraries", Hotkey.F2);
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

    private JButton createToolbarButton(String icon, String backupText, String tooltip, HotkeyData h) {
        JButton b = new WebButton();
        try {
            b.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(icon))));
        } catch (Exception e) {
            b.setText(backupText);
        }
        TLJ2DStyles.applyStyle(b);
        HotkeyManager.registerHotkey (this, b, h, new ButtonHotkeyRunnable ( b, 50 ), TooltipWay.trailing );
        TooltipManager.setTooltip ( b, tooltip, TooltipWay.down, 0 );

        b.setPreferredSize(new Dimension(45, 45));
        b.setMinimumSize(new Dimension(45, 45));
        b.setMaximumSize(new Dimension(45, 45));
        return b;
    }

	private void setWindowIcon(String path) {
        try {
            setIconImage(ImageIO.read(ClassLoader.getSystemResource(path)));
        } catch (Exception e) {}
	}

	private void refreshPlayPauseDisplay() {
        TLSimulationSettings settings = TLSingletonContext.get().getSimulator().getSettings();
        if (!settings.isPaused()) {
            pauseOverlay.setVisible(false);

            if (pauseIcon != null) {
                playButton.setText("");
                playButton.setIcon(pauseIcon);

                ButtonUI bui = playButton.getUI();
                if (bui instanceof WebButtonUI) {
                    ((WebButtonUI)bui).setBottomBgColor(new Color(236, 226, 210));
                }
            } else {
                playButton.setIcon(null);
                playButton.setForeground(new Color(0, 0, 0));
                playButton.setText("\u2759\u2759");
                playButton.setFont(playButton.getFont().deriveFont(25.0f));
            }
        } else {
            pauseOverlay.setVisible(true);

            if (playIcon != null) {
                playButton.setIcon(playIcon);
                playButton.setText("");
                ButtonUI bui = playButton.getUI();
                if (bui instanceof WebButtonUI) {
                    ((WebButtonUI)bui).setBottomBgColor(new Color(214, 235, 214));
                }
            } else {
                playButton.setIcon(null);
                playButton.setForeground(new Color(15, 159, 11));
                playButton.setText("\u25B8");
                playButton.setFont(playButton.getFont().deriveFont(37.0f));
            }
        }
	}

	private void playAction() {
	    TLSimulationSettings settings = TLSingletonContext.get().getSimulator().getSettings();
	    synchronized (settings) {
	        if (settings.isPaused()) {
	            settings.unpause();
	        } else {
	            settings.pause();
	        }
	        refreshPlayPauseDisplay();
	    }
	}

	private void speedAction() {
        TLSimulationSettings settings = TLSingletonContext.get().getSimulator().getSettings();
        double val = speedSlider.getModel().getValue() * 1.0 / speedSlider.getModel().getMaximum();
        synchronized (settings) {
            settings.setAnimationSpeed((float) (Math.pow(36, val) / 6));
        }
	}

	private void helpAction() {
	    final String DOC_URL = "http://stanford.edu/~alexryan/cgi-bin/turtledoc/";
        try {
            Desktop.getDesktop().browse(new URL(DOC_URL).toURI());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(TLJ2DWindow.this,
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
	                    JOptionPane.showMessageDialog(TLJ2DWindow.this,
	                            "All libraries up-to-date!", "Update Successful",
	                            JOptionPane.INFORMATION_MESSAGE);
	                    return;
	                }
	                ReadableByteChannel rbc = Channels.newChannel(libFile.openStream());
	                fos = new FileOutputStream(jarFile);
	                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	                jarFile.setLastModified(connection.getLastModified());
	                JOptionPane.showMessageDialog(TLJ2DWindow.this,
	                        "Successfully updated " + libName + " to latest version.\n" +
	                		"\nRe-run the program to load the new library code.",
	                		"Update Complete", JOptionPane.INFORMATION_MESSAGE);
	            } catch (Exception e) {
	                e.printStackTrace();
	                JOptionPane.showMessageDialog(TLJ2DWindow.this,
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
	            JOptionPane.showMessageDialog(TLJ2DWindow.this,
	                    "Could not update libraries: unknown library location.",
	                    "Error updating libraries",
	                    JOptionPane.ERROR_MESSAGE);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

    @Override
    public TLCanvas getCanvas() {
        return canvas;
    }

    @Override
    public TLConsole getConsole() {
        return console;
    }

}
