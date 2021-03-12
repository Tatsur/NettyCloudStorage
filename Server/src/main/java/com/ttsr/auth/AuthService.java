package com.ttsr.auth;

import com.ttsr.Database;
import com.ttsr.User;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AuthService implements IAuthService{

    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    private static final List<User> USERS = new ArrayList<>();

    @Override
    public void start() {
        System.out.println("Auth service has been started");
//        logger.log(Level.INFO,"Auth service has been started");
        Database database = new Database();
        setUsers(database.getUsers());
    }

    @Override
    public void stop() {
        logger.log(Level.INFO,"Auth service has been finished");
    }

    @Override
    public String getLogin(String login, String password) {
        for (User user : USERS) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user.getLogin();
            }
        }
        return null;
    }

    @Override
    public void setUsers(List<User> users) {
        USERS.addAll(users);
    }
}
