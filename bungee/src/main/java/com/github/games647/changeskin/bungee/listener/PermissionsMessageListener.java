package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

public class PermissionsMessageListener extends AbstractSkinListener {

    public PermissionsMessageListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent messageEvent) {
        byte[] data = messageEvent.getData();
        ByteArrayDataInput dataInput = ByteStreams.newDataInput(data);
        String subChannel = dataInput.readUTF();

        ProxiedPlayer invoker = (ProxiedPlayer) messageEvent.getReceiver();
        if ("PermissionsSuccess".equals(subChannel)) {
            int skinId = dataInput.readInt();

            String encodedData = dataInput.readUTF();
            String encodedSignature = dataInput.readUTF();
            final SkinData targetSkin = new SkinData(encodedData, encodedSignature);
            targetSkin.setSkinId(skinId);

            UUID receiverUUID = UUID.fromString(dataInput.readUTF());
            ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(receiverUUID);
            if (receiver == null || !receiver.isConnected()) {
                //receiver is not online cancel
                return;
            }

            //add cooldown
            plugin.addCooldown(invoker.getUniqueId());
            //Save the target uuid from the requesting player source
            final UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
            preferences.setTargetSkin(targetSkin);

            ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                @Override
                public void run() {
                    if (plugin.getStorage().save(targetSkin)) {
                        plugin.getStorage().save(preferences);
                    }
                }
            });

            if (plugin.getConfig().getBoolean("instantSkinChange")) {
                plugin.applySkin(receiver, targetSkin);
                plugin.sendMessage(receiver, "skin-changed");
            } else {
                plugin.sendMessage(invoker, "skin-changed-no-instant");
            }
        }
    }
}
