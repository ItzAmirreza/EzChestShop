package me.deadlight.ezchestshop.Utils.Objects;
import org.bukkit.block.Chest;

public class ChatWaitObject {

    public String answer;
    public String type;
    public Chest rightChest;

    public ChatWaitObject(String answer, String type, Chest rightchest) {

        this.answer = answer;
        this.type = type;
        this.rightChest = rightchest;

    }

}
