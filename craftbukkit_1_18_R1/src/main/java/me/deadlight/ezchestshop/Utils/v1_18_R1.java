package me.deadlight.ezchestshop.Utils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class v1_18_R1 extends VersionUtils {

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
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItemStack.b(compound);

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
        ((org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer) player).getHandle().b.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entityID));
    }

    @Override
    void spawnHologram(Player player, Location location, String line, int ID) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.b;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        World world = craftWorld.getHandle();
        //------------------------------------------------------

        EntityArmorStand armorstand = new EntityArmorStand(world, location.getX(), location.getY(), location.getZ());
        armorstand.j(true); //invisible
        armorstand.t(true); //Marker
        armorstand.a(IChatBaseComponent.a(line)); //set custom name
        armorstand.n(true); //make custom name visible
        armorstand.e(true); //no gravity
        armorstand.e(ID); //set entity id

        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(armorstand, 0);
        playerConnection.a(packetPlayOutSpawnEntity);
        //------------------------------------------------------
        // sending meta packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(ID, armorstand.ai(), true);
        playerConnection.a(metaPacket);


    }

    @Override
    void spawnFloatingItem(Player player, Location location, ItemStack itemStack, int ID) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        PlayerConnection playerConnection = entityPlayer.b;
        CraftWorld craftWorld = (CraftWorld) location.getWorld();
        World world = craftWorld.getHandle();
        //------------------------------------------------------

        EntityItem floatingItem = new EntityItem(world, location.getX(), location.getY(), location.getZ(), CraftItemStack.asNMSCopy(itemStack));
        floatingItem.e(true); //no gravity
        floatingItem.e(ID); //set entity id
        floatingItem.n(0, 0, 0); //set velocity

        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(floatingItem, 0);
        playerConnection.a(packetPlayOutSpawnEntity);
        //------------------------------------------------------
        // sending meta packet
        PacketPlayOutEntityMetadata metaPacket = new PacketPlayOutEntityMetadata(ID, floatingItem.ai(), true);
        playerConnection.a(metaPacket);
    }

    @Override
    void signFactoryListen(SignMenuFactory signMenuFactory) {

    }

    @Override
    public void injectConnection(Player player) {
        ((CraftPlayer) player).getHandle().b.a.k.pipeline().addBefore("packet_handler", "EzChestShop Listener", new ChannelHandler(player));
    }

    @Override
    public void ejectConnection(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().b.a.k;
        channel.eventLoop().submit(() -> channel.pipeline().remove("EzChestShop Listener"));
    }


}
