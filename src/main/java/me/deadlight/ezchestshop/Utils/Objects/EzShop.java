package me.deadlight.ezchestshop.Utils.Objects;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class EzShop {

    private Location location;
    private ShopSettings settings;
    private List<String> shopViewers = new ArrayList<>();


    public EzShop(Location location, ShopSettings settings) {
        this.location = location;
        this.settings = settings;
    }

    public Location getLocation() {
        return location;
    }
    public ShopSettings getSettings() {
        return settings;
    }
    public List<String> getShopViewers() {
        return shopViewers;
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

}
