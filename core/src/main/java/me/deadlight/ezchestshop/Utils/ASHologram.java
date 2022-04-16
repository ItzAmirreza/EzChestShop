package me.deadlight.ezchestshop.Utils;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ASHologram {

    private int entityID;
    private String name;
    private Player handler;

    public ASHologram(Player p, String name, EntityType type,Location loc,boolean isGlowing) {
        UUID uuid = UUID.randomUUID();

        this.name = name;
        this.entityID = (int) (Math.random() * Integer.MAX_VALUE);
        this.handler = p;
        Utils.versionUtils.spawnHologram(p, loc, name, entityID);
    }


    public void destroy() {
        Utils.versionUtils.destroyEntity(handler, entityID);
    }

    public void setHandler(Player p) {
        this.handler = p;
    }
    public String getName() {
        return name;
    }
}