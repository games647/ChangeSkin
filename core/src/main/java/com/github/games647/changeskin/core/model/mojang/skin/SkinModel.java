package com.github.games647.changeskin.core.model.mojang.skin;

public class SkinModel {

    private long timestamp;

    private String profileId;

    private String profileName;

    private final boolean signatureRequired = true;

    private DataModel textures;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public DataModel getTextures() {
        return textures;
    }

    public void setTextures(DataModel textures) {
        this.textures = textures;
    }

    public boolean isSignatureRequired() {
        return signatureRequired;
    }
}
