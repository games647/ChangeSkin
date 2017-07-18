package com.github.games647.changeskin.bungee.commands;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.tasks.SkinUploader;
import com.github.games647.changeskin.core.model.mojang.auth.Account;

import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class SkinUploadCommand extends Command {

    private final ChangeSkinBungee plugin;

    public SkinUploadCommand(ChangeSkinBungee plugin) {
        super("skin-upload", plugin.getDescription().getName().toLowerCase() + ".command.skinupload", "skinupload");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
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
                    Runnable skinUploader = new SkinUploader(plugin, sender, uploadAccount, url);
                    ProxyServer.getInstance().getScheduler().runAsync(plugin, skinUploader);
                }
            } else {
                plugin.sendMessage(sender, "no-valid-url");
            }
        }
    }
}
