/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auto;

import Main.StartMain;
import RTPower.RTFile;
import RTPower.RTHttp;
import RTPower.RTMail;
import RTPower.RTdate;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.ImageIcon;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author jerry
 */
public class LotteryAuto_HHGJ extends PublicAuto {

    public LotteryAuto_HHGJ(StartMain main_this) {
        super(main_this);

    }

    @Override
    public void GetYanzhengma() {
        if (WebDoMain.equals("")) {
            return;
        }
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/tpl/commonFile/images/gdpic/macpic.php?SR=16318f30487b67ee7af4");
        MainThis.conf_yanzhengma_image.setIcon(new ImageIcon(Http.GetImage()));
        Http.close();

    }

    @Override
    protected boolean Login() {
        //开始登陆

        //获取验证码
        String yanzhengma = Conf("conf_yanzhengma").GetString();
        if (yanzhengma.equals("")) {
            MainMsg("<font color=red>验证码不能为空!</font>", true);
            return false;
        }

        //检查用户名和密码不能为空
        String user_name = Conf("conf_user_name").GetString();
        String user_pass = Conf("conf_user_pass").GetString();
        if (user_name.equals("") || user_pass.equals("")) {
            MainMsg("<font color=red>用户名和密码不能为空!</font>", true);
            return false;
        }

        //进行登录并获得cookies
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/infe/login/login");
        //整理表单项
        String post_data = "username=" + user_name
                + "&passwd=" + user_pass
                + "&SS=9699ba6d49dc3cccf4f993272abbde47&SR=16318f30487b67ee7af4&TS=1496580972"
                + "&rmNum=" + yanzhengma;
        String result = Http.Post(post_data);
        if (result.equals("")) {
            MainMsg("<font color=red>错误:连接页面出错,没有获得数据.!</font>", true);
            Http.close();
            return false;
        }

        //登录成功后的信息为document.logInForm.submit
        if (result.contains("0.5")) {
            //等待0.5分钟后再登录
            MainMsg("<font color=red>错误:登录太快,请-.5分钟后再进行登录!</font>", true);
            Http.close();
            return false;
        }
        if (result.contains("帐号/密码错误，请重新输入")) {
            //等待0.5分钟后再登录
            MainMsg("<font color=red>错误:帐号/密码错误，请重新输入!</font>", true);
            Http.close();
            return false;
        }
        if (result.contains("验证码")) {
            MainMsg("<font color=red>错误:输入验证码错误!</font>", true);
            Http.close();
            return false;
        }
        //获得页面的额cookies
        HashMap Cookie_Map = Http.GetCookiesMap();
        //对初次的cookies进行处理,泊村在全局变量里
        ScanEditCookies(Cookie_Map);
        Http.close();

        //登录成功后 还要获取一个phpsession 最重要的
        boolean back_get_phpsession = GetPhpSessionInfo();
        if (!back_get_phpsession) {
            MainMsg("<font color=red>错误:获取phpsession失败.!</font>", true);
            return false;
        }

        MainMsg(user_name + "登录成功!", false);

        //激活心跳之前,请求一次,用户的账号信息
        //获得用户名 邮箱,银行账号,银行开户行 等信息,记录,并且显示到界面上
        //银行信息 在提现的时候 需要给出银行卡的 省市
        boolean back_bank = GetOneUserBankAndInfo();
        if (!back_bank) {
            MainMsg("<font color=red>错误:获取用户账号与银行信息失败!</font>", true);
            return false;
        }

        return true;
    }

    @Override
    protected void GetApiInfo() {

        //检查是否是可以运行的状态,一下情况之一都不允许往下走
        //1 总开关关闭  2 不再开奖时间范围 (自动购买状态 仅约束 真正下注方法)
        if (AutoStatus == false || !ScanTimeToStart()) {
            return;
        }

        //开始监听Api页面
        String api_url = "http://api.hqylcn.com/BoutApi/Get?vs=3"
                + "&rule_tag=" + Conf("conf_rule_list").GetString()
                + "&clear_money="
                + Conf("conf_api_bout_limit").GetString()
                + "&buy_bei=" + Conf("conf_buy_double").GetString();
        RTHttp Http = new RTHttp(api_url);
        String result = Http.Get();
        Http.close();
        //返回为空,则给出消息通知
        if (result.equals("")) {
            MainMsg("链接官网API失败,没有获得数据,稍后再试......", false);
            RTFile.d("链接官网API失败,没有获得数据,稍后再试......" + result);
            return;
        }

        //解析为hash
        Gson json = new Gson();

        HashMap json_hash;
        try {
            json_hash = json.fromJson(result, HashMap.class);
        } catch (Exception e) {
            MainMsg("错误:APIjson解析错误,稍后再试......", false);
            RTFile.d_error(e, "错误:API接口json解析错误" + result);
            return;
        }
        if (!json_hash.containsKey("status")) {
            MainMsg("官网API没有获取status 信息,等待下次链接......", false);
            return;
        }

        String api_status = json_hash.get("status").toString(); //API状态
        //如果返回值不存在或不是ok 则 终止,不然是没有数据的
        if (!api_status.equals("Ok")) {
            Conf("line_next_status").Update(api_status);
            RTFile.d_out("官网API返回status[" + api_status + "],等待下次链接......");
            return;
        }

        //到这里 hash 正常,就全部解析出来所有要用的数据,然后再做其他的判断
        //正式获得数据
        String next_number = json_hash.get("numberid").toString();//下一期的期号
        //下一期时间戳
        int next_timestamp = Integer.parseInt(json_hash.get("open_timestamp").toString());

        //获得上一期是否给出clear,
        String new_bout_clear = json_hash.get("clear").toString();
        //如果满足停止条件:超过100期并盈利和是clear 则终止
        if (ScanNewBoutClear(next_number, new_bout_clear)) {
            return;
        }

        //获得5通道的url
        String[] buyurl_fivemoneys = GetFiveTDBuyUrl(next_number, json_hash);

        //购买金额总计
        double buy_money_count = Double.valueOf(buyurl_fivemoneys[6]);

        //检查当前时间是否到了可以下注的时间,没有到,则禁止往下走
        if (!ScanNumberTimeStatus(next_timestamp)) {
            return;
        }

        //如果自动购买是关闭的,就在这里终止继续
        if (!GetAutoBuyStatus()) {
            return;
        }
        //如果期号的5个通道数据有一个在 则不再进行下去
        if (DealListMap.containsKey(next_number + "_1")
                || DealListMap.containsKey(next_number + "_2")
                || DealListMap.containsKey(next_number + "_3")
                || DealListMap.containsKey(next_number + "_4")
                || DealListMap.containsKey(next_number + "_5")) {

            return;
        }

        //没有下注信息,则禁止下注并增加此单的不检测
        if (buyurl_fivemoneys[0].equals("")) {
            //增加历史下注信息,防止继续往下走
            DealListMap.put(next_number + "_1", "no_buy_url");
            MainMsg("<font color=red>提示:此期[" + next_number + "]不符合算法或配置"
                    + ",故全通道不参与下注,请等待下期!</font>", true);
            RTFile.d("提示:此期[" + next_number + "]不符合算法或配置"
                    + ",故全通道不参与下注,请等待下期!");
            return;
        }
        //此次购买后的月不能低于 输停线,否则就终止
        //确保严格不低于输停

        if ((NowUseMoney - buy_money_count) < Conf("info_loss_money").GetDouble()) {
            DealListMap.put(next_number + "_1", "buy_money_big_loss_money");
            MainMsg("<font color=red>提示:此期[" + next_number + "]下注总额" + buy_money_count + "元,"
                    + "如果下注剩余资金会低于输停线" + Conf("info_loss_money").GetDouble() + ","
                    + "所以此期跳过!</font>", true);
            RTFile.d("提示:此期[" + next_number + "]下注总额" + buy_money_count + "元,"
                    + "如果下注剩余资金会低于输停线" + Conf("info_loss_money").GetDouble() + ","
                    + "所以此期跳过!");
            return;
        }

        //提交给下注方法
        //显示界面的5通道下期下注提示
//        System.err.println("buyurl:期号" + next_number + "/" + buyurl_fivemoneys[0]);
        //开始进行下一期的购买方法
        GoNextBuyStart(next_number, buy_money_count, buyurl_fivemoneys);
    }

