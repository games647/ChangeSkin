package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SkinModel {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    private static final Gson legacyGson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .registerTypeAdapter(TextureModel.class, new LegacyTextureAdapter())
            .create();
    private static final long LEGACY_TIMESTAMP = 1516492800000L;

    private transient int rowId;
    private transient String encodedValue;
    private transient String encodedSignature;

    //this can be null if initialized by gson
    private transient ReadWriteLock lock = new ReentrantReadWriteLock();

    //the order of these fields are relevant
    private final long timestamp;
    private final UUID profileId;
    private final String profileName;

    private final boolean signatureRequired = true;
    private final Map<TextureType, TextureModel> textures = new EnumMap<>(TextureType.class);

    public SkinModel(int rowId, long timestamp, UUID uuid, String name
            , boolean slimModel, String skinURL, String capeURL, byte[] signature) {
        this.rowId = rowId;

        this.timestamp = timestamp;
        this.profileId = Objects.requireNonNull(uuid);
        this.profileName = name;
        
        if (skinURL != null && !skinURL.isEmpty()) {
            textures.put(TextureType.SKIN, new TextureModel(skinURL, slimModel));
        }

        if (capeURL != null && !capeURL.isEmpty()) {
            textures.put(TextureType.CAPE, new TextureModel(capeURL));
        }
        
        this.encodedSignature = Base64.getEncoder().encodeToString(signature);
        this.encodedValue = serializeData();
    }

    public static SkinModel createSkinFromEncoded(String encodedData, String signature) {
        byte[] data = Base64.getDecoder().decode(encodedData);
        String rawJson = new String(data, StandardCharsets.UTF_8);

        SkinModel skinModel = gson.fromJson(rawJson, SkinModel.class);
        skinModel.setRowId(-1);
        skinModel.encodedSignature = signature;
        skinModel.encodedValue = encodedData;
        Objects.requireNonNull(skinModel.getProfileId());
        return skinModel;
    }

    public int getRowId() {
        getLazyLock().readLock().lock();
        try {
            return rowId;
        } finally {
            getLazyLock().readLock().unlock();
        }
    }

    public boolean isSaved() {
        getLazyLock().readLock().lock();
        try {
            return rowId >= 0;
        } finally {
            getLazyLock().readLock().unlock();
        }
    }

    public void setRowId(int rowId) {
        getLazyLock().writeLock().lock();
        try {
            this.rowId = rowId;
        } finally {
            getLazyLock().writeLock().unlock();
        }
    }

    private ReadWriteLock getLazyLock() {
        if (lock == null) {
            lock = new ReentrantReadWriteLock();
        }

        return lock;
    }

    public boolean isOutdated(Duration autoUpdateDiff) {
        Duration difference = Duration.between(Instant.ofEpochMilli(timestamp), Instant.now());
        return !autoUpdateDiff.isNegative() && difference.compareTo(autoUpdateDiff) >= 0;
    }

    public Lock getSaveLock() {
        return getLazyLock().writeLock();
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public String getSignature() {
        return encodedSignature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public Map<TextureType, TextureModel> getTextures() {
        return textures;
    }

    private String serializeData() {
        String json;

        //at ~21 january 2018 Mojang changed the encoding format from metadata + url to url + metadata. This means
        //the signature is no longer valid
        if (timestamp <= LEGACY_TIMESTAMP) {
            json = legacyGson.toJson(this);
        } else {
            json = gson.toJson(this);
        }

        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String toString() {
        int rowId;

        getLazyLock().readLock().lock();
        try {
            rowId = this.rowId;
        } finally {
            getLazyLock().readLock().unlock();
        }

        return this.getClass().getSimpleName() + '{' +
                "rowId=" + rowId +
                ", encodedValue='" + encodedValue + '\'' +
                ", encodedSignature='" + encodedSignature + '\'' +
                ", timestamp=" + timestamp +
                ", profileId=" + profileId +
                ", profileName='" + profileName + '\'' +
                ", signatureRequired=" + signatureRequired +
                ", textures=" + textures +
                '}';
    }
}
