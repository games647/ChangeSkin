package com.github.games647.changeskin.bungee.command;

import com.github.games647.changeskin.bungee.ChangeSkinBungee;
import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SkinFormatter;

import java.util.Optional;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

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
            plugin.sendMessage(sender, "no-console");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        UserPreference preference = plugin.getLoginSession((PendingConnection) player);
        Optional<SkinModel> optSkin = preference.getTargetSkin();
        if (optSkin.isPresent()) {
            String template = plugin.getCore().getMessage("skin-info");
            sender.sendMessage(TextComponent.fromLegacyText(formatter.apply(template, optSkin.get())));
        } else {
            plugin.sendMessage(sender, "skin-not-found");
        }
    }
}
