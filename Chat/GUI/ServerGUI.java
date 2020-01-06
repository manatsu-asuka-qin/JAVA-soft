package Chat.GUI;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.TitledBorder;
import java.io.*;

public class ServerGUI {
    private  static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();//获取屏幕大小
    protected JFrame frame;
    protected JTextArea logTxArea;//服务器log显示区域
    protected JTextField allowusrTxfield,portTxfield;//设置最大连接数，和监听端口
    protected JTextField serverMsgTxfied;//未使用
    protected JTextArea serverSendArea;//发送服务端消息处
    protected JButton startBt, stopBt, sendBt;//开始、关闭、发送button
    protected JList userList;//用户列表

    protected JMenuBar menuBar;//菜单栏
    protected JMenuItem mItemChooseSystem;//选择系统项
    protected JFileChooser upFileChooser ;//文件上传选择器
    protected JMenu SystemItem;//选择工作系统菜单

    protected String WinOrLinux = "Windows";//工作的系统

    protected DefaultListModel<String> listModel;

    protected ServerGUI(){
        initui();
    }

    @SuppressWarnings("unchecked")

    private void initui(){
        frame = new JFrame("服务器");
        int framewidth = 800;
        int frameheigh = 600;
        frame.setSize(framewidth, frameheigh);
        frame.setLocation((screenSize.width - framewidth) / 2, (screenSize.height - frameheigh) / 2);
        frame.setResizable(true);//边缘可以拉伸
        frame.setLayout(new BorderLayout());//主界面布局方式
        allowusrTxfield = new JTextField("10");
        String port ="8000";
        portTxfield = new JTextField(port);
        startBt = new JButton("启动");
        stopBt = new JButton("停止");


        menuBar = new JMenuBar();//生成菜单栏加入frame
        frame.setJMenuBar(menuBar);

        SystemItem = new JMenu("工作环境");
        menuBar.add(SystemItem);
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

        JPanel settingPanel = new JPanel();//设置区
        settingPanel.setLayout(new GridLayout(1, 6));
        settingPanel.add(new JLabel("系统允许人数"));
        settingPanel.add(allowusrTxfield);
        settingPanel.add(new JLabel("端口号"));
        settingPanel.add(portTxfield);
        settingPanel.add(startBt);
        settingPanel.add(stopBt);
        settingPanel.setBorder(new TitledBorder("配置"));
        frame.add(settingPanel, "North");

        logTxArea = new JTextArea();
        logTxArea.setEnabled(false);
        logTxArea.setForeground(Color.black);

        JScrollPane logPanel= new JScrollPane(logTxArea);
        logPanel.setBorder(new TitledBorder("服务器日志"));

        listModel = new DefaultListModel<String>();
        userList = new JList(listModel);
        JScrollPane usrPanel =new JScrollPane(userList);
        usrPanel.setBorder(new TitledBorder("在线用户"));

        JSplitPane logsplitPanel =  new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,usrPanel,logPanel);
        logsplitPanel.setDividerLocation(100);

        frame.add(logsplitPanel, "Center");

        serverSendArea =new JTextArea();
        sendBt =new JButton("发送");

        JPanel msgoperationPanel = new JPanel(new BorderLayout());
        msgoperationPanel.add(serverSendArea, "Center");
        msgoperationPanel.add(sendBt, "East");
        msgoperationPanel.setBorder(new TitledBorder("广播消息"));
        frame.add(msgoperationPanel, "South");
        frame.setVisible(true);


    }

    public JFileChooser winget(){//打开window的文件
        upFileChooser =  new JFileChooser("C:\\Users\\katom\\eclipse-workspace\\localfile");
        return upFileChooser;
    }

    protected void setUISetting(boolean started){//设置，根据是否连接设置
        portTxfield.setEnabled(!started);
        allowusrTxfield.setEnabled(!started);
        startBt.setEnabled(!started);
        stopBt.setEnabled(started);
        serverMsgTxfied.setEnabled(started);
        sendBt.setEnabled(started);
    }


}
