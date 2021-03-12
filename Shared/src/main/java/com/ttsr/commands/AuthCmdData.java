package com.ttsr.commands;

import java.io.Serializable;

public class AuthCmdData implements Serializable {

    private final String login;
    private final String password;

    public AuthCmdData(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
