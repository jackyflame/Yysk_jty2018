package im.socks.yysk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.UUID;

import fr.ganfra.materialspinner.MaterialSpinner;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/11/21.
 */

public class CustomProxyEditorFragment extends Fragment implements OnBackListener {
    //参考：https://github.com/ssrbackup/shadowsocks-rss/wiki/obfs
    private static final String[] METHODS = new String[]{

            "table",
            "rc4",
            "rc4-md5",
            "rc4-md5-6",
            "aes-128-cfb",
            "aes-192-cfb",
            "aes-256-cfb",
            "aes-128-ctr",
            "aes-192-ctr",
            "aes-256-ctr",
            "bf-cfb",
            "camellia-128-cfb",
            "camellia-192-cfb",
            "camellia-256-cfb",
            "cast5-cfb",
            "des-cfb",
            "idea-cfb",
            "rc2-cfb",
            "seed-cfb",
            "salsa20",
            "chacha20",
            "chacha20-ietf"
    };
    private static final String[] PROTOCOLS = new String[]{

            "origin",
            "verify_simple",
            "verify_sha1",
            "auth_sha1",
            "auth_sha1_v2",
            "auth_sha1_v4",
            "auth_aes128_sha1",
            "auth_aes128_md5"
    };
    private static final String[] OBFS = new String[]{
            "plain",
            "http_simple",
            "http_post",
            "tls_simple",
            "tls1.2_ticket_auth"
    };
    private EditText nameView;
    private EditText hostView;
    private EditText portView;
    private EditText passwordView;

    private EditText protocolParamView;
    private EditText obfsParamView;

    private Spinner methodView;
    private Spinner protocolView;
    private Spinner obfsView;

    private Button saveView;

    private App app = Yysk.app;

