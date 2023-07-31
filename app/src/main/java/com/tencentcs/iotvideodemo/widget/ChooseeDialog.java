package com.tencentcs.iotvideodemo.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencentcs.iotvideodemo.R;
import com.tencentcs.iotvideodemo.utils.Utils;

import java.util.List;

/**
 * Created by lele on 2019/3/19.
 */
public class ChooseeDialog extends Dialog{
    private Context context;
    private List<String> list;
    private String title;
    private String leftStr, rightStr;


    public ChooseeDialog(Context context, String title, List<String> list) {
        super(context);
        this.context = context;
        this.list = list;
        this.title = title;
        init();
    }

    public ChooseeDialog(Context context, String title, String leftStr, String rightStr, List<String> list) {
        super(context);
        this.context = context;
        this.list = list;
        this.title = title;
        this.leftStr = leftStr;
        this.rightStr = rightStr;
        init();
    }

    public ChooseeDialog(Context context, int theme) {
        super(context, theme);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.dialog_choose, null);
        this.setContentView(view);
        TextView txTitle = (TextView) findViewById(R.id.tx_title);
        TextView txLeft = (TextView) findViewById(R.id.tx_left);
        TextView txRight = (TextView) findViewById(R.id.tx_right);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.choosee_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        final DialogChooseeAdapter chooseeAdapter = new DialogChooseeAdapter(context, list);
        recyclerView.setAdapter(chooseeAdapter);
        if (!TextUtils.isEmpty(title)) {
            txTitle.setText(title);
        }
        if (!TextUtils.isEmpty(leftStr)) {
            txLeft.setText(leftStr);
        }
        if (!TextUtils.isEmpty(rightStr)) {
            txRight.setText(rightStr);
        }
        txLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.leftClick(view, chooseeAdapter.getSelectPosition());
                }

            }
        });
        txRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.rightClick(view, chooseeAdapter.getSelectPosition());
                }

            }
        });

        //setContentViewParas(view, 15, 200, 200, 15);
    }

    private onClickListener listener;

    public void setonClickListener(onClickListener listener) {
        this.listener = listener;
    }

    public interface onClickListener {

        void leftClick(View view, int position);

        void rightClick(View view, int position);

    }

    public void showDialg() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = 800;
        getWindow().setAttributes(lp);
    }
}
