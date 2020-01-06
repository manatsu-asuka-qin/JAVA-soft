package Chat;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.*;

import Chat.Model.User;
import Chat.GUI.*;

public class Client extends ClientGUI{
    private Socket socket;//socket连接
    private PrintWriter infoSend;//发送流
    private BufferedReader infoReciev;//接收流

    private User myUser;//本用户
    private ConcurrentHashMap<String,User> onlineusr = new ConcurrentHashMap<>();//用户名与用户的哈希表
    private String sendTo = "ALL";//发送目标

    private MsgThread msgThread;//消息线程

    private boolean isConnected;//是否连接，设置ui的标识

    private Client(){//运行函数
        msgSendArea.addKeyListener(new KeyListener() {
            //设置文本区域可以回车发送
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    send();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        sendBt.addActionListener(e ->send());//按钮发送

        connectBt.addActionListener(e -> {//监听连接按钮
            if (!isConnected){
                frame.setTitle(nameTxfield.getText());
                //建立新线程
                new Thread(()->{
                    LogMsg("正在连接");
                    connectBt.setEnabled(false);
                    connect();//开始连接
                }).start();
            }
        });

        disconnectBt.addActionListener(e -> {//监听断开按钮
            if (isConnected){
                disconnect();//关闭连接
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            //监听窗口的关闭按钮
            @Override
            public void windowClosing(WindowEvent e) {
                if(isConnected){//如果客户端连接，断开连接
                    disconnect();//关闭
                }
                System.exit(0);
            }
        });

        mItemopenfile.addActionListener(e -> {//用选择文件的控件，返回文件路径
            sendMsg("UPLOAD@" + myUser.info());//发送上传文件的指令
            SendFileThread sendFileThread = new SendFileThread();//开启上传文件的进程
            sendFileThread.start();
                });

        userlist.addListSelectionListener(e -> {//选择用户时
            int selectedIndex = userlist.getSelectedIndex();//获取选择的index
            if(selectedIndex <0)//没有选择，默认值：ALL
                return;
            if(selectedIndex == 0){//选择所有人
                sendTo = "ALL";
                msgSendLabel.setText("To：所有人");
            }
            else {//选择了某个人
                String name = listModel.getElementAt(selectedIndex);
                if (onlineusr.containsKey(name)){//该用户存在
                    sendTo = onlineusr.get(name).info();//将发送目标设置为选择的用户的信息
                    msgSendLabel.setText("发送给：" + name);//改变发送目标的标签
                }
                else {//不存在
                    sendTo = "ALL";
                    msgSendLabel.setText("发送给：所有人");
                }
            }

        });

    }

    private void connect(){
        int port;//端口号
        try {
            port = Integer.parseInt(portTxfield.getText().trim());//判断是否整数
        }catch (NumberFormatException e){
            showMsg_Error("端口号必须是整数！");
            return;
        }

        String name = nameTxfield.getText().trim();//用户名
        if (name.equals("")){//为空
            showMsg_Error("名字不能为空!");
            return;
        }
        String ip = ipTxfield.getText().trim();//ip地址
        if (ip.equals("")){//为空
            showMsg_Error("ip地址不能为空!");
            return;
        }

        try {
            listModel.addElement("所有人");
            myUser = new User(name,ip);//
            socket = new Socket(ip,port);//建立tcp连接
            //获取服务器发送来的输入流
            infoReciev = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            infoSend = new PrintWriter(socket.getOutputStream());

            String myIP = socket.getInetAddress().toString().substring(1);
            sendMsg("LOGIN@"+name+"%"+myIP);//发送登录指令

            msgThread = new MsgThread();//开始消息线程
            msgThread.start();
            isConnected = true;
        }catch (Exception e){
            isConnected = false;
            LogMsg("连接服务端失败");
            connectBt.setEnabled(true);
            listModel.removeAllElements();
            e.printStackTrace();
            return;
        }
        LogMsg("登录成功");
        setUISetting(isConnected);
    }

    private synchronized void disconnect(){
        try {
            sendMsg("LOGOUT");//发出登出指令
            msgThread.close();
            onlineusr.clear();
            listModel.clear();
            infoSend.close();
            infoReciev.close();
            socket.close();
            isConnected = false;
            setUISetting(false);
            sendTo = "ALL";
            msgSendLabel.setText("To:所有人");
            LogMsg("已经断开连接");

        }catch (Exception e){
            e.printStackTrace();
            isConnected = true;
            setUISetting(true);
            showMsg_Error("服务器断开失败");
        }
    }

    private void send(){//发送消息函数
        if (!isConnected){
            showMsg_Error("还未连接服务器");
            return;
        }
        String msg = msgSendArea.getText().trim();//获取发送的消息
        if(msg.equals("")){//无消息
            showMsg_Error("消息不能为空");
            return;
        }
        String recvPeople = sendTo;//发送目标获取
        try {
            LogMsg(myUser.getName()+"->"+recvPeople+"\r\n"+msg);
            msg = Base64.getEncoder().encodeToString(msg.getBytes()).trim();//base64加密
            //发送内容为“消息类别@接收者@发送者信息@发送内容
            sendMsg("MSG@" + sendTo + "@" + myUser.info() + "@" + msg);

        }catch (Exception e){
            e.printStackTrace();
            LogMsg("（发送失败）"+myUser.getName()+"->"+recvPeople+"\r\n"+msg);
        }
        msgSendArea.setText(null);//清空输入区
    }



    //显示错误信息
    private void showMsg_Error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //日志消息，显示在对话区域
    private void LogMsg(String msg) {
        msgReciveArea.append(msg + "\r\n");
    }

    //发送消息的函数
    private void sendMsg(String message) {
        infoSend.println(message);
        infoSend.flush();
    }

    private class MsgThread extends Thread{
        private boolean isRuning;//运行标志
        //初始化，运行标志设为true
        MsgThread(){
            isRuning = true;
        }
        public void run(){//运行
            while (isRuning){
                try{
                    String mesg = infoReciev.readLine();
                    StringTokenizer tokenizer_at = new StringTokenizer(mesg,"@");//@分割信息
                    String command = tokenizer_at.nextToken();

                    switch (command){
                        case "USER"://用户指令
                            String type = tokenizer_at.nextToken();
                            switch (type){
                                case "ADD": {//添加用户
                                    String usrDescription = tokenizer_at.nextToken();
                                    User newUsr = new User(usrDescription);//新建用户
                                    onlineusr.put(newUsr.getName(),newUsr);//在线列表添加
                                    listModel.addElement(newUsr.getName());
                                    LogMsg("新用户(" + newUsr.info()+")上线");//登录成功后信息
                                    break;
                                }
                                case "DELETE":{//删除用户
                                    String usrinfo = tokenizer_at.nextToken();
                                    User delUsr = new User(usrinfo);//创建删除的用户信息
                                    onlineusr.remove(delUsr.getName());//删除在线列表中的信息
                                    listModel.removeElement(delUsr.getName());
                                    LogMsg("用户（" + delUsr.info() + "）下线！");//显示登出信息
                                    if (sendTo.equals(delUsr.getName())){
                                        sendTo = "ALL";
                                        msgSendLabel.setText("To: 所有人");
                                    }break;
                                }
                                case "LIST"://列出在线用户
                                    int num = Integer.parseInt(tokenizer_at.nextToken());
                                    for (int i = 0;i < num; i++){
                                        String usrDescription = tokenizer_at.nextToken();
                                        User newUsr = new User(usrDescription);
                                        onlineusr.put(newUsr.getName(),newUsr);
                                        listModel.addElement(newUsr.getName());

                                        LogMsg("获取到用户（" + newUsr.info() + "）在线！");
                                    }break;
                            }break;
                        case "LOGIN"://服务器发送登录情况信息
                            String stat = tokenizer_at.nextToken();
                            if (stat.equals("SUCCESS")){//登录成功
                                LogMsg("登录成功！" + tokenizer_at.nextToken());
                            }
                            else if (stat.equals("FAIL")){//登陆失败
                                LogMsg("登录失败，断开连接！原因：" + tokenizer_at.nextToken());
                                disconnect();
                                isRuning = false;
                                return;
                            }
                            break;
                        case "ERROR"://服务器发送错误信息
                            String error = tokenizer_at.nextToken();
                            LogMsg("服务器返回错误，错误类型：" + error);
                            break;
                        case "CLOSE"://服务器发送关闭指令
                            LogMsg("服务器已关闭，正在断开连接...");
                            disconnect();
                            isRuning = false;
                            return;
                        case "MSG"://消息类格式为（MSG@to@from@内容）
                            StringBuilder buffer = new StringBuilder();
                            String toWho = tokenizer_at.nextToken();//信息接收者
                            String fromUsr = tokenizer_at.nextToken();//发送者
                            String content = tokenizer_at.nextToken();//消息

                            try {
                                content = new String(Base64.getDecoder().decode(content.getBytes("utf-8")));//BASE64解码
                            } catch (Exception w) {
                                w.printStackTrace();
                            }

                            buffer.append(fromUsr);
                            if (toWho.equals("ALL")){//群发的消息
                                buffer.append("（群发）");
                            }
                            buffer.append(": ").append(content);
                            LogMsg(buffer.toString());
                            break;
                    }

                }catch (IOException e){
                    e.printStackTrace();
                    LogMsg("接收消息异常！");
                }
            }
        }
        void close(){
            isRuning = false;
        }
    }

    private class SendFileThread extends Thread{
        private Socket socket = null;
        private DataOutputStream dos;
        private DataInputStream dis;
        private RandomAccessFile sandFileStream;
        private JLabel label = new JLabel();
        private JFrame framec;

        SendFileThread(){
            String ip = ipTxfield.getText().trim();
            framec = new JFrame("文件传输");//创建选择文件组件的容器
            try {
                socket = new Socket(ip,8888);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            JFrame frame = new JFrame();

            upFileChooser = getFilePath();//window打开默认目录
            int i = upFileChooser.showOpenDialog(frame);
            if(i == upFileChooser.APPROVE_OPTION) {
                //点击确定后返回文件及名字
                File upFile = upFileChooser.getSelectedFile();//返回选择文件
                String upFilePath =upFileChooser.getSelectedFile().getPath();//返回选择文件的路径
                String upFilename = upFileChooser.getName();//要上传的文件

                try {
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    dos.writeUTF("ok");

                    sandFileStream = new RandomAccessFile(upFilePath,"r");
                    File file = new File(upFilePath);

                    byte[] buf_upfile = new byte[1024];
                    dos.writeUTF(file.getName());
                    dos.flush();
                    String rsp = dis.readUTF();

                    if (rsp.equals("ok")){
                        long size = dis.readLong();
                        dos.writeLong(sandFileStream.length());
                        dos.writeUTF("ok");
                        dos.flush();

                        int barSize = (int)(sandFileStream.length() / 1024);
                        int barOffset = (int)(size/1024);
                        //弹出窗口
                        framec.setSize(380,120);
                        Container contentPanel = framec.getContentPane();
                        contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
                        JProgressBar progressbar = new JProgressBar();
                        //标签
                        label.setText(file.getName()+ "上传中");
                        contentPanel.add(label);
                        //进度条设置
                        progressbar.setOrientation(JProgressBar.HORIZONTAL);
                        progressbar.setMinimum(0);
                        progressbar.setMaximum(barSize);
                        progressbar.setValue(barOffset);
                        progressbar.setStringPainted(true);
                        progressbar.setPreferredSize(new Dimension(150, 20));
                        progressbar.setBorderPainted(true);
                        progressbar.setBackground(Color.pink);
                        //取消按钮
                        JButton cancelBt = new JButton("取消");
                        //面版容器
                        JPanel barPanel = new JPanel();
                        barPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

                        barPanel.add(progressbar);
                        barPanel.add(cancelBt);

                        contentPanel.add(barPanel);

                        cancelBt.addActionListener(new cancelActionListener());

                        framec.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        framec.setVisible(true);

                        int length;
                        if (size < sandFileStream.length()) {
                            sandFileStream.seek(size);
                            while ((length = sandFileStream.read(buf_upfile)) > 0) {
                                dos.write(buf_upfile, 0, length);
                                progressbar.setValue(++barOffset);
                                dos.flush();
                            }
                        }
                        label.setText(file.getName() + " 发送完成");
                    }
                    dis.close();
                    dos.close();
                    sandFileStream.close();
                }catch (IOException e1){
                    label.setText(" 取消发送,连接关闭");
                }finally {
                    framec.dispose();
                    try {
                        socket.close();//未使用this.socket
                    }catch (IOException e2){
                        e2.printStackTrace();
                        }
                    }
            }
        }

        class cancelActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e3) {
                try {
                    label.setText(" 取消发送,连接关闭");
                    JOptionPane.showMessageDialog(framec, "取消发送给，连接关闭!", "提示：", JOptionPane.INFORMATION_MESSAGE);
                    dis.close();
                    dos.close();
                    sandFileStream.close();
                    framec.dispose();
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        new Client();
    }

}
