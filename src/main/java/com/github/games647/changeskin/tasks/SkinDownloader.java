package com.github.games647.changeskin.tasks;

import com.github.games647.changeskin.ChangeSkin;
import com.github.games647.changeskin.SkinData;
import com.github.games647.changeskin.UserPreferences;

import java.util.UUID;
import org.bukkit.Bukkit;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinDownloader implements Runnable {

    protected final ChangeSkin plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final UUID targetUUID;

    public SkinDownloader(ChangeSkin plugin, CommandSender invoker, Player receiver, UUID targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetUUID = targetSkin;
    }

    @Override
    public void run() {
        SkinData skin = plugin.getStorage().getSkin(targetUUID, true);
        if (skin == null) {
            skin = plugin.downloadSkin(targetUUID);
            if (skin != null) {
                final SkinData newSkin = skin;
                plugin.getStorage().getSkinUUIDCache().put(targetUUID, newSkin);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(newSkin);
                    }
                });
            }
        }

        //Save the target uuid from the requesting player source
        final UserPreferences preferences = plugin.getStorage().getPreferences(receiver.getUniqueId(), false);
        preferences.setTargetSkin(skin);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getStorage().save(preferences);
            }
        });

        if (plugin.getConfig().getBoolean("instantSkinChange")) {
            plugin.getServer().getScheduler().runTask(plugin, new SkinUpdater(plugin, receiver));
        } else if (invoker != null) {
            //if user is online notify the player
            invoker.sendMessage(ChatColor.DARK_GREEN + "Skin was changed. Relogin to see the changes");
        }
    }
}
