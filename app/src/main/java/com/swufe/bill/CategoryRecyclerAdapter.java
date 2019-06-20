package com.swufe.bill;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.swufe.bill.bean.Bill;

import java.util.LinkedList;

class CategoryRecyclerAdapter extends RecyclerView.Adapter<CategoryViewHolder> {
    private LayoutInflater mInfiater;
    private Context mContext;

    private LinkedList<CategoryResBean> cellList = GlobalUtil.getInstance().costRes;

    public String getSelected() {
        return selected;
    }

    private String selected = "";

    public void setOnCategoryClickListener(OnCategoryClickListener onCategoryClickListener) {
        this.onCategoryClickListener = onCategoryClickListener;
    }

    private OnCategoryClickListener onCategoryClickListener;

    public CategoryRecyclerAdapter(Context context){
        this.mContext = context;
        mInfiater = LayoutInflater.from(mContext);
        selected = cellList.get(0).title;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInfiater.inflate(R.layout.cell_category,parent,false);
        CategoryViewHolder myViewHolder = new CategoryViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        final CategoryResBean res = cellList.get(position);
        holder.imageView.setImageResource(res.resBlack);
        holder.textView.setText(res.title);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selected = res.title;
                notifyDataSetChanged();

                if (onCategoryClickListener!=null){
                    onCategoryClickListener.onClick(res.title);
                }

            }
        });

        if (holder.textView.getText().toString().equals(selected)){
            holder.background.setBackgroundResource(R.drawable.bg_edit_text);
        }else {
            holder.background.setBackgroundResource(R.color.colorPrimary);
        }
    }

    public void changeType(int type){
        if (type == Bill.RECORD_TYPE_EXPENSE){
            cellList = GlobalUtil.getInstance().costRes;
        }else {
            cellList = GlobalUtil.getInstance().earnRes;
        }

        selected = cellList.get(0).title;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return cellList.size();
    }

    public interface OnCategoryClickListener{
        void onClick(String category);
    }

}

class CategoryViewHolder extends RecyclerView.ViewHolder{

    RelativeLayout background;
    ImageView imageView;
    TextView textView;

    public CategoryViewHolder(View itemView) {
        super(itemView);
        background = (RelativeLayout) itemView.findViewById(R.id.cell_background);
        imageView = (ImageView) itemView.findViewById(R.id.imageView_category);
        textView = (TextView) itemView.findViewById(R.id.textView_category);
    }
}