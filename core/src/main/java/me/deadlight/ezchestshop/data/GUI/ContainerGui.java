package me.deadlight.ezchestshop.data.GUI;

import dev.triumphteam.gui.guis.GuiItem;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerGui {
    private int rows;
    private GuiItem background;
    private HashMap<String, ContainerGuiItem> items;

    public ContainerGui(FileConfiguration config, String path) {
        this.rows = config.getInt(path + ".rows");
        if (config.contains(path + ".background")) {
            if (config.isString(path + ".background") && config.getString(path + ".background").equalsIgnoreCase("default")) {
                this.background = getDefaultBackground();
            } else {
                this.background =  new GuiItem(ContainerGuiItem.fromPath(config, path + ".background").getItem(), event -> {
                    event.setCancelled(true);
                });
            }
        }
        // Convert the item stream to a Hashmap using the key as key and
        // ContainerGuiItem.fromPath(config, path + ".items." + key) as value
        this.items = config.getConfigurationSection(path + ".items").getKeys(false).stream()
                .collect(Collectors.toMap(key -> key, key -> ContainerGuiItem.fromPath(config, path + ".items." + key),
                        (a, b) -> b, HashMap::new));
    }

    public ContainerGui() {};

    public ContainerGui clone() {
        ContainerGui gui = new ContainerGui();
        gui.rows = this.rows;
        gui.background = this.background;
        gui.items = this.items;
        return gui;
    }

    public ContainerGuiItem getItem(String key) {
        if (key.equals("background")) {
            return new ContainerGuiItem("background", this.background.getItemStack(), -1, -1);
        }
        return items.get(key);
    }

    public List<String> getItemKeys() {
        return items.keySet().stream().collect(Collectors.toList());
    }

    public boolean hasItem(String key) {
        return items.containsKey(key);
    }

    public GuiItem getBackground() {
        return background;
    }

    public void setBackground(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta itemmeta = item.getItemMeta();
            itemmeta.setDisplayName(Utils.colorify("&d"));
            item.setItemMeta(itemmeta);
        }
        this.background = new GuiItem(item, event -> event.setCancelled(true));
    }

    public int getRows() {
        return rows;
    }

    public static GuiItem getDefaultBackground() {
        ItemStack glassis = new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1);
        ItemMeta glassmeta = glassis.getItemMeta();
        glassmeta.setDisplayName(Utils.colorify("&d"));
        glassis.setItemMeta(glassmeta);
        return new GuiItem(glassis, event -> event.setCancelled(true));
    }
}
