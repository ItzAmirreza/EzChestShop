package me.deadlight.ezchestshop.Utils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.bukkit.entity.Player;

public class ChannelHandler extends ChannelInboundHandlerAdapter {


    private final Player player;

    public ChannelHandler(Player player) {
        this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
// object msg hamon packete
// mishe be packet class haye nms cast krdesh
// inja age ctx.fireChannelRead call nashe packet be nms nemirese
        ctx.fireChannelRead(msg);
    }

}
