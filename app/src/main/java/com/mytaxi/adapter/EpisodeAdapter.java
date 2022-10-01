package com.mytaxi.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.item.ItemEpisode;
import com.mytaxi.util.PopUpAds;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.R;

import java.util.ArrayList;

public class EpisodeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ItemEpisode> dataList;
    private Context mContext;
    private RvOnClickListener clickListener;
    private int row_index = -1;
    private boolean isPurchased ;

    public EpisodeAdapter(Context context, ArrayList<ItemEpisode> dataList, boolean isPurchased) {
        this.dataList = dataList;
        this.mContext = context;
        this.isPurchased = isPurchased;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cander_row_episode_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemEpisode singleItem = dataList.get(position);
        holder.text.setText(singleItem.getEpisodeName());
        holder.rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.showInterstitialAds(mContext, holder.getAdapterPosition(), clickListener);
            }
        });

        if (row_index > -1) {
            if (row_index == position) {
                holder.text.setTextColor(mContext.getResources().getColor(R.color.highlight));
            } else {
                holder.text.setTextColor(mContext.getResources().getColor(R.color.text));
            }

        }

        if (singleItem.isDownload()) {
            if (singleItem.isPremium()) {
                if (isPurchased) {
                    holder.imageDownload.setVisibility(View.VISIBLE);
                } else {
                    holder.imageDownload.setVisibility(View.GONE);
                }
            } else {
                holder.imageDownload.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imageDownload.setVisibility(View.GONE);
        }

        holder.imageDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (singleItem.getDownloadUrl().isEmpty()) {
                    Toast.makeText(mContext, mContext.getString(R.string.download_not_found), Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        mContext.startActivity(new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(singleItem.getDownloadUrl())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Toast.makeText(mContext, mContext.getString(R.string.invalid_download), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void select(int position) {
        row_index = position;
        notifyDataSetChanged();
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView imagePlay, imageDownload;
        ConstraintLayout rootLayout;

        ItemRowHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.textEpisodes);
            imagePlay = itemView.findViewById(R.id.imageEpPlay);
            imageDownload = itemView.findViewById(R.id.imageEpDownload);
            rootLayout = itemView.findViewById(R.id.rootLayout);
        }
    }

}
