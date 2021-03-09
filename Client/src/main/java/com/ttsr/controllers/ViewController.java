package com.ttsr.controllers;

import com.ttsr.ClientApp;
import com.ttsr.models.Network;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class ViewController implements Initializable {

    @FXML
    public ListView<String> yourList;
    @FXML
    public ListView<String> cloudList;

    @FXML
    private Button sendButton;
    @FXML
    private TextArea commandLog;
    @FXML
    private TextField textField;

    private Network network;

    private String selectedFile;

    private static final String HISTORY_FILE_NAME = "commandLog.txt";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        yourList.setItems(FXCollections.observableArrayList(ClientApp.USERS_TEST_DATA));

        yourList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = yourList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                yourList.requestFocus();
                if (! cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedFile = null;
                    } else {
                        selectionModel.select(index);
                        selectedFile = cell.getItem();
                    }
                    event.consume();
                }
            });
            cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(event.getButton().equals(MouseButton.PRIMARY)){
                        if(event.getClickCount() == 2){
                            String fileName = cell.getItem();
                            sendFile(fileName);
                        }
                    }
                }
            });
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                yourList.requestFocus();
                if (! cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedFile = null;
                    } else {
                        selectionModel.select(index);
                        selectedFile = cell.getItem();
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }

    private void sendFile(String fileName) {
        String message = textField.getText();
        appendMessage("Я: " + message);
        textField.clear();
//        try {
            if(selectedFile != null){
                network.sendFile(message, selectedFile);
            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            String errorMessage = "Failed to send file";
//            ClientApp.showNetworkError(e.getMessage(), errorMessage);
//        }
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void appendMessage(String message) {
        String timestamp = DateFormat.getInstance().format(new Date());
        commandLog.appendText(timestamp);
        commandLog.appendText(System.lineSeparator());
        commandLog.appendText(message);
        commandLog.appendText(System.lineSeparator());
        commandLog.appendText(System.lineSeparator());
    }
    public void saveHistory(){
        File historyFile = new File(HISTORY_FILE_NAME);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(historyFile))){
            outputStreamWriter.write(commandLog.getText());
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
                commandLog.appendText(line);
                commandLog.appendText(System.lineSeparator());
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showError(String title, String message) {
        ClientApp.showNetworkError(message, title);
    }

    public void updateFileList(List<String> fileList) {
        cloudList.setItems(FXCollections.observableArrayList(fileList));
    }


}