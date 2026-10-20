package com.sello.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sello.smartpantry.R;
import com.sello.smartpantry.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the Suggested Recipes list.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> items;
    private final Listener listener;

    public RecipeAdapter(List<Recipe> items, Listener listener) {
        this.items = new ArrayList<>(items);
        this.listener = listener;
    }

    public void setItems(List<Recipe> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = items.get(position);
        holder.tvRecipeName.setText(recipe.getName());
        holder.tvRecipeInfo.setText(recipe.getIngredients().size() + " ingredients - you have them all");
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvRecipeName;
        final TextView tvRecipeInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeInfo = itemView.findViewById(R.id.tvRecipeInfo);
        }
    }
}
