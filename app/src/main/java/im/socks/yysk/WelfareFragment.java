package im.socks.yysk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.XBean;


public class WelfareFragment extends Fragment {
    private Button integralShopButton;

    private Button watchVideoButton;

    private Button inviteButton;

    private Button shareButton;

    private TextView inviteCodeView;


    private TextView welfareExplainView;

    private RewardedVideoAd rewardedVideoAd;

    private AdView adView;

    private ViewGroup adLayout;

    private final App app = Yysk.app;

    private String videoID;
    private String bannerID;
    private String appID;

    private ProgressDialog progressDialog;

    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name)) {
                updateInviteCode(null);
            } else if (Yysk.EVENT_LOGOUT.equals(name)) {
                updateInviteCode(null);
            } else {

            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstaneState) {
        View view = inflater.inflate(R.layout.fragment_welfare, container, false);
        integralShopButton = view.findViewById(R.id.integralShopView);
        integralShopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openIntegralShop();
            }
        });

        welfareExplainView = view.findViewById(R.id.welfareExplainView);

        watchVideoButton = view.findViewById(R.id.watchVideoView);
        inviteButton = view.findViewById(R.id.inviteView);
        inviteCodeView = view.findViewById(R.id.inviteCodeView);
        shareButton = view.findViewById(R.id.shareView);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doShare();
            }
        });
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doInvite();
            }
        });
        //videoButton.setEnabled(false);
        watchVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rewardIntegral("video");
                openVideo();
            }
        });


        adLayout = view.findViewById(R.id.adLayout);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("正在加载视频...");


        initRefreshLayout(view);

        updateAdmob();
        updateInviteCode(null);

        app.getEventBus().on(Yysk.EVENT_ALL, eventListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        app.getEventBus().un(Yysk.EVENT_ALL, eventListener);
    }

    private boolean checkLogin() {
        if (!app.getSessionManager().getSession().isLogin()) {
            getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            return false;
        } else {
            return true;
        }
    }

    private void initRefreshLayout(View view) {
        SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                updateInviteCode(null);
                updateAdmob();
                //因为速度很快，就马上关闭了
                refreshlayout.finishRefresh(true);
                //refreshlayout.finishRefresh(3000,true);

            }
        });
    }

    private void updateAdmob() {
        if (appID != null && appID.length() > 0) {
            //表示已经载入了
            return;
        }
        //表示为测试的
        app.getApi().getAdmob(new YyskApi.ICallback<List<XBean>>() {
            @Override
            public void onResult(List<XBean> result) {
                MyLog.d("getAdmob=%s", result);
                if (result != null) {
                    initAdmob(result);
                } else {
                    //如果初始化失败?
                    showError("广告初始化失败");
                }
            }
        });

    }

    private void initAdmob(List<XBean> result) {
        for (XBean item : result) {
            if (item.isEquals("name", "appID")) {
                appID = item.getString("value");
            } else if (item.isEquals("name", "bannerID")) {
                bannerID = item.getString("value");
            } else if (item.isEquals("name", "videoID")) {
                videoID = item.getString("value");
            } else if (item.isEquals("name", "welfareExplain")) {
                String welfareExplain = item.getString("value");
                //找到view，然后设置即可
                welfareExplainView.setText(removeBlankLines(welfareExplain));
            } else {
                //
            }
        }


        MyLog.d("appId=%s,videoId=%s,bannerId=%s", appID, videoID, bannerID);
        //初始化
        MobileAds.initialize(getContext().getApplicationContext(), appID);

        adView = new AdView(getContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(bannerID);


        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        adLayout.addView(adView, lp);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                //现在不会发出，仅仅对native ad
                MyLog.d("onAdClicked");
            }

            @Override
            public void onAdOpened() {
                MyLog.d("onAdOpened");
                rewardIntegral("advert");
            }

            @Override
            public void onAdClosed() {
                MyLog.d("onAdClosed");
            }
        });
        adView.loadAd(createAdRequest());

        //初始化成功，就可以创建了
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewarded(RewardItem reward) {
                MyLog.d("onRewarded,type=%s,amount=%s", reward.getType(), reward.getAmount());
                progressDialog.dismiss();
                // Reward the user
                rewardIntegral("video");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                MyLog.d("onRewardedVideoAdLeftApplication");
                progressDialog.dismiss();
            }

            @Override
            public void onRewardedVideoAdClosed() {
                MyLog.d("onRewardedVideoAdClosed");
                progressDialog.dismiss();
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                progressDialog.dismiss();
                MyLog.d("onRewardedVideoAdFailedToLoad,errorCode=%s", errorCode);
                if (errorCode == AdRequest.ERROR_CODE_NO_FILL) {
                    //暂时没有广告返回
                    Toast.makeText(getContext(), "暂时没有视频可以观看", Toast.LENGTH_SHORT).show();
                } else {
                    //其它错误一样显示这个原因
                    Toast.makeText(getContext(), "暂时没有视频可以观看", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onRewardedVideoAdLoaded() {
                MyLog.d("onRewardedVideoAdLoaded");
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    rewardedVideoAd.show();
                } else {
                    //如果用户取消了等待，就不显示了
                    //rewardedVideoAd.show();
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onRewardedVideoAdOpened() {
                MyLog.d("onRewardedVideoAdOpened");
            }

            @Override
            public void onRewardedVideoStarted() {
                MyLog.d("onRewardedVideoStarted");
            }
        });

    }

    /**
     * 对返回的福利说明，去掉空白行，否则占用的空间太多了
     *
     * @param s
     * @return
     */
    private String removeBlankLines(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        BufferedReader reader = new BufferedReader(new StringReader(s));
        String line = null;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    builder.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            MyLog.e(e);
        }
        return builder.toString();
    }


    private AdRequest createAdRequest() {
        AdRequest.Builder builder = new AdRequest.Builder();
        return builder.build();
    }


    private void updateInviteCode(final YyskApi.ICallback<String> cb) {
        inviteCodeView.setText("");
        if (app.getSessionManager().getSession().isLogin()) {
            String phoneNumber = app.getSessionManager().getSession().user.phoneNumber;
            app.getApi().getInviteInfo(phoneNumber, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    MyLog.d("getInviteInfo=%s", result);
                    if (result != null) {
                        inviteCodeView.setText(result.getString("usersk"));
                        //shareButton.setEnabled(true);
                        if (cb != null) {
                            cb.onResult(result.getString("usersk"));
                        }
                    } else {
                        if (cb != null) {
                            cb.onResult(null);
                        }
                    }

                }
            });
        } else {
            //没有登录
            inviteCodeView.setText("");
            if (cb != null) {
                cb.onResult(null);
            }
        }

    }

    private void doInvite() {
        if (checkLogin()) {
            getFragmentStack().show(InviteFragment.newInstance(), null, false);
        }

    }

    private void doShare() {
        if (!checkLogin()) {
            return;
        }

        String code = inviteCodeView.getText().toString();
        if (code == null || code.isEmpty()) {
            //就先获得code，然后
            updateInviteCode(new YyskApi.ICallback<String>() {
                @Override
                public void onResult(String code) {
                    if (code != null && code.length() > 0) {
                        share(code);
                    } else {
                        showError("获得邀请码失败");
                    }
                }
            });
        } else {
            share(code);
        }


    }

    private void share(String code) {
        String text = "雨燕梭客 - 为您提供极智的网络加速服务。我们采用SS/SSR协议，基于Socks5代理，能够让您以低廉的价格获得畅爽的YouTube/Facebook等体验，助您享受自由的小天地，官网http://ganme.info/，【邀请码：" + code + "】";
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, "分享邀请码"));
    }

    private void openVideo() {
        if (!checkLogin()) {
            return;
        }
        if (appID == null || appID.isEmpty()) {
            showError("正在执行初始化操作，请稍后再试");
            updateAdmob();
            return;
        }
        if (rewardedVideoAd != null) {
            //因为载入的很慢
            if (rewardedVideoAd.isLoaded()) {
                progressDialog.dismiss();
                rewardedVideoAd.show();
            } else {
                rewardedVideoAd.loadAd(videoID, createAdRequest());
                progressDialog.show();
            }
        } else {
            //没有准备好，再次获得？
            //getAdmob();

        }
    }


    @Override
    public void onResume() {
        if (adView != null) {
            adView.resume();
        }

        if (rewardedVideoAd != null) {
            rewardedVideoAd.resume(getContext());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }

        if (rewardedVideoAd != null) {
            rewardedVideoAd.pause(getContext());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy(getContext());
        }
        super.onDestroy();
    }

    private void rewardIntegral(String clicktype) {
        Session session = app.getSessionManager().getSession();
        if (session.isLogin()) {
            String phoneNumber = session.user.phoneNumber;
            //video,advert,sign
            app.getApi().rewardIntegral(phoneNumber, clicktype, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    MyLog.d("rewardIntegral=%s", result);
                    if (result != null) {
                        showError(result.getString("error"));
                    } else {
                        //showError("请检查网络后再次尝试");
                    }
                }
            });
        } else {
            //getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
        }

    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void openIntegralShop() {
        if (!checkLogin()) {
            return;
        }

        Session session = app.getSessionManager().getSession();
        String phoneNumber = session.user.phoneNumber;
        app.getApi().getIntegralShopUrl(phoneNumber, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("getIntegralShopUrl=%s", result);
                if (result != null) {
                    //不返回retcode
                    String url = result.getString("url", null);
                    if (url != null) {
                        app.openUrl(url);
                        //打开成功，获得积分
                        rewardIntegral("sign");
                    } else {
                        showError("打开积分商店失败：" + result.getString("error"));
                    }
                } else {
                    //服务器错误，或者没有网络
                    showError("打开积分商店失败，请检查网络后再次尝试");
                }
            }
        });


    }

    private void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public static WelfareFragment newInstance() {
        WelfareFragment fragment = new WelfareFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
