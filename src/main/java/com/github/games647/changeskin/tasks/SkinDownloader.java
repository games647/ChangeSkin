package com.github.games647.changeskin.tasks;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinDownloader implements Runnable {

    private final ChangeSkin plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final UUID targetSkin;

    public SkinDownloader(ChangeSkin plugin, CommandSender invoker, Player receiver, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
    }

    @Override
    public void run() {
        WrappedSignedProperty cachedSkin = plugin.getSkinCache().get(targetSkin);
        if (cachedSkin == null) {
            cachedSkin = plugin.downloadSkin(targetSkin);
            if (cachedSkin != null) {
                plugin.getSkinCache().put(targetSkin, cachedSkin);
            }
        }

        if (receiver != null && plugin.getConfig().getBoolean("instantSkinChange")) {
            plugin.getServer().getScheduler().runTask(plugin, new SkinUpdater(plugin, receiver));
        } else if (invoker != null) {
            //if user is online notify the player
            invoker.sendMessage(ChatColor.DARK_GREEN + "Skin was changed. Relogin to see the changes");
        }
    }
}
