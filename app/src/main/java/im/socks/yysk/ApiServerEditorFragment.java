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
import android.widget.Button;
import android.widget.EditText;

import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/31.
 */

public class ApiServerEditorFragment extends Fragment implements OnBackListener {
    private EditText hostView;
    private EditText portView;
    private Button saveView;

    private final App app = Yysk.app;

    private String host;
    private String port;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_api_server_editor, container, false);
        hostView = view.findViewById(R.id.hostView);
        portView = view.findViewById(R.id.portView);
        saveView = view.findViewById(R.id.saveView);


        saveView.setEnabled(false);
        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });
        XBean data = app.getSettings().getData().getXBean("api_server");

        if (data != null) {
            host = data.getString("host", "");
            port = data.getString("port", "8080");
        } else {
            host = "";
            port = "8080";
        }

        hostView.setText(host);
        portView.setText(port);

        hostView.addTextChangedListener(new TextWatcher() {
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
        portView.addTextChangedListener(new TextWatcher() {
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

        return view;
    }

    @Override
    public boolean onBack() {
        if (isModified()) {
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
        } else {
            return false;
        }
    }

    private boolean isModified() {
        String newHost = hostView.getText().toString();
        String newPort = portView.getText().toString();
        return !newHost.equals(host) || !newPort.equals(port);
    }

    private void doSave() {
        //saveView.setEnabled(false);
        String host = hostView.getText().toString();
        String port = portView.getText().toString();
        //验证host
        boolean ok = true;
        if (host == null || host.isEmpty()) {
            hostView.setError("请输入正确的地址");
            ok = false;
        }
        try {
            int p = Integer.parseInt(port);
            if (p <= 0 || p > 65535) {
                portView.setText("请输入正确的端口号");
                ok = false;
            }
        } catch (NumberFormatException e) {
            portView.setText("请输入正确的端口号");
            ok = false;
        }
        if (!ok) {
            return;
        }

        XBean data = new XBean();
        data.put("host", host);
        data.put("port", port);
        app.getSettings().set("api_server", data);
        getFragmentStack().back(false);
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }


    public static ApiServerEditorFragment newInstance() {
        ApiServerEditorFragment fragment = new ApiServerEditorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

}
