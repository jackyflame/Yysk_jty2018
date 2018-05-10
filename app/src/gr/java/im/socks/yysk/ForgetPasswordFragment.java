package im.socks.yysk;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.CodeUtils;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;
import im.socks.yysk.util.XRspBean;

/**
 * Created by cole on 2017/10/23.
 */

public class ForgetPasswordFragment extends Fragment {

    private PageBar title_bar;
    private EditText phoneNumberView;
    private EditText verifyCodeView;
    private EditText passwordView;

    private EditText edt_imgcode;
    private ImageView img_code;
    private String imageCode;

    private Button getVerifyCodeView;
    private Button submitView;

    private final AppDZ app = Yysk.app;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_password_dz, container, false);

        title_bar = view.findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().back();
            }
        });

        phoneNumberView = view.findViewById(R.id.phoneNumberView);
        verifyCodeView = view.findViewById(R.id.verifyCodeView);
        passwordView = view.findViewById(R.id.passwordView);
        submitView = view.findViewById(R.id.submitView);

        edt_imgcode = view.findViewById(R.id.edt_imgcode);
        img_code = view.findViewById(R.id.img_code);
        img_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshImageCode();
            }
        });
        refreshImageCode();

        getVerifyCodeView = view.findViewById(R.id.getVerifyCodeView);
        getVerifyCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifyCode();
            }
        });


        //submitView.setEnabled(false);
        submitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        return view;
    }

    private void showError(String msg) {
        //或者显示对话框
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void sendVerifyCode() {
        if(edt_imgcode.getText() == null || edt_imgcode.getText().length() <= 0){
            showError("请输入图形验证码");
            return;
        }
        String imgCodeInput = edt_imgcode.getText().toString();
        if(!imgCodeInput.equalsIgnoreCase(imageCode)){
            showError("图形验证码错误");
            return;
        }
        if(phoneNumberView.getText() == null || phoneNumberView.getText().length() <= 0){
            showError("请输电话号码");
            return;
        }
        String phoneNumber = phoneNumberView.getText().toString();
        //检查手机号是否正确
        getVerifyCodeView.setEnabled(false);
        app.getApi().getVerifyCode(phoneNumber,true,new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("getVerifyCode=%s",result);
                getVerifyCodeView.setEnabled(true);
                if(NetUtil.checkAndHandleRsp(result,getContext(),"发送验证码失败",null)){
                    showError("发送验证码成功，请查收短信");
                }
            }
        });
    }

    private void doSubmit() {
        final String phoneNumber = phoneNumberView.getText().toString();
        final String newPassword = passwordView.getText().toString();
        String verifyCode = verifyCodeView.getText().toString();

        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("正在修改密码...");
        dialog.show();
        app.getApi().findBackPassword(phoneNumber, newPassword, verifyCode, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("changePassword=%s",result);
                if (NetUtil.checkAndHandleRsp(result,getContext(),"修改密码失败",null)) {
                    doLogin(phoneNumber, newPassword, dialog);
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    private void doLogin(final String phoneNumber, final String password, final ProgressDialog dialog) {
        boolean autoLogin = true;
        if (autoLogin) {
            dialog.setMessage("正在登录...");
            app.getApi().loginDZ(phoneNumber, password, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    //自动登录
                    dialog.dismiss();
                    if (NetUtil.checkAndHandleRsp(result,getContext(),"自动登录失败","请手动登录",null)) {
                        getFragmentStack().show(null, "main", true);
                        app.getSessionManager().onLogin(result, phoneNumber, password);
                    } else {
                        //api错误
                        getFragmentStack().show(LoginFragment.newInstance(null), "login", true);
                    }
                }
            });
        } else {
            //手动登录
            dialog.dismiss();
            getFragmentStack().show(LoginFragment.newInstance(null), "login", true);
        }
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    public static ForgetPasswordFragment newInstance() {
        ForgetPasswordFragment fragment = new ForgetPasswordFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void refreshImageCode(){
        if(img_code == null){
            return;
        }
        img_code.setImageBitmap(CodeUtils.getInstance().createBitmap());
        imageCode = CodeUtils.getInstance().getCode();
    }
}
