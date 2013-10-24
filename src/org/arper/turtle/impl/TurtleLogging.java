package org.arper.turtle.impl;

public class TurtleLogging {
    
    public static void error(String message, Throwable cause) {
        System.err.println(message);
        cause.printStackTrace();
    }
    
}
