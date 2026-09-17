package com.acadcore.backend.dto;

public class AcceptFacultyInvitationRequest {

    private String token;
    private String password;
    private String confirmPassword;

    public AcceptFacultyInvitationRequest() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}