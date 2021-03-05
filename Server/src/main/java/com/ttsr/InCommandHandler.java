package com.ttsr;

import com.ttsr.commands.GetFileCmdData;
import com.ttsr.commands.SendFileCmdData;
import com.ttsr.commands.SendFileListData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InCommandHandler extends SimpleChannelInboundHandler<Command> {

    private final ServerApp serverApp;
    private String userLogin;
    private String userDir;
    private final byte[] buffer = new byte[4096];
    private String filename;
    private Long filesize;
    private File file;

    public InCommandHandler(ServerApp serverApp, String userLogin) {
        this.serverApp = serverApp;
        userDir = userDir+"/"+userLogin;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(userLogin + "connected");
        File file = new File(userDir);
        if(!file.exists())
        {
            new File(userDir).mkdir();
        }
        Command command = new Command().okCmd(userLogin,"Directory prepared");
        ctx.writeAndFlush(command);
        serverApp.getClients().put(ctx, userLogin);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {
        switch (command.getType()){
            case LS:{
                System.out.println("LS command");
                ArrayList<String> fileList = getFileList();
                ctx.writeAndFlush(new Command().sendFileList(fileList));
                break;
            }
            case SEND: {
                System.out.println("Получена команда Send");
                SendFileCmdData sendFileCmdData = (SendFileCmdData) command.getData();
                filename = sendFileCmdData.getFileName();
                filesize = sendFileCmdData.getFileSize();
                file = new File(userDir + "/" + userLogin);
                if (file.exists()) {
                    ctx.writeAndFlush(new Command().errCmd("File with this filename already exists"));
                } else {
                    ctx.writeAndFlush(new Command().getFile(filename));
                }
                break;
            }
            case GET: {
                System.out.println("Получена команда Get");
                GetFileCmdData getFileCmdData = (GetFileCmdData) command.getData();
                filename = getFileCmdData.getFileName();
                File serverFile = new File(userDir + "/"+filename);
                if (serverFile.exists()&&serverFile.isFile()) {
                    filesize = serverFile.length();
                    Command commandFile = new Command().sendFile(filename, filesize);
                    ctx.writeAndFlush(commandFile);
                    try (InputStream fis = new FileInputStream(serverFile)) {
                        int pointer = 0;
                        while(filesize>buffer.length){
                            pointer=fis.read(buffer);
                            Command cmdSendFileToClient = new Command().file(buffer,pointer);
                            filesize-=pointer;
                            ctx.writeAndFlush(cmdSendFileToClient);
                        }
                        byte[] bufferLast = new byte[Math.toIntExact(filesize)];
                        pointer=fis.read(bufferLast);
                        ctx.writeAndFlush(new Command().file(bufferLast,pointer));
                    }
                }

                else {
                    ctx.writeAndFlush(new Command().errCmd("File doesn't exists"));
                }
                break;
            }
        }
    }

    public ArrayList<String> getFileList(){
        File dir = new File(userDir);
        File[] files = dir.listFiles();
        ArrayList<String> fileList = new ArrayList<>();
        if(files != null){
            for (File file : files) {
                StringBuilder sb = new StringBuilder();
                sb.append(file.getName()).append(" ");
                if(file.isDirectory()){
                    sb.append("/\n");
                }else sb.append(file.length()).append(" bytes.\n");
                fileList.add(sb.toString());
                sb.setLength(0);
            }
        }
        return fileList;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnect!");
        serverApp.getClients().remove(ctx);
    }
}
