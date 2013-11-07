package org.arper.turtle.impl.j2d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.arper.turtle.TLTurtle;
import org.arper.turtle.TLUtils;
import org.arper.turtle.impl.TLLogging;
import org.arper.turtle.impl.TLRenderer;
import org.arper.turtle.impl.TLSingleton;
import org.arper.turtle.impl.swing.TLSwingUtilities;
import org.arper.turtle.ui.TLCanvas;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@SuppressWarnings("serial")
public class TLJ2DCanvas extends JPanel implements TLCanvas {

    public TLJ2DCanvas(int width, int height) {
        this.drawableWidth = width;
        this.drawableHeight = height;

        /* TODO: module state */
        this.renderers = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakKeys()
                .build(new CacheLoader<TLTurtle, TLRenderer>() {
                    @Override
                    public TLRenderer load(TLTurtle key) throws Exception {
                        return new TLJ2DTurtleRenderer(key);
                    }
                });

        clear();
        setDoubleBuffered(true);
        setBackground(null);
        setOpaque(false);

        new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                renderFrame();
            }
        }).start();
    }

    private BufferedImage drawing;
    private final int drawableWidth, drawableHeight;
    private float zoom;
    private final LoadingCache<TLTurtle, TLRenderer> renderers;
    private final Rectangle worldRepaintClip = new Rectangle();

    @Override
    public TLRenderer getRenderer(TLTurtle turtle) {
        try {
            return renderers.get(turtle);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void clear() {
        zoom = 1.0f;
        createDrawing();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Math.round(drawableWidth * zoom), Math.round(drawableHeight * zoom));
    }

    private void createDrawing() {
        try {
            drawing = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .createCompatibleImage(drawableWidth, drawableHeight, Transparency.TRANSLUCENT);
        } catch (Exception e) {
            TLLogging.error("Unable to create canvas buffer. Did you specify a canvas too large?"
                    + " [" + drawableWidth + " x " + drawableHeight + "]", e);
        }
    }

    private Graphics2D getDrawingGraphics() {
        Graphics2D g = drawing != null? drawing.createGraphics() : null;
        if (g != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        return g;
    }

    private void renderFrame() {
        if (!isValid() || !isVisible() || worldRepaintClip.isEmpty()) {
            return;
        }

        repaint(getWorldDirtyClip());
    }

    private void render(Graphics g) {
        TLSwingUtilities.assertOnAwtThread();

        List<TLTurtle> turtles = TLSingleton.getContext().getTurtles();

        Graphics2D bufferGraphics = (Graphics2D) g;
        AffineTransform t = bufferGraphics.getTransform();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        drawBorder(bufferGraphics);

        Graphics2D drawingGraphics = getDrawingGraphics();
        Rectangle canvasClip = new Rectangle(-drawableWidth / 2, -drawableHeight / 2, 
                drawableWidth, drawableHeight);
        
        drawingGraphics.translate(drawableWidth / 2, drawableHeight / 2);
        synchronized (turtles) {
            for (TLTurtle turtle : turtles) {
                getRenderer(turtle).preRender(drawingGraphics, canvasClip);
            }
            drawingGraphics.dispose();
            bufferGraphics.drawImage(drawing, getPadX(), getPadY(), 
                    getDrawingWidth(), getDrawingHeight(), null);


            bufferGraphics.translate(getPadX() + drawableWidth / 2, getPadY() + drawableHeight / 2);
            bufferGraphics.scale(zoom, zoom);
            
            for (TLTurtle turtle : turtles) {
                getRenderer(turtle).render(bufferGraphics, canvasClip);
            }
        }

        bufferGraphics.setTransform(t);
    }

    private void drawBorder(Graphics2D g) {
        Area s = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        s.subtract(new Area(getDrawingRectangle()));

        g.setColor(new Color(40, 40, 40));
        g.fill(s);
    }

    private void requestWorldRepaint(Rectangle r) {
        TLSwingUtilities.assertOnAwtThread();
        if (worldRepaintClip.isEmpty()) {
            worldRepaintClip.setBounds(r);
        } else if (!r.isEmpty()) {
            worldRepaintClip.add(r);
        }
    }

    @Override
    public void paint(Graphics g) {
        render(g);
        worldRepaintClip.setBounds(0, 0, 0, 0);
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
        markDirty(new Rectangle(
                (int) Math.floor(p.getX()) - size, 
                (int) Math.floor(p.getY()) - size,
                2 * size, 
                2 * size));
    }

    private static void scaleRectangle(Rectangle r, float scale) {
        r.setSize((int) Math.ceil(r.getWidth() * scale), (int) Math.ceil(r.getHeight() * scale));
        r.setLocation((int) Math.round(r.getX() * scale), (int) Math.round(r.getY() * scale));
    }

    public void markDirty(final Rectangle r) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                requestWorldRepaint(r);
            }
        });
    }
    
    private Rectangle getWorldDirtyClip() {
        Rectangle transformed = new Rectangle(worldRepaintClip);
        transformed.translate(drawableWidth / 2, drawableHeight / 2);
        scaleRectangle(transformed, zoom);
        transformed.grow(1, 1);
        transformed.translate(getPadX(), getPadY());
        return transformed;
    }

    @Override
    public void drawString(String text, double x, double y) {
        drawString(text, x, y, Color.black, TLJ2DUtilities.LEFT, TLUtils.getDefaultFont());
    }

    @Override
    public void drawString(String text, double x, double y, Color c, int alignment, Font font) {
        Graphics2D g2 = getDrawingGraphics();
        g2.setColor(c);
        g2.setFont(font);
        TLJ2DUtilities.drawMultilineString(g2, text, x, y, alignment);
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
    
    private Rectangle getDrawingRectangle() {
        return new Rectangle(getPadX(), getPadY(), getDrawingWidth(), getDrawingHeight());
    }
    
    private int getDrawingWidth() {
        return (int) Math.ceil(drawableWidth * zoom);
    }
    
    private int getDrawingHeight() {
        return (int) Math.ceil(drawableHeight * zoom);
    }
    
    private int getPadX() {
        return Math.max(0, (getWidth() - getDrawingWidth()) / 2);
    }

    private int getPadY() {
        return Math.max(0, (getHeight() - getDrawingHeight()) / 2);
    }
    
}
