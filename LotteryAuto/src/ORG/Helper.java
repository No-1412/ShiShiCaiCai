/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ORG;

import RTPower.RTFile;

/**
 * 助手方法类
 *
 * @author jerry
 */
public class Helper {

    public  String UserFileName;
    public  String ConfigPath;

    public  void SetConf(String user_file_nameString) {

        UserFileName = user_file_nameString;
        ConfigPath = "." + RTFile.FG + "config" + RTFile.FG + UserFileName + ".txt";

    }

    /**
     * 快捷读取配置文件的方法
     * <br>文件不存在会自动创建配置文件
     *
     * @param Key key
     * @return 字符串 "" 为没有找到活没有值
     */
    public  String GetConfKey(String Key) {
        return RTFile.ReadJsonFileKey(ConfigPath, Key);
    }

    /**
     * 快捷设置配置文件新值
     * <br>文件不存在会自动创建配置文件
     * <br>一般都会强制写出 以及覆盖原来有的同名key
     *
     * @param Key key
     * @param Value 值 string 传入"" 相当于删除了 key
     * @return true 成功 false 失败
     */
    public  boolean SetConfKey(String Key, String Value) {
        return RTFile.WriteJsonFileKey(ConfigPath, Key, Value);
    }

 

}
