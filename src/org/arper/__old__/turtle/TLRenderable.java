package org.arper.__old__.turtle;

import java.awt.Graphics2D;

public interface TLRenderable {
    enum Layer {
        Dynamic,
        Static
    }

    void render(Graphics2D g, Layer layer);
    void markDirty();
}
