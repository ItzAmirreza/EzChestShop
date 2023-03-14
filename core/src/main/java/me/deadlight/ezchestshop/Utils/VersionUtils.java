package me.deadlight.ezchestshop.Utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class VersionUtils {

    abstract String ItemToTextCompoundString(ItemStack itemStack);

    abstract int getItemIndex();

    abstract int getArmorStandIndex();

    abstract void destroyEntity(Player player, int entityID);

    abstract void spawnHologram(Player player, Location location, String line, int ID);

    abstract void spawnFloatingItem(Player player, Location location, ItemStack itemStack, int ID);

    abstract void signFactoryListen(SignMenuFactory signMenuFactory);

    abstract void removeSignMenuFactoryListen(SignMenuFactory signMenuFactory);

    abstract void openMenu(SignMenuFactory.Menu menu, Player player);

    public abstract void injectConnection(Player player) throws IllegalAccessException, NoSuchFieldException;

    public abstract void ejectConnection(Player player) throws NoSuchFieldException, IllegalAccessException;

}
