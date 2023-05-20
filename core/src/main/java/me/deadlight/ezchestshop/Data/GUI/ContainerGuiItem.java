package me.deadlight.ezchestshop.Data.GUI;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ContainerGuiItem {

    private String name;
    private ItemStack item;
    private int row;
    private int column;

    public ContainerGuiItem(String name, ItemStack item, int row, int column) {
        this.name = name;
        this.item = item;
        this.row = row;
        this.column = column;
    }

    public static ContainerGuiItem fromPath(FileConfiguration config, String path) {
        ItemStack item;
        if (config.contains(path + ".material")) {
            String material = config.getString(path + ".material");
            if (material == null || material.equals("")) {
                item = new ItemStack(Material.AIR);
            } else {
                Material mat = Material.matchMaterial(material);
                if (mat == null) {
                    item = new ItemStack(Material.AIR);
                } else {
                    item = new ItemStack(mat);
                }
            }
        } else {
            item = new ItemStack(Material.AIR);
        }
        if (item.getType() != Material.AIR) {
            int amount = 1;
            if (config.contains(path + ".count")) {
                amount = config.getInt(path + ".count");
                //min max the item count, so it doesn't go over 64 or under 1
                amount = Math.min(Math.max(amount, 1), item.getType().getMaxStackSize());
            }
            item.setAmount(amount);
            if (config.contains(path + ".enchanted") && config.getBoolean(path + ".enchanted")) {
                item.addUnsafeEnchantment(Enchantment.LURE, 1);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(meta);
            }
        }

        int row = 1;
        if (config.contains(path + ".row")) {
            row = config.getInt(path + ".row");
            //min max the row, so it doesn't go over 6 or under 1
            row = Math.min(Math.max(row, 1), 6);
        }

        int column = 1;
        if (config.contains(path + ".column")) {
            column = config.getInt(path + ".column");
            //min max the column, so it doesn't go over 9 or under 1
            column = Math.min(Math.max(column, 1), 9);
        }

        return new ContainerGuiItem(path.split("\\.")[path.split("\\.").length - 1],
                item, row, column);
    }

    public String getName() {
        return name;
    }

    public int getSlot() {
        return ((row - 1) * 9) + column - 1;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public ContainerGuiItem setLore(String... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return this;
    }

    public ContainerGuiItem setName(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return this;
    }

    public ContainerGuiItem setLore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
        return this;
    }

    public ContainerGuiItem setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }
}
