package ru.alex9127;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;
import java.util.OptionalInt;

public class UI {
    public TextField x1field;
    public TextField y1field;
    public ColorPicker colorPicker1;
    public TextField x2field;
    public TextField y2field;
    public ColorPicker colorPicker2;
    public TextField x3field;
    public TextField y3field;
    public ColorPicker colorPicker3;
    public Canvas canvas;

    private Point p1 = new Point(0, 0, Color.RED);
    private Point p2 = new Point(0, 0, Color.LIME);
    private Point p3 = new Point(0, 0, Color.BLUE);

    @FXML
    private void initialize() {
        for (TextField field : List.of(x1field, y1field, x2field, y2field, x3field, y3field)) {
            field.textProperty().addListener((observable, oldText, newText) -> redraw());
        }
        for (ColorPicker picker : List.of(colorPicker1, colorPicker2, colorPicker3)) {
            picker.valueProperty().addListener((observable, oldColor, newColor) -> redraw());
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

    private Color colorBaryInterpolate(Color c1, Color c2, Color c3, BarycentricCoords bc) {
        return Color.rgb(
                (int) ((c1.getRed() * bc.alpha() + c2.getRed() * bc.beta()
                        + c3.getRed() * bc.gamma()) * 255),
                (int) ((c1.getGreen() * bc.alpha() + c2.getGreen() * bc.beta()
                        + c3.getGreen() * bc.gamma()) * 255),
                (int) ((c1.getBlue() * bc.alpha() + c2.getBlue() * bc.beta()
                        + c3.getBlue() * bc.gamma()) * 255)
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
        p1 = readPoint(x1field, y1field, colorPicker1, p1);
        p2 = readPoint(x2field, y2field, colorPicker2, p2);
        p3 = readPoint(x3field, y3field, colorPicker3, p3);
        PixelWriter pw = graphics.getPixelWriter();
        drawLine(pw, p1, p2);
        drawLine(pw, p2, p3);
        drawLine(pw, p3, p1);
        drawTriangle(pw, p1, p2, p3);
    }

    private Point readPoint(TextField xField, TextField yField, ColorPicker picker, Point previous) {
        return new Point(
                parse(xField).orElse(previous.x()),
                parse(yField).orElse(previous.y()),
                picker.valueProperty().get()
        );
    }

    private void drawLine(PixelWriter pw, Point start, Point end) {
        boolean steep = Math.abs(end.y() - start.y()) > Math.abs(end.x() - start.x());
        if (steep) {
            start = start.transposed();
            end = end.transposed();
        }

        if (end.x() < start.x()) {
            Point t = start; start = end; end = t;
        }

        int dx = end.x() - start.x();
        int dy = end.y() - start.y();
        double grad = (double) dy / dx;

        plot(pw, steep, start.x(), start.y(), start.color());
        plot(pw, steep, end.x(), end.y(), end.color());
        double intersectY = start.y() + grad;

        double alpha = 0;
        double da = 1.0 / dx;
        for (int x = start.x() + 1; x < end.x(); x++) {
            alpha += da;
            int y = (int) Math.floor(intersectY);
            double f = intersectY - y;
            Color color = colorInterpolate(start.color(), end.color(), alpha);
            plot(pw, steep, x, y, withOpacity(color, 1 - f));
            plot(pw, steep, x, y + 1, withOpacity(color, f));
            intersectY += grad;
        }
    }

    private void plot(PixelWriter pw, boolean steep, int x, int y, Color color) {
        pw.setColor(steep ? y : x, steep ? x : y, color);
    }

    private void drawTriangle(PixelWriter pw, Point a, Point b, Point c) {
        if (b.y() < a.y()) {
            Point t = a; a = b; b = t;
        }
        if (c.y() < b.y()) {
            Point t = b; b = c; c = t;
        }
        if (b.y() < a.y()) {
            Point t = a; a = b; b = t;
        }

        double t = (double) (b.y() - a.y()) / (c.y() - a.y());
        Point d = new Point((int) Math.round(linearInterpolate(a.x(), c.x(), t)), b.y(),
                colorInterpolate(a.color(), c.color(), t));
        drawSimpleTriangle(pw, a, b, d);
        drawSimpleTriangle(pw, b, c, d);
    }

    private void drawSimpleTriangle(PixelWriter pw, Point a, Point b, Point c) {
        double p = 0;
        double t;
        for (int y = a.y() + 1; y <= b.y(); y++) {
            p += 1.0 / (b.y() - a.y());
            double left = linearInterpolate(a.x(), b.x(), p);
            double right = linearInterpolate(
                    b.y() == c.y() ? a.x() : c.x(),
                    b.y() == c.y() ? c.x() : b.x(), p);
            if (right < left) {
                t = left; left = right; right = t;
            }
            for (int x = (int) Math.ceil(left); x <= (int) Math.floor(right); x++) {
                pw.setColor(x, y,
                        colorBaryInterpolate(a.color(), b.color(), c.color(),
                                BarycentricCoords.of(x, y, a, b, c)));
            }
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
