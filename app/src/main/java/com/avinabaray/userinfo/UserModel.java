package com.avinabaray.userinfo;

import java.io.Serializable;

public class UserModel implements Serializable {

    private String firstName;
    private String lastName;
    private String profiles;
    private String username;
    private String password;
    private String email;
    private int id;

    public UserModel(String firstName, String lastName, String profiles, String username, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.profiles = profiles;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfiles() {
        return profiles;
    }

    public void setProfiles(String profiles) {
        this.profiles = profiles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
