package com.ttsr.controllers;

import com.ttsr.ClientApp;
import com.ttsr.models.Network;
import com.ttsr.utils.UtilMethods;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

public class ViewController implements Initializable {

    @FXML
    public ListView<String> yourList;

    @FXML
    public ListView<String> cloudList;

    @FXML
    public ProgressBar progressBar;

    @FXML
    private TextArea commandLog;

    public TextArea getCommandLog() {
        return commandLog;
    }

    private Network network;

    private String selectedItem;
    private String selectedCloudItem;

    public File getSelectedFile() {
        return selectedFile;
    }

    private File selectedFile;
    private File selectedCloudFile;

    private String currentDirectory = "Client" + File.separator + "src" + File.separator + "data";

    private File currentDir = new File(currentDirectory);
    private File parentDir;
    private File parentCloudDir;

    private static final String HISTORY_FILE_NAME = "commandLog.txt";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        ContextMenu contextMenu = new ContextMenu();
        setContextMenu(contextMenu);

        getFileList(currentDir);
        progressBar.setVisible(false);
        yourList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = yourList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
//                cell.setContextMenu(contextMenu);
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                yourList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.select(index);
                        selectedItem = cell.getItem();
                    } else {
                        selectionModel.clearSelection(index);
                        selectedItem = null;
                    }
                    //doubleClick on file in user fileList
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                        String fileNameAndType = selectedItem;
                        String fileName;

                        fileName = getFileNameFromList(fileNameAndType);

                        //local or absolute path
                        if (currentDir.getParent() != null) {
                            parentDir = new File(currentDir.getParent());
                        } else {
                            currentDirectory = currentDir.getAbsolutePath();
                            parentDir = new File(currentDirectory.substring(0, currentDirectory.indexOf(parentDir.toString())));
                        }


                        if (fileName.equals("..") && parentDir.toString().length() > 0) {
                            currentDir = parentDir;
                            getFileList(currentDir);
                        } else {
                            File file = new File(currentDir.getPath() + File.separator + fileName);
                            if(file.exists()) {
                                if (file.isDirectory()) {
                                    currentDir = file;
                                    getFileList(currentDir);
                                } else {
                                    try {
                                        selectedFile = file;
                                        network.sendFileRequest(selectedFile.getName(),selectedFile.length());
                                    } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    event.consume();
                }
            });
            return cell;
        });

        cloudList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = yourList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
//                cell.setContextMenu(contextMenu);
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                cloudList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.select(index);
                        selectedCloudItem = cell.getItem();
                    } else {
                        selectionModel.clearSelection(index);
                        selectedCloudItem = null;
                    }

                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {

                        String fileNameAndType = selectedCloudItem;
                        String fileName;

                        if(fileNameAndType.trim().endsWith("bytes.")) {
                            fileName = fileNameAndType.substring(0, fileNameAndType.lastIndexOf(" ", fileNameAndType.length()-9));
                            try {
                                network.getFileRequest(fileName);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        else if(fileNameAndType.endsWith(File.separator)) {
                            fileName = fileNameAndType.substring(0, fileNameAndType.indexOf(File.separator) - 1);
                            try {
                                network.sendFileListRequest(fileName);
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                        else fileName = fileNameAndType.trim();

//                        if (currentDir.getParent() != null) {
//                            parentCloudDir = new File(currentDir.getParent());
//                        }

//                        if (fileName.equals("..") && parentCloudDir.toString().length() > 0) {
//                            currentDir = parentDir;
//                            getFileList(currentDir);
//                        } else {
//                            File file = new File(currentDir.getPath() + File.separator + fileName);
//                            if(file.exists()) {
//                                if (file.isDirectory()) {
//                                    currentDir = file;
//                                    getFileList(currentDir);
//                                } else {
//                                    try {
//                                        selectedCloudFile = file;
//                                        network.getFileRequest(selectedCloudFile.getName());
//                                    } catch (IOException ioException) {
//                                        ioException.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
                    }
                    event.consume();
                }
            });
            return cell;
        });

    }

    private String getFileNameFromList(String fileNameAndType) {
        String fileName;
        if(fileNameAndType.trim().endsWith("bytes.")) {
            fileName = fileNameAndType.substring(0, fileNameAndType.lastIndexOf(" ", fileNameAndType.length()-9));
        }
        else if(fileNameAndType.endsWith(File.separator))
            fileName = fileNameAndType.substring(0, fileNameAndType.indexOf(File.separator)-1);
        else fileName = fileNameAndType.trim();
        return fileName;
    }

    private void setContextMenu(ContextMenu contextMenu) {
        MenuItem sendItem = new MenuItem("send");
        contextMenu.getItems().add(sendItem);
        sendItem.setOnAction(event -> {
        });
        MenuItem deleteItem = new MenuItem("delete");
        contextMenu.getItems().add(deleteItem);
        sendItem.setOnAction(event -> {

        });
        EventHandler<WindowEvent> eventEventHandler = event -> {
            System.out.println("context menu is showing? " + contextMenu.isShowing());
        };
//        contextMenu.setOnShowing(eventEventHandler);
//        contextMenu.setOnHidden(eventEventHandler);
    }

    public void updateProgressBar(Double progressValue){
        if(progressValue>0 && progressValue < 1) {
            progressBar.setProgress(progressValue);
       }else {
            progressBar.setProgress(0);
            progressBar.setVisible(false);
        }
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

    public void saveHistory() {
        File historyFile = new File(HISTORY_FILE_NAME);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(historyFile))) {
            outputStreamWriter.write(commandLog.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadHistory() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(HISTORY_FILE_NAME))) {
            List<String> lines = new LinkedList<>();
            while (bufferedReader.ready()) {
                lines.add(bufferedReader.readLine());
            }
            while (lines.size() > 100) {
                lines.remove(0);
            }
            for (String line : lines) {
                commandLog.appendText(line);
                commandLog.appendText(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showError(String title, String message) {
        ClientApp.showNetworkError(message, title);
    }

    public void updateCloudFilesList(List<String> fileList) {
        cloudList.setItems(FXCollections.observableArrayList(fileList));
    }

    public void getFileList(File fileDir) {
        yourList.setItems(FXCollections.observableArrayList(UtilMethods.getFileList(fileDir)));
    }
}