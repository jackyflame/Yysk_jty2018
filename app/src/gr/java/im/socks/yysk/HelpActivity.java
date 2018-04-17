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
 * Date: 2018/4/14
 * Time: 22:53
 */
public class HelpActivity extends AppCompatActivity {

    private PageBar title_bar;
    private RecyclerView recyclerView;
    private AdapterImpl adapter;
    private SmartRefreshLayout refreshLayout;

    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
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
        app.getApi().getQuestionList(new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,HelpActivity.this,"获取帮助失败",null)){
                    List<XBean> dataList = NetUtil.getRspDataList(result);
                    adapter.setItems(dataList);
                }
            }
        });
//        List<XBean> leftList = new ArrayList<>();;
//        leftList.add(new XBean("question", "Q:访问不了youtube怎么办？", "answer", "A:有两种情况，第一种您使用的是普通加速套餐，不支持视频类服务加速；第二种您正在使用的线路临时出现异常，可尝试更换线路。\n"));
//        leftList.add(new XBean("question", "Q:访问速度慢怎么办？", "answer", "A:请尝试切换线路，如您自身网络环境较差，可以切换到4G试试。"));
//        leftList.add(new XBean("question", "Q:账号到期了怎么办？", "answer", "A:请联系公司管理员。"));
//        adapter.setItems(leftList);
    }

    private class AdapterImpl extends RecyclerView.Adapter<MyHolder> {
        private Context context;
        private List<XBean> items = new ArrayList<>();

        public AdapterImpl(Context context) {
            this.context = context;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_help_list_item, viewGroup, false);
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

        private TextView txv_question;
        private TextView txv_answer;
        private XBean data;

        public MyHolder(View itemView) {
            super(itemView);
            txv_question = itemView.findViewById(R.id.txv_question);
            txv_answer = itemView.findViewById(R.id.txv_answer);
        }

        public void bind(XBean data) {
            this.data = data;
            txv_question.setText(data.getString("question"));
            txv_answer.setText(data.getString("answer"));
        }
    }

}
