package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.messages.ForwardMessage;
import com.github.games647.changeskin.core.messages.PermissionResultMessage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.event.EventHandler;

public class MessageListener extends AbstractSkinListener {

    public MessageListener(ChangeSkinBungee plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent messageEvent) {
        String channel = messageEvent.getTag();
        if (messageEvent.isCancelled() || !plugin.getName().equals(channel)) {
            return;
        }

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(messageEvent.getData());
        String subChannel = dataInput.readUTF();

        ProxiedPlayer invoker = (ProxiedPlayer) messageEvent.getReceiver();
        if ("PermissionResult".equals(subChannel)) {
            PermissionResultMessage message = new PermissionResultMessage();
            message.readFrom(dataInput);
            if (message.isSuccess()) {
                onPermissionSuccess(message, invoker);
            } else {
                plugin.sendMessage(invoker, "no-permission");
            }
        } else if ("ForwardCmd".equals(subChannel)) {
            onCommandForward(invoker, dataInput);
        }
    }

    private void onPermissionSuccess(PermissionResultMessage message, ProxiedPlayer invoker) {
        SkinModel targetSkin = message.getSkin();

        UUID receiverUUID = message.getReceiverUUID();
        ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(receiverUUID);
        if (receiver == null || !receiver.isConnected()) {
            //receiver is not online cancel
            return;
        }

        //add cooldown
        core.getCooldownService().trackPlayer(invoker.getUniqueId());
        //Save the target uuid from the requesting player source
        final UserPreference preferences = core.getStorage().getPreferences(receiver.getUniqueId());
        preferences.setTargetSkin(targetSkin);

        ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
            if (core.getStorage().save(targetSkin)) {
                core.getStorage().save(preferences);
            }
        });
        
        if (core.getConfig().getBoolean("instantSkinChange")) {
            plugin.applySkin(receiver, targetSkin);
            plugin.sendMessage(invoker, "skin-changed");
        } else {
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }

    private void onCommandForward(CommandSender invoker, ByteArrayDataInput dataInput) {
        ForwardMessage message = new ForwardMessage();
        message.readFrom(dataInput);

        if (message.isOP() && message.isSource()) {
            //bukkit op and it won't run as bungee console
            invoker.addGroups(plugin.getName() + "-OP");
        }

        String line = message.getCommandName() + ' ' + message.getArgs();
        if (message.isSource()) {
            //the player is the actual invoker other it's the console
            ProxyServer.getInstance().getPluginManager().dispatchCommand(invoker, line);
        } else {
            CommandSender console = ProxyServer.getInstance().getConsole();
            ProxyServer.getInstance().getPluginManager().dispatchCommand(console, line);
        }
    }
}
