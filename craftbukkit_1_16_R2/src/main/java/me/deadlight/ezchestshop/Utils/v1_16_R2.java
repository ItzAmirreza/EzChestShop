package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.EzChestShop;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityDestroy;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class v1_16_R2 extends VersionUtils {

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
        net.minecraft.server.v1_16_R2.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound compound = new NBTTagCompound();
        compound = nmsItemStack.save(compound);

        return compound.toString();
    }

    @Override
    int getArmorStandIndex() {
        return 14;
    }

    @Override
    int getItemIndex() {
        return 7;
    }

    @Override
    void destroyEntity(Player player, int entityID) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityID));
    }



}
