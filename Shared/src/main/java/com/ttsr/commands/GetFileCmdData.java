package com.ttsr.commands;

import java.io.Serializable;

public class GetFileCmdData implements Serializable {

    private final String fileName;

    public GetFileCmdData(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
