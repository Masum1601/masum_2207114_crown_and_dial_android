package com.example.watchstore_android_114;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.AdminOrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onUpdateStatus(Order order);
    }

    public AdminOrdersAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + Math.abs(order.getId() % 10000));
        holder.tvCustomerName.setText(order.getUsername() != null ? order.getUsername() : "Unknown");
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
            int maxItems = Math.min(3, order.getItems().size());
            for (int i = 0; i < maxItems; i++) {
                Order.OrderItem item = order.getItems().get(i);
                itemsText.append("• ").append(item.getWatchName())
                        .append(" (x").append(item.getQuantity()).append(") - $")
                        .append(String.format("%.2f", item.getPrice()));
                if (i < maxItems - 1) {
                    itemsText.append("\n");
                }
            }
            if (order.getItems().size() > 3) {
                itemsText.append("\n+ ").append(order.getItems().size() - 3).append(" more items");
            }
            holder.tvOrderItemsList.setText(itemsText.toString());
            holder.tvOrderItemsList.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderItemsList.setVisibility(View.GONE);
        }

        holder.btnUpdateStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUpdateStatus(order);
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

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerName, tvOrderDate, tvOrderTotal, tvOrderStatus, 
                 tvOrderItems, tvOrderItemsList;
        Button btnUpdateStatus;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_admin_order_id);
            tvCustomerName = itemView.findViewById(R.id.tv_admin_customer_name);
            tvOrderDate = itemView.findViewById(R.id.tv_admin_order_date);
            tvOrderTotal = itemView.findViewById(R.id.tv_admin_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_admin_order_status);
            tvOrderItems = itemView.findViewById(R.id.tv_admin_order_items);
            tvOrderItemsList = itemView.findViewById(R.id.tv_admin_order_items_list);
            btnUpdateStatus = itemView.findViewById(R.id.btn_admin_update_status);
        }
    }
}
