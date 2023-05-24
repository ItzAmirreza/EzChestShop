package me.deadlight.ezchestshop.Data;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Manager handles custom commands server admins want to be executed when a shop is interacted with.
 * There are global commands defined in the contfig as well as local commands defined in a separate
 * .yml file.
 */
public class ShopCommandManager {

    public enum ShopType {
        SHOP,
        ADMINSHOP
    }

    public enum ShopAction {
        BUY,
        SELL,
        OPEN
    }

    private List<ShopCommandEntry> shopCommandEntries;
    private HashMap<Location, ShopCommandsAtLocation> shopCommandEntryHashMap = new HashMap<>();

    /**
     * Runs all commands at a specific shop & location.
     * @param loc
     * @param type
     * @param action
     */
    public void executeCommands(Location loc, ShopType type, ShopAction action) {
        if (Config.shopCommandsEnabled) {
            executeCommand(type, action);
            executeCommand(loc, action);
        }
    }

    private void executeCommand(ShopType type, ShopAction action) {
        shopCommandEntries.stream().filter(shopCommandEntry ->
            shopCommandEntry.type == type && shopCommandEntry.action == action
        ).forEach(shopCommandEntry -> runCommands(shopCommandEntry.commands));
    }

    private void executeCommand(Location loc, ShopAction action) {
        ShopCommandsAtLocation shopCommandsAtLocation = shopCommandEntryHashMap.get(loc);
        if (shopCommandsAtLocation != null) {
            ShopCommandEntry shopCommandEntry = shopCommandsAtLocation.getEntry(action);
            if (shopCommandEntry != null) {
                runCommands(shopCommandEntry.commands);
            }
        }
    }

    private void runCommands(List<String> commands) {
        for (String command : commands) {
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            EzChestShop.logDebug("Command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
//        commands.forEach(command -> {
//            if (command.startsWith("/")) {
//                command = command.replaceFirst("/", "");
//            }
//            EzChestShop.logDebug("Command: " + command);
//            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
//        });
    }

    public ShopCommandManager() {
        loadCommands();
    }


    /**
     * Load Stuff
     */

    public void loadCommands() {
        loadConfigCommands();
        loadIndividualShopCommands();
    }

    private void loadConfigCommands() {
        shopCommandEntries = new ArrayList<>();
        YamlConfiguration fc = YamlConfiguration.loadConfiguration(new File(EzChestShop.getPlugin().getDataFolder(), "config.yml"));
        for (ShopType type : ShopType.values()) {
            for (ShopAction action: ShopAction.values()) {
                String path = "shops.commands." + type.name().toLowerCase() + "." + action.name().toLowerCase();
                EzChestShop.logDebug("path: " + path);
                if (fc.contains(path)) {
                    if (fc.isList(path)) {
                        EzChestShop.logDebug(type + ", " + action + ", Commands: " + fc.getStringList(path).stream().collect(Collectors.joining(", ")));
                        shopCommandEntries.add(new ShopCommandEntry(type, action, fc.getStringList(path)));
                    }
                }
            }

        }
    }

    private void loadIndividualShopCommands() {
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder(),
                "shopCommands.yml");
        if (!customConfigFile.exists()) {
            EzChestShop.logConsole("&c[&eEzChestShop&c] &eGenerating shopCommands file...");
            customConfigFile.getParentFile().mkdirs();
            EzChestShop.getPlugin().saveResource("shopCommands.yml", false);
            YamlConfiguration fc = YamlConfiguration.loadConfiguration(customConfigFile);
        }
        YamlConfiguration fc = YamlConfiguration.loadConfiguration(customConfigFile);
        // for all shop types, try loading all actions and check for subkeys (options)
        ConfigurationSection section = fc.getConfigurationSection("commands");
        if (section == null) {
            return;
        }
        for (String location: section.getKeys(false)) {
            for (ShopAction action: ShopAction.values()) {
                String path = "commands." + location + "." + action.name().toLowerCase();
                if (fc.contains(path)) {
                    if (fc.isList(path)) {
                        addEntry(Utils.StringtoLocation(location),
                                new ShopCommandEntry(location, action, fc.getStringList(path)));
                    }
                }
            }

        }
    }


    /**
     * Getters & Setters for the Editor!
     */

    public ShopCommandsAtLocation getCommandsOfShop(Location loc) {
        return shopCommandEntryHashMap.get(loc);
    }

    public void setCommandsOfShop(Location loc, ShopCommandsAtLocation shopCommandsAtLocation) {
        shopCommandEntryHashMap.put(loc, shopCommandsAtLocation);
        //TODO save the data to the config file.
        String path = "commands." + Utils.LocationRoundedtoString(loc, 0);
        EzChestShop.logDebug("path: " + path);
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder(),
                "shopCommands.yml");
        YamlConfiguration fc = YamlConfiguration.loadConfiguration(customConfigFile);

        for (ShopCommandManager.ShopCommandEntry entry : shopCommandsAtLocation.entries) {
            String subpath = "." + entry.action.name().toLowerCase();
            fc.set(path + subpath, entry.commands);
        }

        try {
            fc.save(new File(EzChestShop.getPlugin().getDataFolder(), "shopCommands.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addEntry(Location location, ShopCommandEntry entry) {
        if (shopCommandEntryHashMap.containsKey(location)) {
            ShopCommandsAtLocation prevLocationEntry = shopCommandEntryHashMap.get(location);
            prevLocationEntry.addEntry(entry);
            shopCommandEntryHashMap.put(location, prevLocationEntry);
        } else {
            ShopCommandsAtLocation newLocationEntry = new ShopCommandsAtLocation();
            newLocationEntry.addEntry(entry);
            shopCommandEntryHashMap.put(location, newLocationEntry);
        }
    }


    /**
     * Classes
     */
    private class ShopCommandsAtLocation {

        private List<ShopCommandEntry> entries = new ArrayList<>();

        public ShopCommandEntry getEntry(ShopAction action) {
            for (ShopCommandEntry entry: entries) {
                if (entry.action == action) {
                    return entry;
                }
            }
            return null;
        }

        public void addEntry(ShopCommandEntry entry) {
            entries.add(entry);
        }

    }

    private class ShopCommandEntry {
        private ShopType type;

        private String location;

        private ShopAction action;

        private List<String> commands;

        public ShopCommandEntry(ShopType type, ShopAction action, List<String> commands) {
            this.type = type;
            this.action = action;
            this.commands = commands;
        }

        public ShopCommandEntry(String location, ShopAction action, List<String> commands) {
            this.location = location;
            this.action = action;
            this.commands = commands;
        }
    }


    /**
     * Editor
     */
    public void showActionEditor(Player player, Location location) {
        ComponentBuilder compb = new ComponentBuilder();
        EzShop shop = ShopContainer.getShop(location);
        if (shop == null) return;
        compb.append("\n----------------------------------------------------\n").color(ChatColor.YELLOW);
        compb.append(
                "Choose a action from below! Whenever this action is performed, the configured commands will be executed!\n",
                ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
        compb.append("Editing " + (shop.getSettings().isAdminshop() ? "admin " : "") + "shop trading " +
                        Utils.getFinalItemName(shop.getShopItem()) + "\n").color(ChatColor.YELLOW);

        for (ShopAction action : ShopAction.values()) {
            compb.append(" - ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD)
                    .append(Utils.capitalizeFirstSplit(action.name())).color(ChatColor.YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to edit the " +
                            action.name() + " action!")))
                    .append("\n");
        }

        player.spigot().sendMessage(compb.create());
    }

    public void showCommandEditor(Player player, Location location, ShopAction action) {
        ComponentBuilder compb = new ComponentBuilder();
        EzShop shop = ShopContainer.getShop(location);
        if (shop == null) return;
        compb.append("----------------------------------------------------\n").color(ChatColor.YELLOW);
        compb.append("Editing " + action.name() + " commands of " + (shop.getSettings().isAdminshop() ? "admin " : "") + "shop trading " +
                Utils.getFinalItemName(shop.getShopItem()) + "\n").color(ChatColor.YELLOW);
        ShopCommandManager.ShopCommandsAtLocation cmdsAtLoc = getCommandsOfShop(location);
        if (cmdsAtLoc == null) {
            EzChestShop.logDebug("cmdsAtLoc is null " + location.toString());
            return;
        }
        List<ShopCommandManager.ShopCommandEntry> entries = cmdsAtLoc.entries.stream().filter(entry -> action == entry.action).collect(Collectors.toList());
        if (entries.size() > 0) {
            int size = entries.get(0).commands.size();
            for (int i = 0; i < size; i++) {
                String command = entries.get(0).commands.get(i);

                compb.append(" " + (i + 1) + ".", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD);

                if (size > 1) {
                    if (i != 0) {
                        compb.append(" ⇧", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Move command up")))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name() + " move " + i + " up"));
                    }
                    if (i != size - 1) {
                        compb.append(" ⇩", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Move command down")))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name() + " move " + i + " down"));
                    }
                }

                compb
                    .append(" " + command, ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Edit command")))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name() + " edit " + i + " " + command))
                    .append(" [-]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Remove command")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name() + " remove " + i))
                    .append("\n");
            };
        }
        compb.append("[+]").color(ChatColor.GREEN)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Add new command")))
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0) + " " + action.name() + " add "));
        compb.append("\n ← Back").color(ChatColor.GRAY)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Go back to the action selection")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/ecsadmin shop-commands " + Utils.LocationRoundedtoString(location, 0)));

