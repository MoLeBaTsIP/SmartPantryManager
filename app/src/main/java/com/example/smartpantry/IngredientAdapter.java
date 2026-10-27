package com.example.smartpantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Custom RecyclerView adapter that shows the pantry ingredients from SQLite. */
public class IngredientAdapter
        extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    /** The activity decides what happens when a row button is tapped. */
    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> ingredients = new ArrayList<>();
    private final Listener listener;
    private boolean highlightExpiry;

    public IngredientAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replace the data and redraw the list. */
    public void setData(List<Ingredient> newIngredients, boolean highlightExpiry) {
        ingredients.clear();
        ingredients.addAll(newIngredients);
        this.highlightExpiry = highlightExpiry;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull IngredientViewHolder holder, int position) {

        Ingredient ingredient = ingredients.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(ingredient.getName());
        holder.tvInitial.setText(initialOf(ingredient.getName()));
        holder.tvQuantity.setText(context.getString(R.string.quantity_format,
                Ingredient.formatQuantity(ingredient.getQuantity()),
                ingredient.getUnit()));

        // Expiry line (optional field).
        String expiry = ingredient.getExpiryDate();
        int colour = ContextCompat.getColor(context, R.color.text_secondary);

        if (expiry == null || expiry.trim().isEmpty()) {
            holder.tvExpiry.setText(R.string.expiry_not_set);
        } else {
            holder.tvExpiry.setText(context.getString(R.string.expiry_format, expiry));

            if (highlightExpiry) {
                Date today = new Date();
                Integer days = ExpiryHelper.daysUntil(expiry, today);
                if (days != null && days < 0) {
                    holder.tvExpiry.setText(context.getString(
                            R.string.expiry_expired_format, expiry));
                    colour = ContextCompat.getColor(context, R.color.warning);
                } else if (ExpiryHelper.isExpiringSoon(expiry, today)) {
                    holder.tvExpiry.setText(context.getString(
                            R.string.expiry_soon_format, expiry));
                    colour = ContextCompat.getColor(context, R.color.warning);
                }
            }
        }
        holder.tvExpiry.setTextColor(colour);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(ingredient));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(ingredient));
        holder.itemView.setOnClickListener(v -> listener.onEdit(ingredient));
    }

    // First letter of the name for the round badge ("?" if the name is empty).
    private static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /** Holds the views of one row (item_ingredient.xml). */
    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitial;
        final TextView tvName;
        final TextView tvQuantity;
        final TextView tvExpiry;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvIngredientInitial);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvQuantity = itemView.findViewById(R.id.tvIngredientQuantity);
            tvExpiry = itemView.findViewById(R.id.tvIngredientExpiry);
            btnEdit = itemView.findViewById(R.id.btnEditIngredient);
            btnDelete = itemView.findViewById(R.id.btnDeleteIngredientRow);
        }
    }
}
