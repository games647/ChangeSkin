package com.github.games647.changeskin.bukkit.listener;

import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.shared.SharedListener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

public class AsyncLoginListener extends SharedListener implements Listener {

    protected final ChangeSkinBukkit plugin;

    public AsyncLoginListener(ChangeSkinBukkit plugin) {
        super(plugin.getCore());

        this.plugin = plugin;
    }

    //we are making an blocking request it might be better to ignore it if normal priority events cancelled it
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent preLoginEvent) {
        if (preLoginEvent.getLoginResult() != Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        UUID playerUuid = preLoginEvent.getUniqueId();
        String playerName = preLoginEvent.getName();

        UserPreference preferences = plugin.getStorage().getPreferences(playerUuid);
        if (preferences == null) {
            return;
        }

        plugin.startSession(playerUuid, preferences);

        SkinData targetSkin = preferences.getTargetSkin();
        if (targetSkin == null && plugin.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(playerName, preferences);
        } else {
            targetSkin = core.checkAutoUpdate(targetSkin);
            preferences.setTargetSkin(targetSkin);
            save(targetSkin, preferences);
        }
    }

    @Override
    protected void save(final SkinData skin, final UserPreference preferences) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getStorage().save(skin)) {
                plugin.getStorage().save(preferences);
            }
        });
    }
}
