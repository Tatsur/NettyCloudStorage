package com.ttsr;

import com.ttsr.auth.AuthService;
import com.ttsr.commands.AuthCmdData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class AuthCommandHandler extends SimpleChannelInboundHandler<Command> {

    private final ServerApp serverApp;
    private final AuthService authService;

    public AuthCommandHandler(ServerApp serverApp) {
        this.serverApp = serverApp;
        authService = serverApp.getAuthService();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        if (command.getType().equals(CommandType.AUTH)) {
            AuthCmdData authCommand = (AuthCmdData) command.getData();
            String login = authCommand.getLogin();
            String password = authCommand.getPassword();
            System.out.println(login+ " " + password);
            String authLogin = authService.getLogin(login, password);
            System.out.println("authLogin  "+login);
            if (authLogin != null) {
                ctx.writeAndFlush(new Command().okCmd(login,"Authentication successful"));
                ctx.pipeline().remove(AuthCommandHandler.class);
                ctx.pipeline().addLast(new InCommandHandler(serverApp, login));
                ctx.pipeline().get(InCommandHandler.class).channelActive(ctx);
            } else {
                ctx.writeAndFlush(new Command().errCmd("Login or password are incorrect."));
            }
        }
    }
}
