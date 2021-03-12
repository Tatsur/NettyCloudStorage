package com.ttsr.controllers;

import com.ttsr.ClientApp;
import com.ttsr.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class AuthDialogController {
    private @FXML TextField loginField;
    private @FXML PasswordField passwordField;
    private @FXML Button authButton;

    private Network network;
    private ClientApp clientApp;

    @FXML
    public void executeAuth(ActionEvent actionEvent) throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            ClientApp.showNetworkError("Username and password should be not empty!", "Auth error");
            return;
        }
        network.sendAuthCommand(login, password);
        clientApp.openCloudView();
        network.sendFileListRequest(null);
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }
}
