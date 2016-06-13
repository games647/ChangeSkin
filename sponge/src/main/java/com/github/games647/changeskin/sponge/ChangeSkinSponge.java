package com.github.games647.changeskin.sponge;

import com.github.games647.changeskin.core.ChangeSkinCore;
import com.github.games647.changeskin.core.SkinStorage;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Inject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "changeskin", name = "ChangeSkin", version = "1.7"
        , url = "https://github.com/games647/ChangeSkin"
        , description = "Sponge plugin to change your skin server side")
public class ChangeSkinSponge {

    @Inject
    @DefaultConfig(sharedRoot = true)
    //We will place more than one config there (i.e. H2/SQLite database)
    private File defaultConfigFile;

    private final Logger logger;
    private final Game game;

    private ChangeSkinCore core;

    @Inject
    public ChangeSkinSponge(Logger logger, Game game) {
        this.logger = logger;
        this.game = game;
    }

    @Listener //load config and database
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        //deploy default config
        if (!defaultConfigFile.exists()) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(defaultConfigFile);
                Resources.copy(getClass().getResource("/config.yml"), fileOutputStream);
            } catch (IOException ioEx) {
                logger.error("Error deploying default config", ioEx);
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException ex) {
                        //ignore
                    }
                }
            }
        }

        YAMLConfigurationLoader configLoader = YAMLConfigurationLoader.builder().setFile(defaultConfigFile).build();
        try {
            ConfigurationNode rootNode = configLoader.load();

            ConfigurationNode storageNode = rootNode.getNode("storage");
            String driver = storageNode.getNode("driver").getString();
            String host = storageNode.getNode("host").getString();
            int port = storageNode.getNode("port").getInt();
            String database = storageNode.getNode("database").getString();
            String user = storageNode.getNode("username").getString();
            String pass = storageNode.getNode("password").getString();

            java.util.logging.Logger pluginLogger = java.util.logging.Logger.getLogger("ChangeSkin");

            core = new ChangeSkinCore(pluginLogger, defaultConfigFile.getParentFile());
            SkinStorage storage = new SkinStorage(core, driver, host, port, database, user, pass);
            core.setStorage(storage);

            try {
                storage.createTables();
            } catch (Exception ex) {
                logger.error("Failed to setup database. Disabling plugin...", ex);
                return;
            }

            List<String> defaultSkins = Lists.newArrayList();
            for (ConfigurationNode configurationNode : rootNode.getNode("default-skins").getChildrenList()) {
                defaultSkins.add(configurationNode.getString());
            }

            core.loadDefaultSkins(defaultSkins);

            loadLocale();
        } catch (IOException ioEx) {
            logger.error("Failed to load config", ioEx);
        }
    }

    @Listener //command and event register
    public void onInit(GameInitializationEvent initEvent) {
        CommandManager commandManager = game.getCommandManager();
//        commandManager.register(this, CommandSpec.builder()
//                .executor(new SetSkinCommand(this))
//                .arguments(GenericArguments.string(Text.of("skin"))
//                        , GenericArguments.optional(GenericArguments.string(Text.of("target"))))
//                .build(), "changeskin");

//        commandManager.register(this, CommandSpec.builder()
//                .executor(new SkinInvalidateCommand(this))
//                .build(), "skininvalidate");

        game.getEventManager().registerListeners(this, new LoginListener(this));
    }

    private void loadLocale() {
        //todo
    }

    public ChangeSkinCore getCore() {
        return core;
    }

    public Game getGame() {
        return game;
    }

    public Logger getLogger() {
        return logger;
    }
}
