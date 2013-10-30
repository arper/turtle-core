package org.arper.turtle.impl.j2d;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.alee.extended.painter.NinePatchStatePainter;
import com.alee.extended.painter.Painter;
import com.alee.extended.painter.PainterSupport;
import com.alee.extended.painter.TexturePainter;
import com.alee.utils.ninepatch.NinePatchIcon;
import com.google.common.collect.ImmutableMap;

public class TLJ2DStyles {

    public static BufferedImage loadStyleImage(String path) throws IOException {
        URL url = ClassLoader.getSystemResource(path);
        if (url == null) {
            throw new IOException("Cannot find resource " + path);
        }
        
        return ImageIO.read(url);
    }

    public static Painter<?> getBackgroundPainter() {
        try {
            return new TexturePainter<JComponent>(loadStyleImage("styles/bg.png"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Painter<?> getButtonPainter() {
        try {
            return new NinePatchStatePainter<JComponent>(
                    new ImmutableMap.Builder<String, NinePatchIcon>()
                    .put("selected", new NinePatchIcon(loadStyleImage("styles/s.9.png")))
                    .put("normal", new NinePatchIcon(loadStyleImage("styles/n.9.png")))
                    .build());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
    
    private static Painter<?> loadPainter(JComponent comp) throws IOException {
        if (comp instanceof JButton) {
            return getButtonPainter();
        } else {
            return getBackgroundPainter();
        }
    }

    public static <T extends JComponent> T styled(T comp) {
        applyStyle(comp);
        return comp;
    }
    
    public static void setUndecorated(JComponent comp) {
        try {
            Method setUndecorated = comp.getClass().getMethod("setUndecorated", boolean.class);
            setUndecorated.invoke(comp, true);
        } catch (Exception e) {
        }
        try {
            Method setDrawBackground = comp.getClass().getMethod("setDrawBackground", boolean.class);
            setDrawBackground.invoke(comp, false);
        } catch (Exception e) {
        }
    }
    
    public static void applyStyleToTree(JComponent comp) {
        applyStyle(comp);
        for (Component child : comp.getComponents()) {
            if (child instanceof JComponent) {
                applyStyleToTree((JComponent) child);
            }
        }
    }
    
    public static void applyStyle(JComponent comp) {
        Painter<?> painter;
        try {
            painter = loadPainter(comp);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        setPainter(comp, painter);
    }
    
    public static void setPainter(Component comp, Painter<?> painter) {
        if (painter == null || !(comp instanceof JComponent)) {
            return;
        }

        try {
            Method setPainter = comp.getClass().getMethod("setPainter", Painter.class);
            setPainter.invoke(comp, painter);
        } catch (Exception e) {
            e.printStackTrace();
            PainterSupport.installPainter( (JComponent) comp, painter);
        }
    }
}
