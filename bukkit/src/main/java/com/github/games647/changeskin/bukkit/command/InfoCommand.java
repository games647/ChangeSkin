package com.github.games647.changeskin.bukkit.command;

import com.github.games647.changeskin.bukkit.BukkitLocaleManager;
import com.github.games647.changeskin.bukkit.ChangeSkinBukkit;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SkinFormatter;

import java.util.Optional;
import java.util.UUID;

import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand implements CommandExecutor {

    private final ChangeSkinBukkit plugin;
    private final SkinFormatter formatter = new SkinFormatter();

    public InfoCommand(ChangeSkinBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.getLocaleManager().sendMessage(sender, "no-console");
            return true;
        }

        Player player = (Player) sender;
        UUID uniqueId = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UserPreference preferences = plugin.getStorage().getPreferences(uniqueId);
            Bukkit.getScheduler().runTask(plugin, () -> sendSkinDetails(uniqueId, preferences));
        });

        return true;
    }

    private void sendSkinDetails(UUID uuid, UserPreference preference) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            Optional<SkinModel> optSkin = preference.getTargetSkin();
            BukkitLocaleManager localeManager = plugin.getLocaleManager();
            if (optSkin.isPresent()) {
                String template = localeManager.getLocalizedMessage(player, "skin-info");
                String formatted = formatter.formatSkin(template, optSkin.get(), localeManager.getLocale(player));
                player.spigot().sendMessage(ComponentSerializer.parse(formatted));
            } else {
                localeManager.sendMessage(player, "skin-not-found");
            }
        }
    }
}
