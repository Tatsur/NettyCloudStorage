package com.ttsr.models;

import com.ttsr.ClientApp;
import com.ttsr.Command;
import com.ttsr.CommandType;
import com.ttsr.commands.AuthCmdData;
import com.ttsr.commands.ErrCmdData;
import com.ttsr.commands.OkCmdData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class AuthHandler extends SimpleChannelInboundHandler<Command> {

    private Network network;

    public AuthHandler(Network network) {
        this.network = network;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        switch (command.getType()){
            case OK: {
                OkCmdData okCmdData = (OkCmdData) command.getData();
                System.out.println(okCmdData.getMessage());
                ctx.pipeline().addLast(new InHandler(network));
                ctx.pipeline().remove(AuthHandler.class);
                ctx.pipeline().get(InHandler.class).channelActive(ctx);
                break;
            }
            case ERROR:{
                System.out.println("got auth_error command");
                ErrCmdData data = (ErrCmdData) command.getData();
                ClientApp.showNetworkError(data.getErrMsg(),"auth error");
                break;
            }
            default:
                ClientApp.showNetworkError("Unknown type of command from server: " + command.getType(),"auth bad request");
        }
    }
}
