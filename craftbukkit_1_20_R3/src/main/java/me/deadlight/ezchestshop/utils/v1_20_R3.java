package me.deadlight.ezchestshop.utils;

import io.netty.channel.Channel;
import me.deadlight.ezchestshop.EzChestShop;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class v1_20_R3 extends VersionUtils {

    private static final Map<SignMenuFactory, UpdateSignListener> listeners = new HashMap<>();
    private static Map<Integer, Entity> entities = new HashMap<>();

    /**
     * Convert an Item to a Text Compount. Used in Text Component Builders to show
     * items in chat.
     *
     * @category ItemUtils
     * @param itemStack
     * @return
     */

    @Override
    String ItemToTextCompoundString(ItemStack itemStack) {
        // First we convert the item stack into an NMS itemstack
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        CompoundTag compound = new CompoundTag();
        compound = nmsItemStack.save(compound);

        return compound.toString();
    }

    @Override
    int getArmorStandIndex() {
        return 15;
    }

    @Override
    int getItemIndex() {
        return 8;
    }

    @Override
    void destroyEntity(Player player, int entityID) {
        ((CraftPlayer) player).getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket(entityID));
        entities.remove(entityID);
    }

    @Override
    void spawnHologram(Player player, Location location, String line, int ID) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer ServerPlayer = craftPlayer.getHandle();
        ServerGamePacketListenerImpl ServerGamePacketListenerImpl = ServerPlayer.connection;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        Level world = craftWorld.getHandle();
        //------------------------------------------------------

        ArmorStand armorstand = new ArmorStand(world, location.getX(), location.getY(), location.getZ());
        armorstand.setInvisible(true); //invisible
        armorstand.setMarker(true); //Marker
        armorstand.setCustomName(CraftChatMessage.fromStringOrNull(line)); //set custom name
        armorstand.setCustomNameVisible(true); //make custom name visible
        armorstand.setNoGravity(true); //no gravity
        armorstand.setId(ID); //set entity id

        ClientboundAddEntityPacket ClientboundAddEntityPacket = new ClientboundAddEntityPacket(armorstand, 0);
        ServerGamePacketListenerImpl.send(ClientboundAddEntityPacket);
        //------------------------------------------------------
        //create a list of datawatcher objects

        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(ID, armorstand.getEntityData().getNonDefaultValues());
        ServerGamePacketListenerImpl.send(metaPacket);
        entities.put(ID, armorstand);
    }

    @Override
    void spawnFloatingItem(Player player, Location location, ItemStack itemStack, int ID) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer ServerPlayer = craftPlayer.getHandle();
        ServerGamePacketListenerImpl ServerGamePacketListenerImpl = ServerPlayer.connection;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        Level world = craftWorld.getHandle();
        //------------------------------------------------------

        ItemEntity floatingItem = new ItemEntity(world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));
        floatingItem.setNoGravity(true); //no gravity
        floatingItem.setId(ID); //set entity id
        floatingItem.setDeltaMovement(0, 0, 0); //set velocity

        ClientboundAddEntityPacket ClientboundAddEntityPacket = new ClientboundAddEntityPacket(floatingItem, 0);
        ServerGamePacketListenerImpl.send(ClientboundAddEntityPacket);
        //------------------------------------------------------
        // sending meta packet
        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(ID, floatingItem.getEntityData().getNonDefaultValues());
        ServerGamePacketListenerImpl.send(metaPacket);

        //sending a velocity packet
        floatingItem.setDeltaMovement(0, 0, 0);
        ClientboundSetEntityMotionPacket velocityPacket = new ClientboundSetEntityMotionPacket(floatingItem);
        ServerGamePacketListenerImpl.send(velocityPacket);
        entities.put(ID, floatingItem);
    }

    void renameEntity(Player player, int entityID, String newName) {
        try {
            // the entity only exists on the client, how can I get it?
            Entity e = entities.get(entityID);
            e.setCustomName(CraftChatMessage.fromStringOrNull(newName));
            ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entityID, e.getEntityData().getNonDefaultValues());
            ((CraftPlayer) player).getHandle().connection.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void teleportEntity(Player player, int entityID, Location location) {
        ServerPlayer ServerPlayer = ((CraftPlayer) player).getHandle();
        Entity e = entities.get(entityID);
        Set<RelativeMovement> set = new HashSet<>();
        e.teleportTo(ServerPlayer.serverLevel(), location.getX(), location.getY(), location.getZ(), set, 0, 0);
        // not sure if it's needed
        ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(e);
        ServerPlayer.connection.send(packet);
    }

    @Override
    void signFactoryListen(SignMenuFactory signMenuFactory) {

        listeners.put(signMenuFactory, new UpdateSignListener() {
            @Override
            public void listen(Player player, String[] array) {

                SignMenuFactory.Menu menu = signMenuFactory.getInputs().remove(player);

                if (menu == null) {
                    return;
                }
                setCancelled(true);

                boolean success = menu.getResponse().test(player, array);

                if (!success && menu.isReopenIfFail() && !menu.isForceClose()) {
                    EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> menu.open(player), 2L);
                }

                removeSignMenuFactoryListen(signMenuFactory);

                EzChestShop.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
                    if (player.isOnline()) {
                        Location location = menu.getLocation();
                        player.sendBlockChange(location, location.getBlock().getBlockData());
                    }
                }, 2L);


            }
        });

    }

    @Override
    void removeSignMenuFactoryListen(SignMenuFactory signMenuFactory) {

        listeners.remove(signMenuFactory);

    }

    @Override
    void openMenu(SignMenuFactory.Menu menu, Player player) {
        MenuOpener_v1_20_R3.openMenu(menu, player);
    }

    @Override
    public void injectConnection(Player player) throws IllegalAccessException, NoSuchFieldException {
        //changed from h in 1.20.2 as this is now accessed through inheritance
        Field field = ((CraftPlayer) player).getHandle().connection.getClass().getSuperclass().getDeclaredField("c");
        field.setAccessible(true);
        Connection netManager = (Connection) field.get(((CraftPlayer) player).getHandle().connection);
        netManager.channel.pipeline().addBefore("packet_handler", "ecs_listener", new ChannelHandler_v1_20_R3(player));
    }

    @Override
    public void ejectConnection(Player player) throws NoSuchFieldException, IllegalAccessException {
        Field field = ((CraftPlayer) player).getHandle().connection.getClass().getSuperclass().getDeclaredField("c");
        field.setAccessible(true);
        Connection netManager = (Connection) field.get(((CraftPlayer) player).getHandle().connection);
        Channel channel = netManager.channel;
        channel.eventLoop().submit(() -> channel.pipeline().remove("ecs_listener"));
    }

    @Override
    void showOutline(Player player, Block block, int eID) {
        ServerLevel ServerLevel = ((CraftWorld) block.getLocation().getWorld()).getHandle();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer ServerPlayer = craftPlayer.getHandle();
        ServerGamePacketListenerImpl ServerGamePacketListenerImpl = ServerPlayer.connection;

        Shulker shulker = new Shulker(EntityType.SHULKER, ServerLevel);
        shulker.setInvisible(true); //invisible
        shulker.setNoGravity(true); //no gravity
        shulker.setDeltaMovement(0, 0, 0); //set velocity
        shulker.setId(eID); //set entity id
        shulker.setGlowingTag(true); //set outline
        shulker.setNoAi(true); //set noAI
        Location newLoc = block.getLocation().clone();
        //make location be center of the block vertically and horizontally
        newLoc.add(0.5, 0, 0.5);
        shulker.setPos(newLoc.getX(), newLoc.getY(), newLoc.getZ()); //set position

        ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(shulker);
        ServerGamePacketListenerImpl.send(spawnPacket);

        ClientboundSetEntityDataPacket metaPacket = new ClientboundSetEntityDataPacket(eID, shulker.getEntityData().getNonDefaultValues());
        ServerGamePacketListenerImpl.send(metaPacket);

    }

    public static Map<SignMenuFactory, UpdateSignListener> getListeners() {
        return listeners;
    }


}
