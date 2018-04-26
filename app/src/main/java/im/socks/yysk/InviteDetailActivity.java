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
 * Date: 2018/4/26
 * Time: 23:06
 */
public class InviteDetailActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;
    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_detail);
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
        initListData();
    }

    private void initListData() {
        List<XBean> leftList = new ArrayList<>();;
        leftList.add(new XBean("phone", "18100000000", "is_pay", false));
        leftList.add(new XBean("phone", "18100000001", "is_pay", false));
        leftList.add(new XBean("phone", "18100000002", "is_pay", true));
        adapter.setItems(leftList);
    }

    private class AdapterImpl extends RecyclerView.Adapter<ItemHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_invitedetail_list_item, viewGroup, false);
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
        private TextView txv_phone;
        private TextView txv_statu;
        private XBean data;

        public ItemHolder(View itemView) {
            super(itemView);
            lin_row = itemView.findViewById(R.id.lin_row);
            txv_phone = itemView.findViewById(R.id.txv_phone);
            txv_statu = itemView.findViewById(R.id.txv_statu);
            lin_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });
        }

        public void bind(XBean data) {
            this.data = data;
            txv_phone.setText(data.getString("phone", ""));
            if(data.getBoolean("is_pay", false)){
                txv_statu.setText("已充值");
            }else{
                txv_statu.setText("已注册");
            }
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
