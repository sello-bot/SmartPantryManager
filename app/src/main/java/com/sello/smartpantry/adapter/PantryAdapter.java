package com.sello.smartpantry.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sello.smartpantry.R;
import com.sello.smartpantry.model.Ingredient;
import com.sello.smartpantry.util.DateUtil;
import com.sello.smartpantry.util.Prefs;
import com.sello.smartpantry.util.UnitUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that shows pantry items.
 * It turns each Ingredient object into one row (item_pantry.xml) and reports
 * taps back to the screen through the Listener interface.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onItemClick(Ingredient item);
        void onDeleteClick(Ingredient item);
    }

    private final Context context;
    private final List<Ingredient> items;
    private final Listener listener;

    public PantryAdapter(Context context, List<Ingredient> items, Listener listener) {
        this.context = context;
        this.items = new ArrayList<>(items);
        this.listener = listener;
    }

    /** Replaces the list contents and redraws the screen. */
    public void setItems(List<Ingredient> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient item = items.get(position);

        holder.tvName.setText(item.getName());
        holder.tvQuantity.setText(UnitUtils.formatQty(item.getQuantity()) + " " + item.getUnit());

        // Rows are recycled, so always reset the expiry text colour first.
        holder.tvExpiry.setTextColor(holder.defaultExpiryColors);

        String expiry = item.getExpiryDate();
        if (expiry == null || expiry.isEmpty()) {
            holder.tvExpiry.setVisibility(View.GONE);
        } else {
            holder.tvExpiry.setVisibility(View.VISIBLE);
            String label = "Expires: " + expiry;

            if (Prefs.expiryAlertsEnabled(context)) {
                Integer days = DateUtil.daysUntil(expiry);
                if (days != null && days < 0) {
                    label += " (expired)";
                    holder.tvExpiry.setTextColor(Color.parseColor("#C62828"));
                } else if (days != null && days <= 3) {
                    label += " (expiring soon)";
                    holder.tvExpiry.setTextColor(Color.parseColor("#E65100"));
                }
            }
            holder.tvExpiry.setText(label);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the views of one row so they are looked up only once. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final View btnDelete;
        final ColorStateList defaultExpiryColors;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            defaultExpiryColors = tvExpiry.getTextColors();
        }
    }
}
