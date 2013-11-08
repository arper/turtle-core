package org.arper.turtle.impl.swing;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;

import org.arper.turtle.impl.j2d.TLJ2DCanvas;
import org.arper.turtle.impl.j2d.TLJ2DUtilities;
import org.arper.turtle.impl.swing.plugins.TLSwingConsolePlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingHelpButtonPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingPauseOverlayPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingPlayPauseButtonPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingPropertiesPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingRestartButtonPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingScreenshotButtonPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingSettingsButtonPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingToolbarPlugin;
import org.arper.turtle.impl.swing.plugins.TLSwingTurtlePropertiesPlugin;
import org.arper.turtle.ui.TLCanvas;
import org.arper.turtle.ui.TLWindow;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebFrame;
import com.alee.laf.scroll.WebScrollPane;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;


@SuppressWarnings("serial")
public class TLSwingWindow extends WebFrame implements TLWindow {


    public static final List<Class<?>> DEFAULT_PLUGINS = ImmutableList.<Class<?>>of(
            TLSwingToolbarPlugin.Top.class,
            TLSwingPlayPauseButtonPlugin.class,
            TLSwingConsolePlugin.class,
            TLSwingRestartButtonPlugin.class,
            TLSwingScreenshotButtonPlugin.class,
            TLSwingSettingsButtonPlugin.class,
            TLSwingPropertiesPlugin.class,
            TLSwingHelpButtonPlugin.class,
//            TLTopToolbarButtonsPlugin.class,
            TLSwingPauseOverlayPlugin.class,
//            TLSwingToolbarPlugin.class,
            TLSwingTurtlePropertiesPlugin.class);

    private TLJ2DCanvas canvas;
    private JComponent pluginLayers;

    private final List<TLSwingPlugin> plugins;
    private boolean firstDisplay = true;

    static {
        try {
            WebLookAndFeel.install();
            WebLookAndFeel.setDecorateAllWindows(!TLJ2DUtilities.isMacOS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TLSwingWindow(int canvasWidth, int canvasHeight, Iterable<Class<?>> pluginClasses) {
        canvas = new TLJ2DCanvas(canvasWidth, canvasHeight);
        plugins = TLSwingPluginLoader.loadPluginsForClasses(pluginClasses);
        
        layoutComponents();

        setWindowIcon("icons/turtle.png");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        pack();

        for (TLSwingPlugin plugin : plugins) {
            plugin.initSwingPlugin(this);
        }

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentHidden(ComponentEvent arg0) {
                fireSwingPluginEvent("window_hidden", arg0);
            }

            @Override
            public void componentMoved(ComponentEvent arg0) {
                fireSwingPluginEvent("window_moved", arg0);
            }

            @Override
            public void componentResized(ComponentEvent arg0) {
                fireSwingPluginEvent("window_resized", arg0);
            }

            @Override
            public void componentShown(ComponentEvent arg0) {
                if (firstDisplay) {
                    fireSwingPluginEvent("app_start");
                    firstDisplay = false;
                }
                fireSwingPluginEvent("window_shown", arg0);
            }
        });
    }

    public TLSwingWindow() {
        this(Toolkit.getDefaultToolkit().getScreenSize().width / 2,
                Toolkit.getDefaultToolkit().getScreenSize().height / 2,
                DEFAULT_PLUGINS);
    }

    public void addPluginLayer(JComponent layer) {
        pluginLayers.add(layer, 0);
    }
    
    public JComponent getPluginLayers() {
        return pluginLayers;
    }

    public void fireSwingPluginEvent(String eventName, Object... args) {
        for (TLSwingPlugin plugin : plugins) {
            plugin.onSwingPluginEvent(this, eventName, args);
        }
    }

    public <T extends TLSwingPlugin> T getSwingPlugin(Class<T> pluginClass) {
        return pluginClass.cast(Iterables.find(plugins, Predicates.instanceOf(pluginClass)));
    }

    private void layoutComponents() {
        /* Canvas */
        JScrollPane canvasPane = TLSwingStyles.transparent(new WebScrollPane(canvas, false, false));

        /* Layering */
        pluginLayers = TLSwingStyles.transparent(new WebPanel()); // TLJ2DStyles.styled(new WebPanel());
        pluginLayers.setLayout(new OverlayLayout(pluginLayers));
        pluginLayers.add(canvasPane);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pluginLayers, BorderLayout.CENTER);
    }

    private void setWindowIcon(String path) {
        try {
            setIconImage(ImageIO.read(ClassLoader.getSystemResource(path)));
        } catch (Exception e) {}
    }

    @Override
    public TLCanvas getCanvas() {
        return canvas;
    }

}
