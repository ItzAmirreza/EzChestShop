package me.deadlight.ezchestshop.utils;

import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.LanguageManager;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

public class BlockMaterialUtils {

    private static LanguageManager lm = new LanguageManager();

    /**
     * Check if the given Block is a Shulker box (dye check)
     *
     * @param block the block to check
     * @return true if it is a shulker box, false if not
     */
    public static boolean isShulkerBox(Block block) {
        return isShulkerBox(block.getType());
    }

    /**
     * Check if the given Material is a Shulker box (dye check)
     *
     * @param type the material to check
     * @return true if it is a shulker box, false if not
     */
    public static boolean isShulkerBox(Material type) {
        return Arrays.asList(Material.SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.GRAY_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.PURPLE_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.BLUE_SHULKER_BOX,
                Material.RED_SHULKER_BOX, Material.BLACK_SHULKER_BOX).contains(type);
    }

    /**
     * Get the Inventory of the given Block if it is a Chest, Barrel or any Shulker
     *
     * @param block the block to get the inventory from
     * @return the inventory of the block, null if not applicable
     */
    public static Inventory getBlockInventory(Block block) {
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            return ((Chest) block.getState()).getInventory();
        } else if (block.getType() == Material.BARREL) {
            return ((Barrel) block.getState()).getInventory();
        } else if (isShulkerBox(block)) {
            return ((ShulkerBox) block.getState()).getInventory();
        } else
            return null;
    }

    /**
     * Check if the given Block is a applicable Shop.
     *
     * @param block the block to check
     * @return true if it is a applicable shop container, false if not
     */
    public static boolean isApplicableContainer(Block block) {
        return isApplicableContainer(block.getType());
    }

    /**
     * Check if the given Material is a applicable Shop.
     *
     * @param type the material to check
     * @return true if it is a applicable shop container, false if not
     */
    public static boolean isApplicableContainer(Material type) {
        return (type == Material.CHEST && Config.container_chests)
                || (type == Material.TRAPPED_CHEST && Config.container_trapped_chests)
                || (type == Material.BARREL && Config.container_barrels)
                || (isShulkerBox(type) && Config.container_shulkers);
    }

    /**
     * Get the PersistentDataContainer of the given Block if it is a Chest, Barrel or any Shulker
     *
     * @param block the block to get the PersistentDataContainer from
     * @return the PersistentDataContainer of the block, null if not applicable
     */
    public static PersistentDataContainer getDataContainer(Block block) {
        PersistentDataContainer dataContainer = null;
        TileState state = (TileState) block.getState();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            dataContainer = getCorrectChest(block).getPersistentDataContainer();
        } else if (block.getType() == Material.BARREL || isShulkerBox(block)) {
            dataContainer = state.getPersistentDataContainer();
        }
        return dataContainer;
    }

    /**
     * Check if the given Block is a Shop and return the blockState if it is.
     * @param player the player who is looking at the block
     * @param sendErrors if the method should send errors to the player
     * @param isCreateOrRemove if the method is used for creating or removing a shop (changes the error message)
     * @param target the block the player is looking at
     * @return the blockState if it is a shop, null if not
     */
    public static BlockState getLookedAtBlockState(Player player, boolean sendErrors, boolean isCreateOrRemove, Block target, boolean checkOwner) {
        if (target == null && target.getType() == Material.AIR) {
            if (sendErrors) {
                player.sendMessage(lm.notAChestOrChestShop());
            }
            return null;
        }
        BlockState blockState = target.getState();
        if (EzChestShop.slimefun) {
            boolean sfresult = BlockStorage.hasBlockInfo(blockState.getBlock().getLocation());
            if (sfresult) {
                player.sendMessage(lm.slimeFunBlockNotSupported());
                return null;
            }
        }
        if (blockState instanceof TileState) {
            if (sendErrors) {
                player.sendMessage(lm.notAChestOrChestShop());
            }
            return null;
        }

        if (BlockMaterialUtils.isApplicableContainer(target)) {
            if (sendErrors) {
                player.sendMessage(lm.notAChestOrChestShop());
            }
            return null;
        }

        if (checkIfLookedAtBlockCanBeBroken(player)) {
            if (sendErrors) {
                if (isCreateOrRemove) {
                    player.sendMessage(lm.notAllowedToCreateOrRemove());
                } else {
                    player.sendMessage(lm.notAChestOrChestShop());
                }
            }
            return null;
        }

        target = getCorrectBlock(target);

        PersistentDataContainer container = ((TileState) blockState).getPersistentDataContainer();
        Chest chkIfDCS = getCorrectChest(target);

        if (!container.has(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING) || chkIfDCS == null) {
            if (sendErrors) {
                player.sendMessage(lm.notAChestOrChestShop());
            }
            return null;
        }
        // if we don't need to check the owner, just return the blockstate as is.
        if (!checkOwner) {
            return blockState;
        }
        String owner = Bukkit.getOfflinePlayer(UUID.fromString(container.get(new NamespacedKey(EzChestShop.getPlugin(), "owner"), PersistentDataType.STRING))).getName();

        if (player.getName().equalsIgnoreCase(owner)) {
            return blockState;
        } else if (sendErrors) {
            player.sendMessage(lm.notOwner());
            return null;
        }
        return null;
    }


    /**
     * Check if the player is allowed to break the block they are looking at.
     * The `ecs.admin` permission bypasses this check.
     *
     * @param player the player who is looking at the block
     * @return true if the player is allowed to break the block, false if not
     */
    public static boolean checkIfLookedAtBlockCanBeBroken(Player player) {
        Block exactBlock = player.getTargetBlockExact(6);
        if (exactBlock == null || exactBlock.getType() == Material.AIR || !(BlockMaterialUtils.isApplicableContainer(exactBlock))) {
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

    /**
     * Get the Chest object of the given Block if it is a Chest or a Double Chest
     * @param block the block to get the chest from
     * @return the chest object, null if not a chest
     */
    public static Chest getCorrectChest(Block block) {
        block = getCorrectBlock(block);
        if (block instanceof Chest) {
            return (Chest) block;
        }
        return null;
    }

    /**
     * If the block is a chest or a double chest, it will return the shop container chest, if available.
     *
     * @param target the block to check
     * @return the shop container chest, if available, otherwise the given block or null if it's air
     */
    public static Block getCorrectBlock(Block target) {
        if (target == null || target.getType() == Material.AIR) return null;
        // return the block if it is not a chest
        if (!(target instanceof Chest)) return target;
        Inventory inventory = BlockMaterialUtils.getBlockInventory(target);
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
