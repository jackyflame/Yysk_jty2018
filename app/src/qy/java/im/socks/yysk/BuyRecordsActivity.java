package im.socks.yysk;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class BuyRecordsActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    private AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_records);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new AdapterImpl(this);
        recyclerView.setAdapter(adapter);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(true);
            }
        });

        initListData();
    }

    private void initListData() {
        app.getApi().getBuyRecord(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,BuyRecordsActivity.this,"获取购买记录失败",null)){
                    List<XBean> data = NetUtil.getRspDataList(result);
                    combineRecord(data);
                }
            }
        });
    }

    private void combineRecord(List<XBean> data){
        if(data == null || data.size() == 0){
            adapter.setItems(null);
        }else{
            ////普通套餐：amount表示金额，单位为分
            //List<XBean> leftList = new ArrayList<>();;
            //leftList.add(new XBean("flowNum", "100001", "name", "VIP包月套餐", "type", "支付宝","cost","-20元","time","2018-03-01"));
            //leftList.add(new XBean("flowNum", "100002", "name", "普通包月套餐", "type", "支付宝","cost","-10元","time","2018-03-02"));
            //leftList.add(new XBean("flowNum", "100003", "name", "SVIP包月套餐", "type", "支付宝","cost","-40元","time","2018-03-03"));
            //adapter.setItems(leftList);
            List<XBean> showList = new ArrayList<>();
            for (XBean item:data){
                XBean showItem = new XBean();
                showItem.put("flowNum",item.getString("order_no"));
                showItem.put("name",item.getString("subject"));
                String channel = item.getString("channel");
                if("alipay".equals(channel)){
                    showItem.put("type","支付宝");
                }else if("weixin".equals(channel)){
                    showItem.put("type","微信");
                }else{
                    showItem.put("type","其他");
                }
                String costStr = "-"+(item.getInteger("amount",0)/100)+"元";
                showItem.put("cost",costStr);
                showItem.put("time",item.getString("time_paid"));
                showList.add(showItem);
            }
            adapter.setItems(showList);
        }
    }

    private class AdapterImpl extends RecyclerView.Adapter<MyHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_buy_record_list_item, viewGroup, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.bind(items.get(position));
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

    private class MyHolder extends RecyclerView.ViewHolder{

        private TextView txv_num;
        private TextView txv_name;
        private TextView txv_type;
        private TextView txv_money;
        private TextView txv_time;
        private XBean data;

        public MyHolder(View itemView) {
            super(itemView);
            txv_num = itemView.findViewById(R.id.txv_num);
            txv_name = itemView.findViewById(R.id.txv_name);
            txv_type = itemView.findViewById(R.id.txv_type);
            txv_money = itemView.findViewById(R.id.txv_money);
            txv_time = itemView.findViewById(R.id.txv_time);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_num.setText(data.getString("flowNum"));
            txv_name.setText(data.getString("name"));
            txv_type.setText(data.getString("type"));
            txv_money.setText(data.getString("cost"));
            txv_time.setText(data.getString("time"));
        }
    }
}
