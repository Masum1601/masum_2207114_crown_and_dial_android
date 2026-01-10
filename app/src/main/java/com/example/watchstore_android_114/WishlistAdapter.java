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

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder> {

    private Context context;
    private List<Watch> wishlistWatches;
    private OnWishlistActionListener listener;

    public interface OnWishlistActionListener {
        void onRemoveFromWishlist(Watch watch, String wishlistItemId);
        void onViewDetails(Watch watch);
    }

    public WishlistAdapter(Context context, List<Watch> wishlistWatches, OnWishlistActionListener listener) {
        this.context = context;
        this.wishlistWatches = wishlistWatches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WishlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new WishlistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WishlistViewHolder holder, int position) {
        Watch watch = wishlistWatches.get(position);

        holder.tvWatchName.setText(watch.getName());
        holder.tvWatchBrand.setText(watch.getBrand());
        holder.tvWatchPrice.setText(String.format("$%.2f", watch.getPrice()));
        holder.tvWatchCategory.setText(watch.getCategory() != null ? watch.getCategory() : "Uncategorized");

        if (watch.getStock() > 0) {
            holder.tvStockStatus.setText("In Stock");
            holder.tvStockStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.tvStockStatus.setText("Out of Stock");
            holder.tvStockStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        if (watch.getImageUrl() != null && !watch.getImageUrl().isEmpty()) {
            holder.ivWatchImage.setVisibility(View.VISIBLE);
            holder.tvWatchIcon.setVisibility(View.GONE);
        } else {
            holder.ivWatchImage.setVisibility(View.GONE);
            holder.tvWatchIcon.setVisibility(View.VISIBLE);
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFromWishlist(watch, watch.getId());
            }
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(watch);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewDetails(watch);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wishlistWatches.size();
    }

    static class WishlistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWatchImage;
        TextView tvWatchIcon, tvWatchName, tvWatchBrand, tvWatchPrice, tvWatchCategory, tvStockStatus;
        Button btnRemove, btnViewDetails;

        public WishlistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWatchImage = itemView.findViewById(R.id.iv_wishlist_watch_image);
            tvWatchIcon = itemView.findViewById(R.id.tv_wishlist_watch_icon);
            tvWatchName = itemView.findViewById(R.id.tv_wishlist_watch_name);
            tvWatchBrand = itemView.findViewById(R.id.tv_wishlist_watch_brand);
            tvWatchPrice = itemView.findViewById(R.id.tv_wishlist_watch_price);
            tvWatchCategory = itemView.findViewById(R.id.tv_wishlist_watch_category);
            tvStockStatus = itemView.findViewById(R.id.tv_wishlist_stock_status);
            btnRemove = itemView.findViewById(R.id.btn_wishlist_remove);
            btnViewDetails = itemView.findViewById(R.id.btn_wishlist_view_details);
        }
    }
}
