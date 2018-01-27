package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.google.common.base.Joiner;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
        if (sender instanceof Player) {
            proxy = (Player) sender;
        } else {
            Optional<? extends Player> optPlayer = Bukkit.getOnlinePlayers().stream().findAny();
            if (!optPlayer.isPresent()) {
                sender.sendMessage(ChatColor.DARK_RED + "No player is online to forward this message to Bungee");
                return;
            }

            proxy = optPlayer.get();
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ForwardCmd");
        out.writeUTF(commandName);
        out.writeUTF(Joiner.on(' ').join(args));
        out.writeBoolean(sender instanceof Player);
        out.writeBoolean(sender.isOp());

        proxy.sendPluginMessage(plugin, plugin.getName(), out.toByteArray());
    }
}
