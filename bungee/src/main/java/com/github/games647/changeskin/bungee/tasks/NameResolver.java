package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.shared.SharedNameResolver;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NameResolver extends SharedNameResolver {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final ProxiedPlayer player;

    private final boolean bukkitOp;
    private final boolean keepSkin;

    public NameResolver(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer targetPlayer, String targetName
            , boolean bukkitOp, boolean keepSkin) {
        super(plugin.getCore(), targetName, keepSkin);

        this.plugin = plugin;
        this.invoker = invoker;
        this.player = targetPlayer;

        this.bukkitOp = bukkitOp;
        this.keepSkin = keepSkin;
    }
    @Override
    public void sendMessageInvoker(String id, String... args) {
        if (invoker != null) {
            plugin.sendMessage(invoker, id, args);
        }
    }

    @Override
    protected boolean hasSkinPermission(UUID uuid) {
        if (invoker == null || !plugin.getCore().getConfig().getBoolean("skinPermission")) {
            return true;
        }

        return !plugin.checkPermission(invoker, uuid);
    }

    @Override
    protected void scheduleDownloader(UUID uuid) {
        //run this is the same thread
        new SkinDownloader(plugin, invoker, player, uuid, bukkitOp, keepSkin).run();
    }
}
