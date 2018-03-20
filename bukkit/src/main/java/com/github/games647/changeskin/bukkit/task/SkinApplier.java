package com.github.games647.changeskin.bukkit.task;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.task.SharedApplier;
import com.nametagedit.plugin.NametagEdit;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO;
import static com.comphenix.protocol.PacketType.Play.Server.RESPAWN;

public class SkinApplier extends SharedApplier {

    protected final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final SkinModel targetSkin;
    private final boolean keepSkin;

    public SkinApplier(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver
            , SkinModel targetSkin, boolean keepSkin) {
        super(plugin.getCore(), targetSkin, keepSkin);

        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        if (!isConnected()) {
            return;
        }

        //uuid was successful resolved, we could now make a cooldown check
        if (invoker instanceof Player && core != null) {
            UUID uniqueId = ((Player) invoker).getUniqueId();
            core.getCooldownService().trackPlayer(uniqueId);
        }

        if (plugin.getStorage() != null) {
            UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
            save(preferences);
        }

        applySkin();
    }

    @Override
    protected boolean isConnected() {
        return receiver != null && receiver.isOnline();
    }

    @Override
    protected void applyInstantUpdate() {
        plugin.getApi().applySkin(receiver, targetSkin);

        sendUpdateSelf(WrappedGameProfile.fromPlayer(receiver));
        sendUpdateOthers();

        if (receiver.equals(invoker)) {
            plugin.sendMessage(receiver, "skin-changed");
        } else {
            plugin.sendMessage(invoker, "skin-updated");
        }
    }

    @Override
    protected void sendMessage(String key) {
        plugin.sendMessage(invoker, key);
    }

    @Override
    protected void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void sendUpdateOthers() throws FieldAccessException {
        //triggers an update for others player to see the new skin
        Bukkit.getOnlinePlayers().stream()
                .filter(onlinePlayer -> onlinePlayer.canSee(receiver))
                .forEach(this::hideAndShow);
    }

    private void sendUpdateSelf(WrappedGameProfile gameProfile) throws FieldAccessException {
        Optional.ofNullable(receiver.getVehicle()).ifPresent(Entity::eject);

        sendPacketsSelf(gameProfile);

        //trigger update exp
        receiver.setExp(receiver.getExp());

        //triggers updateAbilities
        receiver.setWalkSpeed(receiver.getWalkSpeed());

        //send the current inventory - otherwise player would have an empty inventory
        receiver.updateInventory();

        PlayerInventory inventory = receiver.getInventory();
        inventory.setHeldItemSlot(inventory.getHeldItemSlot());

        //trigger update attributes like health modifier for generic.maxHealth
        try {
            receiver.getClass().getDeclaredMethod("updateScaledHealth").invoke(receiver);
        } catch (ReflectiveOperationException reflectiveEx) {
            plugin.getLog().error("Failed to invoke updateScaledHealth for attributes", reflectiveEx);
        }

        //prevent the moved too quickly message
        receiver.teleport(receiver.getLocation());

        //tell NameTagEdit to refresh the scoreboard
        if (Bukkit.getPluginManager().isPluginEnabled("NametagEdit")) {
            NametagEdit.getApi().reloadNametag(receiver);
        }
    }

    private void sendPacketsSelf(WrappedGameProfile gameProfile) {
        NativeGameMode gamemode = NativeGameMode.fromBukkit(receiver.getGameMode());
        WrappedChatComponent displayName = WrappedChatComponent.fromText(receiver.getPlayerListName());
        PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, 0, gamemode, displayName);

        //remove the old skin - client updates it only on a complete remove and add
        PacketContainer removeInfo = new PacketContainer(PLAYER_INFO);
        removeInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
        removeInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        //add info containing the skin data
        PacketContainer addInfo = removeInfo.deepClone();
        addInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);

        // Respawn packet - notify the client that it should update the own skin
        Difficulty difficulty = EnumWrappers.getDifficultyConverter().getSpecific(receiver.getWorld().getDifficulty());

        PacketContainer respawn = new PacketContainer(RESPAWN);
        respawn.getIntegers().write(0, receiver.getWorld().getEnvironment().getId());
        respawn.getDifficulties().write(0, difficulty);
        respawn.getGameModes().write(0, gamemode);
        respawn.getWorldTypeModifier().write(0, receiver.getWorld().getWorldType());

        sendPackets(removeInfo, addInfo, respawn);
    }

    @SuppressWarnings("deprecation")
    private void hideAndShow(Player other) {
        //removes the entity and display the new skin
        try {
            other.getClass().getDeclaredMethod("hidePlayer", Plugin.class, Player.class);

            other.hidePlayer(plugin, receiver);
            other.showPlayer(plugin, receiver);
        } catch (NoSuchMethodException noSuckMethodEx) {
            other.hidePlayer(receiver);
            other.showPlayer(receiver);
        }
    }

    private void sendPackets(PacketContainer... packets) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            for (PacketContainer packet : packets) {
                protocolManager.sendServerPacket(receiver, packet);
            }
        } catch (InvocationTargetException ex) {
            plugin.getLog().error("Exception sending instant skin change packet for: {}", receiver, ex);
        }
    }
}
