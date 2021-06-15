package com.github.games647.changeskin.bukkit.task;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BukkitConverters;
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
import com.google.common.hash.Hashing;
import com.nametagedit.plugin.NametagEdit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import static com.comphenix.protocol.PacketType.Play.Server.PLAYER_INFO;
import static com.comphenix.protocol.PacketType.Play.Server.POSITION;
import static com.comphenix.protocol.PacketType.Play.Server.RESPAWN;

public class SkinApplier extends SharedApplier {

    private static final boolean NEW_HIDE_METHOD_AVAILABLE;

    // static final methods are faster, because JVM can inline them and make them accessible
    private static final Field DEBUG_WORLD_FIELD;

    private static final Method PLAYER_HANDLE_METHOD;
    private static final Field INTERACTION_MANAGER;
    private static final Field GAMEMODE_FIELD;

    private static final boolean DISABLED_PACKETS;

    static {
        boolean methodAvailable;
        try {
            Player.class.getDeclaredMethod("hidePlayer", Plugin.class, Player.class);
            methodAvailable = true;
        } catch (NoSuchMethodException noSuchMethodEx) {
            methodAvailable = false;
        }

        boolean localDisable = false;
        Field localWorldKey = null;
        Field localDebugWorld = null;

        Method localHandleMethod = null;
        Field localInteractionField = null;
        Field localGamemode = null;

        // TODO: In ProtocolLib 4.6 We could use a specific modifier for the world key

        // use standard reflection, because we cannot use the performance benefits of MethodHandles
        // MethodHandles are only clearly faster with invokeExact
        // we can use for a nested call of debug world: getDebugField(getNMSWorldFromBukkit) in a single handle
        // But for the resourceKey the return type is not known at compile time - it's an NMS class
        Logger logger = JavaPlugin.getPlugin(ChangeSkinBukkit.class).getLog();
        if (isAtOrAbove("1.16")) {
            try {
                Class<?> nmsWorldClass = MinecraftReflection.getNmsWorldClass();
                localWorldKey = nmsWorldClass.getDeclaredField("dimensionKey");
                localWorldKey.setAccessible(true);

                localDebugWorld = nmsWorldClass.getDeclaredField("debugWorld");
                localDebugWorld.setAccessible(true);

                localHandleMethod = MinecraftReflection.getCraftPlayerClass().getDeclaredMethod("getHandle");

                Class<?> entityPlayerClass = MinecraftReflection.getEntityPlayerClass();
                localInteractionField = entityPlayerClass.getDeclaredField("playerInteractManager");
                localInteractionField.setAccessible(true);

                Class<?> playerInteractManager = MinecraftReflection.getMinecraftClass("PlayerInteractManager");
                localGamemode = playerInteractManager.getDeclaredField("e");
                localGamemode.setAccessible(true);
            } catch (NoSuchFieldException | NoSuchMethodException reflectiveEx) {
                logger.warn("Cannot find 1.16x fields", reflectiveEx);
                localDisable = true;
            }
        }

        NEW_HIDE_METHOD_AVAILABLE = methodAvailable;
        DEBUG_WORLD_FIELD = localDebugWorld;

        PLAYER_HANDLE_METHOD = localHandleMethod;
        INTERACTION_MANAGER = localInteractionField;
        GAMEMODE_FIELD = localGamemode;
        DISABLED_PACKETS = localDisable;
    }

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

        if (!DISABLED_PACKETS) {
            sendUpdateSelf(WrappedGameProfile.fromPlayer(receiver));
        }

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
                .filter(onlinePlayer -> !onlinePlayer.equals(receiver))
                .filter(onlinePlayer -> onlinePlayer.canSee(receiver))
                .forEach(this::hideAndShow);

