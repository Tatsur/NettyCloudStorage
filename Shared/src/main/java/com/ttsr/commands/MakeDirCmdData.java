package com.ttsr.commands;

import java.io.Serializable;

public class MakeDirCmdData implements Serializable {

    private final String dirName;

    public MakeDirCmdData(String dirName) {
        this.dirName = dirName;
    }

    public String getDirName() {
        return dirName;
    }
}
