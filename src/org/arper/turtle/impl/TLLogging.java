package org.arper.turtle.impl;

public class TLLogging {
    
    public static void error(String message, Throwable cause) {
        System.err.println(message);
        cause.printStackTrace();
    }
    
}
