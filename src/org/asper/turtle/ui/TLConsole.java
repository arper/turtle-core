package org.asper.turtle.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.arper.turtle.impl.TLUtil;

import com.google.common.base.Function;

@SuppressWarnings("serial")
public class TLConsole extends JPanel {
    public final PrintStream out;
    public final InputStream in;
    
    private final JTextPane textPane;
    private final JScrollPane textScrollPane;
    private final JTextField inputTextField;
    
    public TLConsole() {
        out = new PrintStream(textAreaOut, true);
        in = textAreaIn;
        
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Lucida Console", Font.PLAIN, 13));
        textPane.setFocusable(false);
        textPane.setPreferredSize(new Dimension(400, 300));
        textPane.setBackground(new Color(255, 250, 245));
        ((DefaultCaret)textPane.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        
        inputTextField = new JTextField();
        inputTextField.setFont(new Font("Lucida Console", Font.PLAIN, 13));
        
        textScrollPane = new JScrollPane(textPane, 
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        
        setLayout(new BorderLayout());
        add(textScrollPane, BorderLayout.CENTER);
        add(inputTextField, BorderLayout.SOUTH);
        
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BoundedRangeModel brm = textScrollPane.getVerticalScrollBar().getModel();
                boolean atBottom = brm.getValue() + brm.getExtent() >= brm.getMaximum() - 5;
                try {
                    textPane.getDocument().insertString(textPane.getDocument().getLength(),
                            text, settings);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                if (atBottom) {
                    textPane.setCaretPosition(textPane.getDocument().getLength());
                    brm.setValue(brm.getMaximum() - brm.getExtent());
                    Rectangle v = textPane.getVisibleRect();
                    v.y = textPane.getHeight() - v.height;
                    textPane.scrollRectToVisible(v);
                }
            }
        });
    }
    
    public synchronized String readString() {
        return doRead("Empty input. Please enter a string: ",
                new Function<String, String>() {
                    @Override
                    public String apply(String arg0) {
                        return arg0.isEmpty()? null : arg0;
                    }
        });
    }

    public synchronized int readInteger() {
        return doRead("That is not an integer! Please enter a whole number: ",
                new Function<String, Integer>() {
                    @Override
                    public Integer apply(String arg0) {
                        return Integer.parseInt(arg0);
                    }
        });
    }
    
    public synchronized int readInteger(final int min, final int max) {
        if (min >= max) {
            throw new IllegalArgumentException("invalid min/max: " + min + " >= " + max);
        }
        
        return doRead("Invalid. Please enter an integer from " + min + " to " + max + ": ",
                new Function<String, Integer>() {
                    @Override
                    public Integer apply(String arg0) {
                        int i = Integer.parseInt(arg0);
                        return i >= min && i <= max? i : null;
                    }
        });
    }
    
    public synchronized double readDouble() {
        return doRead("Invalid. Please enter a number: ",
                new Function<String, Double>() {
                    @Override
                    public Double apply(String arg0) {
                        return Double.parseDouble(arg0);
                    }
        });
    }
    
    public synchronized boolean readBoolean() {
        return doRead("Invalid. Please enter 'yes' or 'no': ",
                new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String arg0) {
                        if (arg0.isEmpty()) return null;
                        char c = Character.toLowerCase(arg0.charAt(0));
                        switch (c) {
                        case 'y': return true;
                        case 'n': return false;
                        default: return null;
                        }
                    }
        });
    }
    
    
    public synchronized Color readColor() {
        return doRead("Not a recognized color. Try again: ",
                new Function<String, Color>() {
                    @Override
                    public Color apply(String s) {
                        return TLUtil.getColorByName(s);
                    }
        });
    }
    
    private synchronized <T> T doRead(String rePrompt, Function<String, T> verifier) {
        Scanner sc = new Scanner(in);
        while (true) {
            String line = sc.nextLine();
            try {
                T obj = verifier.apply(line.trim());
                if (obj != null) {
                    return obj;
                }
            } catch (Exception e) {}
            out.print(rePrompt);
        }
    }
    
    private final BlockingQueue<Byte> inputBuffer = new LinkedBlockingQueue<Byte>();
    private final InputStream textAreaIn = new InputStream() {
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
    
    private final OutputStream textAreaOut = new OutputStream() {
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
