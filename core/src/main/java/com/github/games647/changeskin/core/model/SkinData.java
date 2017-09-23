package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.model.mojang.UUIDTypeAdapter;
import com.github.games647.changeskin.core.model.mojang.skin.MetadataModel;
import com.github.games647.changeskin.core.model.mojang.skin.SkinModel;
import com.github.games647.changeskin.core.model.mojang.skin.TextureModel;
import com.github.games647.changeskin.core.model.mojang.skin.TextureType;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SkinData {

    private static final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    private static final String URL_PREFIX = "http://textures.minecraft.net/texture/";

    private int skinId = -1;

    private final long timestamp;
    private final UUID uuid;
    private final String name;
    private final boolean slimModel;
    private final String skinURL;
    private final String capeURL;
    private final String encodedSignature;

    private final transient String encodedData;

    public SkinData(int skinId, long timestamp, UUID uuid, String name
            , boolean slimModel, String skinURL, String capeURL, byte[] signature) {
        this.skinId = skinId;

        this.timestamp = timestamp;
        this.uuid = uuid;
        this.name = name;
        this.slimModel = slimModel;
        this.skinURL = skinURL;
        this.capeURL = capeURL;
        this.encodedSignature = Base64.getEncoder().encodeToString(signature);
        this.encodedData = serializeData();
    }

    public SkinData(String encodedData, String signature) {
        this.encodedSignature = signature;
        this.encodedData = encodedData;

        SkinModel data = deserializeData(encodedData);
        this.timestamp = data.getTimestamp();
        this.uuid = data.getProfileId();
        this.name = data.getProfileName();

        Map<TextureType, TextureModel> textures = data.getTextures();
        if (textures != null && textures.get(TextureType.SKIN) != null) {
            TextureModel skin = textures.get(TextureType.SKIN);
            this.skinURL = skin.getUrl().replace(URL_PREFIX, "");
            this.slimModel = textures.get(TextureType.SKIN).getMetadata() != null;
        } else {
            this.skinURL = "";
            this.slimModel = false;
        }

        if (textures != null && textures.get(TextureType.CAPE) != null) {
            this.capeURL = textures.get(TextureType.CAPE).getUrl().replace(URL_PREFIX, "");
        } else {
            this.capeURL = "";
        }
    }

    public synchronized int getSkinId() {
        return skinId;
    }

    public synchronized void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public boolean isSlimModel() {
        return slimModel;
    }

    public String getSkinURL() {
        return skinURL;
    }

    public String getCapeURL() {
        return capeURL;
    }

    public String getEncodedSignature() {
        return encodedSignature;
    }

    public String getEncodedData() {
        return encodedData;
    }

    private String serializeData() {
        SkinModel dataModel = new SkinModel();
        dataModel.setTimestamp(timestamp);
        dataModel.setProfileId(uuid);
        dataModel.setProfileName(name);

        if (skinURL != null && !skinURL.isEmpty()) {
            Map<TextureType, TextureModel> textures = Maps.newHashMap();

            TextureModel skinModel = new TextureModel();
            skinModel.setUrl(URL_PREFIX + skinURL);
            if (slimModel) {
                skinModel.setMetadata(new MetadataModel());
            }

            textures.put(TextureType.SKIN, skinModel);

            if (capeURL != null && !capeURL.isEmpty()) {
                TextureModel capeModel = new TextureModel();
                capeModel.setUrl(URL_PREFIX + capeURL);
                textures.put(TextureType.CAPE, capeModel);
            }

            dataModel.setTextures(textures);
        }

        String json = gson.toJson(dataModel);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int hashCode() {
        return Objects.hash(slimModel, skinURL, capeURL);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        SkinData otherSkin = (SkinData) other;
        return this.slimModel != otherSkin.slimModel
                && Objects.equals(this.skinURL, otherSkin.skinURL)
                && Objects.equals(this.capeURL, otherSkin.capeURL);
    }

    private SkinModel deserializeData(String encodedData) {
        byte[] data = Base64.getDecoder().decode(encodedData);
        String rawJson = new String(data, StandardCharsets.UTF_8);

        return gson.fromJson(rawJson, SkinModel.class);
    }
}
