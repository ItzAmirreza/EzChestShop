package me.deadlight.ezchestshop.Utils.Objects;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.persistence.PersistentDataContainer;

public class ChatWaitObject {

    public String answer;
    public String type;
    public Block chest;
    public PersistentDataContainer dataContainer;

    public ChatWaitObject(String answer, String type, Block chest) {

        this.answer = answer;
        this.type = type;
        this.chest = chest;
        this.dataContainer = getDataContainer(chest.getState(), chest.getType());

    }

    public ChatWaitObject(String answer, String type, Block chest, PersistentDataContainer dataContainer) {

        this.answer = answer;
        this.type = type;
        this.chest = chest;
        this.dataContainer = dataContainer;

    }

    private PersistentDataContainer getDataContainer(BlockState state, Material type) {
        if (type == Material.CHEST) {
            return ((Chest) state).getPersistentDataContainer();
        } else if (type == Material.BARREL) {
            return ((Barrel) state).getPersistentDataContainer();
        } else if (Utils.isShulkerBox(type)) {
            return ((ShulkerBox) state).getPersistentDataContainer();
        }
        return null;
    }
}
