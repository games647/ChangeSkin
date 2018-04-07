package com.github.games647.changeskin.bungee.task;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.auth.Account;
import com.github.games647.changeskin.core.shared.task.SharedUploader;

import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class SkinUploader extends SharedUploader {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;

    public SkinUploader(ChangeSkinBungee plugin, CommandSender invoker, Account owner, String url) {
        super(plugin.getCore(), owner, url);

        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    protected void sendMessageInvoker(String key) {
        plugin.sendMessage(invoker, key);
    }

    @Override
    protected void scheduleChangeTask(String oldSkinUrl) {
        Runnable task = new SkinChanger(plugin, owner, url, oldSkinUrl, invoker);
        ProxyServer.getInstance().getScheduler().schedule(plugin, task, 1, TimeUnit.MINUTES);
    }
}
