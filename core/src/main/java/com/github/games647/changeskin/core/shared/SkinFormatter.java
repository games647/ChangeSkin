package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.model.skin.MetadataModel;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.TextureModel;
import com.github.games647.changeskin.core.model.skin.TextureType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class SkinFormatter implements BiFunction<String, SkinModel, String> {

    @Override
    public String apply(String template, SkinModel skin) {
        if (template == null) {
            return null;
        }

        int rowId = skin.getRowId();
        UUID ownerId = skin.getProfileId();
        String ownerName = skin.getProfileName();
        long timeFetched = skin.getTimestamp();

        Map<TextureType, TextureModel> textures = skin.getTextures();
        Optional<TextureModel> skinTexture = Optional.ofNullable(textures.get(TextureType.SKIN));
        String skinUrl = skinTexture.map(TextureModel::getUrl).orElse("");
        String metadata = skinTexture.map(TextureModel::getMetadata).map(MetadataModel::getModel).orElse("");
        String capeUrl = Optional.ofNullable(textures.get(TextureType.CAPE)).map(TextureModel::getUrl).orElse("");

        String time = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeFetched), ZoneId.systemDefault()).toString();
        return template.replace("{0}", Integer.toString(rowId))
                .replace("{1}", ownerId.toString())
                .replace("{2}", ownerName)
                .replace("{3}", time)
                .replace("{4}", skinUrl)
                .replace("{5}", metadata)
                .replace("{6}", capeUrl);

    }
}
