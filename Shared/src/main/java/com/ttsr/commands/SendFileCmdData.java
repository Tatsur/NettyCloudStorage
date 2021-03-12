package com.ttsr.commands;

import java.io.Serializable;

public class SendFileCmdData implements Serializable {

    private final String fileName;
    private final Long fileSize;

    public SendFileCmdData(String fileName, Long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }
}
