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
import java.util.HashMap;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author jerry
 */
public class LotteryAuto_WNSR extends PublicAuto {

    /**
     * 威尼斯的phpsession,链接到另一个真正页面额
     */
    protected String PhpSession;

    /**
     * 通过抓取解析出来的吉利的目前使用域名
     */
    protected String LoginWebDomian;

    public LotteryAuto_WNSR(StartMain main_this) {
        super(main_this);
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
        //确定资金是否大于限制的提现保留额度,大于才进行
        //得出提现可用额度
        int tixian_money = (int) (NowUseMoney - Conf("conf_tixian_limit").GetDouble());
        if (tixian_money <= 0) {
            RTFile.d("可提现的超出部分低于0元,不进行提现,当前金额:" + NowUseMoney);
            MainMsg("可提现的超出部分低于0元,不进行提现,当前金额:" + NowUseMoney, false);
            return;
        }
        if (tixian_money < 100) {
            RTFile.d("平台要求每次100元以上提现,当前提现金额:" + tixian_money);
            MainMsg("平台要求每次100元以上提现,当前提现金额:" + tixian_money, false);
            return;
        }
        //提现密码没有设置不提
        if (Conf("conf_user_out_pass").GetString().equals("")) {
            MainMsg("提现密码为空,不进行提现.", false);
            return;
        }

        //自动提现操作
        //提现前先把资金转到钱包
        //找到他的下主页面
        RTHttp Http = new RTHttp("http://www." + WebDoMain + "/exchange/do/jlex");

        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        String post_data = "from=JL&to=SM&amount=" + tixian_money + "&btnSub=立即转换";
        String retult = Http.Post(post_data);
        Http.close();

        //以上完成了转入钱包,接下来直接全部转出
        Http = new RTHttp("http://www." + WebDoMain + "/exchange/do/jlex");

        //设置cookies
        Http.SetCookies(Http.MakeCookies(CookiesMap));
        post_data = "bank=" + UserInfoMap.get("bank_name").toString() + ""
                + "&bank_account=SM&amount=" + tixian_money + ""
                + "&qkmm=" + Conf("conf_user_out_pass").GetString()
                + "&sub_btn=+提++交++";
        retult = Http.Post(post_data);
        Http.close();
        MainMsg("提现申请已经提交!提出" + tixian_money + "元", false);
        RTFile.d("提现申请已经提交!提出" + tixian_money + "元,原有金额:" + NowUseMoney);
    }

    @Override
    public void GetYanzhengma() {
        MainThis.conf_yanzhengma_image.setText("无需");
        MainThis.conf_yanzhengma.setVisible(false);
    }

    @Override
    protected void ScanEditCookies(HashMap get_cookies) {
//此处是通用的,必须要有的
        if (get_cookies.containsKey("web")) {
            CookiesMap.put("web", get_cookies.get("web").toString());
        }
    }

    @Override
    protected boolean Login() {
        //检查用户名和密码不能为空
        String user_name = Conf("conf_user_name").GetString();
        String user_pass = Conf("conf_user_pass").GetString();
        if (user_name.equals("") || user_pass.equals("")) {
            MainMsg("<font color=red>用户名和密码不能为空!</font>", true);
            return false;
        }
        //先获得首页的cookies,不然没有办法登陆后去其他页面
        RTHttp Http = new RTHttp("http://www." + WebDoMain + "/cn/index");
        //把整个页面获得cookies记录下来
        CookiesMap = Http.GetCookiesMap();
        String retult = Http.Get();
        Http.close();

        //进行登录 
        Http = new RTHttp("http://www." + WebDoMain + "/cn");
        String post_data = "username=" + user_name
                + "&password=" + user_pass
                + "&Submit=登入";
        //转换cookies为string 然后设置本次链接的cxookies
        Http.SetCookies(Http.MakeCookies(CookiesMap));
        String result = Http.Post(post_data);
        //威尼斯 302跳转是正常登陆,否则会提示剩余几次登陆
        if (!result.equals("")) {
            MainMsg("<font color=red>错误:帐号/密码错误，请重新输入!</font>", true);
            Http.close();
            return false;
        }
        //获得页面的额cookies
        HashMap Cookie_Map = Http.GetCookiesMap();
        //对初次的cookies进行处理,泊村在全局变量里
        ScanEditCookies(Cookie_Map);
        Http.close();
//登录成功后 去抓取一个phpsession 在彩票页面,威尼斯使用的是包含页面,包含地址里有phpsession
        boolean back_get_phpsession = GetPhpSessionInfo();
        if (!back_get_phpsession) {
            MainMsg("<font color=red>错误:获取phpsession失败.!</font>", true);
            return false;
        }

        //再获取用户基本信息
        GetUserInfo();

        return true;
    }

