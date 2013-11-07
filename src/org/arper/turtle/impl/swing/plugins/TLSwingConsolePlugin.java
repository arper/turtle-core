package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JToggleButton;

import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingStyles;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.laf.text.WebTextPane;

public class TLSwingConsolePlugin implements TLSwingPlugin {

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        WebTextPane textPane = createOutputPane();
        WebTextField inputField = createInputField();

        TLSwingConsoleImpl.StreamPair streamPair = TLSwingConsoleImpl.create(inputField, textPane);
        out = streamPair.out;
        in = streamPair.in;

        final JComponent console = layoutConsole(inputField, textPane);
        
        final JToggleButton consoleButton = new WebToggleButton("Console");
        consoleButton.setForeground(new Color(190, 190, 190));
        consoleButton.setPreferredSize(new Dimension(100, 38));
        TLSwingStyles.applyStyle(consoleButton);
        WebPanel bottomBar = new WebPanel(false);
        bottomBar.setLayout(new BoxLayout(bottomBar, BoxLayout.X_AXIS));
        bottomBar.add(consoleButton);
        TLSwingStyles.setPainter(bottomBar, TLSwingStyles.getPanelPainter());
        window.getContentPane().add(bottomBar, BorderLayout.SOUTH);
        
        final JComponent consoleFrame = makeResizable(console);
        consoleFrame.setPreferredSize(new Dimension(100, 100));
        consoleFrame.setBounds(100, 100, 300, 300);
        window.addPluginLayer(TLSwingStyles.noLayout(consoleFrame));
        
        consoleFrame.setVisible(false);
        
        consoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleFrame.setVisible(consoleButton.isSelected());
            }
        });
    }

    private InputStream in;
    private PrintStream out;
    
    private JComponent makeResizable(JComponent comp) {
        JComponent parent = comp;
        parent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        new ComponentResizer(parent);
        return parent;
    }
    private WebTextPane createOutputPane() {
        WebTextPane textPane = TLSwingStyles.transparent(new WebTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(getX(), getY(), getWidth(), getHeight());
                super.paintComponent(g);
            }
        });

        textPane.setBackground(new Color(0, 0, 0, 168));

        return textPane;
    }

    private WebTextField createInputField() {
        WebTextField textField = TLSwingStyles.transparent(new WebTextField());
        textField.setBackground(new Color(0, 0, 0, 128));
        textField.setForeground(Color.white);

        return textField;
    }
    
    private JComponent layoutConsole(WebTextField inputField, WebTextPane outputPane) {

        WebPanel comp = new WebPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getBackground());
                ((Graphics2D)g).fill(getBounds());
            }
        };

        comp.add(inputField, BorderLayout.SOUTH);
        comp.add(TLSwingStyles.transparent(
                new WebScrollPane(outputPane, true, false)), BorderLayout.CENTER);
        comp.setBackground(new Color(255, 255, 255, 32));
        comp.setOpaque(false);
        return comp;
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        if ("app_start".equals(name)) {
            TLSingleton.getApplication().out = out;
            TLSingleton.getApplication().in = in;
        }
    }
}
