package com.example.b_shop.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.example.b_shop.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.databinding.ItemCategoryBinding;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final OnCategoryClickListener listener;

    public CategoryAdapter(OnCategoryClickListener listener) {
        super(new CategoryDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.categoryName.setText(category.getName());
            
            // Load category image using Picasso
            String imagePath = category.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get()
                    .load(imagePath)
                    .centerCrop()
                    .into(binding.categoryImage);
            } else {
                // Load a default placeholder image from drawable resources
                binding.categoryImage.setImageResource(R.drawable.ic_category_placeholder);
            }

            binding.getRoot().setOnClickListener(v -> listener.onCategoryClick(category));
        }
    }

    private static class CategoryDiffCallback extends DiffUtil.ItemCallback<Category> {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getCategoryId() == newItem.getCategoryId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   (oldItem.getImagePath() == null ? newItem.getImagePath() == null :
                    oldItem.getImagePath().equals(newItem.getImagePath()));
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }
}