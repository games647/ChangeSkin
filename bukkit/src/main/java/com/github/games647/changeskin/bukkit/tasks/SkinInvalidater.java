package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import org.bukkit.entity.Player;

public class SkinInvalidater implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final Player invoker;

    public SkinInvalidater(ChangeSkinBukkit plugin, Player invoker) {
        this.plugin = plugin;
        this.invoker = invoker;
    }

    @Override
    public void run() {
        UserPreferences preferences = plugin.getStorage().getPreferences(invoker.getUniqueId());
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            plugin.sendMessage(invoker, "dont-have-skin");
        } else {
            plugin.sendMessage(invoker, "invalidate-request");

            SkinData skin = plugin.getCore().downloadSkin(ownedSkin.getUuid());
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
            plugin.getServer().getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, invoker, skin));
        }
    }
}
