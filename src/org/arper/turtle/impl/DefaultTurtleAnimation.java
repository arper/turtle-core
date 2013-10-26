package org.arper.turtle.impl;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.arper.turtle.Turtle;
import org.arper.turtle.impl.display.TLAnimation;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;


public class DefaultTurtleAnimation implements TLAnimation {

	protected static final int PREFERRED_SIZE = 128;
	private static final BufferedImage[] defaultImages;
	private static final String[] DEFAULT_PIECES = {"body.png", "lflipper.png", "rflipper.png", "lfoot.png", "rfoot.png"};
	private static final int BODY_IMAGE = 0, LFLIPPER_IMAGE = 1, RFLIPPER_IMAGE = 2, LFOOT_IMAGE = 3, RFOOT_IMAGE = 4;
	private static int defaultImageWidth, defaultImageHeight;

	static {
		defaultImages = new BufferedImage[DEFAULT_PIECES.length];
		for (int i = 0; i < defaultImages.length; i++) {
		    URL imgURL = ClassLoader.getSystemResource("sprite/" + DEFAULT_PIECES[i]);
		    if (imgURL == null) {
                JOptionPane.showMessageDialog(null, "Could not find turtle image: " + DEFAULT_PIECES[i], "Error loading resources", JOptionPane.ERROR_MESSAGE);
                continue;
		    }
			BufferedImage img;
			try {
			    img = ImageIO.read(imgURL);
			} catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error loading turtle image " + DEFAULT_PIECES[i] + ":\n" + e.getMessage(), "Error loading resources", JOptionPane.ERROR_MESSAGE);
                continue;
			}
			double scaleFactor = PREFERRED_SIZE * 1.0 / Math.max(img.getWidth(), img.getHeight());

			int imgWidth = (int)(img.getWidth() * scaleFactor);
			int imgHeight = (int)(img.getHeight() * scaleFactor);

			if (i == 0) {
				defaultImageWidth = imgWidth;
				defaultImageHeight = imgHeight;
			} else if (defaultImageWidth != imgWidth || defaultImageHeight != imgHeight) {
				System.err.println("Error: incompatible turtle piece " + DEFAULT_PIECES[i] + " (different aspect ratio)");
				continue;
			}
			defaultImages[i] = Scalr.resize(img, Method.ULTRA_QUALITY, PREFERRED_SIZE);
		}
	}

	@Override
	public BufferedImage[] getImages() {
		return defaultImages;
	}

	@Override
	public double getCenterX(int piece) {
		return 0.44;
	}

	@Override
	public double getCenterY(int piece) {
		return 0.5;
	}

	@Override
	public double getSize(Turtle t) {
		return t.getSize() * 5 + 15;
	}

	@Override
	public double getCursorSize(Turtle t) {
		return t.getSize() * 1.4;
	}

	private double getFootAnimationCyclePosition(double s) {
		s = (s % 2); // flick every two seconds
		if (s < .75) return -1;
		else if (s < 1) return 8 * s - 7; // -1 --> 1
		else return 3 - 2 * s; // 1 --> -1
	}

	private double getFlipperAnimationCyclePosition(double s) {
		return Math.sin(s * 3);
	}

	@Override
	public double getPieceRotation(int piece, double sec) {
		double pos;
		final double waveAmount = 0.15;
		double sign;

		switch (piece) {
		case LFLIPPER_IMAGE:
			pos = getFlipperAnimationCyclePosition(sec);
			sign = 1;
			break;
		case RFOOT_IMAGE:
			pos = getFootAnimationCyclePosition(sec);
			sign = 1;
			break;
		case LFOOT_IMAGE:
			pos = getFootAnimationCyclePosition(sec);
			sign = -1;
			break;
		case RFLIPPER_IMAGE:
			pos = getFlipperAnimationCyclePosition(sec);
			sign = -1;
			break;
		case BODY_IMAGE:
		default:
			pos = 0;
			sign = 0;
			break;
		}

		return sign * pos * waveAmount;
	}

	@Override
	public int getBoundingWidth() {
		return defaultImageWidth;
	}

	@Override
	public int getBoundingHeight() {
		return defaultImageHeight;
	}

	@Override
	public int getPieceWidth(int piece) {
		return getBoundingWidth();
	}

	@Override
	public int getPieceHeight(int piece) {
		return getBoundingHeight();
	}
}
