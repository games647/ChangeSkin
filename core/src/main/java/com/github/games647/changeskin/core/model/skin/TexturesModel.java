package com.github.games647.changeskin.core.model.skin;

import com.google.common.base.Objects;

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
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("properties", Arrays.toString(properties))
                .toString();
    }
}
