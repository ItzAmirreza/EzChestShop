package me.deadlight.ezchestshop.utils.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EzTradeShop {

    private Location location;
    private TradeShopSettings settings;
    private OfflinePlayer owner;
    private ItemStack item1;
    private ItemStack item2;
    private SqlQueue sqlQueue;
    private List<String> shopViewers = new ArrayList<>();
    private List<String> shopLoaders = new ArrayList<>();



    public EzTradeShop(Location location, OfflinePlayer owner, ItemStack item1, ItemStack item2, TradeShopSettings settings) {
        this.location = location;
        this.owner = owner;
        this.item1 = item1;
        this.item2 = item2;
        this.settings = settings;
        this.settings.assignShop(this);
        this.settings.createSqlQueue();
        this.createSqlQueue();
    }

    public EzTradeShop(Location location, String ownerID, ItemStack item1, ItemStack item2, TradeShopSettings settings) {
        this.location = location;
        this.owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerID));
        this.item1 = item1;
        this.item2 = item2;
        this.settings = settings;
        this.settings.assignShop(this);
        this.settings.createSqlQueue();
        this.createSqlQueue();
    }

    public Location getLocation() {
        return location;
    }
    public TradeShopSettings getSettings() {
        return settings;
    }

    public ItemStack getItem1() {
        return item1.clone();
    }
    public void setItem1(ItemStack item1) {
        this.item1 = item1;
    }

    public ItemStack getItem2() {
        return item2.clone();
    }
    public void setItem2(ItemStack item2) {
        this.item2 = item2;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setSettings(TradeShopSettings settings) {
        this.settings = settings;
    }

    public UUID getOwnerID() {
        return owner.getUniqueId();
    }
    public SqlQueue getSqlQueue() {
        return sqlQueue;
    }
    public void createSqlQueue() {
        this.sqlQueue = new SqlQueue(this.getLocation(), getSettings(), this);
    }
    public void setOwner(OfflinePlayer owner) {this.owner = owner;}
}
