package me.deadlight.ezchestshop.Utils;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class MenuOpener {

    private static Constructor<PacketPlayOutTileEntityData> constructor;

    static {
        try {
            constructor = PacketPlayOutTileEntityData.class.getDeclaredConstructor(BlockPosition.class, int.class, NBTTagCompound.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void openMenu(SignMenuFactory.Menu menu, Player player) {
        Objects.requireNonNull(player, "player");
        if (!player.isOnline()) {
            return;
        }

        Location location = player.getLocation();
        Location newLocation = location.clone().add(0, (255 - location.getBlockY()), 0);

        menu.setLocation(newLocation);

        BlockPosition position = new BlockPosition(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());

        player.sendBlockChange(newLocation, Material.OAK_SIGN.createBlockData());

        PacketPlayOutOpenSignEditor editorPacket = new PacketPlayOutOpenSignEditor(position);

        NBTTagCompound compound = new NBTTagCompound();

        for (int line = 0; line < SignMenuFactory.SIGN_LINES; line++) {
            compound.setString("Text" + (line + 1), menu.getText().size() > line ? String.format(SignMenuFactory.NBT_FORMAT, menu.color(menu.getText().get(line))) : "");
        }

        compound.setInt("x", newLocation.getBlockX());
        compound.setInt("y", newLocation.getBlockY());
        compound.setInt("z", newLocation.getBlockZ());
        compound.setString("id", SignMenuFactory.NBT_BLOCK_ID);

        PacketPlayOutTileEntityData tileEntityDataPacket = null;
        try {
            tileEntityDataPacket = constructor.newInstance(position, 9, compound);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        connection.sendPacket(tileEntityDataPacket);
        connection.sendPacket(editorPacket);

        menu.getFactory().getInputs().put(player, menu);
    }

}
