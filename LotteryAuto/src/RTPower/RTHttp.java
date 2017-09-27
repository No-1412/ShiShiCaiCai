/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTPower;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 模拟登陆/抓取网页类
 *
 * @author jerry
 */
public class RTHttp {

    private HttpURLConnection Conn;

    private String TextCoding = "utf-8";

    private PrintWriter output = null;
    private BufferedReader input = null;
    private String result = "";
    /**
     * 默认不使用gzip来读
     */
    public boolean GZipStatus = false;
    /**
     * 服务器连接状态码
     */
    public int ServerCode = 0;
    final static int BUFFER_SIZE = 4096;

    /**
     * 传入url
     * <br>准备连接信息
     *
     * @param set_url 完整url
     */
    public RTHttp(String set_url) {
        try {
            URL HttpURL = new URL(set_url);

            Conn = (HttpURLConnection) HttpURL.openConnection();
            // 设置通用的请求属性
            Conn.setRequestProperty("accept", "*/*");
            Conn.setRequestProperty("connection", "Keep-Alive");
            Conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //设置超时时间
            Conn.setConnectTimeout(20000);//连接主机超时时间
            Conn.setReadTimeout(20000);//从主机读取数据超时时间

        } catch (MalformedURLException ex) {
            RTFile.d_error(ex, "创建实例url()失败" + set_url + ex.getMessage());

        } catch (IOException ex) {
            RTFile.d_error(ex, "打开url之间的链接失败" + set_url + ex.getMessage());
        }
    }

    /**
     * 设置编码 默认 utf8
     *
     * @param CoString 编码 utf-8 gb2312 ......
     * @return 实例
     */
    public RTHttp SetCoding(String CoString) {
        TextCoding = CoString;
        return this;
    }

    /**
     * 设置链接headers属性
     *
     * @param key key
     * @param value 值
     * @return 实例
     */
    public RTHttp SetProperty(String key, String value) {
        Conn.setRequestProperty(key, value);
        return this;
    }

    /**
     * 最终的get 请求抓取内容
     *
     * @return 返回字符串的抓取内容
     */
    public String Get() {
        try {
            Conn.connect();

            //读取url响应
            InputStream inputs = Conn.getInputStream();
            if (GZipStatus) {
                inputs = new GZIPInputStream(inputs);
            }
            InputStreamReader inputsr = new InputStreamReader(inputs, TextCoding);
            input = new BufferedReader(inputsr);
            GetResultString();

        } catch (IOException ex) {
            RTFile.d_error(ex, "建立实际链接失败:" + ex.getMessage());
        }
        return result;
    }

    /**
     * post 最终请求抓取内容
     *
     * @param post_data 要传入的post参数,格式为 name=aa&pass=www
     * @return 返回字符串内容
     */
    public String Post(String post_data) {

        Conn.setDoOutput(true);
        Conn.setDoInput(true);

        try {

            //获取输出流
            output = new PrintWriter(Conn.getOutputStream());
            //发送参数
            output.print(post_data);
            output.flush();
 
            //读取url响应
            InputStream inputs = Conn.getInputStream();
            InputStreamReader inputsr = new InputStreamReader(inputs, TextCoding);
            input = new BufferedReader(inputsr);

            GetResultString();

        } catch (IOException ex) {
            RTFile.d_error(ex, "获得输出流错误:" + ex.getMessage());
        }

        return result;
    }

    /**
     * 获取当前链接的cookies 字符串格式
     *
     * @return 返回cookies字符串
     */
    public String GetCookies() {
//        System.err.println("获得请求cookies:" + Conn.getRequestProperty("Cookie"));
        Map<String, List<String>> header = Conn.getHeaderFields();
        List<String> cookieslist = header.get("Set-Cookie");

        if (cookieslist == null) {
            return "";
        }
        String cookies_string = "";
        for (String cstring : cookieslist) {
            cookies_string += cstring + ";";
        }
        return cookies_string;
    }

    /**
     * 获取当前cookies map格式
     *
     * @return
     */
    public HashMap GetCookiesMap() {
        HashMap<String, String> back_map = new HashMap<>();
        Map<String, List<String>> header = Conn.getHeaderFields();
        List<String> cookieslist = header.get("Set-Cookie");
        if (cookieslist == null) {
            return back_map;
        }
        //解析cookies字符串
        for (String cstring : cookieslist) {
//            System.err.println("获取cookies:" + cstring);
            //每列要进行;号的切割循环
            String[] one_cc = cstring.split(";");
            //循环
            for (int i = 0; i < one_cc.length; i++) {
                String one_cookies_string = one_cc[i];
                //再切割
                String[] cookiesq = one_cookies_string.split("=");
                if (cookiesq.length > 1) {
                    back_map.put(cookiesq[0], cookiesq[1]);
                }
            }

        }

        return back_map;
    }

    /**
     * 设置请求连接的 cookies
     *
     * @param cookiesList 传入cookies字符串
     * @return 实例
     */
    public RTHttp SetCookies(String cookiesList) {
        Conn.setRequestProperty("Cookie", cookiesList);
        return this;
    }

    /**
     * 创造cookies
     *
     * @param new_cookiesMap
     * @return
     */
    public String MakeCookies(HashMap new_cookiesMap) {
        String back_cookies_string = "";

        //循环组成新cookies 字符
        Iterator it = new_cookiesMap.keySet().iterator();

        while (it.hasNext()) {
            Object onemap = it.next();
            String key = onemap.toString();
            if (new_cookiesMap.containsKey(key)) {
                String value = new_cookiesMap.get(key).toString();
                back_cookies_string += key + "=" + value + ";";
            }

        }
        return back_cookies_string;
    }

    /**
     * 最终的网络图片抓取
     * <br>可用于验证码的抓取
     *
     * @return 返回byte[] 可以直接写出到控件
     */
    public byte[] GetImage() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            Conn.connect();

            InputStream is = Conn.getInputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int count = -1;
            while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
                outStream.write(data, 0, count);
            }
            data = null;

        } catch (IOException ex) {
            RTFile.d_error(ex, "网络图片抓取失败,错误:" + ex.getMessage());
        }

        return outStream.toByteArray();
    }

    /**
     * 关闭
     */
    public void close() {
        //关闭
        if (input != null) {
            try {
                input.close();
            } catch (IOException ex) {
                RTFile.d_error(ex, "关闭输入流失败:" + ex.getMessage());
            }
        }
        if (output != null) {
            output.close();
        }
        if (Conn != null) {
            Conn = null;
        }
        result = "";
    }

    /**
     * 整理获取获取的内容到公共变量
     */
    private void GetResultString() {
        //获取内容前先获得code
        GetCode();
        String lineString;
        try {
            while ((lineString = input.readLine()) != null) {
                result += lineString;
            }
        } catch (IOException ex) {
            RTFile.d_error(ex, "读取行数据出错:" + ex.getMessage());
        }
    }

    /**
     * 获得服务器状态码
     *
     * @return
     */
    private void GetCode() {
        int server_code = 0;
        try {
            server_code = Conn.getResponseCode();
        } catch (IOException ex) {
            RTFile.d_error(ex, "获取服务器返回状态码失败," + ex.getMessage());
        }
        ServerCode = server_code;

    }
}
