package com.example.watchstore_android_114;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + Math.abs(order.getId() % 10000));
        holder.tvOrderDate.setText(formatDate(order.getOrderDate()));
        holder.tvOrderTotal.setText(String.format("$%.2f", order.getTotalAmount()));
        holder.tvOrderStatus.setText(order.getStatus() != null ? order.getStatus() : "Pending");

        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        holder.tvOrderItems.setText(itemCount + (itemCount == 1 ? " item" : " items"));

        String status = order.getStatus() != null ? order.getStatus() : "Pending";
        int statusColor;
        switch (status.toLowerCase()) {
            case "delivered":
                statusColor = android.R.color.holo_green_dark;
                break;
            case "shipped":
                statusColor = android.R.color.holo_blue_dark;
                break;
            case "processing":
                statusColor = android.R.color.holo_orange_dark;
                break;
            case "cancelled":
                statusColor = android.R.color.holo_red_dark;
                break;
            default:
                statusColor = android.R.color.darker_gray;
                break;
        }
        holder.tvOrderStatus.setTextColor(context.getResources().getColor(statusColor));

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            StringBuilder itemsText = new StringBuilder();
            int maxItems = Math.min(2, order.getItems().size());
            for (int i = 0; i < maxItems; i++) {
                Order.OrderItem item = order.getItems().get(i);
                itemsText.append("• ").append(item.getWatchName())
                        .append(" (x").append(item.getQuantity()).append(")");
                if (i < maxItems - 1) {
                    itemsText.append("\n");
                }
            }
            if (order.getItems().size() > 2) {
                itemsText.append("\n+ ").append(order.getItems().size() - 2).append(" more");
            }
            holder.tvOrderItemsList.setText(itemsText.toString());
            holder.tvOrderItemsList.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderItemsList.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvOrderTotal, tvOrderStatus, tvOrderItems, tvOrderItemsList;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderItems = itemView.findViewById(R.id.tv_order_items);
            tvOrderItemsList = itemView.findViewById(R.id.tv_order_items_list);
        }
    }
}
