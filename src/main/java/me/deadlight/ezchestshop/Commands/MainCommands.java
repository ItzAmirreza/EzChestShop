package me.deadlight.ezchestshop.Commands;
import com.bgsoftware.wildchests.api.handlers.ChestsManager;
import com.bgsoftware.wildchests.api.objects.chests.Chest;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.List;

public class MainCommands implements CommandExecutor {

    private EzChestShop plugin = EzChestShop.getPlugin();


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
                                player.sendMessage(Utils.color("&cNegative price? but you have to use positive price..."));
                            }


                    } else {
                        player.sendMessage(Utils.color("&cYou haven't provided enough arguments! \n" +
                                "&cCorrect usage: /ecs create (Buy price) (Sell price)"));
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
            plugin.logConsole("&cYou are not allowed to execute any command from console.");
        }


        return false;
    }




    private void sendHelp(Player player) {
        String help = "&bEz&cChestShop &7Plugin By Dead_Light© \n" +
                "&7- &c/ecs create (Buy Price) (Sell Price) &7| Create a chest shop by looking at a chest and having the item that you want to sell in your hand. \n" +
                "&7- &c/ecs remove &7| Removes the chest shop that you are looking at \n " +
                "&7Eazy right? :)";
        player.sendMessage(Utils.color(help));
        if (player.hasPermission("admin")) {
            String ahelp = "&cAdmin Help: \n " +
                    "&7- &c/ecsadmin";
            player.sendMessage(Utils.color(ahelp));
        }
    }


    private void createShop(Player player, String[] args) throws IOException {

        Block block = player.getTargetBlockExact(6);
        if (block != null || block.getType() != Material.AIR) {
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
                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                        player.sendMessage(Utils.color("&cThis chest is already a shop!"));


                    } else {
                        //not a shop

                        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
                            ItemStack thatIteminplayer = player.getInventory().getItemInMainHand();
                            ItemStack thatItem = thatIteminplayer.clone();
                            thatItem.setAmount(1);

                            double buyprice = Double.parseDouble(args[1]);
                            double sellprice = Double.parseDouble(args[2]);

                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING, player.getName());
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "buy"), PersistentDataType.DOUBLE, buyprice);
                            container.set(new NamespacedKey(EzChestShop.getPlugin(), "sell"), PersistentDataType.DOUBLE, sellprice);
                            Utils.storeItem(thatItem, container);
                            state.update();
                            player.sendMessage(Utils.color("&aYou have successfully created a chest shop!"));


                        } else {

                            player.sendMessage(Utils.color("&cPlease hold something in your main hand!"));

                        }


                    }

                }
                    else {
                        player.sendMessage(Utils.color("You are not allowed to create/remove a chest shop in this location."));
                    }
                } else {

                    player.sendMessage(Utils.color("&cThe block that you are looking at is not supported type of chest/is not a chest."));

                }

            }


        } else {
            player.sendMessage(Utils.color("&cPlease look at a chest."));
        }

    }
    private void removeShop(Player player, String[] args) {


        Block block = player.getTargetBlockExact(6);

        if (block != null || block.getType() != Material.AIR) {

            BlockState blockState = block.getState();
            if (blockState instanceof TileState) {

                if (block.getType() == Material.CHEST) {

                    if (checkIfLocation(block.getLocation(), player)) {


                    TileState state = (TileState) blockState;

                    PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();

                    if (container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING)) {

                        String owner = container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING);

                        if (player.getName().equalsIgnoreCase(owner)) {
                            //is the owner remove it

                            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "owner"));
                            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "buy"));
                            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "sell"));
                            container.remove(new NamespacedKey(EzChestShop.getPlugin(), "item"));
                            state.update();
                            player.sendMessage(Utils.color("&eThis chest shop successfully removed."));

                        } else {

                            player.sendMessage(Utils.color("&aYou are not the owner of this chest shop!"));

                        }


                    } else {

                        player.sendMessage(Utils.color("&cThe block that you are looking at is not a chest/or this is not a chest shop."));

                    }
                } else {
                        player.sendMessage(Utils.color("You are not allowed to create/remove a chest shop in this location."));
                    }
                } else {
                    player.sendMessage(Utils.color("&cThe block that you are looking at is not a chest/or this is not a chest shop."));
                }

            } else {

                player.sendMessage(Utils.color("&cThe block that you are looking at is not a chest/or this is not a chest shop."));
            }


        } else {

            player.sendMessage(Utils.color("&cThe block that you are looking at is not a chest/or this is not a chest shop."));

        }



    }


    private boolean checkIfLocation(Location location, Player player) {



        if (plugin.integrationWildChests) { // Start of WildChests integration
            ChestsManager cm = plugin.wchests;
            if (cm.getChest(location) != null) {
                Chest schest = cm.getChest(location);
                if (schest.getPlacer().equals(player.getUniqueId())) {
                    return true;

                } else {
                    player.sendMessage(Utils.color("&cYou are not owner of this chest!"));
                    return false;
                }
            }
        } // End of WildChests integration

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

}
