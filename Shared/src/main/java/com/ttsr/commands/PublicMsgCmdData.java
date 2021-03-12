package com.ttsr.commands;

import java.io.Serializable;

public class PublicMsgCmdData implements Serializable {

    private final String login;
    private final String message;

    public PublicMsgCmdData(String login, String message) {
        this.login = login;
        this.message = message;
    }

    public String getLogin() {
        return login;
    }

    public String getMessage() {
        return message;
    }
}
