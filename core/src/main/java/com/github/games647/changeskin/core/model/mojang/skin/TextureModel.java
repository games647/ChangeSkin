package com.github.games647.changeskin.core.model.mojang.skin;

public class TextureModel {

    private MetadataModel metadata;
    private String url;

    /**
     * @return can be null if not slim or this is a cape
     */
    public MetadataModel getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataModel metadata) {
        this.metadata = metadata;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
