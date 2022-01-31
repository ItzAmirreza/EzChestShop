package me.deadlight.ezchestshop.Events;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShulkerShopDropEvent extends Event {
    //only trigger via piston movement

    private Item droppedShulker;
    private Location previousShulkerLocation;


    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public ShulkerShopDropEvent(Item droppedShulker, Location previousShulkerLocation) {
        this.droppedShulker = droppedShulker;
        this.previousShulkerLocation = previousShulkerLocation;
    }



    public Item getDroppedShulker() {
        return this.droppedShulker;
    }

    public Location getPreviousShulkerLocation() {
        return this.previousShulkerLocation;
    }





}
