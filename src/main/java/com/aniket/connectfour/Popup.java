package com.aniket.connectfour;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Popup {
    private final Stage popupWindow;

    public Popup(String popupWindowTitle, Parent popupContent) {
        popupWindow = new Stage();
        popupWindow.initModality(Modality.APPLICATION_MODAL);
        popupWindow.setTitle(popupWindowTitle);
        Scene popupScene = new Scene(popupContent);
        popupWindow.setScene(popupScene);
        popupWindow.setResizable(false);

    }

    public Stage getPopupWindow() {
        return popupWindow;
    }
}
