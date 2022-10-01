package com.mytaxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mytaxi.item.ItemGenre;
import com.mytaxi.util.PopUpAds;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.R;
import java.util.ArrayList;

public class HomeGenreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ItemGenre> dataList;
    private final Context mContext;
    private RvOnClickListener clickListener;
    private int row_index = -1;

    public HomeGenreAdapter(Context context, ArrayList<ItemGenre> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_genre_item_cander, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemGenre singleItem = dataList.get(position);
        holder.tvGenre.setText(singleItem.getGenreName());
        holder.itemLayout.setOnClickListener(v -> PopUpAds.showInterstitialAds(mContext, holder.getAdapterPosition(), clickListener));
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

    static class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView tvGenre;
        LinearLayout itemLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            itemLayout = itemView.findViewById(R.id.itemLayout);
        }
    }

}
