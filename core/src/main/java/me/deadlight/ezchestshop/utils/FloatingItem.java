package me.deadlight.ezchestshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

public class FloatingItem {

    private int entityID;
    private Player player;
    private Location location;

    static VersionUtils versionUtils;

    static {
        try {

            if (Utils.isFolia()) {
                versionUtils = (VersionUtils) Class.forName("me.deadlight.ezchestshop.utils.v1_20_R3").newInstance();

            } else {
                String packageName = Utils.class.getPackage().getName();
                String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                versionUtils = (VersionUtils) Class.forName(packageName + "." + internalsName).newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "EzChestShop could not find a valid implementation for this server version.");
        }
    }

    public FloatingItem(Player player, ItemStack itemStack, Location location) {

        this.player = player;
        this.entityID = (int) (Math.random() * Integer.MAX_VALUE);
        this.location = location;
        versionUtils.spawnFloatingItem(player, location, itemStack, entityID);

    }

    public void destroy() {
        versionUtils.destroyEntity(player, entityID);
    }

    public void teleport(Location location) {
        this.location = location;
        versionUtils.teleportEntity(player, entityID, location);
    }

    public Location getLocation() {
        return location;
    }

}
