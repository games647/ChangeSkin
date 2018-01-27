package com.github.games647.changeskin.core.model.skin;

import com.github.games647.changeskin.core.CommonUtil;
import com.github.games647.changeskin.core.model.UUIDTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

public class SkinPropertyTest {

    private Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    @Test
    public void testSignatureSlim() throws Exception {
        Path stevePath = Paths.get(getClass().getResource("/skins/slimModel.json").toURI());
        String json = Files.readAllLines(stevePath).stream().collect(Collectors.joining());

        TexturesModel texturesModel = gson.fromJson(json, TexturesModel.class);
        assertThat(texturesModel.getId(), is(equalTo((CommonUtil.parseId("78c3a4e837e448189df8f9ce61c5efcc")))));
        assertThat(texturesModel.getName(), is(equalTo("Rashomon_")));

        SkinProperty property = texturesModel.getProperties()[0];
        assertThat(VerifyUtil.isValid(property.getValue(), property.getSignature()), is(equalTo(true)));
    }

    @Test
    public void testSignatureSteve() throws Exception {
        Path stevePath = Paths.get(getClass().getResource("/skins/steveModel.json").toURI());
        String json = Files.readAllLines(stevePath).stream().collect(Collectors.joining());

        TexturesModel texturesModel = gson.fromJson(json, TexturesModel.class);
        assertThat(texturesModel.getId(), is(equalTo((CommonUtil.parseId("0aaa2c13922a411bb6559b8c08404695")))));
        assertThat(texturesModel.getName(), is(equalTo("games647")));

        SkinProperty property = texturesModel.getProperties()[0];
        assertThat(VerifyUtil.isValid(property.getValue(), property.getSignature()), is(equalTo(true)));
    }
}
