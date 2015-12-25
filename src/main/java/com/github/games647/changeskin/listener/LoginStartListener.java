package com.github.games647.changeskin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.async.AsyncMarker;
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

public class LoginStartListener extends PacketAdapter {

    private final ChangeSkin plugin;

    public LoginStartListener(ChangeSkin plugin) {
        super(params().plugin(plugin).types(PacketType.Play.Server.PLAYER_INFO).optionAsync());

        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(final PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        EnumWrappers.PlayerInfoAction infoAction = packet.getPlayerInfoAction().read(0);
        if (infoAction != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
            return;
        }

        Player player = packetEvent.getPlayer();
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);

        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        if (!properties.containsKey("textures")) {
            AsyncMarker asyncMarker = packetEvent.getAsyncMarker();
            SkinRefetcher skinRefetcher = new SkinRefetcher(plugin, player, properties, packetEvent);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, skinRefetcher);

            asyncMarker.incrementProcessingDelay();
        }
    }
}
