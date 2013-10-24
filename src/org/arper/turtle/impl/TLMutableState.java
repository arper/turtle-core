package org.arper.turtle.impl;


/**
 * Raw physical representation of a body in space.
 * All units are in radians, pixels, seconds.  
 */
public interface TLMutableState {
    double getTurnRate();
    double getHeading();
    void setHeading(double heading);
    
    double getSpeed();
    double getX();
    double getY();
    void setLocation(double x, double y);
    
    String getStatus();
    void setStatus(String status);
}
