package me.deadlight.ezchestshop.Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.deadlight.ezchestshop.EzChestShop;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Field;
import java.util.Map;

public class ChannelHandler_v1_18_R2 extends ChannelInboundHandlerAdapter {

    private final Player player;
    private static Field updateSignArrays;

    static {
        try {
            updateSignArrays = PacketPlayInUpdateSign.class.getDeclaredField("c");
            updateSignArrays.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public ChannelHandler_v1_18_R2(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws NoSuchFieldException, IllegalAccessException {

        if (msg instanceof PacketPlayInUseEntity) {
            //now we check if player is right clicking on the outline shulkerbox, if so we open the shop for them
            PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;
            Field field = packet.getClass().getDeclaredField("a"); //The field a is entity ID
            field.setAccessible(true);
            int entityID = (int) field.get(packet);
            if (Utils.activeOutlines.containsKey(entityID)) {
                BlockOutline outline = Utils.activeOutlines.get(entityID);
                outline.hideOutline();
                //Then it means somebody is clicking on the outline shulkerbox
                EzChestShop.getPlugin().getServer().getScheduler().runTaskLater(
                        EzChestShop.getPlugin(), () -> {
                            Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInUse(), outline.block, outline.block.getFace(outline.block), null));
                        }, 1L);
            }
        }

        if (msg instanceof PacketPlayInUpdateSign) {
            for (Map.Entry<SignMenuFactory, UpdateSignListener> entry : v1_18_R2.getListeners().entrySet()) {
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
