package com.github.games647.changeskin.core.model.auth;

import com.github.games647.changeskin.core.model.GameProfile;

public class Account {

    private final GameProfile profile;
    private final String accessToken;

    public Account(GameProfile profile, String accessToken) {
        this.profile = profile;
        this.accessToken = accessToken;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "profile=" + profile +
                '}';
    }
}
