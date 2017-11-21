package com.github.games647.changeskin.core.model.skin;

import com.google.common.base.Objects;

public class MetadataModel {

    private final String model = "slim";

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("model", model)
                .toString();
    }
}