    @Override
    public void AutoTixian() {
        //非运行状态不操作
        if (!AutoStatus) {
            MainMsg("非运行状态下,无法提现.", false);
            return;
        }
        //提现参数没有配置则不运行
        if (Conf("conf_tixian_limit").GetDouble() <= 0) {
            MainMsg("提现保留额配置为0元或小于,不激活自动提现操作.", false);
            return;
        }

        //得出提现可用额度
        int tixian_money = (int) (NowUseMoney - Conf("conf_tixian_limit").GetDouble());
        if (tixian_money <= 0) {
            RTFile.d("可提现的超出部分低于0元,不进行提现,当前金额:" + NowUseMoney);
            MainMsg("可提现的超出部分低于0元,不进行提现,当前金额:" + NowUseMoney, false);
            return;
        }
        //提现密码没有设置不提
        if (Conf("conf_user_out_pass").GetString().equals("")) {
            MainMsg("提现密码为空,不进行提现.", false);
            return;
        }

        //银行市区信息不存在,页不允许
        if (!UserInfoMap.containsKey("bank_pro")
                || !UserInfoMap.containsKey("bank_city")) {
            RTFile.d("没有获得银行地区信息,不能提现.");
            MainMsg("没有获得银行地区信息,不能提现.", false);
            return;
        }
        //正式发起提现post
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/app/member/get_money2_new.php?langx=zh-cn");

        //这一步要开始提交cookies了,直接使用登录时已经创建的 全局cookies
        String cookiesList = Http.MakeCookies(CookiesMap);
        Http.SetCookies(cookiesList);
        String post_data = "uid=" + CookiesMap.get("SESSION_ID").toString()
                + "&act=new"
                + "&password=" + Conf("conf_user_out_pass").GetString()
                + "&CASH=" + tixian_money
                + "&COM=0"
                + "&REAL_CASH=" + tixian_money
                + "&province=" + UserInfoMap.get("bank_pro").toString()
                + "&city=" + UserInfoMap.get("bank_city").toString();
        String result_data = Http.Post(post_data);
        Http.close();
//        System.err.println(result_data);
        //提现结果输出
        if (result_data.contains("alert")) {
            String[] result_data_q = result_data.split("alert\\(");
            String[] result_data_r = result_data_q[1].split("\\)");

            MainMsg("提现失败结果:" + result_data_r[0], false);
            RTFile.d("提现失败结果:" + result_data_r[0]);
        } else {
            MainMsg("提现成功!提出" + tixian_money + "元", false);
            RTFile.d("提现成功!提出" + tixian_money + "元,原有金额:" + NowUseMoney);
            //提现成功了,
            RTMail.Send(UserInfoMap.get("email").toString(), "系统进行了自动提现,提出了" + tixian_money + "元", "资金总额:" + NowUseMoney + "元,提出:" + tixian_money + "元,"
                    + "剩余:" + Conf("conf_tixian_limit").GetDouble() + "元");
            //要改变记录的资金,否则会影响12点点额初始化.
            NowUseMoney = Conf("conf_tixian_limit").GetDouble();//因为多余的全被提出,只剩提现保留金额

        }

    }

    @Override
    protected boolean KeepHeartJump() {

        //心跳页面前,要进行 用户资金信息的获取,以及历史交易的记录
        //使用私有方法,没有抓取到信息或错误 就直接终止运行
        if (!GetUserData()) {
            return false;
        }
        if (!GetBetList()) {
            return false;
        }
        //保持心跳,最终的检测,直接返回结果
        if (HeartJump()) {
            //更新心跳数据到界面
            Conf("info_heart_jump").Update(RTdate.GetNowTime("yyyy-MM-dd HH:mm:ss"));
            //写出控制台消息
            RTFile.d_out(WebDoMain + "线程值守成功...,目前自动下注状态为:" + GetAutoBuyStatus()
                    + "100回合:" + Conf("conf_100_clear").GetIsSelect());
            return true;
        } else {
            return false;
        }

    }

