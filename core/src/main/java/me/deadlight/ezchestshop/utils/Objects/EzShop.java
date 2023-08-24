package me.deadlight.ezchestshop.utils.Objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EzShop {

    private Location location;
    private ShopSettings settings;
    private OfflinePlayer owner;
    private ItemStack shopItem;
    private double buyPrice;
    private double sellPrice;
    private SqlQueue sqlQueue;
    private List<String> shopViewers = new ArrayList<>();
    private List<String> shopLoaders = new ArrayList<>();



    public EzShop(Location location, OfflinePlayer owner, ItemStack shopItem, double buyPrice, double sellPrice, ShopSettings settings) {
        this.location = location;
        this.owner = owner;
        this.shopItem = shopItem;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.settings = settings;
        this.settings.assignShop(this);
        this.settings.createSqlQueue();
        this.createSqlQueue();
    }

    public EzShop(Location location, String ownerID, ItemStack shopItem, double buyPrice, double sellPrice, ShopSettings settings) {
        this.location = location;
        this.owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerID));
        this.shopItem = shopItem;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.settings = settings;
        this.settings.assignShop(this);
        this.settings.createSqlQueue();
        this.createSqlQueue();
    }

    public Location getLocation() {
        return location;
    }
    public ShopSettings getSettings() {
        return settings;
    }

    public ItemStack getShopItem() {
        return shopItem.clone();
    }

    public List<String> getShopViewers() {
        return shopViewers;
    }
    public List<String> getShopLoaders() {
        return shopLoaders;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setSettings(ShopSettings settings) {
        this.settings = settings;
    }
    public void setShopViewers(List<String> shopViewers) {
        this.shopViewers = shopViewers;
    }
    public void addShopViewer(String str) {
        if (this.shopViewers.contains(str)) return;
        this.shopViewers.add(str);
    }
    public void removeShopViewer(String str) {
        this.shopViewers.remove(str);
    }
    public void setShopLoaders(List<String> shopLoaders) {
        this.shopLoaders = shopLoaders;
    }
    public void addShopLoader(String str) {
        if (this.shopLoaders.contains(str)) return;
        this.shopLoaders.add(str);
    }
    public void removeShopLoader(String str) {
        this.shopLoaders.remove(str);
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

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }
}
