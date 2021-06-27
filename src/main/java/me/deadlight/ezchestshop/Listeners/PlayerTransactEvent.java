package me.deadlight.ezchestshop.Listeners;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PlayerTransactEvent extends Event {


    private OfflinePlayer owner;
    private OfflinePlayer customer;
    private double price;
    private LocalDateTime time;
    private boolean isBuy;
    private String itemName;
    private int count;
    private List<UUID> admins;
    private Chest chest;


    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }


    public PlayerTransactEvent(OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, String itemName, int count, List<UUID> admins, Chest chest) {
        this.owner = owner;
        this.customer = customer;
        this.price = price;
        this.time = LocalDateTime.now();
        this.isBuy = isBuy;
        this.itemName = itemName;
        this.count = count;
        this.admins = admins;
        this.chest = chest;
    }


    public OfflinePlayer getOwner() {
        return this.owner;
    }

    public OfflinePlayer getCustomer() {
        return this.customer;
    }

    public double getPrice() {
        return this.price;
    }
    public LocalDateTime getTime() {
        return this.time;
    }

    public boolean isBuy() {
        return this.isBuy;
    }

    public String getItemName() {
        return this.itemName;
    }
    public int getCount() {
        return this.count;
    }

    public List<UUID> getAdminsUUID() {
        return this.admins;
    }
    public Chest getChest() {
        return this.chest;
    }








}
