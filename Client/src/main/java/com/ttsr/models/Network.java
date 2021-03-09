package com.ttsr.models;

import com.ttsr.Command;
import com.ttsr.CommandType;
import com.ttsr.ClientApp;
import com.ttsr.commands.ErrCmdData;
import com.ttsr.commands.GetFileCmdData;
import com.ttsr.commands.OkCmdData;
import com.ttsr.commands.SendFileListData;
import com.ttsr.controllers.ViewController;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;

import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;

    private final String host;
    private final int port;
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;
    private Socket socket;
    public String login;

    public Network() {
        this(SERVER_ADDRESS, SERVER_PORT);
    }

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.err.println("Соединение не было установлено!");
            e.printStackTrace();
            return false;
        }
    }
    public void checkConnectionStatus(){
        Thread thread = new Thread(()-> {
            while(true)
            {
                try {
                    Command command = readCommand();
                    if (command.getType() == CommandType.ERROR) {
                        System.out.println("got auth_error command");
                        ErrCmdData data = (ErrCmdData) command.getData();
                        ClientApp.isClose = true;
                        Platform.runLater(() -> {
                            ClientApp.showNetworkError(data.getErrMsg(), "Connection error");
                        });
                        close();
                        break;
                    }

                } catch (IOException e) {
                    System.err.println("Unknown command");
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    public String sendAuthCommand(String login, String password) {
        try {
            Command authCommand = new Command().authCmd(login, password);
            os.writeObject(authCommand);
            Command command = readCommand();
            if(command == null){
                return "Failed to read command from server";
            }
            System.out.println(command.getType().name());
            switch (command.getType()){
                case OK: {
                    OkCmdData data = (OkCmdData) command.getData();
                    this.login = data.getLogin();
                    System.out.println(login);
                    return null;
                }
                case ERROR:{
                    System.out.println("got auth_error command");
                    ErrCmdData data = (ErrCmdData) command.getData();
                    return data.getErrMsg();
                }
                default:
                    return "Unknown type of command from server: " + command.getType();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public ObjectEncoderOutputStream getOs() {
        return os;
    }

    public ObjectDecoderInputStream getIs() {
        return is;
    }

    private Command readCommand() throws IOException {
        try {
            return  (Command) is.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            String errorMessage = "Unknown type of object from client!";
            System.err.println(errorMessage);
            e.printStackTrace();
            return null;
        }
    }

    public void waitMessages(ViewController viewController) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Command command = readCommand();
                        if(command == null){
                            viewController.showError("Server error","Invalid command from server!");
                            continue;
                        }
                        switch (command.getType()){
                            case GET: {
                                GetFileCmdData data = (GetFileCmdData) command.getData();
                                String message = data.getFileName();
                                Platform.runLater(() -> {
//                                    viewController.appendMessage(formattedMsg);
                                });
                                break;
                            }
                            case LS: {
                                SendFileListData data = (SendFileListData) command.getData();
                                Platform.runLater(() -> {
                                    viewController.updateFileList(data.getFileList());
                                });
                                break;
                            }
                            case ERROR: {
                                ErrCmdData data = (ErrCmdData) command.getData();
                                String errorMsg = data.getErrMsg();
                                Platform.runLater(() -> {
                                    viewController.showError("Server error", errorMsg);
                                });
                                break;
                            }
                            default:{
                                viewController.showError("Unknown command from server",command.getType().toString());
                            }

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Соединение было потеряно!");
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private  void sendCommand(Command command) throws IOException {
        os.writeObject(command);
        os.flush();
    }

    public void setChatMode() {
        waitMessages(null);
    }

    public String getLogin() {
        return login;
    }

    public void sendFile(String message, String selectedFile) {
    }
}
