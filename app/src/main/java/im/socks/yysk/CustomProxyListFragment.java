package im.socks.yysk;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import im.socks.yysk.data.Proxy;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/11/21.
 */

public class CustomProxyListFragment extends Fragment {
    private ProxyAdapter adapter;

    private final App app = Yysk.app;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxy_custom_list,container,false);
        View scanButton = view.findViewById(R.id.scanView);
        View addButton = view.findViewById(R.id.addView);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doScan();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doAdd();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));

        adapter = new ProxyAdapter(getContext());

        recyclerView.setAdapter(adapter);

        SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                adapter.refresh();
                refreshlayout.finishRefresh(true);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(adapter!=null){
            adapter.destroy();
            adapter=null;
        }
    }

    private FragmentStack getFragmentStack(){
        return ((MainActivity)getActivity()).getFragmentStack();
    }
    private void doAdd(){
        getFragmentStack().show(CustomProxyEditorFragment.newInstance(null),null,false);
    }
    private void doScan(){
        IntentIntegrator i = IntentIntegrator.forSupportFragment(this);
        i.setOrientationLocked(true);
        //仅仅需要支持二维码
        i.setDesiredBarcodeFormats(Arrays.asList(BarcodeFormat.QR_CODE.name()));
        i.setPrompt("扫描二维码");
        //i.setBarcodeImageEnabled(false);

        i.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getContext(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                XBean proxy = parseQRCode(result.getContents());
                MyLog.d("qrcode proxy=%s",proxy);
                //就可以添加到自定义的列表中的了
                if(proxy!=null){
                    app.getCustomProxyManager().save(proxy);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("二维码不正确");
                    builder.setMessage("二维码不是有效的ss或者ssr连接："+result.getContents());
                    builder.show();

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    private static XBean parseQRCode(String s) {
        MyLog.d("scan qr=%s",s);
       if(s.startsWith("ssr://")){
           return parseSSR(s);
       }else if(s.startsWith("ss://")){
           return parseSS(s);
       }else{
           return null;
       }

    }
    private static XBean parseSS(String s){
        //text=ss://base64(method:password@hostname:port)
        //参考：https://github.com/shadowsocks/shadowsocks/wiki/Generate-QR-Code-for-Android-or-iOS-Clients

        try{
            s = new String(Base64.decode(s.substring(5),Base64.DEFAULT));
        }catch(IllegalArgumentException e){
            MyLog.e(e,"base64解码失败:%s",s);
            return null;
        }
        int i= s.indexOf(':');
        int j = s.lastIndexOf('@');//password可以包含@，所以必须使用lastIndexOf
        if(i<=0||j<=0||i>=j) {
            return null;
        }
        String method = s.substring(0,i);
        String password = s.substring(i+1,j);

        j+=1;
        i = s.lastIndexOf(':');
        if(i<=0||i<=j) {
            return null;
        }
        String host = s.substring(j,i);
        String port = s.substring(i+1);

        String id = UUID.randomUUID().toString();
        return new XBean("method",method,"password",password,"host",host,"port",port,"id",id);
    }
    private static XBean parseSSR(String s){
        return new SSRParser().parse(s);
    }



    private static class SSRParser{
        public XBean parse(String s) {



            //ssr://{base64}
            s = decodeBase64(s.substring(6));
            //String s2 ="45.76.57.8:10016:origin:rc4-md5:plain:MzQ3NTM2/?remarks=576O5Zu95LyY5YyW57q/6LevWzAuM+mHkeW4gS9HXQ==&protoparam=&obfsparam=";
            int i = s.indexOf("/?");

            if(i<0) {
                i=s.length();
            }
            String s1 = s.substring(0, i);
            String s2 = i < s.length() ?  s.substring(i+2):null;

            String []parts= s1.split(":");
            if(parts.length!=6) {
                return null;
            }

            XBean proxy = new XBean();

            String id = UUID.randomUUID().toString();
            String host = parts[0];
            String port = parts[1];
            String protocol=parts[2];
            String method = parts[3];
            String obfs = parts[4];//
            String password = decodeBase64(parts[5]);

            proxy.put("id",id);
            proxy.put("host",host);
            proxy.put("port",port);
            proxy.put("protocol",protocol);
            proxy.put("method",method);
            proxy.put("obfs",obfs);
            proxy.put("password",password);

            if(s2!=null&&s2.length()>0) {
                XBean params = decodeURLParams(s2);
                //System.out.println(params);
                if(!params.isNull("remarks")) {
                    String name = decodeBase64(params.getString("remarks",""));
                    proxy.put("name",name);
                }

                proxy.put("obfs_param",params.getString("obfsparam",null));
                proxy.put("protocol_param",params.getString("protoparam",null));


            }

            return proxy;




        }

        private XBean decodeURLParams(String s){
            XBean params = new XBean();
            //解码参数
            String []parts = s.split("&");
            for(int j=0;j<parts.length;j++) {
                String []pair = parts[j].split("=");

                String name = pair[0];
                String value=pair.length==2 ? pair[1]:"";
                params.put(name, value);
//			try {
//				name = URLDecoder.decode(name, "utf-8");
//				value = URLDecoder.decode(value, "utf-8");
//
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
            }
            return params;
        }


        private String decodeBase64(String s) {
            try{
                if(s.indexOf('-')!=-1||s.indexOf('_')!=-1){
                    return new String(Base64.decode(s,Base64.URL_SAFE));
                }else{
                    return new String(Base64.decode(s,Base64.DEFAULT));
                }
            }catch(IllegalArgumentException e){
                MyLog.e(e,"base64解码失败:%s",s);
                return "";
            }


        }
    }

    private  class ProxyAdapter extends  RecyclerView.Adapter<ProxyHolder>{
        private List<XBean> items  = new ArrayList<>();
        private Context context;
        //private ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

        private EventBus.IListener eventListener = new EventBus.IListener() {
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
        private App app=Yysk.app;

        public ProxyAdapter(Context context){
            this.context = context;
            //viewBinderHelper = new ViewBinderHelper();
            //viewBinderHelper.setOpenOnlyOne(true);

            this.items = app.getCustomProxyManager().load();
            //监听事件
            app.getEventBus().on(Yysk.EVENT_ALL,eventListener);

        }
        public void refresh(){
            this.items = app.getCustomProxyManager().load();
            notifyDataSetChanged();
        }

        @Override
        public ProxyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_custom_proxy,parent,false);
            return new ProxyHolder(this,view);
        }

        @Override
        public void onBindViewHolder(ProxyHolder holder, int position) {
            holder.bind(items.get(position));
            //viewBinderHelper.bind(holder.swipeLayout,items.get(position).getString("id"));
        }


        @Override
        public int getItemCount() {
            return items.size();
        }
        //

        public void destroy(){
            app.getEventBus().un(Yysk.EVENT_ALL,eventListener);
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
        public void editProxy(XBean proxy){
            //viewBinderHelper.closeLayout(proxy.getString("id"));
            getFragmentStack().show(CustomProxyEditorFragment.newInstance(proxy),null,false);
        }

    }
    private  class ProxyHolder extends RecyclerView.ViewHolder{
        //private SwipeRevealLayout swipeLayout;
        private TextView nameView;
        //private View deleteView;
        //private View editView;
        private ProxyAdapter adapter;
        private XBean data;
        public ProxyHolder(ProxyAdapter adapter,View itemView) {
            super(itemView);
            this.adapter = adapter;
            //swipeLayout  = itemView.findViewById(R.id.swipeLayout);
            nameView = itemView.findViewById(R.id.nameView);
            //deleteView = itemView.findViewById(R.id.deleteView);
            //editView = itemView.findViewById(R.id.editView);

            nameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSelect();
                }
            });
            /*
            deleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doDelete();
                }
            });
            editView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doEdit();
                }
            });*/
        }
        public void bind(XBean data){
            this.data = data;
            String name = app.getCustomProxyManager().buildName(data);
            nameView.setText(name);

        }

        private void doSelect(){
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.context);
            builder.setItems(new String[]{"连接", "编辑","删除"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if(which==0){
                        doConnect();
                    }else if(which==1){
                        doEdit();
                    }else if(which==2){
                        doDelete();
                    }
                }
            });
            builder.show();
            /*
            if(swipeLayout.isOpened()){
                adapter.viewBinderHelper.closeLayout(data.getString("id"));
            }else{
                //执行选择

            }*/
        }
        private void doConnect(){
            Proxy newProxy = app.getCustomProxyManager().newProxy(data);
            Activity activity = getActivity();
            getFragmentStack().back();
            app.getSessionManager().setProxy(activity,newProxy,false);

        }
        private void doDelete(){
            adapter.deleteProxy(data);
        }
        private void doEdit(){
            //打开activity？这里还是使用同一个fragment
            adapter.editProxy(data);
        }
    }

    public static CustomProxyListFragment newInstance(){
        Bundle args = new Bundle();
        CustomProxyListFragment fragment = new CustomProxyListFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
