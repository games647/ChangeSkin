package com.github.games647.changeskin.core.model;

import java.util.Objects;
import java.util.UUID;

public class GameProfile {

    private UUID id;
    private String name;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof GameProfile) {
            GameProfile that = (GameProfile) other;
            return Objects.equals(id, that.id);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
