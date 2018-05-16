package im.socks.yysk;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by cole on 2017/10/27.
 */

public class PageBar extends RelativeLayout {

    TextView titleView;
    TextView backView;

    public PageBar(Context context) {
        this(context, null);
        init(context, null, R.attr.pageBarStyle, 0);
    }

    public PageBar(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.pageBarStyle);
        init(context, attrs, R.attr.pageBarStyle, 0);
    }

    public PageBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, R.attr.pageBarStyle, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PageBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        LayoutInflater.from(context).inflate(R.layout.view_pagebar, this, true);

        titleView = findViewById(R.id.titleView);
        backView = findViewById(R.id.backView);
        TextView actionView = findViewById(R.id.actionView);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PageBar, defStyleAttr, defStyleRes);
        titleView.setText(a.getText(R.styleable.PageBar_pb_title));

        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);

        setBackgroundColor(a.getColor(R.styleable.PageBar_pb_backgroundColor, 0x000000));

        int textSize = a.getDimensionPixelSize(R.styleable.PageBar_pb_textSize, 24);//24sp?
        int backTextSize = a.getDimensionPixelSize(R.styleable.PageBar_pb_backTextSize, 22);//24sp?
        int textColor = a.getColor(R.styleable.PageBar_pb_textColor, 0xffffff);//
        int backTextColor = a.getColor(R.styleable.PageBar_pb_back_textColor, 0xffffff);
        if(backTextColor == 0xffffff && textColor != 0xffffff){
            backTextColor = textColor;
        }

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        backView.setTextSize(TypedValue.COMPLEX_UNIT_PX, backTextSize);
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        titleView.setTextColor(textColor);
        backView.setTextColor(backTextColor);
        actionView.setTextColor(textColor);

        if(a.getBoolean(R.styleable.PageBar_pb_back,false)){
            backView.setVisibility(VISIBLE);
        }
        if(a.getBoolean(R.styleable.PageBar_pb_back_show,true) == false){
            backView.setText("");
        }else{
            String backText = a.getString(R.styleable.PageBar_pb_back_text);
            if(backText != null && !backText.isEmpty()){
                backView.setText(backText);
            }else{
                backView.setText("返回");
            }
        }


        a.recycle();
    }

    public void setPbTitle(String title){
        if(titleView != null){
            titleView.setText(title);
        }
    }

    public void setBackListener(OnClickListener onClickListener){
        if(backView != null){
            backView.setOnClickListener(onClickListener);
        }
    }
}
