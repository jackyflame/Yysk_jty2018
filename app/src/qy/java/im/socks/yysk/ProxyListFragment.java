package im.socks.yysk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.Proxy;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/24.
 */

public class ProxyListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProxyAdapter adapter;
    private TextView errorView;
    private TextView loginView;

    private SmartRefreshLayout refreshLayout;

    private final AppDZ app = Yysk.app;

    /**表示选择proxy后，为start还是reload vpn*/
    private boolean isReloadVpn = false;

    private EventBus.IListener eventListener = new EventBus.IListener() {
        @Override
        public void onEvent(String name, Object data) throws Exception {
            if (Yysk.EVENT_LOGIN.equals(name) || Yysk.EVENT_LOGOUT.equals(name)) {
                doRefresh();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            isReloadVpn = args.getBoolean("is_reload_vpn", false);
        } else {
            isReloadVpn = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxy_list, container, false);

        loginView = view.findViewById(R.id.loginView);
        errorView = view.findViewById(R.id.errorView);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        adapter = new ProxyAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        loginView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);

        loginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
            }
        });
        errorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRefresh();
            }
        });

        initRefresyLayout(view);

        List<XBean> list = app.getDzProxyManager().load();
        if(list != null && list.size() > 0){
            displayProxyList(list);
        }else{
            doRefresh();
        }

        app.getEventBus().on(Yysk.EVENT_ALL, eventListener);

        return view;
    }

    @Override
    public void onDestroyView() {
        if(adapter!=null){
            adapter.destroy();
            adapter=null;
        }
        app.getEventBus().un(Yysk.EVENT_ALL, eventListener);
        super.onDestroyView();
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    private void initRefresyLayout(View view) {
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                doRefresh();
            }
        });
    }

    private void doRefresh() {
        if (app.getSessionManager().getSession().isLogin()) {
            String phoneNumber = app.getSessionManager().getSession().user.mobile_number;
            app.getApi().getDZProxyList(phoneNumber, new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    MyLog.d("getDZProxyList=%s", result);
                    if(result != null && result.hasKeys("data")){
                        List<XBean> proxList = result.getList("data");
                        displayProxyList(proxList);
                        //保存最新列表
                        if (proxList != null) {
                            app.getDzProxyManager().save(proxList);
                        }
                    }
                }
            });
        } else {
            recyclerView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
            loginView.setVisibility(View.VISIBLE);
        }

    }

    private void displayProxyList(List<XBean> result){
        ////测试国家
        //if(result != null && result.size() > 0){
        //    result.get(0).set("title",true);
        //}
        if(result == null){
            refreshLayout.finishRefresh(true);
            errorView.setText("获得代理列表失败");
            errorView.setVisibility(View.VISIBLE);
            loginView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }else{
            List<XBean> displayList = combineProxListData(result);
            if (displayList != null && adapter != null) {
                refreshLayout.finishRefresh(true);
                errorView.setVisibility(View.GONE);
                loginView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setItems(displayList);
                Toast.makeText(getContext(),"为了获得准确的ping时间，建议先断开vpn连接",Toast.LENGTH_LONG).show();
            }
        }
    }

    private List<XBean> combineProxListData(List<XBean> resultList){
        List<XBean> displayList = new ArrayList<>();
        if(resultList == null || resultList.size() == 0){
            return displayList;
        }
        for(XBean item:resultList){
            List<XBean> nodes = item.getList("nodes");
            if(nodes == null || nodes.size() == 0){
                continue;
            }else{
                String state_region = item.getString("state_region");
                String img = item.getString("img");
                nodes.get(0).put("title",true);
                nodes.get(0).put("stateName",state_region);
                nodes.get(0).put("stateImage",img);
                displayList.addAll(nodes);
            }
        }
        return displayList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!app.getVpn().onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static ProxyListFragment newInstance(boolean isReloadVpn) {
        ProxyListFragment fragment = new ProxyListFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_reload_vpn", isReloadVpn);
        fragment.setArguments(args);
        return fragment;
    }

    private class ProxyAdapter extends RecyclerView.Adapter<ProxyHolder> {

        private List<XBean> items = new ArrayList<>();
        private Context context;
        private Ping ping=null;

        private EventBus.IListener adapterEventListener = new EventBus.IListener() {
            @Override
            public void onEvent(String name, Object data) throws Exception {
                if(Yysk.EVENT_CUSTOM_PROXY_ADD.equals(name)){
                    onAddProxy((XBean)data);
                }else if(Yysk.EVENT_CUSTOM_PROXY_REMOVE.equals(name)){
                    onRemoveProxy((XBean)data);
                }else if(Yysk.EVENT_CUSTOM_PROXY_UPDATE.equals(name)){
                    onUpdateProxy((XBean)data);
                }
            }
        };

        public ProxyAdapter(Context context) {
            this.context = context;
            //监听事件
            app.getEventBus().on(Yysk.EVENT_ALL,adapterEventListener);
        }

        @Override
        public ProxyHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_proxy_list_proxy_dz, viewGroup, false);
            return new ProxyHolder(view);
        }

        @Override
        public void onBindViewHolder(ProxyHolder proxyHolder, int i) {
            XBean item = items.get(i);
            proxyHolder.bind(item);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void setItems(List<XBean> items) {
            stopPing();
            this.items.clear();
            this.items.addAll(items);
            notifyDataSetChanged();
            startPing();
        }

        public void destroy(){
            stopPing();
            app.getEventBus().un(Yysk.EVENT_ALL,adapterEventListener);
        }

        private void stopPing(){
            if(ping!=null){
                ping.close();
                ping=null;
            }
        }

        private void startPing(){
            stopPing();
            if(items.isEmpty()){
                return;
            }
            List<String> hosts = new ArrayList<>();
            for(XBean item:items){
                String host = item.getString("host");
                hosts.add(host);
            }
            ping = new Ping();
            ping.setCount(5);
            ping.setTimeout(30);
            ping.ping(hosts, new Ping.IPingListener() {
                @Override
                public void onTime(String host, String time) {
                    updatePingTime(host,time);
                }
            });
        }

        private void updatePingTime(String host,String time){
            for(int i=0;i<items.size();i++){
                XBean item = items.get(i);
                if(item.isEquals("host",host)){
                    item.put("ping_time",time+"ms");
                    notifyItemChanged(i);
                    break;
                }
            }
        }

        public void onRemoveProxy(XBean proxy){
            int position = indexOf(proxy);
            if(position>=0){
                //viewBinderHelper.closeLayout(proxy.getString("id"));
                items.remove(position);
                notifyItemRemoved(position);
            }
        }

        public void onAddProxy(XBean proxy){
            int index = indexOf(proxy);
            if(index>=0){
                items.set(index,proxy);
                notifyItemChanged(index);
            }else{
                items.add(proxy);
                notifyItemInserted(items.size()-1);
            }
        }

        public void onUpdateProxy(XBean proxy){
            int index = indexOf(proxy);
            if(index>=0){
                items.set(index,proxy);
                notifyItemChanged(index);
            }else{
                //如果不存在了的?
                items.add(proxy);
                notifyItemInserted(items.size()-1);
            }
        }

        public void deleteProxy(XBean proxy){
            //可以先执行
            onRemoveProxy(proxy);
            //获得然后执行删除的操作
            app.getCustomProxyManager().remove(proxy);
        }

        private int indexOf(XBean proxy){
            String id = proxy.getString("id");
            for(int i=0;i<items.size();i++){
                XBean item = items.get(i);
                if(item.isEquals("id",id)){
                    return i;
                }
            }
            return -1;
        }
    }

    private class ProxyHolder extends RecyclerView.ViewHolder {

        private XBean data;

        private LinearLayout lin_title;
        private ImageView img_nation;
        private TextView txv_nation;
        private TextView txv_name;
        private TextView txv_speed;

        public ProxyHolder(View itemView) {
            super(itemView);
            init();
        }

        private void init() {
            lin_title = itemView.findViewById(R.id.lin_title);
            img_nation = itemView.findViewById(R.id.img_nation);
            txv_nation = itemView.findViewById(R.id.txv_nation);
            txv_name = itemView.findViewById(R.id.txv_name);
            txv_speed = itemView.findViewById(R.id.txv_speed);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelect();
                }
            });
        }

        public void bind(XBean data) {
            this.data = data;
            if(data.getBoolean("title",false) == true){
                lin_title.setVisibility(View.VISIBLE);
                //img_nation
                txv_nation.setText(data.getString("stateName"));
            }else{
                lin_title.setVisibility(View.GONE);
            }
            txv_name.setText(data.getString("name"));
            txv_speed.setText(data.getString("ping_time","正在测试"));
        }

        private void onSelect() {
            //发出一个事件，然后HomeFragment就可以监听到了
            Proxy proxy = new Proxy();
            proxy.name = data.getString("name");
            proxy.data = data;
            proxy.isCustom = false;
            //所在的activity需要实现onActivityResult => app.getVpn().onActivityResult()
            Activity activity = getActivity();
            getFragmentStack().back();
            app.getSessionManager().setProxy(activity, proxy, isReloadVpn);
        }
    }

}
