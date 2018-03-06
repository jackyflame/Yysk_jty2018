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

/**
 * Created by cole on 2017/11/1.
 */

public class AclEditorFragment extends Fragment implements OnBackListener {
    private EditText aclView;
    private Button saveView;

    private final App app = Yysk.app;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acl_editor, container, false);
        aclView = view.findViewById(R.id.aclView);
        saveView = view.findViewById(R.id.saveView);

        aclView.setText(app.getSettings().getData().getString("acl", ""));

        aclView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                saveView.setEnabled(true);
            }
        });

        saveView.setEnabled(false);

        saveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });
        return view;
    }

    @Override
    public boolean onBack() {

        String text = aclView.getText().toString();
        String oldText = app.getSettings().getData().getString("acl", "");

        if (text.equals(oldText)) {
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

    private void doSave() {
        String text = aclView.getText().toString();
        getFragmentStack().back(false);
        app.getSettings().set("acl", text);
        app.getVpn().reload();
    }

    private FragmentStack getFragmentStack() {
        return ((MainActivity) getActivity()).getFragmentStack();
    }

    public static AclEditorFragment newInstance() {
        AclEditorFragment fragment = new AclEditorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
