package com.ttsr;

import com.ttsr.commands.*;

import java.io.Serializable;
import java.util.ArrayList;

public class Command implements Serializable {
    private CommandType type;
    private Object data;

    public CommandType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public Command authCmd(String login, String password) {
        Command command = new Command();
        command.type = CommandType.AUTH;
        command.data = new AuthCmdData(login, password);
        return command;
    }

    public Command okCmd(String message) {
        Command command = new Command();
        command.type = CommandType.OK;
        command.data = new OkCmdData(message);
        return command;
    }

    public Command errCmd(String errMsg) {
        Command command = new Command();
        command.type = CommandType.ERROR;
        command.data = new ErrCmdData(errMsg);
        return command;
    }

    public Command privateMsgCmd(String receiver, String message) {
        Command command = new Command();
        command.type = CommandType.PRIVATE_MESSAGE;
        command.data = new PrivateMsgCmdData(receiver, message);
        return command;
    }

    public Command publicMsgCmd(String login, String message) {
        Command command = new Command();
        command.type = CommandType.PUBLIC_MESSAGE;
        command.data = new PublicMsgCmdData(login, message);
        return command;
    }

    public Command file(byte[] buffer, int pointer) {
        Command command = new Command();
        command.type = CommandType.FILE;
        command.data = new FileData(buffer, pointer);
        return command;
    }

    public Command sendFile(String fileName, Long fileSize) {
        Command command = new Command();
        command.type = CommandType.SEND;
        command.data = new SendFileCmdData(fileName, fileSize);
        return command;
    }

    public Command getFile(String fileName) {
        Command command = new Command();
        command.type = CommandType.FILE_OK;
        command.data = new GetFileCmdData(fileName);
        return command;
    }

    public Command sendProgress(Double progress){
        Command command = new Command();
        command.type = CommandType.PROGRESS;
        command.data = new SendProgressData(progress);
        return command;
    }

    public Command sendFileList(ArrayList<String> fileList,String directory) {
        Command command = new Command();
        command.type = CommandType.LS;
        command.data = new SendFileListCmdData(fileList, directory);
        return command;
    }

    public Command makeDir(String dirName){
        Command command = new Command();
        command.type= CommandType.MK_DIR;
        command.data= new MakeDirCmdData(dirName);
        return command;
    }

}
