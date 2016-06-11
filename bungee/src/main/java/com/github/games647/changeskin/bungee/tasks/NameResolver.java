package com.github.games647.changeskin.bungee.tasks;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;

import java.util.UUID;
import java.util.logging.Level;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NameResolver implements Runnable {

    private final ChangeSkinBungee plugin;
    private final CommandSender invoker;
    private final String targetName;
    private final ProxiedPlayer player;

    public NameResolver(ChangeSkinBungee plugin, CommandSender invoker, String targetName, ProxiedPlayer targetPlayer) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.player = targetPlayer;
    }

    @Override
    public void run() {
        UUID uuid = plugin.getCore().getUuidCache().get(targetName);
        if (uuid == null) {
            SkinData targetSkin = plugin.getStorage().getSkin(targetName);
            if (targetSkin != null) {
                onNameResolveDatabase(targetSkin);

                return;
            }

            try {
                uuid = plugin.getCore().getUUID(targetName);
                if (uuid == null) {
                    if (invoker != null) {
                        plugin.sendMessage(invoker, "no-resolve");
                    }
                } else {
                    plugin.getCore().getUuidCache().put(targetName, uuid);
                    onNameResolve(uuid);
                }
            } catch (NotPremiumException notPremiumEx) {
                plugin.getLogger().log(Level.FINE, "Requested not premium", notPremiumEx);
                if (invoker != null) {
                    plugin.sendMessage(invoker, "not-premium");
                }
            } catch (RateLimitException rateLimitEx) {
                plugin.getLogger().log(Level.SEVERE, "UUID Rate Limit reached", rateLimitEx);
                if (invoker != null) {
                    plugin.sendMessage(invoker, "rate-limit");
                }
            }
        } else {
            onNameResolve(uuid);
        }
    }

    private void onNameResolveDatabase(SkinData targetSkin) {
        UUID uuid = targetSkin.getUuid();
        if (invoker != null) {
            plugin.sendMessage(invoker, "uuid-resolved");
            if (plugin.getConfiguration().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid)) {
                plugin.sendMessage(invoker, "no-permission");
                return;
            }

            plugin.sendMessage(invoker, "skin-downloading");
        }

        new SkinUpdater(plugin, invoker, player, targetSkin).run();
    }

    private void onNameResolve(UUID uuid) {
        if (uuid != null) {
            if (invoker != null) {
                plugin.sendMessage(invoker, "uuid-resolved");
                if (plugin.getConfiguration().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid)) {
                    plugin.sendMessage(invoker, "no-permission");
                    return;
                }

                plugin.sendMessage(invoker, "skin-downloading");
            }

            //run this is the same thread
            new SkinDownloader(plugin, invoker, player, uuid).run();
        }
    }
}
