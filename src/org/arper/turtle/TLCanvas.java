package org.arper.turtle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import org.arper.turtle.impl.TLUtil;
import org.arper.turtle.impl.TurtleLogging;
import org.asper.turtle.ui.TLWindow;

import com.google.common.collect.Lists;


@SuppressWarnings("serial")
public class TLCanvas extends JPanel {
    
	private BufferedImage backBuffer;
	private int drawableWidth, drawableHeight;
	private List<Turtle> turtles;
	private TLAnimator animator;
	private float zoom;
	private TLWindow parent;
	
	
	public TLCanvas(int width, int height, TLWindow w) {
	    drawableWidth = width;
	    drawableHeight = height;
	    parent = w;
        animator = new TLAnimator();
	    reset();
	}
	
	public void reset() {
        setLayout(new BorderLayout());
        turtles = Lists.newArrayList();
        animator.reset();
        zoom = 1.0f;
        createBuffer();
        setDoubleBuffered(true);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Math.round(drawableWidth * zoom), Math.round(drawableHeight * zoom));
	}

	public TLWindow getWindow() {
		return parent;
	}
	
	public void registerTurtle(Turtle t) {
		if (!turtles.contains(t)) {
			turtles.add(t);
			t.helperSetCanvas(this);
			animator.registerTurtle(t);
		}
	}
	
	public TLAnimator getAnimator() {
		return animator;
	}
	
	private void createBuffer() {
	    if (backBuffer != null) {
	        
	    }
		try {
		    backBuffer = GraphicsEnvironment.getLocalGraphicsEnvironment()
		            .getDefaultScreenDevice()
		            .getDefaultConfiguration()
		            .createCompatibleImage(drawableWidth, drawableHeight, Transparency.TRANSLUCENT);
		} catch (Exception e) {
		    TurtleLogging.error("Unable to create canvas buffer. Did you specify a canvas too large?"
		            + " [" + drawableWidth + " x " + drawableHeight + "]", e);
		}
	}
	
	public Graphics2D getCanvasGraphics() {
		Graphics2D g = backBuffer != null? backBuffer.createGraphics() : null;
		if (g != null) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		g.translate(drawableWidth / 2, drawableHeight / 2);
		return g;
	}

    @Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.scale(zoom, zoom);
		if (backBuffer != null) {
			g.drawImage(backBuffer, 0, 0, null);
		}

        g2.translate(drawableWidth / 2, drawableHeight / 2);
		for (Turtle t : turtles) {
			t.helperGetSprite().render(g2);
		}
	}
	
	private static Rectangle getRectangleContaining(Point2D p1, Point2D p2) {
		double xMin = Math.min(p1.getX(), p2.getX());
		double xMax = Math.max(p1.getX(), p2.getX());
		double yMin = Math.min(p1.getY(), p2.getY());
		double yMax = Math.max(p1.getY(), p2.getY());
		
		return new Rectangle((int) Math.floor(xMin) - 1, (int) Math.floor(yMin) - 1, 
				(int) (Math.ceil(xMax) - Math.floor(xMin)) + 2, 
				(int) (Math.ceil(yMax) - Math.floor(yMin)) + 2);
	}
	
	public void markDirty(Point2D p1, Point2D p2) {		
		markDirty(getRectangleContaining(p1, p2));
	}
	
	public void markDirty(Point2D p, int size) {	
		markDirty(new Rectangle((int) Math.floor(p.getX()) - size, (int) Math.floor(p.getY()) - size,
				2 * size, 2 * size));
	}
	
	private static void scaleRectangle(Rectangle r, float scale) {
		r.setSize((int) Math.ceil(r.getWidth() * scale), (int) Math.ceil(r.getHeight() * scale));
		r.setLocation((int) Math.round(r.getX() * scale), (int) Math.round(r.getY() * scale));
	}

	public void markDirty(Rectangle r) {
		r.grow(100, 100);
        r.translate(drawableWidth / 2, drawableHeight / 2);
		scaleRectangle(r, zoom);
		repaint(r);
	}
	
	public void drawString(String text, double x, double y) {
		drawString(text, x, y, Color.black);
	}
	
	public void drawString(String text, double x, double y, Color c) {
		drawString(text, x, y, c, TLUtil.LEFT);
	}
	
	public void drawString(String text, double x, double y, Color c, int alignment) {
		drawString(text, x, y, c, alignment, new Font(TLUtil.getDefaultFontFamily(), Font.PLAIN, 13));
	}
	
	public void drawString(String text, double x, double y, Color c, int alignment, Font font) {
		Graphics2D g2 = getCanvasGraphics();
		g2.setColor(c);
		g2.setFont(font);
		TLUtil.drawMultilineString(g2, text, x, y, alignment);
		repaint();
	}
	
	public void setZoom(double zoom) {
		float fZoom = (float) zoom;
		if (this.zoom == fZoom)
			return;
		
		if (zoom > 0) {
			this.zoom = fZoom;
			invalidate();
		} else {
			System.err.println("Invalid zoom: " + zoom);
		}
	}
	
	List<Turtle> helperGetTurtles() {
	    return turtles;
	}
}
