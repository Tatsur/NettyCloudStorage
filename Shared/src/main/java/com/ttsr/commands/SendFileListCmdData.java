package com.ttsr.commands;

import java.io.Serializable;
import java.util.ArrayList;

public class SendFileListCmdData implements Serializable {

    private final ArrayList<String> fileList;
    private final String directory;

    public SendFileListCmdData(ArrayList<String> fileList, String directory) {
        this.fileList = fileList;
        this.directory = directory;
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }

    public String getDirectory() {
        return directory;
    }
}
