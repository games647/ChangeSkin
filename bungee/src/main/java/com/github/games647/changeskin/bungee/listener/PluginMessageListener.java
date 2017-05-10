package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

public class PluginMessageListener extends AbstractSkinListener {

    public PluginMessageListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent messageEvent) {
        String channel = messageEvent.getTag();
        if (messageEvent.isCancelled() || !plugin.getDescription().getName().equals(channel)) {
            return;
        }

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(messageEvent.getData());
        String subChannel = dataInput.readUTF();

        ProxiedPlayer invoker = (ProxiedPlayer) messageEvent.getReceiver();
        if ("PermissionsSuccess".equals(subChannel)) {
            onPermissionSuccess(dataInput, invoker);
        } else if ("PermissionsFailure".equals(subChannel)) {
            plugin.sendMessage(invoker, "no-permission");
        } else if ("ForwardCmd".equals(subChannel)) {
            onCommandForward(invoker, dataInput);
        }
    }

    private void onPermissionSuccess(ByteArrayDataInput dataInput, ProxiedPlayer invoker) {
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
        plugin.getCore().addCooldown(invoker.getUniqueId());
        //Save the target uuid from the requesting player source
        final UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
        preferences.setTargetSkin(targetSkin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            if (plugin.getStorage().save(targetSkin)) {
                plugin.getStorage().save(preferences);
            }
        });
        
        if (plugin.getConfig().getBoolean("instantSkinChange")) {
            plugin.applySkin(receiver, targetSkin);
            plugin.sendMessage(invoker, "skin-changed");
        } else {
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }

    private void onCommandForward(ProxiedPlayer invoker, ByteArrayDataInput dataInput) {
        String commandName = dataInput.readUTF();
        String args = dataInput.readUTF();
        boolean isSource = dataInput.readBoolean();
        boolean isOp = dataInput.readBoolean();

        if (isOp && isSource) {
            //bukkit op and it won't run as bungee console
            invoker.addGroups(plugin.getName() + "-OP");
        }

        if (isSource) {
            //the proxied player is the actual invoker other it's the console
            ProxyServer.getInstance().getPluginManager().dispatchCommand(invoker, commandName + ' ' + args);
        } else {
            CommandSender console = ProxyServer.getInstance().getConsole();
            ProxyServer.getInstance().getPluginManager().dispatchCommand(console, commandName + ' ' + args);
        }
    }
}
