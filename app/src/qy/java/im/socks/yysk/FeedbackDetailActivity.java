package im.socks.yysk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
 * Date: 2018/4/22
 * Time: 19:19
 */
public class FeedbackDetailActivity extends AppCompatActivity {

    private PageBar title_bar;
    private TextView txv_content;
    private TextView txv_content_time;

    private LinearLayout lin_reply;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txv_content = findViewById(R.id.txv_content);
        txv_content_time = findViewById(R.id.txv_content_time);

        lin_reply = findViewById(R.id.lin_reply);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new AdapterImpl(this);
        recyclerView.setAdapter(adapter);

        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                initDate();
            }
        });
        initDate();
    }

    private void initDate(){
        long feedId = getIntent().getLongExtra("work_order_id", -1);
        if(feedId < 0){
            showError("获取反馈意见详情失败");
            finish();
        }
        app.getApi().getFeedbackDetail(feedId, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,FeedbackDetailActivity.this,"获取反馈意见详情失败",null)){
                    XBean data = NetUtil.getRspData(result);
                    refreshUI(data);
                    refreshLayout.finishRefresh(true);
                }else{
                    refreshLayout.finishRefresh(true);
                    showError("获取反馈意见详情失败");
                    finish();
                }
            }
        });
    }

    private void refreshUI(XBean data){
        txv_content.setText(data.getString("content", ""));
        String time = data.getString("created","");
        if(time != null){
            time = time.replaceAll("T", " ");
        }
        txv_content_time.setText(time);
        //获取回复列表
        List<XBean> replyList = data.getList("records");
        if(replyList == null || replyList.size() == 0){
            lin_reply.setVisibility(View.INVISIBLE);
        }else{
            lin_reply.setVisibility(View.VISIBLE);
            adapter.setItems(replyList);
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private class AdapterImpl extends RecyclerView.Adapter<ItemHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feedback_detail_list_item, viewGroup, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            holder.bind(items.get(position));
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

        private LinearLayout lin_row;
        private TextView txv_content;
        private TextView txv_time;
        private XBean data;

        public ItemHolder(View itemView) {
            super(itemView);
            lin_row = itemView.findViewById(R.id.lin_row);
            txv_content = itemView.findViewById(R.id.txv_content);
            txv_time = itemView.findViewById(R.id.txv_time);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_content.setText(data.getString("content", ""));
            String time = data.getString("created","");
            if(time != null){
                time = time.replaceAll("T", " ");
            }
            txv_time.setText(time);
        }
    }
}