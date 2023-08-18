package me.deadlight.ezchestshop.Utils.Holograms;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * This class is used to create a hologram that is bound to a block.
 * It will manage and update the shop hologram for all the players that are viewing it.
 * contents
 * location
 * rotation
 */

public class BlockBoundHologram {

    public enum HologramRotation {
        NORTH, SOUTH, EAST, WEST, UP, DOWN;
    }

    private Location location;
    private HologramRotation rotation;
    private List<String> contents;

    // Viewers
    private HashMap<UUID, PlayerBlockBoundHologram> viewerHolograms = new HashMap<>();
    private HashMap<UUID, PlayerBlockBoundHologram> inspectorHolograms = new HashMap<>();

    // Replacements
    protected HashMap<String, String> textDefaultReplacements;
    protected HashMap<String, ItemStack> itemDefaultReplacements;
    protected HashMap<String,  Boolean> conditionalDefaultTags;



    /*
 ███████████             █████     ████   ███              ██████████              █████
░░███░░░░░███           ░░███     ░░███  ░░░              ░░███░░░░███            ░░███
 ░███    ░███ █████ ████ ░███████  ░███  ████   ██████     ░███   ░░███  ██████   ███████    ██████
 ░██████████ ░░███ ░███  ░███░░███ ░███ ░░███  ███░░███    ░███    ░███ ░░░░░███ ░░░███░    ░░░░░███
 ░███░░░░░░   ░███ ░███  ░███ ░███ ░███  ░███ ░███ ░░░     ░███    ░███  ███████   ░███      ███████
 ░███         ░███ ░███  ░███ ░███ ░███  ░███ ░███  ███    ░███    ███  ███░░███   ░███ ███ ███░░███
 █████        ░░████████ ████████  █████ █████░░██████     ██████████  ░░████████  ░░█████ ░░████████
░░░░░          ░░░░░░░░ ░░░░░░░░  ░░░░░ ░░░░░  ░░░░░░     ░░░░░░░░░░    ░░░░░░░░    ░░░░░   ░░░░░░░░
      */

    public BlockBoundHologram(Location location, HologramRotation rotation, List<String> contents,
                              HashMap<String, String> textReplacements, HashMap<String, ItemStack> itemReplacements,
                              HashMap<String,  Boolean> conditionalTags) {
        this.location = location;
        this.rotation = rotation;
        this.contents = contents;
        this.textDefaultReplacements = textReplacements;
        this.itemDefaultReplacements = itemReplacements;
        this.conditionalDefaultTags = conditionalTags;
    }



    public Location getLocation() {
        return location;
    }

    public HologramRotation getRotation() {
        return rotation;
    }

    public List<String> getContents() {
        return contents;
    }

    /*
 ██████   ██████              █████  ███     ██████                 █████████   █████               █████
░░██████ ██████              ░░███  ░░░     ███░░███               ███░░░░░███ ░░███               ░░███
 ░███░█████░███   ██████   ███████  ████   ░███ ░░░  █████ ████   ░███    ░░░  ███████    ██████   ███████    ██████
 ░███░░███ ░███  ███░░███ ███░░███ ░░███  ███████   ░░███ ░███    ░░█████████ ░░░███░    ░░░░░███ ░░░███░    ███░░███
 ░███ ░░░  ░███ ░███ ░███░███ ░███  ░███ ░░░███░     ░███ ░███     ░░░░░░░░███  ░███      ███████   ░███    ░███████
 ░███      ░███ ░███ ░███░███ ░███  ░███   ░███      ░███ ░███     ███    ░███  ░███ ███ ███░░███   ░███ ███░███░░░
 █████     █████░░██████ ░░████████ █████  █████     ░░███████    ░░█████████   ░░█████ ░░████████  ░░█████ ░░██████
░░░░░     ░░░░░  ░░░░░░   ░░░░░░░░ ░░░░░  ░░░░░       ░░░░░███     ░░░░░░░░░     ░░░░░   ░░░░░░░░    ░░░░░   ░░░░░░
                                                      ███ ░███
                                                     ░░██████
                                                      ░░░░░░
     */

    public void hideForAll() {
        for (PlayerBlockBoundHologram hologram : viewerHolograms.values()) {
            hologram.hide();
        }
    }

