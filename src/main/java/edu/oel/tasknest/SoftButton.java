package edu.oel.tasknest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.EmptyBorder;

/** A rounded, restrained button that sits naturally on the color-wash background.
 * @author Muhammad Mehroz Ammar (534699), Ashir Hamid
 */
final class SoftButton extends JButton {
    private static final long serialVersionUID = 1L;
    enum Tone { QUIET, ACTION, DESTRUCTIVE }

    private final Tone tone;
    private boolean selectedStyle;

    SoftButton(String text, Tone tone) {
        super(text);
        this.tone = tone;
        setUI(new BasicButtonUI());
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setBorder(new EmptyBorder(8, 13, 8, 13));
        setForeground(foregroundForTone());
        setFont(new java.awt.Font("Palatino Linotype", java.awt.Font.PLAIN, 14));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    void setSelectedStyle(boolean selectedStyle) {
        this.selectedStyle = selectedStyle;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean hover = getModel().isRollover();
        boolean pressed = getModel().isPressed();
        Color fill = fillColor(hover, pressed);
        if (fill.getAlpha() > 0) {
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        }
        Color outline = outlineColor();
        if (outline.getAlpha() > 0) {
            g.setColor(outline);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        }
        g.dispose();
        super.paintComponent(graphics);
    }

    private Color fillColor(boolean hover, boolean pressed) {
        if (tone == Tone.ACTION) {
            return pressed ? new Color(242, 231, 211, 240) : new Color(255, 247, 229, hover ? 238 : 214);
        }
        if (tone == Tone.DESTRUCTIVE) {
            return hover ? new Color(255, 238, 228, 190) : new Color(255, 238, 228, 0);
        }
        if (selectedStyle) return new Color(255, 249, 237, 222);
        return hover ? new Color(255, 249, 237, 126) : new Color(255, 249, 237, 0);
    }

    private Color outlineColor() {
        if (tone == Tone.ACTION) return new Color(73, 102, 147, 98);
        if (tone == Tone.DESTRUCTIVE) return getModel().isRollover() ? new Color(160, 88, 72, 75) : new Color(160, 88, 72, 0);
        return selectedStyle ? new Color(255, 255, 255, 150) : new Color(255, 255, 255, 0);
    }

    private Color foregroundForTone() {
        if (tone == Tone.DESTRUCTIVE) return new Color(145, 75, 64);
        return new Color(37, 57, 86);
    }
}
