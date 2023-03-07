package me.deadlight.ezchestshop.GUIs;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGui;
import me.deadlight.ezchestshop.Data.GUI.ContainerGuiItem;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Listeners.UpdateChecker;
import me.deadlight.ezchestshop.Utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class GuiEditorGUI {

    public static HashMap<Player, HashMap<Integer, Integer>> guiEditorSameSlots = new HashMap<>();
    public static HashMap<Player, Boolean> guiItemSelector = new HashMap<>();
    public static HashMap<Player, String> guiItemMover = new HashMap<>();

    public void showGuiEditorOverview(Player player) {

        Gui gui = Gui.gui().rows(3).title(Component.text(ChatColor.AQUA + "Gui Editor")).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.getFiller().fill(ContainerGui.getDefaultBackground());
        AtomicInteger i = new AtomicInteger();
        GuiData.getConfig().getKeys(false).forEach(key -> {
            ItemStack item;
            GuiData.GuiType type = GuiData.GuiType.valueOf(key.toUpperCase().replace("-", "_"));
            switch (type) {
                case SHOP_SETTINGS:
                    item = new ItemStack(Material.SMITHING_TABLE);
                    break;
                case TRANSACTION_LOGS:
                    item = new ItemStack(Material.PAPER);
                    break;
                case HOLOGRAM_MESSAGES_MANAGER:
                    item = new ItemStack(Material.WRITABLE_BOOK);
                    break;
                default:
                    item = new ItemStack(Material.CHEST);
            }
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + Utils.capitalizeFirstSplit(type.toString()));
            item.setItemMeta(meta);
            GuiItem GuiItem = new GuiItem(item, event -> {
                event.setCancelled(true);
                showGuiSettingsEditor(player, type);
            });
            // Simple way of spacing them out. If more items are needed one day, increase the row count and change the row/column calculation
            gui.setItem(2, 2 + i.get() * 2, GuiItem);
            i.incrementAndGet();
        });

        gui.open(player);

    }

    public void showGuiSettingsEditor(Player player, GuiData.GuiType type) {
        String guiName = type.toString().replace("_", "-").toLowerCase();

        FileConfiguration config = GuiData.getConfig();
        ContainerGui container = GuiData.getViaType(type);

        Gui gui = Gui.gui().rows(4).title(Component.text(ChatColor.AQUA + Utils.capitalizeFirstSplit(type.toString()) + " Settings")).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        gui.getFiller().fill(ContainerGui.getDefaultBackground());

        // Items:
        ItemStack itemsInfo = new ItemStack(Material.PAPER);
        ItemMeta itemsInfoMeta = itemsInfo.getItemMeta();
        itemsInfoMeta.setDisplayName(ChatColor.YELLOW + "Modify the Items");
        itemsInfo.setItemMeta(itemsInfoMeta);
        gui.setItem(2, 3, new GuiItem(itemsInfo, event -> {
            event.setCancelled(true);
        }));
        ItemStack itemItem = new ItemStack(Material.CHEST);
        ItemMeta itemMeta = itemItem.getItemMeta();
        itemMeta.setDisplayName(ChatColor.YELLOW + "Items: " + ChatColor.WHITE + container.getItemKeys().size());
        itemMeta.setLore(Arrays.asList(ChatColor.YELLOW + "Click here to change the", ChatColor.YELLOW + "items in this gui"));
        itemItem.setItemMeta(itemMeta);
        gui.setItem(3, 3, new GuiItem(itemItem, event -> {
            event.setCancelled(true);
            if (UpdateChecker.getGuiOverflow(type) == -1) {
                showGuiInEditor(player, type);
            } else {
                player.sendMessage(ChatColor.RED + "You have too many items in this gui, they are overflowing the rows! " +
                        "You need at least " + UpdateChecker.getGuiOverflow(type) + " rows to fit all the items!");
            }
        }));

        // Background:
        ItemStack backgroundInfo = new ItemStack(Material.PAPER);
        ItemMeta backgroundInfoMeta = backgroundInfo.getItemMeta();
        backgroundInfoMeta.setDisplayName(ChatColor.YELLOW + "Modify the Background");
        backgroundInfo.setItemMeta(backgroundInfoMeta);
        gui.setItem(2, 5, new GuiItem(backgroundInfo, event -> {
            event.setCancelled(true);
        }));
        ItemStack backgroundItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundItemMeta = backgroundItem.getItemMeta();
        if (container.getBackground() != null) {backgroundItemMeta.setDisplayName(ChatColor.YELLOW + "Background: " + ChatColor.WHITE +
                (config.isString(guiName + ".background") && config.getString(guiName + ".background").equals("default") ? "Default" : "Custom"));
        } else {
            backgroundItemMeta.setDisplayName(ChatColor.YELLOW + "Background: " + ChatColor.WHITE + "None");
        }
        backgroundItemMeta.setLore(Arrays.asList(ChatColor.YELLOW + "Click here to change", ChatColor.YELLOW + "the background item"));
        backgroundItem.setItemMeta(backgroundItemMeta);
        gui.setItem(3, 5, new GuiItem(backgroundItem, event -> {
            event.setCancelled(true);
            showItemEditor(player, type, "background");
        }));

        // Rows:
        ItemStack rowsInfo = new ItemStack(Material.PAPER);
        ItemMeta rowsInfoMeta = rowsInfo.getItemMeta();
        rowsInfoMeta.setDisplayName(ChatColor.YELLOW + "Change the number of Rows");
        rowsInfo.setItemMeta(rowsInfoMeta);
        gui.setItem(2, 7, new GuiItem(rowsInfo, event -> {
            event.setCancelled(true);
        }));
        ItemStack rowsItem = new ItemStack(Material.RAIL);
        ItemMeta rowsItemMeta = rowsItem.getItemMeta();
        rowsItemMeta.setDisplayName(ChatColor.YELLOW + "Rows: " + ChatColor.WHITE + container.getRows());
        rowsItemMeta.setLore(Arrays.asList(ChatColor.YELLOW + "Click here to change", ChatColor.YELLOW + "the number of rows",
                ChatColor.GRAY + "Left click to decrease", ChatColor.GRAY + "Right click to increase"));
        rowsItem.setItemMeta(rowsItemMeta);
        gui.setItem(3, 7, new GuiItem(rowsItem, event -> {
            event.setCancelled(true);
            if (event.isRightClick()) {
                if (container.getRows() < 6) {
                    config.set(guiName + ".rows", container.getRows() + 1);
                    try {
                        config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                        GuiData.loadGuiData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showGuiSettingsEditor(player, type);
                    new UpdateChecker().resetGuiCheck();
                }
            } else if (event.isLeftClick()) {
                if (container.getRows() > 1) {
                    config.set(guiName + ".rows", container.getRows() - 1);
                    try {
                        config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                        GuiData.loadGuiData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showGuiSettingsEditor(player, type);
                    new UpdateChecker().resetGuiCheck();
                }
            }
        }));

        // Back:
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(4, 1, new GuiItem(back, event -> {
            event.setCancelled(true);
            showGuiEditorOverview(player);
        }));

        gui.open(player);
    }

    public void showGuiInEditor(Player player, GuiData.GuiType type) {
        String guiName = type.toString().replace("_", "-").toLowerCase();

        FileConfiguration config = GuiData.getConfig();
        ContainerGui container = GuiData.getViaType(type);

        Gui gui = Gui.gui().rows(container.getRows()).title(Component.text(ChatColor.AQUA + Utils.capitalizeFirstSplit(type.toString()) + " Editor")).create();
        gui.setDefaultClickAction(event -> event.setCancelled(true));
        GuiItem filler = container.getBackground();
        ItemStack fillerItem = new ItemStack(filler.getItemStack().clone());
        if (fillerItem.getType() == Material.AIR) {
            fillerItem = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
            ItemMeta materialMeta = fillerItem.getItemMeta();
            materialMeta.setDisplayName(ChatColor.RED + "AIR");
            fillerItem.setItemMeta(materialMeta);
        }
        ItemMeta fillerMeta = fillerItem.getItemMeta();
        fillerMeta.setLore(Arrays.asList(ChatColor.GRAY + "- Swap Hand click (F) to add", ChatColor.GRAY + "  a new (valid) item."));
        fillerItem.setItemMeta(fillerMeta);
        filler.setItemStack(fillerItem);
        gui.getFiller().fill(filler);

        HashMap<Integer, List<String>> sameSlotItems = new HashMap<>();

        gui.setDefaultTopClickAction(event -> {
            event.setCancelled(true);
            if (guiItemMover.containsKey(player)) {
                // Put the item from guiItemMover into the new slot. Calculate the row and column
                int row = event.getSlot() / 9 + 1;
                int column = event.getSlot() % 9 + 1;

                config.set(guiName + ".items." + guiItemMover.get(player) + ".row", row);
                config.set(guiName + ".items." + guiItemMover.get(player) + ".column", column);
                try {
                    config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                    GuiData.loadGuiData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showGuiInEditor(player, type);
                guiItemMover.remove(player);
            } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
                showGuiItemAdder(player, type, event.getSlot() / 9 + 1, event.getSlot() % 9 + 1);
            }
        });

        container.getItemKeys().forEach(key -> {
            if (key.equals("shop-item")) {
                ContainerGuiItem cgi = container.getItem(key);
                ItemStack item = new ItemStack(Material.CHEST);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Shop Item");
                meta.setLore(Arrays.asList(ChatColor.GRAY + "- Left click to modify", ChatColor.GRAY + "  this item!",
                        ChatColor.GRAY + "- Drop click (Q) to remove", ChatColor.GRAY + "- Shift click to start moving",
                        ChatColor.GRAY + "  click again to place the item.", ChatColor.GRAY + "- Swap Hand click (F) to add",
                        ChatColor.GRAY + "  a new (valid) item."));
                item.setItemMeta(meta);
                gui.setItem(cgi.getSlot(), new GuiItem(item, event -> {
                    event.setCancelled(true);
                    if (event.isShiftClick()) {
                        guiItemMover.put(player, key);
                    } else if (event.isLeftClick()) {
                        showItemEditor(player, type, key);
                    } else if (event.getClick() == ClickType.DROP) {
                        config.set(guiName + ".items." + key, null);
                        try {
                            config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                            GuiData.loadGuiData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showGuiInEditor(player, type);
                    }
                }));
            } else {
                ContainerGuiItem cgi = container.getItem(key).setName(ChatColor.YELLOW + Utils.capitalizeFirstSplit(key.replace("-", "_")));
                if (sameSlotItems.containsKey(cgi.getSlot())) {
                    sameSlotItems.get(cgi.getSlot()).add(key);
                } else {
                    sameSlotItems.put(cgi.getSlot(),
                            new ArrayList<String>(Arrays.asList(key)));
                }
                ItemStack item = cgi.getItem().clone();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW +Utils.capitalizeFirstSplit(key.replace("-", "_")));
                meta.setLore(Arrays.asList(ChatColor.GRAY + "- Left click to modify", ChatColor.GRAY + "  this item!",
                        ChatColor.GRAY + "- Drop click (Q) to remove", ChatColor.GRAY + "- Shift click to start moving",
                        ChatColor.GRAY + "  click again to place the item.", ChatColor.GRAY + "- Swap Hand click (F) to add",
                        ChatColor.GRAY + "  a new (valid) item."));
                item.setItemMeta(meta);
                gui.setItem(cgi.getSlot(), new GuiItem(item, event -> {
                    event.setCancelled(true);
                    if (event.isShiftClick()) {
                        guiItemMover.put(player, key);
                    } else if (event.isLeftClick()) {
                        showItemEditor(player, type, key);
                    } else if (event.getClick() == ClickType.DROP) {
                        config.set(guiName + ".items." + key, null);
                        try {
                            config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                            GuiData.loadGuiData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showGuiInEditor(player, type);
                    }
                }));
            }
        });

        sameSlotItems.keySet().forEach(slot -> {
            if (sameSlotItems.get(slot).size() > 1) {
                if (!guiEditorSameSlots.containsKey(player)) {
                    guiEditorSameSlots.put(player, new HashMap<>());
                }
                if (!guiEditorSameSlots.get(player).containsKey(slot)) {
                    guiEditorSameSlots.get(player).put(slot, 0);
                }
                int index = guiEditorSameSlots.get(player).get(slot);
                String key = sameSlotItems.get(slot).get(index);
                ItemStack item = container.getItem(sameSlotItems.get(slot).get(index)).getItem().clone();
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW +Utils.capitalizeFirstSplit(key.replace("-", "_")));
                List<String> lore = sameSlotItems.get(slot).stream().map(s -> {
                    if (sameSlotItems.get(slot).indexOf(s) == index) {
                        return ChatColor.GREEN + "» " + ChatColor.GRAY + Utils.capitalizeFirstSplit(s.replace("-", "_"));
                    } else {
                        return ChatColor.GRAY + "   " + ChatColor.GRAY + Utils.capitalizeFirstSplit(s.replace("-", "_"));
                    }
                }).collect(Collectors.toList());
                lore.add(0, ChatColor.RED + "Multiple Items in this spot!");
                lore.add(1, ChatColor.GRAY + "- Right click to cycle through:");
                lore.add("");
                lore.addAll(Arrays.asList(ChatColor.GRAY + "- Left click to modify", ChatColor.GRAY + "  this item!",
                        ChatColor.GRAY + "- Drop click (Q) to remove", ChatColor.GRAY + "- Shift click to start moving",
                        ChatColor.GRAY + "  click again to place the item.", ChatColor.GRAY + "- Swap Hand click (F) to add",
                        ChatColor.GRAY + "  a new (valid) item."));
                meta.setLore(lore);
                item.setItemMeta(meta);
                gui.setItem(slot, new GuiItem(item, event -> {
                    event.setCancelled(true);
                    if (event.isShiftClick()) {
                        guiItemMover.put(player, key);
                    } else if (event.isRightClick()) {
                        int newIndex = index + 1;
                        if (newIndex >= sameSlotItems.get(slot).size()) {
                            newIndex = 0;
                        }
                        guiEditorSameSlots.get(player).put(slot, newIndex);
                        showGuiInEditor(player, type);
                    } else if (event.isLeftClick()){
                        showItemEditor(player, type, key);
                    } else if (event.getClick() == ClickType.DROP) {
                        config.set(guiName + ".items." + key, null);
                        try {
                            config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                            GuiData.loadGuiData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showGuiInEditor(player, type);
                    }
                }));
            }
        });

        gui.open(player);
    }


    public void showItemEditor(Player player, GuiData.GuiType type, String item) {
        String guiName = type.toString().replace("_", "-").toLowerCase();

        FileConfiguration config = GuiData.getConfig();
        ContainerGui container = GuiData.getViaType(type);

        Gui gui = Gui.gui().rows(3).title(Component.text(ChatColor.AQUA + Utils.capitalizeFirstSplit(type.toString()) + " Item Editor")).create();
        gui.getFiller().fill(ContainerGui.getDefaultBackground());

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
            if (event.getRawSlot() > event.getInventory().getSize() - 1) {
                if (guiItemSelector.containsKey(player)) {
                    ItemStack clicked = event.getView().getBottomInventory().getItem(event.getSlot()).clone();
                    guiItemSelector.remove(player);
                    if (item.equals("background")) {
                        config.set(guiName + "." + item + ".material", clicked.getType().toString().toLowerCase());
                        config.set(guiName + "." + item + ".count", clicked.getAmount());
                        container.setBackground(new ItemStack(clicked.getType(), clicked.getAmount()));
                    } else {
                        config.set(guiName + ".items." + item + ".material", clicked.getType().toString().toLowerCase());
                        config.set(guiName + ".items." + item + ".count", clicked.getAmount());
                    }
                    try {
                        config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                        GuiData.loadGuiData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showItemEditor(player, type, item);
                }
            }
        });

        gui.setCloseGuiAction(event -> {
            guiItemSelector.remove(player);
        });

        ContainerGuiItem cgi = container.getItem(item);

        // Set the Material and Amount:
        ItemStack material = cgi.getItem();
        if (!material.hasItemMeta()) {
            material = new ItemStack(Material.BLACK_STAINED_GLASS, 1);
            ItemMeta materialMeta = material.getItemMeta();
            materialMeta.setDisplayName(ChatColor.RED + "AIR");
            material.setItemMeta(materialMeta);
        }
        ItemMeta materialMeta = material.getItemMeta();
        materialMeta.setDisplayName(ChatColor.GOLD + "Material & Amount");
        List<String> materialLore = new ArrayList<>();
        materialLore.add(ChatColor.GRAY + "Material: " + ChatColor.YELLOW + material.getType().toString());
        materialLore.add(ChatColor.GRAY + "Amount: " + ChatColor.YELLOW + material.getAmount());
        if (guiItemSelector.containsKey(player)) {
            materialLore.add(ChatColor.GRAY + "Click to change!");
        } else {
            materialLore.add(ChatColor.GREEN + "Click to select!");
        }
        if (item.equals("background")) {
            materialLore.add(ChatColor.GRAY + "Press Q to switch to and");
            materialLore.add(ChatColor.GRAY + "between the default/air.");
        }
        materialMeta.setLore(materialLore);
        material.setItemMeta(materialMeta);

        gui.setItem(2, 4, new GuiItem(material, event -> {
            event.setCancelled(true);
            if (event.getClick() == ClickType.DROP) {
                // First drop click resets to Default type
                // Second drop click transforms the item to air. These 2 can be alternated too.
                if (config.isString(guiName + "." + item) && config.getString(guiName + "." + item).equals("default")) {
                    config.set(guiName + "." + item + ".material", "air");
                    config.set(guiName + "." + item + ".count", 0);
                } else {
                    config.set(guiName + "." + item, "default");
                }
                try {
                    config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                    GuiData.loadGuiData();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showItemEditor(player, type, item);
            } else {
                guiItemSelector.put(player, true);
            }
        }));

        // Set is Enchanted:
        boolean isEnchanted = (material.getEnchantments().size() > 0);
        ItemStack enchanted = new ItemStack(isEnchanted ? Material.ENCHANTED_BOOK : Material.BOOK);
        ItemMeta enchantedMeta = enchanted.getItemMeta();
        enchantedMeta.setDisplayName(ChatColor.GOLD + "Enchanted");
        List<String> enchantedLore = new ArrayList<>();
        enchantedLore.add(ChatColor.GRAY + "Enchanted: " + ChatColor.YELLOW + "" + isEnchanted);
        enchantedLore.add(ChatColor.GRAY + "Click to toggle");
        enchantedMeta.setLore(enchantedLore);
        enchanted.setItemMeta(enchantedMeta);
        gui.setItem(2, 6, new GuiItem(enchanted, event -> {
            event.setCancelled(true);
            if (item.equals("background")) {
                config.set(guiName + "." + item + ".enchanted", !isEnchanted);
                container.setBackground(ContainerGuiItem.fromPath(config, guiName + "." + item).getItem());
            } else {
                config.set(guiName + ".items." + item + ".enchanted", !isEnchanted);
            }
            try {
                config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                GuiData.loadGuiData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            showItemEditor(player, type, item);
        }));

        // Back:
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(3, 1, new GuiItem(back, event -> {
            event.setCancelled(true);
            if (item.equals("background")) {
                showGuiSettingsEditor(player, type);
            } else {
                showGuiInEditor(player, type);
            }
        }));

        gui.open(player);
    }

    public void showGuiItemAdder(Player player, GuiData.GuiType type, int row, int column) {
        String guiName = type.toString().replace("_", "-").toLowerCase();

        FileConfiguration config = GuiData.getConfig();
        ContainerGui container = GuiData.getViaType(type);

        PaginatedGui gui = Gui.paginated().rows(3).title(Component.text(ChatColor.AQUA + Utils.capitalizeFirstSplit(type.toString()) + " Item Adder")).create();
        gui.getFiller().fillBottom(ContainerGui.getDefaultBackground());

        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        gui.setCloseGuiAction(event -> {
            guiItemSelector.remove(player);
        });

        // Loop over all items from the internal guis.yml and see if they exist in the current gui:
        FileConfiguration internalGuis = YamlConfiguration.loadConfiguration(
                new InputStreamReader(EzChestShop.getPlugin().
                        getResource("guis.yml")));
        EzChestShop.logDebug("Path: " + guiName + ".items");
        EzChestShop.logDebug("internalGuis: " + (internalGuis.getConfigurationSection("gui." + guiName + ".items") == null));
        EzChestShop.logDebug("externalGuis: " + (config.getConfigurationSection(guiName + ".items") == null));
        List internal = new ArrayList<>(internalGuis.getConfigurationSection(guiName + ".items").getKeys(false).stream().collect(Collectors.toList()));
        internal.removeAll(config.getConfigurationSection(guiName + ".items").getKeys(false));

        internal.forEach(key -> {
            ItemStack item = new ItemStack(Material.matchMaterial(internalGuis.getString(guiName + ".items." + key + ".material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RED + Utils.capitalizeFirstSplit(key.toString().replace("-", "_")));
            item.setItemMeta(meta);
            gui.addItem(new GuiItem(item, event -> {
                event.setCancelled(true);
                config.set(guiName + ".items." + key + ".material", item.getType().toString().toLowerCase());
                config.set(guiName + ".items." + key + ".count", 1);
                config.set(guiName + ".items." + key + ".enchanted", false);
                config.set(guiName + ".items." + key + ".row", row);
                config.set(guiName + ".items." + key + ".column", column);

                try {
                    config.save(new File(EzChestShop.getPlugin().getDataFolder(), "guis.yml"));
                    GuiData.loadGuiData();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                showGuiInEditor(player, type);
            }));
        });


        // Back:
        ItemStack back = new ItemStack(Material.DARK_OAK_DOOR);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        back.setItemMeta(backMeta);
        gui.setItem(3, 1, new GuiItem(back, event -> {
            event.setCancelled(true);
            showGuiInEditor(player, type);
        }));

        // Prev
        ItemStack prev = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prev.getItemMeta();
        prevMeta.setDisplayName(ChatColor.YELLOW + "Prev");
        prev.setItemMeta(prevMeta);
        gui.setItem(3, 2, new GuiItem(prev, event -> {
            event.setCancelled(true);
            gui.previous();
        }));

        // Next
        ItemStack next = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.YELLOW + "Next");
        next.setItemMeta(nextMeta);
        gui.setItem(3, 8, new GuiItem(next, event -> {
            event.setCancelled(true);
            gui.next();
        }));

        gui.open(player);
    }


}
