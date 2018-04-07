package com.github.games647.changeskin.bungee.task;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.task.SharedSkinChanger;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class SkinChanger extends SharedSkinChanger {

    private final CommandSender invoker;

    public SkinChanger(ChangeSkinBungee plugin, Account owner, String url, String oldSkinUrl, CommandSender invoker) {
        super(plugin.getCore(), owner, url, oldSkinUrl);

        this.invoker = invoker;
    }

    @Override
    protected void sendMessageInvoker(String localeMessage) {
        invoker.sendMessage(TextComponent.fromLegacyText(localeMessage));
    }
}
