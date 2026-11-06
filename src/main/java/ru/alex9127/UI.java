package ru.alex9127;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

public class UI {
    public TextField x1field;
    public TextField y1field;
    public TextField x2field;
    public TextField y2field;
    public TextField x3field;
    public TextField y3field;
    public Canvas canvas;

    private int x1, y1, x2, y2, x3, y3;

    @FXML
    private void initialize() {
        for (TextField field : List.of(x1field, y1field, x2field, y2field, x3field, y3field)) {
            field.textProperty().addListener((observable, oldText, newText) -> redraw());
        }
        redraw();
    }

    private double linearInterpolate(double x1, double x2, double t) {
        return x1 + (x2 - x1) * Math.clamp(t, 0, 1);
    }

    private Color colorInterpolate(Color c1, Color c2, double t) {
        return Color.rgb(
                (int) (linearInterpolate(c1.getRed(), c2.getRed(), t) * 255),
                (int) (linearInterpolate(c1.getGreen(), c2.getGreen(), t) * 255),
                (int) (linearInterpolate(c1.getBlue(), c2.getBlue(), t) * 255)
        );
    }

    private Color withOpacity(Color color, double opacity) {
        return Color.rgb(
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                opacity
        );
    }

    @FXML
    private void redraw() {
        GraphicsContext graphics = canvas.getGraphicsContext2D();
        graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        x1 = parse(x1field).orElse(x1);
        y1 = parse(y1field).orElse(y1);
        x2 = parse(x2field).orElse(x2);
        y2 = parse(y2field).orElse(y2);
        x3 = parse(x3field).orElse(x3);
        y3 = parse(y3field).orElse(y3);
        PixelWriter pw = graphics.getPixelWriter();
        Color c1 = Color.rgb(255, 0, 0, 1);
        Color c2 = Color.rgb(0, 255, 0, 1);
        Color c3 = Color.rgb(0, 0, 255, 1);
        drawLine(pw, x1, y1, x2, y2, c1, c2);
        drawLine(pw, x2, y2, x3, y3, c2, c3);
        drawLine(pw, x3, y3, x1, y1, c3, c1);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void drawLine(PixelWriter pw, int x1, int y1, int x2, int y2, Color c1, Color c2) {
        boolean steep = Math.abs(y2 - y1) > Math.abs(x2 - x1);
        if (steep) {
            int t = x1; x1 = y1; y1 = t;
            t = x2; x2 = y2; y2 = t;
        }

        if (x2 < x1) {
            int t = x1; x1 = x2; x2 = t;
            t = y1; y1 = y2; y2 = t;
            Color c = c1; c1 = c2; c2 = c;
        }

        int dx = x2 - x1;
        int dy = y2 - y1;
        double grad = (double) dy / dx;

        pw.setColor(steep ? y1 : x1, steep ? x1 : y1, c1);
        pw.setColor(steep ? y2 : x2, steep ? x2 : y2, c2);
        double interY = y1 + grad;

        double alpha = 0;
        double da = 1.0 / (x2 - x1);
        for (int x = x1 + 1; x < x2; x++) {
            alpha += da;
            int y = (int) Math.floor(interY);
            double f = interY - y;
            Color color = colorInterpolate(c1, c2, alpha);
            Color p1 = withOpacity(color, 1 - f);
            Color p2 = withOpacity(color, f);
            pw.setColor(steep ? y : x, steep ? x : y, p1);
            pw.setColor(steep ? y + 1 : x, steep ? x : y + 1, p2);
            interY += grad;
        }
    }

    private OptionalInt parse(TextField field) {
        try {
            OptionalInt result = OptionalInt.of(Integer.parseInt((field.getText().trim())));
            field.setBorder(new Border(new BorderStroke(
                    Paint.valueOf("0xff0000"),
                    BorderStrokeStyle.NONE,
                    CornerRadii.EMPTY,
                    BorderWidths.DEFAULT
            )));
            return result;
        } catch (NumberFormatException e) {
            field.setBorder(new Border(new BorderStroke(
                    Paint.valueOf("0xff0000"),
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    BorderWidths.DEFAULT
            )));
            return OptionalInt.empty();
        }
    }
}
