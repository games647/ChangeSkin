package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SkinFormatter;

import java.util.Optional;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.chat.ComponentSerializer;

public class InfoCommand extends Command {

    private final ChangeSkinBungee plugin;
    private final SkinFormatter formatter = new SkinFormatter();

    public InfoCommand(ChangeSkinBungee plugin) {
        super("skin-info", plugin.getName().toLowerCase() + ".command.skininfo");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (!(sender instanceof ProxiedPlayer)) {
            plugin.getLocaleManager().sendMessage(sender, "no-console");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        UserPreference preference = plugin.getLoginSession(player.getPendingConnection());
        Optional<SkinModel> optSkin = preference.getTargetSkin();
        if (optSkin.isPresent()) {
            String template = plugin.getLocaleManager().getLocalizedMessage(player, "skin-info");
            String formatted = formatter.apply(template, optSkin.get());
            sender.sendMessage(ComponentSerializer.parse(formatted));
        } else {
            plugin.getLocaleManager().sendMessage(sender, "skin-not-found");
        }
    }
}
