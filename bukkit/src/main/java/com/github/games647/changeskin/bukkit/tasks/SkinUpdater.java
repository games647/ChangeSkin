package com.github.games647.changeskin.bukkit.tasks;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.nametagedit.plugin.NametagEdit;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO;
import static com.comphenix.protocol.PacketType.Play.Server.POSITION;
import static com.comphenix.protocol.PacketType.Play.Server.RESPAWN;
import static com.comphenix.protocol.PacketType.Play.Server.UPDATE_HEALTH;

public class SkinUpdater implements Runnable {

    protected final ChangeSkinBukkit plugin;
    private final CommandSender invoker;
    private final Player receiver;
    private final SkinModel targetSkin;
    private final boolean keepSkin;

    public SkinUpdater(ChangeSkinBukkit plugin, CommandSender invoker, Player receiver
            , SkinModel targetSkin, boolean keepSkin) {
        this.plugin = plugin;
        this.invoker = invoker;
        this.receiver = receiver;
        this.targetSkin = targetSkin;
        this.keepSkin = keepSkin;
    }

    @Override
    public void run() {
        if (receiver == null || !receiver.isOnline()) {
            return;
        }

        //uuid was successful resolved, we could now make a cooldown check
        if (invoker instanceof Player && plugin.getCore() != null) {
            plugin.getCore().addCooldown(((Player) invoker).getUniqueId());
        }

        if (plugin.getStorage() != null) {
            //Save the target uuid from the requesting player source
            UserPreference preferences = plugin.getStorage().getPreferences(receiver.getUniqueId());
            preferences.setTargetSkin(targetSkin);
            preferences.setKeepSkin(keepSkin);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (plugin.getStorage().save(targetSkin)) {
                    plugin.getStorage().save(preferences);
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
        Bukkit.getOnlinePlayers().stream()
                .filter(onlinePlayer -> !onlinePlayer.equals(receiver))
                .filter(onlinePlayer -> onlinePlayer.canSee(receiver))
                .forEach(onlinePlayer -> {
                    //removes the entity and display the new skin
                    onlinePlayer.hidePlayer(receiver);
                    onlinePlayer.showPlayer(receiver);
                });
    }

    private void sendUpdateSelf(WrappedGameProfile gameProfile) throws FieldAccessException {
        Entity vehicle = receiver.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
        }

        sendPacketsSelf(gameProfile);

        //send the current inventory - otherwise player would have an empty inventory
        receiver.updateInventory();

        PlayerInventory inventory = receiver.getInventory();
        inventory.setHeldItemSlot(inventory.getHeldItemSlot());

        //this is sync so should be safe to call

        //exp
        float experience = receiver.getExp();
        int totalExperience = receiver.getTotalExperience();
        receiver.setExp(experience);
        receiver.setTotalExperience(totalExperience);

        //set to the correct hand position
        setItemInHand();

        //triggers updateAbilities
        receiver.setWalkSpeed(receiver.getWalkSpeed());

        if (Bukkit.getPluginManager().isPluginEnabled("NametagEdit")) {
            NametagEdit.getApi().reloadNametag(receiver);
        }
    }

    private void sendPacketsSelf(WrappedGameProfile gameProfile) {
        NativeGameMode gamemode = NativeGameMode.fromBukkit(receiver.getGameMode());

        //remove the old skin - client updates it only on a complete remove and add
        PacketContainer removeInfo = new PacketContainer(PLAYER_INFO);
        removeInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);

        WrappedChatComponent displayName = WrappedChatComponent.fromText(receiver.getPlayerListName());
        PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, 0, gamemode, displayName);
        removeInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        //add info containing the skin data
        PacketContainer addInfo = new PacketContainer(PLAYER_INFO);
        addInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
        addInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

        //Respawn packet
        // notify the client that it should update the own skin
        Difficulty difficulty = EnumWrappers.getDifficultyConverter().getSpecific(receiver.getWorld().getDifficulty());

        PacketContainer respawn = new PacketContainer(RESPAWN);
        respawn.getIntegers().write(0, receiver.getWorld().getEnvironment().getId());
        respawn.getDifficulties().write(0, difficulty);
        respawn.getGameModes().write(0, gamemode);
        respawn.getWorldTypeModifier().write(0, receiver.getWorld().getWorldType());

        Location location = receiver.getLocation().clone();

        //prevent the moved too quickly message
        PacketContainer teleport = new PacketContainer(POSITION);
        teleport.getModifier().writeDefaults();
        teleport.getDoubles().write(0, location.getX());
        teleport.getDoubles().write(1, location.getY());
        teleport.getDoubles().write(2, location.getZ());
        teleport.getFloat().write(0, location.getYaw());
        teleport.getFloat().write(1, location.getPitch());
        //send an invalid teleport id in order to let Bukkit ignore the incoming confirm packet
        teleport.getIntegers().writeSafely(0, -1337);

        sendPackets(removeInfo, addInfo, respawn, teleport);

        //trigger update attributes like health modifier for generic.maxHealth
        try {
            receiver.getClass().getDeclaredMethod("updateScaledHealth").invoke(receiver);
        } catch (ReflectiveOperationException reflectiveEx) {
            plugin.getLog().error("Failed to invoke updateScaledHealth for attributes", reflectiveEx);
        }

        PacketContainer health = new PacketContainer(UPDATE_HEALTH);
        health.getFloat().write(0, (float) receiver.getHealth());
        health.getFloat().write(1, receiver.getSaturation());
        health.getIntegers().write(0, receiver.getFoodLevel());
        sendPackets(health);
    }

    private float getScaledHealth() {
        if (receiver.isHealthScaled())
            return (float) (receiver.getHealth() * receiver.getHealthScale() / getMaxHealth());

        return (float) receiver.getHealth();
    }

    private void sendPackets(PacketContainer... packets) {
        try {
            for (PacketContainer packet : packets) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, packet);
            }
        } catch (InvocationTargetException ex) {
            plugin.getLog().error("Exception sending instant skin change packet for: {}", receiver, ex);
        }
    }

    private double getMaxHealth() {
        if (MinecraftVersion.getCurrentVersion().compareTo(MinecraftVersion.COMBAT_UPDATE) >= 0) {
            return receiver.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        }

        double maxHealth = receiver.getMaxHealth();
        for(PotionEffect potionEffect : receiver.getActivePotionEffects()){
            //Had to do this because doing if(potionEffect.getType() == PotionEffectType.HEALTH_BOOST)
            //It wouldn't recognize it as the same.
            if(potionEffect.getType().getName().equalsIgnoreCase(PotionEffectType.HEALTH_BOOST.getName())){
                maxHealth -= ((potionEffect.getAmplifier() + 1) * 4);
            }
        }

        return maxHealth;
    }

    private void setItemInHand() {
        if (MinecraftVersion.getCurrentVersion().compareTo(MinecraftVersion.COMBAT_UPDATE) >= 0) {
            receiver.getInventory().setItemInMainHand(receiver.getInventory().getItemInMainHand());
            receiver.getInventory().setItemInOffHand(receiver.getInventory().getItemInOffHand());
            return;
        }

        receiver.getInventory().setItemInHand(receiver.getItemInHand());
    }
}
