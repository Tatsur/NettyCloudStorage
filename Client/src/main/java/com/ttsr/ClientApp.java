package com.ttsr;


import com.ttsr.controllers.AuthDialogController;
import com.ttsr.controllers.ViewController;
import com.ttsr.models.Network;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;


public class ClientApp extends Application {

    public static final List<String> USERS_TEST_DATA = List.of("Oleg", "Alexey", "Peter");
    public static Stage changeNameDialogStage;

    public static Stage primaryStage;
    private Stage authDialogStage;
    private Network network;
    private ViewController viewController;
    public static boolean isClose = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        network = new Network();
        if (!network.connect()) {
            showNetworkError("", "Failed to connect to server");
            return;
        }
        openAuthDialog(primaryStage);
        createChatDialog(primaryStage);
    }

    public static void showNetworkError(String errorDetails, String errorTitle) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Network Error");
            alert.setHeaderText(errorTitle);
            alert.setContentText(errorDetails);
            alert.showAndWait();
            if(isClose) Platform.exit();
        });
    }
    public static void showChangeUsernameDialog() throws IOException {
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(ClientApp.class.getResource("/views/changeUsernameDialog.fxml"));
        Parent root = mainLoader.load();
        changeNameDialogStage = new Stage();
        changeNameDialogStage.setTitle("Change username");
        changeNameDialogStage.setScene(new Scene(root));
        changeNameDialogStage.initModality(Modality.APPLICATION_MODAL);
        changeNameDialogStage.initOwner(primaryStage);
        changeNameDialogStage.show();
    }
    private void createChatDialog(Stage primaryStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(ClientApp.class.getResource("/views/view.fxml"));

        Parent root = mainLoader.load();

        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root, 600, 400));

        viewController = mainLoader.getController();
        viewController.loadHistory();
        viewController.setNetwork(network);

        primaryStage.setOnCloseRequest(event -> {
            viewController.saveHistory();
            network.close();
        });
    }
    private void openAuthDialog(Stage primaryStage) throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ClientApp.class.getResource("/views/authDialog.fxml"));
        Parent authDialogPanel = authLoader.load();
        authDialogStage = new Stage();

        authDialogStage.setTitle("Аутентификая чата");
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        authDialogStage.initOwner(primaryStage);
        Scene scene = new Scene(authDialogPanel);
        authDialogStage.setScene(scene);
        authDialogStage.show();


        AuthDialogController authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setClientApp(this);
        //network.checkConnectionStatus();
    }

    public static void main(String[] args) {
        launch();
    }

    public void openChat() {
        authDialogStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        network.waitMessages(viewController);
    }
}