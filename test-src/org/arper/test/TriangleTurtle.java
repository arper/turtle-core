package org.arper.test;
import java.awt.Color;

import org.arper.turtle.Turtle;


public class TriangleTurtle extends Turtle {
	
	@Override
    public void run() {
	    pause(3);
	    
	    startFillShape();
	    setColor(Color.GREEN);
	    moveForward(300);
	    turnLeft(120);
	    
	    setColor(Color.BLUE);
	    moveForward(300);
	    turnLeft(120);
	    
	    setColor(Color.RED);
	    moveForward(300);
	    turnLeft(150);

        setColor(Color.YELLOW);
        endFillShape();
        setColor(Color.BLACK);
	    
	    penUp();
	    moveForward(150);
		setStatus("I'm awesome!");
		setHeading(0);
		
	}
	
	public static void main(String[] args) {
		Turtle.runTurtleProgram(new TriangleTurtle());
	}
}
