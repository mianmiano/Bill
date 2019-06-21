package com.swufe.bill.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.swufe.bill.MainActivity;
import com.swufe.bill.R;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.widget.PieChartUtils;

import org.jetbrains.annotations.NotNull;

import me.drakeet.multitype.ItemViewBinder;

public class MonthChartBillViewBinder extends ItemViewBinder<Bill,MonthChartBillViewBinder.ViewHolder> {

    private Context mContext;

    public MonthChartBillViewBinder(Context context){
        this.mContext=context;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, Bill item) {
        holder.rank.setText(holder.getAdapterPosition() + 1 + "");
        holder.title.setText(item.getCategory());
        if (item.getType()==2)
            holder.money.setText("+" + item.getAmount());
        else
            holder.money.setText("-" + item.getAmount());
        holder.root.setOnClickListener(v -> {
            new MaterialDialog.Builder(mContext)
                    .title(item.getCategory())
                    .content("\t\t" + item.getAmount().toString() + "元\n\t\t" + item.getRemark()
                            +"\n\n\t\t"+item.getDate())
//                            +"\n\t\t"+DateUtils.long2Str(bBill.getCrdate(), FORMAT_HMS_CN))
                    .positiveText("朕知道了")
                    .icon(PieChartUtils.getDrawable(item.getCategory()))
                    .limitIconToDefaultSize()
                    .show();
        });
    }

    @NotNull
    @Override
    public MonthChartBillViewBinder.ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        View root = layoutInflater.inflate(R.layout.item_recycler_monthchart_rank, viewGroup, false);
        return new ViewHolder(root);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private View root;
        private TextView title;
        private TextView money;
        private TextView rank;

        public ViewHolder(View view) {
            super(view);
            root = view;
            title = view.findViewById(R.id.title);
            money = view.findViewById(R.id.money);
            rank = view.findViewById(R.id.rank);
        }
    }
}
