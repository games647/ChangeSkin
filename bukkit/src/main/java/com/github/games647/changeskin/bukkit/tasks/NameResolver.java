package com.github.games647.changeskin.bukkit.tasks;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;

import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NameResolver implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final String targetName;
    private final Player player;

    public NameResolver(ChangeSkinBukkit plugin, CommandSender invoker, String targetName, Player targetPlayer) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.player = targetPlayer;
    }

    @Override
    public void run() {
        UUID uuid = plugin.getCore().getUuidCache().get(targetName);
        if (uuid == null) {
            if (plugin.getCore().getCrackedNames().containsKey(targetName)) {
                if (invoker != null) {
                    plugin.sendMessage(invoker, "not-premium");
                }

                return;
            }

            SkinData targetSkin = plugin.getStorage().getSkin(targetName);
            if (targetSkin != null) {
                onNameResolveDatabase(targetSkin);
                return;
            }

            try {
                uuid = plugin.getCore().getMojangSkinApi().getUUID(targetName);
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
                plugin.getCore().getCrackedNames().put(targetName, new Object());
                
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
            if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid, true)) {
                return;
            }

            plugin.sendMessage(invoker, "skin-downloading");
        }

        Bukkit.getScheduler().runTask(plugin, new SkinUpdater(plugin, invoker, player, targetSkin));
    }

    private void onNameResolve(UUID uuid) {
        if (invoker != null) {
            plugin.sendMessage(invoker, "uuid-resolved");
            if (plugin.getConfig().getBoolean("skinPermission") && !plugin.checkPermission(invoker, uuid, true)) {
                return;
            }

            plugin.sendMessage(invoker, "skin-downloading");
        }
        
        //run this is the same thread
        new SkinDownloader(plugin, invoker, player, uuid).run();
    }
}
