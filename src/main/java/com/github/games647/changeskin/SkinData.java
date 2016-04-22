package com.github.games647.changeskin;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.model.SkinModel;
import com.github.games647.changeskin.model.TextureModel;
import com.github.games647.changeskin.model.TexturesModel;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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

        JSONObject data = deserializeData(encodedData);
        this.timestamp = (long) data.get("timestamp");
        this.uuid = ChangeSkin.parseId((String) data.get("profileId"));
        this.name = (String) data.get("profileName");

        JSONObject textures = (JSONObject) data.get("textures");

        JSONObject skinData = (JSONObject) textures.get("SKIN");
        if (skinData == null) {
            this.skinURL = null;
        } else {
            this.skinURL = (String) skinData.get("url");
        }

        JSONObject capeData = (JSONObject) textures.get("CAPE");
        if (capeData == null) {
            this.capeURL = null;
        } else {
            this.capeURL = (String) capeData.get("url");
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

    public WrappedSignedProperty convertToProperty() {
        return WrappedSignedProperty.fromValues("textures", encodedData, encodedSignature);
    }

    private String serializeData() {
        SkinModel dataModel = new SkinModel();
        dataModel.setTimestamp(timestamp);
        dataModel.setProfileId(uuid.toString().replace("-", ""));
        dataModel.setProfileName(name);

        if (skinURL != null) {
            TexturesModel texturesModel = new TexturesModel();
            TextureModel skinModel = new TextureModel();
            texturesModel.setSKIN(skinModel);
            skinModel.setUrl(skinURL);

            if (capeURL != null) {
                TextureModel capeModel = new TextureModel();
                capeModel.setUrl(capeURL);
                texturesModel.setCAPE(capeModel);
            }

            dataModel.setTextures(texturesModel);
        }

        String json = new Gson().toJson(dataModel);
        return BaseEncoding.base64().encode(json.getBytes(Charsets.UTF_8));
    }

    private JSONObject deserializeData(String encodedData) {
        byte[] data = BaseEncoding.base64().decode(encodedData);
        String rawJson = new String(data, Charsets.UTF_8);

        return (JSONObject) JSONValue.parse(rawJson);
    }
}
