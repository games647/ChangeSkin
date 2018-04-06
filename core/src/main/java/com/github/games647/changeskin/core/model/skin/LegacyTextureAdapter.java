package com.github.games647.changeskin.core.model.skin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class LegacyTextureAdapter implements JsonSerializer<TextureModel> {

    @Override
    public JsonElement serialize(TextureModel src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject skin = new JsonObject();
        if (src.isSlim()) {
            JsonObject metadata = new JsonObject();
            metadata.add("model", new JsonPrimitive("slim"));
            skin.add("metadata", metadata);
        }

        skin.addProperty("url", src.getUrl());
        return skin;
    }
}
