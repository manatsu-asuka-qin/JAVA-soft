package Chat.GUI;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.TitledBorder;
import java.io.*;

public class ServerGUI {
    private  static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();//��ȡ��Ļ��С
    protected JFrame frame;
    protected JTextArea logTxArea;//������log��ʾ����
    protected JTextField allowusrTxfield,portTxfield;//����������������ͼ����˿�
    protected JTextField serverMsgTxfied;//δʹ��
    protected JTextArea serverSendArea;//���ͷ������Ϣ��
    protected JButton startBt, stopBt, sendBt;//��ʼ���رա�����button
    protected JList userList;//�û��б�

    protected JMenuBar menuBar;//�˵���
    protected JMenuItem mItemChooseSystem;//ѡ��ϵͳ��
    protected JFileChooser upFileChooser ;//�ļ��ϴ�ѡ����
    protected JMenu SystemItem;//ѡ����ϵͳ�˵�

    protected String WinOrLinux = "Windows";//������ϵͳ

    protected DefaultListModel<String> listModel;

    protected ServerGUI(){
        initui();
    }

    @SuppressWarnings("unchecked")

    private void initui(){
        frame = new JFrame("������");
        int framewidth = 800;
        int frameheigh = 600;
        frame.setSize(framewidth, frameheigh);
        frame.setLocation((screenSize.width - framewidth) / 2, (screenSize.height - frameheigh) / 2);
        frame.setResizable(true);//��Ե��������
        frame.setLayout(new BorderLayout());//�����沼�ַ�ʽ
        allowusrTxfield = new JTextField("10");
        String port ="8000";
        portTxfield = new JTextField(port);
        startBt = new JButton("����");
        stopBt = new JButton("ֹͣ");


        menuBar = new JMenuBar();//���ɲ˵�������frame
        frame.setJMenuBar(menuBar);

        SystemItem = new JMenu("��������");
        menuBar.add(SystemItem);
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

        JPanel settingPanel = new JPanel();//������
        settingPanel.setLayout(new GridLayout(1, 6));
        settingPanel.add(new JLabel("ϵͳ��������"));
        settingPanel.add(allowusrTxfield);
        settingPanel.add(new JLabel("�˿ں�"));
        settingPanel.add(portTxfield);
        settingPanel.add(startBt);
        settingPanel.add(stopBt);
        settingPanel.setBorder(new TitledBorder("����"));
        frame.add(settingPanel, "North");

        logTxArea = new JTextArea();
        logTxArea.setEnabled(false);
        logTxArea.setForeground(Color.black);

        JScrollPane logPanel= new JScrollPane(logTxArea);
        logPanel.setBorder(new TitledBorder("��������־"));

        listModel = new DefaultListModel<String>();
        userList = new JList(listModel);
        JScrollPane usrPanel =new JScrollPane(userList);
        usrPanel.setBorder(new TitledBorder("�����û�"));

        JSplitPane logsplitPanel =  new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,usrPanel,logPanel);
        logsplitPanel.setDividerLocation(100);

        frame.add(logsplitPanel, "Center");

        serverSendArea =new JTextArea();
        sendBt =new JButton("����");

        JPanel msgoperationPanel = new JPanel(new BorderLayout());
        msgoperationPanel.add(serverSendArea, "Center");
        msgoperationPanel.add(sendBt, "East");
        msgoperationPanel.setBorder(new TitledBorder("�㲥��Ϣ"));
        frame.add(msgoperationPanel, "South");
        frame.setVisible(true);


    }

    public JFileChooser winget(){//��window���ļ�
        upFileChooser =  new JFileChooser("C:\\Users\\katom\\eclipse-workspace\\localfile");
        return upFileChooser;
    }

    protected void setUISetting(boolean started){//���ã������Ƿ���������
        portTxfield.setEnabled(!started);
        allowusrTxfield.setEnabled(!started);
        startBt.setEnabled(!started);
        stopBt.setEnabled(started);
        serverMsgTxfied.setEnabled(started);
        sendBt.setEnabled(started);
    }


}
