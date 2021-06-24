package me.deadlight.ezchestshop.Commands;
import com.bgsoftware.wildchests.api.handlers.ChestsManager;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.LanguageManager;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.io.IOException;
import java.util.UUID;

public class MainCommands implements CommandExecutor {

    private EzChestShop plugin = EzChestShop.getPlugin();


    public static LanguageManager lm = new LanguageManager();

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

                } else if (mainarg.equalsIgnoreCase("remove")) {

                        removeShop(player ,args);


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

                if (block.getType() == Material.CHEST) {

                    if (checkIfLocation(block.getLocation(), player)) {


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
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "logs"), PersistentDataType.STRING, "none");
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

            }


        } else {
            player.sendMessage(lm.lookAtChest());
        }

    }
    private void removeShop(Player player, String[] args) {


        Block block = player.getTargetBlockExact(6);

        if (block != null && block.getType() != Material.AIR) {

            BlockState blockState = block.getState();
            if (blockState instanceof TileState) {

                if (block.getType() == Material.CHEST) {

                    if (checkIfLocation(block.getLocation(), player)) {


                    TileState state = (TileState) blockState;

                    PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
                    Chest chkIfDCS = ifItsADoubleChestShop((Chest) block.getState());

                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chkIfDCS != null) {

                        if (chkIfDCS != null) {
                            BlockState newBlockState = chkIfDCS.getBlock().getState();
                            container = ((TileState) newBlockState).getPersistentDataContainer();
                            state = (TileState) newBlockState;
                        }


                        String owner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();

                        if (player.getName().equalsIgnoreCase(owner)) {
                            //is the owner remove it

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
                                container.remove(new NamespacedKey(EzChestShop.getPlugin(), "logs"));
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


                            state.update();
                            player.sendMessage(lm.chestShopRemoved());

                        } else {

                            player.sendMessage(lm.notOwner());

                        }


                    } else {

                        player.sendMessage(lm.notAChestOrChestShop());

                    }
                } else {
                        player.sendMessage(lm.notAllowedToCreateOrRemove());
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
        if (exactBlock == null || exactBlock.getType() == Material.AIR || exactBlock.getType() != Material.CHEST) {
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

    private void checkForNewValues(PersistentDataContainer dataContainer, boolean forceInsert) {



    }

}
