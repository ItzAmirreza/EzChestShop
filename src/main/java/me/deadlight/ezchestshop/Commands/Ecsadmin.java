package me.deadlight.ezchestshop.Commands;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class Ecsadmin implements CommandExecutor {
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

                        Utils.reloadConfigs();
                        player.sendMessage(Utils.color("&aEzChestShop successfully reloaded!"));

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

    private void sendHelp(Player player) {
        String msg = Utils.color("&cAdmin help: \n " +
                "&7- &c/&eecsadmin remove &7| removes the shop that you are looking at (although u can remove it by breaking it) \n" +
                "&7- &c/&eecsadmin reload &7| reload the plugin configurations");
        player.sendMessage(msg);
    }

    private void removeShop(Player player, String[] args) {


        Block block = player.getTargetBlockExact(6);

        if (block != null || block.getType() != Material.AIR) {

            BlockState blockState = block.getState();
            if (blockState instanceof TileState) {

                if (block.getType() == Material.CHEST) {
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

                        player.sendMessage(Utils.color("The block that you are looking is not a chest/or this is not a chest shop."));

                    }

                } else {
                    player.sendMessage(Utils.color("The block that you are looking is not a chest/or this is not a chest shop."));
                }

            } else {

                player.sendMessage(Utils.color("The block that you are looking is not a chest/or this is not a chest shop."));
            }


        } else {

            player.sendMessage(Utils.color("The block that you are looking is not a chest/or this is not a chest shop."));

        }



    }

}
