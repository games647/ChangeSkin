package com.github.games647.changeskin.core.model.skin;

import java.util.Arrays;
import java.util.UUID;

public class TexturesModel {

    private UUID id;
    private String name;
    private SkinProperty[] properties;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SkinProperty[] getProperties() {
        return Arrays.copyOf(properties, properties.length);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "id=" + id +
                ", name='" + name + '\'' +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}
