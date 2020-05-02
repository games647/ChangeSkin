package com.github.games647.changeskin.bukkit.events;

import com.github.games647.changeskin.core.model.skin.SkinModel;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerChangeSkinEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final SkinModel skinModel;

    public PlayerChangeSkinEvent(Player player, SkinModel skinModel){
        super(player);
        this.skinModel = skinModel;
    }

    public SkinModel getSkinModel() {
        return skinModel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
