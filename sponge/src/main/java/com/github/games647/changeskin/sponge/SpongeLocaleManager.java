package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.LocaleManager;

import java.nio.file.Path;
import java.util.Locale;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SpongeLocaleManager extends LocaleManager<CommandSource> {

    public SpongeLocaleManager(Logger logger, Path dataFolder) {
        super(logger, dataFolder);
    }

    @Override
    protected Locale getLocale(CommandSource receiver) {
        return receiver.getLocale();
    }

    @Override
    protected void sendLocalizedMessage(CommandSource receiver, String json) {
        receiver.sendMessage(TextSerializers.JSON.deserialize(json));
    }
}
