package com.github.games647.changeskin;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import java.util.UUID;

public class SkinData {

    private final WrappedSignedProperty skinValue;
    private final UUID skinOwner;

    public SkinData(WrappedSignedProperty skinValue, UUID skinOwner) {
        this.skinValue = skinValue;
        this.skinOwner = skinOwner;
    }

    public WrappedSignedProperty getSkinValue() {
        return skinValue;
    }

    public UUID getSkinOwner() {
        return skinOwner;
    }
}
