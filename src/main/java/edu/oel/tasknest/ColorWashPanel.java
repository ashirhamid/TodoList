package edu.oel.tasknest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.JPanel;

/** A softly textured cream-to-blue canvas shared by the app and its dialogs.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
final class ColorWashPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color CREAM = new Color(255, 241, 219);
    private static final Color BLUE = new Color(136, 167, 222);

    ColorWashPanel(java.awt.LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int height = Math.max(1, getHeight());
        float[] stops = {0f, 0.29f, 0.41f, 0.55f, 1f};
        Color[] shades = {
                CREAM,
                CREAM,
                new Color(249, 239, 222),
                new Color(147, 174, 222),
                BLUE
        };
        g.setPaint(new LinearGradientPaint(new Point2D.Float(0, 0), new Point2D.Float(0, height), stops, shades));
        g.fillRect(0, 0, getWidth(), height);
        paintSoftGrain(g, height);
        g.dispose();
    }

    private void paintSoftGrain(Graphics2D g, int height) {
        int start = Math.max(0, (int) (height * 0.27));
        int end = Math.min(height, (int) (height * 0.59));
        for (int y = start; y < end; y += 5) {
            float position = (float) (y - start) / Math.max(1, end - start);
            for (int x = 0; x < getWidth(); x += 5) {
                int noise = noiseAt(x, y);
                int blueThreshold = 12 + (int) (position * 78);
                if (noise < blueThreshold) {
                    int alpha = 12 + (int) (position * 28);
                    int diameter = noise % 9 == 0 ? 2 : 1;
                    g.setColor(new Color(88, 139, 210, alpha));
                    g.fillOval(x, y, diameter, diameter);
                } else if (noise > 246 && position > 0.2f) {
                    g.setColor(new Color(255, 247, 231, 44));
                    g.fillOval(x, y, 1, 1);
                }
            }
        }
    }

    private int noiseAt(int x, int y) {
        long value = (long) x * 1_103_515_245L + (long) y * 12_345L + 0x9E3779B97F4A7C15L;
        value ^= value >>> 16;
        value *= 0x45d9f3bL;
        value ^= value >>> 16;
        return (int) value & 0xFF;
    }
}
