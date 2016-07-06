package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.NotPremiumException;
import com.github.games647.changeskin.core.RateLimitException;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.UUID;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public class NameResolver implements Runnable {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final String targetName;
    private final Player receiver;

    public NameResolver(ChangeSkinSponge plugin, CommandSource invoker, String targetName, Player receiver) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.targetName = targetName;
        this.receiver = receiver;
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

            SkinData targetSkin = plugin.getCore().getStorage().getSkin(targetName);
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
        } else {
            onNameResolve(uuid);
        }
    }

    private void onNameResolveDatabase(SkinData targetSkin) {
        if (invoker != null) {
            plugin.sendMessage(invoker, "uuid-resolved");
            if (plugin.getRootNode().getNode("skinPermission").getBoolean()
                    && !plugin.checkPermission(invoker, targetSkin.getUuid(), true)) {
                return;
            }

            plugin.sendMessage(invoker, "skin-downloading");
        }

        SkinUpdater skinUpdater = new SkinUpdater(plugin, invoker, receiver, targetSkin);
        plugin.getGame().getScheduler().createTaskBuilder().execute(skinUpdater).submit(plugin);
    }

    private void onNameResolve(UUID uuid) {
        if (invoker != null) {
            plugin.sendMessage(invoker, "uuid-resolved");
            plugin.sendMessage(invoker, "skin-downloading");
        }

        //run this is the same thread
        new SkinDownloader(plugin, invoker, receiver, uuid).run();
    }
}
