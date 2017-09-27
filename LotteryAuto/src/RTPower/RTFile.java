/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTPower;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 文件读写类
 *
 * @author
 */
public class RTFile {

    /**
     * 路径分隔符
     */
    public static String FG;
    /**
     * 换行符回车符
     */
    public static String br;

    static {
        FG = System.getProperty("file.separator");
        br = System.getProperty("line.separator");
    }

    /**
     * 检查文件存在与否,不存在给予创建
     *
     * @param FilePath 文件路径
     * @return 文件结果 true 存在或创建成功 false失败
     */
    public static boolean Create(String FilePath) {
        boolean back = false;
        File file = new File(FilePath);
        if (file.exists()) {
            return true;// 文件存在
        } else {
            try {
                back = file.createNewFile();

            } catch (IOException ex) {
                d_error(ex, "创建新文件失败"+FilePath);
            }
        }

        return back;
    }

    /**
     * 读取文件内容
     *
     * @param FilePath 文件路径
     * @return 文本内容 出错为空
     */
    public static String Read(String FilePath) {
        Create(FilePath);
        String result = "";
        try {
            FileReader file = new FileReader(FilePath);
            BufferedReader buff = new BufferedReader(file);
            String line = null;
            while ((line = buff.readLine()) != null) {
                result += line + "\r\n";
            }
        } catch (FileNotFoundException ex) {
            d_error(ex, "文件不存在");
            return "";
        } catch (IOException ex) {
            d_error(ex, "io 错误");
            return "";
        }

        return result;
    }

    /**
     * 向文件写入内容
     *
     * @param FilePath 文件路径
     * @param NewData 文件写入内容
     * @param keep 是否是追加模式 true false是不追加
     * @return 返回成功结果 true false
     */
    public static boolean Write(String FilePath, String NewData, boolean keep) {
        boolean back = false;
        //换行符
        Create(FilePath);
        if (keep) {
            NewData = NewData + br;
        }
        try {
            FileOutputStream file_out = new FileOutputStream(FilePath, keep);
            file_out.write(NewData.getBytes());
            file_out.close();
            back = true;
        } catch (FileNotFoundException ex) {
            d_error(ex, "");
         
        } catch (IOException ex) {
            d_error(ex, "");
        
        }
        return back;
    }

    /**
     * 写出文件日志记录
     * <br>追加写出文件内容
     * <br>自动在当前目录建立log目录并存放 日志文件,日志文件 以日期命名
     *
     * @param LogString
     * @return
     */
    public static boolean d(String LogString) {

        //检查文件和路径存在不存在
        String FileDir = "." + FG + "Log" + FG;
        //检查目录存在与否
        CreateDirectory(FileDir);
        //组合log的文件名
        String FileName = FileDir + RTdate.StempToTime(RTdate.GetNowStemp(), "yyyy-MM-dd") + ".txt";

        //附加内容的时间
        String LogData = RTdate.StempToTime(RTdate.GetNowStemp(), "yyyy-MM-dd HH:mm:ss") + "       " + LogString;
        //写出日志内容
        System.err.println(LogData);
        boolean write_result = Write(FileName, LogData, true);
        return write_result;
    }

    /**
     * 仅仅打印指定格式的 系统消息输出
     *
     * @param LogString
     */
    public static void d_out(String LogString) {
        //附加内容的时间
        String LogData = RTdate.StempToTime(RTdate.GetNowStemp(), "yyyy-MM-dd HH:mm:ss") + "       " + LogString;
        //写出日志内容
        System.err.println(LogData);
    }

    /**
     * 记录系统代码错误详细信息,并打印以及写出log
     *
     * @param ex
     * @param msg
     */
    public static void d_error(Exception ex,String msg) {
        String sOut = ex.getLocalizedMessage();
        StackTraceElement[] trace = ex.getStackTrace();
        for (StackTraceElement s : trace) {
            sOut += "\tat " + s + br;
        }

        d(msg+br+sOut);
    }

    /**
     * 检查目录,不存在自动创建
     *
     * @param Directory 目录名
     * @return true 成功
     */
    public static boolean CreateDirectory(String Directory) {
        File dirname = new File(Directory);
        if (!dirname.isDirectory()) {
            //目录不存在
            dirname.mkdir();
        }
        return true;

    }

    /**
     * 写出Hash到json文件中
     *
     * @param FilePath 文件路径
     * @param json_map HashMap
     * @return false true
     */
    public static boolean WriteJsonFile(String FilePath, HashMap json_map) {
        //写入json内容到文本
        Gson json = new Gson();
        //把内容转为string
        String json_string = json.toJson(json_map);
        //保存到指定路径
        boolean result = Write(FilePath, json_string, false);

        return result;
    }

    /**
     * 读取文件中的json并以Hash 返回
     *
     * @param FilePath 文件路径
     * @return HashMap
     */
    public static HashMap ReadJsonFile(String FilePath) {

        String File_data = Read(FilePath);
        //读取json文本内容
        Gson json = new Gson();
        HashMap back = json.fromJson(File_data, HashMap.class);
        if (back == null) {
            return new HashMap();
        }
        return back;
    }

    /**
     * 读取Json文件内指定的Key
     * <br>所有的返回值都是 String,
     * <br>并且如果没有找到key,则会返回字符串的 ""
     * <br>在得到key值后自行转换
     *
     * @param FilePath json文件路径
     * @param Key 要查找的key
     * @return String 字符串,没有找到返回字符串 ""
     */
    public static String ReadJsonFileKey(String FilePath, String Key) {

        //先读取文件,得到json字符串
        HashMap back_json = ReadJsonFile(FilePath);
        //不为空,则寻找key
        if (!back_json.containsKey(Key)) {
            return "";
        }
        //如果找到了 转为string 返回
        String back_string = back_json.get(Key).toString();
        return back_string;
    }

    /**
     * 向指定的文件内写入json 新key 和值
     * <br>如果存在老的,自动覆盖
     *
     * @param FilePath
     * @param Key
     * @param Value
     * @return
     */
    public static boolean WriteJsonFileKey(String FilePath, String Key, String Value) {

        //先读出文件
        HashMap back_json = ReadJsonFile(FilePath);

        //写入新key
        back_json.put(Key, Value);
        //再进行保存
        boolean write_back = WriteJsonFile(FilePath, back_json);
        return write_back;
    }
}
