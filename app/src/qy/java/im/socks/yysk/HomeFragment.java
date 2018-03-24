package im.socks.yysk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ServiceConnection;
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
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Proxy;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.ConfigUtils;
import im.socks.yysk.vpn.IYyskService;
import im.socks.yysk.vpn.IYyskServiceListener;
import im.socks.yysk.vpn.VpnConfig;


public class HomeFragment extends Fragment {

    private RelativeLayout vpnButton;
    private TextView txv_vpn_statu;

    //proxy part
    private View proxyView;
    private TextView proxyNameView;
    private TextView txv_vpn_update;
    private TextView txv_endtime;
    private Switch bypassChinaView;
    private View editAclView;
    private View testSpeedView;
    private boolean isTimeEnd = false;


    //me part
    private TextView phoneNumberView;
    private View loginLayout;
    private View phoneNumberLayout;
    private PageBar pageBar;

    //private long startTime;

    private final AppDZ app = Yysk.app;

    /**
     * true表示需要登录才可以连接，获得代理列表
     */
    //private final boolean requireLogin = false;

    private Handler mHandler;
    private static final int HANDLER_GOTO_LOGIN = 100002;
    private static final int HANDLER_UPDATE_ENDTIME = 100003;

    private Ping ping = null;
    private float pingTime = -1;

    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name)) {
                //updateMe(false);
                //checkVpnUpdate(false);
            } else if (Yysk.EVENT_LOGOUT.equals(name)) {
                //updateMe(false);
            } else if (Yysk.EVENT_PROXY_CHANGED.equals(name)) {
                //updateProxy((Proxy) data);
            }else if(Yysk.EVENT_PAY_SUCCESS.equals(name)||Yysk.EVENT_PAY_FAIL.equals(name)){
                ////充值成功或者失败都更新一次
                //updateMe(false);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_dz, container, false);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case HANDLER_GOTO_LOGIN:
                        loginLayout.callOnClick();
                        break;
                }
            }
        };

        initConnectLayout(view);

//        initProxyLayout(view);
//
//        initMe(view);
//
//        initRefreshLayout(view);
//
//        updateVpnStatus();
//
//        updateMe(false);

