package me.deadlight.ezchestshop.GUIs;

import me.deadlight.ezchestshop.Utils.LogType;
import me.deadlight.ezchestshop.Utils.Utils;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class LogsGUI {

    public LogsGUI() {

    }

    public void showGUI(Player player, PersistentDataContainer data, Chest chest, LogType type, boolean isAdmin) {
        String guititle;
        if (type == LogType.TRANSACTION) {
            guititle = "&aTransaction logs";
        } else {
            guititle = "&aAction logs";
        }

        Gui gui = new Gui(6, guititle);

        ItemStack door = new ItemStack(Material.OAK_DOOR, 1);
        ItemMeta doorMeta = door.getItemMeta();
        doorMeta.setDisplayName(Utils.color("&eBack to settings"));
        door.setItemMeta(doorMeta);
        gui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });
        GuiItem doorItem = new GuiItem(door, event -> {
           event.setCancelled(true);
           OwnerShopGUI ownerShopGUI = new OwnerShopGUI();
           ownerShopGUI.showGUI(player, data, chest, chest, isAdmin);
        });



        //until slot 53 there is space
        gui.setItem(0, doorItem);


    }


}
