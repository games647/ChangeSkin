package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.command.CommandSource;

public class SkinUploader extends SharedUploader {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;

    public SkinUploader(ChangeSkinSponge plugin, CommandSource invoker, Account owner, String url, String name) {
        super(plugin.getCore(), owner, url);

        this.plugin = plugin;
        this.invoker = invoker;
    }

    public SkinUploader(ChangeSkinSponge plugin, CommandSource invoker, Account owner, String url) {
        this(plugin, invoker, owner, url, null);
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(invoker, id, args);
    }
}
