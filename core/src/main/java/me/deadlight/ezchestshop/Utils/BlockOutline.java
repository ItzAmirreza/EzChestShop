package me.deadlight.ezchestshop.Utils;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static me.deadlight.ezchestshop.Utils.Utils.versionUtils;

public class BlockOutline {

    public Player player;
    public Block block;
    public int outlineID;
    public int destroyAfter; //set seconds to destroy after

    public BlockOutline(Player player, Block block) {
        this.player = player;
        this.block = block;
    }

    public void showOutline() {
        outlineID = (int) (Math.random() * Integer.MAX_VALUE);
        versionUtils.showOutline(player, block, outlineID);
        if (Utils.activeOutlines.containsKey(player.getUniqueId().toString())) {
            //Then add the outline to the map with the player's uuid as key in the List
            Utils.activeOutlines.get(player.getUniqueId().toString()).add(this);
        } else {
            //if the player's uuid is not in the map, then create a new list and add the outline to it
            Utils.activeOutlines.put(player.getUniqueId().toString(), new java.util.ArrayList<BlockOutline>());
            Utils.activeOutlines.get(player.getUniqueId().toString()).add(this);
        }
        //check if destroyAfter is not null
        if (destroyAfter != 0) {
            EzChestShop.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(EzChestShop.getPlugin(), () -> {
                versionUtils.destroyEntity(player, outlineID);
                Utils.activeOutlines.get(player.getUniqueId().toString()).remove(this);
            }, destroyAfter * 20L);
        }
    }

    public void hideOutline() {
        versionUtils.destroyEntity(player, outlineID);
    }


}
