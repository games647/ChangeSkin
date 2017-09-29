package com.github.games647.changeskin.bukkit.commands;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Shynixn
 */
public class SkullCommand implements CommandExecutor {

    //speed by letting the JVM optimize this
    //MethodHandle is only faster for static final fields
    private static final MethodHandle skullProfileSetter;

    static {
        MethodHandle methodHandle = null;
        try {
            Class<?> clazz = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".inventory.CraftMetaSkull");
            Field profileField = clazz.getDeclaredField("profile");
            profileField.setAccessible(true);

            methodHandle = MethodHandles.lookup().unreflectSetter(profileField);
        } catch (Exception ex) {
            ChangeSkinBukkit plugin = JavaPlugin.getPlugin(ChangeSkinBukkit.class);
            plugin.getLog().info("Cannot find loginProfile field for setting skin in offline mode", ex);
        }

        skullProfileSetter = methodHandle;
    }

    private final ChangeSkinBukkit plugin;

    public SkullCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "no-console");
            return true;
        }

        if (args.length == 0) {
            plugin.sendMessage(sender, "select-noargs");
        } else {
            String targetName = args[0].toLowerCase().replace("skin-", "");
            try {
                Player player = (Player) sender;
                int targetId = Integer.parseInt(targetName);
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> applySkin(player, plugin.getStorage().getSkin(targetId)));
            } catch (NumberFormatException numberFormatException) {
                plugin.sendMessage(sender, "invalid-skin-name");
            }
        }

        return true;
    }

    private void applySkin(Player player, SkinData skinData) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            setSkullSkin(player.getInventory().getItem(player.getInventory().getHeldItemSlot()), skinData);
            player.updateInventory();
        });
    }

    private void setSkullSkin(ItemStack itemStack, SkinData skinData) {
        try {
            if(itemStack == null || skinData == null || itemStack.getType() != Material.SKULL_ITEM)
                return;

            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

            WrappedGameProfile gameProfile = new WrappedGameProfile(UUID.randomUUID(), null);
            skullProfileSetter.invokeExact(skullMeta, gameProfile.getHandle());

            gameProfile.getProperties().put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(skinData));
            itemStack.setItemMeta(skullMeta);
        } catch (Error error) {
            //rethrow errors we shouldn't silence them like OutOfMemory
            throw error;
        } catch (Throwable throwable) {
            plugin.getLog().info("Failed to set skull item", throwable);
        }
    }

    private static String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName()
                .replace(".",  ",").split(",")[3];
    }
}
