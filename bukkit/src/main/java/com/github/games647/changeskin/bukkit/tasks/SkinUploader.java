package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;

import org.bukkit.command.CommandSender;

public class SkinUploader extends SharedUploader {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBukkit plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        plugin.sendMessage(invoker, id, args);
    }
}
