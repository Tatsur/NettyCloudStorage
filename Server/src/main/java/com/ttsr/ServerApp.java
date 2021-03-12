package com.ttsr;

import com.ttsr.auth.AuthService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.ConcurrentHashMap;

public class ServerApp {

    private final String SERVER_DIR = "src/Data/";

    private AuthService authService;
    private ConcurrentHashMap<ChannelHandlerContext, String> clients;

    public AuthService getAuthService() {
        return authService;
    }

    public ServerApp() {
        clients = new ConcurrentHashMap<>();
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new OutCommandHandler(),
                                    new AuthCommandHandler(ServerApp.this)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            System.out.println("Server started");
            authService = new AuthService();
            authService.start();
            future.channel().closeFuture().sync();//block
        } catch (InterruptedException e) {
            System.out.println("Server was interrupted");
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            authService.stop();
        }
    }

    public ConcurrentHashMap<ChannelHandlerContext, String> getClients() {
        return clients;
    }

    public static void main(String[] args) {
        new ServerApp();
    }
}
