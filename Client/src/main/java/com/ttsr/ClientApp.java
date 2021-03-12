package com.ttsr;


import com.ttsr.controllers.AuthDialogController;
import com.ttsr.controllers.ViewController;
import com.ttsr.models.Network;
import com.ttsr.utils.UtilMethods;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jdk.jshell.execution.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClientApp extends Application {

    public static List<String> files = new ArrayList<>();
    public static Stage changeNameDialogStage;

    public static Stage primaryStage;
    private Stage authDialogStage;
    private Network network;
    private ViewController viewController;
    public static boolean isClose = false;

    public ViewController getViewController() {
        return viewController;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ClientApp.primaryStage = primaryStage;
        Thread thread = new Thread(()->{
            network = new Network();
            network.connect();
            network.setClientApp(this);
            if (!network.connected) {
                showNetworkError("", "Failed to connect to server");
                return;
            }
        });
        thread.setDaemon(true);
        thread.start();
        openAuthDialog(primaryStage);
        creatCloudView(primaryStage);
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
    private void creatCloudView(Stage primaryStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader();
        mainLoader.setLocation(ClientApp.class.getResource("/com.ttsr/views/view.fxml"));

        Parent root = mainLoader.load();

        primaryStage.setTitle("Netty Cloud Storage");
        primaryStage.setScene(new Scene(root, 600, 400));

        viewController = mainLoader.getController();
        viewController.loadHistory();
        viewController.setNetwork(network);
        network.setViewController(viewController);
        primaryStage.setOnCloseRequest(event -> {
            viewController.saveHistory();
            network.close();
        });
    }
    private void openAuthDialog(Stage primaryStage) throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(ClientApp.class.getResource("/com.ttsr/views/authDialog.fxml"));
        Parent authDialogPanel = authLoader.load();
        authDialogStage = new Stage();

        authDialogStage.setTitle("Authentication");
        authDialogStage.initModality(Modality.WINDOW_MODAL);
        authDialogStage.initOwner(primaryStage);
        Scene scene = new Scene(authDialogPanel);
        authDialogStage.setScene(scene);
        authDialogStage.show();


        AuthDialogController authController = authLoader.getController();
        authController.setNetwork(network);
        authController.setClientApp(this);
    }

    public static void main(String[] args) {
        launch();
    }

    public void openCloudView() {
        authDialogStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getLogin());
    }
}