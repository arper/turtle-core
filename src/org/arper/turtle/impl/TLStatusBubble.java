package org.arper.turtle.impl;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.arper.turtle.TLUtils;

public class TLStatusBubble {

	private static final FontRenderContext fontContext = new FontRenderContext(null, true, false);
	private Shape shape;
	private Point2D center;
	private String text;
	private Font font;
	private Rectangle2D textBounds;
	private LineMetrics lineMetrics;
	private float textX, textY;

	public TLStatusBubble(double x, double y, double stemLength, double bubbleWidth, double bubbleHeight) {
		shape = createBubble(x, y, stemLength, bubbleWidth, bubbleHeight);
	}

	public TLStatusBubble(double x, double y, double stemLength, String text, double pad) {
		this(x, y, stemLength, text, pad, new Font(TLUtils.getDefaultFontFamily(), Font.PLAIN, 11));
	}

	public TLStatusBubble(double x, double y, double stemLength, String text, double pad, Font f) {
		textBounds = TLDisplayUtilities.getMultilineTextBounds(text, f, fontContext);
		lineMetrics = f.getLineMetrics(text, fontContext);
		this.font = f;
		this.text = text;

		double rectWidth = textBounds.getWidth() + 2 * pad;
		double rectHeight = textBounds.getHeight() + 2 * pad;

		shape = createBubble(x, y, stemLength, rectWidth, rectHeight);
		textX = (float) (center.getX());
		textY = (float) (center.getY() - rectHeight / 2 + pad + lineMetrics.getAscent());
	}

	private Shape createBubble(double x, double y, double stemLength, double width, double height) {
		Shape r = createRectangle(x, y, stemLength, width, height);
		Area rect = new Area(r);
		Area stem = new Area(createStem(x, y, stemLength));

		center = new Point2D.Double(r.getBounds2D().getCenterX(), r.getBounds2D().getCenterY());

		rect.add(stem);
		return rect;
	}

	private static final double bottomRatio = 0.4;

	private Shape createRectangle(double x, double y, double stemLength, double width, double height) {
		double stemX = stemLength / Math.sqrt(2);
		double stemY = stemX;
		double arc = Math.min(width, height) * .3;
		return new RoundRectangle2D.Double(x + stemX - bottomRatio * width,
				y - stemY - height, width, height, arc, arc);
	}

	private Shape createStem(double xD, double yD, double stemLengthD) {
		int x = (int) Math.floor(xD);
		int y = (int) Math.floor(yD);
		int spread = (int) Math.round(0.3 * stemLengthD);

		int shiftX = (int) Math.ceil(stemLengthD / Math.sqrt(2)) + 1;
		int shiftY = shiftX;

		Polygon poly = new Polygon();
		poly.addPoint(x, y);
		poly.addPoint(x + shiftX - spread, y - shiftY);
		poly.addPoint(x + shiftX + spread, y - shiftY);
		return poly;
	}

	public Shape getShape() {
		return shape;
	}

	public void drawText(Graphics2D g) {
		if (text != null) {
			Font f = g.getFont();
			g.setFont(font);
			TLDisplayUtilities.drawMultilineString(g, text, textX, textY, TLDisplayUtilities.CENTER);
			g.setFont(f);
		}
	}

	public Point2D getCenter() {
		return center;
	}
}
