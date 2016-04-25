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

    private final boolean isSpigot = firesAsyncPreLoginEvent();
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

        boolean skinFound = false;
        //try to use the existing and put it in the cache so we use it for others
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(player);
        Multimap<String, WrappedSignedProperty> properties = gameProfile.getProperties();
        Collection<WrappedSignedProperty> values = properties.get(ChangeSkinCore.SKIN_KEY);
        for (WrappedSignedProperty property : values) {
            if (property.hasSignature()) {
                //found a skin
                SkinData skinData = new SkinData(property.getValue(), property.getSignature());
                plugin.getStorage().getSkinUUIDCache().put(player.getUniqueId(), skinData);
                skinFound = true;
                break;
            }
        }

        //updates to the chosen one
        UserPreferences preferences = plugin.getStorage().getPreferences(player.getUniqueId(), true);
        if (isSpigot) {
            SkinData targetSkin = preferences.getTargetSkin();
            if (targetSkin != null) {
                properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
            } else if (!skinFound) {
                setRandomSkin(player, properties);
            }
        } else {
            fallbackBukkit(player, preferences, properties);
        }
    }

    private void setRandomSkin(Player player, Multimap<String, WrappedSignedProperty> properties) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getCore().getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            final SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                final UserPreferences preferences = plugin.getStorage().getPreferences(player.getUniqueId(), false);
                preferences.setTargetSkin(targetSkin);
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

    private void fallbackBukkit(Player player, UserPreferences preferences
            , Multimap<String, WrappedSignedProperty> properties) {
        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                //refetch
                Bukkit.getScheduler()
                        .runTaskAsynchronously(plugin, new NameResolver(plugin, null, player.getName(), player));
            } else {
                setRandomSkin(player, properties);
            }
        } else {
            properties.put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
        }
    }

    private boolean firesAsyncPreLoginEvent() {
        String version = Bukkit.getServer().getVersion();
        return version.contains("Paper") || version.contains("Spigot") || version.contains("Taco");
    }
}
