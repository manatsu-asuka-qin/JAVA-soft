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
    private static String linuxRecieveRath = "/home/tie/�ĵ�/recieved";
    //������յ��ļ�Ŀ¼
    private ServerSocket serverSocket;//socket����

    private boolean isStart = false;//�Ƿ�����
    private int usrNum;//����ͻ�������

    private ConcurrentHashMap<String,ClientServiceThread> clientServiceThreadsMap;
    //��ǰ���ӵĿͻ��߳���ͻ�����Ӧ��HashMap
    private ServerThread serverThread;//�����߳�

    private Server(){
        startBt.addActionListener(e -> {//��ʼ��ť����
            if(!isStart){
                startServer();//��ʼ
            }
        });

        stopBt.addActionListener(e -> {//�رհ�ť����
            if (isStart) {
                stopServer();//ֹͣ����
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
            public void windowClosing(WindowEvent e) {//�رմ��ڰ�ť����
                if (isStart) {
                    stopServer();//ֹͣ����
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
                WinOrLinux=String.valueOf(op.showInputDialog(panel,"��ѡ�����е�ϵͳ","ѡ��ϵͳ",1,null,str,str[0]));
            }
        });

    }

    //��ʼͨ�ŷ���
    private void startServer(){
        int port;//��ȡ���Ӳ���
        try {
            port = Integer.parseInt(portTxfield.getText().trim());//��ȡ�˿�
        }catch (NumberFormatException e){
            showMsg_Error("�˿ںű���Ϊ������");
            return;
        }
        if(port < 1024 || port >65535){
            showMsg_Error("�˿ںű�����1024��65535֮��");
            return;
        }

        try {
            usrNum = Integer.parseInt(allowusrTxfield.getText().trim());//��ȡ�趨��������
        }catch (NumberFormatException e){
            showMsg_Error("�������ޱ�������������");
            usrNum = 0;
            return;
        }
        if (usrNum <= 0){
            showMsg_Error("�������ޱ�������������");
            usrNum = 0;
            return;
        }
        //
        try{
            clientServiceThreadsMap = new ConcurrentHashMap<>();//��ʼ�����ӵĿͻ����߳����û����Ĺ�ϣ��
            serverSocket = new ServerSocket(port);//��ʼ�����˿�
            serverThread = new ServerThread();
            serverThread.start();//��ʼ����
            isStart = true;//����������
        }catch (BindException e){
            isStart = false;
            showMsg_Error("����������ʧ�ܣ��˿��Ѿ���ʹ�ã�");
            return;
        }
        catch (Exception e){
            isStart = false;
            showMsg_Error("����������ʧ�ܣ������쳣��");
            e.printStackTrace();
            return;
        }
    }

    //�رշ���
    private synchronized void stopServer(){
        try {
            serverThread.closeThread();
            for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
                ClientServiceThread clientServiceThread = entry.getValue();
                clientServiceThread.sendMsg("CLOSE");//��ÿ���û����͹ر�ָ��
                clientServiceThread.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            showMsg_Error("�رշ����������쳣�� ");
            isStart = true;
            setUISetting(true);
        }
    }

    //������Ϣ
    private void sendAll(){
        if(!isStart){
            showMsg_Error("��������δ���������ܷ�����Ϣ");
            return;
        }

        if (clientServiceThreadsMap.size() == 0){
            showMsg_Error("û���û����ߣ���ȴ��û���¼");
            return;
        }

        String msg = serverSendArea.getText().trim();
        if (msg.equals("")) {
            showMsg_Error("������Ϣ����Ϊ�գ�");
            return;
        }
        LogMsg("Server: " + msg);
        msg = Base64.getEncoder().encodeToString(msg.getBytes()).trim();//BASE64����
        for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
            entry.getValue().sendMsg("MSG@ALL@SERVER@" + msg);//���û�����ÿ���û�����Ϣ
        }
        serverSendArea.setText(null);//��շ�������
    }

    //��ʾ������Ϣ
    private void showMsg_Error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    //��־��Ϣ
    private void LogMsg(String msg){
        logTxArea.append(msg + "\r\n");
    }

    private class ServerThread extends Thread{//�����߳�
        private boolean isRunning;//���б�־��

        ServerThread(){
            this.isRunning = true;
        }//��ʼ���趨��ʾ

        public void run(){//��ʼ������
            while (this.isRunning){
                try {
                    if(!serverSocket.isClosed()){
                        Socket socket = serverSocket.accept();//�����˿�
                        if (clientServiceThreadsMap.size() == usrNum){//����
                            PrintWriter msgOutWriter =new PrintWriter(socket.getOutputStream());
                            msgOutWriter.println("LOGIN@FAIL@�Բ��𣬷��������������Ѵﵽ���ޣ����Ժ��ԣ�");
                            msgOutWriter.flush();
                            msgOutWriter.close();
                            socket.close();
                        }
                        else {//���û����ӽ���
                            ClientServiceThread clientServiceThread = new ClientServiceThread(socket);
                            User usr = clientServiceThread.getUser();
                            clientServiceThreadsMap.put(usr.info(),clientServiceThread);//�����û��̵߳�HashMap
                            listModel.addElement(usr.getName());
                            LogMsg(usr.info() + "�Ѿ��ɹ�����ϵͳ...");
                            clientServiceThread.start();//�����û������߳�
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }

        synchronized void closeThread() throws IOException {//�ر��߳�
            this.isRunning = false;
            serverSocket.close();
            System.out.println("serverSocket close!!!");
        }
    }

    private class ClientServiceThread extends Thread{//�û�������߳�
        private Socket socket;
        private User usr;
        private BufferedReader infoRecieve;//������
        private PrintWriter infoSend;//�����
        private boolean isRunning;//���б�־

        //��ʼ��
        private synchronized boolean init(){
            try{
                infoRecieve = new BufferedReader(new InputStreamReader(socket.getInputStream()));//��ȡ������
                infoSend = new PrintWriter(socket.getOutputStream());

                String info = infoRecieve.readLine();//��ȡ��Ϣ
                StringTokenizer tokenizer_at = new StringTokenizer(info,"@");//@�ָ���Ϣ
                String type = tokenizer_at.nextToken();
                if (!type.equals("LOGIN")){//���ǵ�¼��ָ����ؿͻ��˴�����Ϣ
                    sendMsg("ERROR@MESSAGE_TYPE");
                    return false;
                }
                usr = new User(tokenizer_at.nextToken());
                sendMsg("LOGIN@SUCCESS@" + usr.info() + "����������ӳɹ���");//���ؿͻ��˵�¼�ɹ�����Ϣ

                int usrNum = clientServiceThreadsMap.size();//��ȡ�ѵ�¼���û�����
                if (usrNum>0){
                    StringBuilder builder = new StringBuilder();
                    builder.append("@");
                    //map��¼��ÿ�������ϵ��û���Ϣ��Ȼ�����û����Ӻ��map��������ݷ������û��������û��������������б�
                    for (Map.Entry<String,ClientServiceThread> entry : clientServiceThreadsMap.entrySet()){
                        ClientServiceThread serviceThread = entry.getValue();//��ȡ�ѵ�¼���û������߳�
                        builder.append(serviceThread.getUser().info()).append("@");//���͸��������û���ָ��
                    }
                    //StringBuilder������ת�������ݼ��Ͽ�����Ϣ���͸��ͻ���
                    sendMsg("USER@LIST@" + usrNum + builder.toString());
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        //�����߳���
        ClientServiceThread(Socket socket){
            this.socket = socket;
            this.isRunning = init();
            if (!this.isRunning){
                LogMsg("�����߳̿���ʧ�ܣ�");
            }
        }

        //��������
        public void run(){
            while (isRunning){
                try {
                    String message = infoRecieve.readLine();//��ȡָ��
                    if (message.equals("LOGOUT")) {//�ǳ���ָ��
                        LogMsg(usr.info() + "����...");
                        int clientNum = clientServiceThreadsMap.size();
                        for (Map.Entry<String, ClientServiceThread> entry : clientServiceThreadsMap.entrySet()) {
                            entry.getValue().sendMsg("USER@DELETE@" + usr.info());//�����û�ɾ���ǳ��û�
                        }
                        listModel.removeElement(usr.getName());
                        clientServiceThreadsMap.remove(usr.info());
                        close();
                        return;
                    }
                    else if (message.startsWith("UPLOAD")) {//�ϴ��ļ�ָ��
                        ReceiveFileThread rf = new ReceiveFileThread();//�ϴ��ļ��߳�
                        rf.start();
                    }
                else {//�򵥵���Ϣ
                        dispatchMsg(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        //�ر�
        void close() throws IOException {
            this.isRunning = false;
            this.infoSend.close();
            this.infoRecieve.close();
            this.socket.close();
        }

        //������Ϣ����
        void dispatchMsg(String msg){
            StringTokenizer tokenizer_at = new StringTokenizer(msg,"@");//@�ָ�
            String type = tokenizer_at.nextToken();//��ȡָ������
            if(!type.equals("MSG")){
                sendMsg("ERROR@MESSAGE_TYPE");
                return;
            }
            String toWho = tokenizer_at.nextToken();
            String fromWho = tokenizer_at.nextToken();
            String content = tokenizer_at.nextToken();
            LogMsg(msg);
            LogMsg(fromWho + "->" + toWho + ": " + content);
            if (toWho.equals("ALL")){//Ⱥ��
                for(Map.Entry<String,ClientServiceThread> entry: clientServiceThreadsMap.entrySet())
                {
                    entry.getValue().sendMsg(msg);//ÿ���û��̶߳���
                }
            }
            else {//����
                if (clientServiceThreadsMap.containsKey(toWho)){
                    clientServiceThreadsMap.get(toWho).sendMsg(msg);//��ȡ���շ����̺߳���
                }
                else{
                    sendMsg("ERROR@INVALID_USER");
                }
            }
        }

        //����message
        void sendMsg(String message) {
            infoSend.println(message);
            infoSend.flush();
        }

        User getUser() {
            return usr;
        }//��ȡ�û�
    }

    //��ȡwindows�±���·��
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
                System.out.println("��ʼ�����ʼ��");
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
                showMsg_Error("��ȡ�����գ����ӹرգ�");
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

