package org.arper.turtle;

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