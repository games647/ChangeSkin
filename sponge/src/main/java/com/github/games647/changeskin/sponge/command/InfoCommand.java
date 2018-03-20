package com.github.games647.changeskin.sponge.command;

import com.github.games647.changeskin.core.model.UserPreference;
import com.github.games647.changeskin.core.model.skin.SkinModel;
import com.github.games647.changeskin.core.shared.SkinFormatter;
import com.github.games647.changeskin.sponge.ChangeSkinSponge;
import com.github.games647.changeskin.sponge.PomData;
import com.google.inject.Inject;

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class InfoCommand implements CommandExecutor, ChangeSkinCommand {

    @Inject
    private ChangeSkinSponge plugin;

    @Inject
    private SkinFormatter formatter;


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            plugin.sendMessage(src, "no-console");
            return CommandResult.empty();
        }

        UUID uniqueId = ((Player) src).getUniqueId();
        Task.builder().async()
                .execute(() -> {
                    UserPreference preferences = plugin.getCore().getStorage().getPreferences(uniqueId);
                    Task.builder().execute(() -> sendSkinDetails(uniqueId, preferences)).submit(plugin);
                })
                .submit(plugin);

        return null;
    }

    @Override
    public CommandSpec buildSpec() {
        return CommandSpec.builder()
                .executor(this)
                .permission(PomData.ARTIFACT_ID + ".command.skininfo.base")
                .build();
    }

    private void sendSkinDetails(UUID uuid, UserPreference preference) {
        Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
        if (optPlayer.isPresent()) {
            Player player = optPlayer.get();

            Optional<SkinModel> optSkin = preference.getTargetSkin();
            if (optSkin.isPresent()) {
                String template = plugin.getCore().getMessage("skin-info");
                String formatted = formatter.apply(template, optSkin.get());

                Text text = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(formatted);
                player.sendMessage(text);
            } else {
                plugin.sendMessage(player, "skin-not-found");
            }
        }
    }
}
