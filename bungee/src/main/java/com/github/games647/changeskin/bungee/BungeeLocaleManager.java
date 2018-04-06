package com.github.games647.changeskin.bungee;

import com.github.games647.changeskin.core.LocaleManager;

import java.nio.file.Path;
import java.util.Locale;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

import org.slf4j.Logger;

public class BungeeLocaleManager extends LocaleManager<CommandSender> {

    public BungeeLocaleManager(Logger logger, Path dataFolder) {
        super(logger, dataFolder);
    }

    @Override
    protected Locale getLocale(CommandSender receiver) {
        if (receiver instanceof ProxiedPlayer) {
            return ((ProxiedPlayer) receiver).getLocale();
        }

        return Locale.getDefault();
    }

    @Override
    protected void sendLocalizedMessage(CommandSender receiver, String json) {
        receiver.sendMessage(ComponentSerializer.parse(json));
    }
}