    /**
     * 自动对传入的新cookies进行处理
     * <br>组装成通用的,保存在全局变量,供所有页面使用
     * <br>一定最后使用
     *
     * @param get_cookies
     */
    @Override
    protected void ScanEditCookies(HashMap get_cookies) {

        //此处是通用的,必须要有的
        if (get_cookies.containsKey("LOGINCHK")) {
            CookiesMap.put("LOGINCHK", get_cookies.get("LOGINCHK").toString());
        }
//        new_cookies_list.put("domain", CookieMap.get("domain").toString());
        if (get_cookies.containsKey("BBSESSID")) {
            CookiesMap.put("BBSESSID", get_cookies.get("BBSESSID").toString());
        }
        if (get_cookies.containsKey("BBSESSID")) {
            CookiesMap.put("SESSION_ID", get_cookies.get("BBSESSID").toString());
        }
        if (get_cookies.containsKey("IBCACHE")) {
            CookiesMap.put("IBCACHE", get_cookies.get("IBCACHE").toString());
        }
        CookiesMap.put("charset", "zh-cn");
        //以下是特殊存在的
        if (get_cookies.containsKey("nsk_webver")) {
            CookiesMap.put("nsk_webver", get_cookies.get("nsk_webver").toString());
        }
        if (get_cookies.containsKey("PHPSESSID")) {
            CookiesMap.put("PHPSESSID", get_cookies.get("PHPSESSID").toString());
        }
        if (get_cookies.containsKey("mfid")) {
            CookiesMap.put("mfid", get_cookies.get("mfid").toString());
            CookiesMap.put("lt_mf-id", get_cookies.get("mfid").toString());

        }
    }

    //==========================================================================
    //私有方法
    //==========================================================================
    /**
     * 登陆后立即获得一次phpsession
     * <br>主要是下注页面需要使用
     *
     * @return
     */
    private boolean GetPhpSessionInfo() {
        RTHttp Http = new RTHttp("http://lt." + WebDoMain + "/vender.php?lang=zh-cn&referer_url=/pt/mem/order/CQSC");

        //这一步要开始提交cookies了,直接使用登录时已经创建的 全局cookies
        String cookiesList = Http.MakeCookies(CookiesMap);
        Http.SetCookies(cookiesList);

        String result = Http.Get();
        //获得页面的cookies
        HashMap cookies_list = Http.GetCookiesMap();
        //获取成功后,处理cookies
        ScanEditCookies(cookies_list);
        Http.close();
        //没有返回数据 就终止
        if (result.equals("")) {
            MainMsg("<font color=red>错误:连接页面出错,没有获得数据.!</font>", true);
            return false;
        }

        //判断唯一的正常情况
        if (result.contains("location.href")
                && result.contains("/pt/mem/order/CQSC")) {
            RTFile.d("刷新lt 页面的 phpsession 成功!");
            return true;
        }
        MainMsg("<font color=red>错误:PHPSESSION页面返回数据错误!请看日志</font>", true);
        RTFile.d("错误:PHPSESSION页面返回数据错误!" + result);
        return false;
    }

    /**
     * 仅仅登录后获得一次用户的email 和银行信息
     * <br>显示界面以及记录在用户的配置map里
     */
    private boolean GetOneUserBankAndInfo() {
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/infe/macenter/account/memberdatacontroller/getinfo.json");
        //这一步要开始提交cookies了,直接使用登录时已经创建的 全局cookies
        String cookiesList = Http.MakeCookies(CookiesMap);
        Http.SetCookies(cookiesList);

        String result = Http.Get();
        Http.close();
        if (result.equals("")) {
            MainMsg("<font color=red>错误:获取email和银行信息错误,没有获得数据.!</font>", true);
            return false;
        }
        if (!ScanWebCloseStatus(result)) {
            return false;
        }

        HashMap json_hash;
        //整理要获取的信息,解析json
        Gson json = new Gson();
        try {
            json_hash = json.fromJson(result, HashMap.class);
        } catch (Exception e) {
            MainMsg("<font color=red>错误:解析用户存款信息界面的json出错!</font>", true);
            RTFile.d_error(e, "错误:解析用户存款信息界面的json出错!" + result);
            return false;
        }

        //获得用户名 email 银行账号,银行 市区
        String username = json_hash.get("account").toString();

        String email = json_hash.get("email").toString();
        //以下获得银行账号信息,检测有没有,没有则终止
        if (!json_hash.containsKey("usual")) {
            MainMsg("<font color=red>错误:获取银行信息错误,没有数据.!</font>", true);
            RTFile.d("错误:获取银行信息错误,没有数据.!" + result);
            return false;
        }
        //检查第一个银行配置信息
        ArrayList one_bank = (ArrayList) json_hash.get("usual");
        if (one_bank.size() < 1) {
            MainMsg("<font color=red>错误:您的账号还没有配置银行信息,不能运行!</font>", true);
            RTFile.d("错误:您的账号还没有配置银行信息,不能运行!" + result);
            return false;
        }
        LinkedTreeMap one_bank_json = (LinkedTreeMap) one_bank.get(0);

        String bank_number = one_bank_json.get("bank_account").toString();
        String bank_name = one_bank_json.get("bank_name").toString();
        String bank_city = one_bank_json.get("bank_city").toString();
        String bank_pro = one_bank_json.get("bank_province").toString();

        //保存到UserInfoMap 内
        UserInfoMap.put("user_name", username);
        UserInfoMap.put("email", email);
        UserInfoMap.put("bank_number", bank_number);
        UserInfoMap.put("bank_name", bank_name);
        UserInfoMap.put("bank_city", bank_city);
        UserInfoMap.put("bank_pro", bank_pro);

        //传递请求,刷新界面的配置信息
        Conf("info_user_name").Update(username);
        Conf("info_email").Update(email);
        Conf("info_bank_number").Update(bank_number);
        Conf("info_bank_info").Update(bank_name + bank_pro + bank_city);

        return true;
    }

    /**
     * 心跳,保持彩票页面在线
     *
     * @return
     */
    private boolean HeartJump() {
        RTHttp Http = new RTHttp("http://lt." + WebDoMain + "/pt/mem/order/CQSC");
        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);

