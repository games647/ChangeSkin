package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.messages.ChannelMessage;
import com.github.games647.changeskin.core.messages.ForwardMessage;
import com.google.common.base.Joiner;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class AbstractForwardCommand implements CommandExecutor {

    protected final ChangeSkinBukkit plugin;

    public AbstractForwardCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    protected void onBungeeCord(CommandSender sender, String commandName, String... args) {
        Player proxy;
        boolean isPlayer = sender instanceof Player;
        if (isPlayer) {
            proxy = (Player) sender;
        } else {
            Optional<? extends Player> optPlayer = Bukkit.getOnlinePlayers().stream().findAny();
            if (!optPlayer.isPresent()) {
                sender.sendMessage(ChatColor.DARK_RED + "No player is online to forward this message to Bungee");
                return;
            }

            proxy = optPlayer.get();
        }

        ChannelMessage message = new ForwardMessage(commandName, Joiner.on(' ').join(args), isPlayer, sender.isOp());
        plugin.sendPluginMessage(proxy, message);
    }
}
