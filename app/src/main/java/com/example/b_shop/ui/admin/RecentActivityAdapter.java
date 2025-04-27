package com.example.b_shop.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b_shop.data.local.entities.UserAuditLog;
import com.example.b_shop.databinding.ItemAuditLogBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecentActivityAdapter extends ListAdapter<UserAuditLog, RecentActivityAdapter.AuditLogViewHolder> {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    
    private static final DiffUtil.ItemCallback<UserAuditLog> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserAuditLog>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserAuditLog oldItem, @NonNull UserAuditLog newItem) {
            return oldItem.getLogId() == newItem.getLogId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserAuditLog oldItem, @NonNull UserAuditLog newItem) {
            return oldItem.getLogId() == newItem.getLogId() &&
                   oldItem.getAction().equals(newItem.getAction()) &&
                   oldItem.getDetails().equals(newItem.getDetails()) &&
                   oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    public RecentActivityAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public AuditLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuditLogBinding binding = ItemAuditLogBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new AuditLogViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditLogViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class AuditLogViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuditLogBinding binding;

        public AuditLogViewHolder(@NonNull ItemAuditLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserAuditLog log) {
            binding.tvAction.setText(formatAction(log.getAction()));
            binding.tvDetails.setText(log.getDetails());
            binding.tvTimestamp.setText(formatTimestamp(log.getTimestamp()));

            // Set icon based on action type
            int iconResId = getActionIcon(log.getAction());
            binding.ivActionIcon.setImageResource(iconResId);

            // Set background color based on action type
            int bgColorResId = getActionBackground(log.getAction());
            binding.cardAction.setCardBackgroundColor(
                binding.getRoot().getContext().getColor(bgColorResId)
            );
        }

        private String formatAction(String action) {
            return action.replace("_", " ")
                       .toLowerCase(Locale.getDefault())
                       .replace("user", "User")
                       .replace("role", "Role");
        }

        private String formatTimestamp(long timestamp) {
            return DATE_FORMAT.format(new Date(timestamp * 1000L));
        }

        private int getActionIcon(String action) {
            switch (action) {
                case UserAuditLog.Actions.USER_CREATED:
                    return android.R.drawable.ic_menu_add;
                case UserAuditLog.Actions.USER_BLOCKED:
                    return android.R.drawable.ic_lock_lock;
                case UserAuditLog.Actions.USER_UNBLOCKED:
                    return android.R.drawable.ic_lock_idle_lock;
                case UserAuditLog.Actions.ROLE_CHANGED:
                    return android.R.drawable.ic_menu_manage;
                case UserAuditLog.Actions.SUSPICIOUS_ACTIVITY:
                    return android.R.drawable.ic_dialog_alert;
                default:
                    return android.R.drawable.ic_menu_info_details;
            }
        }

        private int getActionBackground(String action) {
            switch (action) {
                case UserAuditLog.Actions.USER_CREATED:
                    return android.R.color.holo_green_light;
                case UserAuditLog.Actions.USER_BLOCKED:
                    return android.R.color.holo_red_light;
                case UserAuditLog.Actions.SUSPICIOUS_ACTIVITY:
                    return android.R.color.holo_orange_light;
                default:
                    return android.R.color.white;
            }
        }
    }
}