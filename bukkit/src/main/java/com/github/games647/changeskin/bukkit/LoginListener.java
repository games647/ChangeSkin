package com.github.games647.changeskin.bukkit;

import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SharedListener;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;

public class LoginListener extends SharedListener implements Listener {

    protected final ChangeSkinBukkit plugin;

    public LoginListener(ChangeSkinBukkit plugin) {
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

        UserPreference preferences = core.getStorage().getPreferences(playerUuid);
        if (preferences == null) {
            return;
        }

        plugin.startSession(playerUuid, preferences);

        Optional<SkinModel> optSkin = preferences.getTargetSkin();
        if (optSkin.isPresent()) {
            SkinModel targetSkin = optSkin.get();
            if (!preferences.isKeepSkin()) {
                targetSkin = core.checkAutoUpdate(targetSkin);
            }

            preferences.setTargetSkin(targetSkin);
            save(preferences);
        } else if (core.getConfig().getBoolean("restoreSkins")) {
            refetchSkin(playerName, preferences);
        }

        if (!preferences.getTargetSkin().isPresent()) {
            getRandomSkin().ifPresent(preferences::setTargetSkin);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            //in this event isCancelled option in the annotation doesn't work
            return;
        }

        Player player = loginEvent.getPlayer();

        //updates to the chosen one
        UserPreference preferences = plugin.getLoginSession(player.getUniqueId());
        if (preferences != null) {
            preferences.getTargetSkin().ifPresent(skin -> plugin.getApi().applySkin(player, skin));
        }

        plugin.endSession(player.getUniqueId());
    }

    @Override
    protected void save(UserPreference preferences) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Optional<SkinModel> optSkin = preferences.getTargetSkin();
            if (optSkin.isPresent()) {
                if (plugin.getStorage().save(optSkin.get())) {
                    plugin.getStorage().save(preferences);
                }
            } else {
                plugin.getStorage().save(preferences);
            }
        });
    }
}
