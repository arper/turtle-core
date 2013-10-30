package org.arper.turtle.impl.j2d;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.arper.turtle.ui.TLConsole;

public class TLJ2DConsoleImpl {
    
    public static TLConsole create(JTextField textField, JTextComponent textPane) {
        TLJ2DConsoleImpl impl = new TLJ2DConsoleImpl(textField, textPane);
        return new TLConsole(impl.in, new PrintStream(impl.out, true));
    }
    
    private final JTextComponent textPane;
    private final JTextField inputTextField;

    public TLJ2DConsoleImpl(JTextField textField, JTextComponent textPane) {
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
        TLAwtUtilities.runOnAwtThread(new Runnable() {
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

    private static final SimpleAttributeSet INPUT_TEXT_ATTR;
    private static final SimpleAttributeSet OUTPUT_TEXT_ATTR;

    static {
        INPUT_TEXT_ATTR = new SimpleAttributeSet();
        StyleConstants.setForeground(INPUT_TEXT_ATTR, Color.GREEN.darker());

        OUTPUT_TEXT_ATTR = new SimpleAttributeSet();
        StyleConstants.setForeground(OUTPUT_TEXT_ATTR, Color.BLUE);
    }

}
