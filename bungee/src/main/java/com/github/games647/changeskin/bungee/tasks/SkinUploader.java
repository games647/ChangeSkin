package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;

import net.md_5.bungee.api.CommandSender;

public class SkinUploader extends SharedUploader {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(invoker, id, args);
    }
}
