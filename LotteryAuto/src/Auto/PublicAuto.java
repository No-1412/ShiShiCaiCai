/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Auto;

import Main.StartMain;
import ORG.GameConfig;
import RTPower.RTFile;
import RTPower.RTdate;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

/**
 * 自动运行父类
 * <br>所有通公用的方法在这里实现
 * <br>每个平台不同的处理在子类实现
 *
 * @author jerry
 */
abstract public class PublicAuto {
    /**
     * 彩票网站顶级域名部分
     */
    public String WebDoMain;

    //==========================================================================
    //全局声明
    //==========================================================================
    /**
     * 主界面的引用
     */
    public StartMain MainThis;

    /**
     * 主线程
     */
    public Thread AutoThread;
    /**
     * 主线程的开关
     * <br>默认为开
     */
    public boolean AutoStatus = true;

    /**
     * 默认的5通道状态
     */
    public boolean[] FiveTDBuyStatus = new boolean[]{true, true, true, true, true, true};

    //-------------------------------
    /**
     * 对消息控件model 进行全局声明
     */
    protected DefaultListModel main_msg;

    /**
     * 界面交易列表model
     */
    protected DefaultTableModel main_deal;

    /**
     * cookies存储的hash
     */
    protected HashMap CookiesMap = new HashMap();

    /**
     * 所有交易记录的Hash,
     * <br>用来检测当前期号是否被吓住过,就不用重复下注
     * <br>以及主界面的下注列表的 刷新数据
     */
    protected HashMap DealListMap = new HashMap();
    /**
     * 用户的当前存款
     * <br>默认为0,靠心跳刷新获取
     */
    protected double NowUseMoney = 0;

    /**
     * 用户今日交易金额记录
     */
    protected double NowLogMoney = 0;

    /**
     * 正在运行的日期
     * <br>用来进行判断做激活点
     */
    protected String NowStartedDate = "";
    /**
     * 保存用户的email和银行信息
     * <br>user_name
     * <br>email
     * <br>bank_number
     * <br>bank_name
     * <br>bank_city
     * <br>bank_pro
     */
    protected HashMap UserInfoMap = new HashMap();
    
    /**
     * 改变某期某通道的购买金额记录集
     * <br>需要修改则记录
     * <br>期号_通道=金额
     * <br>然后在写出购买金额的时候检测,存在这个通道的key 则 覆盖掉原来的数据
     */
    public HashMap ChangeFiveBuyMoney=new HashMap();
    //==========================================================================
    //公共方法
    //==========================================================================

    /**
     * 初始化运行
     *
     * @param main_this 引用主界面
     */
    public PublicAuto(StartMain main_this) {
        MainThis = main_this;
        //加载msg
        main_msg = (DefaultListModel) MainThis.msg.getModel();
        //加入界面控件
        main_deal = (DefaultTableModel) MainThis.deal.getModel();

    }

    /**
     * 启动程序
     */
    public void Start() {

        StartButtonStatusChange(false);
        //开始登陆,登陆成功后才能进行下一步
        boolean login_status = Login();
        //登陆失败 终止运行
        if (!login_status) {
            StartButtonStatusChange(true);
            return;
        }
        AutoStatus = true;//启动时候开启,stop的时候会false
        //开启线程,进行循环运行
        AutoThread = new Thread(() -> {
            //主线程开关控制
            while (AutoStatus) {

                //开始心跳保持
                boolean heart = KeepHeartJump();
                //开始与api进行通讯
                //只有心跳成功 才进行api通讯,防止心跳死了,API还在一直读取,
                //心跳会更新存款,存款不更新了,下单就会出现盈利错误
                if (heart) {
                    GetApiInfo();
                }

                try {
                    //采用10秒循环,来保证心跳
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    RTFile.d_error(ex, "线程休眠10秒失败");
                }
            }
            MainMsg("系统线程运行结束", false);
            RTFile.d("系统线程运行结束");
        });
        AutoThread.start();
    }

    /**
     * 彻底关闭自动程序
     * <br>在关闭主界面后自动执行
     * <br>彻底关闭和清理
     */
    public void Close() {
        Stop();
        //清理引用
        MainThis = null;
        //销毁线程
        AutoThread = null;
    }

    /**
     * 停止程序
     * <br>不销毁,只是停止线程
     */
    public void Stop() {
 
        AutoStatus = false;//关闭线程开关
        //停止自动下注 按下
        Conf("conf_auto_stop").Update(true);
        StartButtonStatusChange(true);
        MainMsg("系统停止运行.", false);
    }

    /**
     * 获得界面自动购买控件的状态(便捷方法)
     * <br>重要的部分,用来决定是否自动下注
     * <br>并且随着界面的设置不同实时生效
     *
     * @return true 自动购买 false 停止自动购买
     */
    public boolean GetAutoBuyStatus() {
        //获得主界面的start 的 开启开关状态
        return Conf("conf_auto_start").GetIsSelect();
    }

