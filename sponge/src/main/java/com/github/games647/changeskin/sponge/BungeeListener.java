package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedBungeeListener;
import com.github.games647.changeskin.sponge.tasks.SkinApplier;
import com.google.inject.Inject;

import java.util.UUID;

import org.spongepowered.api.Platform.Type;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding.RawDataChannel;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.scheduler.Task;

public class BungeeListener extends SharedBungeeListener<Player> implements RawDataListener {

    private final ChangeSkinSponge plugin;

    @Inject
    @ChannelId("changeskin")
    private RawDataChannel pluginChannel;

    @Inject
    BungeeListener(ChangeSkinSponge plugin) {
        super(plugin);

        this.plugin = plugin;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Type side) {
        Player player = (Player) connection;
        handlePayload(player, data.array());
    }

    @Override
    protected void sendMessage(Player player, String channel, byte[] data) {
        pluginChannel.sendTo(player, out -> out.writeByteArray(data));
    }

    @Override
    protected void runUpdater(Player receiver, SkinModel targetSkin) {
        Runnable skinUpdater = new SkinApplier(plugin, null, receiver, targetSkin, false);
        Task.builder().execute(skinUpdater).submit(plugin);
    }

    @Override
    protected Player getPlayerExact(String name) {
        return Sponge.getServer().getPlayer(name).orElse(null);
    }

    @Override
    protected UUID getUUID(Player player) {
        return player.getUniqueId();
    }

    @Override
    protected boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    protected boolean checkWhitelistPermission(Player player, UUID targetUUID) {
        return plugin.checkWhitelistPermission(player, targetUUID, false);
    }
}
