package com.example.watchstore_android_114;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Watch;

import java.util.List;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.WatchViewHolder> {

    private Context context;
    private List<Watch> watchList;
    private OnWatchActionListener listener;

    public interface OnWatchActionListener {
        void onAddToCart(Watch watch);
        void onAddToWishlist(Watch watch);
        void onViewDetails(Watch watch);
    }

    public WatchAdapter(Context context, List<Watch> watchList, OnWatchActionListener listener) {
        this.context = context;
        this.watchList = watchList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_watch_card, parent, false);
        return new WatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchViewHolder holder, int position) {
        Watch watch = watchList.get(position);
        
        holder.tvName.setText(watch.getName());
        holder.tvBrand.setText(watch.getBrand());
        holder.tvPrice.setText(String.format("$%.2f", watch.getPrice()));
        
        if (watch.getStock() > 0) {
            holder.tvStock.setText("In Stock (" + watch.getStock() + ")");
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_green_light));
        } else {
            holder.tvStock.setText("Out of Stock");
            holder.tvStock.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }

        if (watch.getImageUrl() != null && !watch.getImageUrl().isEmpty()) {
            holder.ivWatchImage.setVisibility(View.VISIBLE);
            holder.tvWatchIcon.setVisibility(View.GONE);
            
            // Load image with Glide
            com.bumptech.glide.Glide.with(context)
                .load(watch.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivWatchImage);
        } else {
            holder.ivWatchImage.setVisibility(View.GONE);
            holder.tvWatchIcon.setVisibility(View.VISIBLE);
        }

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCart(watch);
            }
        });

        holder.btnAddToWishlist.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToWishlist(watch);
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(watch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return watchList.size();
    }

    static class WatchViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWatchImage;
        TextView tvWatchIcon, tvName, tvBrand, tvPrice, tvStock;
        Button btnAddToCart, btnAddToWishlist, btnViewDetails;

        public WatchViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWatchImage = itemView.findViewById(R.id.iv_watch_image);
            tvWatchIcon = itemView.findViewById(R.id.tv_watch_icon);
            tvName = itemView.findViewById(R.id.tv_watch_name);
            tvBrand = itemView.findViewById(R.id.tv_watch_brand);
            tvPrice = itemView.findViewById(R.id.tv_watch_price);
            tvStock = itemView.findViewById(R.id.tv_watch_stock);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            btnAddToWishlist = itemView.findViewById(R.id.btn_add_to_wishlist);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
