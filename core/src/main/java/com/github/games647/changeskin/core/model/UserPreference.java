package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UserPreference {

    private final UUID uuid;
    private final Lock saveLock = new ReentrantLock();

    private int rowId;
    private SkinModel targetSkin;
    private boolean keepSkin;

    public UserPreference(int rowId, UUID uuid, SkinModel targetSkin, boolean keepSkin) {
        this.rowId = rowId;
        this.uuid = uuid;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    public UserPreference(UUID uuid) {
        this(-1, uuid, null, false);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Lock getSaveLock() {
        return saveLock;
    }

    public synchronized int getRowId() {
        //this lock should be acquired in the save method
        return rowId;
    }

    public synchronized void setRowId(int rowId) {
        //this lock should be acquired in the save method
        this.rowId = rowId;
    }

    public boolean isSaved() {
        //this lock should be acquired in the save method
        return rowId >= 0;
    }

    public synchronized boolean isKeepSkin() {
        return keepSkin;
    }

    public synchronized void setKeepSkin(boolean keepSkin) {
        saveLock.lock();
        try {
            this.keepSkin = keepSkin;
        } finally {
            saveLock.unlock();
        }
    }

    public synchronized Optional<SkinModel> getTargetSkin() {
        return Optional.ofNullable(targetSkin);
    }

    public synchronized void setTargetSkin(SkinModel targetSkin) {
        saveLock.lock();
        try {
            this.targetSkin = targetSkin;
        } finally {
            saveLock.unlock();
        }
    }

    @Override
    public synchronized String toString() {
        return this.getClass().getSimpleName() + '{' +
                "rowId=" + rowId +
                ", uuid=" + uuid +
                ", targetSkin=" + targetSkin +
                ", keepSkin=" + keepSkin +
                '}';
    }
}
