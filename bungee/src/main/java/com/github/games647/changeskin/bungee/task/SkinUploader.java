package com.github.games647.changeskin.bungee.task;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.task.SharedUploader;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.chat.ComponentSerializer;

public class SkinUploader extends SharedUploader {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    protected void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(ComponentSerializer.parse(localeMessage));
    }

    @Override
    protected String getUploadedMessage() {
        return plugin.getLocaleManager().getLocalizedMessage(invoker, "skin-uploaded");
    }
}
