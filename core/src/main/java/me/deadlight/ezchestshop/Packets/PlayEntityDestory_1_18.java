package me.deadlight.ezchestshop.Packets;

import org.bukkit.entity.Player;

public class PlayEntityDestory_1_18 {

    public static void destroy(Player player, int entityID) {
        ((org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer) player).getHandle().b.a(new net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy(entityID));
    }
}
