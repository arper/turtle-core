package org.arper.__old__.turtle;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.arper.__old__.turtle.Turtle.PathType;
import org.arper.__old__.turtle.impl.TLUtil;
import org.arper.__old__.turtle.impl.TLVector;
import org.arper.turtle.impl.TLStatusBubble;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;

public class TLRenderer {
    private static final int MAX_SEGMENT_PAINT_LENGTH = 50;
    private static final int MAX_SEGMENT_PAINT_LENGTH_SQ = MAX_SEGMENT_PAINT_LENGTH * MAX_SEGMENT_PAINT_LENGTH;

	public TLRenderer(Turtle owner) {
		this.owner = owner;
		transparency = 0.8f;
		statusBubbleLock = new ReentrantLock();
		turtleAnimation = new DefaultTurtleAnimation();
	}

    protected final Turtle owner;
    private final Lock statusBubbleLock;
    
    private float transparency;
    private TLStatusBubble statusBubble;
    private TLAnimation turtleAnimation;
    private Point2D lastTrackedLocation;
	
	private int getMaxDimension() {
		return Math.max(turtleAnimation.getBoundingWidth(), turtleAnimation.getBoundingHeight());
	}

	public void setAnimation(TLAnimation animation) {
		if (animation == null) {
			System.err.println("Error: invalid turtle animation. Animations can't be null!");
		} else if (turtleAnimation != animation){
			markDirty();
			turtleAnimation = animation;
			markDirty();
		}
	}
	
	public TLAnimation getAnimation() {
		return turtleAnimation;
	}

	public void updateColor() {
		updateComposite();
	}
	
	private void updateComposite() {
        turtleComposite = new TLUtil.MultiplyColorComposite(owner.getColor(), 0.4f);
	}
	
	private static final AlphaComposite bubbleComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
	private Composite turtleComposite;
	public double getTransparency() {
		return transparency;
	}

	public void setTransparency(double alpha) {
		this.transparency = (float) alpha;
		updateComposite();
	}
	
	public void updateStatusBubble() {
		try {
			statusBubbleLock.lock();
			if (!owner.getStatus().equals("")) {
				float start = (float) turtleAnimation.getSize(owner) * .3f;
				float len = (float) 15;
				statusBubble = new TLStatusBubble(start, -start, len, owner.getStatus(), 8);
			} else {
				statusBubble = null;
			}
		} finally {
			statusBubbleLock.unlock();
		}
	}
	
	public void screenRender(Graphics2D g) {
		if (owner.isPenDown()) {
			double cSize = turtleAnimation.getCursorSize(owner);
			g.setColor(owner.getColor());
			g.fill(new Ellipse2D.Double(owner.getX() - cSize / 2, owner.getY() - cSize / 2, cSize, cSize));
			drawPathSegment(g);
			syncCanvasPath(false);
		}
		
		AffineTransform at = g.getTransform();
		
		try {
			statusBubbleLock.lock();
			if (statusBubble != null) {
				g.translate(owner.getX(), owner.getY());
				g.setColor(Color.white);
				g.setComposite(bubbleComposite);
				g.fill(statusBubble.getShape());
				g.setColor(owner.getColor().darker().darker());
				g.draw(statusBubble.getShape());
				statusBubble.drawText(g);
				g.setTransform(at);
			}
		} finally {
			statusBubbleLock.unlock();
		}
		
		if (transparency > 0) {
		    double size = turtleAnimation.getSize(owner);
			double scale = size / getMaxDimension();

			g.translate(owner.getX(), owner.getY());
//			double elapsedSeconds = owner.helperGetAnimationHelper().getElapsedSeconds();
//			double elapsedDistance = owner.helperGetAnimationHelper().getTotalMillipixels() / 100000.0;
			double elapsedSeconds = 0;
			double elapsedDistance = 0;
			double elapsed = elapsedDistance + elapsedSeconds / 2;
			
			AffineTransform at2 = g.getTransform();
			BufferedImage[] images = turtleAnimation.getImages();
			for (int i = 0; i < images.length; i++) {
			    if (images[i] == null)
			        continue;

                double tX = turtleAnimation.getBoundingWidth() * turtleAnimation.getCenterX(i);
                double tY = turtleAnimation.getBoundingHeight() * turtleAnimation.getCenterY(i);
                double rotateAmount = owner.getMutableState().getHeading() + turtleAnimation.getPieceRotation(i, elapsed);
                g.rotate(rotateAmount);

                BufferedImage pretty = getTurtleImage((int) Math.ceil(size), images[i]);
			    if (pretty != null) {
			        g.translate(-turtleAnimation.getCenterX(i) * pretty.getWidth(), 
			                -turtleAnimation.getCenterY(i) * pretty.getHeight());
			        drawImage(pretty, g);
			    } 
			    else {
		            g.scale(scale, scale);
                    g.translate(-tX, -tY);
                    drawImage(pretty, g);
			    }
			    
			    g.setTransform(at2);
			}
	
		}
		g.setTransform(at);
	}
    
