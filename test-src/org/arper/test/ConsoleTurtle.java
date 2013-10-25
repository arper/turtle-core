package org.arper.test;

import java.awt.Color;

import org.arper.__old__.turtle.Turtle;


public class ConsoleTurtle extends Turtle {
    
    @Override
    public void run() {
//        console.out.print("Enter square size: ");
//        int size = console.readInteger();
        int size = 200;
        
        while (true) {
            console.out.print("Enter color: ");
            Color color = console.readColor();
            setColor(color);
            setStatus("Drawing mah squayre...");
            
            drawSquare(size);
            setStatus("Done!");
            
        }
        
    }
    
    private void drawSquare(int size) {
        moveForward(size);
        turnLeft(90);
        moveForward(size);
        turnLeft(90);
        moveForward(size);
        turnLeft(90);
        moveForward(size);
        turnLeft(90);
    }
    
    public static void main(String[] args) {
         Turtle.runTurtleProgram(50000, 50000, new ConsoleTurtle());
    }
}
