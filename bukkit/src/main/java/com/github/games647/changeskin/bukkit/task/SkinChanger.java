package com.github.games647.changeskin.bukkit.task;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.task.SharedSkinChanger;

import org.bukkit.command.CommandSender;

public class SkinChanger extends SharedSkinChanger {

    private final CommandSender invoker;

    public SkinChanger(ChangeSkinBukkit plugin, Account owner, String url, String oldSkinUrl, CommandSender invoker) {
        super(plugin.getCore(), owner, url, oldSkinUrl);

        this.invoker = invoker;
    }

    @Override
    protected void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(localeMessage);
    }
}
