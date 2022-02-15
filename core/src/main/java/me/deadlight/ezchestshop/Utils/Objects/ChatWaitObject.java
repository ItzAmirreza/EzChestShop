package me.deadlight.ezchestshop.Utils.Objects;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.persistence.PersistentDataContainer;

public class ChatWaitObject {

    public String answer;
    public String type;
    public Block containerBlock;
    public PersistentDataContainer dataContainer;

    public ChatWaitObject(String answer, String type, Block containerBlock) {

        this.answer = answer;
        this.type = type;
        this.containerBlock = containerBlock;
        this.dataContainer = getDataContainer(containerBlock.getState(), containerBlock.getType());

    }

    public ChatWaitObject(String answer, String type, Block containerBlock, PersistentDataContainer dataContainer) {

        this.answer = answer;
        this.type = type;
        this.containerBlock = containerBlock;
        this.dataContainer = dataContainer;

    }

    private PersistentDataContainer getDataContainer(BlockState state, Material type) {
        if (type == Material.CHEST || type == Material.TRAPPED_CHEST) {
            return ((Chest) state).getPersistentDataContainer();
        } else if (type == Material.BARREL) {
            return ((Barrel) state).getPersistentDataContainer();
        } else if (Utils.isShulkerBox(type)) {
            return ((ShulkerBox) state).getPersistentDataContainer();
        }
        return null;
    }
}
