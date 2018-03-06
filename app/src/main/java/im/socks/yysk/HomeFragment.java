package im.socks.yysk;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Timer;
import java.util.TimerTask;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Proxy;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.FormatUtil;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.IYyskService;
import im.socks.yysk.vpn.IYyskServiceListener;


public class HomeFragment extends Fragment {

    private TextView vpnButton;

    //proxy part
    private View proxyView;
    private TextView proxyNameView;
    private Switch bypassChinaView;
    private View editAclView;


    //me part
    private TextView moneyView;
    private TextView phoneNumberView;
    private View loginLayout;
    private View phoneNumberLayout;
    private View moneyLayout;


    //统计
    private TextView txView;
    private TextView rxView;
    private TextView txRateView;
    private TextView rxRateView;
    private TextView startTimeView;
    private TextView elapsedTimeView;

    //private long startTime;

    private final App app = Yysk.app;

    /**
     * true表示需要登录才可以连接，获得代理列表
     */
    //private final boolean requireLogin = false;


    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name)) {
                updateMe(false);
            } else if (Yysk.EVENT_LOGOUT.equals(name)) {
                updateMe(false);
            } else if (Yysk.EVENT_PROXY_CHANGED.equals(name)) {
                updateProxy((Proxy) data);
            }else if(Yysk.EVENT_PAY_SUCCESS.equals(name)||Yysk.EVENT_PAY_FAIL.equals(name)){
                //充值成功或者失败都更新一次
                updateMe(false);
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initConnectLayout(view);

        initProxyLayout(view);

        initMe(view);

        //统计
        initStatLayout(view);

        updateVpnStatus();

        updateMe(false);


        initRefreshLayout(view);

        return view;
    }

    private void initRefreshLayout(View view){
        final SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                //refreshlayout.finishRefresh(3000,true);
                updateVpnTime();
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
        vpnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVpn();
            }
        });
    }

    private void initMe(View view) {
        View meLayout = view.findViewById(R.id.meLayout);
        loginLayout = meLayout.findViewById(R.id.loginLayout);
        phoneNumberLayout = meLayout.findViewById(R.id.phoneNumberLayout);
        moneyLayout = meLayout.findViewById(R.id.moneyLayout);

        phoneNumberView = meLayout.findViewById(R.id.phoneNumberView);
        moneyView = meLayout.findViewById(R.id.moneyView);


        loginLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            }
        });
        moneyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMe(true);
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
        bypassChinaView.setChecked(app.getSettings().getData().getBoolean("bypass_china", false));
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
    }

    private void initStatLayout(View view) {

        View statLayout = view.findViewById(R.id.statLayout);
        txView = statLayout.findViewById(R.id.txView);
        rxView = statLayout.findViewById(R.id.rxView);
        txRateView = statLayout.findViewById(R.id.txRateView);
        rxRateView = statLayout.findViewById(R.id.rxRateView);
        startTimeView = statLayout.findViewById(R.id.startTimeView);
        elapsedTimeView = statLayout.findViewById(R.id.elapsedTimeView);

        updateVpnTime();
        updateVpnStat(0, 0, 0, 0);
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

        app.getVpn().unbind(serviceConnection);
        app.getEventBus().un(Yysk.EVENT_ALL, eventListener);

    }


    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }


    private void updateProxy(Proxy proxy) {
        if (proxy != null) {
            //设置text
            proxyNameView.setText(proxy.name);
        } else {
            proxyNameView.setText("请选择代理");
        }
    }

    private void updateMe(final boolean isManual) {
        Session session = app.getSessionManager().getSession();
        if (!session.isLogin()) {

            loginLayout.setVisibility(View.VISIBLE);
            phoneNumberLayout.setVisibility(View.GONE);
            moneyLayout.setVisibility(View.GONE);

            moneyView.setTag(R.id.tx_id, null);
            moneyView.setText("");
            //moneyLayout.setEnabled(true);
        } else {
            phoneNumberView.setText(session.user.phoneNumber);
            loginLayout.setVisibility(View.GONE);
            phoneNumberLayout.setVisibility(View.VISIBLE);
            moneyLayout.setVisibility(View.VISIBLE);

            if (moneyView.getTag(R.id.tx_id) != null) {
                //表示正在查询,不需要处理
                return;
            }

            //如果已经登录，就可以执行操作，为了安全，应该需要添加一个token验证的
            final Long txId = System.currentTimeMillis();
            moneyView.setTag(R.id.tx_id, txId);
            if (isManual) {
                moneyView.setText("正在查询...");
                //moneyLayout.setEnabled(false);
            }

            app.getApi().getUserProfile(session.user.phoneNumber, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    if (!txId.equals(moneyView.getTag(R.id.tx_id))) {
                        return;
                    }
                    moneyView.setTag(R.id.tx_id, null);
                    //moneyLayout.setEnabled(true);
                    if (result != null) {
                        if (result.isEquals("retcode", "succ")) {
                            moneyView.setText(result.getString("money") + " 金币");
                        } else {
                            //获得失败
                            if (isManual) {
                                moneyView.setText("查询失败");
                            }
                        }
                    } else {
                        //查询失败
                        if (isManual) {
                            moneyView.setText("查询失败");
                        }

                    }
                }
            });
        }
    }

    private void updateVpnTime() {
        Context context = getContext();
        long startTime = 0;
        if (yyskService != null) {
            try {
                startTime = yyskService.getStartTime();
            } catch (RemoteException e) {
                MyLog.e(e);
                startTime = 0;
            }
        }

        if (startTime > 0) {
            startTimeView.setText(FormatUtil.formatDate(context, startTime));
            elapsedTimeView.setText(FormatUtil.formatDuration(context, System.currentTimeMillis() - startTime));
        } else {
            startTimeView.setText("");
            elapsedTimeView.setText("");
        }
    }

    private void updateVpnStat(long rxRate, long txRate, long rxTotal, long txTotal) {
        Context context = getContext();

        rxView.setText(FormatUtil.formatSize(context, rxTotal));
        txView.setText(FormatUtil.formatSize(context, txTotal));//KB,

        rxRateView.setText(FormatUtil.formatRate(context, rxRate));
        txRateView.setText(FormatUtil.formatRate(context, txRate));
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
            vpnButton.setText("连接");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
            updateVpnStat(0, 0, 0, 0);
        } else if (status == Yysk.STATUS_CONNECTING) {
            vpnButton.setText("连接中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        } else if (status == Yysk.STATUS_STOPPING) {
            vpnButton.setText("停止中...");
            vpnButton.setEnabled(false);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
        } else if (status == Yysk.STATUS_CONNECTED) {
            vpnButton.setText("停止");
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_on);
            updateVpnStat(0, 0, 0, 0);
        } else {
            //不可能的
            vpnButton.setText("未知:" + status);
            vpnButton.setEnabled(true);
            vpnButton.setBackgroundResource(R.drawable.vpn_button_off);
        }


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
                app.getVpn().start(getActivity());
            } else {
                //如果还没有proxy，就需要先选择
                getFragmentStack().show(ProxyFragment.newInstance(), null, false);
            }

        } else if (status == Yysk.STATUS_CONNECTING) {
            //
        } else if (status == Yysk.STATUS_CONNECTED) {
            app.getVpn().stop();
        } else {
            //
        }
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
                updateVpnStat(args[0], args[1], args[2], args[3]);
            } else if (msg.what == 3) {
                updateVpnTime();
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

}
