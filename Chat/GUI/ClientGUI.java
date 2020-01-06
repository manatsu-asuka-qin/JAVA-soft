package Chat.GUI;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.*;
import java.io.*;

public class ClientGUI{
    private  static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();//获取屏幕大小

    protected JFrame frame;
    protected JTextField nameTxfield,ipTxfield,portTxfield;//用户名、ip端口
    protected JTextArea msgReciveArea,msgSendArea;//消息接收、发送区
    protected JLabel msgSendLabel;//发送给谁的标签
    protected JButton connectBt, disconnectBt, sendBt, uploadBt;//连接与断开连接、发送与上传的按钮
    protected JMenuBar menuBar;//菜单栏
    protected JMenu SystemItem;//选择工作系统菜单
    protected JFileChooser upFileChooser ;//文件上传选择器
    protected JList<String> userlist;//用户列表
    protected DefaultListModel<String> listModel;

    //生成上传文件菜单
    protected JMenu uploadItem ;//上传菜单
    protected JMenuItem mItemChooseSystem;//选择系统项
    protected JMenuItem mItemopenfile;//选择文件

    protected String WinOrLinux = "Windows";//工作的系统


    protected ClientGUI(){
        initui();
    }

    private void initui() {
        frame = new JFrame("客户端");//软件标题
        int framewidth = 800;
        int frameheigh = 600;
        frame.setSize(framewidth, frameheigh);
        frame.setLocation((screenSize.width - framewidth) / 2, (screenSize.height - frameheigh) / 2);
        //屏幕显示定位
        frame.setResizable(true);//边缘可以拉伸
        frame.getContentPane().setLayout(new BorderLayout());//主界面布局方式

        String IP ="127.0.0.1";
        String port= "8000";
        ipTxfield = new JTextField(IP);
        portTxfield = new JTextField(port);
        nameTxfield = new JTextField("qinyang");

        connectBt = new JButton("连接");
        disconnectBt = new JButton("断开");

        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new GridLayout(1, 8));
        settingPanel.add(new JLabel("         名字:"));
        settingPanel.add(nameTxfield);
        settingPanel.add(new JLabel("  服务器IP:"));
        settingPanel.add(ipTxfield);
        settingPanel.add(new JLabel("  端口号:"));
        settingPanel.add(portTxfield);
        settingPanel.add(connectBt);
        settingPanel.add(disconnectBt);
        settingPanel.setBorder(new TitledBorder("客户端连接配置"));
        frame.add(settingPanel, "North");




        menuBar = new JMenuBar();//生成菜单栏加入frame
        frame.setJMenuBar(menuBar);

        //生成新的菜单项并加入菜单栏
        SystemItem = new JMenu("工作环境");
        menuBar.add(SystemItem);
        //生成设定传输协议的菜单项
        mItemChooseSystem = new JMenuItem("选择系统");
        SystemItem.add(mItemChooseSystem);
        //对其菜单项目的点击监听
        mItemChooseSystem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                JOptionPane op = new JOptionPane();
                String[] str={"Windows","Linux"};
                WinOrLinux=String.valueOf(op.showInputDialog(panel,"请选择运行的系统","选择系统",1,null,str,str[0]));
            }
        });



        uploadItem = new JMenu("上传文件");
        menuBar.add(uploadItem);
        //生成打开项
        mItemopenfile = new JMenuItem("打开");
        uploadItem.add(mItemopenfile);


        //文本接收区域，默认不可用
        msgReciveArea = new JTextArea();
        msgReciveArea.setEnabled(false);
        msgReciveArea.setForeground(Color.black);
        //用可以滚动的容器
        JScrollPane msgBoxPanel = new JScrollPane(msgReciveArea);
        msgBoxPanel.setBorder(new TitledBorder("聊天和系统记录"));
        //用户列表
        listModel = new DefaultListModel<String>();
        userlist = new JList<>(listModel);
        JScrollPane usrPanel = new JScrollPane(userlist);
        usrPanel.setBorder(new TitledBorder("在线成员："));
        //设置用户列表和消息列表
        JSplitPane msgsplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,usrPanel,msgBoxPanel);
        frame.add(msgsplitPanel, "Center");

        //输入区域
        msgSendLabel = new JLabel("发送给:所有人  ");
        msgSendArea = new JTextArea();
        sendBt = new JButton("发送");

        Panel sendOperationPanel = new Panel();
        sendOperationPanel.add(sendBt);
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(msgSendLabel, BorderLayout.WEST);
        messagePanel.add(msgSendArea, BorderLayout.CENTER);
        messagePanel.add(sendOperationPanel, BorderLayout.EAST);
        messagePanel.setBorder(new TitledBorder("发送消息"));
        frame.add(messagePanel, "South");

        frame.setVisible(true);

        setUISetting(false);
    }

    public JFileChooser getFilePath(){
        if (WinOrLinux.equals("Windows")){//打开window的文件
            upFileChooser =  new JFileChooser("C:\\Users\\katom\\eclipse-workspace\\localfile");
            return upFileChooser;
        }
        else {//打开Linux文件
            upFileChooser = new JFileChooser("/home/tie/文档");
            return upFileChooser;
        }
    }

    protected void setUISetting(boolean isConnectd){
        //设置，根据是否连接
        nameTxfield.setEnabled(!isConnectd);
        ipTxfield.setEnabled(!isConnectd);
        portTxfield.setEnabled(!isConnectd);
        connectBt.setEnabled(!isConnectd);
        disconnectBt.setEnabled(isConnectd);
        msgSendArea.setEnabled(isConnectd);
        sendBt.setEnabled(isConnectd);

    }

}

