package com.github.games647.changeskin.core.model;

import java.util.UUID;

public class UserPreference {

    private final UUID uuid;
    private SkinData targetSkin;
    private boolean isNew;

    public UserPreference(UUID uuid, SkinData targetSkin) {
        this.uuid = uuid;
        this.targetSkin = targetSkin;
    }

    public UserPreference(UUID uuid) {
        this(uuid, null);

        this.isNew = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isNew() {
        return isNew;
    }

    public SkinData getTargetSkin() {
        return targetSkin;
    }

    public void setTargetSkin(SkinData targetSkin) {
        this.targetSkin = targetSkin;
    }
}
