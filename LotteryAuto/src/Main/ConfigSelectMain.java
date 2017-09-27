/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import ORG.Helper;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import lotteryauto.panel.SelectConfigListOne;
import lotteryauto.panel.SelectPanelManage;
import static lotteryauto.panel.SelectPanelManage.configThis;

/**
 * 配置文件列表选择界面
 *
 * @author jerry
 */
public class ConfigSelectMain extends PublicMain {

    /**
     * 所有的main
     */
    public static HashMap AllMains = new HashMap();
    
    public HashMap AllJpanel = new HashMap();

    /**
     * 所有startmain的刷新控制线程
     */
    public Thread AllThread;
    
    public boolean StartMainThreadStatus = true;

    /**
     * Creates new form ConfigSelectMain
     */
    public ConfigSelectMain() {
        
        initComponents();
        //设置居中
        this.setLocationRelativeTo(null);

        //初始化配置文件加载控件显示
        SelectPanelManage.Select(this);

        //建立一个县城,循环刷新,看看有没有打开的StartMain,并读取数据
        AllThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (StartMainThreadStatus) {
                    REAllStartMainsInfo();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ConfigSelectMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
        });
        AllThread.start();

        /**
         * 监听关闭时间 回收
         */
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                
                AllMains = null;
                AllJpanel = null;
                