    @Override
    protected boolean KeepHeartJump() {
        //由于威尼斯人的 交易信息页面里包含了当前资金与交易记录,
        //并且页面是在吉利的实际页面里的,保持心跳就直接这一个页面足够了

        if (GetDealList()) {
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
        System.err.println("buyurl:期号" + next_number + "/" + buyurl_fivemoneys[0]);

        //开始进行下一期的购买方法
        GoNextBuyStart(next_number, buy_money_count, buyurl_fivemoneys);
    }

    @Override
    public boolean EditPass(String OldPass, String NewPass) {
        return true;
    }

    /**
     * 获得威尼斯的phpsession
     *
     * @return
     */
    private boolean GetPhpSessionInfo() {

        //找到他的下主页面
        RTHttp Http = new RTHttp("http://www." + WebDoMain + "/jl");

        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        String retult = Http.Get();
        Http.close();

        //利用html 解析控件找到里面的  iframe 的 src 就是 phpsession
        Document html_doc = Jsoup.parse(retult);
        Element iframe = html_doc.getElementById("caipiao");
        if (iframe == null) {
            return false;
        }
        //这里就是要用到的带session 的 心跳保持页面
        //session 也要单独的记录下来
        String iframe_src = iframe.attr("src");
        String[] Q_url = iframe_src.split("/");

        PhpSession = Q_url[Q_url.length - 1];
        CookiesMap.put("PHPSESSID", PhpSession);
        LoginWebDomian = Q_url[2];//第二个是域名

        //这里链接一次这个页面,不然貌似进不去
        Http = new RTHttp(iframe_src);
        //设置cookies
        Http.SetCookies(Http.MakeCookies(CookiesMap));
        String retult_html = Http.Get();
        Http.close();
        return true;
    }

    /**
     * 获取用户基本信息 并显示到界面和hash
     */
    private void GetUserInfo() {
        //找到他的下主页面
        RTHttp Http = new RTHttp("http://www." + WebDoMain + "/member-center/member-info");

        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        String retult = Http.Get();
        Http.close();
        //获得返回后,解析json
        Gson json = new Gson();
        //查找其中的 returnData
        HashMap hash_json = json.fromJson(retult, HashMap.class);

        //没有指定数据则终止
        if (!hash_json.containsKey("returnData")) {
            MainMsg("<font color=red>抓取用户信息出错,没有指定数据......</font>", true);
            RTFile.d("抓取用户信息出错,没有指定数据......");
            return;
        }

        LinkedTreeMap user_info_map = (LinkedTreeMap) hash_json.get("returnData");
        String bank_number = user_info_map.get("bank_account").toString();
        String bank_name = user_info_map.get("bank").toString();

        String username = user_info_map.get("username").toString();
        String email = "4771007@qq.com";
        //保存到UserInfoMap 内
        UserInfoMap.put("user_name", username);
        UserInfoMap.put("email", email);
        UserInfoMap.put("bank_number", bank_number);
        UserInfoMap.put("bank_name", bank_name);
        UserInfoMap.put("bank_city", "");
        UserInfoMap.put("bank_pro", "");

        //传递请求,刷新界面的配置信息
        Conf("info_user_name").Update(username);
        Conf("info_email").Update(email);
        Conf("info_bank_number").Update(bank_number);
        Conf("info_bank_info").Update(bank_name);

    }

    /**
     * 获得下注列表 与 用户剩余资金 刷新到界面
     *
     * @return
     */
    private boolean GetDealList() {
        //刷一次金额
        //https://jlcp.555cai.com/cp/bet/money

        RTHttp Http = new RTHttp("https://" + LoginWebDomian + "/cp/bet/money");
        Http.GZipStatus = true;
        //设置cookies
        Http.SetCookies(Http.MakeCookies(CookiesMap));
        String result_m = Http.Get();
        System.err.println(result_m);
        Http.close();

        Http = new RTHttp("https://" + LoginWebDomian + "/cp/cqssc/zdmx?checkout=1");
        Http.GZipStatus = true;
        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        String result = Http.Get();
//        System.err.println(result);
        Http.close();
        if (result.equals("")) {
            MainMsg("<font color=red>超时:抓取历史下注页面为空,稍后再试......</font>", true);
            RTFile.d("超时:抓取历史下注页面为空,稍后再试......");
            return false;
        }
        //解析html
        Document html_doc = Jsoup.parse(result);
        //寻找table 的 tbody 部分
        Elements html_tbody = html_doc.getElementsByTag("tbody");
        //如果小于1就是没有得到 就直接退出
        if (html_tbody.size() < 1) {
            MainMsg("<font color=red>页面解析错误,没有找到tbody数据</font>", true);
            RTFile.d("页面解析错误,没有找到tbody数据......");
            return false;
        }
        //获得用户的资金----------------------------------------------------------
        Element html_user_money = html_doc.getElementById("userMoney");
        if (html_user_money == null) {
            MainMsg("<font color=red>页面解析错误,找到当前用户金额信息</font>", true);
            RTFile.d("页面解析错误,找到当前用户金额信息......");
            return false;
        }
        String UserMoney = html_user_money.attr("value");

        NowUseMoney = Math.floor(Double.parseDouble(UserMoney));
        //刷新主界面存款显示
        Conf("info_user_now_money").Update("" + NowUseMoney);
        //重新刷新配置的下注倍数
        String use_buy_double = Conf("conf_buy_double").GetString();
        Conf("info_buy_double").Update(use_buy_double);

        //激活资金到达配置阀的检测
        ScanMoneyStop();
        //----------------------------------------------------------------------
        //寻找出tr更新下注列表
        Elements html_tbody_tr = html_tbody.get(0).getElementsByTag("tr");

        //循环tr列表,获取td数据 刷新到界面
        for (Iterator<Element> iterator = html_tbody_tr.iterator(); iterator.hasNext();) {
            //在tr里面找td
            Element next = iterator.next();
            Elements all_td = next.getElementsByTag("td");
            //写出到界面以及记录的hash
            String one_time_all = all_td.get(6).text();
            String[] Q_time = one_time_all.split(" ");

            //切割时间,来获得期号前面的部分,他给出的直接期号就是38这样的
            String one_qihao = all_td.get(0).text();//期号
            //不等于3位则给他加足三位
            if (one_qihao.length() < 3) {
                one_qihao = ("000" + one_qihao);
                one_qihao = one_qihao.substring(one_qihao.length() - 3);
            }
            one_qihao = (Q_time[0].replace("-", "")) + "-" + one_qihao;

            String one_td = all_td.get(1).text();
            String one_buy = all_td.get(2).text();
            String one_money = all_td.get(3).text();
            String one_pay = all_td.get(4).text();
//然后把付出的列表写出到界面----------------------------------
            DealGo(Q_time[0] + " " + Q_time[1],
                    one_qihao,
                    MakeTDStringToNumString(one_td),
                    one_buy + "" + one_money,
                    one_pay,
                    "开彩");
        }

        return true;
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
     * 把中文的 万定位 这种转化成 数字
     *
     * @param TDString
     * @return
     */
    private String MakeTDStringToNumString(String TDString) {
        String back_num = "";

        switch (TDString) {
            case "万定位":
                back_num = "1";
                break;
            case "仟定位":
                back_num = "2";
                break;
            case "佰定位":
                back_num = "3";
                break;
            case "拾定位":
                back_num = "4";
                break;
            case "个定位":
                back_num = "5";
                break;
            case "5":
                back_num = "个定位";
                break;
            case "4":
                back_num = "拾定位";
                break;
            case "3":
                back_num = "佰定位";
                break;
            case "2":
                back_num = "仟定位";
                break;
            case "1":
                back_num = "万定位";
                break;
            case "ODD":
                back_num = "单";
                break;
            case "EVEN":
                back_num = "双";
                break;
            //转化下注id====================================
            case "1ODD":
                back_num = "2614";
                break;
            case "2ODD":
                back_num = "2630";
                break;
            case "3ODD":
                back_num = "2646";
                break;
            case "4ODD":
                back_num = "2662";
                break;
            case "5ODD":
                back_num = "2678";
                break;
            case "1EVEN":
                back_num = "2615";
                break;
            case "2EVEN":
                back_num = "2631";
                break;
            case "3EVEN":
                back_num = "2647";
                break;
            case "4EVEN":
                back_num = "2663";
                break;
            case "5EVEN":
                back_num = "2679";
                break;

        }

        return back_num;
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

            back_strings[0] += GetBuyText(String.valueOf(TD),
                    one_buy.get("buy_tag").toString(),
                    back_strings[TD]) + ",";

        }

        //循环结束,写出金额
        back_strings[6] = "" + temp_count_moneys;
        //刷新界面数据
        REMainFiveTdBuyMoney(next_number, show_strings,show_DS_strings);
        return back_strings;
    }

    /**
     * 传入通道和购买的单双以及金额组装成一个urltext
     *
     * @param TD
     * @param TAG
     * @param BuyMoney
     * @return
     */
    private String GetBuyText(String TD, String TAG, String BuyMoney) {
        String back_text = "";

        back_text = "{\"rid\":\"" + MakeTDStringToNumString(TD + TAG)
                + "\",\"plate\":\"B\","
                + "\"content\":\""
                + MakeTDStringToNumString(TAG)
                + "\","
                + "\"scontent\":\""
                + MakeTDStringToNumString(TAG)
                + "\","
                + "\"tp_name\":\""
                + MakeTDStringToNumString(TD)
                + "\","
                + "\"money\":" + BuyMoney
                + ","
                + "\"rate\":\"1.982\"}";
        return back_text;
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
        RTHttp Http = new RTHttp("https://jlcp.555cai.com/cp/bet/add/cqssc");

        String cookiesList = Http.MakeCookies(CookiesMap);
        //设置cookies
        Http.SetCookies(cookiesList);
        //设置下注数据
        String dear_string = buyStrings[0].substring(0, buyStrings[0].length() - 1);
        String post_data = "order=[" + dear_string + "]";

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

        //返回的结果是json
        Gson json = new Gson();
        //转译为Hash
        HashMap json_list;
        try {
            json_list = json.fromJson(back_result, HashMap.class);
        } catch (Exception e) {
            RTFile.d_error(e, "错误:下注结果json解析错误" + back_result);
            MainMsg("<font color=red>错误:下注结果json解析错误,稍后再试......</font>", true);
            return;
        }

        String success = "";
        //找到success
        if (json_list.containsKey("success")) {
            success = json_list.get("success").toString();
        }

        //检查成功标识符,否则都为失败,记录错误显示错误
        if (success.equals("1.0")) {
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
            RTFile.d("成功押注!" + next_number + "期 下注总额: " + buy_money_count + " 元," + buy_msg + post_data);
//            System.err.println(back_result);
        } else {
            String err_text = "";
            //检查有没有消息,有的话检出
            if (json_list.containsKey("err")) {
                err_text = json_list.get("err").toString();
            }
            //其他皆为失败.禁止在下注此期,然后写出错误
            DealListMap.put(next_number + "_1", "buy_error");
            MainMsg("<font color=red>[" + next_number + "期]其他错误:" + err_text + back_result + "</font>", true);
            RTFile.d("下注错误:期号" + next_number + "/" + post_data + back_result);
            RTMail.Send(UserInfoMap.get("email").toString(), "下注错误:[" + next_number + "期下注出错],请看详情", "参数:" + post_data + "<br> 具体返回信息:" + back_result);

        }

    }
}
