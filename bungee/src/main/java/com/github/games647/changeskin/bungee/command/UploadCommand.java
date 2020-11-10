package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.bungee.task.SkinUploader;
import com.github.games647.changeskin.core.model.auth.Account;

import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class UploadCommand extends ChangeSkinCommand {

    private final ChangeSkinBungee plugin;

    public UploadCommand(ChangeSkinBungee plugin) {
        super("skin-upload", plugin.getName().toLowerCase() + ".command.skinupload", "skinupload");

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
