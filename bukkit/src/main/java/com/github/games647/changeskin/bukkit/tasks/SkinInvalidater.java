package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkinInvalidater implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;

    public SkinInvalidater(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        SkinData ownedSkin = preferences.getTargetSkin();
        if (ownedSkin == null) {
            plugin.sendMessage(invoker, "dont-have-skin");
        } else {
            plugin.sendMessage(invoker, "invalidate-request");

            SkinData skin = plugin.getCore().getMojangSkinApi().downloadSkin(ownedSkin.getUuid());
            plugin.getCore().getUuidCache().put(skin.getName(), skin.getUuid());
            plugin.getServer().getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, receiver, skin));
        }
    }
}