        player.spigot().sendMessage(compb.create());

    }

    public void moveCommandIndex(Player player, Location location, ShopAction action, int index, boolean up) {
        EzChestShop.logDebug("Moving command index " + index + " of action " + action.name() + " of shop " + Utils.LocationRoundedtoString(location, 0) + " " + (up ? "up" : "down"));

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        cmds.getEntry(action).commands = Utils.moveListElement(cmds.getEntry(action).commands, index, up);
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action);
    }

    public void addCommand(Player player, Location location, ShopAction action, String command) {
        EzChestShop.logDebug("Adding command " + command + " to action " + action.name() + " of shop " + Utils.LocationRoundedtoString(location, 0));

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        ShopCommandEntry entry =  cmds.getEntry(action);
        if (entry == null) {
            entry = new ShopCommandEntry(Utils.LocationRoundedtoString(location, 0), action, new ArrayList<>());
            cmds.entries.add(entry);
        }
        entry.commands.add(command);
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action);
    }

    public void removeCommand(Player player, Location location, ShopAction action, int index) {
        EzChestShop.logDebug("Removing command index " + index + " of action " + action.name() + " of shop " + Utils.LocationRoundedtoString(location, 0));

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        cmds.getEntry(action).commands.remove(index);
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action);
    }

    public void editCommand(Player player, Location location, ShopAction action, int index, String command) {
        EzChestShop.logDebug("Editing command index " + index + " of action " + action.name() + " of shop " + Utils.LocationRoundedtoString(location, 0) + " to " + command);

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        cmds.getEntry(action).commands.set(index, command);
        setCommandsOfShop(location, cmds);


        showCommandEditor(player, location, action);
    }

}