    /**
     * 助手方法,快捷进行界面控件读取或设置变量信息
     *
     * @param KeyName 界面控件变量名称
     * @return
     */
    public GameConfig Conf(String KeyName) {
        return MainThis.game_config.Find(KeyName);
    }

    /**
     * 快捷设置某个通道的状态,以及更新控件显示
     *
     * @param TDid
     */
    public void SettingFiveTdBuyStatus(int TDid) {
        //设置全局变量
        boolean new_status = !(FiveTDBuyStatus[TDid]);
        FiveTDBuyStatus[TDid] = new_status;
        Conf("line_td_" + TDid).Update(new_status);

        RTFile.d_out("通道" + TDid + "改变状态为:" + FiveTDBuyStatus[TDid]);
        MainMsg("<font color=red>通道[" + TDid + "]改变激活状态为:" + FiveTDBuyStatus[TDid] + "</font>", true);
    }

    /**
     * 增加界面消息控件新消息
     *
     * @param msg
     * @param html_msg
     */
    public void MainMsg(String msg, boolean html_msg) {
        //如果超过100条则自动清理
        int msg_count = main_msg.getSize();
        if (msg_count > 100) {
            main_msg.clear();
        }
        String date = RTdate.GetNowTime("MM-dd HH:mm:ss");
        msg = date + "      " + msg;
        if (html_msg) {
            msg = "<html>" + msg + "</html>";
        }
        main_msg.insertElementAt(msg, 0);
    }

    //==========================================================================
    //可继承的方法
    //==========================================================================
    /**
     * 交易进行全局Hash记录,防止多次购买,并把交易显示到界面deal list里
     * <br>检测的是期号_通道
     *
     * @param buy_timeString 下注时间
     * @param qihao 期号
     * @param TD 通道
     * @param buy 购买详细
     * @param money 输赢金额
     * @param pay_status 派彩
     */
    protected void DealGo(String buy_timeString, String qihao, String TD, String buy, String money, String pay_status) {
        //检查期号和通道存在没有,存在Hash里就不添加
        if (DealListMap.containsKey(qihao + "_" + TD)) {
            return;
        }
        //不存在,则追加一份,防止再次下注
        DealListMap.put(qihao + "_" + TD, buy + "," + money + "," + pay_status);
        //准备要写入的数据

        //money 根据-号来决定颜色
        String new_money;
        if (money.contains("-")) {
            new_money = "<html><font color=red>" + money + "</font></html>";
        } else {
            new_money = "<html><font color=green>" + money + "</font></html>";
        }
        Object[] add_data = new Object[]{
            buy_timeString, qihao, "[" + TD + "]" + buy, new_money, pay_status
        };

        main_deal.insertRow(0, add_data);//插入到第一行

    }

    /**
     * 改变界面的运行按钮的可用状态
     *
     * @param status true 运行可用 false 运行不可用
     */
    protected void StartButtonStatusChange(boolean status) {
        if (status) {

            MainThis.conf_start_button.setEnabled(true);
            MainThis.conf_stop_button.setEnabled(false);
        } else {
            MainThis.conf_start_button.setEnabled(false);
            MainThis.conf_stop_button.setEnabled(true);
        }
    }

    //==========================================================================
    //子类抽象方法
    //==========================================================================
    /**
     * 自动提现方法
     * <br>也可手动调用
     * <br>默认在凌晨第二天时自动激活提现 或到达止盈线自动提现
     */
    abstract public void AutoTixian();

    /**
     * 加载验证码
     * <br>有验证码则进行图片抓取,然后显示在界面上
     * <br>没有验证码则隐藏掉 验证码的输入框
     * <br>主界面启动就会调用
     * <br>登录时候子类自行在login 内决定是否要验证码检测
     *
     */
    abstract public void GetYanzhengma();

    /**
     * 自动对传入的新cookies进行处理
     * <br>每个网站都会有独立的cookies 所以这里就交给每个子类来实现
     * <br>组装成通用的,保存在全局变量,供所有页面使用
     * <br>一定最后使用
     *
     * @param get_cookies
     */
    abstract protected void ScanEditCookies(HashMap get_cookies);

    /**
     * 登陆方法
     * <br>值守前,确定登陆成功
     * <br>返回false 会自动终止
     *
     * @return true 登陆成功 false 登陆失败
     */
    abstract protected boolean Login();

    /**
     * 自动值守心跳
     * <br>获取账号信息和交易信息
     * <br>成功返回true
     * <br>返回false 则不会进行下一步的api通讯,直到 心跳恢复正常
     *
     * @return
     */
    abstract protected boolean KeepHeartJump();

    /**
     * 与API通讯获得下注信息
     * <br>这里面要操作,如果全部正确 就要开始下注
     */
    abstract protected void GetApiInfo();
    
    /**
     * 修改密码方法
     * @param OldPass
     * @param NewPass 
     * @return  
     */
    abstract public boolean EditPass(String OldPass,String NewPass);

}
