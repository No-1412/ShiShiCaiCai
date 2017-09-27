/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTPower;

import java.util.HashMap;

/**
 * 静态全局变量
 * <br>公共Map
 *
 * @author jerry
 */
public class Data {

    /**
     * 全局变量Map
     */
    public static HashMap AllData = new HashMap();

    /**
     * 设置单个key,一维数组
     *
     * @param key
     * @param value
     */
    public static void SetData(String key, Object value) {

        AllData.put(key, value);

    }

    /**
     * 设置二维数组
     *
     * @param group 组tag
     * @param key key
     * @param value 值
     */
    public static void SetData(String group, String key, Object value) {
        //读出现有map的组名的key
        HashMap get_group = (HashMap) GetData(group);
        //如果没有则先创建组
        if (get_group == null) {
            get_group = new HashMap();
        }
        get_group.put(key, value);

        SetData(group, get_group);

    }

    /**
     * 读取一维数组
     *
     * @param key
     * @return Object 需要自己强制转换
     */
    public static Object GetData(String key) {
        if(!AllData.containsKey(key)){
            return "";
        }
        Object get_data = AllData.get(key);
        return get_data;
    }

    public static Object GetData(String group, String key) {
        HashMap get_data = (HashMap) AllData.get(group);
        Object back_data = get_data.get(key);
        return back_data;
    }
    
 

}
