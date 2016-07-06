package com.github.games647.changeskin.bukkit.listener;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.NameResolver;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerLoginListener implements Listener {

    protected final ChangeSkinBukkit plugin;

    private final Random random = new Random();

    public PlayerLoginListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        Player player = loginEvent.getPlayer();
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();

        //updates to the chosen one
        UserPreference preferences = plugin.getLoginSession(player.getUniqueId());
        if (preferences == null) {
            fallbackBukkit(player, properties);
        } else {
            SkinData targetSkin = preferences.getTargetSkin();
            if (targetSkin == null) {
                setRandomSkin(preferences, properties);
            } else {
                properties.clear();
                properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
            }
        }

        plugin.endSession(player.getUniqueId());
    }

    private void setRandomSkin(final UserPreference preferences, Multimap<String, WrappedSignedProperty> properties) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getCore().getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            final SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                preferences.setTargetSkin(targetSkin);
                properties.clear();
                properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));

                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(preferences);
                    }
                });
            }
        }
    }

    private void fallbackBukkit(Player player, Multimap<String, WrappedSignedProperty> properties) {
        UserPreference preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.startSession(player.getUniqueId(), preferences);

        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                NameResolver nameResolver = new NameResolver(plugin, null, player.getName(), player);
                //refetch
                Bukkit.getScheduler().runTaskAsynchronously(plugin, nameResolver);
            } else {
                setRandomSkin(preferences, properties);
            }
        } else {
            properties.clear();
            properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
        }
    }
}