                StartMainThreadStatus = false;
                AllThread = null;
                
            }
        });
        
    }

    /**
     * 刷新所有的StartMain 并读取数据
     */
    public void REAllStartMainsInfo() {
        //出错后,就会整个县城死掉.,这里每次运行建立一个线程
        new Thread(() -> {
            Iterator iter = AllJpanel.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String user_name = (String) entry.getKey();
                SelectConfigListOne one_jpanel = (SelectConfigListOne) AllJpanel.get(user_name);
                //没有这个key 则回复颜色
                if (!AllMains.containsKey(user_name)) {
                    one_jpanel.setBackground(new Color(223, 223, 223, 255));
                    continue;
                }

                //获得运行界面的实例
                StartMain oneMain = (StartMain) AllMains.get(user_name);
                String one_buy_double = oneMain.info_buy_double.getText();//获得倍数
                boolean one_auto_start = oneMain.conf_auto_start.isSelected();//获得自动下注
                boolean one_start_botton = oneMain.conf_start_button.isEnabled();//获得运行按钮
                String one_heart_jump = oneMain.info_heart_jump.getText();

                //获取jpanel 控件面板
                JLabel format_money = (JLabel) one_jpanel.format_money;//用户初始资金
                JLabel user_money = (JLabel) one_jpanel.now_money;//用户初始资金
                JLabel win_money = (JLabel) one_jpanel.win_money;//用户初始资金
                JLabel heart_jump = (JLabel) one_jpanel.heart_jump;//用户初始资金
                JLabel buy_double = (JLabel) one_jpanel.buy_double;//用户初始资金
                JLabel rule_tag = (JLabel) one_jpanel.rule_tag;//用户初始资金
                JLabel info_status = (JLabel) one_jpanel.info_status;//用户初始资金
                JLabel now_qihao = (JLabel) one_jpanel.now_qihao;//当前期号
                JLabel now_msg = (JLabel) one_jpanel.now_msg;//当前期号
                JButton auto_start = (JButton) one_jpanel.auto_start;//下注按钮

                //写出信息到面板
                format_money.setText(oneMain.info_format_money.getText());
                user_money.setText(oneMain.info_user_now_money.getText());
                info_status.setText(oneMain.line_next_status.getText());
                now_qihao.setText(oneMain.line_next_number.getText());
                now_msg.setText(oneMain.msg.getModel().getElementAt(0));
                //切割心跳
                if (!one_heart_jump.equals("--")) {
                    one_heart_jump = one_heart_jump.split(" ")[1];
                }
                heart_jump.setText(one_heart_jump);
                buy_double.setText(one_buy_double);
                //处理赢取计算的金额
                double double_format_money = Double.parseDouble(format_money.getText());
                double double_now_money = Double.parseDouble(user_money.getText());
                double one_win_money = double_now_money - double_format_money;
                win_money.setText("" + one_win_money);
                try {
                    rule_tag.setText("规则:" + oneMain.conf_rule_list.getSelectedItem().toString());
                } catch (Exception e) {
                }

                //刷新5个通道
                REFiveTDInfo(oneMain.line_td_1, oneMain.line_ball_1.getText(), (JButton) one_jpanel.td1);
                REFiveTDInfo(oneMain.line_td_2, oneMain.line_ball_2.getText(), (JButton) one_jpanel.td2);
                REFiveTDInfo(oneMain.line_td_3, oneMain.line_ball_3.getText(), (JButton) one_jpanel.td3);
                REFiveTDInfo(oneMain.line_td_4, oneMain.line_ball_4.getText(), (JButton) one_jpanel.td4);
                REFiveTDInfo(oneMain.line_td_5, oneMain.line_ball_5.getText(), (JButton) one_jpanel.td5);

                //改变下注按钮
                auto_start.setSelected(one_auto_start);
                if (one_auto_start) {
                    auto_start.setText("暂停");
                } else {
                    auto_start.setText("下注");
                }
                //改变背景色
                if (!one_start_botton) {
                    one_jpanel.setBackground(new Color(212, 239, 205, 255));
                    if (!one_auto_start) {
                        one_jpanel.setBackground(new Color(255, 222, 222, 255));
                    }
                } else {
                    one_jpanel.setBackground(new Color(223, 223, 223, 255));
                }
                
            }
        }).start();
        
    }

    /**
     * 刷新5个通道的状态
     *
     * @param five_td_status
     * @param td1
     * @param td2
     * @param td3
     * @param td4
     * @param td5
     */
    private void REFiveTDInfo(JButton mian_button, String money, JButton td) {
        //获得对应通道的实际状态,改变界面
        td.setSelected(mian_button.isSelected());
        td.setText(money);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        WebTagGroup = new javax.swing.ButtonGroup();
        jScrollPane = new javax.swing.JScrollPane();
        jPanel_list = new javax.swing.JPanel();
        jPanel_add = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        text_new_name = new javax.swing.JTextField();
        button_add = new javax.swing.JButton();
        WebTag_HHGJ = new javax.swing.JRadioButton();
        WebTag_WNSR = new javax.swing.JRadioButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        all_close_button = new javax.swing.JButton();
        all_close_buy_button = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        all_close_buy_button1 = new javax.swing.JButton();
        all_close_buy_button2 = new javax.swing.JButton();
        td1_botton = new javax.swing.JButton();
        td2_botton = new javax.swing.JButton();
        td3_botton = new javax.swing.JButton();
        td4_botton = new javax.swing.JButton();
        td5_botton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        gai_td_1 = new javax.swing.JButton();
        gai_td_2 = new javax.swing.JButton();
        gai_td_3 = new javax.swing.JButton();
        gai_td_4 = new javax.swing.JButton();
        gai_td_5 = new javax.swing.JButton();
        hidden_nostart_main = new javax.swing.JButton();
        windows_top = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LotteryAuto V3 总控制台");
        setResizable(false);

        jScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jPanel_list.setPreferredSize(new java.awt.Dimension(0, 0));

        javax.swing.GroupLayout jPanel_listLayout = new javax.swing.GroupLayout(jPanel_list);
        jPanel_list.setLayout(jPanel_listLayout);
        jPanel_listLayout.setHorizontalGroup(
            jPanel_listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 939, Short.MAX_VALUE)
        );
        jPanel_listLayout.setVerticalGroup(
            jPanel_listLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 447, Short.MAX_VALUE)
        );

        jScrollPane.setViewportView(jPanel_list);

        jPanel_add.setBackground(new java.awt.Color(51, 51, 51));

        jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("添加新用户配置文件");

        text_new_name.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        text_new_name.setBorder(null);

        button_add.setBackground(java.awt.Color.black);
        button_add.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        button_add.setForeground(new java.awt.Color(255, 255, 255));
        button_add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/button_black.png"))); // NOI18N
        button_add.setText("创建新配置");
        button_add.setBorder(null);
        button_add.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button_add.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_addActionPerformed(evt);
            }
        });

        WebTag_HHGJ.setBackground(java.awt.Color.darkGray);
        WebTagGroup.add(WebTag_HHGJ);
        WebTag_HHGJ.setForeground(java.awt.Color.orange);
        WebTag_HHGJ.setSelected(true);
        WebTag_HHGJ.setText("辉煌国际 1.97");
        WebTag_HHGJ.setActionCommand("HHGJ");
        WebTag_HHGJ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WebTag_HHGJActionPerformed(evt);
            }
        });

        WebTag_WNSR.setBackground(java.awt.Color.darkGray);
        WebTagGroup.add(WebTag_WNSR);
        WebTag_WNSR.setForeground(java.awt.Color.orange);
        WebTag_WNSR.setText("威尼斯人1.982");
        WebTag_WNSR.setActionCommand("WNSR");
        WebTag_WNSR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WebTag_WNSRActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel_addLayout = new javax.swing.GroupLayout(jPanel_add);
        jPanel_add.setLayout(jPanel_addLayout);
        jPanel_addLayout.setHorizontalGroup(
            jPanel_addLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_addLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel_addLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(WebTag_WNSR)
                    .addComponent(WebTag_HHGJ)
                    .addGroup(jPanel_addLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(text_new_name, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel_addLayout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addGap(129, 129, 129))
                        .addComponent(button_add)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel_addLayout.setVerticalGroup(
            jPanel_addLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_addLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(WebTag_HHGJ)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(WebTag_WNSR)
                .addGap(18, 18, 18)
                .addComponent(text_new_name, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_add)
                .addGap(12, 12, 12))
        );

        jPanel1.setBackground(java.awt.Color.darkGray);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(204, 204, 204));
        jLabel2.setText("全局控制");

        all_close_button.setBackground(java.awt.Color.gray);
        all_close_button.setForeground(new java.awt.Color(255, 255, 255));
        all_close_button.setText("全部关闭运行");
        all_close_button.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        all_close_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_close_buttonActionPerformed(evt);
            }
        });

        all_close_buy_button.setBackground(java.awt.Color.gray);
        all_close_buy_button.setForeground(new java.awt.Color(255, 255, 255));
        all_close_buy_button.setText("全部关闭下注");
        all_close_buy_button.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        all_close_buy_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_close_buy_buttonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel3.setForeground(java.awt.Color.lightGray);
        jLabel3.setText("单独通道管理");

        all_close_buy_button1.setBackground(java.awt.Color.gray);
        all_close_buy_button1.setForeground(new java.awt.Color(255, 255, 255));
        all_close_buy_button1.setText("全部关闭通道");
        all_close_buy_button1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        all_close_buy_button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_close_buy_button1ActionPerformed(evt);
            }
        });

        all_close_buy_button2.setBackground(java.awt.Color.gray);
        all_close_buy_button2.setForeground(new java.awt.Color(255, 255, 255));
        all_close_buy_button2.setText("全部打开通道");
        all_close_buy_button2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        all_close_buy_button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_close_buy_button2ActionPerformed(evt);
            }
        });

        td1_botton.setBackground(java.awt.Color.darkGray);
        td1_botton.setForeground(new java.awt.Color(255, 255, 255));
        td1_botton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        td1_botton.setText("通道1");
        td1_botton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        td1_botton.setSelected(true);
        td1_botton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        td1_botton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                td1_bottonActionPerformed(evt);
            }
        });

        td2_botton.setBackground(java.awt.Color.darkGray);
        td2_botton.setForeground(new java.awt.Color(255, 255, 255));
        td2_botton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        td2_botton.setText("通道2");
        td2_botton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        td2_botton.setSelected(true);
        td2_botton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        td2_botton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                td2_bottonActionPerformed(evt);
            }
        });

        td3_botton.setBackground(java.awt.Color.darkGray);
        td3_botton.setForeground(new java.awt.Color(255, 255, 255));
        td3_botton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        td3_botton.setText("通道3");
        td3_botton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        td3_botton.setSelected(true);
        td3_botton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        td3_botton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                td3_bottonActionPerformed(evt);
            }
        });

        td4_botton.setBackground(java.awt.Color.darkGray);
        td4_botton.setForeground(new java.awt.Color(255, 255, 255));
        td4_botton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        td4_botton.setText("通道4");
        td4_botton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        td4_botton.setSelected(true);
        td4_botton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        td4_botton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                td4_bottonActionPerformed(evt);
            }
        });

        td5_botton.setBackground(java.awt.Color.darkGray);
        td5_botton.setForeground(new java.awt.Color(255, 255, 255));
        td5_botton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        td5_botton.setText("通道5");
        td5_botton.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        td5_botton.setSelected(true);
        td5_botton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        td5_botton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                td5_bottonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 16)); // NOI18N
        jLabel4.setForeground(java.awt.Color.lightGray);
        jLabel4.setText("人工改注");

        gai_td_1.setBackground(java.awt.Color.gray);
        gai_td_1.setForeground(java.awt.Color.white);
        gai_td_1.setText("1改--");
        gai_td_1.setActionCommand("1");
        gai_td_1.setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        gai_td_1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_gai_td_ActionPerformed(evt);
            }
        });

        gai_td_2.setBackground(java.awt.Color.gray);
        gai_td_2.setForeground(java.awt.Color.white);
        gai_td_2.setText("2改--");
        gai_td_2.setActionCommand("2");
        gai_td_2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        gai_td_2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_gai_td_ActionPerformed(evt);
            }
        });

        gai_td_3.setBackground(java.awt.Color.gray);
        gai_td_3.setForeground(java.awt.Color.white);
        gai_td_3.setText("3改--");
        gai_td_3.setActionCommand("3");
        gai_td_3.setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        gai_td_3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_gai_td_ActionPerformed(evt);
            }
        });

        gai_td_4.setBackground(java.awt.Color.gray);
        gai_td_4.setForeground(java.awt.Color.white);
        gai_td_4.setText("4改");
        gai_td_4.setActionCommand("4");
        gai_td_4.setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        gai_td_4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_gai_td_ActionPerformed(evt);
            }
        });

        gai_td_5.setBackground(java.awt.Color.gray);
        gai_td_5.setForeground(java.awt.Color.white);
        gai_td_5.setText("5改");
        gai_td_5.setActionCommand("5");
        gai_td_5.setBorder(new javax.swing.border.LineBorder(java.awt.Color.lightGray, 1, true));
        gai_td_5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all_gai_td_ActionPerformed(evt);
            }
        });

        hidden_nostart_main.setBackground(java.awt.Color.darkGray);
        hidden_nostart_main.setForeground(java.awt.Color.orange);
        hidden_nostart_main.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        hidden_nostart_main.setToolTipText("隐藏未启动的/全部显示账号");
        hidden_nostart_main.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        hidden_nostart_main.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        hidden_nostart_main.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hidden_nostart_mainActionPerformed(evt);
            }
        });

        windows_top.setBackground(java.awt.Color.darkGray);
        windows_top.setForeground(java.awt.Color.orange);
        windows_top.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.png"))); // NOI18N
        windows_top.setToolTipText("窗口最前面!显示");
        windows_top.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        windows_top.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.png"))); // NOI18N
        windows_top.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windows_topActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(windows_top, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hidden_nostart_main, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(all_close_button, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(all_close_buy_button, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(all_close_buy_button1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(all_close_buy_button2, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(td1_botton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gai_td_1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(td2_botton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gai_td_2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(td3_botton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gai_td_3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(td4_botton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gai_td_4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(td5_botton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(gai_td_5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 214, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {all_close_button, all_close_buy_button, all_close_buy_button1, all_close_buy_button2});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(hidden_nostart_main))
                    .addComponent(windows_top))
                .addGap(0, 0, 0)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(all_close_button, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(all_close_buy_button)
                    .addComponent(all_close_buy_button1)
                    .addComponent(all_close_buy_button2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(td1_botton)
                    .addComponent(td2_botton)
                    .addComponent(td3_botton)
                    .addComponent(td4_botton)
                    .addComponent(td5_botton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gai_td_1)
                    .addComponent(gai_td_2)
                    .addComponent(gai_td_3)
                    .addComponent(gai_td_4)
                    .addComponent(gai_td_5))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {all_close_button, all_close_buy_button, all_close_buy_button1, all_close_buy_button2});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel_add, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 941, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_add, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPanel1, jPanel_add});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * 添加按钮事件
     *
     * @param evt
     */
    private void button_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_addActionPerformed
        String user_name = text_new_name.getText();
        
        if (user_name.equals("")) {
            JOptionPane.showMessageDialog(null, "新用户配置文件名不能为空!");
            return;
        }
//配置助手
        String use_tag = WebTagGroup.getSelection().getActionCommand();
        Helper helper = new Helper();
        helper.SetConf(user_name + "_" + use_tag);

        //写出WEB的tag
        helper.SetConfKey("web_tag", use_tag);
        //写入用户名
        helper.SetConfKey("user_name", user_name);

        //打开运行主界面,关闭此页面
        SelectPanelManage.AddJpanel(AllJpanel.size(), user_name, use_tag);
        
        SelectPanelManage.GoStartMain(user_name + "_" + use_tag);
    }//GEN-LAST:event_button_addActionPerformed

    private void WebTag_HHGJActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WebTag_HHGJActionPerformed

    }//GEN-LAST:event_WebTag_HHGJActionPerformed

    private void WebTag_WNSRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WebTag_WNSRActionPerformed

    }//GEN-LAST:event_WebTag_WNSRActionPerformed

    private void all_close_buy_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all_close_buy_buttonActionPerformed
        CloseConf("auto_buy");
    }//GEN-LAST:event_all_close_buy_buttonActionPerformed

    private void all_close_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all_close_buttonActionPerformed
        CloseConf("auto_start");
    }//GEN-LAST:event_all_close_buttonActionPerformed

    private void all_close_buy_button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all_close_buy_button1ActionPerformed
        CloseConf("all_close_td");
    }//GEN-LAST:event_all_close_buy_button1ActionPerformed

    private void all_close_buy_button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all_close_buy_button2ActionPerformed
        CloseConf("all_open_td");
    }//GEN-LAST:event_all_close_buy_button2ActionPerformed

    private void td1_bottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_td1_bottonActionPerformed
        CloseConf("td1_open_close_td");
        td1_botton.setSelected(!td1_botton.isSelected());
    }//GEN-LAST:event_td1_bottonActionPerformed

    private void td2_bottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_td2_bottonActionPerformed
        CloseConf("td2_open_close_td");
        td2_botton.setSelected(!td2_botton.isSelected());
    }//GEN-LAST:event_td2_bottonActionPerformed

    private void td3_bottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_td3_bottonActionPerformed
        CloseConf("td3_open_close_td");
        td3_botton.setSelected(!td3_botton.isSelected());
    }//GEN-LAST:event_td3_bottonActionPerformed

    private void td4_bottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_td4_bottonActionPerformed
        CloseConf("td4_open_close_td");
        td4_botton.setSelected(!td4_botton.isSelected());
    }//GEN-LAST:event_td4_bottonActionPerformed

    private void td5_bottonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_td5_bottonActionPerformed
        CloseConf("td5_open_close_td");
        td5_botton.setSelected(!td5_botton.isSelected());
    }//GEN-LAST:event_td5_bottonActionPerformed

    private void all_gai_td_ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all_gai_td_ActionPerformed
        //获取按钮
        JButton use_button = (JButton) evt.getSource();
        //获取通道
        String TD = evt.getActionCommand();
        //实例
        SelectChangeMoneyMain use_main = new SelectChangeMoneyMain(this, TD);
        use_main.UseButton = use_button;
        use_main.setVisible(true);

    }//GEN-LAST:event_all_gai_td_ActionPerformed

    /**
     * 隐藏没有启动的startmain 操作
     *
     * @param evt
     */
    private void hidden_nostart_mainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hidden_nostart_mainActionPerformed
        //获取按钮状态并转换为要操作的状态
        boolean hidden_status = !(hidden_nostart_main.isSelected());

        //改变界面
        REAllMainShowOrHidden(hidden_status);
        //改变按钮显示
        hidden_nostart_main.setSelected(hidden_status);
    }//GEN-LAST:event_hidden_nostart_mainActionPerformed

    /**
     * 窗口最前面处理
     *
     * @param evt
     */
    private void windows_topActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_windows_topActionPerformed
        
        boolean win_status = windows_top.isSelected();
        this.setAlwaysOnTop(!win_status);
        windows_top.setSelected(!win_status);
    }//GEN-LAST:event_windows_topActionPerformed

    /**
     * 刷新账号面板,根据传入来隐藏或全开所有面板
     * <br>以及重新定位
     *
     * @param hidden_status false 全部显示 true 隐藏掉没有运行的
     */
    private void REAllMainShowOrHidden(boolean hidden_status) {
        
        int panel_number = 0;//定位需要使用的统计数字
        //读取并循环所有面板
//操作刷新状态
        Iterator iter = AllJpanel.entrySet().iterator();

        //循环所有记录中的面板
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String user_name = (String) entry.getKey();
            //获得当前的控件
            SelectConfigListOne one_jpanel = (SelectConfigListOne) AllJpanel.get(user_name);
            
            boolean is_show = true;
            //开始重新定位
            //如果hidden是true 则只显示 运行的,如果false则全部运行
            if (hidden_status) {
                //检查对应的main是否是运行的
                if (!AllMains.containsKey(user_name)) {
                    //没有运行 则改变
                    is_show = false;
                }
            }

            //不允许显示的就隐藏,允许显示的就重新排位
            if (is_show) {
                one_jpanel.setVisible(true);
                //重新排位
                //设置位置
                int use_width = jPanel_list.getWidth();
                one_jpanel.setBounds(0, (panel_number * 82), use_width, 80);

                //数字加1
                panel_number++;
                
            } else {
                one_jpanel.setVisible(false);
            }
            
        }
    }

    /**
     * 打开手动改变下注金额界面
     *
     * @param TD td通道
     */
    public void StartChangeMain(String TD) {
        new SelectChangeMoneyMain(this, TD).setVisible(true);
    }

    /**
     * 全账户管理,关闭开启
     *
     * @param CloseTag
     */
    public void CloseConf(String CloseTag) {
        Iterator iter = AllMains.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String user_name = (String) entry.getKey();
            StartMain one_main = (StartMain) entry.getValue();
            switch (CloseTag) {
                case "auto_buy":
                    one_main.conf_auto_stop.setSelected(true);
                    break;
                case "auto_start":
                    one_main.Auto.Stop();
                    break;
                case "all_close_td"://快速全开全关通道
                    one_main.Auto.FiveTDBuyStatus = new boolean[]{true, true, true, true, true, true};
                    one_main.Auto.SettingFiveTdBuyStatus(1);
                    one_main.Auto.SettingFiveTdBuyStatus(2);
                    one_main.Auto.SettingFiveTdBuyStatus(3);
                    one_main.Auto.SettingFiveTdBuyStatus(4);
                    one_main.Auto.SettingFiveTdBuyStatus(5);
                    td1_botton.setSelected(false);
                    td2_botton.setSelected(false);
                    td3_botton.setSelected(false);
                    td4_botton.setSelected(false);
                    td5_botton.setSelected(false);
                    break;
                case "all_open_td"://快速全开全关通道
                    one_main.Auto.FiveTDBuyStatus = new boolean[]{false, false, false, false, false, false};
                    one_main.Auto.SettingFiveTdBuyStatus(1);
                    one_main.Auto.SettingFiveTdBuyStatus(2);
                    one_main.Auto.SettingFiveTdBuyStatus(3);
                    one_main.Auto.SettingFiveTdBuyStatus(4);
                    one_main.Auto.SettingFiveTdBuyStatus(5);
                    td1_botton.setSelected(true);
                    td2_botton.setSelected(true);
                    td3_botton.setSelected(true);
                    td4_botton.setSelected(true);
                    td5_botton.setSelected(true);
                    
                    break;
                case "td1_open_close_td":
                    one_main.Auto.FiveTDBuyStatus[1] = td1_botton.isSelected();
                    one_main.Auto.SettingFiveTdBuyStatus(1);
                    
                    break;
                case "td2_open_close_td":
                    one_main.Auto.FiveTDBuyStatus[2] = td2_botton.isSelected();
                    one_main.Auto.SettingFiveTdBuyStatus(2);
                    
                    break;
                case "td3_open_close_td":
                    one_main.Auto.FiveTDBuyStatus[3] = td3_botton.isSelected();
                    one_main.Auto.SettingFiveTdBuyStatus(3);
                    
                    break;
                case "td4_open_close_td":
                    one_main.Auto.FiveTDBuyStatus[4] = td4_botton.isSelected();
                    one_main.Auto.SettingFiveTdBuyStatus(4);
                    
                    break;
                case "td5_open_close_td":
                    one_main.Auto.FiveTDBuyStatus[5] = td5_botton.isSelected();
                    one_main.Auto.SettingFiveTdBuyStatus(5);
                    
                    break;
                
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ConfigSelectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ConfigSelectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ConfigSelectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ConfigSelectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(() -> {
//            ConfigSelectMain select_main = new ConfigSelectMain();
//        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup WebTagGroup;
    private javax.swing.JRadioButton WebTag_HHGJ;
    private javax.swing.JRadioButton WebTag_WNSR;
    private javax.swing.JButton all_close_button;
    private javax.swing.JButton all_close_buy_button;
    private javax.swing.JButton all_close_buy_button1;
    private javax.swing.JButton all_close_buy_button2;
    private javax.swing.JButton button_add;
    public javax.swing.JButton gai_td_1;
    public javax.swing.JButton gai_td_2;
    public javax.swing.JButton gai_td_3;
    public javax.swing.JButton gai_td_4;
    public javax.swing.JButton gai_td_5;
    private javax.swing.JButton hidden_nostart_main;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_add;
    public javax.swing.JPanel jPanel_list;
    public javax.swing.JScrollPane jScrollPane;
    private javax.swing.JButton td1_botton;
    private javax.swing.JButton td2_botton;
    private javax.swing.JButton td3_botton;
    private javax.swing.JButton td4_botton;
    private javax.swing.JButton td5_botton;
    private javax.swing.JTextField text_new_name;
    private javax.swing.JButton windows_top;
    // End of variables declaration//GEN-END:variables
}
