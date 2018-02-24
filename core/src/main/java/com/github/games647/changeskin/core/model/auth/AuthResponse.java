package com.github.games647.changeskin.core.model.auth;

import com.github.games647.changeskin.core.model.GameProfile;

public class AuthResponse {

    private String accessToken;
    private GameProfile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public GameProfile getSelectedProfile() {
        return selectedProfile;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "selectedProfile=" + selectedProfile +
                '}';
    }
}
