package com.github.games647.changeskin.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;
import com.google.common.collect.Multimap;

import java.util.UUID;

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

        final UUID uniqueId = player.getUniqueId();
        final Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        if (!properties.containsKey("textures")) {
            final AsyncMarker asyncMarker = packetEvent.getAsyncMarker();
            final String playerName = player.getName();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        UUID ownerUUID = plugin.getUUID(playerName);
                        if (ownerUUID != null) {
                            WrappedSignedProperty cachedSkin = plugin.getSkinCache().get(ownerUUID);
                            if (cachedSkin == null) {
                                cachedSkin = plugin.downloadSkin(ownerUUID);
                                if (cachedSkin != null) {
                                    plugin.getSkinCache().put(ownerUUID, cachedSkin);
                                    plugin.getUserPreferences().put(uniqueId, ownerUUID);
                                }
                            }

                            synchronized (asyncMarker.getProcessingLock()) {
                                properties.put("textures", cachedSkin);
                            }
                        }
                    } finally {
                        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
                        protocolManager.getAsynchronousManager().signalPacketTransmission(packetEvent);
                    }
                }
            });

            asyncMarker.incrementProcessingDelay();
        }
    }
}
