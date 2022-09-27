package com.aniket.connectfour;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private TextField player1Name;
    @FXML
    private TextField player2Name;
    @FXML
    private ColorPicker player1Marker;
    @FXML
    private ColorPicker player2Marker;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void onQuitBtnClick() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onPlayBtnClick(ActionEvent event) {
        //Check if both the names have been entered before starting the game
        if(Objects.equals(player1Name.getText(), "") || Objects.equals(player2Name.getText(), "")) {
            showErrAlert("Name field empty!", "Please enter both player's name.");
        } else {
            //Check if the marker color of both the player is not same.
            if(player1Marker.getValue().equals(player2Marker.getValue())) {
                showErrAlert("Same marker color selected!", "Marker color for both the player can't be the same.");
            } else {
                if(player1Marker.getValue().equals(Color.WHITE) || player2Marker.getValue().equals(Color.WHITE)) {
                    showErrAlert("White marker color selected!", "For the marker to be visible, select marker color other than white.");
                } else {
                    //If all the initial checks are passed then start the game.
                    try {
                        startGame(event);
                    } catch (IOException e) {
                        new Alert(Alert.AlertType.ERROR, "Failed to load the resources!\n" + e.getMessage()).show();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void showErrAlert(String headerText, String contentText) {
        Alert errAlert = new Alert(Alert.AlertType.ERROR);
        errAlert.setHeaderText(headerText);
        errAlert.setContentText(contentText);
        errAlert.show();
    }
    private void startGame(ActionEvent event) throws IOException{

        //Get the stage (window), where the scene has to be loaded.
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        //Close the current stage.
        stage.close();

        //Create an instance of FXML loader.
        FXMLLoader loader = new FXMLLoader();

        //Set the fxml file location of the target view.
        loader.setLocation(getClass().getResource("app-view.fxml"));

        //Load the gameplay view using the loader.
        Parent gamePlayViewParent = loader.load();

        //Get the controller from the loader.
        GamePlayController gamePlayController = loader.getController();

        //Pass the necessary resources to the new scene using controller.
        gamePlayController.init(new Player(player1Name.getText(), player1Marker.getValue()), new Player(player2Name.getText(), player2Marker.getValue()));

        //Create a scene from the previously loaded view.
        Scene gamePlayViewScene = new Scene(gamePlayViewParent);

        //Load the scene onto the target stage.
        stage.setScene(gamePlayViewScene);

        //Display the stage.
        stage.show();
    }

}
