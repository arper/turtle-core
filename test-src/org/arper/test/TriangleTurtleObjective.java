package org.arper.test;
import java.awt.Color;

import org.arper.turtle.TLApplication;
import org.arper.turtle.Turtle;
import org.arper.turtle.config.TLApplicationConfig;
import org.arper.turtle.controller.TLSingleTurtleObjective;


public class TriangleTurtleObjective extends TLSingleTurtleObjective {

	public static void main(String[] args) {
	    new TLApplication(TLApplicationConfig.DEFAULT)
	        .startObjectiveWithNewTurtles(new TriangleTurtleObjective());
	}

    @Override
    public void run(Turtle t, TLApplication app, Object[] args) {
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
