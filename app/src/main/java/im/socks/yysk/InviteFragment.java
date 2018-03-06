package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/11/9.
 */

public class InviteFragment extends Fragment {
    //private Button shareView;
    private Button submitView;
    // private TextView descView;
    //private TextView inviteCodeView;
    private TextView inputInviteCodeView;

    private final App app = Yysk.app;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        //shareView = view.findViewById(R.id.shareView);
        submitView = view.findViewById(R.id.submitView);
        //descView = view.findViewById(R.id.descView);
        //inviteCodeView = view.findViewById(R.id.inviteCodeView);
        inputInviteCodeView = view.findViewById(R.id.inputInviteCodeView);


        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });

        //getInviteCode();

        return view;
    }

    private void doSubmit() {

        String phoneNumber = app.getSessionManager().getSession().user.phoneNumber;
        String code = inputInviteCodeView.getText().toString();
        app.getApi().submitInviteCode(phoneNumber, code, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("submitInviteCode=%s",result);
                if (result != null) {
                    if (result.isEquals("retcode", "200")) {
                        //成功
                    } else {
                        //失败
                    }

                    Toast.makeText(getContext(), result.getString("error"), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getContext(), "提交失败，请检查网络后再次尝试", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public static InviteFragment newInstance() {
        InviteFragment fragment = new InviteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
