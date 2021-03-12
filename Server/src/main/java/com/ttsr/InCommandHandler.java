package com.ttsr;

import com.ttsr.commands.*;
import com.ttsr.utils.UtilMethods;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.*;
import java.util.ArrayList;

public class InCommandHandler extends SimpleChannelInboundHandler<Command> {

    private final ServerApp serverApp;
    private String userLogin;
    private String userDir = "Server"+File.separator+"src"+File.separator+"UsersData";
    private byte[] buffer = new byte[32768];
    private String filename;
    private Long filesize;
    private File file;
    private Long bytesSum = 0L;
    private double lastProgress = 0;

    public InCommandHandler(ServerApp serverApp, String userLogin) {
        this.serverApp = serverApp;
        this.userLogin = userLogin;
        userDir = userDir+File.separator+userLogin;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(userLogin + " connected!");
        File file = new File(userDir);
        if(!file.exists())
        {
            new File(userDir).mkdir();
        }
        Command command = new Command().okCmd("Cloud directory prepared");
        ctx.writeAndFlush(command);
        serverApp.getClients().put(ctx, userLogin);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command command) throws Exception {

        switch (command.getType()){
            case LS:{
                System.out.println("LS command");
                ArrayList<String> fileList = UtilMethods.getFileList(new File(userDir));
                ctx.writeAndFlush(new Command().sendFileList(fileList, userDir));
                break;
            }
            case SEND: {
                System.out.println("Send command");
                SendFileCmdData sendFileCmdData = (SendFileCmdData) command.getData();
                filename = sendFileCmdData.getFileName();
                System.out.println(filename);
                filesize = sendFileCmdData.getFileSize();
                file = new File(userDir + File.separator + filename);
                if (file.exists()) {
                    ctx.writeAndFlush(new Command().errCmd("File with this filename already exists"));
                } else {
                    ctx.writeAndFlush(new Command().getFile(filename));
                }
                break;
            }
            case GET: {
                System.out.println("Get command");
                GetFileCmdData getFileCmdData = (GetFileCmdData) command.getData();
                filename = getFileCmdData.getFileName();
                File serverFile = new File(userDir +File.separator+filename);
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
            case FILE: {
                int pointer = 0;
                try {
                    try (FileOutputStream fos = new FileOutputStream(file, true)) {
                        if (filesize > buffer.length) {
                            FileData file = (FileData) command.getData();
                            pointer = file.getPointer();
                            buffer = file.getBuffer();
                            fos.write(buffer, 0, pointer);
                        } else {
                            byte[] lastBytes;
                            FileData file = (FileData) command.getData();
                            pointer = file.getPointer();
                            lastBytes = file.getBuffer();
                            fos.write(lastBytes, 0, pointer);
                        }
                    }
                    bytesSum += pointer;
                    double progress = (double)bytesSum / (double)filesize;
                    progress = UtilMethods.round(progress,2);
                    if(progress > lastProgress)
                    {
                        lastProgress = progress;
                        ctx.writeAndFlush(new Command().sendProgress(progress));
                    }
                    if(bytesSum.equals(filesize)) {
                        bytesSum = 0L;
                        lastProgress = 0;
                        ctx.writeAndFlush(new Command().sendFileList(UtilMethods.getFileList(new File(userDir)),userDir));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case MK_DIR:{
                System.out.println("Make directory command");
                MakeDirCmdData makeDirCmdData = (MakeDirCmdData) command.getData();
                String dirName = makeDirCmdData.getDirName();
                String dirPath = userDir+"/"+dirName;
                File file = new File(dirPath);
                if(!file.exists()||(file.exists()&&!file.isDirectory()))
                {
                    new File(dirPath).mkdir();
                    Command commandToClient = new Command().okCmd(" Directory created: "+ dirName);
                    ctx.writeAndFlush(commandToClient);
                }
                else {
                    Command commandToClient = new Command().errCmd("Directory already exists!");
                    ctx.writeAndFlush(commandToClient);
                }
            }

            case ERROR:
                System.out.println("error message");
                ErrCmdData errCmdData = (ErrCmdData) command.getData();
                System.out.println(errCmdData.getErrMsg());
                break;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(userLogin+" disconnected!");
        serverApp.getClients().remove(ctx);
    }
}
