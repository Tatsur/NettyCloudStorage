package com.ttsr.models;

import com.ttsr.ClientApp;
import com.ttsr.Command;
import com.ttsr.commands.*;
import com.ttsr.controllers.ViewController;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;

public class InHandler extends SimpleChannelInboundHandler<Command> {


    private final Network network;
    private final ViewController viewController;

    public InHandler(Network network) {
        this.network = network;
        this.viewController = network.viewController;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext chc, Command command) throws Exception {

        switch (command.getType()){
            case FILE_OK: {
                GetFileCmdData data = (GetFileCmdData) command.getData();
                String message = data.getFileName();
                network.sendFile(viewController);
                Platform.runLater(() -> {
                    viewController.appendMessage("File request from server:"+ message);
                    viewController.progressBar.setVisible(true);
                });
                break;
            }
            case LS: {
                SendFileListCmdData data = (SendFileListCmdData) command.getData();
                Platform.runLater(() -> {
                    viewController.appendMessage("Got cloud file list");
                    viewController.updateCloudFilesList(data.getFileList());
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
            case OK: {
                OkCmdData data = (OkCmdData) command.getData();
                String msg = data.getMessage();
                Platform.runLater(() -> {viewController.appendMessage(msg);
                });
                break;
            }
            case PROGRESS:{
                SendProgressData data = (SendProgressData) command.getData();
                Double progressValue = data.getProgress();
                Platform.runLater(() -> {
                    viewController.updateProgressBar(progressValue);
                });
                break;
            }
            default:{
                viewController.showError("Unknown command from server",command.getType().toString());
            }

        }
    }
}
