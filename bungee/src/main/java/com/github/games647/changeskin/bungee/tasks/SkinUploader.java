package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.SharedUploader;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class SkinUploader extends SharedUploader {

    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.invoker = invoker;
    }

    @Override
    protected void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(TextComponent.fromLegacyText(localeMessage));
    }
}
