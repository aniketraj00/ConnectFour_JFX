module com.aniket.connectfour {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.aniket.connectfour to javafx.fxml;
    exports com.aniket.connectfour;
}