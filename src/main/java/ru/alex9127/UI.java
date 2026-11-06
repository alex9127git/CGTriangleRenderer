package ru.alex9127;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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
        graphics.strokeLine(x1, y1, x2, y2);
        graphics.strokeLine(x2, y2, x3, y3);
        graphics.strokeLine(x3, y3, x1, y1);
        pw.setColor(x1, y1, (Color) Paint.valueOf("0xff0000"));
        pw.setColor(x2, y2, (Color) Paint.valueOf("0x00ff00"));
        pw.setColor(x3, y3, (Color) Paint.valueOf("0x0000ff"));
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
