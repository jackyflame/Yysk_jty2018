package im.socks.yysk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pingplusplus.android.Pingpp;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;


public class PayFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private TextView btn_left;
    private TextView btn_center;
    private TextView btn_right;
    private final App app = Yysk.app;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_dz, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        btn_left = view.findViewById(R.id.btn_left);
        btn_left.setSelected(true);
        btn_center = view.findViewById(R.id.btn_center);
        btn_right = view.findViewById(R.id.btn_right);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        adapter = new AdapterImpl(getActivity());
        List<XBean> items = new ArrayList<>();
        //amount表示金额，单位为分
        items.add(new XBean("name", "6金币套餐", "price", "￥6", "amount", 600));
        items.add(new XBean("name", "19金币套餐", "price", "￥18", "amount", 1800));
        items.add(new XBean("name", "32金币套餐", "price", "￥30", "amount", 3000));
        items.add(new XBean("name", "64金币套餐", "price", "￥60", "amount", 6000));
        items.add(new XBean("name", "115金币套餐", "price", "￥108", "amount", 10800));
        adapter.setItems(items);
        recyclerView.setAdapter(adapter);

        SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(true);
            }
        });

        return view;
    }


    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //支付页面返回处理
        if (requestCode == Pingpp.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                onPay(data);
            } else {
                //do nothing
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    /**
     * 处理支付后的返回
     *
     * @param data
     */
    private void onPay(Intent data) {
        String result = data.getExtras().getString("pay_result");
        String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
        String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息

        if ("success".equals(result)) {
            //支付成功
            app.getEventBus().emit(Yysk.EVENT_PAY_SUCCESS,null,false);
            showMessage("支付成功");
        } else if ("fail".equals(result)) {
            //支付失败
            app.getEventBus().emit(Yysk.EVENT_PAY_FAIL,null,false);
            showMessage("支付失败");
        } else if ("cancel".equals(result)) {
            //取消支付，不需要显示信息

        } else if ("invalid".equals(result)) {
            //支付插件未安装（一般是微信客户端未安装的情况）
            if ("wx_app_not_installed".equals(errorMsg)) {
                showMessage("微信未安装，请先安装微信");
            } else {
                showMessage("支付失败:" + errorMsg);
            }
        } else if ("unknown".equals(result)) {
            //app进程异常被杀死(一般是低内存状态下,app进程被杀死)
            showMessage("支付失败:" + errorMsg);
        } else {
            showMessage("支付返回:" + result + "," + errorMsg);
        }
    }


    private void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void showAlert2(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);
        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dialog.dismiss();

            }
        });
        builder.setCancelable(true);
        builder.show();
    }


    private static boolean isWeixinInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.tencent.mm", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.e(e);
        }
        return false;

    }


    public static PayFragment newInstance() {
        PayFragment fragment = new PayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private class PayHolder extends RecyclerView.ViewHolder {
        private XBean data;
        private RelativeLayout rl_root;
        private TextView txv_name;
        private Button btn_buy;

        public PayHolder(View itemView) {
            super(itemView);
            init();
        }

        private void init() {
            rl_root = itemView.findViewById(R.id.rl_root);
            txv_name = itemView.findViewById(R.id.txv_name);
            btn_buy = itemView.findViewById(R.id.btn_buy);
        }

        public void bind(XBean data,boolean isTail) {
            this.data = data;
            txv_name.setText(data.getString("name"));
            btn_buy.setText(data.getString("price"));
            btn_buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doBuy();
                }
            });
            if(isTail == true){
                rl_root.setBackgroundResource(R.drawable.bg_gray_border_item_tail);
            }
        }

        private void doBuy() {
            if (!app.getSessionManager().getSession().isLogin()) {
                getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("请选择支付方式");
                builder.setItems(new String[]{"微信", "支付宝"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String phoneNumber = app.getSessionManager().getSession().user.phoneNumber;
                        final String channel = which == 0 ? "wx" : "alipay";

                        //如果是微信，需要先安装，否则支付不了
                        //支付宝可以不安装，支持h5支付
                        if ("wx".equals(channel)) {
                            if (!isWeixinInstalled(getContext())) {
                                showMessage("微信未安装，不能够使用微信支付");
                                return;
                            }
                        }
                        app.getApi().createOrder(phoneNumber, channel, data.getInteger("amount"), new YyskApi.ICallback<XBean>() {
                            @Override
                            public void onResult(XBean result) {
                                MyLog.d("createOrder=%s",result);
                                //然后打开支付宝执行支付？
                                //或者调用微信执行支付
                                if (result != null) {
                                    XBean charge = result.getXBean("charge");
                                    if (charge != null) {
                                        //创建订单成功
                                        //String orderNo = charge.getString("order_no");
                                        doPay(charge);
                                    } else {
                                        showMessage("购买失败：" + result.getString("error"));
                                    }
                                } else {
                                    //
                                    showMessage("购买失败，请检查网络后再次尝试");
                                }
                            }
                        });
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        }

        private void doPay(XBean data) {
            //除QQ钱包外，其他渠道调起支付方式：
//参数一：Activity 表示当前调起支付的Activity
//参数二：data 表示获取到的charge或order的JSON字符串
            Pingpp.createPayment(PayFragment.this, Json.stringify(data));


//QQ钱包调用方式(注：调起支付时，需要签名打包成apk)
//“qwalletXXXXXXX”需与AndroidManifest.xml中的data值一致
//建议填写规则:qwallet + APP_ID
            //Pingpp.createPayment(getActivity(), Json.stringify(data), "qwalletXXXXXXX");
        }

    }

    private class TitleHolder extends RecyclerView.ViewHolder{

        private TextView txv_title;
        private TextView txv_msg;
        private XBean data;

        public TitleHolder(View itemView) {
            super(itemView);
            txv_title = itemView.findViewById(R.id.txv_title);
            txv_msg = itemView.findViewById(R.id.txv_msg);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_title.setText(data.getString("title"));
            txv_msg.setText(data.getString("msg"));
        }
    }

    private class AdapterImpl extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public PayHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_pay_list_pay_dz, viewGroup, false);
            return new PayHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof TitleHolder){
                ((TitleHolder)holder).bind(items.get(position));
            }else{
                boolean istail = position >= items.size();
                ((PayHolder)holder).bind(items.get(position),istail);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setItems(List<XBean> items) {
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
        }
    }
}
