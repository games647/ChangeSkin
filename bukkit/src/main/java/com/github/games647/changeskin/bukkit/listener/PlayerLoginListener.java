package com.github.games647.changeskin.bukkit.listener;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.NameResolver;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreferences;
import com.google.common.collect.Multimap;

import java.util.Collection;
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
        final UserPreferences preferences = plugin.getCore().getLoginSession(player.getUniqueId());
        if (preferences == null) {
            fallbackBukkit(player, properties);
        } else {
            SkinData targetSkin = preferences.getTargetSkin();
            if (targetSkin == null) {
                final SkinData skinData = getSkinIfPresent(properties);
                if (skinData == null) {
                    setRandomSkin(preferences, properties);
                } else {
                    preferences.setTargetSkin(targetSkin);
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            plugin.getStorage().save(skinData);
                            plugin.getStorage().save(preferences);
                        }
                    });
                }
            } else {
                properties.clear();
                properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
            }
        }

        plugin.getCore().endSession(player.getUniqueId());
    }

    private SkinData getSkinIfPresent(Multimap<String, WrappedSignedProperty> properties) {
        //try to use the existing and put it in the cache so we use it for others
        Collection<WrappedSignedProperty> values = properties.get(ChangeSkinCore.SKIN_KEY);
        for (WrappedSignedProperty property : values) {
            if (property.hasSignature()) {
                //found a skin
                return new SkinData(property.getValue(), property.getSignature());
            }
        }

        return null;
    }

    private void setRandomSkin(final UserPreferences preferences, Multimap<String, WrappedSignedProperty> properties) {
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
        UserPreferences preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.getCore().startSession(player.getUniqueId(), preferences);

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
