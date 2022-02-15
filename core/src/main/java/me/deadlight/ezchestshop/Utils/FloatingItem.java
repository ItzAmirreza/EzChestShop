package me.deadlight.ezchestshop.Utils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.deadlight.ezchestshop.Packets.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class FloatingItem {

//    private WrapperPlayServerEntityDestroy destroy;
    private WrapperPlayServerEntityMetadata meta;
    private WrapperPlayServerSpawnEntity spawn;
    private int entityID;
    private Player player;
    private WrapperPlayServerEntityVelocity velocity;

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

    public FloatingItem(Player player, ItemStack itemStack, Location location) {

        this.spawn = new WrapperPlayServerSpawnEntity();
        //this.destroy = new WrapperPlayServerEntityDestroy();
        this.meta = new WrapperPlayServerEntityMetadata();
        this.velocity = new WrapperPlayServerEntityVelocity();
        this.player = player;
        this.entityID = (int) (Math.random() * Integer.MAX_VALUE);
        this.spawn.setEntityID(entityID);
        this.spawn.setUniqueId(UUID.randomUUID());
        this.spawn.setX(location.getX());
        this.spawn.setY(location.getY());
        this.spawn.setZ(location.getZ());
        this.spawn.setType(EntityType.DROPPED_ITEM);
        //this.destroy.setEntityIds(new int[] { eID });
        this.spawn.sendPacket(player);

        //using packetwrapper
        this.meta.setEntityID(entityID);
        List<WrappedWatchableObject> metadata = new ArrayList<>();
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true)); //setting the value of no graviy to true
        //metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x40)); //just makin the dropped item glow
        int indexofIS = versionUtils.getItemIndex();
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(indexofIS, WrappedDataWatcher.Registry.getItemStackSerializer(false)), BukkitConverters.getItemStackConverter().getGeneric(itemStack))); //ma item
        this.meta.setMetadata(metadata);
        this.meta.sendPacket(player);

        //sending velocity packet
        this.velocity.setEntityID(entityID);
        this.velocity.setVelocityX(0.0);
        this.velocity.setVelocityY(0.0);
        this.velocity.setVelocityZ(0.0);
        this.velocity.sendPacket(player);

    }

    public void destroy() {
        //this.destroy.sendPacket(player);
        PacketContainer destroyEntityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        versionUtils.destroyEntity(player, entityID);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyEntityPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
