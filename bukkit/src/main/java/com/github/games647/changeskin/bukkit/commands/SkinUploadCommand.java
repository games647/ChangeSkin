package com.github.games647.changeskin.bukkit.commands;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinUploader;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkinUploadCommand implements CommandExecutor {

    private final ChangeSkinBukkit plugin;

    public SkinUploadCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.sendMessage(sender, "upload-noargs");
        } else {
            String url = args[0];
            if (url.startsWith("http://") || url.startsWith("https://")) {
                List<Account> accounts = plugin.getCore().getUploadAccounts();
                if (accounts.isEmpty()) {
                    plugin.sendMessage(sender, "no-accounts");
                } else {
                    Account uploadAccount = accounts.get(0);
                    SkinUploader skinUploader = new SkinUploader(plugin, sender, uploadAccount, url);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, skinUploader);
                }
            } else {
                plugin.sendMessage(sender, "no-valid-url");
            }
        }

        return true;
    }
}