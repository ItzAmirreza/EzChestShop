package me.deadlight.ezchestshop.Commands;
import com.bgsoftware.wildchests.api.handlers.ChestsManager;
import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.GUIs.SettingsGUI;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainCommands implements CommandExecutor, TabCompleter {

    private EzChestShop plugin = EzChestShop.getPlugin();


    public static LanguageManager lm = new LanguageManager();
    public static HashMap<UUID, ShopSettings> settingsHashMap = new HashMap<>();
    private enum SettingType {TOGGLE_MSG, DBUY, DSELL, ADMINS, SHAREINCOME};

    public static void updateLM(LanguageManager languageManager) {
        MainCommands.lm = languageManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            if (args.length > 0) {

                String mainarg = args[0];


                if (mainarg.equalsIgnoreCase("create")) {

                    if (args.length >= 3) {

                            if (isPositive(Double.parseDouble(args[1])) && isPositive(Double.parseDouble(args[2]))) {
                                int maxShops = Utils.getMaxPermission(player, "ecs.shops.limit.");
                                maxShops = maxShops == -1 ? 10000 : maxShops;
                                int shops = ShopContainer.getShopCount(player);
                                //EzChestShop.getPlugin().logConsole("Shops: " + shops + ", Max: " + maxShops);
                                if (shops < maxShops) {
                                    try {
                                        createShop(player, args);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    player.sendMessage(lm.maxShopLimitReached(maxShops));
                                }
                            } else {
                                player.sendMessage(lm.negativePrice());
                            }


                    } else {
                        player.sendMessage(lm.notenoughARGS());
                    }

                } else if (mainarg.equalsIgnoreCase("remove")) {
                        removeShop(player ,args);


                } else if (mainarg.equalsIgnoreCase("settings")) {

                    changeSettings(player, args);

                } else {
                    sendHelp(player);
                }

            } else {
                sendHelp(player);
            }


        } else {
            plugin.logConsole(lm.consoleNotAllowed());
        }


        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> fList = new ArrayList<String>();
        List<String> list_mainarg = Arrays.asList("create", "remove", "settings");
        List<String> list_create_1 = Arrays.asList("[BuyPrice]");
        List<String> list_create_2 = Arrays.asList("[SellPrice]");
        List<String> list_settings_1 = Arrays.asList("copy", "paste", "toggle-message", "toggle-buying", "toggle-selling", "admins", "toggle-shared-income");
        List<String> list_settings_2 = Arrays.asList("add", "remove", "list", "clear");
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1)
                StringUtil.copyPartialMatches(args[0], list_mainarg, fList);
            if (args.length > 1) {
                if (args[0].equalsIgnoreCase("create")) {
                    if (args.length == 2)
                        StringUtil.copyPartialMatches(args[1], list_create_1, fList);
                    if (args.length == 3)
                        StringUtil.copyPartialMatches(args[2], list_create_2, fList);
                } else if (args[0].equalsIgnoreCase("settings")) {
                    if (args.length == 2)
                        StringUtil.copyPartialMatches(args[1], list_settings_1, fList);
                    if (args[1].equalsIgnoreCase("admins")) {
                        if (args.length > 2) {
                            if (args.length == 3) {
                                StringUtil.copyPartialMatches(args[2], list_settings_2, fList);
                            }
                            BlockState blockState = getLookedAtBlockStateIfOwner(player, false, false);
                            if (blockState != null) {
                                if (args[2].equalsIgnoreCase("add")) {
                                    if (args.length == 4) {
                                        String adminString = ShopContainer
                                                .getShopSettings(blockState.getLocation()).getAdmins();
                                        if (adminString != null && !adminString.equalsIgnoreCase("none")) {
                                            List<String> adminList = Arrays.asList(adminString
                                                    .split("@")).stream().filter(s -> (s != null && !s.trim().equalsIgnoreCase(""))).map(s ->
                                                    Bukkit.getOfflinePlayer(UUID.fromString(s)).getName())
                                                    .collect(Collectors.toList());
                                            String[] last = args[3].split(",");
                                            List<String> online = Bukkit.getOnlinePlayers().stream().filter(p -> !player.getUniqueId().equals(p.getUniqueId())).map(HumanEntity::getName).collect(Collectors.toList());
                                            online.removeAll(Arrays.asList(last));
                                            online.removeAll(adminList);

                                            if (args[3].endsWith(",")) {
                                                for (String s : online) {
                                                    fList.add(Arrays.asList(last).stream().collect(Collectors.joining(",")) + "," + s);
                                                }
                                            } else {
                                                String lastarg = last[last.length -1];
                                                for (String s : online) {
                                                    if (s.startsWith(lastarg)) {
                                                        last[last.length -1] = s;
                                                        fList.add(Arrays.asList(last).stream().collect(Collectors.joining(",")));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (args[2].equalsIgnoreCase("remove")) {
                                    if (args.length == 4) {
                                        String[] last = args[3].split(",");
                                        String adminString = ShopContainer
                                                .getShopSettings(blockState.getLocation()).getAdmins();
                                        if (adminString != null && !adminString.equalsIgnoreCase("none")) {
                                            List<String> playerList = Arrays.asList(adminString
                                                    .split("@")).stream().filter(s -> (s != null && !s.trim().equalsIgnoreCase(""))).map(s ->
                                                    Bukkit.getOfflinePlayer(UUID.fromString(s)).getName())
                                                    .collect(Collectors.toList());
                                            playerList.removeAll(Arrays.asList(last));
                                            if (args[3].endsWith(",")) {
                                                for (String s : playerList) {
                                                    fList.add(Arrays.asList(last).stream().collect(Collectors.joining(",")) + "," + s);
                                                }
                                            } else {
                                                String lastarg = last[last.length -1];
                                                for (String s : playerList) {
                                                    if (s.startsWith(lastarg)) {
                                                        last[last.length -1] = s;
                                                        fList.add(Arrays.asList(last).stream().collect(Collectors.joining(",")));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return fList;
    }




    private void sendHelp(Player player) {
        String help = "&bEz&cChestShop &7Plugin By Dead_Light© \n" +
                lm.cmdHelp();
        player.sendMessage(Utils.color(help));
        if (player.hasPermission("admin")) {
            String ahelp = "&cAdmin Help: \n " +
                    "&7- &c/ecsadmin";
            player.sendMessage(Utils.color(ahelp));
        }
    }


    private void createShop(Player player, String[] args) throws IOException {

        Block block = player.getTargetBlockExact(6);
        if (block != null && block.getType() != Material.AIR) {
            BlockState blockState = block.getState();

            if (blockState instanceof TileState) {

                if (Utils.isApplicableContainer(block)) {

                    if (checkIfLocation(block.getLocation(), player)) {


                    TileState state = (TileState) blockState;

                    PersistentDataContainer container = state.getPersistentDataContainer();

                    //owner (String) (player name)
                    //buy (double)
                    //sell (double)
                    //item (String) (itemstack)

                    //already a shop
                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || ifItsADoubleChestShop(block) != null) {

                        player.sendMessage(lm.alreadyAShop());

                    } else {
                        //not a shop

                        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            ItemStack thatIteminplayer = player.getInventory().getItemInMainHand();
                            ItemStack thatItem = thatIteminplayer.clone();
                            thatItem.setAmount(1);

                            double buyprice = Double.parseDouble(args[1]);
                            double sellprice = Double.parseDouble(args[2]);

                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE, buyprice);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE, sellprice);
                            //add new settings data later
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, 0);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, 0);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, 0);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, 1);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 0);




                            //msgtoggle 0/1
                            //dbuy 0/1
                            //dsell 0/1
                            //admins [list of uuids seperated with @ in string form]
                            //shareincome 0/1
                            //logs [list of infos seperated by @ in string form]
                            //trans [list of infos seperated by @ in string form]
                            //adminshop 0/1
                            Utils.storeItem(thatItem, container);
                            state.update();
                            ShopContainer.createShop(block.getLocation(), player, thatItem, buyprice, sellprice, false, false, false, "none", true, "none", false);

                            player.sendMessage(lm.shopCreated());


                        } else {

                            player.sendMessage(lm.holdSomething());

                        }


                    }

                }
                    else {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                    }
                } else {

                    player.sendMessage(lm.noChest());

                }

            } else {
                player.sendMessage(lm.lookAtChest());
            }


        } else {
            player.sendMessage(lm.lookAtChest());
        }

    }
    private void removeShop(Player player, String[] args) {
        BlockState blockState = getLookedAtBlockStateIfOwner(player, true, true);
        if (blockState != null) {
            //is the owner remove it
            PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "owner"));
            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "buy"));
            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "sell"));
            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "item"));
            //add new settings data later
            try {
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "dsell"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "admins"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "trans"));
                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"));
                //msgtoggle 0/1
                //dbuy 0/1
                //dsell 0/1
                //admins [list of uuids seperated with @ in string form]
                //shareincome 0/1
                //logs [list of infos seperated by @ in string form]
                //trans [list of infos seperated by @ in string form]
                //adminshop 0/1
            }catch (Exception ex) {
                //nothing really worrying...
            }

            ShopContainer.deleteShop(blockState.getLocation());


            blockState.update();
            player.sendMessage(lm.chestShopRemoved());
        }
    }

    private void changeSettings(Player player, String args[]) {
        if (args.length == 1) {
            BlockState blockState = getLookedAtBlockStateIfOwner(player, true, false);
            if (blockState != null) {
                SettingsGUI settingsGUI = new SettingsGUI();
                settingsGUI.showGUI(player, blockState.getBlock(), false);
                player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5f, 0.5f);
            }
        } else if (args.length >= 2) {

            String settingarg = args[1];

            if (settingarg.equalsIgnoreCase("copy")) {
                copyShopSettings(player);
            } else if (settingarg.equalsIgnoreCase("paste")) {
                pasteShopSettings(player);
            } else if (settingarg.equalsIgnoreCase("toggle-message")) {
                modifyShopSettings(player, SettingType.TOGGLE_MSG, "");
            } else if (settingarg.equalsIgnoreCase("toggle-buying")) {
                modifyShopSettings(player, SettingType.DBUY, "");
            } else if (settingarg.equalsIgnoreCase("toggle-selling")) {
                modifyShopSettings(player, SettingType.DSELL, "");
            } else if (settingarg.equalsIgnoreCase("toggle-shared-income")) {
                modifyShopSettings(player, SettingType.SHAREINCOME, "");
            }

            if (settingarg.equalsIgnoreCase("admins")) {
                if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("clear")) {
                        modifyShopSettings(player, SettingType.ADMINS, "clear");
                    } else if (args[2].equalsIgnoreCase("list")) {
                        BlockState blockState = getLookedAtBlockStateIfOwner(player, true, false);
                        if (blockState != null) {
                            String adminString = ShopContainer.getShopSettings(
                                    blockState.getLocation()).getAdmins();
                            if (adminString != null && !adminString.equalsIgnoreCase("none")) {
                                List<String> adminList = Arrays.asList(adminString.split("@"));
                                if (adminList != null && !adminList.isEmpty()) {
                                    player.sendMessage(ChatColor.GREEN + "Admins:\n" + ChatColor.GRAY + " - " + ChatColor.YELLOW + adminList.stream().map(s -> Bukkit.getOfflinePlayer(
                                            UUID.fromString(s)).getName()).collect(
                                            Collectors.joining("\n" + ChatColor.GRAY + " - " + ChatColor.YELLOW)));
                                } else {
                                    player.sendMessage(ChatColor.GREEN + "Admins:\n" + ChatColor.GRAY + " - " + ChatColor.YELLOW + lm.nobodyStatusAdmins());
                                }
                            }else {
                                player.sendMessage(ChatColor.GREEN + "Admins:\n" + ChatColor.GRAY + " - " + ChatColor.YELLOW + lm.nobodyStatusAdmins());
                            }
                        }
                    }
                } else if (args.length == 4) {
                    if (args[2].equalsIgnoreCase("add")) {
                        modifyShopSettings(player, SettingType.ADMINS, "+" + args[3]);
                    } else if (args[2].equalsIgnoreCase("remove")) {
                        modifyShopSettings(player, SettingType.ADMINS, "-" + args[3]);
                    }
                }
            }
        }
    }

    private void copyShopSettings(Player player) {
        BlockState blockState = getLookedAtBlockStateIfOwner(player, true, false);
        if (blockState != null) {
            ShopSettings settings = ShopContainer.getShopSettings(blockState.getLocation());
            List<String> adminList = (settings.getAdmins() == null || settings.getAdmins().equalsIgnoreCase("none")) ? null : Arrays.asList(settings.getAdmins().split("@"));
            String adminString;
            if (adminList == null || adminList.isEmpty()) {
                adminString = lm.nobodyStatusAdmins();
            } else {
                adminString = adminList.stream().map(id -> Bukkit.getOfflinePlayer(UUID.fromString(id)).getName()).collect(Collectors.joining(", "));
            }
            settingsHashMap.put(player.getUniqueId(), settings.clone());
            player.spigot().sendMessage(new ComponentBuilder(lm.copiedShopSettings()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                lm.toggleTransactionMessageButton() + ": "  + (settings.isMsgtoggle() ? lm.statusOn() : lm.statusOff()) + "\n" +
                lm.disableBuyingButtonTitle() + ": "  + (settings.isDbuy() ? lm.statusOn() : lm.statusOff()) + "\n" +
                lm.disableSellingButtonTitle() + ": "  + (settings.isDsell() ? lm.statusOn() : lm.statusOff()) + "\n" +
                lm.shopAdminsButtonTitle() + ": " + net.md_5.bungee.api.ChatColor.GREEN + adminString + "\n" +
                lm.shareIncomeButtonTitle() + ": "  + (settings.isShareincome() ? lm.statusOn() : lm.statusOff())
            ))).create());
        }
    }

    private void pasteShopSettings(Player player) {
        BlockState blockState = getLookedAtBlockStateIfOwner(player, true, false);
        if (blockState != null) {
            // owner confirmed
            PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
            ShopSettings settings = settingsHashMap.get(player.getUniqueId());
            Database db = EzChestShop.getPlugin().getDatabase();
            String sloc = Utils.LocationtoString(blockState.getLocation());
            String admins = settings.getAdmins() == null ? "none" : settings.getAdmins();
            db.setBool("location", sloc,
                    "msgToggle", "shopdata", settings.isMsgtoggle());
            db.setBool("location", sloc,
                    "buyDisabled", "shopdata", settings.isDbuy());
            db.setBool("location", sloc,
                    "sellDisabled", "shopdata", settings.isDbuy());
            db.setString("location", sloc,
                    "admins", "shopdata", admins);
            db.setBool("location", sloc,
                    "shareIncome", "shopdata", settings.isShareincome());
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER,
                    settings.isMsgtoggle() ? 1 : 0);
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER,
                    settings.isDbuy() ? 1 : 0);
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER,
                    settings.isDsell() ? 1 : 0);
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING,
                    admins);
            container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER,
                    settings.isShareincome() ? 1 : 0);
            ShopSettings newSettings = ShopContainer.getShopSettings(blockState.getLocation());
            newSettings.setMsgtoggle(settings.isMsgtoggle());
            newSettings.setDbuy(settings.isDbuy());
            newSettings.setDsell(settings.isDsell());
            newSettings.setAdmins(settings.getAdmins());
            newSettings.setShareincome(settings.isShareincome());
            blockState.update();
            player.sendMessage(lm.pastedShopSettings());

        }

    }

    private void modifyShopSettings(Player player, SettingType type, String data) {
        BlockState blockState = getLookedAtBlockStateIfOwner(player, true, false);
        if (blockState != null) {
            ShopSettings settings = ShopContainer.getShopSettings(blockState.getLocation());
            Database db = EzChestShop.getPlugin().getDatabase();
            String sloc = Utils.LocationtoString(blockState.getLocation());
            PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
            switch (type) {
                case DBUY:
                    settings.setDbuy(!settings.isDbuy());
                    db.setBool("location", sloc,
                            "buyDisabled", "shopdata", settings.isDbuy());
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER,
                            settings.isDbuy() ? 1: 0);
                    if (settings.isDbuy()) {
                        player.sendMessage(lm.disableBuyingOnInChat());
                    } else {
                        player.sendMessage(lm.disableBuyingOffInChat());
                    }
                    break;
                case DSELL:
                    settings.setDsell(!settings.isDsell());
                    db.setBool("location", sloc,
                            "sellDisabled", "shopdata", settings.isDsell());
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER,
                            settings.isDsell() ? 1 : 0);
                    if (settings.isDsell()) {
                        player.sendMessage(lm.disableSellingOnInChat());
                    } else {
                        player.sendMessage(lm.disableSellingOffInChat());
                    }
                    break;
                case ADMINS:
                    if (data.equalsIgnoreCase("clear")) {
                        data = null;
                        player.sendMessage(lm.clearedAdmins());
                    } else if (data.startsWith("+")) {
                        data = data.replace("+", "");
                        List<UUID> oldData = (settings.getAdmins() == null || settings.getAdmins().equals("none")) ? new ArrayList<>() :
                                new ArrayList<>(Arrays.asList(settings.getAdmins().split("@")))
                                        .stream().map(UUID::fromString).collect(Collectors.toList());
                        List<UUID> newPlayers = Arrays.asList(data.split(",")).stream().map(p -> Bukkit.getOfflinePlayer(p))
                                .filter(p -> p.hasPlayedBefore()).map(p -> p.getUniqueId()).filter(id -> !oldData.contains(id)).collect(Collectors.toList());
                        String newData = newPlayers.stream().map(s -> s.toString()).collect(Collectors.joining("@"));
                        if (newData != null && !newData.equalsIgnoreCase("")) {
                            if (!newPlayers.contains(player.getUniqueId())) {
                                if (settings.getAdmins() == null || settings.getAdmins().equalsIgnoreCase("")) {
                                    data = newData;
                                } else {
                                    data = settings.getAdmins() + "@" + newData;
                                }
                                player.sendMessage(lm.sucAdminAdded(newPlayers.stream()
                                        .map(s -> Bukkit.getOfflinePlayer(s).getName())
                                        .collect(Collectors.joining(", "))));
                            } else {
                                data = settings.getAdmins();
                                player.sendMessage(lm.selfAdmin());
                            }
                        } else {
                            data = settings.getAdmins();
                            player.sendMessage(lm.noPlayer());
                        }

                    } else if (data.startsWith("-")) {
                        data = data.replace("-", "");
                        List<String> oldData = (settings.getAdmins() == null || settings.getAdmins().equalsIgnoreCase("none"))
                                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(settings.getAdmins().split("@")));
                        List<UUID> newPlayers= new ArrayList<>(Arrays.asList(data.split(",")).stream().map(p -> Bukkit.getOfflinePlayer(p))
                                .filter(p -> p.hasPlayedBefore()).map(p -> p.getUniqueId()).collect(Collectors.toList()));
                        if (newPlayers != null && !newPlayers.isEmpty()) {
                            List<String> newData = newPlayers.stream().map(p -> p.toString()).collect(Collectors.toList());
                            oldData.removeAll(newData);
                            data = oldData.stream().collect(Collectors.joining("@"));
                            player.sendMessage(lm.sucAdminRemoved(newPlayers.stream()
                                    .map(s -> Bukkit.getOfflinePlayer(s).getName())
                                    .collect(Collectors.joining(", "))));
                            if (data.trim().equalsIgnoreCase("")) {
                                data = null;
                            }
                        } else {
                            data = settings.getAdmins();
                            player.sendMessage(lm.noPlayer());
                        }
                    }
                    if (data == null || data.equalsIgnoreCase("none")) {
                        data = null;
                    } else if (data.contains("none@")) {
                        data = data.replace("none@", "");
                    }
                    settings.setAdmins(data);
                    String admins = settings.getAdmins() == null ? "none" : settings.getAdmins();
                    db.setString("location", sloc,
                            "admins", "shopdata", admins);
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING,
                            admins);
                    break;
                case TOGGLE_MSG:
                    settings.setMsgtoggle(!settings.isMsgtoggle());
                    db.setBool("location", sloc,
                            "msgToggle", "shopdata", settings.isMsgtoggle());
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER,
                            settings.isMsgtoggle() ? 1 : 0);
                    if (settings.isMsgtoggle()) {
                        player.sendMessage(lm.toggleTransactionMessageOnInChat());
                    } else {
                        player.sendMessage(lm.toggleTransactionMessageOffInChat());
                    }
                    break;
                case SHAREINCOME:
                    settings.setShareincome(!settings.isShareincome());
                    db.setBool("location", sloc,
                            "shareIncome", "shopdata", settings.isShareincome());
                    container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER,
                            settings.isShareincome() ? 1 : 0);
                    if (settings.isShareincome()) {
                        player.sendMessage(lm.sharedIncomeOnInChat());
                    } else {
                        player.sendMessage(lm.sharedIncomeOffInChat());
                    }
                    break;
            }

            blockState.update();
        }
    }


    private boolean checkIfLocation(Location location, Player player) {



        if (plugin.integrationWildChests) { // Start of WildChests integration
            ChestsManager cm = plugin.wchests;
            if (cm.getChest(location) != null) {
                player.sendMessage(Utils.color("&cSorry, but we don't support WildChests yet..."));
                return false;
//                Chest schest = cm.getChest(location);
//                if (schest.getPlacer().equals(player.getUniqueId())) {
//                    return true;
//
//                } else {
//                    player.sendMessage(Utils.color("&cYou are not owner of this chest!"));
//                    return false;
//                }
            }
        } // End of WildChests integration (future integration)

        Block exactBlock = player.getTargetBlockExact(6);
        if (exactBlock == null || exactBlock.getType() == Material.AIR || !(Utils.isApplicableContainer(exactBlock))) {
            return false;
        }

        BlockBreakEvent newevent = new BlockBreakEvent(exactBlock, player);
        Utils.blockBreakMap.put(player.getName(), exactBlock);
        Bukkit.getServer().getPluginManager().callEvent(newevent);

        boolean result = true;
        if (!Utils.blockBreakMap.containsKey(player.getName()) || Utils.blockBreakMap.get(player.getName()) != exactBlock) {
            result = false;
        }
        if (player.hasPermission("ecs.admin")) {
            result = true;
        }
        Utils.blockBreakMap.remove(player.getName());

        return result;

    }

    public boolean isPositive(double price) {
        if (price < 0) {
            return false;
        } else {
            return true;
        }
    }

    private Chest ifItsADoubleChestShop(Block block) {
            //double chest
        if (block instanceof Chest) {
            Chest chest = (Chest) block.getState();
            Inventory inventory = chest.getInventory();
            if (inventory instanceof DoubleChestInventory) {
                DoubleChest doubleChest = (DoubleChest) chest.getInventory().getHolder();
                Chest leftchest = (Chest) doubleChest.getLeftSide();
                Chest rightchest = (Chest) doubleChest.getRightSide();

                if (leftchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || rightchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                    Chest rightone = null;

                    if (!leftchest.getPersistentDataContainer().isEmpty()) {
                        rightone = leftchest;
                    } else {
                        rightone = rightchest;
                    }

                    return rightone;
                }
            }
        }
        return null;
    }

    private BlockState getLookedAtBlockStateIfOwner(Player player, boolean sendErrors, boolean isCreateOrRemove) {
        Block block = player.getTargetBlockExact(6);
        if (block != null && block.getType() != Material.AIR) {
            BlockState blockState = block.getState();

            if (blockState instanceof TileState) {

                if (Utils.isApplicableContainer(block)) {

                    if (checkIfLocation(block.getLocation(), player)) {

                        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                            Inventory inventory = Utils.getBlockInventory(block);
                            if (Utils.getBlockInventory(block) instanceof DoubleChestInventory) {
                                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                                Chest chestleft = (Chest) doubleChest.getLeftSide();
                                Chest chestright = (Chest) doubleChest.getRightSide();

                                if (!chestleft.getPersistentDataContainer().isEmpty()) {
                                    blockState = chestleft.getBlock().getState();
                                } else {
                                    blockState = chestright.getBlock().getState();
                                }
                            }
                        }

                        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
                        Chest chkIfDCS = ifItsADoubleChestShop(block);

                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chkIfDCS != null) {

                            if (chkIfDCS != null) {
                                BlockState newBlockState = chkIfDCS.getBlock().getState();
                                container = ((TileState) newBlockState).getPersistentDataContainer();
                            }


                            String owner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();

                            if (player.getName().equalsIgnoreCase(owner)) {
                                return blockState;
                            } else if (sendErrors) {
                                player.sendMessage(lm.notOwner());
                            }
                        } else if (sendErrors) {
                            player.sendMessage(lm.notAChestOrChestShop());
                        }
                    } else if (sendErrors) {
                        if (isCreateOrRemove) {
                            player.sendMessage(lm.notAllowedToCreateOrRemove());
                        } else {
                            player.sendMessage(lm.notAChestOrChestShop());
                        }
                    }
                } else if (sendErrors) {
                    player.sendMessage(lm.notAChestOrChestShop());
                }
            } else if (sendErrors) {
                player.sendMessage(lm.notAChestOrChestShop());
            }
        } else if (sendErrors) {
            player.sendMessage(lm.notAChestOrChestShop());
        }
        return null;
    }

    private void checkForNewValues(PersistentDataContainer dataContainer, boolean forceInsert) {



    }

}
