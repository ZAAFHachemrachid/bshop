package com.example.b_shop.ui.category;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.databinding.ItemCategoryBinding;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final CategoryClickListener listener;
    private long selectedCategoryId = -1;

    public CategoryAdapter(CategoryClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setSelectedCategory(long categoryId) {
        long oldSelectedId = selectedCategoryId;
        selectedCategoryId = categoryId;
        
        if (oldSelectedId != -1) {
            notifyItemChanged(getPositionForId(oldSelectedId));
        }
        if (categoryId != -1) {
            notifyItemChanged(getPositionForId(categoryId));
        }
    }

    private int getPositionForId(long categoryId) {
        for (int i = 0; i < getCurrentList().size(); i++) {
            if (getItem(i).getCategoryId() == categoryId) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
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

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(getItem(position));
                }
            });
        }

        void bind(Category category) {
            binding.categoryName.setText(category.getName());
            binding.categoryDescription.setText(category.getDescription());
            
            boolean isSelected = category.getCategoryId() == selectedCategoryId;
            binding.getRoot().setSelected(isSelected);
            binding.getRoot().setStrokeColor(isSelected ?
                binding.getRoot().getContext().getColor(com.google.android.material.R.color.design_default_color_primary) :
                android.graphics.Color.TRANSPARENT);
            binding.getRoot().setElevation(isSelected ? 8f : 2f);
            
            // TODO: Load image if category has one
            // if (category.getImageUrl() != null) {
            //     Glide.with(binding.getRoot())
            //         .load(category.getImageUrl())
            //         .into(binding.categoryImage);
            // }
        }
    }

    public interface CategoryClickListener {
        void onCategoryClick(Category category);
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = 
        new DiffUtil.ItemCallback<Category>() {
            @Override
            public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getCategoryId() == newItem.getCategoryId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getDescription().equals(newItem.getDescription());
            }
        };
}