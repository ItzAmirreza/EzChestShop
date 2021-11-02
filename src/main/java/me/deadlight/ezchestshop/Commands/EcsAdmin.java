package me.deadlight.ezchestshop.Commands;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Listeners.PlayerCloseToChestListener;
import me.deadlight.ezchestshop.Utils.Utils;
import me.deadlight.ezchestshop.Utils.WorldGuard.FlagRegistry;
import me.deadlight.ezchestshop.Utils.WorldGuard.WorldGuardUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

                        Config.loadConfig();
                        LanguageManager.reloadLanguages();
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

                    } else {
                        sendHelp(player);
                    }

                }


            } else {

                Utils.sendVersionMessage(player);

            }


        } else {
            sender.sendMessage("Cannot work with console execution.");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> fList = new ArrayList<String>();
        List<String> list_firstarg = Arrays.asList("create", "reload", "remove");
        List<String> list_create_1 = Arrays.asList("[BuyPrice]");
        List<String> list_create_2 = Arrays.asList("[SellPrice]");
        if (sender instanceof Player) {
            if (args.length == 1)
                StringUtil.copyPartialMatches(args[0], list_firstarg, fList);
            if (args.length > 1 && args[0].equalsIgnoreCase("create")) {
                if (args.length == 2)
                    StringUtil.copyPartialMatches(args[1], list_create_1, fList);
                if (args.length == 3)
                    StringUtil.copyPartialMatches(args[2], list_create_2, fList);
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
                                PlayerCloseToChestListener.hideHologram(blockState.getLocation());
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
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "trans"), PersistentDataType.STRING, "none");
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 1);
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "rotation"), PersistentDataType.STRING, Config.settings_defaults_rotation);

                                ShopContainer.createShop(target.getLocation(), player, thatItem, buyprice, sellprice, false,
                                        false, false, "none", true, "none", true, Config.settings_defaults_rotation);
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
