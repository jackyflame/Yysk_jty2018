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
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class DevicesActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
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

        SmartRefreshLayout refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                refreshlayout.finishRefresh(true);
            }
        });

        initListData();
    }

    private void initListData() {
        ////普通套餐：amount表示金额，单位为分
        //List<XBean> leftList = new ArrayList<>();;
        //leftList.add(new XBean("title", "同时登录设备（1台）"));
        //leftList.add(new XBean("model", "HUAWEI-P9"));
        //leftList.add(new XBean("title", "总登录设备（1台）"));
        //leftList.add(new XBean("model", "HUAWEI-P2"));
        //adapter.setItems(leftList);

        app.getApi().getDeviceList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,DevicesActivity.this,"查询设备失败",null)){
                    List<XBean> dataList = NetUtil.getRspDataList(result);
                    combineList(dataList);
                }
            }
        });
    }

    private void combineList(List<XBean> dataList){
        List<XBean> deviceActiveList = new ArrayList<>();
        List<XBean> deviceAllList = new ArrayList<>();
        for(XBean item:dataList){
            if(item.getBoolean("is_connected",false)){
                deviceActiveList.add(item);
            }
            deviceAllList.add(item);
        }
        if(deviceActiveList.size() >= 0){
            XBean title = new XBean();
            title.put("title","同时登录设备（"+deviceActiveList.size()+"台）");
            deviceActiveList.add(0,title);
        }
        if(deviceAllList.size() > 0){
            XBean title = new XBean();
            title.put("title","总登录设备（"+deviceAllList.size()+"台）");
            deviceAllList.add(0,title);
        }
        deviceActiveList.addAll(deviceAllList);
        adapter.setItems(deviceActiveList);
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
                View view = LayoutInflater.from(context).inflate(R.layout.item_device_list_title, viewGroup, false);
                return new TitleHolder(view);
            }else{
                View view = LayoutInflater.from(context).inflate(R.layout.item_device_list_item, viewGroup, false);
                return new ItemHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof TitleHolder){
                ((TitleHolder)holder).bind(items.get(position));
            }else{
                ((ItemHolder)holder).bind(items.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            XBean item = items.get(position);
            if(!item.isEmpty("title")){
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
            this.items.addAll(items);
            notifyDataSetChanged();
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder{

        private TextView txv_name;
        private XBean data;

        public ItemHolder(View itemView) {
            super(itemView);
            txv_name = itemView.findViewById(R.id.txv_name);
        }

        public void bind(XBean data) {
            this.data = data;
            String deviceName = data.getString("model");
            if(StringUtils.isEmpty(deviceName)){
                deviceName = "未知设备";
            }
            txv_name.setText(deviceName);
        }
    }

    private class TitleHolder extends RecyclerView.ViewHolder{

        private TextView txv_title;
        private XBean data;

        public TitleHolder(View itemView) {
            super(itemView);
            txv_title = itemView.findViewById(R.id.txv_title);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_title.setText(data.getString("title"));
        }
    }
}
