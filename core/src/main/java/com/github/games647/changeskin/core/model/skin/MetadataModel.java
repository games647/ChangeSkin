package com.github.games647.changeskin.core.model.skin;

public class MetadataModel {

    private final String model = "slim";

    public String getModel() {
        return model;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "model='" + model + '\'' +
                '}';
    }
}