        //tell NameTagEdit to refresh the scoreboard
        if (Bukkit.getPluginManager().isPluginEnabled("NametagEdit")) {
            NametagEdit.getApi().reloadNametag(receiver);
        }
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
    }

    private void sendPacketsSelf(WrappedGameProfile gameProfile) {
        PacketContainer removeInfo;
        PacketContainer addInfo;
        PacketContainer respawn;
        PacketContainer teleport;

        try {
            NativeGameMode gamemode = NativeGameMode.fromBukkit(receiver.getGameMode());
            WrappedChatComponent displayName = WrappedChatComponent.fromText(receiver.getPlayerListName());
            PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, 0, gamemode, displayName);

            //remove the old skin - client updates it only on a complete remove and add
            removeInfo = new PacketContainer(PLAYER_INFO);
            removeInfo.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            removeInfo.getPlayerInfoDataLists().write(0, Collections.singletonList(playerInfoData));

            //add info containing the skin data
            addInfo = removeInfo.deepClone();
            addInfo.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);

            // Respawn packet - notify the client that it should update the own skin
            respawn = createRespawnPacket(gamemode);

            //prevent the moved too quickly message
            teleport = createTeleportPacket(receiver.getLocation().clone());
        } catch (ReflectiveOperationException reflectiveEx) {
            plugin.getLog().error("Error occurred preparing packets. Cancelling self update", reflectiveEx);
            return;
        }

        sendPackets(removeInfo, addInfo, respawn, teleport);
    }

    @SuppressWarnings("deprecation")
    private void hideAndShow(Player other) {
        //removes the entity and display the new skin
        if (NEW_HIDE_METHOD_AVAILABLE) {
            other.hidePlayer(plugin, receiver);
            other.showPlayer(plugin, receiver);
        } else {
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

    private PacketContainer createRespawnPacket(NativeGameMode gamemode) throws ReflectiveOperationException {
        PacketContainer respawn = new PacketContainer(RESPAWN);

        World world = receiver.getWorld();
        Difficulty difficulty = EnumWrappers.getDifficultyConverter().getSpecific(world.getDifficulty());

        //<= 1.13.1
        int dimensionId = world.getEnvironment().getId();
        respawn.getIntegers().writeSafely(0, dimensionId);

        //> 1.13.1
        if (MinecraftVersion.getCurrentVersion().compareTo(MinecraftVersion.AQUATIC_UPDATE) > 0) {
            try {
                respawn.getDimensions().writeSafely(0, dimensionId);
            } catch (NoSuchMethodError noSuchMethodError) {
                throw new ReflectiveOperationException("Unable to find dimension setter. " +
                        "Your ProtocolLib version is incompatible with this plugin version in combination with " +
                        "Minecraft 1.13.1. " +
                        "Try to download an update of ProtocolLib.", noSuchMethodError);
            }
        }

        // 1.14 dropped difficulty and 1.15 added hashed seed
        respawn.getDifficulties().writeSafely(0, difficulty);
        if (isAtOrAbove("1.15")) {
            long seed = world.getSeed();
            respawn.getLongs().write(0, Hashing.sha256().hashLong(seed).asLong());
        }

        if (isAtOrAbove("1.16")) {
            // a = dimension (as resource key) -> dim type, b = world (resource key) -> world name, c = "hashed" seed
            // dimension and seed covered above - we have to start with 1 because dimensions already uses the first idx
            Object nmsWorld = BukkitConverters.getWorldConverter().getGeneric(world);

            // 1.16.2 dropped the first resourcekey usage
            respawn.getWorldKeys().write(0, world);

            // d = gamemode, e = gamemode (previous)
            respawn.getGameModes().write(0, gamemode);
            respawn.getGameModes().write(1, getPreviousGamemode(receiver));
            // f = debug world, g = flat world, h = flag (copy metadata)
            // get the NMS world
            try {
                respawn.getBooleans().write(0, DEBUG_WORLD_FIELD.getBoolean(nmsWorld));
            } catch (Exception ex) {
                plugin.getLog().error("Cannot fetch debug state of world {}. Assuming false", world, ex);
                respawn.getBooleans().write(0, false);
            } catch (Throwable throwable) {
                throw (Error) throwable;
            }

            respawn.getBooleans().write(1, world.getWorldType() == WorldType.FLAT);
            // flag: true = teleport like, false = player actually died - uses respawn anchor in nether
            respawn.getBooleans().write(2, true);
        } else {
            // world type field replaced with a boolean
            respawn.getWorldTypeModifier().write(0, world.getWorldType());
            respawn.getGameModes().write(0, gamemode);
        }

        return respawn;
    }

    private static boolean isAtOrAbove(String s) {
        return MinecraftVersion.getCurrentVersion().compareTo(new MinecraftVersion(s)) > 0;
    }

    private PacketContainer createTeleportPacket(Location location) {
        PacketContainer teleport = new PacketContainer(POSITION);
        teleport.getModifier().writeDefaults();

        teleport.getDoubles().write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        teleport.getFloat().write(0, location.getYaw())
                .write(1, location.getPitch());

        //send an invalid teleport id in order to let Bukkit ignore the incoming confirm packet
        teleport.getIntegers().writeSafely(0, -1337);
        return teleport;
    }

    private NativeGameMode getPreviousGamemode(Player receiver) {
        try {
            Object nmsPlayer = PLAYER_HANDLE_METHOD.invoke(receiver);
            Object interactionManager = INTERACTION_MANAGER.get(nmsPlayer);
            Enum<?> gamemode = (Enum<?>) GAMEMODE_FIELD.get(interactionManager);
            return NativeGameMode.valueOf(gamemode.name());
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return NativeGameMode.fromBukkit(receiver.getGameMode());
    }
}
