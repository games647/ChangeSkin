package com.github.games647.changeskin.core.model.skin;

import java.util.UUID;

public class TexturesModel {

    private UUID id;
    private String name;
    private SkinProperties[] properties;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkinProperties[] getProperties() {
        return properties;
    }
}
