package com.github.games647.changeskin;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SkinDownloader implements Runnable {

    private final ChangeSkin plugin;
    private final Player source;
    private final UUID targetSkin;

    public SkinDownloader(ChangeSkin plugin, Player source, UUID targetSkin) {
        this.plugin = plugin;
        this.source = source;
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
        source.sendMessage(ChatColor.DARK_GREEN + "Your skin was changed. Please relogin to see the changes");
    }
}
