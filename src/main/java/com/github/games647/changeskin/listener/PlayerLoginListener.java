package com.github.games647.changeskin.listener;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.github.games647.changeskin.ChangeSkin;
import com.github.games647.changeskin.SkinData;
import com.github.games647.changeskin.UserPreferences;
import com.github.games647.changeskin.tasks.NameResolver;
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

    private static final String SKIN_KEY = "textures";

    protected final ChangeSkin plugin;
    private final boolean isSpigot = firesAsyncPreLoginEvent();
    private final Random random = new Random();

    public PlayerLoginListener(ChangeSkin plugin) {
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
        Collection<WrappedSignedProperty> values = properties.get(SKIN_KEY);
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
                properties.put(SKIN_KEY, targetSkin.convertToProperty());
            } else if (!skinFound) {
                setRandomSkin(player, properties);
            }
        } else {
            fallbackBukkit(player, preferences, properties);
        }
    }

    private void setRandomSkin(Player player, Multimap<String, WrappedSignedProperty> properties) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinData> defaultSkins = plugin.getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = random.nextInt(defaultSkins.size());

            final SkinData targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                plugin.getStorage().getPreferences(player.getUniqueId(), false).setTargetSkin(targetSkin);
                properties.put(SKIN_KEY, targetSkin.convertToProperty());

                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        plugin.getStorage().save(targetSkin);
                    }
                });
            }
        }
    }

    private void fallbackBukkit(Player player, UserPreferences preferences
            , Multimap<String, WrappedSignedProperty> properties) {
        if (preferences.getTargetSkin() == null) {
            if (plugin.getConfig().getBoolean("restoreSkins")) {
                //refetch
                Bukkit.getScheduler()
                        .runTaskAsynchronously(plugin, new NameResolver(plugin, null, player.getName(), player));
            } else {
                setRandomSkin(player, properties);
            }
        } else {
            properties.put(SKIN_KEY, preferences.getTargetSkin().convertToProperty());
        }
    }

    private boolean firesAsyncPreLoginEvent() {
        String version = Bukkit.getServer().getVersion();
        return version.contains("Paper") || version.contains("Spigot") || version.contains("Taco");
    }
}
