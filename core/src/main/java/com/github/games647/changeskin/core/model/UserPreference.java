package com.github.games647.changeskin.core.model;

import java.util.UUID;

public class UserPreference {

    private final UUID uuid;
    private SkinData targetSkin;
    private boolean keepSkin;

    public UserPreference(UUID uuid, SkinData targetSkin, boolean keepSkin) {
        this.uuid = uuid;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    public UserPreference(UUID uuid) {
        this(uuid, null, false);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isKeepSkin() {
        return keepSkin;
    }

    public void setKeepSkin(boolean keepSkin) {
        this.keepSkin = keepSkin;
    }

    public SkinData getTargetSkin() {
        return targetSkin;
    }

    public void setTargetSkin(SkinData targetSkin) {
        this.targetSkin = targetSkin;
    }
}
