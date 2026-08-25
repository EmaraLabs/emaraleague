package com.emaralabs.emaraleague.core.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;

/**
 * Animated gradient text for scoreboard titles.
 * Cycles through gradient stops over time for a luxury pulse effect.
 */
public final class GradientAnimator {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final List<Component> frames;
    private int currentFrame = 0;

    /**
     * Create animator with pre-built frames.
     * @param frames List of Adventure Components, one per frame
     */
    public GradientAnimator(List<Component> frames) {
        this.frames = List.copyOf(frames);
    }

    /**
     * Create a gold-yellow pulse animation (luxury feel).
     * @param text The text to animate
     * @param frameCount Number of frames in cycle (e.g. 8)
     * @return GradientAnimator ready to use
     */
    public static GradientAnimator goldPulse(String text, int frameCount) {
        List<Component> frames = new java.util.ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            double ratio = (double) i / frameCount;
            String color1 = interpolateColor(0xFFD700, 0xFFB800, ratio);
            String color2 = interpolateColor(0xFFB800, 0xFFD700, ratio);
            String miniMessage = "<gradient:" + color1 + ":" + color2 + ">" + text + "</gradient>";
            frames.add(MINI.deserialize(miniMessage));
        }
        return new GradientAnimator(frames);
    }

    /**
     * Create a simple two-color fade (more reliable than gradient).
     * @param text The text to animate
     * @param frameCount Number of frames
     * @return GradientAnimator with color fade
     */
    public static GradientAnimator colorFade(String text, int frameCount) {
        List<Component> frames = new java.util.ArrayList<>();
        int[] colors = {0xFFD700, 0xFFB800, 0xFFA500, 0xFFB800};
        for (int i = 0; i < frameCount; i++) {
            int color = colors[i % colors.length];
            frames.add(Component.text(text, net.kyori.adventure.text.format.TextColor.color(color)));
        }
        return new GradientAnimator(frames);
    }

    /**
     * Get next frame as Adventure Component.
     * Call this on each update tick.
     */
    public Component nextFrame() {
        Component frame = frames.get(currentFrame);
        currentFrame = (currentFrame + 1) % frames.size();
        return frame;
    }

    /**
     * Get current frame without advancing.
     */
    public Component currentFrame() {
        return frames.get(currentFrame);
    }

    /**
     * Reset to first frame.
     */
    public void reset() {
        currentFrame = 0;
    }

    /**
     * Get frame count.
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * Interpolate between two hex colors.
     */
    private static String interpolateColor(int color1, int color2, double ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return String.format("#%02X%02X%02X", r, g, b);
    }
}
