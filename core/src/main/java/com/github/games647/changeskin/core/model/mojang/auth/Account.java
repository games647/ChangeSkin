package com.github.games647.changeskin.core.model.mojang.auth;

import com.github.games647.changeskin.core.model.PlayerProfile;

public class Account {

    private final PlayerProfile profile;
    private final String accessToken;

    public Account(PlayerProfile profile, String accessToken) {
        this.profile = profile;
        this.accessToken = accessToken;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
