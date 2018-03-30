package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/23.
 */

public class MoneyFragment extends Fragment {
    //private boolean isViewDestroyed=false;

    private TextView moneyView;
    private TextView integralView;
    private TextView loadingView;
    private View contentView;

    private final App app = Yysk.app;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_money, container, false);
        loadingView = view.findViewById(R.id.loadingView);

        contentView = view.findViewById(R.id.contentView);
        moneyView = view.findViewById(R.id.moneyView);
        integralView = view.findViewById(R.id.integralView);

        loadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        loadData();
        return view;
    }


    private void loadData() {
        loadingView.setText("正在查询...");
        loadingView.setVisibility(View.VISIBLE);
        loadingView.setEnabled(false);

        contentView.setVisibility(View.GONE);

        String phoneNumber = app.getSessionManager().getSession().user.mobile_number;

        app.getApi().getUserProfile(phoneNumber, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                //
                Log.e("Yysk", "getUserProfile=" + result);
                //result={"money":"126.17","integral":"943","uuid":"250035","retcode":"succ", "error":""}
                if (result != null) {
                    if (result.isEquals("retcode", "succ")) {
                        //金币
                        String money = result.getString("money");
                        //积分
                        String integral = result.getString("integral");

                        moneyView.setText("金币 " + money);
                        integralView.setText("积分 " + integral);
                        loadingView.setVisibility(View.GONE);
                        contentView.setVisibility(View.VISIBLE);
                    } else {
                        //显示错误信息
                        String error = result.getString("error", null);
                        loadingView.setEnabled(true);
                        loadingView.setText("查询失败，原因：" + error + "，点击再次查询");
                    }
                } else {
                    //api调用失败，或者网络错误
                    loadingView.setEnabled(true);
                    loadingView.setText("查询失败，请检查网络后，再次查询");
                }
            }
        });
    }

    public static MoneyFragment newInstance() {
        MoneyFragment fragment = new MoneyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
