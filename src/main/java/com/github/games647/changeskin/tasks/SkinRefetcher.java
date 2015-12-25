package com.github.games647.changeskin.tasks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;
import com.google.common.collect.Multimap;

import java.util.UUID;

import org.bukkit.entity.Player;

public class SkinRefetcher implements Runnable {

    private final ChangeSkin plugin;

    private final String playerName;
    private final UUID uniqueId;
    private final AsyncMarker asyncMarker;
    private final Multimap<String, WrappedSignedProperty> properties;
    private final PacketEvent packetEvent;

    public SkinRefetcher(ChangeSkin plugin, Player player
            , Multimap<String, WrappedSignedProperty> properties, PacketEvent packetEvent) {
        this.plugin = plugin;
        this.playerName = player.getName();
        this.uniqueId = player.getUniqueId();
        this.asyncMarker = packetEvent.getAsyncMarker();
        this.properties = properties;
        this.packetEvent = packetEvent;
    }

    @Override
    public void run() {
        try {
            UUID ownerUUID = plugin.getUuidCache().get(playerName);
            if (ownerUUID == null) {
                ownerUUID = plugin.getUUID(playerName);
                if (ownerUUID != null) {
                    plugin.getUuidCache().put(playerName, ownerUUID);
                }
            }

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
}
