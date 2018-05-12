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
 * Time: 18:19
 */
public class FeedbackLisActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);
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
                initListData();
            }
        });

        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FeedbackLisActivity.this,FeedbackActivity.class));
            }
        });

        //initListData();
    }

    private void initListData() {
        app.getApi().getFeedbackList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,FeedbackLisActivity.this,"查询失败",null)){
                    List<XBean> dataList = NetUtil.getRspDataList(result);
                    adapter.setItems(dataList);
                    refreshLayout.finishRefresh(true);
                }else{
                    refreshLayout.finishRefresh(true);
                }
            }
        });
    }

    private class AdapterImpl extends RecyclerView.Adapter<ItemHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_feedback_list_item, viewGroup, false);
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
        private TextView txv_reply;
        private TextView txv_unread;
        private XBean data;

        public ItemHolder(View itemView) {
            super(itemView);
            lin_row = itemView.findViewById(R.id.lin_row);
            txv_content = itemView.findViewById(R.id.txv_content);
            txv_reply = itemView.findViewById(R.id.txv_reply);
            txv_unread = itemView.findViewById(R.id.txv_unread);
            lin_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long id = data.getLong("id",-1l);
                    if(id < 0){
                        showError("获取信息失败，请稍后重试");
                        return;
                    }
                    Intent intent = new Intent(FeedbackLisActivity.this,FeedbackDetailActivity.class);
                    intent.putExtra("work_order_id", id);
                    startActivity(intent);
                }
            });
        }

        public void bind(XBean data) {
            this.data = data;
            String display = data.getString("title", "");
            if(display == null || display.isEmpty()){
                display = data.getString("content", "");
            }
            txv_content.setText(display);
            if(data.getBoolean("is_handled", false)){
                txv_reply.setText("已解决");
            }else if(data.getBoolean("is_resolved", false)){
                txv_reply.setText("已回复");
            }else{
                txv_reply.setText("未回复");
            }
            if(data.getBoolean("has_new", false)){
                txv_unread.setVisibility(View.VISIBLE);
            }else{
                txv_unread.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initListData();
    }
}
