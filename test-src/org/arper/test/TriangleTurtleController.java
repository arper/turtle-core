package org.arper.test;
import java.awt.Color;

import org.arper.turtle.TLApplication;
import org.arper.turtle.TLContext;
import org.arper.turtle.Turtle;
import org.arper.turtle.controllers.SingleTurtleController;


public class TriangleTurtleController extends SingleTurtleController {

	public static void main(String[] args) {
	    TLContext context = new TLContext(500, 500);
	    context.createTurtle();
	    context.getWindow().setVisible(true);
	}

    @Override
    public void runWithTurtle(Turtle t, TLApplication app, Object[] args) {
        t.pause(3);

        t.startFillShape();
        t.setColor(Color.GREEN);
        t.moveForward(300);
        t.turnLeft(120);

        t.setColor(Color.BLUE);
        t.moveForward(300);
        t.turnLeft(120);

        t.setColor(Color.RED);
        t.moveForward(300);
        t.turnLeft(150);

        t.setColor(Color.YELLOW);
        t.endFillShape();
        t.setColor(Color.BLACK);

        t.penUp();
        t.moveForward(150);
        t.setStatus("I'm awesome!");
        t.setHeading(0);
    }
}
