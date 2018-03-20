package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.messages.ChannelMessage;
import com.github.games647.changeskin.core.messages.CheckPermMessage;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedApplier;

import java.util.UUID;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class SkinApplier extends SharedApplier {

    private final ChangeSkinBungee plugin;
    private final ProxiedPlayer receiver;

    private final CommandSender invoker;

    private final boolean bukkitOp;

    public SkinApplier(ChangeSkinBungee plugin, CommandSender invoker, ProxiedPlayer receiver, SkinModel targetSkin
            , boolean bukkitOp, boolean keepSkin) {
        super(plugin.getCore(), targetSkin, keepSkin);

        this.plugin = plugin;
        this.receiver = receiver;
        this.invoker = invoker;

        this.bukkitOp = bukkitOp;
    }

    @Override
    public void run() {
        if (!isConnected()) {
            return;
        }

        UUID receiverUUID = receiver.getUniqueId();
        if (invoker instanceof ProxiedPlayer) {
            if (targetSkin != null && core.getConfig().getBoolean("bukkit-permissions")) {
                Server server = ((ProxiedPlayer) invoker).getServer();
                boolean skinPerm = core.getConfig().getBoolean("skinPermission");

                ChannelMessage message = new CheckPermMessage(targetSkin, receiverUUID, skinPerm, bukkitOp);
                plugin.sendPluginMessage(server, message);
                return;
            }

            //uuid was successful resolved, we could now make a cooldown check
            core.getCooldownService().trackPlayer(((ProxiedPlayer) invoker).getUniqueId());
        }

        //check if that specific player is online
        UserPreference preferences = plugin.getLoginSession(receiver.getPendingConnection());
        if (preferences == null) {
            preferences = plugin.getStorage().getPreferences(receiverUUID);
        }

        save(preferences);
        applySkin();
    }

    @Override
    protected boolean isConnected() {
        return receiver.isConnected();
    }

    @Override
    protected void applyInstantUpdate() {
        plugin.getApi().applySkin(receiver, targetSkin);
    }

    @Override
    protected void sendMessage(String key) {
        plugin.sendMessage(invoker, key);
    }

    @Override
    protected void runAsync(Runnable runnable) {
        runnable.run();
    }
}
