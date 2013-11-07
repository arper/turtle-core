package org.arper.turtle.impl.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.alee.extended.painter.NinePatchIconPainter;
import com.alee.extended.painter.NinePatchStatePainter;
import com.alee.extended.painter.Painter;
import com.alee.extended.painter.PainterSupport;
import com.alee.extended.painter.TexturePainter;
import com.alee.laf.panel.WebPanel;
import com.alee.utils.ninepatch.NinePatchIcon;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;

public class TLSwingStyles {

    private static final LoadingCache<String, BufferedImage> IMAGES_CACHE = CacheBuilder.newBuilder()
            .concurrencyLevel(2)
            .weakValues()
            .build(new CacheLoader<String, BufferedImage>() {
                @Override
                public BufferedImage load(String key) throws Exception {
                    URL url = ClassLoader.getSystemResource(key);
                    if (url == null) {
                        throw new IOException("Cannot find resource " + key);
                    }

                    return ImageIO.read(url);
                }
            });
    
    public static Painter<?> getBackgroundPainter() {
        try {
            return new TexturePainter<JComponent>(IMAGES_CACHE.get("styles/bg.png"));
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Painter<?> getPanelPainter() {
        try {
            return new NinePatchIconPainter<>(IMAGES_CACHE.get("styles/panel.9.png"));
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            return null;
        }
    }

    public static Painter<?> getButtonPainter() {
        try {
            return new NinePatchStatePainter<JComponent>(
                    new ImmutableMap.Builder<String, NinePatchIcon>()
                    .put("selected", new NinePatchIcon(IMAGES_CACHE.get("styles/p.9.png")))
                    .put("rollover", new NinePatchIcon(IMAGES_CACHE.get("styles/r.9.png")))
                    .put("pressed", new NinePatchIcon(IMAGES_CACHE.get("styles/p.9.png")))
                    .put("normal", new NinePatchIcon(IMAGES_CACHE.get("styles/n.9.png")))
                    .build());
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            return null;
        }
    }
    
    private static Painter<?> loadPainter(JComponent comp) throws IOException {
        if (comp instanceof JButton) {
            return getButtonPainter();
        } else {
            return getButtonPainter();
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

    public static JComponent pad(JComponent comp, int padding) {
        return pad(comp, padding, padding, padding, padding);
    }

    public static JComponent pad(JComponent comp, int top, int right, int bottom, int left) {
        JComponent parent = new WebPanel(new BorderLayout());
        parent.setOpaque(comp.isOpaque());
        parent.add(comp, BorderLayout.CENTER);
        comp.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        return parent;
    }
    
    public static JComponent noLayout(JComponent comp) {
        JComponent parent = transparent(new WebPanel());
        parent.setLayout(null);
        parent.add(comp);
        return parent;
    }

    public static JComponent anchor(JComponent comp, String... direction) {
        JComponent parent = comp;
        for (String dir : direction) {
            JPanel nextParent = new WebPanel(new BorderLayout());
            nextParent.setOpaque(false);
            nextParent.add(parent, dir);
            parent = nextParent;
        }
        return parent;
    }

    public static <T extends JComponent> T transparent(T comp) {
        comp.setBackground(new Color(0, 0, 0, 0));
        comp.setOpaque(false);

        if (comp instanceof JScrollPane) {
            ((JScrollPane)comp).setViewportBorder(BorderFactory.createEmptyBorder());
            ((JScrollPane)comp).getViewport().setOpaque(false);
        }
        return comp;
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
