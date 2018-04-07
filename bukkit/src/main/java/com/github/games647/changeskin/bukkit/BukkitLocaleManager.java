package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.core.LocaleManager;

import java.nio.file.Path;
import java.util.Locale;

import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

public class BukkitLocaleManager extends LocaleManager<CommandSender> {

    private static final boolean getLocaleAvailable;

    static {
        getLocaleAvailable = isMethodAvailable(Player.class, "getLocale");
    }

    public BukkitLocaleManager(Logger logger, Path dataFolder) {
        super(logger, dataFolder);
    }

    @Override
    protected void sendLocalizedMessage(CommandSender receiver, String json) {
        receiver.spigot().sendMessage(ComponentSerializer.parse(json));
    }

    @Override
    public Locale getLocale(CommandSender receiver) {
        if (receiver instanceof Player) {
            Player player = (Player) receiver;
            String playerLocale = getPlayerLocale(player);
            if (playerLocale != null) {
                //convert from en_US to en-US which can Java detect
                return Locale.forLanguageTag(playerLocale.replace('_', '-'));
            }
        }

        return Locale.getDefault();
    }

    private String getPlayerLocale(Player player) {
        if (getLocaleAvailable) {
            //this method was only added in 1.12
            return player.getLocale();
        } else {
            return player.spigot().getLocale();
        }
    }

    private static boolean isMethodAvailable(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            clazz.getDeclaredMethod(methodName, parameters);
            return true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            return false;
        }
    }
}
