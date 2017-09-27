/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lotteryauto.panel;

import RTPower.RTFile;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import Main.ConfigSelectMain;
import Main.StartMain;
import java.awt.Dimension;

/**
 * 选择配置文件的界面 中的列表管理
 *
 * @author jerry
 */
public class SelectPanelManage {

    /**
     * 主界面的引用
     */
    public static ConfigSelectMain configThis;

    /**
     * 面板组 只10个
     */
//    public static JPanel[] PanelGroup = new JPanel[10];
    /**
     * 初始化加载用户配置文件列表
     * <br>读取配置目录的文件列表,然后创建控件显示
     *
     * @param aThis
     */
    public static void Select(ConfigSelectMain aThis) {
        //赋值引用
        configThis = aThis;

        //读取配置文件目录
        //没有目录自动创建
        String file_path = "." + RTFile.FG + "config";
        RTFile.CreateDirectory(file_path);

        int config_count = 0;
        //遍历目录下的文件
        File root = new File(file_path);
        File[] files = root.listFiles();
        for (File file : files) {
            if (file.isFile()) {

                String file_name = file.getName();
                if (file_name.contains(".txt")) {
                    String one_user_name = file_name.replace(".txt", "");
                    //切割用户名 获得 tag
                    String[] Q_one_user_name = one_user_name.split("_");
                    //得到文件名开始加载控件
                    AddJpanel(config_count, Q_one_user_name[0], Q_one_user_name[1]);

                    //最后在就加1
                    config_count++;
                }
            }

        }
        configThis.jPanel_list.setPreferredSize(new Dimension(0, config_count * 64));

    }

    /**
     * 在界面添加控件
     *
     * @param panel_number 控件的序号,后期用来删除 定位用
     * @param one_user_name 文件名
     * @param web_tagString
     */
    public static void AddJpanel(int panel_number, String one_user_name, String web_tagString) {
        //new 面板
        SelectConfigListOne config_list_one = new SelectConfigListOne();
        //设置位置
        int use_width = configThis.jPanel_list.getWidth();

        config_list_one.setBounds(0, (panel_number * 82), use_width, 80);

        //设置名称以及头像
        JLabel config_name = (JLabel) config_list_one.config_name;
        config_name.setText(one_user_name);
        config_name.setIcon(new ImageIcon(configThis.getClass().getResource("/images/user" + (panel_number % 5) + ".png")));

        //设置webtag 显示
        JLabel web_tag = (JLabel) config_list_one.web_tag;
        web_tag.setText(web_tagString);
        //设置按钮
        JButton config_button = (JButton) config_list_one.button_del;
        //记录下panel 的 id
        config_button.setActionCommand("" + panel_number);
        //添加组件到面板
        configThis.jPanel_list.add(config_list_one);
//        PanelGroup[panel_number] = config_list_one;
        configThis.AllJpanel.put(one_user_name + "_" + web_tagString, config_list_one);
        configThis.jPanel_list.repaint();
    }

    /**
     * 删除面板并刷新
     *
     * @param del_id 面板组ID
     */
    public static void Delpanel(String del_id) {

        configThis.jPanel_list.remove((JPanel) configThis.AllJpanel.get(del_id));
        configThis.AllJpanel.remove(del_id);
        configThis.jPanel_list.repaint();
    }

    /**
     * 开启运行页面,并关闭自己
     *
     * @param user_name 配置文件用户名
     */
    public static void GoStartMain(String user_name) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                //如果存在则不新开
                if (configThis.AllMains.containsKey(user_name)) {
                    //存在了则让窗口最前
                    StartMain one_start_main = (StartMain) configThis.AllMains.get(user_name);
                    one_start_main.setVisible(true);
                    return;
                }

                StartMain one_start_main = new StartMain(user_name);
                one_start_main.setVisible(true);
                configThis.AllMains.put(user_name, one_start_main);
//                configThis.dispose();
            }
        }).start();

    }

}
