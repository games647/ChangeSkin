package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.mojang.skin.DataModel;
import com.github.games647.changeskin.core.model.mojang.skin.MetadataModel;
import com.github.games647.changeskin.core.model.mojang.skin.SkinModel;
import com.github.games647.changeskin.core.model.mojang.skin.TextureSourceModel;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

public class SkinData {

    public static final String URL_PREFIX = "http://textures.minecraft.net/texture/";

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
        this.uuid = ChangeSkinCore.parseId(data.getProfileId());
        this.name = data.getProfileName();

        DataModel textures = data.getTextures();
        if (textures != null && textures.getSKIN() != null) {
            this.skinURL = textures.getSKIN().getUrl().replace(URL_PREFIX, "");
            this.slimModel = textures.getSKIN().getMetadata() != null;
        } else {
            this.skinURL = "";
            this.slimModel = false;
        }

        if (textures != null && textures.getCAPE() != null) {
            this.capeURL = textures.getCAPE().getUrl().replace(URL_PREFIX, "");
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
        dataModel.setProfileId(uuid.toString().replace("-", ""));
        dataModel.setProfileName(name);

        if (skinURL != null && !skinURL.isEmpty()) {
            DataModel texturesModel = new DataModel();
            TextureSourceModel skinModel = new TextureSourceModel();
            skinModel.setUrl(URL_PREFIX + skinURL);
            if (slimModel) {
                skinModel.setMetadata(new MetadataModel());
            }

            texturesModel.setSKIN(skinModel);

            if (capeURL != null && !capeURL.isEmpty()) {
                TextureSourceModel capeModel = new TextureSourceModel();
                capeModel.setUrl(URL_PREFIX + capeURL);
                texturesModel.setCAPE(capeModel);
            }

            dataModel.setTextures(texturesModel);
        }

        String json = new Gson().toJson(dataModel);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.slimModel, skinURL, capeURL);
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

        return new Gson().fromJson(rawJson, SkinModel.class);
    }
}
