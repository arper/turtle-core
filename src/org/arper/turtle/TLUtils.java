package org.arper.turtle;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TLUtils {

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

    private static float clamp01(float f) {
        return f < 0? 0 : (f > 1? 1 : f);
    }

    public static Color transformColor(Color c, float saturationChange, float brightnessChange) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        hsb[1] += saturationChange;
        hsb[2] += brightnessChange;
        return Color.getHSBColor(hsb[0], clamp01(hsb[1]), clamp01(hsb[2]));
    }

    public static String[] getSystemFontFamilyNames() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    }

    private TLUtils() {
        /* do not instantiate */
    }

}
