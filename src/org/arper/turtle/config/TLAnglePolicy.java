package org.arper.turtle.config;

import org.arper.turtle.TLTurtle;


/**
 * Enumeration that contains acceptable values for the interpretation
 * of supplied angle values -- that is, whether the angles you give should be
 * interpreted as radians or as degrees.
 *
 * The default value for the application's angle policy is Degrees. <p>
 *
 * @see {@link TLApplicationConfig#getAnglePolicy()}
 * @see {@link TLTurtle#turnLeft(double)}
 * @see {@link TLTurtle#getHeading()}
 *
 */
public enum TLAnglePolicy {
	Radians,
	Degrees
}