package com.github.games647.changeskin.bukkit.tasks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinData;
import com.github.games647.changeskin.core.UserPreference;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SkinUpdater implements Runnable {

    private final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final SkinData targetSkin;

    public SkinUpdater(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver, SkinData targetSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
    }

    @Override
    public void run() {
        if (receiver == null || !receiver.isOnline()) {
            return;
        }

        //uuid was successfull resolved, we could now make a cooldown check
        if (invoker instanceof Player) {
            plugin.addCooldown(((Player) invoker).getUniqueId());
        }

        if (plugin.getStorage() != null) {
            //Save the target uuid from the requesting player source
            final UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
            preferences.setTargetSkin(targetSkin);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    if (plugin.getStorage().save(targetSkin)) {
                        plugin.getStorage().save(preferences);
                    }
                }
            });
        }

        if (plugin.getConfig().getBoolean("instantSkinChange")) {
            onInstantUpdate();
        } else if (invoker != null) {
            plugin.sendMessage(invoker, "skin-changed-no-instant");
        }
    }

    private void onInstantUpdate() {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(receiver);
        //remove existing skins
        gameProfile.getProperties().clear();
        if (targetSkin != null) {
            gameProfile.getProperties().put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(targetSkin));
        }

        sendUpdate(gameProfile);
        plugin.sendMessage(receiver, "skin-changed");
        if (invoker != null && !receiver.equals(invoker)) {
            plugin.sendMessage(invoker, "skin-updated");
        }
    }

    private void sendUpdate(WrappedGameProfile gameProfile) throws FieldAccessException {
        sendUpdateSelf(gameProfile);

        //triggers an update for others player to see the new skin
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(receiver) || !onlinePlayer.canSee(receiver)) {
                continue;
            }

            //removes the entity and display the new skin
            onlinePlayer.hidePlayer(receiver);
            onlinePlayer.showPlayer(receiver);
        }
    }

    private void sendUpdateSelf(WrappedGameProfile gameProfile) throws FieldAccessException {
        Entity vehicle = receiver.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        NativeGameMode gamemode = NativeGameMode.fromBukkit(receiver.getGameMode());

        //remove info
        PacketContainer removeInfo = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        removeInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);

        WrappedChatComponent displayName = WrappedChatComponent.fromText(receiver.getPlayerListName());
        PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, 0, gamemode, displayName);
        removeInfo.getPlayerInfoDataLists().write(0, Arrays.asList(playerInfoData));

        //add info containing the skin data
        PacketContainer addInfo = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
        addInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
        addInfo.getPlayerInfoDataLists().write(0, Arrays.asList(playerInfoData));

        //Respawn packet
        PacketContainer respawn = protocolManager.createPacket(PacketType.Play.Server.RESPAWN);
        respawn.getIntegers().write(0, receiver.getWorld().getEnvironment().getId());
        respawn.getDifficulties().write(0, Difficulty.valueOf(receiver.getWorld().getDifficulty().toString()));
        respawn.getGameModes().write(0, gamemode);
        respawn.getWorldTypeModifier().write(0, receiver.getWorld().getWorldType());

        Location location = receiver.getLocation().clone();

        PacketContainer teleport = protocolManager.createPacket(PacketType.Play.Server.POSITION);
        teleport.getModifier().writeDefaults();
        teleport.getDoubles().write(0, location.getX());
        teleport.getDoubles().write(1, location.getY());
        teleport.getDoubles().write(2, location.getZ());
        teleport.getFloat().write(0, location.getYaw());
        teleport.getFloat().write(1, location.getPitch());
        //send an invalid teleport id in order to let bukkit ignore the incoming confirm packet
        teleport.getIntegers().writeSafely(0, -1);

        try {
            //remove the old skin - client updates it only on a complete remove and add
            protocolManager.sendServerPacket(receiver, removeInfo);
            //adds the skin
            protocolManager.sendServerPacket(receiver, addInfo);
            //notify the client that it should update the own skin
            protocolManager.sendServerPacket(receiver, respawn);

            //prevent the moved too quickly message
            protocolManager.sendServerPacket(receiver, teleport);

            //send the current inventory - otherwise player would have an empty inventory
            receiver.updateInventory();

            //this is sync so should be safe to call
            receiver.setHealth(receiver.getHealth());
            receiver.setMaxHealth(receiver.getMaxHealth() + 1);
            receiver.setMaxHealth(receiver.getMaxHealth() - 1);

            receiver.setItemInHand(receiver.getItemInHand());
            receiver.setWalkSpeed(receiver.getWalkSpeed());

            receiver.setFlySpeed(receiver.getFlySpeed());
            receiver.setFlying(receiver.getAllowFlight());
        } catch (InvocationTargetException ex) {
            plugin.getLogger().log(Level.SEVERE, "Exception sending instant skin change packet", ex);
        }
    }
}
