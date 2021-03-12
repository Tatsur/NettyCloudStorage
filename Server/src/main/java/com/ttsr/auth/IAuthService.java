package com.ttsr.auth;

import com.ttsr.User;

import java.util.List;

public interface IAuthService {
    void start();


    String getLogin(String login, String password);

    void stop();

    void setUsers(List<User> users);
}
