package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.UUID;

public class UserPreference {

    private int id;

    private final UUID uuid;
    private SkinModel targetSkin;
    private boolean keepSkin;

    public UserPreference(int id, UUID uuid, SkinModel targetSkin, boolean keepSkin) {
        this.id = id;
        this.uuid = uuid;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    public UserPreference(UUID uuid) {
        this(-1, uuid, null, false);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isSaved() {
        return id >= 0;
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

    //todo: this should be optional
    public SkinModel getTargetSkin() {
        return targetSkin;
    }

    public void setTargetSkin(SkinModel targetSkin) {
        this.targetSkin = targetSkin;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "id=" + id +
                ", uuid=" + uuid +
                ", targetSkin=" + targetSkin +
                ", keepSkin=" + keepSkin +
                '}';
    }
}
