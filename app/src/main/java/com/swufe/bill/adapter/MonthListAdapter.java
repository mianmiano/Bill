package com.swufe.bill.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.swufe.bill.R;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthListBean;
import com.swufe.bill.widget.PieChartUtils;
import com.swufe.bill.widget.SwipeMenuView;

import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class MonthListAdapter extends StickyHeaderGridAdapter {

    private Context mContext;

    private OnStickyHeaderClickListener onStickyHeaderClickListener;

    private List<MonthListBean.DaylistBean> mDatas;

    public MonthListAdapter(Context context, List<MonthListBean.DaylistBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public void setmDatas(List<MonthListBean.DaylistBean> mDatas) {
        this.mDatas = mDatas;
    }

    public void setOnStickyHeaderClickListener(OnStickyHeaderClickListener listener) {
        if (onStickyHeaderClickListener == null)
            this.onStickyHeaderClickListener = listener;
    }

    public void remove(int section, int offset) {
        mDatas.get(section).getList().remove(offset);
        notifySectionItemRemoved(section, offset);
    }

    public void clear() {
        this.mDatas = null;
        notifyAllSectionsDataSetChanged();
    }

    @Override
    public int getSectionCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        return (mDatas == null || mDatas.get(section).getList() == null) ? 0 : mDatas.get(section).getList().size();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_monthlist_header, parent, false);
        return new MyHeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_monthlist_item, parent, false);
        return new MyItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        final MyHeaderViewHolder holder = (MyHeaderViewHolder) viewHolder;
        holder.header_date.setText(mDatas.get(section).getTime());
        holder.header_money.setText(mDatas.get(section).getMoney());
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, final int section, final int position) {
        final MyItemViewHolder holder = (MyItemViewHolder) viewHolder;

        Bill bBill = mDatas.get(section).getList().get(position);
        Log.i(TAG, "onBindItemViewHolder: bill"+position+"="+ bBill.getCategory());
        holder.item_title.setText(bBill.getCategory());
        holder.item_img.setImageDrawable(PieChartUtils.getDrawable(bBill.getCategory()));
        if (bBill.getType()==2) {
            holder.item_money.setText("+" +bBill.getAmount());
        } else {
            holder.item_money.setText("-" + bBill.getAmount());
        }
        Log.i(TAG, "onBindItemViewHolder: category="+bBill.getCategory()+" amount="+bBill.getAmount());

        //监听侧滑删除事件
        holder.item_delete.setOnClickListener(v -> {
            final int section1 = getAdapterPositionSection(holder.getAdapterPosition());
            final int offset1 = getItemSectionOffset(section1, holder.getAdapterPosition());

            //构造对话框进行确认操作
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            Log.i(TAG, "onItemLongClick: builder="+builder);
            builder.setTitle("提示").setMessage("是否删除此条记录")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.i(TAG, "onClick: 对话框事件处理");
                            //数据库删除对应数据
                            Bill bill = mDatas.get(section1).getList().get(offset1);
                            Log.i(TAG, "onClick: bill="+bill.getClass().toString()+" id="+bill.getObjectedId());
                            onDeleteClick(bill);
                            mDatas.remove(position);
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("否",null);
            builder.create().show();
        });
        //监听侧滑编辑事件
        holder.item_edit.setOnClickListener(v -> {
            final int section1 = getAdapterPositionSection(holder.getAdapterPosition());
            final int offset1 = getItemSectionOffset(section1, holder.getAdapterPosition());
//            new MaterialDialog.Builder(mContext)
//                    .title(bBill.getCategory())
//                    .content("\t\t" + bBill.getAmount().toString() + "元\n\t\t" + bBill.getRemark()
//                            +"\n\n\t\t"+bBill.getDate())
////                            +"\n\t\t"+DateUtils.long2Str(bBill.getCrdate(), FORMAT_HMS_CN))
//                    .positiveText("朕知道了")
//                    .icon(PieChartUtils.getDrawable(bBill.getCategory()))
//                    .limitIconToDefaultSize()
//                    .show();
//
//            onStickyHeaderClickListener.OnEditClick(
//                    mDatas.get(section1).getList().get(offset1), section1, offset1);
        });
        //监听单击显示详情事件
        holder.item_layout.setOnClickListener(v -> {
            new MaterialDialog.Builder(mContext)
                    .title(bBill.getCategory())
                    .content("\t\t" + bBill.getAmount().toString() + "元\n\t\t" + bBill.getRemark()
                            +"\n\n\t\t"+bBill.getDate())
//                            +"\n\t\t"+DateUtils.long2Str(bBill.getCrdate(), FORMAT_HMS_CN))
                    .positiveText("朕知道了")
                    .icon(PieChartUtils.getDrawable(bBill.getCategory()))
                    .limitIconToDefaultSize()
                    .show();
        });
    }

    /**
     * 自定义编辑、删除接口
     */
    public interface OnStickyHeaderClickListener {
        void OnDeleteClick(Bill item, int section, int offset);

        void OnEditClick(Bill item, int section, int offset);
    }

    public void onDeleteClick(Bill item){
        item.delete(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Log.i("bmob","成功");
                }else{
                    Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }

    public void OnEditClick(Bill item, int section, int offset){

    }

    public static class MyHeaderViewHolder extends HeaderViewHolder {
        TextView header_date;
        TextView header_money;

        MyHeaderViewHolder(View itemView) {
            super(itemView);
            header_date = itemView.findViewById(R.id.header_date);
            header_money = itemView.findViewById(R.id.header_money);
        }
    }

    public static class MyItemViewHolder extends ItemViewHolder {
        TextView item_title;
        TextView item_money;
        Button item_delete;
        Button item_edit;
        ImageView item_img;
        RelativeLayout item_layout;
        SwipeMenuView mSwipeMenuView;

        MyItemViewHolder(View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_money = itemView.findViewById(R.id.item_money);
            item_delete = itemView.findViewById(R.id.item_delete);
            item_edit = itemView.findViewById(R.id.item_edit);
            item_img = itemView.findViewById(R.id.item_img);
            item_layout = itemView.findViewById(R.id.item_layout);
            mSwipeMenuView = itemView.findViewById(R.id.swipe_menu);
        }
    }

//    public int getItemCount() {
//        return mDatas.size();
//    }
}