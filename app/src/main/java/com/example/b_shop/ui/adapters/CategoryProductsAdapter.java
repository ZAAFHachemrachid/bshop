package com.example.b_shop.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.databinding.ItemProductBinding;

public class CategoryProductsAdapter extends ListAdapter<Product, CategoryProductsAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;

    public CategoryProductsAdapter(OnProductClickListener listener) {
        super(new ProductDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        holder.bind(product);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.productName.setText(product.getName());
            binding.productPrice.setText(String.format("$%.2f", product.getPrice()));
            
            // Load product image using Picasso
            String imagePath = product.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                Picasso.get()
                    .load(imagePath)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_product_placeholder)
                    .into(binding.productImage);
            } else {
                binding.productImage.setImageResource(R.drawable.ic_product_placeholder);
            }

            binding.getRoot().setOnClickListener(v -> listener.onProductClick(product));
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getProductId() == newItem.getProductId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getPrice() == newItem.getPrice() &&
                   (oldItem.getImagePath() == null ? newItem.getImagePath() == null :
                    oldItem.getImagePath().equals(newItem.getImagePath()));
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}