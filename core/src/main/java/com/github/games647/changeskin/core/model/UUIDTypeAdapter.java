package com.github.games647.changeskin.core.model;

import com.github.games647.changeskin.core.CommonUtil;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

public class UUIDTypeAdapter extends TypeAdapter<UUID> {

    public void write(JsonWriter out, UUID value) throws IOException {
        out.value(value.toString().replace("-", ""));
    }

    public UUID read(JsonReader in) throws IOException {
        return CommonUtil.parseId(in.nextString());
    }
}
