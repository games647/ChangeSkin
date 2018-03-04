package com.github.games647.changeskin.core.model;

import com.github.games647.craftapi.model.skin.SkinModel;

import java.time.Instant;
import java.util.UUID;

public class StoredSkin extends SkinModel {

    private transient int skinId;
    private transient byte[] signature;

    public StoredSkin(int skinId, Instant timestamp, UUID uuid, String name
            , boolean slimModel, String skinURL, String capeURL, byte[] signature) {
        super(timestamp, uuid, name, slimModel, skinURL, capeURL);

        this.skinId = skinId;
        this.signature = signature;
    }

    public int getSkinId() {
        synchronized (this) {
            return skinId;
        }
    }

    public boolean isSaved() {
        synchronized (this) {
            return skinId >= 0;
        }
    }

    public void setSkinId(int skinId) {
        synchronized (this) {
            this.skinId = skinId;
        }
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "skinId=" + skinId +
                '}';
    }
}
