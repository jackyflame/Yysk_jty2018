package im.socks.yysk;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.CodeUtils;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;
import im.socks.yysk.util.XRspBean;
import im.socks.yysk.util.NetUtil;

/**
 * Created by cole on 2017/10/23.
 */

public class RegisterFragment extends Fragment {

    private PageBar title_bar;
    private EditText edt_inviteCode;
    private EditText phoneNumberText;
    private EditText verifyCodeText;
    private EditText passwordText;
    private Button registerButton;
    private Button sendVerifyCodeButton;

    private EditText edt_imgcode;
    private ImageView img_code;
    private String imageCode;

    private final AppDZ app = Yysk.app;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_dz, container, false);
        title_bar = view.findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().back();
            }
        });
        edt_inviteCode = view.findViewById(R.id.edt_inviteCode);
        phoneNumberText = view.findViewById(R.id.phoneNumberText);
        verifyCodeText = view.findViewById(R.id.verifyCodeText);
        passwordText = view.findViewById(R.id.passwordText);
        sendVerifyCodeButton = view.findViewById(R.id.sendVerifyCodeButton);
        registerButton = view.findViewById(R.id.registerButton);
        sendVerifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifyCode();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRegister();
            }
        });

        edt_imgcode = view.findViewById(R.id.edt_imgcode);
        img_code = view.findViewById(R.id.img_code);
        img_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshImageCode();
            }
        });
        refreshImageCode();

        TextView txv_link = view.findViewById(R.id.txv_link);
        String linkStr = "注册即代表同意<a href='https://reg.1jiasu.com/clauses'>易加速服务条款</a>和<a href='https://reg.1jiasu.com/privacy'>易加速个人隐私保护政策</a>";
        stripUnderlines(txv_link,linkStr);

        return view;
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void showError(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
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
        if(phoneNumberText.getText() == null || phoneNumberText.getText().length() <= 0){
            showError("请输电话号码");
            return;
        }
        String phoneNumber = phoneNumberText.getText().toString();
        sendVerifyCodeButton.setEnabled(false);
        app.getApi().getVerifyCode(phoneNumber,false,new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("getVerifyCode=%s",result);
                sendVerifyCodeButton.setEnabled(true);
                if(NetUtil.checkAndHandleRsp(result,getContext(),"发送验证码失败",null)){
                    showError("发送验证码成功，请查收短信");
                    //refreshImageCode();
                }else{
                    //refreshImageCode();
                }
            }
        });
    }

    private void doRegister() {
        String inviteCode = StringUtils.getTextViewStr(edt_inviteCode);
        final String phoneNumber = StringUtils.getTextViewStr(phoneNumberText);
        String verifyCode = StringUtils.getTextViewStr(verifyCodeText);
        final String password = StringUtils.getTextViewStr(passwordText);
        //if(StringUtils.isEmpty(inviteCode)){
        //    showError("请输入邀请码");
        //    return;
        //}
        if(StringUtils.isEmpty(phoneNumber)){
            showError("请输入电话号码");
            return;
        }
        if(StringUtils.isEmpty(verifyCode)){
            showError("请输入收到的验证码");
            return;
        }
        if(StringUtils.isEmpty(password)){
            showError("请设置密码");
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setMessage("正在注册...");
        dialog.show();
        app.getApi().register(phoneNumber, password, verifyCode, inviteCode, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("register=%s",result);
                if(NetUtil.checkAndHandleRsp(result,getContext(),"注册失败",null)){
                    doLogin(phoneNumber, password, dialog);
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
                    if(NetUtil.checkAndHandleRsp(result,getContext(),"自动登录失败", "请手动登录",null)){
                        app.getSessionManager().onLogin(result, phoneNumber, password);
                        getFragmentStack().show(null, "main", true);
                    } else {
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

    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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

    private void stripUnderlines(TextView textView,String content) {
        Spanned spannedHtml = Html.fromHtml(content);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        if(urls != null && urls.length > 0){
            for (URLSpan span: urls) {
                int start = clickableHtmlBuilder.getSpanStart(span);
                int end = clickableHtmlBuilder.getSpanEnd(span);
                clickableHtmlBuilder.removeSpan(span);
                span = new URLSpanNoUnderline(span.getURL());
                clickableHtmlBuilder.setSpan(span, start, end, 0);
            }
        }
        textView.setText(clickableHtmlBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }
        @Override public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
