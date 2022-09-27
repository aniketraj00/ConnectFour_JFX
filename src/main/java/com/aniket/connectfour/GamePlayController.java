package com.aniket.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class GamePlayController implements Initializable {

    @FXML
    private StackPane appViewRootNode;
    @FXML
    private MenuBar mainMenuBar;
    @FXML
    private GridPane mainGridPane;
    @FXML
    private Label player1NameLabel;
    @FXML
    private Label player2NameLabel;
    @FXML
    private Label gameStatusLabel;

    private static final int ROW_COUNT = 6;
    private static final int COLUMN_COUNT = 7;
    private static final String COLOR_FOR_HOVER_EFFECT = "#eeeeee40";
    private static final Duration TRANSITION_DURATION = Duration.seconds(0.5);
    private static final double DISC_RADIUS = 40.0;
    private enum GameStatus {
        IN_PROGRESS, DRAW, OVER, WAIT_PLAYER_TURN
    }
    private final Player[][] playGround = new Player[ROW_COUNT][COLUMN_COUNT];
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Player startingPlayer;
    private GameStatus currentGameStatus;
    private Alert mainAlert;
    private Popup gameOverPopup;
    private EventHandler<MouseEvent> onGamePlayHandler;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //Create rectangles corresponding to each of the grid columns and insert it into the root node (stack pane) for hover effect.
        createHoverRectangles();

        //Initialize the gameplay handler.
        onGamePlayHandler = this::onGamePlay;

        //Attach the gameplay handler to the root node.
        appViewRootNode.addEventHandler(MouseEvent.MOUSE_CLICKED, onGamePlayHandler);
    }


    //Event handlers
    @FXML
    private void onNewGame() {
        Stage stage = (Stage) appViewRootNode.getScene().getWindow();
        stage.close();
        try {
            stage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(getClass().getResource("app-init.fxml")))));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "", "Failed to load the resources. " + e.getMessage());
        }
        stage.show();
    }

    @FXML
    private void onRestartGame() {
        //Reset the layout.
        resetLayout();
        resetPlayground();

        //Alternate the user's first move (i.e. if in the first round player1 went first then in this round player2 will go first).
        if(startingPlayer == player1) {
            currentPlayer = player2;
            startingPlayer = player2;
        } else {
            currentPlayer = player1;
            startingPlayer = player1;
        }

        //Attach the click event listener on the root node i.e. stack pane (if removed).
        if(currentGameStatus == GameStatus.OVER || currentGameStatus == GameStatus.DRAW)
            this.appViewRootNode.addEventHandler(MouseEvent.MOUSE_CLICKED, onGamePlayHandler);

        //Change the gameplay status to waiting for player turn.
        currentGameStatus = GameStatus.WAIT_PLAYER_TURN;

        //Update game status label.
        updateGameStatusLabel();
    }

    @FXML
    private void onExit() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void onAboutGame() {
        try {
            StackPane contentNode = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("about-game.fxml")));
            showAlert(Alert.AlertType.INFORMATION, "About Game", "", contentNode, true);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the resources.", e.getMessage());
        }
    }

    @FXML
    private void onAboutDeveloper() {
        try {
            VBox contentNode = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("about-dev.fxml")));
            showAlert(Alert.AlertType.INFORMATION, "About Developer", "", contentNode, false);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load the resources.", e.getMessage());
        }

    }

    private void onGamePlay(MouseEvent e) {

        //Check if it's a valid player turn.
        if(currentGameStatus == GameStatus.WAIT_PLAYER_TURN) {

            //Change the game status to IN_PROGRESS.
            currentGameStatus = GameStatus.IN_PROGRESS;

            //Update the status label.
            updateGameStatusLabel();

            //Calculate the cell height of the grid pane.
            double gridPaneCellHeightErrOffset = 0.2;
            double gridPaneCellHeight = (mainGridPane.getHeight() / ROW_COUNT) + gridPaneCellHeightErrOffset;

            //Get the target node that was clicked.
            Node clickedNode = e.getPickResult().getIntersectedNode();

            //Check if the clicked node is one of the Rectangles that was inserted into the root node for hover effect.
            if(clickedNode.getClass() == Rectangle.class) {

                /* Get the target column index which corresponds to the rectangle that was clicked.
                 * Since the root node contains one element extra (i.e. a VBOX containing other layout contents) in addition
                 * to the rectangles. Hence, the index value is subtracted by one.
                 */
                int targetColumnIdx = appViewRootNode.getChildren().indexOf(clickedNode) - 1;

                //Get the row index of the target empty cell.
                int targetRowIdx = getEmptyCellRowIdx(targetColumnIdx);

                //Check if the cell is empty
                if(targetRowIdx != -1) {

                    //Create a new Circle node (disc).
                    Circle disc = new Circle();

                    //Set the node props.
                    disc.setRadius(DISC_RADIUS);
                    disc.fillProperty().setValue(currentPlayer.getMarkerColor());
                    GridPane.setHalignment(disc, HPos.CENTER);
                    GridPane.setValignment(disc, VPos.CENTER);

                    //Add the node to the topmost cell of the target column in the grid pane.
                    mainGridPane.add(disc, targetColumnIdx, 0);

                    //Make a transition to the target cell.
                    TranslateTransition t = new TranslateTransition();
                    t.setNode(disc);
                    t.setDuration(TRANSITION_DURATION);
                    t.setByY(gridPaneCellHeight * targetRowIdx);
                    t.play();

                    //Update the playground array.
                    playGround[targetRowIdx][targetColumnIdx] = currentPlayer;

                    //Check the game status once the disc transition animation is complete.
                    t.setOnFinished(event ->  {
                        //Check if the current player won, update game status and remove gameplay listener (if required).
                        if(isCurrentPlayerWinner(targetRowIdx, targetColumnIdx)) {
                            currentGameStatus = GameStatus.OVER;
                            showGameOverPopup(updateGameStatusLabel());
                            appViewRootNode.removeEventHandler(MouseEvent.MOUSE_CLICKED, onGamePlayHandler);
                        } else {
                            if(!isPlayGroundFull()) {
                                currentGameStatus = GameStatus.WAIT_PLAYER_TURN;
                                changeCurrentPlayer();
                                updateGameStatusLabel();
                            } else {
                                currentGameStatus = GameStatus.DRAW;
                                showGameOverPopup(updateGameStatusLabel());
                                appViewRootNode.removeEventHandler(MouseEvent.MOUSE_CLICKED, onGamePlayHandler);
                            }
                        }
                    });

                }
            }
        }

    }



    //Auxiliary Methods.
    public void init(Player player1, Player player2) {
        //Init game props.
        this.player1 = player1;
        this.player2 = player2;
        currentPlayer = chooseRandomPlayer();
        startingPlayer = currentPlayer;
        currentGameStatus = GameStatus.WAIT_PLAYER_TURN;

        //Init UI.
        player1NameLabel.setText(player1.getName());
        player2NameLabel.setText(player2.getName());
        updateGameStatusLabel();
    }

    private void initAlert(Alert.AlertType type, String title, String headerText) {
        mainAlert = new Alert(type);
        mainAlert.setTitle(title);
        mainAlert.setHeaderText(headerText);
    }

    private void showAlert(Alert.AlertType type, String title, String headerText, String contentText) {
        if(mainAlert != null && mainAlert.isShowing()) mainAlert.hide();
        initAlert(type, title, headerText);
        mainAlert.setContentText(contentText);
        mainAlert.show();
    }

    private void showAlert(Alert.AlertType type, String title, String headerText, Node contentNode, boolean clearGraphics) {
        if(mainAlert != null && mainAlert.isShowing()) mainAlert.hide();
        initAlert(type, title, headerText);
        if(clearGraphics) mainAlert.setGraphic(new Region());
        mainAlert.getDialogPane().setContent(contentNode);
        mainAlert.show();
    }

    private void createHoverRectangles() {
        for(int i = 0; i < COLUMN_COUNT; i++) {
            Rectangle rect = new Rectangle();

            rect.yProperty().bind(mainMenuBar.heightProperty());
            rect.heightProperty().bind(mainGridPane.heightProperty());
            rect.widthProperty().bind(mainGridPane.widthProperty().divide(COLUMN_COUNT));

            rect.translateXProperty().bind(mainGridPane.widthProperty().divide(COLUMN_COUNT).multiply(i));
            rect.setFill(Color.TRANSPARENT);

            rect.setOnMouseEntered(e -> rect.setFill(Color.valueOf(COLOR_FOR_HOVER_EFFECT)));
            rect.setOnMouseExited(e -> rect.setFill(Color.TRANSPARENT));

            StackPane.setAlignment(rect, Pos.BOTTOM_LEFT);
            appViewRootNode.getChildren().add(rect);

        }
    }

    private int getEmptyCellRowIdx(int columnIdx) {
        for(int i = ROW_COUNT - 1; i >= 0; i--) {
            if(playGround[i][columnIdx] == null) return i;
        }
        return -1;
    }

    private void changeCurrentPlayer() {
        currentPlayer = currentPlayer == player1 ? player2 : player1;
    }

    private String updateGameStatusLabel() {
        String msg;
        if(currentGameStatus == GameStatus.WAIT_PLAYER_TURN) msg = currentPlayer + "'s Turn!";
        else if(currentGameStatus == GameStatus.OVER) msg = currentPlayer + " is the Winner!";
        else if(currentGameStatus == GameStatus.DRAW) msg = "It was a Tie!";
        else msg = "Please wait...";
        gameStatusLabel.setText(msg);
        return msg;
    }

    private boolean isCurrentPlayerWinner(int targetRowIdx, int targetColumnIdx) {
        int count;
        int diagonalStartRowIdx;
        int diagonalStartColIdx;

        //1)Check if the player won by matching four consecutive vertical disc. "|"
        count = 0;
        for(int i = 0; i < ROW_COUNT; i++) {
            if(playGround[i][targetColumnIdx] == currentPlayer) {
                if(++count >= 4) return true;
            }
            else count = 0;
        }

        //2)Check if the player won by matching four consecutive horizontal disc. "-"
        count = 0;
        for(int i = 0; i < COLUMN_COUNT; i++) {
            if(playGround[targetRowIdx][i] == currentPlayer) {
                if(++count >= 4) return true;
            }
            else count = 0;
        }

        //3)Check if the player won by matching four consecutive disc along the diagonal(from left to right). "\
        count = 0;
        diagonalStartRowIdx = targetRowIdx;
        diagonalStartColIdx = targetColumnIdx;
        while (true) {
            if((diagonalStartRowIdx - 1) >= 0 && (diagonalStartColIdx - 1) >= 0) {
                diagonalStartRowIdx--;
                diagonalStartColIdx--;
            } else {
                break;
            }
        }
        for(int i = diagonalStartRowIdx, j = diagonalStartColIdx; i < ROW_COUNT && j < COLUMN_COUNT; i++, j++) {
            if(playGround[i][j] == currentPlayer) {
                if(++count >= 4) return true;
            }
        }

        //4)Check if the player won by matching four consecutive disc along the diagonal(from right to left). "/"
        count = 0;
        diagonalStartRowIdx = targetRowIdx;
        diagonalStartColIdx = targetColumnIdx;
        while (true) {
            if((diagonalStartRowIdx - 1) >= 0 && (diagonalStartColIdx + 1) < COLUMN_COUNT) {
                diagonalStartRowIdx--;
                diagonalStartColIdx++;
            } else {
                break;
            }
        }
        for(int i = diagonalStartRowIdx, j = diagonalStartColIdx; i < ROW_COUNT && j >= 0; i++, j--) {
            if(playGround[i][j] == currentPlayer) {
                if(++count >= 4) return true;
            }
        }

        //5)No Match found return false.
        return false;
    }

    private Player chooseRandomPlayer() {
        return (Math.floor(Math.random() * 2) == 0 ? player1 : player2) ;
    }

    private boolean isPlayGroundFull() {
        for(int i = 0; i < ROW_COUNT; i++) {
            for(int j = 0; j < COLUMN_COUNT; j++) {
                if(playGround[i][j] == null) return false;
            }
        }
        return true;
    }

    private void resetLayout() {
        if(mainGridPane.getChildren().removeAll(mainGridPane.getChildren())) {
            for(int i = 0; i < ROW_COUNT; i++) {
                for(int j = 0; j < COLUMN_COUNT; j++) {
                    Circle c = new Circle();
                    c.setRadius(DISC_RADIUS);
                    c.setFill(Color.WHITE);
                    GridPane.setMargin(c, new Insets(5));
                    GridPane.setHalignment(c, HPos.CENTER);
                    GridPane.setValignment(c, VPos.CENTER);
                    mainGridPane.add(c, j, i);
                }
            }
        }
    }

    private void resetPlayground() {
        for(int i = 0; i < ROW_COUNT; i++) {
            for(int j = 0; j < COLUMN_COUNT; j++) {
                playGround[i][j] = null;
            }
        }
    }

    private void showGameOverPopup(String msg) {
        try {
            //Load the popup layout from the fxml file using fxml loader.
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game-over-popup-view.fxml")));

            //Get the target elements from the loaded popup layout.
            Label popupMsgLabel = (Label) root.lookup("#popupMsgLabel");
            Button popupRestartBtn = (Button) root.lookup("#popupRestartBtn");
            Button popupQuitBtn = (Button) root.lookup("#popupQuitBtn");

            //Update the elements and add the event listeners for popup buttons.
            popupMsgLabel.setText(msg);
            popupRestartBtn.setOnAction(this::onPopupRestartBtn);
            popupQuitBtn.setOnAction(this::onPopupQuitBtn);

            //Create a new popup using the above layout.
            gameOverPopup = new Popup("Connect4", root);
            if(!gameOverPopup.getPopupWindow().isShowing()) {
                //Display the popup.
                gameOverPopup.getPopupWindow().show();
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "", "Failed to load the resources.");
        }
    }

    private void onPopupRestartBtn(ActionEvent e) {
        if(gameOverPopup != null && gameOverPopup.getPopupWindow().isShowing())
            gameOverPopup.getPopupWindow().close();
        onRestartGame();
    }

    private void onPopupQuitBtn(ActionEvent e) {
        onExit();
    }


}