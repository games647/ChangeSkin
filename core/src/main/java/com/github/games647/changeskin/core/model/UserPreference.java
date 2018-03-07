package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserPreference {

    private final UUID uuid;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
        return lock.writeLock();
    }

    public int getRowId() {
        lock.readLock().lock();
        try {
            return rowId;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setRowId(int rowId) {
        lock.writeLock().lock();
        try {
            this.rowId = rowId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isSaved() {
        lock.readLock().lock();
        try {
            return rowId >= 0;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isKeepSkin() {
        lock.readLock().lock();
        try {
            return keepSkin;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setKeepSkin(boolean keepSkin) {
        lock.writeLock().lock();
        try {
            this.keepSkin = keepSkin;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<SkinModel> getTargetSkin() {
        lock.writeLock().lock();
        try {
            return Optional.ofNullable(targetSkin);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setTargetSkin(SkinModel targetSkin) {
        lock.writeLock().lock();
        try {
            this.targetSkin = targetSkin;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return this.getClass().getSimpleName() + '{' +
                    "rowId=" + rowId +
                    ", uuid=" + uuid +
                    ", targetSkin=" + targetSkin +
                    ", keepSkin=" + keepSkin +
                    '}';
        } finally {
            lock.readLock().unlock();
        }
    }
}
