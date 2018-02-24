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
    public void sendMessageInvoker(String id) {
        plugin.sendMessage(invoker, id);
    }

    @Override
    protected boolean hasSkinPermission(UUID uuid) {
        return invoker == null
                || !plugin.getCore().getConfig().getBoolean("skinPermission")
                || !plugin.checkWhitelistPermission(invoker, uuid, true);

    }

    @Override
    protected void scheduleDownloader(UUID uuid) {
        //run this is the same thread
        new SkinDownloader(plugin, invoker, player, uuid, bukkitOp, keepSkin).run();
    }
}
