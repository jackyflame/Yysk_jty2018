package im.socks.yysk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Proxy;
import im.socks.yysk.data.Session;
import im.socks.yysk.data.User;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.IYyskService;
import im.socks.yysk.vpn.IYyskServiceListener;

public class HomeFragment extends Fragment {

    private Button vpnButton;
    private TextView txv_vpn_statu;
    private TextView txv_greeting;
    private VerticalTextview txv_chuxiao_name;

    //proxy part
    private View lin_vpn_lines;
    private TextView txv_line_name;
    private TextView txv_endtime;
    private boolean isTimeEnd = false;

    //private long startTime;
    private final AppDZ app = Yysk.app;

    /**true表示需要登录才可以连接，获得代理列表*/
    //private final boolean requireLogin = false;

    private Ping ping = null;
    private float pingTime = -1;

    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name)) {
                //checkVpnUpdate(false);
                updateMe();
                getAndSetDefaultProxy();
            } else if (Yysk.EVENT_LOGOUT.equals(name)) {
                updateMe(true);
            } else if (Yysk.EVENT_PROXY_CHANGED.equals(name)) {
                updateProxy((Proxy) data);
            } else if(Yysk.EVENT_PROXY_CHANGED_SERVER.equals(name)){
                updateProxy((Proxy) data);
                startVPNWithServer();
            }else if(Yysk.EVENT_PAY_SUCCESS.equals(name)||Yysk.EVENT_PAY_FAIL.equals(name)){
                if(Yysk.EVENT_PAY_SUCCESS.equals(name)){
                    updateAcl();
                }
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_dz, container, false);

        initConnectLayout(view);

        initProxyLayout(view);

        initFunView(view);

        initRefreshLayout(view);

        updateVpnStatus();

        if(app.getSessionManager().getSession().isLogin()){
            updateMe();
        }

        getAndSetDefaultProxy();

        return view;
    }

    private void initFunView(View view){
        view.findViewById(R.id.lin_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkLogin()){
                    startActivity(new Intent(getContext(),InviteActivity.class));
                }
            }
        });

        view.findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkLogin()) {
                    getFragmentStack().show(MyFragment.newInstance(), "MyFragment", false);
                }
            }
        });

        view.findViewById(R.id.lin_end_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkLogin()) {
                    getFragmentStack().show(PayFragment.newInstance(), "PayFragment", false);
                }
            }
        });

        view.findViewById(R.id.img_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkLogin()) {
                    getFragmentStack().show(PayFragment.newInstance(), "PayFragment", false);
                }
            }
        });
    }

    private void initRefreshLayout(View view){
        final SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                updateVpnStatus();
                updateMe();
                //updateProxy(app.getSessionManager().newProxy());
                refreshlayout.finishRefresh(true);
            }
        });
        txv_greeting = view.findViewById(R.id.txv_greeting);
        txv_chuxiao_name = view.findViewById(R.id.txv_chuxiao_name);
        txv_chuxiao_name.setText(14, 0, Color.RED);
        txv_chuxiao_name.setTextStillTime(5000);
        txv_chuxiao_name.setAnimTime(500);
    }

    private void initConnectLayout(View view) {
        vpnButton = view.findViewById(R.id.vpnButton);
        txv_vpn_statu = view.findViewById(R.id.txv_vpn_statu);
        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkLogin()) {
                    return;
                }
                if(isTimeEnd == true){
                    showVPNAlert("加速时间已用完，请联系管理员");
                }else{
                    toggleVpn();
                }
            }
        });
        txv_endtime = view.findViewById(R.id.txv_endtime);
    }

    private void initProxyLayout(View view) {
        lin_vpn_lines = view.findViewById(R.id.lin_vpn_lines);
        txv_line_name = view.findViewById(R.id.txv_line_name);
        lin_vpn_lines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(ProxyFragment.newInstance(),null,false);
            }
        });
        updateProxy(app.getSessionManager().getProxy());
    }

    @Override
    public void onStart() {
        super.onStart();

        //先同步显示一次

        app.getVpn().bind(serviceConnection);
        app.getEventBus().on(Yysk.EVENT_ALL, eventListener);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);//status_event
                handler.sendEmptyMessage(3);//time
            }
        }, 0, 1000);

        //每隔1分钟检查一次金额？
        if (false) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(4);//me
                }
            }, 0, 60000);
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        stopPing();
        app.getVpn().unbind(serviceConnection);
        app.getEventBus().un(Yysk.EVENT_ALL, eventListener);

    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void updateProxy(Proxy proxy) {
        if (proxy != null && proxy.data != null) {
            //设置text
            txv_line_name.setText(proxy.name);
            String host = proxy.data.getString("host");
            startPing(host);
        } else {
            txv_line_name.setText("线路选择");
        }
    }

    private void updateVpnStatus() {
        int status = Yysk.STATUS_STOPPED;
        if (yyskService != null) {
            try {
                status = yyskService.getStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            //vpnservice die
        }
        if (this.vpnStatus == status) {
            return;
        }
        this.vpnStatus = status;
        if (status == Yysk.STATUS_INIT || status == Yysk.STATUS_STOPPED) {
            vpnButton.setText("点击连接");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        } else if (status == Yysk.STATUS_CONNECTING) {
            vpnButton.setText("连接中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        } else if (status == Yysk.STATUS_STOPPING) {
            vpnButton.setText("停止中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
        } else if (status == Yysk.STATUS_CONNECTED) {
            //showVPNAlert("VPN 开启");
            vpnButton.setText("点击断开");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
        } else {
            //
            //showVPNAlert("VPN 已断开");
            //不可能的
            txv_vpn_statu.setText("未知:" + status);
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        }
    }

    /**
     * 自动更新代理并设置默认代理
     * */
    private void checkVpnUpdate(boolean isClick){
        final Session session = app.getSessionManager().getSession();
        if (session.isLogin()) {
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setCancelable(false);
            dialog.setMessage("正在刷新...");
            dialog.show();
            app.apiDZ.checkVpnUpdateVerson(session.user.mobile_number, session.user.password, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    if(result != null && result.isEquals("errorcode", "succ")){
                        final int vpnVersion = result.getInteger("versionid") != null
                                ? result.getInteger("versionid") : -1;
                        final int companyid = result.getInteger("companyid") != null
                                ? result.getInteger("companyid") : -1;
                        //版本过低则更新
                        if(session.vpnVersion <= 0 || session.vpnVersion < vpnVersion){
                            //获取公司代理列表
                            app.getApi().getDZProxyList(session.user.mobile_number, new YyskApi.ICallback<XBean>() {
                                @Override
                                public void onResult(XBean result) {
                                    if(result != null && result.hasKeys("data")){
                                        List<XBean> porxList = result.getList("data");
                                        if(porxList != null && porxList.size() > 0){
                                            //缓存列表
                                            app.getDzProxyManager().save(porxList);
                                            //更新版本
                                            app.getSessionManager().onVpnVerCheck(vpnVersion,companyid);
                                            ////自动设置默认代理
                                            //setDefaultProxy(result);
                                        }
                                    }
                                }
                            });
                            //提示线路更新
                            showVPNAlert("线路更新："+StringUtils.getNowTimeStr());
                            //txv_vpn_update.setText("更新线路("+StringUtils.getNowTimeStr()+")");
                        }else{
                            showVPNAlert("线路无更新");
                            if(session.vpnUpdateTime > 0){
                                //txv_vpn_update.setText("更新线路("+StringUtils.getTimeStr(session.vpnUpdateTime)+")");
                            }
                        }
                    }else{
                        showVPNAlert("线路无更新");
                    }
                    dialog.dismiss();
                }
            });
            //检查过期时间
            checkEndTime(session.user.mobile_number);
        }else if(isClick == true){
            new AlertDialog.Builder(getContext())
                    .setTitle("提醒")
                    .setMessage("请先登录")
                    .setPositiveButton("确定",null)
                    .show();
        }
    }

    /**
     * 刷新剩余加速时间
     * */
    private void checkEndTime(String account){
        app.apiDZ.checkVpnEndTime(account,new YyskApi.ICallback<XBean>(){
            @Override
            public void onResult(XBean result) {
                if(result != null && result.hasKeys("enable")){
                    //服务状态0：停用，1：启用
                    int enable = result.getInteger("enable") != null
                            ? result.getInteger("enable") : -1;
                    //服务到期时间，10位数字时间，精确到秒
                    long expire_date = result.getLong("expire_date") != null
                            ? result.getLong("expire_date") : -1;
                    //刷新到期时间
                    if(enable != 1){
                        isTimeEnd = true;
                    }else{
                        isTimeEnd = false;
                    }
                    txv_endtime.setVisibility(View.VISIBLE);
                    txv_endtime.setText("到期时间："+StringUtils.getTimeStr(expire_date*1000));
                }
            }
        });
    }

    private void toggleVpn() {
        int status = Yysk.STATUS_STOPPED;
        if (yyskService != null) {
            try {
                status = yyskService.getStatus();
            } catch (RemoteException e) {
                MyLog.e(e);
            }
        }
        if (status == Yysk.STATUS_INIT || status == Yysk.STATUS_STOPPED) {
            //设置了代理，就可以启动了
            //或者必须先登录
            //如果选择了代理
            if (app.getSessionManager().getProxy()!=null) {
                startVPNWithServer();
            } else {
                //如果还没有proxy，就需要先选择
                getFragmentStack().show(ProxyFragment.newInstance(), null, false);
            }

        } else if (status == Yysk.STATUS_CONNECTING) {
            //
        } else if (status == Yysk.STATUS_CONNECTED) {
            stopVPNWithServer();
            //showVPNAlert("VPN 已关闭");
        } else {
            //
        }
    }

    private void showVPNAlert(String msg){
        new AlertDialog.Builder(getContext())
                .setTitle("提醒")
                .setMessage(msg)
                .setPositiveButton("确定",null)
                .show();
    }

    private void updateMe(){
        updateMe(false);
    }

    private void updateMe(boolean isLogout){
        if(isLogout == true){
            txv_endtime.setText("");
            return;
        }
        if(app.getSessionManager().isUserInfoNeedUdate()){
            app.getApi().getUserInfo(new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    if(NetUtil.checkAndHandleRsp(result,getContext(),"获取个人信息失败",null)){
                        XBean userInfo = NetUtil.getRspData(result);
                        app.getSessionManager().onUserInfoUpdate(userInfo);
                        int rest_days = userInfo.getInteger("rest_days",0);
                        txv_endtime.setText("剩余时长"+rest_days+"天");
                        XBean packgeInfo = userInfo.getXBean("tariff_package");
                        if(packgeInfo != null){
                            txv_greeting.setText(packgeInfo.getString("greeting"));
                        }
                        updateAds(userInfo.getList("promotions", XBean.class));
                    }
                }
            });
        }else{
            User user = app.getSessionManager().getSession().user;
            int rest_days = user.rest_days;
            txv_endtime.setText("剩余时长"+rest_days+"天");
            XBean packgeInfo = user.tariff_package;
            if(packgeInfo != null){
                txv_greeting.setText(packgeInfo.getString("greeting"));
            }
            updateAds(user.promotions);
        }
    }

    private void updateAds(List<XBean> list){
        if(list == null || list.size() == 0){
            return;
        }
        ArrayList<String> displayList = new ArrayList<>();
        for (XBean item:list){
            displayList.add(item.getString("description",""));
        }
        txv_chuxiao_name.setTextList(displayList);
    }

    private void startVPNWithServer(){
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("连接中...");
        dialog.show();
        //获取选择的线路
        final Proxy proxy = app.getSessionManager().getProxy();
        //获取服务器动态端口信息
        app.getApi().getProxyInfo(proxy.id, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                dialog.dismiss();
                if(NetUtil.checkAndHandleRspWithData(result,getContext(),"获取线路失败",null)){
                    Proxy proxyNew = new Proxy();
                    XBean data = NetUtil.getRspData(result);
                    //重新装填字段（兼容老版本）
                    data.put(Proxy.BEANNAME_ID, proxy.id);
                    data.put(Proxy.BEANNAME_NAME, proxy.name);
                    //设置数据
                    proxyNew.id = proxy.id;
                    proxyNew.name = proxy.name;
                    proxyNew.data = data;
                    proxyNew.isCustom = false;
                    app.getSessionManager().setProxy(getActivity(), proxyNew, false);
                }
            }
        });
    }

    private void stopVPNWithServer(){
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("断开中...");
        dialog.show();
        stopVPNWithServer(dialog);
    }

    private void stopVPNWithServer(final ProgressDialog dialog){
        //获取选择的线路
        Proxy proxy = app.getSessionManager().getProxy();
        if(proxy == null){
            return;
        }
        //获取服务器动态端口信息
        app.getApi().sendProxyClose(proxy.id, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(dialog != null){
                    dialog.dismiss();
                }
                app.getVpn().stop();
            }
        });
    }
    //========================================
    private IYyskService yyskService;
    private int vpnStatus;
    private Timer timer;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                updateVpnStatus();
            } else if (msg.what == 2) {
                long[] args = (long[]) msg.obj;
                //updateVpnStat(args[0], args[1], args[2], args[3]);
            } else if (msg.what == 3) {
                //updateVpnTime();
            } else if (msg.what == 4) {
                //updateMe(false);
            } else {
                super.handleMessage(msg);
            }

        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            MyLog.d("onServiceConnected");
            yyskService = IYyskService.Stub.asInterface(binder);
            if (yyskService != null) {
                try {
                    yyskService.addListener(serviceListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            updateVpnStatus();


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyLog.d("onServiceDisconnected");
            if (yyskService != null) {
                try {
                    yyskService.removeListener(serviceListener);
                } catch (RemoteException e) {
                    MyLog.e(e);
                }
            }
            yyskService = null;
            updateVpnStatus();
        }

        @Override
        public void onBindingDied(ComponentName name) {
            MyLog.d("onBindingDied");
            //对于同一个app，不应出现这个情况，如果是不同的app，另外一个app更新了，会出现，因为接口可能改变，需要
            //unbind和bind
            //应该会先发出onServiceDisconnected事件
        }
    };

    private IYyskServiceListener serviceListener = new IYyskServiceListener.Stub() {
        @Override
        public void onStatusChanged(int status) throws RemoteException {
            MyLog.d("onStatusChanged=%s",status);
            handler.sendEmptyMessage(1);
        }

        @Override
        public void onTrafficUpdate(long rxRate, long txRate, long rxTotal, long txTotal) throws RemoteException {
            MyLog.d("onTrafficUpdate=%s,%s,%s,%s" ,rxRate ,txRate ,rxTotal,txTotal);
            handler.sendMessage(handler.obtainMessage(2, new long[]{rxRate, txRate, rxTotal, txTotal}));
        }
    };

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setDefaultProxy(List<XBean> result) {
        //检查列表
        if(result == null || result.size() == 0){
            return;
        }
        //设置默认选择代理
        for(XBean item:result){
            String porxyID = item.getString(Proxy.BEANNAME_ID);
            if(porxyID != null && !porxyID.isEmpty()){
                //发出一个事件，然后HomeFragment就可以监听到了
                Proxy proxy = new Proxy();
                proxy.id = item.getString(Proxy.BEANNAME_ID);
                proxy.name = item.getString(Proxy.BEANNAME_NAME);
                proxy.data = item;
                proxy.isCustom = false;
                app.getSessionManager().setProxy(getActivity(), proxy, false, false);
                updateProxy(app.getSessionManager().newProxy());
                break;
            }
        }
    }

    private void getAndSetDefaultProxy(){
        final Session session = app.getSessionManager().getSession();
        if (session.isLogin()) {
            Proxy storedProxy = app.getSessionManager().getProxy();
            if(storedProxy == null){
                //获取公司代理列表
                app.getApi().getDZProxyList(session.user.mobile_number, new YyskApi.ICallback<XBean>() {
                    @Override
                    public void onResult(XBean result) {
                        if(result != null && result.hasKeys("data")){
                            List<XBean> porxList = result.getList("data");
                            if(porxList != null && porxList.size() > 0){
                                //缓存列表
                                app.getDzProxyManager().save(porxList);
                                //组装数据
                                for(XBean item:porxList){
                                    List<XBean> nodes = item.getList("nodes");
                                    if(nodes == null || nodes.size() == 0){
                                        continue;
                                    }else{
                                        //自动设置默认代理
                                        setDefaultProxy(nodes);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            }else{
                //startVPNWithServer();
            }
        }
    }
    /*---------------------------------------------------------------------*/
    private void stopPing(){
        if(ping!=null){
            ping.close();
            ping=null;
        }
        MyLog.d("-------------->>>>stopPing");
    }

    private void startPing(String host){
        stopPing();
        if(TextUtils.isEmpty(host)){
            return;
        }
        MyLog.d("-------------->>>>startPing：" + host);
        List<String> hosts = new ArrayList<>();
        hosts.add(host);
        ping = new Ping();
        ping.setCount(5);
        ping.setTimeout(30);
        ping.ping(hosts, new Ping.IPingListener() {
            @Override
            public void onTime(String host, String time, String hostName) {
                MyLog.d("----->>Ping["+host+"]：" + time + "ms");
                if(!TextUtils.isEmpty(time) && StringUtils.strIsFloat(time.trim())){
                    pingTime = Float.valueOf(time.trim());
                }else{
                    pingTime = -1;
                }
            }
        });
    }
    /*---------------------------------------------------------------------*/

    private boolean checkLogin(){
        if(!app.getSessionManager().getSession().isLogin()){
            getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            return false;
        }
        return true;
    }

    private void updateAcl(){
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("更新中...");
        dialog.show();
        //关闭连接
        stopVPNWithServer(null);
        //清空本地选择节点
        updateProxy(null);
        app.getSessionManager().setProxy(getActivity(),null, false, false);
        //清空列表缓存
        app.dzProxyManager.save(new ArrayList<XBean>());
        //更新规则
        app.getApi().getRuleList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRspWithData(result,getContext(),"更新规则失败",null)){
                    String listStr = result.getString("data");
                    if(listStr == null || listStr.isEmpty()){
                        dialog.dismiss();
                        return;
                    }
                    JSONArray jsonArray = JSON.parseArray(listStr);
                    if(jsonArray == null || jsonArray.size() == 0){
                        dialog.dismiss();
                        return;
                    }
                    new android.os.AsyncTask<JSONArray, Void, StringBuffer>(){
                        @Override
                        protected StringBuffer doInBackground(JSONArray... jsonArrays) {
                            JSONArray msgList = jsonArrays[0];
                            StringBuffer rst = new StringBuffer();
                            if(msgList == null || msgList.isEmpty()){
                                return rst;
                            }
                            for (Iterator iterator = msgList.iterator(); iterator.hasNext();) {
                                JSONObject item = (JSONObject) iterator.next();
                                String value = item.getString("value");
                                if(value == null || value.isEmpty()){
                                    continue;
                                }
                                if(rst.length() == 0){
                                    rst.append(value);
                                }else{
                                    rst.append("\n").append(value);
                                }
                            }
                            //保存规则数据
                            app.getSettings().set("acl", rst);
                            //返回数据
                            return rst;
                        }

                        @Override
                        protected void onPostExecute(StringBuffer text) {
                            dialog.dismiss();
                        }
                    }.execute(jsonArray);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(txv_chuxiao_name != null){
            txv_chuxiao_name.startAutoScroll();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(txv_chuxiao_name != null){
            txv_chuxiao_name.stopAutoScroll();
        }
    }
}
