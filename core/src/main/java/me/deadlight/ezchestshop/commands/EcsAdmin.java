package me.deadlight.ezchestshop.commands;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.data.gui.GuiData;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.deadlight.ezchestshop.data.ShopCommandManager;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.guis.GuiEditorGUI;
import me.deadlight.ezchestshop.utils.objects.EzShop;
import me.deadlight.ezchestshop.utils.Utils;
import me.deadlight.ezchestshop.utils.worldguard.FlagRegistry;
import me.deadlight.ezchestshop.utils.worldguard.WorldGuardUtils;
import me.deadlight.ezchestshop.utils.holograms.ShopHologram;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EcsAdmin implements CommandExecutor, TabCompleter {

    public static LanguageManager lm = new LanguageManager();

    public static void updateLM(LanguageManager languageManager) {
        EcsAdmin.lm = languageManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            int size = args.length;

            if (player.hasPermission("ecs.admin") || player.hasPermission("ecs.admin.remove") ||
                    player.hasPermission("ecs.admin.reload") || player.hasPermission("ecs.admin.create")) {

                if (size == 0) {
                    sendHelp(player);
                } else {

                    String firstarg = args[0];
                    if (firstarg.equalsIgnoreCase("remove") && (player.hasPermission("ecs.admin.remove") || player.hasPermission("ecs.admin"))) {
                        Block target = getCorrectBlock(player.getTargetBlockExact(6));
                        if (target != null) {
                            removeShop(player, args, target);
                        } else {
                            player.sendMessage(lm.lookAtChest());
                        }

                    } else if (firstarg.equalsIgnoreCase("reload") && (player.hasPermission("ecs.admin.reload") || player.hasPermission("ecs.admin"))) {

                        reload();
                        player.sendMessage(Utils.colorify("&aEzChestShop successfully reloaded!"));

                    } else if (firstarg.equalsIgnoreCase("remove-emptyshops") && (player.hasPermission("ecs.admin.clear") || player.hasPermission("ecs.admin"))) {
                        List<EzShop> shops = ShopContainer.getShops();
                        int i = 0;
                        for (EzShop ezShop : shops){
                            if(Utils.getBlockInventory(ezShop.getLocation().getBlock()).isEmpty()) {
                                BlockState blockState = ezShop.getLocation().getBlock().getState();
                                if (blockState != null){
                                    PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
                                    container.remove(new NamespacedKey(EzChestShop.getPlugin(), "owner"));
                                    container.remove(new NamespacedKey(EzChestShop.getPlugin(), "buy"));
                                    container.remove(new NamespacedKey(EzChestShop.getPlugin(), "sell"));
                                    container.remove(new NamespacedKey(EzChestShop.getPlugin(), "item"));

                                    try {
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "dsell"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "admins"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"));
                                        container.remove(new NamespacedKey(EzChestShop.getPlugin(), "rotation"));
                                    } catch (Exception ignored) {}

                                    ShopContainer.deleteShop(blockState.getLocation());
                                    ShopHologram.hideForAll(blockState.getLocation());

                                    blockState.update();
                                    i++;
                                }
                            }
                        }
                        player.sendMessage(Utils.colorify("&aEzChestShop successfully cleaned "+i+" empty shops!"));

                    } else if (firstarg.equalsIgnoreCase("create") && (player.hasPermission("ecs.admin.create") || player.hasPermission("ecs.admin"))) {
                        Block target = getCorrectBlock(player.getTargetBlockExact(6));
                        if (target != null) {
                            if (size >= 3) {

                                if (isPositive(Double.parseDouble(args[1])) && isPositive(Double.parseDouble(args[2]))) {
                                    try {
                                        createShop(player, args, target);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    player.sendMessage(lm.negativePrice());
                                }


                            } else {
                                player.sendMessage(lm.notenoughARGS());
                            }
                        } else {
                            player.sendMessage(lm.lookAtChest());
                        }

                    } else if (firstarg.equalsIgnoreCase("transfer-ownership") && (player.hasPermission("ecs.admin.transfer") || player.hasPermission("ecs.admin"))) {
                        Block target = getCorrectBlock(player.getTargetBlockExact(6));
                        if (size == 2) {

                            OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);

                            if (op != null && op.hasPlayedBefore()) {
                                BlockState blockState = getLookedAtBlockState(player, true, false, target);
                                if (blockState != null) {
                                    player.spigot().sendMessage(lm.shopTransferConfirm(args[1], true)); // Confirmation message similar to the clearprofit message.
                                }
                            } else {
                                player.sendMessage(lm.noPlayer());
                            }

                        } else if (size == 3 && args[2].equals("-confirm")) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);

                            if (op != null && op.hasPlayedBefore()) {

                                BlockState blockState = getLookedAtBlockState(player, true, false, target);
                                if (blockState != null) {
                                    ShopContainer.transferOwner(blockState, op);
                                    ShopHologram.getHologram(blockState.getLocation(), player).updateOwner();
                                    player.sendMessage(lm.shopTransferred(args[1]));
                                }

                            } else {
                                player.sendMessage(lm.noPlayer());
                            }
                        } else {
                            sendHelp(player);
                        }
                    } else if (firstarg.equalsIgnoreCase("configure-guis")) {
                        new GuiEditorGUI().showGuiEditorOverview(player);
                    } else if (firstarg.equalsIgnoreCase("shop-commands")) {
                        if (!Config.shopCommandsEnabled) {
                            player.sendMessage(ChatColor.RED +  "Enable this setting in the config!");
                            return false;
                        }
                        if (args.length == 1) {
                            Block target = player.getTargetBlockExact(5);
                            if (target != null) {
                                EzShop shop = ShopContainer.getShop(target.getLocation());
                                if (shop != null) {
                                    Config.shopCommandManager.showActionEditor(player, shop.getLocation());
                                } else {
                                    player.sendMessage(lm.notAChestOrChestShop());
                                }
                            } else {
                                player.sendMessage(lm.lookAtChest());
                            }
                        } else {
                            if (args[1].startsWith("W:")) {
                                Location location = Utils.StringtoLocation(args[1]);
                                if (location != null) {
                                    if (args.length < 3) {
                                        Config.shopCommandManager.showActionEditor(player, location);
                                    } else if (args.length < 4) {
                                        ShopCommandManager.ShopAction action = ShopCommandManager.ShopAction.valueOf(args[2]);
                                        if (!Config.shopCommandManager.hasActionOptions(action)) {
                                            // if the command doesn't have any options, directly show the command editor!
                                            Config.shopCommandManager.showCommandEditor(player, location, action, null);
                                        } else {
                                            Config.shopCommandManager.showOptionEditor(player, location, action);
                                        }
                                    } else if (args.length >= 4) {
                                        ShopCommandManager.ShopAction action = ShopCommandManager.ShopAction.valueOf(args[2]);
                                        String option = args[3].equals("none") ? null : args[3];
                                        if (args.length == 4) {
                                            Config.shopCommandManager.showCommandEditor(player, location, action, option);
                                        } else {
                                            // longer then 3 args
                                            if (args[4].equals("add")) {
                                                if (args.length >= 5) {
                                                    // get the command from any further args
                                                    String newCommand = "";
                                                    for (int i = 5; i < args.length; i++) {
                                                        newCommand += args[i] + " ";
                                                    }
                                                    if (!newCommand.trim().equals("")) {
                                                        Config.shopCommandManager.addCommand(player, location, action, option, newCommand.trim());
                                                    }
                                                }
                                            } else if (args[4].equals("move")) {
                                                if (args.length == 7) {
                                                    Config.shopCommandManager.moveCommandIndex(player, location, action, option, Integer.parseInt(args[5]), args[6].equals("up"));
                                                }

                                            } else if (args[4].equals("edit")) {
                                                if (args.length >= 7) {
                                                    // get the command from any further args
                                                    String newCommand = "";
                                                    for (int i = 6; i < args.length; i++) {
                                                        newCommand += args[i] + " ";
                                                    }
                                                    if (newCommand.trim().equals("")) {
                                                        Config.shopCommandManager.removeCommand(player, location, action, option, Integer.parseInt(args[5]));
                                                    } else {
                                                        Config.shopCommandManager.editCommand(player, location, action, option, Integer.parseInt(args[5]), newCommand.trim());
                                                    }
                                                }

                                            } else if (args[4].equals("remove")) {
                                                if (args.length == 6) {
                                                    Config.shopCommandManager.removeCommand(player, location, action, option, Integer.parseInt(args[5]));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (firstarg.equalsIgnoreCase("debug")) {
                        generateAndUploadLogs(player);

                    } else {
                        sendHelp(player);
                    }

                }


            } else {

                Utils.sendVersionMessage(player);

            }


        } else {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reload();
                sender.sendMessage(Utils.colorify("&aEzChestShop successfully reloaded!"));
            } else {
                sender.sendMessage(Utils.colorify("&cThis command can only be executed by a player or when used for reloading!"));
            }
        }

        return false;
    }

    private void reload() {
        Config.loadConfig();
        ShopHologram.reloadAll();
        LanguageManager.reloadLanguages();
        GuiData.loadGuiData();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> fList = new ArrayList<>();
        List<String> list_firstarg = Arrays.asList("create", "reload", "remove", "help", "transfer-ownership", "configure-guis", "shop-commands", "debug", "remove-emptyshops");
        List<String> list_create_1 = Arrays.asList("[BuyPrice]");
        List<String> list_create_2 = Arrays.asList("[SellPrice]");
        List<String> list_transfer_2 = Arrays.asList("-confirm");
        if (sender instanceof Player) {
            Player p = (Player) sender;
            List<String> list_shop_commands_1;
            if (p.getTargetBlockExact(6) != null) {
                list_shop_commands_1 = Arrays.asList(Utils.LocationRoundedtoString(p.getTargetBlockExact(6).getLocation(), 0));
            } else {
                list_shop_commands_1 = Arrays.asList("Look at a shop for auto location completion!");
            }
            List<String> list_shop_commands_2 = Arrays.asList(Arrays.stream(ShopCommandManager.ShopAction.values()).map(Enum::name).toArray(String[]::new));
            List<String> list_shop_commands_3 = Arrays.asList("[option]");
            List<String> list_shop_commands_4 = Arrays.asList("add", "move", "edit", "remove");
            List<String> list_shop_commands_5 = Arrays.asList("[index]");
            List<String> list_shop_commands_editcreate_6 = Arrays.asList("[command]");
            List<String> list_shop_commands_move_6 = Arrays.asList("up", "down");
            if (p.hasPermission("ecs.admin") || p.hasPermission("ecs.admin.reload") || p.hasPermission("ecs.admin.create") || p.hasPermission("ecs.admin.remove")) {
                if (args.length == 1)
                    StringUtil.copyPartialMatches(args[0], list_firstarg, fList);
                if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
                    if (args.length == 2)
                        StringUtil.copyPartialMatches(args[1], list_create_1, fList);
                    if (args.length == 3)
                        StringUtil.copyPartialMatches(args[2], list_create_2, fList);
                } else if (args.length > 1 && args[0].equalsIgnoreCase("transfer-ownership")) {
                    if (args.length == 3) {
                        StringUtil.copyPartialMatches(args[2], list_transfer_2, fList);
                    } else {
                        // If null is returned a list of online players will be suggested
                        return null;
                    }
                } else if (args.length > 1 && args[0].equalsIgnoreCase("shop-commands")) {
                    if (args.length == 2) {
                        StringUtil.copyPartialMatches(args[1], list_shop_commands_1, fList);
                    } else if (args.length == 3) {
                        StringUtil.copyPartialMatches(args[2], list_shop_commands_2, fList);
                    } else if (args.length == 4) {
                        StringUtil.copyPartialMatches(args[3], list_shop_commands_3, fList);
                    } else if (args.length == 5) {
                        StringUtil.copyPartialMatches(args[4], list_shop_commands_4, fList);
                    } else if (args.length == 6) {
                        if (args[4].equalsIgnoreCase("add")) {
                            StringUtil.copyPartialMatches(args[5], list_shop_commands_editcreate_6, fList);
                        } else {
                            StringUtil.copyPartialMatches(args[5], list_shop_commands_5, fList);
                        }
                    } else if (args.length >= 7) {
                        if (args[4].equalsIgnoreCase("add") || args[4].equalsIgnoreCase("edit")) {
                            StringUtil.copyPartialMatches(args[args.length - 1], list_shop_commands_editcreate_6, fList);
                        } else if (args[4].equalsIgnoreCase("move") && args.length == 7) {
                            StringUtil.copyPartialMatches(args[6], list_shop_commands_move_6, fList);
                        }
                    }
                }
            }
        }
        return fList;
    }


    private void sendHelp(Player player) {
        player.spigot().sendMessage(lm.cmdadminHelp());
    }

    private void removeShop(Player player, String[] args, Block target) {



        if (target != null && target.getType() != Material.AIR) {

            //slimefun check
            if (EzChestShop.slimefun) {
                boolean sfresult = BlockStorage.hasBlockInfo(target.getLocation());
                if (sfresult) {
                    player.sendMessage(lm.slimeFunBlockNotSupported());
                    return;
                }
            }
            BlockState blockState = target.getState();
            if (blockState instanceof TileState) {

                if (Utils.isApplicableContainer(target)) {

                        TileState state = (TileState) blockState;

                        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();

                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                            if (EzChestShop.worldguard) {
                                if (container.get(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER) == 1) {
                                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_ADMIN_SHOP, player)) {
                                        player.spigot().sendMessage(lm.notAllowedToCreateOrRemove(player));
                                        return;
                                    }
                                } else {
                                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_SHOP, player)) {
                                        player.spigot().sendMessage(lm.notAllowedToCreateOrRemove(player));
                                        return;
                                    }
                                }
                            }

                                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "owner"));
                                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "buy"));
                                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "sell"));
                                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "item"));

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

                                } catch (Exception ex) {
                                    //noting really worrying

                                }

                                ShopContainer.deleteShop(blockState.getLocation());
                            ShopHologram.hideForAll(blockState.getLocation());
                                state.update();

                                player.sendMessage(lm.chestShopRemoved());


                        } else {

                            player.sendMessage(lm.notAChestOrChestShop());

                        }

                } else {
                    player.sendMessage(lm.notAChestOrChestShop());
                }

            } else {

                player.sendMessage(lm.notAChestOrChestShop());
            }


        } else {

            player.sendMessage(lm.notAChestOrChestShop());

        }



    }


    private void createShop(Player player, String[] args, Block target) throws IOException {

        if (target != null && target.getType() != Material.AIR) {
            BlockState blockState = target.getState();
            //slimefun check
            if (EzChestShop.slimefun) {
                boolean sfresult = BlockStorage.hasBlockInfo(target.getLocation());
                if (sfresult) {
                    player.sendMessage(lm.slimeFunBlockNotSupported());
                    return;
                }
            }
            //slimefun check
            if (EzChestShop.slimefun) {
                boolean sfresult = BlockStorage.hasBlockInfo(target.getLocation());
                if (sfresult) {
                    player.sendMessage(lm.slimeFunBlockNotSupported());
                    return;
                }
            }

            if (EzChestShop.worldguard) {
                if (!WorldGuardUtils.queryStateFlag(FlagRegistry.CREATE_ADMIN_SHOP, player)) {
                    player.spigot().sendMessage(lm.notAllowedToCreateOrRemove(player));
                    return;
                }
            }

            if (blockState instanceof TileState) {

                if (Utils.isApplicableContainer(target)) {

                        TileState state = (TileState) blockState;

                        PersistentDataContainer container = state.getPersistentDataContainer();

                        //owner (String) (player name)
                        //buy (double)
                        //sell (double)
                        //item (String) (itemstack)

                        //already a shop
                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                            player.sendMessage(lm.alreadyAShop());

                        } else {
                            //not a shop

                            if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                                ItemStack thatIteminplayer = player.getInventory().getItemInMainHand();
                                ItemStack thatItem = thatIteminplayer.clone();
                                thatItem.setAmount(1);
                                if (Utils.isShulkerBox(thatItem.getType()) && Utils.isShulkerBox(target)) {
                                    player.sendMessage(lm.invalidShopItem());
                                    return;
                                }

                                double buyprice = Double.parseDouble(args[1]);
                                double sellprice = Double.parseDouble(args[2]);

                                int isDBuy = Config.settings_zero_equals_disabled ?
                                        (buyprice == 0 ? 1 : (Config.settings_defaults_dbuy ? 1 : 0))
                                        : (Config.settings_defaults_dbuy ? 1 : 0);
                                int isDSell = Config.settings_zero_equals_disabled ?
                                        (sellprice == 0 ? 1 : (Config.settings_defaults_dsell ? 1 : 0))
                                        : (Config.settings_defaults_dsell ? 1 : 0);

                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE, buyprice);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE, sellprice);
                                //add new settings data later
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, Config.settings_defaults_transactions ? 1 : 0);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, isDBuy);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, isDSell);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, Config.settings_defaults_shareprofits ? 1 : 0);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 1);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, Config.settings_defaults_rotation);

                                ShopContainer.createShop(target.getLocation(), player, thatItem, buyprice, sellprice, false,
                                        isDBuy == 1, isDSell == 1, "none", true, true, Config.settings_defaults_rotation);
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
                                player.sendMessage(lm.shopCreated());


                            } else {

                                player.sendMessage(lm.holdSomething());

                            }


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
    public boolean isPositive(double price) {
        if (price < 0) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkIfLocation(Location location, Player player) {
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

    private BlockState getLookedAtBlockState(Player player, boolean sendErrors, boolean isCreateOrRemove, Block target) {
        if (target != null && target.getType() != Material.AIR) {
            BlockState blockState = target.getState();
            if (EzChestShop.slimefun) {
                boolean sfresult = BlockStorage.hasBlockInfo(blockState.getBlock().getLocation());
                if (sfresult) {
                    player.sendMessage(lm.slimeFunBlockNotSupported());
                    return null;
                }
            }
            if (blockState instanceof TileState) {

                if (Utils.isApplicableContainer(target)) {

                    if (checkIfLocation(target.getLocation(), player)) {

                        if (target.getType() == Material.CHEST || target.getType() == Material.TRAPPED_CHEST) {
                            Inventory inventory = Utils.getBlockInventory(target);
                            if (Utils.getBlockInventory(target) instanceof DoubleChestInventory) {
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
                        Chest chkIfDCS = ifItsADoubleChestShop(target);

                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chkIfDCS != null) {

                            return blockState;

                        } else if (sendErrors) {
                            player.sendMessage(lm.notAChestOrChestShop());
                        }
                    } else if (sendErrors) {
                        if (isCreateOrRemove) {
                            player.spigot().sendMessage(lm.notAllowedToCreateOrRemove(player));
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

    private Block getCorrectBlock(Block target) {
        if (target == null) return null;
        Inventory inventory = Utils.getBlockInventory(target);
        if (inventory instanceof DoubleChestInventory) {
            //double chest

            DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
            Chest leftchest = (Chest) doubleChest.getLeftSide();
            Chest rightchest = (Chest) doubleChest.getRightSide();

            if (leftchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)
                    || rightchest.getPersistentDataContainer().has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {


                if (!leftchest.getPersistentDataContainer().isEmpty()) {
                    target = leftchest.getBlock();
                } else {
                    target = rightchest.getBlock();
                }
            }
        }
        return target;
    }


    private void generateAndUploadLogs(Player player) {
        //we gonna get some info about plugin, server, and logs and send it to the API server
        EzChestShop ecsInstance = EzChestShop.getPlugin();

        //MC Version
        String mcVersion = Bukkit.getVersion();
        //Plugin Version
        String pluginVersion = ecsInstance.getDescription().getVersion();
        //Server Version
        String serverVersion = Bukkit.getServer().getVersion();
        //Server Software (Spigot, Paper, etc)
        String serverSoftware = Bukkit.getServer().getName();
        //whether if the server is in offline mode or not
        boolean offlineMode = Bukkit.getServer().getOnlineMode();
        //whether if it got vault or not
        boolean vault = Bukkit.getServer().getPluginManager().getPlugin("Vault") != null;
        //whether if it got slimefun or not
        boolean slimefun = EzChestShop.slimefun;
        //whether if it got worldguard or not
        boolean worldguard = EzChestShop.worldguard;
        //if it got an economy plugin or not
        boolean economy = EzChestShop.economyPluginFound;
        //get list of plugins
        List<String> plugins = new ArrayList<>();
        for (Plugin plugin : Bukkit.getServer().getPluginManager().getPlugins()) {
            plugins.add(plugin.getName() + " " + plugin.getDescription().getVersion());
        }

        //get list of worlds
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getServer().getWorlds()) {
            worlds.add(world.getName());
        }
        //online players
        int onlinePlayers = Bukkit.getServer().getOnlinePlayers().size();

        //number of shops
        int numberOfShops = ShopContainer.getShops().size();

        //put them all in a JSON object and send it to the API server

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("mcVersion", mcVersion);
        jsonObject.addProperty("pluginVersion", pluginVersion);
        jsonObject.addProperty("serverVersion", serverVersion);
        jsonObject.addProperty("serverSoftware", serverSoftware);
        jsonObject.addProperty("offlineMode", offlineMode);
        jsonObject.addProperty("vault", vault);
        jsonObject.addProperty("slimefun", slimefun);
        jsonObject.addProperty("worldguard", worldguard);
        jsonObject.addProperty("economy", economy);
        jsonObject.addProperty("plugins", plugins.toString());
        jsonObject.addProperty("worlds", worlds.toString());
        jsonObject.addProperty("onlinePlayers", onlinePlayers);
        jsonObject.addProperty("numberOfShops", numberOfShops);

        JsonObject ourConfig = new JsonObject();
        FileConfiguration config = EzChestShop.getPlugin().getConfig();

        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) {
                continue; // Skip keys that are sections themselves, only consider leaf keys
            }

            String[] keyParts = key.split("\\.");
            JsonObject currentObject = ourConfig;

            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                if (!currentObject.has(part)) {
                    currentObject.add(part, new JsonObject());
                }
                currentObject = currentObject.getAsJsonObject(part);
            }

            String lastPart = keyParts[keyParts.length - 1];
            if (config.isList(key)) {
                JsonArray jsonArray = new JsonArray();
                List<?> list = config.getList(key);
                for (Object item : list) {
                    jsonArray.add(item.toString());
                }
                currentObject.add(lastPart, jsonArray);
            } else {
                currentObject.addProperty(lastPart, config.get(key).toString());
            }
        }

// Now we gonna censor specific values
        String[] censoredKeys = {
                "database.mysql.ip", "database.mysql.port", "database.mysql.tables-prefix",
                "database.mysql.database", "database.mysql.username", "database.mysql.password",
                "database.mysql.max-pool", "database.mysql.ssl", "notification.discord.webhook-url"
        };

        for (String censoredKey : censoredKeys) {
            String[] keyParts = censoredKey.split("\\.");
            JsonObject currentObject = ourConfig;

            for (int i = 0; i < keyParts.length - 1; i++) {
                currentObject = currentObject.getAsJsonObject(keyParts[i]);
            }

            currentObject.addProperty(keyParts[keyParts.length - 1], "censored");
        }

        jsonObject.add("config", ourConfig);

        //now we gotta put any bukkit generated errors in the latest.log file into the logs field, anything that is related to EzChestShop
        //we gonna use the grep command to do that
        File latestLog = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/logs/latest.log");
        if (latestLog.exists()) {
            try {
                // The reader of the logs will load each individual line.
                BufferedReader reader = new BufferedReader(new FileReader(latestLog));
                String line;

                // lines contains anything ecs related. If it's a error message, it will start with [ecserror], it's an
                // error and we'll include the full error message and stacktrace from the errors map.
                List<String> lines = new ArrayList<>();

                // This pattern will match any time in the format of 00:00:00, which is a format used by the majority of
                // loggers like paper, puprur, spigot, bukkit, etc (at least Elito thinks that's what determines the format)
                Pattern pattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2})");

                // The following variables are used to keep track of the current error we are looking at.
                String currentTime = null;
                String latestTime = null;
                String currentKey = null; // key for the error map => first line of an error msg
                boolean lookingForError = false;
                boolean currentECSrelated = false; // only search for ecs related errors
                // Similar errors will be skipped, but we need to make sure we don't skip errors just based on their key,
                // cause the stacktrace might be different.
                boolean currentMarkedForSkipCauseSimilar = false;
                boolean currentWasSimilar = true; // if the current error key is similar to existing error keys
                // List of keys the current errors matches with. Will be filled with similar errors at first, then
                // non-matching errors will be removed until we found a match, or it's a new error (list empty).
                List<String> matchingKeys = new ArrayList<>();

                // Collecting the lines of the current error
                List<String> currentError = null;

                // These two maps keep track of the errors and how many times they have been repeated.
                HashMap<String, List<String>> errors = new HashMap<>();
                HashMap<String, Integer> errorCounter = new HashMap<>();



                // Read each line of the log file
                while ((line = reader.readLine()) != null) {

                    // get the time matching this regex \d{2}:\d{2}:\d{2}
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find())
                    {
                        // if the line contained a timestamp, update the currentTime.
                        currentTime = matcher.group(1);
                    }
                    // save the stuff or increase the error counter if the time changed
                    if (currentTime != null && !currentTime.equals(latestTime)) {

                        if (lookingForError && currentECSrelated) {
                            // check if the current error was marked for skipping cause it's similar to existing errors
                            if (currentMarkedForSkipCauseSimilar) {
                                if (currentWasSimilar) {
                                    // if previous run checks say it's similar just increase the count
                                    errorCounter.put(currentKey, errorCounter.get(currentKey) + 1);
                                } else {
                                    // otherwise we need to find a new key for the error
                                    if (!matchingKeys.isEmpty()) {
                                        // if the previous checks already found identical errors, just get the first one
                                        // we may have others, but those are more likely duplicates, so just take the
                                        // first one.
                                        currentKey = matchingKeys.get(0);
                                    } else {
                                        // if we haven't found a similar one, generate a new key with a unique number
                                        int i = 1;
                                        while (errors.containsKey(currentKey + " (" + i + ")")) {
                                            i++;
                                        }
                                        currentKey = currentKey + " (" + i + ")";
                                    }

                                    // now that we have potentially modified our key, we need to save it as a new entry
                                    // or increase the counter if it already exists.
                                    if (errors.containsKey(currentKey)) {
                                        errorCounter.put(currentKey, errorCounter.get(currentKey) + 1);
                                    } else {
                                        // save key
                                        errors.put(currentKey, currentError);
                                        errorCounter.put(currentKey, 1);
                                        lines.add("[ecserror]" + currentKey);
                                    }
                                }
                                // reset the current (skip related) error variables
                                currentWasSimilar = true;
                                currentMarkedForSkipCauseSimilar = false;
                            } else {
                                // save the key as there is no similar error problem.
                                errors.put(currentKey, currentError);
                                errorCounter.put(currentKey, 1);
                                lines.add("[ecserror]" + currentKey);
                            }
                        }
                        // reset the current error variables
                        matchingKeys.clear();
                        lookingForError = false;
                        currentKey = null;
                        currentECSrelated = false;
                    }
                    // If the line contains this regex: .*\d{2}:\d{2}:\d{2}.*(ERROR|WARN)
                    // then it's an error or warning, so we add it to the logs
                    if (currentTime != null && line.matches(".*\\d{2}:\\d{2}:\\d{2}.*(ERROR|WARN).*")) {

                        if (currentKey == null) {
                            // make error lines comparable by removing the time
                            String newKey = line.replaceAll("\\d{2}:\\d{2}:\\d{2}", "");
                            if (errors.containsKey(newKey)) {
                                // error start already exists, so mark it and note all possible matching keys
                                currentMarkedForSkipCauseSimilar = true;
                                matchingKeys.add(newKey);
                                int i = 1;
                                while (errors.containsKey(newKey + " (" + i + ")")) {
                                    matchingKeys.add(newKey + " (" + i + ")");
                                    i++;
                                }
                            }
                            // start a new error and continue to skip the rest of the checks
                            currentKey = newKey;
                            currentError = new ArrayList<>();
                            currentError.add(currentKey);
                            latestTime = currentTime;
                            lookingForError = true;
                            continue;
                        }
                    }

                    // Check if it's a related log or error piece, then add it to the correct list.
                    boolean ecsRelatedCheck = line.contains("EzChestShop") || line.contains("ECS") ||
                            line.contains("DeadLight") || line.contains("ezchestshop");
                    if (lookingForError) {
                        // the line is part of an error, so we add it to the current error list
                        if (ecsRelatedCheck) {
                            // the line is related to EzChestShop, so we need to check for similar errors and definitely
                            // mark the error/stacktrace to be included in the logs.
                            if (currentMarkedForSkipCauseSimilar) {
                                // Check if the error is identical to existing errors, even to ones with a key offset.
                                // We do that by removing the entry from the matchingKeys list if a previous error doesn't
                                // contain the current line. (All identical errors must have the same ECS related errors)
                                if (!errors.get(currentKey).contains(line)) {
                                    currentWasSimilar = false;
                                    matchingKeys.remove(currentKey);
                                }
                                int i = 1;
                                while (errors.containsKey(currentKey + " (" + i + ")")) {
                                    if (!errors.get(currentKey + " (" + i + ")").contains(line)) {
                                        matchingKeys.remove(currentKey + " (" + i + ")");
                                    }
                                    i++;
                                }
                            }
                            currentECSrelated = true;
                        }
                        currentError.add(line);
                    } else if (ecsRelatedCheck) {
                        // the line is not an error, but it's related to EzChestShop, so we add it to the logs
                        lines.add(line);
                    }

                    // At the end of the loop set the latestTime to the currentTime if it's not null
                    if (currentTime != null) {
                        latestTime = currentTime;
                    }
                }
                reader.close();


                // Collect the results and format them nicely!
                JsonArray logs = new JsonArray();
                for (String log : lines) {
                    if (log.startsWith("[ecserror]") && errors.containsKey(log.substring(10))) {
                        JsonObject errorObject = new JsonObject();
                        JsonArray error = new JsonArray();
                        for (String logLine : errors.get(log.substring(10))) {
                            error.add(logLine.replace("\\t", "    "));
                        }
                        errorObject.add("errorLogs", error);
                        errorObject.addProperty("count", errorCounter.get(log.substring(10)));
                        logs.add(errorObject);
                    } else {
                        logs.add(log);
                    }
                }

                jsonObject.add("logs", logs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

// Create a new JsonObject and put the existing jsonObject inside the "data" field
        JsonObject payload = new JsonObject();
        payload.add("data", jsonObject);


        try {
            URL url = new URL("https://debug.exl.ink/log");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Write the JSON data to the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8"); // use payload instead of jsonObject
                os.write(input, 0, input.length);
            }

            // Read the response
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Check if the field "status" is "ok" in the response, if so, then it's successful
                if (response.toString().contains("\"status\":\"ok\"")) {
                    // We get the "uuid" field from the response, and send it to the player
                    String uuid = response.toString().split("\"uuid\":\"")[1].split("\"")[0];
                    player.sendMessage(ChatColor.GREEN + "Logs uploaded successfully!");
                    player.sendMessage(ChatColor.GREEN + "This is the link to the logs: " + ChatColor.AQUA + "https://debug.exl.ink/log/" + uuid);
                } else {
                    player.sendMessage(ChatColor.RED + "Something went wrong while uploading the logs!");
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
