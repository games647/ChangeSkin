package com.github.games647.changeskin.bukkit.task;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.task.SharedUploader;

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
    public void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(localeMessage);
    }

    @Override
    protected String getUploadedMessage() {
        return plugin.getLocaleManager().getLocalizedMessage(invoker, "skin-uploaded");
    }
}
