package im.socks.yysk;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/22.
 */

public class LoginFragment extends Fragment {

    private EditText phoneNumberText;
    private EditText passwordText;


    private final App app = Yysk.app;

    private String nextAction;

    private boolean isLoading = false;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            nextAction = args.getString("next_action", null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        View registerButton = view.findViewById(R.id.registerButton);
        View forgetPasswordButton = view.findViewById(R.id.forgetPasswordButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(RegisterFragment.newInstance(), null, false);
            }
        });
        forgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(ForgetPasswordFragment.newInstance(), null, false);
            }
        });


        View loginButton = view.findViewById(R.id.loginButton);
        phoneNumberText = view.findViewById(R.id.phoneNumberView);
        passwordText = view.findViewById(R.id.passwordView);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });


        //for test
        //phoneNumberText.setText("18011353062");
        //passwordText.setText("12345678a");

        return view;
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void doLogin() {

        final String phoneNumber = phoneNumberText.getText().toString();
        final String password = passwordText.getText().toString();

        YyskApi api = app.getApi();

        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("正在登录...");
        dialog.show();


        api.login(phoneNumber, password, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("login=%s",result);
                dialog.dismiss();
                if (result != null) {
                    //登录成功后什么都不返回，正常的实现应该返回一个token，然后其他操作都通过这个token来调用api
                    //
                    if (result.isEquals("retcode", "succ")) {
                        //
                        app.getSessionManager().onLogin(result.getString("uuid"), phoneNumber);

                        //当前的fragment不需要保留在stack了，所以为替代
                        if ("show_money".equals(nextAction)) {
                            getFragmentStack().show(MoneyFragment.newInstance(), null, true);
                        } else {
                            getFragmentStack().back();
                        }

                    } else {
                        //登录失败，显示错误信息
                        showError("登录失败：" + result.getString("error"));
                    }
                } else {
                    //登录失败，主要是api调用失败
                    showError("登录失败，请检查你的本地网络是否通畅，或是登录服务器故障需要恢复后重新尝试登录");
                }

            }
        });

    }

    private void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param nextAction 表示登录成功后，执行什么操作，如果为null，表示不执行，现在仅仅支持null，show_money
     * @return
     */
    public static LoginFragment newInstance(String nextAction) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString("next_action", nextAction);
        fragment.setArguments(args);
        return fragment;
    }
}
