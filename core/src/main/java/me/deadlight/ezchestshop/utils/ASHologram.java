package me.deadlight.ezchestshop.utils;

import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ASHologram {

    private int entityID;
    private String name;
    private Player handler;

    private Location location;

    public ASHologram(Player p, String name,Location location) {
        this.name = name;
        this.entityID = (int) (Math.random() * Integer.MAX_VALUE);
        this.handler = p;
        this.location = location;
        Utils.versionUtils.spawnHologram(p, location, name, entityID);
    }


    public void destroy() {
        Utils.versionUtils.destroyEntity(handler, entityID);
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void teleport(Location location) {
        this.location = location;
        Utils.versionUtils.teleportEntity(handler, entityID, location);
    }

    public void rename(String name) {
        this.name = name;
        Utils.versionUtils.renameEntity(handler, entityID, name);
    }
}