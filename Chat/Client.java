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
    private Socket socket;//socket����
    private PrintWriter infoSend;//������
    private BufferedReader infoReciev;//������

    private User myUser;//���û�
    private ConcurrentHashMap<String,User> onlineusr = new ConcurrentHashMap<>();//�û������û��Ĺ�ϣ��
    private String sendTo = "ALL";//����Ŀ��

    private MsgThread msgThread;//��Ϣ�߳�

    private boolean isConnected;//�Ƿ����ӣ�����ui�ı�ʶ

    private Client(){//���к���
        msgSendArea.addKeyListener(new KeyListener() {
            //�����ı�������Իس�����
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
        sendBt.addActionListener(e ->send());//��ť����

        connectBt.addActionListener(e -> {//�������Ӱ�ť
            if (!isConnected){
                frame.setTitle(nameTxfield.getText());
                //�������߳�
                new Thread(()->{
                    LogMsg("��������");
                    connectBt.setEnabled(false);
                    connect();//��ʼ����
                }).start();
            }
        });

        disconnectBt.addActionListener(e -> {//�����Ͽ���ť
            if (isConnected){
                disconnect();//�ر�����
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            //�������ڵĹرհ�ť
            @Override
            public void windowClosing(WindowEvent e) {
                if(isConnected){//����ͻ������ӣ��Ͽ�����
                    disconnect();//�ر�
                }
                System.exit(0);
            }
        });

        mItemopenfile.addActionListener(e -> {//��ѡ���ļ��Ŀؼ��������ļ�·��
            sendMsg("UPLOAD@" + myUser.info());//�����ϴ��ļ���ָ��
            SendFileThread sendFileThread = new SendFileThread();//�����ϴ��ļ��Ľ���
            sendFileThread.start();
                });

        userlist.addListSelectionListener(e -> {//ѡ���û�ʱ
            int selectedIndex = userlist.getSelectedIndex();//��ȡѡ���index
            if(selectedIndex <0)//û��ѡ��Ĭ��ֵ��ALL
                return;
            if(selectedIndex == 0){//ѡ��������
                sendTo = "ALL";
                msgSendLabel.setText("To��������");
            }
            else {//ѡ����ĳ����
                String name = listModel.getElementAt(selectedIndex);
                if (onlineusr.containsKey(name)){//���û�����
                    sendTo = onlineusr.get(name).info();//������Ŀ������Ϊѡ����û�����Ϣ
                    msgSendLabel.setText("���͸���" + name);//�ı䷢��Ŀ��ı�ǩ
                }
                else {//������
                    sendTo = "ALL";
                    msgSendLabel.setText("���͸���������");
                }
            }

        });

    }

    private void connect(){
        int port;//�˿ں�
        try {
            port = Integer.parseInt(portTxfield.getText().trim());//�ж��Ƿ�����
        }catch (NumberFormatException e){
            showMsg_Error("�˿ںű�����������");
            return;
        }

        String name = nameTxfield.getText().trim();//�û���
        if (name.equals("")){//Ϊ��
            showMsg_Error("���ֲ���Ϊ��!");
            return;
        }
        String ip = ipTxfield.getText().trim();//ip��ַ
        if (ip.equals("")){//Ϊ��
            showMsg_Error("ip��ַ����Ϊ��!");
            return;
        }

        try {
            listModel.addElement("������");
            myUser = new User(name,ip);//
            socket = new Socket(ip,port);//����tcp����
            //��ȡ��������������������
            infoReciev = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            infoSend = new PrintWriter(socket.getOutputStream());

            String myIP = socket.getInetAddress().toString().substring(1);
            sendMsg("LOGIN@"+name+"%"+myIP);//���͵�¼ָ��

            msgThread = new MsgThread();//��ʼ��Ϣ�߳�
            msgThread.start();
            isConnected = true;
        }catch (Exception e){
            isConnected = false;
            LogMsg("���ӷ����ʧ��");
            connectBt.setEnabled(true);
            listModel.removeAllElements();
            e.printStackTrace();
            return;
        }
        LogMsg("��¼�ɹ�");
        setUISetting(isConnected);
    }

    private synchronized void disconnect(){
        try {
            sendMsg("LOGOUT");//�����ǳ�ָ��
            msgThread.close();
            onlineusr.clear();
            listModel.clear();
            infoSend.close();
            infoReciev.close();
            socket.close();
            isConnected = false;
            setUISetting(false);
            sendTo = "ALL";
            msgSendLabel.setText("To:������");
            LogMsg("�Ѿ��Ͽ�����");

        }catch (Exception e){
            e.printStackTrace();
            isConnected = true;
            setUISetting(true);
            showMsg_Error("�������Ͽ�ʧ��");
        }
    }

    private void send(){//������Ϣ����
        if (!isConnected){
            showMsg_Error("��δ���ӷ�����");
            return;
        }
        String msg = msgSendArea.getText().trim();//��ȡ���͵���Ϣ
        if(msg.equals("")){//����Ϣ
            showMsg_Error("��Ϣ����Ϊ��");
            return;
        }
        String recvPeople = sendTo;//����Ŀ���ȡ
        try {
            LogMsg(myUser.getName()+"->"+recvPeople+"\r\n"+msg);
            msg = Base64.getEncoder().encodeToString(msg.getBytes()).trim();//base64����
            //��������Ϊ����Ϣ���@������@��������Ϣ@��������
            sendMsg("MSG@" + sendTo + "@" + myUser.info() + "@" + msg);

        }catch (Exception e){
            e.printStackTrace();
            LogMsg("������ʧ�ܣ�"+myUser.getName()+"->"+recvPeople+"\r\n"+msg);
        }
        msgSendArea.setText(null);//���������
    }



    //��ʾ������Ϣ
    private void showMsg_Error(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    //��־��Ϣ����ʾ�ڶԻ�����
    private void LogMsg(String msg) {
        msgReciveArea.append(msg + "\r\n");
    }

    //������Ϣ�ĺ���
    private void sendMsg(String message) {
        infoSend.println(message);
        infoSend.flush();
    }

    private class MsgThread extends Thread{
        private boolean isRuning;//���б�־
        //��ʼ�������б�־��Ϊtrue
        MsgThread(){
            isRuning = true;
        }
        public void run(){//����
            while (isRuning){
                try{
                    String mesg = infoReciev.readLine();
                    StringTokenizer tokenizer_at = new StringTokenizer(mesg,"@");//@�ָ���Ϣ
                    String command = tokenizer_at.nextToken();

                    switch (command){
                        case "USER"://�û�ָ��
                            String type = tokenizer_at.nextToken();
                            switch (type){
                                case "ADD": {//����û�
                                    String usrDescription = tokenizer_at.nextToken();
                                    User newUsr = new User(usrDescription);//�½��û�
                                    onlineusr.put(newUsr.getName(),newUsr);//�����б����
                                    listModel.addElement(newUsr.getName());
                                    LogMsg("���û�(" + newUsr.info()+")����");//��¼�ɹ�����Ϣ
                                    break;
                                }
                                case "DELETE":{//ɾ���û�
                                    String usrinfo = tokenizer_at.nextToken();
                                    User delUsr = new User(usrinfo);//����ɾ�����û���Ϣ
                                    onlineusr.remove(delUsr.getName());//ɾ�������б��е���Ϣ
                                    listModel.removeElement(delUsr.getName());
                                    LogMsg("�û���" + delUsr.info() + "�����ߣ�");//��ʾ�ǳ���Ϣ
                                    if (sendTo.equals(delUsr.getName())){
                                        sendTo = "ALL";
                                        msgSendLabel.setText("To: ������");
                                    }break;
                                }
                                case "LIST"://�г������û�
                                    int num = Integer.parseInt(tokenizer_at.nextToken());
                                    for (int i = 0;i < num; i++){
                                        String usrDescription = tokenizer_at.nextToken();
                                        User newUsr = new User(usrDescription);
                                        onlineusr.put(newUsr.getName(),newUsr);
                                        listModel.addElement(newUsr.getName());

                                        LogMsg("��ȡ���û���" + newUsr.info() + "�����ߣ�");
                                    }break;
                            }break;
                        case "LOGIN"://���������͵�¼�����Ϣ
                            String stat = tokenizer_at.nextToken();
                            if (stat.equals("SUCCESS")){//��¼�ɹ�
                                LogMsg("��¼�ɹ���" + tokenizer_at.nextToken());
                            }
                            else if (stat.equals("FAIL")){//��½ʧ��
                                LogMsg("��¼ʧ�ܣ��Ͽ����ӣ�ԭ��" + tokenizer_at.nextToken());
                                disconnect();
                                isRuning = false;
                                return;
                            }
                            break;
                        case "ERROR"://���������ʹ�����Ϣ
                            String error = tokenizer_at.nextToken();
                            LogMsg("���������ش��󣬴������ͣ�" + error);
                            break;
                        case "CLOSE"://���������͹ر�ָ��
                            LogMsg("�������ѹرգ����ڶϿ�����...");
                            disconnect();
                            isRuning = false;
                            return;
                        case "MSG"://��Ϣ���ʽΪ��MSG@to@from@���ݣ�
                            StringBuilder buffer = new StringBuilder();
                            String toWho = tokenizer_at.nextToken();//��Ϣ������
                            String fromUsr = tokenizer_at.nextToken();//������
                            String content = tokenizer_at.nextToken();//��Ϣ

                            try {
                                content = new String(Base64.getDecoder().decode(content.getBytes("utf-8")));//BASE64����
                            } catch (Exception w) {
                                w.printStackTrace();
                            }

                            buffer.append(fromUsr);
                            if (toWho.equals("ALL")){//Ⱥ������Ϣ
                                buffer.append("��Ⱥ����");
                            }
                            buffer.append(": ").append(content);
                            LogMsg(buffer.toString());
                            break;
                    }

                }catch (IOException e){
                    e.printStackTrace();
                    LogMsg("������Ϣ�쳣��");
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
            framec = new JFrame("�ļ�����");//����ѡ���ļ����������
            try {
                socket = new Socket(ip,8888);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            JFrame frame = new JFrame();

            upFileChooser = getFilePath();//window��Ĭ��Ŀ¼
            int i = upFileChooser.showOpenDialog(frame);
            if(i == upFileChooser.APPROVE_OPTION) {
                //���ȷ���󷵻��ļ�������
                File upFile = upFileChooser.getSelectedFile();//����ѡ���ļ�
                String upFilePath =upFileChooser.getSelectedFile().getPath();//����ѡ���ļ���·��
                String upFilename = upFileChooser.getName();//Ҫ�ϴ����ļ�

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
                        //��������
                        framec.setSize(380,120);
                        Container contentPanel = framec.getContentPane();
                        contentPanel.setLayout(new BoxLayout(contentPanel,BoxLayout.Y_AXIS));
                        JProgressBar progressbar = new JProgressBar();
                        //��ǩ
                        label.setText(file.getName()+ "�ϴ���");
                        contentPanel.add(label);
                        //����������
                        progressbar.setOrientation(JProgressBar.HORIZONTAL);
                        progressbar.setMinimum(0);
                        progressbar.setMaximum(barSize);
                        progressbar.setValue(barOffset);
                        progressbar.setStringPainted(true);
                        progressbar.setPreferredSize(new Dimension(150, 20));
                        progressbar.setBorderPainted(true);
                        progressbar.setBackground(Color.pink);
                        //ȡ����ť
                        JButton cancelBt = new JButton("ȡ��");
                        //�������
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
                        label.setText(file.getName() + " �������");
                    }
                    dis.close();
                    dos.close();
                    sandFileStream.close();
                }catch (IOException e1){
                    label.setText(" ȡ������,���ӹر�");
                }finally {
                    framec.dispose();
                    try {
                        socket.close();//δʹ��this.socket
                    }catch (IOException e2){
                        e2.printStackTrace();
                        }
                    }
            }
        }

        class cancelActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e3) {
                try {
                    label.setText(" ȡ������,���ӹر�");
                    JOptionPane.showMessageDialog(framec, "ȡ�����͸������ӹر�!", "��ʾ��", JOptionPane.INFORMATION_MESSAGE);
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
