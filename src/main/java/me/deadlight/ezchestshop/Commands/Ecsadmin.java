package me.deadlight.ezchestshop.Commands;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Utils.Utils;
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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Ecsadmin implements CommandExecutor, TabCompleter {

    public static LanguageManager lm = new LanguageManager();

    public static void updateLM(LanguageManager languageManager) {
        Ecsadmin.lm = languageManager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player) {

            Player player = (Player) sender;

            int size = args.length;

            if (player.hasPermission("ecs.admin")) {

                if (size == 0) {
                    sendHelp(player);
                } else {

                    String firstarg = args[0];
                    if (firstarg.equalsIgnoreCase("remove")) {

                        removeShop(player ,args);

                    } else if (firstarg.equalsIgnoreCase("reload")) {

                        Config.loadConfig();
                        Utils.reloadLanguages();
                        player.sendMessage(Utils.color("&aEzChestShop successfully reloaded!"));

                    } else if (firstarg.equalsIgnoreCase("create")) {

                        if (args.length >= 3) {

                            if (isPositive(Double.parseDouble(args[1])) && isPositive(Double.parseDouble(args[2]))) {
                                try {
                                    createShop(player, args);
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
                        sendHelp(player);
                    }

                }


            } else {

                player.sendMessage(Utils.color("&bEz Chest Shop By Dead_Light \n &6Spigot page: https://www.spigotmc.org/resources/ez-chest-shop-ecs-1-14-x-1-16-x.90411/"));

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
        String msg = Utils.color("&cAdmin help: \n " +
                "&7- &c/&eecsadmin remove &7| removes the shop that you are looking at (although u can remove it by breaking it) \n" +
                "&7- &c/&eecsadmin reload &7| reload the plugin configurations \n" +
                "&7- &c/&eecsadmin create (buy) (sell) &7| create an admin shop");
        player.sendMessage(msg);
    }

    private void removeShop(Player player, String[] args) {


        Block block = player.getTargetBlockExact(6);

        if (block != null && block.getType() != Material.AIR) {

            BlockState blockState = block.getState();
            if (blockState instanceof TileState) {

                if (block.getType() == Material.CHEST) {

                        TileState state = (TileState) blockState;

                        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
                        Chest chkIfDCS = ifItsADoubleChestShop((Chest) block.getState());

                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chkIfDCS != null) {

                            if (chkIfDCS != null) {
                                BlockState newBlockState = chkIfDCS.getBlock().getState();
                                container = ((TileState) newBlockState).getPersistentDataContainer();
                                state = (TileState) newBlockState;
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


    private void createShop(Player player, String[] args) throws IOException {

        Block block = player.getTargetBlockExact(6);
        if (block != null && block.getType() != Material.AIR) {
            BlockState blockState = block.getState();

            if (blockState instanceof TileState) {

                if (block.getType() == Material.CHEST) {

                        TileState state = (TileState) blockState;

                        PersistentDataContainer container = state.getPersistentDataContainer();

                        //owner (String) (player name)
                        //buy (double)
                        //sell (double)
                        //item (String) (itemstack)

                        //already a shop
                        if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || ifItsADoubleChestShop((Chest) block.getState()) != null) {

                            player.sendMessage(lm.alreadyAShop());
                            ifItsADoubleChestShop((Chest) block.getState());


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
                                container.set(new NamespacedKey(EzChestShop.getPlugin(), "adminshop"), PersistentDataType.INTEGER, 1);

                                ShopContainer.createShop(block.getLocation(), player, thatItem, buyprice, sellprice, false, false, false, "none", true, "none", true);
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

            }
        }
    }
    public boolean isPositive(double price) {
        if (price < 0) {
            return false;
        } else {
            return true;
        }
    }

    private Chest ifItsADoubleChestShop(Chest chest) {
        //double chest
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
            } else {
                return null;
            }
        } else {
            return null;
        }


    }



}
