package org.arper.turtle;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;

import org.arper.turtle.config.TLAnglePolicy;
import org.arper.turtle.impl.TLAction;
import org.arper.turtle.impl.TLActions;
import org.arper.turtle.impl.TLRenderer;
import org.arper.turtle.impl.TLSimulator;
import org.arper.turtle.impl.TLSingletonContext;
import org.arper.turtle.impl.TLTurtleState;
import org.arper.turtle.impl.j2d.TLJ2DWindow;
import org.arper.turtle.ui.TLCanvas;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * This is the main class that is used for the Turtle Learning Library. The <tt>Turtle</tt> responds to
 * a certain set of basic methods, such as <tt>moveForward</tt> and <tt>turnLeft</tt>. The intent is to solve
 * problems by creating custom turtles that trace challenging shapes.
 *
 * <p>This design documentation contains the following sections, in rough order of importance: </p>
 *
 * &emsp;<a href="#library">I. Using the Library</a> <br>
 * &emsp;<a href="#turtle">II. Becoming the Turtle</a> <br>
 * &emsp;<a href="#properties">III. Turtle Properties</a> <br>
 * &emsp;<a href="#policy">IV. Angle Policy</a> <br>
 * &emsp;<a href="#environment">V. Accessing the Environment</a> <br>
 *
 * <h3 id="library">I. Using the Library</h3>
 * <p>To use the Turtle Learning Library, one should properly configure Eclipse or another IDE to include the
 * <tt>turtle-learning.jar</tt> in your project's build path. Then, simply subclass this <tt>Turtle</tt> class
 * (for example, a new <tt>class TestTurtle extends Turtle</tt>) with a custom {@link #run} method that traces
 * a shape. Finally, add a <tt>public static void main</tt> method to your class that invokes
 * {@link TLTurtle#runTurtleProgram(TLTurtle...) runTurtleProgram}. </p>
 * <pre>
 * {@code
 * import org.atl.Turtle; <br>
 *
 * public class TestTurtle extends Turtle {
 *     public void run() {
 *         moveForward(300);
 *         setStatus("I'm done!");
 *     }
 *
 *     public static void main(String[] args) {
 *         Turtle.runTurtleProgram(new TestTurtle());
 *     }
 * }
 *
 * }</pre>
 *
 * <h3 id="turtle">II. Becoming the Turtle </h3>
 * To break down challenging designs, the programmer will use a very small set of movement
 * commands to move the turtle around, listed below. To make aesthetic changes, such as the turtle's
 * line color or movement speed, see <a href="#properties">Properties</a>.
 * <ul>
 * <li> {@link #moveForward moveForward(pixels)} </li>
 * <li> {@link #turnLeft turnLeft(angle)} / {@link #turnRight turnRight(angle)} </li>
 * <li> {@link #penUp()} / {@link #penDown()} </li>
 * <li> {@link #setHeading(double) setHeading(angle)} </li>
 * <li> {@link #setStatus(String) setStatus("message")} </li>
 * <li> {@link #pause(double) pause(seconds)} </li>
 * <li> {@link #startFillShape()} / {@link #endFillShape()}
 * <li> {@link #moveTo(double, double) moveTo(x, y)} </li>
 * <li> {@link #lookAt(double, double) lookAt(x,y)} </li>
 * </ul>
 *
 * There are also many "getter" methods that allow the programmer to get information about the
 * state of the turtle, such as its current location and drawing color. These are:
 * <ul>
 * <li> {@link #getX()} / {@link #getY()} (alternatively, {@link #getLocation()} to get both <tt>(x,y)</tt> together) </li>
 * <li> {@link #getHeading()}
 * <li> {@link #isPenDown()}
 * </ul>
 *
 * <h3 id="properties">III. Turtle Properties </h3>
 * You can customize the look of both your turtle's path and his on-screen animation. Each pair of the following methods
 * sets and retrieves one property of his appearance:
 * <ul>
 * <li> {@link #setSize(double) setSize(pixels)} / {@link #getSize()} </li>
 * <li> {@link #setColor(java.awt.Color) setColor(Color)} / {@link #getColor()} </li>
 * <li> {@link #setMovementSpeed(double) setMovementSpeed(pixelsPerSecond)} / {@link #getMovementSpeed()} </li>
 * <li> {@link #setTurningSpeed(double) setTurningSpeed(anglePerSecond)} / {@link #getTurningSpeed()} </li>
 * <li> {@link #setStatus(String) setStatus("message")} / {@link #getStatus()} </li>
 * <li> {@link #setPathType(TLPathType) setPathType(PathType)} / {@link #getPathType()} </li>
 * </ul>
 *
 * <h3 id="policy">IV. Angle Policy </h3>
 * You are allowed to control how your Turtle interprets angle measures (that is, in degrees or radians). Simply
 * call the {@link #setAnglePolicy(TLAnglePolicy) setAnglePolicy} method with the appropriate {@link TLAnglePolicy} argument:
 * {@link TLAnglePolicy#Degrees AnglePolicy.Degrees} or {@link TLAnglePolicy#Radians AnglePolicy.Radians}. By default,
 * the turtle's angle policy is set to Degrees. <p>
 *
 * Setting a turtle's angle policy affects all of the methods {@link #turnLeft(double) turnLeft}, {@link #turnRight(double) turnRight},
 * {@link #setHeading(double) setHeading}, {@link #getHeading() getHeading}, {@link #setTurningSpeed(double) setTurningSpeed}
 * and {@link #getTurningSpeed() getTurningSpeed}. Note that changing a turtle's angle policy will not "change" the
 * interpretation of any numbers that you have <i>already</i> given the turtle (only <i>future</i> ones).
 *
 *
 * <h3 id="environment">V. Accessing the Environment </h3>
 * You may wish to change other properties about the simulation itself, such
 * as the background color of the canvas or the title of the window. Take a look at
 * the {@link #getCanvas()} and {@link #getWindow()} methods to access the
 * active {@link TLCanvas} or {@link TLJ2DWindow}, respectively. More information
 * about each is available on their documentation pages.
 *
 * @author Alex Ryan
 * @version 1
 */
public class TLTurtle {

	private List<Point2D> fillShape;// for use to produce fillShapes
	private List<TLListener> listeners;

	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*=============           Constructors            ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/
	/**
	 * Constructs a new Turtle with an appropriate set of default values. After initializing various
	 * helper objects, a {@link #reset()} call is made to set up these defaults. The specified
	 * defaults are listed in the {@link #reset()} documentation.
	 *
	 * @see #reset()
	 */
	public TLTurtle() {
		fillShape = Lists.newArrayList();
		listeners = Lists.newArrayList();
		reset();
	}

	protected TLTurtleState state() {
	    return TLSingletonContext.get().getSimulator().getTurtleState(this);
	}

	protected TLRenderer renderer() {
	    return TLSingletonContext.get().getWindow().getCanvas().getRenderer(this);
	}

	protected void invoke(TLAction action) {
	    TLSingletonContext.get().getSimulator().invokeAndWait(action, this);
	}

	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*=============     Client Interface Methods      ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/

	/**
	 * Resets <b>all</b> of the attributes of the Turtle with a reasonable set of
	 * default values. These defaults can be assumed whenever you are working
	 * with a newly-constructed Turtle. For reference, these values are: <p>
	 *
	 * 	<table rules="rows">
	 * 	<col width="22%" />
	 * 	<col width="78%" />
	 *	<tr>
	 * 	<th align="left">Attribute</th>
	 *	<th align="left">Default Value</th>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>AnglePolicy</tt></td>
	 *	<td><tt>AnglePolicy.Degrees</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>MovementSpeed</tt></td>
	 *	<td><tt>200 (pixels per second)</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>TurningSpeed</tt></td>
	 *	<td><tt>240 (degrees per second)</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>Size</tt></td>
	 *	<td><tt>8 (pixels)</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>PathType</tt></td>
	 *	<td><tt>PathType.Rounded</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>Color</tt></td>
	 *	<td><tt>Color.black</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>Location</tt> (*)</td>
	 *	<td><tt>(0, 0)</tt></td>
	 *	</tr>
	 *	<tr>
	 *	<td><tt>Pen</tt></td>
	 *	<td><tt>Down</tt></td>
	 *	</tr>
	 *	</table> <p>
	 * <b>Please note that even your AnglePolicy gets reset (to Degrees)</b>. <br />
	 * <b>(*)</b> Note: If this turtle has not yet been registered with any canvases, the turtle's
	 * location is unchanged by <tt>reset()</tt>, and the unregistered, untouched default location is <tt>(0, 0)</tt>. This
	 * should never matter to you if you are using the recommended {@link TLTurtle#runTurtleProgram(TLTurtle...)}
	 * method to run your turtles.
	 */
	public void reset() {
	    state().reset();
	}

	/**
	 * Returns the current size of this turtle. The thickness of the turtle's path is
	 * exactly equal to its size, and by default, the actual size of the on-screen Turtle
	 * graphic is roughly five times this value. <p>
	 *
	 * The default value is <tt>1.5</tt>; see also {@link #reset()}.
	 *
	 * @return the size of this turtle
	 *
	 * @see #setSize(double)
	 */
	public double getSize() {
		return state().thickness;
	}

	/**
	 * Changes the size of the turtle's generated path to be exactly equal to
	 * <tt>thickness</tt> pixels. The on-screen turtle image's size will also
	 * scale according to this new thickness. <p>
	 *
	 * Example usage:
	 * <blockquote>
	 * <pre>
	 * 		// inside of your Turtle subclass, e.g. run()
	 *
	 * 		setSize(10);               // 10 pixel thick trail
	 * 		setSize(getSize() * 2);    // doubles the current size
	 * </pre>
	 * </blockquote>
	 *
	 * @param thickness new size for the turtle
	 *
	 * @see #getSize()
	 */
	public void setSize(double thickness) {
	    state().thickness = (float) thickness;
	}

	/**
	 * Returns the current color of the turtle. The color affects the
	 * color of the turtle's trail, the turtle itself and its status bubble,
	 * if the turtle has a {@link #setStatus(String) status}. <p>
	 *
	 * The default is <tt>Color.black</tt>. See also {@link #reset()}.
	 *
	 * @return current color of the turtle
	 * @see #setColor(Color)
	 */
	public Color getColor() {
		return state().color;
	}

	/**
	 * Sets current color of the turtle. The color affects the
	 * color of the turtle's trail, the turtle itself and its status bubble,
	 * if the turtle has a {@link #setStatus(String) status}.<p>
	 *
	 * Be sure to import <tt>java.awt.Color</tt> (or <tt>java.awt.*</tt>) if
	 * you plan on using Colors. <p>
	 *
	 * Example usage:
	 * <blockquote>
	 * <pre>
	 * 		// inside your Turtle subclass, e.g. run()
	 *
	 * 		setColor(Color.red);               // red trail
	 * 		setColor(new Color(255, 180, 0));  // red-orange trail (note: R, G, B)
	 *
	 * 		setColor(Color.blue.darker());	   // slightly darker blue color
	 * 		setColor(Util.randomColor()); // random color!
	 * </pre>
	 * </blockquote>
	 *
	 * @param c a new color for the turtle. <tt>c</tt> must be non-<tt>null</tt>.
	 * @see #getColor()
	 * @see TLUtil#randomColor()
	 */
	public void setColor(Color c) {
	    Preconditions.checkNotNull(c, "Turtle colors cannot be null!");
	    state().color = c;
	}

	/**
	 * Sets the path sharpness for this Turtle's trail. The two accepted
	 * values are {@link TLPathType#Rounded} and {@link TLPathType#Sharp}.
	 * A <tt>Rounded</tt> path has a semicircle capping the ends of all segments
	 * to make it less choppy under some circumstances, where a <tt>Sharp</tt> path
	 * could create a wedge-shaped gap where two thick segments meet
	 * end-to-end at a sufficiently sharp angle. <p>
	 *
	 *  Example usage:
	 *  <blockquote>
	 *  <pre>
	 *  	// inside your Turtle subclass, e.g. run()
	 *  	setPathType(PathType.Rounded);
	 *
	 *  	// or alternatively, the fully qualified name:
	 *  	setPathType(Turtle.PathType.Rounded);
	 *  </pre>
	 *  </blockquote>
	 *
	 * @param type the type of path; one of {@link TLPathType#Rounded} or {@link TLPathType#Sharp}
	 * @see #getPathType()
	 * @see TLPathType
	 */
	public void setPathType(TLPathType type) {
	    state().pathType = type;
	}

	/**
	 * Returns the path sharpness type that is used to draw the Turtle's trail. For more
	 * information, see {@link #setPathType(TLPathType)}. <p>
	 *
	 * The default value is {@link TLPathType#Sharp}. For more information on defaults,
	 * see {@link #reset()}.
	 *
	 * @return the path type of this turtle
	 *
	 * @see #setPathType(TLPathType)
	 * @see TLPathType
	 */
	public TLPathType getPathType() {
		return state().pathType;
	}

	/**
	 * Returns the drawing state of the turtle. If the pen is down, the turtle's trail
	 * is visible; otherwise, the turtle leaves no trail. This state can be altered
	 * through the {@link #penDown()} / {@link #penUp()} methods. <p>
	 *
	 * By default, the pen is set to be <tt>down</tt>. For more information on
	 * defaults, see {@link #reset()}.
	 *
	 * @return the current down-state of the pen
	 * @see #penDown()
	 * @see #penUp()
	 */
	public final boolean isPenDown() {
		return state().isPenDown;
	}

	/**
	 * Causes the turtle to leave a trail as he moves (until the next {@link #penUp()}). The
	 * characteristics of the generated trail can be modified by the {@link #setSize(double) setSize},
	 * {@link #setColor(Color) setColor}, and {@link #setPathType(TLPathType) setPathType} methods. <p>
	 *
	 * The inverse of this method is {@link #penUp()}. As should be expected, additional calls to
	 * <tt>penDown()</tt> before any <tt>penUp()</tt> have no effect. <p>
	 *
	 * Example usage:
	 * <blockquote>
	 * <pre>
	 * 		// in your Turtle subclass, e.g. run()
	 * 		penDown();
	 * 		penDown(); // no effect, already down!
	 * 		moveTo(100, 100);   // turtle leaves trail as he moves
	 * 		penUp();
	 * 		moveTo(100, 200);   // no trail is left as turtle swims
	 * </pre>
	 * </blockquote>
	 *
	 * @see #penUp()
	 * @see #isPenDown()
	 */
	public void penDown() {
	    state().isPenDown = true;
	}

	/**
	 * Causes the turtle to leave no trail as he moves (until the next {@link #penDown()}). <p>
	 *
	 * The inverse of this method is {@link #penDown()}. As should be expected, additional calls to
	 * <tt>penUp()</tt> before any <tt>penDown()</tt> have no effect. <p>
	 *
	 * Example usage:
	 * <blockquote>
	 * <pre>
	 * 		// in your Turtle subclass, e.g. run()
	 * 		penDown();
	 * 		moveTo(100, 100);   // turtle leaves trail as he moves
	 * 		penUp();
	 * 		penUp(); // no effect, already up!
	 * 		moveTo(100, 200);   // no trail is left as turtle swims
	 * </pre>
	 * </blockquote>
	 *
	 * @see #penUp()
	 * @see #isPenDown()
	 */
	public void penUp() {
	    state().isPenDown = false;
	}

	/* Helper method to convert 'angle' interpreted under our anglePolicy to radians. */
	private static double clientAngleToRadians(double angle) {
		switch (TLSingletonContext.get().getAnglePolicy()) {
		case Radians: 	return angle;
		case Degrees: 	return angle * Math.PI / 180;
		default: 		return 0; // unreachable
		}
	}

	/* Helper method to convert 'angle' from radians to the client's anglePolicy. */
	private static double radiansToClientAngle(double angle) {
		switch (TLSingletonContext.get().getAnglePolicy()) {
		case Radians: 	return angle;
		case Degrees:	return angle * 180 / Math.PI;
		default:		return 0; // unreachable
		}
	}

	public void setHeading(double heading) {
	    invoke(TLActions.head((float) heading));
	}

	public void lookAt(double x, double y) {
	    invoke(TLActions.lookAt((float) x, (float) y));
	}

	public void lookAt(Point2D target) {
	    invoke(TLActions.lookAt((float) target.getX(), (float) target.getY()));
	}

	public final double getHeading() {
		return radiansToClientAngle(state().heading);
	}

	public void turnLeft(double angle) {
	    invoke(TLActions.turn(- (float) clientAngleToRadians(angle)));
	}

	public void turnRight(double angle) {
	    invoke(TLActions.turn((float) clientAngleToRadians(angle)));
	}

	public void moveForward(double amount) {
	    invoke(TLActions.forward((float) amount));
	}

	public void moveTo(Point2D location) {
	    invoke(TLActions.moveTo((float) location.getX(), (float) location.getY()));
	}


	public void moveTo(double x, double y) {
	    invoke(TLActions.moveTo((float) x, (float) y));
	}

	public final Point2D getLocation() {
	    return new Point2D.Float(state().location.x, state().location.y);
	}

	public final double getX() {
	    return state().location.x;
	}

	public final double getY() {
	    return state().location.y;
	}

	public void setMovementSpeed(double pixelsPerSecond) {
	    Preconditions.checkArgument(pixelsPerSecond >= TLSimulator.SIMULATION_EPSILON,
	            "Movement speed too slow: must be at least %s. [given=%s]",
	            TLSimulator.SIMULATION_EPSILON,
	            pixelsPerSecond);

	    state().movementSpeed = (float) pixelsPerSecond;
	}

	public final double getMovementSpeed() {
		return state().movementSpeed;
	}

	public void setTurningSpeed(double amountPerSecond) { // note: given in client angle units
        Preconditions.checkArgument(amountPerSecond >= TLSimulator.SIMULATION_EPSILON,
                "Turning speed too slow: must be at least %s. [given=%s]",
                TLSimulator.SIMULATION_EPSILON,
                amountPerSecond);

        state().turningSpeed = (float) clientAngleToRadians(amountPerSecond);
	}

	public final double getTurningSpeed() {
		return radiansToClientAngle(state().turningSpeed);
	}

	public void startFillShape() {
	    Preconditions.checkState(!state().isFilling, "Turtle is already filling a shape!");
	    state().isFilling = true;
	    fillShape.add(state().location);
	}

	public void abandonFillShape() {
	    Preconditions.checkState(state().isFilling, "Turtle is not currently filling any shapes.");
	    state().isFilling = false;
	    fillShape.clear();
	}

	public void endFillShape() {
        Preconditions.checkState(state().isFilling, "Turtle is not currently filling any shapes.");
        /* TODO: shape filling */
//        helperFillShape(canvas.getCanvasGraphics());
        state().isFilling = false;
        fillShape.clear();
	}

	public void fireEvent(String message) {
	    synchronized(listeners) {
	        /* TODO: dispatch this on different thread */
	        for (TLListener listener : listeners) {
	            listener.onTurtleEvent(message, this);
	        }
	    }
	}

	public void addListener(TLListener listener) {
	    synchronized(listeners) {
    	    if (!listeners.contains(listener)) {
    	        listeners.add(listener);
    	    }
	    }
	}

	public void removeListener(TLListener listener) {
	    synchronized(listeners) {
	        listeners.add(listener);
	    }
	}

	public void pause(double seconds, boolean showStatus) {
	    invoke(TLActions.pause((float) seconds, showStatus));
	}

	public void pause(double seconds) {
		pause(seconds, true);
	}

	public void await() {
	    invoke(TLActions.empty());
	}

	public void setStatus(String status) {
	    state().status = status;
	}

	public final String getStatus() {
	    return state().status;
	}

	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*============= Package-Visibility Helper Methods ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/

//	final void helperFillShape(Graphics2D g) {
//	    Stroke s = g.getStroke();
//	    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4));
//	    g.setStroke(this.stroke);
//
//	    Shape poly = createPolygon(fillShape);
//	    g.fill(poly);
//	    g.fill(createPolygon(Lists.reverse(fillShape)));
//
//	    canvas.markDirty(poly.getBounds());
//
//	    g.setStroke(s);
//	}
//
//	private static Shape createPolygon(List<Point2D> points) {
//	    Path2D p = new Path2D.Double();
//	    p.moveTo(points.get(0).getX(), points.get(0).getY());
//	    for (int i = 1; i < points.size(); i++)
//	        p.lineTo(points.get(i).getX(), points.get(i).getY());
//	    p.closePath();
//	    return p;
//	}
//	private synchronized void commit() {
//	    /* TODO: commit logic */
//	    sprite.commitPath();
//	    if (filling && (fillShape.isEmpty() || !fillShape.get(fillShape.size() - 1).equals(location))) {
//	        fillShape.add(location);
//	    }
//	}


	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*=============    Static Utility/Main Methods    ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/

	public static void printHelp() {
		System.err.println("The format for a proper Turtle class is: ");
		System.err.println();
		System.err.println("public class TurtleTest extends Turtle");
		System.err.println("{");
		System.err.println("    public void run()");
		System.err.println("    {");
		System.err.println("        // do your program logic here");
		System.err.println("    }");
		System.err.println();
		System.err.println("    public static void main(String[] args)");
		System.err.println("    {");
		System.err.println("		Turtle.runTurtleProgram(new TurtleTest());");
		System.err.println("    }");
		System.err.println("}");
	}

}
