package org.arper.turtle.impl.swing.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.arper.turtle.impl.swing.TLSwingPlugin;
import org.arper.turtle.impl.swing.TLSwingUtilities;
import org.arper.turtle.impl.swing.TLSwingWindow;

import com.alee.laf.button.WebButton;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.managers.hotkey.Hotkey;
import com.alee.utils.FileUtils;
import com.google.common.base.Strings;

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
                screenshotAction();
            }
        });
        
        window.getSwingPlugin(TLSwingToolbarPlugin.Top.class).add(button, BorderLayout.EAST);
    }
    
    private void screenshotAction() {
        final String imageType = "png";
        BufferedImage image = TLSwingUtilities.createFrameScreenshot(window);

        WebFileChooser chooser = new WebFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File f) {
                String ext = FileUtils.getFileExtPart(f.getPath(), true);
                return Strings.isNullOrEmpty(ext) || ext.equals(".png");
            }

            @Override
            public String getDescription() {
                return "PNG image files (*.png)"; 
            }
        });

        int retval = chooser.showSaveDialog(window);

        if (retval != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        String path = chooser.getSelectedFile().getPath();
        if (!path.endsWith(".png"))
            path += ".png";

        try {
            ImageIO.write(image, imageType, new File(path));
        } catch (IOException ioe) {
            /* TODO: Exception logging */
            ioe.printStackTrace();
        }
    }

    @Override
    public void onSwingPluginEvent(TLSwingWindow window, String name, Object... args) {
        /* do nothing */
    }
}
