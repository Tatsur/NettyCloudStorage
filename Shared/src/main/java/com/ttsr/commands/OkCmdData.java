package com.ttsr.commands;

import java.io.Serializable;

public class OkCmdData implements Serializable {

    private final String message;

    public OkCmdData(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
