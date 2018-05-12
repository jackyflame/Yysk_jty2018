package im.socks.yysk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;


public class MyFragment extends Fragment implements View.OnClickListener {
    //
    private View loginLayout;
    private View phoneNumberLayout;
    private TextView phoneNumberView;
    private TextView txv_packge_title;
    private PageBar title_bar;
    //
    private View logoutView;
    private TextView txv_unread;
    //
    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name) || Yysk.EVENT_LOGOUT.equals(name)) {
                syncLoginStatus();
            }
        }
    };

    private final AppDZ app = Yysk.app;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_dz, container, false);
        loginLayout = view.findViewById(R.id.loginLayout);
        phoneNumberLayout = view.findViewById(R.id.phoneNumberLayout);
        txv_packge_title = view.findViewById(R.id.txv_packge_title);
        phoneNumberView = view.findViewById(R.id.phoneNumberView);
        loginLayout.setOnClickListener(this);
        phoneNumberView.setOnClickListener(this);
        txv_unread = view.findViewById(R.id.txv_unread);
        //
        logoutView = view.findViewById(R.id.logoutView);
        logoutView.setOnClickListener(this);
        //标题退出
        title_bar = view.findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().back();
            }
        });
        //修改密码
        view.findViewById(R.id.btn_change_psw).setOnClickListener(this);
        view.findViewById(R.id.btn_feedback).setOnClickListener(this);
        view.findViewById(R.id.btn_msgs).setOnClickListener(this);
        view.findViewById(R.id.btn_devices).setOnClickListener(this);
        view.findViewById(R.id.btn_help_center).setOnClickListener(this);
        //刷新未读数
        refreshUnreadMsg();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //adjustIconSize(loginView);
        //adjustIconSize(phoneNumberView);
        //adjustIconSize(siteView);
        //
        //adjustIconSize(contactView);
        //adjustIconSize((TextView) view.findViewById(R.id.versionLabelView));
    }

    private void adjustIconSize(TextView view) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        Drawable[] drawables = view.getCompoundDrawables();
        Drawable drawable = drawables[0];
        drawable.setBounds(0, 0, px, px);
        view.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    @Override
    public void onStart() {
        super.onStart();

        syncLoginStatus();

        app.getEventBus().on(Yysk.EVENT_LOGIN, eventListener);
        app.getEventBus().on(Yysk.EVENT_LOGOUT, eventListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        app.getEventBus().un(Yysk.EVENT_LOGIN, eventListener);
        app.getEventBus().un(Yysk.EVENT_LOGOUT, eventListener);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.loginLayout) {
            //or show_money
            if(app.getSessionManager().getSession().isLogin() == false){
                getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            }
        } else if (id == R.id.phoneNumberView) {
            //getFragmentStack().show(MoneyFragment.newInstance(), null, false);
        } else if (id == R.id.btn_help_center) {
            startActivity(new Intent(getContext(),HelpActivity.class));
        } else if (id == R.id.btn_devices) {
            startActivity(new Intent(getContext(),DevicesActivity.class));
        }else if (id == R.id.btn_msgs) {
            startActivity(new Intent(getContext(),SystemMsgActivity.class));
        } else if (id == R.id.btn_feedback) {
            startActivity(new Intent(getContext(),FeedbackLisActivity.class));
            //String url = "https://webchat.7moor.com/wapchat.html?accessId=559eecd0-c91e-11e7-8178-2573f743b2b9&fromUrl=android";
            //app.openUrl(url);
        } else if (id == R.id.versionLayout) {
            startActivity(new Intent(getContext(),SystemMsgActivity.class));
        } else if (id == R.id.logoutView) {
            doLogout();
        } else if (id == R.id.btn_change_psw) {
            startActivity(new Intent(getContext(),ChangePswActivity.class));
        }
    }

    private void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void showHostDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("域名设置");
        builder.setMessage("如果默认的域名不能够正常访问服务端，可以手动设置一个。");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("修改", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                getFragmentStack().show(ApiServerEditorFragment.newInstance(), null, false);
            }
        });
        builder.setNeutralButton("恢复默认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                app.getSettings().set("api_server", null);

            }
        });
        builder.show();
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private String getVersion() {
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void syncLoginStatus() {
        Session session = app.getSessionManager().getSession();
        if (!session.isLogin()) {
            phoneNumberView.setVisibility(View.GONE);
            logoutView.setEnabled(false);
            txv_packge_title.setText("请点击登录");
        } else {
            phoneNumberView.setVisibility(View.VISIBLE);
            phoneNumberView.setText(session.user.mobile_number);
            logoutView.setEnabled(true);
            txv_packge_title.setText("");
            refreshUserInfo();
        }
    }

    private void openNotify() {
        app.getApi().getAnnoUrl(new YyskApi.ICallback<String>() {
            @Override
            public void onResult(String url) {
                Log.e("Yysk", "getAnnoUrl=" + url);
                if (url != null) {
                    app.openUrl(url);
                } else {
                    showError("不能够获得系统消息的地址，请稍后再试");
                }
            }
        });

    }

    private void checkUpdate(){
        app.checkUpdateDZ(getActivity(),false);
    }

    /**
     * 打开官网
     */
    private void openWebSite() {
        //siteView.setEnabled(false);
        Session session = app.getSessionManager().getSession();
        if (session.isLogin()) {
            //如果没有登录?
        }
        String phoneNumber = session.user.mobile_number;
        app.getApi().getSiteUrl(phoneNumber, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                Log.e("Yysk", "getSiteUrl=" + result);
                if (result != null) {
                    String url = result.getString("url");
                    app.openUrl(url);
                } else {
                    showError("不能够获得官网的地址，请稍后再试");
                }
            }
        });

    }

    private void doLogout() {
        AlertDialog.Builder normalDialog =  new AlertDialog.Builder(getContext());
        normalDialog.setTitle("提示");
        normalDialog.setMessage("确认退出登录？");
        normalDialog.setPositiveButton("确定",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //不需要调用api
                app.getSessionManager().onLogout();
                dialog.dismiss();
                getFragmentStack().show(LoginFragment.newInstance(null),"login",true);
            }
        });
        normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // 显示
        normalDialog.show();
    }

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void refreshUserInfo(){
        if(app.getSessionManager().isUserInfoNeedUdate()){
            app.getApi().getUserInfo(new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    if(NetUtil.checkAndHandleRsp(result,getContext(),"获取个人信息失败",null)){
                        XBean userInfo = NetUtil.getRspData(result);
                        app.getSessionManager().onUserInfoUpdate(userInfo);
                        XBean packgeInfo = userInfo.getXBean("tariff_package");
                        txv_packge_title.setText(packgeInfo.getString("name"));
                    }
                }
            });
        }else{
            XBean userInfo = app.getSessionManager().getSession().user.toJson();
            XBean packgeInfo = userInfo.getXBean("tariff_package");
            if(packgeInfo != null){
                txv_packge_title.setText(packgeInfo.getString("name"));
            }
        }
    }

    private void refreshUnreadMsg(){
        app.getApi().getMsgList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,getContext(),"获取公告失败",null)){
                    List<XBean> dataList = NetUtil.getRspDataList(result);
                    new AsyncTask<List<XBean>, Void, Integer>(){
                        @Override
                        protected Integer doInBackground(List<XBean>... lists) {
                            List<XBean> msglist = lists[0];
                            if(msglist == null || msglist.isEmpty()){
                                return 0;
                            }
                            int count = 0;
                            for(XBean item:msglist){
                                boolean isReaded = item.getBoolean("is_read",true);
                                if(isReaded == false){
                                    count++;
                                }
                            }
                            return count;
                        }
                        @Override
                        protected void onPostExecute(Integer count) {
                            if(count != null && count > 0){
                                txv_unread.setVisibility(View.VISIBLE);
                                txv_unread.setText(String.valueOf(count));
                            }else{
                                txv_unread.setVisibility(View.GONE);
                            }
                        }
                    }.execute(dataList);
                }
            }
        });
    }
}
