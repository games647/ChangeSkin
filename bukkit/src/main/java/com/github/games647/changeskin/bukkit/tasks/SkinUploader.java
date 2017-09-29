package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.mojang.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;

import org.bukkit.command.CommandSender;

public class SkinUploader extends SharedUploader {

    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBukkit plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.invoker = invoker;
    }

    @Override
    public void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(localeMessage);
    }
}
