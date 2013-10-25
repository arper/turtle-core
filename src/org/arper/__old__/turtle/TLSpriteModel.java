package org.arper.__old__.turtle;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

public interface TLSpriteModel {
    
    float getBoundingWidth();
    float getBoundingHeight();
    
    float getSize(Turtle t);
    float getCursorSize(Turtle t);
    
    List<Piece> getPieces();
    
    public static interface Piece {
        Point2D getRotationCenter();
        Point2D getCenterOffset();
        BufferedImage getImage();
        float getRotation(float sec);
    }
}
