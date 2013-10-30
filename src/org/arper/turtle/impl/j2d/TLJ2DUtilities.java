package org.arper.turtle.impl.j2d;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TLJ2DUtilities {

    public static boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");

    public static final int LEFT = SwingConstants.LEFT,
            CENTER = SwingConstants.CENTER,
            RIGHT = SwingConstants.RIGHT;


    public static Rectangle2D getMultilineTextBounds(String text, Font f, FontRenderContext frc) {
        LineMetrics lm = f.getLineMetrics(text, frc);
        double maxWidth = 0;
        Iterable<String> lines = Splitter.on('\n').split(text);

        int lineCount = 0;
        for (String s : lines) {
            double width = f.getStringBounds(s, frc).getWidth();
            if (width > maxWidth)
                maxWidth = width;
            ++lineCount;
        }

        return new Rectangle2D.Double(0, 0, maxWidth, lineCount * lm.getHeight());
    }

    public static void drawMultilineString(Graphics2D g, String text, double x, double y, int alignment) {
        FontMetrics lm = g.getFontMetrics();
        List<String> lines = Splitter.on('\n').splitToList(text);

        for (int i = 0; i < lines.size(); i++) {
            float shiftX;
            switch (alignment) {
            case LEFT:
                shiftX = 0;
                break;
            case CENTER:
                shiftX = -(float)g.getFontMetrics().getStringBounds(lines.get(i), g).getWidth() / 2;
                break;
            case RIGHT:
                shiftX = -(float)g.getFontMetrics().getStringBounds(lines.get(i), g).getWidth();
                break;
            default:
                System.err.println("Util.drawMultilineString Error: invalid alignment " + alignment +
                        ". Please use Util.LEFT, Util.CENTER, or Util.RIGHT!");
                return;
            }
            g.drawString(lines.get(i), (float)x + shiftX, (float)y + i * lm.getHeight());
        }
    }

    public static class AlphaColorComposite implements Composite {

        private float intensity;
        private float alpha;

        private float[] overlayAddition;

        protected AlphaColorComposite(Color c, float intensity, float alpha) {
            this.intensity = intensity;
            this.alpha = alpha;

            overlayAddition = new float[]{ c.getRed() * intensity, c.getGreen() * intensity, c.getBlue() * intensity };
        }

        @Override
        public CompositeContext createContext(ColorModel srcColorModel,
                                              ColorModel dstColorModel, RenderingHints hints) {
            return new AlphaColorCompositeContext(srcColorModel, dstColorModel);
        }

        private class AlphaColorCompositeContext implements CompositeContext {

            @SuppressWarnings("unused")
            private ColorModel srcCM, dstCM;
            private int alphaIndex;

            public AlphaColorCompositeContext(ColorModel srcCM, ColorModel dstCM) {
                this.srcCM = srcCM;
                this.dstCM = dstCM;
                alphaIndex = 3;
            }

            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

                int w = Math.min(src.getWidth(), dstIn.getWidth());
                int h = Math.min(src.getHeight(), dstIn.getHeight());
                int[] sP = new int[4], dP = new int[4], oP = new int[4];

                for (int i = 0; i < w; i++) {
                    for (int j = 0; j < h; j++) {
                        src.getPixel(i, j, sP);
                        dstIn.getPixel(i, j, dP);


                        int srcSampleAlpha = sP[alphaIndex];
                        if (srcSampleAlpha != 0) {
                            oP[alphaIndex] = dP[alphaIndex];
                            float srcAlpha = alpha * srcSampleAlpha / 255.0f;

                            for (int k = 0; k < 3; ++k) {
                                int sC = sP[k];
                                int dC = dP[k];

                                float oC = (sC * (1 - intensity) + overlayAddition[k]) * srcAlpha + dC * (1 - srcAlpha);
                                int oCInt = (int) Math.round(oC);
                                oCInt = ((oCInt < 0)? 0 : (oCInt > 255 ? 255 : oCInt)); // clamp to [0, 255]
                                oP[k] = oCInt;
                            }
                        } else {
                            /* There seems to be a Permissions-related bug on the Mac where
                             * the input (dP) pixels are perturbed slightly. Copying 0's into
                             * the output leaves the destination unchanged, however, where copying
                             * 0's on other operating systems blackens the output.
                             */
                            if (isMacOS) {
                                Arrays.fill(oP, 0);
                            } else {
                                System.arraycopy(dP, 0, oP, 0, oP.length); // src transparent, just copy through
                            }
                        }
                        dstOut.setPixel(i, j, oP);
                    }
                }
            }

            @Override
            public void dispose() {
            }
        }
    }

    public static class MultiplyColorComposite implements Composite {

        private float[] color;

        public MultiplyColorComposite(Color color, float intensity) {
            this.color = new float[] {
                    (1 - intensity) + intensity * color.getRed() / 255.0f,
                    (1 - intensity) + intensity * color.getGreen() / 255.0f,
                    (1 - intensity) + intensity * color.getBlue() / 255.0f,
                    (1 - intensity) + intensity * color.getAlpha() / 255.0f};
        }

        @Override
        public CompositeContext createContext(ColorModel srcColorModel,
                                              ColorModel dstColorModel, RenderingHints hints) {
            return new MultiplyColorCompositeContext(srcColorModel, dstColorModel);
        }

        private class MultiplyColorCompositeContext implements CompositeContext {

            @SuppressWarnings("unused")
            private ColorModel srcCM, dstCM;
            private int alphaIndex;

            public MultiplyColorCompositeContext(ColorModel srcCM, ColorModel dstCM) {
                this.srcCM = srcCM;
                this.dstCM = dstCM;
                alphaIndex = 3;
            }

            @Override
            public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {

                int w = Math.min(src.getWidth(), dstIn.getWidth());
                int h = Math.min(src.getHeight(), dstIn.getHeight());
                int[] sP = new int[4], dP = new int[4], oP = new int[4];

                for (int i = 0; i < w; i++) {
                    for (int j = 0; j < h; j++) {
                        src.getPixel(i, j, sP);
                        dstIn.getPixel(i, j, dP);


                        int srcSampleAlpha = sP[alphaIndex];
                        if (srcSampleAlpha != 0) {
                            oP[alphaIndex] = dP[alphaIndex];
                            float srcAlpha = color[alphaIndex] * srcSampleAlpha / 255.0f;

                            for (int k = 0; k < 3; ++k) {
                                int sC = sP[k];
                                int dC = dP[k];

                                float oC = sC * color[k] * srcAlpha + dC * (1 - srcAlpha);
                                int oCInt = (int) Math.round(oC);
                                oCInt = ((oCInt < 0)? 0 : (oCInt > 255 ? 255 : oCInt)); // clamp to [0, 255]
                                oP[k] = oCInt;
                            }
                        } else {
                            /* There seems to be a Permissions-related bug on the Mac where
                             * the input (dP) pixels are perturbed slightly. Copying 0's into
                             * the output leaves the destination unchanged, however, where copying
                             * 0's on other operating systems blackens the output.
                             */
                            if (isMacOS) {
                                Arrays.fill(oP, 0);
                            } else {
                                System.arraycopy(dP, 0, oP, 0, oP.length); // src transparent, just copy through
                            }
                        }
                        dstOut.setPixel(i, j, oP);
                    }
                }
            }

            @Override
            public void dispose() {
            }
        }
    }


    private static final LoadingCache<Map.Entry<Integer, BufferedImage>, BufferedImage> imageCache
        = CacheBuilder.newBuilder().concurrencyLevel(4).build(
                new CacheLoader<Map.Entry<Integer, BufferedImage>, BufferedImage>() {
                    @Override
                    public BufferedImage load(Entry<Integer, BufferedImage> e)
                            throws Exception {
                        return Scalr.apply(Scalr.resize(e.getValue(), Method.ULTRA_QUALITY, e.getKey()), Scalr.OP_ANTIALIAS);
                    }
                });

    public static BufferedImage getScaledImage(int size, BufferedImage im) {
        Map.Entry<Integer, BufferedImage> key = Maps.immutableEntry(size, im);

        /* Only request loading of the given image. If it isn't ready, return null
         * and let caller deal with scaling manually, or try again.
         */
        BufferedImage image = imageCache.getIfPresent(key);
        if (image == null) {
            imageCache.refresh(key);
        }
        return image;
    }

    public static void drawImage(Graphics2D g, BufferedImage image, Composite composite) {
        if (TLJ2DUtilities.isMacOS || composite == null) {
            g.drawImage(image, 0, 0, null);
        } else {
            Composite oldComposite = g.getComposite();
            g.setComposite(composite);
            g.drawImage(image, 0, 0, null);
            g.setComposite(oldComposite);
        }
    }

    public static <T> List<T> sampleListUniformly(List<T> list, int count) {
        if (count >= list.size()) {
            return list;
        }
        List<T> results = Lists.newArrayListWithCapacity(count);

        for (int i = 0; i < count; ++i) {
            int sampleIndex = i * (list.size() - 1) / (count - 1);
            results.add(list.get(sampleIndex));
        }
        return results;
    }

    public static void drawPath(Graphics2D g, List<Point2D.Float> points) {
        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        Path2D.Float path = new Path2D.Float();
        boolean initialMove = false;

        for (Point2D.Float point : points) {
            if (!initialMove) {
                path.moveTo(point.x, point.y);
                initialMove = true;
            } else {
                path.lineTo(point.x, point.y);
            }
        }

        g.draw(path);
        g.setComposite(oldComposite);
    }

    private TLJ2DUtilities() {
        /* do not instantiate */
    }
}
