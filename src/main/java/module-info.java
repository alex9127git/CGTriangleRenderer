module ru.alex {
    requires javafx.controls;
    requires javafx.fxml;

    opens ru.alex9127 to javafx.fxml;
    exports ru.alex9127;
}