package com.github.games647.changeskin.core.model.skin;

public class SkinProperty {

    private final String name = "textures";

    private String value;
    private String signature;

    public String getValue() {
        return value;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
