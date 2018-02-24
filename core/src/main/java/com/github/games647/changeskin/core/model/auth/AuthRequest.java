package com.github.games647.changeskin.core.model.auth;

public class AuthRequest {

    private final Agent agent = new Agent();

    private final String username;
    private final String password;

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "agent=" + agent +
                ", username='" + username + '\'' +
                '}';
    }
}
