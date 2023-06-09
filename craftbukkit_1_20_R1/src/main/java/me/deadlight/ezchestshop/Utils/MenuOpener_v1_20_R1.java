package me.deadlight.ezchestshop.Utils;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor;
import net.minecraft.network.protocol.game.PacketPlayOutTileEntityData;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class MenuOpener_v1_20_R1 {

    private static Constructor<PacketPlayOutTileEntityData> constructor;

    static {
        try {
            constructor = PacketPlayOutTileEntityData.class.getDeclaredConstructor(BlockPosition.class, TileEntityTypes.class, NBTTagCompound.class);
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
//        Location newLocation = location.clone().add(0, (255 - location.getBlockY()), 0);
        Location newLocation = location.clone().add(0, 4, 0);

        menu.setLocation(newLocation);

        BlockPosition position = new BlockPosition(newLocation.getBlockX(), newLocation.getBlockY(), newLocation.getBlockZ());

        player.sendBlockChange(newLocation, Material.OAK_SIGN.createBlockData());

        PacketPlayOutOpenSignEditor editorPacket = new PacketPlayOutOpenSignEditor(position, true);

        NBTTagCompound compound = new NBTTagCompound();

        NBTTagCompound frontText = new NBTTagCompound();
        NBTTagCompound backText = new NBTTagCompound();
        NBTTagList backMessages = new NBTTagList();
        NBTTagList frontMessages = new NBTTagList();


        for (int line = 0; line < SignMenuFactory.SIGN_LINES; line++) {
            String text = menu.getText().size() > line ? String.format(SignMenuFactory.NBT_FORMAT, menu.color(menu.getText().get(line))) : "";
            NBTTagString nbtString = NBTTagString.a(text);

            // Assuming you want to set the same text for both back and front
            backMessages.add(nbtString);
            frontMessages.add(nbtString);
        }

        backText.a("messages", backMessages);
        frontText.a("messages", frontMessages);
        compound.a("back_text", backText);
        compound.a("front_text", frontText);


//        for (int line = 0; line < SignMenuFactory.SIGN_LINES; line++) {
//            compound.a("Text" + (line + 1), menu.getText().size() > line ? String.format(SignMenuFactory.NBT_FORMAT, menu.color(menu.getText().get(line))) : "");
//        } no loger works this way in 1.20, we need to use the new method above


        PacketPlayOutTileEntityData tileEntityDataPacket = null;
        try {
            tileEntityDataPacket = constructor.newInstance(position, TileEntityTypes.h, compound);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        PlayerConnection connection = ((CraftPlayer) player).getHandle().c;

        connection.a(tileEntityDataPacket);
        connection.a(editorPacket);

        menu.getFactory().getInputs().put(player, menu);
    }

}
