package com.github.games647.changeskin.core.shared;

import com.github.games647.changeskin.core.model.skin.MetadataModel;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.model.skin.TextureModel;
import com.github.games647.changeskin.core.model.skin.TextureType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class SkinFormatter implements BiFunction<String, SkinModel, String> {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault());

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
        Optional<TextureModel> capeTexture = Optional.ofNullable(textures.get(TextureType.CAPE));

        String skinUrl = skinTexture.map(TextureModel::getShortUrl).orElse("");
        String slimModel = skinTexture.map(TextureModel::getMetadata).map(MetadataModel::getModel).orElse("Steve");

        String capeUrl = capeTexture.map(TextureModel::getShortUrl).orElse(" - ");

        String timeFormat = timeFormatter.format(Instant.ofEpochMilli(timeFetched));
        return template.replace("{0}", Integer.toString(rowId))
                .replace("{1}", ownerId.toString())
                .replace("{2}", ownerName)
                .replace("{3}", timeFormat)
                .replace("{4}", skinUrl)
                .replace("{5}", slimModel)
                .replace("{6}", capeUrl);
    }
}
