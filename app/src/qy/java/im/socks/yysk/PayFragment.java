package im.socks.yysk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.LinearLayout;
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
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;


public class PayFragment extends Fragment implements View.OnClickListener{

    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private LinearLayout lin_tabrow;
    private TextView btn_left;
    private TextView btn_center;
    private TextView btn_right;
    private final AppDZ app = Yysk.app;

    private List<XBean> leftList = new ArrayList<>();
    private List<XBean> centerList = new ArrayList<>();
    private List<XBean> rightList = new ArrayList<>();

    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay_dz, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        lin_tabrow = view.findViewById(R.id.lin_tabrow);
        btn_left = view.findViewById(R.id.btn_left);
        btn_left.setSelected(true);
        btn_left.setOnClickListener(this);
        btn_center = view.findViewById(R.id.btn_center);
        btn_center.setOnClickListener(this);
        btn_right = view.findViewById(R.id.btn_right);
        btn_right.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));

        //标题退出
        view.findViewById(R.id.backView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().back();
            }
        });

        view.findViewById(R.id.img_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("购买规则");
                builder.setMessage("易加速套餐黄金VIP、铂金VIP、钻石VIP为升序排列，且套餐只能平级或升级购买，平级购买是当前时间与新套餐时间的累加，升级购买会自动折算时间，折算规则如下：折算天数=升级套餐天数/升级套餐价格*当前套餐剩余天数/当前套餐天数*当前套餐价格。");
                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        view.findViewById(R.id.lin_buy_record).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),BuyRecordsActivity.class));
            }
        });

        adapter = new AdapterImpl(getActivity());
        recyclerView.setAdapter(adapter);

        initListData(null);

        SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initListData(refreshlayout);
            }
        });

        return view;
    }

    private void initListData(final RefreshLayout refreshlayout){
        if(refreshlayout == null){
            dialog = new ProgressDialog(getContext());
            dialog.setMessage("获取资费中...");
            dialog.show();
        }
        app.getApi().getOrderList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(dialog != null){
                    dialog.dismiss();
                }
                if(refreshlayout != null){
                    refreshlayout.finishRefresh(true);
                }
                if(NetUtil.checkAndHandleRsp(result,getContext(),"获取资费信息失败", null)){
                    List<XBean> datalist = NetUtil.getRspDataList(result);
                    if(datalist == null || result.size() == 0){
                        adapter.setItems(null);
                    }else{
                        lin_tabrow.setVisibility(View.VISIBLE);
                        handleDataList(datalist);
                    }
                }else{
                    StringUtils.showToast("获取资费信息失败，请稍后再试");
                }
            }
        });
    }

    private void initListTestData(){
        //普通套餐：amount表示金额，单位为分
        leftList.clear();
        leftList.add(new XBean("title", "描述：支持网页浏览加速服务。", "msg", "支持使用Google等互联网基础服务，网页浏览，包括网页搜索、社交网站等，不支持油管等视频；"));
        leftList.add(new XBean("name", "包月套餐", "price", "10元", "amount", 1000));
        leftList.add(new XBean("name", "季度套餐", "price", "27元", "amount", 2700));
        leftList.add(new XBean("name", "半年套餐", "price", "48元", "amount", 4800));
        leftList.add(new XBean("name", "年度套餐", "price", "84元", "amount", 8400));
        //VIP套餐：amount表示金额，单位为分
        centerList.clear();
        centerList.add(new XBean("title","描述：支持网页浏览与视频播放等VIP服务。", "msg", "支持：使用Google等互联网基础服务；视频类播放，譬如油管等应用；"));
        centerList.add(new XBean("name", "包月套餐", "price", "20元", "amount", 2000));
        centerList.add(new XBean("name", "季度套餐", "price", "53元", "amount", 5300));
        centerList.add(new XBean("name", "半年套餐", "price", "96元", "amount", 9600));
        centerList.add(new XBean("name", "年度套餐", "price", "180元", "amount", 18000));
        //SVIP套餐：amount表示金额，单位为分
        rightList.clear();
        rightList.add(new XBean("title", "描述：提供基于跨境物理专线的加速服务，网速太快请系好安全带。", "msg", "支持：所有境外服务，无限量使用；"));
        rightList.add(new XBean("name", "包月套餐", "price", "40元", "amount", 4000));
        rightList.add(new XBean("name", "季度套餐", "price", "114元", "amount", 11400));
        rightList.add(new XBean("name", "半年套餐", "price", "216元", "amount", 21600));
        rightList.add(new XBean("name", "年度套餐", "price", "418元", "amount", 41800));

        adapter.setItems(leftList);
    }

    private void handleDataList(List<XBean> result){
        leftList.clear();
        centerList.clear();
        rightList.clear();
        boolean isSetDefault = false;
        for (int i=0;i<result.size() && i<3;i++){
            XBean item = result.get(i);
            //检查是否设置默认选中套餐
            if(item != null && item.getBoolean("is_default",false)){
                isSetDefault = true;
            }
            if(i == 0){
                packgeOrderList(item,leftList,btn_left);
            }else if(i==1){
                packgeOrderList(item,centerList,btn_center);
            }else if(i==2){
                packgeOrderList(item,rightList,btn_right);
            }
        }
        if(isSetDefault == false){
            btn_center.performClick();
        }
    }

    private void packgeOrderList(XBean bean,List<XBean> list,TextView btn){
        if(bean == null){
            list.clear();
        }else{
            //设置按钮名字
            String packgeName = bean.getString("name");
            if(packgeName != null){
                btn.setText(packgeName);
            }
            //标题行
            XBean title = new XBean();
            title.put("title","描述：" + bean.getString("description"));
            title.put("msg",bean.getString("support"));
            list.add(title);
            //包月套餐
            XBean packgeMonth = new XBean();
            String month_name = bean.getString("monthly_name", "1个月");
            if(bean.getInteger("monthly_gift",0) > 0){
                int month = bean.getInteger("monthly_gift",0)/30;
                if(month > 0) {
                    month_name = month_name + "+送" + month + "个月";
                }
            }
            packgeMonth.put("name",month_name);
            packgeMonth.put("packgeName",packgeName);
            packgeMonth.put("id",bean.getInteger("id",-1));
            packgeMonth.put("price",bean.getString("monthly")+"元");
            packgeMonth.put("amount",bean.getFloat("monthly",0f) * 100);
            packgeMonth.put("type","monthly");
            list.add(packgeMonth);
            //季度套餐
            XBean packgeQuarter = new XBean();
            String quarter_name = bean.getString("packet_quarter_name", "3个月");
            if(bean.getInteger("packet_quarter_gift",0) > 0){
                int month = bean.getInteger("packet_quarter_gift",0)/30;
                if(month > 0) {
                    quarter_name = quarter_name + "+送" + month + "个月";
                }
            }
            packgeQuarter.put("name",quarter_name);
            packgeQuarter.put("packgeName",packgeName);
            packgeQuarter.put("id",bean.getInteger("id",-1));
            packgeQuarter.put("price",bean.getString("packet_quarter")+"元");
            packgeQuarter.put("amount",bean.getFloat("packet_quarter",0f) * 100);
            packgeQuarter.put("type","packet_quarter");
            list.add(packgeQuarter);
            //半年套餐
            XBean packgeHalfYear = new XBean();
            String halfyear_name = bean.getString("half_year_name", "6个月");
            if(bean.getInteger("half_year_gift",0) > 0){
                int month = bean.getInteger("half_year_gift",0)/30;
                if(month > 0) {
                    halfyear_name = halfyear_name + "+送" + month + "个月";
                }
            }
            packgeHalfYear.put("name",halfyear_name);
            packgeHalfYear.put("packgeName",packgeName);
            packgeHalfYear.put("id",bean.getInteger("id",-1));
            packgeHalfYear.put("price",bean.getString("half_year")+"元");
            packgeHalfYear.put("amount",bean.getFloat("half_year",0f) * 100);
            packgeHalfYear.put("type","half_year");
            list.add(packgeHalfYear);
            //年度套餐
            XBean packgeYear = new XBean();
            String year_name = bean.getString("year_name", "12个月");
            if(bean.getInteger("yearly_gift",0) > 0){
                int month = bean.getInteger("yearly_gift",0)/30;
                if(month > 0){
                    year_name = year_name+"+送"+month+"个月";
                }
            }
            packgeYear.put("name",year_name);
            packgeYear.put("packgeName",packgeName);
            packgeYear.put("id",bean.getInteger("id",-1));
            packgeYear.put("price",bean.getString("yearly")+"元");
            packgeYear.put("amount",bean.getFloat("yearly",0f) * 100);
            packgeYear.put("type","yearly");
            list.add(packgeYear);
            //设置默认选择
            boolean is_default = bean.getBoolean("is_default",false);
            if(is_default){
                btn.performClick();
            }
        }
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
     * @param data
     */
    private void onPay(Intent data) {
        String result = data.getExtras().getString("pay_result");
        String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
        String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息

        if ("success".equals(result)) {
            //支付成功
            app.getEventBus().emit(Yysk.EVENT_PAY_SUCCESS,null,false);
            //showMessage("支付成功");
            showUpdateDialog();
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

    private void showUpdateDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("提醒");
        builder.setMessage("支付成功！由于套餐更新需要，当前加速链接已断开并进行了线路更新，需要您再次手动建立连接！");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                getFragmentStack().back();
            }
        });
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_left:
                btn_left.setSelected(true);
                btn_center.setSelected(false);
                btn_right.setSelected(false);
                adapter.setItems(leftList);
                break;
            case R.id.btn_center:
                btn_left.setSelected(false);
                btn_center.setSelected(true);
                btn_right.setSelected(false);
                adapter.setItems(centerList);
                break;
            case R.id.btn_right:
                btn_left.setSelected(false);
                btn_center.setSelected(false);
                btn_right.setSelected(true);
                adapter.setItems(rightList);
                break;
        }
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

                        String phoneNumber = app.getSessionManager().getSession().user.mobile_number;
                        final String channel = which == 0 ? "wx" : "alipay";

                        //如果是微信，需要先安装，否则支付不了
                        //支付宝可以不安装，支持h5支付
                        if ("wx".equals(channel)) {
                            if (!isWeixinInstalled(getContext())) {
                                showMessage("微信未安装，不能够使用微信支付");
                                return;
                            }
                        }
                        int id = data.getInteger("id",-1);
                        int amount = data.getInteger("amount",0);
                        String subject = data.getString("packgeName");
                        String body = data.getString("name");
                        String type = data.getString("type");
                        app.getApi().createOrder(id, channel, amount, subject, body, type, new YyskApi.ICallback<XBean>() {
                            @Override
                            public void onResult(XBean result) {
                                MyLog.d("createOrder=%s",result);
                                //然后打开支付宝执行支付？ 或者调用微信执行支付
                                if (NetUtil.checkAndHandleRsp(result,getContext(),"购买失败",null)) {
                                    XBean charge = result.getXBean("data");
                                    if (charge != null) {
                                        //创建订单成功
                                        //String orderNo = charge.getString("order_no");
                                        doPay(charge);
                                    } else {
                                        showMessage("购买失败：" + result.getString("error"));
                                    }
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if(viewType == 1){
                View view = LayoutInflater.from(context).inflate(R.layout.item_pay_list_title_dz, viewGroup, false);
                return new TitleHolder(view);
            }else{
                View view = LayoutInflater.from(context).inflate(R.layout.item_pay_list_pay_dz, viewGroup, false);
                return new PayHolder(view);
            }

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
        public int getItemViewType(int position) {
            if(!items.get(position).isEmpty("title")){
                return 1;
            }
            return 0;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setItems(List<XBean> items) {
            this.items.clear();
            if(items != null){
                this.items.addAll(items);
            }
            notifyDataSetChanged();
        }
    }
}
