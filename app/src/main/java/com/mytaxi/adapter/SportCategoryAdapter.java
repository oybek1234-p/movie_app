package com.mytaxi.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.item.ItemSportCategory;
import com.mytaxi.util.PopUpAds;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.R;

import java.util.ArrayList;

public class SportCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ItemSportCategory> dataList;
    private Context mContext;
    private RvOnClickListener clickListener;
    private int row_index = -1;

    public SportCategoryAdapter(Context context, ArrayList<ItemSportCategory> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_language_genre_item_cander, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemSportCategory singleItem = dataList.get(position);
        holder.tvList.setText(singleItem.getCategoryName());
        holder.itemLayout.setOnClickListener(v -> PopUpAds.showInterstitialAds(mContext, holder.getAdapterPosition(), clickListener));

        if (row_index > -1) {
            if (row_index == position) {
                holder.tvList.setTypeface(Typeface.DEFAULT_BOLD);
                holder.tvList.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            } else {
                holder.tvList.setTypeface(Typeface.DEFAULT);
                holder.tvList.setTextColor(ContextCompat.getColor(mContext, R.color.maingray));
            }
        }
    }

    public void select(int position) {
        row_index = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView tvList;
        LinearLayout itemLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            tvList = itemView.findViewById(R.id.tvList);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }
    }

}