        //链接获得返回
        String back_result = Http.Get();
        Http.close();
//返回为空,则给出消息通知
        if (back_result.equals("")) {
            MainMsg("<font color=red>心跳保持失败,没有获得返回数据,稍后再试......</font>", true);
            RTFile.d("心跳保持失败,没有获得返回数据,稍后再试......" + back_result);
            return false;
        }
//        System.err.println(back_result);
        //心跳正常还要判断显示页面的内容是不是 有 <title>重庆时时彩</title> 
        //否则可能心跳也是死的,lt 靠的是 *的session保持联系的,
        //没有重庆时时彩关键字,就可能是死掉了,先记录并通知
        if (!back_result.contains("重庆时时彩")) {
            //如果心跳中不是标准页面,再进行一次phpsession请求,
            //如果还是不行则进行停职和通知
            if (!GetPhpSessionInfo()) {
                MainMsg("<font color=red>心跳保持失败,数据不是标准心跳页面内容,请查看日志!</font>", true);
                RTFile.d("心跳保持失败,返回数据不正确!请看详细返回数据:" + back_result);
                //终止运行
                if (UserInfoMap.containsKey("email")) {
                    RTMail.Send(UserInfoMap.get("email").toString(),
                            "检测到心跳数据出错,系统终止,请查看日志.",
                            "心跳出错!请立刻查看运行日志,系统http请求返回了:" + back_result);
                }

                Stop();//停止系统
                return false;
            }
            //否则就给他通过,因为phpsession页面重新刷新了,并且通过

        }
        return true;
    }

    /**
     * 获得用户的目前存款,以及今日交易额
     *
     * @return
     */
    private boolean GetUserData() {
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/infe/macenter/record/cashrecordcontroller/getcashrecord.json");
        //还有个区别是 这个是xml
        Http.SetProperty("Content-Type", "application/json");
        Http.SetProperty("origin", "http://" + WebDoMain + "");
        Http.SetProperty("Host", "hui0999.com");
        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        //链接获得返回
        String back_result = Http.Get();
        Http.close();
        //返回为空,则给出消息通知
        if (back_result.equals("")) {
            MainMsg("<font color=red>错误:抓取用户信息页面为空,稍后再试......</font>", true);
            RTFile.d("错误:抓取用户信息页面为空,稍后再试......");
            return false;
        }
        //这里会遇到网站更新的时候,页面html 是<script>	location.href = '/';</script>
        //遇到这样的额情况需要停止程序,并立刻邮件通知
        if (!ScanWebCloseStatus(back_result)) {
            return false;
        }
        //把结果转成json
        Gson json = new Gson();
        HashMap json_list;
        try {
            json_list = json.fromJson(back_result, HashMap.class);
        } catch (Exception e) {
            MainMsg("<font color=red>错误:用户信息json解析错误,稍后再试......</font>", true);
            RTFile.d_error(e, "错误:用户信息json解析错误" + back_result);
            return false;
        }

        //json 空则失败
        if (json_list.isEmpty()) {
            RTFile.d("错误:用户json为空" + back_result);
            return false;
        }

        //检查状态,是不是要重新登录
        if (json_list.containsKey("status")) {
            if (json_list.get("status").toString().equals("N")) {
                MainMsg("<font color=red>终止:网站提示账号需要重新登录,程序停止运行</font>", true);
                RTFile.d("终止:网站提示账号需要重新登录,程序停止运行" + back_result);
                //这里应该发送一封邮件通知
                RTMail.Send(UserInfoMap.get("email").toString(), "终止:网站提示账号需要重新登录,程序停止运行", RTdate.GetNowTime("yyyy-MM-dd HH:mm:ss") + "被终止运行,请赶紧登陆查看日志.");
                Stop();//停止系统
                return false;
            }
        }
        //没有找到key 页终止
        if (!json_list.containsKey("MAXCREDIT")) {

            MainMsg("<font color=red>错误:json没有找到用户存款信息,稍后再试......</font>", true);
            RTFile.d("错误:json没有找到用户存款信息" + back_result);
            return false;
        }

        String user_money = json_list.get("MAXCREDIT").toString();
        //过滤逗号
        user_money = user_money.replace(",", "");
        String today_money = json_list.get("TOTAL").toString();
        today_money = today_money.replace(",", "");
        //记录用户存款数量
        NowUseMoney = Math.floor(Double.parseDouble(user_money));
        NowLogMoney = Math.floor(Double.parseDouble(today_money));

        //刷新主界面存款显示
        Conf("info_user_now_money").Update("" + NowUseMoney);
        Conf("info_user_log_money").Update("" + NowLogMoney);

        String use_buy_double = Conf("conf_buy_double").GetString();
        Conf("info_buy_double").Update(use_buy_double);

//在此处进行资金止损的完整处理
        ScanMoneyStop();
        return true;

    }

    /**
     * 获取辉煌国际交易列表页面数据
     */
    private boolean GetBetList() {
        String now_date = GetBetListHHDate();
        //押注结果界面
        RTHttp Http = new RTHttp("http://" + WebDoMain + "/infe/macenter/record/betrecordcontroller/getskcompletedetail.json?date=" + now_date + "&gtype=CQSC");
        //还有个区别是 这个是xml
        Http.SetProperty("Content-Type", "application/json");
        Http.SetProperty("origin", "http://" + WebDoMain + "");
        Http.SetProperty("Host", "hui0999.com");
        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);

        //链接获得返回
        String back_result = Http.Get();
        Http.close();
