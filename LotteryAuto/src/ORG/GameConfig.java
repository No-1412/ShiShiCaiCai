/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ORG;

import Main.StartMain;
import RTPower.RTAES;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * 游戏配置以及加载类
 *
 * @author jerry
 */
public class GameConfig {

    /**
     * main 引用
     */
    public StartMain SMain;

    /**
     * 查询的对象 记录下来
     */
    public Object FindObject;

    /**
     * AES加密解密
     */
    public RTAES ConfAes;

    public Helper helper;

    public GameConfig(StartMain start_main, String UserName) {
        //全局引用
        SMain = start_main;
        ConfAes = new RTAES();
        ConfAes.SetKey("qazqazqazqazqazq", "wsxwsxwsxwsxwsxw");
        helper = new Helper();
        helper.SetConf(UserName);
    }

    /**
     * 加载所有配置到界面
     */
    public void LoadAllConfigToMain() {
        SMain.conf_user_name.setText(helper.GetConfKey("user_name"));
        String aes_pass = "";
        String aes_out_pass = "";
        if (!helper.GetConfKey("user_pass").equals("") && !helper.GetConfKey("user_out_pass").equals("")) {
            try {
                //解密写出pass
                aes_pass = new String(ConfAes.decrypt(helper.GetConfKey("user_pass"))).trim();
                aes_out_pass = new String(ConfAes.decrypt(helper.GetConfKey("user_out_pass"))).trim();

            } catch (Exception ex) {
                Logger.getLogger(GameConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        SMain.conf_user_pass.setText(aes_pass);
        SMain.conf_user_out_pass.setText(aes_out_pass);

        SMain.conf_loss_limit.setText(helper.GetConfKey("conf_loss_limit"));
        SMain.conf_win_limit.setText(helper.GetConfKey("conf_win_limit"));
        SMain.conf_tixian_limit.setText(helper.GetConfKey("conf_tixian_limit"));
        SMain.conf_api_bout_limit.setText(helper.GetConfKey("conf_api_bout_limit"));
        //下注倍数
        SMain.conf_buy_double.setSelectedItem(helper.GetConfKey("conf_buy_double"));
        
        //刷新所有通道的下注倍数
        SMain.FormatLineTdBei(helper.GetConfKey("conf_buy_double"));
        //最小期号写出
        String limit_num = helper.GetConfKey("conf_limit_game_num").equals("") ? "120" : helper.GetConfKey("conf_limit_game_num");
        SMain.conf_limit_game_num.setText(limit_num);

        //加载域名
        SMain.info_use_domain.setText(helper.GetConfKey("info_use_domain"));

    }

    /**
     * 自动保存界面所有配置
     */
    public void SaveAllConfigToFile() {

        helper.SetConfKey("user_name", SMain.conf_user_name.getText());
        String aes_pass = "";
        String aes_out_pass = "";
        try {
            String conf_user_pass_t = String.valueOf(SMain.conf_user_pass.getPassword());
            if (!conf_user_pass_t.equals("")) {
                byte[] aes_pass_b = ConfAes.encrypt(conf_user_pass_t);
                aes_pass = ConfAes.bytesToHex(aes_pass_b);
            }

            String conf_user_out_pass_t = String.valueOf(SMain.conf_user_out_pass.getPassword());
            if (!conf_user_out_pass_t.equals("")) {
                byte[] aes_out_pass_b = ConfAes.encrypt(conf_user_out_pass_t);
                aes_out_pass = ConfAes.bytesToHex(aes_out_pass_b);
            }

        } catch (Exception ex) {
            Logger.getLogger(GameConfig.class.getName()).log(Level.SEVERE, null, ex);
        }

        helper.SetConfKey("user_pass", aes_pass);
        helper.SetConfKey("user_out_pass", aes_out_pass);

        helper.SetConfKey("conf_loss_limit", SMain.conf_loss_limit.getText());
        helper.SetConfKey("conf_win_limit", SMain.conf_win_limit.getText());
        helper.SetConfKey("conf_tixian_limit", SMain.conf_tixian_limit.getText());
        helper.SetConfKey("conf_api_bout_limit", SMain.conf_api_bout_limit.getText());
        helper.SetConfKey("conf_buy_double", SMain.conf_buy_double.getSelectedItem().toString());
        try {
              helper.SetConfKey("conf_rule_list", SMain.conf_rule_list.getSelectedItem().toString());
        } catch (Exception e) {
            System.err.println("保存rule出错");
        }
      
        
        helper.SetConfKey("conf_limit_game_num", SMain.conf_limit_game_num.getText());
        //保存域名
        helper.SetConfKey("info_use_domain", SMain.info_use_domain.getText());
    }

    /**
     * 根据字符串定位控件 以及类型
     *
     * @param key_name
     * @return
     */
    public GameConfig Find(String key_name) {
        Object backObject = null;

        switch (key_name) {
            case "conf_user_name":
                backObject = SMain.conf_user_name;
                break;
            case "conf_user_pass":
                backObject = SMain.conf_user_pass;
                break;
            case "conf_user_out_pass":
                backObject = SMain.conf_user_out_pass;
                break;
            case "conf_yanzhengma":
                backObject = SMain.conf_yanzhengma;
                break;
            case "conf_loss_limit"://doubel
                backObject = SMain.conf_loss_limit;
                break;
            case "conf_win_limit"://doubel
                backObject = SMain.conf_win_limit;
                break;
            case "conf_tixian_limit"://doubel
                backObject = SMain.conf_tixian_limit;
                break;
            case "conf_api_bout_limit": //int
                backObject = SMain.conf_api_bout_limit;
                break;
            case "conf_buy_double"://string
                backObject = SMain.conf_buy_double;
                break;
            case "conf_rule_list":
                backObject = SMain.conf_rule_list;
                break;
            case "conf_auto_start":
                backObject = SMain.conf_auto_start;
                break;
            case "conf_auto_stop":
                backObject = SMain.conf_auto_stop;
                break;
            case "conf_100_clear":
                backObject = SMain.conf_100_clear;
                break;
            case "conf_limit_game_num":
                backObject = SMain.conf_limit_game_num;
                break;
            case "info_format_money"://doubel
                backObject = SMain.info_format_money;
                break;
            case "info_loss_money"://doubel
                backObject = SMain.info_loss_money;
                break;
            case "info_win_money"://doubel
                backObject = SMain.info_win_money;
                break;

            case "info_use_domain":
                backObject = SMain.info_use_domain;
                break;
            case "info_user_now_money":
                backObject = SMain.info_user_now_money;
                break;
            case "info_user_log_money":
                backObject = SMain.info_user_log_money;
                break;
            case "info_buy_double":
                backObject = SMain.info_buy_double;
                break;
            case "info_user_name":
                backObject = SMain.info_user_name;
                break;
            case "info_bank_number":
                backObject = SMain.info_bank_number;
                break;
            case "info_bank_info":
                backObject = SMain.info_bank_info;
                break;
            case "info_email":
                backObject = SMain.info_email;
                break;
            case "info_heart_jump":
                backObject = SMain.info_heart_jump;
                break;
            case "line_next_number":
                backObject = SMain.line_next_number;
                break;
            case "line_next_status":
                backObject = SMain.line_next_status;
                break;
            case "line_td_1":
                backObject = SMain.line_td_1;
                break;
            case "line_td_2":
                backObject = SMain.line_td_2;
                break;
            case "line_td_3":
                backObject = SMain.line_td_3;
                break;
            case "line_td_4":
                backObject = SMain.line_td_4;
                break;
            case "line_td_5":
                backObject = SMain.line_td_5;
                break;
            case "line_ball_1":
                backObject = SMain.line_ball_1;
                break;
            case "line_ball_2":
                backObject = SMain.line_ball_2;
                break;
            case "line_ball_3":
                backObject = SMain.line_ball_3;
                break;
            case "line_ball_4":
                backObject = SMain.line_ball_4;
                break;
            case "line_ball_5":
                backObject = SMain.line_ball_5;
                break;
            //------------------------
            //单通道倍数
            case "line_td1_bei":
                backObject = SMain.line_td1_bei;
                break;
            case "line_td2_bei":
                backObject = SMain.line_td2_bei;
                break;
            case "line_td3_bei":
                backObject = SMain.line_td3_bei;
                break;
            case "line_td4_bei":
                backObject = SMain.line_td4_bei;
                break;
            case "line_td5_bei":
                backObject = SMain.line_td5_bei;
                break;
            default:
        }

        FindObject = backObject;

        return this;

    }

    /**
     * 获得控件的Text 文本
     * <br>支持 文本框 密码框 Label 和下拉框
     *
     * @return text 属性的字符串
     */
    public String GetString() {

        String back_string = "";

        if (FindObject == null) {
            return back_string;
        }
        if (FindObject.getClass() == JTextField.class) {
            JTextField use_key = (JTextField) FindObject;
            back_string = use_key.getText();
        }
        if (FindObject.getClass() == JLabel.class) {
            JLabel use_key = (JLabel) FindObject;
            back_string = use_key.getText();
        }
        if (FindObject.getClass() == JPasswordField.class) {
            JPasswordField use_key = (JPasswordField) FindObject;
            back_string = String.valueOf(use_key.getPassword());
        }
        if (FindObject.getClass() == JComboBox.class) {
            JComboBox use_key = (JComboBox) FindObject;
            back_string = use_key.getSelectedItem().toString();
        }

        return back_string;
    }

    /**
     * 返回text 的 double 类型
     * <br>自动转换,文本框为空的话 返回0.0
     *
     * @return
     */
    public double GetDouble() {
        String back_string = GetString();
        back_string = (back_string.equals("") ? "0" : back_string);
        return Double.valueOf(back_string);
    }

    /**
     * 返回text 的 int 类型 ,为空则返回 0
     *
     * @return
     */
    public int GetInt() {
        String back_string = GetString();
        back_string = (back_string.equals("") ? "0" : back_string);
        return Integer.valueOf(back_string);
    }

    /**
     * 获得控件是否被选中
     * <br>isselect 属性的返回
     *
     * @return
     */
    public boolean GetIsSelect() {
        boolean back_boolean = false;
        if (FindObject == null) {
            return false;
        }
        if (FindObject.getClass() == JRadioButton.class) {
            JRadioButton use_key = (JRadioButton) FindObject;
            back_boolean = use_key.isSelected();
        }
        if (FindObject.getClass() == JCheckBox.class) {
            JCheckBox use_key = (JCheckBox) FindObject;
            back_boolean = use_key.isSelected();
        }
        if (FindObject.getClass() == JButton.class) {
            JButton use_key = (JButton) FindObject;
            back_boolean = use_key.isSelected();
        }
        return back_boolean;
    }

    /**
     * 保存值到对应的控件上
     * <br>根据不同的类型进行不同的更新
     *
     * @param Value
     */
    public void Update(Object Value) {
        //为空不更新
        if (FindObject == null) {
            return;
        }
        if (FindObject.getClass() == JTextField.class) {
            JTextField use_key = (JTextField) FindObject;
            use_key.setText((String) Value);
        }
        if (FindObject.getClass() == JLabel.class) {
            JLabel use_key = (JLabel) FindObject;
            use_key.setText((String) Value);
        }
        if (FindObject.getClass() == JPasswordField.class) {
            JPasswordField use_key = (JPasswordField) FindObject;
            use_key.setText((String) Value);
        }
        if (FindObject.getClass() == JComboBox.class) {
            JComboBox use_key = (JComboBox) FindObject;
            use_key.getSelectedItem().toString();
            use_key.setSelectedItem((String) Value);
        }
        if (FindObject.getClass() == JRadioButton.class) {
            JRadioButton use_key = (JRadioButton) FindObject;
            use_key.setSelected((boolean) Value);
        }
        if (FindObject.getClass() == JCheckBox.class) {
            JCheckBox use_key = (JCheckBox) FindObject;
            use_key.setSelected((boolean) Value);
        }
        if (FindObject.getClass() == JButton.class) {
            JButton use_key = (JButton) FindObject;
            use_key.setSelected((boolean) Value);
        }
    }

}
