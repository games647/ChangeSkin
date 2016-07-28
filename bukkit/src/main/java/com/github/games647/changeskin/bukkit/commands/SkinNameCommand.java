package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkinNameCommand implements CommandExecutor {

    private final ChangeSkinBukkit plugin;

    public SkinNameCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}