package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public class SkinModel {

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private transient int skinId;
    private transient final String encodedValue;
    private transient String encodedSignature;

    private final long timestamp;
    private final UUID profileId;
    private final String profileName;
    private final Map<TextureType, TextureModel> textures = Maps.newHashMap();

    public SkinModel(int skinId, long timestamp, UUID uuid, String name
            , boolean slimModel, String skinURL, String capeURL, byte[] signature) {
        this.skinId = skinId;

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
        skinModel.setEncodedSignature(signature);
        return skinModel;
    }

    public synchronized int getSkinId() {
        return skinId;
    }

    public synchronized void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public String getEncodedValue() {
        return encodedValue;
    }

    public String getSignature() {
        return encodedSignature;
    }

    private void setEncodedSignature(String encodedSignature) {
        this.encodedSignature = encodedSignature;
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

    public boolean isSignatureRequired() {
        return true;
    }

    private String serializeData() {
        String json = gson.toJson(this);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
