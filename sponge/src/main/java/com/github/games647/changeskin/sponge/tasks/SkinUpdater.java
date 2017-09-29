package com.github.games647.changeskin.sponge.tasks;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SkinUpdater implements Runnable {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;
    private final SkinData targetSkin;
    private final boolean keepSkin;

    public SkinUpdater(ChangeSkinSponge plugin, CommandSource invoker, Player receiver, SkinData targetSkin
            , boolean keepSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        if (!receiver.isOnline()) {
            return;
        }

        //uuid was successful resolved, we could now make a cooldown check
        if (invoker instanceof Player) {
            plugin.getCore().addCooldown(((Player) invoker).getUniqueId());
        }

        //Save the target uuid from the requesting player source
        final UserPreference preferences = plugin.getCore().getStorage().getPreferences(receiver.getUniqueId());
        preferences.setTargetSkin(targetSkin);
        preferences.setKeepSkin(keepSkin);

        Task.builder().async()
                .execute(() -> {
                    if (plugin.getCore().getStorage().save(targetSkin)) {
                        plugin.getCore().getStorage().save(preferences);
                    }
                })
                .submit(plugin);

        if (plugin.getCore().getConfig().getBoolean("instantSkinChange")) {
            onInstantUpdate();
        } else if (invoker != null) {
            plugin.sendMessageKey(invoker, "skin-changed-no-instant");
        }
    }

    private void onInstantUpdate() {
        GameProfile profile = receiver.getProfile();
        if (targetSkin != null) {
            //remove existing skins
            profile.getPropertyMap().clear();

            ProfileProperty profileProperty = ProfileProperty.of(ChangeSkinCore.SKIN_KEY
                    , targetSkin.getEncodedData(), targetSkin.getEncodedSignature());
            profile.getPropertyMap().put(ChangeSkinCore.SKIN_KEY, profileProperty);
        }

        sendUpdate();
        if (invoker != null) {
            plugin.sendMessageKey(invoker, "skin-changed");
        }
    }

    private void sendUpdate() {
        sendUpdateSelf();

        //triggers an update for others player to see the new skin
        Sponge.getServer().getOnlinePlayers().stream()
                .filter(onlinePlayer -> onlinePlayer.equals(receiver))
                .filter(onlinePlayer -> onlinePlayer.canSee(receiver))
                .forEach(onlinePlayer -> {
                    //removes the entity and display the new skin
                    onlinePlayer.offer(Keys.VANISH, true);
                    onlinePlayer.offer(Keys.VANISH, false);
                });
    }

    private void sendUpdateSelf() {
        receiver.getTabList().removeEntry(receiver.getUniqueId());
        receiver.getTabList().addEntry(TabListEntry.builder()
                .displayName(receiver.getDisplayNameData().displayName().get())
                .latency(receiver.getConnection().getLatency())
                .list(receiver.getTabList())
                .gameMode(receiver.getGameModeData().type().get())
                .profile(receiver.getProfile())
                .build());

        Location<World> oldLocation = receiver.getLocation();
        World receiverWorld = receiver.getWorld();
        Sponge.getServer().getWorlds()
                .stream()
                .filter(world -> !world.equals(receiverWorld))
                .findFirst()
                .ifPresent(world -> {
                    receiver.setLocation(world.getSpawnLocation());
                    receiver.setLocation(oldLocation);
                });
    }
}
