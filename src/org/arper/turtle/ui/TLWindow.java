package org.arper.turtle.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.arper.turtle.TLSimulationSettings;
import org.arper.turtle.TLTurtle;
import org.arper.turtle.impl.TLSingletonContext;
import org.arper.turtle.impl.display.TLJ2DCanvas;


//import com.alee.laf.WebLookAndFeel;
//import com.alee.laf.button.WebButtonUI;

@SuppressWarnings("serial")
public class TLWindow extends JFrame {
	private TLJ2DCanvas canvas;
	private JToolBar controlPanel;
	private JComponent pauseOverlay;
	private JButton playButton;
	private ImageIcon playIcon, pauseIcon;
	private JButton showConsoleButton;
	private JButton restartButton;
    private JButton helpButton;
    private JButton updateButton;
	private JSlider speedSlider;
	public final TLConsole console;

	private JFrame consoleWindow;

	static {
	    try {
//	        com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme("Default-Large-Font", "atl", "Turtle");
//            UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");

//            UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");

//            com.jtattoo.plaf.mint.MintLookAndFeel.setTheme("Red", "atl", "Turtle");
//            UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");

//	        SubstanceLookAndFeel.setSkin(new MistAquaSkin());
//	        UIManager.setLookAndFeel(new SubstanceMistSilverLookAndFeel());
//	        UIManager.put(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);

//	        UIManager.setLookAndFeel(new WebLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public TLWindow(int canvasWidth, int canvasHeight) {
	    console = new TLConsole();
	    layoutToolbar();
	    layoutCanvas(canvasWidth, canvasHeight);
        refreshPlayPauseDisplay();
	    try {
	        setIconImage(ImageIO.read(ClassLoader.getSystemResource("icons/turtle.png")));
	    } catch (Exception e) {}
	    controlPanel.setMinimumSize(controlPanel.getPreferredSize());
		pack();

		canvas.addKeyListener(inputListener);
		getContentPane().addKeyListener(inputListener);
		this.addKeyListener(inputListener);
		controlPanel.addKeyListener(inputListener);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public TLWindow() {
		this(Toolkit.getDefaultToolkit().getScreenSize().width / 4,
				Toolkit.getDefaultToolkit().getScreenSize().height / 4);
	}

	public TLCanvas getCanvas() {
		return canvas;
	}

	private void showConsoleAction() {
	    if (consoleWindow == null) {
	        consoleWindow = new JFrame("Console - " + getTitle());
	        consoleWindow.setLocationByPlatform(true);
	        consoleWindow.setLayout(new BorderLayout());
	        consoleWindow.add(console, BorderLayout.CENTER);
	        consoleWindow.pack();
	        consoleWindow.setVisible(true);
	    } else if (!consoleWindow.isVisible()) {
	        consoleWindow.setLocationByPlatform(true);
	        consoleWindow.setVisible(true);
	    }
        consoleWindow.toFront();
	}

	private void refreshPlayPauseDisplay() {
        TLSimulationSettings settings = TLSingletonContext.get().getSimulator().getSettings();
        if (!settings.isPaused()) {
            pauseOverlay.setVisible(false);

            if (pauseIcon != null) {
                playButton.setText("");
                playButton.setIcon(pauseIcon);

//                ButtonUI bui = playButton.getUI();
//                if (bui instanceof WebButtonUI) {
//                    ((WebButtonUI)bui).setBottomBgColor(new Color(236, 226, 210));
//                }
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
//                ButtonUI bui = playButton.getUI();
//                if (bui instanceof WebButtonUI) {
//                    ((WebButtonUI)bui).setBottomBgColor(new Color(214, 235, 214));
//                }
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
            JOptionPane.showMessageDialog(TLWindow.this,
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
	                    JOptionPane.showMessageDialog(TLWindow.this,
	                            "All libraries up-to-date!", "Update Successful",
	                            JOptionPane.INFORMATION_MESSAGE);
	                    return;
	                }
	                ReadableByteChannel rbc = Channels.newChannel(libFile.openStream());
	                fos = new FileOutputStream(jarFile);
	                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	                jarFile.setLastModified(connection.getLastModified());
	                JOptionPane.showMessageDialog(TLWindow.this,
	                        "Successfully updated " + libName + " to latest version.\n" +
	                		"\nRe-run the program to load the new library code.",
	                		"Update Complete", JOptionPane.INFORMATION_MESSAGE);
	            } catch (Exception e) {
	                e.printStackTrace();
	                JOptionPane.showMessageDialog(TLWindow.this,
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
	            JOptionPane.showMessageDialog(TLWindow.this,
	                    "Could not update libraries: unknown library location.",
	                    "Error updating libraries",
	                    JOptionPane.ERROR_MESSAGE);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

    private void layoutCanvas(int width, int height) {
	    setLayout(new BorderLayout());

        canvas = new TLJ2DCanvas(width, height);
        canvas.setBackground(Color.WHITE);


        final JScrollPane canvasScrollPane = new JScrollPane(canvas);

        JLayeredPane centerWrapper = new JLayeredPane();
        centerWrapper.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent ce) {
                canvasScrollPane.setSize(ce.getComponent().getSize());
                pauseOverlay.setSize(ce.getComponent().getSize());
                pauseOverlay.doLayout();
            }
        });
        centerWrapper.setPreferredSize(canvasScrollPane.getPreferredSize());
        centerWrapper.add(canvasScrollPane, 0, 0);
        centerWrapper.add(pauseOverlay, 1, 1);
        centerWrapper.moveToFront(pauseOverlay);

        add(centerWrapper, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.WEST);
        add(new JComponent(){}, BorderLayout.NORTH);
        add(new JComponent(){}, BorderLayout.SOUTH);
	}

    private void layoutToolbar() {
        try {
            ImageIcon icon = new ImageIcon(ImageIO.read(ClassLoader.getSystemResource("icons/pause2.png")));
            JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel pauseLabel = new JLabel("Paused");
            pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            pauseLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pauseLabel.setFont(new Font("Helvetica", Font.BOLD, 128));
            pauseLabel.setForeground(new Color(0f, 0f, 0f, .03f));
            pauseLabel.setOpaque(false);
            pauseLabel.setBackground(new Color(0, 0, 0, 0));
            JLabel instrLabel = new JLabel("spacebar or button at left to run");
            instrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            instrLabel.setHorizontalAlignment(SwingConstants.CENTER);
            instrLabel.setFont(new Font("Helvetica", Font.BOLD, 28));
            instrLabel.setForeground(new Color(0f, 0f, 0f, .03f));
            instrLabel.setOpaque(false);
            instrLabel.setBackground(new Color(0, 0, 0, 0));

            pauseOverlay = Box.createVerticalBox();
            pauseOverlay.add(Box.createVerticalGlue());
            pauseOverlay.add(iconLabel);
            pauseOverlay.add(pauseLabel);
            pauseOverlay.add(instrLabel);
            pauseOverlay.add(Box.createVerticalGlue());
        } catch (Exception e) {
            JLabel l = new JLabel("||");
            l.setFont(new Font("Helvetica", Font.BOLD, 256));
            l.setForeground(new Color(0.0f, 0.0f, 0.0f, 0.04f));
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setHorizontalTextPosition(SwingConstants.CENTER);
            pauseOverlay = l;
        }
        controlPanel = new JToolBar(JToolBar.VERTICAL);
        controlPanel.setFloatable(false);

        playButton = createToolbarButton(null, "\u25B8", "Play/Pause (Spacebar)");
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
        controlPanel.add(playButton);
        controlPanel.addSeparator();

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
        controlPanel.add(makeLabel("Fast"));
        controlPanel.add(speedSlider);
        controlPanel.add(makeLabel("Slow"));
        controlPanel.addSeparator();

        showConsoleButton = createToolbarButton("icons/console.png", "Console",
                "Show Console (C)");
        showConsoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showConsoleAction();
            }
        });
        controlPanel.add(showConsoleButton);

        restartButton = createToolbarButton("icons/restart.png", "Restart",
                "Restart Application (R) [requires debug mode]");
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* TODO: restart */
//                TLApplication.restart(TLWindow.this);
            }
        });
        controlPanel.add(restartButton);

        helpButton = createToolbarButton("icons/help.png", "Help",
                "Help - Go to Online Documentation (http://stanford.edu/~alexryan/cgi-bin/turtledoc/)");
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpAction();
            }
        });

        updateButton = createToolbarButton("icons/update.png", "Update", "Download latest libraries");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAction();
            }
        });
        controlPanel.add(Box.createVerticalGlue());
        controlPanel.add(updateButton, "END");
        controlPanel.add(helpButton, "END");
    }

    private JButton createToolbarButton(String icon, String backupText, String tooltip) {
        JButton b = new JButton();
        try {
            b.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(icon))));
        } catch (Exception e) {
            b.setText(backupText);
        }

        b.setPreferredSize(new Dimension(45, 45));
        b.setMinimumSize(new Dimension(45, 45));
        b.setMaximumSize(new Dimension(45, 45));
        b.setFocusable(false);
        b.setToolTipText(tooltip);
        return b;
    }

	private JComponent makeLabel(String text) {
	    JLabel l = new JLabel(text);
        l.setFont(new Font("Helvetica", Font.PLAIN, 14));
	    l.setHorizontalTextPosition(SwingConstants.CENTER);
	    l.setHorizontalAlignment(SwingConstants.CENTER);
	    l.setAlignmentY(JComponent.CENTER_ALIGNMENT);
	    l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
//	    l.setForeground(new Color(60, 95, 142));
	    return l;
	}

	private final KeyListener inputListener = new KeyAdapter() {
	    @Override
        public void keyPressed(KeyEvent e) {
	        switch (e.getKeyCode()) {
	        case KeyEvent.VK_SPACE:
	            playButton.doClick();
	            break;
	        case KeyEvent.VK_1:
	            speedSlider.setValue(0);
	            break;
	        case KeyEvent.VK_2:
	            speedSlider.setValue(speedSlider.getMaximum() / 4);
	            break;
            case KeyEvent.VK_3:
                speedSlider.setValue(2 * speedSlider.getMaximum() / 4);
                break;
            case KeyEvent.VK_4:
                speedSlider.setValue(3 * speedSlider.getMaximum() / 4);
                break;
            case KeyEvent.VK_5:
                speedSlider.setValue(4 * speedSlider.getMaximum() / 4);
                break;
            case KeyEvent.VK_C:
                showConsoleButton.doClick();
                break;
            case KeyEvent.VK_R:
                restartButton.doClick();
                break;
	        }
	    }
	};

}
