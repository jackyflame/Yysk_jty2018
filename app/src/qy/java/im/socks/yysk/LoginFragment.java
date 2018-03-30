package im.socks.yysk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.api.YyskDZApi;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk
 * Author: haozi
 * Date: 2018/1/2
 * Time: 17:24
 */

public class LoginFragment extends Fragment {

    private EditText phoneNumberText;
    private EditText passwordText;
    private PageBar title_bar;

    private final AppDZ app = Yysk.app;

    private String nextAction;

    private boolean isLoading = false;

    private XBean loginRst;
    private String phoneNumber;
    private String password;


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
        View view = inflater.inflate(R.layout.fragment_login_dz, container, false);

        title_bar = view.findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().back();
            }
        });
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

        if(app.getSessionManager().getSession().user != null){
            String phoneNum = app.getSessionManager().getSession().user.mobile_number;
            if(phoneNum != null){
                phoneNumberText.setText(phoneNum);
            }
        }

        return view;
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void doLogin() {
        phoneNumber = phoneNumberText.getText().toString();
        password = passwordText.getText().toString();
        if(StringUtils.isEmpty(phoneNumber)){
            showError("请输入电话号码");
            return;
        }
        if(StringUtils.isEmpty(password)){
            showError("请输入密码");
            return;
        }
        YyskDZApi api = app.getApi();
        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("正在登录...");
        dialog.show();

        api.loginDZ(phoneNumber, password, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("login=%s",result);
                dialog.dismiss();
                if (result != null) {
                    //登录成功后什么都不返回，正常的实现应该返回一个token，然后其他操作都通过这个token来调用api
                    if (result.isEquals("status_code", 0)) {
                        //缓存登录信息
                        loginRst = result.getXBean("data");
                        //保存登录状态信息
                        saveLoginRst(phoneNumber,password);
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

    private void showBindDialog(final String account, final XBean result){
        //获取绑定数据
        String terminal_num = result.getString("terminal_num");
        String binded_terminal_num = result.getString("binded_terminal_num");
        //根据绑定数据提示操作
        if(!StringUtils.isInteger(terminal_num) || !StringUtils.isInteger(binded_terminal_num)){
            new AlertDialog.Builder(getContext())
                    .setTitle("提醒")
                    .setMessage("获取绑定设备数据失败，请稍后再试")
                    .setPositiveButton("确定",null)
                    .show();
        }else{
            int terminalNum = Integer.valueOf(terminal_num);
            int bindedNum = Integer.valueOf(binded_terminal_num);
            if(terminalNum <= bindedNum){
                new AlertDialog.Builder(getContext())
                        .setTitle("提醒")
                        .setMessage("请联系您团队的管理员授权设备数，否则无法获得使用权限！")
                        .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getFragmentStack().back();
                            }
                        })
                        .show();
            }else{
                new AlertDialog.Builder(getContext())
                        .setTitle("提醒")
                        .setMessage("是否绑定该设备使用，将消耗一个设备数，您总共拥有"+terminal_num+"个设备数？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                doBindDevice(account,result);
                            }
                        })
                        .setNegativeButton("取消",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getFragmentStack().back();
                            }
                        })
                        .show();
            }
        }
    }

    private void doBindDevice(String account, XBean result){

        YyskDZApi api = app.getApi();

        final ProgressDialog dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("正在绑定...");
        dialog.show();

        api.bindDevice(account, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("bindDevice=%s",result);
                dialog.dismiss();
                if (result != null) {
                    if (result.isEquals("errorcode", "succ")) {
                        //提示成功
                        Toast.makeText(getContext(),"终端绑定成功",Toast.LENGTH_SHORT).show();
                        //保存登录状态信息
                        saveLoginRst(phoneNumber,password);
                        //返回主页面
                        getFragmentStack().back();
                    }else{
                        showError("绑定失败：" + result.getString("error"));
                    }
                } else {
                    showError("绑定失败，请检查你的本地网络是否通畅，或是服务器故障需要恢复后重新尝试");
                }
            }
        });
    }

    private void saveLoginRst(String phoneNum,String psw){
        //判断是否登录成功
        if(loginRst == null || loginRst.isEquals("retcode", "succ")){
            return;
        }
        //保存登录状态信息
        app.getSessionManager().onLogin(loginRst,phoneNum,psw);
    }
}