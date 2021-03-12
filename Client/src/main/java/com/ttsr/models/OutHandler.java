package com.ttsr.models;

import com.ttsr.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object command, ChannelPromise promise) throws Exception {
        Command cmdToServer = (Command) command;
        ctx.writeAndFlush(cmdToServer);
    }
}