//返回为空,则给出消息通知
        if (back_result.equals("")) {
            MainMsg("<font color=red>超时:抓取历史下注页面为空,稍后再试......</font>", true);
            RTFile.d("超时:抓取历史下注页面为空,稍后再试......");
            return false;
        }

        //把结果转成json 获取结果,然后提交给全局的DealHash 记录
        //后期下注的时候,需要进行检测跳过已经下过的记录
        Gson json = new Gson();
        //转译为Hash
        HashMap json_list;

        try {
            json_list = json.fromJson(back_result, HashMap.class
            );
        } catch (Exception e) {
            RTFile.d_error(e, "错误:历史下注json解析错误" + back_result);
            MainMsg("<font color=red>错误:历史下注json解析错误,稍后再试......</font>", true);
            return false;
        }

        //检测其中记录data 也就是列表的 数据
        //这里无法获得全部,因为 辉煌国际采用的是 美国太平洋时间,所以只有在 白天才有数据
        if (json_list.containsKey("data")) {
            ArrayList deal_list = (ArrayList) json_list.get("data");

            Iterator it = deal_list.iterator();

            //循环遍历
            while (it.hasNext()) {
                LinkedTreeMap onedeal = (LinkedTreeMap) it.next();
                String orderid_date = onedeal.get("adddate").toString();//日
                String orderid_time = onedeal.get("addtime").toString();//时间
                String deal_pay = onedeal.get("ispay").toString();//派彩状态

                String deal_money = onedeal.get("amount").toString();//交易金额
                String win_money = onedeal.get("wingold").toString();//盈亏
                String orderid_string = onedeal.get("content").toString();//单号数据

                //截取期号以及其他数据,单号数据中包括了好几个数据,分别截取出来显示
                Document doc = Jsoup.parse(orderid_string);
                Elements font_s = doc.getElementsByTag("font");//以font为节点读取
                String order_qihao = font_s.get(1).text();//期号
                String order_buyString = font_s.get(3).text();//下注信息
                //对获得的期号进行多余字符过滤字符
                order_qihao = order_qihao.replace("第", "");
                order_qihao = order_qihao.replace("期", "");

                //得到押注的位数和结果
                String[] order_buyString_q = order_buyString.split(" ");
                String buy_location = order_buyString_q[0];//押注的通道
                String buy_type = order_buyString_q[1];//押注的单活双
                //完成数据的整理,将数据写出到全局变量

                //已经有在历史列表的,全部加入历史deal记录里
                //循环写出单号的记录到全局变量,方便下面购买时候检测
                //刷新界面的列表
                //获得内容,检查通道存在一样的不,存在就不重复写出
                DealGo(orderid_time, order_qihao,
                        buy_location,
                        buy_type + " " + deal_money,
                        win_money,
                        deal_pay);

            }

            return true;
        }

        RTFile.d("错误:,没有获取历史下注的json中的data key," + back_result);
        return false;

    }

    /**
     * 对资金进行计算,进行止损检测
     * <br>以及检测0点激活,初始化与自动提现
     */
    protected void ScanMoneyStop() {
        //采用 NowStartedDate 来激活刷新
        String now_date = RTdate.GetNowTime("yyyy-MM-dd");
        //======================================================================
        //2个时间不一致,则激活一次刷新初始资金和止损资金
        //默认人工启动程序后 初始化是当日,就不会激活这个激活点,只有程序值守到第二天 才会激活
        if (!NowStartedDate.equals(now_date)) {

            //激活提现,要不等于"" ,代表不是刚运行程序
            if (!NowStartedDate.equals("")) {
                main_deal.setRowCount(0);//每次启动都是清空老数据列,包括激活点的时候
                main_msg.clear();

                AutoTixian();
                //是值守到激活点的话,强制开启 自动下注
                Conf("conf_auto_start").Update(true);

                //每天到了12点后,激活新的计算时,5通道全部打开
                FiveTDBuyStatus = new boolean[]{true, true, true, true, true, true};
                Conf("line_td_1").Update(true);
                Conf("line_td_2").Update(true);
                Conf("line_td_3").Update(true);
                Conf("line_td_4").Update(true);
                Conf("line_td_5").Update(true);

            }
            //清空一次显示交易列表
            //只能在每天12点的时候清理,否则当前期号会检测到没有限制数据,就又下注一次

            DealListMap.clear();//清空记录交易的列表全局HAsh
            ChangeFiveBuyMoney.clear();
            //最后改变时间,防止今日再次刷新
            NowStartedDate = now_date;

            //初始化止损和止盈信息
            double StopLoss = Conf("conf_loss_limit").GetDouble(); //获得止损配置
            double StopWin = Conf("conf_win_limit").GetDouble();//获得止盈配置
            //生成新的3个配置信息
            Conf("info_format_money").Update("" + NowUseMoney);//初始资金就是当前资金
            Conf("info_loss_money").Update("" + (NowUseMoney - StopLoss));
            Conf("info_win_money").Update("" + (NowUseMoney + StopWin));

            MainMsg("提示:新一天,重计止损额.止损:"
                    + (NowUseMoney - StopLoss) + "元,"
                    + "止盈:" + (NowUseMoney + StopWin) + "元,"
                    + "初始:" + NowUseMoney + "元.", false);

            RTFile.d("提示:新一天,重计止损额.止损:"
                    + (NowUseMoney - StopLoss) + "元,"
                    + "止盈:" + (NowUseMoney + StopWin) + "元,"
                    + "初始:" + NowUseMoney + "元.");
        }

        //初始化0点激活 就结束了
        //==============================================================================
        //运行到这来,资金都记录好了,在这里进行资金比对,小于或等于就停止下注
        if (NowUseMoney <= Conf("info_loss_money").GetDouble()
                && GetAutoBuyStatus()) {
            //自动下注关闭
            Conf("conf_auto_stop").Update(true);

            RTMail.Send(UserInfoMap.get("email").toString(), "紧急:系统到达止损线" + Conf("info_loss_money").GetDouble() + "元", "目前资金" + NowUseMoney + "元,已经达到止损线,请立即查看分析.");
            RTFile.d("紧急:系统到达止损线" + Conf("info_loss_money").GetDouble() + "元,目前资金" + NowUseMoney + "元,已经达到止损线,请立即查看分析.");
            MainMsg("提示:到止损线,今日停注."
                    + "止损:" + Conf("info_loss_money").GetString() + "元,"
                    + "当前:" + NowUseMoney + "元.", false);

        }
        //检测止盈
        if (NowUseMoney >= (double) Conf("info_win_money").GetDouble()
                && GetAutoBuyStatus()) {
            //先发送邮件提醒,否则金额就被提现了
            RTMail.Send(UserInfoMap.get("email").toString(), "到达止盈线" + Conf("info_win_money").GetDouble() + "元", "目前资金" + NowUseMoney + "元,已经达到止盈线,系统将自动提现,并停止继续自动下单.");
            RTFile.d("到达止盈线" + Conf("info_win_money").GetDouble() + "元,目前资金" + NowUseMoney + "元,已经达到止盈线,系统将自动提现,并停止继续自动下单.");
//说明赚的钱已经超过初始金额加上的盈利部分,可以终止今天的交易开关了
            AutoTixian();//先自动提现
            //自动关闭自动下注
            Conf("conf_auto_stop").Update(true);
            MainMsg("提示:到赢停线了,今日停注."
                    + "赢停:" + Conf("info_win_money").GetString() + "元,"
                    + "当前:" + NowUseMoney + "元.", false);

        }
    }

    /**
     * 针对辉煌国际的 太平洋时间,来判断要查询的交易记录的日期
     * <br>辉煌国际比北京时间慢12小时,也就是中午12点才是当日,12点前是前一日
     *
     * @return 日期字符串yyyy-MM-dd
     */
    private String GetBetListHHDate() {
        long now_date_stamp = RTdate.GetNowStemp();
        //减去12小时
        now_date_stamp = now_date_stamp - 43200;
        //得出字符串
        String back_time = RTdate.StempToTime(now_date_stamp, "yyyy-MM-dd");
        return back_time;
    }

    /**
     * 扫描是否到了可以下注的时间范围,否则就false
     *
     * @return
     */
    private boolean ScanTimeToStart() {

        //当前时间戳
        long now_time = RTdate.GetNowStemp();
        //获得今日开头的时间戳
        long day_start_time_stamp = RTdate.TimeToStemp(
                RTdate.GetNowTime("yyyy-MM-dd"), "yyyy-MM-dd");

        //得到2点到9点45点的时间戳
        long day_time_2 = day_start_time_stamp + 7200;
        long day_time_945 = day_start_time_stamp + 35100;
        //进行2点到945点的检查,在期间内,就终止
        if (now_time > day_time_2 && now_time < day_time_945) {
            return false;
        }

        return true;
    }

    /**
     * 根据传来的json 具体判断得出5个通道的 购买url
     * <br>同时刷新5个球到界面
     * <br>[0] 是完整的组合url ,其他 1,2,3,4,5 是各自通道的单独购买金额
     *
     * @param json_hash
     * @return
     */
    private String[] GetFiveTDBuyUrl(String next_number, HashMap json_hash) {
        //5通道准备返回url数组
        //第六个是金额统计
        String[] back_strings = new String[]{"", "", "", "", "", "", ""};

        //专门为show line 的球做一个变量
        String[] show_strings = new String[]{"", "", "", "", "", "", ""};
        //show 的单双值
        String[] show_DS_strings = new String[]{"", "", "", "", "", "", ""};
        double temp_count_moneys = 0;
        //循环5次读5个通道数据
        for (int TD = 1; TD <= 5; TD++) {

            //如果TD数据不再则跳出下一个
            if (!json_hash.containsKey("td" + TD)) {
                continue;
            }
//读取数据
            LinkedTreeMap one_buy = (LinkedTreeMap) json_hash.get("td" + TD);

            //如果存在强制下注并且金额和购买的tag都存在,则强制下注
            if(ChangeFiveBuyMoney.containsKey(next_number + "_" + TD+"_status")
                    &&ChangeFiveBuyMoney.containsKey(next_number + "_" + TD+"_buy")
                    &&ChangeFiveBuyMoney.containsKey(next_number + "_" + TD)){
                //直接覆盖下注状态
                one_buy.put("buy_status", ChangeFiveBuyMoney.containsKey(next_number + "_" + TD+"_status"));
            }
            
            //如果状态是false也跳过
            if (one_buy.get("buy_status").toString().equals("false")) {
                continue;
            }

            //如果此期此通道是允许下注的状态,并且检测到有这期这个通道的手动设置
            //则覆盖掉原有数据
            if (ChangeFiveBuyMoney.containsKey(next_number + "_" + TD)) {
                //设置存在,则强制改变
                String new_money = ChangeFiveBuyMoney.get(next_number + "_" + TD).toString();
                one_buy.put("buy_money", new_money);
            }
            //检查是否手工更改了下注结果
            if (ChangeFiveBuyMoney.containsKey(next_number + "_" + TD+"_buy")) {
                //设置存在,则强制改变
                String new_tag = ChangeFiveBuyMoney.get(next_number + "_" + TD+"_buy").toString();
                one_buy.put("buy_tag", new_tag);
            }

            //如果money不为空,则重新修改倍数
            if (!one_buy.get("buy_money").toString().equals("")) {
                double new_one_buy_money = Double.parseDouble(one_buy.get("buy_money").toString());
                new_one_buy_money = new_one_buy_money / Conf("conf_buy_double").GetDouble();
                new_one_buy_money = new_one_buy_money * Conf("line_td" + TD + "_bei").GetDouble();
                one_buy.put("buy_money", (int) new_one_buy_money);
            }

            //这里为界面显示准备专门的数据,
            //这个数据不论通道是否关闭 都会写出提供给界面使用
            show_strings[TD] = one_buy.get("buy_money").toString();
            show_DS_strings[TD] = one_buy.get("buy_tag").toString();

            //在这里进行当前期号和当前通道的检测,如果大于规定的期号,以及资金等于初始下注额
            //就对通道进行关闭,这里关闭通道会影响下面的方法,改变了设置会跳过这个通道
            ScanLimitNumber(next_number, TD, show_strings[TD]);

            //如果通道是关闭的,则不进行组装url
            if (!FiveTDBuyStatus[TD]) {
                continue;//跳到下一个
            }

            //为下单数据准备数组,这里的条件是满足:1 通道状态是true,通道数据是true.json数据存在
            //则为下单准备数据
            back_strings[TD] = one_buy.get("buy_money").toString();

            //获得当前通道mongey
            double temp_one_money = (back_strings[TD].equals("")) ? 0 : Double.valueOf(back_strings[TD]);
            //累加进统计
            temp_count_moneys = temp_count_moneys + temp_one_money;
//组装url,第一个参数是所有的完整url
            back_strings[0] += "&orders[" + TD + ":" + one_buy.get("buy_tag").toString() + "]"
                    + "={\"gold\":"
                    + back_strings[TD] + ",\"odds\":\"1.97\"}";

        }

        //循环结束,写出金额
        back_strings[6] = "" + temp_count_moneys;
        //刷新界面数据
        REMainFiveTdBuyMoney(next_number, show_strings,show_DS_strings);
        return back_strings;
    }

    /**
     * 根据数组刷新界面5个球的值和期号
     * <br>要刷的期号与界面一致就不重复刷
     *
     * @param next_number
     * @param five_moneys
     */
    private void REMainFiveTdBuyMoney(String next_number, String[] five_moneys,String[] five_DS) {
        
        //如果期号等于传入的期号 就不重复刷新界面
        if (next_number.equals(Conf("line_next_number").GetString())) {
            return;
        }
        //写出5个通道的数据,空的就--
        for (int TD = 1; TD <= 5; TD++) {
            String one_money = (five_moneys[TD].equals("")) ? "--" : five_moneys[TD];
            String one_DS=five_DS[TD].replace("ODD", "单").replace("EVEN", "双");
            
            Conf("line_ball_" + TD).Update(one_DS+one_money);
        }

        Conf("line_next_number").Update(next_number);

    }

    /**
     * 检查当前时间戳和下一期的时间戳
     * <br>没有到时间则false 并帅新剩余秒数
     * <br>到了时间则 true 并显示 下注中...
     *
     * @param next_timestamp
     * @return
     */
    private boolean ScanNumberTimeStatus(int next_timestamp) {
        long now_time = RTdate.GetNowStemp();
        int next_timestamp_start = next_timestamp - 270;
        //下一期提前270秒,大于当前时间,说明还没有到下注时间
        if (next_timestamp_start > now_time) {
            Conf("line_next_status").Update("等待 " + (next_timestamp_start - now_time) + " 秒");
            return false;
            //如果减掉270秒,已经小于当前时间了,说明到了下注时间,
            //这时候就检查 最终截止时间是不是大于当前30秒,就还在下注时间范围
            //否则就是要下一期了
        } else if ((next_timestamp - now_time) > 30) {
            Conf("line_next_status").Update("下注中(" + (next_timestamp - now_time) + "秒)");
        } else {
            Conf("line_next_status").Update("停止下注.");
            return false;
        }

        return true;
    }

    /**
     * 开始进行下注操作
     * <br>如果开关是关闭的则不进行下注
     *
     * @param next_number
     * @param buyStrings
     */
    private void GoNextBuyStart(String next_number, double buy_money_count, String[] buyStrings) {
        //总开关关闭了,自动购买开关关闭了 都不在往下进行
        if (!GetAutoBuyStatus() || !AutoStatus) {
            return;
        }
        //开始下注请求
        RTHttp Http = new RTHttp("http://lt." + WebDoMain + "/pt/mem/ajax/order/CQSC");

        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        //设置下注数据
        String post_data = "game=CQSC&game_num=" + next_number + buyStrings[0];
//        String post_data = "game=CQSC&game_num=" + next_number + "&orders[1:ODD]={\"gold\":5,\"odds\":\"1.97\"}";

        String back_result = Http.Post(post_data);
//        System.err.println(back_result);
        Http.close();

        //没有获得返回数据则终止
        if (back_result.equals("")) {
            MainMsg("<font color=red>" + next_number + "期下注失败,返回结果为空,稍后再试......</font>", true);
            //记录页面
            RTFile.d(next_number + "期下注失败,返回结果为空,稍后再试......" + back_result);
            return;
        }

        //检测下注结果
        Document doc = Jsoup.parse(back_result);
        String doc_msg = doc.getElementsByTag("msg").text();

        //检查成功标识符,否则都为失败,记录错误显示错误
        if (doc_msg.contains("重庆时时彩")) {
            String buy_msg = "";
            if (!buyStrings[1].equals("")) {
                buy_msg += "[通道1:" + buyStrings[1] + "元]";
            }
            if (!buyStrings[2].equals("")) {
                buy_msg += "[通道2:" + buyStrings[2] + "元]";
            }
            if (!buyStrings[3].equals("")) {
                buy_msg += "[通道3:" + buyStrings[3] + "元]";
            }
            if (!buyStrings[4].equals("")) {
                buy_msg += "[通道4:" + buyStrings[4] + "元]";
            }
            if (!buyStrings[5].equals("")) {
                buy_msg += "[通道5:" + buyStrings[5] + "元]";
            }

            //成功,增加历史购买记录,防止重复购买
            DealListMap.put(next_number + "_1", "buy_ok");
            MainMsg("成功押注!" + next_number + "期 下注总额: " + buy_money_count + " 元," + buy_msg, false);
            RTFile.d("成功押注!" + next_number + "期 下注总额: " + buy_money_count + " 元," + buy_msg);
        } else {
            //其他皆为失败.禁止在下注此期,然后写出错误
            DealListMap.put(next_number + "_1", "buy_error");
            MainMsg("<font color=red>[" + next_number + "期]其他错误:" + doc_msg + "</font>", true);
            RTFile.d("下注错误:期号" + next_number + "/" + post_data + back_result);
            RTMail.Send(UserInfoMap.get("email").toString(), "下注错误:[" + next_number + "期下注出错],请看详情", "参数:" + post_data + "<br> 具体返回信息:" + doc_msg);

        }

    }

    /**
     * 扫描网站维护,一旦出现立即停止程序并发送邮件
     * <script>	location.href = '/';</script>
     *
     * @param back_result
     * @return
     */
    private boolean ScanWebCloseStatus(String back_result) {
        if (back_result.contains("script")
                && back_result.contains("location.href")
                && back_result.contains("'/'")) {
            MainMsg("<font color=red>警告:检测到网站进行更新!系统停止运行.</font>", true);
            RTFile.d("检测到网站进行更新!系统停止运行." + back_result);
            if (UserInfoMap.containsKey("email")) {
                RTMail.Send(UserInfoMap.get("email").toString(), "检测到网站进行更新!系统停止运行.", "请立刻登录系统和网站查看运行状态,系统http请求返回了:" + back_result);
            }

            Stop();//停止系统
            return false;
        }

        return true;
    }

    /**
     * 检测上一期是不是clear,并且期数是不是已经到了100期
     * <br>如果到了100期,之后出现了clear 并且是盈利的 则进行终止运行
     *
     * @param numberid
     * @param clear
     */
    private boolean ScanNewBoutClear(String numberid, String clear) {

        //如果没有开启则直接返回false
        if (!Conf("conf_100_clear").GetIsSelect() || !GetAutoBuyStatus()) {
            return false;
        }
        //先拆分期号
        String now_qihao_string = numberid.split("-")[1];
        //转换为数字
        int now_qihao = Integer.parseInt(now_qihao_string);
        //小于100 则不终止
        if (now_qihao < 100) {
            return false;
        }
        //clear 不是true 页不终止
        if (!clear.equals("true")) {
            return false;
        }
        //当前存款小于初始资金,说明没有盈利
        if (NowUseMoney <= Conf("info_format_money").GetDouble()) {
            return false;
        }

        //以下就是满足了所有条件的,
        //1 期号大于100
        //并且是clear 新一局
        //并且资金大于初始资金,说明盈利的
        //进行消息写出 邮件发送 stop
        //停止自动下注 按下
        String msg_text = "当期[" + numberid + "]期是新一局,并目前已经超过100期,也是盈利状态,故自动停止下注,等待重新激活下注";
        MainMsg("<font color=green>" + msg_text + "</font>", true);
        RTFile.d(msg_text);
        RTMail.Send(UserInfoMap.get("email").toString(), msg_text, "当前存款:" + NowUseMoney + "<br>时间:"
                + RTdate.GetNowTime("yyyy-MM-dd HH:mm:ss")
        );
        AutoTixian();//先自动提现
        Conf("conf_auto_stop").Update(true);
        return true;
    }

    /**
     * 对当前期号 当前通道的下注额进行检测
     * <br>超过规定的期号就开始检测,当前通道下注额是不是初始值,是的话就停止这个通道
     *
     * @param numberid
     * @param TD
     * @param TDmoney
     */
    private void ScanLimitNumber(String numberid, int TD, String TDmoney) {
        //如果通道关闭就直接跳过,自动购买关闭的时候页不运行
        if (!FiveTDBuyStatus[TD] || !GetAutoBuyStatus()) {
            return;
        }
        //先准备要使用的数据
        //拆分当前期号获得数字
        String now_qihao_string = numberid.split("-")[1];
        //转换为数字
        int now_qihao = Integer.parseInt(now_qihao_string);
        //获得界面配置部分的 最小期号
        int limit_qihao = Conf("conf_limit_game_num").GetInt();
        //如果当前期号是120就直接跳过,因为马上就是下一期了
        if (now_qihao == 120) {
            return;
        }
        //如果当前期号小于设置的期号就跳过
        if (limit_qihao >= now_qihao) {
            return;
        }
        //这里就是当前期号大于设定的期号,则开始检查通道的金额
        double GameFormatBuyLimitMoney = Conf("line_td" + TD + "_bei").GetDouble() * 10;
        double TDbuymoney = (TDmoney.equals("")) ? 0 : Double.valueOf(TDmoney);
        //比较2个金额,如果相等,则关闭这个通道
        if (TDbuymoney == GameFormatBuyLimitMoney) {
            FiveTDBuyStatus[TD] = false;
            Conf("line_td_" + TD).Update(false);
            MainMsg("<font color=red>限制:" + numberid + "超过" + limit_qihao + "期,[通道" + TD + "]符合要求,进行暂停继续下注.</font>", true);
            RTFile.d("限制:" + numberid + "超过" + limit_qihao + "期,[通道" + TD + "]符合要求,进行暂停继续下注.");
        }
    }

    /**
     * 修改密码
     *
     * @param OldPass
     * @param NewPass
     * 请求网址:http://hui0999.com/infe/macenter/account/memberdatacontroller/modifyloginpwd.json
     * 请求参数是json password_old	"m8m8m8sa" password	"*****" REpassword "*****"
     * 结果是:json message	"修改成功" status	"Y" maintain	"N"
     *
     */
    @Override
    public boolean EditPass(String OldPass, String NewPass) {
        System.err.println("开始修改密码");
        RTHttp http = new RTHttp("http://" + WebDoMain + "/infe/macenter/account/memberdatacontroller/modifyloginpwd.json");
        String post_data = "{\"password_old\":\"" + OldPass + "\",\"password\":\"" + NewPass + "\",\"REpassword\":\"" + NewPass + "\"}";
        String cookiesList = http.MakeCookies(CookiesMap);
        //设置cookies
        http.SetCookies(cookiesList);
        String result_data = http.Post(post_data);
        System.err.println(result_data);
        http.close();

        //返回为空,则给出消息通知
        if (result_data.equals("")) {
            MainMsg("<font color=red>错误:修改密码返回空结果,请重试</font>", true);
            RTFile.d("错误:修改密码返回空结果,请重试");
            return false;
        }

        //把结果转成json
        Gson json = new Gson();
        HashMap json_list;
        try {
            json_list = json.fromJson(result_data, HashMap.class);
        } catch (Exception e) {
            MainMsg("<font color=red>错误:修改密码json解析错误,稍后再试......</font>", true);
            RTFile.d_error(e, "错误:修改密码json解析错误" + result_data);
            return false;
        }

        //json 空则失败
        if (json_list.isEmpty()) {
            RTFile.d("错误:修改密码json为空" + result_data);
            return false;
        }

        String message = json_list.get("message").toString();
        //检查状态,是不是要重新登录
        if (json_list.containsKey("status")) {
            if (json_list.get("status").toString().equals("Y")) {
                MainMsg("<font color=red>修改密码成功!</font>", true);
                return true;
            }
        }
        MainMsg("<font color=red>修改密码失败!" + message + "</font>", true);
        RTFile.d("修改密码失败" + message + result_data);
        return false;
    }

}