    /**
     * Updates the hologram location
     */
    public void updateLocation(Location location) {
        this.location = location;
    }

    public void updateContents(List<String> contents) {
        this.contents = contents;
    }

    public void updateRotation(HologramRotation rotation) {
        this.rotation = rotation;
    }

    public PlayerBlockBoundHologram getPlayerHologram(Player player) {
        if (!viewerHolograms.containsKey(player.getUniqueId())) {
            PlayerBlockBoundHologram hologram =
                    new PlayerBlockBoundHologram(player, this, textDefaultReplacements,
                            itemDefaultReplacements, conditionalDefaultTags);
            viewerHolograms.put(player.getUniqueId(), hologram);
        }
        return viewerHolograms.get(player.getUniqueId());
    }

    protected void removeViewer(Player player) {
        if (!viewerHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        viewerHolograms.remove(player.getUniqueId());
    }

    public boolean hasInspector(Player player) {
        return inspectorHolograms.containsKey(player.getUniqueId());
    }

    protected void removeInspector(Player player) {
        if (!inspectorHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        inspectorHolograms.remove(player.getUniqueId());
    }
    protected void addInspector(Player player, PlayerBlockBoundHologram hologram) {
        if (inspectorHolograms.containsKey(player.getUniqueId())) {
            return;
        }
        inspectorHolograms.put(player.getUniqueId(), hologram);
    }

    /*
 █████   █████          ████
░░███   ░░███          ░░███
 ░███    ░███   ██████  ░███  ████████   ██████  ████████   █████
 ░███████████  ███░░███ ░███ ░░███░░███ ███░░███░░███░░███ ███░░
 ░███░░░░░███ ░███████  ░███  ░███ ░███░███████  ░███ ░░░ ░░█████
 ░███    ░███ ░███░░░   ░███  ░███ ░███░███░░░   ░███      ░░░░███
 █████   █████░░██████  █████ ░███████ ░░██████  █████     ██████
░░░░░   ░░░░░  ░░░░░░  ░░░░░  ░███░░░   ░░░░░░  ░░░░░     ░░░░░░
                              ░███
                              █████
                             ░░░░░
    */

    private void hideAndShow() {
        for (PlayerBlockBoundHologram hologram : viewerHolograms.values()) {
            hologram.hide();
            hologram.show();
        }
    }


    public Location getHoloLoc(Block containerBlock) {
        Location holoLoc = containerBlock.getLocation();
        Inventory inventory = Utils.getBlockInventory(containerBlock);

        PersistentDataContainer container = ((TileState) containerBlock.getState()).getPersistentDataContainer();
        String rotation = container.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"),
                PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;
        // Add rotation checks
        switch (rotation) {
            case "north":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, -0.8));
                break;
            case "east":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0.8, 0, 0));
                break;
            case "south":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 0, 0.8));
                break;
            case "west":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(-0.8, 0, 0));
                break;
            case "down":
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, -1.5, 0));
                break;
            default:
                holoLoc = getCentralLocation(containerBlock, inventory, new Vector(0, 1, 0));
                break;
        }


        return holoLoc;
    }

    private Location getCentralLocation(Block containerBlock, Inventory inventory, Vector direction) {
        Location holoLoc;
        if (inventory instanceof DoubleChestInventory) {
            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D)
                    .add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
            if (direction.getY() == 0) {
                Location lloc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D);
                Location hloc = holoLoc.clone();
                double angle = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle = (-(angle / Math.PI) * 360.0d) / 2.0d + 180.0d;
                hloc = hloc.add(direction);
                double angle2 = (Math.atan2(hloc.getX() - lloc.getX(), hloc.getZ() - lloc.getZ()));
                angle2 = (-(angle2 / Math.PI) * 360.0d) / 2.0d + 180.0d;
                if (angle == angle2 || angle == angle2 - 180 || angle == angle2 + 180) {
                    holoLoc.add(direction.multiply(1.625));
                } else {
                    holoLoc.add(direction);
                }
            } else {
                holoLoc.add(direction);
            }

        } else {
            holoLoc = containerBlock.getLocation().clone().add(0.5D, 0, 0.5D).add(direction);
        }
        return holoLoc;
    }

}
