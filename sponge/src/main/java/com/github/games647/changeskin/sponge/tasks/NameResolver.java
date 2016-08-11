package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class NameResolver implements Runnable {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final String targetName;
    private final Player receiver;
    private final boolean keepSkin;

    public NameResolver(ChangeSkinSponge plugin, CommandSource invoker, String targetName, Player receiver
            , boolean keepSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.receiver = receiver;
        this.keepSkin = keepSkin;
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

            try {
                uuid = plugin.getCore().getMojangSkinApi().getUUID(targetName);
                if (uuid == null) {
                    if (invoker != null) {
                        plugin.sendMessage(invoker, "no-resolve");
                    }
                } else {
                    plugin.getCore().getUuidCache().put(targetName, uuid);
                }
            } catch (NotPremiumException notPremiumEx) {
                plugin.getLogger().debug("Requested not premium", notPremiumEx);
                plugin.getCore().getCrackedNames().put(targetName, new Object());

                if (invoker != null) {
                    plugin.sendMessage(invoker, "not-premium");
                }
            } catch (RateLimitException rateLimitEx) {
                plugin.getLogger().error("UUID Rate Limit reached", rateLimitEx);
                if (invoker != null) {
                    plugin.sendMessage(invoker, "rate-limit");
                }
            }
        }

        if (uuid != null) {
            onNameResolve(uuid);
        }
    }

    private void onNameResolve(UUID uuid) {
        if (invoker != null) {
            plugin.sendMessage(invoker, "uuid-resolved");
            plugin.sendMessage(invoker, "skin-downloading");
        }

        //run this is the same thread
        new SkinDownloader(plugin, invoker, receiver, uuid, keepSkin).run();
    }
}
