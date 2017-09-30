package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.serializer.TextSerializers;

public class SkinUploader extends SharedUploader {

    private final CommandSource invoker;

    public SkinUploader(ChangeSkinSponge plugin, CommandSource invoker, Account owner, String url, String name) {
        super(plugin.getCore(), owner, url);

        this.invoker = invoker;
    }

    public SkinUploader(ChangeSkinSponge plugin, CommandSource invoker, Account owner, String url) {
        this(plugin, invoker, owner, url, null);
    }

    @Override
    public void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(localeMessage));
    }
}
