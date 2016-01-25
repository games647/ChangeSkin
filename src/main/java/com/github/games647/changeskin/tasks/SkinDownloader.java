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
    private final UUID targetSkin;

    public SkinDownloader(ChangeSkin plugin, CommandSender invoker, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
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

        //if user is online notify the player
        if (invoker != null) {
            invoker.sendMessage(ChatColor.DARK_GREEN + "Skin was changed. Relogin to see the changes");
            if (plugin.getConfig().getBoolean("instantSkinChange") && invoker instanceof Player) {
                plugin.getServer().getScheduler().runTask(plugin, new ApplySkin(plugin, (Player) invoker));
            }
        }
    }
}
