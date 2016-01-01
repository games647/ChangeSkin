package com.github.games647.changeskin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;
import com.github.games647.changeskin.tasks.SkinRefetcher;
import com.google.common.collect.Multimap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerInfoListener extends PacketAdapter {

    private final ChangeSkin plugin;

    public PlayerInfoListener(ChangeSkin plugin) {
        super(params().plugin(plugin).types(PacketType.Play.Server.PLAYER_INFO).optionAsync());

        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        EnumWrappers.PlayerInfoAction infoAction = packet.getPlayerInfoAction().read(0);
        if (infoAction != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
            return;
        }

        Player player = packetEvent.getPlayer();
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);

        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        //skin isn't already downloaded
        if (!plugin.getSkinCache().containsKey(player.getUniqueId())
                //no other skins was selected
                && !plugin.getUserPreferences().containsKey(player.getUniqueId())
                //player doesn't have a skin yet
                && !properties.containsKey("textures")) {
            SkinRefetcher skinRefetcher = new SkinRefetcher(plugin, player, properties, packetEvent);
            Bukkit.getScheduler().runTask(plugin, skinRefetcher);

//            AsyncMarker asyncMarker = packetEvent.getAsyncMarker();
//            asyncMarker.incrementProcessingDelay();
        }
    }
}
