package com.ttsr.controllers;

import com.ttsr.ClientApp;
import com.ttsr.models.Network;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ViewController {

    @FXML
    public ListView<String> usersList;
    public Button changeUsernameButton;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea chatHistory;
    @FXML
    private TextField textField;

    private Network network;

    private String selectedRecipient;

    private static final String HISTORY_FILE_NAME = "chatHistory.txt";

    @FXML
    public void initialize() {
        usersList.setItems(FXCollections.observableArrayList(ClientApp.USERS_TEST_DATA));
        sendButton.setOnAction(event -> sendMessage());
        changeUsernameButton.setOnAction(event -> {
            try {
                ClientApp.showChangeUsernameDialog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        textField.setOnAction(event -> sendMessage());

        usersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = usersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                usersList.requestFocus();
                if (! cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedRecipient = null;
                    } else {
                        selectionModel.select(index);
                        selectedRecipient = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }

    private void sendMessage() {
        String message = textField.getText();
        appendMessage("Я: " + message);
        textField.clear();
        try {
            if(selectedRecipient != null){
                network.sendPrivateMessage(message,selectedRecipient);
            }
            else {
                network.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            String errorMessage = "Failed to send message";
            ClientApp.showNetworkError(e.getMessage(), errorMessage);
        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void appendMessage(String message) {
        String timestamp = DateFormat.getInstance().format(new Date());
        chatHistory.appendText(timestamp);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(message);
        chatHistory.appendText(System.lineSeparator());
        chatHistory.appendText(System.lineSeparator());
    }
    public void saveHistory(){
        File historyFile = new File(HISTORY_FILE_NAME);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(historyFile))){
            outputStreamWriter.write(chatHistory.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadHistory(){
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(HISTORY_FILE_NAME))){
            List<String> lines = new LinkedList<>();
            while (bufferedReader.ready()){
                lines.add(bufferedReader.readLine());
            }
            while (lines.size()>100){
                lines.remove(0);
            }
            for (String line : lines) {
                chatHistory.appendText(line);
                chatHistory.appendText(System.lineSeparator());
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showError(String title, String message) {
        ClientApp.showNetworkError(message, title);
    }

    public void updateUsers(List<String> users) {
        usersList.setItems(FXCollections.observableArrayList(users));
    }
}