    private XBean proxy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            String s = args.getString("proxy");
            proxy = Json.parse(args.getString("proxy"), XBean.class);
            MyLog.d("edit proxy=%s", proxy);

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proxy_editor, container, false);

        nameView = view.findViewById(R.id.nameView);
        hostView = view.findViewById(R.id.hostView);
        portView = view.findViewById(R.id.portView);
        passwordView = view.findViewById(R.id.passwordView);

        methodView = view.findViewById(R.id.methodView);
        protocolView = view.findViewById(R.id.protocolView);
        obfsView = view.findViewById(R.id.obfsView);

        protocolParamView = view.findViewById(R.id.protocolParamView);
        obfsParamView = view.findViewById(R.id.obfsParamView);

        saveView = view.findViewById(R.id.saveView);



        initSpinner(methodView,"method",METHODS);
        initSpinner(protocolView,"protocol",PROTOCOLS);
        initSpinner(obfsView,"obfs",OBFS);

        if (proxy != null) {
            nameView.setText(proxy.getString("name"));
            hostView.setText(proxy.getString("host"));
            portView.setText(proxy.getString("port"));
            passwordView.setText(proxy.getString("password"));

            obfsParamView.setText(proxy.getString("obfs_param"));
            protocolParamView.setText(proxy.getString("protocol_param"));

            selectItem(methodView, METHODS, proxy.getString("method", null));
            selectItem(protocolView, PROTOCOLS, proxy.getString("protocol", null));
            selectItem(obfsView, OBFS, proxy.getString("obfs", null));
        }

        saveView.setEnabled(false);
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });

        hookEditText(nameView,hostView,portView,passwordView,protocolParamView,obfsParamView);
        hookSpinner(methodView,protocolView,obfsView);


        return view;
    }
    private void hookEditText(EditText ...views){
        for(EditText view :views){
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    saveView.setEnabled(isModified());
                }
            });
        }
    }
    private void hookSpinner(Spinner ...spinners){
        for(Spinner spinner:spinners){
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    saveView.setEnabled(isModified());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    saveView.setEnabled(isModified());
                }
            });

        }
    }
    private static void selectItem(Spinner spinner, String[] items, String value) {
        if (value == null || value.isEmpty()) {
            //spinner.setSelection(0);
            return;
        }
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(value)) {
                MyLog.d("value=%s,index=%s", value, i);
                if (spinner instanceof MaterialSpinner) {
                    spinner.setSelection(i + 1);
                } else {
                    spinner.setSelection(i);
                }

                break;
            }
        }
    }

    private void doSave() {

        String id = null;
        if (this.proxy != null) {
            id = this.proxy.getString("id");
        } else {
            id = UUID.randomUUID().toString();
        }

        String name = nameView.getText().toString();
        String host = hostView.getText().toString();
        String port = portView.getText().toString();
        String password = passwordView.getText().toString();

        String protocolParam = protocolParamView.getText().toString();
        String obfsParam = obfsParamView.getText().toString();
        //下面3个方法是正确的，当使用MaterialSpinner的时候，因为内部已经调整
        String method = (String) methodView.getSelectedItem();
        String protocol = (String) protocolView.getSelectedItem();
        String obfs = (String) obfsView.getSelectedItem();


        XBean newProxy = new XBean();
        newProxy.put("id", id);
        newProxy.put("name", name);
        newProxy.put("host", host);
        newProxy.put("port", port);
        newProxy.put("password", password);
        newProxy.put("method", method);
        newProxy.put("protocol", protocol);
        newProxy.put("obfs", obfs);


        newProxy.put("obfs_param", obfsParam);
        newProxy.put("protocol_param", protocolParam);

        MyLog.d("save proxy=%s", newProxy);

        app.getCustomProxyManager().save(newProxy);

        getFragmentStack().back(false);


    }

    private boolean isModified() {
        String name = nameView.getText().toString();
        String host = hostView.getText().toString();
        String port = portView.getText().toString();
        String password = passwordView.getText().toString();

        String protocolParam = protocolParamView.getText().toString();
        String obfsParam = obfsParamView.getText().toString();
        //下面3个方法是正确的，当使用MaterialSpinner的时候，因为内部已经调整
        String method = (String) methodView.getSelectedItem();
        String protocol = (String) protocolView.getSelectedItem();
        String obfs = (String) obfsView.getSelectedItem();


        if (proxy != null) {
            return isModified(proxy,"name",name)||isModified(proxy,"host",host)||isModified(proxy,"port",port)||isModified(proxy,"method",method)||isModified(proxy,"password",password)
                    ||isModified(proxy,"protocol",protocol)||isModified(proxy,"protocol_param",protocolParam)||isModified(proxy,"obfs",obfs)||isModified(proxy,"obfs_param",obfsParam);

        } else {
            return isModified(null,name)||isModified(null,host)||isModified(null,port)||isModified(null,method)||isModified(null,password)
                    ||isModified(null,protocol)||isModified(null,protocolParam)||isModified(null,obfs)||isModified(null,obfsParam);
        }
    }
    private boolean isModified(XBean bean,String name,String newValue){
        return isModified(bean.getString(name),newValue);
    }
    private boolean isModified(String oldValue,String newValue){
        if(oldValue==newValue){
            return false;
        }
        if(newValue!=null&&newValue.isEmpty()){
            newValue=null;
        }
        if(oldValue!=null&&oldValue.isEmpty()){
            oldValue=null;
        }

        if(oldValue!=null){
            return !oldValue.equals(newValue);
        }
        return oldValue!=newValue;
    }

    private void initSpinner(Spinner spinner,String name,String []items){
        if(proxy!=null){
            String value = proxy.getString(name);
            items = mergeArray(items,value);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item,items);
        //设置下拉列表风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将适配器添加到spinner中去
        spinner.setAdapter(adapter);
    }


    private static String [] mergeArray(String []array,String value){
        if(value==null||value.isEmpty()){
            return array;
        }
        for(String a : array){
            if(a.equals(value)){
                return array;
            }
        }
        String []newArray = Arrays.copyOf(array,array.length+1);
        newArray[newArray.length-1]=value;
        return newArray;
    }

    @Override
    public boolean onBack() {
        if (!isModified()) {
            //如果没有改变，返回false，表示继续使用默认的处理，也就是关闭fragment
            return false;
        } else {
            //提示是否保存修改
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("内容已经修改，是否保存?");
            builder.setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    getFragmentStack().back(false);
                }
            });
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doSave();
                }
            });
            builder.show();
            return true;
        }
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    public static CustomProxyEditorFragment newInstance(XBean proxy) {
        CustomProxyEditorFragment fragment = new CustomProxyEditorFragment();
        Bundle args = new Bundle();
        args.putString("proxy", proxy != null ? Json.stringify(proxy) : null);
        fragment.setArguments(args);
        return fragment;
    }
}
