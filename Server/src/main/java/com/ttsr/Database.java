package com.ttsr;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection connection;
    private List<User> users;
    private static final Logger logger = Logger.getLogger(Database.class.getName());

    public Database()  {
        init();
        try(Connection connection = getConnection()){
            System.out.println("Database connected");
//            logger.log(Level.INFO,"Database connected");
            this.connection = connection;
            createDB();
            writeDB();
            users = getUsersFromDB();
        } catch (SQLException e ){
            logger.log(Level.ERROR,e.getMessage());
            //e.printStackTrace();
        }
    }
    private void init() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.log(Level.ERROR,e.getMessage());
            //e.printStackTrace();
        }
    }

    public Connection getConnection() throws  SQLException{
        return DriverManager.getConnection("jdbc:sqlite:cloud.s3db");
    }
    private void clearDB() throws SQLException {
        try(Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE if exists 'users'");
        }
    }
    private void createDB() throws SQLException {
        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE if not exists 'users'('id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "'login' text, 'password' text)");
        }
    }
    private void writeDB() throws SQLException {
        String sql2 = "INSERT INTO users (login, password)" +
                "SELECT * FROM (SELECT ?,?) AS tmp " +
                "WHERE NOT EXISTS (" +
                "    SELECT login FROM users WHERE login = ?" +
                ") LIMIT 1;";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql2)) {
            addUserRow(preparedStatement,"login1","pass1");
            addUserRow(preparedStatement,"login2","pass2");
            addUserRow(preparedStatement,"login3","pass3");
            preparedStatement.executeBatch();
        }

    }
    private List<User> getUsersFromDB() throws SQLException {
        List<User> users = new ArrayList<>();
        try(ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM users")) {
            while (resultSet.next()) {
                String login = resultSet.getString("login");
                String password = resultSet.getString("password");
                String username = resultSet.getString("username");
                users.add(new User(login,password));
            }
        }
        return users;
    }
    public List<User> getUsers()  {
        return users;
    }
    private void addUserRow(PreparedStatement preparedStatement, String login, String password) throws SQLException {
        preparedStatement.setString(1,login);
        preparedStatement.setString(2,password);
        preparedStatement.addBatch();
    }

    public void stop() {
        try {
            connection.close();
            logger.log(Level.INFO,"Database connection closed");
        } catch (SQLException e) {
            logger.log(Level.ERROR,"Database exception when trying to close the connection"+e.getMessage());
        }
    }
}
