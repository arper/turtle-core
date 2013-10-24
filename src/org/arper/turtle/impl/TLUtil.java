package org.arper.turtle.impl;

import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

public final class TLUtil {

	public static final int LEFT = SwingConstants.LEFT, 
	CENTER = SwingConstants.CENTER,
	RIGHT = SwingConstants.RIGHT;

	private static String defaultFontFamily = "Comic Sans MS";
	public static void setDefaultFontFamily(String fontFamily) {
		if (fontFamily != null) {
			defaultFontFamily = fontFamily;
		} else {
			System.err.println("Util.setDefaultFontFamily error: fontFamily is null!");
		}
	}

	public static String getDefaultFontFamily() {
		return defaultFontFamily;
	}

	public static Color randomColor() {
		return new Color((float)Math.random(), (float)Math.random(), (float)Math.random(), 1);
	}
	
	public static Color randomBrightColor() {
		return Color.getHSBColor((float) Math.random(), 1.0f, 1.0f);
	}
	
	public static Color randomDarkColor() {
		return Color.getHSBColor((float) Math.random(), 0.9f, .2f);
	}
	
	private static final Pattern COLOR_MATCHER = Pattern.compile("(\\d+),(\\d+),(\\d+)");
	private static Map<String, Color> colorsByName;
	static {
	    colorsByName = new HashMap<String, Color>();
	    
	    colorsByName.put("red", Color.RED);
	    colorsByName.put("green", Color.GREEN);
	    colorsByName.put("blue", Color.BLUE);
	    colorsByName.put("yellow", Color.YELLOW);
	    colorsByName.put("cyan", Color.CYAN);
	    colorsByName.put("aqua", new Color(0, 128, 255));
        colorsByName.put("turquoise", new Color(0, 255, 213));
        colorsByName.put("seagreen", new Color(0, 255, 128));
        colorsByName.put("skyblue", new Color(135, 206, 235));
        colorsByName.put("steelblue", new Color(70, 130, 180));
	    colorsByName.put("teal", new Color(0, 179, 179));
        colorsByName.put("forest", new Color(17,79,20));
        colorsByName.put("forestgreen", new Color(17,79,20));
	    colorsByName.put("magenta", Color.MAGENTA);
        colorsByName.put("indigo", new Color(147, 0, 255));
        colorsByName.put("navy", new Color(28, 0, 168));
        colorsByName.put("purple", new Color(128, 0, 255));
        colorsByName.put("beige", new Color(230, 210, 133));
        colorsByName.put("cinnamon", new Color(126, 42, 0));
        colorsByName.put("redorange", new Color(255, 85, 0));
        colorsByName.put("salmon", new Color(255, 155, 133));
        colorsByName.put("brown", new Color(95, 58, 15));
        colorsByName.put("lavender", new Color(213, 166, 255));
        colorsByName.put("periwinkle", new Color(204, 204, 255));
        colorsByName.put("olive", new Color(128, 128, 0));
        colorsByName.put("yellowgreen", new Color(192, 255, 62));
	    colorsByName.put("pink", Color.PINK);
	    colorsByName.put("white", Color.WHITE);
	    colorsByName.put("black", Color.BLACK);
	    colorsByName.put("gray", Color.GRAY);
        colorsByName.put("grey", Color.GRAY);
        colorsByName.put("orange", Color.ORANGE);
        colorsByName.put("gold", new Color(242, 211, 70));
	}
	
	private static float clamp01(float f) {
	    return f < 0? 0 : (f > 1? 1 : f);
	}
	
	public static Color transformColor(Color c, float saturationChange, float brightnessChange) {
	    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
	    hsb[1] += saturationChange;
	    hsb[2] += brightnessChange;
	    return Color.getHSBColor(hsb[0], clamp01(hsb[1]), clamp01(hsb[2]));
	}
	
    public static Color getColorByName(String s) {
        s = s.toLowerCase().replaceAll("\\s+","");
        try {
            return Color.decode(s.toUpperCase());
        } catch (Exception e) {}
        
        Matcher m = COLOR_MATCHER.matcher(s);
        if (m.matches()) {
            try {
                return new Color((int) Double.parseDouble(m.group(1)),
                        (int) Double.parseDouble(m.group(2)),
                        (int) Double.parseDouble(m.group(3)));
            } catch (Exception e) {
                return null;
            }
        }
        
        int sat = 0, bright= 0;
        while (true) {
            if (s.startsWith("bright")) {
                bright++;
                s = s.substring("bright".length());
            } else if (s.startsWith("light")) {
                bright++;
                s = s.substring("light".length());
            } else if (s.startsWith("dark")) {
                bright--;
                s = s.substring("dark".length());
            } else if (s.startsWith("bold")){
                sat++;
                s = s.substring("bold".length());
            } else if (s.startsWith("pale")) {
                sat--;
                s = s.substring("pale".length());
            } else {
                break;
            }
        }
        
        Color baseColor = colorsByName.get(s);
        if (baseColor != null) {
            return transformColor(baseColor, sat * .5f, bright * .5f);
        }
        
        return null;
    }

	static List<String> splitStringIntoLines(String text) {
		if (text == null) {
			return new ArrayList<String>();
		}

		List<String> retval = new ArrayList<String>();
		int lineStart = 0;
		for (int pos = 0; pos < text.length(); pos++) {
			if (text.charAt(pos) == '\n') {
				retval.add(text.substring(lineStart, pos));
				lineStart = pos + 1;
			}
		}
		retval.add(text.substring(lineStart));
		return retval;
	}

	public static Rectangle2D getMultilineTextBounds(String text, Font f, FontRenderContext frc) {
		LineMetrics lm = f.getLineMetrics(text, frc);
		double maxWidth = 0;
		List<String> lines = splitStringIntoLines(text);

		for (String s : lines) {
			double width = f.getStringBounds(s, frc).getWidth();
			if (width > maxWidth)
				maxWidth = width;
		}

		return new Rectangle2D.Double(0, 0, maxWidth, lines.size() * lm.getHeight());
	}

	public static void drawMultilineString(Graphics2D g, String text, double x, double y, int alignment) {
		FontMetrics lm = g.getFontMetrics();
		List<String> lines = splitStringIntoLines(text);
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

	public static String[] getSystemFontFamilyNames() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	}
	
	public static boolean isMacComputer = System.getProperty("os.name").toLowerCase().contains("mac");

	static class AlphaColorComposite implements Composite {

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
							if (isMacComputer) {
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
                            if (isMacComputer) {
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
}
