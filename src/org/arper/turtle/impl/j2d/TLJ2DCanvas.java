package org.arper.turtle.impl.j2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
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
import org.arper.turtle.impl.TLSingletonContext;
import org.arper.turtle.ui.TLCanvas;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@SuppressWarnings("serial")
public class TLJ2DCanvas extends JPanel implements TLCanvas {
    
    private static final Stroke BORDER_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_MITER, 5.0f, new float[]{4.0f, 7.0f, 2.0f, 7.0f}, 0.0f);
    
    public TLJ2DCanvas(int width, int height, int padding) {
        this.drawableWidth = width;
        this.drawableHeight = height;
        this.padding = padding;

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
    private final Rectangle clip = new Rectangle();
    private final int padding;

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
        return new Dimension(2 * padding + Math.round(drawableWidth * zoom), 
                2 * padding + Math.round(drawableHeight * zoom));
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
        if (!isValid() || !isVisible()) {
            return;
        }

        repaint(clip);
    }

    private void render(Graphics g) {
        TLAwtUtilities.assertOnAwtThread();

        List<TLTurtle> turtles = TLSingletonContext.get().getTurtles();

        Graphics2D bufferGraphics = (Graphics2D) g;
        AffineTransform t = bufferGraphics.getTransform();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bufferGraphics.scale(zoom, zoom);
//        drawBorder(bufferGraphics);

        Graphics2D drawingGraphics = getDrawingGraphics();
        drawingGraphics.translate(padding + drawableWidth / 2, padding + drawableHeight / 2);
        synchronized (turtles) {
            for (TLTurtle turtle : turtles) {
                getRenderer(turtle).preRender(drawingGraphics);
            }
            drawingGraphics.dispose();
            bufferGraphics.drawImage(drawing, 0, 0, null);
//            
//
            bufferGraphics.translate(padding + drawableWidth / 2, padding + drawableHeight / 2);
            for (TLTurtle turtle : turtles) {
                getRenderer(turtle).render(bufferGraphics);
            }
            
        }
        
        bufferGraphics.setTransform(t);
    }
    
    private void drawBorder(Graphics2D g) {
        Stroke s = g.getStroke();
        Composite composite = g.getComposite();
//        g.setStroke(BORDER_STROKE);
//        g.setColor(Color.GRAY);
//        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g.fillRect(padding, padding, drawableWidth, drawableHeight);
        g.setComposite(composite);
        
        g.setStroke(s);
    }

    private void updateClip(Rectangle newClip) {
        TLAwtUtilities.assertOnAwtThread();
        if (clip.isEmpty()) {
            clip.setBounds(newClip);
        } else if (!newClip.isEmpty()) {
            clip.add(newClip);
        }
    }

    @Override
    public void paint(Graphics g) {
        render(g);
        clip.setBounds(0, 0, 0, 0);
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

    public void markDirty(final Rectangle r) {
        r.grow(1, 1);
        r.translate(padding + drawableWidth / 2, padding + drawableHeight / 2);
        scaleRectangle(r, zoom);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateClip(r);
            }
        });
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

}
