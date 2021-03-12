package com.ttsr;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutCommandHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object command, ChannelPromise promise) throws Exception {
        Command cmdToClient = (Command) command;
        ctx.writeAndFlush(cmdToClient);
    }
}
