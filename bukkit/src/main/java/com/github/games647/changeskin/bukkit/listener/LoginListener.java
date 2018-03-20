package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.bukkit.tasks.NameResolver;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class LoginListener implements Listener {

    private final ChangeSkinBukkit plugin;

    public LoginListener(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        if (loginEvent.getResult() != Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        Player player = loginEvent.getPlayer();

        //updates to the chosen one
        UserPreference preferences = plugin.getLoginSession(player.getUniqueId());
        if (preferences == null) {
            fallbackBukkit(player);
        } else {
            Optional<SkinModel> targetSkin = preferences.getTargetSkin();
            if (targetSkin.isPresent()) {
                plugin.getApi().applySkin(player, targetSkin.get());
            } else {
                setRandomSkin(player, preferences);
            }
        }

        plugin.endSession(player.getUniqueId());
    }

    private void setRandomSkin(Player receiver, UserPreference preferences) {
        //skin wasn't found and there are no preferences so set a default skin
        List<SkinModel> defaultSkins = plugin.getCore().getDefaultSkins();
        if (!defaultSkins.isEmpty()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(defaultSkins.size());

            SkinModel targetSkin = defaultSkins.get(randomIndex);
            if (targetSkin != null) {
                preferences.setTargetSkin(targetSkin);
                plugin.getApi().applySkin(receiver, targetSkin);

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getStorage().save(preferences));
            }
        }
    }

    private void fallbackBukkit(Player player) {
        UserPreference preferences = plugin.getStorage().getPreferences(player.getUniqueId());
        plugin.startSession(player.getUniqueId(), preferences);

        Optional<SkinModel> targetSkin = preferences.getTargetSkin();
        if (targetSkin.isPresent()) {
            plugin.getApi().applySkin(player, targetSkin.get());
        } else if (plugin.getConfig().getBoolean("restoreSkins")) {
            //refetch
            Runnable nameResolver = new NameResolver(plugin, null, player.getName(), player, false);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, nameResolver);
        } else {
            setRandomSkin(player, preferences);
        }
    }
}
