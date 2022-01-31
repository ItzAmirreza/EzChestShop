package me.deadlight.ezchestshop.Events;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PlayerTransactEvent extends Event {


    private OfflinePlayer owner;
    private OfflinePlayer customer;
    private double price;
    private LocalDateTime time;
    private boolean isBuy;
    private ItemStack item;
    private String itemName;
    private int count;
    private List<UUID> admins;
    private Block containerBlock;


    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }


    public PlayerTransactEvent(OfflinePlayer owner, OfflinePlayer customer, double price, boolean isBuy, ItemStack item, int count, List<UUID> admins, Block containerBlock) {
        this.owner = owner;
        this.customer = customer;
        this.price = price;
        this.time = LocalDateTime.now();
        this.isBuy = isBuy;
        this.item = item;
        this.itemName = Utils.getFinalItemName(item);
        this.count = count;
        this.admins = admins;
        this.containerBlock = containerBlock;
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

    public ItemStack getItem() {
        return this.item;
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
    public Block getContainerBlock() {
        return this.containerBlock;
    }

    public boolean isShareIncome() {
        return getBoolean(containerBlock, "shareincome");
    }

    public double getBuyPrice() {
        return getDouble(containerBlock, "buy");
    }

    public double getSellPrice() {
        return getDouble(containerBlock, "sell");
    }

    private boolean getBoolean(Block containerBlock, String key) {
        TileState state = (TileState) containerBlock.getState();
        PersistentDataContainer container = state.getPersistentDataContainer();
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.INTEGER) == 1;
    }

    private double getDouble(Block containerBlock, String key) {
        TileState state = (TileState) containerBlock.getState();
        PersistentDataContainer container = state.getPersistentDataContainer();
        return container.get(new NamespacedKey(EzChestShop.getPlugin(), key), PersistentDataType.DOUBLE);
    }








}