    private void drawPathSegment(Graphics2D g) {
        Point2D location = owner.getLocation();
        TLVector dir = new TLVector(location).subtract(new TLVector(lastTrackedLocation));
        if (dir.lengthSquared() > 0) {
            Stroke s = g.getStroke();
            g.setStroke(owner.getStroke());
            
            if (owner.getPathType() == PathType.Rounded) {
                g.draw(new Line2D.Double(lastTrackedLocation, location));
            } else { 
                double len_sq = dir.lengthSquared();
                double extension = -Math.expm1(-len_sq / 12) / 2; // extension = (1 - e^{len_sq / 4}) / 2
                dir = dir.normalize();
                Point2D augmentedLoc = new TLVector(location).add(dir.scale(extension)).asPoint2D();
                Point2D loweredLast = new TLVector(lastTrackedLocation).add(dir.scale(-extension)).asPoint2D();
                g.draw(new Line2D.Double(loweredLast, augmentedLoc));
            }
            g.setStroke(s);
        }
    }
    
    public void commitPath() {
        syncCanvasPath(true);
    }
    
    private void syncCanvasPath(boolean isFinal) {
        Point2D newLocation = owner.getLocation();
        if (newLocation.equals(lastTrackedLocation)) {
            return;
        } else if (lastTrackedLocation == null) {
            lastTrackedLocation = newLocation;
            return;
        }
        
        if (isFinal || newLocation.distanceSq(lastTrackedLocation) > MAX_SEGMENT_PAINT_LENGTH_SQ) {
            if (owner.isPenDown()) {
                drawPathSegment(owner.getCanvas().getCanvasGraphics());
                owner.getCanvas().markDirty(lastTrackedLocation, newLocation);
            }
            lastTrackedLocation.setLocation(newLocation);
        }
    }
	
	private void drawImage(BufferedImage i, Graphics2D g) {
        if (TLUtil.isMacComputer) {
            g.drawImage(i, 0, 0, null);
        } else {
            Composite c = g.getComposite();
            g.setComposite(turtleComposite);
	        g.drawImage(i, 0, 0, null);
            g.setComposite(c);
	    }
	}
	
	public void markDirty() {
		if (owner.getCanvas() == null) {
			return;
		}
        Point2D spriteOrigin = owner.getLocation();
		
		if (statusBubble != null) {
			Rectangle r = statusBubble.getShape().getBounds();
			r.translate((int)Math.round(spriteOrigin.getX()), (int)Math.round(spriteOrigin.getY()));
			owner.getCanvas().markDirty(r);
		}
		double maxSize = Math.max(turtleAnimation.getSize(owner) / Math.sqrt(2), turtleAnimation.getCursorSize(owner) / 2);
		owner.getCanvas().markDirty(spriteOrigin, (int)Math.ceil(maxSize));
	}
	
	private static final LoadingCache<Map.Entry<Integer, BufferedImage>, BufferedImage> imageCache;
	static {
	    imageCache = CacheBuilder.newBuilder().concurrencyLevel(4).build(
	            new CacheLoader<Map.Entry<Integer, BufferedImage>, BufferedImage>() {

                    @Override
                    public BufferedImage load(Entry<Integer, BufferedImage> e)
                            throws Exception {
                        return Scalr.apply(Scalr.resize(e.getValue(), Method.ULTRA_QUALITY, e.getKey()), Scalr.OP_ANTIALIAS);
                    }
	            });
	}
	private static BufferedImage getTurtleImage(int size, BufferedImage im) {
	    Map.Entry<Integer, BufferedImage> e = Maps.immutableEntry(size, im);
	    imageCache.refresh(e);
	    return imageCache.getIfPresent(e);
	}
}
