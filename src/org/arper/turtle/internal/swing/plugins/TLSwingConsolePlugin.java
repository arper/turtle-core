package org.arper.turtle.internal.swing.plugins;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.arper.turtle.internal.TLSingleton;
import org.arper.turtle.internal.swing.TLSwingLineNumberDisplay;
import org.arper.turtle.internal.swing.TLSwingPlugin;
import org.arper.turtle.internal.swing.TLSwingResizeHandles;
import org.arper.turtle.internal.swing.TLSwingStyles;
import org.arper.turtle.internal.swing.TLSwingUtilities;
import org.arper.turtle.internal.swing.TLSwingWindow;

import com.alee.extended.panel.WebOverlay;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.managers.hotkey.HotkeyData;

public class TLSwingConsolePlugin implements TLSwingPlugin {

    private JTextPane textPane;
    private JTextField inputField;

    @Override
    public void initSwingPlugin(final TLSwingWindow window) {
        textPane = createOutputPane();
        inputField = createInputField();

        textPane.setFocusable(false);
        textPane.setEditable(false);
        textPane.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 13));
        ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        inputField.setFont(new Font("DejaVu Sans Mono", Font.PLAIN, 13));

        final JComponent consoleOverlay = createConsoleOverlay();
        consoleOverlay.setVisible(false);
        final JToggleButton consoleButton = new WebToggleButton();
        TLSwingToolbarPlugin.styleToolbarButton(consoleButton,
                window, "icons/console.png", "Console", new HotkeyData(KeyEvent.VK_BACK_QUOTE){
            private static final long serialVersionUID = 1L;
            @Override
            public String toString() {
                return "`";
            }
        });

        consoleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                consoleOverlay.setVisible(consoleButton.isSelected());
            }
        });

        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(
                consoleButton, BorderLayout.WEST);
        window.addOverlay(TLSwingStyles.noLayout(consoleOverlay));

        TLSwingUtilities.anchorComponent(consoleOverlay,
                new Point2D.Double(0, 0),
                consoleButton,
                new Point2D.Double(0, 1),
                false,
                true);
    }

    private JComponent createConsoleOverlay() {
        final JComponent console = layoutConsole();
        console.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        final WebOverlay consoleOverlay = new WebOverlay(console);
        TLSwingStyles.setPainter(consoleOverlay, TLSwingStyles.getPanelPopupPainter());
        final JComponent resizeHandle = TLSwingResizeHandles.createResizeHandle();

        TLSwingResizeHandles.installResizeHandle(resizeHandle, new Point(1, 1), consoleOverlay);
        consoleOverlay.addOverlay(resizeHandle, SwingConstants.TRAILING, SwingConstants.BOTTOM);

        consoleOverlay.setMinimumSize(new Dimension(100, 70));
        consoleOverlay.setPreferredSize(new Dimension(300, 200));
        consoleOverlay.setMaximumSize(new Dimension(800, 600));
        consoleOverlay.setSize(consoleOverlay.getPreferredSize());
        return consoleOverlay;
    }

    private JTextPane createOutputPane() {
        JTextPane textPane = TLSwingStyles.transparent(new JTextPane() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });

        textPane.setBackground(new Color(0, 0, 0, 32));

        return textPane;
    }

    private JTextField createInputField() {
        JTextField textField = TLSwingStyles.transparent(new WebTextField());
        textField.setBackground(new Color(0, 0, 0, 128));
        textField.setForeground(Color.white);

        return textField;
    }

    private JComponent layoutConsole() {
        WebPanel comp = TLSwingStyles.transparent(new WebPanel(new BorderLayout()));
        WebScrollPane scrollPane = new WebScrollPane(textPane, true, false);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JComponent lines = TLSwingLineNumberDisplay.createLineCountingDisplay(textPane,
                1.0f, new Color(84, 84, 84), new Color(84, 84, 84));
        scrollPane.setRowHeaderView(lines);
        TLSwingStyles.transparent(scrollPane.getRowHeader());

        comp.add(inputField, BorderLayout.SOUTH);
        comp.add(TLSwingStyles.transparent(scrollPane), BorderLayout.CENTER);
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
        StyleConstants.setForeground(INPUT_TEXT_ATTR, Color.WHITE);

        OUTPUT_TEXT_ATTR = new SimpleAttributeSet();
        StyleConstants.setForeground(OUTPUT_TEXT_ATTR, Color.BLUE);
    }

    private static class ConsoleImpl {

        private final JTextComponent textPane;
        private final JTextField inputTextField;

        public ConsoleImpl(JTextField textField, JTextComponent textPane) {
            this.textPane = textPane;
            this.inputTextField = textField;

            inputTextField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String text = inputTextField.getText() + '\n';
                    doPrint(text, new SimpleAttributeSet(INPUT_TEXT_ATTR));
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

        private void doPrint(final String text, final MutableAttributeSet settings) {
            TLSwingStyles.setFontStyles(settings, textPane.getFont());
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
