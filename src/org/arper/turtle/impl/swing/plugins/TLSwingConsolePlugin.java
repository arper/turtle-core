package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.InputStream;
import java.io.PrintStream;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingStyles;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.extended.panel.WebCollapsiblePane;
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

        JComponent console = layoutConsole(inputField, textPane);
        console.setPreferredSize(new Dimension(400, 300));

        JComponent flyout = createFlyout(console);
        window.addPluginLayer(TLSwingStyles.anchor(flyout,
                BorderLayout.SOUTH, BorderLayout.WEST));
    }

    private InputStream in;
    private PrintStream out;

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

    private WebCollapsiblePane createFlyout(JComponent console) {
        WebCollapsiblePane consoleFlyout = new WebCollapsiblePane();
        consoleFlyout.setContent(console);
        consoleFlyout.setContentMargin(0);
        consoleFlyout.setTitlePanePostion (SwingConstants.TOP);
        consoleFlyout.setStateIconPostion(SwingConstants.LEFT);
        swapIcons(consoleFlyout);

        consoleFlyout.getExpandButton().setPainter(TLSwingStyles.getButtonPainter());
        consoleFlyout.getExpandButton().setText("  Console");
        consoleFlyout.getExpandButton().setFontSize(13);
        consoleFlyout.getExpandButton().setFontName("Lucida");
        consoleFlyout.getExpandButton().setForeground(Color.white);

        consoleFlyout.getHeaderPanel().setUndecorated(true);
        consoleFlyout.setExpanded(false, false);
        TLSwingStyles.setUndecorated(consoleFlyout);

        return consoleFlyout;
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

    private void swapIcons(WebCollapsiblePane pane) {
        ImageIcon collapsed = pane.getCollapseIcon();
        pane.setCollapseIcon(pane.getExpandIcon());
        pane.setExpandIcon(collapsed);
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        if ("app_start".equals(name)) {
            TLSingleton.getApplication().out = out;
            TLSingleton.getApplication().in = in;
        }
    }
}
