package com.github.games647.changeskin.core.model.auth;

import com.github.games647.changeskin.core.model.GameProfile;

public class AuthenticationResponse {

    private String accessToken;
    private GameProfile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public GameProfile getSelectedProfile() {
        return selectedProfile;
    }
}
