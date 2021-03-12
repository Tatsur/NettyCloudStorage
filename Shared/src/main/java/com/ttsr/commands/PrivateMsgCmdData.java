package com.ttsr.commands;

import java.io.Serializable;

public class PrivateMsgCmdData implements Serializable {

    private final String receiver;
    private final String message;

    public PrivateMsgCmdData(String receiver, String message) {
        this.receiver = receiver;
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }
}
