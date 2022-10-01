package com.mytaxi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mytaxi.item.ItemPlan;
import com.mytaxi.util.RvOnClickListener;
import com.mytaxi.apps.R;

import java.util.ArrayList;

public class PlanAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ItemPlan> dataList;
    private Context mContext;
    private RvOnClickListener clickListener;
    private int row_index = -1;

    public PlanAdapter(Context context, ArrayList<ItemPlan> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cander_row_select_plan, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
        final ItemRowHolder holder = (ItemRowHolder) viewHolder;
        final ItemPlan singleItem = dataList.get(position);
        holder.textPlanName.setText(singleItem.getPlanName());
        holder.textPlanPrice.setText(singleItem.getPlanPrice());
        holder.textPlanCurrency.setText(singleItem.getPlanCurrencyCode());
        holder.textPlanDuration.setText(mContext.getString(R.string.plan_day_for, singleItem.getPlanDuration()));
        holder.lytPlan.setOnClickListener(v -> clickListener.onItemClick(position));
        holder.btnProceed.setOnClickListener(view -> clickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public void setOnItemClickListener(RvOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {
        TextView textPlanName;
        TextView textPlanPrice;
        TextView textPlanDuration;
        TextView textPlanCurrency;
        TextView btnProceed;
        LinearLayout lytPlan;

        ItemRowHolder(View itemView) {
            super(itemView);
            textPlanName = itemView.findViewById(R.id.textPackName);
            textPlanPrice = itemView.findViewById(R.id.textPrice);
            textPlanDuration = itemView.findViewById(R.id.textDay);
            textPlanCurrency = itemView.findViewById(R.id.textCurrency);
            btnProceed = itemView.findViewById(R.id.btnProceed);
            lytPlan = itemView.findViewById(R.id.lytPlan);
        }
    }

}
