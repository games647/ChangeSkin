package com.github.games647.changeskin.core.model.skin;

import java.util.Collections;
import java.util.Map;

public class TextureModel {

    private static final String URL_PREFIX = "http://textures.minecraft.net/texture/";

    private final String url;
    private Map<String, String> metadata;

    public TextureModel(String shortUrl, boolean slimModel) {
        this.url = URL_PREFIX + shortUrl;

        if (slimModel) {
            metadata = Collections.singletonMap("model", "slim");
        }
    }

    public TextureModel(String shortUrl) {
        this(shortUrl, false);
    }

    public boolean isSlim() {
        return metadata != null && metadata.containsKey("model");
    }

    public String getUrl() {
        return url;
    }

    public String getShortUrl() {
        return url.replace(URL_PREFIX, "");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "url='" + url + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