//        checkVpnUpdate(false);

        return view;
    }

    private void initRefreshLayout(View view){
        final SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //refreshlayout.finishRefresh(3000,true);
                updateVpnStatus();
                updateMe(false);
                //需要更新吗？
                updateProxy(app.getSessionManager().newProxy());
                refreshlayout.finishRefresh(true);
            }
        });
    }

    private void initConnectLayout(View view) {
        vpnButton = view.findViewById(R.id.vpnButton);
        txv_vpn_statu = view.findViewById(R.id.txv_vpn_statu);
        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTimeEnd == true){
                    showVPNAlert("加速时间已用完，请联系管理员");
                }else{
                    toggleVpn();
                }
            }
        });
        txv_endtime = view.findViewById(R.id.txv_endtime);
    }

    private void initMe(View view) {
        View meLayout = view.findViewById(R.id.meLayout);
        loginLayout = meLayout.findViewById(R.id.loginLayout);
        phoneNumberLayout = meLayout.findViewById(R.id.phoneNumberLayout);
        phoneNumberView = meLayout.findViewById(R.id.phoneNumberView);

        pageBar = view.findViewById(R.id.pageBar);

        loginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            }
        });
    }

    private void initProxyLayout(View view) {
        View proxyLayout = view.findViewById(R.id.proxyLayout);
        proxyView = proxyLayout.findViewById(R.id.proxyView);
        proxyNameView = proxyLayout.findViewById(R.id.nameView);
        proxyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(ProxyFragment.newInstance(),null,false);
            }
        });

        bypassChinaView = proxyLayout.findViewById(R.id.bypassChinaView);
        bypassChinaView.setChecked(app.getSettings().getData().getBoolean("bypass_china", VpnConfig.BYPASS_CHINA_DEFAULT));
        bypassChinaView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                app.getSettings().set("bypass_china", isChecked);
                app.getVpn().reload();

            }
        });

        editAclView = proxyLayout.findViewById(R.id.editAclView);
        editAclView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(AclEditorFragment.newInstance(), null, false);
            }
        });
        //
        updateProxy(app.getSessionManager().getProxy());

        testSpeedView = proxyLayout.findViewById(R.id.testSpeedView);
        testSpeedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.openUrl("https://fast.com");
            }
        });

        proxyLayout.findViewById(R.id.proxyUpdateView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkVpnUpdate(true);
            }
        });

        txv_vpn_update = proxyLayout.findViewById(R.id.txv_vpn_update);
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
            proxyNameView.setText(proxy.name);
            String host = proxy.data.getString("host");
            startPing(host);
        } else {
            proxyNameView.setText("请选择代理");
        }
    }

    private void updateMe(final boolean isManual) {
        Session session = app.getSessionManager().getSession();
//        if (!session.isLogin()) {
//            loginLayout.setVisibility(View.VISIBLE);
//            phoneNumberLayout.setVisibility(View.GONE);
//            //moneyLayout.setEnabled(true);
//            //mHandler.sendEmptyMessageDelayed(HANDLER_GOTO_LOGIN,1000);
//        } else {
//            phoneNumberView.setText(session.user.phoneNumber);
//            loginLayout.setVisibility(View.GONE);
//            phoneNumberLayout.setVisibility(View.VISIBLE);
//            if(!TextUtils.isEmpty(session.user.entername)){
//                pageBar.setPbTitle(session.user.entername);
//            }
//        }
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
            txv_vpn_statu.setText("您还未连接");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        } else if (status == Yysk.STATUS_CONNECTING) {
            txv_vpn_statu.setText("连接中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        } else if (status == Yysk.STATUS_STOPPING) {
            txv_vpn_statu.setText("停止中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
        } else if (status == Yysk.STATUS_CONNECTED) {
            showVPNAlert("VPN 开启");
            txv_vpn_statu.setText("已连接成功");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
        } else {
            //
            showVPNAlert("VPN 已断开");
            //不可能的
            txv_vpn_statu.setText("未知:" + status);
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        }
    }

    private void checkVpnUpdate(boolean isClick){
        final Session session = app.getSessionManager().getSession();
        if (session.isLogin()) {
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setCancelable(false);
            dialog.setMessage("正在刷新...");
            dialog.show();
            app.apiDZ.checkVpnUpdateVerson(session.user.phoneNumber, session.user.psw, new YyskApi.ICallback<XBean>() {
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
                            app.getApi().getDZProxyList(session.user.phoneNumber, new YyskApi.ICallback<List<XBean>>() {
                                @Override
                                public void onResult(List<XBean> result) {
                                    if(result != null && result.size() > 0){
                                        //缓存列表
                                        app.getDzProxyManager().save(result);
                                        //更新版本
                                        app.getSessionManager().onVpnVerCheck(vpnVersion,companyid);
                                        //自动设置默认代理
                                        setDefaultProxy(result);
                                    }
                                }
                            });
                            //提示线路更新
                            showVPNAlert("线路更新："+StringUtils.getNowTimeStr());
                            txv_vpn_update.setText("更新线路("+StringUtils.getNowTimeStr()+")");
                        }else{
                            showVPNAlert("线路无更新");
                            if(session.vpnUpdateTime > 0){
                                txv_vpn_update.setText("更新线路("+StringUtils.getTimeStr(session.vpnUpdateTime)+")");
                            }
                        }
                    }else{
                        showVPNAlert("线路无更新");
                    }
                    dialog.dismiss();
                }
            });
            //检查过期时间
            checkEndTime(session.user.phoneNumber);
        }else if(isClick == true){
            new AlertDialog.Builder(getContext())
                    .setTitle("提醒")
                    .setMessage("请先登录")
                    .setPositiveButton("确定",null)
                    .show();
        }
    }

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
                if(pingTime <= 0){
                    showVPNAlert("线路失效不可用，请联系客服");
                    return;
                }else{
                    //showVPNAlert("线路可用");
                }
                app.getVpn().start(getActivity());
            } else {
                //如果还没有proxy，就需要先选择
                getFragmentStack().show(ProxyFragment.newInstance(), null, false);
            }

        } else if (status == Yysk.STATUS_CONNECTING) {
            //
        } else if (status == Yysk.STATUS_CONNECTED) {
            app.getVpn().stop();
            showVPNAlert("VPN 已关闭");
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
                updateMe(false);
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
            if(item != null && !item.isEmpty("host") && !item.isEmpty("port")){
                Proxy proxy = new Proxy();
                proxy.name = item.getString("name");
                proxy.data = item;
                proxy.isCustom = false;
                app.getSessionManager().setProxy(getActivity(), proxy, false, false);
                updateProxy(app.getSessionManager().newProxy());
                break;
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
            public void onTime(String host, String time) {
                MyLog.d("----->>Ping["+host+"]：" + time + "ms");
                if(!TextUtils.isEmpty(time) && StringUtils.strIsFloat(time.trim())){
                    pingTime = Float.valueOf(time.trim());
                }else{
                    pingTime = -1;
                }
            }
        });
    }
}
