package me.deadlight.ezchestshop.Commands;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.GUI.GuiData;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.ShopCommandManager;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.GUIs.GuiEditorGUI;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Listeners.UpdateChecker;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import me.deadlight.ezchestshop.Utils.WorldGuard.WorldGuardUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                            player.sendMessage("Enable this setting in the config!");
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
                                if (args.length < 3) {
                                    Config.shopCommandManager.showActionEditor(player, location);
                                } else {
                                    if (location != null) {
                                        ShopCommandManager.ShopAction action = ShopCommandManager.ShopAction.valueOf(args[2]);
                                        if (args.length == 3) {
                                            Config.shopCommandManager.showCommandEditor(player, location, action);
                                        } else {
                                            // longer then 3 args
                                            if (args[3].equals("add")) {
                                                if (args.length >= 5) {
                                                    // get the command from any further args
                                                    String newCommand = "";
                                                    for (int i = 4; i < args.length; i++) {
                                                        newCommand += args[i] + " ";
                                                    }
                                                    Config.shopCommandManager.addCommand(player, location, action, newCommand.trim());
                                                }
                                            } else if (args[3].equals("move")) {
                                                if (args.length == 6) {
                                                    Config.shopCommandManager.moveCommandIndex(player, location, action, Integer.parseInt(args[4]), args[5].equals("up"));
                                                }

                                            } else if (args[3].equals("edit")) {
                                                if (args.length >= 6) {
                                                    // get the command from any further args
                                                    String newCommand = "";
                                                    for (int i = 5; i < args.length; i++) {
                                                        newCommand += args[i] + " ";
                                                    }
                                                    Config.shopCommandManager.editCommand(player, location, action, Integer.parseInt(args[4]), newCommand.trim());
                                                }

                                            } else if (args[3].equals("remove")) {
                                                if (args.length == 5) {
                                                    Config.shopCommandManager.removeCommand(player, location, action, Integer.parseInt(args[4]));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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
        LanguageManager.reloadLanguages();
        GuiData.loadGuiData();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> fList = new ArrayList<>();
        List<String> list_firstarg = Arrays.asList("create", "reload", "remove", "help", "transfer-ownership", "configure-guis", "shop-commands");
        List<String> list_create_1 = Arrays.asList("[BuyPrice]");
        List<String> list_create_2 = Arrays.asList("[SellPrice]");
        List<String> list_transfer_2 = Arrays.asList("-confirm");
        if (sender instanceof Player) {
            Player p = (Player) sender;
            List<String> list_shop_commands_1 = Arrays.asList(Utils.LocationRoundedtoString(p.getTargetBlockExact(6).getLocation(), 0));
            List<String> list_shop_commands_2 = Arrays.asList(Arrays.stream(ShopCommandManager.ShopAction.values()).map(Enum::name).toArray(String[]::new));
            List<String> list_shop_commands_3 = Arrays.asList("add", "move", "edit", "remove");
            List<String> list_shop_commands_4 = Arrays.asList("[index]");
            List<String> list_shop_commands_editcreate_5 = Arrays.asList("[command]");
            List<String> list_shop_commands_move_5 = Arrays.asList("up", "down");
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
                        if (args[3].equalsIgnoreCase("add")) {
                            StringUtil.copyPartialMatches(args[4], list_shop_commands_editcreate_5, fList);
                        } else {
                            StringUtil.copyPartialMatches(args[4], list_shop_commands_4, fList);
                        }
                    } else if (args.length >= 6) {
                        if (args[3].equalsIgnoreCase("add") || args[3].equalsIgnoreCase("edit")) {
                            StringUtil.copyPartialMatches(args[args.length - 1], list_shop_commands_editcreate_5, fList);
                        } else if (args[3].equalsIgnoreCase("move") && args.length == 6) {
                            StringUtil.copyPartialMatches(args[5], list_shop_commands_move_5, fList);
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
                                        player.sendMessage(lm.notAllowedToCreateOrRemove());
                                        return;
                                    }
                                } else {
                                    if (!WorldGuardUtils.queryStateFlag(FlagRegistry.REMOVE_SHOP, player)) {
                                        player.sendMessage(lm.notAllowedToCreateOrRemove());
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
                                PlayerCloseToChestListener.hideHologram(blockState.getLocation(), true);
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
                    player.sendMessage(lm.notAllowedToCreateOrRemove());
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

                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, player.getUniqueId().toString());
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE, buyprice);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE, sellprice);
                                //add new settings data later
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "msgtoggle"), PersistentDataType.INTEGER, Config.settings_defaults_transactions ? 1 : 0);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "dbuy"), PersistentDataType.INTEGER, Config.settings_zero_equals_disabled ?
                                        (buyprice == 0 ? 1 : (Config.settings_defaults_dbuy ? 1 : 0))
                                        : (Config.settings_defaults_dbuy ? 1 : 0));
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "dsell"), PersistentDataType.INTEGER, Config.settings_zero_equals_disabled ?
                                        (sellprice == 0 ? 1 : (Config.settings_defaults_dsell ? 1 : 0))
                                        : (Config.settings_defaults_dsell ? 1 : 0));
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "admins"), PersistentDataType.STRING, "none");
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "shareincome"), PersistentDataType.INTEGER, Config.settings_defaults_shareprofits ? 1 : 0);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 1);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, Config.settings_defaults_rotation);

                                ShopContainer.createShop(target.getLocation(), player, thatItem, buyprice, sellprice, false,
                                        false, false, "none", true, true, Config.settings_defaults_rotation);
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
}
