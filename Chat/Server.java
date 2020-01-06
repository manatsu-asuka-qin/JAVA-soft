package Chat;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;
import java.util.Base64;

import Chat.Model.User;
import Chat.GUI.*;



public class Server extends ServerGUI{
    private static String winRecievePath = "C:\\Users\\katom\\eclipse-workspace\\recievefile\\";
    private static String linuxRecieveRath = "/home/tie/文档/recieved";
    //保存接收的文件目录
    private ServerSocket serverSocket;//socket连接

    private boolean isStart = false;//是否允许
    private int usrNum;//允许客户端人数

    private ConcurrentHashMap<String,ClientServiceThread> clientServiceThreadsMap;
    //当前连接的客户线程与客户名对应的HashMap
    private ServerThread serverThread;//服务线程

    private Server(){
        startBt.addActionListener(e -> {//开始按钮监听
            if(!isStart){
                startServer();//开始
            }
        });

        stopBt.addActionListener(e -> {//关闭按钮监听
            if (isStart) {
                stopServer();//停止服务
            }
        });

        sendBt.addActionListener(e -> sendAll());

        serverSendArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    sendAll();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {//关闭窗口按钮监听
                if (isStart) {
                    stopServer();//停止服务
                }
                System.exit(0);
            }
        });

        mItemChooseSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                JOptionPane op = new JOptionPane();
                String[] str={"Windows","Linux"};
                WinOrLinux=String.valueOf(op.showInputDialog(panel,"请选择运行的系统","选择系统",1,null,str,str[0]));
            }
        });

    }

    //开始通信服务
    private void startServer(){
        int port;//获取连接参数
        try {
            port = Integer.parseInt(portTxfield.getText().trim());//获取端口
        }catch (NumberFormatException e){
            showMsg_Error("端口号必须为整数！");
            return;
        }
        if(port < 1024 || port >65535){
            showMsg_Error("端口号必须在1024～65535之间");
            return;
        }

        try {
            usrNum = Integer.parseInt(allowusrTxfield.getText().trim());//获取设定允许人数
        }catch (NumberFormatException e){
            showMsg_Error("人数上限必须是正整数！");
            usrNum = 0;
            return;
        }
        if (usrNum <= 0){
            showMsg_Error("人数上限必须是正整数！");
            usrNum = 0;
            return;
        }
        //
        try{
            clientServiceThreadsMap = new ConcurrentHashMap<>();//初始化连接的客户端线程与用户名的哈希表
            serverSocket = new ServerSocket(port);//开始监听端口
            serverThread = new ServerThread();
            serverThread.start();//开始服务
            isStart = true;//设置已启动
        }catch (BindException e){
            isStart = false;
            showMsg_Error("启动服务器失败：端口已经被使用！");
            return;
        }
        catch (Exception e){
            isStart = false;
            showMsg_Error("启动服务器失败：启动异常！");
            e.printStackTrace();
            return;
        }
    }

    //关闭服务
    private synchronized void stopServer(){
        try {
            serverThread.closeThread();
            for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
                ClientServiceThread clientServiceThread = entry.getValue();
                clientServiceThread.sendMsg("CLOSE");//对每个用户发送关闭指令
                clientServiceThread.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            showMsg_Error("关闭服务器出现异常！ ");
            isStart = true;
            setUISetting(true);
        }
    }

    //发送消息
    private void sendAll(){
        if(!isStart){
            showMsg_Error("服务器还未启动，不能发送消息");
            return;
        }

        if (clientServiceThreadsMap.size() == 0){
            showMsg_Error("没有用户在线，请等待用户登录");
            return;
        }

        String msg = serverSendArea.getText().trim();
        if (msg.equals("")) {
            showMsg_Error("发送消息不能为空！");
            return;
        }
        LogMsg("Server: " + msg);
        msg = Base64.getEncoder().encodeToString(msg.getBytes()).trim();//BASE64加密
        for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
            entry.getValue().sendMsg("MSG@ALL@SERVER@" + msg);//给用户表中每个用户发消息
        }
        serverSendArea.setText(null);//清空发送区域
    }

    //显示错误信息
    private void showMsg_Error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    //日志消息
    private void LogMsg(String msg){
        logTxArea.append(msg + "\r\n");
    }

    private class ServerThread extends Thread{//服务线程
        private boolean isRunning;//运行标志符

        ServerThread(){
            this.isRunning = true;
        }//初始化设定表示

        public void run(){//开始服务函数
            while (this.isRunning){
                try {
                    if(!serverSocket.isClosed()){
                        Socket socket = serverSocket.accept();//监听端口
                        if (clientServiceThreadsMap.size() == usrNum){//满人
                            PrintWriter msgOutWriter =new PrintWriter(socket.getOutputStream());
                            msgOutWriter.println("LOGIN@FAIL@对不起，服务器在线人数已达到上限，请稍候尝试！");
                            msgOutWriter.flush();
                            msgOutWriter.close();
                            socket.close();
                        }
                        else {//新用户连接进来
                            ClientServiceThread clientServiceThread = new ClientServiceThread(socket);
                            User usr = clientServiceThread.getUser();
                            clientServiceThreadsMap.put(usr.info(),clientServiceThread);//加入用户线程的HashMap
                            listModel.addElement(usr.getName());
                            LogMsg(usr.info() + "已经成功连接系统...");
                            clientServiceThread.start();//开启用户服务线程
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        synchronized void closeThread() throws IOException {//关闭线程
            this.isRunning = false;
            serverSocket.close();
            System.out.println("serverSocket close!!!");
        }
    }

    private class ClientServiceThread extends Thread{//用户服务的线程
        private Socket socket;
        private User usr;
        private BufferedReader infoRecieve;//输入流
        private PrintWriter infoSend;//输出流
        private boolean isRunning;//运行标志

        //初始化
        private synchronized boolean init(){
            try{
                infoRecieve = new BufferedReader(new InputStreamReader(socket.getInputStream()));//获取出入流
                infoSend = new PrintWriter(socket.getOutputStream());

                String info = infoRecieve.readLine();//读取信息
                StringTokenizer tokenizer_at = new StringTokenizer(info,"@");//@分割消息
                String type = tokenizer_at.nextToken();
                if (!type.equals("LOGIN")){//不是登录的指令，返回客户端错误信息
                    sendMsg("ERROR@MESSAGE_TYPE");
                    return false;
                }
                usr = new User(tokenizer_at.nextToken());
                sendMsg("LOGIN@SUCCESS@" + usr.info() + "与服务器连接成功！");//返回客户端登录成功的消息

                int usrNum = clientServiceThreadsMap.size();//获取已登录的用户数量
                if (usrNum>0){
                    StringBuilder builder = new StringBuilder();
                    builder.append("@");
                    //map记录了每个连接上的用户信息，然后新用户连接后把map里面的内容发给新用户，在新用户本地生成在线列表
                    for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
                        ClientServiceThread serviceThread = entry.getValue();//获取已登录的用户服务线程
                        builder.append(serviceThread.getUser().info()).append("@");//发送更新在线用户的指令
                    }
                    //StringBuilder类内容转化成内容加上控制信息发送给客户端
                    sendMsg("USER@LIST@" + usrNum + builder.toString());
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        //服务线程类
        ClientServiceThread(Socket socket){
            this.socket = socket;
            this.isRunning = init();
            if (!this.isRunning){
                LogMsg("服务线程开启失败！");
            }
        }

        //开启服务
        public void run(){
            while (isRunning){
                try {
                    String message = infoRecieve.readLine();//读取指令
                    if (message.equals("LOGOUT")) {//登出的指令
                        LogMsg(usr.info() + "下线...");
                        int clientNum = clientServiceThreadsMap.size();
                        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreadsMap.entrySet()) {
                            entry.getValue().sendMsg("USER@DELETE@" + usr.info());//在线用户删除登出用户
                        }
                        listModel.removeElement(usr.getName());
                        clientServiceThreadsMap.remove(usr.info());
                        close();
                        return;
                    }
                    else if (message.startsWith("UPLOAD")) {//上传文件指令
                        ReceiveFileThread rf = new ReceiveFileThread();//上传文件线程
                        rf.start();
                    }
                else {//简单的消息
                        dispatchMsg(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        //关闭
        void close() throws IOException {
            this.isRunning = false;
            this.infoSend.close();
            this.infoRecieve.close();
            this.socket.close();
        }

        //发送信息函数
        void dispatchMsg(String msg){
            StringTokenizer tokenizer_at = new StringTokenizer(msg,"@");//@分割
            String type = tokenizer_at.nextToken();//获取指令类型
            if(!type.equals("MSG")){
                sendMsg("ERROR@MESSAGE_TYPE");
                return;
            }
            String toWho = tokenizer_at.nextToken();
            String fromWho = tokenizer_at.nextToken();
            String content = tokenizer_at.nextToken();
            LogMsg(msg);
            LogMsg(fromWho + "->" + toWho + ": " + content);
            if (toWho.equals("ALL")){//群发
                for(Map.Entry<String,ClientServiceThread> entry: clientServiceThreadsMap.entrySet())
                {
                    entry.getValue().sendMsg(msg);//每个用户线程都发
                }
            }
            else {//单发
                if (clientServiceThreadsMap.containsKey(toWho)){
                    clientServiceThreadsMap.get(toWho).sendMsg(msg);//获取接收方的线程后发送
                }
                else{
                    sendMsg("ERROR@INVALID_USER");
                }
            }
        }

        //发送message
        void sendMsg(String message) {
            infoSend.println(message);
            infoSend.flush();
        }

        User getUser() {
            return usr;
        }//获取用户
    }

    //获取windows下保存路径
    public String getRecievePath(){
        if (WinOrLinux.equals("Windows"))
            {
                return winRecievePath;
            }
        else {
            return linuxRecieveRath;
        }
    }

    private class ReceiveFileThread extends Thread{
        private ServerSocket revFileSocket = null;
        private Socket socket = null;

        ReceiveFileThread(){
            try{
                revFileSocket = new ServerSocket(8888);
                socket = revFileSocket.accept();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("开始传输初始化");
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dis.readUTF();
                String filename = dis.readUTF();
                dos.writeUTF("ok");
                dos.flush();
                String frontPath = getRecievePath();
                File file = new File(frontPath + filename );

                RandomAccessFile recieveFileStream = new RandomAccessFile(frontPath + filename ,"rw");

                long size = 0;
                if (file.exists() && file.isFile()) {
                    size = file.length();
                }

                dos.writeLong(size);
                dos.flush();
                long allSize = dis.readLong();
                String rsp = dis.readUTF();

                int barSize = (int) (allSize / 1024);
                int barOffset = (int) (size / 1024);
                if (rsp.equals("ok")){
                    recieveFileStream.seek(size);
                    int length;
                    byte[] buf_downfile = new byte[1024];
                    while ((length = dis.read(buf_downfile,0,buf_downfile.length) )!=-1){
                        recieveFileStream.write(buf_downfile,0,length);
                    }
                    System.out.println("end");
                }
                dis.close();
                dos.close();
                recieveFileStream.close();
                if (barOffset >= barSize) {
                    file.renameTo(new File(frontPath + filename));
                }
            }catch (IOException e){
                showMsg_Error("已取消接收，连接关闭！");
            }finally {

                try{
                    this.socket.close();
                    this.revFileSocket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }


            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}

