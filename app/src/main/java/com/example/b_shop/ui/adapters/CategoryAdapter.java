package com.example.b_shop.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.Category;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.databinding.ItemCategoryWithProductsBinding;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final OnCategoryClickListener categoryListener;
    private final CategoryProductsAdapter.OnProductClickListener productListener;
    private Map<Integer, List<Product>> categoryProducts;

    public CategoryAdapter(OnCategoryClickListener categoryListener, 
                         CategoryProductsAdapter.OnProductClickListener productListener) {
        super(new CategoryDiffCallback());
        this.categoryListener = categoryListener;
        this.productListener = productListener;
        this.categoryProducts = new HashMap<>();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryWithProductsBinding binding = ItemCategoryWithProductsBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = getItem(position);
        holder.bind(category);
    }

    public void setProductsForCategory(int categoryId, List<Product> products) {
        categoryProducts.put(categoryId, products);
        notifyItemChanged(getPositionForCategory(categoryId));
    }

    private int getPositionForCategory(int categoryId) {
        for (int i = 0; i < getCurrentList().size(); i++) {
            if (getCurrentList().get(i).getCategoryId() == categoryId) {
                return i;
            }
        }
        return -1;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryWithProductsBinding binding;
        private final CategoryProductsAdapter productsAdapter;

        CategoryViewHolder(ItemCategoryWithProductsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.productsAdapter = new CategoryProductsAdapter(productListener);
            binding.productsList.setAdapter(productsAdapter);
        }

        void bind(Category category) {
            binding.categoryName.setText(category.getName());
            
            // Load category image
            String imagePath = category.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get()
                    .load(imagePath)
                    .fit()
                    .centerCrop()
                    .into(binding.categoryImage);
            } else {
                binding.categoryImage.setImageResource(R.drawable.ic_category_placeholder);
            }

            // Handle products
            List<Product> products = categoryProducts.get(category.getCategoryId());
            if (products != null) {
                productsAdapter.submitList(products);
                binding.productsLoading.setVisibility(View.GONE);
                binding.productsList.setVisibility(View.VISIBLE);
            } else {
                binding.productsLoading.setVisibility(View.VISIBLE);
                binding.productsList.setVisibility(View.GONE);
            }

            binding.getRoot().setOnClickListener(v -> categoryListener.onCategoryClick(category));
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