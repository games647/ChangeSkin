package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.core.LocaleManager;

import java.nio.file.Path;
import java.util.Locale;

import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class BukkitLocaleManager extends LocaleManager<CommandSender> {

    public BukkitLocaleManager(Logger logger, Path dataFolder) {
        super(logger, dataFolder);
    }

    @Override
    public Locale getLocale(CommandSender receiver) {
        if (receiver instanceof Player) {
            String locale = ((Player) receiver).getLocale();
            return Locale.forLanguageTag(locale.replace('_', '-'));
        }

        return Locale.getDefault();
    }

    @Override
    protected void sendLocalizedMessage(CommandSender receiver, String json) {
        receiver.spigot().sendMessage(ComponentSerializer.parse(json));
    }
}
