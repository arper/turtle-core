package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingStyles;
import org.arper.turtle.impl.swing.TLSwingUtilities;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.extended.panel.WebOverlay;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.laf.text.WebTextPane;
import com.alee.managers.hotkey.Hotkey;

public class TLSwingConsolePlugin implements TLSwingPlugin {

    private WebTextPane textPane;
    private WebTextField inputField;

    @Override
    public void initSwingPlugin(final TLSwingWindow window) {
        textPane = createOutputPane();
        inputField = createInputField();

        final JComponent console = layoutConsole(inputField, textPane);

        final JToggleButton consoleButton = new WebToggleButton();
        TLSwingToolbarPlugin.styleToolbarButton(consoleButton,
                window, "icons/console.png", "Show/Hide Console", Hotkey.TAB);

        final WebOverlay consoleOverlay = new WebOverlay(TLSwingStyles.transparent(new WebPanel()));
        consoleOverlay.addOverlay(console, SwingConstants.LEFT, SwingConstants.TOP);
//
//
//        final JComponent consoleFrame = TLSwingUtilities.makeResizable(console, 0, 5, 5, 0);
//        consoleFrame.setPreferredSize(new Dimension(100, 100));
//        consoleFrame.setBounds(100, 100, 300, 300);
//        consoleFrame.setLocation(100, 100);
//
        window.addPluginLayer(consoleOverlay);
//        consoleFrame.setVisible(false);
//
        consoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleOverlay.setVisible(consoleButton.isSelected());
            }
        });
//
//        TLSwingUtilities.anchorComponent(consoleFrame,
//                new Point2D.Double(0, 0),
//                window.getPluginLayers(),
//                new Point2D.Double(0, 0));

        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(
                consoleButton, BorderLayout.WEST);
    }

    private WebTextPane createOutputPane() {
        WebTextPane textPane = TLSwingStyles.transparent(new WebTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
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
            ConsoleImpl impl = new ConsoleImpl(inputField, textPane);
            TLSingleton.getApplication().out = new PrintStream(impl.out, true);
            TLSingleton.getApplication().in = impl.in;
        }
    }


    private static final SimpleAttributeSet INPUT_TEXT_ATTR;
    private static final SimpleAttributeSet OUTPUT_TEXT_ATTR;

    static {
        INPUT_TEXT_ATTR = new SimpleAttributeSet();
        StyleConstants.setForeground(INPUT_TEXT_ATTR, Color.GREEN.darker());

        OUTPUT_TEXT_ATTR = new SimpleAttributeSet();
        StyleConstants.setForeground(OUTPUT_TEXT_ATTR, Color.BLUE);
    }

    private static class ConsoleImpl {

        private final JTextComponent textPane;
        private final JTextField inputTextField;

        public ConsoleImpl(JTextField textField, JTextComponent textPane) {
            this.textPane = textPane;
            this.inputTextField = textField;

            textPane.setEditable(false);
            textPane.setFont(new Font("Lucida Console", Font.PLAIN, 13));
            ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            inputTextField.setFont(new Font("Lucida Console", Font.PLAIN, 13));

            inputTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = inputTextField.getText() + '\n';
                    doPrint(text, INPUT_TEXT_ATTR);
                    inputTextField.setText("");
                    byte[] bytes;
                    try {
                        bytes = text.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                        return;
                    }
                    for (byte b : bytes) {
                        inputBuffer.add(b);
                    }
                }
            });
        }

        private void doPrint(final String text, final AttributeSet settings) {
            TLSwingUtilities.runOnAwtThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        textPane.getDocument().insertString(textPane.getDocument().getLength(),
                                text, settings);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                    //                BoundedRangeModel brm = textScrollPane.getVerticalScrollBar().getModel();
                    //                boolean atBottom = brm.getValue() + brm.getExtent() >= brm.getMaximum() - 5;
                    //                try {
                    //                    textPane.getDocument().insertString(textPane.getDocument().getLength(),
                    //                            text, settings);
                    //                } catch (BadLocationException e1) {
                    //                    e1.printStackTrace();
                    //                }
                    //                if (atBottom) {
                    //                    textPane.setCaretPosition(textPane.getDocument().getLength());
                    //                    brm.setValue(brm.getMaximum() - brm.getExtent());
                    //                    Rectangle v = textPane.getVisibleRect();
                    //                    v.y = textPane.getHeight() - v.height;
                    //                    textPane.scrollRectToVisible(v);
                    //                }
                }
            }, false);
        }


        private final BlockingQueue<Byte> inputBuffer = new LinkedBlockingQueue<Byte>();
        private final InputStream in = new InputStream() {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (len == 0)
                    return 0;

                int count = 0;
                do {
                    int next = read();
                    if (next == -1)
                        return count;
                    b[off + count] = (byte)next;
                    ++count;
                } while (count < len && available() > 0 && b[count-1] != '\n');

                return count;
            }

            @Override
            public int available() throws IOException {
                return inputBuffer.size();
            }

            @Override
            public void close() throws IOException {
                inputBuffer.clear();
                super.close();
            }

            @Override
            public int read() throws IOException {
                try {
                    return inputBuffer.take();
                } catch (InterruptedException e) {
                    return -1;
                }
            }
        };

        private final OutputStream out = new OutputStream() {
            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                doPrint(new String(b, off, len), OUTPUT_TEXT_ATTR);
            }

            @Override
            public void flush() throws IOException {
                textPane.repaint();
            }

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b});
            }
        };
    }
}
