package im.socks.yysk;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.share.wechat.WechatMoments;

public class SharePopWindow extends PopupWindow {

    private Context mContext;
    private View view;
    private TextView txv_invite_code;

    private String inviteCode;
    private ClickListener mListener;

    interface ClickListener{
        void wechatClick();
        void friendCycleClick();
        void qqClick();
    }

    public SharePopWindow(Context context,String inviteCode,ClickListener clickListener) {
        super(context);
        this.mContext = context;
        this.inviteCode = inviteCode;
        this.mListener = clickListener;
        initView();
    }

    private void initView(){
        //设置窗口视图内容
        this.view = LayoutInflater.from(mContext).inflate(R.layout.layout_share_popwindow, null);
        txv_invite_code = view.findViewById(R.id.txv_invite_code);
        txv_invite_code.setText("分享你的邀请码:"+inviteCode);
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        view.findViewById(R.id.txv_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.wechatClick();
                }
            }
        });
        view.findViewById(R.id.txv_friend_cycle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.friendCycleClick();
                }
            }
        });
        view.findViewById(R.id.txv_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    mListener.qqClick();
                }
            }
        });

        /* 设置弹出窗口特征 */
        // 设置外部可点击
        this.setOutsideTouchable(true);
//        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
//        this.view.setOnTouchListener(new View.OnTouchListener() {
//
//            public boolean onTouch(View v, MotionEvent event) {
//                int height = view.findViewById(R.id.pop_layout).getTop();
//                int y = (int) event.getY();
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    if (y < height) {
//                        dismiss();
//                    }
//                }
//                return true;
//            }
//        });
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        // 设置弹出窗体可点击
        this.setFocusable(true);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }
}
