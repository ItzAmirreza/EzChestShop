package me.deadlight.ezchestshop.data;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.utils.ItemUtils;
import me.deadlight.ezchestshop.utils.StringUtils;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.Utils;
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
import java.util.Arrays;
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

    private List<ShopAction> actionsWithOption = Arrays.asList(ShopAction.BUY, ShopAction.SELL);
    private List<ShopCommandEntry> shopCommandEntries;
    private HashMap<Location, ShopCommandsAtLocation> shopCommandEntryHashMap = new HashMap<>();

    /**
     * Runs all commands at a specific shop & location.
     * @param loc
     * @param type
     * @param action
     */
    public void executeCommands(Player player, Location loc, ShopType type, ShopAction action, String option) {
        if (Config.shopCommandsEnabled) {
            // Always run in the following order:
            // 1. Global wildcard command
            // 2. Global command
            // 3. Local wildcard command
            // 4. Local command

            // If the option is a wildcard, we should not run the command twice.
            boolean isNotAlreadyWildcard = option != null && !option.equals("*");
            if (isNotAlreadyWildcard) {
                executeCommand(player, type, action, "*");
            }
            executeCommand(player, type, action, option);
            if (isNotAlreadyWildcard) {
                executeCommand(player, loc, action, "*");
            }
            executeCommand(player, loc, action, option);
        }
    }

    private void executeCommand(Player player, ShopType type, ShopAction action, String option) {
        shopCommandEntries.stream().filter(shopCommandEntry ->
            shopCommandEntry.type == type && shopCommandEntry.action == action &&
                    (shopCommandEntry.option == null || shopCommandEntry.option.equalsIgnoreCase(option))
        ).forEach(shopCommandEntry -> runCommands(player, shopCommandEntry.commands));
    }

    private void executeCommand(Player player, Location loc, ShopAction action, String option) {
        ShopCommandsAtLocation shopCommandsAtLocation = shopCommandEntryHashMap.get(loc);
        if (shopCommandsAtLocation != null) {
            ShopCommandEntry shopCommandEntry = shopCommandsAtLocation.getEntry(action, option);
            if (shopCommandEntry != null) {
                runCommands(player, shopCommandEntry.commands);
            }
        }
    }

    private void runCommands(Player player, List<String> commands) {
        for (String command : commands) {
            if (command.startsWith("/")) {
                command = command.replaceFirst("/", "");
            }
            // use placeholderapi if available, otherwise just replace the player name.
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                // if the Player extension is not installed,
                // we'll have to make sure the default %player_name% placeholder is replaced regardless.
                if (!command.contains("%player_name%") && PlaceholderAPI.containsPlaceholders("%player_name%")) {
                    command = command.replace("%player_name%", player.getName());
                }
                command = PlaceholderAPI.setPlaceholders(player, command);
            } else {
                command = command.replace("%player_name%", player.getName());
            }
            //TODO it might be cool to dispatch commands as the player too, if that's at some point
            // needed, we can implement it here.
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
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
                if (fc.contains(path)) {
                    // if there's a list, there are no options.
                    if (fc.isList(path)) {
                        shopCommandEntries.add(new ShopCommandEntry(type, action, null, fc.getStringList(path)));
                    } else {
                        // interate over all options.
                        fc.getConfigurationSection(path).getKeys(false).forEach(option -> {
                            String path2 = path + "." + option;
                            if (fc.isList(path2)) {
                                shopCommandEntries.add(new ShopCommandEntry(type, action, option, fc.getStringList(path2)));
                            }
                        });
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
                    // if there's a list, there are no options.
                    if (fc.isList(path)) {
                        addEntry(StringUtils.StringtoLocation(location),
                                new ShopCommandEntry(location, action, null, fc.getStringList(path)));
                    } else {
                        // interate over all options.
                        fc.getConfigurationSection(path).getKeys(false).forEach(option -> {
                            String path2 = path + "." + option;
                            if (fc.isList(path2)) {
                                addEntry(StringUtils.StringtoLocation(location),
                                        new ShopCommandEntry(location, action, option, fc.getStringList(path2)));
                            }
                        });
                    }
                }
            }

        }
    }


    /**
     * Getters & Setters for the Editor!
     */

    public ShopCommandsAtLocation getCommandsOfShop(Location loc) {
        if (!shopCommandEntryHashMap.containsKey(loc)) {
            shopCommandEntryHashMap.put(loc, new ShopCommandsAtLocation());
        }
        return shopCommandEntryHashMap.get(loc);
    }

    public void setCommandsOfShop(Location loc, ShopCommandsAtLocation shopCommandsAtLocation) {
        shopCommandEntryHashMap.put(loc, shopCommandsAtLocation);
        //TODO save the data to the config file.
        String path = "commands." + StringUtils.LocationRoundedtoString(loc, 0);
        File customConfigFile = new File(EzChestShop.getPlugin().getDataFolder(),
                "shopCommands.yml");
        YamlConfiguration fc = YamlConfiguration.loadConfiguration(customConfigFile);

        for (ShopCommandManager.ShopCommandEntry entry : shopCommandsAtLocation.entries) {
            String subpath = "." + entry.action.name().toLowerCase();
            // ignore any entries that should not be saved.
            // If there is no entry without options, the user might not have used "none" in the command,
            // so we want to save it as option "none" entry.
            if (!hasActionOptions(entry.action) && entry.option != null) {
                if (shopCommandsAtLocation.getEntry(entry.action, null) != null) {
                    continue;
                } else  {
                    entry.option = null;
                }
            }
            if (entry.option != null) {
                subpath += "." + entry.option;
            }
            fc.set(path + subpath, entry.commands);
        }
        shopCommandsAtLocation.entries.removeIf(entry -> entry.commands == null || entry.commands.isEmpty());


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

    public boolean hasActionOptions(ShopAction action) {
        return actionsWithOption.contains(action);
    }


    /**
     * Classes
     */
    private class ShopCommandsAtLocation {

        private List<ShopCommandEntry> entries = new ArrayList<>();

        public List<String> getOptions(ShopAction action) {
            List<String> options = new ArrayList<>();
            for (ShopCommandEntry entry: entries) {
                if (entry.action == action) {
                    if (entry.option != null) {
                        options.add(entry.option);
                    }
                }
            }
            return options;
        }

        public ShopCommandEntry getEntry(ShopAction action, String option) {
            for (ShopCommandEntry entry: entries) {
                if (entry.action == action) {
                    if (entry.option == null && option == null) {
                        return entry;
                    } else if (entry.option != null && option != null && entry.option.equals(option)) {
                        return entry;
                    }
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

        private String option;

        private List<String> commands;

        public ShopCommandEntry(ShopType type, ShopAction action, String option, List<String> commands) {
            this.type = type;
            this.action = action;
            this.option = option;
            this.commands = commands;
        }

        public ShopCommandEntry(String location, ShopAction action, String option, List<String> commands) {
            this.location = location;
            this.action = action;
            this.option = option;
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
        compb.append("Editing " + (shop.getSettings().isAdminshop() ? "admin " : "") + "shop trading " +
                        ItemUtils.getFinalItemName(shop.getShopItem()) + "\n").color(ChatColor.YELLOW);

        compb.append(
                "Choose a action from below! Whenever this action is performed, the configured commands will be executed!\n",
                ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
        for (int i = 0; i < ShopAction.values().length; i++) {
            ShopAction action = ShopAction.values()[i];
            String option = "";
            if (!hasActionOptions(action)){
                option = " none";
            }
            compb.append(" - ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD)
                    .append(StringUtils.capitalizeFirstSplit(action.name())).color(ChatColor.YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + option))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to edit the " +
                            action.name() + " action!")));
            if (i != ShopAction.values().length - 1) {
                compb.append("\n", ComponentBuilder.FormatRetention.NONE);
            }
        }

        player.spigot().sendMessage(compb.create());
    }

    public void showOptionEditor(Player player, Location location, ShopAction action) {
        // if this action has no option, show the command editor instead.
        if (!hasActionOptions(action)) {
            showCommandEditor(player, location, action, null);
            return;
        }
        ComponentBuilder compb = new ComponentBuilder();
        EzShop shop = ShopContainer.getShop(location);
        if (shop == null) return;
        compb.append("\n----------------------------------------------------\n").color(ChatColor.YELLOW);
        compb.append(
                "Choose a option from below! * is the wildcard option that will be executed every time. " +
                        "The buy and sell options are for specific buy/sell amounts, so use numbers!\n",
                ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
        compb.append("Editing options of " + (shop.getSettings().isAdminshop() ? "admin " : "") + "shop trading " +
                ItemUtils.getFinalItemName(shop.getShopItem()) + "\n").color(ChatColor.YELLOW);

        ShopCommandsAtLocation commandsAtLocation;
        if (shopCommandEntryHashMap.containsKey(location)) {
            commandsAtLocation = shopCommandEntryHashMap.get(location);
        } else {
            commandsAtLocation = new ShopCommandsAtLocation();
        }
        List<String> options = commandsAtLocation.getOptions(action);
        // always show to wildcard option to raise awareness for it.
        if (!options.contains("*")) {
            options.add("*");
        }
        for (String option : options) {
            compb.append(" - ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GOLD)
                    .append(StringUtils.capitalizeFirstSplit(option)).color(ChatColor.YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + option))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to edit the " +
                            option + " option!")))
                    .append("\n");
        }

        //TODO add a button to add a new option
        compb.append("[+]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " "))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to add a new option!")));
        compb.append("\n ← Back").color(ChatColor.GRAY)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Go back to the action selection")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0)));

        player.spigot().sendMessage(compb.create());
    }

    public void showCommandEditor(Player player, Location location, ShopAction action, String option) {
        ComponentBuilder compb = new ComponentBuilder();
        if (!hasActionOptions(action) && option != null) {
            // if the player opens the shop commands with an option, for an action that has no options, ignore the options.
            option = null;
        }
        // none is passed as the option if option is null.
        String optionCmd = (option == null ? "none" : option);
        EzShop shop = ShopContainer.getShop(location);
        if (shop == null) return;
        ShopCommandManager.ShopCommandsAtLocation cmdsAtLoc = getCommandsOfShop(location);
        if (cmdsAtLoc == null) {
            cmdsAtLoc = new ShopCommandManager.ShopCommandsAtLocation();
        }
        compb.append("----------------------------------------------------\n").color(ChatColor.YELLOW);
        compb.append("Editing " + action.name() + (option != null ? " " + option : "") + " commands of " + (shop.getSettings().isAdminshop() ? "admin " : "") + "shop trading " +
                ItemUtils.getFinalItemName(shop.getShopItem()) + "\n").color(ChatColor.YELLOW);
        compb.append(
                "Add/Edit/Delete commands here. If you need longer commands, use the shopCommands.yml file!\n",
                ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY);
        final String finalOption = option; // required for the lambda expression below.
        List<ShopCommandManager.ShopCommandEntry> entries = cmdsAtLoc.entries.stream().filter(entry -> action == entry.action &&
                (finalOption == null || finalOption.equals(entry.option))).collect(Collectors.toList());
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
                                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + optionCmd + " move " + i + " up"));
                    }
                    if (i != size - 1) {
                        compb.append(" ⇩", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Move command down")))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + optionCmd + " move " + i + " down"));
                    }
                }

                compb
                    .append(" " + command, ComponentBuilder.FormatRetention.NONE).color(ChatColor.YELLOW)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Edit command")))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + optionCmd + " edit " + i + " " + command))
                    .append(" [-]", ComponentBuilder.FormatRetention.NONE).color(ChatColor.RED)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Remove command")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + optionCmd + " remove " + i))
                    .append("\n");
            }
        }
        compb.append("[+]").color(ChatColor.GREEN)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Add new command")))
                .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + " " + action.name() + " " + optionCmd + " add "));
        compb.append("\n ← Back").color(ChatColor.GRAY)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Go back to the action selection")))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/ecsadmin shop-commands " + StringUtils.LocationRoundedtoString(location, 0) + (option == null ? "" : " " + action.name())));

        player.spigot().sendMessage(compb.create());

    }

    public void moveCommandIndex(Player player, Location location, ShopAction action, String option, int index, boolean up) {

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        cmds.getEntry(action, option).commands = Utils.moveListElement(cmds.getEntry(action, option).commands, index, up);
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action, option);
    }

    public void addCommand(Player player, Location location, ShopAction action, String option, String command) {

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        ShopCommandEntry entry =  cmds.getEntry(action, option);
        if (entry == null) {
            entry = new ShopCommandEntry(StringUtils.LocationRoundedtoString(location, 0), action, option, new ArrayList<>());
            cmds.entries.add(entry);
        }
        entry.commands.add(command);
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action, option);
    }

    public void removeCommand(Player player, Location location, ShopAction action, String option, int index) {

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        cmds.getEntry(action, option).commands.remove(index);
        if (cmds.getEntry(action, option).commands.size() == 0) {
            cmds.getEntry(action, option).commands = null;
        }
        setCommandsOfShop(location, cmds);

        showCommandEditor(player, location, action, option);
    }

    public void editCommand(Player player, Location location, ShopAction action, String option, int index, String command) {

        ShopCommandManager.ShopCommandsAtLocation cmds = getCommandsOfShop(location);
        ShopCommandEntry entry = cmds.getEntry(action, option);
        if (entry == null) {
            entry = new ShopCommandEntry(StringUtils.LocationRoundedtoString(location, 0), action, option, new ArrayList<>());
            cmds.entries.add(entry);
        }
        if (index >= entry.commands.size()) {
            player.sendMessage(ChatColor.RED + "Invalid command index! Adding the command instead.");
            entry.commands.add(command);
        } else {
            entry.commands.set(index, command);
        }
        setCommandsOfShop(location, cmds);


        showCommandEditor(player, location, action, option);
    }

}
