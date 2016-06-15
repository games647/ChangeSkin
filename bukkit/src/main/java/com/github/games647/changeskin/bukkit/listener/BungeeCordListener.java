package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.SkinUpdater;
import com.github.games647.changeskin.core.SkinData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

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

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(message);
        String subchannel = dataInput.readUTF();

//        UUID receiverUUID = UUID.fromString(dataInput.readUTF());
//        Player receiver = Bukkit.getPlayer(receiverUUID);
//        if (receiver == null) {
//            plugin.getLogger().warning("BungeeCord requested Skin update, but receiver player isn't online");
//            return;
//        }

        String encodedData = dataInput.readUTF();
        if (encodedData.equalsIgnoreCase("null")) {
            Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, player, null));
            return;
        }


        String signature = dataInput.readUTF();
        
        SkinData skinData = new SkinData(encodedData, signature);
        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, null, player, skinData));
    }
}
