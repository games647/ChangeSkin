package com.github.games647.changeskin.core;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinModel;
import com.github.games647.changeskin.core.model.TextureSourceModel;
import com.github.games647.changeskin.core.model.DataModel;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SkinData {

    private int skinId = -1;

    private final long timestamp;
    private final UUID uuid;
    private final String name;
    private final String skinURL;
    private final String capeURL;
    private final String encodedSignature;

    private final transient String encodedData;

    public SkinData(int skinId, long timestamp, UUID uuid, String name
            , String skinURL, String capeURL, String signature) {
        this.skinId = skinId;

        this.timestamp = timestamp;
        this.uuid = uuid;
        this.name = name;
        this.skinURL = skinURL;
        this.capeURL = capeURL;
        this.encodedSignature = signature;
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
            this.skinURL = textures.getSKIN().getUrl();
        } else {
            this.skinURL = null;
        }

        if (textures != null && textures.getCAPE() != null) {
            this.capeURL = textures.getCAPE().getUrl();
        } else {
            this.capeURL = null;
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

        if (skinURL != null) {
            DataModel texturesModel = new DataModel();
            TextureSourceModel skinModel = new TextureSourceModel();
            texturesModel.setSKIN(skinModel);
            skinModel.setUrl(skinURL);

            if (capeURL != null) {
                TextureSourceModel capeModel = new TextureSourceModel();
                capeModel.setUrl(capeURL);
                texturesModel.setCAPE(capeModel);
            }

            dataModel.setTextures(texturesModel);
        }

        String json = new Gson().toJson(dataModel);
        return BaseEncoding.base64().encode(json.getBytes(StandardCharsets.UTF_8));
    }

    private SkinModel deserializeData(String encodedData) {
        byte[] data = BaseEncoding.base64().decode(encodedData);
        String rawJson = new String(data, StandardCharsets.UTF_8);

        return new Gson().fromJson(rawJson, SkinModel.class);
    }
}
