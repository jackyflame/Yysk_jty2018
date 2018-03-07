package im.socks.yysk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.XBean;


public class MyFragment extends Fragment implements View.OnClickListener {
    //
    private View loginLayout;
    private View phoneNumberLayout;
    private TextView phoneNumberView;
    private TextView loginView;
    private TextView siteView;
    //
    private TextView feedbackView;
    private TextView versionView;
    private View versionLayout;


    //
    private Button logoutView;
    //
    //private boolean isViewDestroyed=false;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_dz, container, false);
        loginLayout = view.findViewById(R.id.loginLayout);
        phoneNumberLayout = view.findViewById(R.id.phoneNumberLayout);

        loginView = view.findViewById(R.id.loginView);
        phoneNumberView = view.findViewById(R.id.phoneNumberView);
        siteView = view.findViewById(R.id.siteView);
        //
        feedbackView = view.findViewById(R.id.feedbackView);
        //contactView = view.findViewById(R.id.contactView);
        versionView = view.findViewById(R.id.versionView);
        versionLayout = view.findViewById(R.id.versionLayout);

        //qqView = view.findViewById(R.id.qqView);
        //
        logoutView = view.findViewById(R.id.logoutView);
        //
        loginView.setOnClickListener(this);
        phoneNumberView.setOnClickListener(this);
        siteView.setOnClickListener(this);
        //
        feedbackView.setOnClickListener(this);
        versionLayout.setOnClickListener(this);
        //
        logoutView.setOnClickListener(this);

        versionView.setText(getVersion());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adjustIconSize(loginView);
        adjustIconSize(phoneNumberView);
        adjustIconSize(siteView);
        //
        adjustIconSize(feedbackView);
        //adjustIconSize(contactView);
        adjustIconSize((TextView) view.findViewById(R.id.versionLabelView));
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
        if (id == R.id.loginView) {
            //or show_money
            getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
        } else if (id == R.id.phoneNumberView) {
            //getFragmentStack().show(MoneyFragment.newInstance(), null, false);
        } else if (id == R.id.userIdView) {
            //
        } else if (id == R.id.siteView) {
            openWebSite();
        } else if (id == R.id.feedbackView) {
            openFeedback();
        }
        else if (id == R.id.notifyView) {
            //showError("显示通知，未实现");
            openNotify();
        } else if (id == R.id.hostView) {
            //域名设置
            showHostDialog();
        } else if (id == R.id.versionLayout) {
            checkUpdate();
        } else if (id == R.id.logoutView) {
            doLogout();
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
            loginLayout.setVisibility(View.VISIBLE);
            phoneNumberLayout.setVisibility(View.GONE);
            logoutView.setEnabled(false);
        } else {
            loginLayout.setVisibility(View.GONE);
            phoneNumberLayout.setVisibility(View.VISIBLE);
            phoneNumberView.setText(session.user.phoneNumber);
            logoutView.setEnabled(true);
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

    private void openFeedback() {
        //String url = "https://kefu.easemob.com/webim/im.html?configId=3806da3a-1fa7-4e6b-bcc2-6617529c088d";
        String url = "https://webchat.7moor.com/wapchat.html?accessId=559eecd0-c91e-11e7-8178-2573f743b2b9&fromUrl=android";
        app.openUrl(url);
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
        String phoneNumber = session.user.phoneNumber;
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
        //不需要调用api
        app.getSessionManager().onLogout();
    }

    public static MyFragment newInstance() {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
