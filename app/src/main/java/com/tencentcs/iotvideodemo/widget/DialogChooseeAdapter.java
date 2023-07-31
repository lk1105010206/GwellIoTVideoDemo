package com.tencentcs.iotvideodemo.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.tencentcs.iotvideodemo.R;

import java.util.List;

/**
 * Created by lele on 2019/3/19.
 */
public class DialogChooseeAdapter extends RecyclerView.Adapter<DialogChooseeAdapter.ChooseeHoler> {
    private Context context;
    private List<String> list;
    private int selectPosition = -1;

    public DialogChooseeAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChooseeHoler onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_dialog_choose_item, viewGroup, false);
        ChooseeHoler chooseeHoler = new ChooseeHoler(view);
        return chooseeHoler;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseeHoler chooseeHoler, final int i) {
        if (selectPosition == i) {
            chooseeHoler.ivSelect.setImageResource(R.drawable.icon_select);
        } else {
            chooseeHoler.ivSelect.setImageResource(R.drawable.icon_unselect);
        }
        if (!TextUtils.isEmpty(list.get(i))) {
            chooseeHoler.txName.setText(list.get(i));
        }
        chooseeHoler.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPosition = i;
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChooseeHoler extends RecyclerView.ViewHolder {
        private View view;
        private TextView txName;
        private ImageView ivSelect;

        public ChooseeHoler(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            txName = (TextView) view.findViewById(R.id.tx_name);
            ivSelect = (ImageView) view.findViewById(R.id.iv_select);
        }
    }

    public int getSelectPosition() {
        return selectPosition;
    }
}
