package me.deadlight.ezchestshop.Utils;
import java.util.UUID;
import java.util.logging.Level;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.deadlight.ezchestshop.Packets.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ASHologram {

    private int entityID;
//    private WrapperPlayServerSpawnEntity spawn;
//    private WrapperPlayServerEntityMetadata meta;
    //private WrapperPlayServerEntityDestroy destroy;
    private String name;
    private Player handler;

    static VersionUtils versionUtils;

    static {
        try {
            String packageName = Utils.class.getPackage().getName();
            String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            versionUtils = (VersionUtils) Class.forName(packageName + "." + internalsName).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "EzChestShop could not find a valid implementation for this server version.");
        }
    }

    public ASHologram(Player p, String name, EntityType type,Location loc,boolean isGlowing) {
        UUID uuid = UUID.randomUUID();

        this.name = name;
        this.entityID = (int) (Math.random() * Integer.MAX_VALUE);
        this.handler = p;
        versionUtils.spawnHologram(p, loc, name, entityID);
    }


    public void destroy() {
        PacketContainer destroyEntityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        versionUtils.destroyEntity(this.handler, entityID);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(handler, destroyEntityPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setHandler(Player p) {
        this.handler = p;
    }
    public String getName() {
        return name;
    }
}