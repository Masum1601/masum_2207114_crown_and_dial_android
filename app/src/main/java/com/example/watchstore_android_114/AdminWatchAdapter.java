package com.example.watchstore_android_114;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Watch;

import java.util.List;

public class AdminWatchAdapter extends RecyclerView.Adapter<AdminWatchAdapter.AdminWatchViewHolder> {

    private Context context;
    private List<Watch> watchList;
    private OnAdminWatchActionListener listener;

    public interface OnAdminWatchActionListener {
        void onEditWatch(Watch watch);
        void onDeleteWatch(Watch watch);
    }

    public AdminWatchAdapter(Context context, List<Watch> watchList, OnAdminWatchActionListener listener) {
        this.context = context;
        this.watchList = watchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminWatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_watch, parent, false);
        return new AdminWatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminWatchViewHolder holder, int position) {
        Watch watch = watchList.get(position);
        
        holder.tvId.setText("#" + (position + 1));
        holder.tvName.setText(watch.getName());
        holder.tvBrand.setText(watch.getBrand());
        holder.tvPrice.setText(String.format("$%.2f", watch.getPrice()));
        holder.tvStock.setText(String.valueOf(watch.getStock()));
        holder.tvCategory.setText(watch.getCategory());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditWatch(watch);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteWatch(watch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchList.size();
    }

    static class AdminWatchViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvName, tvBrand, tvPrice, tvStock, tvCategory;
        Button btnEdit, btnDelete;

        public AdminWatchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tv_admin_watch_id);
            tvName = itemView.findViewById(R.id.tv_admin_watch_name);
            tvBrand = itemView.findViewById(R.id.tv_admin_watch_brand);
            tvPrice = itemView.findViewById(R.id.tv_admin_watch_price);
            tvStock = itemView.findViewById(R.id.tv_admin_watch_stock);
            tvCategory = itemView.findViewById(R.id.tv_admin_watch_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_watch);
            btnDelete = itemView.findViewById(R.id.btn_delete_watch);
        }
    }
}
