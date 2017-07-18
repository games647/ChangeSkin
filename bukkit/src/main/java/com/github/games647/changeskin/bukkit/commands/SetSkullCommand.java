package com.github.games647.changeskin.bukkit.commands;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.model.SkinData;

import java.lang.reflect.Field;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * Created by Shynixn
 */
public class SetSkullCommand implements CommandExecutor {

    private final ChangeSkinBukkit plugin;

    public SetSkullCommand(ChangeSkinBukkit plugin) {
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
                Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> applySkin(player,plugin.getStorage().getSkin(targetId)));
            } catch (NumberFormatException numberFormatException) {
                plugin.sendMessage(sender, "invalid-skin-name");
            }
        }
        return true;
    }

    private void applySkin(Player player, SkinData skinData) {
        Bukkit.getServer().getScheduler().runTask(plugin, () -> {
            setSkullSkin(player.getInventory().getItem(player.getInventory().getHeldItemSlot()), skinData);
            player.updateInventory();
        });
    }

    private void setSkullSkin(ItemStack itemStack, SkinData skinData) {
        try {
            if(itemStack == null || skinData == null || itemStack.getType() != Material.SKULL_ITEM)
                return;

            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            Class<?> clazz = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".inventory.CraftMetaSkull");
            Object craftSkullMeta = clazz.cast(meta);
            Field field = craftSkullMeta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            WrappedGameProfile gameProfile = new WrappedGameProfile(UUID.randomUUID(), null);
            field.set(craftSkullMeta, gameProfile.getHandle());
            gameProfile.getProperties().put(ChangeSkinCore.SKIN_KEY, plugin.convertToProperty(skinData));
            itemStack.setItemMeta((ItemMeta) craftSkullMeta);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
    }
}
