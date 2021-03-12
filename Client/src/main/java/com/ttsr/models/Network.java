package com.ttsr.models;

import com.ttsr.Command;
import com.ttsr.CommandType;
import com.ttsr.ClientApp;
import com.ttsr.commands.*;
import com.ttsr.controllers.ViewController;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableDoubleValue;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Network {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8189;
    public static final int BUFFER_SIZE = 32768;
    public static byte[] buffer;

    private final String host;
    private final int port;

    public String login;
    public ViewController viewController;
    private ClientApp clientApp;

    public ClientApp getClientApp() {
        return clientApp;
    }

    public void setClientApp(ClientApp clientApp) {
        this.clientApp = clientApp;
    }

    public boolean connected;
    public boolean authOk;

    private Channel channel;
    private EventLoopGroup group;

    public void setViewController(ViewController viewController) {
        this.viewController = viewController;
    }

    public Network() {
        this(SERVER_ADDRESS, SERVER_PORT);
    }

    public Network(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(SERVER_ADDRESS,SERVER_PORT)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new OutHandler(),
                                    new AuthHandler(Network.this)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.connect().sync();
            buffer = new byte[BUFFER_SIZE];
            channel = future.channel();
            connected = true;
            sendFileListRequest(null);
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            connected = false;
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendAuthCommand(String login, String password) {
        try {
            Command authCommand = new Command().authCmd(login, password);
            sendCommand(authCommand);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        channel.writeAndFlush(new Command().errCmd("Client closed connection"));
        group.shutdownGracefully();
    }
    private  void sendCommand(Command command) throws IOException {
        channel.writeAndFlush(command);
    }

    public String getLogin() {
        return login;
    }

    public void sendFileRequest(String fileName, Long fileSize) throws IOException {
        Command fileCmdData = new Command().sendFile(fileName,fileSize);
        sendCommand(fileCmdData);
    }
    public void getFileRequest(String fileName) throws IOException {
        Command getFileCmdData = new Command().getFile(fileName);
        sendCommand(getFileCmdData);
    }
    public void sendFileListRequest(String directory) throws IOException {
        Command fileListCmdData = new Command().sendFileList(new ArrayList<>(), directory);
        sendCommand(fileListCmdData);
    }
    public void sendFile(ViewController viewController) {
        File fileToSend = viewController.getSelectedFile();
        Long fileSize = fileToSend.length();
        viewController.progressBar.setVisible(true);
        try (InputStream fis = new FileInputStream(fileToSend)) {
            int pointer = 0;
            while (fileSize > buffer.length) {
                fileSize -= pointer;
                pointer = fis.read(buffer);
                sendCommand(new Command().file(buffer, pointer));
            }
            byte[] lastBytes = new byte[Math.toIntExact(fileSize)];
            pointer = fis.read(lastBytes);
            sendCommand(new Command().file(lastBytes, pointer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
