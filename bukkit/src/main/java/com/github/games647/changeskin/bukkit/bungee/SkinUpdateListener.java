package com.github.games647.changeskin.bukkit.bungee;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.task.SkinApplier;
import com.github.games647.changeskin.core.message.SkinUpdateMessage;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class SkinUpdateListener implements PluginMessageListener {

    private final ChangeSkinBukkit plugin;

    public SkinUpdateListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        SkinUpdateMessage updateMessage = new SkinUpdateMessage();
        updateMessage.readFrom(dataInput);

        String playerName = updateMessage.getPlayerName();
        Player receiver = Bukkit.getPlayerExact(playerName);
        if (receiver != null) {
            Bukkit.getScheduler().runTask(plugin, new SkinApplier(plugin, player, receiver, null, false));
        }
    }
}
