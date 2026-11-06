package ru.alex9127;

import javafx.scene.paint.Color;

public record Point(int x, int y, Color color) {
    @SuppressWarnings("SuspiciousNameCombination")
    public Point transposed() {
        return new Point(y, x, color);
    }
}
