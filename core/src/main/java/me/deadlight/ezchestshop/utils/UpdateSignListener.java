package me.deadlight.ezchestshop.utils;

import org.bukkit.entity.Player;

public abstract class UpdateSignListener {

    private boolean cancelled = false;

    public abstract void listen(Player player, String[] array);

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
