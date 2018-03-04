package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SkinModel {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private transient int rowId;
    private transient String encodedValue;
    private transient String encodedSignature;

    private final transient Lock saveLock = new ReentrantLock();

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
        this.profileId = uuid;
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
        return skinModel;
    }

    public synchronized int getRowId() {
        //this lock should be acquired in the save method
        return rowId;
    }

    public synchronized boolean isSaved() {
        //this lock should be acquired in the save method
            return rowId >= 0;
    }

    public synchronized void setRowId(int rowId) {
        //this lock should be acquired in the save method
        this.rowId = rowId;
    }

    public Lock getSaveLock() {
        return saveLock;
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
        String json = gson.toJson(this);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public synchronized String toString() {
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
