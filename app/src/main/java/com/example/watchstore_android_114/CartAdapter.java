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

import com.example.watchstore_android_114.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartActionListener listener;

    public interface OnCartActionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartActionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        
        holder.tvWatchName.setText(item.getWatchName());
        holder.tvWatchBrand.setText(item.getWatchBrand());
        holder.tvPrice.setText(String.format("$%.2f", item.getWatchPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvStock.setText("Available: " + item.getAvailableStock());

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncreaseQuantity(item);
            }
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecreaseQuantity(item);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(item);
            }
        });

        holder.btnIncrease.setEnabled(item.getQuantity() < item.getAvailableStock());
        
        holder.btnDecrease.setEnabled(item.getQuantity() > 1);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvWatchName, tvWatchBrand, tvPrice, tvQuantity, tvStock, tvWatchIcon;
        Button btnIncrease, btnDecrease, btnRemove;
        ImageView ivWatchImage;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWatchName = itemView.findViewById(R.id.tv_cart_watch_name);
            tvWatchBrand = itemView.findViewById(R.id.tv_cart_watch_brand);
            tvPrice = itemView.findViewById(R.id.tv_cart_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStock = itemView.findViewById(R.id.tv_cart_stock);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            ivWatchImage = itemView.findViewById(R.id.iv_cart_watch_image);
            tvWatchIcon = itemView.findViewById(R.id.tv_cart_watch_icon);
        }
    }
}
