package com.github.games647.changeskin;

import java.util.UUID;

public class UserPreferences {

    private final UUID uuid;
    private SkinData targetSkin;

    public UserPreferences(UUID uuid, SkinData targetSkin) {
        this.uuid = uuid;
        this.targetSkin = targetSkin;
    }

    public UserPreferences(UUID uuid) {
        this(uuid, null);
    }

    public UUID getUuid() {
        return uuid;
    }

    public SkinData getTargetSkin() {
        return targetSkin;
    }

    public void setTargetSkin(SkinData targetSkin) {
        this.targetSkin = targetSkin;
    }
}
