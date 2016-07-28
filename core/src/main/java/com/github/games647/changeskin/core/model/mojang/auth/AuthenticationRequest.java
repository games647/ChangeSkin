package com.github.games647.changeskin.core.model.mojang.auth;

public class AuthenticationRequest {

    private final Agent agent = new Agent();

    private final String username;
    private final String password;

    public AuthenticationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
