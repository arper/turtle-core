package org.arper.turtle.ui;

import java.awt.Color;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.arper.turtle.TLUtils;

import com.google.common.base.Function;

public class TLConsole {

    public TLConsole(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }
    
    public final InputStream in;
    public final PrintStream out;

    public synchronized String readString() {
        return doRead("Empty input. Please enter a string: ",
                new Function<String, String>() {
                    @Override
                    public String apply(String arg0) {
                        return arg0.isEmpty()? null : arg0;
                    }
        });
    }

    public synchronized int readInt() {
        return doRead("That is not an integer! Please enter a whole number: ",
                new Function<String, Integer>() {
                    @Override
                    public Integer apply(String arg0) {
                        return Integer.parseInt(arg0);
                    }
        });
    }

    public synchronized int readInt(final int min, final int max) {
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
                        return TLUtils.getColorByName(s);
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
}
