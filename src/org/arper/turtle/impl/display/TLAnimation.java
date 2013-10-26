package org.arper.turtle.impl.display;

import java.awt.image.BufferedImage;

import org.arper.turtle.Turtle;

public interface TLAnimation {
    public abstract BufferedImage[] getImages();
    public abstract int getBoundingWidth();
    public abstract int getBoundingHeight();

    public abstract int getPieceWidth(int piece);
    public abstract int getPieceHeight(int piece);
    public abstract double getCenterX(int piece);
    public abstract double getCenterY(int piece);
    public abstract double getPieceRotation(int piece, double sec);

    public abstract double getSize(Turtle t);
    public abstract double getCursorSize(Turtle t);


    public static final TLAnimation Empty = new TLAnimation() {
        @Override
        public BufferedImage[] getImages() {
            return new BufferedImage[0];
        }

        @Override
        public int getBoundingWidth() {
            return 0;
        }

        @Override
        public int getBoundingHeight() {
            return 0;
        }

        @Override
        public int getPieceWidth(int piece) {
            return 0;
        }

        @Override
        public int getPieceHeight(int piece) {
            return 0;
        }

        @Override
        public double getCenterX(int piece) {
            return 0;
        }

        @Override
        public double getCenterY(int piece) {
            return 0;
        }

        @Override
        public double getPieceRotation(int piece, double sec) {
            return 0;
        }

        @Override
        public double getSize(Turtle t) {
            return 0;
        }

        @Override
        public double getCursorSize(Turtle t) {
            return 0;
        }
    };
}
