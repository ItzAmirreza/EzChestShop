package me.deadlight.ezchestshop.Utils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Packets.*;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FloatingItem {

//    private WrapperPlayServerEntityDestroy destroy;
    private WrapperPlayServerEntityMetadata meta;
    private WrapperPlayServerSpawnEntity spawn;
    private int entityID;
    private Player player;
    private WrapperPlayServerEntityVelocity velocity;


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

        //using packetwrapper
        this.meta.setEntityID(entityID);
        List<WrappedWatchableObject> metadata = new ArrayList<>();
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true)); //setting the value of no graviy to true
        //metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x40)); //just makin the dropped item glow
        int indexofIS = 7;
        if (Utils.family1_17) {
            indexofIS = 8;
        }
        metadata.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(indexofIS, WrappedDataWatcher.Registry.getItemStackSerializer(false)), BukkitConverters.getItemStackConverter().getGeneric(itemStack))); //ma item
        this.meta.setMetadata(metadata);

        //sending velocity packet
        this.velocity.setEntityID(entityID);
        this.velocity.setVelocityX(0.0);
        this.velocity.setVelocityY(0.0);
        this.velocity.setVelocityZ(0.0);

    }

    public void spawn() {
        this.spawn.sendPacket(player);
        this.meta.sendPacket(player);
        this.velocity.sendPacket(player);
    }

    public void destroy() {
        //this.destroy.sendPacket(player);
        EzChestShop.logDebug("Destroying...");
        PacketContainer destroyEntityPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        if (Utils.is1_17) {
            destroyEntityPacket.getIntegers().writeSafely(0, entityID);
        } else if (Utils.is1_17_1) {
            PlayEntityDestory_1_17_1.destroy(player, entityID);
        } else {
            destroyEntityPacket.getIntegers().writeSafely(0, 1);
            destroyEntityPacket.getIntegerArrays().writeSafely(0, new int[]{entityID});
        }
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, destroyEntityPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EzChestShop.logDebug("Destroyed.");
    }



}
