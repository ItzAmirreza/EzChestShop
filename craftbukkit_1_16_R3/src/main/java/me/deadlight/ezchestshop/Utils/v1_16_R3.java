package me.deadlight.ezchestshop.Utils;
import io.netty.channel.Channel;
import me.deadlight.ezchestshop.EzChestShop;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class v1_16_R3 extends VersionUtils {

    private static final Map<SignMenuFactory, UpdateSignListener> listeners = new HashMap<>();

    /**
     * Convert a Item to a Text Compount. Used in Text Component Builders to show
     * items in chat.
     *
     * @category ItemUtils
     * @param itemStack
     * @return
     */
    @Override
    String ItemToTextCompoundString(ItemStack itemStack) {
        // First we convert the item stack into an NMS itemstack
        net.minecraft.server.v1_16_R3.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
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
        ((org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityID));
    }

    @Override
    void spawnHologram(Player player, Location location, String line, int ID) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        World world = craftWorld.getHandle();
        //------------------------------------------------------

        EntityArmorStand armorstand = new EntityArmorStand(world, location.getX(), location.getY(), location.getZ());
        armorstand.setInvisible(true); //invisible
        armorstand.setMarker(true); //Marker
        armorstand.setCustomName(CraftChatMessage.fromStringOrNull(line)); //set custom name
        armorstand.setCustomNameVisible(true); //make custom name visible
        armorstand.setNoGravity(true); //no gravity
        armorstand.e(ID); //set entity id

        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(armorstand, 0);
        playerConnection.sendPacket(packetPlayOutSpawnEntity);
        //------------------------------------------------------
        // sending meta packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(ID, armorstand.getDataWatcher(), true);
        playerConnection.sendPacket(metaPacket);

    }

    @Override
    void spawnFloatingItem(Player player, Location location, ItemStack itemStack, int ID) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        World world = craftWorld.getHandle();
        //------------------------------------------------------

        EntityItem floatingItem = new EntityItem(world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));
        floatingItem.setNoGravity(true); //no gravity
        floatingItem.e(ID); //set entity id
        floatingItem.setMot(0, 0, 0); //set velocity

        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(floatingItem, 0);
        playerConnection.sendPacket(packetPlayOutSpawnEntity);
        //------------------------------------------------------
        // sending meta packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(ID, floatingItem.getDataWatcher(), true);
        playerConnection.sendPacket(metaPacket);

        //sending a velocity packet
        floatingItem.setMot(0, 0, 0);
        PacketPlayOutEntityVelocity velocityPacket = new PacketPlayOutEntityVelocity(floatingItem);
        playerConnection.sendPacket(velocityPacket);

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
                    Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> menu.open(player), 2L);
                }

                if (success) {
                    removeSignMenuFactoryListen(signMenuFactory);
                }

                Bukkit.getScheduler().runTaskLater(EzChestShop.getPlugin(), () -> {
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
        MenuOpener_v1_16_R3.openMenu(menu, player);
    }

    @Override
    public void injectConnection(Player player) {
        ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "ecs_listener", new ChannelHandler_v1_16_R3(player));
    }

    @Override
    public void ejectConnection(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> channel.pipeline().remove("ecs_listener"));
    }

    @Override
    void showOutline(Player player, Block block, int eID) {
        WorldServer worldServer = ((CraftWorld) block.getLocation().getWorld()).getHandle();
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.playerConnection;

        EntityShulker shulker = new EntityShulker(EntityTypes.SHULKER, worldServer);
        shulker.setInvisible(true); //invisible
        shulker.setNoGravity(true); //no gravity
        shulker.setMot(0, 0, 0); //set velocity
        shulker.e(eID); //set entity id
        shulker.i(true); //set outline
        shulker.setNoAI(true); //set noAI
        Location newLoc = block.getLocation().clone();
        //make location be center of the block vertically and horizontally
        newLoc.add(0.5, 0, 0.5);
        shulker.setPosition(newLoc.getX(), newLoc.getY(), newLoc.getZ()); //set position

        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(shulker);
        playerConnection.sendPacket(spawnPacket);

        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(eID, shulker.getDataWatcher(), true);
        playerConnection.sendPacket(metaPacket);

    }


    public static Map<SignMenuFactory, UpdateSignListener> getListeners() {
        return listeners;
    }

}
