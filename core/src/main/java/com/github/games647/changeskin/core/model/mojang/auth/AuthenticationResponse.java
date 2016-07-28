package com.github.games647.changeskin.core.model.mojang.auth;

import com.github.games647.changeskin.core.model.PlayerProfile;

public class AuthenticationResponse {

    private String accessToken;
    private PlayerProfile selectedProfile;

    public String getAccessToken() {
        return accessToken;
    }

    public PlayerProfile getSelectedProfile() {
        return selectedProfile;
    }
}
