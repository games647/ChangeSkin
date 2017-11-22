package com.github.games647.changeskin.core.model.skin;

import com.google.common.base.Objects;

public class TextureModel {

    private static final String URL_PREFIX = "http://textures.minecraft.net/texture/";

    private MetadataModel metadata;
    private final String url;

    public TextureModel(String shortUrl, boolean slimModel) {
        this.url = URL_PREFIX + shortUrl;

        if (slimModel) {
            metadata = new MetadataModel();
        }
    }

    public TextureModel(String shortUrl) {
        this(shortUrl, false);
    }

    /**
     * @return can be null if not slim or this is a cape
     */
    public MetadataModel getMetadata() {
        return metadata;
    }

    public String getUrl() {
        return url;
    }

    public String getShortUrl() {
        return url.replace(URL_PREFIX, "");
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("url", url)
                .add("metadata", metadata)
                .toString();
    }
}
