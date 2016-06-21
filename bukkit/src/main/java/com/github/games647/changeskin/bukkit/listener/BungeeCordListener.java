package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinUpdater;
import com.github.games647.changeskin.core.SkinData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeCordListener implements PluginMessageListener {

    private final ChangeSkinBukkit plugin;

    public BungeeCordListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(plugin.getName())) {
            return;
        }

        plugin.getLogger().log(Level.INFO, "Received instant update request from BungeeCord. "
                + "This request should only be send if the command /setskin was invoked");

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subchannel = dataInput.readUTF();

        String encodedData = dataInput.readUTF();
        if (encodedData.equalsIgnoreCase("null")) {
            Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, player, null));
            return;
        }

        Player receiver = player;
        try {
            String playerName = dataInput.readUTF();
            receiver = Bukkit.getPlayerExact(playerName);
            plugin.getLogger().log(Level.INFO, "Instant update for ", playerName);
        } catch (Exception ex) {
            plugin.getLogger().warning("You are using an outdated ChangeSkin spigot version");
        }

        String signature = dataInput.readUTF();
        
        SkinData skinData = new SkinData(encodedData, signature);
        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, receiver, skinData));
    }
}
