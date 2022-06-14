package me.deadlight.ezchestshop.Utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.server.v1_14_R1.PacketPlayInUpdateSign;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;

public class ChannelHandler extends ChannelInboundHandlerAdapter {

    private final Player player;
    private static Field updateSignArrays;

    static {
        try {
            updateSignArrays = PacketPlayInUpdateSign.class.getDeclaredField("b");
            updateSignArrays.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public ChannelHandler(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof PacketPlayInUpdateSign) {
            for (Map.Entry<SignMenuFactory, UpdateSignListener> entry : v1_14_R1.getListeners().entrySet()) {
                UpdateSignListener listener = entry.getValue();

                try {
                    listener.listen(player, (String[]) updateSignArrays.get(msg));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (listener.isCancelled()) {
                    return;
                }
            }
        }

        ctx.fireChannelRead(msg);
    }

}
