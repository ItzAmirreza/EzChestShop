package me.deadlight.ezchestshop.Packets;

import org.bukkit.entity.Player;

public class PlayEntityDestory_1_17_1 {

    public static void destroy(Player player, int entityID) {
        ((org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer) player).getHandle().b.sendPacket(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entityID));
    }
}
