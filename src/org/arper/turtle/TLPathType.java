package org.arper.turtle;

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
 *  @see TLTurtle#getPathType()
 *  @see TLTurtle#setPathType(TLPathType)
 */
public enum TLPathType {
	Rounded,
	Sharp
}