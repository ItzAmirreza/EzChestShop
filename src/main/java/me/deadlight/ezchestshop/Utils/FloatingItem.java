package me.deadlight.ezchestshop.Utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.deadlight.ezchestshop.Packets.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FloatingItem {

    private WrapperPlayServerEntityDestroy destroy;
    private WrapperPlayServerEntityMetadata meta;
    private WrapperPlayServerSpawnEntity spawn;
    private int eID;
    private Player player;
    private WrapperPlayServerEntityVelocity velocity;


    public FloatingItem(Player player, ItemStack itemStack, Location location) {

        this.spawn = new WrapperPlayServerSpawnEntity();
        this.destroy = new WrapperPlayServerEntityDestroy();
        this.meta = new WrapperPlayServerEntityMetadata();
        this.velocity = new WrapperPlayServerEntityVelocity();
        this.player = player;
        this.eID = (int) (Math.random() * Integer.MAX_VALUE);
        this.spawn.setEntityID(eID);
        this.spawn.setUniqueId(UUID.randomUUID());
        this.spawn.setX(location.getX());
        this.spawn.setY(location.getY());
        this.spawn.setZ(location.getZ());
        this.spawn.setType(EntityType.DROPPED_ITEM);
        this.destroy.setEntityIds(new int[] { eID });
        this.spawn.sendPacket(player);

        //using packetwrapper
        this.meta.setEntityID(eID);
        List<WrappedWatchableObject> metadata = new ArrayList<>();
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true)); //setting the value of no graviy to true
        //metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x40)); //just makin the dropped item glow
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(7, WrappedDataWatcher.Registry.getItemStackSerializer(false)), BukkitConverters.getItemStackConverter().getGeneric(itemStack))); //ma item
        this.meta.setMetadata(metadata);
        this.meta.sendPacket(player);

        //sending velocity packet
        this.velocity.setEntityID(eID);
        this.velocity.setVelocityX(0.0);
        this.velocity.setVelocityY(0.0);
        this.velocity.setVelocityZ(0.0);
        this.velocity.sendPacket(player);

    }

    public void destroy() {
        this.destroy.sendPacket(player);
    }



}
