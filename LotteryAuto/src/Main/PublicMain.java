/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import java.awt.Color;
import javax.swing.ImageIcon;

/**
 * 所有窗体统一父类
 * 
 * @author jerry
 */
public class PublicMain extends javax.swing.JFrame {
    
    public PublicMain() {
        //加载logo
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/ico_logo.png"));
        this.setIconImage(icon.getImage());
        //设置标题
        this.setTitle("LotteryAuto V3");
        //设置整体风格为灰色
        this.setBackground(Color.LIGHT_GRAY);
        this.getContentPane().setBackground(Color.LIGHT_GRAY);
    }
}
