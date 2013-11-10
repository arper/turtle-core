package org.arper.turtle.impl.swing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.alee.extended.image.WebImage;
import com.alee.laf.rootpane.WebResizeCorner;
import com.sun.awt.AWTUtilities;

public class TLSwingResizeHandles {
    
    public static JComponent createResizeHandle() {
        JComponent resizeHandle = new WebImage(WebResizeCorner.class.getResource("icons/corner.png"));
        resizeHandle.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
        return resizeHandle;
    }
    
    public static void installResizeHandle(JComponent handle, Point resizeWeights, 
            JComponent toResize) {
        HandleDragListener listener = new HandleDragListener(toResize, resizeWeights);
        handle.addMouseListener(listener);
        handle.addMouseMotionListener(listener);
    }
    
    private static int clamp(int x, int min, int max) {
        return x < min? min : (x > max? max : x);
    }
    
    private static class HandleDragListener implements MouseMotionListener, MouseListener {

        public HandleDragListener(JComponent toResize, Point resizeWeights) {
            this.toResize = toResize;
            this.resizeWeights = resizeWeights;
            
            lastMouseLocation = new Point();
            dragging = false;
        }

        private final JComponent toResize;
        private final Point resizeWeights;
        
        private final Point lastMouseLocation;
        private boolean dragging;
        
        @Override
        public void mouseDragged(MouseEvent e) {
            if (!dragging) {
                return;
            }

            int dx = resizeWeights.x * (e.getX() - lastMouseLocation.x);
            int dy = resizeWeights.y * (e.getY() - lastMouseLocation.y);

            toResize.setSize(
                    clamp(toResize.getWidth() + dx, 
                            toResize.getMinimumSize().width, toResize.getMaximumSize().width), 
                    clamp(toResize.getHeight() + dy, 
                            toResize.getMinimumSize().height, toResize.getMaximumSize().height));
            
//            lastMouseLocation.setLocation(e.getPoint());
            toResize.validate();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            /* do nothing */
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            /* do nothing */
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            /* do nothing */
        }

        @Override
        public void mouseExited(MouseEvent e) {
            /* do nothing */
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                lastMouseLocation.setLocation(e.getX(), e.getY());
                dragging = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                dragging = false;
            }
        }
    }
}
