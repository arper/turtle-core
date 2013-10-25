package org.arper.__old__.turtle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.arper.__old__.turtle.impl.TLMutableState;
import org.arper.__old__.turtle.impl.TLUtil;
import org.arper.__old__.turtle.impl.TLVector;
import org.arper.turtle.impl.TLAction;
import org.arper.turtle.impl.TLActions;
import org.arper.turtle.ui.TLConsole;
import org.arper.turtle.ui.TLWindow;

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
 * {@link Turtle#runTurtleProgram(Turtle...) runTurtleProgram}. </p>
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
 * <li> {@link #setPathType(PathType) setPathType(PathType)} / {@link #getPathType()} </li>
 * </ul>
 * 
 * <h3 id="policy">IV. Angle Policy </h3>
 * You are allowed to control how your Turtle interprets angle measures (that is, in degrees or radians). Simply 
 * call the {@link #setAnglePolicy(AnglePolicy) setAnglePolicy} method with the appropriate {@link AnglePolicy} argument:
 * {@link AnglePolicy#Degrees AnglePolicy.Degrees} or {@link AnglePolicy#Radians AnglePolicy.Radians}. By default, 
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
 * active {@link TLCanvas} or {@link TLWindow}, respectively. More information
 * about each is available on their documentation pages.
 * 
 * @author Alex Ryan
 * @version 1
 */
public abstract class Turtle {

	/**
	 * Enumeration that contains acceptable values for a turtle's interpretation
	 * of supplied angle values -- that is, whether the angles you give should be
	 * interpreted as radians or as degrees. A more detailed explanation is provided
	 * under the <b>Angle Policy</b> section of the Turtle class overview. <p>
	 * 
	 *  The default value for a turtle's angle policy is Degrees. <p>
	 *  
	 *  Example usage:
	 *  <blockquote>
	 *  <pre>
	 *  	// inside your Turtle subclass, e.g. run()
	 *  	setAnglePolicy(AnglePolicy.Degrees);
	 *  
	 *  	// or alternatively, the fully qualified name:
	 *  	setAnglePolicy(Turtle.AnglePolicy.Degrees);
	 *  </pre>
	 *  </blockquote>
	 *  
	 *  @see Turtle#getAnglePolicy()
	 *  @see Turtle#setAnglePolicy(AnglePolicy)
	 *
	 */
	public enum AnglePolicy {
		Radians, 
		Degrees
	}

	/**
	 * Enumeration that contains acceptable values for the sharpness of a turtle's generated path. 
	 * The value {@link #Rounded} will cause all segments drawn by the turtle to 
	 * be capped at the ends with a semicircle, where the value {@link #Sharp} will
	 * leave all segments alone, so two segments meeting at a sharp angle and
	 * sufficient thickness end-to-end may create a wedged gap where they meet. <p>
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
	 *  @see Turtle#getPathType()
	 *  @see Turtle#setPathType(PathType)
	 */
	public enum PathType {
		Rounded,
		Sharp
	}

	
	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*=============        Instance Variables         ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/
	
	/* attributes */
	private TLCanvas canvas;
	private final Point2D location;		// pixels
    private volatile double heading;         // radians
	private volatile double movementSpeed;	// pixels per second
	private volatile double turningSpeed;	// radians per second
	private volatile Color color;			// note: non-null
	private AnglePolicy anglePolicy;
	private volatile PathType pathType;
	private volatile boolean down;
	private volatile float thickness;		// pixels
	private volatile String status;
	
	private List<Point2D> fillShape;// for use to produce fillShapes
	private boolean filling;

	/* Helper objects */
	private Stroke stroke;
	private TLRenderer sprite;
	private List<TurtleListener> listeners;

	protected TLConsole console;

	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*=============           Constructors            ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/
	/**
	 * Turtle constructor that automatically registers the constructed
	 * turtle with the given canvas. Equivalent to a default construction
	 * followed by registering the turtle with the <tt>canvas</tt>, i.e.
	 * 
	 * <blockquote>
	 * <pre>
	 * 		Canvas c = ...;
	 * 		Turtle t1 = new Turtle(c);
	 * 
	 * 		Turtle t2 = new Turtle();
	 * 		c.registerTurtle(t2);
	 * 
	 * 		// t1 and t2 are now in equivalent states
	 * </pre>
	 * </blockquote>
	 * 
	 * For more information about Turtle construction, see the {@linkplain Turtle#Turtle() default constructor.} 
	 * 
	 * @param canvas the canvas with which to register the newly created turtle
	 * 
	 * @see Turtle#Turtle()
	 * @see TLCanvas#registerTurtle(Turtle)
	 */
	public Turtle(TLCanvas canvas) {
		this();
		canvas.registerTurtle(this);
	}

	/**
	 * Constructs a new Turtle with an appropriate set of default values. After initializing various
	 * helper objects, a {@link #reset()} call is made to set up these defaults. The specified
	 * defaults are listed in the {@link #reset()} documentation.
	 * 
	 * The constructed Turtle will be unusable until it has been registered with an
	 * {@link TLCanvas}. Invoking any of the Turtle methods before then will cause
	 * undefined behavior that may result in a crash or unintended results. <p>
	 * 
	 * <b>Note:</b> The recommended way for running your Turtle programs, {@link Turtle#runTurtleProgram(Turtle...)},
	 * registers all of the supplied Turtles with a newly-created {@link TLCanvas} before calling each of their
	 * <tt>run()</tt> methods. You should not try to create your own <tt>Canvas</tt> when running your
	 * program in this recommended way.
	 * 
	 * @see #reset()
	 * @see #Turtle(TLCanvas)
	 * @see TLCanvas#registerTurtle(Turtle)
	 */
	public Turtle() {
		sprite = new TLRenderer(this);
		location = new Point2D.Double();
		fillShape = Lists.newArrayList();
		listeners = Lists.newArrayList();
		reset();
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
	 * should never matter to you if you are using the recommended {@link Turtle#runTurtleProgram(Turtle...)}
	 * method to run your turtles.
	 */
	public void reset() {
		setAnglePolicy(AnglePolicy.Degrees);
		setMovementSpeed(200);
		setTurningSpeed(radiansToClientAngle(1.5 * Math.PI));
		setSize(8);
		setPathType(PathType.Rounded);
		setColor(Color.black);
		fillShape.clear();
		filling = false;
		if (canvas != null) {
			penUp();
			moveTo(0, 0);
		}
		penDown();
	}

	private void refreshStroke() {
		if (pathType == PathType.Sharp) {
			stroke = new BasicStroke(thickness, BasicStroke.CAP_BUTT,  BasicStroke.JOIN_BEVEL);
		} else {
			stroke = new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		}
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
		return thickness;
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
		this.thickness = (float) thickness;
		refreshStroke();
		sprite.updateStatusBubble();
		sprite.markDirty();
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
		return color;
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
		if (c == null) {
			System.err.println("Turtle colors cannot be null!");
		} else {
			color = c;
			sprite.updateColor();
		}
	}

	/**
	 * Sets the path sharpness for this Turtle's trail. The two accepted
	 * values are {@link PathType#Rounded} and {@link PathType#Sharp}.
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
	 * @param type the type of path; one of {@link PathType#Rounded} or {@link PathType#Sharp}
	 * @see #getPathType()
	 * @see PathType
	 */
	public void setPathType(PathType type) {
		pathType = type;
		refreshStroke();
	}

	/**
	 * Returns the path sharpness type that is used to draw the Turtle's trail. For more
	 * information, see {@link #setPathType(PathType)}. <p>
	 * 
	 * The default value is {@link PathType#Sharp}. For more information on defaults,
	 * see {@link #reset()}.
	 * 
	 * @return the path type of this turtle
	 * 
	 * @see #setPathType(PathType)
	 * @see PathType
	 */
	public PathType getPathType() {
		return pathType;
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
		return down;
	}

	/**
	 * Causes the turtle to leave a trail as he moves (until the next {@link #penUp()}). The 
	 * characteristics of the generated trail can be modified by the {@link #setSize(double) setSize}, 
	 * {@link #setColor(Color) setColor}, and {@link #setPathType(PathType) setPathType} methods. <p>
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
		down = true;
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
		down = false;
	}

	/**
	 * Returns the current {@link AnglePolicy} of the turtle, which defines how angles
	 * are interpreted by the turtle (as Degrees or Radians). <p>
	 * 
	 * The default value for the turtle's <tt>AnglePolicy</tt> is {@link AnglePolicy#Degrees AnglePolicy.Degrees}.
	 * For more information about default values, see {@link #reset()}.
	 * 
	 * @return the current AnglePolicy of this turtle
	 * 
	 * @see #setAnglePolicy(AnglePolicy)
	 * @see AnglePolicy
	 */
	public AnglePolicy getAnglePolicy() {
		return anglePolicy;
	}

	/**
	 * Sets how the turtle should interpret angle measures that are passed as parameters to its
	 * methods (as Radians or as Degrees). Acceptable values for <tt>policy</tt> are
	 * {@link AnglePolicy#Degrees} or {@link AnglePolicy#Radians}.
	 * 
	 * Setting a turtle's angle policy affects all of the methods {@link #turnLeft(double) turnLeft}, {@link #turnRight(double) turnRight},
	 * {@link #setHeading(double) setHeading}, {@link #getHeading() getHeading}, {@link #setTurningSpeed(double) setTurningSpeed} 
	 * and {@link #getTurningSpeed() getTurningSpeed}. Note that changing a turtle's angle policy will not "change" the
	 * interpretation of any numbers that you have <i>already</i> given the turtle (only <i>future</i> ones).
	 * 
	 *  <blockquote>
	 *  <pre>
	 *  	// inside your Turtle subclass, e.g. run()
	 *  	setAnglePolicy(AnglePolicy.Degrees);
	 *  	turnRight(360); // turn around once, clockwise
	 *  
	 *  	// or alternatively, a fully qualified name:
	 *  	setAnglePolicy(Turtle.AnglePolicy.Radians);
	 *  	setHeading(Math.PI / 2); // look 'up' on y-axis
	 *		                         // note: PI / 2 radians = 90 degrees 
	 *  </pre>
	 *  </blockquote>
	 * 
	 * @param policy new angle policy to use for this Turtle
	 * 
	 * @see #getAnglePolicy()
	 * @see AnglePolicy
	 */
	public void setAnglePolicy(AnglePolicy policy) {
		this.anglePolicy = policy;
	}

	/* Helper method to convert 'angle' interpreted under our anglePolicy to radians. */
	private double clientAngleToRadians(double angle) {
		switch (anglePolicy) {
		case Radians: 	return angle;
		case Degrees: 	return angle * Math.PI / 180;
		default: 		return 0; // unreachable
		}
	}

	/* Helper method to convert 'angle' from radians to the client's anglePolicy. */
	private double radiansToClientAngle(double angle) {
		switch (anglePolicy) {
		case Radians: 	return angle;
		case Degrees:	return angle * 180 / Math.PI;
		default:		return 0; // unreachable
		}
	}

	public void setHeading(double heading) {
		double newHeading = clientAngleToRadians(-heading);
		double diff = (newHeading - this.heading) % (2 * Math.PI);
		if (diff < 0) diff += 2 * Math.PI;
		if (diff > Math.PI) diff -= 2 * Math.PI;

		animateTurnRight(diff);
	}

	public void lookAt(double x, double y) {
        TLVector l = new TLVector(x, y).subtract(new TLVector(this.location));
        if (l.lengthSquared() < TLAnimator.EPSILON) {
            return;
        }

        setHeading(-radiansToClientAngle(l.angle()));
	}

	public void lookAt(Point2D target) {
	    TLVector l = new TLVector(target).subtract(new TLVector(this.location));
		if (l.lengthSquared() < TLAnimator.EPSILON) {
			return;
		}

		setHeading(-radiansToClientAngle(l.angle()));
	}

	public final double getHeading() {
		return radiansToClientAngle(heading);
	}

	public void turnLeft(double angle) {
	    animateTurnRight(-clientAngleToRadians(angle));
	}

	public void turnRight(double angle) {
	    animateTurnRight(clientAngleToRadians(angle));
	}

	public void moveForward(double amount) {
	    TLVector nextLocation = new TLVector(location).add(TLVector.unitVectorInDirection(heading).scale(amount));
		animateMoveTo(nextLocation.asPoint2D());
	}

	public void moveTo(Point2D location) {
		lookAt(location);
		animateMoveTo(location);
	}


	public void moveTo(double x, double y) {
		moveTo(new Point2D.Double(x, y));
	}

	public final Point2D getLocation() {
		return location;
	}

	public final double getX() {
		return location.getX();
	}

	public final double getY() {
		return location.getY();
	}

	public void setMovementSpeed(double pixelsPerSecond) {
		if (pixelsPerSecond <= TLAnimator.EPSILON) {
			System.err.println("Error: attempt to set illegal movement speed: " + pixelsPerSecond);
			System.err.println(" > Movement speed must be positive!");
		} else {
			movementSpeed = pixelsPerSecond;
		}
	}

	public final double getMovementSpeed() {
		return movementSpeed;
	}

	public void setTurningSpeed(double amountPerSecond) { // note: given in client angle units
		if (amountPerSecond <= TLAnimator.EPSILON) {
			System.err.println("Error: attempt to set illegal turning speed: " + amountPerSecond);
			System.err.println(" > Turning speed must be positive!");
		} else {
			turningSpeed = clientAngleToRadians(amountPerSecond);
		}
	}

	public final double getTurningSpeed() {
		return radiansToClientAngle(turningSpeed);
	}
	
	public void startFillShape() {
	    Preconditions.checkState(!filling, "Turtle is already filling a shape!");
	    filling = true;
	    fillShape.add(location);
	}
	
	public void abandonFillShape() {
	    Preconditions.checkState(filling, "Turtle is not currently filling any shapes.");
	    filling = false;
	    fillShape.clear();
	}
	
	public void endFillShape() {
        Preconditions.checkState(filling, "Turtle is not currently filling any shapes.");
        helperFillShape(canvas.getCanvasGraphics());
        filling = false;
        fillShape.clear();
	}

	public void fireEvent(String message) {
	    synchronized(listeners) {
	        for (TurtleListener listener : listeners) {
	            listener.onTurtleEvent(message, this);
	        }
	    }
	}
	
	public void addListener(TurtleListener listener) {
	    synchronized(listeners) {
    	    if (!listeners.contains(listener)) {
    	        listeners.add(listener);
    	    }
	    }
	}
	
	public void removeListener(TurtleListener listener) {
	    synchronized(listeners) {
	        listeners.add(listener);
	    }
	}
	
	public void pause(double seconds, boolean showStatus) {
	    invoke(TLActions.pause(seconds, showStatus));
	}

	public void pause(double seconds) {
		pause(seconds, true);
	}

	public void await() {
	    if (canvas != null) {
	        canvas.getAnimator().await(this);
	    }
	}

	public void setStatus(String status) {
		if (status == null) status = "";

		if (!this.status.equals(status)) {
			sprite.markDirty();
			this.status = status;
			sprite.updateStatusBubble();
			sprite.markDirty();
		}
	}

	public final String getStatus() {
		return status;
	}
	
	public void setAnimation(TLAnimation animation) {
		sprite.setAnimation(animation);
	}
	
	public TLAnimation getAnimation() {
		return sprite.getAnimation();
	}
	
	public Stroke getStroke() {
	    return stroke;
	}

	protected final TLCanvas getCanvas() {
		return canvas;
	}
	
	protected final TLWindow getWindow() {
		return canvas.getWindow();
	}

	/*--------------------------------------------------------------*/
	/*==============================================================*/
	/*============= Package-Visibility Helper Methods ==============*/
	/*==============================================================*/
	/*--------------------------------------------------------------*/


	final void helperSetCanvas(TLCanvas canvas) {
		this.canvas = canvas;
		console = canvas.getWindow().console;
	}
	
	final void helperFillShape(Graphics2D g) {
	    Stroke s = g.getStroke();
	    g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4));
	    g.setStroke(this.stroke);
	    
	    Shape poly = createPolygon(fillShape);
	    g.fill(poly);
	    g.fill(createPolygon(Lists.reverse(fillShape)));
	    
	    canvas.markDirty(poly.getBounds());
	    
	    g.setStroke(s);
	}
	
	private static Shape createPolygon(List<Point2D> points) {
	    Path2D p = new Path2D.Double();
	    p.moveTo(points.get(0).getX(), points.get(0).getY());
	    for (int i = 1; i < points.size(); i++)
	        p.lineTo(points.get(i).getX(), points.get(i).getY());
	    p.closePath();
	    return p;
	}

	private void animateTurnRight(double radians) {
	    invoke(TLActions.turn(radians));
	}
	
	private void animateMoveTo(Point2D point) {
	    invoke(TLActions.moveTo(point.getX(), point.getY()));
	}
	
	private void invoke(TLAction action) {
	    if (canvas == null) {
	        action.execute(getMutableState());
	    } else {
	        canvas.getAnimator().invokeAndWait(action, this);
	    }
	    commit();
	}

	private synchronized void commit() {
	    sprite.commitPath();
	    if (filling && (fillShape.isEmpty() || !fillShape.get(fillShape.size() - 1).equals(location))) {
	        fillShape.add(location);
	    }
	}
    
	private final TLMutableState mutableRep = new TLMutableState() {
	    
        @Override
        public void setHeading(double heading) {
            Turtle.this.heading = heading;
        }

        @Override
        public void setLocation(double x, double y) {
            location.setLocation(x, y);
        }

        @Override
        public double getTurnRate() {
            return Turtle.this.turningSpeed;
        }

        @Override
        public double getHeading() {
            return Turtle.this.heading;
        }

        @Override
        public double getSpeed() {
            return Turtle.this.movementSpeed;
        }

        @Override
        public double getX() {
            return Turtle.this.location.getX();
        }

        @Override
        public double getY() {
            return Turtle.this.location.getY();
        }

        @Override
        public String getStatus() {
            return Turtle.this.status;
        }

        @Override
        public void setStatus(String status) {
            Turtle.this.setStatus(status);
        }

	};
	
	/* package-private */ TLMutableState getMutableState() {
	    return mutableRep;
	}
	
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
	
	public static TLWindow runTurtleProgram(Turtle... turtles) {
		return TLApplication.run(Toolkit.getDefaultToolkit().getScreenSize().width * 3 / 4,
				Toolkit.getDefaultToolkit().getScreenSize().height * 3 / 4, turtles);
	}
    
	public static TLWindow runTurtleProgram(final int width, final int height, 
	        final Turtle... turtles) {
	    return TLApplication.run(width, height, turtles);
	}
	
	public void init() {
		// do nothing by default.
	}

	public void run() {
		System.err.println("Error: your Turtle program appears to be made incorrectly. Did you forget your run() method?");
		printHelp();
		System.exit(1);
	}

	public void run(int index) {
		run();
	}
}
