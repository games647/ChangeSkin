package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.shared.SharedNameResolver;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameResolver extends SharedNameResolver {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player player;

    public NameResolver(ChangeSkinBukkit plugin, CommandSender invoker, String targetName, Player player
            , boolean keepSkin) {
        super(plugin.getCore(), targetName, keepSkin);

        this.plugin = plugin;
        this.invoker = invoker;
        this.player = player;
    }

    @Override
    public void sendMessageInvoker(String id, String... args) {
        if (invoker != null) {
            plugin.sendMessage(invoker, id, args);
        }
    }

    @Override
    protected boolean hasSkinPermission(UUID uuid) {
        return invoker == null
                || !plugin.getConfig().getBoolean("skinPermission")
                || !plugin.checkPermission(invoker, uuid, true);

    }

    @Override
    protected void scheduleDownloader(UUID uuid) {
        //run this is the same thread
        new SkinDownloader(plugin, invoker, player, uuid, keepSkin).run();
    }
}
