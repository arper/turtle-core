package org.arper.turtle.internal;

public class TLLogging {
    
    public static void error(String message, Throwable cause) {
        System.err.println(message);
        cause.printStackTrace();
    }
    
}
