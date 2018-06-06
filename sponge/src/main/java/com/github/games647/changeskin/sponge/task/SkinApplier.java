package com.github.games647.changeskin.sponge.task;

import com.flowpowered.math.vector.Vector3d;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.task.SharedApplier;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;

import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SkinApplier extends SharedApplier {

    private final ChangeSkinSponge plugin;
    private final CommandSource invoker;
    private final Player receiver;

    public SkinApplier(ChangeSkinSponge plugin, CommandSource invoker, Player receiver, SkinModel targetSkin
            , boolean keepSkin) {
        super(plugin.getCore(), targetSkin, keepSkin);

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        if (!isConnected()) {
            return;
        }

        //uuid was successful resolved, we could now make a cooldown check
        if (invoker instanceof Player) {
            UUID uniqueId = ((Player) invoker).getUniqueId();
            core.getCooldownService().trackPlayer(uniqueId);
        }

        if (core.getStorage() != null) {
            UserPreference preferences = core.getStorage().getPreferences(receiver.getUniqueId());
            save(preferences);
        }

        applySkin();
    }

    @Override
    protected boolean isConnected() {
        return receiver.isOnline();
    }

    @Override
    protected void applyInstantUpdate() {
        plugin.getApi().applySkin(receiver, targetSkin);

        sendUpdate();
        plugin.sendMessage(invoker, "skin-changed");
    }

    @Override
    protected void sendMessage(String key) {
        plugin.sendMessage(invoker, key);
    }

    @Override
    protected void runAsync(Runnable runnable) {
        Task.builder().async()
                .execute(runnable)
                .submit(plugin);
    }

    private void sendUpdate() {
        sendUpdateSelf();

        //triggers an update for others player to see the new skin
        receiver.offer(Keys.VANISH, true);
        receiver.offer(Keys.VANISH, false);
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
        Vector3d rotation = receiver.getRotation();
        World receiverWorld = receiver.getWorld();
        Sponge.getServer().getWorlds()
                .stream()
                .filter(world -> !world.equals(receiverWorld))
                .findFirst()
                .ifPresent(world -> {
                    receiver.setLocation(world.getSpawnLocation());
                    receiver.setLocationAndRotation(oldLocation, rotation);
                });
    }
}
