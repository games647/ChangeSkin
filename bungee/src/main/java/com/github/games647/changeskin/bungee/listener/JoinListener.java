package com.github.games647.changeskin.bungee.listener;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;

import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class JoinListener implements Listener {

    protected final ChangeSkinBungee plugin;
    private final Random random = new Random();

    public JoinListener(ChangeSkinBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PostLoginEvent postLoginEvent) {
        ProxiedPlayer player = postLoginEvent.getPlayer();

        //updates to the chosen one
        final UserPreferences preferences = plugin.getCore().getLoginSession(player.getUniqueId());
        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin == null) {
            final SkinData skinData = getSkinIfPresent(player);
            if (skinData == null) {
                setRandomSkin(preferences, player);
            } else {
                preferences.setTargetSkin(targetSkin);
                ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(skinData);
                        plugin.getStorage().save(preferences);
                    }
                });
            }
        } else {
            plugin.applySkin(player, targetSkin);
        }

        plugin.getCore().endSession(player.getUniqueId());
    }

    private SkinData getSkinIfPresent(ProxiedPlayer player) {
        //try to use the existing and put it in the cache so we use it for others
        InitialHandler initialHandler = (InitialHandler) player.getPendingConnection();
        LoginResult loginProfile = initialHandler.getLoginProfile();
        //this is null on offline mode
        if (loginProfile != null) {
            Property[] properties = loginProfile.getProperties();
            for (Property property : properties) {
                //found a skin
                return new SkinData(property.getValue(), property.getSignature());
            }
        }

        return null;
    }

    private void setRandomSkin(final UserPreferences preferences, ProxiedPlayer player) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getCore().getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            final SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                preferences.setTargetSkin(targetSkin);
                plugin.applySkin(player, targetSkin);

                ProxyServer.getInstance().getScheduler().runAsync(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(preferences);
                    }
                });
            }
        }
    }
}
