package Chat.GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.*;
import java.io.*;

public class ClientGUI{
    private  static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();//��ȡ��Ļ��С

    protected JFrame frame;
    protected JTextField nameTxfield,ipTxfield,portTxfield;//�û�����ip�˿�
    protected JTextArea msgReciveArea,msgSendArea;//��Ϣ���ա�������
    protected JLabel msgSendLabel;//���͸�˭�ı�ǩ
    protected JButton connectBt, disconnectBt, sendBt, uploadBt;//������Ͽ����ӡ��������ϴ��İ�ť
    protected JMenuBar menuBar;//�˵���
    protected JMenu SystemItem;//ѡ����ϵͳ�˵�
    protected JFileChooser upFileChooser ;//�ļ��ϴ�ѡ����
    protected JList<String> userlist;//�û��б�
    protected DefaultListModel<String> listModel;

    //�����ϴ��ļ��˵�
    protected JMenu uploadItem ;//�ϴ��˵�
    protected JMenuItem mItemChooseSystem;//ѡ��ϵͳ��
    protected JMenuItem mItemopenfile;//ѡ���ļ�

    protected String WinOrLinux = "Windows";//������ϵͳ


    protected ClientGUI(){
        initui();
    }

    private void initui() {
        frame = new JFrame("�ͻ���");//�������
        int framewidth = 800;
        int frameheigh = 600;
        frame.setSize(framewidth, frameheigh);
        frame.setLocation((screenSize.width - framewidth) / 2, (screenSize.height - frameheigh) / 2);
        //��Ļ��ʾ��λ
        frame.setResizable(true);//��Ե��������
        frame.getContentPane().setLayout(new BorderLayout());//�����沼�ַ�ʽ

        String IP ="127.0.0.1";
        String port= "8000";
        ipTxfield = new JTextField(IP);
        portTxfield = new JTextField(port);
        nameTxfield = new JTextField("qinyang");

        connectBt = new JButton("����");
        disconnectBt = new JButton("�Ͽ�");

        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(1, 8));
        settingPanel.add(new JLabel("         ����:"));
        settingPanel.add(nameTxfield);
        settingPanel.add(new JLabel("  ������IP:"));
        settingPanel.add(ipTxfield);
        settingPanel.add(new JLabel("  �˿ں�:"));
        settingPanel.add(portTxfield);
        settingPanel.add(connectBt);
        settingPanel.add(disconnectBt);
        settingPanel.setBorder(new TitledBorder("�ͻ�����������"));
        frame.add(settingPanel, "North");




        menuBar = new JMenuBar();//���ɲ˵�������frame
        frame.setJMenuBar(menuBar);

        //�����µĲ˵������˵���
        SystemItem = new JMenu("��������");
        menuBar.add(SystemItem);
        //�����趨����Э��Ĳ˵���
        mItemChooseSystem = new JMenuItem("ѡ��ϵͳ");
        SystemItem.add(mItemChooseSystem);
        //����˵���Ŀ�ĵ������
        mItemChooseSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                JOptionPane op = new JOptionPane();
                String[] str={"Windows","Linux"};
                WinOrLinux=String.valueOf(op.showInputDialog(panel,"��ѡ�����е�ϵͳ","ѡ��ϵͳ",1,null,str,str[0]));
            }
        });



        uploadItem = new JMenu("�ϴ��ļ�");
        menuBar.add(uploadItem);
        //���ɴ���
        mItemopenfile = new JMenuItem("��");
        uploadItem.add(mItemopenfile);


        //�ı���������Ĭ�ϲ�����
        msgReciveArea = new JTextArea();
        msgReciveArea.setEnabled(false);
        msgReciveArea.setForeground(Color.black);
        //�ÿ��Թ���������
        JScrollPane msgBoxPanel = new JScrollPane(msgReciveArea);
        msgBoxPanel.setBorder(new TitledBorder("�����ϵͳ��¼"));
        //�û��б�
        listModel = new DefaultListModel<String>();
        userlist = new JList<>(listModel);
        JScrollPane usrPanel = new JScrollPane(userlist);
        usrPanel.setBorder(new TitledBorder("���߳�Ա��"));
        //�����û��б����Ϣ�б�
        JSplitPane msgsplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,usrPanel,msgBoxPanel);
        frame.add(msgsplitPanel, "Center");

        //��������
        msgSendLabel = new JLabel("���͸�:������  ");
        msgSendArea = new JTextArea();
        sendBt = new JButton("����");

        Panel sendOperationPanel = new Panel();
        sendOperationPanel.add(sendBt);
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(msgSendLabel, BorderLayout.WEST);
        messagePanel.add(msgSendArea, BorderLayout.CENTER);
        messagePanel.add(sendOperationPanel, BorderLayout.EAST);
        messagePanel.setBorder(new TitledBorder("������Ϣ"));
        frame.add(messagePanel, "South");

        frame.setVisible(true);

        setUISetting(false);
    }

    public JFileChooser getFilePath(){
        if (WinOrLinux.equals("Windows")){//��window���ļ�
            upFileChooser =  new JFileChooser("C:\\Users\\katom\\eclipse-workspace\\localfile");
            return upFileChooser;
        }
        else {//��Linux�ļ�
            upFileChooser = new JFileChooser("/home/tie/�ĵ�");
            return upFileChooser;
        }
    }

    protected void setUISetting(boolean isConnectd){
        //���ã������Ƿ�����
        nameTxfield.setEnabled(!isConnectd);
        ipTxfield.setEnabled(!isConnectd);
        portTxfield.setEnabled(!isConnectd);
        connectBt.setEnabled(!isConnectd);
        disconnectBt.setEnabled(isConnectd);
        msgSendArea.setEnabled(isConnectd);
        sendBt.setEnabled(isConnectd);

    }

}

