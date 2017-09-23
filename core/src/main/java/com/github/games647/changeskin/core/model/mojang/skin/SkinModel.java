package com.github.games647.changeskin.core.model.mojang.skin;

import java.util.Map;
import java.util.UUID;

public class SkinModel {

    private long timestamp;
    private UUID profileId;
    private String profileName;
    private Map<TextureType, TextureModel> textures;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Map<TextureType, TextureModel> getTextures() {
        return textures;
    }

    public void setTextures(Map<TextureType, TextureModel> textures) {
        this.textures = textures;
    }

    public boolean isSignatureRequired() {
        return true;
    }
}
