package me.deadlight.ezchestshop.Utils;

import org.bukkit.Location;

public class ASHologramObject {

    /**
     * Object used to store hologram data in order to compactly remove them at a later date.
     */

    private ASHologram hologram;
    private ASHologram hologram2;
    private FloatingItem floatingItem;
    private Location shopLocation;

    public ASHologramObject(ASHologram hologram, ASHologram hologram2, FloatingItem floatingItem, Location shopLocation) {
        this.hologram = hologram;
        this.hologram2 = hologram2;
        this.floatingItem = floatingItem;
        this.shopLocation = shopLocation;
    }


    public ASHologram getHologram() {
        return hologram;
    }

    public ASHologram getHologram2() {
        return hologram2;
    }

    public FloatingItem getFloatingItem() {
        return floatingItem;
    }

    public Location getLocation() {
        return shopLocation;
    }
}
