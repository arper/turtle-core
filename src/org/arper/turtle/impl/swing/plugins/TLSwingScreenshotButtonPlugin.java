package org.arper.turtle.impl.swing.plugins;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingUtilities;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.managers.hotkey.Hotkey;
import com.google.common.base.Throwables;

public class TLSwingScreenshotButtonPlugin implements TLSwingPlugin {

    private TLSwingWindow window;

    @Override
    public void initSwingPlugin(TLSwingWindow window) {
        this.window = window;

        WebButton button = TLSwingToolbarPlugin.createToolbarButton(window,
                "icons/screenshot.png", "Take Screenshot of Window", Hotkey.F3);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                TLSwingUtilities.runOffAwtThread(new Runnable() {
                    @Override
                    public void run() {
                        screenshotAction();
                    }
                });
            }
        });

        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(button, BorderLayout.EAST);
    }

    private BufferedImage takeRobotScreenshot() throws AWTException {
        Callable<BufferedImage> robotImageCallable = new Callable<BufferedImage>() {
            @Override
            public BufferedImage call() throws Exception {
                return new Robot(window.getGraphicsConfiguration().getDevice())
                    .createScreenCapture(window.getBounds());
            }
        };

        try {
            return TLSwingUtilities.callOffAwtThread(robotImageCallable).get();
        } catch (InterruptedException e) {
            /* TODO: exception handling */
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            Throwables.propagateIfPossible(e, AWTException.class);
            return null;
        }
    }

    private BufferedImage takeSwingRenderedScreenshot() {
        Rectangle rec = window.getBounds();
        BufferedImage bufferedImage = new BufferedImage(rec.width, rec.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bufferedImage.createGraphics();
        window.paint(g);
        g.dispose();
        return bufferedImage;
    }

    private void screenshotAction() {
        TLSwingUtilities.assertOffAwtThread();

        File file = new File("screenshot-"
                + DateFormat.getDateTimeInstance().format(new Date())
                + ".png");

        BufferedImage image;
        try {
            image = takeRobotScreenshot();
        } catch (Exception e) {
            /* TODO: exception handling */
            e.printStackTrace();
            image = takeSwingRenderedScreenshot();
        }

        if (image != null) {
            try {
                ImageIO.write(image, "png", file);
            } catch (Exception e) {
                /* TODO: exception handling */
                e.printStackTrace();
            }
        }
    }

//        final String imageType = "png";
//
//        WebFileChooser chooser = new WebFileChooser();
//        chooser.setAcceptAllFileFilterUsed(false);
//        chooser.setFileFilter(new FileFilter(){
//            @Override
//            public boolean accept(File f) {
//                String ext = FileUtils.getFileExtPart(f.getPath(), true);
//                return Strings.isNullOrEmpty(ext) || ext.equals(".png");
//            }
//
//            @Override
//            public String getDescription() {
//                return "PNG image files (*.png)";
//            }
//        });
//
//        int retval = chooser.showSaveDialog(window);
//
//        if (retval != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
//            return;
//        }
//
//        String path = chooser.getSelectedFile().getPath();
//        if (!path.endsWith(".png"))
//            path += ".png";
//
//        try {
//            ImageIO.write(image, imageType, new File(path));
//        } catch (IOException ioe) {
//            /* TODO: Exception logging */
//            ioe.printStackTrace();
//        }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }
}
