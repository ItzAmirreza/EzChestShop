package me.deadlight.ezchestshop.Utils.Objects;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.PlayerContainer;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.TileState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class EzShop {

    private static LanguageManager lm = new LanguageManager();

    private Location location;
    private ShopSettings settings;
    private OfflinePlayer owner;
    private ItemStack shopItem;
    private double buyPrice;
    private double sellPrice;
    private SqlQueue sqlQueue;

    private List<UUID> shopViewers = new ArrayList<>();
    private List<UUID> shopLoaders = new ArrayList<>();

    //Map for Hologram Objects based on a OfflinePlayer.
    private HashMap<OfflinePlayer, Hologram> holoMap = new HashMap<>();

    public EzShop(Location location, OfflinePlayer owner, ItemStack shopItem, double buyPrice, double sellPrice, ShopSettings settings) {
        this.location = location;
        this.owner = owner;
        this.shopItem = shopItem;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.settings = settings;
        sqlQueue = new SqlQueue(location, settings);
    }

    public EzShop(Location location, String ownerID, ItemStack shopItem, double buyPrice, double sellPrice, ShopSettings settings) {
        this.location = location;
        this.owner = Bukkit.getOfflinePlayer(UUID.fromString(ownerID));
        this.shopItem = shopItem;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.settings = settings;
    }

    public Location getLocation() {
        return location;
    }
    public ShopSettings getSettings() {
        return settings;
    }
    public List<UUID> getShopViewers() {
        return shopViewers;
    }
    public List<UUID> getShopLoaders() {
        return shopLoaders;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setSettings(ShopSettings settings) {
        this.settings = settings;
    }
    public void setShopViewers(List<UUID> shopViewers) {
        this.shopViewers = shopViewers;
    }
    public void addShopViewer(UUID str) {
        if (this.shopViewers.contains(str)) return;
        this.shopViewers.add(str);
    }
    public void removeShopViewer(UUID str) {
        this.shopViewers.remove(str);
    }
    public void setShopLoaders(List<UUID> shopLoaders) {
        this.shopLoaders = shopLoaders;
    }
    public void addShopLoader(UUID str) {
        if (this.shopLoaders.contains(str)) return;
        this.shopLoaders.add(str);
    }
    public void removeShopLoader(UUID str) {
        this.shopLoaders.remove(str);
    }

    public boolean isShopLoader(OfflinePlayer player) {
        return shopLoaders.contains(player.getUniqueId());
    }

    public boolean isShopViewer(OfflinePlayer player) {
        return shopViewers.contains(player.getUniqueId());
    }

    public SqlQueue getSqlQueue() {
        return sqlQueue;
    }


    public ItemStack getShopItem() {
        return shopItem;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public Hologram getHologram(OfflinePlayer player) {
        if (holoMap.containsKey(player)) {
            return holoMap.get(player);
        } else {
            if (player.isOnline()) {
                Hologram holo = new Hologram(getTexts(player.getPlayer()), getItems(player.getPlayer()));
                holoMap.put(player, holo);
                return holo;
            }
            //No need to create new Data for Offline players, this should not be returned.
            return null;
        }
    }


    /**
     *
     * @param player the player to show the holograms to
     * @param loc the location where the shop container is that contains the persistent data
     */
    public static void showHologram(Player player, Location loc) {
        if (player == null || !ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        shop.addShopLoader(player.getUniqueId());
        if (Config.holodistancing_show_item_first) {
            // Show just the floating items or whatever defined.
            shop.getHologram(player).spawnLoaded();
        } else {
            // Show the item and the text.
            shop.updateHologramData(player);
            shop.getHologram(player).spawnLoaded().spawnLook();
        }
    }

    /**
     *
     * @param player the player to hide the holograms from
     * @param loc the location where the shop container is that contains the persistent data
     */
    public static void showHologramLook(Player player, Location loc) {
        if (player == null || !ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        PlayerContainer pc = PlayerContainer.get(player);
        if (pc.getLookedAtShop() != null) {
            hideHologramLook(player, pc.getLookedAtShop());
        }

        //make sure the Hologram is saved in the PlayerContainer so it can be removed later again.
        pc.setLookedShop(loc);
        shop.addShopViewer(player.getUniqueId());

        // Show the hologram text
        shop.updateHologramData(player);
        shop.getHologram(player).spawnLook();
    }

    /**
     *
     * @param loc the location where the shop container is that contains the persistent data
     */
    public static void hideHologram(Location loc) {
        if (!ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        //Hide the hologram compltely from all players looking at this shop.
        new ArrayList<>(shop.getShopLoaders()).forEach(id -> {
            hideHologram(Bukkit.getOfflinePlayer(id), loc);
        });
    }

    /**
     *
     * @param player the player to hide the holograms from
     * @param loc the location where the shop container is that contains the persistent data
     */
    public static void hideHologram(OfflinePlayer player, Location loc) {
        if (player == null || !ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        //(!true && !true) = false       (!false && !true) = false        (!false && !false) = true
        if (!shop.isShopViewer(player) && !shop.isShopLoader(player)) return;
        //Hide the hologram compltely from the defined player
        EzChestShop.logDebug("hide1: Loader: " + shop.isShopLoader(player) + ", Viewer: " + shop.isShopViewer(player));
        shop.removeShopLoader(player.getUniqueId());
        shop.removeShopViewer(player.getUniqueId());
        EzChestShop.logDebug("hide2: Loader: " + shop.isShopLoader(player) + ", Viewer: " + shop.isShopViewer(player));
        PlayerContainer.get(player).setLookedShop(null);
        // Hide the hologram
        shop.getHologram(player).destroyLoaded().destroyLook();
    }

    /**
     *
     * @param player the player to hide the holograms from
     * @param loc the location where the shop container is that contains the persistent data
     */
    public static void hideHologramLook(OfflinePlayer player, Location loc) {
        if (player == null || !ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        if (!shop.isShopViewer(player)) return;
        //Hide just the text or whatever defined from the player
        EzChestShop.logDebug("hidel1: Loader: " + shop.isShopLoader(player) + ", Viewer: " + shop.isShopViewer(player));
        shop.removeShopViewer(player.getUniqueId());
        EzChestShop.logDebug("hidel2: Loader: " + shop.isShopLoader(player) + ", Viewer: " + shop.isShopViewer(player));
        PlayerContainer.get(player).setLookedShop(null);
        // Hide the hologram
        shop.getHologram(player).destroyLook();
    }


    public static void updateAllHolograms() {
        for (Location sloc : ShopContainer.getShops()) {
            EzShop.updateHologram(sloc);
        }
    }

    public static void updateHologram(Location loc) {
        EzShop shop = ShopContainer.getShop(loc);
        if (!ShopContainer.isShop(loc)) return;
        new ArrayList<>(shop.getShopLoaders()).forEach(id -> {
            updateHologram(Bukkit.getOfflinePlayer(id), loc);
        });
    }

    public static void updateHologram(OfflinePlayer player, Location loc) {
        if (player == null || !ShopContainer.isShop(loc)) return;
        EzShop shop = ShopContainer.getShop(loc);
        shop.getHologram(player).destroyLoaded().destroyLook();
        shop.updateHologramData(player);
        if (shop.isShopLoader(player)) {
            shop.getHologram(player).spawnLoaded();
        }
        if (shop.isShopViewer(player)) {
            shop.getHologram(player).spawnLook();
        }
    }

    public Hologram updateHologramData(OfflinePlayer player) {
        if (player.isOnline()) {
            Hologram holo = new Hologram(getTexts(player.getPlayer()), getItems(player.getPlayer()));
            holoMap.put(player, holo);
            return holo;
        }
        return null;
    }

    private List<ASHologram> getTexts(Player player) {
        Location lineLocation = getHoloLoc(getLocation().getBlock()).clone().subtract(0, 0.1, 0);
        List<ASHologram> holoList = new ArrayList<>();
        String itemname = Utils.getFinalItemName(getShopItem());
        List<String> structure = new ArrayList<>(getSettings().isAdminshop() ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(getLocation()).getRotation().equals("down")) Collections.reverse(structure);
        List<String> possibleCounts = Utils.calculatePossibleAmount(player, getOwner(), player.getInventory().getStorageContents(),
                Utils.getBlockInventory(getLocation().getBlock()).getStorageContents(), getBuyPrice(), getSellPrice(), getShopItem());
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                String line = Utils.colorify(element.replace("%item%", itemname).replace("%buy%", Utils.formatNumber(getBuyPrice(), Utils.FormatType.HOLOGRAM)).
                        replace("%sell%", Utils.formatNumber(getSellPrice(), Utils.FormatType.HOLOGRAM)).replace("%currency%", Config.currency)
                        .replace("%owner%", getOwner().getName()).replace("%maxbuy%", possibleCounts.get(0)).replace("%maxsell%", possibleCounts.get(1)));
                if (getSettings().isDbuy() || getSettings().isDsell()) {
                    line = line.replaceAll("<separator>.*?<\\/separator>", "");
                    if (getSettings().isDbuy() && getSettings().isDsell()) {
                        line = lm.disabledButtonTitle();
                    } else if (getSettings().isDbuy()) {
                        line = line.replaceAll("<buy>.*?<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
                    } else if (getSettings().isDsell()) {
                        line = line.replaceAll("<sell>.*?<\\/sell>", "").replaceAll("<buy>|<\\/buy>", "");
                    }
                } else {
                    line = line.replaceAll("<separator>|<\\/separator>", "").replaceAll("<buy>|<\\/buy>", "").replaceAll("<sell>|<\\/sell>", "");
                }
                ASHologram hologram = new ASHologram(player, line, EntityType.ARMOR_STAND, lineLocation, false);
                holoList.add(hologram);
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }
        return holoList;
    }

    private List<FloatingItem> getItems(Player player) {
        Location lineLocation = getHoloLoc(getLocation().getBlock()).clone().subtract(0, 0.1, 0);
        List<FloatingItem> holoList = new ArrayList<>();
        List<String> structure = new ArrayList<>(getSettings().isAdminshop() ? Config.holostructure_admin : Config.holostructure);
        if (ShopContainer.getShopSettings(getLocation()).getRotation().equals("down")) Collections.reverse(structure);
        for (String element : structure) {
            if (element.equalsIgnoreCase("[Item]")) {
                lineLocation.add(0, 0.15 * Config.holo_linespacing, 0);
                FloatingItem floatingItem = new FloatingItem(player, getShopItem(), lineLocation);
                holoList.add(floatingItem);
                lineLocation.add(0, 0.35 * Config.holo_linespacing, 0);
            } else {
                lineLocation.add(0, 0.3 * Config.holo_linespacing, 0);
            }
        }
        return holoList;
    }

    private Location getHoloLoc(Block containerBlock) {
        Location holoLoc;
        Inventory inventory = Utils.getBlockInventory(containerBlock);
        PersistentDataContainer container = ((TileState) containerBlock.getState()).getPersistentDataContainer();
        String rotation = container.get(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING);
        rotation = rotation == null ? Config.settings_defaults_rotation : rotation;
        rotation = Config.holo_rotation ? rotation : Config.settings_defaults_rotation;
        //Add rotation checks
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
            holoLoc = leftchest.getLocation().clone().add(0.5D, 0, 0.5D).add(rightchest.getLocation().add(0.5D, 0, 0.5D)).multiply(0.5);
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
