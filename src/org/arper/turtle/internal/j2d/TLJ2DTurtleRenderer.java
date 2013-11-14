package org.arper.turtle.internal.j2d;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.arper.turtle.TLPathType;
import org.arper.turtle.TLTurtle;
import org.arper.turtle.internal.TLAnimation;
import org.arper.turtle.internal.TLDefaultTurtleAnimation;
import org.arper.turtle.internal.TLRenderer;
import org.arper.turtle.internal.TLSingleton;
import org.arper.turtle.internal.TLTurtleState;
import org.arper.turtle.internal.swing.TLSwingUtilities;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TLJ2DTurtleRenderer implements TLRenderer {
    private static final AlphaComposite STATUS_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
    private static final int MAX_SAMPLED_POINTS = 30;

    public TLJ2DTurtleRenderer(TLTurtle owner) {
        this.owner = owner;
        turtleAnimation = new TLDefaultTurtleAnimation();
        sampledPathPoints = Lists.newArrayList();
    }

    private final TLTurtle owner;
    
    private TLTurtleState renderedState;
    private TLTurtleState newState;
    private List<Point2D.Float> sampledPathPoints;

    private TLJ2DStatusBubble statusBubble;
    private TLAnimation turtleAnimation;

    private Composite turtleComposite;
    private Stroke stroke;

    private int getMaxDimension() {
        return Math.max(turtleAnimation.getBoundingWidth(), turtleAnimation.getBoundingHeight());
    }

    private void refreshRenderState(Graphics2D g) {
        Preconditions.checkNotNull(newState, "Attempt to render before state has been set.");

        boolean noState = (renderedState == null);

        /* turtleComposite */
        if (noState || !Objects.equal(newState.color, renderedState.color)) {
            turtleComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f); // = new TLJ2DUtilities.MultiplyColorComposite(newState.color, 0.4f);
        }

        /* statusBubble */
        if (noState || !Objects.equal(newState.status, renderedState.status)) {
            if (newState.status == null) {
                statusBubble = null;
            } else {
                float start = (float) turtleAnimation.getSize(owner) * .3f;
                float len = (float) 15;
                statusBubble = new TLJ2DStatusBubble(start, -start, len, newState.status, 8);
            }
        }

        /* stroke */
        boolean strokeChange = noState || newState.pathType != renderedState.pathType
                || newState.thickness != renderedState.thickness;

        /* segment committing; must do before stroke is refreshed */
        if (renderedState != null &&
                !sampledPathPoints.isEmpty() && (
                strokeChange ||
                newState.color != renderedState.color ||
                newState.isPenDown != renderedState.isPenDown)) {
            sampledPathPoints.add(renderedState.location);
            drawPathSegments(g, renderedState.color);
            sampledPathPoints.clear();
        }
        
        /* stroke refreshing */
        if (strokeChange) {
            int cap, join;
            if (newState.pathType == TLPathType.Sharp) {
                cap = BasicStroke.CAP_BUTT;
                join = BasicStroke.JOIN_BEVEL;
            } else {
                cap = BasicStroke.CAP_ROUND;
                join = BasicStroke.JOIN_ROUND;
            }
            stroke = new BasicStroke(newState.thickness, cap, join);
        }

        /* path point sampling */
        if (newState.isPenDown && shouldSample()) {
            sampledPathPoints.add(new Point2D.Float(newState.location.x,
                    newState.location.y));
            resampleIfNecessary();
        }
    }

    private boolean shouldSample() {
        return sampledPathPoints.isEmpty() ||
                sampledPathPoints.get(sampledPathPoints.size() - 1).distanceSq(newState.location) >= 0.5f;
    }

    private void resampleIfNecessary() {
        if (sampledPathPoints.size() >= MAX_SAMPLED_POINTS * 2 + 1) {
            sampledPathPoints = TLJ2DUtilities.sampleListUniformly(sampledPathPoints, MAX_SAMPLED_POINTS);
        }
    }

    @Override
    public void markDirty() {
        TLSwingUtilities.assertOffAwtThread();

        if (renderedState != null) {
            markDirtyAtLocation(renderedState.location);
        }

        markDirtyAtLocation(owner.getLocation());
    }

    private void markDirtyAtLocation(Point2D loc) {
        /* TODO: context modularization */
        TLJ2DCanvas canvas = (TLJ2DCanvas) TLSingleton.getContext().getWindow().getCanvas();

        if (statusBubble != null) {
            Rectangle r = statusBubble.getShape().getBounds();
            r.translate((int) Math.round(loc.getX()), (int) Math.round(loc.getY()));
            canvas.markDirty(r);
        }

        double maxSize = Math.max(turtleAnimation.getSize(owner) / Math.sqrt(2), turtleAnimation.getCursorSize(owner) / 2);
        canvas.markDirty(loc, (int)Math.ceil(maxSize));
    }

    @Override
    public void preRender(Graphics2D g, Shape canvasClipRect) {
        TLSwingUtilities.assertOnAwtThread();

        if (newState == null) {
            newState = new TLTurtleState();
        }

        /* TODO: context modularization */
        newState.set(TLSingleton.getContext().getSimulator().getTurtleState(owner));

        refreshRenderState(g);
    }

    @Override
    public void render(Graphics2D g, Shape canvasClipRect) {
        TLSwingUtilities.assertOnAwtThread();

        Shape oldClip = g.getClipBounds();
        g.clip(canvasClipRect);
        drawPathSegments(g, newState.color);
        drawPaintCursor(g);
        g.setClip(oldClip);
        
        drawStatusBubble(g);
        drawTurtle(g);

        if (renderedState == null) {
            renderedState = new TLTurtleState();
        }
        renderedState.set(newState);
    }

    int max = 0;

    private void drawPaintCursor(Graphics2D g) {
        if (!newState.isPenDown) {
            return;
        }

        double cSize = turtleAnimation.getCursorSize(owner);
        g.setColor(newState.color);
        g.fill(new Ellipse2D.Double(newState.location.x - cSize / 2,
                newState.location.y - cSize / 2,
                cSize,
                cSize));
    }
    
    private void drawPathSegments(Graphics2D g, Color color) {
        if (sampledPathPoints.isEmpty()) {
            return;
        }
        
        Stroke oldStroke = g.getStroke();
        g.setStroke(stroke);
        g.setColor(color);
        TLJ2DUtilities.drawPath(g, sampledPathPoints);
        g.setStroke(oldStroke);
    }
    
    private void drawStatusBubble(Graphics2D g) {
        if (statusBubble == null) {
            return;
        }

        AffineTransform at = g.getTransform();
        g.translate(newState.location.x, newState.location.y);
        g.setColor(Color.white);
        g.setComposite(STATUS_COMPOSITE);
        g.fill(statusBubble.getShape());
        g.setColor(owner.getColor().darker().darker());
        g.draw(statusBubble.getShape());
        statusBubble.drawText(g);
        g.setTransform(at);
    }

    private void drawTurtle(Graphics2D g) {
        AffineTransform at = g.getTransform();
        double size = turtleAnimation.getSize(owner);
        double scale = size / getMaxDimension();

        g.translate(newState.location.x, newState.location.y);
        //          double elapsedSeconds = owner.helperGetAnimationHelper().getElapsedSeconds();
        //          double elapsedDistance = owner.helperGetAnimationHelper().getTotalMillipixels() / 100000.0;
        double elapsedSeconds = 0;
        double elapsedDistance = 0;
        double elapsed = elapsedDistance + elapsedSeconds / 2;

        AffineTransform at2 = g.getTransform();
        BufferedImage[] images = turtleAnimation.getImages();
        for (int i = 0; i < images.length; i++) {
            if (images[i] == null) {
                continue;
            }

            double tX = turtleAnimation.getBoundingWidth() * turtleAnimation.getCenterX(i);
            double tY = turtleAnimation.getBoundingHeight() * turtleAnimation.getCenterY(i);
            double rotateAmount = newState.heading + turtleAnimation.getPieceRotation(i, elapsed);
            g.rotate(rotateAmount);
//
            BufferedImage pretty = TLJ2DUtilities.getScaledImage((int) Math.ceil(size), images[i]);
            if (pretty != null) {
                g.translate(-turtleAnimation.getCenterX(i) * pretty.getWidth(),
                        -turtleAnimation.getCenterY(i) * pretty.getHeight());
            } else {
                g.scale(scale, scale);
                g.translate(-tX, -tY);
            }
            TLJ2DUtilities.drawImage(g, pretty, turtleComposite);
//
            g.setTransform(at2);
        }

        g.setTransform(at);
    }

    //    private void drawPathSegment(Graphics2D g) {
    //        TLVector dir = new TLVector(newState.location).subtract(lastVertex);
    //
    //        if (dir.lengthSquared() > 0) {
    //            Stroke s = g.getStroke();
    //            g.setStroke(stroke);
    //
    //            if (owner.getPathType() == PathType.Rounded) {
    //                g.draw(new Line2D.Double(lastVertex, newState.location));
    //            } else {
    //                double len_sq = dir.lengthSquared();
    //                double extension = -Math.expm1(-len_sq / 12) / 2; // extension = (1 - e^{len_sq / 4}) / 2
    //                dir = dir.normalize();
    //                Point2D augmentedLoc = new TLVector(newState.location).add(dir.scale((float) extension)).asPoint2D();
    //                Point2D loweredLast = new TLVector(lastVertex).add(dir.scale(-(float)extension)).asPoint2D();
    //                g.draw(new Line2D.Double(loweredLast, augmentedLoc));
    //            }
    //            g.setStroke(s);
    //        }
    //    }
}