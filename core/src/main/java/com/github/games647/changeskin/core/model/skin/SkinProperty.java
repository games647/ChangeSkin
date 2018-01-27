package com.github.games647.changeskin.core.model.skin;

import com.google.common.base.Objects;

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
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("value", value)
                .add("signature", signature)
                .toString();
    }
}
