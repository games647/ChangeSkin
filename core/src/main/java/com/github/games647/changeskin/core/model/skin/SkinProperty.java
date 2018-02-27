package com.github.games647.changeskin.core.model.skin;

public class SkinProperty {

    public static final String SKIN_KEY = "textures";

    private final String name = SKIN_KEY;

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
