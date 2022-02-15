package me.deadlight.ezchestshop.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

abstract class VersionUtils {

    abstract String ItemToTextCompoundString(ItemStack itemStack);

    abstract int getItemIndex();

    abstract int getArmorStandIndex();

    abstract void destroyEntity(Player player, int entityID);

}
