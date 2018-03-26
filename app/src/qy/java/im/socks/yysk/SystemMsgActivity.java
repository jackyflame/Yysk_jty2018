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

import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class SystemMsgActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_msg);
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
        //普通套餐：amount表示金额，单位为分
        List<XBean> leftList = new ArrayList<>();;
        leftList.add(new XBean("title", "包月套餐1", "time", "1分钟前", "isOpen", false));
        leftList.add(new XBean("title", "包月套餐2", "time", "1天前", "isOpen", false));
        leftList.add(new XBean("title", "包月套餐3", "time", "一周前", "isOpen", true));
        adapter.setItems(leftList);
    }

    private class AdapterImpl extends RecyclerView.Adapter<MyHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_pay_list_title_dz, viewGroup, false);
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
            this.items.addAll(items);
            notifyDataSetChanged();
        }
    }

    private class MyHolder extends RecyclerView.ViewHolder{

        private ImageView img_title;
        private TextView txv_title;
        private TextView txv_time;
        private XBean data;

        public MyHolder(View itemView) {
            super(itemView);
            img_title = itemView.findViewById(R.id.img_title);
            txv_title = itemView.findViewById(R.id.txv_title);
            txv_time = itemView.findViewById(R.id.txv_time);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_title.setText(data.getString("title"));
            txv_time.setText(data.getString("time"));
            boolean isReaded = data.getBoolean("isOpen",false);
            if(isReaded){
                txv_title.setTextColor(getResources().getColor(R.color.gray));
                img_title.setImageResource(R.mipmap.ic_msg_readed);
            }else{
                txv_title.setTextColor(getResources().getColor(R.color.blue));
                img_title.setImageResource(R.mipmap.ic_msg_unread);
            }
        }
    }
}
