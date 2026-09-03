module com.mkforge.pacxon {
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires javafx.base;
    requires java.logging;
    requires java.desktop;

    opens com.mkforge.pacxon to javafx.fxml;
    exports com.mkforge.pacxon;
}
